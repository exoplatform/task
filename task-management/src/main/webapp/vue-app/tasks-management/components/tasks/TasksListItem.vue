<template>
  <div
    :class="getTaskPriorityColor(task.task.priority)"
    class="taslListItemView  px-4 py-3 d-flex align-center">
    <div class="taskCheckBox">
      <v-radio
        v-model="enabled"
        value="radio-1"
        class="shrink mr-2 mt-0"/>
    </div>
    <div class="taskTitle pr-3">
      <span>{{ task.task.title }}</span>
    </div>
    <div class="taskProject">
      <div :class="task.task.status.project.color || 'noProjectColor'" class="taskProjectName mr-3 pa-1">
        <span class="font-weight-bold">{{ task.task.status.project.name }}</span>
      </div>
    </div>
    <div class="taskAssignee">
      <exo-user-avatar
        :username="task.assignee.username"
        :title="task.assignee.displayName"
        :avatar-url="task.assignee.avatar"
        :size="iconSize"/>
    </div>
    <div class="taskLabels">
      <span v-if="labels && labels.length == 1" class="labelText">{{ labels[0].name }}</span>
      <span v-else-if="labels && labels.length > 1" class="labelText">{{ labels.length }} labels</span>
      <span v-else class="noLabelText"> {{ $t('label.noLabel') }}</span>
    </div>
    <div class="taskActions d-flex justify-center align-center">
      <div class="taskComment d-flex">
        <i class="uiIcon uiCommentIcon"></i>
        <span class="taskCommentNumber caption">4</span>
      </div>
      <div class="taskAttachment  d-flex pl-3">
        <i class="uiIcon uiAttachIcon"></i>
        <span class="taskAttachNumber caption">2</span>
      </div>
    </div>
    <div class="taskStat">
      <span class="taskStatLabel pl-2">{{ task.task.status.name }}</span>
    </div>
    <div class="taskDueDate">
      <span>22/12/2020</span>
    </div>
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
        iconSize: 32,
        labels:['label1', 'label2']
      }
    },
    methods: {
      getTaskPriorityColor(priority) {
        switch(priority) {
          case "HIGH":
            return "taskHighPriority";
          case "NORMAL":
            return "taskNormalPriority";
          case "LOW":
            return "taskLowPriority";
          case "NONE":
            return "taskNonePriority";
        }
      },
    }
  }
</script>