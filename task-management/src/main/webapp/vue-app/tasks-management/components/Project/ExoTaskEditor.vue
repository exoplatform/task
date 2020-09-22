<template>
  <div class="projectDescription">
    <textarea 
      id="descriptionContent" 
      ref="editor" 
      v-model="inputVal" 
      :placeholder="placeholder" 
      cols="30" 
      rows="10" 
      class="textarea"></textarea>

  </div>
</template>

<script>
  export default {
    props: {
      value: {
        type: String,
        default: ''
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
        SMARTPHONE_LANDSCAPE_WIDTH: 768,
        inputVal: this.value,
        charsCount: 0,
        editorReady: false
      };
    },
    watch: {
      inputVal(val) {
        this.$emit('input', val);
      },
      value(val) {
        // watch value to reset the editor value if the value has been updated by the component parent
        const editorData = CKEDITOR.instances['activityContent'].getData();
        if (editorData != null && val !== editorData) {
          if (val === '') {
            this.initCKEditor();
          } else {
            CKEDITOR.instances['activityContent'].setData(val);
          }
        }
      }
    },
    mounted() {
      this.initCKEditor();
    },
    methods: {
      initCKEditor: function () {
        CKEDITOR.plugins.addExternal('/commons-extension/eXoPlugins/embedsemantic/','plugin.js');
        if (typeof CKEDITOR.instances['activityContent'] !== 'undefined') {
          CKEDITOR.instances['activityContent'].destroy(true);
        }
        let extraPlugins = 'simpleLink,widget';
        const windowWidth = $(window).width();
        const windowHeight = $(window).height();
        if (windowWidth > windowHeight && windowWidth < this.SMARTPHONE_LANDSCAPE_WIDTH) {
          // Disable suggester on smart-phone landscape
          extraPlugins = 'simpleLink';
        }
        // this line is mandatory when a custom skin is defined
        CKEDITOR.basePath = '/commons-extension/ckeditor/';
        const self = this;
        $(this.$refs.editor).ckeditor({
          customConfig: '/commons-extension/ckeditorCustom/config.js',
          extraPlugins: extraPlugins,
          allowedContent: true,
          removePlugins: 'image,confirmBeforeReload,maximize,resize',
          toolbar: [
            ['Bold','Italic', 'Underline','BulletedList'],
          ],
          typeOfRelation: 'mention_activity_stream',
          autoGrow_onStartup: false,
          autoGrow_maxHeight: 300,
          on: {
            instanceReady: function() {
              self.editorReady = true;
            },
            change: function(evt) {
              const newData = evt.editor.getData();

              self.inputVal = newData;

              const pureText = newData ? newData.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
              self.charsCount = pureText.length;
            },
            destroy: function () {
              self.inputVal = '';
              self.charsCount = 0;
            }
          }
        });
      },
      setFocus: function() {
        CKEDITOR.instances['activityContent'].focus();
      },
      getMessage: function() {
        const newData = CKEDITOR.instances['activityContent'].getData();
        return newData ? newData : '';
      }
    }
  };
</script>
