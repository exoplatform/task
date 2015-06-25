// TODO: Move juzu-ajax, mentionsPlugin module into task management project if need
require(['project-menu', 'ta_edit_inline', 'SHARED/jquery',
        'SHARED/juzu-ajax', 'SHARED/mentionsPlugin', 'SHARED/bts_modal', 'SHARED/bts_tab'
        ], function(pMenu, editInline, $) {
  var taApp = {};

  taApp.getUI = function() {
    var $taskManagement = $('#taskManagement');
    var $leftPanel = $('#taskManagement > .leftPanel');
    var $centerPanel = $('#taskManagement > .centerPanel');
    var $rightPanel = $('#taskManagement > .rightPanel');
    var $rightPanelContent = $rightPanel.find('.rightPanelContent');
    var $centerPanelContent = $centerPanel.find('.centerPanelContent');
    
    return {
      '$taskManagement' : $taskManagement,
      '$leftPanel' : $leftPanel,
      '$centerPanel' : $centerPanel,
      '$rightPanel' : $rightPanel,
      '$rightPanelContent' : $rightPanelContent,
      '$centerPanelContent' : $centerPanelContent
    };
  }
  
  taApp.showDialog = function(controllerURL, data) {
    var $modalPlace = $('.modalPlace');
    $modalPlace.jzLoad(controllerURL, data, function() {
      var $dialog = $modalPlace.children().first();
      $dialog.modal({'backdrop': false});
    });
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

  taApp.showRightPanel = function($centerPanel, $rightPanel) {
    $centerPanel.removeClass('span9').addClass('span5');
    $rightPanel.show();
  };
  
  taApp.hideRightPanel = function($centerPanel, $rightPanel, $rightPanelContent) {
    $rightPanelContent.html('');
    $rightPanel.hide();
    $centerPanel.removeClass('span5').addClass('span9');
  };
  
  taApp.reloadTaskList = function(projectId, callback) {
    var $centerPanelContent = taApp.getUI().$centerPanelContent;
    $centerPanelContent.jzLoad('TaskController.listTasks()', {projectId: projectId}, function() {
      if (callback) {
        callback();
      }
    });
  }
    
  taApp.reloadProjectTree = function(id) {
    var $leftPanel = taApp.getUI().$leftPanel;
    var $listProject = $leftPanel.find('ul.list-projects');
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
      error : function(jqXHR, textStatus, errorThrown) {
        console.error && console.error('update failure: ' + jqXHR.responseText);
      }
    });
  }
  
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
    
    //welcome    
    var $inputTask = $centerPanelContent.find('input[name="taskTitle"]');
    taApp.showOneTimePopover($inputTask);
    $inputTask.focus();

    $rightPanel.on('click', '.close-right-panel', function(e) {
        taApp.hideRightPanel($centerPanel, $rightPanel, $rightPanelContent);
        return false;
    });

    $centerPanel.on('click', '[data-taskid]', function(e) {
        var $li = $(e.target || e.srcElement).closest('[data-taskid]');
        var taskId = $li.data('taskid');
        var currentTask = $rightPanelContent.find('.task-detail').attr('task-id');
        $rightPanelContent.jzLoad('TaskController.detail()', {id: taskId}, function(html) {
          $centerPanel.find('li.selected').removeClass('selected');
          $li.addClass('selected');
          taApp.showRightPanel($centerPanel, $rightPanel);
          editInline.initEditInline(taskId);
          $rightPanelContent.find('textarea').exoMentions({
            onDataRequest:function (mode, query, callback) {
              var _this = this;
              $('#taskDetailContainer').jzAjax('UserController.findUsersToMention()', {
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
    });

    $rightPanel.on('click', 'a.task-completed-field', function(e){
        var $a = $(e.target || e.srcElement).closest('a');
        $a.toggleClass('icon-completed');
        var taskId = $a.closest('.task-detail').attr('task-id');
        taApp.setTaskComplete(taskId, $a.hasClass('icon-completed'));
        return false;
    });
    $rightPanel.on('click', 'a.action-clone-task', function(e){
        var $a = $(e.target).closest('a');
        var taskId = $a.closest('.task-detail').attr('task-id');
        $a.jzAjax('TaskController.clone()', {
            data: {id: taskId},
            success: function(response) {
              var id = response.id; 
              var projectId = $leftPanel.find('.active .project-name').data('id');
              taApp.reloadTaskList(projectId, function() {
                $centerPanel.find('.taskItem[data-taskid="' + id + '"]').click();      
              });
            }
        });
    });
    $rightPanel.on('click', 'a.action-delete-task', function(e){
        var $a = $(e.target).closest('a');
        var taskId = $a.closest('.task-detail').attr('task-id');
        $a.jzAjax('TaskController.delete()', {
            data: {id: taskId},
            success: function(response) {
                window.location.reload();
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

    $rightPanel.on('submit', '.comment-form form', function(e) {
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

    $rightPanel.on('click', 'a.delete-comment', function(e) {
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

    $rightPanel.on('click', 'a.load-all-comments', function(e) {
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

    $rightPanel.on('submit', 'form.create-project-form', function(e) {
        var $form = $(e.target).closest('form');
        var name = $form.find('input[name="name"]').val();
        var description = $form.find('textarea[name="description"]').val();
        var $breadcumbs = $rightPanel.find('.breadcrumb');
        var parentId = $breadcumbs.data('value');

        if(name == '') {
            name = 'Untitled Project';
        }

        var createURL = $rightPanel.jzURL('ProjectController.createProject');        
        $.ajax({
            type: 'POST',
            url: createURL,
            data: {name: name, description: description, parentId: parentId},
            success: function(data) {
                // Reload project tree;
                taApp.reloadProjectTree(data.id);
            },
            error: function() {
                alert('error while create new project. Please try again.')
            }
        });

        return false;
    });

    $leftPanel.on('click', 'a.project-name', function(e) {
        var $a = $(e.target).closest('a');
        var projectId = $a.data('id');
        var currentProject = $centerPanel.find('.projectListView').data('projectid');

        if (currentProject != projectId && ($a.data('canview') || projectId <= 0)) {
            taApp.reloadTaskList(projectId, function() {
              $a.closest('.leftPanel > ul').find('li.active').removeClass('active');
              $a.closest('li').addClass('active');

              //welcome
              if ($a.data('id') == '0' && $leftPanel.find('.project-item').length == 0) {
                var $addProject = $taskManagement.find('.add-new-project');
                taApp.showOneTimePopover($addProject);
              }
              
              var $inputTask = $centerPanelContent.find('input[name="taskTitle"]');
              taApp.showOneTimePopover($inputTask);
              $inputTask.focus();
            });
        }
        // Show project summary at right panel
        if(projectId > 0 && $a.data('canedit') && (currentProject != projectId || $rightPanel.is(':hidden') || !$rightPanelContent.children().first().data('projectid'))) {
            $rightPanelContent.jzLoad('ProjectController.projectDetail()', {id: projectId}, function () {
                $a.closest('ul.list-projects[parentid="0"]').find('li.active').removeClass('active');
                $a.closest('li').addClass('active');
                taApp.showRightPanel($centerPanel, $rightPanel);
                //TODO: check can edit to init editInline
                if($rightPanelContent.find('[data-projectid]').data('canedit')) {
                    editInline.initEditInlineForProject(projectId);
                }
            });
        } else {
            taApp.hideRightPanel($centerPanel, $rightPanel, $rightPanelContent);
        }
    });

    $centerPanel.on('click', 'a.btn-add-task', function(e) {
        $centerPanel.find('.input-field input').focus();
        return false;
    });

    $centerPanel.on('submit', 'form.form-create-task', function(e) {
        var $form = $(e.target).closest('form');
        var projectId = $form.closest('.projectListView').attr('data-projectId');
        var taskInput = $form.find('input[name="taskTitle"]').val();
        $form.jzAjax('TaskController.createTask()', {
            method: 'POST',
            data: {projectId: projectId, taskInput: taskInput},
            success: function(html) {
                $centerPanelContent.html(html);
            }
        });
        return false;
    });

    var submitFilter = function(e) {
        var $projectListView =  $(e.target).closest('.projectListView');
        var projectId = $projectListView.attr('data-projectId');
        var groupBy = $projectListView.find('[name="groupBy"]').val();
        if(groupBy == undefined) {
            groupBy = '';
        }
        var orderBy = $projectListView.find('[name="orderBy"]').val();
        if(orderBy == undefined) {
            orderBy = '';
        }
        var filter = $projectListView.find('[name="filter"]').val();
        if (filter == undefined) {
            filter = '';
        }
        var keyword = $projectListView.closest('.projectListView').find('input[name="keyword"]').val();
        $centerPanelContent.jzLoad('TaskController.listTasks()',
            {
                projectId: projectId,
                keyword: keyword,
                groupBy: groupBy,
                orderBy: orderBy,
                filter: filter
            },
            function() {
                taApp.hideRightPanel($centerPanel, $rightPanel, $rightPanelContent);
            }
        );
    };

    $centerPanel.on('submit', '.projectListView form.form-search', function(e) {
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
        submitFilter(e);
    });
    $centerPanel.on('click', '[data-orderby]', function(e) {
        var $searchForm = $centerPanel.find('form.form-search');
        var $a = $(e.target || e.srcElement).closest('[data-orderby]');
        $('[name="orderBy"]').val($a.data('orderby'));
        submitFilter(e);
    });
    $centerPanel.on('click', '[data-taskfilter]', function(e) {
        var $searchForm = $centerPanel.find('form.form-search');
        var $a = $(e.target || e.srcElement).closest('[data-taskfilter]');
        $('[name="filter"]').val($a.data('taskfilter'));
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

        var updateProjectURL = $a.jzURL('ProjectController.saveProjectInfo');
        $.ajax({
            url: updateProjectURL,
            data: {
                projectId: projectId,
                name: 'color',
                value: color
            },
            type: 'POST',
            success: function(data) {
                $project.find('a.colorPie').attr('class', color + ' colorPie');
                $project.find('a > i.iconCheckBox').removeClass('iconCheckBox');
                $a.find('i').addClass('iconCheckBox');
            },
            error: function() {
                alert('Error while update color of project, please try again.');
            }
        });

        $project.find('.sub-item.open').removeClass('open');

        return false;
    });

    $centerPanel.on('change', '.taskItem input[type="checkbox"]', function(e) {
        var $input = $(e.target);
        var $taskItem = $input.closest('.taskItem');
        var taskId = $taskItem.data('taskid');
        var isCompleted = this.checked;
        //
        taApp.setTaskComplete(taskId, isCompleted);
    });

    // Table Project Collapse
     $centerPanel.on('click', '.table-project-collapse .toggle-collapse-group' ,function() {
        if($(this).parents('.heading').next('.collapse-content').is(':visible')) {
            $(this).parents('.heading').next('.collapse-content').slideUp(200);
            $(this).find('.uiIcon').attr('class','uiIcon uiIconArrowRight');
        }
        else {
            $(this).parents('.heading').next('.collapse-content').slideDown(500);
            $(this).find('.uiIcon').attr('class','uiIcon uiIconArrowDown');
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
            error : function(jqXHR, textStatus, errorThrown) {
                console.error && console.error('update failure: ' + jqXHR.responseText);
            }
        });

        return true;
    });
  });
});