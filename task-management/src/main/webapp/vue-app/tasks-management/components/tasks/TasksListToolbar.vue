<template>
  <v-app id="TasksToolbar">
    <v-toolbar
      id="TasksListToolbar"
      flat
      class="tasksToolbar">
      <v-toolbar-title>
        <v-btn
          class="btn px-2 btn-primary addNewProjectButton"
          @click="openTaskDrawer()">
          <v-icon dark class="d-block d-sm-none">mdi-plus</v-icon>
          <span class="d-none font-weight-regular d-sm-inline">
            + {{ $t('label.addTask') }}
          </span>
        </v-btn>
      </v-toolbar-title>
      <v-spacer/>
      <v-spacer/>
      <v-scale-transition>
        <v-text-field
          v-model="keyword"
          :placeholder=" $t('label.filterTask') "
          prepend-inner-icon="fa-filter"
          class="inputTasksFilter pa-0 mr-3 my-auto"
          clearable/>
      </v-scale-transition>
      <v-scale-transition>
        <select
          v-model="primaryFilterSelected"
          name="primaryFilter"
          class="selectPrimaryFilter input-block-level ignore-vuetify-classes  pa-0 mr-3 my-auto"
          @change="changePrimaryFilter">

          <option
            v-for="item in primaryFilter"
            :key="item.name"
            :value="item.name">
            {{ $t('label.dueDate.'+item.name.toLowerCase()) }}
          </option>
        </select>
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
      :query="keyword"
      @filter-num-changed="filterNumChanged"
      @filter-task="filterTasks"
      @reset-filter-task="resetFilterTask"
      @filter-task-query="filterTaskquery"/>
  </v-app>
</template>
<script>
  export default {
    props: {
      taskCardTab:{
        type: String,
        default: ''
      },
      taskListTab: {
        type: String,
        default: ''
      },
    },
    data () {
      return {
        tasks:null,
        keyword: null,
        awaitingSearch: false,
        searchOnKeyChange:true,
        filterNumber:0,
        primaryFilterSelected:'ALL',
        drawer:null,
        primaryFilter: [
          {name: "ALL"},{name: "ASSIGNED"},{name: "COLLABORATED"},{name: "OVERDUE"},{name: "TODAY"},{name: "TOMORROW"},{name: "UPCOMING"}
        ],
      }
    },
    watch: {
      keyword() {  
          if (!this.awaitingSearch) {
            const searchOnKeyChange = this.searchOnKeyChange
            setTimeout(() => {
              this.$emit('keyword-changed', this.keyword,searchOnKeyChange);
              this.awaitingSearch = false;
            }, 1000);
          }
          this.awaitingSearch = true;  
        this.searchOnKeyChange= true;
      },
    },created() {
    this.primaryFilterSelected = localStorage.getItem('primary-filter-tasks');
    localStorage.setItem('primary-filter-tasks', 'ALL');
    this.changePrimaryFilter();
  },
    methods: {
      resetFilterTask(){
        this.$emit('reset-filter-task-dashboard');
      },
      filterTaskquery(e,filterGroupSort,filterLabels){
        this.searchOnKeyChange=false
        this.showCompleteTasks=e.showCompleteTasks;
        this.keyword=e.query
        this.$emit('filter-task-query',e,filterGroupSort,filterLabels)
      },
      filterTasks(e){
        this.tasks=e.tasks.tasks;
        this.showCompleteTasks=e.showCompleteTasks;
        this.$emit('filter-task-dashboard', { tasks:this.tasks,showCompleteTasks:this.showCompleteTasks });
      },
      openDrawer() {
        this.$refs.filterTasksDrawer.open();
      },
      openTaskDrawer() {
        const defaultTask = {
          id:null,
          status:{project:this.project},
          priority:'NONE',
          description:'',
          title:''
        }
        this.$root.$emit('open-task-drawer', defaultTask);
      },
      changePrimaryFilter(){  
       this.searchOnKeyChange=false 
       this.keyword=""   
       this.$emit('primary-filter-task', this.primaryFilterSelected);     
      },
      resetFields(activeField){
        this.searchOnKeyChange=false
          this.keyword=''
          this.$refs.filterTasksDrawer.resetFields(activeField);
      },
      filterNumChanged(filtersnumber,source){
        this.filterNumber=filtersnumber
      },
      getFilterNum(){
        if(this.filterNumber>0){
          return `(${this.filterNumber})`
        } return ''
      }

    }
  }
</script>
