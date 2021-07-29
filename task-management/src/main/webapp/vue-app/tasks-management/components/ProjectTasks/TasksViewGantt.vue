<template>
  <div v-if="hideGantt">
    <div class="noTasksProject">
      <div class="noTasksProjectIcon"><i class="uiIcon uiIconTask"></i></div>
      <div class="noTasksProjectLabel"><span>{{ $t('label.noTasks') }}</span></div>
      <div class="addunscheduledTask">
        <v-btn @click="$root.$emit('displayTasksUnscheduledDrawer', unscheduledTaskList)" text>
          {{ $t('Unscheduled Tasks') }}
        </v-btn>
      </div>
    </div>
  </div>
  <div v-else class="gantt-wrapper">
    <div class="unscheduled-task-container">
      <v-btn
        :title="$t('label.noWorkPlan.tasks')"
        class="unscheduled-task-btn"
        fab
        small
        dark
        @click="$root.$emit('displayTasksUnscheduledDrawer', unscheduledTaskList)">
        <v-icon>mdi-calendar-range</v-icon>
        <span class="unscheduled-task-badge">{{ unscheduledTaskList.length }}</span>
      </v-btn>
    </div>
    <div 
      id="gantt-chart"
      class="gantt-chart-container">
    </div>
  </div>
</template>
<script>
export default {
  props: {
    tasksList: {
      type: Array,
      default: () => []
    },
  },
  data () {
    return {
      gantt: '',
      lang: eXo && eXo.env.portal.language || 'en',
      fullDateFormat: {
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
      },
      tasksToDisplay: [],
      tasks: [],
      isGanttDisplayed: false,
      ganttScale: 'Week',
      unscheduledTaskList: [],
      ganttItems: []
    };
  },
  computed: {
    ganttTasks() {
      return this.ganttItems.length;
    },
    hideGantt() {
      return this.tasksList && this.tasksList.length === 0 || this.ganttTasks === 0;
    }
  },
  watch: {
    tasksList() {
      this.ganttItems = this.getTasksToDisplay(this.tasksList);
    }
  },
  created() {
    this.$root.$on('hide-tasks-project', () => {
      this.isGanttDisplayed = false;
      $('#gantt-chart').empty();
    });
    this.$root.$on('gantt-displayed', value => {
      this.unscheduledTaskList = [];
      this.isGanttDisplayed = false;
      $('#gantt-chart').empty();
      if ( value && value.length ) {
        this.tasks = value;
        this.tasksToDisplay = this.getTasksToDisplay(this.tasks);
        if ( this.isGanttDisplayed === false && this.ganttItems.length ) {
          this.initGanttChart(this.tasksToDisplay);
        }
      }
    });
    this.$root.$on('refresh-gantt', taskItems => {
      this.unscheduledTaskList = [];
      const tasksInGantt = this.getTasksToDisplay(taskItems);
      if ( this.tasksToDisplay && this.tasksToDisplay.length && tasksInGantt.length > this.tasksToDisplay.length) {
        this.tasksToDisplay.push(tasksInGantt[tasksInGantt.length-1]);
        this.gantt.refresh(this.tasksToDisplay);
      }
      this.$root.$emit('refresh-unscheduled-gantt',this.unscheduledTaskList);
    });
    this.$root.$on('task-updated', task => {
      this.unscheduledTaskList = [];
      this.tasks.forEach((taskItem, index) => {
        if (taskItem.id === task.id) {
          this.tasks[index].task = task;
        }
      });
      this.tasksToDisplay = this.getTasksToDisplay(this.tasks);
      this.$root.$emit('refresh-unscheduled-gantt',this.unscheduledTaskList);
      if (this.ganttTasks > 0 ) {
        this.gantt.refresh(this.tasksToDisplay);
      }
    });
  },
  mounted() {
    this.$root.$on('scale-value-changed', value => {
      this.gantt.change_view_mode(value);
      this.ganttScale = value;
    });
  },
  methods: {
    getTasksToDisplay(tasksList) {
      const ganttTasksList = [];
      this.unscheduledTaskList = [];
      if ( tasksList && tasksList.length ) {
        tasksList.forEach((item) => {
          const task ={
            id: '',
            name: '',
            start: '',
            end: '',
            custom_class: '',
            progress: '',
          };
          task.id = `Task-${item.task.id}`;
          task.name = item.task.title;
          if (item.task.startDate != null) { 
            task.start = this.dateFormat (item.task.startDate.time);
            if ( item.task.dueDate === null ) {
              task.end = this.dateFormat (item.task.startDate.time);
            } else {
              task.end = this.dateFormat (item.task.dueDate.time);
            }
          } else {
            if (item.task.dueDate != null) { 
              task.start= this.dateFormat (item.task.dueDate.time );
              task.end = this.dateFormat (item.task.dueDate.time);
            }
          }
          task.custom_class=`bar_${item.task.priority}`;
          if (item.task.startDate != null || item.task.dueDate != null) {
            task.progress= 100;
            ganttTasksList.push(task);
          } else {
            this.unscheduledTaskList.push(item);
          }
        });
      }
      return ganttTasksList;
    },
    dateFormat(date) {
      const dateValue = new Date(date);
      return `${dateValue.getFullYear()}-${dateValue.getMonth()+1}-${dateValue.getDate()}`;
    },
    openTaskDraweryId(taskId) {
      this.$tasksService.getTaskById(taskId).then(data => {
        this.task = data;
        this.$root.$emit('open-task-drawer', this.task);
      }); 
    },
    updatedTaskDueDate(taskId,start, end) {
      this.$tasksService.getTaskById(taskId).then(data => {
        this.task = data;
        if (this.task.id != null) {
          const newStartDate = this.toDateObject(start);
          const newDueDate = this.toDateObject(end);
          this.task.startDate = newStartDate;
          this.task.dueDate = newDueDate;
          this.$taskDrawerApi.updateTask(this.task.id, this.task).then(() => {
            this.$root.$emit('show-alert', {
              type: 'success',
              message: this.$t('alert.success.task.date')
            });
          }).catch(e => {
            console.error('Error when updating task\'s date', e);
            this.$root.$emit('show-alert', {
              type: 'error',
              message: this.$t('alert.error')
            });
          });
        }
      }); 
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
          nanos: 0,
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
    addScrollToDateArea() {
      const target = $('.gantt-dates');
      $('.gantt-grid').scroll(function() {
        target.prop('scrollLeft', this.scrollLeft);
      });
    },
    getTaskAssigneeAndCoworkers(task) {
      const assigneeAndCoworkerArray = [];
      if (task.assignee && !assigneeAndCoworkerArray.includes(task.assignee)) {
        assigneeAndCoworkerArray.push(task.assignee);
      }
      if (task.coworker || task.coworker.length > 0) {
        task.coworker.forEach((coworker) => {
          if (coworker && !assigneeAndCoworkerArray.includes(coworker)){
            assigneeAndCoworkerArray.push(coworker);
          }
        });
      }
      return assigneeAndCoworkerArray;
    },
    avatarToDisplay (task) {
      const assigneeAndCoworkerArray = this.getTaskAssigneeAndCoworkers(task);
      if (assigneeAndCoworkerArray.length > 1) {
        return assigneeAndCoworkerArray.slice(0, 2);
      } else {
        return assigneeAndCoworkerArray;
      }
    },
    popupTaskDates(start, end) {
      return `<span class="popup-date">${this.$t('label.from')}</span> ${start.split('-').reverse().join('-')} <span class="popup-date">${this.$t('label.to')}</span> ${end.split('-').reverse().join('-')}`;
    },
    initGanttChart(tasks) {
      const svgGantt = '<svg id="gantt"></svg>';
      $('#gantt-chart').append(svgGantt);
      const self = this;
      /*global Gantt :true*/
      /*eslint no-undef: 2*/
      this.gantt = new Gantt('#gantt', tasks, {
        bar_height: 18,
        padding: 20,
        bar_corner_radius: 5,
        language: this.lang,
        custom_popup_html: function(task) {
          const taskId = parseInt(task.id.split('-')[1]);
          const taskPopupToDisplay = self.tasks.find((taskItem) => {
            return taskItem.id === taskId;
          });
          const avatarListToDisplay = self.avatarToDisplay(taskPopupToDisplay);
          let avatarToDisplay = '';
          if (avatarListToDisplay.length ) {
            avatarListToDisplay.forEach(avatar => {
              avatarToDisplay += `<a class="assigneeAvatar" style="background-image:url(${avatar.avatar})"></a>`;
            });
          }
          return `<div class="popup-container ${task.custom_class}">
                <p class="popup-title">${task.name}</p>
                <div class="d-flex justify-space-between align-center">
                <div class="avatarToDisplay"> ${avatarToDisplay}</div>
                <div class="taskDates"> ${self.popupTaskDates(task.start, task.end)} </div>
                </div>
              </div>`;
        },
        on_click: function (task) {
          const taskId = task.id.split('-')[1];
          if (taskId) {
            self.openTaskDraweryId(taskId);
          }
          $('.popup-wrapper').css('opacity', '0');
        },
        on_date_change: function(task, start, end) {
          const taskId = task.id.split('-')[1];
          if (taskId) {
            self.updatedTaskDueDate(taskId,start,end);
          }
        },
      });
      this.gantt.change_view_mode(this.ganttScale);
      this.isGanttDisplayed = true;
      this.addScrollToDateArea();
    }
  }
};
</script>