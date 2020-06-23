import './initComponents.js';
import '../taskDrawer/initComponents.js';

Vue.use(Vuetify);

export function formatSearchResult(results, term) {
  if (results && results.length) {
    results = results.map(task => {
      const commentCount = task.commentCount;
      task = task.task;
      task.commentCount = commentCount;
      task.title = task.title.replace(new RegExp(`(${term})`, 'ig'), `<span class="searchMatchExcerpt">$1</span>`);
      task.excerpt = $('<div />').html(task.description).text().replace(new RegExp(`(${term})`, 'ig'), `<span class="searchMatchExcerpt">$1</span>`);
      return task;
    });
  }
  return results;
}
