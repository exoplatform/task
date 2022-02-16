<template>
  <exo-drawer ref="assigneeAndCoworkerDrawer" right>
    <template slot="title">
      {{ $t('label.assigneeAndCoworkers') }}
    </template>
    <template v-if="tasksAssigneeAndCoworker" slot="content">
      <v-layout column class="ma-3">
        <v-flex
          v-for="user in tasksAssigneeAndCoworker"
          :key="user"
          class="flex-grow-1 text-truncate my-2">
          <exo-user
            :identity="user"
            popover />
        </v-flex>
      </v-layout>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    tasksAssigneeAndCoworker: [],
  }),
  mounted() {
    this.$root.$on('displayTasksAssigneeAndCoworker', assigneeAndCoworkerDrawer => {
      this.tasksAssigneeAndCoworker = assigneeAndCoworkerDrawer;
      this.$refs.assigneeAndCoworkerDrawer.open();
    });
  }
};
</script>