<template>
  <div class="TaskSubCommentItem ps-10 pe-0 pb-2">
    <div class="commentItem">
      <div class="commentHeader d-flex">
        <exo-user-avatar
          :username="subcomment.author.username"
          :avatar-url="subcomment.author.avatar"
          :title="subcomment.author.displayName"
          :size="30"
          :url="subcomment.author.url" />
        <div class="commentContent ps-3 d-flex align-center">
          <a
            class="primary-color--text font-weight-bold subtitle-2 pe-2">{{ subcomment.author.displayName }} <span v-if="subcomment.author.external" class="externalTagClass">{{ ` (${$t('label.external')})` }}</span></a>
          <span :title="displayCommentDate" class="dateTime caption font-italic d-block"> {{ relativeTimeSubComment }}</span>
        </div>
      </div>
      <div class="commentBody ms-10 mt-1">
        <div
          class="taskContentComment"
          v-html="subcomment.formattedComment"></div>
        <v-btn
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
</template>
<script>
export default {
    
  props: {
    subcomment: {
      type: Object,
      default: () => {
        return {};
      }
    },
    commentid: {
      type: String,
      default: ''
    }
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
      },
    };
  },
  computed: {
    relativeTimeSubComment()
    {
      return this.getRelativeTime(this.subcomment.comment.createdTime.time);
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
        return this.displayCommentDate(this.subcomment.comment.createdTime.time);
      }
    },
    displayCommentDate( dateTimeValue ) {
      return dateTimeValue && this.$dateUtil.formatDateObjectToDisplay(new Date(dateTimeValue), this.dateTimeFormat, this.lang) || '';
    },
    openCommentDrawer() {
      this.$root.$emit('displayTaskComment', this.commentid );
    }
  }
};
</script>