define('taskListView', ['SHARED/jquery', 'taskManagementApp'], function($, taApp) {
    var listView = {};
    listView.init = function() {
        taApp.onReady(function($) {
            //
        });
    };

    listView.renderTask = function(task) {
        var template = $('[data-template="list-task-item"]').html();
        var result = template;
        result = result.replace('{{taskid}}', task.id);
        result = result.replace('{{title}}', taApp.escape(task.title));

        var color = '';
        if (task.status) {
            color = task.status.project.color;
        }
        result = result.replace('{{projectColor}}', color);

        result = result.replace('{{dueDateColorClass}}', task.dueDateCssClass);
        result = result.replace('{{taskDueDate}}', task.dueDateString);
        result = result.replace('{{taskCompleted}}', task.completed);

        return result;
    };

    return listView;
});