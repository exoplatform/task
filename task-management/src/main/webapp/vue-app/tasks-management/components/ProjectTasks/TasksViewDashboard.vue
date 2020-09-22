<template>
  <v-app :id="'projectTask-'+projectId" class="projectTasksDashboard">
    <tasks-view-toolbar
      :task-card-tab-view="'#tasks-view-board'"
      :task-list-tab-view="'#tasks-view-list'"
      :task-gantt-tab-view="'#tasks-view-gantt'"
      :tasks-view-tab-model="'tasks-view-board'"
      @taskViewChangeTab="getChangeTabValue"/>
    <v-tabs-items>
      <v-tab-item
        v-show="taskViewTabName == 'board'"
        style="display: block"
        eager>
        <tasks-view-board
          :status-list="statusList"/>
      </v-tab-item>
      <v-tab-item
        v-show="taskViewTabName == 'list'"
        eager>
        <tasks-view-list
          :status-list="statusList"/>
      </v-tab-item>
      <v-tab-item
        v-show="taskViewTabName == 'gantt'"
        eager>
        <tasks-view-gantt/>
      </v-tab-item>
    </v-tabs-items>
  </v-app>
</template>
<script>
  export default {
    props: {
      projectId: {
        type: Number,
        default: 0
      }
    },
    data () {
      return {
        taskViewTabName: 'board',
        statusList: []
      }
    },
    created() {
      this.getStatusByProject(this.projectId);
    },
    methods : {
      hideProjectDetails() {
        document.dispatchEvent(new CustomEvent('hideProjectTasks'));
      },
      getChangeTabValue(value) {
        this.taskViewTabName = value;
      },
      getStatusByProject(ProjectId) {
        return this.$tasksService.getStatusesByProjectId(ProjectId).then(data => {
          this.statusList = data;
        });
      },
    }
  }
</script>