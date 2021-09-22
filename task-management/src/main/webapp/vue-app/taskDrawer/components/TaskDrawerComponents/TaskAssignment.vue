<template>
  <div class="assigneeParent">
    <v-menu
      id="assigneeMenu"
      v-model="globalMenu"
      :content-class="menuId"
      :close-on-content-click="false"
      :nudge-left="0"
      :max-width="300"
      attach
      transition="scale-transition"
      class="taskAssignMenu"
      offset-y
      allow-overflow
      eager
      bottom>
      <template v-slot:activator="{ on }">
        <div class="d-flex align-center taskAssignItem" v-on="on">
          <div 
            v-if="taskAssigneeObj && taskAssigneeObj.profile && taskAssigneeObj.profile.fullName" 
            class="assigneeName">
            <exo-user-avatar
              :username="taskAssigneeObj.remoteId"
              :fullname="taskAssigneeFullName"
              :avatar-url="taskAssigneeObj.profile.avatarUrl"
              :title="taskAssigneeObj.profile.fullName"
              :size="24"
              :url="null"
              class="pe-2" />
          </div>
          <span
            v-if="taskCoworkers.length > 0"
            class="user-name coworkerNumber pe-2 caption font-italic lighten-2"> +{{ taskCoworkers.length }} {{ $t('label.coworker') }}
          </span>
          <a
            class="taskAssignBtn mt-n1">
            <i class="uiIcon uiAddAssignIcon"></i>
            <span class="text-decoration-underline">{{ $t('label.assign') }}</span>
          </a>
        </div>
      </template>
      <v-card class="pb-4">
        <v-card-text class="assignTaskMenu pb-0 d-flex justify-space-between">
          <span>{{ $t('label.assignTo') }} :</span>
          <a class="ms-4" @click="assignToMe()">
            <i class="uiIcon uiAssignToMeIcon"></i>
            <span>{{ $t('label.addMe') }}</span>
          </a>
        </v-card-text>
        <exo-identity-suggester
          ref="autoFocusInput3"
          :labels="suggesterLabels"
          :value="taskAssigneeObj"
          :search-options="searchOptions"
          name="taskAssignee"
          type-of-relations="''"
          height="40"
          include-users
          @input="assigneeValueChanged($event)"
          @removeValue="removeAssignee()" />
        <v-divider class="mt-4 mb-4" />
        <v-card-text class="assignTaskMenu pt-0 pb-0 d-flex justify-space-between">
          <span>{{ $t('label.coworkers') }} :</span>
          <a class="ms-4" @click="setMeAsCoworker()">
            <i class="uiIcon uiAddMeCoworkerIcon"></i>
            <span>{{ $t('label.addMe') }}</span>
          </a>
        </v-card-text>
        <exo-identity-suggester
          ref="autoFocusInput3"
          :labels="suggesterLabels"
          :value="taskCoworkerObj"
          :search-options="searchOptions"
          name="taskCoworkers"
          type-of-relations="''"
          height="40"
          include-users
          multiple
          @input="coworkerValueChanged($event)"
          @removeValue="removeCoworker($event)" />
      </v-card>
    </v-menu>
  </div>
</template>

<script>

