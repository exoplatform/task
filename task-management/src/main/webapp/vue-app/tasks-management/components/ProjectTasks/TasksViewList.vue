<template>
  <v-card
    class="tasksView tasksViewList pt-6"
    flat>
    <v-item-group class="pa-4">
      <div class="ma-0 border-box-sizing">
        <div
          v-for="(status, index) in statusList"
          :key="index"
          class="py-0 px-4 projectTaskItem">
          <tasks-view-header
            :status="status"
            :view-type="'list'"/>
        </div>
        <v-divider/>
      </div>
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