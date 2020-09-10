import TasksManagement from './components/TasksManagement.vue';
import ProjectCardList from './components/Project/ProjectCardList.vue';
import ProjectDashboard from './components/Project/ProjectDashboard.vue';
import ProjectListToolbar from './components/Project/ProjectListToolbar.vue';
import ProjectCard from './components/Project/ProjectCard.vue';
import ProjectCardFront from './components/Project/ProjectCardFront.vue';
import ProjectCardReverse from './components/Project/ProjectCardReverse.vue';

import TasksDashboard from './components/tasks/TasksDashboard.vue';
import TasksListToolbar from './components/tasks/TasksListToolbar.vue';
import TasksList from './components/tasks/TasksList.vue';
import TasksListItem from './components/tasks/TasksListItem.vue';
import TasksCardsList from './components/tasks/TasksCardsList.vue'
import TaskCard from './components/tasks/TaskCard.vue'

const components = {
  'tasks-management': TasksManagement,
  'project-dashboard': ProjectDashboard,
  'project-list-toolbar': ProjectListToolbar,
  'project-card-list': ProjectCardList,
  'project-card': ProjectCard,
  'project-card-front': ProjectCardFront,
  'project-card-Reverse': ProjectCardReverse,

  'tasks-dashboard': TasksDashboard,
  'tasks-list-toolbar': TasksListToolbar,
  'tasks-list': TasksList,
  'tasks-list-item': TasksListItem,
  'tasks-cards-list': TasksCardsList,
  'task-card': TaskCard

};

for (const key in components) {
  Vue.component(key, components[key]);
}

//get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('TasksManagement');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
