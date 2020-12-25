<template>
  <div>
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="deleteConfirmMessage"
      :title="$t('popup.deleteTask')"
      :ok-label="$t('label.ok')"
      :cancel-label="$t('popup.cancel')"
      @ok="deleteConfirm()" />
    <exo-drawer
      id="task-Drawer"
      ref="addTaskDrawer"
      class="taskDrawer"
      body-classes="hide-scroll decrease-z-index-more"
      right
      @closed="onCloseDrawer">
      <template 
        v-if="task.id!=null" 
        slot="title" 
        class="d-flex justify-space-between">
        <div class="drawerTitleAndProject d-flex">
          <span>{{ $t('label.drawer.header') }}</span>
          <div class="taskProjectName">
            <task-projects
              :task="task"
              @projectsListOpened="closePriority(); closeStatus(); closeLabelsList(); closeTaskDates();closeAssignements()"/>
          </div>
        </div>
        <div v-if="menuActions.length" id="taskActionMenu">
          <i class="uiIcon uiThreeDotsIcon" @click="displayActionMenu = true"></i>
          <v-menu
            v-model="displayActionMenu"
            :attach="'#taskActionMenu'"
            transition="slide-x-reverse-transition"
            content-class="taskActionMenu"
            offset-y>
            <v-list class="pa-0" dense>
              <v-list-item v-for="menuAction in menuActions" :key="menuAction.title">
                <v-list-item-title class="subtitle-2" @click="menuAction.action">
                  <i :class="`uiIcon ${menuAction.uiIcon} pr-2`"></i>
                  <span>{{ menuAction.title }}</span>
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </div>
      </template>
      <template v-else slot="title">
        <div class="drawerTitleAndProject d-flex">
          <span>{{ $t('label.drawer.header.add') }}</span>
          <div class="taskProjectName">
            <task-projects
              :task="task"
              @projectsListOpened="closePriority(); closeStatus(); closeLabelsList(); closeTaskDates();closeAssignements()"/>
          </div>
        </div>
      </template>
      <template slot="content">
        <div class="taskTitleAndMark d-flex">
          <v-btn
            id="check_btn"
            class="ml-n2"
            icon
            dark
            @click="task.completed =!task.completed">
            <v-icon v-if="task.completed" class="markAsCompletedBtn">mdi-checkbox-marked-circle</v-icon>
            <v-icon v-else class="markAsCompletedBtn">mdi-checkbox-blank-circle-outline</v-icon>
          </v-btn>
          <v-textarea
            ref="autoFocusInput4"
            v-model="task.title"
            :class="{taskCompleted: task.completed}"
            :placeholder="$t('label.tapTask.name')"
            type="text"
            class="pl-0 pt-0 task-name"
            auto-grow
            rows="1"
            row-height="13"
            required
            autofocus
            @change="updateTaskTitle()"/>
        </div>
        <div class="taskAssignement ml-8 pb-3">
          <task-assignment
            :task="task"
            @updateTaskAssignement="updateTaskAssignee($event)"
            @updateTaskCoworker="updateTaskCoworker($event)"
            @assignmentsOpened="closePriority(); closeStatus(); closeProjectsList();closeTaskDates();closeLabelsList()"/>
        </div>
        <v-divider class="my-0" />
        <div class="d-flex  pt-4 pb-2">
          <div class="taskDates">
            <task-form-date-pickers
              :task="task"
              :date-picker-top="datePickerTop"
              @startDateChanged="updateTaskStartDate($event)"
              @dueDateChanged="updateTaskDueDate($event)"/>
          </div>
          <div class="taskStatusAndPriority">
            <task-priority
              :task="task"
              @updateTaskPriority="updateTaskPriority($event)"
              @PriorityListOpened="closeStatus(); closeProjectsList(); closeLabelsList();closeTaskDates();closeAssignements()"/>
            <task-status
              :task="task"
              @statusListOpened="closePriority(); closeProjectsList();closeLabelsList();closeTaskDates();closeAssignements()"
              @updateTaskStatus="updateTaskStatus($event)"/>
          </div>
        </div>
        <v-divider class="my-0" />
        <div class="taskDescription py-4">
          <task-description-editor
            v-model="task.description"
            :placeholder="$t('editinline.taskDescription.empty')"/>
        </div>
        <div class="taskLabelsName mt-3 mb-3">
          <task-labels
            :task="task"
            @labelsListOpened="closePriority(); closeStatus(); closeProjectsList();closeTaskDates();closeAssignements()"/>
        </div>
        <v-flex
          v-if="task.id!=null"
          xs12
          class="pt-2 taskCommentsAndChanges">
          <v-tabs color="#578DC9">
            <v-tab class="text-capitalize">{{ $t('label.comments') }}</v-tab>
            <v-tab class="text-capitalize">{{ $t('label.changes') }}</v-tab>
            <v-tab-item class="pt-5 taskComments">
              <div>
                <div
                  v-for="(item, i) in comments"
                  :key="i"
                  class="pr-0 pl-0 TaskCommentItem">
                  <task-comments
                    :task="task"
                    :comment="item"
                    :comments="comments"
                    :is-open="!showEditor"
                    :close-editor="subEditorIsOpen"
                    @isOpen="OnCloseAllEditor()"
                    @showSubEditor="OnUpdateEditorStatus"/>
                </div>
                <div v-if="showEditor" class="comment commentEditor d-flex align-start">
                  <exo-user-avatar
                    :username="currentUserName"
                    :avatar-url="currentUserAvatar"
                    :size="30"
                    :url="null"/>
                  <div class="editorContent ml-2">
                    <task-comment-editor
                      ref="commentEditor"
                      v-model="editorData"
                      :placeholder="commentPlaceholder"
                      :reset="reset"
                      class="comment"/>
                    <v-btn
                      :disabled="disabledComment"
                      depressed
                      small
                      dark
                      class="mt-1 mb-2 commentBtn"
                      @click="addTaskComment()">{{ $t('comment.label.comment') }}</v-btn>
                  </div>
                </div>
                <a
                  v-else
                  class="pl-4"
                  @click="openEditor">{{ $t('comment.label.comment') }}</a>
              </div>
            </v-tab-item>
            <v-tab-item class="pt-5 taskChanges">
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
    </exo-drawer>
  </div>

