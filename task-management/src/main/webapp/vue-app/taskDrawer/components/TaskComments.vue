<template>
  <v-layout
    @mouseover="hover = true"
    @mouseleave="hover = false">
    <v-list-item-avatar size="30" class="mt-2">
      <v-img :src="getUserAvatar(comment.author.username)"/>
    </v-list-item-avatar>
    <v-list-item-content
      class="pt-1 pb-0">
      <v-flex xs12>
        <v-layout>
          <div>
            <a
              class="primary-color--text font-weight-bold subtitle-2"
              v-html="comment.author.displayName"></a>
            <span :title="absoluteTime" class="dateTime caption pl-4">{{ relativeTime }}</span>
          </div>
          <v-flex class="d-flex flex-row-reverse">
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
          </v-flex>
        </v-layout>
      </v-flex>
      <v-layout>
        <v-col class="py-0">
          <v-row>   
            <span
              class="taskContentComment"
              v-html="comment.comment"></span></v-row>
          <v-row><v-btn
            id="reply_btn"
            depressed
            text
            small
            color="primary"
            @click="openEditor()">{{ $t('comment.message.Reply') }}
          </v-btn></v-row>
        </v-col>
      </v-layout>
      <v-list class="py-0">
        <v-list-item
          v-for="(item, i) in comment.subComments"
          :key="i"
          class="pr-0 pb-2">
          <task-comments
            :comment="item"
            :task="task"
            :sub="true"
            :comments="comment.subComments"
            @openSubEditor="openEditor()"/>
        </v-list-item>
        <v-list-item 
          v-focus 
          v-if="showEditor && !sub" 
          class="subComment">
          <v-list-item-avatar
            class="mt-0"
            size="30"
            tile>
            <v-img :src="currentUserAvatar"/>
          </v-list-item-avatar>
          <v-layout row class="editorContent ml-0">
            <task-comment-editor
              v-model="editorData"
              :placeholder="commentPlaceholder"
              class="mr-4 subComment"
              @subShowEditor="openEditor"/>
            <v-btn
              :disabled="disabledComment"
              depressed
              small
              dark
              class="commentBtn mt-1 mb-2"
              @click="addTaskSubComment()">{{ $t('comment.label.comment') }}
            </v-btn>
          </v-layout>
        </v-list-item>
      </v-list>
    </v-list-item-content>
  </v-layout>
</template>

<script>
    import {addTaskSubComment, removeTaskComment} from '../taskDrawerApi';

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
            }
        },
        computed: {
            relativeTime() {
                return this.getRelativeTime(this.comment.createdTime.time)
            },
            currentUserAvatar() {
                return `/rest/v1/social/users/${eXo.env.portal.userName}/avatar`;
            },
            showDeleteButtom() {
                return this.hover && eXo.env.portal.userName === this.comment.author.username;
            },
            absoluteTime() {
              return this.getAbsoluteTime(this.comment.createdTime.time)
            }
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
            getUserAvatar(username) {
                return `/rest/v1/social/users/${username}/avatar`;
            },
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
              this.editorData=this.editorData.replace(/\n|\r/g,'');
              addTaskSubComment(this.task.id, this.comment.id, this.editorData).then((comment => {
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
                const msPerMonth = msPerDay * 30;
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
                } else if (elapsed < msPerMonth) {
                    return this.$t('task.timeConvert.About_?_Days').replace('{0}', Math.round(elapsed / msPerDay));
                } else if (elapsed === msPerMonth) {
                    return this.$t('task.timeConvert.About_A_Month');
                } else {
                    return this.$t('task.timeConvert.About_?_Months').replace('{0}', Math.round(elapsed / msPerMonth));
                }
            },
            getAbsoluteTime(timestamp) {
              return new Date(timestamp).toLocaleString();
            }
        }
    }
</script>