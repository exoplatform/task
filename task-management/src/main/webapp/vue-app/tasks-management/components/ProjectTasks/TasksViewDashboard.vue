<template>
  <v-app :id="'projectTask-'+project.id" class="projectTasksDashboard">
    <div class="taskViewBreadcrumb pa-4">
      <a
        class="text-color"
        @click="hideProjectDetails()">
        <i class="uiIcon uiBackIcon"></i>
        <span>{{ project.name }}</span>
      </a>
    </div>
    <tasks-view-toolbar
      :project="project"
      :task-card-tab-view="'#tasks-view-board'"
      :task-list-tab-view="'#tasks-view-list'"
      :task-gantt-tab-view="'#tasks-view-gantt'"
      :tasks-view-tab-model="'tasks-view-board'"
      @taskViewChangeTab="getChangeTabValue"/>
    <v-tabs-items
      v-if="tasksList && tasksList.length">
      <v-tab-item
        v-show="taskViewTabName == 'board'"
        style="display: block"
        eager>
        <tasks-view-board
          :status-list="statusList"
          :tasks-list="tasksList"/>
      </v-tab-item>
      <v-tab-item
        v-show="taskViewTabName == 'list'"
        eager>
        <tasks-view-list
          :status-list="statusList"
          :tasks-list="tasksList"/>
      </v-tab-item>
      <v-tab-item
        v-show="taskViewTabName == 'gantt'"
        eager>
        <tasks-view-gantt
          :tasks-list="tasksList"/>
      </v-tab-item>
    </v-tabs-items>
    <div
      v-else
      class="noTasksProject">
      <div class="noTasksProjectIcon"><i class="uiIcon uiIconTask"></i></div>
      <div class="noTasksProjectLabel"><span>{{ $t('label.noTasks') }}</span></div>
      <div class="noTasksProjectLink"><a href="#">{{ $t('label.addTask') }}</a></div>
    </div>
    <tasks-assignee-coworker-drawer/>
  </v-app>
</template>
<script>
  export default {
    props: {
      project: {
        type: Number,
        default: 0
      }
    },
    data () {
      return {
        taskViewTabName: 'board',
        statusList: [],
        tasksList: []
      }
    },
    watch:{
      project(){
      this.getStatusByProject(this.project.id);
      this.getTasksByProject(this.project.id);
      }
    },
    created() {
      this.$root.$on('task-added', task => {
       this.getTasksByProject(this.project.id);
      });
    },
    methods : {
      hideProjectDetails() {
        window.history.pushState('myprojects', 'My Projects', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest?myprojects`);
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
      getTasksByProject(ProjectId) {
        return this.$tasksService.getTasksByProjectId(ProjectId).then(data => {
          this.tasksList = data;
        });
      },
    }
  }
</script>9a