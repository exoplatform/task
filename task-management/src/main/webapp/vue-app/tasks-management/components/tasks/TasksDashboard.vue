<template>
  <v-app
    id="tasksListApplication"
    class="projectAndTasksContainer transparent pa-4"
    flat>
    <tasks-list-toolbar
      ref="taskToolBar"
      :task-card-tab="'#tasks-cards'"
      :task-list-tab="'#tasks-list'"
      :keyword="keyword"
      :show-completed-tasks="showCompletedTasks"
      @keyword-changed="keywordChanged"
      @filter-task-dashboard="filterTaskDashboard"
      @filter-task-query="filterTaskQuery"
      @primary-filter-task="getTasksByPrimary"
      @reset-filter-task-dashboard="resetFilterTaskDashboard" />
    <div
      v-if="(!tasks || !tasks.length) && !loadingTasks && !filterActive"
      class="noTasksProject">
      <div class="noTasksProjectIcon"><i class="uiIcon uiIconTask"></i></div>
      <div class="noTasksProjectLabel"><span>{{ $t('label.noTask') }}</span></div>
    </div>
    <div v-else>
      <div v-if="filterActive && filterTaskQueryResult && filterTaskQueryResult.projectName" class="px-0 pt-8 pb-4">
        <div 
          v-for="(project,i) in filterTaskQueryResult.projectName" 
          :key="project.name" 
          class="pt-5">
          <div v-if=" project.value && project.value.displayName" class="d-flex align-center assigneeFilter">
            <a
              class="toggle-collapse-group pointer"
              href="#"
              @click="showDetailsTask(project.rank)">
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
              class="pe-2" />
            <span class="amount-item">({{ filterTaskQueryResult.tasks[i].length }})</span>
            <hr
              role="separator"
              aria-orientation="horizontal"
              class="my-0 v-divider theme--light">
          </div>
          <div v-else class="d-flex align-center assigneeFilter">
            <a
              :id="'iconTask'+project.rank"
              class="toggle-collapse-group pointer"
              href="#"
              @click="showDetailsTask(project.rank)">
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
            <div v-if="project.name==='Unassigned'" class="defaultAvatar">
              <img :src="defaultAvatar">
            </div>
            <span class="nameGroup">{{ $t(getGroupName(project.name)) }}</span>
            <span class="amount-item">({{ filterTaskQueryResult.tasks[i].length }})</span>
            <hr
              role="separator"
              aria-orientation="horizontal"
              class="my-0 v-divider theme--light">
          </div>
          <div :id="'taskView'+project.rank" class="view-task-group-sort">
            <tasks-cards-list
              :tasks="filterTaskQueryResult.tasks[i]"
              :show-completed-tasks="showCompletedTasks"
              class="d-md-none" />
            <tasks-list
              :tasks="filterTaskQueryResult.tasks[i]"
              :show-completed-tasks="showCompletedTasks"
              class="d-md-block d-none" />
          </div>
        </div>
      </div>
      <div v-else>
        <div class="d-md-block d-none">
          <tasks-list
            :tasks="tasks"
            :show-completed-tasks="showCompletedTasks" />
        </div>
        <div class="d-md-none">
          <tasks-cards-list
            :tasks="tasks"
            :show-completed-tasks="showCompletedTasks" />
        </div>
      </div>
    </div>
    <v-row class="ma-0 border-box-sizing">
      <v-btn
        v-if="canShowMore && !filterActive"
        :loading="loadingTasks"
        :disabled="loadingTasks"
        class="loadMoreButton ma-auto mt-4 btn"
        block
        @click="loadNextPage">
        {{ $t('spacesList.button.showMore') }}
      </v-btn>
    </v-row>
  </v-app>
