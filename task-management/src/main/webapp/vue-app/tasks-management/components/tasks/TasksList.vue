<template>
  <v-app class="tasksList ma-3">
    <div
      v-for="task in tasks"
      :key="task.id"
      class="tasksListItem">
      <tasks-list-item
        :task="task"
        @update-taskCompleted="updateTaskCompleted"/>
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
    methods:{
      updateTaskCompleted(e){
        window.setTimeout(() => this.tasks = this.tasks.filter((t) => t.task.id !== e.id), 500);
      },
    }
  }

</script>
