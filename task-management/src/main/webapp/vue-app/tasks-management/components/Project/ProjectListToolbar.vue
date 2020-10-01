<template>
  <v-app id="projectBoardToolbar">
    <v-toolbar
      id="ProjectListToolbar"
      flat
      class="tasksToolbar pb-3">
      <v-toolbar-title>
        <v-btn
          class="btn px-2 btn-primary addNewProjectButton"
          @click="openDrawer">
          <span class="d-none font-weight-regular d-sm-inline">
            + {{ $t('label.addProject') }}
          </span>
        </v-btn>
      </v-toolbar-title>
      <v-spacer/>
      <!--<v-scale-transition>
        <v-text-field
          :placeholder="$t('label.filterProject','Enter project name')"
          prepend-inner-icon="fa-filter"
          class="inputTasksFilter pa-0 mr-3 my-auto"/>
      </v-scale-transition>
      <v-scale-transition>
        <select
          class="selectTasksFilter my-auto mr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
          <option
            value="allProjects">
            {{ $t('label.allProject') }}
          </option>
          <option
            value="favorites">
            Favorites
          </option>
        </select>
      </v-scale-transition>-->
    </v-toolbar>
    <add-project-drawer
      ref="addProjectDrawer"/>
  </v-app>
</template>
<script>
  export default {
    methods: {
      openDrawer() {
        this.$refs.addProjectDrawer.open();
      },
    }
  }
</script>
