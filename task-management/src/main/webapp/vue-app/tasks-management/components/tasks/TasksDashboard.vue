<template>
  <v-app
    id="tasksListApplication"
    class="projectAndTasksContainer transparent"
    flat>
    <tasks-list-toolbar
      :task-card-tab="'#tasks-cards'"
      :task-list-tab="'#tasks-list'"
      @taskAdded="getTasksList()"
      @changed="changeSelectedTabItem()"/>
    <v-tabs-items>
      <v-tab-item v-show="isTasksTabChanged" eager>
        <tasks-cards-list
          :tasks="tasks"/>
      </v-tab-item>
      <v-tab-item v-show="!isTasksTabChanged" eager>
        <tasks-list
          :tasks="tasks"/>
      </v-tab-item>
    </v-tabs-items>
    <tasks-assignee-coworker-drawer/>
  </v-app>
</template>
<script>
  export default {
    data () {
      return {
        isTasksTabChanged: false,
        tasks: []
      }
    },
    created() {
      this.getTasksList();
    },
    methods: {
      changeSelectedTabItem() {
        this.isTasksTabChanged = !this.isTasksTabChanged;
      },
      getTasksList() {
        return this.$tasksService.getMyTasksList().then(data => {
          this.tasks = data.tasks;
        });
      }
    }
  }
</script>