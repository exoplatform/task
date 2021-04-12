<template>
  <div v-if="tasksList && tasksList.length > 0 && getTasksToDisplay(tasksList).length === 0">
    <div class="noTasksProject">
      <div class="noTasksProjectIcon"><i class="uiIcon uiIconTask"></i></div>
      <div class="noTasksProjectLabel"><span>{{ $t('label.noTasks') }}</span></div>
    </div>
  </div>
  <div
    v-else
    class="gantt-chart-container">
    <div class="gantt-left-sidebar">
      <v-btn-toggle
        v-model="view_mode"
        tile
        color="deep-purple accent-3"
        group
        @change="getViewMode($event)">
        <v-btn value="Day">
          Day
        </v-btn>

        <v-btn value="Week">
          Week
        </v-btn>

        <v-btn value="Month">
          Month
        </v-btn>
      </v-btn-toggle>
    </div>
    <svg id="gantt"></svg>
  </div>
</template>
<script>
export default {
  props: {
    tasksList: {
      type: Array,
      default: () => []
    }
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
      view_mode: 'Day',
    };
  },
  mounted() {
    this.tasksToDisplay = this.getTasksToDisplay(this.tasksList);
    this.initGanttChart();
    this.addScrollToDateArea();
  },
  methods: {
    getTasksToDisplay(tasksList) {
      const GanttTasksList = [];
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
          GanttTasksList.push(task);
        }
      });
      return GanttTasksList;
    },
    dateFormat(date) {
      const dateValue = new Date(date);
      return `${dateValue.getFullYear()}-${dateValue.getMonth()}-${dateValue.getDate()}`;
    },
    openTaskDraweryId(taskId) {
      this.$tasksService.getTaskById(taskId).then(data => {
        this.task = data;
        this.$root.$emit('open-task-drawer', this.task);
      }); 
    },
    addScrollToDateArea() {
      const target = $('.gantt-dates');
      $('.gantt-grid').scroll(function() {
        target.prop('scrollLeft', this.scrollLeft);
      });
    },
    getViewMode(value) {
      console.warn('value',value);
      this.gantt.change_view_mode(this.view_mode);
    },
    initGanttChart() {
      const self = this;
      /*global Gantt :true*/
      /*eslint no-undef: 2*/
      this.gantt = new Gantt('#gantt', this.tasksToDisplay, {
        bar_height: 18,
        padding: 20,
        bar_corner_radius: 5,
        language: this.lang,
        on_click: function (task) {
          const taskId = task.id.split('-')[1];
          if (taskId) {
            self.openTaskDraweryId(taskId);
          }
          console.log(task);
        },
      });
      this.gantt.change_view_mode('Day');
    }
  }
};
</script>