define('project-menu', ['SHARED/jquery'], function($) {
  var pMenu = {};
  
  pMenu.init = function(taApp) {
    pMenu.taApp = taApp;
    var ui = taApp.getUI();
    var $leftPanel = ui.$leftPanel;
    var $centerPanel = ui.$centerPanel;
    var $rightPanel = ui.$rightPanel;
    var $rightPanelContent = ui.$rightPanelContent;    
    var $cloneProject = $('.confirmCloneProject');
    var $modalPlace = $('.modalPlace');
    
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
                alert('error while clone project. Please try again.')
            }
        });
    });
    
    $rightPanel.on('click', '.projectDetail .action-clone-project', function() {
      var $detail = $(this).closest('[data-projectid]');
      var pId = $detail.attr('data-projectId');
      var projectName = $detail.find('.projectName').html();
      
      showCloneProject(pId, projectName);
    });    
    //end clone-project

    $leftPanel.on('click', 'a.new-project', function(e) {
      var parentId = $(e.target).closest('a').attr('data-projectId');
      $rightPanelContent.jzLoad('ProjectController.projectForm()', {parentId: parentId}, function() {
        taApp.showRightPanel($centerPanel, $rightPanel);
        $rightPanel.find('[name="name"]').on('blur', function(e) {
            $(e.target || e.srcElement).closest('form').submit();
        });
        var $ancestors = $rightPanel.find('.editable');
        $ancestors.editable({
          mode : 'inline',
          showbuttons: false
        });
      });
      return true;
    });        
   
    $leftPanel.on('click', '.delete-project', function(e) {
      var $deleteBtn = $(e.target);
      var pid = $deleteBtn.closest('.project-menu').attr('data-projectId');
      taApp.showDialog('ProjectController.openConfirmDelete()', {id : pid});
    });
    
    $rightPanel.on('click', 'a.action-delete-project', function(e) {
      var $projectDetail = $(e.target).closest('[data-projectid]');
      var projectId = $projectDetail.attr('data-projectId');
      taApp.showDialog('ProjectController.openConfirmDelete()', {id : projectId});
    });    
    
    //begin share-project            
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

    $modalPlace.on('click', '.actionEditPermission, .actionCloseEditPermission', function(e) {
        var $a = $(e.target || e.srcElement).closest('a');
        var $td = $a.closest('td');
        $td.find('.permission-display').toggleClass('hidden');
        $td.find('.action-mentions').toggleClass('hidden');
    });

    $modalPlace.on('click', '.replaceTextArea', function(e) {
        var $replaceTextArea = $(e.target).closest('.replaceTextArea');
        $replaceTextArea.find('.cursorText input').focus();
    });

    $modalPlace.on('keydown keyup', '.replaceTextArea .cursorText input', function(e) {
        var $input = $(e.target);
        var $replaceTextArea = $input.closest('.replaceTextArea');
        var $td = $input.closest('td');
        var maxWidth = $replaceTextArea.width();
        var tdWidth = $td.width();

        $td.css('width', tdWidth + 'px')

        var val = $input.val();
        var width = (val.length + 1) * 8;
        if (width <= 0) {
            width = 3;
        } else if (width > maxWidth) {
            width = maxWidth;
        }
        $input.css('width', width + 'px');
    });

    var permissionTimeout = false;
    var currentVal = '';
    $modalPlace.on('keyup', '.replaceTextArea .cursorText input', function(e) {
        var $input = $(e.target);
        var $autoCompleted = $input.closest('.exo-mentions').find('.autocomplete-menu');

        var val = $.trim($input.val());
        if (currentVal != val && val != '') {
            currentVal = val;

            if (permissionTimeout) {
                clearTimeout(permissionTimeout);
            }
            permissionTimeout = setTimeout(function() {
                $input.jzAjax('ProjectController.findPermission()', {
                    method: 'POST',
                    data: {keyword: currentVal},
                    success: function(response) {
                        $autoCompleted.html(response);
                        $autoCompleted.show();
                    }
                });
            }, 500);
        }

        if ($autoCompleted.is(':visible')) {
            var code = e.keyCode || e.which;
            if (code == 13) { // Enter
                $autoCompleted.find('[data-suggest-permission].active').click();
                $input.val('');
                currentVal = '';
            } else if (code == 38 || code == 40) {
                var isUp = (code == 38);
                var $li = $autoCompleted.find('[data-suggest-permission].active');
                $li.removeClass('active');
                if (isUp) {
                    var $prev = $li.prev();
                    if ($prev.length == 0) {
                        $prev = $autoCompleted.find('[data-suggest-permission]').last();
                    }
                    $prev.addClass('active');
                } else {
                    var $next = $li.next();
                    if ($next.length == 0) {
                        $next = $autoCompleted.find('[data-suggest-permission]').first();
                    }
                    $next.addClass('active');
                }
            }
        }
    });

    $modalPlace.on('blur', '.replaceTextArea .cursorText input', function(e) {
        var $input = $(e.target);
        var $suggestion = $input.closest('.exo-mentions').find('.autocomplete-menu');
        setTimeout(function() {
            $suggestion.hide();
        }, 200);
    });

    $modalPlace.on('click', '[data-suggest-permission]', function(e) {
        var $li = $(e.target).closest('[data-suggest-permission]');
        var $autoCompleted = $li.closest('.exo-mentions').find('.autocomplete-menu');
        var $input = $li.closest('.exo-mentions').find('.cursorText input');
        if ($li.length > 0) {
            var fieldType = $autoCompleted.closest('[data-type]').data('type');
            var type = $li.data('suggest-type');
            var permission = $li.data('suggest-permission');
            if (type == 1) {
                // User permission
                var displayName = $li.data('suggest-displayname');
                addUser(fieldType, permission, displayName);
            } else {
                var msType = $li.data('suggest-mstype');
                var groupId = $li.data('suggest-groupid');
                var groupName = $li.data('suggest-groupname');
                addMembership(fieldType, msType, groupId, groupName);
            }
        }
        $input.val('');
        currentVal = '';
        $autoCompleted.hide();
    });

    $modalPlace.on('click', '.sharePrjDialog .removePermission', function(e) {
      var $remove = $(e.target);
      var pid = $remove.closest('.sharePrjDialog').attr('data-projectId');
      var type = $remove.closest('[data-type]').data('type');
      $remove.closest('.replaceTextArea').click();
      $remove.closest('[data-permission]').remove();
    });

    $modalPlace.on('click', '.groupMembership [data-mstype]', function(e) {
        var $a = $(e.target).closest('[data-mstype]');
        var msType = $a.data('mstype');
        var msTypeName = $a.html();
        var $permission = $a.closest('[data-permission]');
        var oldPermission = $permission.data('permission');
        var perms = oldPermission.split(':');
        var newPermission = msType + ":" + perms[1];
        $permission.attr('data-permission', newPermission).data('permission', newPermission);
        $permission.find('.dropdown-toggle').html(msTypeName + ' <i  class="uiIconArrowDownMini uiIconLightGray"></i>');
    });
    
    $modalPlace.on('click', '.sharePrjDialog .openUserSelector', function(e) {
      var $openUserSelector = $(e.target);
      var pid = $openUserSelector.closest('.sharePrjDialog').attr('data-projectId');
      var type = $openUserSelector.closest('[data-type]').data('type');
      
      $('.sharePrjDialog .selectorDialog').jzLoad('ProjectController.openUserSelector()', {'id': pid, 'type': type}, function() {
        var $dialog = $('.userSelectorDialog');
        $dialog.modal({'backdrop': false});
      });
    });
    
    $modalPlace.on('click', '.sharePrjDialog .openGroupSelector', function(e) {
      var $openGroupSelector = $(e.target);
      var pid = $openGroupSelector.closest('.sharePrjDialog').attr('data-projectId');
      var type = $openGroupSelector.closest('[data-type]').data('type');
      
      $('.sharePrjDialog .selectorDialog').jzLoad('ProjectController.openGroupSelector()', {'type': type}, function() {
        var $dialog = $('.groupSelectorDialog');
        $dialog.find('.treeContainer li').first().addClass('selected');
        $dialog.modal({'backdrop': false});
      });
    });

    $modalPlace.on('click', '.sharePrjDialog .openGroupSelectorForSearch', function(e) {
      var $openGroupSelector = $(e.target);
      //var type = $openGroupSelector.closest('[data-type]').data('type');
      var type = "searchUser";

      $('.sharePrjDialog .groupSelectorForSearchDialog').jzLoad('ProjectController.openGroupSelector()', {'type': type, onlyGroup: true}, function() {
          var $dialog = $('.groupSelectorForSearchDialog .groupSelectorDialog');
          //$dialog.find('.treeContainer li').first().addClass('selected');
          //. This is work around to fix: Uncaught RangeError: Maximum call stack size exceeded
          var old = $.fn.modal.Constructor.prototype.enforceFocus;
          $.fn.modal.Constructor.prototype.enforceFocus = function () {};

          $dialog.modal({'backdrop': false});

          $.fn.modal.Constructor.prototype.enforceFocus = old;
      });
    });

    $modalPlace.on('change', '.userSelectorDialog input[name="selectall"]', function(e) {
        var $checkbox = $(e.target);
        var isChecked = $checkbox.is(':checked');
        $checkbox.closest('.userSelectorDialog').find('input[name="username"]').attr('checked', isChecked);
    });

    $modalPlace.on('change', '.userSelectorDialog input[name="username"]', function(e) {
      var $checkbox = $(e.target);
      var $container = $checkbox.closest('.userSelectorDialog');
      var $unchecked = $container.find('input[name="username"]:not(:checked)');
      var isCheckall = $unchecked.length == 0;
      $container.find('input[name="selectall"]').attr('checked', isCheckall);
    });

    $modalPlace.on('click', '.userSelectorDialog .addUser', function(e) {
      var $add = $(e.target);
      var pid = $add.closest('.sharePrjDialog').attr('data-projectId');
      var type = $add.closest('.userSelectorDialog').attr('data-type');
      var users = [];
      $('.userSelectorDialog .chkUser:checked').each(function(idx, elem) {
          var $el = $(elem);
          var username = $el.val();
          var displayName = $el.data('dispayname');
            users.push({username: username, displayName: displayName});
      });
      
      //
      if (users.length == 0) {
        alert('no user selected');
      } else {
          $.each(users, function(index, user) {
              addUser(type, user.username, user.displayName);
          });
          $add.closest('.modal').remove();
      }
    });

    $modalPlace.on('click', '.userSelectorDialog form.formSearchUser a[type="submit"]', function(e) {
        $(e.target).closest('form').submit();
    });

    $modalPlace.on('submit', '.userSelectorDialog form.formSearchUser', function(e) {
        // Submit form search user
        var $form = $(e.target);
        var keyword = $form.find('[name="keyword"]').val();
        var groupId = $form.find('[name="group"]').val();
        var filter = $form.find('[name="filter"]').val();

        var pid = $form.closest('.sharePrjDialog').attr('data-projectId');
        var type = $form.closest('[data-type]').data('type');

        $('.sharePrjDialog .selectorDialog').jzLoad('ProjectController.openUserSelector()',
            {'id': pid, 'type': type, groupId: groupId, keyword: keyword, filter: filter},
            function() {
                var $dialog = $('.userSelectorDialog');
                $dialog.modal({'backdrop': false});
            }
        );

        return false;
    });
    
    $modalPlace.on('click', '.groupSelectorDialog [data-membershiptype]', function(e) {
      var $msItem = $(e.target);
      var pid = $msItem.closest('.sharePrjDialog').attr('data-projectId');
      var $groupDialog = $msItem.closest('.groupSelectorDialog'); 
      var type = $groupDialog.attr('data-type');
      //
      var $group = $groupDialog.find('[data-groupid].nodeSelected');
      if ($group.length == 0) {
          alert('You have to select group first');
          return;
      }
      var group = $group.data('groupid');
      var groupName = $group.data('displayname');
      var msType = $msItem.data('membershiptype');

      //TODO: need improve this code, it's so messy
      if(msType != '') {
          addMembership(type, msType, group, groupName);
      } else {
          $modalPlace.find('input[name="group"]').val(group);
      }
      $msItem.closest('.modal').remove();
    });

    $modalPlace.on('click', '[data-groupid]', function(e) {
        var $group = $(e.target || e.srcElement).closest('[data-groupid]');
        var $treeContainer = $group.closest('.treeContainer');
        $treeContainer.find('.nodeSelected').removeClass('nodeSelected');
        $group.addClass('nodeSelected');

        if ($group.closest('.explained').length == 0) {
            $treeContainer.find('.explained').removeClass('explained').addClass('collapsed');
            $treeContainer.find('.expandIcon').removeClass('expandIcon').addClass('collapseIcon');
        }

        //. Explained
        $group.parents('li.collapsed')
            .removeClass('collapsed').addClass('explained');
        if ($group.hasClass('collapseIcon')) {
            $group.removeClass('collapseIcon').addClass('expandIcon');
        }
    });
    
    $modalPlace.on('click', '.sharePrjDialog .close', function(e) {
      var $close = $(e.target);
      $close.closest('.modal').remove();
    });

    $modalPlace.on('click', '.sharePrjDialog .savePermission', function(e) {
        var $a = $(e.target);
        var $type = $a.closest('[data-type]');
        var type = $type.data('type');
        var permissions = [];
        var pid = $a.closest('[data-projectid]').data('projectid');
        $type.find('[data-permission]').each(function(index, ele) {
            var $el = $(ele);
            var perm = $el.data('permission');
            permissions.push(perm);
        });
        $modalPlace.jzAjax('ProjectController.savePermission()', {
                data: {'id': pid, 'permissions': permissions, 'type': type},
                method: 'POST',
                traditional: true,
                success: function(response) {
                    $modalPlace.html(response);
                    var $dialog = $('.sharePrjDialog');
                    $dialog.modal({'backdrop': false});
                },
                error: function(jqXHR, textStatus, errorThrown ) {
                    alert('save permission failure: ' + jqXHR.responseText);
                }
        });
    });

    function addUser(type, username, displayName) {
        var $container = $modalPlace.find('[data-type="'+type+'"] .replaceTextArea');
        var $existing = $container.find('[data-permission="'+username+'"]');
        if ($existing.length > 0) {
            return;
        }
        var html = '<span data-permission="'+username+'">'+displayName+'<a href="javascript:void(0)" class="removePermission"><i  class="uiIconClose uiIconLightGray"></i></a></span>';
        $(html).insertBefore($container.find('.cursorText'));
    };
    function addMembership(type, msType, groupId, groupName) {
        var $container = $modalPlace.find('[data-type="'+type+'"] .replaceTextArea');
        var $listMembershipType = $modalPlace.find('.listMembershipType');
        var permission = msType + ":" + groupId;
        var $existing = $container.find('[data-permission="'+permission+'"]');
        if ($existing.length > 0) {
            return;
        }
        var $item = $('<div/>');
        $item.addClass('groupMembership');
        $item.attr('data-permission', permission);
        $item.append('<span data-toggle="dropdown" class="dropdown-toggle">'+(msType == '*' ? 'Any' : msType)+' <i class="uiIconArrowDownMini uiIconLightGray"></i></span>');
        $item.append($listMembershipType.html());
        $item.append(' in ');
        $item.append('<span>'+groupName+' <a href="javascript:void(0)" class="removePermission"><i class="uiIconClose uiIconLightGray"></i></a></span>');

        $item.insertBefore($container.find('.cursorText'));
    }
    //end share-project
  }
  
  pMenu.initDeleteProjectDialog = function() {
    var $confirm = $('.confirmDeleteProject');
    var pid = $confirm.data('id');
    
    $confirm.on('click', '.confirmDelete', function(e) {
      var deleteProjectURL = $confirm.jzURL('ProjectController.deleteProject');
      var deleteChild = $confirm.find('.deleteChild').is(':checked');
      
      $.ajax({
        type: 'POST',
        url: deleteProjectURL,
        data: {projectId: pid, deleteChild : deleteChild},
        success: function () {
          pMenu.taApp.reloadProjectTree();
        },
        error: function () {
          alert('Delete project failure, please try again.');
        }
      });
    });
  }
  
  return pMenu;
});