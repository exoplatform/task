import tasksApp from './components/TasksApp.vue';
import TaskDetails from './components/TaskDetails.vue';
import * as projectService from '../../js/projectService.js';
import * as taskDrawerApi from '../../js/taskDrawerApi.js';

Vue.use(Vuetify);
Vue.component('task-details', TaskDetails);

const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

window.Object.defineProperty(Vue.prototype, '$projectService', {
  value: projectService,
});

window.Object.defineProperty(Vue.prototype, '$taskDrawerApi', {
  value: taskDrawerApi,
});

// getting language of user
const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';

const resourceBundleName = 'locale.portlet.taskManagement';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/${resourceBundleName}-${lang}.json`;

export function init(itemsLimit) {
//getting locale ressources
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      // init Vue app when locale ressources are ready
      new Vue({
        data: {
          itemsLimit: itemsLimit
        },
        render: h => h(tasksApp),
        i18n,
        vuetify,
      }).$mount('#tasks');
    });
}
