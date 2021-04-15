<template>
  <v-app
    :id="'projectTask-'+project.id"
    :class="taskViewTabName === 'board' && !filterProjectActive && 'projectTaskBoardContainer'"
    class="projectTasksDashboard">
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="deleteConfirmMessage"
      :title="$t('popup.deleteStatus')"
      :ok-label="$t('label.ok')"
      :cancel-label="$t('popup.cancel')"
      @ok="deleteConfirm()" />
    <div class="projectTasksWrapper d-flex justify-space-between">
      <div class="taskViewBreadcrumb px-0 pt-1 pb-5">
        <a
          class="text-color"
          @click="hideProjectDetails()">
          <i class="uiIcon uiBackIcon"></i>
          <span>{{ project.name }}</span>
        </a>
      </div>
      <div class="projectTasksTabFilter">
        <tasks-view-toolbar
          :allow-gantt="allowGantt"
          :project="project"
          :status-list="statusList"
          :task-card-tab-view="'#tasks-view-board'"
          :task-list-tab-view="'#tasks-view-list'"
          :task-gantt-tab-view="'#tasks-view-gantt'"
          @keyword-changed="filterByKeyword"
          @taskViewChangeTab="getChangeTabValue"
          @filter-task-dashboard="filterTaskDashboard"
          @reset-filter-task-dashboard="resetFiltertaskDashboard" />
      </div>
    </div>
    <div v-if="filterProjectActive && groupName && groupName.projectName" class="px-0 ">
      <div 
        v-for="(projectItem,i) in groupName.projectName" 
        :key="projectItem.name" 
        class="pt-5">
        <div
          v-if=" projectItem.value && projectItem.value.displayName && projectItem.name!==''"
          class="d-flex align-center mr-3 assigneeFilter pointer">
          <a
            class="toggle-collapse-group"
            href="#"
            @click="showDetailsTask(projectItem.rank)">
            <i
              :id="'uiIconMiniArrowDown'+projectItem.rank"
              class="uiIcon uiIconMiniArrowDown"
              style="display: block">
            </i>
            <i
              :id="'uiIconMiniArrowRight'+projectItem.rank"
              class="uiIcon  uiIconMiniArrowRight"
              style="display: none">
            </i>
          </a>
          <exo-user-avatar
            v-if="projectItem.value.avatar"
            :username="projectItem.value.username"
            :fullname="projectItem.value.displayName"
            :avatar-url="projectItem.value.avatar"
            :title="projectItem.value.displayName"
            :size="26"
            class="pe-2" />
          <span class="amount-item">({{ tasksList[i].length }})</span>

          <hr
            role="separator"
            aria-orientation="horizontal"
            class="my-0 v-divider theme--light">
          <i
            v-if="taskViewTabName==='list'"
            icon
            small
            class="uiIconSocSimplePlus d-flex"
            @click="openTaskDrawer()">
          </i>
        </div>
        <div
          v-else
          class="d-flex align-center mr-3 assigneeFilter pointer">
          <a
            :id="'iconTask'+projectItem.rank"
            class="toggle-collapse-group"
            href="#"
            @click="showDetailsTask(projectItem.rank)">
            <i
              :id="'uiIconMiniArrowDown'+projectItem.rank"
              class="uiIcon uiIconMiniArrowDown"
              style="display: block">
            </i>
            <i
              :id="'uiIconMiniArrowRight'+projectItem.rank"
              class="uiIcon  uiIconMiniArrowRight"
              style="display: none">
            </i>
          </a>
          <div v-if="projectItem.name==='Unassigned' || projectItem.name===''" class="defaultAvatar">
            <img :src="defaultAvatar">
          </div>

          <span class="nameGroup">{{ $t(getNameGroup(projectItem.name)) }}</span>
          <span class="amount-item">({{ tasksList[i].length }})</span>
          <hr
            role="separator"
            aria-orientation="horizontal"
            class="my-0 v-divider theme--light">
          <i
            v-if="taskViewTabName==='list'"
            icon
            small
            class="uiIconSocSimplePlus d-flex"
            @click="openTaskDrawer()">
          </i>
        </div>
        <div :id="'taskView'+projectItem.rank" class="view-project-group-sort">
          <div
            v-show="taskViewTabName == 'board'"
            style="display: block"
            eager>
            <tasks-view-board
              :project="project"
              :status-list="statusList"
              :tasks-list="tasksList[i]"
              @update-status="updateStatus"
              @create-status="createStatus"
              @move-status="moveStatus"
              @delete-status="deleteStatus" />
          </div>
          <div
            v-show="taskViewTabName == 'list'"
            eager>
            <tasks-view-list
              :project="project"
              :status-list="statusList"
              :tasks-list="tasksList[i]" />
          </div>
        </div>
      </div>
    </div>
    <v-tabs-items
      v-show="!filterProjectActive"
      v-if="!loadingTasks"
      :key="id">
      <div
        v-show="taskViewTabName == 'board'"
        style="display: block"
        eager>
        <tasks-view-board
          :project="project" 
          :status-list="statusList"
          :tasks-list="tasksList"
          :filter-task-completed="filterAsCompleted"
          :filter-no-active="true"
          @update-status="updateStatus"
          @create-status="createStatus"
          @move-status="moveStatus"
          @delete-status="deleteStatus" />
      </div>
      <div
        v-show="taskViewTabName == 'list'"
        eager>
        <tasks-view-list
          :project="project"
          :status-list="statusList"
          :tasks-list="tasksList"
          :filter-task-completed="filterAsCompleted"
          :filter-by-status="filterByStatus=true"
          @update-status="updateStatus" />
      </div>
      <div
        v-show="taskViewTabName == 'gantt' && allowGantt"
        eager>
        <tasks-view-gantt :tasks-list="tasksList" />
      </div>
    </v-tabs-items>

    <div class="ma-0 border-box-sizing">
      <v-btn
        v-if="loadingTasks"
        :loading="loadingTasks"
        :disabled="loadingTasks"
        class="loadMoreButton ma-auto mt-4 btn"
        block
        @click="loadNextPage">
        {{ $t('spacesList.button.showMore') }}
      </v-btn>
    </div>
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
      defaultAvatar: '/portal/rest/v1/social/users/default-image/avatar',
      keyword: null,
      loadingTasks: false,
      taskViewTabName: 'board',
      deleteConfirmMessage: null,
      statusList: [],
      tasksList: [],
      groupName: null,
      filterProjectActive: true,
      filterByStatus: false,
      status: null,
      filterAsCompleted: false,
      allowGantt: false
    };
  },
  watch: {
    project(){
      this.getStatusByProject(this.project.id);
      this.tasksList=[];
      this.getTasksByProject(this.project.id,'');
    }
  },
  created() {
    this.$featureService.isFeatureEnabled('tasks.gantt').then(enabled => this.allowGantt = enabled);
    this.$root.$on('update-task-list', () => {
      this.getTasksByProject(this.project.id,'');
    });
    this.$root.$on('deleteTask', (event) => {
      if (event && event.detail) {
        this.tasksList = this.tasksList.filter((t) => t.id !== event.detail);
      }
    });
    this.$root.$on('update-task-completed', (event) => {
      if (event) {
        window.setTimeout(() => this.tasksList = this.tasksList.filter((t) => t.id !== event.id), 500);
      }
    });
  },
  methods: {
    hideProjectDetails() {
      this.$root.$emit('set-url', {type: 'myProjects',id: ''});
      this.$root.$emit('close-quick-task-form');
      document.dispatchEvent(new CustomEvent('hideProjectTasks'));
      this.taskViewTabName = 'board';
      this.$root.$emit('hide-tasks-project');
    },
    getChangeTabValue(value) {
      this.taskViewTabName = value;
      this.getTasksByProject(this.project.id,'');
      if ( value === 'gantt' ) {
        this.$root.$emit('gantt-displayed', this.tasksList);
      }
    },
    getStatusByProject(ProjectId) {
      return this.$tasksService.getStatusesByProjectId(ProjectId).then(data => {
        this.statusList = data;
      });
    },
    filterByKeyword(keyword,searchonkeyChange){
      if (searchonkeyChange){
        this.getTasksByProject(this.project.id,keyword);
      }
    },
    getTasksByProject(ProjectId,query) {
      const currentTab= this.taskViewTabName;
      if (localStorage.getItem(`filterStorage${ProjectId}+${currentTab}`)!==null){
        const localStorageSaveFilter = localStorage.getItem(`filterStorage${ProjectId}+${currentTab}`);
        if (localStorageSaveFilter.split('"')[10].split('}')[0].split(':')[1].split(',')[0] === ProjectId.toString() && localStorageSaveFilter.split('"')[13] === currentTab) {
          this.groupBy = localStorageSaveFilter.split('"')[3];
          this.sortBy = localStorageSaveFilter.split('"')[7];
          const tasksFilter = {
            query: query,
            groupBy: this.groupBy,
            orderBy: this.sortBy,
            offset: 0,
            limit: 0,
            showCompleteTasks: false,
          };
          return this.getFilter(tasksFilter,ProjectId);
        }
      } else {
        this.getFilterProject(ProjectId,currentTab).then(() => {
          const tasksFilter = {
            query: query,
            groupBy: this.groupBy,
            orderBy: this.sortBy,
            offset: 0,
            limit: 0,
            showCompleteTasks: false,
          };
          const jsonToSave = {
            groupBy: this.groupBy,
            sortBy: this.sortBy,
            projectId: ProjectId,
            tabView: (this.taskViewTabName!==''? this.taskViewTabName : 'list'),
          };
          localStorage.setItem(`filterStorage${ProjectId}+${jsonToSave.tabView}`,JSON.stringify(jsonToSave));
          return this.getFilter(tasksFilter,ProjectId);
        });
      }
    },
    resetFiltertaskDashboard(){
      this.getTasksByProject(this.project.id,'');
      this.filterByStatus=false;
    },
    filterTaskDashboard(e){
      this.loadingTasks = true;
      const tasks=e.tasks;
      this.filterAsCompleted = e.showCompleteTasks;
      tasks.showCompleteTasks=e.showCompleteTasks;
      if (tasks.groupBy==='completed'){
        tasks.showCompleteTasks=true;
      }
      if (tasks.groupBy==='none'){
        this.filterByStatus=false;
      }
      if (tasks.groupBy==='status'){
        tasks.groupBy='';
        this.filterProjectActive=false;
        this.filterByStatus=true;
        return this.$tasksService.filterTasksList(tasks,'','','',this.project.id).then(data => {
          this.filterProjectActive=false;
          this.filterByStatus=true;
          this.tasksList = data && data.tasks || [];

        }).finally(() => this.loadingTasks = false);
      } else {
        this.filterByStatus=false;
        return this.$tasksService.filterTasksList(e.tasks,'','',e.filterLabels.labels,this.project.id).then(data => {
          if (data.projectName){
            this.filterProjectActive=true;
            this.tasksList = data && data.tasks || [];
            this.groupName=data;
          }
          else {
            this.filterProjectActive=false;
            this.tasksList = data && data.tasks || [];
          }

        })
          .finally(() => this.loadingTasks = false);
      }
    },
    getNameGroup(name){
      if (name==='Unassigned' || name===''){
        return 'label.unassigned';
      } else if (name==='No Label'){
        return 'label.noLabel';
      } else if (name==='No due date'){
        return 'label.noDueDate';
      } else if (name==='Completed'){
        return 'label.task.completed';
      } else if (name==='Uncompleted'){
        return 'label.task.uncompleted';
      } else
      {return name;}

    },
    showDetailsTask(id){
      const uiIconMiniArrowDown = document.querySelector(`#${`uiIconMiniArrowDown${id}`}`);
      const uiIconMiniArrowRight = document.querySelector(`#${`uiIconMiniArrowRight${id}`}`);

      const detailsTask = document.querySelector(`#${`taskView${id}`}`);
      if (detailsTask.style.display !== 'none') {
        detailsTask.style.display = 'none';
        uiIconMiniArrowDown.style.display = 'none';
        uiIconMiniArrowRight.style.display = 'block';
      }
      else {detailsTask.style.display = 'block';
        uiIconMiniArrowDown.style.display = 'block';
        uiIconMiniArrowRight.style.display = 'none';}
    },
    deleteStatus(status) {
      this.deleteConfirmMessage = `${this.$t('popup.msg.deleteStatus')} : ${status.name}? `;
      this.status=status;
      this.$refs.deleteConfirmDialog.open();
    },
    deleteConfirm() {
      return this.$statusService.deleteStatus(this.status.id).then( () => {
        this.$root.$emit('show-alert',{type: 'success',message: this.$t('alert.success.status.deleted')} );
        this.getStatusByProject(this.project.id);
      }).catch(e => {
        console.error('Error when deleting status', e);
        this.$root.$emit('show-alert',{type: 'error',message: this.$t('alert.error')} );
      });
    },
    updateStatus(status) {
      if (status!=null){
        return this.$statusService.updateStatus(status).then( () => {
          this.$root.$emit('show-alert',{type: 'success',message: this.$t('alert.success.status.update')} );
          this.getStatusByProject(this.project.id);
        }).then( () => {
          this.getTasksByProject(this.project.id,'');
        }).catch(e => {
          console.error('Error when updating status', e);
          this.$root.$emit('show-alert',{type: 'error',message: this.$t('alert.error')} );
        });
      } else {
        this.getStatusByProject(this.project.id);
        this.getTasksByProject(this.project.id,'');
      }

    },
    createStatus() {
      this.statusList.forEach(function (element, index) {
        if (!element.project){
          element.project=this.project;
        }
        element.rank=index;
      },  this);
      return this.$statusService.createStatus(this.statusList).then( () => {
        this.$root.$emit('show-alert',{type: 'success',message: this.$t('alert.success.status.created')} );
        this.getStatusByProject(this.project.id);
      }).catch(e => {
        console.error('Error when creating status', e);
        this.$root.$emit('show-alert',{type: 'error',message: this.$t('alert.error')} );
      });
    },
    moveStatus() {
      this.statusList.forEach(function (element, index) {
        if (!element.project){
          element.project=this.project;
        }
        element.rank=index;
      },  this);
      return this.$statusService.moveStatus(this.statusList).then( () => {
        this.$root.$emit('show-alert',{type: 'success',message: this.$t('alert.success.status.moved')} );
        this.getStatusByProject(this.project.id);
      }).catch(e => {
        console.error('Error when moving status', e);
        this.$root.$emit('show-alert',{type: 'error',message: this.$t('alert.error')} );
      });
    },
    getFilter(tasksFilter,ProjectId){
      if (tasksFilter.groupBy==='status') {
        tasksFilter.groupBy = '';
        this.filterProjectActive=false;
        this.filterByStatus=true;
        this.$tasksService.filterTasksList(tasksFilter,'','','',ProjectId).then(data => {
          this.filterProjectActive=false;
          this.tasksList = data && data.tasks || [];
        }).finally(() => this.loadingTasks = false);
      }  else {
        this.$tasksService.filterTasksList(tasksFilter,'','','',ProjectId).then(data => {
          this.filterByStatus=false;
          if (data.projectName){
            this.filterProjectActive=true;
            this.tasksList = data && data.tasks || [];
            this.groupName=data;
          }
          else {
            this.filterProjectActive=false;
            this.tasksList = data && data.tasks || [];
          }

        }).finally(() => this.loadingTasks = false);
      }


    },
    getFilterProject(ProjectId,currentTab){
      return this.$projectService.getFilterSettings(ProjectId,currentTab).then((resp) =>{
        if (resp && resp.value){
          const StorageSaveFilter = resp.value;
          if (StorageSaveFilter.split('"')[10].split('}')[0].split(':')[1].split(',')[0] === ProjectId.toString()) {
            this.groupBy = StorageSaveFilter.split('"')[3];
            this.sortBy = StorageSaveFilter.split('"')[7];
          }
        } else {
          this.groupBy = 'none';
          this.sortBy = '';
        }
      });
    },
    openTaskDrawer() {
      const defaultTask= {id: null,
        status: {project: this.project},
        priority: 'NONE',
        description: '',
        title: ''};
      this.$root.$emit('open-task-drawer', defaultTask);
    },
  }
};
</script>
