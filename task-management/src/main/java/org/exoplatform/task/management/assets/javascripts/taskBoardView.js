define('taskBoardView', ['jquery', 'taskManagementApp', 'SHARED/edit_inline_js'],
    function($, taApp, editinline) {

        var boardView = {};
        boardView.init = function() {
            taApp.onReady(function($) {
                boardView.initDomEventListener();
                boardView.initEditInline();
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

        return boardView;
});
