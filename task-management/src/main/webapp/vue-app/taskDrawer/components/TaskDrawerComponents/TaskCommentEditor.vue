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
        let extraPlugins = 'simpleLink,suggester,widget,embedsemantic';
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

          autoGrow_onStartup: true,
          on: {
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