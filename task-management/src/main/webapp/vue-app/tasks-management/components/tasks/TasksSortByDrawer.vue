<template>
  <div
    ref="filterSortTasksDrawer"
    class="filterSortTasksDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <form ref="form1" class="mt-4">
      <v-label for="name">
        <span class="font-weight-bold body-2">{{ $t('label.task.sort') }}</span>
      </v-label>
    </form>
    <v-radio-group
      v-model="sortBy"
      mandatory>
      <v-radio
        :label="$t('label.task.status')"
        value="status" />
      <v-radio
        :label="$t('label.task.title')"
        value="title" />
      <v-radio
        :label="$t('label.task.dueDate')"
        value="dueDate" />
      <v-radio
        :label="$t('label.task.priority')"
        value="priority" />
      <v-radio
        :label="$t('label.task.rank')"
        value="rank" />
    </v-radio-group>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: ''
    },
  },
  data() {
    return {
      sortBy: this.value,
    };
  },
  watch: {
    sortBy(val) {
      this.$emit('input', val);
    },
  },
  created() {
    this.$root.$on('reset-filter-task-group-sort',tasks =>{
      this.sortBy = '';
    });
    this.$root.$on('reset-filter-task-sort',sortBy =>{
      this.sortBy = sortBy;
    });
  }
};
</script>

