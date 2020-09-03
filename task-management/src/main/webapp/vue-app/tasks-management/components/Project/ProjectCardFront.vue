<template>
  <v-card class="tasksCardItem">
    <div :class="project.color || 'noProjectColor'" class="taskItemToolbar d-flex px-2 py-3 align-center font-weight-bold">
      <i
        :class="project.color && 'white--text' || 'toolbarNoColor'"
        icon
        small
        class="uiIconInformation taskInfoIcon d-flex"
        @click="$emit('flip')">
      </i>
      <v-spacer />
      <span :class="project.color && 'white--text' || 'toolbarNoColor'">{{ project.name }}</span>
      <v-spacer />
      <i
        :class="project.color && 'white--text' || 'toolbarNoColor'"
        icon
        small
        class="uiIconVerticalDots taskInfoIcon d-flex">
      </i>
    </div>
    <div class="taskItemInfo pa-3">
      <div class="taskItemDescription">
        <ellipsis
          v-if="project.description"
          :title="project.description"
          :data="project.description"
          :line-clamp="3"
          end-char=".."/>
        <div v-else>
          <span class="noProjectDescription">No description available</span>
        </div>
      </div>
      <v-divider class="pb-4"/>
      <div class="ProjectSpace">
        <div v-if="isSpaceProject">
          <v-list-item>
            <v-list-item-avatar size="28" class="spaceAvatar py-1">
              <v-img src="/portal/rest/v1/social/spaces/default-image/avatar"/>
            </v-list-item-avatar>
            <v-list-item-title class="body-2">
              <a href="#">Space Name</a>
            </v-list-item-title>
          </v-list-item>
        </div>
        <div v-else>
          <v-list-item>
            <v-list-item-avatar size="28" class="spaceAvatar py-1">
              <i class="uiIconEcmsNameSpace noSpaceProjectIcon"></i>
            </v-list-item-avatar>
            <v-list-item-title class="noSpaceLabel body-2">No Space</v-list-item-title>
          </v-list-item>
        </div>
      </div>
      <div class="SpaceAdmin">
        <v-list-item>
          <v-list-item-avatar size="28" class="userAvatar py-1">
            <v-img src="/portal/rest/v1/social/users/default-image/avatar"/>
          </v-list-item-avatar>
          <v-list-item-content>
            <v-list-item-title class="body-2">
              <a href="#">Admin Name</a>
            </v-list-item-title>
          </v-list-item-content>
          <v-icon size="12" class="orange--text lighten-2">fa-star</v-icon>
        </v-list-item>
      </div>
    </div>
  </v-card>
</template>
<script>
  export default {
    props: {
      project: {
        type: Object,
        default: null,
      }
    },
    data () {
      return {
        isSpaceProject: false,
      }
    },
    created() {
      this.projectSpaceInformation();
    },
    methods: {
      projectSpaceInformation() {
        if(this.project.manager) {
          const el = this.project.manager.find(a => a.includes("manager:/spaces"));
          if (el) {
            this.isSpaceProject = true;
          }
        }
      }
    }
  }
</script>