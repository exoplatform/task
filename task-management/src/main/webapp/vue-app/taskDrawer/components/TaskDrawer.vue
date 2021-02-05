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
            @change="updateTaskTitle()"/>
        </div>
        <div
          v-if="task && task.id"
          :title="$t('tooltip.viewAllChanges')"
          class="lastUpdatedTask pb-3"
          @click="$root.$emit('displayTaskChanges')">
          <span class="pr-2">{{ $t('label.task.lastUpdate') }}
            {{ displayedDate(lastTaskChangesLog) }}
            {{ $t('label.task.lastUpdateBy') }}
            {{ lastTaskChangesLogAuthor }}</span>
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
            :task="task"
            v-model="task.description"
            :value="task.description"
            :placeholder="$t('editinline.taskDescription.empty')"
            @addTaskDescription="addTaskDescription($event)"/>
        </div>
        <div class="taskLabelsName mt-3 mb-3">
          <task-labels
            :task="task"
            @labelsListOpened="closePriority(); closeStatus(); closeProjectsList();closeTaskDates();closeAssignements()"/>
        </div>
        <v-divider class="my-0" />
        <v-flex
          v-if="task.id!=null"
          xs12
          class="pt-2 taskCommentsAndChanges">
          <div class="taskComments">
            <div class="commentActionsContent">
              <div v-if="comments && comments.length" class="taskCommentNumber pb-3 d-flex ">
                <span class="ViewAllCommentLabel" @click="$root.$emit('displayTaskComment')">{{ $t('comment.message.viewAllComment') }} ({{ comments.length }})</span>
                <div
                  :title="$t('comment.message.addYourComment')"
                  class="addCommentBtn pl-3"
                  @click="$root.$emit('displayTaskComment')">
                  <i class="uiIcon uiIconComment"></i>
                </div>
              </div>
              <div v-else class="taskCommentEmpty align-center pt-6 pb-3 pr-3">
                <i class="uiIcon uiIconComment"></i>
                <span class="noCommentLabel">{{ $t('comment.message.noComment') }}</span>
                <span class="ViewAllCommentText" @click="$root.$emit('displayTaskComment')">{{ $t('comment.message.addYourComment') }}</span>
              </div>
            </div>
            <div v-if="comments && comments.length" class="pr-0 pl-0 TaskCommentItem">
              <task-last-comment
                :task="task"
                :comment="comments[comments.length-1]" />
            </div>
          </div>
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
    <task-comments-drawer
      ref="taskCommentDrawer"
      :task="task"
      :comments="comments"/>
    <task-changes-drawer
      ref="taskChangesDrawer"
      :task="task"
      :logs="logs"/>
  </div>

