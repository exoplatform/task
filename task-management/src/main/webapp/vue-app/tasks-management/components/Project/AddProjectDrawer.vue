<template>
  <exo-drawer
    ref="addProjectDrawer"
    class="addProjectDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right
    @closed="cancel">
    <template slot="title">
      {{ labelDrawer }}
    </template>
    <template slot="content">
      <v-form 
        v-if="projectInformation"
        ref="taskEventForm"
        class="flex"
        flat>
        <div 
          ref="addProjectTitle" 
          class="addProjectTitle d-flex align-center my-3"
          @click="showManager = true,showParticipant = true">
          <i class="uiIcon uiIconProject"></i>
          <input
            ref="autoFocusInput1"
            v-model="projectInformation.name"
            :placeholder="$t('label.label.titleProject')"
            type="text"
            class="projectInputTitle pl-3 pt-1"
            single-line
            @change="resetCustomValidity">
        </div>
        <v-divider class="py-3" />
        <div class="projectPermissionsUsers">
          <div @click="showManager = true,showParticipant = true">
            <p class="permisionLabel body-1">{{ $t('label.permission') }}</p>
          </div>
          <div class="listOfManager" @click="showParticipant = true">
            <div class="listOfManageravatar">
              <a
                v-for="managerAvatar in avatarManagerToDisplay"
                :key="managerAvatar.avatar"
                href="#"
                class="flex-nowrap flex-shrink-0 d-flex">
                <div class="v-avatar pull-left my-auto">
                  <img :src="managerAvatar.avatar">
                </div>
                <button
                  type="button"
                  class="peopleInfoIcon d-flex not-clickable primary-border-color ml-1 v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--small primary--text"
                  title="Space manager">
                  <span class="v-btn__content">
                    <span class="d-flex uiIconMemberAdmin primary--text"></span>
                  </span>
                </button>
              </a>
              <div class="seeMoreAvatars">
                <div
                  v-if="listOfManager.length > maxAvatarToShow"
                  class="seeMoreItem"
                  @click="$root.$emit('displayTasksAssigneeAndCoworker', listOfManager)">
                  <v-avatar
                    :size="iconSize">
                    <img
                      :src="listOfManager[maxAvatarToShow].avatar"
                      :title="listOfManager[maxAvatarToShow].displayName">
                  </v-avatar>
                  <span class="seeMoreAvatarList">+{{ showMoreAvatarsNumber }}</span>
                </div>
              </div>
            </div>

            <div class="editManager">
              <div
                v-show="showManager"
                class="editManager"
                @click="showManager = false">
                <a href="#" class="editManager">
                  <i class="fas fa-pencil-alt uiIconProject"></i>
                  {{ $t('label.editManager') }}
                </a>
                <label class="editManagerInfo">
                  <i class="uiIconInformation uiIconProject"></i>
                  {{ $t('label.editManagerInfo') }}
                </label>
              </div>
              <project-assignee-manager
                v-if="!showManager"
                ref="assigneeManager"
                :project="project"
                :manager="manager" />
            </div>
          </div>
          <div class="listOfParticipant" @click="showManager = true">
            <div class="listOfParticipantavatar">
              <a
                v-for="participant in avatarParticipantToDisplay"
                :key="participant.avatar"
                href="#"
                class="flex-nowrap flex-shrink-0 d-flex">
                <div class="v-avatar pull-left my-auto">
                  <img :src="participant.avatar">
                </div>
              </a>
              <div class="seeMoreAvatars">
                <div
                  v-if="listOfParticipant.length > maxAvatarToShow"
                  class="seeMoreItem">
                  <v-avatar
                    :size="iconSize">
                    <img
                      :src="listOfParticipant[maxAvatarToShow].avatar"
                      :title="listOfParticipant[maxAvatarToShow].displayName">
                  </v-avatar>
                  <span class="seeMoreAvatarList">+{{ showMoreAvatarsParticipantNumber }}</span>
                </div>
              </div>
            </div>
            <div class="editParticipant">
              <div
                v-if="showParticipant"
                class="editParticipant"
                @click="showParticipant = false">
                <a class="editParticipant" href="#">
                  <i class="fas fa-plus"></i>
                  {{ $t('label.editParticipant') }}
                </a>
                <label class="editParticipantInfo">
                  <i class="uiIconInformation uiIconProject"></i>
                  {{ $t('label.editParticipantInfo') }}
                </label>
              </div>
              <project-assignee-participator
                v-if="!showParticipant"
                :project="project"
                :participator="participator" />
            </div>
          </div>
        </div>
        <hr>
        <div class="projectDescription" @click="showManager = true,showParticipant = true">
          <v-label
            for="description">
            {{ $t('label.description') }}
          </v-label>
          <exo-task-editor
            ref="richEditor"
            v-model="projectInformation.description"
            :max-length="MESSAGE_MAX_LENGTH"
            :id="project.id"
            :placeholder="$t('task.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)" />
        </div>
        <v-divider class="py-3" />
        <div class="projectLabelsName mt-3 mb-3">
          <v-label
            for="labels">
            {{ $t('label.labels') }}
          </v-label>
          <project-labels
            :project="project"
            @add-label="addLabelOnCreate" />
        </div>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn mr-2"
          @click="cancel">
          {{ $t('popup.cancel') }}
        </v-btn>
        <v-btn
          :disabled="postDisabled"
          :loading="postProject"
          class="btn btn-primary"
          @click="saveProject">
          {{ $t('label.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data() {
    return {
      MESSAGE_MAX_LENGTH: 255,
      defaultParticipant: [{avatar: '/portal/rest/v1/social/users/default-image/avatar'}],
      listOfManager: [],
      listOfParticipant: [],
      activityComposerActions: [],
      projectInformation: null,
      manager: [],
      participator: [],
      postProject: false,
      project: {},
      maxAvatarToShow: 3,
      showManager: true,
      showParticipant: true,
      labelsToAdd: [],
    };
  },
  computed: {
    postDisabled: function() {
      if (this.projectInformation !== null){
        let pureText = this.projectInformation.description ? this.projectInformation.description.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
        const div = document.createElement('div');
        div.innerHTML = pureText;
        pureText = div.textContent || div.innerText || '';          
        return pureText.length> this.MESSAGE_MAX_LENGTH ;
      }
      return '';
    },

    avatarParticipantToDisplay () {
      this.getProjectParticipant();
      if (this.listOfParticipant.length > this.maxAvatarToShow) {
        return this.listOfParticipant.slice(0, this.maxAvatarToShow - 1);
      } else if (this.listOfParticipant.length>0){
        return this.listOfParticipant;
      } else {
        return this.defaultParticipant;
      }
    },
    currentUserAvatar() {
      const urlAvatar = `/rest/v1/social/users/${eXo.env.portal.userName}/avatar`;
      return [{avatar: urlAvatar}];
    },
    avatarManagerToDisplay () {
      this.getProjectManagers();
      if (this.listOfManager.length > this.maxAvatarToShow) {
        return this.listOfManager.slice(0, this.maxAvatarToShow - 1);
      } else if (this.listOfManager.length>0){
        return this.listOfManager;
      } else {
        return this.currentUserAvatar;
      }

    },
    showMoreAvatarsNumber() {
      return this.listOfManager.length - this.maxAvatarToShow;
    },
    showMoreAvatarsParticipantNumber() {
      return this.listOfParticipant.length - this.maxAvatarToShow;
    },
    labelDrawer(){
      if (typeof this.project.id !== 'undefined'){
        return this.$t('label.editProject');
      }
      else {
        return this.$t('label.addProject');
      }
    }
  },
  created() {
    this.$root.$on('task-project-manager',manager =>{
      this.manager=manager;
    });
    this.$root.$on('task-project-participator',participator =>{
      this.participator=participator;
    });
  },
 
  methods: {
    addLabelOnCreate(label){
      this.labelsToAdd.push(label);
    },
    open(project) {
      if (project && project.id){
        return this.$projectService.getProject(project.id,true).then(project => {
          this.project=project;
          this.manager=[];
          this.participator=[];
          this.projectInformation={
            name: '',
            description: '',
            id: '',
          };    
          window.setTimeout(() => {
            document.dispatchEvent(new CustomEvent('loadAllProjectLabels', {
              detail: this.project
            }));
          },
          200);
          if (this.project.name !== null ||this.project.name !==''){
            this.projectInformation.name=this.project.name;
          }
          if (this.project.id !== null ||this.project.id !==''){
            this.projectInformation.id=this.project.id;
          }
          if (this.project.description !== null || this.project.description !== ''){
            this.projectInformation.description = this.project.description;
          }
          if (this.project && this.project.manager){
            this.manager = this.project.manager;
            const managerIdentity = this.project.managerIdentities;
            if (this.project && this.project.space) {
              this.manager = this.manager.map((item) => {
                if (item.includes('manager:/spaces/')) {
                  const spacePrettyName = item.substr((item.indexOf(/spaces/)+8)).slice(0,item.length);
                  const spaceFullName = this.project.space.substr(0, this.project.space.indexOf(/spaces/)-2);
                  return {
                    id: `space:${spacePrettyName}`,
                    remoteId: spacePrettyName,
                    providerId: 'space',
                    profile: {
                      fullName: spaceFullName,
                      originalName: spacePrettyName,
                      avatarUrl: `/portal/rest/v1/social/spaces/${spacePrettyName}/avatar`,
                    },
                  };
                } else {
                  const managerIdentityElement = managerIdentity.filter(element => element.username === item);
                  return {
                    id: `organization:${item}`,
                    remoteId: item,
                    providerId: 'organization',
                    profile: {
                      fullName: managerIdentityElement[0].displayName,
                      avatarUrl: managerIdentityElement[0].avatar,
                    },
                  };
                }
              });
            } else {
              this.manager = managerIdentity.map(user => ({
                id: `organization:${user.username}`,
                remoteId: user.username,
                providerId: 'organization',
                profile: {
                  fullName: user.displayName,
                  avatarUrl: user.avatar,
                },
              }));
            }
          }

          if (this.project && this.project.participator && this.project.participator.length ){
            this.participator = this.project.participator;
            const participatorIdentity = this.project.participatorIdentities;
            if (this.project && this.project.space) {
              this.participator = this.participator.map((item) => {
                if (item.includes('member:/spaces/')) {
                  const spacePrettyName = item.substr((item.indexOf(/spaces/)+8)).slice(0,item.length);
                  const spaceFullName = this.project.space.substr(0, this.project.space.indexOf(/spaces/)-2);
                  return {
                    id: `space:${spacePrettyName}`,
                    remoteId: spacePrettyName,
                    providerId: 'space',
                    profile: {
                      fullName: spaceFullName,
                      originalName: spacePrettyName,
                      avatarUrl: `/portal/rest/v1/social/spaces/${spacePrettyName}/avatar`,
                    },
                  };
                } else {
                  const participatorIdentityElement = participatorIdentity.filter(element => element.username === item);
                  return {
                    id: `organization:${item}`,
                    remoteId: item,
                    providerId: 'organization',
                    profile: {
                      fullName: participatorIdentityElement[0].displayName,
                      avatarUrl: participatorIdentityElement[0].avatar,
                    },
                  };
                }
              });
            } else {
              this.participator = participatorIdentity.map(user => ({
                id: `organization:${user.username}`,
                remoteId: user.username,
                providerId: 'organization',
                profile: {
                  fullName: user.displayName,
                  avatarUrl: user.avatar,
                },
              }));
            }
          }
          this.$refs.addProjectDrawer.open();
          window.setTimeout(() => this.$refs.addProjectTitle.querySelector('input').focus(), 200);

        });
      } else {
        this.project=project;
        this.manager=[];
        this.participator=[];
        this.projectInformation={
          name: '',
          description: '',
          id: '',
        };
        window.setTimeout(() => {
          document.dispatchEvent(new CustomEvent('loadAllProjectLabels', {
            detail: null
          }));
        },
        200);
        this.$refs.addProjectDrawer.open();
        window.setTimeout(() => this.$refs.addProjectTitle.querySelector('input').focus(), 200);
      }
    },
    cancel() {
      if (this.project !== {}){
        this.projectInformation.name =this.project.name;
        this.projectInformation.description = this.project.description;
        this.manager=this.project.manager;
        this.participator=this.project.participator;
        if ( typeof this.project.id === 'undefined' && this.projectInformation.description!=='') {
          this.projectInformation.description='';
        }
      }
      this.$refs.addProjectDrawer.close();
      this.showManager=true;
      this.showParticipant=true;
    },
    resetCustomValidity() {
      if (this.$refs.autoFocusInput1) {
        this.$refs.autoFocusInput1.setCustomValidity('');
      }
    },
    validateForm() {
      this.resetCustomValidity();
      if (!this.projectInformation.name) {
        this.$refs.autoFocusInput1.setCustomValidity(this.$t('task.message.missingTitle'));
      } else if (this.projectInformation.name.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim().length < 3 || this.projectInformation.name.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim().length > 200) {
        this.$refs.autoFocusInput1.setCustomValidity(this.$t('task.message.missingLengthTitle'));
      }
      if (!this.$refs.taskEventForm.validate() // Vuetify rules
                || !this.$refs.taskEventForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }
      return true;
    },
    saveProject() {
      if (this.validateForm()) {
        const projects = {
          id: this.projectInformation.id,
          name: this.projectInformation.name,
          description: this.projectInformation.description,
          manager: [],
          participator: [],
        };
        if ( this.manager && this.manager.length ) {
          if (this.manager.filter(e => e.providerId === 'space').length > 0) {
            this.manager.forEach(manager_el => {
              if (manager_el.providerId ==='space') {
                projects.spaceName=manager_el.profile.fullName;
                projects.manager.push(`manager:/spaces/${manager_el.profile.originalName}`);
                projects.participator.push(`member:/spaces/${manager_el.profile.originalName}`);
              } else {
                projects.manager.push(manager_el.remoteId);
              }
            });
          } else {
            this.manager.forEach(user => {
              projects.manager.push(user.remoteId);
            });
          }
        } else {
          const urlPath = document.location.pathname;
          if (urlPath.includes('g/:spaces')) {
            const spaceName =urlPath.split(':')[2].split('/')[1];
            projects.spaceName=spaceName;
          }
        }

        if (this.participator && this.participator.length) {
          if (this.participator.filter(e => e.providerId !== 'space').length > 0) {
            this.participator.forEach(participator_el => {
              if (participator_el.providerId !=='space') {
                projects.participator.push(participator_el.remoteId);
              }
            });
          }
        }
        const managers = this.manager;
        if (typeof projects.id !== 'undefined' && projects.id!== '') {
          this.postProject = true;
          return this.$projectService.updateProjectInfo(projects).then(project => {
            this.$emit('update-cart', project);
            this.$root.$emit('update-projects-list', {});
            this.postProject = false;
            this.$refs.addProjectDrawer.close();
            this.showManager=true;
            this.showParticipant=true;
            this.$root.$emit('show-alert',{type: 'success',message: this.$t('alert.success.project.updated')} );
          }).then(() => {
            this.project.managerIdentities = managers.map(user => ({
              avatar: user.profile.avatarUrl,
              displayName: user.profile.fullName || user.profile.fullname,
              username: user.remoteId,
            }));
          }).then(() => {
            this.$root.$emit('update-projects-list-avatar', this.project.managerIdentities);
          })
            .catch(e => {
              console.error('Error updating project', e);
              this.$root.$emit('show-alert',{type: 'error',message: this.$t('alert.error')} );
              this.postProject = false;
            });
        } else {
          return this.$projectService.addProject(projects).then(project => {
            this.labelsToAdd.forEach(item => {
              item.project=project;
              this.$taskDrawerApi.addLabel(item);
            });
            this.$emit('update-cart', project);
            this.$root.$emit('update-projects-list', {});
            this.postProject = false;
            this.$refs.addProjectDrawer.close();
            this.showManager=true;
            this.showParticipant=true;
            this.$root.$emit('show-alert',{type: 'success',message: this.$t('alert.success.project.created')} );
          })
            .catch(e => {
              console.error('Error saving project', e);
              this.$root.$emit('show-alert',{type: 'error',message: this.$t('alert.error')} );
              this.postProject = false;
            });
        }

      }
    },
    getProjectManagers() {
      this.listOfManager=[];
      if (this.project.managerIdentities) {
        this.project.managerIdentities.forEach(user => {
          this.listOfManager.push(user);
        });
      }
    },
    getProjectParticipant() {
      this.listOfParticipant=[];
      if (this.project.participatorIdentities) {
        this.project.participatorIdentities.forEach(user => {
          this.listOfParticipant.push(user);
        });
      }
    },
  }
};
</script>
