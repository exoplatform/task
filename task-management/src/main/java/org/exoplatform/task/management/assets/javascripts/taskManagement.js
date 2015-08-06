define('taskManagementApp', ['jquery', 'SHARED/juzu-ajax'],
    function($) {

        var taApp = {};

        var isDomReadyExcuted = false;
        $(document).ready(function($) {
            isDomReadyExcuted = true;
        });

        taApp.onReady = function(callback) {
            if (isDomReadyExcuted) {
                callback();
            } else {
                $(document).ready(callback);
            }
        };

        var taskUI = false;
        taApp.getUI = function() {
            if (!taskUI) {
                var $taskManagement = $('#taskManagement');
                var $leftPanel = $('#taskManagement > .leftPanel');
                var $centerPanel = $('#taskManagement > .centerPanel');
                var $rightPanel = $('#taskManagement > .rightPanel');
                var $rightPanelContent = $rightPanel.find('.rightPanelContent');
                var $centerPanelContent = $centerPanel.find('.centerPanelContent');

                taskUI = {
                    '$taskManagement' : $taskManagement,
                    '$leftPanel' : $leftPanel,
                    '$centerPanel' : $centerPanel,
                    '$rightPanel' : $rightPanel,
                    '$rightPanelContent' : $rightPanelContent,
                    '$centerPanelContent' : $centerPanelContent
                };
            }
            return taskUI;
        }

        taApp.showDialog = function(controllerURL, data) {
            var $modalPlace = $('.modalPlace');
            $modalPlace.jzLoad(controllerURL, data, function() {
                var $dialog = $modalPlace.children().first();
                $dialog.modal({'backdrop': false});
            });
        }

        taApp.showOneTimePopover = function($popover) {
            if ($popover.data('content')) {
                $popover.popover('show');
                $(document).one('click', function() {
                    $popover.popover('hide');
                    $popover.popover('destroy');
                });
            }
        }

        taApp.showRightPanel = function($centerPanel, $rightPanel) {
            $centerPanel.removeClass('span9').addClass('span5');
            $rightPanel.show();
            $rightPanel.find('[data-toggle="tooltip"]').tooltip();
            $rightPanel.find('*[rel="tooltip"]').tooltip({
                placement: 'top'
            });
        };

        taApp.hideRightPanel = function($centerPanel, $rightPanel, $rightPanelContent) {
            $rightPanelContent.html('');
            $rightPanel.hide();
            $centerPanel.removeClass('span5').addClass('span9');
        };

        taApp.reloadTaskList = function(projectId, filter, callback) {
            var $centerPanelContent = taApp.getUI().$centerPanelContent;
            if ($.isFunction(filter)) {
                filter = '';
                callback = filter;
            }
            if (typeof filter !== 'string') {
                filter = '';
            }
            var data = {projectId: projectId, filter: filter};
            $centerPanelContent.jzLoad('TaskController.listTasks()', data, function() {
                if (callback) {
                    callback();
                }
            });
        }

        taApp.reloadProjectTree = function(id) {
            var $leftPanel = taApp.getUI().$leftPanel;
            var $listProject = $leftPanel.find('ul.list-projects.projectTree');
            $listProject.jzLoad('ProjectController.projectTree()', function() {
                if (id) {
                    $listProject.find('a.project-name[data-id="' + id+ '"]').click();
                } else {
                    if ($listProject.find('a.project-name').length > 0) {
                        $listProject.find('.project-name').first().click();
                    } else {
                        $leftPanel.find('.project-name[data-id="0"]').first().click();
                    }
                }
            });
        }

        taApp.setTaskComplete = function(taskId, isCompleted) {
            var ui = taApp.getUI();
            var $taskItem = ui.$centerPanel.find('.taskItem[data-taskid="' + taskId + '"]');
            var $next = $taskItem.next('.taskItem').first();
            if ($next.length == 0) {
                $next.prev('.taskItem').first();
            }

            var data = {taskId: taskId, completed: isCompleted};
            //
            $taskItem.jzAjax('TaskController.updateCompleted()', {
                data: data,
                success: function(message) {
                    if (isCompleted) {
                        $taskItem.fadeOut(500, function() {
                            $taskItem.remove();
                        });
                        if ($next.length == 0) {
                            ui.$leftPanel.find('.active .project-name').click();
                        } else {
                            $next.click();
                        }
                    }
                },
                error : function(jqXHR, textStatus, errorThrown) {
                    console.error && console.error('update failure: ' + jqXHR.responseText);
                }
            });
        }

        return taApp;
});
