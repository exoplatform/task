<template>
  <div v-if="tasksList && tasksList.length > 0 && getTasksToDisplay(tasksList).length === 0">
    <div class="noTasksProject">
      <div class="noTasksProjectIcon"><i class="uiIcon uiIconTask"></i></div>
      <div class="noTasksProjectLabel"><span>{{ $t('label.noTasks') }}</span></div>
    </div>
  </div>
  <div
    v-else
    class="gantt-chart-container d-flex">
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
      lang: eXo && eXo.env.portal.language || 'en',
      fullDateFormat: {
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
      },
      tasksToDisplay: []
    };
  },
  mounted() {
    this.tasksToDisplay = this.getTasksToDisplay(this.tasksList);
    this.initGanttChart();
    const target = $('.gantt-tasks-title');
    $('.gantt-container').scroll(function() {
      target.prop('scrollTop', this.scrollTop);
    });

  },
  methods: {
    getTasksTitle(tasksList) {
      const taskByElement = [];
      tasksList.forEach((element) => {
        taskByElement.push(`${element[1]}~${element[4]}`);
      });
      return taskByElement;
    },
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
          task.progress= 50;
          GanttTasksList.push(task);
        }
      });
      return GanttTasksList;
    },
    dateFormat(date) {
      const dateValue = new Date(date);
      return `${dateValue.getFullYear()}-${dateValue.getMonth()}-${dateValue.getDate()}`;
    },
    initGanttChart() {
      /*global Gantt :true*/
      /*eslint no-undef: 2*/
      const gantt = new Gantt('#gantt', this.tasksToDisplay, {
        bar_height: 15,
        bar_corner_radius: 5,
        language: this.lang
      });
      const new_height = gantt.$svg.getAttribute('height') - 100;
      gantt.$svg.setAttribute('height', new_height);
      gantt.change_view_mode('Week');
    }
  }
};
</script>