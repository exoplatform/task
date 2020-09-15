<template>
  <exo-drawer
    ref="AddProjectDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('label.addProject') }}
    </template>
    <template slot="content">
      <div class="addProjectTitle d-flex align-center pa-4">
        <i class="uiIcon uiIconProject"></i>
        <v-text-field
          :label="$t('label.label.titleProject')"
          class="projectInputTitle pl-3 pt-1"
          single-line
        ></v-text-field>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
    methods: {
      open() {
        this.$refs.AddProjectDrawer.open();
      },
      cancel() {
        this.$refs.AddProjectDrawer.close();
      },
    }
  }
</script>