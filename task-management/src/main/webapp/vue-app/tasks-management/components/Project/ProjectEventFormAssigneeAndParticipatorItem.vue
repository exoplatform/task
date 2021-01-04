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
import {getUser} from '../../../../js/tasksService'

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
   data () {
      return {
        isExternal : false,
      }
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
      const fullName = profile && (profile.displayName || profile.fullname || profile.fullName);
      return this.isExternal ? fullName.concat(' (').concat(this.$t('label.external')).concat(')') : fullName;
    },
  },
  created() {
    getUser(this.attendee.id.replace('organization:', ''))
      .then(user => {
        this.isExternal = user.external === 'true';
      });
  },
};
</script>
