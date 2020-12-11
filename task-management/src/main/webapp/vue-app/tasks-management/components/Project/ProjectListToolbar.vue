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
          <i class="uiIcon uiIconPlus"></i>
          <span class="d-none font-weight-regular d-sm-inline">
            {{ $t('label.addProject') }}
          </span>
        </v-btn>
      </v-toolbar-title>
      <v-spacer/>
      <v-scale-transition>
        <v-text-field
          v-model="keyword"
          :placeholder="$t('label.filterProject')"
          prepend-inner-icon="fa-filter"
          class="inputTasksFilter pa-0 mr-3 my-auto"/>
      </v-scale-transition>
      <v-scale-transition>
        <select
          v-model="projectFilterSelected"
          name="projectFilter"
          class="selectTasksFilter my-auto mr-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
          <option
            v-for="item in projectFilter"
            :key="item.name"
            :value="item.name">
            {{ $t('label.project.filter.'+item.name.toLowerCase()) }}
          </option>
        </select>
      </v-scale-transition>
    </v-toolbar>

  </v-app>
</template>
<script>
  export default {
    props: {
      keyword: {
        type: String,
        default: null,
      },
      projectFilterSelected: {
        type: String,
        default: 'ALL',
      },
    },
    data () {
      return {
        projectFilter: [
          {name: "ALL"},{name: "MANAGED"},{name: "COLLABORATED"},{name: "WITH_TASKS"}
        ],
      }
    },
    watch: {
      keyword() {
        this.$emit('keyword-changed', this.keyword);
      },
      projectFilterSelected() {
        this.$emit('filter-changed', this.projectFilterSelected);
      }
    },
    methods: {
      openDrawer() {
       this.$root.$emit('open-project-drawer', {})
      },
    }
  }
</script>
