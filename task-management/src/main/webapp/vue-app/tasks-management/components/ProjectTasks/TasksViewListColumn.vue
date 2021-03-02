<template>
  <div :id="status.id">
    <div v-if="filterByStatus===true">
      <tasks-view-header-status
        :status="status"
        :view-type="'list'"
        :max-tasks-to-show="maxTasksToShow"
        :tasks-number="tasksList.length" />
    </div>
    <div :id="'taskView'+status.id" :class="filterByStatus===true ? 'pt-5 ml-7 mr-2' : ''">
      <task-view-list-item
        v-for="taskItem in tasksList"
        :key="taskItem.task.id"
        :task="taskItem"
        :show-completed-tasks="showCompletedTasks"
        @update-task-completed="updateTaskCompleted" />
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
      default: ''
    },
    showCompletedTasks: {
      type: Boolean,
      default: false
    },
    filterByStatus: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      maxTasksToShow: 6,
      drag: false,
      task: null,
      newStatus: null,
    };
  },
  methods: {
    updateTaskCompleted(e){
      this.$emit('updateTaskCompleted', e);
    },
  }
};
</script>
