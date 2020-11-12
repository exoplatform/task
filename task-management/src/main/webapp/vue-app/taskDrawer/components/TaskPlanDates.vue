<template>
  <div class="taskPlanDateCalender">
    <v-flex
      @mouseover="showDeleteDateBtn = true"
      @mouseleave="showDeleteDateBtn = false">
      <v-menu
        v-model="rangeDateMenu"
        :close-on-content-click="false"
        attach
        transition="scale-transition"
        offset-y
        left
        min-width="290px">
        <template v-slot:activator="{ on }">
          <v-text-field
            v-model="dateRangeText"
            :label="$t('label.noWorkPlan')"
            :placeholder="$t('label.chooseDate')"
            class="pt-0 pl-1 mt-0 dateFont"
            prepend-icon
            append-icon
            readonly
            style="width: 100%"
            @click="$emit('planDatesOpened')"
            v-on="on">
            model: {{ dates }}
            <template v-slot:prepend class="mr-4">
              <i class="uiIconCalendar uiIconBlue"></i>
            </template>
            <template v-slot:append class="mr-4">
              <i
                v-show="dateRangeDeleteBtn"
                :title="$t('label.remove')"
                style="font-size: 10px"
                class="uiIconTrashMini uiIconBlue "
                @click="removeScheduledDate()"></i>
            </template>
          </v-text-field>
        </template>
        <v-date-picker
          v-model="dates"
          no-title
          range>
          <v-spacer/>
          <v-alert
            v-show="dateRangeError"
            width="240"
            class="mb-0 dueDateError"
            type="error">
            {{ $t('editinline.taskPlan.errorMessage') }}
          </v-alert>
        </v-date-picker>
      </v-menu>
    </v-flex>
  </div>
</template>
<script>
  export default {
    props: {
      task: {
        type: Object,
        default: () => {
          return {};
        }
      }
    },
    data() {
      return {
        dates: [],
        showDeleteDateBtn : false,
        rangeDateMenu : false,
      }
    },
    computed: {
      dateRangeDeleteBtn() {
        return this.showDeleteDateBtn && this.dates.length !== 0
      },
      dateRangeText () {
        return this.dates.join('~')
      },
      dateRangeError() {
        return this.dates[1] < this.dates[0]
      },
    },
    watch: {
      dates(val) {
        if(val[1] && val[1] > val[0]) {
          const startDate = {};
          const endDate = {};
          this.dateFormat(new Date(val[0]), startDate);
          this.dateFormat(new Date(val[1]), endDate);
          this.task.startDate = startDate;
          this.task.endDate = endDate;
          this.updateTask();
        }
      },
    },
    created() {
      document.addEventListener('loadPlanDates', event => {
        if (event && event.detail) {
          const task = event.detail;
          if (task.startDate != null) {
            this.dates[0] = new Date(task.startDate.time).toISOString().substr(0, 10);
            this.dates[1] = new Date(task.endDate.time).toISOString().substr(0, 10);
          }
        }
      });
      document.addEventListener('closePlanDates',()=> {
        if (this.rangeDateMenu) {
          window.setTimeout(() => {
            this.rangeDateMenu = false;
          }, 100);
        }
      });
    },
    methods: {
      dateFormat(date, newDate) {
        newDate.time = date.getTime();
        newDate.year = date.getUTCFullYear() - 1900;
        newDate.month = date.getMonth();
        newDate.day = date.getDay();
        newDate.hours = date.getHours();
        newDate.minutes = date.getMinutes();
        newDate.seconds = date.getSeconds();
        newDate.timezoneOffset = date.getTimezoneOffset();
        newDate.date = date.getDate();
      },
      removeScheduledDate() {
        this.dates = []
        this.task.startDate = null;
        this.task.endDate = null;
        this.updateTask()
      },
    }
  }

</script>