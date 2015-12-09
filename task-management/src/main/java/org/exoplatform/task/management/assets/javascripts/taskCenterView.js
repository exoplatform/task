define('taskCenterView', ['SHARED/jquery', 'taskManagementApp', 'SHARED/bootstrap_tooltip','SHARED/bootstrap_popover'], function($, taApp) {
    var centerView = {};
    centerView.init = function() {
        taApp.onReady(function($) {
            centerView.initDomEvent();
        });
    };

    centerView.initDomEvent = function() {
        var ui = taApp.getUI();
        var $centerPanel = ui.$centerPanel;
        var $centerPanelContent = ui.$centerPanelContent;

        $centerPanel.find('[rel="tooltip"]').tooltip();

        $centerPanel.off('click', '[data-taskcompleted]').on('click', '[data-taskcompleted]', function(e) {
            e.stopPropagation();
            var $a = $(e.target).closest('[data-taskcompleted]');
            var $taskItem = $a.closest('.taskItem');
            var taskId = $taskItem.data('taskid');
            var isCompleted = $a.data('taskcompleted');
            //
            taApp.setTaskComplete(taskId, !isCompleted);
        });

        var $permalink = $centerPanelContent.find('.projectPermalink');
        var link = $permalink[0].href;
        $centerPanelContent.find('.projectPermalinkPopoverContent input').attr("value", link);
        $permalink.popover({
            html: true,
            content: $centerPanelContent.find('.projectPermalinkPopoverContent').html()
        }).on('shown', function(e) {
            $(e.target).closest('.projectPermalinkContainer').find('.popover-content input').select();
        });
        $(document).on('click', function(e) {
            if ($(e.target).closest('.projectPermalinkContainer').length > 0) {
                e.stopPropagation();
                return false;
            } else {
                $permalink.popover('hide');
            }
        });
    };

    centerView.initTaskLabel = function() {
      var ui = taApp.getUI();
      var $centerPanel = ui.$centerPanel;
      var taskLabelUrl = $centerPanel.jzURL('TaskController.findLabelByTask');
      $centerPanel.find('.taskItem').each(function(idx, elem) {
        var $taskItem = $(elem);
        var taskId = $taskItem.data('taskid');
        //
        var labels = '';
        $.get(taskLabelUrl, { taskId: taskId}, function(data) {
          $.each(data, function(i, lbl) {
            labels += '<span class="labels">';
            labels += '<a href="#" class="' + lbl.color + ' label">' + taApp.escape(lbl.name) + '</a>';
            labels += '</span>';
          });

          $(labels).insertAfter($taskItem.find('.text-time'));
        },'json');
      });
      
    };

    centerView.submitFilter = function(e) {
        var ui = taApp.getUI();
        var $centerPanel = ui.$centerPanel;
        var $centerPanelContent = ui.$centerPanelContent;
        var $rightPanel = ui.$rightPanel;
        var $rightPanelContent = ui.$rightPanelContent;

        var $projectListView =  $centerPanel.find('.projectListView');
        var projectId = $projectListView.attr('data-projectid');
        var labelId = $projectListView.attr('data-labelid');
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
        var viewType = $projectListView.find('[name="viewType"]').val();
        if (viewType == undefined) {
            viewType = 'list';
        }
        var page = $projectListView.find('[name="page"]').val();
        if (page == undefined) {
            page = 1;
        }

        var keyword = $projectListView.closest('.projectListView').find('input[name="keyword"]').val();
        $centerPanelContent.jzLoad('TaskController.listTasks()',
            {
                projectId: projectId,
                labelId : labelId,
                keyword: keyword,
                groupBy: groupBy,
                orderBy: orderBy,
                filter: filter,
                viewType: viewType,
                page: page
            },
            function(html, status, xhr) {
                if (xhr.status >= 400) {
                    taApp.showWarningDialog(xhr.responseText);
                } else {
                    taApp.hideRightPanel($centerPanel, $rightPanel, $rightPanelContent);
                }
            }
        );
    };

    return centerView;
});
