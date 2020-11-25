<template>
  <exo-drawer
    id="task-Drawer"
    ref="addTaskDrawer"
    class="taskDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right
    @closed="onCloseDrawer">
    <template v-if="task.id!=null" slot="title">
      {{ $t('label.drawer.header') }}
    </template>
    <template v-else slot="title">
      {{ $t('label.drawer.header.add') }}
    </template>
    <template slot="content">
      <div class="taskTitleAndMark d-flex">
        <v-btn
          id="check_btn"
          class="ml-n2"
          icon
          dark
          @click="task.completed =!task.completed">
          <v-icon class="markAsCompletedBtn">mdi-checkbox-marked-circle</v-icon>
        </v-btn>
        <input
          ref="autoFocusInput1"
          v-model="task.title"
          :class="{taskCompleted: task.completed}"
          :placeholder="$t('label.tapTask.name')"
          type="text"
          class="pl-0 pt-0 task-name"
          required
          autofocus
          @change="resetCustomValidity">
      </div>
      <div class="taskProjectName">
        <task-projects
          :task="task"
          @projectsListOpened="closePriority(); closeStatus(); closeLabelsList(); closeDueDateCalendar();closePlanDatesCalendar();closeAssignements()"/>
      </div>
      <div class="taskLabelsName">
        <task-labels
          :task="task"
          @labelsListOpened="closePriority(); closeStatus(); closeProjectsList();closeDueDateCalendar();closePlanDatesCalendar();closeAssignements()"/>
      </div>
      <div class="taskAssignement pb-3">
        <task-assignment
          :task="task"
          @updateTaskAssignement="updateTaskAssignee($event)"
          @updateTaskCoworker="updateTaskCoworker($event)"
          @assignmentsOpened="closePriority(); closeStatus(); closeProjectsList();closeDueDateCalendar();closePlanDatesCalendar(); closeLabelsList()"/>
      </div>
      <v-divider class="my-0" />
      <div class="d-flex  pt-4 pb-2">
        <div class="taskDates">
          <task-due-date
            :task="task"
            @dueDateOpened="closeLabelsList(); closeProjectsList(); closeStatus(); closePriority();closePlanDatesCalendar();closeAssignements()"/>
          <task-plan-dates
            @planDatesOpened="closePriority(); closeStatus(); closeProjectsList(); closeLabelsList(); closeDueDateCalendar();closeAssignements()"/>
        </div>
        <div class="taskStatusAndPriority">
          <task-priority
            :task="task"
            @updateTaskPriority="task.priority = $event"
            @PriorityListOpened="closeStatus(); closeProjectsList(); closeLabelsList();closeDueDateCalendar(); closePlanDatesCalendar();closeAssignements()"/>
          <task-status
            :task="task"
            @statusListOpened="closePriority(); closeProjectsList();closeLabelsList();closeDueDateCalendar();closePlanDatesCalendar();closeAssignements()"
            @updateTaskStatus="task.status = $event"/>
        </div>
      </div>
      <v-divider class="my-0" />
      <div class="taskDescription py-4">
        <exo-task-editor
          ref="richEditor"
          v-model="task.description"
          :max-length="MESSAGE_MAX_LENGTH"
          :id="task.id"
          :placeholder="$t('editinline.taskDescription.empty')"/>
      </div>
      <v-container v-show="dateRangeAlerte" py-0>
        <v-flex xs12>
          <v-alert type="warning" class="mb-0 pa-2 dueDateAlert">
            {{ $t('message.dueDateBeforeEndDate') }}
          </v-alert>
        </v-flex>
      </v-container>
      <v-flex
        v-if="task.id!=null"
        xs12
        class="pt-2">
        <v-tabs color="#578DC9">
          <v-tab class="text-capitalize">{{ $t('label.comments') }}</v-tab>
          <v-tab class="text-capitalize">{{ $t('label.changes') }}</v-tab>
          <v-tab-item class="pt-5">
            <v-list>
              <v-list-item
                v-for="(item, i) in comments"
                :key="i"
                class="pr-0 pl-0">
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
                    ref="commentEditor"
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
    </template>
    <template v-if="!task.id" slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn mr-2"
          @click="cancel">
          {{ $t('popup.cancel') }}
        </v-btn>
        <v-btn
          :disabled="disableSaveButton"
          class="btn btn-primary"
          @click="addTask">
          {{ $t('label.save') }}
        </v-btn>
      </div>
    </template>
    <template v-else slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn mr-2"
          @click="cancel">
          {{ $t('popup.cancel') }}
        </v-btn>
        <v-btn
          :disabled="disableSaveButton"
          class="btn btn-primary"
          @click="updateTask">
          {{ $t('label.edit') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
  import {updateTask, addTask, addTaskToLabel, getTaskLogs, getTaskComments, addTaskComments, urlVerify} from '../taskDrawerApi';
  export default {
    props: {
      task: {
        type: Object,
        default: () => {
          return {};
        }
      },
    },
    data() {
      return {
        enableAutosave: true,
        editorData: null,
        reset: false,
        disabledComment: true,
        dates: [],
        showEditor: true,
        showSubEditor: false,
        commentPlaceholder: this.$t('comment.message.addYourComment'),
        descriptionPlaceholder: this.$t('editinline.taskDescription.empty'),
        chips: [],
        autoSaveDelay: 1000,
        saveDescription: '',
        logs: [],
        comments: [],
        subEditorIsOpen: false,
        taskPriority: 'NORMAL',
        labelsToAdd: [],
        assignee: '',
        taskCoworkers: [],
        saving: false
      }
    },
    computed: {
      taskLink() {
        if (this.task == null || this.task.id == null) {
          return ""
        }
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/tasks/taskDetail/${this.task.id}`;
      },
      currentUserAvatar() {
        return `/portal/rest/v1/social/users/${eXo.env.portal.userName}/avatar`;
      },
      dateRangeAlerte() {
        return this.dates[1] > this.date
      },
      taskTitle() {
        return this.task && this.task.title;
      },
      taskTitleValid() {
        return this.taskTitle && this.taskTitle.length >= 5 && this.taskTitle.length < 1024;
      },
      disableSaveButton() {
        return this.saving || !this.taskTitleValid;
      },
    },
    watch: {
      'task.description': function (newValue, oldValue) {
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
    },
    created() {
      this.$root.$on('open-task-drawer', task => {
        window.setTimeout(() => {
            document.dispatchEvent(new CustomEvent('loadTaskPriority', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadProjectStatus', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadDueDate', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadProjectName', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadPlanDates', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadTaskLabels', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadAssignee', {detail: task}));
          },
          200)
      });

      document.addEventListener('labelListChanged', event => {
        if (event && event.detail) {
          const label = event.detail;
          this.labelsToAdd.push(label);
        }
      });
      document.addEventListener('taskAssigneeChanged', event => {
        if (event && event.detail) {
          this.assignee = event.detail;
        }
      });
      document.addEventListener('taskCoworkerChanged', event => {
        if (event && event.detail) {
          this.taskCoworkers = event.detail;
        }
      });
    },
    destroyed: function () {
      document.removeEventListener('keyup', this.escapeKeyListener);
    },
    methods: {
      closePriority() {
        document.dispatchEvent(new CustomEvent('closePriority'));
      },
      closeStatus() {
        document.dispatchEvent(new CustomEvent('closeStatus'));
      },
      closeProjectsList() {
        document.dispatchEvent(new CustomEvent('closeProjectList'));
      },
      closeLabelsList() {
        document.dispatchEvent(new CustomEvent('closeLabelsList'));
      },
      closeDueDateCalendar() {
        document.dispatchEvent(new CustomEvent('closeDueDate'));
      },
      closePlanDatesCalendar() {
        document.dispatchEvent(new CustomEvent('closePlanDates'));
      },
      closeAssignements() {
        document.dispatchEvent(new CustomEvent('closeAssignments'));
      },
      openEditor() {
        this.showEditor = true;
        this.subEditorIsOpen = true;
        this.editorData = null;
      },
      OnUpdateEditorStatus: function (val) {
        this.showEditor = !val;
        if (val === false) {
          this.subEditorIsOpen = false;
        }
      },
      OnCloseAllEditor() {
        this.subEditorIsOpen = true;
      },
      addTaskComment() {
        let comment = this.$refs.commentEditor.getMessage();
        comment = this.urlVerify(comment);
        addTaskComments(this.task.id,comment).then(comment => {
          this.comments.push(comment);
          this.reset = !this.reset;
        });
      },
      getUserAvatar(username) {
        return `/rest/v1/social/users/${username}/avatar`;
      },

      updateTask() {
        if(this.task.id!=null){
          this.resetCustomValidity();
          updateTask(this.task.id,this.task);
          window.setTimeout(() => {
             this.$root.$emit('task-added', this.task)
          }, 200);
        }
        this.$refs.addTaskDrawer.close();
      },
      addTask() {
        this.resetCustomValidity();
        this.task.coworker = this.taskCoworkers;
        this.task.assignee = this.assignee;
        addTask(this.task).then(task => {
          this.labelsToAdd.forEach(item => {
            addTaskToLabel(task.id, item);
          });
          this.$emit('addTask', this.task);
          this.$root.$emit('task-added', this.task);
          this.showEditor=false;
          this.enableAutosave=false
          this.$refs.addTaskDrawer.close();
        });
      },
      updateTaskAssignee(value) {
        if (this.task.id !== null) {
          if(value) {
            this.task.assignee = value;
          } else {
            this.task.assignee = ''
          }
        } else {
          if(value) {
            this.assignee = value;
          } else {
            this.assignee = ''
          }
        }
      },
      updateTaskCoworker(value) {
        if( this.task.id !== null) {
          if (value && value.length) {
            this.task.coworker = value
          } else {
            this.task.coworker = []
          }
        } else {
          if (value && value.length) {
            this.taskCoworkers = value
          } else {
            this.taskCoworkers = []
          }
        }
      },
      autoSaveDescription() {
        if(this.task.id!=null && this.enableAutosave){
          clearTimeout(this.saveDescription);
          this.saveDescription = setTimeout(() => {
            Vue.nextTick(() => this.updateTask(this.task.id));
          }, this.autoSaveDelay);
        }
        this.enableAutosave=true
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
      urlVerify(text) {
        return urlVerify(text);
      },
      open(task) {
        this.enableAutosave=false;
        this.task=task
        if(this.task.id!=null){
        this.retrieveTaskLogs();
        this.getTaskComments();
      }
        this.$refs.addTaskDrawer.open();
      },
      cancel() {
        this.$emit('updateTaskList');
        this.showEditor=false;
        this.$refs.addTaskDrawer.close();
      },
      onCloseDrawer() {
        this.enableAutosave=false;
        this.$root.$emit('task-drawer-closed', this.task)
        this.task={}
      },
      resetCustomValidity() {
        if (this.$refs.autoFocusInput1) {
          this.$refs.autoFocusInput1.setCustomValidity('');
        }
      },
    }
  }
</script>