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
            <tasks-view-header
              :status="status"
              :view-type="'list'"
              :max-tasks-to-show="maxTasksToShow"
              :tasks-number="getTasksByStatus(tasksList,status.name).length"/>
            <v-divider/>
            <task-view-list-item
              v-for="task in getTasksByStatus(tasksList,status.name)"
              :key="task.task.id"
              :task="task"/>
          </div>
          <v-divider/>
        </div>
      </v-item-group>
    </v-card>
  </v-app>
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
      }
    },
    data () {
      return {
        maxTasksToShow: 6,
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
    }
  }
</script>