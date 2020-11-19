<template>
  <div>
    <v-menu
      id="assigneeMenu"
      v-model="globalMenu"
      :close-on-content-click="false"
      :nudge-left="0"
      :max-width="300"
      attach
      transition="scale-transition"
      class="taskAssignMenu"
      offset-y
      bottom>
      <template v-slot:activator="{ on }">
        <div class="d-flex align-center taskAssignItem">
          <div v-if="taskAssignee && taskAssignee.profile && taskAssignee.profile.fullname">
            <exo-user-avatar
              :username="taskAssignee.profile.username"
              :fullname="taskAssignee.profile.fullname"
              :avatar-url="taskAssignee.profile.avatar"
              :title="taskAssignee.profile.fullname"
              :size="26"/>
          </div>
          <span
            v-if="taskCoworkers.length > 0"
            class="user-name pl-2 caption font-italic lighten-2"> +{{ taskCoworkers.length }} {{ $t('label.coworker') }}
          </span>
          <a
            :class="taskAssignee && taskAssignee.profile && taskAssignee.profile.fullname && 'pl-4' || ''"
            class="taskAssignBtn mt-n1"
            v-on="on">
            <i class="uiIcon uiAddAssignIcon"></i>
            <span class="text-decoration-underline">{{ $t('label.assign') }}</span>
          </a>
        </div>
      </template>
      <v-card class="pb-4">
        <v-card-text class="pb-0">
          {{ $t('label.assignTo') }} :
        </v-card-text>
        <exo-identity-suggester
          ref="autoFocusInput3"
          :labels="suggesterLabels"
          :value="taskAssigneeObj"
          name="taskAssignee"
          type-of-relations="user_to_invite"
          height="40"
          include-users
          @removeValue="unassign"/>
        <a class="ml-4" @click="assignToMe()">{{ $t('label.assignToMe') }}</a>
        <v-divider class="mt-2"/>
        <v-card-text class="pb-0">
          {{ $t('label.coworkers') }} :
        </v-card-text>
        <exo-identity-suggester
          ref="autoFocusInput3"
          :labels="suggesterLabels"
          :value="taskCoworkerObj"
          name="taskCoworkers"
          type-of-relations="user_to_invite"
          height="40"
          include-users
          multiple/>
        <a class="ml-4" @click="setMeAsCoworker()">{{ $t('label.addMeAsCoworker') }}</a>
      </v-card>
    </v-menu>
  </div>
</template>

<script>
  import {updateTask} from '../../taskDrawerApi'

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
      }
    },
    data() {
      return {
        suggestedAssignee: [],
        suggestedCoworkers: [],
        taskAssignee: {},
        taskAssigneeObj: {},
        taskCoworkerObj:[],
        taskCoworkers: [],
        assigneeValueModel: {},
        currentUser: eXo.env.portal.userName,
        menu: false,
      }
    },
    computed: {
      suggesterLabels() {
        return {
          placeholder: this.$t('label.assignee'),
          noDataLabel: this.$t('label.noDataLabel'),
        };
      }
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
        this.taskAssignee = {};
        this.taskAssigneeObj = {};
        this.taskCoworkers = [];
        this.taskCoworkerObj = [];
        this.assigneeValueModel = {};
        if (event && event.detail) {
          const task = event.detail;
          if(task.id==null && !task.status.project) {
            this.assigneeValueModel = this.taskAssignee.identityValueSuggester;
            this.$identityService.getIdentityByProviderIdAndRemoteId('organization',this.currentUser).then(user => {
              this.taskAssignee = user;
              this.taskAssigneeObj = {
                id: `organization:${user.remoteId}`,
                remoteId: user.remoteId,
                providerId: 'organization',
                profile: {
                  fullName: user.profile.fullname,
                  avatarUrl: user.profile.avatar,
                },
              }
            })
          } else {
            if (task.coworker) {
              for (let i = 0; i < task.coworker.length; i++) {
                this.$identityService.getIdentityByProviderIdAndRemoteId('organization',task.coworker[i]).then(user => {
                  this.taskCoworkers.push(user);
                  const taskCoworker = {
                    id: `organization:${user.remoteId}`,
                    remoteId: user.remoteId,
                    providerId: 'organization',
                    profile: {
                      fullName: user.profile.fullname,
                      avatarUrl: user.profile.avatar,
                    },
                  }
                  this.taskCoworkerObj.push(taskCoworker)
                })
              }
              }
            }
            if (task.assignee) {
              this.$identityService.getIdentityByProviderIdAndRemoteId('organization',task.assignee).then(user => {
                this.taskAssignee = user;
                this.taskAssigneeObj = {
                  id: `organization:${user.remoteId}`,
                  remoteId: user.remoteId,
                  providerId: 'organization',
                  profile: {
                    fullName: user.profile.fullname,
                    avatarUrl: user.profile.avatar,
                  },
                }
              })
            }
          }
      });
    },
    methods: {
      updateTask(assignee) {
        this.task.coworker = [];
        if (typeof this.taskAssignee !== 'undefined') {
          this.task.assignee = assignee;
        } else {
          this.task.assignee = '';
        }
        for (let i = 0; i < this.taskCoworkers.length; i++) {
          this.task.coworker.push(this.taskCoworkers[i].username)
        }
        updateTask(this.task.id, this.task);
      },
      assignToMe() {
        this.$identityService.getIdentityByProviderIdAndRemoteId('organization',this.currentUser).then(user => {
          this.taskAssignee = user;
          this.taskAssigneeObj = {
            id: `organization:${user.remoteId}`,
            remoteId: user.remoteId,
            providerId: 'organization',
            profile: {
              fullName: user.profile.fullname,
              avatarUrl: user.profile.avatar,
            },
          }
        })
        this.updateTask(this.currentUser);
      },
      setMeAsCoworker() {
        if (!this.task.coworker.includes(this.currentUser)) {
          this.taskCoworkers.push(this.currentUser);
          this.$identityService.getIdentityByProviderIdAndRemoteId('organization',this.currentUser).then(user => {
            this.taskCoworkers.push(user);
            const taskCoworker = {
              id: `organization:${user.remoteId}`,
              remoteId: user.remoteId,
              providerId: 'organization',
              profile: {
                fullName: user.profile.fullname,
                avatarUrl: user.profile.avatar,
              },
            }
            this.taskCoworkerObj.push(taskCoworker)
          })
        }
        this.updateTask(this.currentUser);
      },
      unassign() {
        this.taskAssignee = undefined;
        this.updateTask();
      }
    }
  }
</script>