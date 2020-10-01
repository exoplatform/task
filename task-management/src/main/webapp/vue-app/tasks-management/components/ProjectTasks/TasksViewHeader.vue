<template>
  <div
    :id="'task-'+viewType+'-'+status.name"
    class="tasksViewHeader d-flex justify-space-between align-center">
    <div class="taskStatusName font-weight-bold text-color mb-1">{{ status.name }}</div>
    <!--<div class="taskNumberAndActions d-flex align-center mb-1">
      <span v-if="tasksNumber < maxTasksToShow" class="caption">{{ tasksNumber }}</span>
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

      </div>
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
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconBefore pr-1"></i>
              <span>{{ $t('label.status.before') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconAfter pr-1"></i>
              <span>{{ $t('label.status.after') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconDelete pr-1"></i>
              <span>{{ $t('label.delete') }}</span>
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </div>-->
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
      }
    },
    data() {
      return {
        displayActionMenu: false,
        tasksStatsStartValue:1,
        originalTasksToShow: this.maxTasksToShow,
        disableBtnLoadMore: false
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
    }
  }
</script>