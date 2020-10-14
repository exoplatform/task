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
      <div class="taskDisplay">
        <v-tabs
          @change="$emit('changed')">
          <v-tab :href="taskCardTab" class="taskTabCards">
            <i class="uiIcon uiIconCards"></i>
            <span>{{ $t('label.cardsView') }}</span>
          </v-tab>
          <v-tab :href="taskListTab" class="taskTabList">
            <i class="uiIcon uiIconList"></i>
            <span>{{ $t('label.listView') }}</span>
          </v-tab>
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
    <task-drawer
      v-if="drawer"
      :drawer="drawer"
      :task="task"
      @updateTaskList="updateTaskList()"
      @addTask="onAddTask()"
      @closeDrawer="onCloseDrawer"/>
    <task-filter-drawer
      ref="filterTasksDrawer" 
      @filter-task="filterTasks"
      @reset-filter-task="resetFilterTask"/>
  </v-app>
</template>
<script>
  export default {
    props: {
      keyword: {
        type: String,
        default: null,
      },
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
        task: {id:null,status:{}},
        drawer:null
      }
    },
    watch: {
      keyword() {
        this.$emit('keyword-changed', this.keyword);
      }
    },
    methods: {
      resetFilterTask(){
        this.$emit('reset-filter-task-dashboard');
      },
      filterTasks(e){
        this.tasks=e;
        this.$emit('filter-task-dashboard', this.tasks);
      },
      openDrawer() {
        this.$refs.filterTasksDrawer.open();
      },
      openTaskDrawer() {
        this.drawer = true;
      },
      onCloseDrawer: function (drawer) {
        this.drawer = drawer;
      },
      onAddTask() {
        this.$emit('taskAdded')
      }
    }
  }
</script>
