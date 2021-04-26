import './initComponents.js';
import '../taskDrawer/initComponents.js';
import { tasksConstants } from '../../js/tasksConstants.js';
import * as tasksService from '../../js/tasksService.js';
import * as projectService from '../../js/projectService.js';
import * as statusService from '../../js/statusService.js';
import * as taskDrawerApi from '../../js/taskDrawerApi.js';

Vue.use(Vuetify);
Vue.use(VueEllipsis);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

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


//getting language of user
const lang = eXo && tasksConstants.LANG || 'en';

//should expose the locale ressources as REST API
const url = [`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/i18n/bundle/locale.portlet.taskManagement-${lang}.json`,`${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/i18n/bundle/locale.attachmentsSelector.attachments-${lang}.json`];

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    new Vue({
      template: '<tasks-management></tasks-management>',
      i18n,
      vuetify,
    }).$mount('#tasksManagement');
  });
}