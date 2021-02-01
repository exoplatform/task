<template>

  <div :id="status.id">
    <draggable 
      v-model="tasksList" 
      :move="checkMove"
      group="people" 
      @start="drag=true" 
      @end="drag=false">
      <task-view-list-item
        v-for="task in tasksList"
        :key="task.task.id"
        :task="task"
        :show-completed-tasks="showCompletedTasks"
        @update-task-completed="updateTaskCompleted"/>
    </draggable>  
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
      newStatus:null
    };
  },
    watch: {
      drag(val) {
        if(!val&&this.task&&this.newStatus&&this.task.status.name !== this.newStatus){
             this.$emit('updateTaskStatus', this.task,this.newStatus);
        }
      },
   },
    methods: {
      updateTaskCompleted(e){
        this.$emit('updateTaskCompleted', e);
      },
      checkMove(evt){
         if(evt){
      this.task = evt.draggedContext.element.task
      this.newStatus = evt.to.parentElement.id
       } 
      
    },

    }
  }
</script>
