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
            <v-list-item-content class="drawerTitle d-flex text-header-title text-truncate">
              <i class="uiIcon uiArrowBAckIcon" @click="closeDrawer"></i>
              <span class="pl-2">{{ $t('label.comments') }}</span>
            </v-list-item-content>
            <v-list-item-action class="drawerIcons align-end d-flex flex-row">
              <v-btn
                :disabled="isEditorActive"
                icon
                :title="$t('comment.message.addYourComment')"
                class="addCommentBtn"
                @click="openEditorToBottom(commentId)">
                <i class="uiIcon uiIconTaskAddComment"></i>
              </v-btn>
              <v-btn icon>
                <v-icon @click="closeDrawer()">mdi-close</v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-flex>
        <v-divider class="my-0" />
        <v-flex id="commentDrawerContent" class="drawerContent flex-grow-1 overflow-auto border-box-sizing">
          <div v-if="this.comments && this.comments.length" class="TaskCommentContent">
            <div
              v-for="(item, i) in comments"
              :key="i"
              class="pr-0 pl-0 TaskCommentItem">
              <task-comments
                :task="task"
                :comment="item"
                :comments="comments"
                :last-comment="commentId"
                :show-new-comment-editor="showNewCommentEditor"
                @confirmDialogOpened="$emit('confirmDialogOpened')"
                @confirmDialogClosed="$emit('confirmDialogClosed')" />
            </div>
          </div> 
          <div v-else>
            <div class="editorContent commentEditorContainer newCommentEditor">
              <task-comment-editor
                ref="commentEditor"
                :max-length="MESSAGE_MAX_LENGTH"
                :placeholder="$t('task.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"
                :task="task"
                :show-comment-editor="true"
                :id="'commentContent-editor'"
                class="subComment subCommentEditor"
                @addNewComment="addTaskComment($event)" />
            </div>
          </div>  
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
  },
  data() {
    return {
      commentId: '',
      showNewCommentEditor: false,
      test: false,
      MESSAGE_MAX_LENGTH: 1250,
    };
  },
  computed: {
    isEditorActive() {
      return !this.comments.length;
    }
  },
  mounted() {
    this.$root.$on('displayTaskComment', (commentId, isNewComment) => {
      this.showTaskCommentDrawer = true;
      this.commentId = commentId;
      this.showNewCommentEditor = isNewComment;
      this.openEditorToBottom(commentId);
    });
    this.$root.$on('hideTaskComment', () => {
      this.showTaskCommentDrawer = false;
    });
  },
  methods: {
    addTaskComment() {
      let comment = this.$refs.commentEditor.getMessage();
      comment = this.urlVerify(comment);
      this.$taskDrawerApi.addTaskComments(this.task.id,comment).then(comment => {
        this.comments.push(comment);
      });
    },
    closeDrawer() {
      this.showTaskCommentDrawer = false;
    },
    urlVerify(text) {
      return this.$taskDrawerApi.urlVerify(text);
    },
    openEditorToBottom(commentId) {
      this.$root.$emit('showNewCommentEditor');
      window.setTimeout(() => {
        const commentsDiv = document.getElementById('commentDrawerContent');
        $('#commentDrawerContent').animate({
          scrollTop: commentsDiv.scrollHeight
        }, 1000);
      }, 500);
    },
  }
};
</script>
