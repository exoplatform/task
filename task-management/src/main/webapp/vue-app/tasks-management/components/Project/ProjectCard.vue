<template>
  <v-app id="projectCard">
    <v-flex :class="flipCard && 'taskCardFlip taskCardFlipped' || 'taskCardFlip'">
      <div class="taskCardFront pa-3">
        <project-card-front
          :project="project"
          @openDrawer="openEditDrawer"
          @closed="onCloseDrawer"
          @refreshProjects="refreshProjects"
          @flip="flipCard = true; flip()"/>
      </div>
      <div class="tasksCardBack pa-3">
        <project-card-Reverse
          ref="reversCard"
          :project="project"
          @flip="flipCard = false; flip()"/>
      </div>
    </v-flex>
  </v-app>
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
      flipCard: false,
    }
  },
  methods:{
    openEditDrawer() {
       this.$root.$emit('open-project-drawer', this.project)
    },
    onCloseDrawer: function(drawer){
      this.drawer = drawer;
    },
    refreshProjects: function(){
    this.$emit('refreshProjects');
    },
    flip: function(){
      if(this.flipCard){
        this.$refs.reversCard.getStats(this.project);
      }
    }
  }
}
</script>

