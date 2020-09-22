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
      <tasks-view-dashboard :project-id="projectId"/>
    </div>
  </v-app>
</template>
<script>
  export default {
    data () {
      return {
        displayDetails : false,
        projectId: ''
      }
    },
    created() {
      document.addEventListener('showProjectTasks', (event) => {
        if (event && event.detail) {
          this.displayDetails = true;
          this.projectId =  event.detail;
        }
      });
      document.addEventListener('hideProjectTasks', (event) => {
        this.displayDetails = false;
      });
    },
  }
</script>