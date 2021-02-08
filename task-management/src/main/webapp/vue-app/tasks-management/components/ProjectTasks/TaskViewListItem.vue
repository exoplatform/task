<template>
  <div
    :class="getTaskPriorityColor(task.task.priority)"
    class="taskListItemView px-4 py-3 d-flex align-center">
    <div class="taskCheckBox" @click="updateCompleted">
      <v-switch
        ref="autoFocusInput2"
        class="d-none"
        true-value="true"
        false-value="false"/>
      <i :title="$t(getTaskCompletedTitle())" :class="getTaskCompleted()"></i>
    </div>
    <div class="taskTitle pr-3">
      <a
        ref="tooltip"
        :class="getTitleTaskClass()"
        class="text-truncate"
        @click="openTaskDrawer()">{{ task.task.title }}
      </a>
    </div>
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
    <div class="taskLabels " @click="openTaskDrawer()">
      <v-chip
        v-if="task.labels && task.labels.length == 1"
        :color="task.labels[0].color"
        class="mx-1 white--text font-weight-bold"
        label
        small>
        <span class="text-truncate">
          {{ task.labels[0].name }}
        </span>
      </v-chip>
      <div
        v-else-if="task.labels && task.labels.length > 1"
        :title="getLabelsList(task.labels)"
        class="taskTags d-flex justify-center">
        <i class="uiIcon uiTagIcon"></i>
        <span class="taskAttachNumber caption">{{ task.labels.length }}</span>
      </div>
    </div>
    <div class="taskActions d-flex justify-center align-center" @click="openTaskDrawer()">
      <div v-if="task.commentCount" class="taskComment d-flex">
        <i class="uiIcon uiCommentIcon"></i>
        <span class="taskCommentNumber caption">{{ task.commentCount }}</span>
      </div>
    </div>
    <div class="taskDueDate" @click="openTaskDrawer()">
      <div v-if="taskDueDate">
        <date-format :value="taskDueDate" :format="dateTimeFormat" />
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
        iconSize: 26,
        dateTimeFormat: {
          year: 'numeric',
          month: 'numeric',
          day: 'numeric',
        },
        assigneeAndCoworkerArray: [],
        isPersonnalTask : this.task.task.status === null,
        labelList: '',
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
       this.$root.$emit('open-task-drawer', this.task.task);
      },
    getTaskCompleted() {
      if(this.task.task.completed===true){
        return 'uiIconValidate';
      }
      else {
        return 'uiIconCircle'
      }
    },
    getTaskCompletedTitle() {
      if(this.task.task.completed===true){
        return 'message.markAsUnCompleted';
      }
      else {
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
    showCompleted(){
      if(this.getTaskCompleted()==='uiIconValidate'){
        this.showCompleteTasks=false
      }else {
        this.showCompleteTasks=true
      }
      return this.showCompleteTasks
    },
    getTitleTaskClass() {
      if(this.task.task.completed===true){
        return 'text-color strikethrough';
      }
      else {
        return 'text-color'
      }
    },
    }
  }
</script>
