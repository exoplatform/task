<template>
  <div>
    <textarea 
      ref="editor"
      :id="id" 
      v-model="inputVal" 
      :placeholder="placeholder" 
      cols="30" 
      rows="10" 
      class="textarea"></textarea>
  </div>
</template>

<script>
  import {findUsersToMention} from '../../taskDrawerApi';

  export default {
    props: {
      value: {
        type: String,
        default: ''
      },
      reset: {
        type: Boolean,
        default: false
      },
      placeholder: {
        type: String,
        default: ''
      },
    },
    data() {
      return {
        inputVal: this.value,
        id: `commentContent${parseInt(Math.random() * 10000).toString()}`,
      };
    },
    watch: {
      inputVal(val) {
        this.$emit('input', val);
      },
      reset() {
        CKEDITOR.instances[this.id].destroy(true);
        this.initCKEditor();
      },
    },
    mounted() {
      this.initCKEditor();
      $('body').suggester('addProvider', 'task:people', function (query, callback) {
        const _this = this;
        findUsersToMention(query).then((data) => {
          const result = [];
          for (let i = 0; i < data.length; i++) {
            const d = data[i];
            const item = {
              uid: d.id.substr(1),
              name: d.name,
              avatar: d.avatar
            };
            result.push(item);
          }
          callback.call(_this, result);
        });
      });
    },
    methods: {
      initCKEditor: function () {
        let extraPlugins = 'simpleLink,selectImage,suggester';
        const windowWidth = $(window).width();
        const windowHeight = $(window).height();
        if (windowWidth > windowHeight && windowWidth < 768) {
          // Disable suggester on smart-phone landscape
          extraPlugins = 'simpleLink,selectImage';
        }
        // this line is mandatory when a custom skin is defined
        CKEDITOR.basePath = '/commons-extension/ckeditor/';
        const self = this;
        $(this.$refs.editor).ckeditor({
          customConfig: '/commons-extension/ckeditorCustom/config.js',
          extraPlugins: extraPlugins,
          removePlugins: 'confirmBeforeReload,maximize,resize',
          autoGrow_onStartup: false,
          autoGrow_maxHeight: 300,
          on: {
            focus: function (evt) {
              const el = $('.cke_toolbar_last');
              $(el[0]).remove();
              // Show the editor toolbar, except for smartphones in landscape mode
              if ($(window).width() > 767 || $(window).width() < $(window).height()) {
                evt.editor.execCommand('autogrow');
                const $content = $(`#${  evt.editor.id  }_contents`);
                const contentHeight = $content.height();
                const $ckeBottom = $(`#${  evt.editor.id  }_bottom`);
                $ckeBottom[0].style.display = "block";
                $ckeBottom.animate({
                  height: "39"
                }, {
                  step: function (number) {
                    $content.height(contentHeight - number);
                    if (number >= 9) {
                      $ckeBottom.addClass('cke_bottom_visible');
                    }
                  }
                });
              } else {
                $(`#${  evt.editor.id  }_bottom`).removeClass('cke_bottom_visible');
                $(`#${  evt.editor.id  }_bottom`)[0].style.display = "none";
              }
            },
            blur: function (evt) {
              $(document.body).trigger('click');
              // Hide the editor toolbar
              if (windowWidth > 767 || windowWidth < windowHeight) {
                $(`#${  evt.editor.id  }_contents`).css('height', $(`#${  evt.editor.id  }_contents`).height() + 39);
                $(`#${  evt.editor.id  }_bottom`).css('height', '0px');
                $(`#${  evt.editor.id  }_bottom`).removeClass('cke_bottom_visible');
              }
            },
            change: function(evt) {
              const newData = evt.editor.getData();

              self.inputVal = newData;
            },
            destroy: function () {
              self.inputVal = '';
            },
          },
          suggester: {
            /* eslint-disable no-template-curly-in-string */
            renderMenuItem: '<li data-value="${uid}"><div class="avatarSmall" style="display: inline-block;"><img src="${avatar}"></div>${name} (${uid})</li>',
            renderItem: '<span class="exo-mention">${name}<a href="#" class="remove"><i class="uiIconClose uiIconLightGray"></i></a></span>',
            sourceProviders: ['task:people']
          },

        });
      },
      getMessage: function() {
        const newData = CKEDITOR.instances[this.id].getData();
        return newData ? newData.trim() : '';
      }
    }
  };
</script>