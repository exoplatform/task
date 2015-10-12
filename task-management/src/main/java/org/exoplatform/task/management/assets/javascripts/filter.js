define('taFilter', ['jquery', 'selectize'], function($) {
  var taApp = null;

  var taFilter = {
      init: function(ta) {
        taApp = ta;
        var ui = taApp.getUI();
        var $centerContent = ui.$centerPanelContent;

        $centerContent.on('click', '.uiIconFilter', function(e) {
          var $icon = $(e.target);
          
          if (ui.$rightPanelContent.find('.taskFilter').length || !$icon.hasClass('uiIconBlue')) {
            $icon.toggleClass('uiIconBlue');
          }

          //
          if ($icon.hasClass('uiIconBlue')) {
            ui.$rightPanelContent.jzLoad('FilterController.showFilter()', taFilter.getFilterData(), function() {
              taApp.showRightPanel(ui.$centerPanel, ui.$rightPanel);
              
              taFilter.initSearchForm();
            });
          } else {
            taFilter.removeFilter();            
          }
        });
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
        $filter.find('[name="completed"]').on('click', function(e) {
          taFilter.submitFilter();
        });
        
        //selectize: labels, tags, assignee
        var defOpts = {
            create: true,
            wrapperClass: 'exo-mentions dropdown',
            dropdownClass: 'dropdown-menu uiDropdownMenu',
            inputClass: 'selectize-input replaceTextArea ',
            plugins: {
                remove_button: {
                    label: '&#120;',
                    className : 'removeValue'
                }
            },
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
        //tags
        initTagFilter($filter, defOpts);
        //assignee
        initAssignee($filter, defOpts);
      },
      
      removeFilter: function() {
        var ui = taApp.getUI();
        taFilter.submitFilter(true);
        taApp.hideRightPanel(ui.$centerPanel, ui.$rightPanel, ui.$rightPanelContent);
      },

      submitFilter: function(reset) {
        var $content = taApp.getUI().$centerPanelContent;
        var data = taFilter.getFilterData(reset);
        $content.data('filterData', data);
        $content.jzLoad('TaskController.listTasks()', data);
      },
      
      //reset = true --> not submit filter data from right panel 
      getFilterData: function(reset) {
        var ui = taApp.getUI();
        var $content = ui.$centerPanelContent; 
        var $view = $content.find('.projectListView, .taskBoardView');
        
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

        //projectId, labelId, filterLabelIds, tags, statusId, dueDate, priority, assignee, completed, keyword, advanceSearch, groupby, orderBy, filter, viewType, securityContext
        var data = {
          'projectId': projectId,
          'labelId': labelId,
          'advanceSearch': !reset,
          'groupBy': groupBy,
          'orderBy': orderBy,
          'filter': filter,
          'viewType': viewType
        };
         
        if (!reset) {
          var $rightPanelContent = ui.$rightPanelContent;
          if ($content.find('.uiIconFilter').hasClass('uiIconBlue')) {
            var keyword = $.trim($rightPanelContent.find('[name="keyword"]').val());
            data.keyword = keyword;
            
            var labelIds = $rightPanelContent.find('[name="label"]').data('selectize');
            if (labelIds) {
              var val = labelIds.getValue();
              if (val) {
                data.filterLabelIds = val;              
              }
            }
            
            var tags = $rightPanelContent.find('[name="tag"]').data('selectize');
            if (tags) {
              var val = tags.getValue();
              if (val) {
                data.tags = val;              
              }
            }
            
            var status = $.trim($rightPanelContent.find('[name="status"]').val());
            if (status) {
              data.statusId = status;
            }
            
            var assignee = $rightPanelContent.find('[name="assignee"]').data('selectize');
            if (assignee) {
              var val = assignee.getValue();
              if (val) {
                data.assignee = val;              
              }
            }
            
            var due = $rightPanelContent.find('[name="due"]').val();
            if (due) {
              data.dueDate = due;
            }
            
            var priority = $.trim($rightPanelContent.find('[name="priority"]').val());
            if (priority) {
              data.priority = priority;
            }
            
            var completed = $rightPanelContent.find('[name="completed"]').is(':checked');
            if (!completed) {
              data.completed = completed;
            }
          }          
        }
        return data;
      }
  };
  
  function initLabelFilter($filter, defOpts) {
    var $lblFilter = $filter.find('[name="label"]'); 
    $lblFilter.selectize($.extend({
      options: $lblFilter.data('options'),
      create: false,
      searchField: ['name'],
      labelField: 'name',
      valueField: 'id',
      render: {
        option: function(item, escape) {
            return '<li class="data">' +
                '<a href="" class="text">' + escape(item.name) + '</a>'
            '</li>';
        },
        item: function(item, escape) {
            return '<div class="label '+item.color+'">' + escape(item.name) +'</div>';
        }
      }
    }, defOpts));
  }
  
  function initTagFilter($filter, defOpts) {
    var $tagFilter = $filter.find('[name="tag"]');
    $tagFilter.selectize($.extend({
      render: {
        option: function(item, escape) {
            return '<li class="data">' +
                '<a href="">' + escape(item.text) + '</a>'
                '</li>';
        },
        item: function(item, escape) {
            return '<div class="label primary">' + escape(item.text) +'</div>';
        },
        option_create: function(data, escape) {
            return '<li class="create"><a href="javascript:void(0)">Add <strong>' + escape(data.input) + '</strong>&hellip;</a></li>';
        }
      }
    }, defOpts));
  }
  
  function initAssignee($filter, defOpts) {    
    var selectizeOptions = $.extend({}, defOpts, {
        valueField: 'id',
        labelField: 'text',
        searchField: ['text', 'id'],
        openOnFocus: false,
        wrapperClass: 'exo-mentions dropdown',
        dropdownClass: 'dropdown-menu uiDropdownMenu autocomplete-menu',
        inputClass: 'selectize-input replaceTextArea',
        hideSelected: true,
        closeAfterSelect: true,
        create: false,
        plugins: {
            task_remove_button: {
                label: '<i class="uiIconClose uiIconLightGray"></i>',
                className : 'removeValue'
            },
            no_results: {}
        },
        render: {
            option: function(item, escape) {
                return '<li class="data">' +
                    '<div class="avatarSmall">' +
                    '   <img src="'+item.avatar+'">' +
                    '</div>' +
                    '<span class="text">' + escape(item.text) + ' (' + item.id +')' + '</span>' +
                    '<span class="user-status"><i class="uiIconColorCircleGray"></i></span>' +
                    '</li>';
            },
            item: function(item, escape) {
                return '<span class="" href="#">' + escape(item.text) +'</span>';
            }
        },
        onInitialize: function() {
            var self      = this;
            var settings  = self.settings;
            var $dropdown         = self.$dropdown;
            var $wrapper = self.$wrapper;
            var $dropdown_content = self.$dropdown_content;
            $dropdown_content.remove();
            $dropdown_content = $('<ul>').addClass(settings.dropdownContentClass).appendTo($dropdown);
            $('<div class="autocomplete-menu loading">' +
                '<div class="loading center muted">' +
                '<i class="uiLoadingIconMini"></i>' +
                '<div class="loadingText">Loading...</div>' +
                '</div>' +
                '</div>').appendTo($wrapper);
            self.$dropdown_content = $dropdown_content;
        },
        load: function(query, callback) {
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
    });
    
    var $asFilter = $filter.find('[name="assignee"]');
    $asFilter.selectize(selectizeOptions);
  }
  
  return taFilter;
});