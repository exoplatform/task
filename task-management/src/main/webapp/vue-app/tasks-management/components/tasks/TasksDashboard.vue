<template>
  <v-app
    id="tasksListApplication"
    class="projectAndTasksContainer transparent"
    flat>
    <tasks-list-toolbar
      ref="taskToolBar"
      :task-card-tab="'#tasks-cards'"
      :task-list-tab="'#tasks-list'"
      :keyword="keyword"
      @keyword-changed="keywordChanged"
      @changed="changeSelectedTabItem()"
      @filter-task-dashboard="filterTaskDashboard"
      @filter-task-query="filterTaskquery"
      @primary-filter-task="getTasksByPrimary"
      @reset-filter-task-dashboard="resetFiltertaskDashboard"/>
    <div
      v-if="(!tasks || !tasks.length) && !loadingTasks"
      class="noTasksProject">
      <div class="noTasksProjectIcon"><i class="uiIcon uiIconTask"></i></div>
      <div class="noTasksProjectLabel"><span>{{ $t('label.noTasks') }}</span></div>
      <!-- <div class="noTasksProjectLink"><a href="#">{{ $t('label.addTask') }}</a></div> -->
    </div>
    <div v-else>
      <div v-if="filterActive">
        <div v-for="(project,i) in tasksFilter.projectName" :key="project.name">
          <div v-if=" project.value && project.value.displayName" class="d-flex align-center assigneeFilter">
            <a
              class="toggle-collapse-group pointer"
              style="margin-right: 10px"
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
              class="pr-2"/>
            <span class="amount-item">({{ tasksFilter.tasks[i].length }})</span>

          </div>
          <div v-else class="d-flex align-center assigneeFilter">
            <a
              :id="'iconTask'+project.rank"
              class="toggle-collapse-group pointer"
              style="margin-right: 10px"
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
            <span class="amount-item">({{ tasksFilter.tasks[i].length }})</span>
          </div>
          <hr
            role="separator"
            aria-orientation="horizontal"
            class="my-0 v-divider theme--light">
          <div :id="'taskView'+project.rank" style="margin-left: 10px; display: block">
            <tasks-cards-list
              v-show="isTasksTabChanged"
              :tasks="tasksFilter.tasks[i]"
              class="d-md-none"/>
            <tasks-list
              :tasks="tasksFilter.tasks[i]"
              class="d-md-block d-none"/>
          </div>
        </div>
      </div>
      <div v-else>
        <div class="d-md-block d-none">
          <tasks-list
            :tasks="tasks"/>
        </div>
        <div class="d-md-none">
          <tasks-cards-list
            :tasks="tasks"/>
        </div>
      </div>
    </div>
    <!--<v-tabs-items v-show="!filterActive" :key="id">
      &lt;!&ndash;<v-tab-item v-show="isTasksTabChanged" eager>
         <tasks-cards-list
          :tasks="tasks"/>
      </v-tab-item>&ndash;&gt;
      <v-tab-item eager>
        <tasks-list
          :tasks="tasks"/>
      </v-tab-item>
    </v-tabs-items>-->
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
        isTasksTabChanged: false,
        primaryfilter:'ALL',
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
        showCompleteTasks: false,
        tasksFilter:null,
        filterActive:false,
        groupBy:null,
        sortBy:null,
        labels:[],
        filterTasks : {
            query: '',
            assignee: '',
            watcher:'',
            coworker:'',
            statusId: '',
            dueDate: '',
            priority: '',
            projectId: -2,
            groupBy: '',
            orderBy: '',
            offset: this.offset,
            limit: this.limitToFetch,
            showCompleteTasks:this.showCompleteTasks
          },
        defaultAvatar:"/portal/rest/v1/social/users/default-image/avatar",
      }
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
      this.originalLimitToFetch = this.limitToFetch = this.limit;
      this.$root.$on('task-added', task => {
       this.searchTasks();
      });
      this.$root.$on('filter-task-groupBy',tasks =>{
        this.tasksFilter = tasks;
        this.filterActive=true;
      });
    },
    methods: {
      keywordChanged(keyword,searchonkeyChange){
        this.keyword=keyword
        if(searchonkeyChange){
          this.filterTasks.query=this.keyword
          this.resetSearch();
          this.searchTasks();
          return;
    
       /*  this.startTypingKeywordTimeout = Date.now();
        if (!this.loadingTasks) {
          this.loadingTasks = true;
          this.waitForEndTyping();
        } */
       }
      },
      resetFiltertaskDashboard(){
        //this.searchTasks();
        this.groupBy=''
        this.sortBy=''
        this.filterActive=false;
        this.resetSearch();
        this.getTasksByPrimary(this.primaryfilter)
      },
      filterTaskDashboard(e){
        this.tasks=e.tasks;
        this.showCompleteTasks=e.showCompleteTasks;
        this.filterActive=false;
      },

      filterTaskquery(e,filterGroupSort,filterLabels){
        this.groupBy=filterGroupSort.groupBy
        this.sortBy=filterGroupSort.sortBy
        this.labels=filterLabels.labels
        if(this.primaryfilter==='ALL'){
          this.tasksFilter=e;
          this.resetSearch();
          this.searchTasks(this.tasksFilter)
        }else{
          if((this.primaryfilter === 'OVERDUE' || this.primaryfilter === 'TODAY' || this.primaryfilter === 'TOMORROW' || this.primaryfilter === 'UPCOMING')&&e.dueDate&&e.dueDate!==this.primaryfilter){
            this.tasks=[]
            this.filterActive=false;
          }else if(this.primaryfilter === 'ASSIGNED' && e.assignee){
          this.tasks=[]
            this.filterActive=false;
          } else{
            if(!this.primaryfilter === 'ASSIGNED'){
              this.filterTasks.assignee=e.assignee 
            }
            if(!(this.primaryfilter === 'OVERDUE' || this.primaryfilter === 'TODAY' || this.primaryfilter === 'TOMORROW' || this.primaryfilter === 'UPCOMING')){
              this.filterTasks.dueDate=e.dueDate 
            }
            this.filterTasks.query=e.query           
            this.filterTasks.statusId=e.statusId 
            this.filterTasks.priority=e.priority 
            this.filterTasks.showCompleteTasks=e.showCompleteTasks 
            this.resetSearch();
            this.searchTasks(this.tasksFilter)
        }
        }
      /*    return this.$tasksService.filterTasksList(e,filterGroupSort.groupBy,filterGroupSort.sortBy,filterLabels.labels).then((tasks) => {
          if (tasks.projectName){
             this.tasksFilter = tasks;
             this.filterActive=true;
          }else {
            //this.filterTaskDashboard(e)
            this.tasks=tasks.tasks;
            this.showCompleteTasks=e.showCompleteTasks;
            this.filterActive=false;
          }         
        }) */
      },
      changeSelectedTabItem() {
        this.isTasksTabChanged = !this.isTasksTabChanged;
      },
      searchTasks(tasks) {
        if(!tasks){
         tasks = this.filterTasks;
        }
        this.loadingTasks = true;
        if(tasks.assignee){
          tasks.projectId=-3
        }
        return this.$tasksService.filterTasksList(tasks,this.groupBy,this.sortBy,this.labels).then(data => {
           if (data.projectName){
             this.tasksFilter = data;
             this.filterActive=true;
          }else {
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
          .finally(() => this.loadingTasks = false);
      },
      getTasksByPrimary(primaryfilter) { 
        this.primaryfilter=primaryfilter         
        if(primaryfilter && (primaryfilter === 'OVERDUE' || primaryfilter === 'TODAY' || primaryfilter === 'TOMORROW' || primaryfilter === 'UPCOMING')){
          this.filterTasks.dueDate=primaryfilter
          this.filterTasks.assignee=''
          this.filterTasks.watcher=''
          this.filterTasks.coworker=''
         this.filterTasks.statusId=''
         this.filterTasks.priority=''
         this.filterTasks.query=''
         this.groupBy=''
         this.orderBy=''          
         this.filterTasks.projectId=-2
         this.resetSearch();
          this.searchTasks()
        }else if(primaryfilter && (primaryfilter === 'ASSIGNED')){
        this.filterTasks.dueDate=''
         this.filterTasks.assignee='ME'
         this.filterTasks.watcher=''
          this.filterTasks.coworker=''
         this.filterTasks.statusId=''
         this.filterTasks.priority=''
         this.filterTasks.query=''
         this.groupBy=''
         this.orderBy=''
         this.filterTasks.projectId=-3
         this.resetSearch();
          this.searchTasks()
        }else if(primaryfilter && (primaryfilter === 'WATCHED')){
          this.filterTasks.dueDate=''
          this.filterTasks.assignee=''
         this.filterTasks.watcher='ME'
         this.filterTasks.coworker=''
         this.filterTasks.statusId=''
         this.filterTasks.priority=''
         this.filterTasks.query=''
         this.groupBy=''
         this.orderBy=''
         this.filterTasks.projectId=-3
         this.resetSearch();
          this.searchTasks()
        }else if(primaryfilter && (primaryfilter === 'COLLABORATED')){
         this.filterTasks.dueDate=''
         this.filterTasks.assignee=''
         this.filterTasks.watcher=''
         this.filterTasks.coworker='ME'
         this.filterTasks.statusId=''
         this.filterTasks.priority=''
         this.filterTasks.query=''
         this.groupBy=''
         this.orderBy=''
         this.filterTasks.projectId=-3
         this.resetSearch();
          this.searchTasks()
        }else{
         this.filterTasks.dueDate=''
          this.filterTasks.assignee=''
          this.filterTasks.watcher=''
         this.filterTasks.coworker=''
         this.filterTasks.statusId=''
         this.filterTasks.priority=''
         this.filterTasks.query=''
         this.groupBy=''
         this.orderBy=''
         this.filterTasks.projectId=-2
         this.resetSearch();
         this.searchTasks() 
        }
        if(this.$refs.taskToolBar){
          this.$refs.taskToolBar.resetFields("primary");
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
          return 'label.noProject'
        }
        else if (name==='No Label'){
          return 'label.noLabel'
        }
        else if (name==='No due date'){
          return 'label.noDueDate'
        }
        else if (name==='Overdue'){
          return 'label.overdue'
        }
        else if (name==='Today'){
          return 'label.today'
        }
        else if (name==='Tomorrow'){
          return 'label.tomorrow'
        }
        else if (name==='Upcoming'){
          return 'label.upcoming'
        }
        return name;
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
      }
    }
  }
</script>
