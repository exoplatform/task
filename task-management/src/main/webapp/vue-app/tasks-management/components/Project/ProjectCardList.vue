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
          <v-row class="ma-0 border-box-sizing">
            <v-btn
              :disabled="disableBtn"
              class="loadMoreButton ma-auto mt-4 btn"
              block
              @click="loadNextPage">
              {{ $t('spacesList.button.showMore') }}
            </v-btn>
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
        projects: [],
        offset: 0,
        projectSize: 0,
        limit: 20,
        limitToFetch: 0,
        originalLimitToFetch: 0,
        disableBtn:false
      }
    },
    created() {
      this.originalLimitToFetch = this.limitToFetch = this.limit;
      this.getProjectsList();
    },
    methods: {
      getProjectsList() {
        return this.$projectService.getProjectsList(this.offset, this.limitToFetch).then(data => {
          this.projects.push(...data.projects);
          this.projectSize = data.projectNumber;
        });
      },
      loadNextPage() {
        if(this.limitToFetch <= this.projectSize) {
          this.offset+= this.limit;
          this.limitToFetch = this.limitToFetch += this.limit;
          this.getProjectsList()
        }else{
          this.disableBtn=true
        }
        
      },
    }
  }
</script>
