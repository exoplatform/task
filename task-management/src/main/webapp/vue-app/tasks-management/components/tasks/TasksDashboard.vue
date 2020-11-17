<template>
  <v-app
    id="tasksListApplication"
    class="projectAndTasksContainer transparent"
    flat>
    <tasks-list-toolbar
      :task-card-tab="'#tasks-cards'"
      :task-list-tab="'#tasks-list'"
      :keyword="keyword"
      @keyword-changed="keyword = $event"
      @changed="changeSelectedTabItem()"
      @filter-task-dashboard="filterTaskDashboard"
      @reset-filter-task-dashboard="resetFiltertaskDashboard"/>
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
    <v-row class="ma-0 border-box-sizing">
      <v-btn
        v-if="canShowMore"
        :loading="loadingTasks"
        :disabled="loadingTasks"
        class="loadMoreButton ma-auto mt-4 btn"
        block
        @click="loadNextPage">
        {{ $t('spacesList.button.showMore') }}
      </v-btn>
    </v-row>
    <tasks-assignee-coworker-drawer/>
  </v-app>
</template>
<script>
  export default {
    data () {
      return {
        isTasksTabChanged: false,
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
      }
    },
    computed: {
      canShowMore() {
        return this.loadingTasks || this.tasks.length >= this.limitToFetch;
      },
    },
    watch: {
      keyword() {
        if (!this.keyword) {
          this.resetSearch();
          this.searchTasks();
          return;
        }
        this.startTypingKeywordTimeout = Date.now();
        if (!this.loadingTasks) {
          this.loadingTasks = true;
          this.waitForEndTyping();
        }
      },
      limitToFetch() {
        this.searchTasks();
      },
    },
    created() {
      window.history.pushState('mytasks', 'My Tasks', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest?mytasks`);
      this.originalLimitToFetch = this.limitToFetch = this.limit;
      this.$root.$on('task-added', task => {
       this.searchTasks();
      });
    },
    methods: {
      resetFiltertaskDashboard(){
        this.searchTasks();
      },
      filterTaskDashboard(e){
        this.tasks=e.tasks;
        this.showCompleteTasks=e.showCompleteTasks;
      },
      changeSelectedTabItem() {
        this.isTasksTabChanged = !this.isTasksTabChanged;
      },
      searchTasks() {
        const tasks = {
          query: this.keyword,
          offset: this.offset,
          limit: this.limitToFetch,
          showCompleteTasks:this.showCompleteTasks,
        };
        this.loadingTasks = true;
        return this.$tasksService.filterTasksList(tasks).then(data => {
          this.tasks = data && data.tasks || [];
          this.tasksSize = data && data.tasksNumber || 0;
          return this.$nextTick();
        }).then(() => {
          if (this.keyword && this.tasks.length >= this.limitToFetch) {
            this.limitToFetch += this.pageSize;
          }
        })
          .finally(() => this.loadingTasks = false);
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
    }
  }
</script>
