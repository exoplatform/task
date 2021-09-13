<template>
  <v-app class="tasksList pt-5">
    <div
      v-for="task in tasks"
      :key="task.id"
      class="tasksListItem">
      <tasks-list-item
        :task="task"
        :show-completed-tasks="showCompletedTasks" />
    </div>
  </v-app>
</template>
<script>
export default {
  props: {
    tasks: {
      type: Array,
      default: () => [],
    },
    showCompletedTasks: {
      type: Boolean,
      default: false,
    },    
  },
};

</script>
