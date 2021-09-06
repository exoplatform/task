<template>
  <v-app id="TasksCardList" class="tasksCardsContainer">
    <v-card flat>
      <v-item-group class="pa-4">
        <v-container class="pa-0">
          <v-row class="ma-0 border-box-sizing">
            <v-col
              v-for="task in tasks"
              :key="task.id"
              :id="'tasks-'+task.id"
              cols="12"
              md="6"
              lg="4"
              xl="3"
              class="pa-0 projectItem">
              <task-card
                :task="task"
                :show-completed-tasks="showCompletedTasks" />
            </v-col>
          </v-row>
        </v-container>
      </v-item-group>
    </v-card>
  </v-app>
</template>
<script>
export default {
  props: {
    tasks: {
      type: Array,
      default: () =>[],
    },
    showCompletedTasks: {
      type: Boolean,
      default: false,
    },
  },

};
</script>
