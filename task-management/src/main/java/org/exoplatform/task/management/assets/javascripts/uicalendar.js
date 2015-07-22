define('task_ui_calendar', ['jquery', 'SHARED/uiCalendar', 'SHARED/bootstrap_datepicker'],
    function($, uiCalendar) {
        var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        var UITaskCalendar = $.extend({}, uiCalendar, {
            calendarId: 'UITaskCalendarControl',
            eXoName: 'eXo.webui.UITaskCalendar',
            init : function(field, isDisplayTime, datePattern, value, monthNames) {
                if (monthNames == undefined || !monthNames) {
                    monthNames = months.join(',') + ',';
                }
                uiCalendar.init.call(this, field, isDisplayTime, datePattern, value, monthNames);
            },
            hide: function() {
                // Do not hide calendar
            },
            destroy: function() {
                uiCalendar.hide.apply(this, arguments);
            },
            create : function() {
                uiCalendar.create.apply(this, arguments);
                var clndr = document.getElementById(this.calendarId);
                clndr.removeAttribute('style');
            },
            show: function() {
                uiCalendar.show.apply(this, arguments);
                var clndr = document.getElementById(this.calendarId);
                clndr.removeAttribute('style');
                clndr.firstChild.removeAttribute('style');
                clndr.firstChild.firstChild.removeAttribute('style');
                clndr.firstChild.style.position = 'relative';
            },
            renderCalendar: function() {
                var table = uiCalendar.renderCalendar.apply(this, arguments);
                table = table.replace(/eXo\.webui\.UICalendar/g, this.eXoName);
                return table;
            }
        });

        var eXo = window.eXo || {};
        eXo.webui = eXo.webui || {};
        eXo.webui.UITaskCalendar = UITaskCalendar;

        return UITaskCalendar;
    }
);