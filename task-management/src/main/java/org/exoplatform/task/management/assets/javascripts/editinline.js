define('ta_edit_inline',
    ['SHARED/jquery', 'task_ui_calendar', 'taFilter', 'SHARED/edit_inline_js', 'SHARED/selectize2','SHARED/taskLocale',
        'x_editable_select3', 'x_editable_selectize', 'x_editable_calendar', 'x_editable_ckeditor', 'SHARED/suggester'],
    function($, uiCalendar, taFilter, editinline, selectize, locale) {

        /**
         * This is plugin for selectize, it is used to delete assignee of task
         * @param str
         * @returns {string}
         */
        var escape_html = function(str) {
            return (str + '')
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;');
        };
        
        var reloadTaskList = function(taApp) {
          var $rightPanelContent = taApp.getUI().$rightPanelContent;
          var $center = taApp.getUI().$centerPanelContent;
          var selectedTask = $center.find('.table-project > .selected').data('taskid');
          //
          taFilter.submitFilter(!taFilter.isEnable(), function() {
            if (selectedTask) {
              var $taskItem = taApp.getUI().$centerPanelContent.find('.table-project > *[data-taskid="' + selectedTask + '"]');
              $taskItem.addClass('selected');
            }
          });
        }

        selectize.define('no_results', function( options ) {
            var self = this;

            options = $.extend({
                html: function(data) {
                    return (
                        '<div class="autocomplete-menu not-found">' +
                        ' <div class="noMatch center muted">'+ locale.noMatch +'</div>' +
                        '</div>'
                        );
                }
            }, options );

            self.displayEmptyResultsMessage = function () {
                //this.$empty_results_container.css( 'top', this.$control.outerHeight() );
                this.$empty_results_container.show();
            };

            self.refreshOptions = (function () {
                var original = self.refreshOptions;

                return function () {
                    original.apply( self, arguments );
                    this.hasOptions || !self.lastQuery ? this.$empty_results_container.hide() :
                        this.displayEmptyResultsMessage();
                }
            })();

            self.onBlur = (function () {
                var original = self.onBlur;

                return function () {
                    original.apply( self, arguments );
                    this.$empty_results_container.hide();
                };
            })();

            self.setup = (function() {
                var original = self.setup;
                return function() {
                    original.apply(self, arguments);
                    self.$empty_results_container = $( options.html( $.extend( {
                        classNames: self.$input.attr( 'class' ) }, options ) ) );
                    self.$empty_results_container.insertBefore( self.$dropdown );
                    self.$empty_results_container.hide();
                };
            })();
        });

        /**
         * End selectize plugin
         */


        $.fn.editableform.buttons = '<button type="submit" class="btn btn-primary editable-submit"><i class="uiIconTick icon-white"></i></button>'+
            '<button type="button" class="btn editable-cancel"><i class="uiIconClose"></i></button>';

        var defaultOptions = {
            mode: 'inline',
            showbuttons: false,
            onblur: 'submit',
            emptyclass: 'muted',
            highlight: false
        };
        var getDefaultOptionForType = function(type) {
            var options = $.extend({}, defaultOptions, {});
            if (type == 'text') {
                options.inputclass = 'blackLarge';
                options.clear = false;
            } else if (type == 'textarea') {
                options.emptytext = "Description";
            } else if (type == 'calendar') {
                options.mode = 'popup';
                options.emptytext = ' ' + locale.dueDate;
            } else if (type == 'select') {
                options.sourceCache = false;
            }

            return options;
        }


        var editInline = {};

        var ui;
        var $leftPanel;
        var $centerPanel;
        var $rightPanel;
        var $rightPanelContent;
        var $cloneProject;
        var $modalPlace;
        var defaultOptionValues;

        editInline.init = function(taApp) {
            editInline.taApp = taApp;
            ui = taApp.getUI();
            $leftPanel = ui.$leftPanel;
            $centerPanel = ui.$centerPanel;
            $rightPanel = ui.$rightPanel;
            $rightPanelContent = ui.$rightPanelContent;
            $cloneProject = $('.confirmCloneProject');
            $modalPlace = $('.modalPlace');
        };

        editInline.saveTaskDetailFunction = function(params) {
            var currentTaskId = $rightPanelContent.find('[data-taskid]').data('taskid');
            if (currentTaskId == 0 || currentTaskId == undefined) {
                return;
            }
            var d = new $.Deferred;
            //if left empty, task title will be set to Untitled Task
            if (params.name == 'title' && !params.value) {
               var $title = $rightPanel.find('[data-name="title"]');
               params.value = $title.data('emptytext');
            }
            var data = params;
            params.pk = currentTaskId;
            data.taskId = currentTaskId;
            $('#taskManagement').jzAjax('TaskController.saveTaskInfo()',{
                data: data,
                method: 'POST',
                traditional: true,
                success: function(response) {
                    d.resolve(response);
                    if (params.name == 'title') {
                        $centerPanel.find('[data-taskid="'+data.taskId+'"] .taskName').text(params.value);
                    }
                    if (params.name =='duedate') {
                        var tDate = new Date($("[name='toDate']").val()).getTime();
                        var dDate = new Date(params.value).getTime();
                        if (tDate >= (dDate + 86400000 )) {
                            $("#due-warning").show();
                        } else {
                            $("#due-warning").hide();
                        }
                    }
                    if ($.isNumeric(response)) {
                      editInline.taApp.updateTaskNum(response);                                      
                    }

                    reloadTaskList(editInline.taApp);
                },
                error: function(xhr, textStatus, errorThrown ) {
                  $('[data-name="' + params.name + '"]').first().editable('toggle');
                  d.reject('update failure: ' + xhr.responseText);
                  editInline.taApp.showWarningDialog(xhr.responseText);
                }
            });
            return d.promise();
        };

        var UICalendarFrom = $.extend({}, uiCalendar, {
            calendarId: 'UITaskFromCalendarControl',
            eXoName: 'eXo.webui.UITaskCalendarFrom'
        });
        var UICalendarTo = $.extend({}, uiCalendar, {
            calendarId: 'UITaskToCalendarControl',
            eXoName: 'eXo.webui.UITaskCalendarTo'
        });
        eXo.webui.UITaskCalendarFrom = UICalendarFrom;
        eXo.webui.UITaskCalendarTo = UICalendarTo;

        editInline.initWorkPlan = function(taskId) {
            var $fieldWorkPlan = $('.fieldWorkPlan');
            var $popover = $fieldWorkPlan.find('.editableField');
            var $removeWorkPlan = $fieldWorkPlan.find('.removeWorkPlan');

            var saveWorkPlan = function(plan) {
                var data = {
                    pk : taskId,
                    name : 'workPlan'
                };
                if (plan) {
                    data.value = plan;
                }
                var callback = editInline.saveTaskDetailFunction(data);
                callback.done(function(response) {
                    $popover.html(response);
                    if (plan == null) {
                        $("#due-warning").hide();
                        $removeWorkPlan.addClass("hide");
                    } else {
                        $removeWorkPlan.removeClass("hide");
                    }
                }).fail(function() {
//                    alert('fail to update');
                });
            };


            $removeWorkPlan.click(function() {
                saveWorkPlan(null);
            });

            $(document).on('click', function(e) {
                if ($(e.target).closest('.fieldWorkPlan').length == 0) {
                    UICalendarFrom.destroy();
                    UICalendarTo.destroy();
                    $popover.popover('hide');
                    $popover.closest('.uiEditableInline').removeClass('active').addClass('inactive');
                }
            });
            $popover.popover({
                placement: 'left',
                html: true,
                content: function() {
                    return $fieldWorkPlan.find('.rangeCalendar').html()
                }
            }).on("shown.bs.popover", function(e) {
                var $pop = $(this).parent().find('.popover');
                if ($pop.length == 0) {
                    return;
                }
                $popover.closest('.uiEditableInline').removeClass('inactive').addClass('active');

                var updatePopoverPossition = function() {
                    var height = $pop.outerHeight();
                    var cssHeight = '-' + (height/2 - 15) + 'px';
                    $pop.css('top', cssHeight);
                };

                var $inputFrom = $pop.find('[name="fromDate"]');
                var $inputTo = $pop.find('[name="toDate"]');
                var $inputFromTime = $pop.find('[name="fromTime"]');
                var $inputToTime = $pop.find('[name="toTime"]');
                var $inputAllday = $pop.find('[name="allday"]');

                UICalendarFrom.init($inputFrom[0], false, 'yyyy-MM-dd', $inputFrom.val());
                UICalendarTo.init($inputTo[0], false, 'yyyy-MM-dd', $inputTo.val());

                $pop.off('change', '[name="allday"]').on('change', '[name="allday"]', function(e) {
                    var $this = $(this);
                    var $timeSelector = $this.closest('.choose-time');
                    if($this.is(':checked')) {
                        $timeSelector.addClass('all-day');
                    } else {
                        $timeSelector.removeClass('all-day');
                    }
                });
                $pop.off('change', '[name="fromDate"], [name="toDate"], [name="fromTime"], [name="toTime"], [name="allday"]')
                    .on('change', '[name="fromDate"], [name="toDate"], [name="fromTime"], [name="toTime"], [name="allday"]', function(e) {

                        UICalendarFrom.show();
                        UICalendarTo.show();

                        var isAllDay = $inputAllday.is(':checked');
                        if (isAllDay) {
                            $inputFromTime.val('00:00').attr('value', '00:00');
                            $inputToTime.val('23:59').attr('value', '23:59');
                        }

                        var fromDate = $.trim($inputFrom.val());
                        var toDate = $.trim($inputTo.val());
                        var fromTime = $.trim($inputFromTime.val());
                        var toTime = $.trim($inputToTime.val());

                        if (fromDate == '' || toDate == '') {
                            return;
                        }

                        var fromDates = fromDate.split('-');
                        var toDates = toDate.split('-');
                        var fromTimes = fromTime.split(':');
                        var toTimes = toTime.split(':');

                        var fDate = new Date(fromDates[0], fromDates[1] - 1, fromDates[2], fromTimes[0], fromTimes[1], 0).getTime();
                        var tDate = new Date(toDates[0], toDates[1] - 1, toDates[2], toTimes[0], toTimes[1], 0).getTime();
                        var dDate = new Date($('[data-name="duedate"]').html()).getTime();

                        if (fDate >= tDate) {
                            $pop.find('.errorMessage').html(locale.taskPlan.errorMessage);
                            updatePopoverPossition();
                        } else {
                            if (tDate >= (dDate + 86400000 )) {
                                $("#due-warning").show();
                            } else {
                                $("#due-warning").hide();
                            }
                            $pop.find('.errorMessage').html('');
                            updatePopoverPossition();
                            saveWorkPlan([fromDate + " " + fromTime, toDate + " " + toTime]);
                            var $rangeCalendar = $fieldWorkPlan.find('.rangeCalendar');
                            if (isAllDay) {
                                $rangeCalendar.find('.choose-time').addClass('all-day');
                                $rangeCalendar.find('[name="allday"]').attr('checked', true);
                            } else {
                                $rangeCalendar.find('.choose-time').removeClass('all-day');
                                $rangeCalendar.find('[name="allday"]').attr('checked', false);
                            }
                            $rangeCalendar.find('[name="fromDate"]').val(fromDate);
                            $rangeCalendar.find('[name="toDate"]').val(toDate);
                            $rangeCalendar.find('[name="fromTime"]').val(fromTime).attr('value', fromTime);
                            $rangeCalendar.find('[name="toTime"]').val(toTime).attr('value', toTime);
                        }
                    });
                $pop.off('click', '[data-time]')
                    .on('click', '[data-time]', function(e) {
                        var $a = $(e.target).closest('[data-time]');
                        var time = $a.data('time');
                        var $input = $a.closest('ul').parent().find('input');
                        $input.val(time);
                        $input.change();
                    });

                updatePopoverPossition();
            })
        };

        var selectizeOptions = {
            valueField: 'id',
            labelField: 'text',
            searchField: ['text'],
            openOnFocus: false,
            create: false,
            hideSelected: true,
            closeAfterSelect: true,
            maxOptions: 10,
            highlight: false,
            sourceProviders: ['exo:taskuser'],
            plugins: ['remove_button', 'no_results'],
            renderMenuItem: function(item, escape) {
              if(!item.avatar) {
                for (index = 0; index < defaultOptionValues.length; ++index) {
                  if(defaultOptionValues[index].id == item.id) {
                    item = defaultOptionValues[index];
                    break;
                  }
                }
              }
              if (item.deleted === true || item.enable === false) {
                return '';
              }
              return '<div class="avatarMini">' +
                '   <img src="'+item.avatar+'">' +
                '</div>' +
                '<div class="text">' + editInline.taApp.escape(item.text) + ' (' + item.id +')' + '</div>' +
                '<div class="user-status"><i class="uiIconColorCircleGray"></i></div>';
            },
            renderItem: function(item, escape) {
              if(!item.avatar) {
                for (index = 0; index < defaultOptionValues.length; ++index) {
                  if(defaultOptionValues[index].id == item.id) {
                    item = defaultOptionValues[index];
                    break;
                  }
                }
              }
              var text = editInline.taApp.escape(item.text);
              var cssClass = '';
              if (item.deleted === true) {
                cssClass = 'muted';
              } else if (item.enable === false) {
                text += '&nbsp;<span class="muted" style="font-style: italic">(' + locale.inactive + ')</span>';
              }
              return '<div class="item">' + text +'</div>';
            },
            onDropdownOpen: function($dropdown) {
                $dropdown.closest('.exo-mentions').addClass('dropdown-opened');
            },
            onDropdownClose: function($dropdown) {
                $dropdown.closest('.exo-mentions').removeClass('dropdown-opened');
            },
            providers: {
              'exo:taskuser': function(query, callback) {
                var $projectId = $rightPanel.find('.breadcrumbCont > ul').data('value');
                if (!query || !query.length) return callback(defaultOptionValues);
                $.ajax({
                    url: $rightPanel.jzURL('UserController.findUser'),
                    data: {query: query, projectId: $projectId},
                    type: 'GET',
                    error: function() {
                        callback();
                    },
                    success: function(res) {
                        callback(res);
                    }
                });
              }
            },
            score: function(search) {
                var score = this.getScoreFunction(search);
                return function(item) {
                    if (item.deleted === true || item.enable === false) {
                        return 0;
                    } else {
                        return score(item);
                    }
                };
            }
        };
        var saveAssignee = function(taskId, name, value, selectize) {
            var val = value;
            if (name == 'coworker') {
                val = [];
                if (value != '') {
                    val = value.split(',');
                }
            }

            var options = selectize.options;
            $rightPanel.jzAjax('TaskController.saveTaskInfo()',{
                data: {taskId: taskId, name: name, value: val},
                method: 'POST',
                traditional: true,
                success: function(response) {
                    var $assignee = $rightPanel.find('input[name="assignee"]');
                    var $editable = $assignee.closest('.uiEditableInline');
                    var assignee = $assignee.val();
                    var coworkders = $rightPanel.find('input[name="coworker"]').val();

                    var assg = false;
                    if (assignee != '' && options[assignee] != undefined) {
                        var assg = options[assignee];
                    }
                    if (coworkders == '') {
                        coworkders = [];
                    } else {
                        coworkders = coworkders.split(',');
                    }
                    var numberCoWorker = coworkders.length;

                    if (!assg) {
                        $editable.find('.unassigned').removeClass('hide');
                        $editable.find('.assigned').addClass('hide');
                    } else {
                        $editable.find('.assigned img').attr('src', assg.avatar);
                        $editable.find('.unassigned').addClass('hide');
                        $editable.find('.assigned').removeClass('hide');
                    }

                    var $editAssignee = $editable.find('.editAssignee');
                    if (numberCoWorker > 0) {
                        var coworkerLabel = numberCoWorker > 1 ? locale.coworkers.toLowerCase() : locale.coworker.toLowerCase();
                        $editAssignee.html('+' + numberCoWorker + ' ' + coworkerLabel);
                    } else if (!assg){
                        $editAssignee.html(locale.unassigned);
                    } else {
                        var text = editInline.taApp.escape(assg.text);
                        if (assg.deleted === true) {
                            $editAssignee.addClass('muted');
                        } else if (assg.enable === false) {
                            text += '&nbsp;<span class="muted" style="font-style: italic">(' + locale.inactive + ')</span>';
                        }
                        $editAssignee.html(text);
                    }
                    
                    if ($.isNumeric(response)) {
                      editInline.taApp.updateTaskNum(response);                                      
                    }
                    
                    reloadTaskList(editInline.taApp);
                },
                error: function(response) {
                  $('[data-name="' + name + '"]').editable('toggle');
                  editInline.taApp.showWarningDialog(response.responseText);
                }
            });
        }
        editInline.initAssignment = function(taskId) {
            var getDisplayNameURL = $rightPanel.jzURL('UserController.getDisplayNameOfUser');
            var val = [];
            var $assignee = $rightPanel.find('input[name="assignee"]');
            var $coworker = $rightPanel.find('input[name="coworker"]');
            var $assignMe = $rightPanel.find('[data-action="assign"], [data-action="add-coworker"]');
            var tDate = new Date($("[name='toDate']").val()).getTime();
            var dDate = new Date($('[data-name="duedate"]').html()).getTime();

            if (tDate >= (dDate + 86400000 )) {
                $("#due-warning").show();
            } else {
                $("#due-warning").hide();
            }

            if ($assignMe.length > 0) {
                $assignMe.each(function() {
                    var v = $(this).data('value');
                    if (v) {
                        val.push(v);
                    }
                });
            }

            var assignee = $assignee.val();
            // This is workaround because some failure action in save before,
            // it save "[object Object]" or "undefined" to database
            if (assignee == '[object Object]' || assignee == undefined) {
                assignee = '';
                $assignee.val('');
            }

            if (assignee != '') {
                val.push(assignee);
            }
            if ($coworker.val() != '') {
                val.push($coworker.val());
            }
            val = val.join(',');

            if (val != '') {
                $.ajax({
                    url: getDisplayNameURL,
                    async: false,
                    data: {usernames: val},
                    success: function(res) {
                        defaultOptionValues = res;
                    }
                });
            }

            var onInit = selectizeOptions.onInitialize;
            var opts = $.extend({}, selectizeOptions, {
                type: "tag",
                preload: true,
                onInitialize: function() {
                    if(onInit) {
                        onInit.apply(this, arguments);
                    }
                    if (assignee != '') {
                        this.$input.closest('.inputUser').find('.selectize-input input').attr('disabled', 'disabled');
                    }
                },
                onItemAdd: function(value, $item) {
                    this.$input.closest('.inputUser').find('.selectize-input input').attr('disabled', 'disabled');
                },
                onItemRemove: function(value, $item) {
                    this.enable();
                    this.$input.closest('.inputUser').find('.selectize-input input').removeAttr('disabled');
                    var _this = this;
                },
                onChange: function(value) {
                    saveAssignee(taskId,'assignee', value, this);
                }
            });
            $assignee.suggester(opts);

            opts = $.extend({}, selectizeOptions, {
              type: "tag",
              preload: true,
              onChange: function(value) {
                  saveAssignee(taskId,'coworker', value, this);
              }
            });
            $coworker.suggester(opts);

            $assignMe.click(function(e) {
                var $action = $(e.target).closest('[data-action]');
                var action = $action.data('action');
                var value = $action.data('value');

                if (action == 'assign') {
                    $assignee[0].selectize.setValue(value, false);
                } else {
                    $coworker[0].selectize.addItem(value, false);
                }
            });
        };

        editInline.initEditInline = function(taskId) {
            var $fieldWorkPlan = $('.fieldWorkPlan');
            if (!$fieldWorkPlan.hasClass('readOnly')) {
                editInline.initWorkPlan(taskId);
            }

            //tabs in task detail
            $('.taskTabs a').click(function(e) {
                e.preventDefault();

                var $tab = $(this);
                if ($tab.attr('href') == '.taskLogs') {
                    $tab.closest('.task-detail').find('.taskLogs').jzLoad('TaskController.renderTaskLogs()', {taskId: taskId}, function(html, status, xhr) {
                      if (xhr.status >= 400) {
                        editInline.taApp.showWarningDialog(xhr.responseText);
                      } else {
                        $tab.tab('show');                        
                      }
                    });
                } else {
                    $tab.tab('show');
                }
            });

            $rightPanel.on('show.bs.tab', '[href="#tab-changes"]', function(e) {
                var taskId = $(e.target).closest('[data-taskid]').data('taskid');
                $rightPanel.find('#tab-changes').jzLoad('TaskController.renderTaskLogs()', {taskId: taskId}, function(html, status, xhr) {
                  if (xhr.status >= 400) {
                    editInline.taApp.showWarningDialog(xhr.responseText);
                  }
                });
            });

            var $taskDetailContainer = $('[data-taskid]');
            $taskDetailContainer.find('.editable').each(function() {
                var $this = $(this);
                var dataType = $this.attr('data-type');
                var fieldName = $this.attr('data-name');
                var editOptions = getDefaultOptionForType(dataType);
                editOptions = $.extend({}, editOptions, {
                    pk: taskId,
                    url: editInline.saveTaskDetailFunction
                });
                if (fieldName == 'dueDate') {
                    editOptions.emptytext = " " + locale.dueDate;
                    editOptions.mode = 'popup';
                }
                if (fieldName == 'status') {
                    var currentStatus = $this.attr('data-val');
                    editOptions.value = currentStatus;
                    var currStatusLabel = $this.attr('data-label');
                    $this.html(currStatusLabel);
                }
                if (fieldName == 'priority') {
                    /*var priority = [];
                    $.each($this.data('priority').split(','), function (idx, elem) {
                        priority.push({'text': elem, 'value': elem});
                    });
                    //
                    editOptions.source = priority;*/
                    editOptions.success = function (response, newValue) {
                        $this.parent().find('i').attr('class', 'uiIconColorPriority' + newValue);
                    }
                }
                if (fieldName == 'title') {
                	editOptions.emptyclass = '';
                }
                if (fieldName == 'description') {
                    editOptions.emptytext = locale.taskDescriptionEmpty;
                    editOptions.display = function(value, response) {
                        $(this).html(editInline.taApp.convertURLsAsLinks(value));
                        // do not propagate event when clicking a link in the description
                        $("a", this).click(function(e) {
                            e.stopPropagation();
                        });
                    };
                }
                if (fieldName == 'labels') {
                    var isLoaded = false;
                    var allLabels = {};
                    var opts = $this.data('selectizie-opts');
                    $.each(opts, function(index, val) {
                        allLabels[val.id] = val;
                    });

                    editOptions.success = function (response, newValue) {
                        var isEmpty = newValue.length == 0 || newValue[0] == '';
                        var $i = $this.parent().find('.icon-hash');
                        if (isEmpty) {
                            $i.removeClass('hide');
                        } else {
                            $i.addClass('hide');
                        }
                        if (fieldName == 'labels') {
                            $('.rightPanelContent ').trigger('saveLabel');
                        }
                    };

                    editOptions.emptytext = locale.labels;
                    editOptions.selectize = {
                        create: true,
                        options: opts,
                        valueField: 'id',
                        labelField: 'text',
                        searchField: 'text',
                        highlight: true,
                        openOnFocus: true,
                        onItemRemove: function(value) {
                          $this.trigger('labelRemoved', value);
                        },
                        onItemAdd: function(value, $item) {
                          $this.trigger('labelAdded', value);
                        },
                        load: function(query, callback) {
                            if (isLoaded) {
                                callback(allLabels);
                            }
                            //. Load all label of user
                            $.ajax({
                                url: $rightPanel.jzURL('UserController.findLabel'),
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
                        },
                        render: {
                            option: function(item, escape) {
                                var it = allLabels[item.id] != undefined ? allLabels[item.id] : item;
                                return '<li class="data">' +
                                    '<a href="" class="text">' + escape(it.text) + '</a>'
                                '</li>';
                            },
                            item: function(item, escape) {
                                var it = allLabels[item.id] != undefined ? allLabels[item.id] : item;
                                return '<div class="label '+it.color+'">' + escape(it.text) +'</div>';
                            },
                            option_create: function(data, escape) {
                                return '<li class="create"><a href="javascript:void(0)">'+ locale.createLabel +' <strong>' + escape(data.input) + '</strong>&hellip;</a></li>';
                            }
                        }
                    };
                    editOptions.value2html = function(val) {
                        var text = val, color = '';

                        var label = allLabels[val];
                        if (label != undefined) {
                            text = label.text;
                            color = label.color;
                        }

                        var encoder = $('<div></div>');
                        return '<span class="'+color+' label">'+ encoder.text(text).html()+'</span>';
                    }
                }

                $this.on('shown', function (e, editable) {
                    if (editable != undefined) {
                        $this.parent().removeClass('inactive').addClass('active');
                        $this.data('editable').input.$input.on('blur', function() {
                            $(document.body).trigger('click');
                        });
                    }
                }).on('hidden', function (e, editable) {
                    if (editable != undefined) {
                        $this.parent().removeClass('active').addClass('inactive');
                    }
                });
                $this.editable(editOptions);
                if (fieldName == 'project') {
                    var val = $this.editable('getValue', true);
                    if (val > 0) {
                        if ($this.find('a.removeProject').length == 0) {
                            var $close = $('<a class="removeProject" href="javascript:void(0)"><i class="uiIconClose"></i></a>');
                            $this.find('li').addClass('active').append($close);
                        }
                        $this.off('click', 'a.removeProject').on('click', 'a.removeProject', function(e) {
                          e.stopPropagation();
                          $this.jzAjax('TaskController.saveTaskInfo()', {
                            data: {taskId: taskId, name: 'project', value: 0},
                            method: 'POST',
                            success: function(e) {
                              $this.find('li').html('No Project').removeClass('active').addClass('muted');
                              $this.editable('setValue', 0);
                              if ($.isNumeric(e)) {
                                editInline.taApp.updateTaskNum(e);
                              }

                              reloadTaskList(editInline.taApp);
                            },
                            error: function(xhr) {
                              editInline.taApp.showWarningDialog(xhr.responseText);
                            }
                          });
                        });
                    }
                }
                if ($this.hasClass('readOnly')) {
                    $this.off();
                }
            });
            editInline.initAssignment(taskId);
        };

        editInline.initEditInlineForProject = function(projectId, $projectForm) {
            var $project = $projectForm;
            $project.find('.editable').each(function(){
                var $this = $(this);
                var dataType = $this.attr('data-type');
                var fieldName = $this.attr('data-name');
                var editOptions = getDefaultOptionForType(dataType);
                editOptions = $.extend({}, editOptions, {
                    pk: projectId
//                    url: editInline.saveProjectDetailFunction
                });
                if (fieldName == 'name') {
                	editOptions.emptyclass = '';
                }
                if (fieldName == 'description') {
                    editOptions.emptytext = locale.projectDescriptionEmpty;
                }
                if(fieldName == 'manager' || fieldName == 'participator') {
                    var findUserURL = $this.jzURL('UserController.findUser');
                    var getDisplayNameURL = $this.jzURL('UserController.getDisplayNameOfUser');
                    editOptions.showbuttons = true;
                    editOptions.emptytext = (fieldName == 'manager' ? locale.noManager : locale.noParticipator);
                    editOptions.source = findUserURL;
                    editOptions.select2= {
                        multiple: true,
                        allowClear: true,
                        placeholder: locale.selectUser,
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
                    editOptions.emptytext = " " + locale.dueDate;
                    editOptions.mode = 'popup';
                }
                $this.editable(editOptions);

                $this.on('shown', function(e, editable) {
                    $this.parent().removeClass('inactive').addClass('active');
                }).on('hidden', function(e, editable) {
                    $this.parent().removeClass('active').addClass('inactive');
                }).on('save', function() {                  
                  $this.closest('.addProject').find('.btn-primary').attr('disabled', false);
                });
            });
            
//            $project.find('.calInteg').on('change', function() {
//              editInline.saveProjectDetailFunction({'pk': projectId, 'name': $(this).attr('name'), 'value': $(this).is(':checked')});
//            });
        };

        return editInline;
    }
);