define('x_editable_selectize', ['SHARED/jquery', 'SHARED/edit_inline_js', 'SHARED/selectize', 'SHARED/taskLocale'], function(jquery, editinline, selectize, locale) {
    (function ($) {
        "use strict";

        var Selectize = function (options) {
            var selectize = options.selectize || {};
            this.selectizeOptions = $.extend({}, Selectize.defaultOptions, selectize);
            this.init('selectize', options, Selectize.defaults);
        };

        //inherit from Abstract input
        $.fn.editableutils.inherit(Selectize, $.fn.editabletypes.abstractinput);

        $.extend(Selectize.prototype, {
            /**
             Renders input from tpl

             @method render()
             **/
            render: function() {

            },

            /**
             Default method to show value in element. Can be overwritten by display option.

             @method value2html(value, element)
             **/
            value2html: function(value, element) {
                if (value.length == 0 || value == '') {
                    $(element).html(this.options.emptytext);
                    return;
                }

                var _this = this;
                var html = [];
                $.each(value, function(index, val) {
                    var span;
                    if (_this.options.value2html) {
                        span = _this.options.value2html(val);
                    } else {
                        var encoder = $('<div></div>');
                        span = '<div  class="label primary">'+ encoder.text(val).html() +'</div>';
                    }
                    html.push(span);
                });
                var $html = html.join("\n");
                $(element).html($html);
            },

            /**
             Converts value to string.
             It is used in internal comparing (not for sending to server).

             @method value2str(value)
             **/
            value2str: function(value) {
                if (value == undefined) {
                    return;
                }
                return value.join(this.selectizeOptions.delimiter);
            },

            /*
             Converts string to value. Used for reading value from 'data-value' attribute.

             @method str2value(str)
             */
            str2value: function(str) {

                // this is mainly for parsing value defined in data-value attribute.
                // If you will always set value by javascript, no need to overwrite it
                if (!str) {
                    return [];
                }
                str = "" + str;
                if (str.length == 0) {
                    return [];
                }
                return str.split(this.selectizeOptions.delimiter);
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
                this.$input.find('[name="field_input"]').val(value);

                // Build Selectize
                this.selectize = this.$input.find('[name="field_input"]').selectize(this.selectizeOptions);
            },

            /**
             Returns value of input.

             @method input2value()
             **/
            input2value: function() {
                var val = this.$input.find('[name="field_input"]').val();
                return val.split(this.selectizeOptions.delimiter);
            },

            /**
             Activates input: sets focus on the first field.

             @method activate()
             **/
            activate: function() {
                this.$input.find('[name="field_input"]').focus();
            },

            /**
             Attaches handler to submit form in case of 'showbuttons=false' mode

             @method autosubmit()
             **/
            autosubmit: function() {
                /*this.$input.keydown(function (e) {
                    if (e.which === 13) {
                        var $this = $(this);
                        $this.closest('form').submit();
                    }
                });*/
            }
        });

        Selectize.defaultOptions = $.extend({}, selectize.defaults, {
            options: [],
            wrapperClass: 'exo-mentions dropdown',
            dropdownClass: 'dropdown-menu uiDropdownMenu',
            inputClass: 'selectize-input replaceTextArea ',
            openOnFocus: false,
            create: true,
            plugins: {
                remove_button: {
                    label: '&#120;',
                    className : 'removeValue'
                }
            },
            hideSelected: true,
            closeAfterSelect: true,
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
                    return '<li class="create"><a href="javascript:void(0)">' + locale.createLabel + ' <strong>' + escape(data.input) + '</strong>&hellip;</a></li>';
                }
            },
            onInitialize: function() {
                var self      = this;
                var settings  = self.settings;
                var $dropdown         = self.$dropdown;
                var $dropdown_content = self.$dropdown_content;
                $dropdown_content.remove();
                $dropdown_content = $('<ul>').addClass(settings.dropdownContentClass).appendTo($dropdown);
                self.$dropdown_content = $dropdown_content;
                setTimeout(function() {
                    self.focus();
                }, 100);
            },
            score: function(search) {
                var score = this.getScoreFunction(search);
                return function(item) {
                    var s = score(item);
                    return s;
                };
            }
        });

        Selectize.defaults = $.extend({}, $.fn.editabletypes.abstractinput.defaults, {
            tpl: '<div>' +
                 '   <input type="text" name="field_input" value=""/>' +
                 '</div>',
            inputclass: '',
            emptytext: '',
            selectize: {},
            value2html: false
        });

        $.fn.editabletypes.selectize = Selectize;

    }(jquery));
    return jquery;
});