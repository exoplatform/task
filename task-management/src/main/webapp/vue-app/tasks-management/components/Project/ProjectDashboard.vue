<template>
  <v-app
    id="projectListApplication"
    class="projectAndTasksContainer transparent"
    flat>
    <div v-if="!displayDetails">
      <project-list-toolbar
        :keyword="keyword"
        @keyword-changed="keyword = $event"/>
      <project-card-list
        :keyword="keyword"
        :loading-projects="loadingProjects"/>
    </div>
    <div v-else>
      <tasks-view-dashboard :project="project"/>
    </div>
    <project-manager-drawer />
  </v-app>
</template>
<script>
  export default {
    data () {
      return {
        displayDetails : false,
        project: '',
        keyword: null,
        loadingProjects: false,
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