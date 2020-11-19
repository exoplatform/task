<template>
  <exo-drawer
    ref="filterTasksDrawer"
    class="filterTasksDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('label.filter') }}
    </template>
    <template slot="content">
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

          <!--<v-switch v-model="showCompleteTasks" class="mt-0 ml-4" />
          <v-switch
            ref="autoFocusInput2"
            v-model="showCompleteTasks"
            true-value="true"
            false-value="false"
            class="float-left my-0 ml-4" />-->
        </div>
      </form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <div class="VuetifyApp d-flex">
          <v-btn 
            class="reset"
            @click="reset">
            <template>
              <i class="fas fa-redo"></i>
              {{ $t('popup.reset') }}
            </template>
          </v-btn>
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
      }
    },
    data () {
      return {
        dueDateSelected:'',
        prioritySelected:'',
        query:'',
        assignee:'',
        assigneeTask:'',
        dueDate: [
          {name: ""},{name: "OVERDUE"},{name: "TODAY"},{name: "TOMORROW"},{name: "UPCOMING"}
        ],
        priority: [
          {name: ""},{name: "NONE"},{name: "LOW"},{name: "NORMAL"},{name: "HIGH"}
        ],
        showCompleteTasks:false,
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
        this.showCompleteTasks=true;
      },
      reset() {
        this.query=this.task.query;
        this.assignee=this.task.assignee;
        this.dueDateSelected='';
        this.prioritySelected='';
        this.showCompleteTasks=true;
        this.$emit('reset-filter-task');
      },
      filterTasks(){
        const tasks = {
          query: this.query,
          assignee: '',
          dueDate: this.dueDateSelected,
          priority: this.prioritySelected,
          showCompleteTasks: this.showCompleteTasks
        };
        if (this.assignee !== null && this.assignee !== undefined && this.assignee !== ''){
          tasks.assignee = this.assignee.remoteId;
        }
        return this.$tasksService.filterTasksList(tasks).then(tasks => {
          this.$emit('filter-task',{ tasks,showCompleteTasks:this.showCompleteTasks });
          this.$refs.filterTasksDrawer.close();

        })
                .catch(e => {
                  console.debug("Error updating project", e);
                  this.$emit('error', e && e.message ? e.message : String(e));
                });

      }
    }
  }
</script>

