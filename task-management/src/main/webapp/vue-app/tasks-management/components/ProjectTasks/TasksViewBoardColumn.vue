<template>
  <div :id="status.name">
    <tasks-view-header
      :status="status"
      :view-type="'board'"
      :tasks-number="tasksList.length"/>
    <v-divider/>
    <draggable 
      v-model="tasksList" 
      :move="checkMove"
      group="people" 
      @start="drag=true" 
      @end="drag=false">
      <task-view-card
        v-for="task in tasksList"
        :key="task.task.id"
        :task="task"
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
      }
    },
    data() {
    return {
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
