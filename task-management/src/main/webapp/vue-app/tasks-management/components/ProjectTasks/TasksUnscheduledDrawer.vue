<template>
  <exo-drawer 
    ref="unscheduledTasksDrawer" 
    right
    body-classes="hide-scroll decrease-z-index-more"
    class="unscheduledTasksDrawer">
    <template slot="title">
      <div class="title-wrapper d-flex align-center justify-space-between">
        <div class="titleAndFilter">
          <span v-if="!displayFilterArea" class="no-work-plan-title">{{ $t('label.noWorkPlan.tasks') }}</span>
          <v-text-field
            v-else
            v-model="keyword"
            :placeholder=" $t('label.filterTask') "
            class="unscheduledTasksFilter pa-0 me-3 my-auto"
            clearable
            autofocus />
        </div>
        <div class="unscheduledTasksAction">
          <v-btn
            v-if="!displayFilterArea && filtredTasks && filtredTasks.length"
            class="filter-btn"
            icon>
            <v-icon @click="showFilter = !showFilter">mdi-filter</v-icon>
          </v-btn>
          <v-btn
            v-if="displayFilterArea && filtredTasks && filtredTasks.length"
            class="filter-btn"
            icon>
            <v-icon @click="showFilter = !showFilter">mdi-close-circle</v-icon>
          </v-btn>
          <v-btn icon>
            <v-icon @click="openTaskDrawer()">mdi-plus</v-icon>
          </v-btn>
        </div>
      </div>
    </template>
    <template v-if="filtredTasks && filtredTasks.length" slot="content">
      <task-view-card
        v-for="taskItem in filtredTasks"
        :key="taskItem.task.id"
        :task="taskItem"
        :show-completed-tasks="showCompletedTasks" />
    </template>
    <template v-else slot="content">
      <div class="noUnscheduledTasksFound">
        <div class="noTasksIcon"><i class="uiIcon uiIconTask"></i></div>
        <div class="noTasksLabel"><span>{{ $t('label.noTask') }}</span></div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    project: {
      type: String,
      default: ''
    },
    showCompletedTasks: {
      type: Boolean,
      default: false
    },
  },
  data: () => ({
    unscheduledTasks: [],
    keyword: null,
    showFilter: false
  }),
  computed: {
    displayFilterArea () {
      return this.showFilter;
    },
    filtredTasks () {
      if (this.keyword === null) {
        return this.unscheduledTasks;
      }  else {
        return this.unscheduledTasks.filter(task => task.task.title && task.task.title.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
      }
    }
  },
  mounted() {
    this.$root.$on('displayTasksUnscheduledDrawer', tasksList => {
      this.unscheduledTasks = [];
      this.showFilter= false;
      this.unscheduledTasks = tasksList;
      this.$refs.unscheduledTasksDrawer.open();
      this.$root.$emit('display-back-arrow');
    });
    this.$root.$on('open-task-drawer', () => {
      if (this.$refs.unscheduledTasksDrawer && this.$refs.unscheduledTasksDrawer.drawer) {
        this.$root.$emit('display-back-arrow');
      }
      this.$refs.unscheduledTasksDrawer.close();
    });
    this.$root.$on('displayUnscheduledDrawer', () => {
      this.$refs.unscheduledTasksDrawer.open();
    });
    this.$root.$on('refresh-unscheduled-gantt', tasksList => {
      this.unscheduledTasks = [];
      this.unscheduledTasks = tasksList;
    });
  },
  methods: {
    openTaskDrawer() {
      const defaultTask= {id: null,
        status: {project: this.project},
        priority: 'NONE',
        description: '',
        title: ''};
      this.$root.$emit('open-task-drawer', defaultTask);
      this.$root.$emit('display-back-arrow');
    },
  },
};
</script>