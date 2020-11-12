<template>
  <div class="taskDueDateCalender">
    <v-menu
      ref="menu"
      v-model="displayDueDateCalendar"
      :close-on-content-click="false"
      attach
      transition="scale-transition"
      offset-y
      min-width="290px">
      <template v-slot:activator="{ on }">
        <v-text-field
          v-model="date"
          :placeholder="$t('label.chooseDate')"
          :label="$t('label.dueDate')"
          class="pt-0 mt-0 dateFont"
          prepend-icon
          readonly
          @click="$emit('dueDateOpened')"
          v-on="on">
          <template v-slot:prepend class="mr-4">
            <i class="uiIconClock uiIconBlue"></i>
          </template>
        </v-text-field>
      </template>
      <v-date-picker
        v-model="date"
        no-title
        scrollable
        @input="displayDueDateCalendar = false;addDueDate()">
        <v-spacer/>
        <v-btn
          small
          min-width="40"
          text
          color="primary"
          @click="date=null;$refs.menu.save(date);addDueDate()">{{ $t('label.none') }}</v-btn>
        <v-divider vertical/>
        <v-btn
          min-width="40"
          small
          text
          color="primary"
          @click="$refs.menu.save(date);addDays()">{{ $t('label.today') }}</v-btn>
        <v-divider vertical />
        <v-btn
          min-width="40"
          small
          text
          color="primary"
          @click="$refs.menu.save(date);addDays(1)">{{ $t('label.tomorrow') }}</v-btn>
        <v-divider vertical />
        <v-btn
          min-width="40"
          small
          text
          color="primary"
          @click="$refs.menu.save(date);addDays(7)">{{ $t('label.nextweek') }}</v-btn>
      </v-date-picker>
    </v-menu>
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
        date: null,
        displayDueDateCalendar: false
      }
    },
    created() {
      document.addEventListener('loadDueDate', event => {
        if (event && event.detail) {
          const task = event.detail;
          if (task.dueDate != null) {
            this.date = new Date(task.dueDate.time).toISOString().substr(0, 10);
          }
        }
      });
      document.addEventListener('closeDueDate',()=> {
        if (this.displayDueDateCalendar) {
          window.setTimeout(() => {
            this.displayDueDateCalendar = false;
          }, 100);
        }
      });
    },
    methods: {
      addDueDate() {
        if (this.date === null) {
          this.task.dueDate = null;
        } else {
          const dueDate = {};
          const date = new Date(this.date);
          this.dateFormat(date, dueDate);
          this.task.dueDate = dueDate;
        }
        this.updateTask();
      },
      addDays(days) {
        if (days) {
          const date = new Date();
          date.setDate(date.getDate() + days);
          this.date =  date.toISOString().substr(0, 10);
          this.addDueDate();
        } else {
          this.date =  new Date().toISOString().substr(0, 10);
          this.addDueDate();
        }
      },
    }

  }
</script>