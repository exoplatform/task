<template>
  <div
    :id="'task-'+viewType+'-'+status.id"
    class="tasksViewHeader d-flex justify-space-between align-center">
    <input
      v-if="editStatus || status.edit"
      ref="autoFocusInput1"
      v-model="status.name"
      placeholder="Status Name"
      type="text"
      class="taskStatusName font-weight-bold text-color mb-1"
      required
      autofocus
      @keyup="checkImput($event,index)">
    <div
      v-else
      class="taskStatusName font-weight-bold text-color mb-1"
      @click="editStatus = true">{{ getI18N(status.name) }}</div>
    <div class="taskNumberAndActions d-flex align-center mb-1">
      <span class="caption">{{ tasksNumber }}</span>
      <i
        icon
        small
        class="uiIconVerticalDots taskInfoIcon d-flex"
        @click="displayActionMenu = true">
      </i>
      <v-menu
        v-model="displayActionMenu"
        :attach="`#task-${viewType}-${status.id}`"
        transition="slide-x-reverse-transition"
        content-class="taskStatusActionMenu"
        offset-y>
        <v-list class="pa-0" dense>
          <v-list-item class="menu-list" @click="openTaskDrawer()" >
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconTask pr-1"></i>
              <span>{{ $t('label.addTask') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            v-if="project.canManage"
            class="menu-list"
            @click="addColumn(index)" >
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconRotateLeft pr-1"></i>
              <span>{{ $t('label.status.before') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            v-if="project.canManage"
            class="menu-list"
            @click="addColumn(index+1)" >
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconRotateRight pr-1"></i>
              <span> {{ $t('label.status.after') }} </span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            v-if="project.canManage"
            class="menu-list"
            @click="deleteStatus()" >
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconDelete pr-1"></i>
              <span>{{ $t('label.delete') }}</span>
            </v-list-item-title>
          </v-list-item>


        </v-list>
      </v-menu>
    </div>
  </div>
</template>
<script>
  export default {
    props: {
      status: {
        type: Object,
        default: null
      },
      viewType: {
        type: String,
        default: ''
      },
      tasksNumber: {
        type: Number,
        default: 0
      },
      maxTasksToShow: {
        type: Number,
        default: 0
      },
      index: {
        type: Number,
        default: 0
      },
      project: {
        type: Number,
        default: 0
      }
    },
    data() {
      return {
        displayActionMenu: false,
        editStatus: false,
        tasksStatsStartValue:1,
        originalTasksToShow: this.maxTasksToShow,
        disableBtnLoadMore: false,
      }
    },
    created() {
      $(document).on('mousedown', () => {
        if (this.displayActionMenu) {
          window.setTimeout(() => {
            this.displayActionMenu = false;
          }, 200);
        }
      });
    },
    methods: {
      openTaskDrawer() {
        const defaultTask= {id:null,
        status:this.status,
        priority:'NONE',
        description:'',
        title:''}
        this.$root.$emit('open-task-drawer', defaultTask)
      },
      checkImput: function(e,index) {
      if (e.keyCode === 13) {
        this.saveStatus(index)
      }
      if (e.keyCode === 27) {
        this.cancelAddColumn(index)
      }
    },

      deleteStatus() {
          this.$emit('delete-status', this.status);
      },
      saveStatus(index) {
        if(this.status.id){
          this.$emit('update-status', this.status);
          this.editStatus=false
        }else{
          this.status.rank=index
          this.$emit('add-status');
          this.editStatus=false
        }
      },
      addColumn(index) {
          this.$emit('add-column',index);
      },
      cancelAddColumn(index) {
        if(this.status.id){
          this.editStatus=false
          this.status.edit=false
        }else{
          this.$emit('cancel-add-column',index);
        }
      },
      getI18N(label){
        const fieldLabelI18NKey = `tasks.status.${label}`;
        const fieldLabelI18NValue = this.$t(fieldLabelI18NKey);
        return  fieldLabelI18NValue === fieldLabelI18NKey ? label : fieldLabelI18NValue;
      }
    }
  }
</script>