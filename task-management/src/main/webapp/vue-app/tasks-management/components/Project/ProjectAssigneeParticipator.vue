<template>
  <v-flex class="user-suggester">
    <exo-identity-suggester
      ref="invitedAttendeeAutoComplete"
      v-model="invitedAttendee"
      :labels="participantSuggesterLabels"
      :search-options="{currentUser: ''}"
      :ignore-items="ignoredMembers"
      name="inviteAttendee"
      type-of-relations="''"
      include-users
      include-spaces />
    <div v-if="participator" class="identitySuggester no-border mt-0">
      <project-event-form-assignee-and-participator-item
        v-for="attendee in participator"
        :key="attendee.id"
        :attendee="attendee"
        @remove-attendee="removeAttendee" />
    </div>
  </v-flex>
</template>



<script>
  export default {
    props: {
      participator: {
        type: Array,
        default: () => [],
      },
    },
    data() {
      return {
        currentUser: null,
        invitedAttendee: [],
      };
    },
    computed: {
      participantSuggesterLabels() {
        return {
          searchPlaceholder: this.$t('label.searchPlaceholder'),
          placeholder: this.$t('label.inviteManagers'),
          noDataLabel: this.$t('label.noDataLabel'),
        };
      },
      ignoredMembers() {
        if(this.participator){
          return this.participator.map(attendee => `${attendee.providerId}:${attendee.remoteId}`);
        }
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
    methods:{
      reset() {
        if (this.participator && !this.participator.length>0) { // In case of edit existing event
          // Add current user as default attendee
          const urlPath = document.location.pathname
          if(urlPath.includes('g/:spaces')) {
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
        this.$root.$emit('task-project-participator',this.participator);
      },
    }
  };
</script>
