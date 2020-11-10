<template>
  <v-flex class="user-suggester">
    <exo-identity-suggester
      ref="invitedAttendeeAutoComplete"
      v-model="invitedAttendee"
      :labels="participantSuggesterLabels"
      :search-options="{currentUser: ''}"
      :ignore-items="ignoredMembers"
      name="inviteAttendee"
      include-users
      include-spaces />
    <div v-if="manager" class="identitySuggester no-border mt-0">
      <task-event-form-assignee-item
        v-for="attendee in manager"
        :key="attendee.id"
        :attendee="attendee"
        :creator="manager.creator"
        @remove-attendee="removeAttendee" />
    </div>
  </v-flex>
</template>



<script>
  export default {
    props: {
      value: {
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
          searchPlaceholder: this.$t('agenda.searchPlaceholder'),
          placeholder: this.$t('agenda.addParticipants'),
          noDataLabel: this.$t('agenda.noDataLabel'),
        };
      },
      ignoredMembers() {
        if(this.value){
          return this.value.map(attendee => `${attendee.providerId}:${attendee.remoteId}`);
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
        if (!this.value) {
          this.value = [];
        }

        const found = this.value.find(attendee => {
          return attendee.remoteId === this.invitedAttendee.remoteId
                  && attendee.providerId === this.invitedAttendee.providerId;
        });
        if (!found) {
          this.value.push(
             this.$suggesterService.convertSuggesterItemToIdentity(this.invitedAttendee),
          );
        }
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
        if (!this.value || !this.value.length) { // In case of edit existing event
          // Add current user as default attendee
          if (this.currentUser) {
            this.value = [ {
              id: eXo.env.portal.userIdentityId,
              providerId: 'organization',
              remoteId: eXo.env.portal.userName,
              profile: {
                avatar: this.currentUser.avatar,
                fullName: this.currentUser.fullname,
              },
            }];
          } else {
            this.value = [];
          }
        }
        this.$emit('initialized');
      },
      removeAttendee(attendee) {
        const index = this.value.findIndex(addedAttendee => {
          return attendee.remoteId === addedAttendee.remoteId
                  && attendee.providerId === addedAttendee.providerId;
        });
        if (index >= 0) {
          this.value.splice(index, 1);
        }
      },
    }
  };
</script>
