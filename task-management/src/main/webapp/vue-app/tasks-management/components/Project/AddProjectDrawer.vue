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
        <div ref="addProjectTitle" class="addProjectTitle d-flex align-center">
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
        <!--<v-divider class="py-3"/>
        <div class="projectPermissionsUsers">
          <p class="permisionLabel body-1">{{ $t('label.permission') }}</p>
          <div class="listOfManager" >
            <a
              v-for="manager in listOfManager" 
              :key="manager.src" 
              href="#" 
              class="flex-nowrap flex-shrink-0 d-flex">
              <div class="v-avatar pull-left my-auto">
                <img :src="manager.src">
              </div>
              <button 
                type="button" 
                class="peopleInfoIcon d-flex not-clickable primary-border-color ml-1 v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--small primary--text" 
                title="Space manager">
                <span class="v-btn__content">
                  <span class="d-flex uiIconMemberAdmin primary&#45;&#45;text"></span>
                </span>
              </button>
            </a>
            <div class="editManager">
              <a href="#" class="editManager">
                <i class="fas fa-pencil-alt uiIconProject"></i>
                {{ $t('label.editManager') }}
              </a>
              <label class="editManagerInfo">
                <i class="uiIconInformation uiIconProject"></i>
                {{ $t('label.editManagerInfo') }}
              </label>
            </div>
          </div>
          <div class="listOfParticipant" >
            <a 
              v-for="participant in listOfParticipant" 
              :key="participant.src" 
              href="#" 
              class="flex-nowrap flex-shrink-0 d-flex">
              <div class="v-avatar pull-left my-auto">
                <img :src="participant.src">
              </div>
            </a>
            <div class="editParticipant">
              <a class="editParticipant" href="#">
                <i class="fas fa-plus"></i>
                {{ $t('label.editParticipant') }}
              </a>
              <label class="editParticipantInfo">
                <i class="uiIconInformation uiIconProject"></i>
                {{ $t('label.editParticipantInfo') }}
              </label>
            </div>
          </div>
        </div>-->
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
            :placeholder="$t('task.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"/>
        </div>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <div class="VuetifyApp">
          <div class="d-btn">
            <v-btn
              class="btn mr-2"
              @click="cancel">
              <template>
                {{ $t('popup.cancel') }}
              </template>
            </v-btn>

            <v-btn
              :disabled="postDisabled"
              :loading="postProject"
              class="btn btn-primary"
              @click="saveProject">
              <template>
                {{ $t('label.save') }}
              </template>
            </v-btn>
          </div>
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
    data() {
      return {
        MESSAGE_MAX_LENGTH:250,
        listOfManager:[{src:"/portal/rest/v1/social/users/default-image/avatar"}],
        listOfParticipant:[{src:"/portal/rest/v1/social/users/default-image/avatar"}],
        activityComposerActions: [],
        projectInformation:null,
        postProject:false,
        project:{}
      };
    },
    computed: {
      postDisabled: function() {
        if(this.projectInformation !== null){
          const pureText = this.projectInformation.description ? this.projectInformation.description.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim() : '';
          return pureText.length> this.MESSAGE_MAX_LENGTH ;
        }
      },
      suggesterLabels() {
        return {
          placeholder: this.$t('label.inviteManagers'),
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
          if ( typeof this.project.id === 'undefined' && this.projectInformation.description!=='') {
            this.projectInformation.description='';
          }
        }
        this.$refs.addProjectDrawer.close();
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
        } else if (this.projectInformation.name.replace(/\s/g, "").length < 3 || this.projectInformation.name.replace(/\s/g, "").length > 200) {
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
            manager: eXo.env.portal.userName
          };
          if (typeof projects.id !== 'undefined') {
            this.postProject = true;
            return this.$projectService.updateProjectInfo(projects).then(project => {
              this.$emit('update-cart', project);
              this.$root.$emit('update-projects-list', {})
              this.postProject = false;
              this.$refs.addProjectDrawer.close();
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
            })
                    .catch(e => {
                      console.debug("Error saving project", e);
                      this.$emit('error', e && e.message ? e.message : String(e));
                      this.postProject = false;
                    });
          }

        }
      }
    }
  }
</script>
