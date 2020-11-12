<template>
  <v-chip
    :close="canRemoveAttendee"
    class="identitySuggesterItem mr-4 mt-4"
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
    creator: {
      type: Object,
      default: () => ({}),
    },
  },
  computed: {
    canRemoveAttendee() {
      if (this.creator && this.creator.id) {
        return Number(this.attendee.id) !== Number(this.creator.id);
      } else {
        return Number(this.attendee.id) !== Number(eXo.env.portal.userIdentityId);
      }
    },
    avatarUrl() {
      const profile = this.attendee && (this.attendee.profile || this.attendee.space);
      return profile && (profile.avatarUrl || profile.avatar);
    },
    displayName() {
      const profile = this.attendee && (this.attendee.profile || this.attendee.space);
      return profile && (profile.displayName || profile.fullname || profile.fullName);
    },
  },
};
</script>
