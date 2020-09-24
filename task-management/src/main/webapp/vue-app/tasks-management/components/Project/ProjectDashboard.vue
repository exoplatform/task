<template>
  <v-app
    id="projectListApplication"
    class="projectAndTasksContainer transparent"
    flat>
    <div v-if="!displayDetails">
      <project-list-toolbar/>
      <project-card-list/>
    </div>
    <div v-else>
      <tasks-view-dashboard :project="project"/>
    </div>
  </v-app>
</template>
<script>
  export default {
    data () {
      return {
        displayDetails : false,
        project: ''
      }
    },
    created() {
      document.addEventListener('showProjectTasks', (event) => {
        if (event && event.detail) {
          this.displayDetails = true;
          this.project =  event.detail;
        }
      });
      document.addEventListener('hideProjectTasks', (event) => {
        this.displayDetails = false;
      });
    },
  }
</script>