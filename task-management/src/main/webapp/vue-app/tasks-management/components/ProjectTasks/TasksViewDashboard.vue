<template>
  <v-app :id="'projectTask-'+project.id" class="projectTasksDashboard">
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="deleteConfirmMessage"
      :title="$t('popup.deleteTask')"
      :ok-label="$t('label.ok')"
      :cancel-label="$t('popup.cancel')"
      @ok="deleteConfirm()" />
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
      @keyword-changed="filterByKeyword"
      @taskViewChangeTab="getChangeTabValue"
      @filter-task-dashboard="filterTaskDashboard"
      @reset-filter-task-dashboard="resetFiltertaskDashboard"/>
    <v-tabs-items
      v-if="tasksList && tasksList.length">
      <v-tab-item
        v-show="taskViewTabName == 'board'"
        style="display: block"
        eager>
        <tasks-view-board
          :status-list="statusList"
          :tasks-list="tasksList"
          @update-status="updateStatus"
          @create-status="createStatus"
          @delete-status="deleteStatus"/>
      </v-tab-item>
      <v-tab-item
        v-show="taskViewTabName == 'list'"
        eager>
        <tasks-view-list
          :status-list="statusList"
          :tasks-list="tasksList"/>
      </v-tab-item>
      <!--<v-tab-item
        v-show="taskViewTabName == 'gantt'"
        eager>
        <tasks-view-gantt
          :tasks-list="tasksList"/>
      </v-tab-item>-->
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
        keyword: null,
        taskViewTabName: 'board',
        deleteConfirmMessage: null,
        statusList: [],
        tasksList: [],
        status:null
      }
    },
    watch:{
      project(){
      this.getStatusByProject(this.project.id);
      this.getTasksByProject(this.project.id,"");
      }
    },
    created() {
      this.$root.$on('task-added', task => {
       this.getTasksByProject(this.project.id,"");
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
      filterByKeyword(keyword){
        this.getTasksByProject(this.project.id,keyword);
      },
      getTasksByProject(ProjectId,query) {
        const tasks = {
          query: query,
          offset: 0,
          limit: 0,
          showCompleteTasks:false,
        };
        return this.$tasksService.filterTasksList(tasks,'','','',0,0,ProjectId).then(data => {
          this.tasksList = data && data.tasks || [];
        })

      },
      resetFiltertaskDashboard(){
        this.getTasksByProject(this.project.id,"");
      },
      filterTaskDashboard(e){
        const tasks=e.tasks;
        tasks.showCompleteTasks=e.showCompleteTasks;
        return this.$tasksService.filterTasksList(e.tasks,'','','',0,0,this.project.id).then(data => {
          this.tasksList = data && data.tasks || [];
        })
      },
      deleteStatus(status) {
        this.deleteConfirmMessage = `${this.$t('popup.msg.deleteTask')} : ${status.name}? `;
        this.status=status
        this.$refs.deleteConfirmDialog.open();
      },
      deleteConfirm() {
         return this.$statusService.deleteStatus(this.status.id)
      },
      updateStatus() {
         return this.$statusService.updateStatus(this.status).then(resp => {
           this.getStatusByProject(this.project.id) 
        }) 
      },
      createStatus() {
        this.statusList.forEach(function (element, index) {
        if(!element.project){
          element.project=this.project
        }
        element.rank=index
        },  this);
        return this.$statusService.createStatus(this.statusList).then(resp => {
           this.getStatusByProject(this.project.id) 
        }) 
      },
    }
  }
</script>