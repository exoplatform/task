<template>
  <div class="taskPriority">
    <v-select
      ref="selectPriority"
      v-model="priority"
      :items="priorities"
      item-value="key"
      item-text="value"
      attach
      solo
      dense
      class="pt-0 selectFont"
      @change="updateTaskPriority()"
      @click="$emit('PriorityListOpened')">
      <template v-slot:append>
        <v-icon :class="priorityDefaultColor" size="20">mdi-flag-variant</v-icon>
      </template>
      <template slot="item" slot-scope="data">
        <v-list-avatar class="me-2">
          <v-icon :class="getTaskPriorityColor(data.item.key)" size="20">mdi-flag-variant</v-icon>
        </v-list-avatar>
        <v-list-tile-content>
          <v-list-tile-title class="body-2">
            {{ data.item.value }}
          </v-list-tile-title>
        </v-list-tile-content>
      </template>
    </v-select>
  </div>
</template>
<script>
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
      priority: '',
      priorityDefaultColor: 'highPriorityColor',
      priorities: [
        {key: 'HIGH',value: this.$t('label.priority.high')},
        {key: 'NORMAL',value: this.$t('label.priority.normal')},
        {key: 'LOW',value: this.$t('label.priority.low')},
        {key: 'NONE',value: this.$t('label.priority.none')}
      ],
    };
  },
  watch: {
    priority() {
      if (this.task.priority !== this.priority) {
        this.updateTaskPriority();
      }
    }
  },
  created() {
    $(document).on('mousedown', () => {
      if (this.$refs.selectPriority && this.$refs.selectPriority.isMenuActive) {
        window.setTimeout(() => {
          this.$refs.selectPriority.isMenuActive = false;
        }, 200);
      }
    });
    document.addEventListener('loadTaskPriority', event => {
      if (event && event.detail) {
        const task = event.detail;
        if (task.id!=null) {
          this.priority = task.priority;
          this.priorityDefaultColor = this.getTaskPriorityColor(task.priority);
        } else {
          this.priority = 'NONE';
          this.priorityDefaultColor = this.getTaskPriorityColor('NONE');
        }
      }
    });
    document.addEventListener('closePriority',()=> {
      if (this.$refs.selectPriority && this.$refs.selectPriority.isMenuActive) {
        window.setTimeout(() => {
          this.$refs.selectPriority.isMenuActive = false;
        }, 100);
      }
    });
  },
  methods: {
    getTaskPriorityColor(priority) {
      switch (priority) {
      case 'HIGH':
        return 'highPriorityColor';
      case 'NORMAL':
        return 'normalPriorityColor';
      case 'LOW':
        return 'lowPriorityColor';
      case 'NONE':
        return 'nonePriorityColor';
      }
    },
    updateTaskPriority() {
      this.priorityDefaultColor = this.getTaskPriorityColor(this.priority);
      const value = {
        'priority': this.priority,
        'taskId': this.task.id,
      };
      this.$emit('updateTaskPriority',value);
    },
  }
};
</script>
