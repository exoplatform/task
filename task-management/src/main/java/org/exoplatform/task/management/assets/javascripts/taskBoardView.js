define('taskBoardView', ['jquery', 'taskManagementApp', 'SHARED/edit_inline_js', 'SHARED/task_jquery_ui'],
    function($, taApp, editinline) {

        var boardView = {};
        boardView.init = function() {
            taApp.onReady(function($) {
                boardView.initDomEventListener();
                boardView.initEditInline();
                boardView.initDragDrop();
            });
        };

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
                        }
                    });
                }
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
                    error: function(jqXHR, textStatus, errorThrown ) {
                        d.reject('update failure: ' + jqXHR.responseText);
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
                inputclass: 'blackLarge',
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
            var orderBy = $centerPanelContent.find('[name="orderBy"]').val();
            var isSortable = (orderBy == 'rank');

            var placeHoderClass = isSortable ? 'draggableHighlight' : 'draggableFullContainer';
            $centerPanelContent.find('.taskBoardContainer').each(function() {
                var $this = $(this);
                var connected = '.taskBoardContainer' + $this.data('connected');
                $this.sortable({
                    connectWith: connected,
                    revert: true,
                    placeholder: placeHoderClass,
                    //handle: ".dragable",
                    containment: '.taskBoardView .table-project-collapse',
                    opacity: 0.9,
                    receive: function( event, ui ) {
                        if (isSortable) {
                            return;
                        }
                        var $task = ui.item;
                        var $status = $(event.target).closest('[data-statusid]');
                        var taskId = $task.data('taskid');
                        var statusId = $status.data('statusid');

                        $centerPanelContent.jzAjax('TaskController.saveTaskInfo()',{
                            data: {
                                taskId: taskId,
                                name: 'status',
                                value: statusId
                            },
                            method: 'POST',
                            traditional: true,
                            success: function(response) {

                            },
                            error: function(jqXHR, textStatus, errorThrown ) {
                                alert('can not update task status');
                                $status.sortable('cancel');
                                $(ui.sender).sortable('cancel');
                            }
                        });
                    },
                    update: function(event, ui) {
                        if (!isSortable) {
                            return;
                        }
                        var $task = ui.item;
                        var taskId = $task.data('taskid');
                        var $status = $(event.target).closest('[data-statusid]');
                        if ($status.has('.taskItem[data-taskid="'+taskId+'"]').length == 0) {
                            return;
                        }

                        var statusId = $status.data('statusid');
                        var listItem = $status.sortable('toArray', {attribute: 'data-taskid'});

                        for(var i = 0; i < listItem.length; i++) {
                            listItem[i] = parseInt(listItem[i]);
                        }

                        $centerPanelContent.jzAjax('TaskController.saveTaskOrder()',{
                            data: {
                                taskId: taskId,
                                newStatusId: statusId,
                                orders: listItem
                            },
                            method: 'POST',
                            traditional: true,
                            success: function(response) {

                            },
                            error: function(jqXHR, textStatus, errorThrown ) {
                                alert('can not update task status and order');
                                $status.sortable('cancel');
                                $(ui.sender).sortable('cancel');
                            }
                        });
                    }
                });
            });
        }

        return boardView;
});
