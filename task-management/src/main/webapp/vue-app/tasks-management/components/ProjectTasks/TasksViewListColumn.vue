<template>
  <div :id="status.id">
    <div v-show="groupByStatus">
      <tasks-view-header
        :status="status"
        :view-type="'list'"
        :max-tasks-to-show="maxTasksToShow"
        :tasks-number="tasksList.length"/>
      <v-divider class="py-3"/>
    </div>
    <div :id="'taskView'+status.id">
      <task-view-list-item
        v-for="task in tasksList"
        :key="task.task.id"
        :task="task"
        :show-completed-tasks="showCompletedTasks"
        @update-task-completed="updateTaskCompleted"/>
    </div>
    <!--<draggable
      v-model="tasksList"
      group="people" 
      @start="drag=true" 
      @end="drag=false">
      <task-view-list-item
        v-for="task in tasksList"
        :key="task.task.id"
        :task="task"
        :show-completed-tasks="showCompletedTasks"
        @update-task-completed="updateTaskCompleted"/>
    </draggable>-->
  </div>

</template>
<script>

 
  export default {
    props: {
      tasksList: {
        type: Array,
        default: () => []
      },
      status: {
        type: String,
        default: ""
      },
      showCompletedTasks: {
        type: Boolean,
        default: false
      }
    },
    data() {
    return {
      maxTasksToShow: 6,
      drag: false,
      task:null,
      newStatus:null,
      groupByStatus: false,
    };
  },created() {
    this.$root.$on('filter-group-by-status',tasks =>{
      this.groupByStatus= true;
    });
  },
    methods: {
      updateTaskCompleted(e){
        this.$emit('updateTaskCompleted', e);
      },
    }
  }
</script>
