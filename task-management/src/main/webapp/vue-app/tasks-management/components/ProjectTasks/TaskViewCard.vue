<template>
  <v-app id="taskCardItem" class="py-3">
    <v-card
      :class="getTaskPriorityColor(task.task.priority)"
      class="taskCard pa-3"
      flat>
      <div class="taskTitleId">
        <div class="taskTitle d-flex align-start">
          <i :title="$t('message.markAsCompleted')" class="uiIcon uiIconCircle"></i>
          <a
            ref="tooltip">
            <ellipsis
              v-if="task.task.title "
              :title="task.task.title "
              :data="task.task.title "
              :line-clamp="2"
              end-char=".."
              @click="openTaskDrawer()"/>
          </a>
        </div>
      </div>
      <div class="taskLabelsAndWorker d-flex justify-space-between my-3">
        <div class="taskAssignee  d-flex flex-nowrap">
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
          <span
            v-if="task.labels && task.labels.length == 1"
            class="labelText">
            {{ task.labels[0].name }}
          </span>
          <span
            v-else-if="task.labels && task.labels.length > 1"
            :title="getLabelsList(task.labels)"
            class="labelText">
            {{ task.labels.length }} {{ $t('label.labels') }}
          </span>
          <span v-else class="noLabelText caption"> {{ $t('label.noLabel') }}</span>
        </div>
      </div>
      <v-divider/>
      <siv class="taskStatusAndDate d-flex justify-space-between pt-3">
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
        <div class="taskDueDate">
          <div v-if="taskDueDate">
            <date-format :value="taskDueDate" :format="dateTimeFormat" />
          </div>
          <div v-else>
            <span class="caption text-sub-title">{{ $t('label.noDueDate') }}</span>
          </div>
        </div>
      </siv>
    </v-card>
    <task-drawer
      v-if="drawer"
      :drawer="drawer"
      :task="task.task"
      @closeDrawer="onCloseDrawer"/>
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
        user: {},
        iconSize: 32,
        dateTimeFormat: {
          year: 'numeric',
          month: 'numeric',
          day: 'numeric',
        },
        assigneeAndCoworkerArray: [],
        isPersonnalTask : this.task.task.status === null,
        drawer:null
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
      getLabelsList(taskLabels) {
        if (taskLabels.length > 1) {
          let labelText = '';
          taskLabels.forEach((label) => {
            labelText += `${label.name}\r\n`;
          });
          return labelText;
        }
      },
      openTaskDrawer() {
        this.drawer = true;
      },
      onCloseDrawer: function(drawer){
        this.drawer = drawer;
      }
    }
  }
</script>