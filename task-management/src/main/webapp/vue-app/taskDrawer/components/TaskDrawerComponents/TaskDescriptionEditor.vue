<template>
  <div
    id="taskDescriptionId"
    :class="editorReady && 'active'"
    class="taskDescription">
    <div
      :data-text="placeholder"
      :title="$t('tooltip.clickToEdit')"
      contentEditable="true"
      class="py-1 px-2 taskDescriptionToShow"
      @click="showDescriptionEditor()"
      v-html="inputVal ? urlVerify(inputVal) : inputVal">
      {{ placeholder }}
    </div>
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
  import {urlVerify, updateTask} from '../../taskDrawerApi';
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
      task : {
        type: Object,
         default: function () {
          return {}
        }
      }
    },
    data() {
      return {
        inputVal : this.value,
        editorReady: false
      };
    },
    computed: {
      taskDescription () {
        return this.task && this.task.id && this.task.description || ''
      }
    },
    watch: {
      inputVal(val) {
        const editorData = CKEDITOR.instances['descriptionContent'] && CKEDITOR.instances['descriptionContent'].getData();
        if (editorData != null && val !== editorData) {
          if (val === '') {
            CKEDITOR.instances['descriptionContent'].setData('');
            this.initCKEditor();
          } else {
            CKEDITOR.instances['descriptionContent'].setData(val);
          }
        }
      },
      value() {
        this.inputVal = this.taskDescription;
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
        this.editorReady = false;
      },
    },
    created() {
      document.addEventListener('drawerClosed', () => {
        this.saveDescription(this.inputVal);
        this.editorReady = false;
        this.inputVal = ''
      });
      document.addEventListener('onAddTask', () => {
        this.$emit('addTaskDescription',this.inputVal);
        this.editorReady = false;
      });
      $(document).on('click','.taskDescriptionToShow a',function(e){
        e.preventDefault();
        e.stopPropagation();
        const url = $(this).attr('href');
        window.open(url, '_blank');
      });
    },
    methods: {
      saveDescription: function (newValue) {
        if (newValue !== this.task.description) {
          if(this.task.id && !isNaN(this.task.id)){
            self.inputVal = newValue;
            this.task.description = newValue;
            updateTask(this.task.id ,this.task)
            .then(task => {
               this.$root.$emit('show-alert', { type: 'success', message: this.$t('alert.success.task.description') });})
              .catch(e => {
                console.debug("Error when updating task's title", e);
                this.$root.$emit('show-alert',{type:'error',message:this.$t('alert.error')} );
                   });
          }
        }
      },
      initCKEditor: function () {
        CKEDITOR.plugins.addExternal('embedsemantic', '/commons-extension/eXoPlugins/embedsemantic/', 'plugin.js');
        let extraPlugins = 'simpleLink,widget,embedsemantic';
        const windowWidth = $(window).width();
        const windowHeight = $(window).height();
        if (windowWidth > windowHeight && windowWidth < 768) {
          extraPlugins = 'simpleLink,selectImage,embedsemantic';
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
            blur: function (evt) {
              $(document.body).trigger('click');
              self.editorReady = false;
            },
            change: function(evt) {
              const newData = evt.editor.getData();
              self.inputVal = newData;
            },
            destroy: function () {
              self.inputVal = '';
              self.editorReady = false;
            }
          },
        });
      },
      showDescriptionEditor:function () {
        this.editorReady = !this.editorReady;
      },

      urlVerify(text) {
        return urlVerify(text);
      }
    }
  };
</script>
