import TaskSearchCard from './components/TaskSearchCard.vue';

const components = {
  'task-search-card': TaskSearchCard,
};

for (const key in components) {
  Vue.component(key, components[key]);
}