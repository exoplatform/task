<template>
  <div
    :id="'task-'+viewType+'-'+status.id"
    class="tasksViewHeader  d-flex justify-space-between align-center">
    <div
      class="d-flex tasksViewHeaderLeft">
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
      <div v-if="(editStatus || status.edit) && project.canManage">
        <v-text-field
          v-if="editStatus || status.edit"
          ref="autoFocusInput1"
          v-model="status.name"
          :placeholder="$t('label.tapStatus.name')"
          type="text"
          :rules="nameRules"
          @focus="editStatusMode(project.canManage)"
          @blur="cancelAddColumn(status.name)"
          class="taskStatusNameEdit font-weight-bold text-color mb-1"
          required
          autofocus
          outlined
          dense
          @keyup="checkInput($event,index)">
          <i
            dark
            class="uiIcon40x40TickBlue ma-1"
            slot="append"
            @click="checkInput(13,status.name)">
          </i>
          <i
            dark
            class="uiIconClose ma-1"
            slot="append"
            @click="cancelAddColumn(status.name)">
          </i>
        </v-text-field>
      </div>

      <div
        class="d-flex taskStatusName  font-weight-bold text-color mb-1"
        v-else
        @click="editStatusMode(project.canManage)">
        <div
          class="statusName text-truncate"
          :title="getI18N(status.name)">
          {{ getI18N(status.name) }}
        </div>
        <div class="uiTaskNumber">{{ tasksNumber }}</div>
      </div>
    </div>
    <div
      class="taskNumberAndActions d-flex align-center mb-1"
      @click="editStatus = false">
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
        :title="tooltipAddTask"
        class="uiIconSocSimplePlus d-flex"
        @click="openTaskDrawer()">
      </i>
      <i
        v-if="project.canManage"
        icon
        small
        class="uiIconVerticalDots taskInfoIcon d-flex"
        @click="displayActionMenu = true">
      </i>
      <v-menu
        v-model="displayActionMenu"
        v-if="project.canManage"
        :attach="`#task-${viewType}-${status.id}`"
        transition="slide-x-reverse-transition"
        content-class="taskStatusActionMenu"
        offset-y>
        <v-list class="pa-0" dense>
          <v-list-item
            v-if="index>0"
            class="menu-list"
            @click="moveBeforeColumn(index)">
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconArrowLeft pe-1"></i>
              <span>{{ $t('label.status.move.before') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            class="menu-list"
            @click="addColumn(index)">
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconRotateLeft pe-1"></i>
              <span>{{ $t('label.status.before') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            class="menu-list"
            @click="addColumn(index+1)">
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconRotateRight pe-1"></i>
              <span> {{ $t('label.status.after') }} </span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            v-if="index < statusListLength-1"
            class="menu-list"
            @click="moveAfterColumn(index)">
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconArrowRight pe-1"></i>
              <span> {{ $t('label.status.move.after') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            class="menu-list"
            @click="deleteStatus()">
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconDelete pe-1"></i>
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
    statusListLength: {
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
      tasksStatsStartValue: 1,
      originalTasksToShow: this.maxTasksToShow,
      disableBtnLoadMore: false,rules: [],
      nameRules: []
    };
  },
  computed: {
    initialTasksToShow() {
      return this.originalTasksToShow;
    },
    limitTasksToshow() {
      return this.tasksStatsStartValue;
    },
    limitStatusLabel() {
      return this.$t('label.status.name.rules');
    },
    tooltipAddTask(){
      return this.$t('label.addTask');
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
    this.nameRules = [v => !v || (v.length > 2 && v.length < 51) || this.limitStatusLabel];
  },
  methods: {
    loadNextTasks() {
      if (this.tasksNumber - this.originalTasksToShow >= this.maxTasksToShow) {
        this.tasksStatsStartValue = this.originalTasksToShow+1;
        this.originalTasksToShow += this.maxTasksToShow;
      } else {
        this.tasksStatsStartValue = this.originalTasksToShow;
        this.originalTasksToShow = this.tasksNumber;
        this.disableBtnLoadMore = true;
      }
    },
    openTaskDrawer() {
      const defaultTask= {id: null,
        status: this.status,
        priority: 'NONE',
        description: '',
        title: ''};
      this.$root.$emit('open-task-drawer', defaultTask);
    },
    checkInput: function(e,index) {
      if (e.keyCode === 13 || e === 13) {
        this.saveStatus(index);
      }
      if (e.keyCode === 27) {
        this.cancelAddColumn(index);
      }
    },

    deleteStatus() {
      this.$emit('delete-status', this.status);
    },
    saveStatus(index) {
      if (this.status.name.length>=3 && this.status.name.length<=50){
        if (this.status.id){
          this.$emit('update-status', this.status);
          this.editStatus=false;
        } else {
          this.status.rank=index;
          this.$emit('add-status');
          this.editStatus=false;
        }
      }
    },
    openQuickAdd() {
      this.$emit('open-quick-add');
    },
    addColumn(index) {
      this.$emit('add-column',index);
    },
    moveBeforeColumn(index) {
      this.$emit('move-column',index,index-1);
    },
    moveAfterColumn(index) {
      this.$emit('move-column',index,index+1);
    },
    cancelAddColumn(index) {
      if (this.status.id){
        this.editStatus=false;
        this.status.edit=false;
        this.$emit('update-status', null);
      } else {
        this.editStatus=false;
        this.status.edit=false;
        this.$emit('cancel-add-column',index);
        this.$emit('update-status', null);

      }
    },
    editStatusMode(canManage){
      if (canManage){
        this.editStatus=true;
      }

    },
    getI18N(label){
      const fieldLabelI18NKey = `tasks.status.${label}`;
      const fieldLabelI18NValue = this.$t(fieldLabelI18NKey);
      return  fieldLabelI18NValue === fieldLabelI18NKey ? label : fieldLabelI18NValue;
    },
    showDetailsTask(viewType,id){
      const uiIconMiniArrowDown = document.querySelector(`#${`uiIconMiniArrowDown${viewType}${id}`}`);
      const uiIconMiniArrowRight = document.querySelector(`#${`uiIconMiniArrowRight${viewType}${id}`}`);

      const detailsTask = document.querySelector(`#${`taskView${id}`}`);
      if (detailsTask.style.display !== 'none') {
        detailsTask.style.display = 'none';
        uiIconMiniArrowDown.style.display = 'none';
        uiIconMiniArrowRight.style.display = 'block';
      }
      else {detailsTask.style.display = 'block';
        uiIconMiniArrowDown.style.display = 'block';
        uiIconMiniArrowRight.style.display = 'none';}
    },
  }
};
</script>