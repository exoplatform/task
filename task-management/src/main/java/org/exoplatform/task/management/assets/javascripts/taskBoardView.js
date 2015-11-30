define('taskBoardView', ['SHARED/jquery', 'taskManagementApp', 'SHARED/edit_inline_js', 'SHARED/task_jquery_ui'],
    function($, taApp, editinline) {

        var boardView = {};
        boardView.init = function() {
            taApp.onReady(function($) {
                boardView.initDomEventListener();
                boardView.initEditInline();
                boardView.initDragDrop();
            });
        };

        boardView.renderTask = function(task) {
            var template = $('[data-template-name="board-view-task-item"]').html();
            var result = template;

            result = result.replace('{{taskid}}', task.id);
            result = result.replace('{{title}}', taApp.escape(task.title));

            var completedClass = '';
            if (task.completed) {
                completedClass = 'task-completed';
            }
            result = result.replace('{{completedClass}}', completedClass);

            result = result.replace('{{priority}}', task.priority.toLowerCase());
            result = result.replace('{{dueDateColorClass}}', task.dueDateCssClass);
            result = result.replace('{{taskDueDate}}', task.dueDateString);

            return result;
        }

        boardView.initDomEventListener = function() {
            var ui = taApp.getUI();
            var $centerPanelContent = ui.$centerPanelContent;
            var $centerPanel = ui.$centerPanel;
            $centerPanel.off('click', '[data-statusid] .actionRemoveStatus').on('click', '[data-statusid] .actionRemoveStatus', function(e) {
                if ($centerPanel.find('[data-statusid]').length < 2) {
                    alert('There are only one status. It can not be deleted');
                    return;
                }
                var $status = $(e.target).closest('[data-statusid]');
                var status = $status.data('statusid');
                if (status > 0) {
                    $centerPanelContent.jzAjax('TaskController.removeStatus()', {
                        data: {statusId: status},
                        method: 'POST',
                        success: function(response) {
                            $centerPanelContent.html(response);
                        },
                        error: function(xhr) {
                          taApp.showWarningDialog(xhr.responseText);
                        }
                    });
                } else {
                    $centerPanel.find('.col.col-new').hide();
                }
            });

            $centerPanel.off('click', '.actionAddStatus').on('click', '.actionAddStatus', function(e) {
                $centerPanel.find('.col.col-new').show();
                $centerPanel.find('[name="statusName"]').focus();
            });
            $centerPanel.off('submit', '.formCreateNewStatus').on('submit', '.formCreateNewStatus', function(e) {
                var $form = $(e.target).closest('form');
                var statusName = $form.find('[name="statusName"]').val();
                var projectId = $form.closest('[data-projectid]').data('projectid');
                if (projectId == undefined || projectId <= 0) {
                    alert('Can not create status for undefined project');
                    return;
                }
                if (statusName == undefined || statusName == '') {
                    alert('status name is required');
                    return false;
                } else {
                    $centerPanelContent.jzAjax('TaskController.createStatus()', {
                        data: {
                            name: statusName,
                            projectId: projectId
                        },
                        method: 'POST',
                        success: function(response) {
                            $centerPanelContent.html(response);
                        }, 
                        error: function(xhr) {
                          taApp.showWarningDialog(xhr.responseText);
                        }
                    });
                }
                return false;
            });

            $centerPanel.on('click', '[data-taskid]', function(e) {
                $('[name="taskTitle"]:focus').blur();
                e.stopPropagation();
            });
            $centerPanel.off('click', '.taskBoardContainer').on('click', '.taskBoardContainer', function(e) {
                var $container = $(e.target).closest('.taskBoardContainer');
                $container.find('form.createTaskInListView').css('visibility', 'visible');
                $container.find('[name="taskTitle"]').focus()
                    .one('blur', function(e) {
                        var $this = $(e.target);
                        var $form = $(e.target).closest('form');
                        setTimeout(function() {
                            if (!$this.is(':focus')) {
                                var val = $this.val();
                                if (val == '') {
                                    $form.css('visibility', 'hidden');
                                } else {
                                    $form.submit();
                                }
                            }
                        }, 50);
                    });
            });
            $centerPanel.off('submit', '.taskBoardContainer').on('submit', '.taskBoardContainer', function(e) {
                var $form = $(e.target);
                var data = {};
                $form.find('input').each(function() {
                    var $input = $(this);
                    var name = $input.attr('name');
                    var val = $input.val();
                    if ($input.attr('type') == 'text') {
                        $input.val('');
                    }
                    data[name] = val;
                });
                if (data.taskTitle != undefined && data.taskTitle != '') {
                    $centerPanel.jzAjax('TaskController.createTaskInListView()', {
                        method: 'POST',
                        data: data,
                        success: function(task) {
                            var html = boardView.renderTask(task);
                            $form.before(html);

                            // Update task number in status column
                            var statusId = $form.closest('[data-statusid]').data('statusid');
                            var $count = $('[data-statusid="'+statusId+'"] > [data-taskcount-status]');
                            var count = $count.data('taskcount-status');
                            count++;
                            $count.html(count);
                            if (!$count.hasClass('number-tasks')) {
                                $count.addClass('number-tasks');
                            }
                            $count.data('taskcount-status', count);
                            $count.attr('data-taskcount-status', count);

                            // Update task number in assignee group
                            if (data['groupBy'] == 'assignee') {
                                var $ac = $('.amount-item.user-'+data['assignee']+'[data-taskcount-assignee]');
                                if ($ac.length > 0) {
                                    var c = $ac.data('taskcount-assignee');
                                    c++;
                                    $ac.html(c);
                                    $ac.data('taskcount-assignee', c);
                                    $ac.attr('data-taskcount-assignee', c);
                                }
                            }

                        },
                        error: function(xhr) {
                          taApp.showWarningDialog(xhr.responseText);
                        }
                    });
                }
                $form.css('visibility', 'hidden');
                return false;
            });
        };

        boardView.initEditInline = function() {
            var ui = taApp.getUI();
            var $centerPanelContent = ui.$centerPanelContent;
            var saveStatusFunction = function(params) {
                var d = new $.Deferred;
                var data = {
                    id: params.pk,
                    name: params.value
                };
                $centerPanelContent.jzAjax('StatusController.updateStatus()',{
                    data: data,
                    method: 'POST',
                    success: function(response) {
                        d.resolve();
                    },
                    error: function(xhr, textStatus, errorThrown ) {
                      $('[data-pk="' + params.pk + '"]').first().editable('toggle');
                      d.reject('update failure: ' + xhr.responseText);
                      taApp.showWarningDialog(xhr.responseText);
                    }
                });
                return d.promise();
            };
            var options = {
                url: saveStatusFunction,
                toggle: 'dblclick',
                mode: 'inline',
                showbuttons: false,
                onblur: 'submit',
                emptyclass: 'muted',
                highlight: false,
                inputclass: 'input-small input-board',
                clear: false
            };
            $centerPanelContent.find('.taskBoardView .editable').each(function() {
                var $this = $(this);
                $this.editable(options);
                $this.on('shown', function(e, editable) {
                    $this.parent().removeClass('inactive').addClass('active');
                }).on('hidden', function(e, editable) {
                    $this.parent().removeClass('active').addClass('inactive');
                });
            });
        };

        boardView.initDragDrop = function() {
            var taskUI = taApp.getUI();
            var $centerPanelContent = taskUI.$centerPanelContent;
            var groupBy = $centerPanelContent.find('[name="groupBy"]').val();
            var orderBy = $centerPanelContent.find('[name="orderBy"]').val();
            var isSortable = (orderBy == 'rank');

            var placeHoderClass = isSortable ? 'draggableHighlight' : 'draggableFullContainer';
            $centerPanelContent.find('.taskBoardContainer').each(function() {
                var $this = $(this);
                var connected = '.taskBoardContainer';
                $this.sortable({
                    items: ".taskItem",
                    connectWith: connected,
                    revert: true,
                    placeholder: placeHoderClass,
                    handle: ".dragable",
                    containment: '.taskBoardView .table-project-collapse',
                    opacity: 0.9,
                    receive: function( event, ui ) {
                        var $container = $(event.target);
                        if ($container.find('div.taskItem').length <= 1) {
                            //. Force Form to add task always at bottom of container
                            var $form = $container.find('form');
                            var $clone = $form.clone();
                            $form.remove();
                            $container.append($clone);
                        }
                    },
                    update: function(event, ui) {
                        var $task = ui.item;
                        var taskId = $task.data('taskid');
                        var $status = $(event.target).closest('[data-statusid]');
                        if ($status.has('.taskItem[data-taskid="'+taskId+'"]').length == 0) {
                            return;
                        }

                        var $sender = ui.sender;
                        if ($sender == null) {
                            $sender = $status;
                        }
                        var oldGroup = $sender.data('groupby-value');
                        var newGroup = $status.data('groupby-value');

                        var statusId = $status.data('statusid');
                        var listItem = $status.sortable('toArray', {attribute: 'data-taskid'});

                        for(var i = 0; i < listItem.length; i++) {
                            listItem[i] = parseInt(listItem[i]);
                        }

                        $centerPanelContent.jzAjax('TaskController.saveDragAndDropTask()', {
                            data: {
                                taskId: taskId,
                                newStatusId: statusId,
                                groupBy: groupBy,
                                oldGroup: oldGroup,
                                newGroup: newGroup,
                                needUpdateOrder: isSortable,
                                orders: listItem
                            },
                            method: 'POST',
                            traditional: true,
                            success: function(response) {

                            },
                            error: function(xhr, textStatus, errorThrown ) {
                                taApp.showWarningDialog(xhr.responseText);
                                $status.sortable('cancel');
                                $(ui.sender).sortable('cancel');
                            }
                        });
                    },
                    over: function(event, ui) {
                        var $container = $(event.target);
                        var $form = $container.find('form');
                        $form.hide();
                    },
                    out: function(event, ui) {
                        var $container = $(event.target);
                        var $form = $container.find('form');
                        $form.show();
                    }
                });
            });
        }

        return boardView;
});
