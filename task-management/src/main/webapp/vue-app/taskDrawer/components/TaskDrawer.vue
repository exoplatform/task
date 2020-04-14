<template>
  <v-navigation-drawer
    id="task-Drawer"
    v-model="drawer"
    right
    temporary
    stateless
    absolute
    width="33%">
    <div class="drawer-header">
      <v-flex xs12>
        <v-card flat>
          <v-layout>
            <v-flex xs10>
              <v-card-text class="blueGrey-Color drawer-title">{{ $t('label.drawer.header') }}</v-card-text>
            </v-flex>
            <v-flex xs1>
              <v-btn
                class="my-2"
                icon
                @click="navigateTo(`tasks/taskDetail/${task.id}`)">
                <i class="uiIconAndroidOpen grey-color"></i>
              </v-btn>
            </v-flex>
            <v-flex xs1>
              <v-btn
                class="my-2 grey-color"
                icon
                @click="closeDrawer()">
                <v-icon size="22">mdi-close</v-icon>
              </v-btn>
            </v-flex>
          </v-layout>
        </v-card>
      </v-flex>
    </div>
    <div class="drawer-content">
      <v-container pt-0>
        <v-layout row>
          <v-col class="pb-0">
            <task-projects :task="task"/>
            <task-labels :task="task"/>
            <v-btn
              id="check_btn"
              class="ml-n2"
              icon
              dark
              @click="markAsCompleted()">
              <v-icon dark size="18">mdi-checkbox-marked-circle</v-icon>
            </v-btn>
            <v-text-field
              v-if="!task.completed"
              v-model="task.title"
              :placeholder="$t('label.title')"
              class="pl-0 pt-0 task-name"
              type="text"
              color="#578DC9"
              @change="updateTask"/>
            <v-text-field
              v-else
              v-model="task.title"
              :placeholder="$t('label.title')"
              class="pl-0 pt-0 task-name"
              style="text-decoration: line-through"
              type="text"/>
          </v-col>
          <v-container py-0>
            <v-flex xs12>
              <v-layout>
                <v-flex
                  xs4
                  class="pl-1">
                  <v-menu
                    v-custom-click-outside="closeDatePickerMenu"
                    ref="menu"
                    v-model="datePickerMenu"
                    :close-on-content-click="false"
                    :return-value.sync="date"
                    attach
                    transition="scale-transition"
                    offset-y
                    min-width="290px">
                    <template v-slot:activator="{ on }">
                      <v-text-field
                        v-model="date"
                        :placeholder="$t('label.dueDate')"
                        class="pt-0 mt-0 dateFont"
                        prepend-icon
                        readonly
                        solo
                        @click="openDatePickerMenu()"
                        @change="updateDueDate"
                        v-on="on">
                        <template v-slot:prepend class="mr-4">
                          <i class="uiIconClock uiIconBlue"></i>
                        </template>
                      </v-text-field>
                    </template>
                    <v-date-picker
                      v-model="date" 
                      no-title 
                      scrollable>
                      <v-spacer/>
                      <v-btn
                        small
                        min-width="40"
                        text
                        color="primary"
                        @click="date=null;$refs.menu.save(date);updateDueDate()">{{ $t('popup.clear') }}</v-btn>
                      <v-btn
                        v-if="task.dueDate != null"
                        min-width="40"
                        small
                        text 
                        color="primary" 
                        @click="$refs.menu.save(date);updateDueDate()">OK</v-btn>
                      <v-btn
                        v-else
                        min-width="40"
                        small
                        text 
                        color="primary" 
                        @click="$refs.menu.save(date);addDueDate()">OK</v-btn>
                    </v-date-picker>
                  </v-menu>
                </v-flex>
                <v-flex 
                  xs5>
                  <task-assignment :task="task"
                                   :global-menu="assigneeMenu"
                                   @menuIsOpen="openAssigneeMenu()"/>
                </v-flex>
                <v-flex 
                  xs3>
                  <div v-if="task.status != null">
                    <v-select
                      v-custom-click-outside="closeStatusList"
                      ref="selectStatus"
                      v-model="task.status.name"
                      :items="taskStatus"
                      item-value="key"
                      item-text="value"
                      attach
                      class="pt-0 selectFont"
                      solo
                      @change="updateTaskStatus()">
                      <template v-slot:prepend>
                        <i class="uiIconTime uiIconBlue"></i>
                      </template>
                    </v-select>
                  </div>
                </v-flex>
              </v-layout>
            </v-flex>
          </v-container>
          <v-flex xs12>
            <div class="py-3 px-4 mr-4">
              <div>
                <task-description-editor 
                  v-model="task.description" 
                  :placeholder="descriptionPlaceholder"/>
              </div>
            </div>
          </v-flex>
          <v-flex xs12 pl-3>
            <v-layout row ml-4>
              <v-flex 
                xs4
                row>
                <div style="white-space: nowrap">
                  <v-select
                    v-custom-click-outside="closePrioritiesList"
                    ref="selectPriority"
                    v-model="task.priority"
                    :items="priorities"
                    item-value="key"
                    item-text="value"
                    attach
                    solo
                    class="pt-0 selectFont"
                    @change="updateTask(task.id)">
                    <template v-slot:prepend>
                      <v-icon :color="getTaskPriorityColor(task.priority)" size="20">mdi-flag-variant</v-icon>
                    </template>
                  </v-select>
                </div>
              </v-flex>
            </v-layout>
          </v-flex>

          <v-flex xs12 class="pt-2 px-4">
            <v-tabs color="#578DC9">
              <v-tab class="text-capitalize">{{ $t('label.comments') }}</v-tab>
              <v-tab class="text-capitalize">{{ $t('label.changes') }}</v-tab>
              <v-tab-item class="pt-5">
                <v-list>
                  <v-list-item
                    v-for="(item, i) in comments"
                    :key="i"
                    class="pr-0">
                    <task-comments
                      :task="task"
                      :comment="item" 
                      :comments="comments"
                      @showSubEditor="showEditor = !showEditor"/>
                  </v-list-item>
                  <v-list-item v-if="showEditor">
                    <v-list-item-avatar 
                      class="mt-0" 
                      size="30" 
                      tile>
                      <v-img :src="currentUserAvatar"/>
                    </v-list-item-avatar>
                    <v-layout row class="editorContent ml-0">
                      <task-comment-editor
                        v-model="editorData"      
                        :placeholder="commentPlaceholder"
                        :reset="reset"
                        class="mr-4 comment"/>
                      <v-btn
                        :disabled="disabledComment"
                        depressed
                        small
                        dark
                        class="mt-1 mb-2 commentBtn"
                        @click="addTaskComment()">{{ $t('comment.label.comment') }}</v-btn>
                    </v-layout>
                  </v-list-item>
                  <a 
                    v-else 
                    class="pl-4" 
                    @click="openEditor">{{ $t('comment.label.comment') }}</a>
                </v-list>
              </v-tab-item>
              <v-tab-item class="pt-5">
                <v-list class="py-0">
                  <v-list-item
                    v-for="(item, i) in logs"
                    :key="i"
                    class="pr-0">
                    <log-details :change-log="item"/>
                  </v-list-item>
                </v-list>
              </v-tab-item>
            </v-tabs>
          </v-flex>
        </v-layout>
      </v-container>
    </div>
  </v-navigation-drawer>
