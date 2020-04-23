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
              <v-card
                flat
                color="transparent">
                <a @click="navigateTo('tasks/my-task')"><v-card-text class="body-1 text-uppercase color-title px-0">
                  {{ $t('label.tasks.header') }}
                </v-card-text></a>
              </v-card>
            </v-flex>
          </v-layout>
        </v-flex>
        <v-flex
          xs12
          px-3>
          <v-layout
            row
            mx-0
            align-center>
            <v-flex
              d-flex
              xs12>
              <v-layout mx-0>
                <v-flex 
                  v-if="tasks.length > 0" 
                  xs12 
                  style="cursor: pointer" 
                  @click="navigateTo('tasks')">
                  <v-card
                    class="mx-auto"
                    flat
                    color="#F7FAFD">
                    <v-layout>
                      <v-flex>
                        <v-layout
                          row
                          mx-0
                          justify-center>
                          <v-flex
                            xs12
                            d-flex>
                            <v-layout mx-0 pl-2>
                              <v-flex
                                d-flex
                                xs4>
                                <v-layout
                                  mx-0
                                  justify-center
                                  class="py-2">
                                  <template>
                                    <div class="text-center">
                                      <v-avatar tile color="#F7FAFD">
                                        <i class="uiIconEcmsViewTimeline iconlarge"></i>
                                      </v-avatar>
                                    </div>
                                  </template>
                                </v-layout>
                              </v-flex>
                              <v-flex
                                d-flex
                                xs8>
                                <v-card
                                  class="py-1"
                                  flat
                                  color="#F7FAFD">
                                  <div class="title">{{ incomingTasksSize }} {{ $t('label.tasks.tasks') }}</div>
                                  <div class="caption color-title">{{ $t('label.incoming') }}</div>
                                </v-card>
                              </v-flex>
                            </v-layout>
                          </v-flex>
                        </v-layout>
                      </v-flex>
                    </v-layout>
                  </v-card>
                </v-flex>
                <v-divider 
                  vertical 
                  inset 
                  class="mt-2"/>
                <v-flex 
                  v-if="tasks.length > 0" 
                  xs12 
                  style="cursor: pointer" 
                  @click="navigateTo('tasks/my-task/overdue')">
                  <v-card
                    class="mx-auto"
                    flat
                    color="#F7FAFD">
                    <v-layout>
                      <v-flex>
                        <v-layout
                          row
                          mx-0
                          justify-center>
                          <v-flex
                            xs12
                            d-flex>
                            <v-layout mx-0>
                              <v-flex
                                d-flex
                                xs5>
                                <v-layout
                                  mx-0
                                  justify-center
                                  class="pa-2">
                                  <template>
                                    <div class="text-center">
                                      <v-avatar tile color="#F7FAFD">
                                        <v-icon
                                          size="50"
                                          color="#D07B7B">mdi-alarm</v-icon>
                                      </v-avatar>
                                    </div>
                                  </template>
                                </v-layout>
                              </v-flex>
                              <v-flex
                                d-flex
                                xs7>
                                <v-card
                                  class="py-1"
                                  flat
                                  color="#F7FAFD">
                                  <div class="title">{{ overdueTasksSize }} {{ $t('label.tasks.tasks') }}</div>
                                  <div class="caption color-title">{{ $t('label.overdue') }}</div>
                                </v-card>
                              </v-flex>
                            </v-layout>
                          </v-flex>
                        </v-layout>
                      </v-flex>
                    </v-layout>
                  </v-card>
                </v-flex>
              </v-layout>
          </v-flex></v-layout>
        </v-flex>
        <v-flex
          :class="tasks.length > 0 && 'pl-2 pt-4'"
          d-flex
          xs12>
          <v-layout
            row
            mx-0>
            <v-flex
              xs12
              class="pt-2">
              <transition-group name="list">
                <span
                  v-for="task in tasks"
                  :key="task.id"
                  class="list-item">
                  <task-details :task="task" @removeTask="removeTask"/>
                </span>
              </transition-group>
              <div v-if="!loadingTasks && tasks.length === 0" class="noTasks">
                <div class="noTasksContent">
                  <i class="uiNoTaskIcon"></i>
                  <div class="noTasksTitle">{{ $t('label.noTask') }}</div>
                  <p>{{ $t('label.thereIsNoTasks') }}</p>
                </div>
              </div>
        </v-flex></v-layout></v-flex>
      </v-layout>
    </v-container>
  </v-app>
</template>

<script>
  import {getMyAllTasks, getMyIncomingTasks, getMyOverdueTasks} from '../tasksApi'

  export default {

    data() {
      return {
        placeholder: '',
        tasks: [],
        loadingTasks: true,
        incomingTasksSize:'',
        overdueTasksSize:''
      }
    },
    created(){
      this.itemsLimit = this.$parent.$data.itemsLimit;
      this.getMyAllTasks();
      this.getMyIncomingTasksSize();
      this.getMyOverdueTasksSize();
    },
    methods: {
      getMyAllTasks() {
        getMyAllTasks(this.itemsLimit).then(
          (tasks) => {
            let tasksWithDuedate = [];
            let tasksWithoutDuedate = [];
            for (let i = 0; i < tasks.length; i++) {
              if (tasks[i].dueDate) {
                tasksWithDuedate.push(tasks[i])
              } else {
                tasksWithoutDuedate.push(tasks[i])
              }
            }
            tasksWithDuedate = tasksWithDuedate.sort((a, b) => ((a.dueDate.time - b.dueDate.time)));
            tasksWithoutDuedate = tasksWithoutDuedate.sort((a, b) => ((a.createdTime - b.createdTime)));
            this.tasks = tasksWithDuedate.concat(tasksWithoutDuedate);
            this.loadingTasks = false
          }
        )
      },
      getMyIncomingTasksSize() {
        getMyIncomingTasks(this.itemsLimit).then(
          (data) => {
            this.incomingTasksSize = data.size;
          }
        )
      },
      getMyOverdueTasksSize() {
        getMyOverdueTasks(this.itemsLimit).then(
          (data) => {
            this.overdueTasksSize = data.size;
          }
        )
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