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
              <div
                :title="$t('comment.message.addYourComment')"
                class="addCommentBtn"
                @click="openEditorToBottom()">
                <i class="uiIcon uiIconTaskAddComment"></i>
              </div>
              <v-btn icon>
                <v-icon @click="closeDrawer()">mdi-close</v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-flex>
        <v-divider class="my-0" />
        <v-flex id="commentDrawerContent" class="drawerContent flex-grow-1 overflow-auto border-box-sizing">
          <div class="TaskCommentContent">
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
      test: false
    };
  },
  mounted() {
    this.$root.$on('displayTaskComment', (commentId, isNewComment) => {
      this.showTaskCommentDrawer = true;
      this.commentId = commentId;
      this.showNewCommentEditor = isNewComment;
      window.setTimeout(() => {
        const commentsDiv = document.getElementById('commentDrawerContent');
        $('#commentDrawerContent').animate({
          scrollTop: commentsDiv.scrollHeight
        }, 1000);
      }, 500);
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
    openEditorToBottom() {
      this.$root.$emit('showNewCommentEditor');
      const commentsDiv = document.getElementById('commentDrawerContent');
      $('#commentDrawerContent').animate({
        scrollTop: commentsDiv.scrollHeight
      }, 500);
    }
  }
};
</script>
