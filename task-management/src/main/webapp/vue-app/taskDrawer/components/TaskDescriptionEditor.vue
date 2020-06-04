<template>
  <div id="taskDescriptionId" class="taskDescription">
    <div
      v-if="!editorReady" 
      :placeholder="placeholder"
      :title="$t('tooltip.clickToEdit')"
      contenteditable="true"
      class="py-1 px-2"
      @click="showDescriptionEditor()" 
      v-html="urlVerify(inputVal)">{{ placeholder }}</div>
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
                CKEDITOR.basePath = '/commons-extension/ckeditor/';
                const self = this;
                $(this.$refs.editor).ckeditor({
                    customConfig: '/commons-extension/ckeditorCustom/config.js',
                    removePlugins: 'suggester,simpleLink,confighelper',
                    toolbarLocation: 'top',
                    toolbar: [
                        ['Styles'],
                        ['Bold','Italic','Underline'],
                        ['TextColor'],
                        ['NumberedList','BulletedList']
                    ],
                    autoGrow_onStartup: false,
                    autoGrow_maxHeight: 300,
                    on: {
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
            this.editorReady = !this.editorReady;
          },
          urlVerify(text) {
            return text.replace(/((?:href|src)=")?((((https?|ftp|file):\/\/)|www\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])/ig,
                    function (matchedText, hrefOrSrc) {
                      // the second group of the regex captures the html attribute 'html' or 'src',
                      // so if it exists it means that it is already an html link or an image and it should not be converted
                      if (hrefOrSrc) {
                        return matchedText;
                      }
                      let url = matchedText;
                      if (url.indexOf('www.') === 0) {
                        url = 'http://' + url;
                      }
                      return '<a href="' + url + '" target="_blank">' + matchedText + '</a>';
                    });
          }
        }
    };
</script>