</template>

<script>

  import {updateTask, getTaskLogs, getTaskComments, addTaskComments} from '../taskDrawerApi';

  export default {
    props: {
      drawer: {
        type: Boolean,
        default: false
      },
      task: {
        type: Object,
        default: () => {
          return {};
        }
      },
    },
    data() {
      return {
        editorData: null,
        emptyValue: '',
        reset: false,
        disabledComment: true,
        priorities: [{key:'HIGH',value:this.$t('label.priority.high')},
          {key:'NORMAL',value:this.$t('label.priority.normal')},
          {key:'LOW',value:this.$t('label.priority.low')},
          {key:'NONE',value:this.$t('label.priority.none')}],

        taskStatus: [{key:'ToDo',value:this.$t('exo.tasks.status.todo')},
          {key:'InProgress',value:this.$t('exo.tasks.status.inprogress')},
          {key:'WaitingOn',value:this.$t('exo.tasks.status.waitingon')},
          {key:'Done',value:this.$t('exo.tasks.status.done')}],
        
        date: null,
        datePickerMenu: false,
        assigneeMenu : false,
        showEditor : true,
        commentPlaceholder : this.$t('comment.message.addYourComment'),
        descriptionPlaceholder : this.$t('editinline.taskDescription.empty'),
        chips: [],
        autoSaveDelay: 1000,
        saveDescription: '',
        logs:[],
        comments:[],
      }
    },
    computed: {
      currentUserAvatar() {
        return `/rest/v1/social/users/${eXo.env.portal.userName}/avatar`;
      }
    },
    watch: {
      'task.description': function(newValue, oldValue) {  
        if (newValue !== oldValue) { 
          this.autoSaveDescription(); 
        } 
      },
      editorData(val) {
        this.disabledComment = val === '';
      } 
    },
    created() {
      this.retrieveTaskLogs();
      this.getTaskComments();
      if (this.task.dueDate != null) {
        this.date = new Date(this.task.dueDate.time).toISOString().substr(0, 10);
      }
      document.addEventListener('keyup', this.escapeKeyListener);
    },
    destroyed: function() {
      document.removeEventListener('keyup', this.escapeKeyListener);
    },
    methods: {
      closeDrawer() {
        this.drawer = false;
        this.$emit('closeDrawer',this.drawer);
        this.showEditor=false;
      },
      openEditor() {
          this.showEditor = true;
      },
      addTaskComment() {
        this.editorData=this.editorData.replace(/\n|\r/g,'');
        addTaskComments(this.task.id,this.editorData).then((comment => {
          this.comments.push(comment)
        })
        );
        this.reset = !this.reset;
      },
      getUserAvatar(username) {
        return `/rest/v1/social/users/${username}/avatar`;
      },
      getTaskPriorityColor(priority) {
        switch (priority) {
          case "HIGH":
            return "#bc4343";
          case "NORMAL":
            return "#ffb441";
          case "LOW":
            return "#2eb58c";
          case "NONE":
            return "#578dc9";
        }
      },
      markAsCompleted(){
        this.task.completed = !this.task.completed;
        this.updateTask()
      },
      addDueDate() {
        if (this.date === null) {
          this.task.dueDate = null;
        } else {
          const dueDate = {};
          const date = new Date(this.date);
          dueDate.time = date.getTime();
          dueDate.year = date.getUTCFullYear() - 1900;
          dueDate.month = date.getMonth();
          dueDate.day = date.getDay();
          dueDate.hours = date.getHours();
          dueDate.minutes = date.getMinutes();
          dueDate.seconds = date.getSeconds();
          dueDate.timezoneOffset = date.getTimezoneOffset();
          dueDate.date = date.getDate();
          this.task.dueDate = dueDate;
        }
        this.updateTask();
      },
      updateDueDate() {
        if (this.date === null) {
          this.task.dueDate = null;
        } else {
          const date = new Date(this.date);
          this.task.dueDate.time = date.getTime();
          this.task.dueDate.year = date.getUTCFullYear() - 1900;
          this.task.dueDate.month = date.getMonth();
          this.task.dueDate.day = date.getDay();
          this.task.dueDate.hours = date.getHours();
          this.task.dueDate.minutes = date.getMinutes();
          this.task.dueDate.seconds = date.getSeconds();
          this.task.dueDate.timezoneOffset = date.getTimezoneOffset();
          this.task.dueDate.date = date.getDate();
        }
        this.updateTask()
      },
      updateTaskStatus() {
        let statusId = 0;
        switch (this.task.status.name) {
          case "ToDo":
            statusId = 1;
            break;
          case "InProgress":
            statusId = 2;
            break;
          case "WaitingOn":
            statusId = 3;
            break;
          case "Done":
            statusId = 4;
            break;
        }
        if (this.task.status.project.id > 1) {
          this.task.status.id = 4 * (this.task.status.project.id -1 ) + statusId;
        } else {
          this.task.status.id = statusId;
        }
        this.updateTask()
      },
      updateTask() {
        updateTask(this.task.id,this.task);
      },
      autoSaveDescription() {
        clearTimeout(this.saveDescription);
        this.saveDescription = setTimeout(() => {
          Vue.nextTick(() => this.updateTask(this.task.id));
        }, this.autoSaveDelay);
      },
      retrieveTaskLogs() {
        getTaskLogs(this.task.id).then(
                (data) => {
                  this.logs = data;
                });
        return this.logs
      },
      getTaskComments() {
        getTaskComments(this.task.id).then(
                (data) => {
                  this.comments = data;
                });
        return this.comments
      },
     
      navigateTo(pagelink) {
        window.open(`${ eXo.env.portal.context }/${ eXo.env.portal.portalName }/${ pagelink }`, '_blank');
      },
      closeDatePickerMenu() {
        this.datePickerMenu = false;
      },
      closeStatusList() {
        if (typeof this.$refs.selectStatus !== 'undefined') {
          this.$refs.selectStatus.isMenuActive = false;
        }
      },
      closePrioritiesList() {
        if (typeof this.$refs.selectPriority !== 'undefined') {
          this.$refs.selectPriority.isMenuActive = false;
        }
      },
      openDatePickerMenu() {
        this.closePrioritiesList();
        this.closeStatusList();
        this.assigneeMenu = false;

      },
      openAssigneeMenu() {
        this.closePrioritiesList();
        this.closeStatusList();
        this.datePickerMenu = false;
      },
      escapeKeyListener: function(evt) {
        if (evt.keyCode === 27) {
          this.$refs.selectPriority.isMenuActive = false;
          this.$refs.selectStatus.isMenuActive = false;
          this.datePickerMenu = false;
        }
      }
    }
  }
</script>