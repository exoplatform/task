// TODO: Move juzu-ajax, mentionsPlugin module into task management project if need
require(['taskManagementApp', 'project-menu', 'taFilter', 'taskCenterView', 'taskListView', 'ta_edit_inline', 'SHARED/jquery', 'SHARED/taskLocale',
        'SHARED/juzu-ajax', 'SHARED/mentionsPlugin', 'SHARED/bts_modal', 'SHARED/bts_tab', 'SHARED/suggester', 'SHARED/commons-editor'
        ], function(taApp, pMenu, taFilter, taskCenterView, taskListView, editInline, $, locale) {
  
$(document).ready(function() {
    var ui = taApp.getUI();
    var $taskManagement = ui.$taskManagement;
    var $leftPanel = ui.$leftPanel;
    var $centerPanel = ui.$centerPanel;
    var $rightPanel = ui.$rightPanel;
    var $rightPanelContent = ui.$rightPanelContent;
    var $centerPanelContent = ui.$centerPanelContent;
    
    pMenu.init(taApp);
    editInline.init(taApp);
    taFilter.init(taApp);

    $('[data-toggle="tooltip"]').tooltip();
    $('*[rel="tooltip"]').tooltip({
        placement: 'top'
    });
    
    //welcome    
    var $inputTask = $centerPanelContent.find('input[name="taskTitle"]');
    taApp.showOneTimePopover($inputTask);

    $centerPanel.on('click', '.show-hide-left', function(e) {
    	var $icon = $(e.target);
    	if ($icon.hasClass('uiIconMiniArrowLeft')) {
    	  taApp.hideLeftPanel();    		
    	} else {
    	  taApp.showLeftPanel();
    	}
    });
    
    $rightPanel.on('click', '.close-right-panel', function(e) {
        taApp.hideRightPanel($centerPanel, $rightPanel, $rightPanelContent);
        return false;
    });

    //
    $('body').suggester('addProvider', 'task:people', function(query, callback) {
        var _this = this;
        $('[data-taskid]').jzAjax('UserController.findUsersToMention()', {
            data: {query: query},
            success: function(data) {
                var result = [];
                for (var i = 0; i < data.length; i++) {
                    var d = data[i];
                    var item = {
                        uid: d.id.substr(1),
                        name: d.name,
                        avatar: d.avatar
                    };
                    result.push(item);
                }
                callback.call(_this, result);
            }
        });
    });
    var taskLoadedCallback = function(taskId, isAjax) {
        var $li = $centerPanel.find('[data-taskid="'+taskId+'"]');
        $centerPanel.find('[data-taskid].selected').removeClass('selected');
        $li.addClass('selected');
        editInline.initEditInline(taskId);
        var $permalink = $rightPanelContent.find('.taskPermalink');
        var link = $permalink[0].href;
        $rightPanelContent.find('.taskPermalinkPopoverContent input').attr("value", link);
        $permalink.popover({
            html: true,
            content: $rightPanelContent.find('.taskPermalinkPopoverContent').html()
        }).on('shown', function(e) {
            $(e.target).closest('.taskPermalinkContainer').find('.popover-content input').select();
        });
        $(document).on('click', function(e) {
            if ($(e.target).closest('.taskPermalinkContainer').length > 0) {
                e.stopPropagation();
                return false;
            } else {
              $permalink.popover('hide');
            }
        });

        initCKEditor($rightPanelContent.find('textarea'));

        addSubCommentsActions();
        return false;
    };

    var initCKEditor = function(element) {
        var windowWidth = $(window).width();
        var windowHeight = $(window).height();

        var extraPlugins = 'simpleLink,simpleImage,suggester';

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

                    if (pureText.length > 0 && pureText.length <= MAX_LENGTH) {
                        $("#taskCommentButton").removeAttr("disabled");
                    } else {
                        $("#taskCommentButton").prop("disabled", true);
                    }
                    if (pureText.length <= MAX_LENGTH) {
                        evt.editor.getCommand('simpleImage').enable();
                    } else {
                        evt.editor.getCommand('simpleImage').disable();
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
    function loadTaskDetail(taskId) {
        var currentTask = $rightPanelContent.find('[data-taskid]').data('taskid');
        if ($rightPanel.is(':visible') && currentTask == taskId) {
            return;
        }
        $rightPanelContent.jzLoad('TaskController.detail()', {id: taskId}, function(html, status, xhr) {
            if (xhr.status >= 400) {
                taApp.showWarningDialog(xhr.responseText);
            } else {
                taApp.showRightPanel($centerPanel, $rightPanel);
                taskLoadedCallback(taskId, true);
                return false;
            }
        });
    };
    $centerPanel.off('click', '[data-taskid]').on('click', '[data-taskid]', function(e) {
        var $li = $(e.target || e.srcElement).closest('[data-taskid]');
        var taskId = $li.data('taskid');
        loadTaskDetail(taskId);
    });
    if ($rightPanel.is(':visible') && $rightPanel.find('[data-taskid]')) {
        var taskId = $rightPanel.find('[data-taskid]').data('taskid');
        taskLoadedCallback(taskId, false);
    }

    $rightPanel.on('click', '[data-taskcompleted]', function(e){
        var $a = $(e.target).closest('[data-taskcompleted]');
        var taskId = $a.closest('.uiBox').data('taskid');
        taApp.setTaskComplete(taskId, !$a.data('taskcompleted'), taFilter.isShowCompletedTask());
    });
    $rightPanel.on('click', 'a.action-clone-task', function(e){
        var $a = $(e.target).closest('a');
        var taskId = $a.closest('[data-taskid]').data('taskid');
        $a.jzAjax('TaskController.clone()', {
            data: {id: taskId},
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

    $rightPanel.on('click', '.editAssignee', function(e) {
        e.stopPropagation();
        var $edit = $(e.target).closest('.uiEditableInline');
        $edit.addClass('active').removeClass('inactive');
        $edit.find('.assignmentPopup')
            .on('click', function(e) {
                e.stopPropagation();
            })
            .show();
        $(document).one('click', function(e) {
            $edit.find('.assignmentPopup').off('click').hide();
            $edit.addClass('inactive').removeClass('active');
        });
    });

    var initCommentEditor = function() {
        initCKEditor($rightPanelContent.find('textarea'));
    };

    var addSubCommentsActions  = function(selectedParentCommentId) {
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
        initCKEditor($rightPanelContent.find('textarea'));
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
        initCKEditor($rightPanelContent.find('textarea'));
        $rightPanelContent.find("form").attr('data-commentid', null);
        $rightPanelContent.find(".parentCommentBlock").addClass("hidden");
      });
    };

    var enhanceCommentsLinks = function(commentId) {
        addSubCommentsActions(commentId);
        $rightPanelContent.find('.contentComment').each(function(index, ele) {
            var commentContainer = $(ele);
            commentContainer.html(taApp.convertURLsAsLinks(commentContainer.html()));
        });
    };

    $rightPanel.on('submit', '.commentFormBox form', function(e) {
        e.preventDefault();
        var $form = $(e.target).closest('form');
        var $taskDetail = $form.closest('[data-taskid]');
        var $allComments = $form.closest('[data-allcomment]');
        var $commentContainer = $taskDetail.find('#tab-comments');

        var loadAllComment = $allComments.data('allcomment');
        var taskId = $taskDetail.data('taskid');
        var parentCommentId = $form.attr('data-commentid');
        var comment = $.trim($form.find('textarea').val());
        if (comment == '') {
            alert('Please fill your comment!');
            return false;
        }
        var postCommentURL = $form.jzURL('TaskController.comment');
        var xhr = $.post(postCommentURL, { "taskId": taskId, "comment": comment, "parentCommentId" : parentCommentId}, function(data) {
            $commentContainer.jzLoad('TaskController.renderTaskComments()', {id: taskId, loadAllComment: loadAllComment}, function() {
                enhanceCommentsLinks(parentCommentId);
                initCommentEditor();
            });
        },'json');
        xhr.fail(function() {
          taApp.showWarningDialog(xhr.responseText);
        });

        return false;
    });

    $rightPanel.on('click', '[data-commentid] a.controllDelete', function(e) {
        var $a = $(e.target).closest('a');
        var $allComments = $a.closest('[data-allcomment]');
        var $comment = $a.closest('[data-commentid]');
        var $commentContainer = $a.closest('#tab-comments');
        var $task = $a.closest('[data-taskid]');

        var taskId = $task.data('taskid');
        var commentId = $comment.data('commentid');
        var parentCommentId = $comment.data('parent-comment');
        var loadAllComment = $allComments.data('allcomment');
        var deleteURL = $a.jzURL('TaskController.deleteComment');
        $.ajax({
            url: deleteURL,
            data: {commentId: commentId},
            type: 'POST',
            success: function(data) {
                $commentContainer.jzLoad('TaskController.renderTaskComments()', {id: taskId, loadAllComment: loadAllComment}, function() {
                  enhanceCommentsLinks(parentCommentId);
                  initCommentEditor();
                });
            },
            error: function(xhr, textStatus, errorThrown) {
                alert(xhr.responseText);
            }
        });
    });

    $rightPanel.on('click', 'a.load-all-comments', function(e) {
        e.preventDefault();
        var $a = $(e.target).closest('a');
        var loadAll = $a.data('loadall');
        var $comment = $a.closest('#tab-comments');
        var taskId = $comment.closest('[data-taskid]').data('taskid');
        $comment.jzLoad('TaskController.renderTaskComments()', {id: taskId, loadAllComment: loadAll}, function(html, status, xhr) {
          if (xhr.status >= 400) {
            taApp.showWarningDialog(xhr.responseText);
          } else {
            enhanceCommentsLinks();
            initCommentEditor();            
          }
        });
    });

    $leftPanel.on('click', 'a.collapseTree', function(e) {
        var $a = $(e.target).closest('a');
        var $icon = $a.find('.uiIconLightGray');
        var $list = $a.closest('.dropdown').find('> .list-projects, .list-labels').first();
        $list.slideToggle(300, 'linear', function() {
            $icon.toggleClass('uiIconArrowDownMini').toggleClass('uiIconArrowRightMini');
            $a.closest('.accordion-heading').toggleClass('collapsed');
        });
    });
    $leftPanel.on('click', 'a.collapseSubProject, .collapseSubLabel', function(e){
        var $a = $(e.target).closest('a');
        var $icon = $a.find('.uiIconLightGray');
        var $list = $a.closest('.dropdown').find('> .list-projects, .list-labels').first();

        $list.slideToggle(300, 'linear', function() {
            $icon.toggleClass('uiIconArrowDownMini').toggleClass('uiIconArrowRightMini');
        });
    });
    
    var projectLoaded = function(id, $a) {
      if (!$a) {        
        $a = $leftPanel.find('.project-item[data-projectid=' + id + '] .project-name');      
      }
      $a.closest('.leftPanel > ul').find('li.active').removeClass('active');
      $a.closest('li').addClass('active');

      //welcome
      if ($a.data('id') == '0' && $leftPanel.find('.project-item').length == 0) {
        var $addProject = $taskManagement.find('.add-new-project');
        taApp.showOneTimePopover($addProject);
      }
      
      var $inputTask = $centerPanelContent.find('input[name="taskTitle"]');
      taApp.showOneTimePopover($inputTask);
    }
    
    var loadProjectDetail = function(id) {
      var $a = $leftPanel.find('.project-item[data-projectid=' + id + '] .project-name');      
      $a.closest('.leftPanel > ul').find('li.active').removeClass('active');
      $a.closest('li').addClass('active');

      taApp.hideRightPanel($centerPanel, $rightPanel, $rightPanelContent);
    }

    $leftPanel.on('click', 'a.project-name', function(e) {
        var $a = $(e.target).closest('a');
        var projectId = $a.data('id');
        
        var filter = $a.data('filter');
        var currentProject = $centerPanel.find('.projectListView').data('projectid');

        if (projectId > 0 && !$a.data('canview')) {
            var projectName = $a.html();
            taApp.showWarningDialog(locale.resolve('noPermissionToAccessProject', projectName));
            return false;
        }

        if ((currentProject != projectId || filter != undefined) && ($a.data('canview') || projectId <= 0)) {
            taApp.reloadTaskList(projectId, -1, filter, function() {
              projectLoaded(projectId, $a);
            });
        }

        if(projectId > 0 && $a.data('canedit') && (currentProject != projectId || $rightPanel.is(':hidden') || !$rightPanelContent.children().first().data('projectid'))) {
          loadProjectDetail(projectId);
        } else {
            taApp.hideRightPanel($centerPanel, $rightPanel, $rightPanelContent);
        }
    });
    
    var currentProject = $centerPanel.find('.projectListView').data('projectid');
    if (currentProject > 0 && !$rightPanel.is(':visible')) {
      var $a = $leftPanel.find('.project-item[data-projectid=' + currentProject + '] .project-name');
      projectLoaded(currentProject);
      if(currentProject > 0 && $a.data('canedit')) {
        loadProjectDetail(currentProject);        
      }
    }

    $centerPanel.on('click', 'a.btn-add-task', function(e) {
        $centerPanel.find('.input-field input').focus();
        return false;
    });
    
    $centerPanel.on('blur', '.input-field input', function(e) {
    	if ($(e.target).val()) {
    		$centerPanel.find('.form-create-task').submit();    		
    	}
    });

    $centerPanel.on('submit', 'form.form-create-task', function(e) {
        var $form = $(e.target).closest('form');
        var projectId = $form.closest('.projectListView').attr('data-projectId');
        var labelId = $form.closest('[data-labelid]').data('labelid');
        var $input = $form.find('input[name="taskTitle"]');
        var taskInput = $input.val();
        if (!taskInput) return false;
        $input.val('');
        var filter = $leftPanel.find('.active .project-name[data-id="' + projectId + '"]').data('filter');
        if (filter == undefined) {
            filter = '';
        }

        $form.jzAjax('TaskController.createTask()', {
            method: 'POST',
            data: {projectId: projectId, labelId: labelId, taskInput: taskInput, filter: filter},
            success: function(task) {
                var $taskContainer = $centerPanelContent.find('.newTaskContainer ul');
                if ($taskContainer.length == 0) {
                    var id = task.id;
                    taApp.reloadTaskList(projectId, labelId, filter, function() {
                        $centerPanel.find('.taskItem[data-taskid="' + id + '"]').click();
                        $centerPanel.find('input[name="taskTitle"]').focus();
                    });
                } else {
                    var taskDetail = task.detail;
                    var html = taskListView.renderTask(taskDetail);
                    $taskContainer.prepend(html);
                    loadTaskDetail(task.id);
                }

                var $taskRow = $centerPanelContent.find('.taskList .taskItem');
                if ($taskRow.length > 1) {
                  $centerPanelContent.find('.groupByOptions').show();
                }
                
                if (projectId < 0) {
                  taApp.updateTaskNum(true);
                }
            }, 
            error: function(xhr) {
              taApp.showWarningDialog(xhr.responseText);
            }
        });
        return false;
    });

    var submitFilter = taskCenterView.submitFilter;

    $centerPanel.on('submit', '.projectListView form.taskSearchForm', function(e) {
        submitFilter(e);
        return false;
    });

    $centerPanel.on('change', '.taskList form.filter-form select', function(e) {
        submitFilter(e);
        return false;
    });
    $centerPanel.on('click', '[data-groupby]', function(e) {
        var $searchForm = $centerPanel.find('form.form-search');
        var $a = $(e.target || e.srcElement).closest('[data-groupby]');
        var $input = $('[name="groupBy"]').val($a.data('groupby'));
        if (taFilter.isEnable()) {
          taFilter.submitFilter();
        } else {
          submitFilter(e);          
        }
    });
    $centerPanel.on('click', '[data-orderby]', function(e) {
        var $searchForm = $centerPanel.find('form.form-search');
        var $a = $(e.target || e.srcElement).closest('[data-orderby]');
        $('[name="orderBy"]').val($a.data('orderby'));
        if (taFilter.isEnable()) {
          taFilter.submitFilter();
        } else {
          submitFilter(e);          
        }
    });
    $centerPanel.on('click', '[data-taskfilter]', function(e) {
        var $searchForm = $centerPanel.find('form.form-search');
        var $a = $(e.target || e.srcElement).closest('[data-taskfilter]');
        $('[name="filter"]').val($a.data('taskfilter'));
        submitFilter(e);
    });
    $centerPanel.on('click', 'a[data-viewtype]', function(e) {
        var $a = $(e.target || e.srcElement).closest('[data-viewtype]');
        var $li = $a.parent();
        if ($li.hasClass('disabled') || $li.hasClass('active')) {
            return;
        }
        var viewType = $a.data('viewtype');
        $('[name="viewType"]').val(viewType);
        submitFilter(e);
    });
    $centerPanel.on('click', 'a[data-viewpage]', function(e) {
        var $a = $(e.target).closest('[data-viewpage]');
        var page = $a.data('viewpage');
        $('[name="page"]').val(page);
        submitFilter(e);
    });

    //show hide the search box for responsive
    $centerPanel.on('click', '.action-search' ,function() {
        $(this).css('display','none');
         $(this).parents('.uiHeaderBar').addClass('toggle');
        return false;
    });
    $centerPanel.on('click', '.action-close' ,function() {
        $(this).css('display','none');
        $(this).prev().css('display','inline-block');
         $(this).parents('.uiHeaderBar').removeClass('toggle');
        return false;
    });
   
     //hide the popover when there are no project in left panel
    $('.add-new-project').on('click',function() {
        $(this).parent().find('.popover').css('display','none');
    });

    $leftPanel.on('click', '.changeProjectColor', function(e) {
        e.preventDefault();
        var $a = $(e.target).closest('a,li');
        var $project = $a.closest('.project-item');
        var projectId = $project.attr('data-projectId');
        var color = $a.attr('data-color');

        var updateProjectURL = $a.jzURL('ProjectController.changeProjectColor');
        $.ajax({
            url: updateProjectURL,
            data: {
                projectId: projectId,
                color: color
            },
            type: 'POST',
            success: function(data) {
                $project.find('a.colorPie').attr('class', color + ' colorPie');
                $project.find('a > i.uiIconTick').removeClass('uiIconTick');
                $a.find('i').addClass('uiIconTick');
                $centerPanelContent.find('[data-taskid][data-task-projectid="'+projectId+'"] .project-color').attr('class', 'project-color ' + color);
            },
            error: function(xhr) {
              taApp.showWarningDialog(xhr.responseText);
            }
        });

        $project.find('.sub-item.open').removeClass('open');

        return false;
    });

    // Table Project Collapse
     $centerPanel.on('click', '.table-project-collapse .toggle-collapse-group' ,function() {
        if($(this).parents('.heading').next('.collapse-content').is(':visible')) {
            $(this).parents('.heading').next('.collapse-content').slideUp(200);
            $(this).find('.uiIcon').attr('class','uiIcon uiIconMiniArrowRight');
        }
        else {
            $(this).parents('.heading').next('.collapse-content').slideDown(500);
            $(this).find('.uiIcon').attr('class','uiIcon uiIconMiniArrowDown');
        }
    });

    $leftPanel.on('click', '.actionShowHiddenProject', function(e) {
        var $a = $(e.target || e.srcElement).closest('a');
        var $projects = $a.closest('li[data-showhiddenproject]');
        var isShowHidden = $projects.data('showhiddenproject');
        var data = {show: !isShowHidden};
        $a.jzAjax('UserController.showHiddenProject()', {
            data: data,
            success: function(message) {
                $projects.attr('data-showhiddenproject', !isShowHidden ? "true" : "false");
                $projects.data('showhiddenproject', !isShowHidden);
            },
            error : function(jqXHR, textStatus, errorThrown) {
                console.error && console.error('update failure: ' + jqXHR.responseText);
            }
        });
        return true;
    });

    $leftPanel.on('click', '.actionHideProject', function(e) {
        var $a = $(e.target || e.srcElement).closest('a');
        var $project = $(e.target).closest('.project-item');
        var projectId = $project.data('projectid');
        var isHidden = $project.data('hiddenproject');
        var data = {projectId: projectId, hide: !isHidden};
        $a.jzAjax('UserController.hideProject()', {
            data: data,
            success: function(message) {
                // Update data attribute in DOM element
                $project.attr('data-hiddenproject', !isHidden ? "true" : "false");
                $project.data('hiddenproject', !isHidden);

                //. Check is all projects are hidden or not
                var $projectList = $project.closest('li[data-showhiddenproject]');
                if ($projectList.find('[parentid="0"] > li[data-hiddenproject="false"]').length == 0) {
                    // If all project are hidden, we need add message "All projects are hidden"
                    $projectList.find('li.not-all-project-hidden')
                        .removeClass('not-all-project-hidden')
                        .addClass('all-project-hidden');

                } else {
                    $projectList.find('li.all-project-hidden')
                        .removeClass('all-project-hidden')
                        .addClass('not-all-project-hidden');
                }
            },
            error : function(xhr, textStatus, errorThrown) {
              taApp.showWarningDialog(xhr.responseText);
            }
        });

        return true;
    });
  });
});