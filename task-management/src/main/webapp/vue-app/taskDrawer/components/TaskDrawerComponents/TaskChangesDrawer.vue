<template>
  <div
    v-if="showTaskChangesDrawer"
    ref="taskChangesDrawer"
    :class="showTaskChangesDrawer && 'showTaskChangesDrawer' || ''"
    class="taskCommentDrawer">
    <v-container fill-height class="pa-0">
      <v-layout column>
        <v-flex class="mx-0 drawerHeader flex-grow-0">
          <v-list-item class="pr-0">
            <v-list-item-content class="drawerTitle d-flex text-header-title text-truncate">
              <i class="uiIcon uiArrowBAckIcon" @click="closeDrawer"></i>
              <span class="pl-2">{{ $t('label.changes') }}</span>
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
              class="pl-0 pr-0">
              <v-list-item-content class="pt-1">
                <div class="d-flex">
                  <exo-user-avatar
                    :username="item.author"
                    :fullname="item.authorFullName"
                    :size="30"
                    :url="null"
                    class="changeUserAvatar"/>

                  <p class="changesText mb-0 pl-1" v-html="renderChangeHTML(item)"></p>
                </div>
                <!-- <span
                      v-if="logMsg(item) == ('assign' || 'unassign')"
                      class="pl-3 text-truncate"
                      v-html="$t(item.actionName.toString(), {
                        '0': `<a href='/portal/dw/profile/${item.author}'>${item.authorFullName}</a>`,
                        '1': `<a href='/portal/dw/profile/${item.target}'>${item.targetFullName}</a>`
                    })"></span>
                    <span
                      v-else
                      class="pl-3 text-truncate"
                      v-html="$t(logMsg(item), {
                        '0': `<a href='/portal/dw/profile/${item.author}'>${item.authorFullName}</a>`,
                        '1': item.target
                    })"></span>-->
                <div>
                  <div class="dateTime caption">
                    <date-format :value="item.createdTime" :format="dateTimeFormat" />
                  </div>
                </div>
                <!-- <div>
                    <span class="dateTime caption">
                      <date-format :value="item.createdTime" :format="dateTimeFormat" />
                    </span>
                  </div>-->
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
      }
    },
    mounted() {
      this.$root.$on('displayTaskChanges', taskChangesDrawer => {
        this.showTaskChangesDrawer = true
      });
      this.$root.$on('hideTaskChanges', taskChangesDrawer => {
        this.showTaskChangesDrawer = false
      });
    },
    methods: {
      closeDrawer() {
        this.showTaskChangesDrawer = false
      },
      logMsg(item) {
        return `log.${ item.actionName }`
      },
    /*  logStringMessage (item) {
        return this.$t(this.logMsg(item));
      },*/
      renderChangeHTML(item) {
        let str = '';
        if ( item.actionName === 'assign' || item.actionName === 'unassign') {
          str = `${"<p class='changesItem assignDiv mb-0'>" +
            "<span>"}${ this.$t(this.logMsg(item))}</span>`+
            `<a href='/portal/dw/profile/item.target'> ${item.targetFullName} </a>`+
            `</p>`
        } else {
          str = `${"<p class='changesItem mb-0'>" +
            "<p class='text-truncate mb-0'>"}${ this.$t(this.logMsg(item))  }${item.target}</p>`+
          `</p>`
        }
        return str;
      }
    }
  };
</script>
<style>

</style>