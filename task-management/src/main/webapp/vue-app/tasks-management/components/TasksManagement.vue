<template>
  <v-app id="TasksManagementPortlet">
    <v-alert 
      v-model="alert" 
      :type="type"
      dismissible>
      {{ message }}
    </v-alert>

    <v-tabs 
      v-if="showTabs"
      v-model="tab"
      slider-size="4"
      class="tasksMenuParent white">
      <v-tab href="#tab-2" @click="getMyProjects()">
        {{ $t('label.projects') }}
      </v-tab>
      <v-tab href="#tab-1" @click="getMyTasks()">
        {{ $t('label.tasks.header') }}
      </v-tab>
    </v-tabs>
    <v-tabs-items v-model="tab">
      <v-tab-item value="tab-1">
        <tasks-dashboard />
      </v-tab-item>
      <v-tab-item value="tab-2">
        <project-dashboard :space-name="spaceName" />
      </v-tab-item>
    </v-tabs-items>
    <add-project-drawer
      ref="addProjectDrawer" />
      
    <task-drawer
      ref="taskDrawer"
      :task="task" />

    <tasks-assignee-coworker-drawer />
  </v-app>
</template>
<script>
export default {
  data () {
    return {
      tab: '',
      showTabs: false,
      spaceName: '',
      projectId: '',
      alert: false,
      type: '',
      message: '',
      task: {
        type: Object,
        default: () => ({}),
      }
    };
  },
  created(){
    this.$root.$on('show-alert', message => {
      this.displayMessage(message);
    });
    this.$root.$on('open-project-drawer', project => {
      this.$refs.addProjectDrawer.open(project);
    });
    this.$root.$on('show-project-details-tasks',project =>{
      document.dispatchEvent(new CustomEvent('showProjectTasks', {detail: project}));
      this.tab='tab-2';
    });
    this.$root.$on('set-url',context =>{
      
      if (context.type==='task'){
        this.setTaskUrl(context.id);
      }
      if (context.type==='project'){
        this.setProjectUrl(context.id);
      }
      if (context.type==='myProjects'){
        this.getMyProjects(); 
      }
    });
    this.$root.$on('open-task-drawer', task => {
      this.task=task;
      if (task.id){
        this.setTaskUrl(task.id);
      }
      this.$refs.taskDrawer.open(task);
    });
    this.$root.$on('task-drawer-closed', task => {
      if (this.tab==='tab-1'){
        this.getMyTasks();
      }

      else if (task && task.status && task.status.project) {
        this.setProjectUrl(task.status.project.id);
      } else {
        this.tab='tab-1';
      }
    });
    const urlPath = document.location.pathname;
    if (urlPath.includes('g/:spaces')){
      this.spaceName = urlPath.split('g/:spaces:')[1].split('/')[0];
      this.tab='tab-2';
      this.showTabs=false;
    } else {
      if (urlPath.includes('myTasks')){
        this.tab='tab-1';
        this.projectId='';
        this.showTabs=true;
      }
      else {
        this.tab='tab-2';
        this.showTabs=true;
      }
    }
    if (urlPath.includes('taskDetail')){
      let taskId = urlPath.split('taskDetail/')[1].split(/[^0-9]/)[0];
      taskId = taskId && Number(taskId) || 0;
      if (taskId) {
        this.$tasksService.getTaskById(taskId).then(data => {
          this.task = data;
          if (this.task.status && this.task.status.project){
            this.tab='tab-2';
            this.projectId=this.task.status.project.id;
            this.showTabs=false;
            window.setTimeout(() => {
              document.dispatchEvent(new CustomEvent('showProjectTasks', {detail: this.task.status.project}));
            }, 100);
          } else {
            this.tab='tab-1';
            this.projectId='';
            this.showTabs=true;
          }
          window.setTimeout(() => {
            this.$refs.taskDrawer.open(this.task);
          }, 200);
        });
      } 
    }
    if (urlPath.includes('projectDetail')){
      let projectId = urlPath.split('projectDetail/')[1].split(/[^0-9]/)[0];
      projectId = projectId && Number(projectId) || 0;
      if (projectId) {
        this.projectId=projectId;
        this.showTabs=false;
        this.tab='tab-2';
        this.$projectService.getProject(projectId).then(data => {
          document.dispatchEvent(new CustomEvent('showProjectTasks', {detail: data}));
        });
      }
    }
  },
  methods: {
    getMyTasks(){
      this.showTabs=true;
      window.history.pushState('mytasks', 'My Tasks', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/tasks/myTasks`);
    },
    getMyProjects(){
      this.projectId='';
      this.showTabs=true;
      const urlPath = document.location.pathname;
      if (urlPath.includes('g/:spaces')){
        window.history.pushState('task', 'Task details', `${urlPath.split('tasks')[0]}tasks`); 
      } else {
        window.history.pushState('myprojects', 'My Projects', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/tasks/myProjects`);
      }       
    },
    setTaskUrl(id){
      // this.projectId='';
      const urlPath = document.location.pathname;
      window.history.pushState('task', 'Task details', `${urlPath.split('tasks')[0]}tasks/taskDetail/${id}`);
    },
    setProjectUrl(id){
      const urlPath = document.location.pathname;
      this.projectId=id;
      this.showTabs=false;
      window.history.pushState('task', 'Task details', `${urlPath.split('tasks')[0]}tasks/projectDetail/${id}`); 
    },
    displayMessage(message) {
      this.message=message.message;
      this.type=message.type;
      this.alert = true;
      window.setTimeout(() => this.alert = false, 5000);
    }
  }
};
</script>