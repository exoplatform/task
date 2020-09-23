<template>
  <div
    :id="'task-'+viewType+'-'+status.name"
    class="tasksViewHeader d-flex justify-space-between">
    <p class="taskStatusName font-weight-bold text-color mb-1">{{ status.name }}</p>
    <p class="taskNumberAndActions mb-1">
      <span class="caption">{{ tasksNumber }}</span>
      <i
        class="uiIcon uiIconThreeDots"
        @click="displayActionMenu = true"></i>
      <v-menu
        v-model="displayActionMenu"
        :attach="`#task-${viewType}-${status.name}`"
        transition="slide-x-reverse-transition"
        content-class="taskStatusActionMenu"
        offset-y>
        <v-list>
          <v-list-item>
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconTask pr-1"></i>
              <span>{{ $t('label.addTask') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconBefore pr-1"></i>
              <span>{{ $t('label.status.before') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconAfter pr-1"></i>
              <span>{{ $t('label.status.after') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconDelete pr-1"></i>
              <span>{{ $t('label.delete') }}</span>
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </p>
  </div>
</template>
<script>
  export default {
    props: {
      status: {
        type: Object,
        default: null
      },
      viewType: {
        type: String,
        default: ''
      },
      tasksNumber: {
        type: Number,
        default: 0
      }
    },
    data() {
      return {
        displayActionMenu: false,
      }
    },
    created() {
      $(document).on('mousedown', () => {
        if (this.displayActionMenu) {
          window.setTimeout(() => {
            this.displayActionMenu = false;
          }, 200);
        }
      });
    },
  }
</script>