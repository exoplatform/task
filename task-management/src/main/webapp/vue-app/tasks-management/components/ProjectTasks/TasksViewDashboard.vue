<template>
  <v-app :id="'projectTask-'+project.id" class="projectTasksDashboard px-4">
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="deleteConfirmMessage"
      :title="$t('popup.deleteStatus')"
      :ok-label="$t('label.ok')"
      :cancel-label="$t('popup.cancel')"
      @ok="deleteConfirm()" />
    <div class="taskViewBreadcrumb px-0 pt-4 pb-5">
      <a
        class="text-color"
        @click="hideProjectDetails()">
        <i class="uiIcon uiBackIcon"></i>
        <span>{{ project.name }}</span>
      </a>
    </div>
    <tasks-view-toolbar
      :allow-gantt="allowGantt"
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
    <div v-if="filterProjectActive && groupName && groupName.projectName" class="px-0 pt-8 pb-4">
      <div 
        v-for="(project,i) in groupName.projectName" 
        :key="project.name" 
        class="pt-5">

        <div
          v-if=" project.value && project.value.displayName && project.name!==''"
          class="d-flex align-center assigneeFilter pointer"
          @click="showDetailsTask(project.rank)">
          <a
            class="toggle-collapse-group"
            href="#">
            <i
              :id="'uiIconMiniArrowDown'+project.rank"
              class="uiIcon uiIconMiniArrowDown"
              style="display: block">
            </i>
            <i
              :id="'uiIconMiniArrowRight'+project.rank"
              class="uiIcon  uiIconMiniArrowRight"
              style="display: none">
            </i>
          </a>
          <exo-user-avatar
            v-if="project.value.avatar"
            :username="project.value.username"
            :fullname="project.value.displayName"
            :avatar-url="project.value.avatar"
            :title="project.value.displayName"
            :size="26"
            class="pr-2"/>
          <span class="amount-item">({{ tasksList[i].length }})</span>

          <hr
            role="separator"
            aria-orientation="horizontal"
            class="my-0 v-divider theme--light">

        </div>
        <div
          v-else
          class="d-flex align-center assigneeFilter pointer"
          @click="showDetailsTask(project.rank)">

          <a
            :id="'iconTask'+project.rank"
            class="toggle-collapse-group"
            href="#">
            <i
              :id="'uiIconMiniArrowDown'+project.rank"
              class="uiIcon uiIconMiniArrowDown"
              style="display: block">
            </i>
            <i
              :id="'uiIconMiniArrowRight'+project.rank"
              class="uiIcon  uiIconMiniArrowRight"
              style="display: none">
            </i>
          </a>
          <div v-if="project.name==='Unassigned' || project.name===''" class="defaultAvatar">
            <img :src="defaultAvatar">
          </div>

          <span class="nameGroup">{{ $t(getNameGroup(project.name)) }}</span>
          <span class="amount-item">({{ tasksList[i].length }})</span>
          <hr
            role="separator"
            aria-orientation="horizontal"
            class="my-0 v-divider theme--light">
        </div>
        <div :id="'taskView'+project.rank" class="view-project-group-sort">
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
              @delete-status="deleteStatus"/>
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
    </div>
    <v-tabs-items
      v-show="!filterProjectActive"
      v-if="tasksList && tasksList.length && !loadingTasks"
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
          @update-status="updateStatus"
          @create-status="createStatus"
          @move-status="moveStatus"
          @delete-status="deleteStatus"/>
      </div>
      <div
        v-show="taskViewTabName == 'list'"
        eager>
        <tasks-view-list
          :status-list="statusList"
          :tasks-list="tasksList"
          :filter-task-completed="filterAsCompleted"
          :filter-by-status="filterByStatus"
          @update-status="updateStatus"/>
      </div>
      <v-tab-item
        v-show="taskViewTabName == 'gantt' && allowGantt"
        eager>
        <tasks-view-gantt
          :tasks-list="tasksList"/>
      </v-tab-item>
    </v-tabs-items>
    <div
      v-if="(!tasksList || !tasksList.length) && !loadingTasks"
      class="noTasksProject">
      <div class="noTasksProjectIcon"><i class="uiIcon uiIconTask"></i></div>
      <div class="noTasksProjectLabel"><span>{{ $t('label.noTasks') }}</span></div>
      <!-- <div class="noTasksProjectLink"><a href="#">{{ $t('label.addTask') }}</a></div> -->
    </div>
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
        defaultAvatar:"/portal/rest/v1/social/users/default-image/avatar",
        keyword: null,
        loadingTasks: false,
        taskViewTabName: 'board',
        deleteConfirmMessage: null,
        statusList: [],
        tasksList: [],
        groupName:null,
        filterProjectActive:true,
        filterByStatus:false,
        status:null,
        filterAsCompleted: false,
        allowGantt: false,
      }
    },
    watch:{
      project(){
        this.getStatusByProject(this.project.id);
        this.getTasksByProject(this.project.id,"");
      }
    },
    created() {
      this.$featureService.isFeatureEnabled('tasks.gantt').then(enabled => this.allowGantt = enabled);
      this.$root.$on('update-task-list', task => {
        this.getTasksByProject(this.project.id,"");
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
    methods : {
      hideProjectDetails() {
        this.$root.$emit('set-url', {type:"myProjects",id:""})
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
      filterByKeyword(keyword,searchonkeyChange){
        if(searchonkeyChange){
        this.getTasksByProject(this.project.id,keyword);
        }
      },
      getTasksByProject(ProjectId,query) {
        this.loadingTasks = true;
        if(localStorage.getItem(`filterStorage${ProjectId}`)!==null){
          const localStorageSaveFilter = localStorage.getItem(`filterStorage${ProjectId}`);
          if (localStorageSaveFilter.split('"')[10].split('}')[0].split(':')[1] === ProjectId.toString()) {
            this.groupBy = localStorageSaveFilter.split('"')[3];
            this.sortBy = localStorageSaveFilter.split('"')[7];
            const tasksFilter = {
              query: query,
              groupBy: this.groupBy,
              orderBy: this.sortBy,
              offset: 0,
              limit: 0,
              showCompleteTasks:false,
            };
            return this.getFilter(tasksFilter,ProjectId);
          }
        }else {
          this.getFilterProject(ProjectId).then(() => {
            const tasksFilter = {
              query: query,
              groupBy: this.groupBy,
              orderBy: this.sortBy,
              offset: 0,
              limit: 0,
              showCompleteTasks:false,
            };
            const jsonToSave = {
              groupBy: this.groupBy,
              sortBy: this.sortBy,
              projectId: ProjectId,
            }
            localStorage.setItem(`filterStorage${ProjectId}`,JSON.stringify(jsonToSave));
            return this.getFilter(tasksFilter,ProjectId);
          });
        }
      },
      resetFiltertaskDashboard(){
        this.getTasksByProject(this.project.id,"");
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
          return this.$tasksService.filterTasksList(tasks,'','','',this.project.id).then(data => {
              this.filterProjectActive=false
              this.filterByStatus=true
              this.tasksList = data && data.tasks || [];

          }).finally(() => this.loadingTasks = false);
        } else {
          return this.$tasksService.filterTasksList(e.tasks,'','',e.filterLabels.labels,this.project.id).then(data => {
          if(data.projectName){
            this.filterProjectActive=true
            this.tasksList = data && data.tasks || [];
            this.groupName=data;
          }
          else {
            this.filterProjectActive=false
            this.tasksList = data && data.tasks || [];
          }

        })
        .finally(() => this.loadingTasks = false);
      }
        },
      getNameGroup(name){
        if (name==='Unassigned' || name===''){
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

      },
      showDetailsTask(id){
        const uiIconMiniArrowDown = document.querySelector(`#${`uiIconMiniArrowDown${id}`}`);
        const uiIconMiniArrowRight = document.querySelector(`#${`uiIconMiniArrowRight${id}`}`);

        const detailsTask = document.querySelector(`#${`taskView${id}`}`);
        if (detailsTask.style.display !== 'none') {
          detailsTask.style.display = 'none';
          uiIconMiniArrowDown.style.display = 'none';
          uiIconMiniArrowRight.style.display = 'block'
        }
        else {detailsTask.style.display = 'block'
          uiIconMiniArrowDown.style.display = 'block';
          uiIconMiniArrowRight.style.display = 'none'}
      },
      deleteStatus(status) {
        this.deleteConfirmMessage = `${this.$t('popup.msg.deleteStatus')} : ${status.name}? `;
        this.status=status
        this.$refs.deleteConfirmDialog.open();
      },
      deleteConfirm() {
         return this.$statusService.deleteStatus(this.status.id).then(resp => {
           this.$root.$emit('show-alert',{type:'success',message:this.$t('alert.success.status.deleted')} );
           this.getStatusByProject(this.project.id)
        }).catch(e => {
            console.debug("Error when deleting status", e);
                this.$root.$emit('show-alert',{type:'error',message: this.$t('alert.error')} );
          });
      },
      updateStatus(status) {
         return this.$statusService.updateStatus(status).then(resp => {
           this.$root.$emit('show-alert',{type:'success',message: this.$t('alert.success.status.update')} );
           this.getStatusByProject(this.project.id)
        }).catch(e => {
            console.debug("Error when updating status", e);
           this.$root.$emit('show-alert',{type:'error',message: this.$t('alert.error')} );
        });
      },
      createStatus() {
        this.statusList.forEach(function (element, index) {
        if(!element.project){
          element.project=this.project
        }
        element.rank=index
        },  this);
        return this.$statusService.createStatus(this.statusList).then(resp => {
          this.$root.$emit('show-alert',{type:'success',message: this.$t('alert.success.status.created')} );
           this.getStatusByProject(this.project.id)
        }).catch(e => {
           console.debug("Error when creating status", e);
            this.$root.$emit('show-alert',{type:'error',message: this.$t('alert.error')} );
           });
      },
      moveStatus() {
        this.statusList.forEach(function (element, index) {
        if(!element.project){
          element.project=this.project
        }
        element.rank=index
        },  this);
        return this.$statusService.moveStatus(this.statusList).then(resp => {
          this.$root.$emit('show-alert',{type:'success',message: this.$t('alert.success.status.moved')} );
           this.getStatusByProject(this.project.id)
        }).catch(e => {
           console.debug("Error when moving status", e);
            this.$root.$emit('show-alert',{type:'error',message: this.$t('alert.error')} );
           });
      },
    getFilter(tasksFilter,ProjectId){
      this.$tasksService.filterTasksList(tasksFilter,'','','',ProjectId).then(data => {
        if(data.projectName){
          this.filterProjectActive=true
          this.tasksList = data && data.tasks || [];
          this.groupName=data;
        }
        else {
          this.filterProjectActive=false
          this.tasksList = data && data.tasks || [];
        }

      })
              .finally(() => this.loadingTasks = false);
    },
    getFilterProject(ProjectId){
      return this.$projectService.getFilterSettings(ProjectId).then((resp) =>{
        if (resp && resp.value){
          const StorageSaveFilter = resp.value;
          if (StorageSaveFilter.split('"')[10].split('}')[0].split(':')[1] === ProjectId.toString()) {
            this.groupBy = StorageSaveFilter.split('"')[3];
            this.sortBy = StorageSaveFilter.split('"')[7];
          }
        }else {
          this.groupBy = 'none';
          this.sortBy = '';
        }
      });
    },
    }
  }
</script>
