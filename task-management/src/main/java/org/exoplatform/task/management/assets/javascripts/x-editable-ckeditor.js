define('x_editable_ckeditor', ['SHARED/jquery', 'SHARED/edit_inline_js', 'SHARED/task_ck_editor'], function(jquery, editinline) {
    (function ($) {
        "use strict";

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

                this.$input.ckeditor({
                    customConfig: '',
                    stylesSet: [],
                    removeButtons: 'Cut,Copy,Paste,Undo,Redo,Anchor,Underline,Subscript,Superscript,Link,Unlink,About'
                });
                var _this = this;
                CKEDITOR.on('instanceReady', function(e) {
                    _this.$input.parent().find('> .cke').removeClass('cke');
                });
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