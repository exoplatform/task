<template>
  <v-app
    id="projectListApplication"
    class="projectAndTasksContainer transparent"
    flat>
    <div v-show="!displayDetails">
      <project-list-toolbar
        :keyword="keyword"
        :project-filter-selected="projectFilterSelected"
        @keyword-changed="keyword = $event"
        @filter-changed="projectFilterSelected = $event"/>
      <project-card-list
        :keyword="keyword"
        :project-filter-selected="projectFilterSelected"
        :loading-projects="loadingProjects"/>
    </div>
    <div v-show="displayDetails">
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
        projectFilterSelected: 'ALL',
      }
    },
    created() {
      document.addEventListener('showProjectTasks', (event) => {
        if (event && event.detail) {
          this.displayDetails = true;
          this.project =  event.detail;
          window.history.pushState('project', this.project.name, `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest?projectId=${this.project.id}`);
        }
      });
      document.addEventListener('hideProjectTasks', (event) => {
        this.displayDetails = false;
      });
    },
  }
</script>