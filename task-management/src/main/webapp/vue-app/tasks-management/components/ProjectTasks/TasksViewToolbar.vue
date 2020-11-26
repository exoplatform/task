<template>
  <v-app>
    <v-toolbar
      id="TasksDashboardToolbar"
      flat
      class="tasksToolbar pb-3">
      <v-toolbar-title>
        <v-btn
          class="btn px-2 btn-primary addNewTaskButton"
          @click="openTaskDrawer()">
          <span class="d-none font-weight-regular d-sm-inline">
            + {{ $t('label.addTask') }}
          </span>
        </v-btn>
      </v-toolbar-title>
      <v-spacer/>
      <div class="taskDisplay pt-2">
        <v-tabs>
          <v-tab
            :href="taskCardTabView"
            class="taskTabBoard"
            @change="$emit('taskViewChangeTab', 'board')">
            <i class="uiIcon uiIconBoard"></i>
            <span>{{ $t('label.boardView') }}</span>
          </v-tab>
          <v-tab
            :href="taskListTabView"
            class="taskTabList"
            @change="$emit('taskViewChangeTab', 'list')">
            <i class="uiIcon uiIconList"></i>
            <span>{{ $t('label.listView') }}</span>
          </v-tab>
          <!--<v-tab
            :href="taskGanttTabView"
            class="taskTabGantt"
            @change="$emit('taskViewChangeTab', 'gantt')">
            <i class="uiIcon uiIconGantt"></i>
            <span>Gantt</span>
          </v-tab>-->
        </v-tabs>
      </div>
      <v-spacer/>
      <v-scale-transition>
        <v-text-field
          v-model="keyword"
          :placeholder=" $t('label.filterTask') "
          prepend-inner-icon="fa-filter"
          class="inputTasksFilter pa-0 mr-3 my-auto"/>
      </v-scale-transition>
      <v-scale-transition>
        <v-btn
          class="btn px-2 btn-primary filterTasksSetting"
          outlined
          @click="openDrawer">
          <i class="uiIcon uiIconFilterSetting pr-3"></i>
          <span class="d-none font-weight-regular caption d-sm-inline">
            {{ $t('label.filter') }} (3)
          </span>
        </v-btn>
      </v-scale-transition>
    </v-toolbar>
    <task-filter-drawer
      ref="filterTasksDrawer"
      :project="project.id"
      :query="keyword"
      @filter-task="filterTasks"
      @reset-filter-task="resetFilterTask"/>
  </v-app>
</template>
<script>
  export default {
    props: {
      project:{
        type: Object,
        default: null
      },
      taskCardTabView:{
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
    },
    data () {
      return {
        keyword: null,
        awaitingSearch: false,
        searchonkeyChange:true
      }
    },
    watch: {
      keyword() {
         if(this.searchonkeyChange){
        if (!this.awaitingSearch) {
          setTimeout(() => {
            this.$emit('keyword-changed', this.keyword);
            this.awaitingSearch = false;
          }, 1000); 
        }
        this.awaitingSearch = true;
        }
      },
    },
    methods: {
      openDrawer() {
        this.$refs.filterTasksDrawer.open();
      },
      openTaskDrawer() {
        const defaultTask= {id:null,
        status:{project:this.project},
        priority:'NONE',
        description:'',
        title:''}
        this.$root.$emit('open-task-drawer', defaultTask)
      },
      resetFilterTask(){
        this.searchonkeyChange=false
        this.keyword=""
        this.searchonkeyChange=true
        this.$emit('reset-filter-task-dashboard');
      },
      filterTasks(e){
        this.searchonkeyChange=false
        this.showCompleteTasks=e.showCompleteTasks;
        this.keyword=e.tasks.query
        this.searchonkeyChange=true
        this.$emit('filter-task-dashboard', { tasks:e.tasks,showCompleteTasks:e.showCompleteTasks });
      }
    }
  }
</script>