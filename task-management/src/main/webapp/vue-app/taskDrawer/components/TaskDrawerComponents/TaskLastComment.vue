<template>
  <div 
    :id="id"
    class="commentItem">
    <div class="commentHeader d-flex">
      <exo-user-avatar
        :username="comment.author.username"
        :avatar-url="comment.author.avatar"
        :title="comment.author.displayName"
        :size="30"
        :url="comment.author.url"/>
      <div class="commentContent pl-3 d-flex align-center">
        <a
          class="primary-color--text font-weight-bold subtitle-2 pr-2">{{ comment.author.displayName }} <span v-if="comment.author.external" class="externalTagClass">{{ ` (${$t('label.external')})` }}</span></a>
        <span :title="absoluteTime()" class="dateTime caption font-italic d-block">{{ relativeTime }}</span>
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
        @click="openCommentDrawer">{{ $t('comment.message.Reply') }}
      </v-btn>
    </div>
    <div v-if="comment.subComments && comment.subComments.length" class="py-0 TaskSubComments">
      <div
        v-for="(item, i) in comment.subComments"
        :key="i">
        <div class="TaskSubCommentItem pl-10 pr-0 pb-2">
          <div class="commentItem">
            <div class="commentHeader d-flex">
              <exo-user-avatar
                :username="item.author.username"
                :avatar-url="item.author.avatar"
                :title="item.author.displayName"
                :size="30"
                :url="item.author.url"/>
              <div class="commentContent pl-3 d-flex align-center">
                <a
                  class="primary-color--text font-weight-bold subtitle-2 pr-2">{{ item.author.displayName }} <span v-if="lastSubComment.author.external" class="externalTagClass">{{ ` (${$t('label.external')})` }}</span></a>
                <span :title="absoluteTime()" class="dateTime caption font-italic d-block">{{ relativeTime }}</span>
              </div>
            </div>
            <div class="commentBody ml-10 mt-1">
              <div
                class="taskContentComment"
                v-html="item.formattedComment"></div>
              <v-btn
                id="reply_btn"
                depressed
                text
                small
                color="primary"
                @click="$root.$emit('displayTaskComment')">{{ $t('comment.message.Reply') }}
              </v-btn>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  export default {
    props: {
      task: {
        type: Boolean,
        default: false
      },
      comment: {
        type: Object,
        default: () => {
          return {};
        }
      },
    },
    computed: {
      relativeTime() {
        return this.getRelativeTime(this.comment.comment.createdTime.time)
      },
      lastSubComment() {
        return this.comment.subComments && this.comment.subComments[this.comment.subComments.length-1];
      },
      id() {
        return `comment-${this.comment.comment.id}`
      }
    },
    methods: {
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
      openCommentDrawer() {
        this.$root.$emit('displayTaskComment');
        this.$root.$emit('displaySubCommentEditor', this.id);
      }
    }

  }
</script>