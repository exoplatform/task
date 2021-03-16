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
                @click="openEditorToBottom">
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
                :is-open="!showEditor"
                :close-editor="subEditorIsOpen"
                :comment-id="commentId"
                :id="id"
                @isOpen="OnCloseAllEditor()"
                @confirmDialogOpened="$emit('confirmDialogOpened')"
                @confirmDialogClosed="$emit('confirmDialogClosed')"
                @showSubEditor="OnUpdateEditorStatus" />
            </div>
          </div>
          <div v-if="showEditor" class="comment commentEditor d-flex align-start">
            <exo-user-avatar
              :username="currentUserName"
              :avatar-url="currentUserAvatar"
              :size="30"
              :url="null" />
            <div class="editorContent ml-2">
              <task-comment-editor
                ref="commentEditor"
                v-model="editorData"
                :max-length="MESSAGE_MAX_LENGTH"
                :placeholder="$t('task.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"
                :reset="reset"
                :task="task"
                :id="id"
                class="comment" />
              <v-btn
                :disabled="postDisabled"
                depressed
                small
                type="button"
                class="btn btn-primary ignore-vuetify-classes btnStyle mt-1 mb-2 commentBtn"
                @click="addTaskComment()">
                {{ $t('comment.label.comment') }}
              </v-btn>
            </div>
          </div>
          <!--<a
            v-else
            class="pl-4"
            @click="openEditor">{{ $t('comment.label.comment') }}</a>-->
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
    subEditorIsOpen: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      currentUserName: eXo.env.portal.userName,
      showEditor: false,
      showSubEditor: false,
      disabledComment: true,
      editorData: null,
      MESSAGE_MAX_LENGTH: 1250,
      id: `commentContent${parseInt(Math.random() * 10000).toString()}`,
      commentId: ''
    };
  },
  computed: {
    currentUserAvatar() {
      return `/portal/rest/v1/social/users/${eXo.env.portal.userName}/avatar`;
    },
    postDisabled: function() {
      if (this.disabledComment){
        return true;
      }
      else if (this.editorData !== null && this.editorData!==''){
        let pureText = this.editorData ? this.editorData.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
        const div = document.createElement('div');
        div.innerHTML = pureText;
        pureText = div.textContent || div.innerText || '';
        return pureText.length> this.MESSAGE_MAX_LENGTH ;
      } else {return true;}
    },
  },
  watch: {
    editorData(val) {
      this.disabledComment = val === '';
    },
    showEditor() {
      this.showSubEditor = !this.showEditor;
    },
  },
  mounted() {
    this.$root.$on('displayTaskComment', () => {
      this.showTaskCommentDrawer = true;
      window.setTimeout(() => {
        const commentsDiv = document.getElementById('commentDrawerContent');
        $('#commentDrawerContent').animate({
          scrollTop: commentsDiv.scrollHeight
        }, 1000);
      }, 500);
      if ( this.comments && !this.comments.length ) {
        this.openEditor();
      }
    });

    this.$root.$on('displaySubCommentEditor', id => {
      if ( id == null ) {
        window.setTimeout(() => {
          this.openEditorToBottom();
        }, 500);
      } else {
        window.setTimeout(() => {
          document.dispatchEvent(new CustomEvent('openSubComment',{'detail': id}));
        }, 500);
      }
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
        this.reset = !this.reset;
      });
    },
    openEditor() {
      this.showEditor = true;
      this.subEditorIsOpen = true;
      this.editorData = null;
    },
    OnUpdateEditorStatus: function (val) {
      this.showEditor = !val;
      if (val === false) {
        this.subEditorIsOpen = false;
      }
    },
    OnCloseAllEditor() {
      this.subEditorIsOpen = true;
    },
    closeDrawer() {
      this.showTaskCommentDrawer = false;
      this.showEditor = false;
      this.subEditorIsOpen = false;
    },
    urlVerify(text) {
      return this.$taskDrawerApi.urlVerify(text);
    },
    openEditorToBottom() {
      const commentsDiv = document.getElementById('commentDrawerContent');
      $('#commentDrawerContent').animate({
        scrollTop: commentsDiv.scrollHeight
      }, 500);
      this.openEditor();
    }
  }
};
</script>
