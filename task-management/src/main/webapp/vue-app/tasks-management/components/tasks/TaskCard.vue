<template>
  <v-app id="taskCardItem" class="pa-3">
    <v-card class="taskCard pa-3" flat>
      <div class="taskTitleId d-flex justify-space-between">
        <div class="taskTitle d-flex align-start">
          <v-radio
            v-model="enabled"
            value="radio-1"
            class="shrink mt-0"/>
          <ellipsis
            v-if="task.title "
            :title="task.title "
            :data="task.title "
            :line-clamp="2"
            end-char=".."/>
        </div>
        <div class="taskId">
          <span class="caption text-sub-title">ID : {{ task.id }}</span>
        </div>
      </div>
      <div class="taskProjectAndLabel d-flex justify-space-between align-center mt-3">
        <div class="taskProject">
          <div :class="task.status.project.color || 'noProjectColor'" class="taskProjectName mr-3 pa-1">
            <span class="font-weight-bold">{{ task.status.project.name }}</span>
          </div>
        </div>
        <div class="taskLabels">
          <span v-if="labels && labels.length == 1" class="labelText">{{ labels[0].name }}</span>
          <span v-else-if="labels && labels.length > 1" class="labelText">{{ labels.length }} labels</span>
          <span v-else class="noLabelText"> {{ $t('label.noLabel') }}</span>
        </div>
      </div>
      <div class="taskActionsAndWorker d-flex justify-space-between my-3">
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
        <div class="taskAssignee">
          <exo-user-avatar
            :username="task.assignee"
            :title="user.fullname"
            :avatar-url="user.avatar"
            :size="iconSize"/>
        </div>
      </div>
      <v-divider/>
      <siv class="taskStatusAndDate d-flex justify-space-between pt-3">
        <div class="taskStat">
          <span class="taskStatLabel pl-2">{{ task.status.name }}</span>
        </div>
        <div class="taskDueDate">
          <span>22/12/2020</span>
        </div>
      </siv>
    </v-card>
  </v-app>
</template>
<script>
  export default {
    props: {
      task: {
        type: Object,
        default: () => ({}),
      }
    },
    data() {
      return {
        enabled: false,
        labels:[],
        user: {},
        iconSize: 32,
      }
    },
    created() {
      this.getLabelsByTaskId(this.task.id);
      this.getUser(this.task.assignee);
    },
    methods: {
      getLabelsByTaskId(id) {
        return this.$tasksService.getLabelsByTaskId(id).then(data => {
          this.labels = data;
        });
      },
      getUser(userName) {
        return this.$userService.getUser(userName).then(data => {
          this.user = data;
        });
      }
    }
  }
</script>