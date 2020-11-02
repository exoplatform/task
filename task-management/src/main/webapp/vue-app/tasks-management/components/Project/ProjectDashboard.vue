<template>
  <v-app
    id="projectListApplication"
    class="projectAndTasksContainer transparent"
    flat>
    <div v-show="!displayDetails">
      <project-list-toolbar
        :keyword="keyword"
        @keyword-changed="keyword = $event"/>
      <project-card-list
        :keyword="keyword"
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
      }
    },
    created() {
    //  window.history.pushState('myprojects', 'My Projects', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest?myprojects`);

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