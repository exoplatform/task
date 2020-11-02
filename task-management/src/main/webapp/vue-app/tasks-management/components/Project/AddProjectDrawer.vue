<template>
  <exo-drawer
    ref="addProjectDrawer"
    class="addProjectDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
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
          class="addProjectTitle d-flex align-center"
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
        <v-divider class="py-3"/>
        <div class="projectPermissionsUsers">
          <div @click="showManager = true,showParticipant = true">
            <p class="permisionLabel body-1" >{{ $t('label.permission') }}</p>

          </div>
          <div class="listOfManager" @click="showParticipant = true">
            <div class="listOfManageravatar" >
              <a
                v-for="manager in avatarManagerToDisplay"
                :key="manager.avatar"
                href="#"
                class="flex-nowrap flex-shrink-0 d-flex">
                <div class="v-avatar pull-left my-auto">
                  <img :src="manager.avatar">
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

            <div class="editManager" >
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
              <exo-identity-suggester
                v-show="!showManager"
                ref="autoFocusInput3"
                :labels="suggesterLabelsManagers"
                v-model="manager"
                :search-options="{currentUser: ''}"
                name="assignee"
                type-of-relations="user_to_invite"
                include-users
                multiple/>
            </div>
          </div>
          <div class="listOfParticipant" @click="showManager = true" >
            <div class="listOfParticipantavatar" >

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
              <exo-identity-suggester
                v-if="!showParticipant"
                ref="autoFocusInput3"
                :labels="suggesterLabelsParticipant"
                v-model="participator"
                :search-options="{currentUser: ''}"
                name="participant"
                type-of-relations="user_to_invite"
                min-height="37"
                include-users
                multiple/>
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
            :placeholder="$t('task.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"/>
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
        MESSAGE_MAX_LENGTH:255,
        defaultManager:[{avatar:"/portal/rest/v1/social/users/default-image/avatar"}],
        defaultParticipant:[{avatar:"/portal/rest/v1/social/users/default-image/avatar"}],
        listOfManager:[],
        listOfParticipant:[],
        activityComposerActions: [],
        projectInformation:null,
        manager:'',
        participator:'',
        postProject:false,
        project:{},
        maxAvatarToShow : 3,
        showManager: true,
        showParticipant:true
      };
    },
    computed: {
      postDisabled: function() {
        if(this.projectInformation !== null){
          let pureText = this.projectInformation.description ? this.projectInformation.description.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
          const div = document.createElement('div');
          div.innerHTML = pureText;
          pureText = div.textContent || div.innerText || '';          return pureText.length> this.MESSAGE_MAX_LENGTH ;
        }
      },

      avatarParticipantToDisplay () {
        this.getProjectParticipant();
        if (this.listOfParticipant.length > this.maxAvatarToShow) {
          return this.listOfParticipant.slice(0, this.maxAvatarToShow - 1);
        } else if (this.listOfParticipant.length>0){
          return this.listOfParticipant;
        }else {
          return this.defaultParticipant;
        }
      },
      avatarManagerToDisplay () {
        this.getProjectManagers();
        if (this.listOfManager.length > this.maxAvatarToShow) {
          return this.listOfManager.slice(0, this.maxAvatarToShow - 1);
        } else if(this.listOfManager.length>0){
          return this.listOfManager;
        }else {
          return this.defaultManager;
        }

      },
      showMoreAvatarsNumber() {
        return this.listOfManager.length - this.maxAvatarToShow;
      },
      showMoreAvatarsParticipantNumber() {
        return this.listOfParticipant.length - this.maxAvatarToShow;
      },
      suggesterLabelsManagers() {
        return {
          placeholder: this.$t('label.inviteManagers'),
          noDataLabel: this.$t('label.noDataLabel'),
        };
      },
      suggesterLabelsParticipant() {
        return {
          placeholder: this.$t('label.inviteParticipant'),
          noDataLabel: this.$t('label.noDataLabel'),
        };
      },
      labelDrawer(){
        if (typeof this.project.id !== 'undefined'){
          return this.$t('label.editProject');
        }
        else {
          return this.$t('label.addProject')
        }
      }
    },
 
    methods: {
      open(project) { 
        this.project=project
        this.manager=''
        this.participator=''
        this.projectInformation={
          name:'',
          description:'',
          id:'',
        };
            if (this.project.name !== null ||this.project.name !==''){
        this.projectInformation.name=this.project.name;
      }
      if (this.project.id !== null ||this.project.id !==''){
        this.projectInformation.id=this.project.id;
      }
      if (this.project.description !== null || this.project.description !== ''){
        this.projectInformation.description = this.project.description;
      }

      this.$refs.addProjectDrawer.open();
      window.setTimeout(() => this.$refs.addProjectTitle.querySelector('input').focus(), 200);
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
          if (this.manager && this.manager.length) {
            this.manager.forEach(user => {
              projects.manager.push(user.remoteId)
            })
          }

          if (this.participator && this.participator.length) {
            this.participator.forEach(user => {
              projects.participator.push(user.remoteId)
            })
          }

          if (typeof projects.id !== 'undefined') {
            this.postProject = true;
            return this.$projectService.updateProjectInfo(projects).then(project => {
              this.$emit('update-cart', project);
              this.$root.$emit('update-projects-list', {})
              this.postProject = false;
              this.$refs.addProjectDrawer.close();
              this.showManager=true;
            })
                    .catch(e => {
                      console.debug("Error updating project", e);
                      this.$emit('error', e && e.message ? e.message : String(e));
                      this.postProject = false;
                    });
          } else {
            return this.$projectService.addProject(projects).then(project => {
              this.$emit('update-cart', project);
             this.$root.$emit('update-projects-list', {})
             this.postProject = false;
               this.$refs.addProjectDrawer.close();
              this.showManager=true;
            })
                    .catch(e => {
                      console.debug("Error saving project", e);
                      this.$emit('error', e && e.message ? e.message : String(e));
                      this.postProject = false;
                    });
          }

        }
      },
      getProjectManagers() {
        this.listOfManager=[]
        if(this.project.managerIdentities) {
          this.project.managerIdentities.forEach(user => {
            this.listOfManager.push(user)
          })
        }
      },
      getProjectParticipant() {
        this.listOfParticipant=[]
        if(this.project.participatorIdentities) {
          this.project.participatorIdentities.forEach(user => {
            this.listOfParticipant.push(user)
          })
        }
      },
    }
  }
</script>