</template>
<script>
  import {updateTask, addTask, addTaskToLabel, getTaskLogs, getTaskComments, addTaskComments, urlVerify, cloneTask} from '../taskDrawerApi';
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
        displayActionMenu: false,
        menuActions: [],
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
        assignee: null,
        taskCoworkers: [],
        taskDueDate: null,
        taskStartDate: null,
        saving: false,
        deleteConfirmMessage: null,
        isManager :false,
        isParticipator :false,
        datePickerTop: true,
        currentUserName: eXo.env.portal.userName,
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
      taskTitle() {
        return this.task && this.task.title;
      },
      taskTitleValid() {
        return this.taskTitle && this.taskTitle.trim() && this.taskTitle.trim().length >= 3 && this.taskTitle.length < 1024;
      },
      disableSaveButton() {
        return this.saving || !this.taskTitleValid;
      },
    },
    watch: {
       'task.description': function (newValue, oldValue) {
        if (newValue !== this.task.description) {
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
      $(document).on('mousedown', () => {
        if (this.displayActionMenu) {
          window.setTimeout(() => {
            this.displayActionMenu = false;
          }, 200);
        }
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
      document.addEventListener('taskOrigin', event => {
        if (event && event.detail) {
          if(event.detail === 'projectView') {
            if (this.task.status.project.id) {
              this.$projectService.getProject(this.task.status.project.id, true).then(data => {
                this.isManager = data.managerIdentities.some(manager => manager.username === eXo.env.portal.userName);
                this.isParticipator = this.isManager || data.participatorIdentities.some(participator => participator.username === eXo.env.portal.userName);
                // add menu actions
                this.menuActions = [];
                this.addMenuAction(this.$t('label.delete'), 'uiIconTrash', this.isManager, 'deleteTask');
                this.addMenuAction(this.$t('label.clone'), 'uiIconCloneNode', this.isParticipator, 'cloneTask');
                this.menuActions = this.menuActions.filter(menuAction => menuAction.enabled);
              });
            }
          }
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
      closeTaskDates() {
        document.dispatchEvent(new CustomEvent('closeDates'));
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

      updateTaskTitle() {
        if(this.task.id!=null){
          updateTask(this.task.id,this.task);
        }
      },
      updateTaskPriority(value) {
        if(value) {
          if (this.task.id != null) {
            this.task.priority = value;
            updateTask(this.task.id, this.task);
          } else {
            this.taskPriority = value;
          }
        }
      },
      updateTaskStatus(value) {
        if(value) {
          if (this.task.id != null) {
            this.task.status = value;
            updateTask(this.task.id, this.task);
          }
        }
      },
      updateTaskStartDate(value) {
        if(value) {
          if(this.task.id!=null){
            this.task.startDate = value;
            updateTask(this.task.id,this.task);
          } else {
            this.taskStartDate = value;
          }
        }
      },
      updateTaskDueDate(value) {
        if(value) {
          if(this.task.id!=null){
            this.task.dueDate = value;
            updateTask(this.task.id,this.task);
          } else {
            this.taskDueDate = value;
          }
        }
      },
      updateTask() {
        if(this.task.id!=null){
          updateTask(this.task.id,this.task);
          window.setTimeout(() => {
             this.$root.$emit('task-added', this.task)
          }, 200);
        }
      },
      addTask() {
        this.task.coworker = this.taskCoworkers;
        this.task.assignee = this.assignee;
        this.task.startDate = this.taskStartDate;
        this.task.dueDate = this.taskDueDate;
        this.task.priority = this.taskPriority;
        addTask(this.task).then(task => {
          this.labelsToAdd.forEach(item => {
            addTaskToLabel(task.id, item);
          });
          this.$emit('addTask', this.task);
          this.$root.$emit('task-added', this.task);
          this.showEditor=false;
          //this.enableAutosave=false
          this.$refs.addTaskDrawer.close();
          this.labelsToAdd = [];
        });
      },
      updateTaskAssignee(value) {
        if (this.task.id !== null) {
          if(value) {
            this.task.assignee = value;
          } else {
            this.task.assignee = null
          }
          updateTask(this.task.id,this.task);
        } else {
          if(value) {
            this.assignee = value;
          } else {
            this.assignee = null
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
          updateTask(this.task.id,this.task);
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
            //Vue.nextTick(() => this.updateTask(this.task.id));
            updateTask(this.task.id,this.task);
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
        this.enableAutosave=true;
        this.task=task
        window.setTimeout(() => {
            document.dispatchEvent(new CustomEvent('loadTaskPriority', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadProjectStatus', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadProjectName', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadPlanDates', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadTaskLabels', {detail: task}));
            document.dispatchEvent(new CustomEvent('loadAssignee', {detail: task}));
          },
          200);
        if(task.id!=null){
        this.retrieveTaskLogs();
        this.getTaskComments();
        this.$root.$emit('set-url', {type:"task",id:task.id})
      }
        this.$refs.addTaskDrawer.open();
      },
      cancel() {
        this.$emit('updateTaskList');
        this.showEditor=false;
        this.$refs.addTaskDrawer.close();
      },
      onCloseDrawer() {
        this.$root.$emit('task-drawer-closed', this.task)
        this.enableAutosave=false;
        this.showEditor=false;
        this.$nextTick(() => {
          document.dispatchEvent(new CustomEvent('drawerClosed'));
        }).then(()=>{
          this.task={}
        })
      },
      deleteTask() {
        this.deleteConfirmMessage = `${this.$t('popup.msg.deleteTask')} : ${this.task.title}? `;
        this.$refs.deleteConfirmDialog.open();
      },
      cloneTask() {
        cloneTask(this.task.id).then(task => {
          this.$root.$emit('open-task-drawer', task);
        });
      },
      deleteConfirm() {
        return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/tasks/${this.task.id}`, {
          method: 'DELETE',
          credentials: 'include',
        }).then(resp => {
          if (!resp || !resp.ok) {
            throw new Error('error message');
          }
        })
      },
      addMenuAction(title, uiIcon, enabled, actionFunctionName) {
        this.menuActions.push({
          title: title,
          uiIcon: uiIcon,
          enabled: enabled,
          action: this[actionFunctionName]
        });
      },
    }
  }
</script>
