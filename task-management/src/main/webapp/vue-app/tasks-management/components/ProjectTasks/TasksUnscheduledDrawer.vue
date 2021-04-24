<template>
  <exo-drawer 
    ref="unscheduledTasksDrawer" 
    right
    body-classes="hide-scroll decrease-z-index-more"
    class="unscheduledTasksDrawer">
    <template slot="title">
      <div class="title-wrapper d-flex align-center justify-space-between">
        <span class="no-work-plan-title">{{ $t('label.noWorkPlan.tasks') }}</span>
        <v-btn icon>
          <v-icon @click="openTaskDrawer()">mdi-plus</v-icon>
        </v-btn>
      </div>
    </template>
    <template v-if="unscheduledTasks" slot="content">
      <task-view-card
        v-for="taskItem in unscheduledTasks"
        :key="taskItem.task.id"
        :task="taskItem" />
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    project: {
      type: String,
      default: ''
    }
  },
  data: () => ({
    unscheduledTasks: [],
  }),
  watch: {
    projectId() {
      console.warn('this.projectId',this.projectId);
    }
  },
  mounted() {
    this.$root.$on('displayTasksUnscheduledDrawer', tasksList => {
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