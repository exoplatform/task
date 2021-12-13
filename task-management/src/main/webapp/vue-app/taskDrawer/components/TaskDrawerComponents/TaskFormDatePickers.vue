<template>
  <div>
    <div class="taskPlanDateCalender d-flex align-center">
      <i class="uiIconStartDate uiIconBlue"></i>
      <date-picker
        ref="taskStartDate"
        v-model="startDate"
        :default-value="false"
        :placeholder="$t('label.startDate')"
        :max-value="maximumStartDate"
        width="100%"
        class="flex-grow-1 my-auto"
        @input="emitStartDate(startDate)">
        <template slot="footer">
          <div class="dateFooter">
            <v-btn-toggle
              class="d-flex flex-wrap justify-space-between"
              tile
              color="primary"
              background-color="primary"
              group>
              <v-btn
                value="left"
                class="my-0"
                small
                @click="addBtnStartDate()">
                {{ $t('label.today') }}
              </v-btn>

              <v-btn
                value="center"
                class="my-0"
                small
                @click="addBtnStartDate(1)">
                {{ $t('label.tomorrow') }}
              </v-btn>

              <v-btn
                value="right"
                class="my-0"
                small
                @click="addBtnStartDate(7)">
                {{ $t('label.nextweek') }}
              </v-btn>

              <v-btn
                value="right"
                class="my-0"
                small
                @click="resetStartDate()">
                {{ $t('label.none') }}
              </v-btn>
            </v-btn-toggle>
          </div>
        </template>
      </date-picker>
    </div>
    <div class="taskDueDateCalender d-flex align-center">
      <i class="uiIconDueDate uiIconBlue"></i>
      <date-picker
        ref="taskDueDate"
        v-model="dueDate"
        :default-value="false"
        :placeholder="$t('label.dueDate')"
        :min-value="minimumEndDate"
        width="100%"
        class="flex-grow-1 my-auto"
        @input="emitDueDate(dueDate)">
        <template slot="footer">
          <div class="dateFooter">
            <v-btn-toggle
              class="d-flex flex-wrap justify-space-between"
              tile
              color="primary"
              background-color="primary"
              group>
              <v-btn
                value="left"
                class="my-0"
                small
                :disabled="startDate > checkDate()"                
                @click="addBtnDate()">
                {{ $t('label.today') }}
              </v-btn>

              <v-btn
                value="center"
                class="my-0"
                small
                :disabled="startDate > checkDate(1)"                
                @click="addBtnDate(1)">
                {{ $t('label.tomorrow') }}
              </v-btn>

              <v-btn
                value="right"
                class="my-0"
                small
                :disabled="startDate > checkDate(7)"                
                @click="addBtnDate(7)">
                {{ $t('label.nextweek') }}
              </v-btn>

              <v-btn
                value="right"
                class="my-0"
                small
                @click="resetDueDate()">
                {{ $t('label.none') }}
              </v-btn>
            </v-btn-toggle>
          </div>
        </template>
      </date-picker>
    </div>
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
  data () {
    return {    
      startDate: null,
      dueDate: null,
      actualDueDate: {},
      actualTask: {},
      dateShortCutItems: [
        {key: 'today',value: this.$t('label.today')},
        {key: 'tomorrow',value: this.$t('label.tomorrow')},
        {key: 'nextweek',value: this.$t('label.nextweek')},
        {key: 'datePicker',value: this.$t('label.pickDate')}
      ],
      dateItem: ''
    };
  },
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
    task(newVal) {
      if (JSON.stringify(newVal) !== '{}') {
        this.actualTask = this.task;
        this.reset();
      }
    },
  },
  mounted() {
    this.actualTask = this.task;
    this.reset();
    document.addEventListener('closeDates',()=> {
      if (this.$refs.taskStartDate && this.$refs.taskStartDate.menu) {
        this.$refs.taskStartDate.menu = false;
      }
      if (this.$refs.taskDueDate && this.$refs.taskDueDate.menu) {
        this.$refs.taskDueDate.menu = false;
      }
    });

    $('.taskAssignItem').off('click').on('click', () => {
      if (this.$refs.taskStartDate && this.$refs.taskStartDate.menu) {
        this.$refs.taskStartDate.menu = false;
      }
      if (this.$refs.taskDueDate && this.$refs.taskDueDate.menu) {
        this.$refs.taskDueDate.menu = false;
      }
    });
  },
  methods: {
    checkDate(days) {
      if (!days) {
        days=0;
      }
      const date = new Date();
      date.setDate(date.getDate() + days);
      return date;
    },  
    reset() {
      if (this.actualTask.id!=null) {
        this.startDate = null;
        this.dueDate = null;
        if (this.actualTask.startDate!=null) {
          this.$nextTick().then(() => {
            this.startDate = this.toDate(this.actualTask.startDate.time);
          });
        }
        if (this.actualTask.dueDate!=null) {
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
        };
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
    addBtnDate(days) {
      if (days) {
        const date = new Date();
        date.setDate(date.getDate() + days);
        this.dueDate = date;
      } else {
        this.dueDate = new Date();
      }
      if (this.$refs.taskDueDate && this.$refs.taskDueDate.menu) {
        this.$refs.taskDueDate.menu = false;
      }
    },
    addBtnStartDate(days) {
      if (days) {
        const date = new Date();
        date.setDate(date.getDate() + days);
        this.startDate = date;
      } else {
        this.startDate = new Date();
      }
      if (this.$refs.taskStartDate && this.$refs.taskStartDate.menu) {
        this.$refs.taskStartDate.menu = false;
      }
    },
    resetDueDate() {
      this.dueDate = null;
      this.$emit('dueDateChanged','none');
      if (this.$refs.taskDueDate && this.$refs.taskDueDate.menu) {
        this.$refs.taskDueDate.menu = false;
      }
    },
    resetStartDate() {
      this.startDate = null;
      this.$emit('startDateChanged','none');
      if (this.$refs.taskStartDate && this.$refs.taskStartDate.menu) {
        this.$refs.taskStartDate.menu = false;
      }
    },
    emitStartDate(date) {
      const newDate = this.toDateObject(date);
      if ((!date && this.actualTask.startDate) || (date && !this.actualTask.startDate) || (this.actualTask.startDate && !this.datesEquals(newDate,this.actualTask.startDate))) {
        this.$emit('startDateChanged',newDate);
      }
    },
    emitDueDate(date) {
      const newDate = this.toDateObject(date);
      if ((!date && this.actualTask.dueDate) || (date && !this.actualTask.dueDate) || (this.actualTask.dueDate && !this.datesEquals(newDate,this.actualTask.dueDate))) {
        this.$emit('dueDateChanged',newDate);
      }
    },
    datesEquals(date1,date2){
      if (date1.month===date2.month&&date1.year===date2.year&&date1.date===date2.date){
        return true;
      }
      return false;
    }
  }
};
</script>
