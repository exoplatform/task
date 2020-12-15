<template>
  <div
    id="taskDescriptionId"
    :class="editorReady && 'active'"
    class="taskDescription">
    <div
      :placeholder="placeholder"
      :title="$t('tooltip.clickToEdit')"
      contenteditable="true"
      class="py-1 px-2 taskDescriptionToShow"
      @click="showDescriptionEditor()"
      v-html="inputVal ? urlVerify(inputVal) : inputVal">{{ placeholder }}</div>
    <textarea
      id="descriptionContent"
      ref="editor"
      v-model="inputVal"
      class="d-none"
      cols="30"
      rows="10"></textarea>
  </div>
</template>

<script>
  import {urlVerify} from '../../taskDrawerApi';

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
        editorReady: false
      };
    },
    watch: {
      inputVal(val) {
        this.$emit('input', val);
      },
      editorReady(val) {
        const ckeContent = document.querySelectorAll('[id=cke_descriptionContent]');
        if (val === true) {
          this.initCKEditor();
          if (ckeContent) {
            for (let i = 0; i < ckeContent.length; i++) {
              ckeContent[i].classList.remove('hiddenEditor');
            }
          }
          document.getElementById('taskDescriptionId').classList.remove("taskDescription")
          CKEDITOR.instances['descriptionContent'].focus(true);

        } else {
          for (let i = 0; i < ckeContent.length; i++) {
            ckeContent[i].classList.add('hiddenEditor');
            document.getElementById('taskDescriptionId').classList.add("taskDescription")
          }
        }
      },
      reset() {
        CKEDITOR.instances['descriptionContent'].destroy(true);
        this.initCKEditor();
      },
    },
    methods: {
      initCKEditor: function () {
        let extraPlugins = 'simpleLink,widget,embedsemantic';
        const windowWidth = $(window).width();
        const windowHeight = $(window).height();
        if (windowWidth > windowHeight && windowWidth < 768) {
          // Disable suggester on smart-phone landscape
          extraPlugins = 'simpleLink,selectImage';
        }
        CKEDITOR.basePath = '/commons-extension/ckeditor/';
        const self = this;
        $(this.$refs.editor).ckeditor({
          customConfig: '/commons-extension/ckeditorCustom/config.js',
          //removePlugins: 'suggester,simpleLink,confighelper',
          extraPlugins: extraPlugins,
          removePlugins: 'confirmBeforeReload,maximize,resize',
          toolbarLocation: 'bottom',
          autoGrow_onStartup: true,
          on: {
            focus: function() {
              console.log('test');
            },
            blur: function () {
              $(document.body).trigger('click');
              const newData = CKEDITOR.instances['descriptionContent'].getData();
              this.inputVal = newData;
              self.editorReady = !self.editorReady;
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
      showDescriptionEditor:function () {
        this.editorReady = true;
      },
      urlVerify(text) {
        return urlVerify(text);
      }
    }
  };
</script>