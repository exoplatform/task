<template>
  <v-app class="tasksList ma-3">
    <div
      v-for="task in tasks"
      :key="task.id"
      class="tasksListItem">
      <tasks-list-item
        :task="task"/>
    </div>
  </v-app>
</template>
<script>
  export default {
    props: {
      tasks: {
        type: Array,
        default: () => [],
      }
    },
  }
</script>