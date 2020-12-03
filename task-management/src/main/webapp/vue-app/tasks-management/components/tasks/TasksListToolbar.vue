<template>
  <v-app id="TasksToolbar">
    <v-toolbar
      id="TasksListToolbar"
      flat
      class="tasksToolbar pb-3">
      <v-toolbar-title>
        <v-btn
          class="btn px-2 btn-primary addNewProjectButton"
          @click="openTaskDrawer()">
          <span class="d-none font-weight-regular d-sm-inline">
            + {{ $t('label.addTask') }}
          </span>
        </v-btn>
      </v-toolbar-title>
      <v-spacer/>
      <!--<div class="taskDisplay">
        <v-tabs
          @change="$emit('changed')">
          &lt;!&ndash;<v-tab :href="taskCardTab" class="taskTabCards">
            <i class="uiIcon uiIconCards"></i>
            <span>{{ $t('label.cardsView') }}</span>
          </v-tab>&ndash;&gt;
          <v-tab :href="taskListTab" class="taskTabList">
            <i class="uiIcon uiIconList"></i>
            <span>{{ $t('label.listView') }}</span>
          </v-tab>
        </v-tabs>
      </div>-->
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
            {{ $t('label.filter') }}
          </span>
        </v-btn>
      </v-scale-transition>
    </v-toolbar>
    <task-filter-drawer
      ref="filterTasksDrawer"
      :query="keyword"
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
        searchonkeyChange:true,
        task: {
          id:null,
          status:{}
          },
        drawer:null
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
      resetFilterTask(){
        this.$emit('reset-filter-task-dashboard');
      },filterTaskquery(e){
        this.searchonkeyChange=false
        this.showCompleteTasks=e.showCompleteTasks;
        this.keyword=e.query
        this.searchonkeyChange=true
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
        this.task = {
          id:null,
          status:{},
        };
        this.$root.$emit('open-task-drawer', this.task);
      },
    }
  }
</script>
