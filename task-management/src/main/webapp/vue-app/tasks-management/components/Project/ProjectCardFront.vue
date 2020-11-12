<template>
  <v-card 
    :class="project.color && project.color+'_border_bottom' || ''"
    class="tasksCardItem" 
    flat>
    <div :class="project.color || 'noProjectColor'" class="taskItemToolbar d-flex px-2 py-3 align-center font-weight-bold">
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
        class="projectCardTitle">
        {{ project.name }}
      </span>
      <v-spacer />
      <i
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
          <v-list-item class="menu-list" @click="openEditDrawer()" >
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconEdit pr-1"></i>
              <span>{{ $t('label.edit') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item class="draftButton" @click="confirmDeleteProject()">
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconTrash pr-1"></i>
              <span>{{ $t('label.delete') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item class="clone" @click="confirmCloneProject()">
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconCloneNode pr-1"></i>
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
              <i class="uiIcon uiIconHide pr-1"></i>
              <span>{{ $t('label.hide') }}</span>
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="subtitle-2">
              <i class="uiIcon uiIconStar pr-1"></i>
              <span>{{ $t('label.addAsFavorite') }}</span>
            </v-list-item-title>
          </v-list-item>
          -->
        </v-list>
      </v-menu>
    </div>
    <div class="taskItemInfo pa-3">
      <div
        class="taskItemDescription"
        @click="showProjectTasksDetails(project)">
        <p
          v-if="project.description">{{ getDescription() }}</p>
        <div v-else>
          <span class="noProjectDescription">{{ $t('label.noDescription') }}</span>
        </div>
      </div>
      <v-divider class="pb-4 descriptionDivider"/>
      <div class="ProjectSpace">
        <div v-if="project.space">
          <v-list-item class="px-0">
            <v-list-item-avatar size="28" class="spaceAvatar py-1">
              <v-img src="/portal/rest/v1/social/spaces/default-image/avatar"/>
            </v-list-item-avatar>
            <v-list-item-title class="body-2">
              <a href="#">{{ getSpaceName }}</a>
            </v-list-item-title>
          </v-list-item>
        </div>
        <div v-else>
          <v-list-item class="px-0">
            <v-list-item-avatar size="28" class="spaceAvatar py-1">
              <i class="uiIconEcmsNameSpace noSpaceProjectIcon"></i>
            </v-list-item-avatar>
            <v-list-item-title class="noSpaceLabel body-2">{{ $t('label.noSpace') }}</v-list-item-title>
          </v-list-item>
        </div>
      </div>
      <v-divider class="d-sm-inline"/>
      <div class="SpaceAdmin d-flex justify-space-between align-center">
        <div class="spaceAdminWrapper">
          <v-list-item v-if="managerIdentities && managerIdentities.length === 1" class="px-0">
            <v-list-item-avatar size="28" class="userAvatar py-1">
              <v-img :src="project.managerIdentities[0].avatar"/>
            </v-list-item-avatar>
            <v-list-item-content>
              <v-list-item-title class="body-2">
                <a :href="project.managerIdentities[0].url">{{ project.managerIdentities[0].displayName }}</a>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <div
            v-else
            :class="showAllAvatarList && 'AllManagerAvatar'"
            class="managerAvatarsList d-flex flex-nowrap my-3">
            <exo-user-avatar
              v-for="manager in avatarToDisplay"
              :key="manager"
              :username="manager.username"
              :title="manager.displayName"
              :avatar-url="manager.avatar"
              :size="iconSize"
              :style="'background-image: url('+manager.avatar+')'"
              class="mr-1 projectManagersAvatar"/>
            <div class="seeMoreAvatars">
              <div
                v-if="managerIdentities.length > maxAvatarToShow"
                class="seeMoreItem"
                @click="$root.$emit('displayProjectManagers', managerIdentities)">
                <v-avatar
                  :size="iconSize"
                  :style="'background-image: url('+managerIdentities[maxAvatarToShow].avatar+')'"
                  :title="managerIdentities[maxAvatarToShow].displayName"/>
                <span class="seeMoreAvatarList">+{{ showMoreAvatarsNumber }}</span>
              </div>
            </div>
          </div>
        </div>
        <!--<i class="uiIcon uiIconStar"></i>-->
      </div>
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
        iconSize: 28,
        maxAvatarToShow : 5
      }
    },
    computed: {
      avatarToDisplay () {
        if(this.managerIdentities.length > this.maxAvatarToShow) {
          return this.managerIdentities.slice(0, this.maxAvatarToShow-1);
        } else {
          return this.managerIdentities;
        }
      },
      showMoreAvatarsNumber() {
        return this.managerIdentities.length - this.maxAvatarToShow;
      },
      getSpaceName(){
        const str=this.project.space
        return str.substr(0, str.indexOf(/spaces/)-2);
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
      this.$root.$on('update-projects-list-avatar',managerIdentities =>{
        this.managerIdentities=managerIdentities;
      });
    },
    methods : {
      showProjectTasksDetails(project) {
        document.dispatchEvent(new CustomEvent('showProjectTasks', {detail: project}));
      },
      openEditDrawer() {
       this.$emit('openDrawer')
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
                .then(() => this.$emit('projectDeleted'))
                .then(window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest`);
      },
      cloneProject() {
        this.$projectService.cloneProject(this.project)
                .then(() => this.$emit('projectCloned'))
                .then(window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest`)
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
      }
    },

  }
</script>
