<template>
  <div
    :class="[getTaskPriorityColor(task.task.priority), removeCompletedTask && 'completedTask' || '']"
    class="taskListItemView  px-4 py-3 d-flex align-center">
    <div class="taskCheckBox">
      <v-switch
        ref="autoFocusInput2"
        class="d-none"
        reset
        true-value="true"
        false-value="false" />
      <i 
        :title="$t(getTaskCompletedTitle())" 
        :class="getTaskCompleted()"
        @click="updateCompleted"></i>
    </div>
    <div class="taskTitleAndId ps-2 d-lg-none" @click="openTaskDrawer()">
      <div class="taskId">
        <span class="caption text-sub-title">ID : {{ task.task.id }}</span>
      </div>
      <div class="taskTitle pe-3">
        <a
          ref="tooltip"
          :class="getTitleTaskClass()"
          :title="task.task.title"
          class="text-truncate">
          {{ task.task.title }}
        </a>
      </div>
    </div>
    <div class="taskTitle pe-14 d-lg-block d-md-none" @click="openTaskDrawer()">
      <a
        ref="tooltip"
        :class="getTitleTaskClass()"
        :title="task.task.title"
        class="text-truncate">
        {{ task.task.title }}
      </a>
    </div>
    <div class="taskProject pe-10">
      <div
        v-if="!isPersonnalTask"
        class="projectSpaceDetails d-flex align-center TasksListViewProject">
        <div class="spaceAvatar pe-1 d-lg-block d-md-none">
          <a
            v-if="task.space!==null"
            :href="spaceUrl(task.space.url)">
            <v-avatar
              :size="30"
              tile>
              <v-img
                :src="task.space.avatarUrl"
                :height="30"
                :width="30"
                :max-height="30"
                :max-width="30"
                class="mx-auto spaceAvatarImg" />
            </v-avatar>
          </a>
        </div>
        <div :class="task.space ? 'taskProjectNameChipSpace' : 'taskProjectNameChip'">
          <v-chip
            :color="getTaskColor()"
            text-color="white"
            class="font-weight-bold"
            small
            @click="showProjectTasksDetails()">
            <span class="text-truncate" :title="getNameProject()">
              {{ getNameProject() }}
            </span>
          </v-chip>
        </div>
      </div>
    </div>
    <div class="taskAssignee v-avatar d-flex pe-7 flex-nowrap">
      <exo-user-avatar
        v-for="user in avatarToDisplay"
        :key="user"
        :username="user.username"
        :title="user.displayName"
        :avatar-url="user.avatar"
        :external="user.external"
        :retrieve-extra-information="false"
        :size="iconSize"
        :style="'background-image: url('+user.avatar+')'"
        class="mx-1 taskWorkerAvatar" />
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
    <div class="taskLabels pe-6" @click="openTaskDrawer()">
      <v-chip
        v-if="task.labels && task.labels.length == 1"
        :color="task.labels[0].color"
        :title="task.labels[0].name"
        class="mx-1 font-weight-bold"
        style="max-width: 70%"
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
    <div class="taskActions d-flex justify-center pe-9 align-center " @click="openTaskDrawer()">
      <div v-if="task.commentCount" class="taskComment d-flex">
        <i class="uiIcon uiCommentIcon"></i>
        <span class="taskCommentNumber caption">{{ task.commentCount }}</span>
      </div>
    </div>
    <div class="taskStat pe-9 d-lg-block d-md-none " @click="openTaskDrawer()">
      <span
        v-if="task && task.task && task.task.status && task.task.status"
        :title="getTaskStatusLabel(task.task.status.name)"
        class="taskStatLabel ps-2">
        {{ getTaskStatusLabel(task.task.status.name) }}
      </span>
    </div>
    <div class="taskDateAndStat" @click="openTaskDrawer()">
      <div class="taskStat d-lg-none">
        <span v-if="task && task.task && task.task.status && task.task.status" class="taskStatLabel ps-2">
          {{ getTaskStatusLabel(task.task.status.name) }}
        </span>
      </div>
      <div class="taskDueDate ">
        <div v-if="taskDueDate" :class="getOverdueTask(taskDueDate) ? 'red--text' : ''">
          <date-format :value="taskDueDate" :format="dateTimeFormat" />
        </div>
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
    },
    showCompletedTasks: {
      type: Boolean,
      default: false
    },
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
      isPersonnalTask: this.task.status === null,
      labelList: '',
      isSpaceProject: this.task.space !== null,
      maxAvatarToShow: 1,
    };
  },
  computed: {
    taskDueDate() {
      return this.task && this.task.task.dueDate && this.task.task.dueDate.time;
    },
    avatarToDisplay () {
      if (this.assigneeAndCoworkerArray.length > this. maxAvatarToShow) {
        return this.assigneeAndCoworkerArray.slice(0, this.maxAvatarToShow-1);
      } else {
        return this.assigneeAndCoworkerArray;
      }
    },
    showMoreAvatarsNumber() {
      return this.assigneeAndCoworkerArray.length - this.maxAvatarToShow;
    },
    removeCompletedTask() {
      return this.task.task.completed === true && !this.showCompletedTasks;
    }
  },
  created() {
    this.$root.$on('update-completed-task',(value,id)=>{
      if (this.task.id === id){
        this.task.task.completed=value;
        if (this.task.task.completed === true ){
          this.$root.$emit('update-cart', this.task.task);
          this.$root.$emit('update-task-completed', this.task.task);
        }
      }
    });
    this.$root.$on('update-task-assignee',(value,id)=>{
      this.updateTaskAssignee(value,id);
    });
    this.$root.$on('update-remove-task-labels',(value,id)=>{
      this.updateRemoveTaskLabels(value,id);
    });
    this.$root.$on('update-task-labels',(value,id)=>{
      this.updateTaskLabels(value,id);
    });
    this.$root.$on('update-task-comments',(value,id)=>{
      this.updateTaskComments(value,id);
    });
    this.$root.$on('update-task-coworker',(value,id)=>{
      this.updateTaskCoworker(value,id);
    });
    this.getTaskAssigneeAndCoworkers();
    this.$root.$on('update-task-project', task => {
      if (this.task.task.status && this.task.task.id===task.id){
        this.isPersonnalTask=false;
      }
    });
  },
  methods: {
    updateRemoveTaskLabels(value,id){
      if (this.task.id === id){
        this.task.labels=this.task.labels.filter(item => item.id !== value);
      }
    },
    updateTaskLabels(value,id){
      if (this.task.id === id){
        this.task.labels.push(value);
      }
    },
    updateTaskComments(value,id){
      if (this.task.id === id){
        this.task.commentCount=value;
      }
    },
    updateTaskCoworker(value,id){
      if (this.task.id === id){
        if (value && value.length) {
          this.task.coworker=[];
          value.forEach((coworker) => {
            this.$identityService.getIdentityByProviderIdAndRemoteId('organization',coworker).then(user => {
              const taskCoworker = {
                id: `organization:${user.remoteId}`,
                username: user.remoteId,
                providerId: 'organization',
                displayName: user.profile.fullname,
                avatar: user.profile.avatar,
              };
              if (taskCoworker && !this.task.coworker.includes(taskCoworker)){
                this.task.coworker.push(taskCoworker);
              }
            });
          });
        } else {
          this.task.coworker = [];
        }
        this.$root.$emit('task-assignee-coworker-updated', this.task);
      }
    },
    updateTaskAssignee(value,id){

      if (this.task.id === id){
        if (value===null){
          this.task.assignee = null;
        } else {
          this.$identityService.getIdentityByProviderIdAndRemoteId('organization',value).then(user => {
            this.task.assignee = {
              id: `organization:${user.remoteId}`,
              username: user.remoteId,
              providerId: 'organization',
              displayName: user.profile.fullname,
              avatar: user.profile.avatar,
            };
          });
        }
        this.$root.$emit('task-assignee-coworker-updated', this.task);
      }
    },
    getTaskPriorityColor(priority) {
      switch (priority) {
      case 'HIGH':
        return 'taskHighPriority';
      case 'NORMAL':
        return 'taskNormalPriority';
      case 'LOW':
        return 'taskLowPriority';
      case 'NONE':
        return 'taskNonePriority';
      }
    },
    getTaskColor() {
      if (this.task.task.status){
        return this.task.task.status.project.color ? this.task.task.status.project.color : 'noProjectColor' ;
      }
      else {
        return null;
      }
    },
      
    updateCompleted() {
      const task = {
        id: this.task.task.id,
        isCompleted: !this.task.task.completed,
      };
      
      if (task.id) {
        return this.$tasksService.updateCompleted(task).then(updatedTask => {
          if (updatedTask.completed) {
            this.$root.$emit('show-alert', {type: 'success', message: this.$t('alert.success.task.completed')});
          } else {
            this.$root.$emit('show-alert', {type: 'success', message: this.$t('alert.success.task.unCompleted')});
          }
          this.$root.$emit('update-cart', updatedTask);
          this.$root.$emit('update-task-completed', updatedTask);
          this.task.task.completed = updatedTask.completed;
        }).catch(e => {
          console.error('Error updating project', e);
          this.$root.$emit('show-alert', {
            type: 'error',
            message: this.$t('alert.error')
          });
          this.postProject = false;
        });
      }


    },
    getTaskCompleted() {
      if (this.task.task.completed===true){
        return 'uiIconValidate';
      }
      else {
        return 'uiIconCircle';
      }
    },
    getTitleTaskClass() {
      if (this.task.task.completed===true){
        return 'text-color strikethrough';
      }
      else {
        return 'text-color';
      }
    },
    getTaskCompletedTitle() {
      if (this.task.task.completed===true){
        return 'message.markAsUnCompleted';
      }
      else {
        return 'message.markAsCompleted';
      }
    },
    getNameProject(){
      if (this.task.task.status){
        return  this.task.task.status.project.name  ;
      }
      else {
        return null;
      }
    },
    getTaskAssigneeAndCoworkers() {
      this.assigneeAndCoworkerArray=[];
      if (this.task.assignee && !this.assigneeAndCoworkerArray.includes(this.task.assignee)) {
        this.assigneeAndCoworkerArray.push(this.task.assignee);
      }
      if (this.task.coworker || this.task.coworker.length > 0) {
        this.task.coworker.forEach((coworker) => {
          if (coworker && !this.assigneeAndCoworkerArray.includes(coworker)){
            this.assigneeAndCoworkerArray.push(coworker);
          }
        });
      }
    },
    getTaskStatusLabel(status) {
      switch (status) {
      case 'ToDo':
        return this.$t('exo.tasks.status.todo');
      case 'InProgress':
        return this.$t('exo.tasks.status.inprogress');
      case 'WaitingOn':
        return this.$t('exo.tasks.status.waitingon');
      case 'Done':
        return this.$t('exo.tasks.status.done');
      default:
        return status;
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
    onCloseDrawer: function(drawer){
      this.drawer = drawer;
    },
    spaceUrl(spaceUrl) {
      if (!this.spaceUrl) {
        return '#';
      }
      return `${eXo.env.portal.context}/g/:spaces:${spaceUrl}/`;
    },
    showProjectTasksDetails() {
      this.$root.$emit('show-project-details-tasks', this.task.task.status.project);
    },
    dateFormatter(dueDate) {
      if (dueDate) {
        const date = new Date(dueDate);
        const day = date.getDate();
        const month = date.getMonth()+1;
        const year = date.getFullYear();
        const formattedTime = `${  year}-${  month  }-${day  }`;
        return formattedTime;
      }
    },
    getOverdueTask(value){
      const Today = new Date();
      const formattedTimeToday = `${  Today.getFullYear()}-${  Today.getMonth()+1  }-${Today.getDate()  }`;
      const date = this.dateFormatter(value);
      if (date===formattedTimeToday){
        return false;
      }
      else if (new Date(value) < new Date().getTime()){
        return true;
      } else {
        return false;
      }
    }
  }
};
</script>
