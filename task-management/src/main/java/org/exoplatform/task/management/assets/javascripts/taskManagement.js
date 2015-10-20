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
            return taskUI;
        }

        taApp.showDialog = function(controllerURL, data) {
            var $modalPlace = $('.modalPlace');
            $modalPlace.jzLoad(controllerURL, data, function() {
                var $dialog = $modalPlace.children().first();
                $dialog.modal({'backdrop': false});
            });
        }
                
        taApp.showWarningDialog = function(msg) {
          var $modalPlace = $('.modalPlace');
          if (msg) {
            $modalPlace.jzLoad('ProjectController.openWarningDialog()', {'msg': msg}, function() {
              taApp.showWarningDialog();
            });
          } else {
            $modalPlace.find('.uiPopup').modal({'backdrop': false});
          }
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
        
        var centerPanelWidth = function(expand) {
        	var ui = taApp.getUI();
        	var $center = ui.$centerPanel;
        	$center.removeClass('span5 span9 span8 span12');
        	if (expand) {
        		if (!ui.$leftPanel.is(':visible') && !ui.$rightPanel.is(':visible')) {
        			$center.addClass('span12');
        		} else if (!ui.$leftPanel.is(':visible')) {
        			$center.addClass('span8');
        		} else if (!ui.$rightPanel.is(':visible')) {
        			$center.addClass('span9');
        		}
        	} else {
        		if (ui.$leftPanel.is(':visible') && ui.$rightPanel.is(':visible')) {
        			$center.addClass('span5');
        		} else if (ui.$leftPanel.is(':visible')) {
        			$center.addClass('span9');
        		} else if (ui.$rightPanel.is(':visible')) {
        			$center.addClass('span8');
        		}
        	}
        }
        
        taApp.showLeftPanel = function() {
        	var ui = this.getUI();
        	var $center = ui.$centerPanel;
        	ui.$leftPanel.show();
        	$center.find('.show-hide-left .uiIconMiniArrowRight')
        		.toggleClass('uiIconMiniArrowLeft uiIconMiniArrowRight');
        	$center.css('margin-left', $center.data('margin-left'));
        	centerPanelWidth();
        }
        
        taApp.hideLeftPanel = function() {
        	var ui = this.getUI();
        	var $center = ui.$centerPanel;
        	ui.$leftPanel.hide();
        	$center.find('.show-hide-left .uiIconMiniArrowLeft').toggleClass('uiIconMiniArrowLeft uiIconMiniArrowRight');
        	$center.data('margin-left', $center[0].style.marginLeft);
        	$center.css('margin-left', '0px');
        	centerPanelWidth(true);
        };

        taApp.showRightPanel = function($centerPanel, $rightPanel) {
            $rightPanel.show();
            $rightPanel.find('[data-toggle="tooltip"]').tooltip();
            $rightPanel.find('*[rel="tooltip"]').tooltip({
                placement: 'top'
            });
            centerPanelWidth();
        };

        taApp.hideRightPanel = function($centerPanel, $rightPanel, $rightPanelContent) {
            $rightPanelContent.html('');
            $rightPanel.hide();
            centerPanelWidth(true);
        };

        taApp.reloadTaskList = function(projectId, labelId, filter, callback) {
            var $centerPanelContent = taApp.getUI().$centerPanelContent;
            if ($.isFunction(filter)) {
              callback = filter;
              filter = '';
            }
            if (typeof filter !== 'string') {
                filter = '';
            }
            var data = {projectId: projectId, labelId: labelId, filter: filter};
            $centerPanelContent.jzLoad('TaskController.listTasks()', data, function(html, status, xhr) {
              if (xhr.status >= 400) {
                taApp.showWarningDialog(xhr.responseText);
              } else {
                if (callback) {
                  callback();
                }                
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
                error : function(xhr, textStatus, errorThrown) {
                  taApp.showWarningDialog(xhr.responseText);
                }
            });
        }

        return taApp;
});
