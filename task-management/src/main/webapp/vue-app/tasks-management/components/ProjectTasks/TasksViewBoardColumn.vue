<template>
  <div :id="status.id">
    <tasks-view-header
      :status="status"
      :project="project"
      :view-type="'board'"
      :tasks-number="tasksList.length"
      :index="index"
      :status-list-length="statusListLength"
      @delete-status="deleteStatus"
      @update-status="updateStatus"
      @add-status="createStatus"
      @cancel-add-column="cancelAddColumn"
      @move-column="moveColumn"
      @open-quick-add="quickAddTask1=true"
      @add-column="addColumn" />
    <v-divider />
    <div @mousedown="cancelDrag">
      <quick-add-card
        :status="status"
        :quick-add-task="quickAddTask1"
        :task-title="taskTitle1"
        class="status-add-task" 
        @close-quick-form="quickAddTask1=false" />
      <draggable 
        v-model="tasksList"
        :move="checkMove"
        :animation="200"
        :key="draggableKey"
        ghost-class="ghost-card"
        class="draggable-palceholder taskBoardColumn"
        handle=".taskBoardCardItem"
        :options="{group:'status'}"
        :class="filterNoActive && 'taskBoardNoFilterColumn'"
        @end="drag=true">
        <task-view-card
          :id="idViewCard"
          v-for="taskItem in tasksList"
          :key="taskItem.task.id"
          :task="taskItem"
          :show-completed-tasks="showCompletedTasks"
          @update-task-completed="updateTaskCompleted" />
    
        <quick-add-card 
          :status="status" 
          :quick-add-task="quickAddTask"
          :task-title="taskTitle"
          @close-quick-form="quickAddTask=false" />

        <v-btn 
          v-if="!quickAddTask"
          class="btn px-2 quickAddNewTaskButton"
          @click="quickAddTask=true">
          <v-icon dark class="d-block d-sm-none">mdi-plus</v-icon>
          <span class="d-none font-weight-regular d-sm-inline">
            + {{ $t('label.addTask') }}
          </span>
        </v-btn>
      </draggable>
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
    index: {
      type: Number,
      default: 0
    },
    project: {
      type: Number,
      default: 0
    },
    statusListLength: {
      type: Number,
      default: 0
    },
    showCompletedTasks: {
      type: Boolean,
      default: false
    },
    filterNoActive: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      quickAddTask: false,
      quickAddTask1: false,
      taskTitle: '',
      idViewCard: `DatePicker${parseInt(Math.random() * 10000)}`,
      taskTitle1: '',
      drag: false,
      task: null,
      newStatus: null,
      draggableKey: 1,
    };
  },
  created() {
    this.$root.$on('close-quick-task-form', () => {
      this.closeForm();
    });
    this.$root.$on('task-assignee-updated', () => {
      this.closeForm();
    });
  },
  watch: {
    drag(val) {
      if (val&&this.task&&this.newStatus&&this.task.status.name !== this.newStatus){
        this.$emit('updateTaskStatus', this.task,this.newStatus);
        Array.from(document.getElementsByClassName('draggable-palceholder')).forEach(element => element.style.backgroundColor= '#FFFFFF');
        this.drag = false;
      }},
  },
  methods: {
    cancelDrag() {
      if (event.target && !$(event.target).parents(`#${this.idViewCard}`).length) {
        return event.preventDefault ? event.preventDefault() : event.returnValue = false;
      }
    },
    updateTaskCompleted(task) {
      if (!this.showCompletedTasks && task.completed) {
        const index = this.tasksList.findIndex(taskEl => taskEl.id === task.id);
        this.tasksList.splice(index, 1);
        setTimeout(() => {
          this.reRenderTasks();
        }, 500);
      }
    },
    checkMove(evt){
      if (evt){
        Array.from(document.getElementsByClassName('draggable-palceholder')).forEach(element => element.style.backgroundColor= '#FFFFFF');
        Array.from(evt.to.parentElement.getElementsByClassName('draggable-palceholder')).forEach(element => element.style.backgroundColor= '#f2f2f2');
        this.task = evt.draggedContext.element.task;
        this.newStatus = evt.relatedContext.component.$parent.status.name;
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
     
    moveColumn(index,orientation) {  
      this.$emit('move-column',index,orientation);
    },
    cancelAddColumn(index) {
      this.$emit('cancel-add-column',index);         
    },
    closeForm() {
      this.quickAddTask=false;
      this.quickAddTask1=false;
      this.taskTitle='';
      this.taskTitle1='';
    },
    reRenderTasks() {
      this.draggableKey += 1;
    },  
  }
};

</script>