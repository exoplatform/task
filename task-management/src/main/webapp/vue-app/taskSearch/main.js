import './initComponents.js';
import '../taskDrawer/initComponents.js';

Vue.use(Vuetify);

export function formatSearchResult(results, term) {
  if (results && results.tasks && results.tasks.length) {
    results = results.tasks.map(task => {
      const commentCount = task.commentCount;
      task = task.task;
      task.commentCount = commentCount;
      task.titleExcerpt = task.title.replace(new RegExp(`(${term})`, 'ig'), '<span class="searchMatchExcerpt">$1</span>');
      task.descriptionExcerpt = $('<div />').html(task.description).text().replace(new RegExp(`(${term})`, 'ig'), '<span class="searchMatchExcerpt">$1</span>');
      return task;
    });
  }
  return results;
}

$('.VuetifyApp .v-application').first().append('<div id="TaskSearchDrawers" />');

const appId = 'TaskSearchDrawers';

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

const urls = [`${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.taskManagement-${lang}.json`];
exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
  new Vue({
    template: '<task-search-drawer />',
    vuetify,
    i18n
  }).$mount(`#${appId}`);
});
