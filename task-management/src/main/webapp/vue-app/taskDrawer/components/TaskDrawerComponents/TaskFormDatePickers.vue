<template>
  <div>
    <div class="taskPlanDateCalender d-flex align-center">
      <i class="uiIconCalendar uiIconBlue"></i>
      <date-picker
        ref="taskStartDate"
        v-model="startDate"
        :default-value="false"
        :placeholder="$t('label.startDate')"
        :max-value="maximumStartDate"
        class="flex-grow-1 my-auto"
        @input="emitStartDate(startDate)"/>
    </div>
    <div class="taskDueDateCalender d-flex align-center">
      <i class="uiIconClock uiIconBlue"></i>
      <date-picker
        ref="taskDueDate"
        v-model="dueDate"
        :default-value="false"
        :placeholder="$t('label.dueDate')"
        :min-value="minimumEndDate"
        class="flex-grow-1 my-auto"
        @input="emitDueDate(dueDate)" />
    </div>
  </div>
</template>
<script>
  export default {
    props: {
      task: {
        type: Object,
        default:() => {
          return {}
        }
      },
      taskDueDate: {
        type: String,
        default: () => 'endDate',
      },
      datePickerTop: {
        type: Boolean,
        default: false,
      },
    },
    data: () => ({
      startDate: null,
      dueDate: null,
      actualDueDate: {},
      actualTask: {},
    }),
    computed: {
      minimumEndDate() {
        if (!this.startDate) {
          return null;
        }
        return new Date(this.startDate);
      },
      maximumStartDate() {
        if (!this.dueDate) {
          return null;
        }
        return new Date(this.dueDate);
      },
    },
    watch: {
      task(newVal, oldVal) {
        if(JSON.stringify(newVal) !== '{}') {
          this.actualTask = this.task;
          this.reset();
        }
      },
    },
    mounted() {
      this.actualTask = this.task;
      this.reset();
      document.addEventListener('closeDates',()=> {
        if (this.$refs.taskStartDate.menu) {
          window.setTimeout(() => {
            this.$refs.taskStartDate.menu = false;
          }, 100);
        }
        if (this.$refs.taskDueDate.menu) {
          window.setTimeout(() => {
            this.$refs.taskDueDate.menu = false;
          }, 100);
        }
      });
    },
    methods: {
      reset() {
        if (this.actualTask.id!=null) {
          this.startDate = null;
          this.dueDate = null;
          if(this.actualTask.startDate!=null) {
            this.$nextTick().then(() => {
              this.startDate = this.toDate(this.actualTask.startDate.time);
            });
          }
          if(this.actualTask.dueDate!=null) {
            this.$nextTick().then(() => {
              this.dueDate = this.toDate(this.actualTask.dueDate.time);
            });
          }
        } else {
          this.startDate = null;
          this.dueDate = null;
        }
      },
      toDate(date) {
        if (!date) {
          return null;
        } else if (typeof date === 'number') {
          return new Date(date);
        } else if (typeof date === 'string') {
          if (date.indexOf('T') === 10 && date.length > 19) {
            date = date.substring(0, 19);
          }
          return new Date(date);
        } else if (typeof date === 'object') {
          return new Date(date);
        }
      },
      toDateObject(date) {
        date = this.toDate(date);
        if (!date) {
          return null;
        } else {
          const newDateObject ={
            time: '',
            nanos: '',
            year: '',
            month: '',
            day: '',
            hours: '',
            minutes: '',
            seconds: '',
            timezoneOffset: '',
            date: ''
          }
          newDateObject.time = date.getTime();
          newDateObject.year = date.getUTCFullYear() - 1900;
          newDateObject.month = date.getMonth();
          newDateObject.day = date.getDay();
          newDateObject.hours = date.getHours();
          newDateObject.minutes = date.getMinutes();
          newDateObject.seconds = date.getSeconds();
          newDateObject.timezoneOffset = date.getTimezoneOffset();
          newDateObject.date = date.getDate();
          return newDateObject;
        }
      },
      emitStartDate(date) {
        if(date!== null) {
          this.$emit('startDateChanged',this.toDateObject(date));
        }
      },
      emitDueDate(date) {
        if(date!== null) {
          this.$emit('dueDateChanged',this.toDateObject(date));
        }
      },
    }
  }
</script>