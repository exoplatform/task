<template>
  <div
    class="commentItem"
    :id="`commentItem-${comment.comment.id}`">
    <div
      class="commentWrapper"
      @mouseover="hover = true"
      @mouseleave="hover = false">
      <div class="commentHeader d-flex">
        <exo-user 
          :profile-ix="comment.author.username"
          :extra-class="'position-relative'"
          :size="30"
          bold-title
          link-style
          popover />
        <div class="commentContent d-flex align-center">
          <span :title="displayCommentDate" class="dateTime caption font-italic d-block">{{ relativeTime }}</span>
        </div>
        <div class="removeCommentBtn">
          <v-btn
            v-show="showDeleteButtom"
            :title="$t('label.remove')"
            :size="32"
            class="deleteComment"
            icon
            small
            @click="$emit('openConfirmDeleteDialog', comment.comment.id)"
            v-on="on">
            <i class="uiIconTrashMini uiIconLightGray "></i>
          </v-btn>
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
          @click="$emit('openCommentEditor',comment.comment.id)">
          {{ $t('comment.message.Reply') }}
        </v-btn>
      </div>
    </div>
    <div v-if="comment && comment.subComments" class="py-0 TaskSubComments">
      <div
        v-for="(item, i) in comment.subComments"
        :key="i"
        class="TaskSubCommentItem pe-0 pb-2">
        <task-comment-item 
          :comment="item"
          :comments="comment.subComments"
          :avatar-size="30"
          @openCommentEditor="$emit('openCommentEditor',comment.comment.id)"
          @openConfirmDeleteDialog="$emit('openConfirmDeleteDialog',item.comment.id,true)" />
      </div>
    </div>
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
    showOnly: {
      type: Boolean,
      default: true
    },
    comments: {
      type: Object,
      default: () => {
        return {};
      }
    },
  },
  data() {
    return {
      hover: false,
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
    showDeleteButtom() {
      return this.hover && eXo.env.portal.userName === this.comment.author.username;
    },
    relativeTime() {
      return this.getRelativeTime(this.comment.comment.createdTime.time);
    },
  },
  methods: {
    displayCommentDate( dateTimeValue ) {
      return dateTimeValue && this.$dateUtil.formatDateObjectToDisplay(new Date(dateTimeValue), this.dateTimeFormat, this.lang) || '';
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
        return this.displayCommentDate(this.comment.comment.createdTime.time);
      }
    }
  },
};
</script>

