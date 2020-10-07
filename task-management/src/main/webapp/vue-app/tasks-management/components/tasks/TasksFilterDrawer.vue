<template>
  <exo-drawer
    ref="filterTasksDrawer"
    class="filterTasksDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('label.filter') }}
    </template>
    <template slot="content">
      <form ref="form1">
        <v-label for="name">
          {{ $t('spacesList.label.name') }}
        </v-label>
        <input
          ref="autoFocusInput1"
          :placeholder="$t('spacesList.label.displayName')"
          type="text"
          name="name"
          class="input-block-level ignore-vuetify-classes my-3"
          required >
        <v-label for="assignee">
          {{ $t('spacesList.label.name') }}
        </v-label>
        <exo-identity-suggester
          ref="autoFocusInput3"
          :labels="suggesterLabels"
          name="assignee"
          type-of-relations="user_to_invite"
          height="100"
          include-users
          include-spaces
          multiple/>
      </form>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
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
        this.$refs.filterTasksDrawer.open();
      },
      cancel() {
        this.$refs.filterTasksDrawer.close();
      }
    }
  }
</script>
