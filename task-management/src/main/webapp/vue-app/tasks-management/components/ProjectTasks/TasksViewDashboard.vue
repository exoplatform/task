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
      :status-list="statusList"
      :task-card-tab-view="'#tasks-view-board'"
      :task-list-tab-view="'#tasks-view-list'"
      :task-gantt-tab-view="'#tasks-view-gantt'"
      :tasks-view-tab-model="'tasks-view-board'"
      @keyword-changed="filterByKeyword"
      @taskViewChangeTab="getChangeTabValue"
      @filter-task-dashboard="filterTaskDashboard"
      @reset-filter-task-dashboard="resetFiltertaskDashboard"/>
    <div v-if="filterProjectActive">
      <div v-for="(project,i) in groupName.projectName" :key="project.name">

        <div v-if=" project.value && project.value.displayName" class="d-flex align-center assigneeFilter">

          <exo-user-avatar
            :username="project.value.username"
            :fullname="project.value.displayName"
            :avatar-url="project.value.avatar"
            :title="project.value.displayName"
            :size="26"
            class="pr-2"/>
          <span class="amount-item">({{ tasksList[i].length }})</span>

        </div>
        <div
          v-else
          class="d-flex align-center assigneeFilter">
          <span class="nameGroup">{{ $t(getNameGroup(project.name)) }}</span>
          <span class="amount-item">({{ tasksList[i].length }})</span>
        </div>

        <div
          v-show="taskViewTabName == 'board'"
          style="display: block"
          eager>
          <tasks-view-board
            :status-list="statusList"
            :tasks-list="tasksList[i]"/>
        </div>
        <div
          v-show="taskViewTabName == 'list'"
          eager>

          <tasks-view-list
            :status-list="statusList"
            :tasks-list="tasksList[i]"/>
        </div>
      </div>
    </div>

    <v-tabs-items
      v-show="!filterProjectActive"
      v-if="tasksList && tasksList.length"
      :key="id">
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
      },
    },
    data () {
      return {
        keyword: null,
        taskViewTabName: 'board',
        statusList: [],
        tasksList: [],
        groupName:null,
        filterProjectActive:false,
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
          this.filterProjectActive=false;

        })

      },
      resetFiltertaskDashboard(){
        this.getTasksByProject(this.project.id,"");
      },
      filterTaskDashboard(e){
        const tasks=e.tasks;
        tasks.showCompleteTasks=e.showCompleteTasks;
        if (tasks.groupBy==='completed'){
          tasks.showCompleteTasks=true;
        }
        return this.$tasksService.filterTasksList(e.tasks,'','',e.filterLabels.labels,0,0,this.project.id).then(data => {
          if(data.projectName){
            this.filterProjectActive=true
            this.tasksList = data && data.tasks || [];
            this.groupName=data;
          }
          else {
            this.tasksList = data && data.tasks || [];
            this.filterProjectActive=false
          }

        })
      },
      getNameGroup(name){
        if (name==='Unassigned'){
          return 'label.unassigned'
        }else if(name==='No Label'){
          return 'label.noLabel'
        }else if(name==='No due date'){
          return 'label.noDueDate'
        }else if(name==='Completed'){
          return 'label.task.completed'
        }else if(name==='Uncompleted'){
          return 'label.task.uncompleted'
        }else
          {return name}

      }
    }
  }
</script>
