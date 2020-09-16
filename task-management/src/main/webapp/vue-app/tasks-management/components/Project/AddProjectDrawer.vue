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
          :label="$t('label.label.titleProject')"
          class="projectInputTitle pl-3 pt-1"
          single-line/>
      </div>
      <v-divider class="py-3"/>
      <div class="projectPermissionsUsers">
        <p class="permisionLabel body-1">{{ $t('label.permission') }}</p>
        <exo-identity-suggester
          ref="autoFocusInput3"
          :labels="suggesterLabels"
          name="inviteManagers"
          type-of-relations="user_to_invite"
          height="100"
          include-users
          include-spaces
          multiple/>
      </div>
      <v-divider class="py-3"/>
      <div class="projectDescription">
        <v-label
          for="description">
          {{ $t('label.description') }}
        </v-label>
        <textarea
          name="description"
          rows="20"
          maxlength="2000"
          noresize
          class="input-block-level ignore-vuetify-classes my-3" >
        </textarea>
      </div>
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
        this.$refs.AddProjectDrawer.open();
      },
      cancel() {
        this.$refs.AddProjectDrawer.close();
      },
    }
  }
</script>