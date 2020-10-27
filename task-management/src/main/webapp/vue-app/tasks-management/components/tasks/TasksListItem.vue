<template>
  <div
    :class="getTaskPriorityColor(task.task.priority)"
    class="taskListItemView  px-4 py-3 d-flex align-center">
    <div class="taskCheckBox">
      <i :title="$t('message.markAsCompleted')" class="uiIcon uiIconCircle"></i>
    </div>
    <div class="taskTitle pr-3">
      <a
        ref="tooltip"
        class="text-color"
        @click="openTaskDrawer()">
        <span>{{ task.task.title }}</span>
      </a>
    </div>
    <div class="taskProject pr-3">
      <div v-if="isPersonnalTask" class="taskProjectName mr-3 pa-1">
        <span class="body-2 text-sub-title">{{ $t('label.noProject') }}</span>
      </div>
      <div v-else class="projectSpaceDetails d-flex">
        <div class="spaceAvatar pr-1">
          <a
            v-if="task.space!==null"
            :href="spaceUrl(task.space.url)">
            <v-avatar
              :size="32"
              tile>
              <v-img
                :src="task.space.avatarUrl"
                :height="31"
                :width="31"
                :max-height="31"
                :max-width="31"
                class="mx-auto spaceAvatarImg"/>
            </v-avatar>
          </a>
          <v-avatar
            v-else
            :size="32"
            tile
            class="noSpaceAvatar">
            <i class="uiIconEcmsNameSpace noSpaceProjectIcon"></i>
          </v-avatar>
        </div>
        <div
          :class="getTaskColor()"
          class="taskProjectName taskProjectNameCard pa-1">
          <span class="font-weight-bold">{{ getNameProject() }}</span>
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
    <div class="taskLabels">
      <span v-if="task.labels && task.labels.length == 1" class="labelText">{{ task.labels[0].name }}</span>
      <span
        v-else-if="task.labels && task.labels.length > 1"
        :title="getLabelsList(task.labels)"
        class="labelText">{{ task.labels.length }} {{ $t('label.labels') }}
      </span>
      <span v-else class="noLabelText body-2"> {{ $t('label.noLabel') }}</span>
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
      <span v-if="isPersonnalTask" class="body-2 text-sub-title">{{ $t('label.noStatus') }}</span>
      <span v-else class="taskStatLabel pl-2">
        {{ getTaskStatusLabel(getNameProject()) }}
      </span>
    </div>
    <div class="taskDueDate">
      <div v-if="taskDueDate">
        <date-format :value="taskDueDate" :format="dateTimeFormat" />
      </div>
      <div v-else>
        <span class="body-2 text-sub-title">{{ $t('label.noDueDate') }}</span>
      </div>
    </div>
    <task-drawer 
      v-if="drawer"
      :drawer="drawer"
      :task="task.task"
      @updateTaskList="updateTaskList()"
      @closeDrawer="onCloseDrawer"/>
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
        drawer:null,
        isSpaceProject: this.task.space !== null,
        maxAvatarToShow : 3
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
        this.drawer = true;
      },
      onCloseDrawer: function(drawer){
        this.drawer = drawer;
      },
      spaceUrl(spaceUrl) {
        if (!this.spaceUrl) {
          return '#';
        }
        return `${eXo.env.portal.context}/g/:spaces:${spaceUrl}/`;
      }
    }
  }
</script>
