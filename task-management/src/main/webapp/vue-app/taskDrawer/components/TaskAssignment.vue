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
      offset-y
      bottom>
      <template v-slot:activator="{ on }">
        <v-list-item 
          :title="$t('tooltip.clickToEdit')" 
          style="cursor: pointer;" 
          class="px-0"
          @click="$emit('assignmentsOpened')">
          <v-list-item-avatar
            v-if="task.assignee"
            size="22" 
            class="mr-2 ml-0 pt-1">
            <v-img v-if="(typeof taskAssignee.username !== 'undefined')" :src="taskAssignee.avatar"/>
          </v-list-item-avatar>
          <span
            v-if="taskCoworkers.length > 0"
            class="user-name pr-2"> + {{ taskCoworkers.length }} {{ $t('label.coworker') }}</span>
          <span
            v-else-if="task.assignee"
            class="user-name pr-2"
            v-html="taskAssignee.fullname"></span>
          <a v-on="on">+ {{ $t('popup.add') }}</a>
        </v-list-item>
      </template>
      <v-card class="pb-4">
        <v-card-text class="pb-0">
          {{ $t('label.assignTo') }} :
        </v-card-text>
        <v-autocomplete
          v-custom-click-outside="closeAssigneeMenu"
          ref="assigneeMenu"
          v-model="taskAssignee"
          :items="suggestedAssignee"
          :search-input.sync="searchAssignee"
          :hide-no-data="!searchAssignee"
          flat
          no-filter
          solo
          style="width: 90%"
          chips
          class="pt-0 ml-4 assignTo"
          @change="updateTask()">
          <template v-slot:selection="{ attrs, item, parent, selected }">
            <v-chip
              v-if="(typeof item !== 'undefined')"
              v-bind="attrs"
              label
              dark
              class="pr-1"
              color="#578DC9"
              small>
              <v-avatar left>
                <v-img :src="item.avatar"/>
              </v-avatar>
              <span class="pr-2 assignee-name">
                {{ item.fullname }}
              </span>
              <v-icon
                x-small
                class="pa-0"
                @click="unassign()">close</v-icon>
            </v-chip>
          </template>
          <template v-slot:item="{ index, item }">
            <v-chip
              v-if="(typeof item !== 'undefined')"
              label
              color="white"
              small>
              <v-avatar left>
                <v-img :src="item.avatar"/>
              </v-avatar>
              {{ item.fullname }}
            </v-chip>
          </template>
        </v-autocomplete>
        <a class="ml-4" @click="assignToMe()">{{ $t('label.assignToMe') }}</a>
        <v-divider class="mt-2"/>
        <v-card-text class="pb-0">
          {{ $t('label.coworkers') }} :
        </v-card-text>
        <v-autocomplete
          v-custom-click-outside="closeCoworkerMenu"
          id="coworkerInput"
          ref="coworkerMenu"
          v-model="taskCoworkers"
          :items="suggestedCoworkers"
          :search-input.sync="searchCoworkers"
          deletable-chips
          flat
          no-filter
          multiple
          solo
          style="width: 90%"
          chips
          class="pt-0 ml-4 assignTo"
          @change="updateTask()">
          <template v-slot:selection="{ attrs, item, parent, selected }">
            <v-chip
              v-bind="attrs"
              label
              dark
              class="pr-1"
              color="#578DC9"
              small>
              <v-avatar left>
                <v-img :src="item.avatar"/>
              </v-avatar>
              <span class="assignee-name pr-2">
                {{ item.fullname }}
              </span>
              <v-icon
                x-small
                class="pa-0"
                @click="parent.selectItem(item)">close</v-icon>
            </v-chip>
          </template>
          <template v-slot:item="{ index, item }">
            <v-chip
              label
              color="white"
              small>
              <v-avatar left>
                <v-img :src="item.avatar"/>
              </v-avatar>
              {{ item.fullname }}
            </v-chip>
          </template>
        </v-autocomplete>
        <a class="ml-4" @click="setMeAsCoworker()">{{ $t('label.addMeAsCoworker') }}</a>
      </v-card>
    </v-menu>
  </div>
</template>

<script>
  import {updateTask, getSuggestedUsers, getUser} from '../taskDrawerApi'

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
        taskAssignee: '',
        taskCoworkers: [],
        currentUser: '',
        menu: false,
        searchAssignee : '',
        searchCoworkers : ''
      }
    },
    watch: {
      searchAssignee(val) {
        if (val) {
          this.suggestedAssignee = this.searchSuggestedUsers(val);
        }
      },
      searchCoworkers(val) {
        if (val) {
          this.suggestedCoworkers = this.searchSuggestedCoworkers(val);
        }
      },
      task(val) {
        if (val) {
          this.getUsers();
        }
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
      this.getUsers();
    },
    methods: {
      getUsers() {
        if (this.task.assignee) {
          getUser(this.task.assignee).then((user) => {
            this.taskAssignee = user;
            this.suggestedAssignee.push(this.taskAssignee);
          });
        }
      if (this.task.coworker) {
        for (let i = 0; i < this.task.coworker.length; i++) {
          getUser(this.task.coworker[i]).then((user) => {
            this.taskCoworkers.push(user);
            this.suggestedCoworkers.push(user);
          });
        }
      }
        getUser(eXo.env.portal.userName).then((user) => {
          this.currentUser = user;
        });
      },
      searchSuggestedUsers(query) {
        getSuggestedUsers(query, this.task.status !== null ? this.task.status.project.name : null).then((users) => {
          this.suggestedAssignee = users;
          if (this.taskAssignee && !this.suggestedAssignee.includes(this.taskAssignee)) {
            this.suggestedAssignee.push(this.taskAssignee)
          }
        })
      },
      searchSuggestedCoworkers(query) {
        getSuggestedUsers(query, this.task.status !== null ? this.task.status.project.name : null).then((users) => {
          this.suggestedCoworkers = users;
          if (this.taskCoworkers) {
            for (let i = 0; i < this.taskCoworkers.length; i++) {
              if (!this.suggestedCoworkers.includes(this.taskCoworkers[i])) {
                this.suggestedCoworkers.push(this.taskCoworkers[i])
              }
            }
          }
        })
      },
      updateTask() {
        this.task.coworker = [];
        if (typeof this.taskAssignee !== 'undefined') {
          this.task.assignee = this.taskAssignee.username;
        } else {
          this.task.assignee = '';
        }
        for (let i = 0; i < this.taskCoworkers.length; i++) {
          this.task.coworker.push(this.taskCoworkers[i].username)
        }
        updateTask(this.task.id, this.task);
      },
      assignToMe() {
        this.taskAssignee = this.currentUser;
        if (!this.suggestedAssignee.includes(this.currentUser)) {
          this.suggestedAssignee.push(this.currentUser);
        }
        this.updateTask()
      },
      setMeAsCoworker() {
        if (!this.taskCoworkers.includes(this.currentUser)) {
          this.taskCoworkers.push(this.currentUser);
          if (!this.suggestedCoworkers.includes(this.currentUser)) {
            this.suggestedCoworkers.push(this.currentUser);
          }
          this.updateTask()
        }
      },
      unassign() {
        this.taskAssignee = undefined;
        this.updateTask();
      },
      closeCoworkerMenu() {
        if (typeof this.$refs.coworkerMenu !== 'undefined') {
          this.$refs.coworkerMenu.isMenuActive = false;
        }
      },
      closeAssigneeMenu() {
        if (typeof this.$refs.assigneeMenu !== 'undefined') {
          this.$refs.assigneeMenu.isMenuActive = false;
        }
      },
    }
  }
</script>