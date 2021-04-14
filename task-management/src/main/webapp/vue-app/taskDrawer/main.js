import './initComponents.js';
import * as tasksService from '../../js/tasksService.js';
import * as projectService from '../../js/projectService.js';
import * as statusService from '../../js/statusService.js';
import * as taskDrawerApi from '../../js/taskDrawerApi.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('TaskDrawer');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vue.use(Vuetify);

window.Object.defineProperty(Vue.prototype, '$tasksService', {
  value: tasksService,
});

window.Object.defineProperty(Vue.prototype, '$projectService', {
  value: projectService,
});

window.Object.defineProperty(Vue.prototype, '$statusService', {
  value: statusService,
});

window.Object.defineProperty(Vue.prototype, '$taskDrawerApi', {
  value: taskDrawerApi,
});
