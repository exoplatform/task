require(['jquery', 'taskManagementApp'], function($, taApp) {
  $(function() {
      var ui = taApp.getUI();
      var $leftPanel = ui.$leftPanel;
      var $centerPanel = ui.$centerPanel;
      var $rightPanel = ui.$rightPanel;
      var $taskManagement = ui.$taskManagement;
      var $rightPanelContent = ui.$rightPanelContent;
      var $centerPanelContent = ui.$centerPanelContent;
      
      var $modalPlace = $('.modalPlace');
      
      //show input to create label
      $leftPanel.on('click', '.new-label', function() {
        var $listLabels = $leftPanel.find('.list-labels');
        if (!$listLabels.find('input').length) {
          var input = '<li><div class="noLabel center muted form-add-label"><input type="text" class="addLabelInput"/></div></li>'
          $listLabels.html(input + $listLabels.html());
          $listLabels.find('input').focus();
        }
      });
      //create sub label
      $leftPanel.on('click', '.new-sub-label', function(e) {
        var $lblItem = $(e.target).closest('.label-item');
        var $subList = $lblItem.find('.list-labels');
        if (!$subList.length) {
          $subList = $('<ul class="list-labels" data-parentId="' + $lblItem.data('labelid') + '"></ul>');
          $lblItem.append($subList);
        }
        var input = '<li><div class="noLabel center muted form-add-label"><input type="text" class="addLabelInput"/></div></li>';
        $subList.html(input + $subList.html());
        $subList.find('input').focus();        
      });

      //submit to create label
      $leftPanel.on('blur keypress', '.addLabelInput', function(e) {
        var $input = $(e.target);

        //create label if press enter or loss focus
        if (!$input.is(':focus') || e.keyCode == 13) {
          var label = $input.val().trim();
          if (label) {
            var parentId = $input.closest('.list-labels').data('parentid');
            createLabel(label, parentId, reloadLabel);
          }

          //close input
          if ((e.keyCode == 13 && label) || e.keyCode == 27 || !$input.is(':focus')) {
            $input.closest('li').remove();
          }
        }
      });

      //delete label dialog      
      $leftPanel.on('click', '.delete-label', function(e) {
        var labelId = $(e.target).closest('.label-item').data('labelid');
        //
        $modalPlace.jzLoad('LabelController.openConfirmDeleteLabelDialog()', {'labelId': labelId}, function() {
          var $dialog = $('.confirmDeleteLabel');
          $dialog.modal({'backdrop': false});
          //
          $dialog.on('click', '.confirmDelete', function() {
            //confirm delete
            var $lblItem = $(e.target).closest('.label-item');        
            var $active = $lblItem.closest('.accordion-heading').find('.active');
            //choose next active label item 
            var $next = $active;
            if ($next.data('labelid') == $lblItem.data('labelid')) {
              $next = $lblItem.prev('li').find('.label-name');          
            }
            if (!$next.length) {
              $next = $lblItem.next('li').find('.label-name');          
            }
            if (!$next.length) {
              $next = $('.label-name[data-labelid="0"]');
            }
            $next = $next.data('labelid');
            
            //delete current label
            var id = $lblItem.data('labelid');        
            deleteLabel(id, function() {
              reloadLabel($active.length ? $next : "");
            });            
          });
        });        
      });

      //open edit label dialog
      $leftPanel.on('click', '.list-labels .openEditDialog', function(e) {
        var labelId = $(e.target).closest('.label-item').data('labelid');
        //
        $modalPlace.jzLoad('LabelController.openEditLabelDialog()', {'labelId': labelId}, function() {
          var $dialog = $('.edit-label-dialog');
          $dialog.modal({'backdrop': false});
          
          var $save = $dialog.find('.saveLabel');
          var $select = $dialog.find('select'); 
          var $text = $dialog.find('input');
          var parentId = $select.val();
          var name = $text.val();
          //
          $select.on('change', function() {
            if ($select.val() != parentId) {
              $save.attr('disabled', false);              
            }
          });
          $text.on('keyup', function() {
            if ($.trim($text.val()) != name) {
              $save.attr('disabled', false);
            }
          });
          
        });
      });
      //update label
      $modalPlace.on('click', '.edit-label-dialog .saveLabel', function(e) {
        var $dialog = $(e.target).closest('.edit-label-dialog');
        var labelId = $dialog.data('id');
        var parentId = $dialog.find('.lblParent').val();
        var lblName = $.trim($dialog.find('.lblName').val());
        lblName = lblName.length ? lblName : "Untitled Label";
        
        updateLabel(labelId, parentId, lblName, reloadLabel);
      });
      //change color
      $leftPanel.on('click', '.changeLabelColor', function(e) {        
        var $colorCell= $(e.currentTarget);
        var color = $colorCell.data('color');
        var $lblItem = $colorCell.closest('.label-item');
        var labelId = $lblItem.data('labelid');
        //
        requestController({'labelId': labelId, 'color': color}, 'LabelController.changeColor', reloadLabel);
      });
      //toggle hidden
      $leftPanel.on('click', '.actionHideLabel', function(e) {
        var $lblItem = $(e.target).closest('.label-item');
        if ($lblItem.data('hiddenlabel')) {
          var parentId = $lblItem.closest('[data-parentid]').data('parentid');
          if (parentId != 0) {
            if ($('.label-item[data-labelid="' + parentId + '"]').data('hiddenlabel')) {
              alert('To show a sub-label, please show its parent first.');
              return;
            }
          }          
        }
        
        var id = $lblItem.data('labelid');        
        //
        requestController({'labelId': id}, 'LabelController.toggleHidden', function() {
          var next = "";
          if ($lblItem.hasClass('active') & !$lblItem.data('hiddenlabel') && $lblItem.closest('[data-showhiddenlabel]').attr('data-showhiddenlabel') == 'false') {
            next = 0;
          }
          reloadLabel(next);
        });
      });
      //show hidden label
      $leftPanel.on('click', '.actionShowHiddenLabel', function(e) {
        requestController({}, 'LabelController.toggleShowHiddenLabel', function() {
          var $showHidden = $(e.target).closest('*[data-showhiddenlabel]');
          var isShown = $showHidden.attr('data-showhiddenlabel') == 'true';
          $showHidden.attr('data-showhiddenlabel', isShown ? 'false' : 'true');
          //
          var $active = $('.label-name').closest('.accordion-heading').find('.active'); 
          if ($active.length && $active.data('hiddenlabel')) {
            $('.label-name[data-labelid="0"').click();
          }
        });
      });

      //show tasks of label
      $leftPanel.on('click', 'a.label-name', function(e) {
        var $a = $(e.target);
        var labelId = $a.data('labelid');
        reloadTaskList(labelId);
      });

      //listen to event of editing label of task
      $rightPanelContent.on('labelAdded', '[data-name="labels"]', function(e, labelId) {
        $rightPanelContent.data('edit_labelid', getActiveLabel());
      });      
      $rightPanelContent.on('labelRemoved', '[data-name="labels"]', function(e, labelId) {
        $rightPanelContent.data('edit_labelid', getActiveLabel());
      });
      $rightPanelContent.on('saveLabel', function() {
        var labelId = $rightPanelContent.data('edit_labelid'); 
        if (labelId !== undefined && labelId != -1) {
          reloadLabel(labelId);
        }
      });
      
      function getActiveLabel() {
        var $container = $('.label-name[data-labelid="0"]').closest('.accordion-heading');
        if ($container.hasClass('active')) {
          return 0;
        } else if ($container.find('.active').length) {
          return $container.find('.active').data('labelid');
        }
        return -1;
      }
      
      function labelLoaded(labelid) {
        $leftPanel.find('.active').removeClass('active');
        $('.label-name[data-labelid="' + labelid + '"]').closest('li').addClass('active');

        //welcome
        if (labelid == '0' && $leftPanel.find('.label-item').length == 0) {
          var $addLabel = $taskManagement.find('.add-new-label');
          taApp.showOneTimePopover($addLabel);
        }

        var $inputTask = $centerPanelContent.find('input[name="taskTitle"]');
        taApp.showOneTimePopover($inputTask);
        $inputTask.focus();
      }

      function reloadLabel(selectLabel) {
        var $listLabels = $leftPanel.find('.list-labels');
        var $container = $listLabels.first().closest('.accordion-heading');
        var $active = $container.hasClass('active') ? $container : $container.find('.active');
        if (!$.isNumeric(selectLabel)) {
          selectLabel = $active.find('.label-name').first().data('labelid');
        }
        $listLabels.parent().jzLoad("LabelController.getAllLabels()", {}, function() {
          if ($active.length) {
            var $task = $centerPanel.find('.taskItem.selected');
            var selectTask = $task.length ? $task.data('taskid') : 0;
            reloadTaskList(selectLabel, selectTask);
          }
        });
      }
      
      function reloadTaskList(labelId, taskId) {
        taApp.reloadTaskList(-5, labelId, null, function() {
          labelLoaded(labelId);
          var $task = taApp.getUI().$centerPanel.find('.taskItem[data-taskid="' + taskId + '"]'); 
          if ($task.length) {
            $task.click();
          } else {
            taApp.hideRightPanel($centerPanel, $rightPanel, $rightPanelContent);            
          }
        });
      }
      
      function updateLabel(labelId, parentId, lblName, callback) {
        requestController({'labelId': labelId, 'parentId': parentId, "lblName": lblName}, 
            'LabelController.updateLabel', callback);
      }

      function deleteLabel(labelId, callback) {
        requestController({'labelId': labelId}, 'LabelController.deleteLabel', callback);
      }

      function createLabel(label, parentId, callback) {
        requestController({'name': label, 'parentId': parentId}, 'LabelController.createLabel', callback);
      }
      
      function requestController(data, controller, success) {
        var url = taApp.getUI().$leftPanel.jzURL(controller);
        $.ajax({
            type: 'POST',
            url: url,
            data: data,
            success: function(data) {
                if (success) {
                  success.call(this, data);
                }
            },
            error: function(xhr) {
              taApp.showWarningDialog(xhr.responseText);
            }
        });
      }
  });
});