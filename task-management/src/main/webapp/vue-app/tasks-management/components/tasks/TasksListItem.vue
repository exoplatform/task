<template>
  <div class="d-flex align-center">
    <div class="taskCheckBox">
      <v-checkbox
        v-model="enabled"
        hide-details
        class="shrink mr-2 mt-0"/>
    </div>
    <div class="taskTitle pr-3">
      <span>{{ task.title }}</span>
    </div>
    <div class="taskProject">
      <div :class="task.status.project.color || 'noProjectColor'" class="taskProjectName mr-3 pa-1">
        <span class="font-weight-bold">{{ task.status.project.name }}</span>
      </div>
    </div>
    <div class="taskAssignee">
      <exo-user-avatar
        :username="task.assignee"
        :title="user.fullname"
        :avatar-url="user.avatar"
        :size="iconSize"/>
    </div>
    <div class="taskLabels">
      <span v-if="labels && labels.length == 1" class="labelText">{{ labels[0].name }}</span>
      <span v-else-if="labels && labels.length > 1" class="labelText">{{ labels.length }} labels</span>
      <span v-else class="noLabelText"> {{ $t('label.noLabel') }}</span>
    </div>
    <!--<div class="taskActions"><span>task actions</span></div>-->
    <div class="taskStat d-flex align-center">
      <i class="uiBatteryHalfIcon"></i>
      <span class="taskStatLabel pl-2">{{ task.status.name }}</span>
    </div>
    <div class="taskDueDate">22/12/2020</div>
  </div>
</template>
<script>
  export default {
    props: {
      task: {
        type: Object,
        default: null
      }
    },
    data () {
      return {
        enabled: false,
        user: {},
        iconSize: 37,
        labels:[]
      }
    },
    created() {
      this.getUser(this.task.assignee);
      this.getLabelsByTaskId(this.task.id);
    },
    methods: {
      getUser(userName) {
        return this.$userService.getUser(userName).then(data => {
          this.user = data;
        });
      },
      getLabelsByTaskId(id) {
        return this.$tasksService.getLabelsByTaskId(id).then(data => {
          this.labels = data;
        });
      }
    }
  }
</script>