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
    });
    this.$root.$on('open-task-drawer', () => {
      this.$refs.unscheduledTasksDrawer.close();
    });
  }
};
</script>