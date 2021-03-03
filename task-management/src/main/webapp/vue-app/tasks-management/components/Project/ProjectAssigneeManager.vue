<template>
  <v-flex class="user-suggester">
    <exo-identity-suggester
      ref="invitedAttendeeAutoComplete"
      v-model="invitedAttendee"
      :labels="participantSuggesterLabels"
      :search-options="searchOptions"
      :ignore-items="ignoredMembers"
      :type-of-relations="relationsType"
      name="inviteAttendee"
      include-users
      include-spaces />
    <div v-if="manager" class="identitySuggester no-border mt-0">
      <project-event-form-assignee-and-participator-item
        v-for="attendee in manager"
        :key="attendee.id"
        :attendee="attendee"
        @remove-attendee="removeAttendee" />
    </div>
  </v-flex>
</template>



<script>
export default {
  props: {
    manager: {
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
      invitedAttendee: []
    };
  },
  computed: {
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
        placeholder: this.$t('label.inviteManagers'),
        noDataLabel: this.$t('label.noDataLabel'),
      };
    },
    ignoredMembers() {
      if (this.manager){
        return this.manager.map(attendee => `${attendee.providerId}:${attendee.remoteId}`);
      }
      return '';
    },
  },
  watch: {
    currentUser() {
      this.reset();
    },
    invitedAttendee() {
      if (!this.invitedAttendee) {
        this.$nextTick(this.$refs.invitedAttendeeAutoComplete.$refs.selectAutoComplete.deleteCurrentItem);
        return;
      }
      if (!this.manager) {
        this.manager = [];
      }

      const found = this.manager.find(attendee => {
        return attendee.remoteId === this.invitedAttendee.remoteId
                  && attendee.providerId === this.invitedAttendee.providerId;
      });
      if (!found) {
        this.manager.push(
          this.$suggesterService.convertSuggesterItemToIdentity(this.invitedAttendee),
        );
      }
      this.$root.$emit('task-project-manager',this.manager);
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
    reset() {
      if (this.manager && !this.manager.length>0) { // In case of edit existing event
        // Add current user as default attendee
        const urlPath = document.location.pathname;
        if (urlPath.includes('g/:spaces')) {
          this.manager = [{
            id: `space:${eXo.env.portal.spaceName}`,
            providerId: 'space',
            remoteId: eXo.env.portal.spaceGroup,
            profile: {
              avatar: `/portal/rest/v1/social/spaces/${eXo.env.portal.spaceName}/avatar`,
              fullname: eXo.env.portal.spaceDisplayName,
              originalName: eXo.env.portal.spaceGroup,
            },
          }];
        } else {
          if (this.currentUser) {
            this.manager = [{
              id: `organization:${eXo.env.portal.userIdentityId}`,
              providerId: 'organization',
              remoteId: eXo.env.portal.userName,
              profile: {
                avatar: this.currentUser.avatar,
                fullname: this.currentUser.fullname,
              },
            }];
          } else {
            this.manager = [];
          }
        }
      }
      this.$refs.invitedAttendeeAutoComplete.focus();
      this.$emit('initialized');
    },
    removeAttendee(attendee) {
      const index = this.manager.findIndex(addedAttendee => {
        return attendee.remoteId === addedAttendee.remoteId
                  && attendee.providerId === addedAttendee.providerId;
      });
      if (index >= 0) {
        this.manager.splice(index, 1);
      }
      this.$root.$emit('task-project-manager',this.manager);
    },
  }
};
</script>
