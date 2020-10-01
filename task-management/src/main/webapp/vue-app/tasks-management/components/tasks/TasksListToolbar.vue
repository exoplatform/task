<template>
  <v-app id="TasksToolbar">
    <v-toolbar
      id="TasksListToolbar"
      flat
      class="tasksToolbar pb-3">
      <!--<v-toolbar-title>
        <v-btn
          class="btn px-2 btn-primary addNewProjectButton">
          <span class="d-none font-weight-regular d-sm-inline">
            + {{ $t('label.addTask') }}
          </span>
        </v-btn>
      </v-toolbar-title>-->
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
      <!--<v-scale-transition>
        <v-text-field
          :placeholder="$t('label.filterTask','Enter task')"
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
      </v-scale-transition>-->
    </v-toolbar>
    <task-filter-drawer
      ref="filterTasksDrawer"/>
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
    methods: {
      openDrawer() {
        this.$refs.filterTasksDrawer.open();
      },
    }
  }
</script>
