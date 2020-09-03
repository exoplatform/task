<template>
  <v-app id="tasksList" class="ma-3">
    <div
      v-for="task in tasks"
      :key="task.id"
      class="tasksListItem pa-4">
      <tasks-list-item
        :task="task"/>
    </div>
  </v-app>
</template>
<script>
  export default {
    data () {
      return {
        tasks: []
      }
    },
    created() {
      this.getTasksList();
      console.log('tessjhfjdf dhjkhdjgkhdf dhgjdhgjkd');
      console.log(this.tasks);
    },
    methods: {
      getTasksList() {
        return this.$tasksService.getMyTasksList().then(data => {
          this.tasks = data;
        });
      }
    }
  }
</script>