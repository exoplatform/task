<template>
  <v-app
    id="tasks"
    class="VuetifyApp"
    flat>
    <v-container pa-0>
      <v-layout
        row
        mx-3
        class="white">
        <v-flex
          d-flex
          xs12>
          <v-layout
            row
            mx-0
            align-center>
            <v-flex
              d-flex
              xs12>
              <v-flex class="d-flex my-2">
                <div class="d-flex align-center">
                  <a
                    class="body-1 text-uppercase color-title px-0"
                    @click="navigateTo('tasks/myTasks')">
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
          :class="tasks.length > 0"
          d-flex
          xs12>
          <v-layout
            row
            mx-0>
            <v-flex
              xs12>
              <div v-if="tasks.length>0">
                <div>
                  <div class="nameGroup">
                    <span class="nameGroup">{{ $t('label.overdue') }}</span>
                    <div class="amount-item pointer" @click="navigateTo('tasks/myTasks')">{{ tasksOverdueSize }}</div>
                    <hr
                      role="separator"
                      aria-orientation="horizontal"
                      class="v-divider theme&#45;&#45;light separator">
                  </div>
                  <div
                    v-for="task in tasksOverdueList"
                    :key="task.id"
                    class="list-item">
                    <task-details :task="task" @removeTask="removeTask"/>
                  </div>
                </div>
                <div>
                  <div class="nameGroup">
                    <span class="nameGroup">{{ $t('label.today') }}</span>
                    <div class="amount-item pointer" @click="navigateTo('tasks/myTasks')">{{ tasksTodaySize }}</div>
                    <hr
                      role="separator"
                      aria-orientation="horizontal"
                      class="v-divider theme&#45;&#45;light separator">
                  </div>

                  <div
                    v-for="task in tasksTodayList"
                    :key="task.id"
                    class="list-item">
                    <task-details :task="task" @removeTask="removeTask"/>
                  </div>
                </div>

                <div>
                  <div class="nameGroup">
                    <span class="nameGroup">{{ $t('label.tomorrow') }}</span>
                    <div class="amount-item pointer" @click="navigateTo('tasks/myTasks')">{{ tasksTomorrowSize }}</div>
                    <hr
                      role="separator"
                      aria-orientation="horizontal"
                      class="v-divider theme&#45;&#45;light separator">
                  </div>

                  <div
                    v-for="task in tasksTomorrowList"
                    :key="task.id"
                    class="list-item">
                    <task-details :task="task" @removeTask="removeTask"/>
                  </div>
                </div>

                <div>
                  <div class="nameGroup">
                    <span class="nameGroup">{{ $t('label.upcoming') }}</span>
                    <div class="amount-item pointer" @click="navigateTo('tasks/myTasks')">{{ tasksUpcomingSize }}</div>
                    <hr
                      role="separator"
                      aria-orientation="horizontal"
                      class="v-divider theme&#45;&#45;light separator">
                  </div>

                  <div
                    v-for="task in tasksUpcomingList"
                    :key="task.id"
                    class="list-item">
                    <task-details :task="task" @removeTask="removeTask"/>
                  </div>
                </div>

                <div class="text-center">
                  <button
                    type="button"
                    class="btn color-title btn-show"
                    @click="navigateTo('tasks/myTasks')">{{ $t('label.tasks.btn.show') }}</button>
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
        tasksOverdueSize:'',
        tasksTodaySize:'',
        tasksTomorrowSize:'',
        tasksUpcomingSize:'',
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
        priorityStatus :['High', 'In Normal', 'Low', 'None', null],
      }
    },computed:{
    tasksOverdueList(){
      if(this.tasksOverdue){
        if(this.tasksOverdue.length>10){
          return  this.tasksOverdue.slice(0, 10);
        }else {
          return this.tasksOverdue;
        }
      }
      else {return this.tasksOverdue;}
    },
    tasksTodayList(){
      if(this.tasksToday){
        if(this.tasksToday.length>1){
          if(this.tasksOverdueList.length<10){
            return  this.tasksToday.slice(0, 10-this.tasksOverdueList.length);
          }
        }
      }else {return this.tasksToday;}

    },
    tasksTomorrowList(){
      if(this.tasksTomorrow){
        if(this.tasksTomorrow.length>1){
          return  this.tasksTomorrow.slice(0, 10-this.tasksOverdueList.length-this.tasksTodayList.length);
        }
      }else {return this.tasksTomorrow;}
    },
    tasksUpcomingList(){
      if(this.tasksUpcoming){
        if(this.tasksUpcoming.length>1){
          return  this.tasksUpcoming.slice(0, 10-this.tasksOverdueList.length-this.tasksTodayList.length-this.tasksTomorrowList.length);
        }
      }else {return this.tasksUpcoming;}
    },
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
          limit: 0,
          showCompleteTasks:false,
        };
        filterTasksList(task,'','priority','','-2').then(
                (data) => {
                  this.tasksOverdue = data.tasks;
                  this.tasksOverdue=this.tasksOverdue.sort((a, b) => {
                    return this.priorityStatus.indexOf(a.task.priority) - this.priorityStatus.indexOf(b.task.priority);
                  });
                  this.tasksOverdueSize=data.tasksNumber;
                  this.tasks = this.tasks.concat(this.tasksOverdue);
                  this.loadingTasks = false
                }
        )
      },
      getMyTodayTasks() {
        const task = {
          dueCategory: 'today',
          offset: 0,
          limit: 0,
          showCompleteTasks:false,
        };
        filterTasksList(task,'','priority','','-2').then(
                (data) => {
                  this.tasksToday = data.tasks;
                  this.tasksToday=this.tasksToday.sort((a, b) => {
                    return this.priorityStatus.indexOf(a.task.priority) - this.priorityStatus.indexOf(b.task.priority);
                  });
                  this.tasksTodaySize=data.tasksNumber;
                  this.tasks = this.tasks.concat(this.tasksToday);
                  this.loadingTasks = false
                }
        )
      },getMyTomorrowTasks() {
        const task = {
          dueCategory: 'tomorrow',
          offset: 0,
          limit: 0,
          showCompleteTasks:false,
        };
        filterTasksList(task,'','priority','','-2').then(
                (data) => {
                  this.tasksTomorrow = data.tasks;
                  this.tasksTomorrow=this.tasksTomorrow.sort((a, b) => {
                    return this.priorityStatus.indexOf(a.task.priority) - this.priorityStatus.indexOf(b.task.priority);
                  });
                  this.tasksTomorrowSize=data.tasksNumber;
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
                  this.tasksUpcoming=this.tasksUpcoming.sort((a, b) => {
                    return this.priorityStatus.indexOf(a.task.priority) - this.priorityStatus.indexOf(b.task.priority);
                  });
                  this.tasksUpcomingSize=data.tasksNumber;
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
