<template>
  <task-drawer
    v-if="drawer"
    :drawer="drawer"
    :task="task"
    @closeDrawer="drawer = false"/>
</template>

<script>
export default {
  data: () => ({
    drawer: false,
    task: null,
  }),
  mounted() {
    document.addEventListener('openTaskDetail', this.openTaskDetail);
  },
  methods: {
    openTaskDetail(event) {
      this.task = event && event.detail;
      this.drawer = true;
    },
  }
};
</script>
