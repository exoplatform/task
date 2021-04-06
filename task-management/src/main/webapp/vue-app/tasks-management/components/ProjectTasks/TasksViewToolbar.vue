<template>
  <v-app>
    <v-toolbar
      id="TasksDashboardToolbar"
      flat
      class="tasksToolbar">
      <div class="taskDisplay">
        <v-tabs class="projectTasksViewTabs">
          <v-tab
            :href="taskCardTabView"
            class="taskTabBoard"
            @change="changeTaskViewTab('board')">
            <i class="uiIcon uiIconBoard"></i>
            <span>{{ $t('label.boardView') }}</span>
          </v-tab>
          <v-tab
            :href="taskListTabView"
            class="taskTabList"
            @change="changeTaskViewTab('list')">
            <i class="uiIcon uiIconList"></i>
            <span>{{ $t('label.listView') }}</span>
          </v-tab>
          <v-tab
            v-if="allowGantt"
            :href="taskGanttTabView"
            class="taskTabGantt"
            @change="changeTaskViewTab('gantt')">
            <i class="uiIcon uiIconGantt"></i>
            <span>Gantt</span>
          </v-tab>
        </v-tabs>
      </div>
      <v-spacer />
      <v-scale-transition>
        <v-text-field
          v-model="keyword"
          :placeholder=" $t('label.filterTask') "
          prepend-inner-icon="fa-filter"
          class="inputTasksFilter pa-0 mr-3 my-auto"
          clearable />
      </v-scale-transition>
      <v-scale-transition>
        <v-btn
          class="btn px-2 btn-primary filterTasksSetting"
          outlined
          @click="openDrawer">
          <i class="uiIcon uiIconFilterSetting pr-3"></i>
          <span class="d-none font-weight-regular caption d-sm-inline">
            {{ $t('label.filter') }} {{ getFilterNum() }}
          </span>
        </v-btn>
      </v-scale-transition>
    </v-toolbar>
    <task-filter-drawer
      ref="filterTasksDrawer"
      :project="project.id"
      :query="keyword"
      :status-list="statusList"
      :task-view-tab-name="taskViewTabName"
      @filter-num-changed="filterNumChanged"
      @filter-task="filterTasks"
      @reset-filter-task="resetFilterTask" />
  </v-app>
</template>
<script>
export default {
  props: {
    project: {
      type: Object,
      default: null
    },
    statusList: {
      type: Array,
      default: () => []
    },
    taskCardTabView: {
      type: String,
      default: ''
    },
    taskListTabView: {
      type: String,
      default: ''
    },
    taskGanttTabView: {
      type: String,
      default: ''
    },
    allowGantt: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      keyword: null,
      awaitingSearch: false,
      filterNumber: 0,
      searchonkeyChange: true,
      taskViewTabName: '',
    };
  },
  watch: {        
    keyword() {  
      if (!this.awaitingSearch) {
        const searchonkeyChange = this.searchonkeyChange;
        setTimeout(() => {
          this.$emit('keyword-changed', this.keyword,searchonkeyChange);
          this.awaitingSearch = false;
        }, 1000);
      }
      this.awaitingSearch = true;
      if (this.searchonkeyChange){
        this.resetFields('query'); }      
      this.searchonkeyChange= true;
    },
  },
  methods: {
    openDrawer() {
      this.$refs.filterTasksDrawer.open();
    },
    openTaskDrawer() {
      const defaultTask= {id: null,
        status: {project: this.project},
        priority: 'NONE',
        description: '',
        title: ''};
      this.$root.$emit('open-task-drawer', defaultTask);
    },
    resetFilterTask(){
      this.searchonkeyChange=false;
      this.keyword='';
      this.searchonkeyChange=true;
      this.$emit('reset-filter-task-dashboard');
    },
    filterTasks(e){
      this.searchonkeyChange=false;
      this.showCompleteTasks=e.showCompleteTasks;
      this.keyword=e.tasks.query;
      this.$emit('filter-task-dashboard', { tasks: e.tasks,filterLabels: e.filterLabels,showCompleteTasks: e.showCompleteTasks });
    },
    resetFields(activeField){
      this.$refs.filterTasksDrawer.resetFields(activeField);
    },
    filterNumChanged(filtersnumber){
      this.filterNumber=filtersnumber;
    },
    getFilterNum(){
      if (this.filterNumber>0){
        return `(${this.filterNumber})`;
      } return '';
    },
    changeTaskViewTab(view){
      this.taskViewTabName=view;
      this.$emit('taskViewChangeTab', view);
    }
  }
};
</script>
