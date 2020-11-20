<template>
  <v-card
    class="tasksView tasksViewBoard tasksCardsContainer pt-6"
    flat>
    <v-item-group class="pa-4">
      <v-container class="pa-0">
        <v-row class="ma-0 border-box-sizing">
          <v-col
            v-for="(status, index) in statusList"
            :key="index"
            class="py-0 px-4 projectTaskItem">
            <tasks-view-board-column
              v-col
              :status="status"        
              :tasks-list="getTasksByStatus(tasksList,status.name)"
              @updateTaskCompleted="updateTaskCompleted"
              @updateTaskStatus="updateTaskStatus" />
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
      }
    },
    methods: {
      getTasksByStatus(items ,statusName) {
        const tasksByStatus = [];
        items.forEach((item) => {
          if(item.task.status) {
            if(item.task.status.name ===  statusName) {
              tasksByStatus.push(item);
            }
          }
        });
        return tasksByStatus;
      },
      updateTaskCompleted(e){
        window.setTimeout(() => this.tasksList = this.tasksList.filter((t) => t.task.id !== e.id), 500);

      },
      updateTaskStatus(task,newStatus){
              const status = this.statusList.find(s => s.name === newStatus);
              if(status){
               task.status = status;
               this.updateTask(task)
              } 
      },
      updateTask(task) {
        if(task.id!=null){
          updateTask(task.id,task);
/*           window.setTimeout(() => {
             this.$root.$emit('task-added', this.task)
          }, 200); */
        }
      },
    }
  }
</script>
