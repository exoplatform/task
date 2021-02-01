<template>
  <v-app class="tasksList">
    <v-card
      class="tasksView tasksListItem tasksViewList pt-5"
      flat>
      <v-item-group>
        <div class="ma-0 border-box-sizing">
          <div
            class="pt-0 pb-8 px-3 projectTaskItem">
            <tasks-view-list-column
              :status="statusList"
              :tasks-list="tasksList"
              :show-completed-tasks="filterTaskCompleted"
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
      },
      filterTaskCompleted: {
        type: Boolean,
        default: false
      }
    },
    data () {
      return {
        tasksStatsStartValue:0
      }
    },
    mounted() {
      this.$root.$on('deleteTask', (event) => {
        if (event && event.detail) {
          this.tasksList = this.tasksList.filter((t) => t.id !== event.detail);
        }
      });
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
        window.setTimeout(() => this.tasksList = this.tasksList.filter((t) => t.task.id !== e.id), 500);
      }
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
