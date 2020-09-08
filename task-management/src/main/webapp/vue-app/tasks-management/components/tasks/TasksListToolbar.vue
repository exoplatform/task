<template>
  <v-toolbar
    id="TasksListToolbar"
    flat
    class="tasksToolbar pb-3">
    <v-toolbar-title>
      <v-btn
        class="btn px-2 btn-primary addNewProjectButton">
        <span class="d-none font-weight-regular d-sm-inline">
          + {{ $t('label.addTask') }}
        </span>
      </v-btn>
    </v-toolbar-title>
    <v-spacer/>
    <v-scale-transition>
      <v-text-field
        placeholder="Filter by task"
        prepend-inner-icon="fa-filter"
        class="inputTasksFilter pa-0 mr-3 my-auto"/>
    </v-scale-transition>
  </v-toolbar>
</template>