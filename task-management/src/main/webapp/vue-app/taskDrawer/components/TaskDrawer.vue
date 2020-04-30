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
            <task-projects 
              :task="task" 
              :projects-list="projectsList" 
              @openProjectsList="openProjectsList()"/>
            <task-labels 
              :task="task" 
              :labels-list="labelsList" 
              @openLabelsList="openLabelsList()"/>
            <v-btn
              id="check_btn"
              class="ml-n2"
              icon
              dark
              @click="markAsCompleted()">
              <v-icon dark size="18">mdi-checkbox-marked-circle</v-icon>
            </v-btn>
            <v-textarea
              v-if="!task.completed"
              id="task-name"
              v-model="task.title"
              :placeholder="$t('label.title')"
              auto-grow
              rows="1"
              class="pl-0 pt-0 task-name"
              @change="updateTask"/>
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
                        v-on="on">
                        <template v-slot:prepend class="mr-4">
                          <i class="uiIconClock uiIconBlue"></i>
                        </template>
                      </v-text-field>
                    </template>
                    <v-date-picker
                      v-model="date" 
                      no-title
                      scrollable
                      @input="datePickerMenu = false;addDueDate()">
                      <v-spacer/>
                      <v-btn
                        small
                        min-width="40"
                        text
                        color="primary"
                        @click="date=null;$refs.menu.save(date);addDueDate()">{{ $t('label.none') }}</v-btn>
                      <v-divider vertical/>
                      <v-btn
                        min-width="40"
                        small
                        text
                        color="primary"
                        @click="$refs.menu.save(date);addDays()">{{ $t('label.today') }}</v-btn>
                      <v-divider vertical />
                      <v-btn
                        min-width="40"
                        small
                        text
                        color="primary"
                        @click="$refs.menu.save(date);addDays(1)">{{ $t('label.tomorrow') }}</v-btn>
                      <v-divider vertical />
                      <v-btn
                        min-width="40"
                        small
                        text
                        color="primary"
                        @click="$refs.menu.save(date);addDays(7)">{{ $t('label.nextweek') }}</v-btn>
                    </v-date-picker>                    
                  </v-menu>
                </v-flex>
                <v-flex 
                  xs5>
                  <task-assignment 
                    :task="task"
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
                      :items="projectStatuses"
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
          <v-container py-0>
            <v-flex xs12>
              <v-layout>
                <v-flex
                  xs7
                  class="pl-1"
                  @mouseover="showDeleteDateBtn = true"
                  @mouseleave="showDeleteDateBtn = false">
                  <v-menu
                    v-custom-click-outside="closeDateRangeMenu"
                    v-model="rangeDateMenu"
                    :close-on-content-click="false"
                    attach
                    transition="scale-transition"
                    offset-y
                    min-width="290px">
                    <template v-slot:activator="{ on }">
                      <v-text-field
                        v-model="dateRangeText"
                        :placeholder="$t('label.noWorkPlan')"
                        class="pt-0 pl-1 mt-0 dateFont"
                        prepend-icon
                        append-icon
                        readonly
                        style="width: 100%"
                        solo
                        @click="openDateRangeMenu()"
                        v-on="on">
                        model: {{ dates }}
                        <template v-slot:prepend class="mr-4">
                          <i class="uiIconPLFCalendar uiIconBlue"></i>
                        </template>
                        <template v-slot:append class="mr-4">
                          <v-btn 
                            v-show="dateRangeDeleteBtn"
                            :title="$t('label.remove')"
                            icon
                            @click="removeScheduledDate()">
                            <i style="font-size: 10px" class="uiIconTrashMini uiIconBlue "></i>
                          </v-btn>
                        </template>
                      </v-text-field>
                    </template>
                    <v-date-picker 
                      v-model="dates"
                      no-title 
                      range>
                      <v-spacer/>
                      <v-alert 
                        v-show="dateRangeError" 
                        width="240" 
                        class="mb-0 dueDateError" 
                        type="error">
                        {{ $t('editinline.taskPlan.errorMessage') }}
                      </v-alert>
                    </v-date-picker>
                  </v-menu>
                </v-flex>
                <v-flex xs5 pl-3>
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
                          <template slot="item" slot-scope="data">
                            <v-list-avatar class="mr-2">
                              <v-icon :color="getTaskPriorityColor(data.item.key)" size="20">mdi-flag-variant</v-icon>
                            </v-list-avatar>
                            <v-list-tile-content>
                              <v-list-tile-title class="body-2"> {{ data.item.value }}
                              </v-list-tile-title>
                            </v-list-tile-content>
                          </template>
                        </v-select>
                      </div>
                    </v-flex>
                  </v-layout>
                </v-flex>
              </v-layout>
            </v-flex>
          </v-container>        
          <v-container v-show="dateRangeAlerte" py-0>
            <v-flex xs12>
              <v-alert type="warning" class="mb-0 pa-2 dueDateAlert">
                {{ $t('message.dueDateBeforeEndDate') }}
              </v-alert>
            </v-flex>
          </v-container>
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
                      :is-open="!showEditor"
                      :close-editor="subEditorIsOpen"
                      @isOpen="OnCloseAllEditor()"
                      @showSubEditor="OnUpdateEditorStatus"/>
                  </v-list-item>
                  <v-list-item v-if="showEditor" class="comment">
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

  import {updateTask, getTaskLogs, getTaskComments, addTaskComments, getStatusesByProjectId} from '../taskDrawerApi';

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
        labelsList: false,
        projectsList: false,
        date: null,
        datePickerMenu: false,
        dates: [],
        showDeleteDateBtn : false,
        rangeDateMenu : false,
        assigneeMenu : false,
        showEditor : true,
        showSubEditor : false,
        commentPlaceholder : this.$t('comment.message.addYourComment'),
        descriptionPlaceholder : this.$t('editinline.taskDescription.empty'),
        chips: [],
        autoSaveDelay: 1000,
        saveDescription: '',
        logs:[],
        comments:[],
        subEditorIsOpen : false,
        projectStatuses : [],
      }
    },
    computed: {
      currentUserAvatar() {
        return `/rest/v1/social/users/${eXo.env.portal.userName}/avatar`;
      },
      dateRangeText () {
        return this.dates.join('~')
      },
      dateRangeAlerte () {
          return this.dates[1] > this.date
      } ,
      dateRangeError() {
        return this.dates[1] < this.dates[0]
      },
      dateRangeDeleteBtn() {
        return this.showDeleteDateBtn && this.dates.length !== 0
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
      },
      showEditor() {
        this.showSubEditor = !this.showEditor;
      },
      dates(val) {
          if(val[1] && val[1] > val[0]) {
              const startDate = {};
              const endDate = {};
              this.dateFormat(new Date(val[0]), startDate);
              this.dateFormat(new Date(val[1]), endDate);
              this.task.startDate = startDate;
              this.task.endDate = endDate;
              this.updateTask();
          }
      },
      drawer: {
        immediate: true,
        handler() {
          if (this.drawer) {
            $('body').addClass('hide-scroll');
          }
          this.$nextTick().then(() => {
            $('.v-overlay').click(() => {
              this.drawer = false;
              $('body').removeClass('hide-scroll');
              this.$emit('closeDrawer',this.drawer);
            });
          });
        }
      }
    },
    created() {
      this.retrieveTaskLogs();
      this.getTaskComments();
      this.getStatusesByProjectId();
      if (this.task.dueDate != null) {
          this.date = new Date(this.task.dueDate.time).toISOString().substr(0, 10);
      }
      if (this.task.startDate != null) {
          this.dates[0] = new Date(this.task.startDate.time).toISOString().substr(0, 10);
          this.dates[1] = new Date(this.task.endDate.time).toISOString().substr(0, 10);
      }
      document.addEventListener('keyup', this.escapeKeyListener);
    },
    destroyed: function() {
      document.removeEventListener('keyup', this.escapeKeyListener);
    },
    methods: {
      closeDrawer() {
        this.drawer = false;
        $('body').removeClass('hide-scroll');
        this.$emit('closeDrawer',this.drawer);
        this.showEditor=false;
      },
      openEditor() {
          this.showEditor = true;
          this.subEditorIsOpen = true;
          this.editorData = null;
      },
      OnUpdateEditorStatus : function(val){
        this.showEditor = !val;
        if (val === false) {
          this.subEditorIsOpen = false;
        }
      },
      OnCloseAllEditor() {
        this.subEditorIsOpen = true;
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
        this.$emit('updateTaskList');
        this.updateTask()
      },
      addDueDate() {
        if (this.date === null) {
          this.task.dueDate = null;
        } else {
          const dueDate = {};
          const date = new Date(this.date);
          this.dateFormat(date, dueDate);
          this.task.dueDate = dueDate;
        }
        this.updateTask();
      },
      addDays(days) {
        if (days) {
          const date = new Date();
          date.setDate(date.getDate() + days);
          this.date =  date.toISOString().substr(0, 10);
          this.addDueDate();
        } else {
          this.date =  new Date().toISOString().substr(0, 10);
          this.addDueDate();
        }
      },
      removeScheduledDate() {
        this.dates = []
        this.task.startDate = null;
        this.task.endDate = null;
        this.updateTask()
      },
      dateFormat(date, newDate) {
        newDate.time = date.getTime();
        newDate.year = date.getUTCFullYear() - 1900;
        newDate.month = date.getMonth();
        newDate.day = date.getDay();
        newDate.hours = date.getHours();
        newDate.minutes = date.getMinutes();
        newDate.seconds = date.getSeconds();
        newDate.timezoneOffset = date.getTimezoneOffset();
        newDate.date = date.getDate();
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
      getStatusesByProjectId() {
        if (this.task.status != null) {
          getStatusesByProjectId(this.task.status.project.id).then(
                  (data) => {
                    this.projectStatuses = data;
                    for (let i = 0; i < data.length; i++) {
                      switch (data[i].name) {
                        case 'ToDo':
                          this.projectStatuses[i] = {key: 'ToDo', value: this.$t('exo.tasks.status.todo')}
                          break;
                        case 'InProgress':
                          this.projectStatuses[i] = {key: 'InProgress', value: this.$t('exo.tasks.status.inprogress')}
                          break;
                        case 'WaitingOn':
                          this.projectStatuses[i] = {key: 'WaitingOn', value: this.$t('exo.tasks.status.waitingon')}
                          break;
                        case 'Done':
                          this.projectStatuses[i] = {key: 'Done', value: this.$t('exo.tasks.status.done')}
                          break;
                        default:
                          this.projectStatuses[i] = {key: data[i].name, value: data[i].name}
                      }
                    }
                  });
        }
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
      closeDateRangeMenu() {
        this.rangeDateMenu = false
      },
      openDateRangeMenu() {
        this.closePrioritiesList();
        this.closeStatusList();
        this.assigneeMenu = false;
        this.labelsList = false;
        this.projectsList = false;
      },
      openDatePickerMenu() {
        this.closePrioritiesList();
        this.closeStatusList();
        this.rangeDateMenu = false;
        this.assigneeMenu = false;
        this.labelsList = false;
        this.projectsList = false;
      },
      openAssigneeMenu() {
        this.closePrioritiesList();
        this.closeStatusList();
        this.datePickerMenu = false;
        this.rangeDateMenu = false;
        this.labelsList = false;
        this.projectsList = false;
      },
      openLabelsList() {
        this.labelsList = true;
        this.closePrioritiesList();
        this.closeStatusList();
        this.datePickerMenu = false;
        this.rangeDateMenu = false;
        this.assigneeMenu = false;
        this.projectsList = false;
      },
      openProjectsList() {
        this.projectsList = true;
        this.closePrioritiesList();
        this.closeStatusList();
        this.datePickerMenu = false;
        this.rangeDateMenu = false;
        this.assigneeMenu = false;
        this.labelsList = false;
      },
      escapeKeyListener: function(evt) {
        if (evt.keyCode === 27) {
          this.drawer = false;
          $('body').removeClass('hide-scroll');
          this.$emit('closeDrawer', this.drawer);
        }
      }
    }
  }
</script>