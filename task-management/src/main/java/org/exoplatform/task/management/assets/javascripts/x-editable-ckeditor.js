define('x_editable_ckeditor', ['SHARED/jquery', 'SHARED/edit_inline_js', 'SHARED/commons-editor'], function(jquery, editinline, ckeditor) {
    (function ($) {
        "use strict";

        CKEDITOR.basePath = '/commons-extension/ckeditor/';
        var CKEditor = function (options) {
            this.init('ckeditor', options, CKEditor.defaults);
        };

        //inherit from Abstract input
        $.fn.editableutils.inherit(CKEditor, $.fn.editabletypes.textarea);

        $.extend(CKEditor.prototype, {
            /**
             Renders input from tpl

             @method render()
             **/
            render: function() {
                this.$input.first().attr('name', 'TaskDescription');
                var editor = this.$input.first().ckeditor({
                  customConfig: '/commons-extension/ckeditorCustom/config.js',
                  toolbarLocation: 'top',
                  removePlugins: 'suggester,simpleLink,confighelper',
                  toolbar: [
                    ['Styles'],
                    ['Bold','Italic','Underline'],
                    ['TextColor'],
                    ['NumberedList','BulletedList']
                  ],
                  on: {
                      blur: function () {
                          $(document.body).trigger('click');
                      }
                  }
                });
                editor.editor.setData(this.options.scope.innerHTML);
            },
            value2html: function(value, element) {
                /*var html = '', lines;
                if(value) {
                    lines = value.split("\n");
                    for (var i = 0; i < lines.length; i++) {
                        lines[i] = $('<div>').text(lines[i]).html();
                    }
                    html = lines.join('<br>');
                }*/
                $(element).html(value);
            },

            html2value: function(html) {
                if(!html) {
                    return '';
                }

                /*var regex = new RegExp(String.fromCharCode(10), 'g');
                var lines = html.split(/<br\s*\/?>/i);
                for (var i = 0; i < lines.length; i++) {
                    var text = $('<div>').html(lines[i]).text();

                    // Remove newline characters (\n) to avoid them being converted by value2html() method
                    // thus adding extra <br> tags
                    text = text.replace(regex, '');

                    lines[i] = text;
                }
                return lines.join("\n");*/
                return html;
            }
        });

        CKEditor.defaults = $.extend({}, $.fn.editabletypes.textarea.defaults, {
            //tpl:'<div><textarea></textarea></div>',
            ckeditor: {}
        });

        $.fn.editabletypes.ckeditor = CKEditor;

    }(jquery));
    return jquery;
});