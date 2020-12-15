<template>
  <v-app
    id="tasks"
    class="VuetifyApp"
    flat>
    <v-container pa-0>
      <v-layout
        row
        mx-0
        class="white">
        <v-flex
          d-flex
          xs12
          px-3>
          <v-layout
            row
            mx-0
            align-center>
            <v-flex
              d-flex
              xs12>
              <v-flex class="d-flex mx-3 my-2">
                <div class="d-flex align-center">
                  <a class="body-1 text-uppercase color-title px-0" @click="navigateTo('tasks/myTasks')">
                    {{ $t('label.tasks.header') }}
                  </a>
                </div>
                <v-spacer />
                <v-btn
                  :title="$t('label.addTask')"
                  icon
                  text
                  @click="openTaskDrawer">
                  <v-icon>mdi-plus</v-icon>
                </v-btn>
              </v-flex>
            </v-flex>
          </v-layout>
        </v-flex>
        <v-flex
          :class="tasks.length > 0 && 'pl-2'"
          d-flex
          xs12>
          <v-layout
            row
            mx-0>
            <v-flex
              xs12
              class="pt-2">
              <div v-if="tasks.length>0">
                <div>
                  <div class="nameGroup">
                    <span class="nameGroup">{{ $t('label.overdue') }}</span>
                    <div class="amount-item">{{ tasksOverdue.length }}</div>
                    <hr
                      role="separator"
                      aria-orientation="horizontal"
                      class="v-divider theme&#45;&#45;light separator">
                  </div>
                  <div
                    v-for="task in tasksOverdue"
                    :key="task.id"
                    class="list-item">
                    <task-details :task="task" @removeTask="removeTask"/>
                  </div>
                </div>
                <div>
                  <div class="nameGroup">
                    <span class="nameGroup">{{ $t('label.today') }}</span>
                    <div class="amount-item">{{ tasksToday.length }}</div>
                    <hr
                      role="separator"
                      aria-orientation="horizontal"
                      class="v-divider theme&#45;&#45;light separator">
                  </div>

                  <div
                    v-for="task in tasksToday"
                    :key="task.id"
                    class="list-item">
                    <task-details :task="task" @removeTask="removeTask"/>
                  </div>
                </div>

                <div>
                  <div class="nameGroup">
                    <span class="nameGroup">{{ $t('label.tomorrow') }}</span>
                    <div class="amount-item">{{ tasksTomorrow.length }}</div>
                    <hr
                      role="separator"
                      aria-orientation="horizontal"
                      class="v-divider theme&#45;&#45;light separator">
                  </div>

                  <div
                    v-for="task in tasksTomorrow"
                    :key="task.id"
                    class="list-item">
                    <task-details :task="task" @removeTask="removeTask"/>
                  </div>
                </div>

                <div>
                  <div class="nameGroup">
                    <span class="nameGroup">{{ $t('label.upcoming') }}</span>
                    <div class="amount-item">{{ tasksUpcoming.length }}</div>
                    <hr
                      role="separator"
                      aria-orientation="horizontal"
                      class="v-divider theme&#45;&#45;light separator">
                  </div>
                </div>
              </div>

              <div v-if="!loadingTasks && tasks.length===0" class="noTasks">
                <div class="noTasksContent">
                  <i class="uiNoTaskIcon"></i>
                  <div class="noTasksTitle">{{ $t('label.noTask') }}</div>
                </div>
              </div>
        </v-flex></v-layout></v-flex>
      </v-layout>
      <task-drawer
        ref="taskDrawer"
        :task="task"/>
    </v-container>
  </v-app>
</template>

<script>
  import {filterTasksList} from '../../../js/tasksService'

  export default {

    data() {
      return {
        placeholder: '',
        tasks: [],
        tasksOverdue: [],
        tasksToday: [],
        tasksTomorrow: [],
        tasksUpcoming: [],
        loadingTasks: true,
        TasksWithoutUpcomingSize:'',
        TasksSize:'',
        task: {
          id:null,
          status:{project:this.project},
          priority:'NONE',
          description:'',
          title:''
        },
      }
    },
    created(){
      this.$root.$on('open-task-drawer', task => {
        this.$refs.taskDrawer.open(task);
      });
      this.itemsLimit = this.$parent.$data.itemsLimit;
      this.getMyOverDueTasks();
      this.getMyTodayTasks();
      this.getMyTomorrowTasks();
      this.getMyUpcomingTasks();
    },
    methods: {
      getMyOverDueTasks() {
        const task = {
          dueCategory: 'overDue',
          offset: 0,
          limit: 2,
          showCompleteTasks:false,
        };
        filterTasksList(task,'','priority','','-2').then(
                (data) => {
                  this.tasksOverdue = data.tasks;
                  this.tasks = this.tasks.concat(this.tasksOverdue);
                  this.loadingTasks = false
                }
        )
      },
      getMyTodayTasks() {
        const task = {
          dueCategory: 'today',
          offset: 0,
          limit: 6,
          showCompleteTasks:false,
        };
        filterTasksList(task,'','priority','','-2').then(
                (data) => {
                  this.tasksToday = data.tasks;
                  this.tasks = this.tasks.concat(this.tasksToday);
                  this.loadingTasks = false
                }
        )
      },getMyTomorrowTasks() {
        const task = {
          dueCategory: 'tomorrow',
          offset: 0,
          limit: 2,
          showCompleteTasks:false,
        };
        filterTasksList(task,'','priority','','-2').then(
                (data) => {
                  this.tasksTomorrow = data.tasks;
                  this.tasks = this.tasks.concat(this.tasksTomorrow);
                  this.loadingTasks = false
                }
        )
      },getMyUpcomingTasks() {
        const task = {
          dueCategory: 'upcoming',
          offset: 0,
          showCompleteTasks:false,
        };
        filterTasksList(task,'','priority','','-2').then(
                (data) => {
                  this.tasksUpcoming = data.tasks;
                  this.tasks = this.tasks.concat(this.tasksUpcoming);
                  this.loadingTasks = false
                }
        )
      },
      openTaskDrawer() {
        const defaultTask={
          id:null,
          status:{project:this.project},
          priority:'NONE',
          description:'',
          title:''
        }
        this.$root.$emit('open-task-drawer', defaultTask);
      },
      removeTask(value) {
        this.tasks.splice(this.tasks.findIndex(function(i){
          return i.id === value;
        }), 1);
        document.body.style.overflow = 'auto';
      },
      navigateTo(pagelink) {
        location.href=`${ eXo.env.portal.context }/${ eXo.env.portal.portalName }/${ pagelink }` ;
      },
    }
  }
</script>
