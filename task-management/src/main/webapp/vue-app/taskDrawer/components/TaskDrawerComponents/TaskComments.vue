<template>
  <div
    class="commentItem"
    @mouseover="hover = true"
    @mouseleave="hover = false">
    <div class="commentHeader d-flex">
      <exo-user-avatar
        :username="comment.author.username"
        :avatar-url="comment.author.avatar"
        :title="comment.author.displayName"
        :size="30"
        :url="comment.author.url"/>
      <div class="commentContent pl-3 d-flex align-center">
        <a
          class="primary-color--text font-weight-bold subtitle-2 pr-2"
          v-html="comment.author.displayName"></a>
        <span :title="absoluteTime()" class="dateTime caption font-italic d-block">{{ relativeTime }}</span>
      </div>
      <div class="removeCommentBtn">
        <v-dialog
          v-model="confirmDeleteComment"
          width="500">
          <template v-slot:activator="{ on }">
            <v-btn
              v-show="showDeleteButtom"
              :title="$t('label.remove')"
              class="deleteComment"
              icon
              v-on="on">
              <i class="uiIconTrashMini uiIconLightGray "></i>
            </v-btn>
          </template>

          <v-card>
            <v-card-title
              class="font-weight-black grey lighten-2 py-2"
              primary-title>
              {{ $t('popup.confirmation') }}
            </v-card-title>

            <v-card-text class="pt-5">
              <i class="uiIconColorQuestion"></i>
              {{ $t('popup.msg.deleteComment') }}
            </v-card-text>

            <v-divider/>

            <v-card-actions>
              <v-spacer/>
              <v-btn
                depressed
                small
                color="primary"
                dark
                @click="confirmDeleteComment=!confirmDeleteComment;removeTaskComment()">{{ $t('label.ok') }}
              </v-btn>
              <v-btn
                depressed
                text
                small
                @click="confirmDeleteComment=!confirmDeleteComment">{{ $t('popup.cancel') }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
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
    <div class="py-0 pl-10 TaskSubComments">
      <div
        v-for="(item, i) in comment.subComments"
        :key="i"
        class="TaskSubCommentItem pr-0 pb-2">
        <task-comments
          :comment="item"
          :task="task"
          :sub="true"
          :comments="comment.subComments"
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
            :placeholder="commentPlaceholder"
            class="subComment"
            @subShowEditor="openEditor"/>
          <v-btn
            :disabled="disabledComment"
            depressed
            small
            dark
            class="commentBtn mt-1 mb-2"
            @click="addTaskSubComment()">{{ $t('comment.label.comment') }}
          </v-btn>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import {addTaskSubComment, removeTaskComment, urlVerify} from '../../taskDrawerApi';

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
            type: Boolean,
            default: false
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
                currentUserName: eXo.env.portal.userName
            }
        },
        computed: {
            relativeTime() {
                return this.getRelativeTime(this.comment.comment.createdTime.time)
            },
            currentUserAvatar() {
                return `/rest/v1/social/users/${this.currentUserName}/avatar`;
            },
            showDeleteButtom() {
                return this.hover && eXo.env.portal.userName === this.comment.author.username;
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
            addTaskSubComment() {
              let subComment = this.$refs.subCommentEditor.getMessage();
              subComment = this.urlVerify(subComment);
              console.warn('this.comment.id',this.comment.id);
              addTaskSubComment(this.task.id, this.comment.id, subComment).then((comment => {
                        this.comment.subComments = this.comment.subComments || [];
                        this.comment.subComments.push(comment)
                      })
              );
              this.showEditor = false;
            },
            removeTaskComment: function () {
              removeTaskComment(this.comment.id);
                for (let i = 0; i < this.comments.length; i++) {
                    if (this.comments[i] === this.comment) {
                        this.comments.splice(i, 1);
                    }
                }
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
            }
        }
    }
</script>