<template>
  <v-app id="TasksManagementPortlet">
    <v-tabs 
      v-model="tab" 
      slider-size="4" 
      class="tasksMenuParent white">
      <v-tab href="#tab-1" @click="getMyTasks()">
        {{ $t('label.tasks.header') }}
      </v-tab>
      <v-tab href="#tab-2" @click="getMyProjects()">
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
    <add-project-drawer
      ref="addProjectDrawer"/>
      
    <task-drawer
      ref="taskDrawer"
      :task="task"/>
  </v-app>
</template>
<script>
  export default {
    data () {
      return {
        tab: 'tab-2',
        task: {
        type: Object,
        default: () => ({}),
      }
      }
    },
 
  created(){
     this.$root.$on('open-project-drawer', project => {
       this.$refs.addProjectDrawer.open(project);
      });
     this.$root.$on('open-task-drawer', task => {
       this.task=task;
       if(task.id){
       window.history.pushState('task', 'Task details', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest?taskId=${task.id}`);
       }
       this.$refs.taskDrawer.open(task);
      });
     this.$root.$on('task-drawer-closed', task => {
       if(this.tab==='tab-1'){
         this.getMyTasks()
       }

       else if(task && task.status && task.status.project) {
         // document.dispatchEvent(new CustomEvent('showProjectTasks', {detail: this.task.status.project}));
         window.history.pushState('task', 'Task details', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest?projectId=${task.status.project.id}`);
          }else{
           this.tab='tab-1' 
          }
      });
     let search = document.location.search.substring(1);
     if(search.includes('mytasks')){
        search = this.tab='tab-1'
        search.replace('mytasks','')
     }
     if(search.includes('myprojects')){
        this.tab='tab-2'
        search = search.replace('myprojects','')
     }
    if(search) {
      const parameters = JSON.parse(
        `{"${decodeURI(search)
          .replace(/"/g, '\\"')
          .replace(/&/g, '","')
          .replace(/=/g, '":"')}"}`
      );
      const taskId = parameters.taskId && Number(parameters.taskId) || 0;
      const projectId = parameters.projectId && Number(parameters.projectId) || 0;
      if (projectId) {
          this.$projectService.getProject(projectId).then(data => {
          document.dispatchEvent(new CustomEvent('showProjectTasks', {detail: data}));
        })
      } 
      if (taskId) {
          this.$tasksService.getTaskById(taskId).then(data => {
          this.task = data  
          if(this.task.status && this.task.status.project){
              document.dispatchEvent(new CustomEvent('showProjectTasks', {detail: this.task.status.project}));
          }else{
           this.tab='tab-1' 
          }
          this.$refs.taskDrawer.open(this.task);
         // window.history.pushState('task', 'Task details', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest?taskId=${taskId}`);
        })
      } 
    }
  },
  methods: {
      getMyTasks(){
      window.history.pushState('mytasks', 'My Tasks', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest?mytasks`);
      },
      getMyProjects(){
        window.history.pushState('myprojects', 'My Projects', `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest?myprojects`);
      }
}
   }
</script>