define('taskManagementApp', ['SHARED/jquery', 'SHARED/taskLocale', 'SHARED/juzu-ajax'],
    function($, locale) {

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

        taApp.initTooltip = function($rootElement) {
            $rootElement.find('[rel="tooltip"]').tooltip();
        }

        taApp.showDialog = function(controllerURL, data) {
            var $modalPlace = $('.modalPlace');
            $modalPlace.jzLoad(controllerURL, data, function() {
                var $dialog = $modalPlace.children().first();
                $dialog.modal({'backdrop': false});
            });
        }
        
        taApp.escape = function(text) {
          var encoder = $('<div></div>');
          return encoder.text(text).html();
        }
                
        taApp.showWarningDialog = function(msg) {
          var $modalPlace = $('.modalPlaceForMessage');
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
            var data = {projectId: projectId, labelId: labelId, dueCategory: filter};
            $centerPanelContent.jzLoad('TaskController.listTasks()', data, function(html, status, xhr) {
              if (xhr.status >= 400) {
                taApp.showWarningDialog(xhr.responseText);
              } else {
                if (projectId == -1) {
                  var $items = $centerPanelContent.find('[name="incomNum"]');
                  var incomNum = parseInt($items.val());
                  if (incomNum != -1) {
                    taApp.updateTaskNum(incomNum);                    
                  }
                }
                
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

        taApp.setTaskComplete = function(taskId, isCompleted, isShownCompletedTask) {
            var ui = taApp.getUI();
            var $taskItem = ui.$centerPanel.find('.taskItem[data-taskid="' + taskId + '"]');
            var isRightPanelOpened = false;
            if (ui.$rightPanel.is(':visible')) {
                isRightPanelOpened = taskId == ui.$rightPanel.find('[data-taskid]').data('taskid');
            }
            var $next = $taskItem.next('.taskItem').first();
            if ($next.length == 0) {
                $next.prev('.taskItem').first();
            }

            if (isCompleted) {
                $taskItem.addClass('task-completed');
            } else {
                $taskItem.removeClass('task-completed');
            }

            var data = {taskId: taskId, completed: isCompleted};
            //
            $taskItem.jzAjax('TaskController.updateCompleted()', {
                data: data,
                type: 'POST',
                success: function(message) {
                    if (isCompleted) {
                        $taskItem.addClass('task-completed');
                        if (!isShownCompletedTask) {
                            setTimeout(function() {
                                $taskItem.fadeOut(1000, function() {
                                    $taskItem.remove();
                                    if (isRightPanelOpened) {
                                        if ($next.length > 0) {
                                            $next.click();
                                        } else {
                                            taApp.hideRightPanel(ui.$centerPanel, ui.$rightPanel, ui.$rightPanelContent);
                                        }
                                    }
                                });
                            }, 1000);
                        }
                    } else {
                        $taskItem.removeClass('task-completed');
                    }

                    // Update tooltip and icon
                    var $iconTemplate = $taskItem.find('.uiIconValidate');
                    if (isCompleted) {
                        $iconTemplate.removeClass('uiIconLightGray').addClass('uiIconBlue');
                    } else {
                        $iconTemplate.removeClass('uiIconBlue').addClass('uiIconLightGray');
                    }
                    $taskItem.find('[rel="tooltip"]')
                        .attr('data-taskcompleted', isCompleted ? 'true' : 'false')
                        .data('taskcompleted', isCompleted)
                        .attr('data-original-title', isCompleted ? locale.markAsUnCompleted : locale.markAsCompleted)
                        .tooltip('fixTitle');

                    //
                    if (isRightPanelOpened) {
                        if (isCompleted) {
                            ui.$rightPanel.find('[data-taskid]').addClass('task-completed');
                        } else {
                            ui.$rightPanel.find('[data-taskid]').removeClass('task-completed');
                        }
                        var $iconTemplateAtRight = ui.$rightPanel.find('.uiIconValidate');
                        if (isCompleted) {
                            $iconTemplateAtRight.removeClass('uiIconLightGray').addClass('uiIconBlue');
                        } else {
                            $iconTemplateAtRight.removeClass('uiIconBlue').addClass('uiIconLightGray');
                        }

                        ui.$rightPanel.find('[data-taskcompleted][rel="tooltip"]')
                            .attr('data-taskcompleted', isCompleted ? 'true' : 'false')
                            .data('taskcompleted', isCompleted)
                            .attr('data-original-title', isCompleted ? locale.markAsUnCompleted : locale.markAsCompleted)
                            .tooltip('fixTitle');
                    }

                    taApp.updateTaskNum(message);
                },
                error : function(xhr, textStatus, errorThrown) {
                  taApp.showWarningDialog(xhr.responseText);
                  // Revert strike-through added before
                  if (isCompleted) {
                    $taskItem.addClass('task-completed');
                  } else {
                    $taskItem.removeClass('task-completed');
                  }
                }
            });
        }
        
        taApp.updateTaskNum = function(increase) {
          var $taskNum = $('.project-name[data-id="-1"] .badgeDefault');
          if ($.isNumeric(increase)) {
            $taskNum.text(increase);
          } else if (typeof increase == 'boolean') {
            var num = parseInt($taskNum.text());
            $taskNum.text(num + (increase ? 1 : -1));            
          }
        }

        /**
         * This function parses the given text and convert URLs to HTTP links
         * @param text
         * @returns {string}
         */
        taApp.convertURLsAsLinks = function(text) {
            return text.replace(/(?<!(href|src)=\")((((https?|ftp|file):\/\/)|www\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])/ig,
              function(url){
                  var value = url;
                  if(url.indexOf('www.') == 0) {
                      url = 'http://' + url;
                  }
                  return '<a href="' + url + '" target="_blank">' + value + '</a>';
              });
        };

        return taApp;
});
