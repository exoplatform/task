<template>
  <div :id="'comment-'+comment.comment.id">
    <task-comment-item
      :comment="comment"
      :comments="comments"
      :can-delete="canDelete"
      @openConfirmDeleteDialog="confirmCommentDelete($event)"
      @openCommentEditor="commentActions($event)" />
    <div class="editorContent commentEditorContainer" :class="comment.comment.id === lastComment && newCommentEditor && 'newCommentEditor'">
      <task-comment-editor
        ref="commentEditor"
        :max-length="MESSAGE_MAX_LENGTH"
        :placeholder="$t('task.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"
        :task="task"
        :show-comment-editor="comment.comment.id === lastComment"
        :last-comment="lastComment"
        :id="'commentContent-'+comment.comment.id"
        :comment-id="comment.comment.id"
        class="subComment subCommentEditor"
        @addNewComment="addTaskComment($event)" />
    </div>
    <exo-confirm-dialog
      ref="CancelSavingCommentDialog"
      :message="$t('popup.msg.deleteComment')"
      :title="$t('popup.confirmation')"
      :ok-label="$t('popup.delete')"
      :cancel-label="$t('popup.cancel')"
      persistent
      @ok="removeTaskComment()"
      @dialog-opened="$emit('confirmDialogOpened')"
      @dialog-closed="$emit('confirmDialogClosed')" />
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
      type: Object,
      default: () => null
    },
    lastComment: {
      type: String,
      default: ''
    },
    showNewCommentEditor: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      MESSAGE_MAX_LENGTH: 1250,
      lang: eXo.env.portal.language,
      commentId: '',
      commentToDelete: '',
      dateTimeFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      },
    };
  },
  mounted() {
    this.$root.$on('showNewCommentEditor', () => {
      this.lastComment = this.comments[this.comments.length-1].comment.id;
      this.showNewCommentEditor = true;
      this.$root.$emit('newCommentEditor', this.lastComment);
    });
  },
  computed: {
    newCommentEditor() {
      return this.showNewCommentEditor;
    }
  },
  methods: {
    addTaskComment(commentId) {
      let commentText = this.$refs.commentEditor.getMessage();
      commentText = this.urlVerify(commentText);
      if (this.newCommentEditor) {
        this.$taskDrawerApi.addTaskComments(this.task.id,commentText).then(comment => {
          this.comments.push(comment);
        });
      } else {
        this.$taskDrawerApi.addTaskSubComment(this.task.id, commentId, commentText).then((comment => {
          this.comment.subComments = this.comment.subComments || [];
          this.comment.subComments.push(comment);
        }));
      }
    },
    removeTaskComment() {
      this.$taskDrawerApi.removeTaskComment(this.commentToDelete);
      this.comments.forEach((comment,index) => {
        if ( comment === this.comment) {
          if ( this.comment.comment.id === this.commentToDelete ) {
            this.comments.splice(index, 1);
          } else {
            comment.subComments.forEach((subComment,index) => {
              if ( subComment.comment.id === this.commentToDelete) {
                this.comment.subComments.splice(index,1);
              }
            });
          }
        }
      });
      this.$emit('confirmDialogClosed');
    },
    displayCommentDate(dateTimeValue) {
      return dateTimeValue && this.$dateUtil.formatDateObjectToDisplay(new Date(dateTimeValue), this.dateTimeFormat, this.lang) || '';
    },
    urlVerify(text) {
      return this.$taskDrawerApi.urlVerify(text);
    },
    confirmCommentDelete (value) {
      this.commentToDelete = value;
      this.$refs.CancelSavingCommentDialog.open();
    },
    commentActions(value) {
      this.showNewCommentEditor = false;
      this.$nextTick().then(() => this.$root.$emit('showEditor', value));
    }
  }
};
</script>
