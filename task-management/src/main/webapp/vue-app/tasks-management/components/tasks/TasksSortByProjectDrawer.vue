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
      v-model="orderBy"
      mandatory>
      <v-radio
        :label="$t('label.task.dueDate')"
        value="dueDate" />
      <v-radio
        :label="$t('label.task.priority')"
        value="priority" />
      <v-radio
        :label="$t('label.task.title')"
        value="title" />
      <v-radio
        :label="$t('label.task.lastUpdate')"
        value="createdTime" />
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
      orderBy: this.value,
    };
  },
  watch: {
    orderBy(val) {
      this.$emit('input', val);
    },
  },
  created() {
    this.$root.$on('reset-filter-task-group-sort', () =>{
      this.orderBy = '';
    });
    this.$root.$on('reset-filter-task-sort',orderBy =>{
      this.orderBy = orderBy;
    });
  }
};
</script>

