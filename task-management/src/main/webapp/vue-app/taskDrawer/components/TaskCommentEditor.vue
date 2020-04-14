<template>
  <div>
    <textarea 
      id="commentContent"
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
      };
    },
    watch: {
      inputVal(val) {
        this.$emit('input', val);
      },
      reset() {
        CKEDITOR.instances['commentContent'].destroy(true);
        this.initCKEditor();
      },
    },
    mounted() {
      this.initCKEditor();
    },
    methods: {
      initCKEditor: function () {
        // this line is mandatory when a custom skin is defined
        CKEDITOR.basePath = '/commons-extension/ckeditor/';
        const self = this;
        $(this.$refs.editor).ckeditor({
          customConfig: '/commons-extension/ckeditorCustom/config.js',
          removePlugins: 'image,confirmBeforeReload,maximize,resize',
          toolbar: [
            ['Bold','Italic','BulletedList', 'NumberedList', 'Blockquote'],
          ],
          autoGrow_onStartup: false,
          autoGrow_maxHeight: 300,
          on: {
            blur: function () {
              $(document.body).trigger('click');
            },
            change: function(evt) {
              const newData = evt.editor.getData();

              self.inputVal = newData;
            },
            destroy: function () {
              self.inputVal = '';
            }
          },
        });
      },
    }
  };
</script>