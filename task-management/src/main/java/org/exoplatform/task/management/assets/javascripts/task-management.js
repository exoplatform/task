// TODO: Move juzu-ajax, mentionsPlugin module into task management project if need
require(['project-menu', 'x_editable_select3', 'SHARED/jquery', 'SHARED/edit_inline_js', 'SHARED/juzu-ajax', 'SHARED/mentionsPlugin', 'SHARED/bts_modal', 'x_editable_selectize'], function(pMenu, $) {
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
            $next.find('.viewTaskDetail').click();            
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
    
    //welcome    
    var $inputTask = $centerPanelContent.find('input[name="taskTitle"]');
    taApp.showOneTimePopover($inputTask);
    $inputTask.focus();
    
    $.fn.editableform.buttons = '<button type="submit" class="btn btn-primary editable-submit"><i class="uiIconTick icon-white"></i></button>'+
        '<button type="button" class="btn editable-cancel"><i class="uiIconClose"></i></button>';
    
    var saveTaskDetailFunction = function(params) {
        var currentTaskId = $rightPanelContent.find('[data-taskid]').data('taskid');
        if (currentTaskId == 0 || currentTaskId == undefined) {
            return;
        }
        var d = new $.Deferred;
        var data = params;
        params.pk = currentTaskId;
        data.taskId = currentTaskId;
        $('#taskDetailContainer').jzAjax('TaskController.saveTaskInfo()',{
            data: data,
            method: 'POST',
            traditional: true,
            success: function(response) {
                d.resolve();
            },
            error: function(jqXHR, textStatus, errorThrown ) {
                d.reject('update failure: ' + jqXHR.responseText);
            }
        });
        return d.promise();
    };
    
    var initWorkPlan = function(taskId) {
      var $workplan = $('.workPlan');
      
      var saveWorkPlan = function(plan) {
        var data = {
            pk : taskId,
            name : 'workPlan'
          };
        if (plan) {
          data.value = plan;
        }
        var callback = saveTaskDetailFunction(data);
        
        callback.done(function() {
          $centerPanel.find('.selected .viewTaskDetail').click();
        }).fail(function() {                
          alert('fail to update');
        });
      }
      
      var $removeWorkPlan = $workplan.find('.removeWorkPlan');
      $removeWorkPlan.click(function() {
        saveWorkPlan(null);
      });
      
      //
      var $duration = $workplan.find('.duration');
      var currStart = $workplan.data('startdate').split('-');
      var startDate = new Date(currStart[0], currStart[1] - 1, currStart[2], currStart[3], currStart[4]);
      var endDate = new Date();
      endDate.setTime(startDate.getTime() + parseFloat($workplan.data('duration')));
      //
      $duration.popover({
        html : true,
        content : $('.rangeCalendar').html()
      }).on('shown.bs.popover', function() { 
        //setup calendar
        $workplan.find('.popover .calendar').each(function() {
          var $calendar = $(this);
          var $popover = $calendar.closest('.popover');
          
          if ($calendar.hasClass('fromCalendar')) {
            $calendar.datepicker('setDate', startDate);
          } else {
            $calendar.datepicker('setDate', endDate);
          }
          $calendar.datepicker('clearDates');
          
          //
          $calendar.on('changeDate', function(e) {
            var dateFrom, dateTo;
            if ($calendar.hasClass('fromCalendar')) {
              dateFrom = $calendar.datepicker('getDate');
              dateTo = $calendar.parent().find('.toCalendar').datepicker('getDate');
            } else {
              dateFrom = $calendar.parent().find('.fromCalendar').datepicker('getDate');
              dateTo = $calendar.datepicker('getDate');
            }
            
            if (dateFrom && dateTo) {
              var allDay = $popover.find('input[name="allDay"]').is(':checked');
              var timeFrom = [0, 0], timeTo = [23, 59];
              if (!allDay) {
                var $selectors = $popover.find('.timeSelector');
                timeFrom = $selectors.first().editable('getValue').timeFrom.split(':');
                timeTo = $selectors.last().editable('getValue').timeTo.split(':');
              }
              
              var setTime = function(dt, time) {
                dt.setHours(time[0]);
                dt.setMinutes(time[1]);
                dt.setMilliseconds(0);
              }
              setTime(dateFrom, timeFrom);
              setTime(dateTo, timeTo);

              saveWorkPlan([dateFrom.getTime(), dateTo.getTime()]);
            }
          });
        });

        //
        var $allDay = $workplan.find('.popover input[type="checkbox"]');           
        $allDay.on('change', function(e) {
          var $chk = $(e.target);
          if ($chk.is(':checked')) {
            $workplan.find('.popover .timeSelector').hide();
          } else {
            $workplan.find('.popover .timeSelector').show();
          }
        });
        if (startDate.getHours() == 0 && startDate.getMinutes() == 0 &&
            endDate.getHours() == 23 && endDate.getMinutes() == 59) {          
          $allDay[0].checked = true;
          $allDay.trigger('change');
        }

        //setup timeSelector
        var tmp = new Date(0,0,0,0,0,0,0);
        var times = [], d = tmp.getDate();
        while (tmp.getDate() == d) {
          var h = tmp.getHours(), m = tmp.getMinutes();
          var t = (h < 10 ? '0' + h : h) + ':' + (m < 10 ? '0' + m : m);
          times.push({'value': t, 'text': t});
          tmp.setMinutes(tmp.getMinutes() + 30);
        }
        times.push({'value': '23:59', 'text': '23:59'});
        
        var options = {
            source : times,
            mode : 'inline',
            showbuttons : false
        }
        
        //default time
        var getIdx = function(date) {
          var idx = date.getHours() * 2;
          if (date.getMinutes() == 59) {
            idx += 2;
          } else if (date.getMinutes() == 30) {
            idx += 1;
          }
          return idx;
        }

        $workplan.find('.popover .timeSelector').each(function() {
          var $selector = $(this);
          if ($selector.data('name') == 'timeFrom') {
            options.value = times[getIdx(startDate)].value;
          } else {
            options.value = times[getIdx(endDate)].value;
          }
          $selector.editable(options);
        });
      });
    };
    
    var initDueDate = function() {
      var $dueDate = $('.editable[data-name="dueDate"]');
      $dueDate.on('shown', function(e) {
        var $content = $dueDate.parent().find('.popover .popover-content');
        if ($content.find('.calControl').length == 0) {
          $content.html($content.html() + "<div>" 
              + $dueDate.parent().find('.dueDateControl').html()
              + "</div>");
        }
      });
      
      $dueDate.parent().on('click', '.calControl', function(e) {
        var $btn = $(e.target);
        var next = new Date();
        if ($btn.hasClass('none')) {
          next = null;       
        } else if ($btn.hasClass('tomorrow')) {
          next.setDate(next.getDate() + 1);
        } else if ($btn.hasClass('nextWeek')) {
          next.setDate(next.getDate() + 7);
        }
        
        $dueDate.editable('setValue', next);
      });
    };

    var initEditInline = function(taskId) {
        //initWorkPlan(taskId);

        var $taskDetailContainer = $('#taskDetailContainer, [data-taskid]');
        $taskDetailContainer.find('.editable').each(function(){
            var $this = $(this);
            var dataType = $this.attr('data-type');
            var fieldName = $this.attr('data-name');
            var editOptions = {
                mode: 'inline',
                showbuttons: false,
                onblur: 'submit',
                emptyclass: 'muted',
                pk: taskId,
                url: saveTaskDetailFunction
            };

            if(dataType == 'textarea') {
                editOptions.emptytext = "Description";
            } else if (dataType == 'text') {
                editOptions.inputclass = 'blackLarge';
                editOptions.clear = false;
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
            if (fieldName == 'priority') {
              var priority = [];
              $.each($this.data('priority').split(','), function(idx, elem) {
                priority.push({'text': elem, 'value': elem});
              });
              //
              editOptions.source = priority;
            }
            if(fieldName == 'tags') {
                editOptions.emptytext = '<i class="uiIconHag" data-toggle="tooltip" title="Click to edit"></i> No Tags';
            }

            $this.editable(editOptions);
            initDueDate();
        });
    };

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
          initEditInline(taskId);
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
                $centerPanel.find('.taskItem[data-taskid="' + id + '"] .viewTaskDetail').click();      
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

    var saveProjectDetailFunction = function(params) {
        var d = new $.Deferred;
        var data = params;
        data.projectId = params.pk;
        $rightPanel.jzAjax('ProjectController.saveProjectInfo()',{
            data: data,
            method: 'POST',
            traditional: true,
            success: function(response) {
                d.resolve();
                //
                if (params.name == 'name') {
                    $leftPanel
                        .find('li.project-item a.project-name[data-id="'+ data.projectId +'"]')
                        .html(data.value);
                }
            },
            error: function(jqXHR, textStatus, errorThrown ) {
                d.reject('update failure: ' + jqXHR.responseText);
            }
        });
        return d.promise();
    };
    var initEditInlineForProject = function(projectId) {
        var $project = $rightPanel.find('[data-projectid]');
        $project.find('.editable').each(function(){
            var $this = $(this);
            var dataType = $this.attr('data-type');
            var fieldName = $this.attr('data-name');
            var editOptions = {
                mode: 'inline',
                onblur: 'submit',
                showbuttons: false,
                emptyclass: 'muted',
                pk: projectId,
                url: saveProjectDetailFunction
            };

            if(dataType == 'textarea') {
                editOptions.emptytext = "Description";
            } else if (dataType == 'text')  {
                editOptions.inputclass = 'blackLarge';
                editOptions.clear = false;
            }
            if(fieldName == 'manager' || fieldName == 'participator') {
                var findUserURL = $this.jzURL('UserController.findUser');
                var getDisplayNameURL = $this.jzURL('UserController.getDisplayNameOfUser');
                editOptions.showbuttons = true;
                editOptions.emptytext = (fieldName == 'manager' ? "No Manager" : "No Participator");
                editOptions.source = findUserURL;
                editOptions.select2= {
                    multiple: true,
                    allowClear: true,
                    placeholder: 'Select an user',
                    tokenSeparators:[","],
                    minimumInputLength: 1,
                    initSelection: function (element, callback) {
                        return $.get(getDisplayNameURL, { usernames: element.val() }, function (data) {
                            callback(data);
                        });
                    }
                };

                //. This is workaround for issue of xEditable: https://github.com/vitalets/x-editable/issues/431
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
            if(fieldName == 'dueDate') {
                editOptions.emptytext = "no Duedate";
                editOptions.mode = 'popup';
            }
            $this.editable(editOptions);

            $this.on('shown', function(e, editable) {
                $this.parent().removeClass('inactive').addClass('active');
            }).on('hidden', function(e, editable) {
                $this.parent().removeClass('active').addClass('inactive');
            });
        });
    };

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
                var $addProject = $taskManagement.find('.addProject');
                taApp.showOneTimePopover($addProject);
              }
              
              var $inputTask = $centerPanelContent.find('input[name="taskTitle"]');
              taApp.showOneTimePopover($inputTask);
              $inputTask.focus();
            });
        }
        // Show project summary at right panel
        if(projectId > 0 && $a.data('canedit') && (currentProject != projectId || $rightPanel.is(':hidden'))) {
            $rightPanelContent.jzLoad('ProjectController.projectDetail()', {id: projectId}, function () {
                $a.closest('ul.list-projects[parentid="0"]').find('li.active').removeClass('active');
                $a.closest('li').addClass('active');
                taApp.showRightPanel($centerPanel, $rightPanel);
                //TODO: check can edit to init editInline
                if($rightPanelContent.find('[data-projectid]').data('canedit')) {
                    initEditInlineForProject(projectId);
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