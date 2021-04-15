<template>
  <div
    ref="filterGroupTasksDrawer"
    class="filterGroupTasksDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <div v-if="taskViewTabName === 'gantt'">
      <form ref="form1" class="mt-4">
        <v-label for="name">
          <span class="font-weight-bold body-2">Scale </span>
        </v-label>
      </form>
      <v-radio-group
        v-model="scaleBy"
        mandatory>
        <v-radio
          label="Day"
          value="Day" />
        <v-radio
          label="Week"
          value="Week" />
        <v-radio
          label="Month"
          value="Month" />
      </v-radio-group>
    </div>
    <div v-else>
      <form ref="form1" class="mt-4">
        <v-label for="name">
          <span class="font-weight-bold body-2">{{ $t('label.task.groupBy') }}</span>
        </v-label>
      </form>
      <v-radio-group
        v-model="groupBy"
        mandatory>
        <v-radio
          v-if="taskViewTabName !== 'list'"
          :label="$t('label.task.none')"
          value="none" />
        <v-radio
          v-if="taskViewTabName === 'list'"
          :label="$t('label.task.status')"
          value="status" />
        <v-radio
          :label="$t('label.task.assignee')"
          value="assignee" />
        <v-radio
          :label="$t('label.task.labels')"
          value="label" />
        <v-radio
          :label="$t('label.task.dueDate')"
          value="dueDate" />
        <v-radio
          :label="$t('label.task.completed')"
          value="completed" />
      </v-radio-group>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: ''
    },
    taskViewTabName: {
      type: String,
      default: ''
    },
  },
  data() {
    return {
      groupBy: this.value,
      scaleBy: 'Week'

    };
  },
  watch: {
    groupBy(val) {
      this.$emit('input', val);
    },
    scaleBy(val) {
      this.$emit('scale-changed', val);
    }
  },created() {
    this.$root.$on('reset-filter-task-group-sort',groupBy =>{
      this.groupBy = groupBy;
    });
  }
};
</script>

