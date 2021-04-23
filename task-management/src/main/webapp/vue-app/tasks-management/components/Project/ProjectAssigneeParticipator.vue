<template>
  <v-flex class="user-suggester">
    <div v-if="participator" class="identitySuggester no-border mt-0">
      <div v-if="spaces.length">
        <div class="mt-4">{{ $t('label.spaceMembers') }}</div>
        <project-event-form-assignee-and-participator-item
          v-for="attendee in spaces"
          :key="attendee.id"
          :attendee="attendee"
          :project="project"
          :removable="participator.length>1"
          @remove-attendee="removeAttendee" />
      </div>
      <div v-if="spaces.length && users.length" class="mt-2 font-weight-bold">{{ $t('label.and') }}</div>
      <div v-if="users.length">
        <project-event-form-assignee-and-participator-item
          v-for="attendee in users"
          :key="attendee.id"
          :attendee="attendee"
          :project="project"
          :removable="participator.length>1"
          @remove-attendee="removeAttendee" />
      </div>
    </div>
    <div
      v-show="!showParticipant && canEditParticipant"
      class="editParticipant"
      @click="showParticipant = true">
      <a class="editParticipant" href="#">
        <i class="fas fa-plus"></i>
        {{ $t('label.editParticipant') }}
      </a>
    </div>
    <exo-identity-suggester
      v-show="showParticipant && canEditParticipant"
      ref="invitedAttendeeAutoComplete"
      v-model="invitedAttendee"
      :labels="participantSuggesterLabels"
      :search-options="searchOptions"
      :ignore-items="ignoredMembers"
      :type-of-relations="relationsType"
      name="inviteAttendee"
      include-users
      :include-spaces="canEditParticipant" />
  </v-flex>
</template>



<script>
export default {
  props: {
    participator: {
      type: Array,
      default: () => [],
    },
    project: {
      type: Object,
      default: () => ({}),
    }
  },
  data() {
    return {
      currentUser: null,
      invitedAttendee: [],
      users: [],
      spaces: [],
      showParticipant: false,
    };
  },
  computed: {
    canEditParticipant() {
      return !this.project.spaceDetails;
    },
    searchOptions(){
      if (this.project.spaceDetails){
        return {
          spaceURL: this.project.spaceDetails.prettyName,
          currentUser: this.currentUser
        };
      }
      return this.currentUser;
    },
    relationsType(){
      if (this.project.spaceDetails){
        return 'member_of_space';
      }
      return '';
    },
    participantSuggesterLabels() {
      return {
        searchPlaceholder: this.$t('label.searchPlaceholder'),
        placeholder: this.$t('label.inviteParticipant'),
        noDataLabel: this.$t('label.noDataLabel'),
      };
    },
    ignoredMembers() {
      if (this.participator){
        return this.participator.map(attendee => `${attendee.providerId}:${attendee.remoteId}`);
      }
      return '';
    },
  },
  watch: {
    currentUser() {
      this.reset();
    },
    project() {
      this.reset();
    },
    invitedAttendee() {
      if (!this.invitedAttendee) {
        this.$nextTick(this.$refs.invitedAttendeeAutoComplete.$refs.selectAutoComplete.deleteCurrentItem);
        return;
      }
      if (!this.participator) {
        this.participator = [];
      }

      const found = this.participator.find(attendee => {
        return attendee.remoteId === this.invitedAttendee.remoteId
                  && attendee.providerId === this.invitedAttendee.providerId;
      });
      if (!found) {
        this.participator.push(
          this.$suggesterService.convertSuggesterItemToIdentity(this.invitedAttendee),
        );
      }
      this.spaces = this.participator.filter(attendee => attendee.providerId === 'space');
      this.users = this.participator.filter(attendee => attendee.providerId === 'organization');
      this.$root.$emit('task-project-participator',this.participator);
      this.invitedAttendee = null;
    },
  },
  created() {
    this.reset();
  },
  mounted(){
    this.$userService.getUser(eXo.env.portal.userName).then(user => {
      this.currentUser = user;
    });
  },
  methods: {
    showParticipant_(e) {
      this.showParticipant=e;
    },
    reset() {
      if (this.participator && !this.participator.length>0) { // In case of edit existing event
        // Add current user as default attendee
        const urlPath = document.location.pathname;
        if (urlPath.includes('g/:spaces')) {
          this.participator = [{
            id: `space:${eXo.env.portal.spaceName}`,
            providerId: 'space',
            remoteId: eXo.env.portal.spaceName,
            profile: {
              avatar: `/portal/rest/v1/social/spaces/${eXo.env.portal.spaceName}/avatar`,
              fullname: eXo.env.portal.spaceDisplayName,
            },
          }];
        } else {
          if (this.currentUser) {
            this.participator = [{
              id: `organization:${eXo.env.portal.userIdentityId}`,
              providerId: 'organization',
              remoteId: eXo.env.portal.userName,
              profile: {
                avatar: this.currentUser.avatar,
                fullname: this.currentUser.fullname,
              },
            }];
          } else {
            this.participator = [];
          }
        }
      }
      this.spaces = this.participator.filter(attendee => attendee.providerId === 'space');
      this.users = this.participator.filter(attendee => attendee.providerId === 'organization');
      this.$refs.invitedAttendeeAutoComplete.focus();
      this.$emit('initialized');
    },
    removeAttendee(attendee) {
      const index = this.participator.findIndex(addedAttendee => {
        return attendee.remoteId === addedAttendee.remoteId
                  && attendee.providerId === addedAttendee.providerId;
      });
      if (index >= 0) {
        this.participator.splice(index, 1);
      }
      this.spaces = this.participator.filter(attendee => attendee.providerId === 'space');
      this.users = this.participator.filter(attendee => attendee.providerId === 'organization');
      this.$root.$emit('task-project-participator',this.participator);
    },
  }
};
</script>
