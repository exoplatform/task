define('project-menu', ['SHARED/jquery'], function($) {
  var pMenu = {};
  
  pMenu.init = function(taApp) {
    var ui = taApp.getUI();
    var $leftPanel = ui.$leftPanel;
    var $centerPanel = ui.$centerPanel;
    var $rightPanel = ui.$rightPanel;
    var $rightPanelContent = ui.$rightPanelContent;    
    var $cloneProject = $('.confirmCloneProject');
    
    //begin clone-project
    $leftPanel.on('click', 'a.clone-project', function(e) {
      var pId = $(e.target).closest('a').attr('data-projectId');
      var projectName = $(this).closest('.project-item').find('.project-name').html();

      //
      showCloneProject(pId, projectName);
    });
    
    function showCloneProject(pId, projectName) {
      $cloneProject.find('.pId').val(pId);
      var msg = $cloneProject.find('.msg');
      msg.html(msg.attr('data-msg').replace('{}', projectName));
      $cloneProject.modal({'backdrop': false});
    }
    
    $cloneProject.find('.btn-primary').click(function(e) {
      var pId = $cloneProject.find('.pId').val();
      var cloneTask = $cloneProject.find('.cloneTask').is(':checked');
        
        var cloneURL = $rightPanel.jzURL('ProjectController.cloneProject');
        $.ajax({
            type: 'POST',
            url: cloneURL,
            data: {'id': pId, 'cloneTask': cloneTask},
            success: function(data) {
                window.location.reload();
            },
            error: function() {
                alert('error while create new project. Please try again.')
            }
        });
    });
    
    $rightPanel.on('click', '.projectDetail .action-clone-project', function() {
      var $detail = $(this).closest('.projectDetail');
      var pId = $detail.attr('data-projectId');
      var projectName = $detail.find('.projectName').html();
      
      showCloneProject(pId, projectName);
    });    
    //end clone-project

    $leftPanel.on('click', 'a.new-project', function(e) {
      var parentId = $(e.target).closest('a').attr('data-projectId');
      $rightPanelContent.jzLoad('ProjectController.projectForm()', {parentId: parentId}, function() {
        taApp.showRightPanel($centerPanel, $rightPanel);
      });
      return true;
    });    
   
    $rightPanel.on('click', 'a.action-delete-project', function(e) {
      var $projectDetail = $(e.target).closest('.projectDetail');
      var projectId = $projectDetail.attr('data-projectId');
      var deleteProjectURL = $projectDetail.jzURL('ProjectController.deleteProject');
      var projectName = $projectDetail.find('a.editable[data-name="name"]').html();
      var confirmed = confirm('Are you sure you want to delete project: ' + projectName);
      if (confirmed) {
        $.ajax({
          type: 'POST',
          url: deleteProjectURL,
          data: {projectId: projectId},
          success: function (resp) {
            $projectDetail.remove();
            taApp.hideRightPanel($centerPanel, $rightPanel, $rightPanelContent);
            $leftPanel
            .find('li.project-item a.project-name[data-id="' + projectId + '"]')
            .closest('li.project-item').remove();
          },
          error: function () {
            alert('Delete project failure, please try again.');
          }
        });
      }
      return true;
    });    
    
    //begin share-project        
    var $modalPlace = $('.modalPlace');
    function openShareDialog(pid) {
      $modalPlace.jzLoad('ProjectController.openShareDialog()', {'id': pid}, function() {
        var $dialog = $('.sharePrjDialog');
        $dialog.modal({'backdrop': false});
      });
    }
    
    $leftPanel.on('click', 'a.share-project', function(e) {
      var pid = $(e.target).closest('.project-menu').attr('data-projectId');
      openShareDialog(pid); 
    });

    $modalPlace.on('click', '.sharePrjDialog .remove', function(e) {
      var $remove = $(e.target);
      var pid = $remove.closest('.sharePrjDialog').attr('data-projectId');
      var type = $remove.closest('.manager').length != 0 ? 'manager' : 'participant';
      var permission = $remove.parent().find('.permission').html();
      
      //
      $modalPlace.jzLoad('ProjectController.removePermission()', {'id': pid, 'type': type, 'permission': permission}, function() {
        var $dialog = $('.sharePrjDialog');
        $dialog.modal({'backdrop': false});
      });
    });
    
    $modalPlace.on('click', '.sharePrjDialog .openUserSelector', function(e) {
      var $openUserSelector = $(e.target);
      var pid = $openUserSelector.closest('.sharePrjDialog').attr('data-projectId');
      var type = $openUserSelector.closest('.manager').length != 0 ? 'manager' : 'participant';
      
      $('.sharePrjDialog .selectorDialog').jzLoad('ProjectController.openUserSelector()', {'id': pid, 'type': type}, function() {
        var $dialog = $('.userSelectorDialog');
        $dialog.modal({'backdrop': false});
      });
    });
    
    $modalPlace.on('click', '.sharePrjDialog .openGroupSelector', function(e) {
      var $openGroupSelector = $(e.target);
      var pid = $openGroupSelector.closest('.sharePrjDialog').attr('data-projectId');
      var type = $openGroupSelector.closest('.manager').length != 0 ? 'manager' : 'participant';
      
      $('.sharePrjDialog .selectorDialog').jzLoad('ProjectController.openGroupSelector()', {'id': pid, 'type': type}, function() {
        var $dialog = $('.groupSelectorDialog');
        $dialog.find('.grp li').first().addClass('selected');
        $dialog.modal({'backdrop': false});
      });
    });    
    
    $modalPlace.on('click', '.userSelectorDialog .addUser', function(e) {
      var $add = $(e.target);
      var pid = $add.closest('.sharePrjDialog').attr('data-projectId');
      var type = $add.closest('.userSelectorDialog').attr('data-type');
      var users = [];
      $('.userSelectorDialog .chkUser:checked').each(function(idx, elem) {
        users.push($(elem).val());
      });
      
      //
      if (users.length == 0) {
        alert('no user selected');
      } else {
        addPermission(pid, users.join(), type);
      }
    });
    
    $modalPlace.on('click', '.groupSelectorDialog .msItem', function(e) {
      var $msItem = $(e.target);
      var pid = $msItem.closest('.sharePrjDialog').attr('data-projectId');
      var $groupDialog = $msItem.closest('.groupSelectorDialog'); 
      var type = $groupDialog.attr('data-type');
      //
      var group = $groupDialog.find('.grp .selected').attr('data-groupId');
      var msType = $msItem.attr('data-msType');
      
      //
      addPermission(pid, msType + ":" + group, type);
    });
    
    $modalPlace.on('click', '.groupSelectorDialog .grpItem', function(e) {
      var $grpItem = $(e.target);
      $grpItem.parent().find('.selected').removeClass('selected');
      $grpItem.addClass('selected');
    });
    
    $modalPlace.on('click', '.sharePrjDialog .close', function(e) {
      var $close = $(e.target);
      $close.closest('.modal').remove();
    });
    
    function addPermission(pid, permissions, type) {
      $modalPlace.jzLoad('ProjectController.addPermission()', {'id': pid, 'permissions': permissions, 'type': type}, function() {
        var $dialog = $('.sharePrjDialog');
        $dialog.modal({'backdrop': false});
      });
    }
    
    //end share-project
  }
  
  return pMenu;
});