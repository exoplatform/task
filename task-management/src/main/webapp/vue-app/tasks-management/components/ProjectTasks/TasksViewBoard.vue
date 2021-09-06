<template>
  <v-card
    class="tasksView tasksViewBoard tasksCardsContainer"
    flat>
    <v-item-group class="pb-0 pt-5 px-0">
      <v-container class="pa-0 mx-0">
        <v-row class="ma-0 border-box-sizing tasksViewBoardRowContainer">
          <div v-if="project.canManage">
            <draggable
              :move="checkMoveStatus"
              :list="statusList"
              :animation="200"
              ghost-class="ghost-card"
              class="d-flex"
              @start="dragStatus=true"
              @end="dragStatus=false">
              <v-col
                v-for="(status, index) in statusList"
                :key="index"
                :id="status.id"
                class="py-0 px-3 projectTaskItem">
                <tasks-view-board-column
                  :project="project"
                  :status="status"
                  class="draggable-palceholder"
                  :tasks-list="getTasksByStatus(tasksList,status.name)"
                  :index="index"
                  :show-completed-tasks="showCompletedTasks"
                  :status-list-length="statusList.length"
                  :filter-no-active="filterNoActive"
                  @updateTaskCompleted="updateTaskCompleted"
                  @updateTaskStatus="updateTaskStatus"
                  @delete-status="deleteStatus"
                  @update-status="updateStatus"
                  @add-column="addColumn"
                  @move-column="moveColumn"
                  @cancel-add-column="cancelAddColumn"
                  @create-status="createStatus" />
              </v-col>
            </draggable>
          </div>
          <div v-else class="d-flex">
            <v-col
              v-for="(status, index) in statusList"
              :key="index"
              :id="status.id"
              class="py-0 px-3 projectTaskItem">
              <tasks-view-board-column
                :project="project"
                :status="status"
                :tasks-list="getTasksByStatus(tasksList,status.name)"
                :index="index"
                :show-completed-tasks="showCompletedTasks"
                :status-list-length="statusList.length"
                :filter-no-active="filterNoActive"
                @updateTaskCompleted="updateTaskCompleted"
                @updateTaskStatus="updateTaskStatus"
                @delete-status="deleteStatus"
                @update-status="updateStatus"
                @add-column="addColumn"
                @move-column="moveColumn"
                @cancel-add-column="cancelAddColumn"
                @create-status="createStatus" />
            </v-col>
          </div>
        </v-row>
      </v-container>
    </v-item-group>
  </v-card>
</template>
<script>
export default {
  props: {
    statusList: {
      type: Array,
      default: () => []
    },
    tasksList: {
      type: Array,
      default: () => []
    },
    project: {
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
    },
  },
  data() {
    return {
      indexItem: -1,
      dragStatus: false,
      indexStatusFrom: 0,
      indexStatusTo: 0,
    };
  },
  watch: {
    dragStatus(val) {
      if (!val){
        this.$emit('move-status',this.indexStatusTo);
        Array.from(document.getElementsByClassName('draggable-palceholder')).forEach(element => element.style.backgroundColor= '#FFFFFF');
      }},
  },
  created(){
    this.$root.$on('update-task-project', task =>{
      this.updateTaskProject(task);
    });
  },
  methods: {
    updateTaskProject(task){
      this.tasksList = this.tasksList.filter(item => item.id !== task.id);
    },
    checkMoveStatus(evt){
      if (evt){
        Array.from(document.getElementsByClassName('draggable-palceholder')).forEach(element => element.style.backgroundColor= '#FFFFFF');
        Array.from(evt.to.parentElement.getElementsByClassName('draggable-palceholder')).forEach(element => element.style.backgroundColor= '#f2f2f2');
        this.indexStatusFrom = evt.draggedContext.index;
        this.indexStatusTo = evt.draggedContext.futureIndex;
      }
    },
    getTasksByStatus(items ,statusName) {
      const tasksByStatus = [];
      items.forEach((item) => {
        if (item.task && item.task.status && item.task.status.name === statusName) {
          tasksByStatus.push(item);
        }
      });
      return tasksByStatus;
    },
    updateTaskCompleted(e){
      if ( !this.showCompletedTasks ) {
        this.$root.$emit('update-task-completed', e);
      }
    },
    updateTaskStatus(task,newStatus){
      // eslint-disable-next-line eqeqeq
      const status = this.statusList.find(s => s.name == newStatus);
      if (status){
        task.status = status;
        this.updateTask(task);
      } 
    },
    updateTask(task) {
      if (task.id!=null){
        this.$taskDrawerApi.updateTask(task.id,task).then( () => {
          this.$root.$emit('show-alert', { type: 'success', message: this.$t('alert.success.task.status') });
        }).catch(e => {
          console.error('Error when updating task\'s status', e);
          this.$root.$emit('show-alert',{type: 'error',message: this.$t('alert.error')} );
        });
      }
    },
    deleteStatus(status) {
      this.$emit('delete-status', status);
      this.indexItem=-1;
    },
    updateStatus(status) {
      this.$emit('update-status', status);
      this.indexItem=-1;
    },
    createStatus() {
      this.$emit('create-status');
      this.indexItem=-1;
    },
    addColumn(index) {
      if (this.indexItem!==-1){
        this.statusList.splice( this.indexItem, 1);  
      }
      const newStatus = {name: ''};
      newStatus.edit=true;
      this.statusList.splice( index, 0, newStatus);
      this.indexItem=index;
    },
    moveColumn(fromIndex,toIndex) {  
      const element = this.statusList[fromIndex];
      this.statusList.splice(fromIndex, 1);
      this.statusList.splice(toIndex, 0, element);
      this.$emit('move-status');
    },
    cancelAddColumn(index) {
      this.statusList.splice( index, 1);       
    },

  }
};
</script>
