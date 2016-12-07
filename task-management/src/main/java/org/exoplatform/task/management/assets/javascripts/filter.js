define('taFilter', ['SHARED/jquery', 'SHARED/selectize2'], function($) {
  var taApp = null;

  var taFilter = {
      init: function(ta) {
        taApp = ta;
        var ui = taApp.getUI();
        var $centerContent = ui.$centerPanelContent;

        $centerContent.off('click.toggleFilter').on('click.toggleFilter', '.toggleFilter', function(e) {
          var $icon = $(e.target).find('.uiIconFilter');
          var $rightContent = ui.$rightPanelContent; 

          $rightContent.jzAjax('FilterController.toggleFilter()', {
            data: taFilter.getFilterData(),

            success: function(response) {
              $rightContent.html(response);

              if ($rightContent.find('.taskFilter').length) {
                $icon.addClass('uiIconBlue');                
                taApp.showRightPanel(ui.$centerPanel, ui.$rightPanel);
                taFilter.initSearchForm();
                taFilter.submitFilter();
              } else {
                $icon.removeClass('uiIconBlue');
                taFilter.removeFilter();
              }
            }
          });
        });
        
        ui.$rightPanelContent.off('click.hideFilter').on('click.hideFilter', '.hideFilter', taFilter.hide);
      },
      
      hide: function(e) {        
        var ui = taApp.getUI();
        taApp.hideRightPanel(ui.$centerPanel, ui.$rightPanel, ui.$rightPanelContent);
      },

      isEnable: function() {
        return taApp.getUI().$centerPanelContent.find('.uiIconFilter').hasClass('uiIconBlue');
      },

      initSearchForm: function() {
        var $filter = taApp.getUI().$rightPanelContent.find('.taskFilter');
        
        //contains
        var timeout = null;        
        $filter.find('[name="keyword"]').on('keyup', function(e) {
          if (timeout) {
            window.clearTimeout(timeout);
          }
          timeout = window.setTimeout(taFilter.submitFilter, 700);
        });        
        //status, due, priority
        $filter.find('[name="status"], [name="due"], [name="priority"]').on('change', function(e) {
          taFilter.submitFilter();
        });
        //completed
        $filter.find('[name="showCompleted"]').on('click', function(e) {
          taFilter.submitFilter();
        });
        
        //selectize: labels, assignee
        var defOpts = {
            create: true,
            type : 'tag',
            wrapperClass: 'exo-mentions dropdown',
            dropdownClass: 'dropdown-menu uiDropdownMenu',
            inputClass: 'selectize-input replaceTextArea ',
            plugins: ['remove_button'],
            hideSelected: true,
            closeAfterSelect: true,
            onItemAdd: function() {
              taFilter.submitFilter();
            },
            onItemRemove: function() {
              taFilter.submitFilter();
            }
        };

        //labels
        initLabelFilter($filter, defOpts);
        //assignee
        initAssignee($filter, defOpts);
      },
      
      removeFilter: function() {
        var ui = taApp.getUI();
        taFilter.submitFilter(true);
        taApp.hideRightPanel(ui.$centerPanel, ui.$rightPanel, ui.$rightPanelContent);
      },

      submitFilter: function(reset, callback) {
        var $content = taApp.getUI().$centerPanelContent;
        var data = taFilter.getFilterData(reset);
        $content.data('filterData', data);
        $content.jzLoad('TaskController.listTasks()', data, callback);
      },
      
      //reset = true --> not submit filter data from right panel 
      getFilterData: function(reset) {
        var ui = taApp.getUI();
        var $content = ui.$centerPanelContent; 
        var $view = $content.find('.projectListView, .taskBoardView');
        var $taskFilter = ui.$rightPanelContent.find('.taskFilter');
        
        var projectId = $view.data('projectid');
        var labelId = $view.data('labelid');
        var groupBy = $view.find('[name="groupBy"]').val();
        if(groupBy == undefined) {
            groupBy = '';
        }
        var orderBy = $view.find('[name="orderBy"]').val();
        if(orderBy == undefined) {
            orderBy = '';
        }
        var filter = $view.find('[name="filter"]').val();
        if (filter == undefined) {
            filter = '';
        }
        var viewType = $view.find('[name="viewType"]').val();
        if (viewType == undefined) {
            viewType = 'list';
        }

        //projectId, labelId, filterLabelIds, statusId, dueDate, priority, assignee, completed, keyword, groupby, orderBy, filter, viewType, securityContext
        var data = {
          'projectId': projectId,
          'labelId': labelId,
          'groupBy': groupBy,
          'orderBy': orderBy,
          'filter': filter,
          'viewType': viewType
        };
         
        if (!reset) {
          var $rightPanelContent = ui.$rightPanelContent;
          if (taFilter.isEnable()) {
            var keyword = $taskFilter.find('[name="keyword"]');
            if (keyword.length) {
              data.keyword = $.trim(keyword.val());              
            }
            
            var labelIds = $taskFilter.find('[name="label"]').data('selectize');
            if (labelIds) {
              var val = labelIds.getValue();
              data.filterLabelIds = val;
            }
            
            var status = $taskFilter.find('[name="status"]');
            if (status.length) {
              data.statusId = $.trim(status.val());              
            }

            var assignee = $taskFilter.find('[name="assignee"]').data('selectize');
            if (assignee) {
              var val = assignee.getValue();
              data.assignee = val;
            }
            
            var due = $taskFilter.find('[name="due"]');
            if (due.length) {
              data.dueDate = due.val();
            }
            
            var priority = $taskFilter.find('[name="priority"]');
            if (priority.length) {
              data.priority = $.trim(priority.val());
            }
            
            var showCompleted = $taskFilter.find('[name="showCompleted"]');
            if (showCompleted.length) {
              data.showCompleted = showCompleted.is(':checked');
            }
          }          
        }
        return data;
      },

      isShowCompletedTask: function() {
          var ui = taApp.getUI();
          var $centerPanelContent = ui.$centerPanelContent;

          return $centerPanelContent.find('[name="showCompleted"]').val() == 'true';
      }
  };
  
  function initLabelFilter($filter, defOpts) {
    var $lblFilter = $filter.find('[name="label"]'); 
    var isLoaded = false;
    var allLabels = {};
    //
    $lblFilter.suggester($.extend({}, defOpts, {
      options: $lblFilter.data('options'),
      create: false,
      searchField: ['name'],
      items: $lblFilter.data('items'),
      labelField: 'name',
      valueField: 'id',
      sourceProviders: ['exo:taskfilterlabel'],
      renderMenuItem: function(item, escape) {
        return '<li class="data">' +
          '<a href="" class="text">' + escape(item.name) + '</a>'
        '</li>';
      },
      renderItem: function(item, escape) {
        return '<div class="label '+item.color+'">' + escape(item.name) +'</div>';
      },
      providers: {
        'exo:taskfilterlabel': function(query, callback) {
          if (isLoaded) {
            callback(allLabels);
          }
          //. Load all label of user
          $.ajax({
            url: $filter.jzURL('UserController.findLabel'),
            data: {},
            type: 'GET',
            error: function() {
              callback();
            },
            success: function(res) {
              callback(res);
              isLoaded = true;
              $.each(res, function(index, val) {
                allLabels[val.id] = val;
              });
            }
          });
        }
      }
    }));
  }
  
  function initAssignee($filter, defOpts) {    
    var $asFilter = $filter.find('[name="assignee"]');
    var defaultOptionValues = $asFilter.data('options');
    $asFilter.suggester({
      type : 'tag',
      plugins: ['remove_button'],
      preload: false,
      persist: false,
      createOnBlur: true,
      highlight: false,
      hideSelected: true,
      openOnFocus: true,
      sourceProviders: ['exo:taskfilteruser'],
      valueField: 'id',
      labelField: 'text',
      searchField: ['text', 'id'],
      loadThrottle: null,
      hideSelected: true,
      closeAfterSelect: true,
      onItemAdd: function(value, $item) {
        this.$input.parent().find('.selectize-input input').attr('disabled', 'disabled');
        taFilter.submitFilter();
      },
      onItemRemove: function(value, $item) {
        this.$input.parent().find('.selectize-input input').removeAttr('disabled');
        taFilter.submitFilter();
      },
      onInitialize: function() {
        for (index = 0; index < defaultOptionValues.length; ++index) {
            $asFilter[0].selectize.addOption(defaultOptionValues[index]);
        }
        var items = $asFilter.data('items');
        if(items && items.length) {
          $asFilter[0].selectize.addItem(items[0], false);
          this.$input.parent().find('.selectize-input input').attr('disabled', 'disabled');
        }
      },
      renderItem: function(item, escape) {
        if(!item.avatarUrl) {
          for (index = 0; index < defaultOptionValues.length; ++index) {
            if(defaultOptionValues[index].id == item.id) {
              item = defaultOptionValues[index];
              break;
            }
          }
        }
        return '<div class="item">' + escape(item.text) +'</div>';
      },
      renderMenuItem: function(item, escape) {
        if(!item.avatarUrl) {
          for (index = 0; index < defaultOptionValues.length; ++index) {
            if(defaultOptionValues[index].id == item.id) {
              item = defaultOptionValues[index];
              break;
            }
          }
        }
        var avatar = item.avatarUrl;
        if (avatar == null) {
          avatar = '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
        }
        if(!item.text) {
            item.text = item.value;
        }
        return '<div class="avatarMini">' +
          '   <img src="'+item.avatar+'">' +
          '</div>' +
          '<div class="text">' + escape(item.text) + ' (' + item.id +')' + '</div>' +
          '<div class="user-status"><i class="uiIconColorCircleGray"></i></div>';
      },
      sortField: [{field: 'order'}, {field: '$score'}],
      providers: {
        'exo:taskfilteruser': function(query, callback) {
          if (!query.length) return callback();
          $.ajax({
              url: $filter.jzURL('UserController.findUser'),
              data: {query: query},
              type: 'GET',
              error: function() {
                  callback();
              },
              success: function(res) {
                  callback(res);
              }
          });
        }
      }
    });
  }
  return taFilter;
});