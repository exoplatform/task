<template>
  <v-app
    id="projectListApplication"
    class="projectAndTasksContainer pa-4 pb-2 transparent"
    flat>
    <div v-show="!displayDetails">
      <project-list-toolbar
        :keyword="keyword"
        :project-filter-selected="projectFilterSelected"
        :space-name="spaceName"
        @keyword-changed="keyword = $event"
        @filter-changed="projectFilterSelected = $event" />
      <project-card-list
        :keyword="keyword"
        :space-name="spaceName"
        :project-filter-selected="projectFilterSelected"
        :loading-projects="loadingProjects" />
    </div>
    <div v-show="displayDetails">
      <tasks-view-dashboard :project="project" />
    </div>
    <project-manager-drawer />
  </v-app>
</template>
<script>
export default {
  props: {
    spaceName: {
      type: String,
      default: '',
    },
    displayDetails: {
      type: Boolean,
      default: false,
    }
  },
  data () {
    return {
      project: '',
      keyword: null,
      loadingProjects: false,
      projectFilterSelected: 'ALL',
    };
  },
  created() {
    document.addEventListener('showProjectTasks', (event) => {
      if (event && event.detail) {
        this.project =  event.detail;
        window.setTimeout(() => {
          this.$root.$emit('set-url', {type: 'project',id: this.project.id});
        }, 200);
      }
    });
    document.addEventListener('hideProjectTasks', () => {
      this.displayDetails = false;
    });
  },
};
</script>