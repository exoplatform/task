<template>
  <div :id="status.id">
    <tasks-view-header
      :status="status"
      :project="project"
      :view-type="'board'"
      :tasks-number="tasksList.length"
      :index="index"
      @delete-status="deleteStatus"
      @update-status="updateStatus"
      @add-status="createStatus"
      @cancel-add-column="cancelAddColumn"
      @add-column ="addColumn"/>
    <v-divider/>
    <draggable 
      v-model="tasksList" 
      :move="checkMove"
      :animation="200"
      ghost-class="ghost-card"
      group="people"
      class="draggable-palceholder taskBoardColumn"
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
      },
      index: {
        type: Number,
        default: 0
      },
      project: {
        type: Number,
        default: 0
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
        if(!val&&this.task&&this.newStatus&&this.task.status.id !== this.newStatus){
             this.$emit('updateTaskStatus', this.task,this.newStatus);
             document.getElementsByClassName("taskBoardColumn").forEach(element => element.style.backgroundColor= "#FFFFFF");
        }},
   },
    methods: {
      updateTaskCompleted(e){
        this.$emit('updateTaskCompleted', e);
      },
      checkMove(evt){
        if(evt){
          document.getElementsByClassName("taskBoardColumn").forEach(element => element.style.backgroundColor= "#FFFFFF");
          evt.to.parentElement.getElementsByClassName("taskBoardColumn").forEach(element => element.style.backgroundColor= "#f2f2f2");
          this.task = evt.draggedContext.element.task
          this.newStatus = evt.to.parentElement.id;
        }
      },
    deleteStatus(status) {
          this.$emit('delete-status', status);
      },
    updateStatus(status) {
          this.$emit('update-status', status);
      },
    createStatus() {
          this.$emit('create-status');
      },
     
    addColumn(index) {  
        this.$emit('add-column',index);
      },
    cancelAddColumn(index) {
          this.$emit('cancel-add-column',index);         
      },  
  }
    }

</script>
<style>
  @keyframes beginDrag {
    from {
      transform: rotate(0deg)
    }
    10% {
      -webkit-transform: rotate(-12deg);
      transform: rotate(-12deg)
    }
    30% {
      -webkit-transform: rotate(8deg);
      transform: rotate(8deg)
    }
    55% {
      -webkit-transform: rotate(-5deg);
      transform: rotate(-5deg)
    }
    80% {
      -webkit-transform: rotate(3deg);
      transform: rotate(3deg)
    }
    to {
      -webkit-transform: rotate(-1deg);
      transform: rotate(-1deg)
    }
  }
  .ghost-card {
    opacity: 0.5;
    background: #F7FAFC;
    border: 1px solid #578dc9;
    animation: beginDrag 1s ease forwards;
  }
</style>
