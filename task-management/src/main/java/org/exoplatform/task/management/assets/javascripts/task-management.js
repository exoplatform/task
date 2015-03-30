jQuery(document).ready(function($) {
    var saveTaskDetailFunction = function(params) {
        var d = new $.Deferred;
        var data = params;
        data.taskId = params.pk;
        $('#taskDetailContainer').jzAjax('TaskController.saveTaskInfo()',{
            data: data,
            method: 'POST',
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
            if(dataType == 'select2') {
                editOptions.inputclass = 'input-large';
                editOptions.select2 = {
                    tags: ['html', 'javascript', 'css', 'ajax'],
                        tokenSeparators: [",", " "]
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