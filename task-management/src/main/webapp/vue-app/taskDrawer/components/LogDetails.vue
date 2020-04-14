<template>
  <v-list-item-content class="pt-1">
    <v-flex xs12>
      <v-layout>
        <v-flex xs8>
          <span
            v-if="changeLog.actionName == ('assign' || 'unassign')"
            v-html="$t(logMsg, {
              '0': `<a href='/portal/dw/profile/${changeLog.author}'>${changeLog.authorFullName}</a>`,
              '1': `<a href='/portal/dw/profile/${changeLog.target}'>${changeLog.targetFullName}</a>`
          })"></span>
          <span
            v-else
            v-html="$t(logMsg, {
              '0': `<a href='/portal/dw/profile/${changeLog.author}'>${changeLog.authorFullName}</a>`,
              '1': changeLog.target
          })"></span>
        </v-flex>
        <v-flex xs4>
          <span class="dateTime caption">{{ relativeTime }}</span>
        </v-flex>
      </v-layout>
    </v-flex>
  </v-list-item-content>
</template>

<script>
  export default {
      props: {
          changeLog: {
              type: Object,
              default: () => {
                  return {};
              }
          },
      },
    computed : {
      logMsg() {
        return this.getLogMsg(this.changeLog.actionName)
      },
      relativeTime() {
        return this.getRelativeTime(this.changeLog.createdTime)
      }
    },
    methods : {
      getLogMsg(item) {
        return `log.${ item.toString()}`
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
    }
  }
</script>