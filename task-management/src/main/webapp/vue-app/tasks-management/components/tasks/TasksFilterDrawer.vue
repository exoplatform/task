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
            <v-toolbar dense>
              <v-tabs
                v-model="tab">
                <v-tab class="text-capitalize">{{ $t('label.filter.groupandsort') }}</v-tab>
                <v-tab v-if="taskViewTabName != 'gantt'" class="text-capitalize">{{ $t('label.filter.filter') }}</v-tab>
                <v-tab v-if="taskViewTabName != 'gantt'" class="text-capitalize">{{ $t('label.filter.label') }}</v-tab>
              </v-tabs>
            </v-toolbar>

            <v-tabs-items v-model="tab">
              <v-tab-item v-if="!project">
                <v-card>
                  <tasks-group-drawer
                    ref="filterGroupTasksDrawer"
                    v-model="groupBy" />
                  <tasks-sort-by-drawer
                    ref="filterSortTasksDrawer"
                    v-model="orderBy" />
                </v-card>
              </v-tab-item>

              <v-tab-item v-if="project">
                <v-card>
                  <tasks-group-project-drawer
                    ref="filterGroupTasksDrawer"
                    v-model="groupBy"
                    :task-view-tab-name="taskViewTabName"
                    @scale-changed="changeScale" />
                  <tasks-sort-by-project-drawer
                    v-if="taskViewTabName != 'gantt'"
                    ref="filterSortTasksDrawer"
                    v-model="orderBy" />
                </v-card>
              </v-tab-item>

              <v-tab-item>
                <v-card>
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
                      required>

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
                        :value="item.id"
                        class="text-capitalize">
                        {{ statusFilterLabel(item.name) }}
                      </option>
                    </select>

                    <v-label for="assignee">
                      <span class="font-weight-bold body-2">{{ $t('label.assignee') }}</span>
                    </v-label>
                    <exo-identity-suggester
                      ref="autoFocusInput3"
                      :labels="suggesterLabels"
                      v-model="assignee"
                      :search-options="searchOptions"
                      name="assignee"
                      type-of-relations="user_to_invite"
                      height="40"
                      include-users />
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
                            v-model="showCompletedTasks"
                            type="checkbox">
                          <div class="slider round"><span class="absolute-yes">{{ $t(`filter.task.showCompleted.yes`,"YES") }}</span></div>
                          <span class="absolute-no">{{ $t(`filter.task.showCompleted.no`) }}</span>
                        </label>
                      </form>
                    </div>
                  </form>
                </v-card>
              </v-tab-item>

              <v-tab-item>
                <v-card>
                  <tasks-labels-drawer
                    ref="filterLabelsTasksDrawer"
                    :project-id="projectId"
                    :labels="labels" />
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
            class="btn me-2"
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
      default: ''
    },
    orderBy: {
      type: String,
      default: ''
    },
    project: {
      type: String,
      default: ''
    },
    query: {
      type: String,
      default: ''
    },
    taskViewTabName: {
      type: String,
      default: ''
    },
    statusList: {
      type: Array,
      default: () =>[],
    },
    showCompletedTasks: {
      type: Boolean,
      default: false,
    },
  },
  data () {
    return {
      tab: null,
      dueDateSelected: '',
      prioritySelected: '',
      statusSelected: '',
      projectId: 0,
      assignee: '',
      assigneeTask: '',
      dueDate: [
        {name: ''},{name: 'OVERDUE'},{name: 'TODAY'},{name: 'TOMORROW'},{name: 'UPCOMING'}
      ],
      priority: [
        {name: ''},{name: 'NONE'},{name: 'LOW'},{name: 'NORMAL'},{name: 'HIGH'}
      ],
      labels: [],
    };
  },
  computed: {
    suggesterLabels() {
      return {
        placeholder: this.$t('label.assignee'),
        noDataLabel: this.$t('label.noDataLabel'),
      };
    },
    searchOptions() {
      if (this.project) {
        const options = {
          includeCurrentUser: true,
        };
        return {
          searchUrl: '/portal/rest/projects/projectParticipants/'.concat(this.project).concat('/'),
          options: options
        };
      }
      return {};
    },
  },
  created() {
    this.$root.$on('filter-task-labels',labels =>{
      this.labels = labels;
    });
  },
  methods: {
    open() {
      const urlPath = document.location.pathname;
      this.getTabView();
      const projectId = urlPath.includes('projectDetail') && urlPath.split('projectDetail/')[1].split(/[^0-9]/)[0] ?
        Number(urlPath.split('projectDetail/')[1].split(/[^0-9]/)[0]) : 0;
      if (projectId > 0) {
        window.setTimeout(() => {
          document.dispatchEvent(new CustomEvent('loadFilterProjectLabels', {
            detail: projectId
          }));
        }, 200);

      }
      const filterStorageProperty = projectId > 0 ? `filterStorage${projectId}+${this.taskViewTabName}` : 'filterStorageNone+list';

      const localStorageSaveFilter = localStorage.getItem(filterStorageProperty) ?
        JSON.parse(localStorage.getItem(filterStorageProperty)) : null;

      this.groupBy = projectId > 0 && localStorageSaveFilter && localStorageSaveFilter.projectId === projectId && localStorageSaveFilter.groupBy ||
      localStorageSaveFilter && localStorageSaveFilter.projectId === 'None' && localStorageSaveFilter.groupBy ? localStorageSaveFilter.groupBy : '';

      this.orderBy = projectId > 0 && localStorageSaveFilter && localStorageSaveFilter.projectId === projectId && localStorageSaveFilter.orderBy ||
      localStorageSaveFilter && localStorageSaveFilter.projectId === 'None' && localStorageSaveFilter.orderBy ? localStorageSaveFilter.orderBy : '';
      
      this.$root.$emit('reset-filter-task-group-sort', this.groupBy);
      this.$root.$emit('reset-filter-task-sort', this.orderBy);
      this.$refs.filterTasksDrawer.open();
    },
    cancel() {
      this.$refs.filterTasksDrawer.close();
      this.query=this.task.query;
      this.assignee=this.task.assignee;
      this.dueDateSelected='';
      this.prioritySelected='';
      this.statusSelected='';
      if (this.taskViewTabName === 'list'){
        this.groupBy='status';
      } else {
        this.groupBy='none';
      }
      this.orderBy='';
      this.labels='';
      this.showCompletedTasks=false;
      this.getFilterNumber();
      this.$root.$emit('reset-filter-task-group-sort',this.groupBy);
    },
    reset() {
      this.query = this.task.query;
      this.assignee = this.task.assignee;
      this.dueDateSelected = '';
      this.prioritySelected = '';
      this.statusSelected = '';
      if (this.taskViewTabName === 'list') {
        this.groupBy = 'status';
      } else {
        this.groupBy = '';
      }
      this.orderBy = '';
      this.labels = '';
      this.showCompletedTasks = false;
      this.getFilterNumber();
      const jsonToSave = {
        groupBy: this.groupBy,
        orderBy: this.orderBy,
        projectId: this.project || 'None',
        tabView: this.taskViewTabName !== '' ? this.taskViewTabName : 'list',
      };
      this.saveValueFilterInStorage(jsonToSave);      
      this.$root.$emit('reset-filter-task-group-sort', this.groupBy);
      this.$emit('reset-filter-task');
    },
    resetFields(source) {
      this.query='';
      this.assignee='';
      this.dueDateSelected='';
      this.prioritySelected='';
      this.statusSelected='';
      this.groupBy='';
      this.orderBy='';
      this.labels='';
      this.showCompletedTasks=false;
      this.$root.$emit('reset-filter-task-group-sort',this.groupBy);
      this.getFilterNumber(source);
    },
    filterTasks() {
      if (this.taskViewTabName === 'gantt') {
        this.cancel();
      } else {
        if (this.groupBy === 'completed') {
          this.showCompletedTasks = true;
        }
        const tasks = {
          query: this.query,
          assignee: '',
          statusId: this.statusSelected,
          dueDate: this.dueDateSelected,
          priority: this.prioritySelected,
          showCompletedTasks: this.showCompletedTasks,
          groupBy: this.groupBy,
          orderBy: this.orderBy,
        };
        const filterGroupSort = {
          groupBy: this.groupBy,
          orderBy: this.orderBy,
        };
        const filterLabels = {
          labels: [],
        };
        if (this.labels) {
          this.labels.forEach(user => {
            filterLabels.labels.push(user.id);
          });
        }
        if (this.assignee) {
          tasks.assignee = this.assignee.remoteId;
        }
        const jsonToSave = {
          groupBy: this.groupBy,
          orderBy: this.orderBy,
          projectId: this.project || 'None',
          tabView: (this.taskViewTabName !== '' ? this.taskViewTabName : 'list'),
        };
        this.saveValueFilterInStorage(jsonToSave);
        if (this.project) {
          this.$emit('filter-task', {tasks, filterLabels, showCompletedTasks: this.showCompletedTasks});
          this.getFilterNumber();
          if (this.$refs.filterTasksDrawer) {
            this.$refs.filterTasksDrawer.close();
          }
        } else {
          this.$emit('filter-task-query', tasks, filterGroupSort, filterLabels);

          this.getFilterNumber();
          this.$refs.filterTasksDrawer.close();
        }
      }
    },
    getFilterNumber(source){
      let filtersnumber = 0;
      if (this.query){
        filtersnumber++;
      }
      if (this.assignee){
        filtersnumber++;
      }
      if (this.dueDateSelected){
        filtersnumber++;
      }
      if (this.prioritySelected){
        filtersnumber++;
      }
      if (this.statusSelected){
        filtersnumber++;
      }
      if (this.labels&&this.labels.length>0){
        filtersnumber++;
      }
      if (this.showCompletedTasks){
        filtersnumber++;
      }
      this.$emit('filter-num-changed',filtersnumber,source);
    },
    saveValueFilterInStorage(value) {
      this.$projectService.saveFilterSettings(value).then((response) => {
        if (response) {
          value.showCompletedTasks = this.showCompletedTasks;
          if (this.project) {
            localStorage.setItem(`filterStorage${value.projectId}+${value.tabView}`, JSON.stringify(value));
          } else {
            localStorage.setItem('filterStorageNone+list', JSON.stringify(value));
          }
        }
      });
    },
    statusFilterLabel(item) {
      if (this.$t(`label.status.${item.toLowerCase()}`).includes('label.status')) {
        return item;
      } else {
        return this.$t(`label.status.${item.toLowerCase()}`);
      }
    },
    getTabView(){
      const urlPath=document.location.pathname;
      if (document.getElementsByClassName('taskTabList')[0] && urlPath.includes('projectDetail')) {
        if (document.getElementsByClassName('taskTabList')[0].getAttribute('aria-selected')==='true'){
          this.taskViewTabName='list';
        } else if (document.getElementsByClassName('taskTabBoard')[0].getAttribute('aria-selected')==='true'){
          this.taskViewTabName='board';
        } else if (document.getElementsByClassName('taskTabGantt')[0].getAttribute('aria-selected')==='true'){
          this.taskViewTabName='gantt';
        }
      }
      return this.taskViewTabName;
    },
    changeScale(value) {
      this.$root.$emit('scale-value-changed', value);
    }
  }
};
</script>
