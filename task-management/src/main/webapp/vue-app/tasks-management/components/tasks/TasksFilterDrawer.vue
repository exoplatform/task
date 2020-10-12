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
          :placeholder="$t('spacesList.label.displayName')"
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
          name="assignee"
          type-of-relations="user_to_invite"
          height="100"
          include-users
          include-spaces
          multiple/>
        <v-label for="taskDueDate">
          <span class="font-weight-bold body-2">{{ $t('filter.task.due') }}</span>
        </v-label>
        <select
          v-model="dueDate"
          name="taskDueDate"
          class="input-block-level ignore-vuetify-classes my-3">
          <option 
            value="" 
            selected 
            disabled>{{ $t('filter.task.all') }}</option>
          <option
            v-for="item in dueDate"
            :key="item.name"
            :value="item.name">
            {{ $t('label.'+item.name.toLowerCase()) }}
          </option>
        </select>

        <v-label for="priority">
          <span class="font-weight-bold body-2">{{ $t('label.priority') }}</span>
        </v-label>
        <select
          v-model="priority"
          name="priority"
          class="input-block-level ignore-vuetify-classes my-3">
          <option
            value=""
            selected
            disabled>{{ $t('filter.task.all') }}</option>
          <option
            v-for="item in priority"
            :key="item.name"
            :value="item.name">
            {{ $t('label.priority.'+item.name.toLowerCase()) }}
          </option>
        </select>
        <div class="showCompleteTasks d-flex flex-wrap pt-2">
          <label for="showCompleteTasks" class="v-label theme--light my-auto float-left">
            <span class="font-weight-bold body-2">{{ $t('filter.task.showCompleted') }}</span>
          </label>
          <v-switch
            ref="autoFocusInput2"
            v-model="showCompleteTasks"
            true-value="showCompleteTasks"
            false-value="hideCompleteTasks"
            class="float-left my-0 ml-4" />
        </div>
      </form>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
    data () {
      return {
        dueDate: [
          {name: "OVERDUE"},{name: "TODAY"},{name: "TOMORROW"},{name: "UPCOMING"}
        ],
        priority: [
          {name: "NONE"},{name: "LOW"},{name: "NORMAL"},{name: "HIGH"}
        ],
        showCompleteTasks: true
      }
    },
    computed: {
      suggesterLabels() {
        return {
          placeholder: this.$t('label.inviteManagers'),
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
      }
    }
  }
</script>
