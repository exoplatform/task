<template>

  <div :id="status.id">
    <task-view-list-item
      v-for="task in tasksList"
      :key="task.task.id"
      :task="task"
      @update-task-completed="updateTaskCompleted"/>
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
      }
    },
    data() {
    return {
      maxTasksToShow: 6,
      drag: false,
      task:null,
      newStatus:null
    };
  },
    methods: {
      updateTaskCompleted(e){
        this.$emit('updateTaskCompleted', e);
      },
    }
  }
</script>
