<template>
  <div :id="'comment-'+comment.comment.id">
    <task-comment-item
      :comment="comment"
      :can-delete="canDelete"
      @openConfirmDeleteDialog="confirmCommentDelete()"
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
          this.comment.subComments = this.comment.commentText || [];
          this.comment.subComments.push(comment);
        }));
      }
      this.$emit('newCommentAdded');
    },
    removeTaskComment() {
      this.$taskDrawerApi.removeTaskComment(this.comment.comment.id);
      for (let i = 0; i < this.comments.length; i++) {
        if (this.comments[i] === this.comment) {
          this.comments.splice(i, 1);
        }
      }
      this.$emit('confirmDialogClosed');
    },
    displayCommentDate(dateTimeValue) {
      return dateTimeValue && this.$dateUtil.formatDateObjectToDisplay(new Date(dateTimeValue), this.dateTimeFormat, this.lang) || '';
    },
    urlVerify(text) {
      return this.$taskDrawerApi.urlVerify(text);
    },
    confirmCommentDelete: function () {
      this.$refs.CancelSavingCommentDialog.open();
    },
    commentActions(value) {
      this.showNewCommentEditor = false;
      this.$nextTick().then(() => this.$root.$emit('showEditor', value));
    }
  }
};
</script>