export default {
  props: {
    task: {
      type: Object,
      default: () => {
        return {};
      }
    },
    globalMenu: {
      type: Object,
      default: () => {
        return false;
      }
    },
  },
  data() {
    return {
      suggestedAssignee: [],
      suggestedCoworkers: [],
      taskAssignee: {},
      taskAssigneeObj: {},
      taskCoworkerObj: [],
      taskCoworkers: [],
      currentUser: eXo.env.portal.userName,
      menu: false,
      menuId: `AssigneeMenu${parseInt(Math.random() * 10000)
        .toString()
        .toString()}`,
    };
  },
  computed: {
    suggesterLabels() {
      return {
        placeholder: this.$t('label.assignee'),
        noDataLabel: this.$t('label.noDataLabel'),
      };
    },
    searchOptions() {
      if (this.task && this.task.status && this.task.status.project) {
        return {
          searchUrl: '/portal/rest/projects/projectParticipants/'.concat(this.task.status.project.id).concat('/')
        };
      } else {
        return this.currentUser;
      }
    },
    taskAssigneeFullName() {
      return this.taskAssigneeObj.profile.external ? this.taskAssigneeObj.profile.fullName.concat(' (').concat(this.$t('label.external')).concat(')') : this.taskAssigneeObj.profile.fullName;
    },
  },
  mounted() {
    $(document).on('click', (e) => {
      if (e.target && !$(e.target).parents(`.${this.menuId}`).length && !$(e.target).parents('.v-autocomplete__content').length) {
        this.globalMenu = false;
      }
    });

  },
  created() {
    document.addEventListener('closeAssignments',()=> {
      if (this.globalMenu) {
        window.setTimeout(() => {
          this.globalMenu = false;
        }, 100);
      }
    });
    document.addEventListener('loadAssignee', event => {
      this.taskAssigneeObj = {};
      this.taskCoworkers = [];
      this.taskCoworkerObj = [];
      if (event && event.detail) {
        const task = event.detail;
        this.task = task;
        if (task.id==null && !task.status.project) {
          this.$identityService.getIdentityByProviderIdAndRemoteId('organization',this.currentUser).then(user => {
            this.taskAssigneeObj = {
              id: `organization:${user.remoteId}`,
              remoteId: user.remoteId,
              providerId: 'organization',
              profile: {
                fullName: user.profile.fullname,
                avatarUrl: user.profile.avatar,
                external: user.profile.dataEntity.external === 'true',
              },
            };
          });
        } else {
          if (task.coworker && task.coworker.length) {
            for (let i = 0; i < task.coworker.length; i++) {
              this.$identityService.getIdentityByProviderIdAndRemoteId('organization',task.coworker[i]).then(user => {
                this.taskCoworkers.push(user.remoteId);
                const taskCoworker = {
                  id: `organization:${user.remoteId}`,
                  remoteId: user.remoteId,
                  providerId: 'organization',
                  profile: {
                    fullName: user.profile.fullname,
                    avatarUrl: user.profile.avatar,
                    external: user.profile.dataEntity.external === 'true',
                  },
                };
                this.taskCoworkerObj.push(taskCoworker);
              });
            }
          }
        }
        if (task.assignee) {
          this.$identityService.getIdentityByProviderIdAndRemoteId('organization',task.assignee).then(user => {
            this.taskAssigneeObj = {
              id: `organization:${user.remoteId}`,
              remoteId: user.remoteId,
              providerId: 'organization',
              profile: {
                fullName: user.profile.fullname,
                avatarUrl: user.profile.avatar,
                external: user.profile.dataEntity.external === 'true',
              },
            };
          });
        }
      }
    });
  },
  methods: {
    assignToMe() {
      if ( this.task.assignee !== this.currentUser ) {
        this.$identityService.getIdentityByProviderIdAndRemoteId('organization',this.currentUser).then(user => {
          this.taskAssigneeObj = {
            id: `organization:${user.remoteId}`,
            remoteId: user.remoteId,
            providerId: 'organization',
            profile: {
              fullName: user.profile.fullname,
              avatarUrl: user.profile.avatar,
              external: user.profile.dataEntity.external === 'true',
            },
          };
        });
        this.$emit('updateTaskAssignment', this.currentUser);
      }
    },
    setMeAsCoworker() {
      if (this.task.id===null) {
        this.$identityService.getIdentityByProviderIdAndRemoteId('organization',this.currentUser).then(user => {
          this.taskCoworkers.push(this.currentUser);
          const taskCoworker = {
            id: `organization:${user.remoteId}`,
            remoteId: user.remoteId,
            providerId: 'organization',
            profile: {
              fullName: user.profile.fullname,
              avatarUrl: user.profile.avatar,
              external: user.profile.dataEntity.external === 'true',
            },
          };
          this.taskCoworkerObj.push(taskCoworker);
        }).then(() => {
          document.dispatchEvent(new CustomEvent('taskCoworkerChanged',{detail: this.taskCoworkers}));
          this.isNewTask = false;
        });
      } else {
        if (!this.taskCoworkers.includes(this.currentUser)) {
          this.$identityService.getIdentityByProviderIdAndRemoteId('organization',this.currentUser).then(user => {
            this.taskCoworkers.push(this.currentUser);
            const taskCoworker = {
              id: `organization:${user.remoteId}`,
              remoteId: user.remoteId,
              providerId: 'organization',
              profile: {
                fullName: user.profile.fullname,
                avatarUrl: user.profile.avatar,
                external: user.profile.dataEntity.external === 'true',
              },
            };
            this.taskCoworkerObj.push(taskCoworker);
          }).then(() => {
            this.$emit('updateTaskCoworker',this.taskCoworkers );
          });
        }
      }
    },
    assigneeValueChanged(value) {
      if (value && value.id) {
        if (value.remoteId !== this.currentUser && this.task.assignee !== value.remoteId) {
          this.taskAssigneeObj = value;
          this.$emit('updateTaskAssignment', value.remoteId);
        }
        else {
          if ( this.task.id ===null ) {
            this.$emit('updateTaskAssignment', this.taskAssigneeObj.remoteId);
          }
        }
      }
    },
    coworkerValueChanged(value) {
      if (typeof value !== 'undefined') {
        if (value && value.length && !this.taskCoworkers.includes(value[value.length-1].remoteId) ) {
          this.taskCoworkers.push(value[value.length-1].remoteId);
          this.$emit('updateTaskCoworker',this.taskCoworkers );
          this.taskCoworkerObj.push(value[value.length-1]);
        }
      }
    },
    removeCoworker(value) {
      this.taskCoworkers.splice(this.taskCoworkers.findIndex(taskCoworker => taskCoworker === value.remoteId), 1);
      this.$emit('updateTaskCoworker',this.taskCoworkers);
    },
    removeAssignee() {
      this.taskAssigneeObj = {};
      this.$emit('updateTaskAssignment', null);
    },
  }
};
</script>
