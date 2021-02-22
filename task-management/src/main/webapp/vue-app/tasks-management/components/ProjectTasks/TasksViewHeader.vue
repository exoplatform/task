<template>
  <div
    :id="'task-'+viewType+'-'+status.id"
    class="tasksViewHeader d-flex justify-space-between align-center">
    <div
      class="py-3 d-flex">
      <a
        v-if="viewType=== 'list'"
        class="toggle-collapse-group d-flex"
        href="#"
        @click="showDetailsTask(viewType,status.id)">
        <i
          :id="'uiIconMiniArrowDown'+viewType+status.id"
          class="uiIcon uiIconMiniArrowDown"
          style="display: block">
        </i>
        <i
          :id="'uiIconMiniArrowRight'+viewType+status.id"
          class="uiIcon  uiIconMiniArrowRight"
          style="display: none">
        </i>
      </a>
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
        @click="editStatus = true">{{ status.name }} <span v-if="viewType=== 'list'" class="caption font-weight-bold">({{ tasksNumber }})</span></div>

    </div>
    <div class="taskNumberAndActions d-flex align-center mb-1">
      <span class="uiTaskNumber">{{ tasksNumber }}</span>
      <!-- <span v-if="tasksNumber < maxTasksToShow" class="caption">{{ tasksNumber }}</span>
      <div v-else class="showTasksPagination">
        <span class="caption">
          {{ limitTasksToshow }} - {{ initialTasksToShow }} of {{ tasksNumber }}
        </span>
        <v-btn
          :disabled="disableBtnLoadMore"
          icon
          small
          @click="loadNextTasks">
          <i class="uiIcon uiIconArrowNext text-color"></i>
        </v-btn>

      </div> -->
      <i
        icon
        small
        class="uiIconSocSimplePlus d-flex"
        @click="openQuickAdd">
      </i>      
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
    computed: {
      initialTasksToShow() {
        return this.originalTasksToShow;
      },
      limitTasksToshow() {
        return this.tasksStatsStartValue;
      }
    },
    created() {
      $(document).on('mousedown', () => {
        if (this.displayActionMenu) {
          window.setTimeout(() => {
            this.displayActionMenu = false;
          }, 200);
        }
/*         if (this.editStatus || this.status.edit) {
          window.setTimeout(() => {
            this.cancelAddColumn(this.index);
          }, 200);
        } */
      });
    },
    methods: {
      loadNextTasks() {
        if(this.tasksNumber - this.originalTasksToShow >= this.maxTasksToShow) {
          this.tasksStatsStartValue = this.originalTasksToShow+1;
          this.originalTasksToShow += this.maxTasksToShow;
        } else {
          this.tasksStatsStartValue = this.originalTasksToShow;
          this.originalTasksToShow = this.tasksNumber;
          this.disableBtnLoadMore = true;
        }
      },
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
      openQuickAdd(index) {       
          this.$emit('open-quick-add');    
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
      showDetailsTask(viewType,id){
        const uiIconMiniArrowDown = document.querySelector(`#${`uiIconMiniArrowDown${viewType}${id}`}`);
        const uiIconMiniArrowRight = document.querySelector(`#${`uiIconMiniArrowRight${viewType}${id}`}`);

        const detailsTask = document.querySelector(`#${`taskView${id}`}`);
        if (detailsTask.style.display !== 'none') {
          detailsTask.style.display = 'none';
          uiIconMiniArrowDown.style.display = 'none';
          uiIconMiniArrowRight.style.display = 'block'
        }
        else {detailsTask.style.display = 'block'
          uiIconMiniArrowDown.style.display = 'block';
          uiIconMiniArrowRight.style.display = 'none'}
      },
    }
  }
</script>