<template>
  <v-app id="ProjectCardList" class="tasksListContainer">
    <v-card flat>
      <v-item-group class="pa-4">
        <v-container class="pa-0">
          <v-row class="ma-0 border-box-sizing">
            <v-col
              v-for="project in projects"
              :key="project.id"
              :id="'project-'+project.id"
              cols="12"
              md="6"
              lg="4"
              xl="3"
              class="pa-0 projectItem">
              <project-card
                :project="project"/>
            </v-col>
          </v-row>
        </v-container>
      </v-item-group>
    </v-card>
  </v-app>
</template>
<script>
  export default {
    data() {
      return {
        maxTasksSize: 10,
        projects: []
      }
    },
    created() {
      this.getProjectsList();
    },
    methods: {
      getProjectsList() {
        return this.$tasksService.getProjectsList().then(data => {
          this.projects = data;
        });
      }
    }
  }
</script>