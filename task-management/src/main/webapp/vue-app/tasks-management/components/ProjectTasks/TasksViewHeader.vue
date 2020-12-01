<template>
  <div
    :id="'task-'+viewType+'-'+status.name"
    class="tasksViewHeader d-flex justify-space-between align-center">
    <div v-if="!status.edit" class="taskStatusName font-weight-bold text-color mb-1">{{ status.name }}</div>
    <div 
      v-if="status.edit" 
      d-flex 
      align-center 
      mb-1>
      <input         
        ref="autoFocusInput1" 
        v-model="status.name" 
        placeholder="Status Name"
        type="text"
        class="pl-0 pt-0 status-name"
        required  
        autofocus>
      <a class="taskAssignBtn mt-n1" @click="addStatus(index)">
        <i class="uiIcon uiIconTick"></i>
      </a>
      <a class="taskAssignBtn mt-n1" @click="cancelAddColumn(index)">
        <i class="uiIcon uiIconClose"></i>
      </a>
      
    </div>
    <div class="taskNumberAndActions d-flex align-center mb-1">
      <span class="caption">{{ tasksNumber }}</span>
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
        class="uiIcon uiIconThreeDots"
        @click="displayActionMenu = true"></i>
      <v-menu
        v-model="displayActionMenu"
        :attach="`#task-${viewType}-${status.name}`"
        transition="slide-x-reverse-transition"
        content-class="taskStatusActionMenu"
        offset-y>
        <v-list>
          <v-list-item>
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconTask pr-1"></i>
              <span>{{ $t('label.addTask') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2" @click="addColumn(index)">
              <i class="uiIcon uiIconBefore pr-1"></i>
              <span>{{ $t('label.status.before') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2" @click="addColumn(index+1)">
              <i class="uiIcon uiIconAfter pr-1"></i>
              <span>{{ $t('label.status.after') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2" @click="deleteStatus">
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
      }
    },
    data() {
      return {
        displayActionMenu: false,
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
      deleteStatus() {
          this.$emit('delete-status', this.status);
      },
      addStatus(index) {
        this.status.rank=index
          this.$emit('add-status');
      },
      addColumn(index) {
          this.$emit('add-column',index);         
      },
      cancelAddColumn(index) {
          this.$emit('cancel-add-column',index);
      },
    }
  }
</script>