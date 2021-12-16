<template>
  <div
    v-if="showTaskChangesDrawer"
    ref="taskChangesDrawer"
    :class="showTaskChangesDrawer && 'showTaskChangesDrawer' || ''"
    class="taskCommentDrawer">
    <v-container fill-height class="pa-0">
      <v-layout column>
        <v-flex class="mx-0 drawerHeader flex-grow-0">
          <v-list-item class="pe-0">
            <v-list-item-content class="drawerTitle d-flex text-header-title text-truncate">
              <i class="uiIcon uiArrowBAckIcon" @click="closeDrawer"></i>
              <span class="ps-2">{{ $t('label.changes') }}</span>
            </v-list-item-content>
            <v-list-item-action class="drawerIcons align-end d-flex flex-row">
              <v-btn icon>
                <v-icon @click="closeDrawer()">mdi-close</v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-flex>
        <v-divider class="my-0" />
        <v-flex class="drawerContent flex-grow-1 overflow-auto border-box-sizing">
          <v-list class="py-0">
            <v-list-item
              v-for="(item, i) in logs"
              :key="i"
              class="pe-0">
              <v-list-item-content class="mt-n1">
                <div class="d-flex">
                  <exo-user-avatar
                    :username="item.author"
                    :fullname="item.authorFullName"
                    :avatar-url="item.authorAvatarUrl"
                    :size="30"
                    :url="null"
                    class="changeUserAvatar" />
                  <div class="d-flex" v-html="renderChangeHTML(item)"></div>
                </div>
                <div>
                  <div class="dateTime caption px-10 my-n3">
                    <date-format :value="item.createdTime" :format="dateTimeFormat" />
                  </div>
                </div>
              </v-list-item-content>
            </v-list-item>
          </v-list>
        </v-flex>
      </v-layout>
    </v-container>
  </div>
</template>
<script>
export default {
  props: {
    logs: {
      type: Object,
      default: () => {
        return {};
      }
    },
    task: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      showTaskChangesDrawer: false,
      dateTimeFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      },
      currentUserName: eXo.env.portal.userName,
      changeAuthor: {},
      userAvatar: '',
      avatarUrl: ''
    };
  },
  watch: {
    userAvatar() {
      this.avatarUrl = this.userAvatar;
    }
  },
  mounted() {
    this.$root.$on('displayTaskChanges', () => {
      this.showTaskChangesDrawer = true;
    });
    this.$root.$on('hideTaskChanges', () => {
      this.showTaskChangesDrawer = false;
    });
  },
  methods: {
    closeDrawer() {
      this.showTaskChangesDrawer = false;
    },
    logMsg(item) {
      return `log.${ item.actionName }`;
    },
    renderChangeHTML(item) {
      let str = '';
      if ( item.actionName === 'assign' || item.actionName === 'unassign') {
        const targetFullName = item.isTargetFullNameExternal ? `${item.targetFullName} (${this.$t('label.external')})` :  `${item.targetFullName}`;
        str = `<p class='text-truncate my-auto text-color ms-1 subtitle-2' title='${item.authorFullName} ${this.$t(this.logMsg(item))} ${targetFullName}'>` +
            `<span>${ this.$t(this.logMsg(item))}</span>`+
            `<a href='/portal/dw/profile/${item.target}'> ${targetFullName} </a>`+
            '</p>';
      } else if ( item.actionName === 'edit_project' ) {
        str = `<p class='text-truncate my-auto text-color ms-1 subtitle-2' title='${item.authorFullName} ${this.$t(this.logMsg(item))}  ${item.target}'>` +
            `<span>${ this.$t(this.logMsg(item))}</span>`+
            `<a href='#'> ${item.target} </a>`+
            '</p></p>';
      } else if ( item.actionName === 'edit_priority' ) {
        str = `<p class='text-truncate my-auto text-color ms-1 subtitle-2' title='${item.authorFullName} ${this.$t(this.logMsg(item))} ${item.task.status.priority}'> ${ this.$t(this.logMsg(item)) } ${item.task.priority}</p>`;
      } else {
        str = '<p>' +
            `<p class='text-truncate my-auto text-color ml-1' title='${item.authorFullName} ${this.$t(this.logMsg(item))} ${item.target}'> ${ this.$t(this.logMsg(item)) } ${item.target}</p>`+
          '</p>';
      }
      return str;
    },
  }
};
</script>