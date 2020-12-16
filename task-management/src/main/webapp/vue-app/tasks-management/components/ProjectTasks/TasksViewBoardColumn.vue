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
      group="people" 
      class="draggable-palceholder" 
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
