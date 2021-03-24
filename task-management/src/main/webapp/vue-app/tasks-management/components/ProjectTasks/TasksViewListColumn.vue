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
      <div :id="status.id">
        <draggable
          v-model="tasksList"
          :move="checkMove"
          group="people"
          @start="drag=true"
          @end="drag=false">
          <task-view-list-item
            v-for="tasks in tasksList"
            :key="tasks.task.id"
            :task="tasks"
            :show-completed-tasks="showCompletedTasks"
            @update-task-completed="updateTaskCompleted" />
        </draggable>
      </div>
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
  watch: {
    drag(val) {
      if (!val&&this.task&&this.newStatus&&this.task.status.id.toString() !== this.newStatus){
        this.$emit('updateTaskStatus', this.task,this.newStatus);
      }
    },
  },
  methods: {
    updateTaskCompleted(e){
      this.$emit('updateTaskCompleted', e);
    },
    checkMove(evt){
      if (evt){
        this.task = evt.draggedContext.element.task;
        this.newStatus = evt.to.parentElement.id;
      }

    },
  }
};
</script>
