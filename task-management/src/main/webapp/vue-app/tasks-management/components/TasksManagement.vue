<template>
  <v-app id="TasksManagementPortlet">
    <v-tabs 
      v-model="tab" 
      slider-size="4" 
      class="tasksMenuParent white">
      <v-tab href="#tab-1">
        {{ $t('label.tasks.header') }}
      </v-tab>
      <v-tab href="#tab-2">
        {{ $t('label.projects') }}
      </v-tab>
    </v-tabs>
    <v-tabs-items v-model="tab">
      <v-tab-item value="tab-1">
        <tasks-dashboard/>
      </v-tab-item>
      <v-tab-item value="tab-2">
        <project-dashboard/>
      </v-tab-item>
    </v-tabs-items>
  </v-app>
</template>
<script>
  export default {
    data () {
      return {
        tab: 'tab-2'
      }
    }
  }
</script>