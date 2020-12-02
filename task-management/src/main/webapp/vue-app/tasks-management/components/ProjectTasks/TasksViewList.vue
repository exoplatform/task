<template>
  <v-app class="tasksList">
    <v-card
      class="tasksView tasksListItem tasksViewList pt-6"
      flat>
      <v-item-group class="pa-4">
        <div class="ma-0 border-box-sizing">
          <div
            v-for="(status, index) in statusList"
            :key="index"
            class="pt-0 pb-8 px-4 projectTaskItem">
            <tasks-view-list-column
              :status="status"        
              :tasks-list="getTasksByStatus(tasksList,status.name)"
              @updateTaskCompleted="updateTaskCompleted"
              @updateTaskStatus="updateTaskStatus" />
          </div>
      </div></v-item-group>
    </v-card>
  </v-app>
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
    data () {
      return {
        
        tasksStatsStartValue:0,
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
