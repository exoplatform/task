import './initComponents.js';
import { tasksConstants } from '../../js/tasksConstants.js';
import * as tasksService from '../../js/tasksService.js';

Vue.use(Vuetify);
Vue.use(VueEllipsis);
const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
})

window.Object.defineProperty(Vue.prototype, '$tasksService', {
  value: tasksService,
});

//getting language of user
const lang = eXo && tasksConstants.LANG || 'en';

//should expose the locale ressources as REST API
const url = `${tasksConstants.PORTAL}/${tasksConstants.PORTAL_REST}/i18n/bundle/locale.portlet.taskManagement-${lang}.json`;

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