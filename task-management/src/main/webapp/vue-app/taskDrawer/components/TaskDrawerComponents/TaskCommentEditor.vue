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
    <div
      v-show="editorReady"
      :class="charsCount > maxLength ? 'tooManyChars' : ''"
      class="activityCharsCount"
      style="">
      {{ charsCount }}{{ maxLength > -1 ? ' / ' + maxLength : '' }}
      <i class="uiIconMessageLength"></i>
    </div>
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
      maxLength: {
        type: Number,
        default: -1
      }
    },
    data() {
      return {
        inputVal: this.value,
        id: `commentContent${parseInt(Math.random() * 10000).toString()}`,
        charsCount: 0,
        editorReady: false,
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
            instanceReady: function() {
              self.editorReady = true;
            },
            change: function(evt) {
              const newData = evt.editor.getData();

              self.inputVal = newData;

              let pureText = newData ? newData.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
              const div = document.createElement('div');
              div.innerHTML = pureText;
              pureText = div.textContent || div.innerText || '';
              self.charsCount = pureText.length;
            },
            destroy: function () {
              self.inputVal = '';
              self.charsCount = 0;
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
