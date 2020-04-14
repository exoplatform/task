<template>
  <div>
    <v-menu
      v-custom-click-outside="closeMenu"
      v-model="globalMenu"
      :close-on-content-click="false"
      :nudge-left="40"
      attach
      transition="scale-transition"
      offset-y
      bottom>
      <template v-slot:activator="{ on }">
        <v-list-item :title="$t('tooltip.clickToEdit')" style="cursor: pointer;">
          <v-list-item-avatar size="22" class="mr-2 ml-0 pt-1">
            <v-img :src="getUserAvatar(task.assignee)"/>
          </v-list-item-avatar>
          <span
            v-if="task.coworker.length > 0"
            class="user-name"
            v-on="on"> + {{ task.coworker.length }} {{ $t('label.coworker') }}</span>
          <span
            v-else
            class="user-name"
            v-on="on"
            v-html="getUserFullName(task.assignee)"></span>
        </v-list-item>
      </template>
      <v-card class="pb-4">
        <v-card-text class="pb-0">
          Assigned to :
        </v-card-text>
        <v-autocomplete
          v-custom-click-outside="closeAssigneeMenu"
          ref="assigneeMenu"
          v-model="task.assignee"
          :items="users"
          hide-selected
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
              color="#578DC9"
              small>
              <v-avatar left>
                <v-img :src="getUserAvatar(item)"/>
              </v-avatar>
              <span class="pr-2">
                {{ task.assignee }}
              </span>
            </v-chip>
          </template>
          <template v-slot:item="{ index, item }">
            <v-chip
              label
              color="white"
              small>
              <v-avatar left>
                <v-img :src="getUserAvatar(item)"/>
              </v-avatar>
              {{ item }}
            </v-chip>
          </template>
        </v-autocomplete>
        <a class="ml-4" @click="assignToMe()">{{ $t('label.assignTo') }}</a>
        <v-divider class="mt-2"/>
        <v-card-text class="pb-0">
          Coworkers :
        </v-card-text>
        <v-autocomplete
          v-custom-click-outside="closeCoworkerMenu"
          id="coworkerInput"
          ref="coworkerMenu"
          v-model="task.coworker"
          :items="users"
          deletable-chips
          flat
          autofocus
          hide-selected
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
                <v-img :src="getUserAvatar(item)"/>
              </v-avatar>
              <span class="pr-2">
                {{ item }}
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
                <v-img :src="getUserAvatar(item)"/>
              </v-avatar>
              {{ item }}
            </v-chip>
          </template>
        </v-autocomplete>
        <a class="ml-4" @click="setMeAsCoworker()">{{ $t('label.addMeAsCoworker') }}</a>
      </v-card>
    </v-menu>
  </div>
</template>

<script>
  import {getAllUsers, getUserInformations, updateTask} from '../taskDrawerApi'

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
          for (let i = 0; i < this.users.length; i++) {
            this.users[i] = this.users[i].username;
          }
        })
      },
      getUserFullName(useName) {
        getUserInformations(useName).then((userInfo) => {
          this.userFullName = userInfo.fullname;
        });
        return this.userFullName;
      },
      getUserAvatar(username) {
        return `/rest/v1/social/users/${username}/avatar`;
      },
      updateTask() {
        updateTask(this.task.id, this.task);
      },
      assignToMe() {
        this.task.assignee = eXo.env.portal.userName;
        this.updateTask()
      },
      setMeAsCoworker() {
        if (!this.task.coworker.includes(eXo.env.portal.userName)) {
          this.task.coworker.push(eXo.env.portal.userName);
          this.updateTask()
        }
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