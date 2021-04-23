<template>
  <exo-drawer 
    ref="unscheduledTasksDrawer" 
    right
    body-classes="hide-scroll decrease-z-index-more"
    class="unscheduledTasksDrawer">
    <template slot="title">
      {{ $t('label.noWorkPlan.tasks') }}
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
  data: () => ({
    unscheduledTasks: [],
  }),
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
  }
};
</script>