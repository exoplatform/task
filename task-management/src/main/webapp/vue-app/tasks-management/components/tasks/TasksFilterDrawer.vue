<template>
  <exo-drawer
    ref="filterTasksDrawer"
    class="filterTasksDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('label.sortAndFilter') }}
    </template>
    <template slot="content">
      <v-tabs>
        <v-tab>Group and Sort</v-tab>
        <v-tab>Group and Sort</v-tab>
      </v-tabs>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
    methods: {
      open() {
        this.$refs.filterTasksDrawer.open();
      },
      cancel() {
        this.$refs.filterTasksDrawer.close();
      }
    }
  }
</script>
