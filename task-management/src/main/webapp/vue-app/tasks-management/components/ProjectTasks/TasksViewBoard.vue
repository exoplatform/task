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
            <tasks-view-header
              :status="status"
              :view-type="'board'"
              :tasks-number="getTasksByStatus(tasksList,status.name).length"/>
            <v-divider/>
            <task-view-card
              v-for="task in getTasksByStatus(tasksList,status.name)"
              :key="task.task.id"
              :task="task"/>
          </v-col>
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
      }
    }
  }
</script>