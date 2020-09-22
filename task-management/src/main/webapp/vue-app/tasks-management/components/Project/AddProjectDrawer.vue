<template>
  <exo-drawer
    ref="AddProjectDrawer"
    class="addProjectDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('label.addProject') }}
    </template>
    <template slot="content">
      <div class="addProjectTitle d-flex align-center">
        <i class="uiIcon uiIconProject"></i>
        <v-text-field
          v-model="projectinformation.name"
          :label="$t('label.label.titleProject')"
          class="projectInputTitle pl-3 pt-1"
          single-line/>
      </div>
      <v-divider class="py-3"/>
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
              <i class="fas fa-pencil-alt uiIconProject"></i>
              {{ $t('label.editParticipant') }}
            </a>
            <label class="editParticipantInfo">
              <i class="uiIconInformation uiIconProject"></i>
              {{ $t('label.editParticipantInfo') }}
            </label>
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
          v-model="projectinformation.description"
          :max-length="MESSAGE_MAX_LENGTH"
          :placeholder="$t('task.placeholder').replace('{0}', MESSAGE_MAX_LENGTH)"/>
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn mr-2"
          @click="cancel">
          <template>
            {{ $t('popup.cancel') }}
          </template>
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="saveProject">
          <template>
            {{ $t('label.save') }}
          </template>
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
    data() {
      return {
        MESSAGE_MAX_LENGTH:1000,
        listOfManager:[{src:"/portal/rest/v1/social/users/default-image/avatar"},{src:"/portal/rest/v1/social/users/default-image/avatar"}],
        listOfParticipant:[{src:"/portal/rest/v1/social/users/default-image/avatar"}],
        projectinformation:{
          name:'',
          description:'',
        },
      };
    },
    computed: {
      suggesterLabels() {
        return {
          placeholder: this.$t('label.inviteManagers'),
          noDataLabel: this.$t('label.noDataLabel'),
        };
      }
    },
    methods: {
      open() {
        this.$refs.AddProjectDrawer.open();
      },
      cancel() {
        this.$refs.AddProjectDrawer.close();
      },
      saveProject() {
        const projects = {
          name: this.projectinformation.name,
          description: this.projectinformation.description,
          manager: eXo.env.portal.userName
        };
        return this.$projectService.addProject(projects).then(project=> {
          this.$emit('update-cart', project);
          if(project) {
            window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/taskstest`;
          } else {
            window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}`;
          }
        })
                .catch(e => {
                  console.debug("Error saving project", e);
                  this.$emit('error', e && e.message ? e.message : String(e));
                });
      }
    }
  }
</script>
