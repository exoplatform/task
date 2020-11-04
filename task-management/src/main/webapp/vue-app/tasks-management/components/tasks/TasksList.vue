<template>
  <v-app class="tasksList ma-3">
    <div
      v-for="task in tasks"
      :key="task.id"
      class="tasksListItem">
      <tasks-list-item
        :task="task" 
        @update-cart="updateCart"/>
    </div>
  </v-app>
</template>
<script>
  export default {
    props: {
      tasks: {
        type: Array,
        default: () => [],
      }
    },
    methods:{
      updateCart(e){

          this.tasks = this.tasks.filter((t) => t.task.id !== e.id);

      }
    }
  }

</script>
