define('project-menu', ['SHARED/jquery', 'ta_edit_inline', 'SHARED/task_ck_editor'], function($, editinline) {
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
            error: function(xhr) {
              $cloneProject.modal('hide');
              taApp.showWarningDialog(xhr.responseText);
            }
        });
    });
    
    $modalPlace.on('click', '.action-clone-project', function() {
      var $detail = $(this).closest('[data-projectid]');
      var pId = $detail.attr('data-projectId');
      var projectName = $detail.find('.projectName').html();
      
      showCloneProject(pId, projectName);
    });    
    //end clone-project
    
    $leftPanel.on('click', 'a.new-project', function(e) {
      var parentId = $(e.target).closest('a').attr('data-projectId');
      
      $modalPlace.jzLoad('ProjectController.projectForm()', {parentId: parentId}, function(text, status, xhr) {
        var $dialog = $modalPlace.find('.addProject');
        if (xhr.status >= 400) {
          taApp.showWarningDialog(xhr.responseText);
        } else {
          $dialog.modal({'backdrop': false});
          
          $dialog.find('[name="name"]').on('keyup', function(e) {
            if (e.which == 13) {
              //don't submit form by enter keypress
              return false;
            } else {
              if ($.trim($(e.target).val()) != '') {
                $dialog.find('.btn-primary').attr('disabled', false);
              }
            }
          });
          
          $dialog.find('.calInteg').click(function() {
            $dialog.find('.btn-primary').attr('disabled', false);
          });
          
          var $ancestors = $dialog.find('.editable');
          $ancestors.editable({
            mode : 'inline',
            showbuttons: false
          }).on('save', function() {                  
            $dialog.find('.btn-primary').attr('disabled', false);
          });
          
          
          CKEDITOR.basePath = '/task-management/assets/org/exoplatform/task/management/assets/ckeditorCustom/';
          $dialog.find('textarea').ckeditor({
            customConfig: '/task-management/assets/org/exoplatform/task/management/assets/ckeditorCustom/config.js'
          });
          CKEDITOR.on('instanceReady', function(e) {
            $dialog.find('.cke').removeClass('cke');
          });
          
          CKEDITOR.instances.description.on('change', function(e) {
            $dialog.find('.btn-primary').attr('disabled', false);
          });          
        }
      });
      return true;
    });
    
    $modalPlace.on('submit', 'form.create-project-form', function(e) {
      var $dialog = $modalPlace.find('.addProject');
      var $form = $(e.target).closest('form');
      var $title = $form.find('input[name="name"]');
      var name = $title.val();
      var description = $form.find('textarea[name="description"]').val();
      var $breadcumbs = $dialog.find('.breadcrumb');
      var parentId = $breadcumbs.data('value');
      var calInteg = $dialog.find('.calInteg').is(':checked');

      if(name == '') {
          name = $title.attr('placeholder');
      }

      var createURL = $dialog.jzURL('ProjectController.createProject');        
      $.ajax({
          type: 'POST',
          url: createURL,
          data: {name: name, description: description, parentId: parentId, calInteg: calInteg},
          success: function(data) {
              // Reload project tree;
              taApp.reloadProjectTree(data.id);
          },
          error: function(xhr) {
            if (xhr.status >= 400) {
              taApp.showWarningDialog(xhr.responseText);
            } else {
              alert('error while create new project. Please try again.');              
            }
          }
      });
      $dialog.modal('hide');
      return false;
  });

    $leftPanel.on('click', '.edit-project', function(e) {
      var projectId = $(e.target).closest('.project-item').attr('data-projectId');
      
      $modalPlace.jzLoad('ProjectController.projectDetail()', {id: projectId}, function (html, status, xhr) {
        var $dialog = $modalPlace.find('.addProject');
        if (xhr.status >= 400) {
          taApp.showWarningDialog(xhr.responseText);
        } else {
          $dialog.modal({'backdrop': false});
          //
          if($modalPlace.find('[data-projectid]').data('canedit')) {
            editinline.initEditInlineForProject(projectId, $dialog);
            //
            $modalPlace.find('.calInteg').click(function() {
              $dialog.find('.btn-primary').attr('disabled', false);
            });
            //
            $modalPlace.find('.btn-primary').click(function() {
              saveProjectDetail();
              $dialog.modal('hide');
            });
          }          
        }
      });
    });
    
    var saveProjectDetail = function() {
      var params = {};
      params.pk = $modalPlace.find('[data-projectid]').data('projectid');
      params.parent = $modalPlace.find('[data-name="parent"]').data('editable').value;
      var $title = $modalPlace.find('[data-name="name"]').data('editable');      
      params.name = $title.value;
      if (!params.name) {
    	  params.name = $modalPlace.find('[data-name="name"]').text();
      }
      
      params.description = $modalPlace.find('[data-name="description"]').data('editable').value;
      params.calendarIntegrated = $modalPlace.find('[name="calendarIntegrated"]').is(':checked');
      
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
              $leftPanel
                .find('li.project-item a.project-name[data-id="'+ data.projectId +'"]').html(data.name);
              $centerPanel.find('[data-projectid="'+data.projectId+'"] .projectName').html(data.name);
              taApp.reloadProjectTree(data.projectId);
          },
          error: function(xhr, textStatus, errorThrown ) {
            d.reject('update failure: ' + xhr.responseText);
            taApp.showWarningDialog(xhr.responseText);
          }
      });
      return d.promise();
  };

    $leftPanel.on('click', '.delete-project', function(e) {
      var $deleteBtn = $(e.target);
      var pid = $deleteBtn.closest('.project-menu').attr('data-projectId');
      taApp.showDialog('ProjectController.openConfirmDelete()', {id : pid});
    });
    
    $modalPlace.on('click', 'a.action-delete-project', function(e) {
      var $projectDetail = $(e.target).closest('[data-projectid]');
      var projectId = $projectDetail.attr('data-projectId');
      taApp.showDialog('ProjectController.openConfirmDelete()', {id : projectId});
    });    
    
    //begin share-project            
    function openShareDialog(pid) {
      $modalPlace.jzLoad('ProjectController.openShareDialog()', {'id': pid}, function(html, status, xhr) {
        var $dialog = $('.sharePrjDialog');
        if (xhr.status >= 400) {
          taApp.showWarningDialog(xhr.responseText);
        } else {
          $dialog.modal({'backdrop': false});          
        }
      });
    }
    
    $leftPanel.on('click', 'a.share-project', function(e) {
      var pid = $(e.target).closest('.project-menu').attr('data-projectId');
      openShareDialog(pid); 
    });

    $modalPlace.on('click', '.actionEditPermission, .actionCloseEditPermission', function(e) {
        var $a = $(e.target || e.srcElement).closest('a');
        var $td = $a.closest('td');
        $td.find('.permission-display').toggleClass('hide');
        $td.find('.action-mentions').toggleClass('hide');
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
    
    $modalPlace.on('click', '.sharePrjDialog .uiPageIterator [data-page]', function() {
      var page = $(this).data('page');
      var $form = $('.userSelectorDialog form.formSearchUser');
      openUserSelector($form, page);
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
        $checkbox.closest('.userSelectorDialog').find('input[name="username"]').click();
    });

    $modalPlace.on('change', '.userSelectorDialog input[name="username"]', function(e) {
      var $checkbox = $(e.target);
      
      var users = $modalPlace.data('selected_users');
      users = users || {};
      if ($checkbox.is(':checked')) {
        users[$checkbox.val()] = $checkbox.data('dispayname');
      } else {
        users[$checkbox.val()] = null;
      }
      $modalPlace.data('selected_users', users);
      
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
      var selected = $modalPlace.data('selected_users');
      if (selected) {
        for (var uid in selected) {
          if (selected[uid]) {
            users.push({username: uid, displayName: selected[uid]});            
          }
        }
      }      
      
      //
      if (users.length == 0) {
        alert('no user selected');
      } else {
          $.each(users, function(index, user) {
              addUser(type, user.username, user.displayName);
          });
          $add.closest('.modal').remove();
      }
      $modalPlace.data('selected_users', null);
    });

    $modalPlace.on('click', '.userSelectorDialog form.formSearchUser a[type="submit"]', function(e) {
        $(e.target).closest('form').submit();
    });    
    
    var openUserSelector = function($form, page) {
      // Submit form search user      
      var keyword = $form.find('[name="keyword"]').val();
      var groupId = $form.find('[name="group"]').val();
      var filter = $form.find('[name="filter"]').val();        

      var pid = $form.closest('.sharePrjDialog').attr('data-projectId');
      var type = $form.closest('[data-type]').data('type');

      $('.sharePrjDialog .selectorDialog').jzLoad('ProjectController.openUserSelector()',
          {'id': pid, 'type': type, groupId: groupId, keyword: keyword, filter: filter, page: page},
          function() {
              var $dialog = $('.userSelectorDialog');
              $dialog.modal({'backdrop': false});
              var users = $modalPlace.data('selected_users');
              if (users) {
                for (var uid in users) {
                  if (users[uid]) {
                    $('.userSelectorDialog .chkUser[value="' + uid + '"]').click();                    
                  }
                }
              }
          }
      );
    }

    $modalPlace.on('submit', '.userSelectorDialog form.formSearchUser', function(e) {
        $modalPlace.data('selected_users', null);
        var $form = $(e.target);
        openUserSelector($form, 0);
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
                    if ($dialog.length) {
                      $dialog.modal({'backdrop': false});                      
                    }
                },
                error: function(xhr, textStatus, errorThrown ) {
                  taApp.showWarningDialog(xhr.responseText);
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
        error: function (xhr) {
          taApp.showWarningDialog(xhr.responseText);
        }
      });
    });
  }
  
  return pMenu;
});