<template>
  <div
    :id="'comment-'+comment.comment.id"
    class="commentItem"
    @mouseover="hover = true"
    @mouseleave="hover = false">
    <div class="commentHeader d-flex">
      <exo-user-avatar
        :username="comment.author.username"
        :title="comment.author.displayName"
        :size="30"
        :url="comment.author.url"/>
      <div class="commentContent pl-3 d-flex align-center">
        <a
          class="primary-color--text font-weight-bold subtitle-2 pr-2">{{ comment.author.displayName }} <span v-if="comment.author.external" class="externalTagClass">{{ ` (${$t('label.external')})` }}</span></a>
        <span :title="absoluteTime()" class="dateTime caption font-italic d-block">{{ relativeTime }}</span>
      </div>
      <div class="removeCommentBtn">
        <v-btn
          v-show="showDeleteButtom"
          :title="$t('label.remove')"
          :size="32"
          class="deleteComment"
          icon
          small
          @click="confirmCommentDelete()"
          v-on="on">
          <i class="uiIconTrashMini uiIconLightGray "></i>
        </v-btn>
      </div>
    </div>
    <div class="commentBody ml-10 mt-1">
      <div
        class="taskContentComment"
        v-html="comment.formattedComment"></div>
      <v-btn
        id="reply_btn"
        depressed
        text
        small
        color="primary"
        @click="openEditor()">{{ $t('comment.message.Reply') }}
      </v-btn>
    </div>
    <div class="py-0 TaskSubComments">
      <div
        v-for="(item, i) in comment.subComments"
        :key="i"
        class="TaskSubCommentItem pl-10 pr-0 pb-2">
        <task-comments
          :comment="item"
          :task="task"
          :sub="true"
          :comments="comment.subComments"
          @confirmDialogOpened="$emit('confirmDialogOpened')"
          @confirmDialogClosed="$emit('confirmDialogClosed')"
          @openSubEditor="openEditor()"/>
      </div>
      <div
        v-focus
        v-if="showEditor && !sub"
        class="subComment subCommentEditor ml-10 d-flex align-start">
        <exo-user-avatar
          :username="currentUserName"
          :avatar-url="currentUserAvatar"
          :size="30"
          :url="null"/>
        <div class="editorContent ml-2">
          <task-comment-editor
            ref="subCommentEditor"
            v-model="editorData"
            :max-length="MESSAGE_MAX_LENGTH"
            :placeholder="$t('task.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"
            :task="task"
            :id="id"
            class="subComment subCommentEditor"
            @subShowEditor="openEditor"/>
          <v-btn
            :disabled="postDisabled"
            depressed
            small
            type="button" 
            class="btn btn-primary ignore-vuetify-classes btnStyle mt-1 mb-2 commentBtn"
            @click="addTaskSubComment(comment)">{{ $t('comment.label.comment') }}
          </v-btn>
        </div>
      </div>
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
  import {addTaskSubComment, urlVerify, removeTaskComment} from '../../taskDrawerApi';

    export default {
        name: "TaskComments",
        directives: {
          focus: {
            inserted: function (el) {
              el.focus()
            }
          }
        },
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
          sub: {
            type: Boolean,
            default: false
          },
          closeEditor: {
            type: Boolean,
            default: false
          },
          isOpen:{
            type: Boolean,
            default: false
          },
          id: {
            type: String,
            default: ''
          },
          commentId: {
            type: String,
            default: ''
          }
        },
        data() {
            return {
                hover: false,
                editorData: '',
                disabledComment: '',
                confirmDeleteComment: false,
                commentPlaceholder: this.$t('comment.message.addYourComment'),
                showEditor : false,
                currentUserName: eXo.env.portal.userName,
                MESSAGE_MAX_LENGTH:1250,
            }
        },
      computed: {
            relativeTime() {
                return this.getRelativeTime(this.comment.comment.createdTime.time)
            },
            showDeleteButtom() {
                return this.hover && eXo.env.portal.userName === this.comment.author.username;
            },
           postDisabled: function () {
           if (this.disabledComment) {
             return true
           } else if (this.editorData !== null && this.editorData!=='') {
           let pureText = this.editorData ? this.editorData.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
           const div = document.createElement('div');
           div.innerHTML = pureText;
           pureText = div.textContent || div.innerText || '';
           return pureText.length > this.MESSAGE_MAX_LENGTH;
          }else {return true}
        },
        },
        watch: {
            editorData(val) {
              this.disabledComment = val === '';
            },
            showEditor(val) {
              this.$emit('showSubEditor', val);
            },
            closeEditor(val) {
              if (val === true) {
                this.showEditor = false
              }
            },
          commentId() {
              if( this.commentId === `comment-${this.comment.comment.id}` ) {
                this.openSubEditor();
              }
          }
        },
      mounted() {
          document.addEventListener('openSubComment', event => {
            if (event && event.detail && event.detail === `comment-${this.comment.comment.id}`) {
              this.commentId = event.detail;
            }
          })
      },
      methods: {
          openEditor() {
            if (this.isOpen) {
              this.$emit('isOpen')
                setTimeout(this.openSubEditor, 100)
              } else {
                this.openSubEditor()
              }
            },
            openSubEditor() {
              this.showEditor = true;
              this.editorData = '';
              if (this.sub) {
                this.$emit('openSubEditor')
              }
            },
            addTaskSubComment(commentItem) {
              let subComment = this.$refs.subCommentEditor.getMessage();
              subComment = this.urlVerify(subComment);
              addTaskSubComment(this.task.id, commentItem.comment.id, subComment).then((comment => {
                        this.comment.subComments = this.comment.subComments || [];
                        this.comment.subComments.push(comment)
                      })
              );
              this.showEditor = false;
            },
            removeTaskComment() {
              removeTaskComment(this.comment.comment.id);
                for (let i = 0; i < this.comments.length; i++) {
                  if (this.comments[i] === this.comment) {
                    this.comments.splice(i, 1);
                  }
                }
                this.$emit('confirmDialogClosed')
            },
            getRelativeTime(previous) {
                const msPerMinute = 60 * 1000;
                const msPerHour = msPerMinute * 60;
                const msPerDay = msPerHour * 24;
                const msPerMaxDays = msPerDay * 2;
                const elapsed = new Date().getTime() - previous;

                if (elapsed < msPerMinute) {
                    return this.$t('task.timeConvert.Less_Than_A_Minute');
                } else if (elapsed === msPerMinute) {
                    return this.$t('task.timeConvert.About_A_Minute');
                } else if (elapsed < msPerHour) {
                    return this.$t('task.timeConvert.About_?_Minutes').replace('{0}', Math.round(elapsed / msPerMinute));
                } else if (elapsed === msPerHour) {
                    return this.$t('task.timeConvert.About_An_Hour');
                } else if (elapsed < msPerDay) {
                    return this.$t('task.timeConvert.About_?_Hours').replace('{0}', Math.round(elapsed / msPerHour));
                } else if (elapsed === msPerDay) {
                    return this.$t('task.timeConvert.About_A_Day');
                } else if (elapsed < msPerMaxDays) {
                    return this.$t('task.timeConvert.About_?_Days').replace('{0}', Math.round(elapsed / msPerDay));
                } else {
                  return this.absoluteTime({dateStyle: "short"});
                }
            },
            absoluteTime(options) {
              const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
              return new Date(this.comment.comment.createdTime.time).toLocaleString(lang, options).split("/").join("-");
            },
            urlVerify(text) {
              return urlVerify(text);
            },
            confirmCommentDelete: function () {
              this.$refs.CancelSavingCommentDialog.open();
            },
        }
    }
</script>
