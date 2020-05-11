define('taskDetailView', ['SHARED/jquery', 'taskManagementApp', 'taskCenterView', 'SHARED/juzu-ajax','SHARED/commons-editor'], function($, taApp, taskCenterView) {
    var detailView = {};
    detailView.init = function() {
        taApp.onReady(function($) {
            detailView.initDomEvent();
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
        $rightPanel.on('click', 'a.action-watch-task', function(e){
            var $a = $(e.target).closest('a');
            var taskId =$a.closest('[data-taskid]').data('taskid');
            var isWatchedTask =$a.closest('[data-WatchedTask]').data('watchedtask');
            taApp.showDialog('TaskController.watchUnwatch()', {id : taskId, isWatched : isWatchedTask});
        });
        $rightPanel.on('click', 'a.action-clone-task', function(e){
            var $a = $(e.target).closest('a');
            var taskId = $a.closest('[data-taskid]').data('taskid');
            taApp.showDialog('TaskController.openConfirmCloneTask()', {id : taskId});
        });

        $rightPanel.on('click', 'a.action-delete-comment', function (e) {
            var commentId = $(e.target).closest('[data-commentid]').data('commentid');
            taApp.showDialog('TaskController.openConfirmDeleteComment()', {id: commentId});

        });
    };

    var addSubCommentsActions  = function(selectedParentCommentId) {
        var ui = taApp.getUI();
        var $rightPanelContent=ui.$rightPanelContent;
        if(selectedParentCommentId) {
            $('#SubCommentShowAll_' + selectedParentCommentId).hide();
            $rightPanelContent.find('[data-parent-comment=' + selectedParentCommentId + ']').removeClass('hidden');
        }
        $rightPanelContent.find('.replyCommentLink').on("click", function() {
            var parentCommentId = $(this).closest('.commentItem').attr('data-parent-comment');
            if(!parentCommentId) {
                parentCommentId = $(this).closest('.commentItem').attr('data-commentid');
            }
            if(!parentCommentId) parentCommentId = "";
            $(this).closest('#tab-comments').find("form").attr('data-commentid', parentCommentId);
            $rightPanelContent.find(".parentCommentBlock").removeClass("hidden");
            try {
                CKEDITOR.instances.comment.destroy();
            } catch(e) {
                console.log(e);
            }
            var $inputContainer = $(".commentList.inputContainer");
            $inputContainer.addClass("subCommentBlock");
            $inputContainer.insertAfter($(".commentItem[data-commentid=" + parentCommentId + "], .commentItem[data-parent-comment=" + parentCommentId + "]").last());
            $inputContainer.find(".commentInput").html($('<div>').append($inputContainer.find('textarea').clone()).html());
            initCKEditor($rightPanelContent.find('.commentBox .commentInput textarea'));
        });
        $rightPanelContent.find(".subCommentShowAllLink").on("click", function() {
            var parentCommentId = $(this).attr('data-parent-comment');
            $('#SubCommentShowAll_' + parentCommentId).hide();
            $rightPanelContent.find('[data-parent-comment=' + parentCommentId + ']').removeClass('hidden');
        });
        $rightPanelContent.find(".parentCommentLink").on("click", function() {
            try {
                CKEDITOR.instances.comment.destroy();
            } catch(e) {
                console.log(e);
            }
            var $inputContainer = $(".commentList.inputContainer");
            $inputContainer.removeClass("subCommentBlock");
            $inputContainer.insertAfter($(".commentItem[data-commentid]").last());
            $inputContainer.find(".commentInput").html($('<div>').append($inputContainer.find('textarea').clone()).html());
            initCKEditor($rightPanelContent.find('.commentBox .commentInput textarea'));
            $rightPanelContent.find("form").attr('data-commentid', null);
            $rightPanelContent.find(".parentCommentBlock").addClass("hidden");
        });
    };

    detailView.initCommentEditor = function() {
        var $commentContainer = $('#tab-comments');
        initCKEditor($commentContainer.find('.commentBox .commentInput textarea'));
    };
    var initCKEditor = function(element) {
        if (!element || !element.length) {
            return;
        }
        var windowWidth = $(window).width();
        var windowHeight = $(window).height();
        var extraPlugins = 'simpleLink,selectImage,suggester';
        var MAX_LENGTH = 2000;
        // TODO this line is mandatory when a custom skin is defined, it should not be mandatory
        CKEDITOR.basePath = '/commons-extension/ckeditor/';
        element.ckeditor ({
            customConfig: '/commons-extension/ckeditorCustom/config.js',
            extraPlugins: extraPlugins,
            placeholder: element[0].title,
            on : {
                instanceReady: function (evt) {
                    // Hide the editor toolbar
                    $("#taskCommentButton").prop("disabled", true);
                    $('#' + evt.editor.id + '_bottom').removeClass('cke_bottom_visible');
                },
                focus: function (evt) {
                    // Show the editor toolbar, except for smartphones in landscape mode
                    if ($(window).width() > 767 || $(window).width() < $(window).height()) {
                        //$('#' + evt.editor.id + '_bottom').css('display', 'block');
                        evt.editor.execCommand('autogrow');
                        var $content = $('#' + evt.editor.id + '_contents');
                        var contentHeight = $content.height();
                        var $ckeBottom = $('#' + evt.editor.id + '_bottom');
                        $ckeBottom[0].style.display = "block";
                        $ckeBottom.animate({
                            height: "39"
                        }, {
                            step: function (number, tween) {
                                $content.height(contentHeight - number);
                                if (number >= 9) {
                                    $ckeBottom.addClass('cke_bottom_visible');
                                }
                            }
                        });
                    } else {
                        $('#' + evt.editor.id + '_bottom').removeClass('cke_bottom_visible');
                        $('#' + evt.editor.id + '_bottom')[0].style.display = "none";
                    }
                },
                blur: function (evt) {
                    // Hide the editor toolbar
                    if (windowWidth > 767 || windowWidth < windowHeight) {
                        $('#' + evt.editor.id + '_contents').css('height', $('#' + evt.editor.id + '_contents').height() + 39);
                        $('#' + evt.editor.id + '_bottom').css('height', '0px');
                        $('#' + evt.editor.id + '_bottom').removeClass('cke_bottom_visible');
                    }
                },
                change: function( evt) {
                    var newData = evt.editor.getData();
                    var pureText = newData? newData.replace(/<[^>]*>/g, "").replace(/&nbsp;/g,"").trim() : "";
                    var disabledBtn = ( pureText.length == 0 && newData.indexOf("data-plugin-name=\"selectImage\"") < 0 ) || pureText.length > MAX_LENGTH;
                    $("#taskCommentButton").prop("disabled", disabledBtn);
                    if (pureText.length <= MAX_LENGTH) {
                        evt.editor.getCommand('selectImage').enable();
                    } else {
                        evt.editor.getCommand('selectImage').disable();
                    }
                },
                key: function( evt) {
                    var newData = evt.editor.getData();
                    var pureText = newData? newData.replace(/<[^>]*>/g, "").replace(/&nbsp;/g,"").trim() : "";
                    if (pureText.length > MAX_LENGTH) {
                        if ([8, 46, 33, 34, 35, 36, 37,38,39,40].indexOf(evt.data.keyCode) < 0) {
                            evt.cancel();
                        }
                    }
                }
            },
            suggester: {
                //suffix: ' ',
                renderMenuItem: '<li data-value="${uid}"><div class="avatarSmall" style="display: inline-block;"><img src="${avatar}"></div>${name} (${uid})</li>',
                renderItem: '<span class="exo-mention">${name}<a href="#" class="remove"><i class="uiIconClose uiIconLightGray"></i></a></span>',
                sourceProviders: ['task:people']
            }
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

    detailView.initDeleteCommentDialog = function() {
        $('.confirmDeleteComment')
            .off('click', '.confirmDelete')
            .on('click', '.confirmDelete', function(e) {
                var $a = $(e.target).closest('button');
                var $comment = $a.closest('[data-commentid]');
                var $commentContainer = $('#tab-comments');
                var $task = $a.closest('[data-taskid]');
                var taskId = $task.data('taskid');//ok
                var commentId = $comment.data('commentid');
                var parentCommentId = $commentContainer.find('.subCommentBlock').data('parent-comment');
                var loadAllComment = $commentContainer.children().data('allcomment');
                var deleteURL = $a.jzURL('TaskController.deleteComment');
                $.ajax({
                    url: deleteURL,
                    data: {commentId: commentId},
                    type: 'POST',
                    success: function(data) {
                        $commentContainer.jzLoad('TaskController.renderTaskComments()', {id: taskId, loadAllComment: loadAllComment}, function() {
                            detailView.enhanceCommentsLinks(parentCommentId);
                            detailView.initCommentEditor();
                        });
                    },
                    error: function(xhr, textStatus, errorThrown) {
                        if (xhr.status >= 400) {
                            alert(xhr.responseText);
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
    detailView.enhanceCommentsLinks = function(commentId) {
        var ui = taApp.getUI();
        var $rightPanel = ui.$rightPanel;
        var $rightPanelContent=ui.$rightPanelContent;
        addSubCommentsActions(commentId);
        $rightPanel.find('.contentComment').each(function(index, ele) {
            var commentContainer = $(ele);
            commentContainer.html(taApp.convertURLsAsLinks(commentContainer.html()));
        });
        $rightPanelContent.find(".subCommentShowAllLink").on("click", function() {
            var parentCommentId = $(this).attr('data-parent-comment');
            $('#SubCommentShowAll_' + parentCommentId).toggle();
            $rightPanelContent.find('[data-parent-comment=' + parentCommentId + ']').toggleClass('hidden');
        });
    };
    return detailView;
})
