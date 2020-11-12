import TaskDrawer from '../taskDrawer/components/TaskDrawer.vue';
import TaskLabels from '../taskDrawer/components/TaskLabels.vue';
import TaskProjects from '../taskDrawer/components/TaskProject.vue';
import TaskAssignment from '../taskDrawer/components/TaskAssignment.vue';
import TaskCommentEditor from '../taskDrawer/components/TaskCommentEditor.vue';
import TaskDescriptionEditor from '../taskDrawer/components/TaskDescriptionEditor.vue';
import LogDetails from '../taskDrawer/components/LogDetails.vue';
import TaskComments from '../taskDrawer/components/TaskComments.vue';
import TaskPriority from '../taskDrawer/components/TaskPriority.vue';
import TaskStatus from "./components/TasksStatus.vue";
import TaskDueDate from "./components/TaskDueDate.vue";
import TaskPlanDates from "./components/TaskPlanDates.vue";

const components = {
    'task-drawer': TaskDrawer,
    'task-labels': TaskLabels,
    'task-projects': TaskProjects,
    'task-assignment': TaskAssignment,
    'task-comment-editor': TaskCommentEditor,
    'task-description-editor': TaskDescriptionEditor,
    'log-details': LogDetails,
    'task-comments': TaskComments,
    'task-priority' : TaskPriority,
    'task-status' : TaskStatus,
    'task-due-date': TaskDueDate,
    'task-plan-dates': TaskPlanDates
};

for (const key in components) {
    Vue.component(key, components[key]);
}