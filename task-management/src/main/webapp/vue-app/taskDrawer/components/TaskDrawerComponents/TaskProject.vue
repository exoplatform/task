<template>
  <div>
    <v-container
      pa-0
      fluid
      d-flex>
      <i class="uiIcon icon-ArrowRight uiIconLightGray mr-1"></i>
      <v-combobox
        ref="select"
        :filter="filterProjects"
        v-model="projectModel"
        :items="projects"
        :label="projectLabel"
        attach
        class="pt-0 mb-0 inputTaskProjectName taskInputArea"
        solo
        prepend-icon
        @click="$emit('projectsListOpened')"
        @change="deleteProject()">
        <template v-slot:selection="{ attrs, item, parent, selected }">
          <v-chip
            v-if="item === Object(item)"
            v-bind="attrs"
            :color="`${item.color} lighten-3`"
            :input-value="selected"
            :title="$t('tooltip.clickToEdit')"
            class="projectName"
            small
            close
            text-color="white"
            @click="$emit('projectsListOpened')"
            @click:close="deleteProject">
            <span 
              class="body-2 text-truncate"
              @click="parent.selectItem(item)">
              {{ item.name }}
            </span>
          </v-chip>
        </template>
        <template v-slot:item="{ index, item }">
          <v-list-item @click="updateTaskProject(item)">
            <v-chip
              :color="`${item.color} lighten-3`"
              dark
              close
              text-color="white"
              small>
              <span class="text-truncate">
                {{ item.name }}
              </span>
            </v-chip>
          </v-list-item>
        </template>
      </v-combobox>
    </v-container>
  </div>
</template>

<script>
  import {getProjects, updateTask, getDefaultStatusByProjectId} from '../../taskDrawerApi'
  export default {
    props: {
      task: {
        type: Object,
        default: () => {
          return {};
        }
      }
    },
    data() {
      return {
        projects: [],
        projectModel:null,
        search: null,
        projectLabel: '',
        menuId: `ProjectMenu${parseInt(Math.random() * 10000)
          .toString()
          .toString()}`,
      }
    },
    watch: {
      projectModel () {
        setTimeout(() => {
          this.$refs.select.isMenuActive = false;
          }, 50);
      },
      task() {
        if(this.task && this.task.status && this.task.status.project && !this.task.status.name) {
          getDefaultStatusByProjectId(this.task.status.project.id).then((status) => {
            this.task.status = status;
          })
        }
      }
      },
    created() {
      this.getProjects();
      $(document).on('mousedown', () => {
        if (this.$refs.select.isMenuActive) {
          window.setTimeout(() => {
            this.$refs.select.isMenuActive = false;
          }, 200);
        }
      });
      document.addEventListener('closeProjectList',()=> {
        setTimeout(() => {
          if (typeof this.$refs.select !== 'undefined') {
            this.$refs.select.isMenuActive = false;
          }
        }, 100);
      });
      document.addEventListener('loadProjectName', event => {
        if (event && event.detail) {
          const task = event.detail;
          if(task.id!=null && task.status && task.status.project) {
            this.projectModel = this.task.status.project;
            this.projectLabel = this.$t('label.tapProject.name')
          } else if (task.id==null && task.status && task.status.project){
            this.projectModel = this.task.status.project;
            this.projectLabel = this.$t('label.tapProject.name')
          } else {
            this.projectModel = null;
            this.projectLabel = this.$t('label.noProject');
          }
        }
      });
    },
    methods: {
      getProjects() {
        getProjects().then((projects) => {
          this.projects = projects.projects;
        })
      },
      filterProjects(item, queryText) {
        return ( item.name.toLocaleLowerCase().indexOf(queryText.toLocaleLowerCase()) >-1 || item.name.toLocaleLowerCase().indexOf(queryText.toLocaleLowerCase()) > -1 );},
      updateTask() {
        updateTask(this.task.id, this.task);
        },
      updateTaskProject(project) {
        getDefaultStatusByProjectId(project.id).then((status) => {
          this.task.status = status;
          this.updateTask();
          this.projectModel = project;
        })
      },
      deleteProject(event) {
        this.task.status = null;
        this.projectModel = null;
        this.projectLabel = this.$t('label.noProject');
        this.updateTask();
        event.preventDefault();
        event.stopPropagation();
        },
    }
  }
</script>