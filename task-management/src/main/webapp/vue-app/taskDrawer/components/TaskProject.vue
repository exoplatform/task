<template>
  <div>
    <v-container
      pa-0
      fluid>
      <v-combobox
        v-custom-click-outside="closeDropDownList"
        ref="select"
        :filter="filterProjects"
        v-model="projectModel"
        :items="projects"
        :label="$t('label.tapProject.name')"
        attach
        class="pt-0 mb-0 inputTaskProjectName taskInputArea"
        solo
        prepend-icon
        @click="openProjectsList()"
        @change="deleteProject()">
        <template v-slot:prepend>
          <i class="uiIconFolder uiIconBlue mr-1"></i>
        </template>
        <template v-slot:selection="{ attrs, item, parent, selected }">
          <v-chip
            v-if="item === Object(item)"
            v-bind="attrs"
            :color="`${item.color} lighten-3`"
            :input-value="selected"
            :title="$t('tooltip.clickToEdit')"
            label
            class="projectName"
            small>
            <span 
              class="px-4 body-2" 
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
              label
              small>
              {{ item.name }}
            </v-chip>
          </v-list-item>
        </template>
      </v-combobox>
    </v-container>
  </div>
</template>

<script>
    import {getProjects, updateTask, getDefaultStatusByProjectId} from '../taskDrawerApi'

    export default {
        props: {
            task: {
                type: Object,
                default: () => {
                    return {};
                }
            },
            projectsList: {
              type: Boolean,
              default: false
            },
        },
        data() {
            return {
                projects: [],
                projectModel:null,
                search: null,
            }
        },
        watch: {
          projectModel () {
            setTimeout(() => {
              this.$refs.select.isMenuActive = false;
            }, 50)
          },
          projectsList() {
            this.closeDropDownList()
          },
          task(){
            if (this.task.status != null) {
              this.projectModel = this.task.status.project;
            }else{
                    getDefaultStatusByProjectId(this.task.status.project.id).then((status) => {
                    this.task.status = status;
                })
            }
          }
        },
        created() {
            this.getProjects();
            
        },
        methods: {
            getProjects() {
                getProjects().then((projects) => {
                    this.projects = projects.projects;
                })
            },
            filterProjects(item, queryText) {
              return (
                      item.name.toLocaleLowerCase().indexOf(queryText.toLocaleLowerCase()) >
                      -1 ||
                      item.name.toLocaleLowerCase().indexOf(queryText.toLocaleLowerCase()) > -1
              );
            },
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
            deleteProject() {
                this.task.status = null;
                this.projectModel = null
                this.updateTask();
            },
            closeDropDownList() {
              if (typeof this.$refs.select !== 'undefined') {
                this.$refs.select.isMenuActive = false;
              }
            },
            openProjectsList() {
              this.$emit('openProjectsList')
            }
        }
    }
</script>