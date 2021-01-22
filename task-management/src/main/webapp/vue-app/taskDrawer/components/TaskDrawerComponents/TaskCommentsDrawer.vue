<template>
  <div
    v-if="showTaskCommentDrawer"
    ref="taskCommentDrawer"
    :class="showTaskCommentDrawer && 'showTaskCommentDrawer' || ''"
    class="taskCommentDrawer">
    <v-container fill-height class="pa-0">
      <v-layout column>
        <v-flex class="mx-0 drawerHeader flex-grow-0">
          <v-list-item class="pr-0">
            <v-list-item-content class="drawerTitle align-start d-flex text-header-title text-truncate">
              <i class="uiIcon uiArrowBAckIcon" @click="closeDrawer"></i>
              {{ $t('label.comments') }}
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
          <div class="TaskCommentContent">
            <div
              v-for="(item, i) in comments"
              :key="i"
              class="pr-0 pl-0 TaskCommentItem">
              <task-comments
                :task="task"
                :comment="item"
                :comments="comments"
                :is-open="!showEditor"
                :close-editor="subEditorIsOpen"/>
            </div>
          </div>
          <div v-if="showEditor" class="comment commentEditor d-flex align-start">
            <exo-user-avatar
              :username="currentUserName"
              :avatar-url="currentUserAvatar"
              :size="30"
              :url="null"/>
            <div class="editorContent ml-2">
              <task-comment-editor
                ref="commentEditor"
                v-model="editorData"
                :max-length="MESSAGE_MAX_LENGTH"
                :placeholder="$t('task.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"
                :reset="reset"
                class="comment"/>
              <v-btn
                :disabled="postDisabled"
                depressed
                small
                type="button"
                class="btn btn-primary ignore-vuetify-classes btnStyle mt-1 mb-2 commentBtn"
                @click="addTaskComment()">{{ $t('comment.label.comment') }}</v-btn>
            </div>
          </div>
          <a
            v-else
            class="pl-4"
            @click="openEditor">{{ $t('comment.label.comment') }}</a>
        </v-flex>
      </v-layout>
    </v-container>
  </div>
</template>
<script>
  export default {
    props: {
      comment: {
        type: Object,
        default: () => {
          return {};
        }
      },
      comments: {
        type: Object,
        default: () => {
          return {};
        }
      },
      task: {
        type: Boolean,
        default: false
      },
      showTaskCommentDrawer: {
        type: Boolean,
        default: false
      },
      showEditor:{
        type: Boolean,
        default: false
      },
      subEditorIsOpen: {
        type: Boolean,
        default: false
      },
    },
    data() {
      return {
        currentUserName: eXo.env.portal.userName,
      }
    },
    mounted() {
      this.$root.$on('displayTaskComment', taskCommentDrawer => {
        this.showTaskCommentDrawer = true
      });
      this.$root.$on('hideTaskComment', taskCommentDrawer => {
        this.showTaskCommentDrawer = false
      });
    },
    methods: {
      closeDrawer() {
        this.showTaskCommentDrawer = false
      },
      currentUserAvatar() {
        return `/portal/rest/v1/social/users/${eXo.env.portal.userName}/avatar`;
      },
    }
  };
</script>
<style>

</style>