<template>
  <v-flex :class="flipCard && 'taskCardFlip taskCardFlipped' || 'taskCardFlip'">
    <div class="taskCardFront pa-3">
      <project-card-front
        :project="project"
        @flip="flipCard = true"/>
    </div>
    <div class="tasksCardBack pa-3">
      <project-card-Reverse
        :project="project"
        @flip="flipCard = false"/>
    </div>
  </v-flex>
</template>
<script>
export default {
  props: {
    project: {
      type: Object,
      default: () => ({}),
    }
  },
  data () {
    return {
      flipCard: false
    }
  }
}
</script>
