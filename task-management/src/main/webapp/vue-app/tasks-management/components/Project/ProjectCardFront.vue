<template>
  <v-card 
    :class="project.color && project.color+'_border_bottom' || ''"
    class="tasksCardItem" 
    flat>
    <div class="taskItemToolbar d-flex px-2 align-center font-weight-bold">
      <i
        :class="project.color && 'white--text' || 'toolbarNoColor'"
        icon
        small
        class="uiIconInformation taskInfoIcon d-flex"
        @click="$emit('flip')">
      </i>
      <div class="spacer d-none d-sm-inline"></div>
      <span
        :class="project.color && 'white--text' || 'toolbarNoColor'"
        class="projectCardTitle py-3"
        @click="showProjectTasksDetails(project)">
        {{ project.name }}
      </span>
      <v-spacer />
      <i
        v-if="project.canManage"
        :class="project.color && 'white--text' || 'toolbarNoColor'"
        icon
        small
        class="uiIconVerticalDots taskInfoIcon d-flex"
        @click="displayActionMenu = true">
      </i>
      <v-menu
        v-model="displayActionMenu"
        :attach="`#project-${project.id}`"
        transition="slide-x-reverse-transition"
        content-class="projectActionMenu"
        offset-y>
        <v-list class="pa-0" dense>
          <v-list-item class="menu-list" @click="openEditDrawer()">
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconEdit pe-1"></i>
              <span>{{ $t('label.edit') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item class="draftButton" @click="confirmDeleteProject()">
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconTrash pe-1"></i>
              <span>{{ $t('label.delete') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item class="clone" @click="confirmCloneProject()">
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconCloneNode pe-1"></i>
              <span>{{ $t('label.clone') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item class="px-2 noColorLabel">
            <v-list-item-title class="noColorLabel caption text-center text&#45;&#45;secondary">
              <span @click="changeColorProject(project,'')">{{ $t('label.noColor') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2 row projectColorPicker mx-auto my-2">
              <span
                v-for="(color, i) in projectColors"
                :key="i"
                :class="[ color.class , color.class === project.color ? 'isSelected' : '']"
                class="projectColorCell"
                @click="changeColorProject(project,color.class)"></span>
            </v-list-item-title>
          </v-list-item>
          <!--
         <v-list-item>
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconHide pe-1"></i>
              <span>{{ $t('label.hide') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconStar pe-1"></i>
              <span>{{ $t('label.addAsFavorite') }}</span>
            </v-list-item-title>
          </v-list-item>
          -->
        </v-list>
      </v-menu>
    </div>
    <div class="taskItemInfo pa-3">
      <div
        :class="getClassDescription()"
        class="taskItemDescription"
        @click="showProjectTasksDetails(project)">
        <p
          v-if="project.description">
          {{ getDescription() }}
        </p>
        <div v-else>
          <span class="noProjectDescription">{{ $t('label.noDescription') }}</span>
        </div>
      </div>
      <div class="ProjectSpace">
        <div v-if="project.space">
          <v-list-item class="px-0">
            <v-list-item-avatar size="28" class="spaceAvatar py-1">
              <v-img :src="getSpaceAvatarURL" />
            </v-list-item-avatar>
            <v-list-item-title class="body-2">
              <a href="#">{{ getSpaceName }}</a>
            </v-list-item-title>
          </v-list-item>
        </div>
      </div>
    </div>
    <div class="SpaceAdmin">
      <v-divider />
      <div class="spaceAdminContainer">
        <div class="spaceAdminWrapper">
          <exo-user 
            v-if="avatarToDisplay && avatarToDisplay.length === 1"
            :identity="avatarToDisplay[0]"
            :size="28"
            :extra-class="'my-2'"
            link-style
            popover />
          <div
            v-else
            :class="showAllAvatarList && 'AllManagerAvatar'"
            class="managerAvatarsList d-flex flex-nowrap my-2">
            <exo-user-avatars-list
              :users="avatarToDisplay"
              :max="5"
              :icon-size="28"
              avatar-overlay-position
              @open-detail="$root.$emit('displayProjectManagers', avatarToDisplay)" />
          </div>
        </div>
      </div>
      <!--<i class="uiIcon uiIconStar"></i>-->
    </div>
    <exo-confirm-dialog
      ref="CancelSavingChangesDialog"
      :message="$t('popup.msg.delete', {0: project.name})"
      :title="$t('popup.confirm')"
      :ok-label="$t('popup.delete')"
      :cancel-label="$t('popup.cancel')"
      @ok="deleteProject()" />
    <exo-confirm-dialog
      ref="CancelSavingChangesCloneDialog"
      :message="$t('popup.msg.clone', {0: project.name})"
      :title="$t('popup.confirm')"
      :ok-label="$t('popup.clone')"
      :cancel-label="$t('popup.cancel')"
      @ok="cloneProject()" />
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
      displayActionMenu: false,
      waitTimeUntilCloseMenu: 200,
      projectColors: [
        { class: 'asparagus' },
        { class: 'munsell_blue' },
        { class: 'navy_blue' },
        { class: 'purple' },
        { class: 'red' },
        { class: 'brown' },
        { class: 'laurel_green' },
        { class: 'sky_blue' },
        { class: 'blue_gray' },
        { class: 'light_purple' },
        { class: 'hot_pink' },
        { class: 'light_brown' },
        { class: 'moss_green' },
        { class: 'powder_blue' },
        { class: 'light_blue' },
        { class: 'pink' },
        { class: 'Orange' },
        { class: 'gray' },
        { class: 'green' },
        { class: 'baby_blue' },
        { class: 'light_gray' },
        { class: 'beige' },
        { class: 'yellow' },
        { class: 'plum' },
      ],
      managerIdentities: this.project && this.project.managerIdentities,
      projectManagers: [],
    };
  },
  computed: {
    avatarToDisplay () {
      return this.projectManagers;
    },
    getSpaceName(){
      const str=this.project.space;
      return str.substr(0, str.indexOf(/spaces/)-2);
    },
    getSpaceAvatarURL() {
      const str=this.project.space;
      return `/portal/rest/v1/social/spaces/${ (str.substr(str.indexOf(/spaces/)+8)).slice(0,-1) }/avatar`;
    }
  },
  created() {
    $(document).on('mousedown', () => {
      if (this.displayActionMenu) {
        window.setTimeout(() => {
          this.displayActionMenu = false;
        }, this.waitTimeUntilCloseMenu);
      }
    });
    if ( this.managerIdentities && this.managerIdentities.length) {
      this.managerIdentities.forEach((identity) => {
        this.$userService.getUser(identity.username)
          .then(user => {
            this.projectManagers.push(user);
          });
      });
    }
    this.$root.$on('update-projects-list-avatar',managerIdentities =>{
      this.project.managerIdentities=managerIdentities;
    });
  },
  methods: {
    showProjectTasksDetails(project) {
      document.dispatchEvent(new CustomEvent('showProjectTasks', {detail: project}));
    },
    openEditDrawer() {
      this.$emit('openDrawer');
    },
    onCloseDrawer: function (drawer) {
      this.drawer = drawer;
    },
    confirmDeleteProject: function () {
      this.$refs.CancelSavingChangesDialog.open();
    },
    confirmCloneProject: function () {
      this.$refs.CancelSavingChangesCloneDialog.open();
    },
    deleteProject() {
      this.$projectService.deleteProjectInfo(this.project)
        .then(() => this.$emit('refreshProjects'))
        .then(this.$root.$emit('show-alert',{type: 'success',message: this.$t('alert.success.project.deleted')} ))
        .catch(e => {
          console.error('Error updating project', e);
          this.$root.$emit('show-alert',{type: 'error',message: this.$t('alert.error')} );
        });
    },
    cloneProject() {
      this.$projectService.cloneProject(this.project)
        .then(() => this.$emit('refreshProjects'))
        .then(this.$root.$emit('show-alert',{type: 'success',message: this.$t('alert.success.project.cloned')} ))
        .catch(e => {
          console.error('Error updating project', e);
          this.$root.$emit('show-alert',{type: 'error',message: this.$t('alert.error')} );
        });
    },
    changeColorProject(project,color) {
      this.$projectService.updateProjectColor(project, color)
        .then(() => this.$emit('projectChangeColor'))
        .then(this.project.color = color);
    },
    getDescription(){
      let text=this.project.description;
      const div = document.createElement('div');
      div.innerHTML = text;
      text = div.textContent || div.innerText || '';
      return text;
    },
    getClassDescription(){
      if (this.project && !this.project.space){
        return 'largeDescriptionArea';
      } else if (this.project && this.project.space){
        return 'largeDescriptionAreaSpace';
      }
    },
  },

};
</script>