</template>
<script>
 import {
    updateTask,
    addTask,
    addTaskToLabel,
    getTaskLogs,
    getTaskComments,
    cloneTask
} from '../taskDrawerApi';
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
            reset: false,
            dates: [],
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
            isManager: false,
            isParticipator: false,
            datePickerTop: true,
            currentUserName: eXo.env.portal.userName,
            MESSAGE_MAX_LENGTH: 1250,
            dateTimeFormat: {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            },
          lang: eXo.env.portal.language
        }
    },
    computed: {
      taskLink() {
        if (this.task == null || this.task.id == null) {
          return ""
        }
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/tasks/taskDetail/${this.task.id}`;
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
      lastTaskChangesLog() {
        return this.logs && this.logs.length && this.logs[0].createdTime || '';
      },
      lastTaskChangesLogAuthor() {
        return this.logs && this.logs.length && this.logs[0].authorFullName || '';
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
                if (event.detail === 'projectView') {
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
    destroyed: function() {
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
        updateTaskTitle() {
            if (this.task.id != null) {
                updateTask(this.task.id, this.task).then(task => {
                        this.$root.$emit('show-alert', {
                            type: 'success',
                            message: this.$t('alert.success.task.title')
                        });
                    })
                    .catch(e => {
                        console.debug("Error when updating task's title", e);
                        this.$root.$emit('show-alert', {
                            type: 'error',
                            message: this.$t('alert.error')
                        });
                    });
            }
        },
        updateTaskPriority(value) {
            if (value) {
                if (this.task.id != null) {
                    this.task.priority = value;
                    updateTask(this.task.id, this.task).then(task => {
                        this.$root.$emit('show-alert', {
                            type: 'success',
                            message: this.$t('alert.success.task.priority')
                        });
                    }).catch(e => {
                        console.debug("Error when updating task's priority", e);
                        this.$root.$emit('show-alert', {
                            type: 'error',
                            message: this.$t('alert.error')
                        });
                    });
                } else {
                    this.taskPriority = value;
                }
            }
        },
        updateTaskStatus(value) {
            if (value) {
                if (this.task.id != null) {
                    this.task.status = value;
                    updateTask(this.task.id, this.task).then(task => {
                        this.$root.$emit('show-alert', {
                            type: 'success',
                            message: this.$t('alert.success.task.status')
                        });
                    }).catch(e => {
                        console.debug("Error when updating task's status", e);
                        this.$root.$emit('show-alert', {
                            type: 'error',
                            message: this.$t('alert.error')
                        });
                    });
                }
            }
        },
        updateTaskStartDate(value) {
            if (value) {
                if (this.task.id != null) {
                    this.task.startDate = value;
                    updateTask(this.task.id, this.task).then(task => {
                        this.$root.$emit('show-alert', {
                            type: 'success',
                            message: this.$t('alert.success.task.startDate')
                        });
                    }).catch(e => {
                        console.debug("Error when updating task's start date", e);
                        this.$root.$emit('show-alert', {
                            type: 'error',
                            message: this.$t('alert.error')
                        });
                    });
                } else {
                    this.taskStartDate = value;
                }
            }
        },
        updateTaskDueDate(value) {
            if (value && value !== 'none') {
                if (this.task.id != null) {
                    this.task.dueDate = value;
                    updateTask(this.task.id, this.task).then(task => {
                        this.$root.$emit('show-alert', {
                            type: 'success',
                            message: this.$t('alert.success.task.duetDate')
                        });
                    }).catch(e => {
                        console.debug("Error when updating task's due date", e);
                        this.$root.$emit('show-alert', {
                            type: 'error',
                            message: this.$t('alert.error')
                        });
                    });
                } else {
                    this.taskDueDate = value;
                }
            } else if (value === 'none') {
                this.task.dueDate = null;
                updateTask(this.task.id, this.task).then(task => {
                    this.$root.$emit('show-alert', {
                        type: 'success',
                        message: this.$t('alert.success.task.duetDate')
                    });
                }).catch(e => {
                    console.debug("Error when updating task's due date", e);
                    this.$root.$emit('show-alert', {
                        type: 'error',
                        message: this.$t('alert.error')
                    });
                });
            }
        },
        updateTask() {
            if (this.task.id != null) {
                updateTask(this.task.id, this.task).then(task => {
                        this.$root.$emit('update-task-list', this.task);
                        this.$root.$emit('show-alert', {
                            type: 'success',
                            message: this.$t('alert.success.task.updated')
                        });
                    })
                    .catch(e => {
                        console.debug("Error when updating task", e);
                        this.$root.$emit('show-alert', {
                            type: 'error',
                            message: this.$t('alert.error')
                        });
                    });
            }
        },
        addTask() {
            document.dispatchEvent(new CustomEvent('onAddTask'));
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
                this.$root.$emit('update-task-list', this.task);
                this.$root.$emit('show-alert', {
                    type: 'success',
                    message: this.$t('alert.success.task.created')
                });
                this.showEditor = false;
                this.$refs.addTaskDrawer.close();
                this.labelsToAdd = [];
            }).catch(e => {
                console.debug("Error when adding task title", e);
                this.$root.$emit('show-alert', {
                    type: 'error',
                    message: this.$t('alert.error')
                });
            });
        },
        updateTaskAssignee(value) {
            if (this.task.id !== null) {
                if (value) {
                    this.task.assignee = value;
                } else {
                    this.task.assignee = null
                }
                updateTask(this.task.id, this.task).then(task => {
                    this.$root.$emit('show-alert', {
                        type: 'success',
                        message: this.$t('alert.success.task.assignee')
                    });
                }).catch(e => {
                    console.debug("Error when updating task's assignee", e);
                    this.$root.$emit('show-alert', {
                        type: 'error',
                        message: this.$t('alert.error')
                    });
                });
            } else {
                if (value) {
                    this.assignee = value;
                } else {
                    this.assignee = null
                }
            }
        },
        updateTaskCoworker(value) {
            if (this.task.id !== null) {
                if (value && value.length) {
                    this.task.coworker = value
                } else {
                    this.task.coworker = []
                }
                updateTask(this.task.id, this.task).then(task => {
                    this.$root.$emit('show-alert', {
                        type: 'success',
                        message: this.$t('alert.success.task.coworker')
                    });
                }).catch(e => {
                    console.debug("Error when updating task's coworkers", e);
                    this.$root.$emit('show-alert', {
                        type: 'error',
                        message: this.$t('alert.error')
                    });
                });
            } else {
                if (value && value.length) {
                    this.taskCoworkers = value
                } else {
                    this.taskCoworkers = []
                }
            }
        },
        addTaskDescription(value) {
            this.task.description = value;
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
        open(task) {
            this.task = task
            window.setTimeout(() => {
                    document.dispatchEvent(new CustomEvent('loadTaskPriority', {
                        detail: task
                    }));
                    document.dispatchEvent(new CustomEvent('loadProjectStatus', {
                        detail: task
                    }));
                    document.dispatchEvent(new CustomEvent('loadProjectName', {
                        detail: task
                    }));
                    document.dispatchEvent(new CustomEvent('loadPlanDates', {
                        detail: task
                    }));
                    document.dispatchEvent(new CustomEvent('loadTaskLabels', {
                        detail: task
                    }));
                    document.dispatchEvent(new CustomEvent('loadAssignee', {
                        detail: task
                    }));
                },
                200);
            if (task.id != null) {
                this.retrieveTaskLogs();
                this.getTaskComments();
                this.$root.$emit('set-url', {
                    type: "task",
                    id: task.id
                })
            }
            this.$refs.addTaskDrawer.open();
        },
        cancel() {
            this.$emit('updateTaskList');
            this.showEditor = false;
            this.$refs.addTaskDrawer.close();
        },
        onCloseDrawer() {
            this.$root.$emit('task-drawer-closed', this.task)
            this.showEditor = false;
            this.task = {};
            document.dispatchEvent(new CustomEvent('drawerClosed'));
            document.dispatchEvent(new CustomEvent('loadTaskLabels', {
                detail: {}
            }));
            this.$root.$emit('hideTaskComment');
            this.$root.$emit('hideTaskChanges');
        },
        deleteTask() {
            this.deleteConfirmMessage = `${this.$t('popup.msg.deleteTask')} : <strong>${this.task.title}</strong>? `;
            this.$refs.deleteConfirmDialog.open();
        },
        cloneTask() {
            cloneTask(this.task.id).then(task => {
                this.$root.$emit('show-alert', {
                    type: 'success',
                    message: this.$t('alert.success.task.cloned') 
                });
                this.$root.$emit('update-task-list', this.task);
                this.$root.$emit('open-task-drawer', task);
            }).catch(e => {
                console.debug("Error when cloning task", e);
                this.$root.$emit('show-alert', {
                    type: 'error',
                    message: this.$t('alert.error')
                });
            });
        },
        deleteConfirm() {
            const idTask = this.task.id;
            return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/tasks/${this.task.id}`, {
                method: 'DELETE',
                credentials: 'include',
            }).then(resp => {
                this.$root.$emit('update-task-list', this.task);
                this.$root.$emit('show-alert', {
                    type: 'success',
                    message: this.$t('alert.success.task.deleted') 
                });
                document.dispatchEvent(new CustomEvent('deleteTask', {
                    detail: idTask
                }));
            }).catch(e => {
                console.debug("Error when deleting task", e);
                this.$root.$emit('show-alert', {
                    type: 'error',
                    message: this.$t('alert.error')
                });
            });
        },
        addMenuAction(title, uiIcon, enabled, actionFunctionName) {
            this.menuActions.push({
                title: title,
                uiIcon: uiIcon,
                enabled: enabled,
                action: this[actionFunctionName]
            });
        },
      displayedDate(value) {
        return value && this.$dateUtil.formatDateObjectToDisplay(new Date(value), this.dateTimeFormat, this.lang) || '';
      },
    }
}
</script>
