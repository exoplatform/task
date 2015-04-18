jQuery(document).ready(function($) {
    var saveTaskDetailFunction = function(params) {
        var d = new $.Deferred;
        var data = params;
        data.taskId = params.pk;
        $('#taskDetailContainer').jzAjax('TaskController.saveTaskInfo()',{
            data: data,
            method: 'POST',
            traditional: true,
            success: function(response) {
                d.resolve();
            },
            error: function(jqXHR, textStatus, errorThrown ) {
                alert('update failure, http status: ' + textStatus);
                d.reject('update failure, http status: ' + textStatus);
            }
        });
        return d.promise();
    };

    var initEditInline = function(taskId) {
        var $taskDetailContainer = $('#taskDetailContainer');
        $taskDetailContainer.find('.editable').each(function(){
            var $this = $(this);
            var dataType = $this.attr('data-type');
            var fieldName = $this.attr('data-name');
            var editOptions = {
                mode: 'inline',
                showbuttons: false,
                pk: taskId,
                url: saveTaskDetailFunction
            };

            if(dataType == 'textarea') {
                editOptions.showbuttons = 'bottom';
                editOptions.emptytext = "Description";
            }
            if(fieldName == 'assignee' || fieldName == 'coworker') {
                var findUserURL = $this.jzURL('UserController.findUser');
                var getDisplayNameURL = $this.jzURL('UserController.getDisplayNameOfUser');
                //editOptions.source = findUserURL;
                editOptions.showbuttons = true;
                editOptions.emptytext = "Unassigned";
                editOptions.source = findUserURL;
                editOptions.select2= {
                    multiple: (fieldName == 'coworker'),
                    allowClear: true,
                    placeholder: 'Select an user',
                    tokenSeparators:[","],
                    minimumInputLength: 1,
                    initSelection: function (element, callback) {
                        return $.get(getDisplayNameURL, { usernames: element.val() }, function (data) {
                            callback((fieldName == 'coworker') ? data : data[0]);
                        });
                    }
                };

                //. This is workaround for issue of xEditable: https://github.com/vitalets/x-editable/issues/431
                if(fieldName == 'coworker') {
                    editOptions.display = function (value, sourceData) {
                        //display checklist as comma-separated values
                        if (!value || !value.length) {
                            $(this).empty();
                            return;
                        }
                        if (value && value.length > 0) {
                            //. Temporary display username in text field. It will be replace with displayName after ajax Get success
                            $(this).html(value.join(', '));
                            var $this = $(this);
                            $.get(getDisplayNameURL, { usernames: value.join(',') }, function (data) {
                                var html = [];
                                $.each(data, function (i, v) {
                                    html.push($.fn.editableutils.escape(v.text));
                                });
                                $this.html(html.join(', '));
                            });
                        }
                    };
                }
            }
            if(fieldName == 'dueDate') {
                editOptions.emptytext = "no Duedate";
                editOptions.mode = 'popup';
            }
            if(fieldName == 'status') {
                //var allStatusURL = $this.jzURL('StatusController.getAllStatus');
                var currentStatus = $this.attr('data-val');
                //console.log(allStatusURL);
                //editOptions.source = allStatusURL;
                editOptions.value = currentStatus;
            }
            if(fieldName == 'coworker') {
                $this.on('shown', function(e) {
                    var $a = $(e.target);
                    var val = $a.attr('data-value');
                    if(val != '') {

                    }
                });
            }
            $this.editable(editOptions);
        });
    };

    var $taskListcontainer = $('#taskListcontainer');
    var $taskDetailContainer = $('#taskDetailContainer');
    $taskDetailContainer.hide();
    $taskListcontainer.removeClass('span6').addClass('span12');
    $taskListcontainer.on('click', 'li.task', function(e){
        var $li = $(e.target || e.srcElement).closest('li.task');
        var taskId = $li.attr('task-id');
        $taskDetailContainer.jzLoad('TaskController.detail()', {id: taskId}, function(html){
            $taskDetailContainer.show();
            $taskListcontainer.removeClass('span12').addClass('span6');
            initEditInline(taskId);
            return false;
        });
    });
    $taskDetailContainer.on('click', 'a.close', function(e) {
        $taskDetailContainer.html('');
        $taskDetailContainer.hide();
        $taskListcontainer.removeClass('span6').addClass('span12');
    });
    $taskDetailContainer.on('click', 'a.status-icon', function(e){
        e.preventDefault();
        var $a = $(e.target || e.srcElement).closest('a');
        var isCompleted = $a.hasClass('icon-completed');
        var taskId = $taskDetailContainer.find('.task-detail').attr('task-id');
        var statusToUpdate = isCompleted ? 1 : 4; // 1 = TODO, 4 = Done
        var data = {taskId: taskId, status: statusToUpdate};
        $a.jzAjax('TaskController.updateTaskStatus()', {
            data: data,
            success: function(message) {
                $a.toggleClass('icon-completed');
            }
        });
    });
    $taskDetailContainer.on('click', 'a.action-clone-task', function(e){
        var $a = $(e.target).closest('a');
        var taskId = $a.closest('.task-detail').attr('task-id');
        $a.jzAjax('TaskController.clone()', {
            data: {id: taskId},
            success: function(response) {
                console.log(response);
                window.location.reload();
            }
        });
    });
    $taskDetailContainer.on('click', 'a.action-delete-task', function(e){
        var $a = $(e.target).closest('a');
        var taskId = $a.closest('.task-detail').attr('task-id');
        $a.jzAjax('TaskController.delete()', {
            data: {id: taskId},
            success: function(response) {
                console.log(response);
                window.location.reload();
            }
        });
    });
});