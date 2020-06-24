import TaskSearchCard from './components/TaskSearchCard.vue';
import TaskSearchDrawer from './components/TaskSearchDrawer.vue';

const components = {
  'task-search-card': TaskSearchCard,
  'task-search-drawer': TaskSearchDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}