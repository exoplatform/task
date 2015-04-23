// TODO: Move juzu-ajax, mentionsPlugin module into task management project if need
require(['SHARED/jquery', 'SHARED/edit_inline_js', 'SHARED/juzu-ajax', 'SHARED/mentionsPlugin'], function(jQuery) {
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
                //editOptions.source = allStatusURL;
                editOptions.value = currentStatus;
            }
            if(fieldName == 'tags') {
                editOptions.showbuttons = true;
                editOptions.emptytext = "No Tags";
                editOptions.display = function(value, sourceData, response) {
                    if(value && value.length > 0) {
                        var html = [];
                        $.each(value, function(i, v) {
                            if(typeof v == 'string') {
                                html.push('<span class="badgeDefault badgePrimary">' + v + '</span>');
                            } else {
                                html.push('<span class="badgeDefault badgePrimary">' + v.text + '</span>');
                            }
                        });
                        $(this).html(html.join(' '));
                    } else {
                        $(this).empty();
                    }
                };
                editOptions.select2 = {
                    tags: [],
                    tokenSeparators: [',']
                };
            }
            $this.editable(editOptions);
        });
    };

    var $taskListcontainer = $('#taskListcontainer');
    var $taskDetailContainer = $('#taskDetailContainer');
    $taskDetailContainer.hide();
    $taskListcontainer.removeClass('span6').addClass('span12');

    var currentTask = 0;
    $taskListcontainer.on('click', 'li.task', function(e){
        var $li = $(e.target || e.srcElement).closest('li.task');
        var taskId = $li.attr('task-id');
        if (taskId != currentTask || $taskDetailContainer.is(':hidden')) {
            $taskDetailContainer.jzLoad('TaskController.detail()', {id: taskId}, function(html) {
                $taskDetailContainer.show();
                $taskListcontainer.removeClass('span12').addClass('span6');
                initEditInline(taskId);
                currentTask = taskId;
                $taskDetailContainer.find('textarea').exoMentions({
                    onDataRequest:function (mode, query, callback) {
                        var _this = this;
                        $taskDetailContainer.jzAjax('UserController.findUsersToMention()', {
                            data: {query: query},
                            success: function(data) {
                                callback.call(_this, data);
                            }
                        });
                    },
                    idAction : 'taskCommentButton',
                    elasticStyle : {
                        maxHeight : '52px',
                        minHeight : '22px',
                        marginButton: '4px',
                        enableMargin: false
                    }
                });
                return false;
            });
        }
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
                window.location.reload();
            }
        });
    });

    $taskDetailContainer.on('submit', '.comment-form form', function(e) {
        e.preventDefault();
        var $form = $(e.target).closest('form');
        var $listComments = $form.closest('.task-detail').find('.list-comments');
        var taskId = $form.closest('.task-detail').attr('task-id');
        var comment = $.trim($form.find('textarea').val());
        if (comment == '') {
            alert('Please fill your comment!');
            return false;
        }
        var postCommentURL = $form.jzURL('TaskController.comment');
        $.post(postCommentURL, { taskId: taskId, comment: comment}, function(data) {
            var html = [];
            html.push('<li class="comment media">');
            html.push('    <a class="pull-left avatarXSmall" href="#">');
            html.push('     <img class="media-object" src="'+ data.author.avatar +'" alt="'+ data.author.displayName +'">');
            html.push('    </a>');
            html.push('    <div class="media-body">');
            html.push('    <div class="pull-right">');
            html.push('        <span class="muted">'+data.createdTimeString+'</span>');
            html.push('        <span class="comment-action">');
            html.push('            <a href="#" class="action-link delete-comment" commen-id="'+data.id+'"><i class="uiIconLightGray uiIconTrashMini"></i></a>');
            html.push('        </span>');
            html.push('    </div>');
            html.push('    <h6 class="media-heading"><a href="#">'+data.author.displayName+'</a></h6>');
            html.push('<div>');
            html.push(      data.formattedComment);
            html.push('</div>');
            html.push('</div>');
            html.push('</li>');
            var $html = $(html.join("\n"));
            $listComments.append($html);
            $listComments.find('li.no-comment').remove();
        },'json');

        return false;
    });

    $taskDetailContainer.on('click', 'a.delete-comment', function(e) {
        e.preventDefault();
        var $a = $(e.target).closest('a');
        var commentId = $a.attr('commen-id');
        var deleteURL = $a.jzURL('TaskController.deleteComment');
        $.ajax({
            url: deleteURL,
            data: {commentId: commentId},
            type: 'POST',
            success: function(data) {
                $a.closest('li.comment').remove();
            },
            error: function() {
                alert('Error while delete comment, please try again.');
            }
        });
    });

    $taskDetailContainer.on('click', 'a.load-all-comments', function(e) {
        e.preventDefault();
        var $a = $(e.target).closest('a');
        var $taskContainer = $a.closest('.task-detail');
        var taskId = $taskContainer.attr('task-id');
        var getAllCommentsURL = $a.jzURL('TaskController.loadAllComments');
        var $commentList = $taskContainer.find('ul.list-comments');
        $commentList.jzLoad(getAllCommentsURL, {taskId: taskId}, function(data) {
            $a.remove();
        });
    });
});
});