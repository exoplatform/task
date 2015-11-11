define('taskCenterView', ['SHARED/jquery', 'taskManagementApp'], function($, taApp) {
    var centerView = {};
    centerView.init = function() {
        taApp.onReady(function($) {
            //
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
            labels += '<span class="inline-block-hide labels">';
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
