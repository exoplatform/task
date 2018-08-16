define('taskDetailView', ['SHARED/jquery', 'taskManagementApp', 'taskCenterView'], function($, taApp, taskCenterView) {
    var detailView = {};
    detailView.init = function() {
        taApp.onReady(function($) {
            detailView.initDomEvent();
            detailView.enhanceCommentsLinks();
        });
    };

    detailView.initDomEvent = function() {
        var ui = taApp.getUI();
        var $rightPanel = ui.$rightPanel;

        $rightPanel.on('click', 'a.action-delete-task', function(e){
            var $a = $(e.target).closest('a');
            var taskId = $a.closest('[data-taskid]').data('taskid');
            taApp.showDialog('TaskController.openConfirmDeleteTask()', {id : taskId});
        });

        $rightPanel.on('click', 'a.action-clone-task', function(e){
            var $a = $(e.target).closest('a');
            var taskId = $a.closest('[data-taskid]').data('taskid');
            taApp.showDialog('TaskController.openConfirmCloneTask()', {id : taskId});
        });
    };

    detailView.initDeleteTaskDialog = function() {
        $('.confirmDeleteTask')
            .off('click', '.confirmDelete')
            .on('click', '.confirmDelete', function(e) {
                var $target = $(e.target);
                var taskid = $target.closest('[data-taskid]').data('taskid');
                $target.jzAjax('TaskController.delete()', {
                    data: {id: taskid},
                    success: function(response) {
                        //
                        taskCenterView.submitFilter();
                        taApp.updateTaskNum(response.incomNum);
                     },
                    error: function(xhr) {
                        if (xhr.status >= 400) {
                            taApp.showWarningDialog(xhr.responseText);
                        }
                    }
                });
            });
    };

    detailView.initCloneTaskDialog = function() {
        var ui = taApp.getUI();
        var $leftPanel = ui.$leftPanel;
        var $centerPanel = ui.$centerPanel;

        $('.confirmCloneTask')
            .off('click', '.confirmClone')
            .on('click', '.confirmClone', function(e) {
                var $target = $(e.target);
                var taskid = $target.closest('[data-taskid]').data('taskid');
                $target.jzAjax('TaskController.clone()', {
                    data: {id: taskid},
                    success: function(response) {
                        var id = response.id;
                        var projectId = $leftPanel.find('.active .project-name').data('id');
                        taApp.reloadTaskList(projectId, -1, function() {
                            $centerPanel.find('.taskItem[data-taskid="' + id + '"]').click();
                        });
                    },
                    error: function(xhr) {
                        taApp.showWarningDialog(xhr.responseText);
                    }
                });
            });
    };

    /**
     * Converts all URLs in comments as HTTP links
     */
    detailView.enhanceCommentsLinks = function() {
        var ui = taApp.getUI();
        var $rightPanel = ui.$rightPanel;

        $rightPanel.find('.contentComment').each(function(index, ele) {
            var commentContainer = $(ele);
            commentContainer.html(taApp.convertURLsAsLinks(commentContainer.html()));
        });
    };

    return detailView;
})
