<template>
  <v-chip
    :close="canRemoveAttendee"
    class="identitySuggesterItem me-2 mt-2"
    @click:close="$emit('remove-attendee', attendee)">
    <v-avatar left>
      <v-img :src="avatarUrl" />
    </v-avatar>
    <span class="text-truncate">
      {{ displayName }}
    </span>
  </v-chip>
</template>

<script>

export default {
  props: {
    attendee: {
      type: Object,
      default: () => ({}),
    },
    project: {
      type: Object,
      default: () => ({}),
    },
    removable: {
      type: Boolean,
      default: true,
    }
  },
  data () {
    return {
      isExternal: false,
    };
  },
  computed: {
    canRemoveAttendee() {
      if (this.removable===false){
        return false;
      }
      if (this.project.spaceDetails && this.project.spaceDetails.prettyName) {
        return this.attendee.id !== `space:${this.project.spaceDetails.prettyName}`;
      } else {
        return true;
      }
    },
    avatarUrl() {
      const profile = this.attendee && (this.attendee.profile || this.attendee.space);
      return profile && (profile.avatarUrl || profile.avatar);
    },
    displayName() {
      const profile = this.attendee && (this.attendee.profile || this.attendee.space);
      const fullName = profile && (profile.displayName || profile.fullname || profile.fullName);
      return this.isExternal && fullName.indexOf(this.$t('label.external')) < 0 ? `${fullName} (${this.$t('label.external')})` : fullName;
    },
  },
  created() {
    if (this.attendee.providerId === 'organization'){
      this.$userService.getUser(this.attendee.remoteId)
        .then(user => {
          this.isExternal = user.external === 'true';
        });
    }
  },
};
</script>
