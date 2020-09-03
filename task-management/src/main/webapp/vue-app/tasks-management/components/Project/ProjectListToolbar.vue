<template>
  <v-toolbar 
    id="ProjectListToolbar"
    flat 
    class="tasksToolbar pb-3">
    <v-toolbar-title>
      <v-btn
        class="btn px-2 btn-primary addNewProjectButton">
        <span class="d-none font-weight-regular d-sm-inline">
          + Add Project
        </span>
      </v-btn>
    </v-toolbar-title>
    <v-spacer/>
    <v-scale-transition>
      <v-text-field
        placeholder="Filter by project"
        prepend-inner-icon="fa-filter"
        class="inputTasksFilter pa-0 mr-3 my-auto"/>
    </v-scale-transition>
    <v-scale-transition>
      <select
        class="selectTasksFilter my-auto mr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
        <option
          value="allProjects">
          All Projects
        </option>
        <option
          value="favorites">
          Favorites
        </option>
      </select>
    </v-scale-transition>
  </v-toolbar>
</template>