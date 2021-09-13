import TasksManagement from './components/TasksManagement.vue';
import ProjectCardList from './components/Project/ProjectCardList.vue';
import ProjectDashboard from './components/Project/ProjectDashboard.vue';
import ProjectListToolbar from './components/Project/ProjectListToolbar.vue';
import ProjectCard from './components/Project/ProjectCard.vue';
import ProjectCardFront from './components/Project/ProjectCardFront.vue';
import ProjectCardReverse from './components/Project/ProjectCardReverse.vue';
import AddProjectDrawer from './components/Project/AddProjectDrawer.vue';
import ExoTaskEditor from './components/Project/ExoTaskEditor.vue';
import ProjectManagersDrawer from './components/Project/ProjectManagersDrawer.vue';
import ProjectAssigneeManager from './components/Project/ProjectAssigneeManager.vue';
import ProjectEventFormAssigneeAndParticipatorItem from './components/Project/ProjectEventFormAssigneeAndParticipatorItem.vue';
import ProjectAssigneeParticipator from './components/Project/ProjectAssigneeParticipator.vue';
import ProjectLabels from './components/Project/ProjectLabels.vue';
import EditableLabels from './components/Project/EditableLabels.vue';

import TasksDashboard from './components/tasks/TasksDashboard.vue';
import TasksListToolbar from './components/tasks/TasksListToolbar.vue';
import TasksList from './components/tasks/TasksList.vue';
import TasksListItem from './components/tasks/TasksListItem.vue';
import TasksCardsList from './components/tasks/TasksCardsList.vue';
import TaskCard from './components/tasks/TaskCard.vue';
import TasksFilterDrawer from './components/tasks/TasksFilterDrawer.vue';
import TasksAssigneeAndCoworkerDrawer from './components/tasks/TasksAssigneeAndCoworkerDrawer.vue';
import TasksGroupDrawer from './components/tasks/TasksGroupDrawer.vue';
import TasksSortByDrawer from './components/tasks/TasksSortByDrawer.vue';
import TasksLabelsDrawer from './components/tasks/TasksLabelsDrawer.vue';
import TasksGroupProjectDrawer from './components/tasks/TasksGroupProjectDrawer.vue';
import TasksSortByProjectDrawer from './components/tasks/TasksSortByProjectDrawer.vue';

import TasksViewDashboard from './components/ProjectTasks/TasksViewDashboard.vue';
import TasksViewToolbar from './components/ProjectTasks/TasksViewToolbar.vue';
import TasksViewBoard from './components/ProjectTasks/TasksViewBoard.vue';
import TasksViewList from './components/ProjectTasks/TasksViewList.vue';
import TasksViewListColumn from './components/ProjectTasks/TasksViewListColumn.vue';
import TaskViewGantt from './components/ProjectTasks/TasksViewGantt.vue';
import TasksViewHeader from './components/ProjectTasks/TasksViewHeader.vue';
import TaskViewCard from './components/ProjectTasks/TaskViewCard.vue';
import TaskViewListItem from './components/ProjectTasks/TaskViewListItem.vue';
import TasksViewBoardColumn from './components/ProjectTasks/TasksViewBoardColumn.vue';
import QuickAddCard from './components/ProjectTasks/QuickAddCard.vue';
import TasksViewHeaderStatus from './components/ProjectTasks/TasksViewHeaderStatus.vue';
import TasksUnscheduledDrawer from './components/ProjectTasks/TasksUnscheduledDrawer.vue';

const components = {
  'tasks-management': TasksManagement,
  'project-dashboard': ProjectDashboard,
  'project-list-toolbar': ProjectListToolbar,
  'project-card-list': ProjectCardList,
  'project-card': ProjectCard,
  'project-card-front': ProjectCardFront,
  'project-card-Reverse': ProjectCardReverse,
  'add-project-drawer': AddProjectDrawer,
  'exo-task-editor': ExoTaskEditor,
  'project-manager-drawer': ProjectManagersDrawer,
  'project-assignee-manager': ProjectAssigneeManager,
  'project-event-form-assignee-and-participator-item': ProjectEventFormAssigneeAndParticipatorItem,
  'project-assignee-participator': ProjectAssigneeParticipator,
  'project-labels': ProjectLabels,
  'editable-labels': EditableLabels,

  'tasks-dashboard': TasksDashboard,
  'tasks-list-toolbar': TasksListToolbar,
  'tasks-list': TasksList,
  'tasks-list-item': TasksListItem,
  'tasks-cards-list': TasksCardsList,
  'task-card': TaskCard,
  'tasks-filter-drawer': TasksFilterDrawer,
  'tasks-assignee-coworker-drawer': TasksAssigneeAndCoworkerDrawer,
  'tasks-group-drawer': TasksGroupDrawer,
  'tasks-sort-by-drawer': TasksSortByDrawer,
  'tasks-labels-drawer': TasksLabelsDrawer,
  'tasks-group-project-drawer': TasksGroupProjectDrawer,
  'tasks-sort-by-project-drawer': TasksSortByProjectDrawer,

  'tasks-view-dashboard': TasksViewDashboard,
  'tasks-view-toolbar': TasksViewToolbar,
  'tasks-view-board': TasksViewBoard,
  'tasks-view-board-column': TasksViewBoardColumn,
  'tasks-view-list': TasksViewList,
  'tasks-view-list-column': TasksViewListColumn,
  'tasks-view-gantt': TaskViewGantt,
  'tasks-view-header': TasksViewHeader,
  'task-view-card': TaskViewCard,
  'task-view-list-item': TaskViewListItem,
  'quick-add-card': QuickAddCard,
  'tasks-view-header-status': TasksViewHeaderStatus,
  'tasks-unscheduled-drawer': TasksUnscheduledDrawer,
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