<template>
  <v-app id="taskCardItem" class="py-3">
    <v-card
      :class="[getTaskPriorityColor(task.task.priority)]"
      class="taskCard taskViewCard pa-3"
      flat>
      <div class="taskTitleId  d-flex justify-space-between">
        <div class="taskTitle d-flex align-start">
          <div class="taskCheckBox" @click="updateCompleted" >
            <v-switch
              ref="autoFocusInput2"
              class="d-none"
              true-value="true"
              false-value="false"/>
            <i :title="$t(getTaskCompletedTitle())" :class="getTaskCompleted()"></i>
          </div>
          <a
            ref="tooltip"
            :class="getTitleTaskClass()"
            :title="task.task.title"
            class="taskCardViewTitle"
            @click="openTaskDrawer()">
            <span class="taskTitleEllipsis">{{ task.task.title }}</span>
          </a>
        </div>
        <div class="taskId" @click="openTaskDrawer()">
          <span class="caption text-sub-title">ID : {{ task.task.id }}</span>
        </div>
      </div>
      <div 
        v-if="assigneeAndCoworkerArray && assigneeAndCoworkerArray.length"
        class="taskWorker d-flex justify-space-between align-center my-3">
        <div
          :class="assigneeAndCoworkerArray && !assigneeAndCoworkerArray.length && task && task.labels && !task.labels.length && 'hideTaskAssignee'"
          class="taskAssignee d-flex flex-nowrap">
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

      </div>
      <v-divider v-if="taskDueDate || (task.labels && task.labels.length)"/>
      <div class="taskActionsAndDate d-flex justify-space-between pt-3">
        <div 
          class="taskActionsAndLabels d-flex align-center"
          @click="openTaskDrawer()">
          <div v-if="task.commentCount" class="taskComment d-flex pr-2">
            <i class="uiIcon uiCommentIcon"></i>
            <span class="taskCommentNumber caption">{{ task.commentCount }}</span>
          </div>
          <div v-if="task.labels && task.labels.length" class="taskLabels ">
            <v-chip
              v-if="task.labels && task.labels.length == 1"
              :color="task.labels[0].color"
              class="mx-1 white--text font-weight-bold"
              label
              small>
              {{ task.labels[0].name }}
            </v-chip>
            <div
              v-else-if="task.labels && task.labels.length > 1"
              :title="getLabelsList(task.labels)"
              class="taskTags d-flex">
              <i class="uiIcon uiTagIcon"></i>
              <span class="taskAttachNumber caption">{{ task.labels.length }}</span>
            </div>
          </div>
        </div>
        <div v-if="taskDueDate" class="taskStatusAndDate">
          <div class="taskDueDate">
            <div >
              <date-format :value="taskDueDate" :format="dateTimeFormat" />
            </div>
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
        maxAvatarToShow : 3,
        showCompleteTasks: false

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
      this.$root.$on('update-completed-task',(value,id)=>{
        if (this.task.id === id){
          this.task.task.completed=value;
          if (this.task.task.completed === true && !this.showCompletedTasks){
            this.$emit('update-task-completed', this.task.task);
            this.removeCompletedTask = true;
          }
        }
      });
      this.getTaskAssigneeAndCoworkers();
    },
    methods: {
      getTaskPriorityColor(priority) {
        switch (priority) {
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
        if (this.task.assignee) {
          this.assigneeAndCoworkerArray.push(this.task.assignee);
        }
        if (this.task.coworker || this.task.coworker.length > 0) {
          this.task.coworker.forEach((coworker) => {
            if (coworker){
              this.assigneeAndCoworkerArray.push(coworker);
            }
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
        this.$root.$emit('open-task-drawer', this.task.task,)
      },

      onCloseDrawer: function (drawer) {
        this.drawer = drawer;
      },
      getTaskCompleted() {
        if (this.task.task.completed === true) {
          return 'uiIconValidate';
        } else {
          return 'uiIconCircle'
        }
      },
      getTaskCompletedTitle() {
        if (this.task.task.completed === true) {
          return 'message.markAsUnCompleted';
        } else {
          return 'message.markAsCompleted'
        }
      },
      updateCompleted() {

        const task = {
          id: this.task.task.id,
          showCompleteTasks: this.showCompleted(),
        };


        if (typeof task.id !== 'undefined') {
          return this.$tasksService.updateCompleted(task).then(task => {
            this.$emit('update-task-completed', task);
          }).then(this.task.task.completed = task.showCompleteTasks)
                  .catch(e => {
                    console.debug("Error updating project", e);
                    this.$emit('error', e && e.message ? e.message : String(e));
                    this.postProject = false;
                  });
        }


      },
      showCompleted() {
        if (this.getTaskCompleted() === 'uiIconValidate') {
          this.showCompleteTasks = false
        } else {
          this.showCompleteTasks = true
        }
        return this.showCompleteTasks
      },
      getTitleTaskClass() {
        if (this.task.task.completed === true) {
          return 'text-color strikethrough';
        } else {
          return 'text-color'
        }
      },
    }
  }
</script>
