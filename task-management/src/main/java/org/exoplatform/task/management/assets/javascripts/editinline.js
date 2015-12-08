define('ta_edit_inline',
    ['SHARED/jquery', 'task_ui_calendar', 'SHARED/edit_inline_js', 'SHARED/selectize',
        'x_editable_select3', 'x_editable_selectize', 'x_editable_calendar', 'x_editable_ckeditor'],
    function($, uiCalendar, editinline, selectize) {

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
        selectize.define('task_remove_button', function(options) {
            options = $.extend({
                label     : '&times;',
                title     : 'Remove',
                className : 'remove',
                append    : true
            }, options);

            var self = this;
            var html = '<span class="' + options.className + '" tabindex="-1" title="' + escape_html(options.title) + '">' + options.label + '</span>';

            /**
             * Appends an element as a child (with raw HTML).
             *
             * @param {string} html_container
             * @param {string} html_element
             * @return {string}
             */
            var append = function(html_container, html_element) {
                var pos = html_container.search(/(<\/[^>]+>\s*)$/);
                return html_container.substring(0, pos) + html_element + html_container.substring(pos);
            };

            this.setup = (function() {
                var original = self.setup;
                return function() {
                    // override the item rendering method to add the button to each
                    if (options.append) {
                        var render_item = self.settings.render.item;
                        self.settings.render.item = function(data) {
                            return append(render_item.apply(this, arguments), html);
                        };
                    }

                    original.apply(this, arguments);

                    // add event listener
                    this.$control.on('click', '.' + options.className, function(e) {
                        e.preventDefault();
                        //if (self.isLocked) return;
                        var $item = $(e.currentTarget).parent();
                        self.setActiveItem($item);
                        if (self.deleteSelection()) {
                            self.setCaret(self.items.length);
                        }
                    });

                };
            })();

        });

        selectize.define('no_results', function( options ) {
            var self = this;

            options = $.extend({
                html: function(data) {
                    return (
                        '<div class="autocomplete-menu not-found">' +
                        ' <div class="noMatch center muted">No match</div>' +
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
                options.emptytext = ' Due date';
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
            $('#taskDetailContainer').jzAjax('TaskController.saveTaskInfo()',{
                data: data,
                method: 'POST',
                traditional: true,
                success: function(response) {
                    d.resolve(response);
                    if (params.name == 'title') {
                        $centerPanel.find('[data-taskid="'+data.taskId+'"] .taskName').text(params.value);
                    }
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

                        if (fDate >= tDate) {
                            $pop.find('.errorMessage').html('The End date time must be after the Start date time');
                            updatePopoverPossition();
                        } else {
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
            wrapperClass: 'exo-mentions dropdown',
            dropdownClass: 'dropdown-menu uiDropdownMenu autocomplete-menu',
            inputClass: 'selectize-input replaceTextArea',
            create: false,
            hideSelected: true,
            closeAfterSelect: true,
            maxOptions: 10,
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
                        '<span class="avatarMini">' +
                        '   <img src="'+item.avatar+'">' +
                        '</span>' +
                        '<span class="text">' + editInline.taApp.escape(item.text) + ' (' + item.id +')' + '</span>' +
                        '<span class="user-status"><i class="uiIconColorCircleGray"></i></span>' +
                        '</li>';
                },
                item: function(item, escape) {
                    return '<span class="" href="#">' + editInline.taApp.escape(item.text) +'</span>';
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
                    url: $rightPanel.jzURL('UserController.findUser'),
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
                        var coworkerLabel = numberCoWorker > 1 ? 'coworkers' : 'coworker';
                        $editAssignee.html('+' + numberCoWorker + ' ' + coworkerLabel);
                    } else if (!assg){
                        $editAssignee.html('Unassigned');
                    } else {
                        $editAssignee.html(editInline.taApp.escape(assg.text));
                    }
                },
                error: function(response) {
                  $('[data-name="' + name + '"]').editable('toggle');
                  editInline.taApp.showWarningDialog(response.responseText);
                }
            });
        }
        editInline.initAssignment = function(taskId) {
            var getDisplayNameURL = $rightPanel.jzURL('UserController.getDisplayNameOfUser');
            var options = [];
            var val = [];
            var $assignee = $rightPanel.find('input[name="assignee"]');
            var $coworkder = $rightPanel.find('input[name="coworker"]');
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
            if ($coworkder.val() != '') {
                val.push($coworkder.val());
            }
            val = val.join(',');

            if (val != '') {
                $.ajax({
                    url: getDisplayNameURL,
                    async: false,
                    data: {usernames: val},
                    success: function(res) {
                        options = res;
                    }
                });
            }

            var onInit = selectizeOptions.onInitialize;
            var opts = $.extend({}, selectizeOptions, {
                options: options,
                onInitialize: function() {
                    onInit.apply(this, arguments);
                    if (assignee != '') {
                        this.disable();
                    }
                },
                onItemAdd: function(value, $item) {
                    this.disable();
                },
                onItemRemove: function(value, $item) {
                    this.enable();
                    this.close();
                },
                onChange: function(value) {
                    saveAssignee(taskId,'assignee', value, this);
                }
            });
            $assignee.selectize(opts);
            opts = $.extend({}, selectizeOptions, {
                options: options,
                onChange: function(value) {
                    saveAssignee(taskId,'coworker', value, this);
                }
            });
            $coworkder.selectize(opts)
        };

        editInline.initEditInline = function(taskId) {
            editInline.initWorkPlan(taskId);

            var calIcon = $('.date-work-plan .uiIconPLFCalendar');
            calIcon.click(function() {
              if (calIcon.data('integrated')) {
                calIcon.data('integrated', false);
                calIcon.removeClass('uiIconPLFBlue');
                calIcon.attr('data-original-title', calIcon.data('title-add'));
              } else {
                calIcon.data('integrated', true);
                calIcon.addClass('uiIconPLFBlue');
                calIcon.attr('data-original-title', calIcon.data('title-remove'));
              }
              calIcon.tooltip('show');
              
              editInline.saveTaskDetailFunction({'name': 'calendarIntegrated', 'value': calIcon.data('integrated')});
            });
            
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

            var $taskDetailContainer = $('#taskDetailContainer, [data-taskid]');
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
                    editOptions.emptytext = " Due date";
                    editOptions.mode = 'popup';
                }
                if (fieldName == 'status') {
                    var currentStatus = $this.attr('data-val');
                    editOptions.value = currentStatus;
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
                if (fieldName == 'tags' || fieldName == 'labels') {
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
                    }
                }
                if (fieldName == 'title') {
                	editOptions.emptyclass = '';
                }
                if (fieldName == 'tags') {
                    editOptions.emptytext = 'Tags';
                    editOptions.selectize = {
                        create: true,
                        valueField: 'id',
                        labelField: 'text',
                        searchField: 'text',
                        options: [],
                        load: function(query, callback) {
                            $.ajax({
                                url: $rightPanel.jzURL('TaskController.findTags'),
                                data: {keyword: query},
                                type: 'GET',
                                error: function() {
                                    callback();
                                },
                                success: function(res) {
                                    callback(res);
                                }
                            });
                        }
                    };

                } else if (fieldName == 'labels') {
                    var isLoaded = false;
                    var allLabels = {};
                    var opts = $this.data('selectizie-opts');
                    $.each(opts, function(index, val) {
                        allLabels[val.id] = val;
                    });

                    editOptions.emptyText = 'Labels';
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

                $this.editable(editOptions);
                $this.on('shown', function (e, editable) {
                    if (editable != undefined) {
                        $this.parent().removeClass('inactive').addClass('active');
                    }
                }).on('hidden', function (e, editable) {
                    if (editable != undefined) {
                        $this.parent().removeClass('active').addClass('inactive');
                    }
                });
                if (fieldName == 'project') {
                    var enableEditProject = function($editable) {
                        $editable.editable('enable');
                    };
                    var disableEditProject = function($editable) {
                        $editable.editable('disable');

                        $editable.off('click', 'a.removeProject').on('click', 'a.removeProject', function(e) {
                            $editable.jzAjax('TaskController.saveTaskInfo()', {
                                data: {taskId: taskId, name: 'project', value: 0},
                                method: 'POST',
                                success: function(e) {
                                    $editable.find('li').html('No Project').removeClass('active').addClass('muted');
                                    $editable.editable('setValue', 0);
                                    enableEditProject($editable);
                                }, 
                                error: function(xhr) {
                                  editInline.taApp.showWarningDialog(xhr.responseText);
                                }
                            });
                        });
                    };
                    $this.on('save', function(e, params) {
                        if (params.newValue > 0) {
                            disableEditProject($this);
                        } else {
                            enableEditProject($this);
                        }
                    });

                    var val = $this.editable('getValue', true);
                    if (val > 0) {
                        if ($this.find('a.removeProject').length == 0) {
                            var $close = $('<a class="removeProject" href="javascript:void(0)"><i class="uiIconClose"></i></a>');
                            $this.find('li').addClass('active').append($close);
                        }
                        disableEditProject($this);
                    }
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
                if(fieldName == 'manager' || fieldName == 'participator') {
                    var findUserURL = $this.jzURL('UserController.findUser');
                    var getDisplayNameURL = $this.jzURL('UserController.getDisplayNameOfUser');
                    editOptions.showbuttons = true;
                    editOptions.emptytext = (fieldName == 'manager' ? "No Manager" : "No Participator");
                    editOptions.source = findUserURL;
                    editOptions.select2= {
                        multiple: true,
                        allowClear: true,
                        placeholder: 'Select an user',
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
                    editOptions.emptytext = " Due date";
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