</template>
<script>
export default {
  data () {
    return {
      primaryFilter: 'ALL',
      tasks: [],
      keyword: null,
      loadingTasks: false,
      offset: 0,
      tasksSize: 0,
      pageSize: 20,
      limit: 20,
      limitToFetch: 0,
      originalLimitToFetch: 0,
      startSearchAfterInMilliseconds: 600,
      endTypingKeywordTimeout: 50,
      startTypingKeywordTimeout: 0,
      showCompletedTasks: false,
      filterTaskQueryResult: null,
      filterActive: false,
      groupBy: null,
      orderBy: null,
      labels: [],
      taskQueryFilter: {
        query: '',
        assignee: '',
        statusId: '',
        dueDate: '',
        priority: '',
        projectId: -2,
        showCompletedTasks: this.showCompletedTasks,
        groupBy: '',
        orderBy: '',
      },
      filterTasks: {
        query: '',
        assignee: '',
        watcher: '',
        coworker: '',
        statusId: '',
        dueDate: '',
        priority: '',
        projectId: -2,
        groupBy: '',
        orderBy: '',
        offset: this.offset,
        limit: this.limitToFetch,
        showCompletedTasks: this.showCompletedTasks
      },
      defaultAvatar: '/portal/rest/v1/social/users/default-image/avatar',
    };
  },
  computed: {
    canShowMore() {
      return this.loadingTasks || this.tasks.length >= this.limitToFetch;
    },
  },
  watch: {
    limitToFetch() {
      this.searchTasks();
    },
  },
  created() {
    this.groupBy = localStorage.getItem('filterStorageNone+list') ?
      JSON.parse(localStorage.getItem('filterStorageNone+list')).groupBy : false;

    this.orderBy = localStorage.getItem('filterStorageNone+list') ?
      JSON.parse(localStorage.getItem('filterStorageNone+list')).orderBy : false;

    this.showCompletedTasks = localStorage.getItem('filterStorageNone+list') ?
      JSON.parse(localStorage.getItem('filterStorageNone+list')).showCompletedTasks : false;
    this.filterTasks.showCompletedTasks = this.showCompletedTasks;

    this.$root.$on('task-added', () => {
      this.updateTasksList();
    });

    this.$root.$on('task-assignee-coworker-updated', () => {
      this.updateTasksList();
    });

    this.$root.$on('task-priority-updated', () => {
      this.updateTasksList();
    });

    this.$root.$on('task-due-date-updated', () => {
      this.updateTasksList();
    });

    this.$root.$on('deleteTask', () => {
      this.updateTasksList();
    });

    this.$root.$on('update-cart', (event) => {
      if (event && !this.showCompletedTasks) {
        window.setTimeout(() => this.tasks = this.tasks.filter((t) => t.id !== event.id), 500);
      }
    });

    this.$root.$on('update-task-completed', (event) => {
      if (event && !this.showCompletedTasks) {
        window.setTimeout(() => this.updateTasksList(), 500);
      }
    });
  },
  methods: {
    keywordChanged(keyword,searchonkeyChange){
      this.keyword=keyword;
      if (searchonkeyChange){
        this.filterTasks.query=this.keyword;
        this.resetSearch();
        this.searchTasks();
      }
    },
    resetFilterTaskDashboard(){
      this.groupBy='';
      this.orderBy='';
      this.filterActive=false;
      this.resetSearch();
      this.getTasksByPrimary(this.primaryFilter);
    },
    filterTaskDashboard(e){
      this.tasks=e.tasks;
      this.showCompletedTasks = e.showCompletedTasks;
      this.filterActive=false;
    },

    filterTaskQuery(e, filterGroupSort, filterLabels) {
      this.showCompletedTasks = e.showCompletedTasks;
      this.groupBy = filterGroupSort.groupBy;
      this.orderBy = filterGroupSort.orderBy;
      this.labels = filterLabels.labels;
      if (this.primaryFilter === 'ALL') {
        this.taskQueryFilter = e;
        this.resetSearch();
        this.searchTasks(this.taskQueryFilter);
      } else {
        if ((this.primaryFilter === 'OVERDUE' || this.primaryFilter === 'TODAY' || this.primaryFilter === 'TOMORROW' || this.primaryFilter === 'UPCOMING' ||
          this.primaryFilter === 'ASSIGNED' && e.assignee) && e.dueDate && e.dueDate !== this.primaryFilter) {
          this.tasks = [];
          this.filterActive = false;
        } else {
          if (this.primaryFilter !== 'ASSIGNED') {
            this.filterTasks.assignee = e.assignee;
          }
          if (!(this.primaryFilter === 'OVERDUE' || this.primaryFilter === 'TODAY' || this.primaryFilter === 'TOMORROW' || this.primaryFilter === 'UPCOMING')) {
            this.filterTasks.dueDate = e.dueDate;
          }
          this.filterTasks.query = e.query;
          this.filterTasks.statusId = e.statusId;
          this.filterTasks.priority = e.priority;
          this.filterTasks.showCompletedTasks = e.showCompletedTasks;
          this.resetSearch();
          this.searchTasks();
        }
      }
    },
    searchTasks(tasksFilter) {
      if (!tasksFilter) {
        tasksFilter = this.filterTasks;
      }
      this.loadingTasks = true;
      if (tasksFilter.assignee) {
        tasksFilter.projectId = -3;
      }
      
      return this.$tasksService.filterTasksList(tasksFilter,this.groupBy,this.orderBy,this.labels).then(data => {
        if (data.projectName){
          this.filterTaskQueryResult = data;
          this.filterActive=true;
        } else {
          this.tasks = data && data.tasks || [];
          this.tasksSize = data && data.tasksNumber || 0;
          this.filterActive=false;
        } 
        return this.$nextTick();
      }).then(() => {
        if (this.keyword && this.tasks.length >= this.limitToFetch) {
          this.limitToFetch += this.pageSize;
        }
      })
        .finally(() => {
          this.loadingTasks = false;
          this.$root.$applicationLoaded();
        });
    },
    getTasksByPrimary(primaryFilter) {
      this.primaryFilter=primaryFilter;         
      if (primaryFilter && (primaryFilter === 'OVERDUE' || primaryFilter === 'TODAY' || primaryFilter === 'TOMORROW' || primaryFilter === 'UPCOMING')){
        this.filterTasks.dueDate=primaryFilter;
        this.filterTasks.assignee='';
        this.filterTasks.watcher='';
        this.filterTasks.coworker='';
        this.filterTasks.statusId='';
        this.filterTasks.priority='';
        this.filterTasks.query='';
        this.groupBy='';
        this.orderBy='';          
        this.filterTasks.projectId=-2;
        this.resetSearch();
        this.searchTasks();
      } else if (primaryFilter && (primaryFilter === 'ASSIGNED')){
        this.filterTasks.dueDate='';
        this.filterTasks.assignee='ME';
        this.filterTasks.watcher='';
        this.filterTasks.coworker='';
        this.filterTasks.statusId='';
        this.filterTasks.priority='';
        this.filterTasks.query='';
        this.groupBy='';
        this.orderBy='';
        this.filterTasks.projectId=-3;
        this.resetSearch();
        this.searchTasks();
      } else if (primaryFilter && (primaryFilter === 'WATCHED')){
        this.filterTasks.dueDate='';
        this.filterTasks.assignee='';
        this.filterTasks.watcher='ME';
        this.filterTasks.coworker='';
        this.filterTasks.statusId='';
        this.filterTasks.priority='';
        this.filterTasks.query='';
        this.groupBy='';
        this.orderBy='';
        this.filterTasks.projectId=-3;
        this.resetSearch();
        this.searchTasks();
      } else if (primaryFilter && (primaryFilter === 'COLLABORATED')){
        this.filterTasks.dueDate='';
        this.filterTasks.assignee='';
        this.filterTasks.watcher='';
        this.filterTasks.coworker='ME';
        this.filterTasks.statusId='';
        this.filterTasks.priority='';
        this.filterTasks.query='';
        this.groupBy='';
        this.orderBy='';
        this.filterTasks.projectId=-3;
        this.resetSearch();
        this.searchTasks();
      } else {
        this.filterTasks.dueDate='';
        this.filterTasks.assignee='';
        this.filterTasks.watcher='';
        this.filterTasks.coworker='';
        this.filterTasks.statusId='';
        this.filterTasks.priority='';
        this.filterTasks.query='';
        if (localStorage.getItem('filterStorageNone+list')) {
          const localStorageSavedFilter = JSON.parse(localStorage.getItem('filterStorageNone+list'));
          this.groupBy = localStorageSavedFilter.groupBy;
          this.orderBy = localStorageSavedFilter.orderBy;
        } else {
          this.getFilterProject().then(() => {
            const jsonToSave = {
              groupBy: this.groupBy,
              orderBy: this.orderBy,
              projectId: 'None',
              tabView: 'list',
              showCompletedTasks: false,
            };
            localStorage.setItem('filterStorageNone+list', JSON.stringify(jsonToSave));
            this.filterTasks.projectId=-2;
            this.searchTasks();
          });
        }
        this.filterTasks.projectId=-2;
        this.resetSearch();
        this.searchTasks(); 
      }
      if (this.$refs.taskToolBar){
        this.$refs.taskToolBar.resetFields('primary');
      }
    },

    resetSearch() {
      if (this.limitToFetch !== this.originalLimitToFetch) {
        this.limitToFetch = this.originalLimitToFetch;
      }
    },
    loadNextPage() {
      this.limitToFetch += this.pageSize;
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() - this.startTypingKeywordTimeout > this.startSearchAfterInMilliseconds) {
          this.searchTasks();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
    getGroupName(name){
      if (name==='No project'){
        return 'label.noProject';
      }
      else if (name==='No Label'){
        return 'label.noLabel';
      }
      else if (name==='No due date'){
        return 'label.noDueDate';
      }
      else if (name==='Overdue'){
        return 'label.overdue';
      }
      else if (name==='Today'){
        return 'label.today';
      }
      else if (name==='Tomorrow'){
        return 'label.tomorrow';
      }
      else if (name==='Upcoming'){
        return 'label.upcoming';
      }
      return name;
    },
    showDetailsTask(id){
      const uiIconMiniArrowDown = document.querySelector(`#uiIconMiniArrowDown${id}`);
      const uiIconMiniArrowRight = document.querySelector(`#uiIconMiniArrowRight${id}`);

      const detailsTask = document.querySelector(`#taskView${id}`);
      if (detailsTask.style.display !== 'none') {
        detailsTask.style.display = 'none';
        uiIconMiniArrowDown.style.display = 'none';
        uiIconMiniArrowRight.style.display = 'block';
      }
      else {detailsTask.style.display = 'block';
        uiIconMiniArrowDown.style.display = 'block';
        uiIconMiniArrowRight.style.display = 'none';}
    },
    getFilterProject(){
      return this.$projectService.getFilterSettings('None','list').then((resp) =>{
        if (resp && resp.value){
          const StorageSaveFilter = resp.value;
          if (StorageSaveFilter.split('"')[11] === 'None') {
            this.groupBy = StorageSaveFilter.split('"')[3];
            this.orderBy = StorageSaveFilter.split('"')[7];
          }
        } else {
          this.groupBy = 'none';
          this.orderBy = '';
        }
      });
    },
    updateTasksList() {
      const filterTasks = this.filterActive ? this.taskQueryFilter : this.filterTasks;
      this.$tasksService.filterTasksList(filterTasks, this.groupBy, this.orderBy, this.labels).then(data => {

        if (this.filterActive) {
          this.filterTaskQueryResult = data;
        } else {
          this.tasks = data.tasks;
        }
      });
    },
  }
};
</script>
