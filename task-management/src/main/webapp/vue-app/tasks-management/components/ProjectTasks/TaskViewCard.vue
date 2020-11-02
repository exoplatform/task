<template>
  <v-app id="taskCardItem" class="py-3">
    <v-card
      :class="getTaskPriorityColor(task.task.priority)"
      class="taskCard taskViewCard pa-3"
      flat>
      <div class="taskTitleId  d-flex justify-space-between">
        <div class="taskTitle d-flex align-start">
          <i :title="$t('message.markAsCompleted')" class="uiIcon uiIconCircle"></i>
          <a
            ref="tooltip"
            class="text-color">
            <ellipsis
              v-if="task.task.title "
              :title="task.task.title "
              :data="task.task.title "
              :line-clamp="2"
              end-char=".."
              @click="openTaskDrawer()"/>
          </a>
        </div>
        <div class="taskId">
          <span class="caption text-sub-title">ID : {{ task.task.id }}</span>
        </div>
      </div>
      <div class="taskLabelsAndWorker d-flex justify-space-between align-center my-3">
        <div class="taskAssignee d-flex flex-nowrap">
          <exo-user-avatar
            v-for="user in avatarToDisplay"
            :key="user"
            :username="user.username"
            :title="user.displayName"
            :avatar-url="user.avatar"
            :size="iconSize"
            :style="'background-image: url('+user.avatar+')'"
            class="mx-1 taskWorkerAvatar"/>
          <div class="seeMoreAvatars">
            <div
              v-if="assigneeAndCoworkerArray.length > maxAvatarToShow"
              class="seeMoreItem"
              @click="$root.$emit('displayTasksAssigneeAndCoworker', assigneeAndCoworkerArray)">
              <v-avatar
                :size="iconSize">
                <img
                  :src="assigneeAndCoworkerArray[maxAvatarToShow].avatar"
                  :title="assigneeAndCoworkerArray[maxAvatarToShow].displayName">
              </v-avatar>
              <span class="seeMoreAvatarList">+{{ showMoreAvatarsNumber }}</span>
            </div>
          </div>
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
          <span v-else class="noLabelText body-2"> {{ $t('label.noLabel') }}</span>
        </div>
      </div>
      <div class="taskActionsWrapper mb-3">
        <div class="taskActions d-flex align-center">
          <div class="taskComment d-flex">
            <i class="uiIcon uiCommentIcon"></i>
            <span class="taskCommentNumber caption">4</span>
          </div>
          <div class="taskAttachment  d-flex pl-3">
            <i class="uiIcon uiAttachIcon"></i>
            <span class="taskAttachNumber caption">2</span>
          </div>
        </div>
      </div>
      <v-divider/>
      <div class="taskStatusAndDate d-flex justify-end pt-2">
        <div class="taskDueDate">
          <div v-if="taskDueDate">
            <date-format :value="taskDueDate" :format="dateTimeFormat" />
          </div>
          <div v-else>
            <span class="body-2 text-sub-title">{{ $t('label.noDueDate') }}</span>
          </div>
        </div>
      </div>
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
        user: {},
        iconSize: 26,
        dateTimeFormat: {
          year: 'numeric',
          month: 'numeric',
          day: 'numeric',
        },
        assigneeAndCoworkerArray: [],
        isPersonnalTask : this.task.task.status === null,
        drawer:null,
        maxAvatarToShow : 3
      }
    },
    computed: {
      taskDueDate() {
        return this.task && this.task.task.dueDate && this.task.task.dueDate.time;
      },
      avatarToDisplay () {
        if(this.assigneeAndCoworkerArray.length > this. maxAvatarToShow) {
          return this.assigneeAndCoworkerArray.slice(0, this.maxAvatarToShow-1);
        } else {
          return this.assigneeAndCoworkerArray;
        }
      },
      showMoreAvatarsNumber() {
        return this.assigneeAndCoworkerArray.length - this.maxAvatarToShow;
      }
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
          if(this.task.assignee){
        this.assigneeAndCoworkerArray.push(this.task.assignee);
        } 
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
        this.$root.$emit('open-task-drawer', this.task.task)
      },

      onCloseDrawer: function(drawer){
        this.drawer = drawer;
      }
    }
  }
</script>