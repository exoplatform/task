<template>
  <div @click.stop>
    <v-combobox
      id="labelInput"
      ref="selectLabel"
      v-model="model"
      :filter="filter"
      :hide-no-data="!search"
      :items="items"
      :search-input.sync="search"
      :label="$t('label.tapLabel.name')"
      attach
      class="pt-0 inputProjectLabel"
      hide-selected
      multiple
      small-chips
      prepend-icon
      solo
      @click="openLabelsList()"
      @change="search = ''">
      <template v-slot:prepend class="mr-4">
        <i class="uiIconTag uiIconBlue"></i>
      </template>
      <template v-slot:no-data>
        <v-list-item>
          <span class="subheading">{{ $t('label.createLabel') }}</span>
          <v-chip
            label
            small>
            {{ search }}
          </v-chip>
        </v-list-item>
      </template>
      <template v-slot:selection="{ attrs, item, parent, selected }">
        <editable-labels
          :item="item"
          :parent="parent"
          @edit-label-on-create="editLabelBeforeCreate"
          @remove-label="removeLabel" />
      </template>
    </v-combobox>
  </div>
</template>

<script>
export default {
  props: {
    project: {
      type: Object,
      default: () => {
        return {};
      }
    },
  },
  data() {
    return {
      index: -1,
      items: [],
      nonce: 1,
      model: [],
      x: 0,
      search: null,
      y: 0,
    };
  },
  watch: {
    model(val, prev) {
      if (val.length === prev.length) {
        this.search = null;
        return;
      }
      this.model = val.map(v => {
        if (typeof v === 'string') {
          v = {
            text: v,
            name: v,
          };
          this.items.push(v);
          this.nonce++;
          this.addLabel(v);
        }
        return v;
      });
    },

  },
  created() {
    $(document).on('mousedown', () => {
      if (this.$refs.selectLabel && this.$refs.selectLabel.isMenuActive) {
        window.setTimeout(() => {
          this.$refs.selectLabel.isMenuActive = false;
        }, 200);
      }
    });
    document.addEventListener('closeLabelsList',()=> {
      setTimeout(() => {
        if (typeof this.$refs.selectLabel !== 'undefined') {
          this.$refs.selectLabel.isMenuActive = false;
        }
      }, 100);
    });
    document.addEventListener('loadAllProjectLabels', event => {
      this.model = [];
      if (event && event.detail) {
        const project = event.detail;
        this.model = [];
        this.getProjectLabels(project.id);
      }
    });
  },
  methods: {
    filter(item, queryText, itemText) {
      if (item.header) {
        return false;
      }
      const hasValue = function (val) {
        return val != null ? val : '';
      };
      const text = hasValue(itemText);
      const query = hasValue(queryText);
      return text.toString().toLowerCase().indexOf(query.toString().toLowerCase()) > -1;
    },

    getProjectLabels(projectId) {
      this.$taskDrawerApi.getProjectLabels(projectId).then((labels) => {
        this.model = labels.map(function (el) {
          const o = Object.assign({}, el);
          o.text = o.name;
          o.editMenu=false;
          return o;
        });
      });
    },

    addLabel(label) {
      if ( this.project.id!= null ) {
        label.project=this.project;
        this.$taskDrawerApi.addLabel(label).then( () => {
          this.$root.$emit('show-alert', {
            type: 'success',
            message: this.$t('alert.success.label.created')
          });
          this.getProjectLabels(this.project.id);
        }).catch(e => {
          console.error('Error when adding labels', e);
          this.$root.$emit('show-alert', {
            type: 'error',
            message: this.$t('alert.error')
          });
        });
      } else {
        this.$emit('add-label', label);
      }
      this.model.push(label);
      document.getElementById('labelInput').focus();
    },

    removeLabel(item) {
      this.$taskDrawerApi.removeLabel(item.id).then( () => {
        this.$root.$emit('show-alert', {
          type: 'success',
          message: this.$t('alert.success.label.deleted')
        });
      }).catch(e => {
        console.error('Error when removibg labels', e);
        this.$root.$emit('show-alert', {
          type: 'error',
          message: this.$t('alert.error')
        });
      });
    },
    openLabelsList() {
      this.$emit('labelsListOpened');
    },
    editLabelBeforeCreate(label) {
      this.$emit('edit-label-on-create', label);
    }
  }
};
</script>