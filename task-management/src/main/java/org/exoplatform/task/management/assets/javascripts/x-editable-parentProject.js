define('x_editable_select3', ['SHARED/jquery', 'SHARED/edit_inline_js'], function(jquery) {
    (function ($) {
        "use strict";

        var ParentProject = function (options) {
            this.init('ParentProject', options, ParentProject.defaults);
        };

        //inherit from Abstract input
        $.fn.editableutils.inherit(ParentProject, $.fn.editabletypes.abstractinput);

        $.extend(ParentProject.prototype, {
            /**
             Renders input from tpl

             @method render()
             **/
            render: function() {
                var _this = this;
                var timeout = false;
                var currentVal = false;
                //TODO: this is tip-trict and it's not good, please find better solution
                var projectId = _this.$input.closest('[data-projectid]').data('projectid');
                if (projectId == undefined) {
                    projectId = 0;
                }
                var handler = function(e) {
                    var val = _this.$input.find('[name="search"]').val();
                    if (currentVal !== val) {
                        currentVal = val;
                        if (timeout !== false) {
                            clearTimeout(timeout);
                            timeout = false;
                        }
                        timeout = setTimeout(function () {
                            var text = _this.$input.find('[name="search"]').val();
                            _this.$input.find('.uiDropdownMenu').jzLoad('ProjectController.findProject()',
                                {
                                    keyword: text,
                                    currentProject: projectId
                                },
                                function () {
                                    _this.$input.find('.uiDropdownMenu').show();
                                }
                            );
                        }, 50);
                    }
                    if (e.which == 38 || e.which == 40) {
                        var isUp = (e.which == 38);
                        var $matches = _this.$input.find('[data-matched="true"]');
                        var index = false;
                        for (var i = 0; i < $matches.length; i++) {
                            var $e = $($matches.get(i));
                            if ($e.hasClass('active')) {
                                index = isUp ? i - 1 : i + 1;
                                $e.removeClass('active');
                                break;
                            }
                        }
                        if (index == false) {
                            index = 0;
                        } else if (index < 0) {
                            index = $matches.length - 1;
                        } else if (index >= $matches.length) {
                            index = 0;
                        }
                        $($matches.get(index)).addClass('active');
                    }
                };
                this.$input.find('[name="search"]').focus(handler).keyup(handler);
                this.$input.on('click', '[data-projectid]', function(e) {
                    var $a = $(e.target || e.srcElement).closest('[data-projectid]');
                    var projectId = $a.data('projectid');
                    _this.$input.find('[name="parent_project_id"]').val(projectId);
                    _this.$input.find('.uiDropdownMenu').hide();
                    _this.$input.closest('form').submit();
                });
            },

            /**
             Default method to show value in element. Can be overwritten by display option.

             @method value2html(value, element)
             **/
            value2html: function(value, element) {
                var options = this.options;
                $(element).data('value', value);
                $(element).jzLoad('ProjectController.getBreadCumbs()', {id: value, isBreadcrumb: options.breadcrumb}, function() {
                    if(!options.breadcrumb && value > 0) {
                        var $close = $('<a class="removeProject" href="javascript:void(0)"><i class="uiIconClose"></i></a>');
                        $(element).find('li').append($close);
                    }
                });
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

                var val = parseInt(str);
                return val;
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
                this.$input.find('[name="parent_project_id"]').val(value);
            },

            /**
             Returns value of input.

             @method input2value()
             **/
            input2value: function() {
                var val = this.$input.find('[name="parent_project_id"]').val();
                return val;
            },

            /**
             Activates input: sets focus on the first field.

             @method activate()
             **/
            activate: function() {
                this.$input.find('[name="search"]').focus();
            },

            /**
             Attaches handler to submit form in case of 'showbuttons=false' mode

             @method autosubmit()
             **/
            autosubmit: function() {
                this.$input.keydown(function (e) {
                    if (e.which === 13) {
                        var $this = $(this);
                        var projectId = $this.find('.active[data-matched="true"]').first().data('projectid');
                        if (projectId == undefined) {
                            var projectId = $this.find('[data-matched="true"]').first().data('projectid');
                        }
                        if (projectId != undefined) {
                            $this.find('[name="parent_project_id"]').val(projectId);
                        }
                        $this.closest('form').submit();
                    }
                });
            }
        });

        ParentProject.defaults = $.extend({}, $.fn.editabletypes.abstractinput.defaults, {
            tpl: '<div>' +
                 '   <div>' +
                 '       <input type="hidden" name="parent_project_id" value=""/>' +
                 '       <input type="text" name="search" autocomplete="off"/>' +
                 '   </div>' +
                 '   <div class="uiDropdownMenu dropdown-menu " style="z-index: 9; display: none;width:100%"></div>'+
                 '</div>',
            inputclass: '',
            breadcrumb: true
        });

        $.fn.editabletypes.ParentProject = ParentProject;

    }(jquery));
    return jquery;
});