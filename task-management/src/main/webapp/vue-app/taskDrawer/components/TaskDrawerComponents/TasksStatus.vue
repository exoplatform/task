<template>
  <div class="taskStatus">
    <div v-if="task.status && task.status.project">
      <v-select
        id="selectStatus"
        ref="selectStatus"
        v-model="taskStatus"
        :items="projectStatuses"
        item-value="key"
        item-text="value"
        class="pt-0 selectFont"
        attach
        dense
        solo
        @change="updateTaskStatus()"
        @click="$emit('statusListOpened')" />
    </div>
  </div>
</template>
<script>
export default {
  props: {
    task: {
      type: Object,
      default: () => {
        return {};
      }
    },
  },
  data () {
    return {
      taskStatus: 'ToDo',
      projectStatuses: [],
    };
  },
  created() {
    $(document).on('mousedown', () => {
      if (this.$refs.selectStatus && this.$refs.selectStatus.isMenuActive) {
        window.setTimeout(() => {
          this.$refs.selectStatus.isMenuActive = false;
        }, 200);
      }
    });
    document.addEventListener('closeStatus',()=> {
      if (this.$refs.selectStatus && this.$refs.selectStatus.isMenuActive) {
        window.setTimeout(() => {
          this.$refs.selectStatus.isMenuActive = false;
        }, 100);
      }
    });
    document.addEventListener('loadProjectStatus', this.loadProjectStatus);
  },
  destroyed() {
    document.removeEventListener('loadProjectStatus', this.loadProjectStatus);
  },
  methods: {
    updateTaskStatus() {
      if (this.taskStatus != null) {
        this.$taskDrawerApi.getStatusesByProjectId(this.task.status.project.id).then(
          (projectStatuses) => {
            const status = projectStatuses.find(s => s.name === this.taskStatus);
            this.task.status = status;
            this.$emit('updateTaskStatus',status);
          });
      }
    },
    getStatusesByProjectId(task) {
      this.$taskDrawerApi.getStatusesByProjectId(task.status.project.id).then(
        (data) => {
          this.projectStatuses = data;
          for (let i = 0; i < data.length; i++) {
            switch (data[i].name) {
            case 'ToDo':
              this.projectStatuses[i] = {key: 'ToDo', value: this.$t('exo.tasks.status.todo')};
              break;
            case 'InProgress':
              this.projectStatuses[i] = {key: 'InProgress', value: this.$t('exo.tasks.status.inprogress')};
              break;
            case 'WaitingOn':
              this.projectStatuses[i] = {key: 'WaitingOn', value: this.$t('exo.tasks.status.waitingon')};
              break;
            case 'Done':
              this.projectStatuses[i] = {key: 'Done', value: this.$t('exo.tasks.status.done')};
              break;
            default:
              this.projectStatuses[i] = {key: data[i].name, value: data[i].name};
            }
          }
        });
    },
    loadProjectStatus(event) {
      if (event && event.detail) {
        const task = event.detail;
        if (task.status != null && task.status.project) {
          this.getStatusesByProjectId(task);
          if (task.status.name) {
            this.taskStatus = task.status.name;
          }
        } else {
          this.projectStatuses.push({key: 'ToDo', value: this.$t('exo.tasks.status.todo')});
          this.projectStatuses.push({key: 'InProgress', value: this.$t('exo.tasks.status.inprogress')});
          this.projectStatuses.push({key: 'WaitingOn', value: this.$t('exo.tasks.status.waitingon')});
          this.projectStatuses.push({key: 'Done', value: this.$t('exo.tasks.status.done')});
          this.taskStatus = 'ToDo';
        }
      }
    },
  }
};
</script>