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
          var input = '<li><div class="noLabel center muted"><input type="text" class="addLabelInput"/></div></li>'
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
        var input = '<li><div class="noLabel center muted"><input type="text" class="addLabelInput"/></div></li>';
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

      //delete label
      $leftPanel.on('click', '.delete-label', function(e) {
        var $lblItem = $(e.target).closest('.label-item');
        var id = $lblItem.data('labelid');
        deleteLabel(id, reloadLabel);
      });

      //open edit label dialog
      $leftPanel.on('click', '.list-labels .openEditDialog', function(e) {
        var labelId = $(e.target).closest('.label-item').data('labelid');
        //
        $modalPlace.jzLoad('LabelController.openEditLabelDialog()', {'labelId': labelId}, function() {
          var $dialog = $('.edit-label-dialog');
          $dialog.modal({'backdrop': false});
        });
      });
      //update label
      $modalPlace.on('click', '.edit-label-dialog .saveLabel', function(e) {
        var $dialog = $(e.target).closest('.edit-label-dialog');
        var labelId = $dialog.data('id');
        var parentId = $dialog.find('.lblParent').val();
        var lblName = $dialog.find('.lblName').val();
        if (lblName.trim().length) {
          updateLabel(labelId, parentId, lblName, reloadLabel);
        }
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
        var id = $lblItem.data('labelid');
        //
        requestController({'labelId': id}, 'LabelController.toggleHidden', reloadLabel);
      });
      //show hidden label
      $leftPanel.on('click', '.actionShowHiddenLabel', function(e) {
        requestController({}, 'LabelController.toggleShowHiddenLabel', function() {
          var $showHidden = $(e.target).closest('*[data-showhiddenlabel]');
          var isShown = $showHidden.attr('data-showhiddenlabel') == 'true';
          $showHidden.attr('data-showhiddenlabel', isShown ? 'false' : 'true');
//          alert($showHidden.data('showhiddenlabel'));
        });
      });
      
      //show tasks of label
      $leftPanel.on('click', 'a.label-name', function(e) {        
        var $a = $(e.target);
        var labelId = $a.data('labelid');
        taApp.reloadTaskList(-5, labelId, null, function() {
          labelLoaded($a);
          taApp.hideRightPanel($centerPanel, $rightPanel, $rightPanelContent);
        });
      });

      function labelLoaded($a) {
        $a.closest('.leftPanel > ul').find('li.active').removeClass('active');
        $a.closest('li').addClass('active');

        //welcome
        if ($a.data('labelid') == '0' && $leftPanel.find('.label-item').length == 0) {
          var $addLabel = $taskManagement.find('.add-new-label');
          taApp.showOneTimePopover($addLabel);
        }
        
        var $inputTask = $centerPanelContent.find('input[name="taskTitle"]');
        taApp.showOneTimePopover($inputTask);
        $inputTask.focus();
      }

      function reloadLabel() {
        var $listLabels = $leftPanel.find('.list-labels');
        $listLabels.parent().jzLoad("LabelController.getAllLabels()");
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
            error: function() {
                alert('error requesting label controller. Please try again.');
            }
        });
      }
  });
});