define('x_editable_calendar', ['SHARED/jquery', 'SHARED/edit_inline_js', 'task_ui_calendar', 'SHARED/bootstrap_datepicker'],
    function(jquery, editinline, uiCalendar) {
        (function ($) {
            "use strict";

            //. Use Dateparser and formatter of bootstrap-datepicker
            var DPG = $.fn.datepicker.DPGlobal;

            // This is workaround because the name 'datepicker' is used in both bootstrap-datepicker and jquery-ui.
            // I clone $.fn.datepicker.DPGlobal to other property (in gatein-resource.xml) to use here
            if (DPG == undefined) {
                DPG = $.fn.task_datepicker.DPGlobal;
            }

            var activeClass = 'active';
            var isNone = function(date) {
                return date == undefined || !date;
            };
            var isToday = function(date) {
                if (date == undefined || !date) return false;
                var current = new Date();
                return (current.getDate() == date.getDate()
                            && current.getMonth() == date.getMonth()
                            && current.getFullYear() == date.getFullYear());
            };
            var isTomorrow = function(date) {
                if (date == undefined || !date) return false;
                var current = new Date();
                current = new Date(current.getTime() + 86400000);

                return (current.getDate() == date.getDate()
                    && current.getMonth() == date.getMonth()
                    && current.getFullYear() == date.getFullYear());
            };
            var isNextWeek = function(date, firstDayOfWeek) {
                if (date == undefined || !date) return false;
                var current = new Date();
                var dayOfWeek = current.getDay();
                var dayDiff = (dayOfWeek - firstDayOfWeek + 7) % 7;
                var startOfWeek = new Date(current.getTime() - dayDiff*86400000 + 7*86400000);
                startOfWeek.setHours(0, 0, 0, 0);
                var endOfWeek = new Date(startOfWeek.getTime() + 7*86400000 - 1);

                return (date.getTime() > startOfWeek.getTime()
                        && date.getTime() < endOfWeek.getTime());
            };

            var Calendar = function (options) {
                this.options = options;
                this.init('calendar', options, Calendar.defaults);
            };

            //inherit from Abstract input
            $.fn.editableutils.inherit(Calendar, $.fn.editabletypes.abstractinput);

            $.extend(Calendar.prototype, {
                /**
                 Renders input from tpl

                 @method render()
                 **/
                render: function() {
                    var _this = this;
                    this.$input.on('click', '[data-date]', function(e) {
                        var $date = $(e.target).closest('[data-date]');
                        if ($date.hasClass(activeClass)) return;
                        var val = $date.data('date');
                        var nextTime = new Date().getTime();
                        var oneDay = 86400000; // = 24 * 60 * 60 * 1000;
                        if (val == 'none') {
                            _this.$input.find('input[name="calendar"]').val('').change();
                            return;
                        } else if (val == 'tomorrow'){
                            nextTime += oneDay;
                        } else if (val == 'nextweek') {
                            nextTime += oneDay * 7;
                        }

                        var nextDate = new Date(nextTime);

                        uiCalendar.setDate(nextDate.getFullYear(), nextDate.getMonth() + 1, nextDate.getDate());
                    });
                },

                /**
                 Default method to show value in element. Can be overwritten by display option.

                 @method value2html(value, element)
                 **/
                value2html: function(value, element) {
                    //TODO: check if current date is today, tomorrow or next week
                    var date = DPG.parseDate(value, this.options.format, 'en');
                    var dateString = DPG.formatDate(date, this.options.viewformat, 'en');
                    $(element).html(dateString);
                },

                html2value: function(html) {
                    var date = DPG.parseDate(html, this.options.viewformat, 'en');
                    var format = this.options.format;
                    format = format.toLowerCase();
                    var val = DPG.formatDate(date, format, 'en');
                    return val;
                },

                /**
                 Converts value to string.
                 It is used in internal comparing (not for sending to server).

                 @method value2str(value)
                 **/
                value2str: function(value) {
                    return "" + value;
                },

                /*
                 Converts string to value. Used for reading value from 'data-value' attribute.

                 @method str2value(str)
                 */
                str2value: function(str) {

                    // this is mainly for parsing value defined in data-value attribute.
                    // If you will always set value by javascript, no need to overwrite it
                    return str;
                },

                /**
                 Sets value of input.

                 @method value2input(value)
                 @param {mixed} value
                 **/
                value2input: function(value) {
                    if(value === false) {
                        return;
                    }
                    var $in = this.$input.find('input[name="calendar"]');
                    $in.val(value);
                    uiCalendar.init($in[0], false, this.options.format, '');
                    var date = value ? uiCalendar.currentDate : false;
                    var selected = '';
                    if (isNone(date)) {
                        selected = 'none';
                    } else if (isToday(date)) {
                        selected = 'today';
                        $('.uiCalendarComponent .highLight.today').addClass('selected');
                    } else if (isTomorrow(date)) {
                        selected = 'tomorrow';
                    } else if (isNextWeek(date, uiCalendar.getFirstDayOfWeek() - 1)) {
                        selected = 'nextweek';
                    }
                    $('[data-date="'+selected+'"]').addClass(activeClass);
                },

                /**
                 Returns value of input.

                 @method input2value()
                 **/
                input2value: function() {
                    var val = this.$input.find('[name="calendar"]').val();
                    return val;
                },

                /**
                 Attaches handler to submit form in case of 'showbuttons=false' mode

                 @method autosubmit()
                 **/
                autosubmit: function() {
                    this.$input.keydown(function (e) {
                        if (e.which === 13) {
                            var $this = $(this);
                            $this.closest('form').submit();
                        }
                    });
                    this.$input.on('change', '[name="calendar"]', function(e){
                        $(this).closest('form').submit();
                    });
                }
            });

            Calendar.defaults = $.extend({}, $.fn.editabletypes.abstractinput.defaults, {
                tpl: '' +
                    '   <div class="header  nav-inline">'+
                    '       <a class="" data-date="none" href="javascript:void(0)">None</a>' +
                    '       <a class="" data-date="today" href="javascript:void(0)">Today</a>' +
                    '       <a class="" data-date="tomorrow" href="javascript:void(0)">Tomorrow</a>' +
                    '       <a class="" data-date="nextweek" href="javascript:void(0)">Next week</a>' +
                    '   </div>' +
                    '   <div>' +
                    '       <input type="hidden" name="calendar" value=""/>' +
                    '   </div>' +
                    '',
                inputclass: '',
                format: 'yyyy-MM-dd',
                viewformat: 'dd M yyyy'
            });

            $.fn.editabletypes.calendar = Calendar;

        }(jquery));
        return jquery;
    }
);
