define('ta_edit_inline',
    ['jquery', 'SHARED/edit_inline_js', 'selectize', 'x_editable_select3', 'x_editable_selectize'],
    function($, editinline, selectize) {

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
            var data = params;
            params.pk = currentTaskId;
            data.taskId = currentTaskId;
            $('#taskDetailContainer').jzAjax('TaskController.saveTaskInfo()',{
                data: data,
                method: 'POST',
                traditional: true,
                success: function(response) {
                    d.resolve();
                },
                error: function(jqXHR, textStatus, errorThrown ) {
                    d.reject('update failure: ' + jqXHR.responseText);
                }
            });
            return d.promise();
        };

        editInline.initWorkPlan = function(taskId) {
            var $workplan = $('.workPlan');

            var saveWorkPlan = function(plan) {
                var data = {
                    pk : taskId,
                    name : 'workPlan'
                };
                if (plan) {
                    data.value = plan;
                }
                var callback = saveTaskDetailFunction(data);

                callback.done(function() {
                    $centerPanel.find('.selected .viewTaskDetail').click();
                }).fail(function() {
                    alert('fail to update');
                });
            }

            var $removeWorkPlan = $workplan.find('.removeWorkPlan');
            $removeWorkPlan.click(function() {
                saveWorkPlan(null);
            });

            //
            var $duration = $workplan.find('.duration');
            var currStart = $workplan.data('startdate').split('-');
            var startDate = new Date(currStart[0], currStart[1] - 1, currStart[2], currStart[3], currStart[4]);
            var endDate = new Date();
            endDate.setTime(startDate.getTime() + parseFloat($workplan.data('duration')));
            //
            $duration.popover({
                html : true,
                content : $('.rangeCalendar').html()
            }).on('shown.bs.popover', function() {
                //setup calendar
                $workplan.find('.popover .calendar').each(function() {
                    var $calendar = $(this);
                    var $popover = $calendar.closest('.popover');

                    if ($calendar.hasClass('fromCalendar')) {
                        $calendar.datepicker('setDate', startDate);
                    } else {
                        $calendar.datepicker('setDate', endDate);
                    }
                    $calendar.datepicker('clearDates');

                    //
                    $calendar.on('changeDate', function(e) {
                        var dateFrom, dateTo;
                        if ($calendar.hasClass('fromCalendar')) {
                            dateFrom = $calendar.datepicker('getDate');
                            dateTo = $calendar.parent().find('.toCalendar').datepicker('getDate');
                        } else {
                            dateFrom = $calendar.parent().find('.fromCalendar').datepicker('getDate');
                            dateTo = $calendar.datepicker('getDate');
                        }

                        if (dateFrom && dateTo) {
                            var allDay = $popover.find('input[name="allDay"]').is(':checked');
                            var timeFrom = [0, 0], timeTo = [23, 59];
                            if (!allDay) {
                                var $selectors = $popover.find('.timeSelector');
                                timeFrom = $selectors.first().editable('getValue').timeFrom.split(':');
                                timeTo = $selectors.last().editable('getValue').timeTo.split(':');
                            }

                            var setTime = function(dt, time) {
                                dt.setHours(time[0]);
                                dt.setMinutes(time[1]);
                                dt.setMilliseconds(0);
                            }
                            setTime(dateFrom, timeFrom);
                            setTime(dateTo, timeTo);

                            saveWorkPlan([dateFrom.getTime(), dateTo.getTime()]);
                        }
                    });
                });

                //
                var $allDay = $workplan.find('.popover input[type="checkbox"]');
                $allDay.on('change', function(e) {
                    var $chk = $(e.target);
                    if ($chk.is(':checked')) {
                        $workplan.find('.popover .timeSelector').hide();
                    } else {
                        $workplan.find('.popover .timeSelector').show();
                    }
                });
                if (startDate.getHours() == 0 && startDate.getMinutes() == 0 &&
                    endDate.getHours() == 23 && endDate.getMinutes() == 59) {
                    $allDay[0].checked = true;
                    $allDay.trigger('change');
                }

                //setup timeSelector
                var tmp = new Date(0,0,0,0,0,0,0);
                var times = [], d = tmp.getDate();
                while (tmp.getDate() == d) {
                    var h = tmp.getHours(), m = tmp.getMinutes();
                    var t = (h < 10 ? '0' + h : h) + ':' + (m < 10 ? '0' + m : m);
                    times.push({'value': t, 'text': t});
                    tmp.setMinutes(tmp.getMinutes() + 30);
                }
                times.push({'value': '23:59', 'text': '23:59'});

                var options = {
                    source : times,
                    mode : 'inline',
                    showbuttons : false
                }

                //default time
                var getIdx = function(date) {
                    var idx = date.getHours() * 2;
                    if (date.getMinutes() == 59) {
                        idx += 2;
                    } else if (date.getMinutes() == 30) {
                        idx += 1;
                    }
                    return idx;
                }

                $workplan.find('.popover .timeSelector').each(function() {
                    var $selector = $(this);
                    if ($selector.data('name') == 'timeFrom') {
                        options.value = times[getIdx(startDate)].value;
                    } else {
                        options.value = times[getIdx(endDate)].value;
                    }
                    $selector.editable(options);
                });
            });
        };

        editInline.initDueDate = function() {
            var $dueDate = $('.editable[data-name="dueDate"]');
            $dueDate.on('shown', function(e) {
                var $content = $dueDate.parent().find('.popover .popover-content');
                if ($content.find('.calControl').length == 0) {
                    $content.html($content.html() + "<div>"
                        + $dueDate.parent().find('.dueDateControl').html()
                        + "</div>");
                }
            });

            $dueDate.parent().on('click', '.calControl', function(e) {
                var $btn = $(e.target);
                var next = new Date();
                if ($btn.hasClass('none')) {
                    next = null;
                } else if ($btn.hasClass('tomorrow')) {
                    next.setDate(next.getDate() + 1);
                } else if ($btn.hasClass('nextWeek')) {
                    next.setDate(next.getDate() + 7);
                }

                $dueDate.editable('setValue', next);
            });
        };


        var selectizeOptions = {
            valueField: 'id',
            labelField: 'text',
            searchField: ['text', 'id'],
            openOnFocus: false,
            wrapperClass: 'exo-mentions dropdown',
            dropdownClass: 'dropdown-menu uiDropdownMenu autocomplete-menu',
            inputClass: 'selectize-input replaceTextArea',
            create: false,
            hideSelected: true,
            closeAfterSelect: true,
            plugins: {
                task_remove_button: {
                    label: '<i class="uiIconClose uiIconLightGray"></i>',
                    className : 'removeValue'
                }
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
                    if (assignee == '' && coworkders == '') {
                        $editable.find('.unassigned').removeClass('hidden');
                        $editable.find('.assigned').addClass('hidden');
                    } else {
                        $editable.find('.unassigned').addClass('hidden');
                        $editable.find('.assigned').removeClass('hidden');
                    }
                    if (coworkders == '') {
                        coworkders = [];
                    } else {
                        coworkders = coworkders.split(',');
                    }
                    var numberCoWorker = coworkders.length;
                    if (assignee == '') {
                        assignee = coworkders[0];
                        numberCoWorker--;
                    }

                    // Update avatar and display name
                    var assg = options[assignee];
                    $editable.find('.assigned img').attr('src', assg.avatar);
                    $editable.find('.editAssignee').html(numberCoWorker == 0 ? assg.text : '+' + numberCoWorker + ' Coworkers');
                },
                error: function(response) {
                    alert('can not save co-workers');
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
            //initWorkPlan(taskId);

            //tabs in task detail
            $('.taskTabs a').click(function(e) {
                e.preventDefault();

                var $tab = $(this);
                if ($tab.attr('href') == '.taskLogs') {
                    $tab.closest('.task-detail').find('.taskLogs').jzLoad('TaskController.renderTaskLogs()', {taskId: taskId}, function() {
                        $tab.tab('show');
                    });
                } else {
                    $tab.tab('show');
                }
            });

            var $taskDetailContainer = $('#taskDetailContainer, [data-taskid]');
            $taskDetailContainer.find('.editable').each(function() {
                var $this = $(this);
                var dataType = $this.attr('data-type');
                var fieldName = $this.attr('data-name');
                var editOptions = getDefaultOptionForType(dataType);
                var editOptions = $.extend({}, editOptions, {
                    pk: taskId,
                    url: editInline.saveTaskDetailFunction
                });
                if (fieldName == 'dueDate') {
                    editOptions.emptytext = "no Duedate";
                    editOptions.mode = 'popup';
                }
                if (fieldName == 'status') {
                    var currentStatus = $this.attr('data-val');
                    editOptions.value = currentStatus;
                }
                if (fieldName == 'priority') {
                    var priority = [];
                    $.each($this.data('priority').split(','), function (idx, elem) {
                        priority.push({'text': elem, 'value': elem});
                    });
                    //
                    editOptions.source = priority;
                    editOptions.success = function (response, newValue) {
                        $this.parent().find('i').attr('class', 'uiIconColorPriority' + newValue);
                    }
                }
                if (fieldName == 'tags') {
                    editOptions.emptytext = 'No Tags';
                    editOptions.success = function (response, newValue) {
                        var isEmpty = newValue.length == 0 || newValue[0] == '';
                        var $i = $this.parent().find('.icon-hash');
                        if (isEmpty) {
                            $i.removeClass('hidden');
                        } else {
                            $i.addClass('hidden');
                        }
                    }
                }

                $this.editable(editOptions);
                $this.on('shown', function (e, editable) {
                    $this.parent().removeClass('inactive').addClass('active');
                }).on('hidden', function (e, editable) {
                    $this.parent().removeClass('active').addClass('inactive');
                });
            });
            editInline.initDueDate();
            editInline.initAssignment(taskId);
        };

        editInline.saveProjectDetailFunction = function(params) {
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
                    if (params.name == 'name') {
                        $leftPanel
                            .find('li.project-item a.project-name[data-id="'+ data.projectId +'"]')
                            .html(data.value);
                    }
                },
                error: function(jqXHR, textStatus, errorThrown ) {
                    d.reject('update failure: ' + jqXHR.responseText);
                }
            });
            return d.promise();
        };

        editInline.initEditInlineForProject = function(projectId) {
            var $project = $rightPanel.find('[data-projectid]');
            $project.find('.editable').each(function(){
                var $this = $(this);
                var dataType = $this.attr('data-type');
                var fieldName = $this.attr('data-name');
                var editOptions = getDefaultOptionForType(dataType);

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
                    editOptions.emptytext = "no Duedate";
                    editOptions.mode = 'popup';
                }
                $this.editable(editOptions);

                $this.on('shown', function(e, editable) {
                    $this.parent().removeClass('inactive').addClass('active');
                }).on('hidden', function(e, editable) {
                    $this.parent().removeClass('active').addClass('inactive');
                });
            });
        };

        return editInline;
    }
);
