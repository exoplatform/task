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
      <div class="taskViewBreadcrumb text-truncate px-0 pt-1 pb-5">
        <a
          class="text-color"
          @click="hideProjectDetails()">
          <i class="uiIcon uiBackIcon"></i>
          <span>{{ project.name }}</span>
        </a>
      </div>
      <div class="projectTasksTabFilter">
        <tasks-view-toolbar
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
      v-if="!loadingTasks">
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
        v-show="taskViewTabName == 'gantt'"
        eager>
        <tasks-view-gantt :tasks-list="allProjectTasks" />
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
    <tasks-unscheduled-drawer :project="project" />
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
      taskFilter: {},
      taskViewTabName: 'board',
      deleteConfirmMessage: null,
      statusList: [],
      tasksList: [],
      allProjectTasks: [],
      groupName: null,
      filterProjectActive: true,
      filterByStatus: false,
      status: null,
      filterAsCompleted: false,
    };
  },
  watch: {
    project() {
      this.getStatusByProject(this.project.id);
      this.tasksList=[];
      this.getTasksByProject(this.project.id,'');
      this.getAllProjectTasks(this.project.id);
    }
  },
  created() {
    this.$root.$on('task-added', task => {
      this.$tasksService.filterTasksList(this.taskFilter,'','','',this.project.id).then(data => {
        if (Array.isArray(data.tasks[0])) {
          const tasksArrayIndex = data.tasks.findIndex(tasksArray => tasksArray.findIndex(t => t.id === task.id) > -1);

          this.$set(this.tasksList, tasksArrayIndex, data.tasks[tasksArrayIndex]);
        } else {
          const taskIndex = data.tasks.findIndex(t => t.id === task.id);          
          this.tasksList.splice(taskIndex, 0, data.tasks[taskIndex]);
        }
      });
    });

    this.$root.$on('task-assignee-coworker-updated', task => {
      this.updateModifiedTask(task);
    });
    
    this.$root.$on('task-priority-updated', value => {
      
      if (this.taskFilter.sortBy === 'priority') {
        this.$tasksService.filterTasksList(this.taskFilter,'','','',this.project.id).then(data => {
          if (Array.isArray(data.tasks[0])) {
            const tasksArrayIndex = this.tasksList.findIndex(tasksArray => tasksArray.findIndex(t => t.id === value.taskId) > -1);
            this.$set(this.tasksList, tasksArrayIndex, data.tasks[tasksArrayIndex]);
          } else {
            const taskOldIndex = this.tasksList.findIndex(t => t.id === value.taskId);
            const taskNewIndex = data.tasks.findIndex(task => task.id === value.taskId);

            this.tasksList.splice(taskOldIndex, 1);
            this.tasksList.splice(taskNewIndex, 0, data.tasks[taskNewIndex]); 
          }
        });
      }
    });

    this.$root.$on('task-due-date-updated', task => {
      this.updateModifiedTask(task);
    });
    
    this.$root.$on('refresh-tasks-list', () => {
      this.getTasksByProject(this.project.id,'');
      if ( this.taskViewTabName === 'gantt' ) {
        return this.$tasksService.getTasksByProjectId(this.project.id).then(data => {
          this.allProjectTasks = data ? data : [];
          this.$root.$emit('refresh-gantt', this.allProjectTasks);
        });
      }
    });
    this.$root.$on('deleteTask', (event) => {
      if (event && event.detail) {
        const taskId = event.detail;
        if (Array.isArray(this.tasksList[0])) {
          const targetTasksArrayIndex = this.tasksList.findIndex(tasksArray => tasksArray.findIndex(t => t.id === taskId) > -1);
          const updatedArray = this.tasksList[targetTasksArrayIndex].filter((t) => t.id !== taskId);
          this.$set(this.tasksList, targetTasksArrayIndex, updatedArray);
        } else {
          this.tasksList = this.tasksList.filter((t) => t.id !== taskId);
        }
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
        this.getAllProjectTasks(this.project.id);
      }
    },
    getAllProjectTasks(projectId) {
      return this.$tasksService.getTasksByProjectId(projectId).then(data => {
        this.allProjectTasks = [];
        this.allProjectTasks = data ? data : [];
        this.$root.$emit('gantt-displayed', this.allProjectTasks);
      });
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
    getTasksByProject(ProjectId, query) {
      const currentTab = this.taskViewTabName;
      this.tasksList = [];
      this.filterProjectActive = false;
      this.groupName = null;
      const projectFilter = JSON.parse(localStorage.getItem(`filterStorage${ProjectId}+${currentTab}`));
      if (projectFilter) {
        if (projectFilter['projectId'] === ProjectId && projectFilter['tabView'] === currentTab) {
          const groupBy = projectFilter['groupBy'].trim();
          const sortBy = projectFilter['sortBy'].trim();
          this.tasksFilter = {
            query: query,
            groupBy: groupBy,
            sortBy: sortBy,
            offset: 0,
            limit: 0,
            showCompleteTasks: false,
          };
          if (groupBy === 'completed') {
            this.tasksFilter.showCompleteTasks = true;
          }
          return this.getFilter(this.tasksFilter, ProjectId);
        }
      } else {
        this.getFilterProject(ProjectId, currentTab).then(() => {
          this.tasksFilter = {
            query: query,
            groupBy: '',
            orderBy: '',
            offset: 0,
            limit: 0,
            showCompleteTasks: false,
          };
          
          if (this.taskFilter.groupBy === 'completed') {
            this.tasksFilter.showCompleteTasks = true;
          }
          const jsonToSave = {
            groupBy: this.taskFilter.groupBy,
            sortBy: this.taskFilter.sortBy,
            projectId: ProjectId,
            tabView: (this.taskViewTabName !== '' ? this.taskViewTabName : 'list'),
          };
          localStorage.setItem(`filterStorage${ProjectId}+${jsonToSave.tabView}`, JSON.stringify(jsonToSave));
          return this.getFilter(this.tasksFilter, ProjectId);
        });
      }
    },
    resetFiltertaskDashboard(){
      this.getTasksByProject(this.project.id,'');
      this.filterByStatus=false;
    },
    filterTaskDashboard(e){
      this.loadingTasks = true;
      this.taskFilter = e.tasks;
      this.filterAsCompleted = e.showCompleteTasks;
      this.taskFilter.showCompleteTasks=e.showCompleteTasks;
      if (this.taskFilter.groupBy==='completed'){
        this.taskFilter.showCompleteTasks=true;
      }
      if (this.taskFilter.groupBy==='none'){
        this.filterByStatus=false;
      }
      if (this.taskFilter.groupBy==='status'){
        this.taskFilter.groupBy='';
        this.filterProjectActive=false;
        this.filterByStatus=true;
        return this.$tasksService.filterTasksList(this.taskFilter,'','',e.filterLabels.labels,this.project.id).then(data => {
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
      if (tasksFilter) {
        this.taskFilter = tasksFilter;
      }
      if (tasksFilter.groupBy==='status') {
        tasksFilter.groupBy = '';
        this.filterProjectActive=false;
        this.filterByStatus=true;
        this.$tasksService.filterTasksList(this.tasksFilter,'','','',ProjectId).then(data => {
          this.filterProjectActive=false;
          this.tasksList = data && data.tasks || [];
        }).finally(() => this.loadingTasks = false);
      }  else {
        this.$tasksService.filterTasksList(this.tasksFilter,'','','',ProjectId).then(data => {
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
            this.taskFilter.groupBy = StorageSaveFilter.split('"')[3].trim();
            this.taskFilter.sortBy = StorageSaveFilter.split('"')[7].trim();
          }
        } else {
          this.taskFilter.groupBy = 'none';
          this.taskFilter.sortBy = '';
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
    updateModifiedTask(task) {
      this.$tasksService.filterTasksList(this.taskFilter, '', '', '', this.project.id).then(data => {
        if (Array.isArray(data.tasks[0])) {
          if (data.tasks.length !== this.tasksList.length) {
            this.getTasksByProject(this.project.id, '');
            if (this.taskViewTabName === 'gantt') {
              return this.$tasksService.getTasksByProjectId(this.project.id).then(tasks => {
                this.allProjectTasks = tasks ? tasks : [];
                this.$root.$emit('refresh-gantt', this.allProjectTasks);
              });
            }
          } else {
            const tasksArrayOldIndex = this.tasksList.findIndex(tasksArray => tasksArray.findIndex(t => t.id === task.id) > -1);
            const tasksArrayNewIndex = data.tasks.findIndex(tasksArray => tasksArray.findIndex(t => t.id === task.id) > -1);

            this.$set(this.tasksList, tasksArrayOldIndex, data.tasks[tasksArrayOldIndex]);
            this.$set(this.tasksList, tasksArrayNewIndex, data.tasks[tasksArrayNewIndex]);
          }
        } else {
          const taskOldIndex = this.tasksList.findIndex(t => t.id === task.id);
          const taskNewIndex = data.tasks.findIndex(t => t.id === task.id);

          this.tasksList.splice(taskOldIndex, 1);
          this.tasksList.splice(taskNewIndex, 0, data.tasks[taskNewIndex]);
        }
      });
    }
  }
};
</script>
