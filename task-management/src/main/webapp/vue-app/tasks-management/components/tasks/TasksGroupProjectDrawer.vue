<template>
  <div
    ref="filterGroupTasksDrawer"
    class="filterGroupTasksDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <form ref="form1" class="mt-4">
      <v-label for="name">
        <span class="font-weight-bold body-2">{{ $t('label.task.groupBy') }}</span>
      </v-label>
    </form>
    <v-radio-group
      v-model="groupBy"
      mandatory>
      <v-radio
        :label="$t('label.task.none')"
        value="none"/>
      <v-radio
        :label="$t('label.task.assignee')"
        value="assignee"/>
      <v-radio
        :label="$t('label.task.labels')"
        value="label"/>
      <v-radio
        :label="$t('label.task.dueDate')"
        value="dueDate"/>
      <v-radio
        :label="$t('label.task.completed')"
        value="completed"/>
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
        groupBy: this.value,
      }
    },
    watch: {
      groupBy(val) {
        this.$emit('input', val);
      },
    },created() {
      this.$root.$on('reset-filter-task-group-sort',tasks =>{
        this.groupBy = tasks;
      });
    }
  }
</script>

