<template>
  <div 
    :id="id"
    class="commentItem">
    <div class="commentHeader d-flex">
      <exo-user 
        :profile-id="comment.author.username"
        :size="30"
        :extra-class="'position-relative'"
        bold-title
        link-style
        popover />
      <div class="commentContent ps-3 d-flex align-center">
        <span :title="displayCommentDate" class="dateTime caption font-italic d-block text-sub-title">{{ relativeTime }}</span>
      </div>
    </div>
    <div class="commentBody ms-10 mt-1">
      <div
        class="taskContentComment"
        v-html="comment.formattedComment"></div>
      <v-btn
        id="reply_btn"
        depressed
        text
        small
        color="primary"
        @click="openCommentDrawer">
        {{ $t('comment.message.Reply') }}
      </v-btn>
    </div>
    <div v-if="comment.subComments && comment.subComments.length" class="py-0 TaskSubComments">
      <div
        v-for="(item, i) in comment.subComments"
        :key="i">
        <div class="TaskSubCommentItem ps-10 pe-0 pb-2">
          <div class="commentItem">
            <div class="commentHeader d-flex">
              <exo-user
                :profile-id="item.author.username"
                :size="30"
                :extra-class="'position-relative'"
                bold-title
                link-style
                popover />
              <div class="commentContent ps-3 d-flex align-center">
                <span :title="displayCommentDate" class="dateTime caption font-italic d-block text-sub-title">{{ relativeTime }}</span>
              </div>
            </div>
            <div class="commentBody ms-10 mt-1">
              <div
                class="taskContentComment"
                v-html="item.formattedComment"></div>
              <v-btn
                id="reply_btn"
                depressed
                text
                small
                color="primary"
                @click="openCommentDrawer">
                {{ $t('comment.message.Reply') }}
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
  data () {
    return {
      lang: eXo.env.portal.language,
      dateTimeFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }
    };
  },
  computed: {
    relativeTime() {
      return this.getRelativeTime(this.comment.comment.createdTime.time);
    },
    id() {
      return `comment-${this.comment.comment.id}`;
    },
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
        return this.displayCommentDate(this.comment.comment.createdTime.time);
      }
    },
    displayCommentDate( dateTimeValue ) {
      return dateTimeValue && this.$dateUtil.formatDateObjectToDisplay(new Date(dateTimeValue), this.dateTimeFormat, this.lang) || '';
    },
    openCommentDrawer() {
      this.$root.$emit('displayTaskComment', this.comment.comment.id );
    }
  }

};
</script>