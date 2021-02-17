<template>
  <v-card
    class="tasksView tasksViewBoard tasksCardsContainer"
    flat>
    <v-item-group class="pb-4 pt-5 px-0">
      <v-container class="pa-0 mx-0">
        <v-row class="ma-0 border-box-sizing tasksViewBoardRowContainer">
          <v-col
            v-for="(status, index) in statusList"
            :key="index"
            class="py-0 px-3 projectTaskItem">
            <tasks-view-board-column
              :project="project"
              :status="status"
              :tasks-list="getTasksByStatus(tasksList,status.name)"
              :index="index"
              :show-completed-tasks="filterTaskCompleted"
              @updateTaskCompleted="updateTaskCompleted"
              @updateTaskStatus="updateTaskStatus"
              @delete-status="deleteStatus"
              @update-status="updateStatus"
              @add-column="addColumn"
              @cancel-add-column="cancelAddColumn"
              @create-status="createStatus"/>
          </v-col>
        </v-row>
      </v-container>
    </v-item-group>
  </v-card>
</template>
<script>
import {updateTask} from '../../../taskDrawer/taskDrawerApi';
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
      filterTaskCompleted: {
        type: Boolean,
        default: false
      }
    },
    data() {
    return {
      index: -1
    };
  },
    methods: {
      getTasksByStatus(items ,statusName) {
        const tasksByStatus = [];
        items.forEach((item) => {
          if(item.task) {
            if (item.task.status) {
              if (item.task.status.name === statusName) {
                tasksByStatus.push(item);
              }
            }
          }
        });
        return tasksByStatus;
      },
      updateTaskCompleted(e){
        if( !this.filterTaskCompleted ) {
          this.$root.$emit('update-task-completed', e);
        }
      },
      updateTaskStatus(task,newStatus){
               // eslint-disable-next-line eqeqeq
              const status = this.statusList.find(s => s.id == newStatus);
              if(status){
               task.status = status;
               this.updateTask(task)
              } 
      },
      updateTask(task) {
        if(task.id!=null){
          updateTask(task.id,task).then(task => {
          this.$root.$emit('show-alert', { type: 'success', message: this.$t('alert.success.task.status') });
        }).catch(e => {
          console.debug("Error when updating task's status", e);
          this.$root.$emit('show-alert',{type:'error',message: this.$t('alert.error')} );
          });
        }
      },
      deleteStatus(status) {
          this.$emit('delete-status', status);
          this.index=-1
      },
      updateStatus(status) {
          this.$emit('update-status', status);
          this.index=-1
      },
      createStatus() {
          this.$emit('create-status');
          this.index=-1
      },
      addColumn(index) {
        if(this.index!==-1){
         this.statusList.splice( this.index, 1)  
        }
        const newStatus = {name:""}
        newStatus.edit=true;
        this.statusList.splice( index, 0, newStatus)
        this.index=index
      },
      cancelAddColumn(index) {
        this.statusList.splice( index, 1)       
      },

    }
  }
</script>
