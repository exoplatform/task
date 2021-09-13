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
          class="addProjectTitle d-flex align-center my-3">
          <i class="uiIcon uiIconProject"></i>
          <input
            ref="autoFocusInput1"
            v-model="projectInformation.name"
            :placeholder="$t('label.label.titleProject')"
            type="text"
            class="projectInputTitle ps-3 pt-1"
            single-line
            @change="resetCustomValidity">
        </div>
        <v-divider class="my-4" />
        <div class="projectPermissionsUsers">
          <div class="listOfManager">
            <div class="editManager">
              <div class="permisionLabel body-1"> {{ $t('label.projectManagers') }}</div>
              <label class="editManagerInfo">
                <i class="uiIconInformation uiIconProject"></i>
                {{ $t('label.editManagerInfo') }}
              </label>
              <project-assignee-manager
                ref="projectManager"
                :project="project"
                :manager="manager"
                @managerAssignmentsOpened="closeParticipantAssignement" />
            </div>
          </div>
          <v-divider class="my-4" />
          <div class="listOfParticipant">
            <div class="editParticipant">
              <div class="permisionLabel body-1"> {{ $t('label.projectParticipants') }}</div>
              <label class="editParticipantInfo">
                <i class="uiIconInformation uiIconProject"></i>
                {{ $t('label.editParticipantInfo') }}
              </label>
              <project-assignee-participator
                ref="projectParticipator"
                :project="project"
                :participator="participator"
                @participatorAssignmentsOpened="closeManagerAssignement" />
            </div>
          </div>
        </div>
        <hr>
        <div class="projectDescription">
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
            @add-label="addLabelOnCreate"
            @edit-label-on-create="editLabelBeforeCreate" />
        </div>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
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
    editLabelBeforeCreate(label){
      const objIndex = this.labelsToAdd.findIndex((obj => obj.name === label.name));
      this.labelsToAdd[objIndex].name = label.text;
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
            this.manager = this.project.managersDetail;
          }
          if (this.project && this.project.participator && this.project.participator.length ){
            this.participator = this.project.participatorsDetail;
          }
          this.$refs.addProjectDrawer.open();
          window.setTimeout(() => this.$refs.addProjectTitle.querySelector('input').focus(), 200);

        });
      } else {
        this.project=project;
        this.project.spaceName=eXo.env.portal.spaceName;
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
      this.$refs.projectManager.showManager_(false);
      this.$refs.projectParticipator.showParticipant_(false);
    },
    closeParticipantAssignement(){
      this.$refs.projectParticipator.showParticipant_(false);
    },
    closeManagerAssignement(){
      this.$refs.projectManager.showManager_(false);
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
                projects.spaceName = eXo.env.portal.spaceName;
                projects.manager.push(`manager:/spaces/${manager_el.remoteId}`);
                projects.participator.push(`member:/spaces/${manager_el.remoteId}`);
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
          projects.spaceName=eXo.env.portal.spaceName;
        }

        if (this.participator && this.participator.length) {
          if (this.participator.filter(e => e.providerId !== 'space').length > 0) {
            this.participator.forEach(participator_el => {
              if (participator_el.providerId !=='space') {
                projects.participator.push(participator_el.remoteId);
              } else {
                projects.participator.push(`member:/spaces/${participator_el.remoteId}`);
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
            this.$refs.projectManager.showManager_(false);
            this.$refs.projectParticipator.showParticipant_(false);
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
            this.$refs.projectManager.showManager_(false);
            this.$refs.projectParticipator.showParticipant_(false);
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
  }
};
</script>
