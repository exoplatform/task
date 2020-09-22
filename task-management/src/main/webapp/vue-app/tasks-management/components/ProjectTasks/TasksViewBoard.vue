<template>
  <v-card
    class="tasksView tasksViewBoard pt-6"
    flat>
    <v-item-group class="pa-4">
      <v-container class="pa-0">
        <v-row class="ma-0 border-box-sizing">
          <v-col
            v-for="(status, index) in statusList"
            :key="index"
            class="py-0 px-4 projectTaskItem">
            <tasks-view-header
              :status="status"
              :view-type="'board'"/>
            <v-divider/>
          </v-col>
        </v-row>
      </v-container>
    </v-item-group>
  </v-card>
</template>
<script>
  export default {
    props: {
      statusList: {
        type: Array,
        default: () => []
      }
    }
  }
</script>