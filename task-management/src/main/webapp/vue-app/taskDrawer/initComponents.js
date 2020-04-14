import TaskDrawer from '../taskDrawer/components/TaskDrawer.vue';
import TaskLabels from '../taskDrawer/components/TaskLabels.vue';
import TaskProjects from '../taskDrawer/components/TaskProject.vue';
import TaskAssignment from '../taskDrawer/components/TaskAssignment.vue';
import TaskCommentEditor from '../taskDrawer/components/TaskCommentEditor.vue';
import TaskDescriptionEditor from '../taskDrawer/components/TaskDescriptionEditor.vue';
import LogDetails from '../taskDrawer/components/LogDetails.vue'
import TaskComments from '../taskDrawer/components/TaskComments.vue'

const components = {
    'task-drawer': TaskDrawer,
    'task-labels': TaskLabels,
    'task-projects': TaskProjects,
    'task-assignment': TaskAssignment,
    'task-comment-editor': TaskCommentEditor,
    'task-description-editor': TaskDescriptionEditor,
    'log-details': LogDetails,
    'task-comments': TaskComments,
};

for (const key in components) {
    Vue.component(key, components[key]);
}