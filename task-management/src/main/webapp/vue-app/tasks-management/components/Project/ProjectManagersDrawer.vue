<template>
  <exo-drawer ref="managersDrawer" right>
    <template slot="title">
      {{ $t('label.projectManager') }}
    </template>
    <template v-if="projectManagers" slot="content">
      <v-layout column class="ma-3">
        <v-flex
          v-for="manager in projectManagers"
          :key="manager"
          class="flex-grow-1 text-truncate my-2">
          <exo-user-avatar
            :username="manager.username"
            :fullname="manager.displayName"
            :title="manager.displayName" />
        </v-flex>
      </v-layout>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
    data: () => ({
      projectManagers: [],
    }),
    mounted() {
      this.$root.$on('displayProjectManagers', projectManagers => {
        this.projectManagers = projectManagers;
        this.$refs.managersDrawer.open();
      });
    }
  };
</script>