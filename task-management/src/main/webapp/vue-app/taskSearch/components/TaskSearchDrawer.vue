<template>
  <task-drawer
    ref="taskDrawer"
    :task="task" />
</template>

<script>
export default {
  data: () => ({
    task: null,
  }),
  mounted() {
    document.addEventListener('openTaskDetail', this.openTaskDetail);
  },
  methods: {
    openTaskDetail(event) {
      this.task = event && event.detail;
      this.$refs.taskDrawer.open(this.task);
    },
  }
};
</script>
