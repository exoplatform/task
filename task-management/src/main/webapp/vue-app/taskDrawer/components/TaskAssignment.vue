<template>
  <div>
    <v-menu
      v-custom-click-outside="closeMenu"
      id="assigneeMenu"
      v-model="globalMenu"
      :close-on-content-click="false"
      :nudge-left="40"
      attach
      transition="scale-transition"
      offset-y
      bottom>
      <template v-slot:activator="{ on }">
        <v-list-item :title="$t('tooltip.clickToEdit')" style="cursor: pointer;">
          <v-list-item-avatar
            v-if="(typeof taskAssignee !== 'undefined')"
            size="22" 
            class="mr-2 ml-0 pt-1">
            <v-img v-if="(typeof taskAssignee.username !== 'undefined')" :src="getUserAvatar(taskAssignee.username)"/>
          </v-list-item-avatar>
          <v-list-item-avatar 
            v-else 
            size="22" 
            class="mr-2 ml-0 pt-1">
            <i class="uiIconUser"></i>
          </v-list-item-avatar>
          <span
            v-if="taskCoworkers.length > 0"
            class="user-name"
            v-on="on"> + {{ taskCoworkers.length }} {{ $t('label.coworker') }}</span>
          <span
            v-else-if="taskCoworkers.length === 0 && (typeof taskAssignee === 'undefined')"
            class="user-name"
            v-on="on"> {{ $t('label.unassigned') }} </span>
          <span
            v-else
            class="user-name"
            v-on="on"
            v-html="taskAssignee.fullname"></span>
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
          :items="users"
          :filter="filterUsers"  
          flat
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
                <v-img :src="getUserAvatar(item.username)"/>
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
              label
              color="white"
              small>
              <v-avatar left>
                <v-img :src="getUserAvatar(item.username)"/>
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
          :items="users"
          :filter="filterUsers"
          deletable-chips
          flat
          autofocus
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
                <v-img :src="getUserAvatar(item.username)"/>
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
                <v-img :src="getUserAvatar(item.username)"/>
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
  import {getAllUsers, updateTask} from '../taskDrawerApi'

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
        users: [],
        taskAssignee: '',
        taskCoworkers: [],
        currentUser: '',
        userFullName: '',
        menu: false,
      }
    },
    watch: {
      globalMenu(val) {
        this.$emit('menuIsOpen',val)
      }
    },
    created() {
      this.getUsers();
    },
    methods: {
      getUsers() {
        getAllUsers().then((users) => {
          this.users = users.users;
          this.taskAssignee = this.users.find(user => user.username === this.task.assignee);
          for (let i = 0; i < this.task.coworker.length; i++) {
            this.taskCoworkers.push(this.users.find(user => user.username === this.task.coworker[i]))
          }
          this.currentUser = this.users.find(user => user.username === eXo.env.portal.userName);
        })
      },
      getUserAvatar(username) {
        return `/rest/v1/social/users/${username}/avatar`;
      },
      filterUsers(item, queryText) {
        return (
                item.fullname.toLocaleLowerCase().indexOf(queryText.toLocaleLowerCase()) >
                -1 ||
                item.fullname.toLocaleLowerCase().indexOf(queryText.toLocaleLowerCase()) > -1
        );
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
        this.updateTask()
      },
      setMeAsCoworker() {
        if (!this.taskCoworkers.includes(this.currentUser)) {
          this.taskCoworkers.push(this.currentUser);
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
      closeMenu() {
        this.globalMenu = false;
      }
    }
  }
</script>