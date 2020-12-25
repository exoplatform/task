<template>
  <exo-drawer
    ref="filterTasksDrawer"
    class="filterTasksDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('label.sortAndFilter') }}
    </template>
    <template slot="content">
      <div class="VuetifyApp">
        <template>
          <v-card class="taskFilter">
            <v-toolbar>
              <v-tabs
                v-model="tab">
                <v-tab class="text-capitalize">{{ $t('label.filter.groupandsort') }}</v-tab>
                <v-tab class="text-capitalize">{{ $t('label.filter.filter') }}</v-tab>
                <v-tab class="text-capitalize">{{ $t('label.filter.label') }}</v-tab>
              </v-tabs>
            </v-toolbar>

            <v-tabs-items v-model="tab">
              <v-tab-item v-if="!project">
                <v-card >
                  <tasks-group-drawer
                    ref="filterGroupTasksDrawer"
                    v-model="groupBy"/>
                  <tasks-sort-by-drawer
                    ref="filterSortTasksDrawer"
                    v-model="sortBy"/>
                </v-card>
              </v-tab-item>

              <v-tab-item v-if="project">
                <v-card >
                  <tasks-group-project-drawer
                    ref="filterGroupTasksDrawer"
                    v-model="groupBy"/>
                  <tasks-sort-by-project-drawer
                    ref="filterSortTasksDrawer"
                    v-model="sortBy"/>
                </v-card>
              </v-tab-item>

              <v-tab-item>
                <v-card >
                  <form ref="form1" class="mt-4">
                    <v-label for="name">
                      <span class="font-weight-bold body-2">{{ $t('filter.task.contains') }}</span>
                    </v-label>
                    <input
                      ref="autoFocusInput1"
                      v-model="query"
                      :placeholder="$t('filter.task.contains')"
                      type="text"
                      name="name"
                      class="input-block-level ignore-vuetify-classes my-3"
                      required >

                    <v-label v-if="project" for="status">
                      <span class="font-weight-bold body-2">{{ $t('filter.task.status') }}</span>
                    </v-label>
                    <select
                      v-if="project"
                      v-model="statusSelected"
                      name="status"
                      class="input-block-level ignore-vuetify-classes my-3">
                      <option value="">{{ $t('label.priority.') }}</option>
                      <option
                        v-for="item in statusList"
                        :key="item.id"
                        :value="item.id">
                        {{ $t('label.status.'+item.name.toLowerCase()) }}
                      </option>
                    </select>

                    <v-label for="assignee">
                      <span class="font-weight-bold body-2">{{ $t('label.assignee') }}</span>
                    </v-label>
                    <exo-identity-suggester
                      ref="autoFocusInput3"
                      :labels="suggesterLabels"
                      v-model="assignee"
                      name="assignee"
                      type-of-relations="user_to_invite"
                      height="40"
                      include-users/>
                    <v-label for="taskDueDate">
                      <span class="font-weight-bold body-2">{{ $t('filter.task.due') }}</span>
                    </v-label>
                    <select
                      v-model="dueDateSelected"
                      name="taskDueDate"
                      class="input-block-level ignore-vuetify-classes my-3">

                      <option
                        v-for="item in dueDate"
                        :key="item.name"
                        :value="item.name">
                        {{ $t('label.dueDate.'+item.name.toLowerCase()) }}
                      </option>
                    </select>

                    <v-label for="priority">
                      <span class="font-weight-bold body-2">{{ $t('label.priority') }}</span>
                    </v-label>
                    <select
                      v-model="prioritySelected"
                      name="priority"
                      class="input-block-level ignore-vuetify-classes my-3">

                      <option
                        v-for="item in priority"
                        :key="item.name"
                        :value="item.name">
                        {{ $t('label.priority.'+item.name.toLowerCase()) }}
                      </option>
                    </select>


                    <div class="showCompleteTasks d-flex flex-wrap pt-2">
                      <form class="switchEnabled">
                        <label class="col-form-label pt-0" max-rows="6">{{ $t(`filter.task.showCompleted`)
                        }}:</label>
                        <label class="switch">
                          <input
                            v-model="showCompleteTasks"
                            type="checkbox">
                          <div class="slider round"><span class="absolute-yes">{{ $t(`filter.task.showCompleted.yes`,"YES") }}</span></div>
                          <span class="absolute-no">{{ $t(`filter.task.showCompleted.no`) }}</span>
                        </label>
                      </form>
                    </div>
                  </form>
                </v-card>
              </v-tab-item>

              <v-tab-item >
                <v-card >
                  <tasks-labels-drawer
                    ref="filterLabelsTasksDrawer"
                    :labels="labels"/>
                </v-card>
              </v-tab-item>
            </v-tabs-items>
          </v-card>
        </template>
      </div>

    </template>
    <template slot="footer">
      <div class="VuetifyApp flex d-flex">
        <v-btn
          class="reset"
          @click="reset">
          <template>
            <i class="fas fa-redo"></i>
            {{ $t('popup.reset') }}
          </template>
        </v-btn>
        <v-spacer />
        <div class="d-btn">
          <v-btn
            class="btn mr-2"
            @click="cancel">
            <template>
              {{ $t('popup.cancel') }}
            </template>
          </v-btn>

          <v-btn
            :loading="postProject"
            class="btn btn-primary"
            @click="filterTasks">
            <template>
              {{ $t('label.confirm') }}
            </template>
          </v-btn>
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
    props: {
      task: {
        type: Object,
        default: () => ({}),
      },
      groupBy: {
        type: String,
        default:''
      },
      sortBy: {
        type: String,
        default:''
      },
      project: {
        type: String,
        default:''
      },
      query: {
        type: String,
        default:''
      },
      statusList: {
        type: Array,
        default: () =>[],
      }
    },
    data () {
      return {
        tab: null,
        dueDateSelected:'',
        prioritySelected:'',
        statusSelected:'',
        assignee:'',
        assigneeTask:'',
        dueDate: [
          {name: ""},{name: "OVERDUE"},{name: "TODAY"},{name: "TOMORROW"},{name: "UPCOMING"}
        ],
        priority: [
          {name: ""},{name: "NONE"},{name: "LOW"},{name: "NORMAL"},{name: "HIGH"}
        ],
        showCompleteTasks:false,
        labels:[],
      }
    },
    computed: {
      suggesterLabels() {
        return {
          placeholder: this.$t('label.assignee'),
          noDataLabel: this.$t('label.noDataLabel'),
        };
      }
    },
    created() {
      this.$root.$on('filter-task-labels',labels =>{
        this.labels = labels;
      });
    },
    methods: {
      open() {
        this.$refs.filterTasksDrawer.open();
      },
      cancel() {
        this.$refs.filterTasksDrawer.close();
        this.query=this.task.query;
        this.assignee=this.task.assignee;
        this.dueDateSelected='';
        this.prioritySelected='';
        this.statusSelected='';
        this.groupBy='none';
        this.sortBy='';
        this.labels='';
        this.showCompleteTasks=false;
        this.getFilterNumber()
        this.$root.$emit('reset-filter-task-group-sort',this.groupBy);
      },
      reset() {
        this.query=this.task.query;
        this.assignee=this.task.assignee;
        this.dueDateSelected='';
        this.prioritySelected='';
        this.statusSelected='';
        this.groupBy='';
        this.sortBy='';
        this.labels='';
        this.showCompleteTasks=false;
        this.getFilterNumber()
        this.$root.$emit('reset-filter-task-group-sort',this.groupBy);
        this.$emit('reset-filter-task');
      },
      resetFields(source) {
        this.query='';
        this.assignee='';
        this.dueDateSelected='';
        this.prioritySelected='';
        this.statusSelected='';
        this.groupBy='';
        this.sortBy='';
        this.labels='';
        this.showCompleteTasks=false;
        this.$root.$emit('reset-filter-task-group-sort',this.groupBy);
        this.getFilterNumber(source)
      },
      filterTasks(){
        const tasks = {
          query: this.query,
          assignee: '',
          statusId: this.statusSelected,
          dueDate: this.dueDateSelected,
          priority: this.prioritySelected,
          showCompleteTasks: this.showCompleteTasks,
          groupBy: this.groupBy,
          orderBy: this.sortBy,
        };
        const filterGroupSort = {
          groupBy: this.groupBy,
          sortBy: this.sortBy,
        };
        const filterLabels = {
          labels: [],
        };
        if(this.labels && this.labels!== null && this.labels !== ''){
          this.labels.forEach(user => {
            filterLabels.labels.push(user.id)
          })
        }
        if (this.assignee !== null && this.assignee !== undefined && this.assignee !== ''){
          tasks.assignee = this.assignee.remoteId;
        }
        if(this.project){
          this.$emit('filter-task',{ tasks,filterLabels,showCompleteTasks:this.showCompleteTasks });
          this.getFilterNumber()
          this.$refs.filterTasksDrawer.close();
        }else{
          this.$emit('filter-task-query', tasks,filterGroupSort,filterLabels);
       
        this.getFilterNumber()
          this.$refs.filterTasksDrawer.close();
      }
      },
      getFilterNumber(source){
        let filtersnumber = 0
        if(this.query){
          filtersnumber++
        }
        if(this.assignee){
          filtersnumber++
        }
        if(this.dueDateSelected){
          filtersnumber++
        }
        if(this.prioritySelected){
          filtersnumber++
        }
        if(this.statusSelected){
          filtersnumber++
        }
        if(this.labels&&this.labels.length>0){
          filtersnumber++
        }
        if(this.showCompleteTasks){
          filtersnumber++
        }
        this.$emit('filter-num-changed',filtersnumber,source)
      }

    }
  }
</script>
