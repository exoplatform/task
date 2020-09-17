<template>
  <div
    :class="getTaskPriorityColor(task.task.priority)"
    class="taskListItemView  px-4 py-3 d-flex align-center">
    <div class="taskCheckBox">
      <i :title="$t('message.markAsCompleted')" class="uiIcon uiIconCircle"></i>
    </div>
    <div class="taskTitle pr-3">
      <span>{{ task.task.title }}</span>
    </div>
    <div class="taskProject">
      <div v-if="isPersonnalTask" class="taskProjectName mr-3 pa-1">
        <span class="caption text-sub-title">{{ $t('label.noProject') }}</span>
      </div>
      <div 
        v-else 
        :class="task.task.status.project.color || 'noProjectColor'" 
        class="taskProjectName mr-3 pa-1">
        <span class="font-weight-bold">{{ task.task.status.project.name }}</span>
      </div>
    </div>
    <div class="taskAssignee d-flex flex-nowrap">
      <exo-user-avatar
        v-for="user in assigneeAndCoworkerArray"
        :key="user"
        :username="user.username"
        :title="user.displayName"
        :avatar-url="user.avatar"
        :size="iconSize"
        :style="'background-image: url('+user.avatar+')'"
        class="mx-1 taskWorkerAvatar"/>
    </div>
    <div class="taskLabels">
      <span v-if="task.labels && task.labels.length == 1" class="labelText">{{ task.labels[0].name }}</span>
      <span
        v-else-if="task.labels && task.labels.length > 1"
        :title="getLabelsList(task.labels)"
        class="labelText">{{ task.labels.length }} {{ $t('label.labels') }}
      </span>
      <span v-else class="noLabelText caption"> {{ $t('label.noLabel') }}</span>
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
      <span v-if="isPersonnalTask" class="caption text-sub-title">{{ $t('label.noStatus') }}</span>
      <span v-else class="taskStatLabel pl-2">
        {{ getTaskStatusLabel(task.task.status.name) }}
      </span>
    </div>
    <div class="taskDueDate">
      <div v-if="taskDueDate">
        <date-format :value="taskDueDate" :format="dateTimeFormat" />
      </div>
      <div v-else>
        <span class="caption text-sub-title">{{ $t('label.noDueDate') }}</span>
      </div>
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
        iconSize: 30,
        dateTimeFormat: {
          year: 'numeric',
          month: 'numeric',
          day: 'numeric',
        },
        assigneeAndCoworkerArray: [],
        isPersonnalTask : this.task.task.status === null,
        labelList: ''
      }
    },
    computed: {
      taskDueDate() {
        return this.task && this.task.task.dueDate && this.task.task.dueDate.time;
      },
    },
    created() {
      this.getTaskAssigneeAndCoworkers();
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
      getTaskAssigneeAndCoworkers() {
        this.assigneeAndCoworkerArray.push(this.task.assignee);
        if (this.task.coworker || this.task.coworker.length > 0 )
        {
          this.task.coworker.forEach((coworker) => {
            this.assigneeAndCoworkerArray.push(coworker);
          })
        }
      },
      getTaskStatusLabel(status) {
        switch(status) {
          case "ToDo":
            return this.$t('exo.tasks.status.todo');
          case "InProgress":
            return this.$t('exo.tasks.status.inprogress');
          case "WaitingOn":
            return this.$t('exo.tasks.status.waitingon');
          case "Done":
            return this.$t('exo.tasks.status.done');
        }
      },
      getLabelsList(taskLabels) {
        if (taskLabels.length > 1) {
          let labelText = '';
          taskLabels.forEach((label) => {
            labelText += `${label.name}\r\n`;
          });
          return labelText;
        }
      }
    }
  }
</script>