<template>
  <div
    :class="getTaskPriorityColor(task.task.priority)"
    class="taskListItemView  px-4 py-3 d-flex align-center">
    <div class="taskCheckBox" >
      <v-switch
        ref="autoFocusInput2"
        class="d-none"
        reset
        true-value="true"
        false-value="false"/>
      <i 
        :title="$t(getTaskCompletedTitle())" 
        :class="getTaskCompleted()"
        @click="updateCompleted"></i>
    </div>
    <div class="taskTitleAndId pl-2 d-lg-none" @click="openTaskDrawer()">
      <div class="taskId">
        <span class="caption text-sub-title">ID : {{ task.task.id }}</span>
      </div>
      <div class="taskTitle pr-3">
        <a
          ref="tooltip"
          :class="getTitleTaskClass()"
          class="text-truncate">
          {{ task.task.title }}
        </a>
      </div>
    </div>
    <div class="taskTitle pr-3 d-lg-block d-md-none" @click="openTaskDrawer()">
      <a
        ref="tooltip"
        :class="getTitleTaskClass()"
        class="text-truncate">
        {{ task.task.title }}
      </a>
    </div>
    <div class="taskProject pr-4">
      <div
        v-if="!isPersonnalTask"
        class="projectSpaceDetails d-flex align-center TasksListViewProject">
        <div class="spaceAvatar pr-1 d-lg-block d-md-none">
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
                class="mx-auto spaceAvatarImg"/>
            </v-avatar>
          </a>
        </div>
        <div class="taskProjectNameChip">
          <v-chip
            :color="getTaskColor()"
            text-color="white"
            class="font-weight-bold"
            small
            @click="showProjectTasksDetails()">
            <span class="text-truncate">
              {{ getNameProject() }}
            </span>
          </v-chip>
        </div>
      </div>
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
    <div class="taskStat d-lg-block d-md-none" @click="openTaskDrawer()">
      <span v-if="task && task.task && task.task.status && task.task.status" class="taskStatLabel pl-2">
        {{ getTaskStatusLabel(task.task.status.name) }}
      </span>
    </div>
    <div class="taskDateAndStat" @click="openTaskDrawer()">
      <div class="taskStat d-lg-none">
        <span v-if="task && task.task && task.task.status && task.task.status" class="taskStatLabel pl-2">
          {{ getTaskStatusLabel(task.task.status.name) }}
        </span>
      </div>
      <div class="taskDueDate" @click="openTaskDrawer()">
        <div v-if="taskDueDate">
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
        isPersonnalTask : this.task.status === null,
        labelList: '',
        isSpaceProject: this.task.space !== null,
        maxAvatarToShow : 3,
        showCompleteTasks: false
      }
    },
    computed: {
      taskDueDate() {
        return this.task && this.task.task.dueDate && this.task.task.dueDate.time;
      },
      avatarToDisplay () {
        this.getTaskAssigneeAndCoworkers();
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
      getTaskColor() {
        if(this.task.task.status){
          return this.task.task.status.project.color ? this.task.task.status.project.color : 'noProjectColor' ;
        }
        else {
          return null
        }
      },updateCompleted() {

        const task = {
          id: this.task.task.id,
          showCompleteTasks: this.showCompleted(),
        };


        if (typeof task.id !== 'undefined') {
          return this.$tasksService.updateCompleted(task).then(task => {
            if(task.completed){
              this.$root.$emit('show-alert', {type: 'success',message: this.$t('alert.success.task.completed')});   
            }else{
              this.$root.$emit('show-alert', {type: 'success',message: this.$t('alert.success.task.unCompleted')});
            }
            this.$root.$emit('update-cart', task);
          }).then(this.task.task.completed = task.showCompleteTasks)
                  .catch(e => {
                    console.debug("Error updating project", e);
                    this.$emit('error', e && e.message ? e.message : String(e));
                    this.postProject = false;
                  });
        }


      },
      getTaskCompleted() {
        if(this.task.task.completed===true){
          return 'uiIconValidate';
        }
        else {
          return 'uiIconCircle'
        }
      },
      getTitleTaskClass() {
        if(this.task.task.completed===true){
          return 'text-color strikethrough';
        }
        else {
          return 'text-color'
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
      showCompleted(){
        if(this.getTaskCompleted()==='uiIconValidate'){
          this.showCompleteTasks=false
        }else {
          this.showCompleteTasks=true
        }
        return this.showCompleteTasks
      },
      getNameProject(){
        if(this.task.task.status){
          return  this.task.task.status.project.name  ;
        }
        else {
          return null
        }
      },
      getTaskAssigneeAndCoworkers() {
        this.assigneeAndCoworkerArray=[]
                if(this.task.assignee) {
                  if (!this.assigneeAndCoworkerArray.some(assigneeAndCowoker => assigneeAndCowoker.username === this.task.assignee.username)) {

                    this.assigneeAndCoworkerArray.push(this.task.assignee);
                  }
                }
        if (this.task.coworker && this.task.coworker.length > 0 )
        {
          if(!  this.assigneeAndCoworkerArray.some(assigneeAndCowoker => assigneeAndCowoker.username === this.task.coworker.username)){

            this.task.coworker.forEach((coworker) => {
            this.assigneeAndCoworkerArray.push(coworker);
          })
        }}
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
      },
      openTaskDrawer() {
        this.$root.$emit('open-task-drawer', this.task.task)
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
    }
  }
</script>
