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
              v-if="canShowMore"
              :loading="loadingProjects"
              :disabled="loadingProjects"
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
    props: {
      keyword: {
        type: String,
        default: null,
      },
      loadingProjects: {
        type: Boolean,
        default: false,
      },
    },
    data() {
      return {
        maxTasksSize: 10,
        projects: [],
        offset: 0,
        projectSize: 0,
        pageSize: 20,
        limit: 20,
        limitToFetch: 0,
        originalLimitToFetch: 0,
        disableBtn:false,
        startSearchAfterInMilliseconds: 600,
        endTypingKeywordTimeout: 50,
        startTypingKeywordTimeout: 0,
      }
    },
    computed: {
      canShowMore() {
        return this.loadingProjects || this.projects.length >= this.limitToFetch;
      },
    },
    watch: {
      keyword() {
        if (!this.keyword) {
          this.resetSearch();
          this.searchProjects();
          return;
        }
        this.startTypingKeywordTimeout = Date.now();
        if (!this.loadingProjects) {
          this.loadingProjects = true;
          this.waitForEndTyping();
        }
      },
      limitToFetch() {
        this.searchProjects();
      },
    },
    created() {
      this.originalLimitToFetch = this.limitToFetch = this.limit;
      this.searchProjects();
    },
    methods: {
      searchProjects() {
        this.loadingProjects = true;
        return this.$projectService.getProjectsList(this.keyword,this.offset, this.limitToFetch).then(data => {
          //this.projects.push(...data.projects);
          this.projects = data && data.projects || [];
          this.projectSize = data && data.projectNumber || 0;
          return this.$nextTick();
        }).then(() => {
          if (this.keyword && this.projects.length >= this.limitToFetch) {
            this.limitToFetch += this.pageSize;
          }
        })
          .finally(() => this.loadingProjects = false);
      },
      resetSearch() {
        if (this.limitToFetch !== this.originalLimitToFetch) {
          this.limitToFetch = this.originalLimitToFetch;
        }
      },
      loadNextPage() {
        this.originalLimitToFetch = this.limitToFetch += this.pageSize;
      },
      waitForEndTyping() {
        window.setTimeout(() => {
          if (Date.now() - this.startTypingKeywordTimeout > this.startSearchAfterInMilliseconds) {
            this.searchProjects();
          } else {
            this.waitForEndTyping();
          }
        }, this.endTypingKeywordTimeout);
      },
    }
  }
</script>
