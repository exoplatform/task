<template>
  <div @click.stop>
    <v-combobox
      v-custom-click-outside="closeDropDownList"
      id="labelInput"
      ref="selectLabel"
      v-model="model"
      :filter="filter"
      :hide-no-data="!search"
      :items="items"
      :search-input.sync="search"
      :label="$t('label.tapLabel.name')"
      attach
      class="pt-0"
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
        <v-chip
          v-if="item === Object(item)"
          v-bind="attrs"
          :color="`${item.color} lighten-3`"
          :input-value="selected"
          class="pr-1"
          label
          small>
          <span class="pr-2">
            {{ item.text }}
          </span>
          <v-icon
            x-small
            class="pr-0"
            @click="parent.selectItem(item);removeTaskFromLabel(item)">close</v-icon>
        </v-chip>
      </template>
      <template v-slot:item="{ index, item }">
        <v-list-item @click="addTaskToLabel(item)">
          <v-chip
            :color="`${item.color} lighten-3`"
            dark
            label
            small>
            {{ item.text }}
          </v-chip>
        </v-list-item>
      </template>
    </v-combobox>
  </div>
</template>

<script>
    import {getMyAllLabels, getTaskLabels, addTaskToLabel, removeTaskFromLabel} from '../taskDrawerApi';

    export default {
        props: {
            task: {
                type: Object,
                default: () => {
                    return {};
                }
            },
            labelsList: {
                  type: Boolean,
                  default: false
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
            }
        },
        watch: {
            model(val, prev) {

                if (val.length === prev.length) {
                  this.search = null
                  return
                }
                this.model = val.map(v => {
                    if (typeof v === 'string') {
                        v = {
                            text: v,
                            name: v,
                        }
                        this.items.push(v)
                        this.nonce++
                        this.addTaskToLabel(v)
                    }
                    return v
                })
            },
            labelsList() { 
                  this.closeDropDownList()
              
            }
        },
        created() {
            this.getMyAllLabels();
            this.getTaskLabels();
        },
        methods: {
            filter(item, queryText, itemText) {
                if (item.header) {
                    return false
                }

                const hasValue = function (val) {
                    return val != null ? val : ''
                };

                const text = hasValue(itemText)
                const query = hasValue(queryText)
                return text.toString()
                    .toLowerCase()
                    .indexOf(query.toString().toLowerCase()) > -1
            },
            getMyAllLabels() {
                getMyAllLabels().then((labels) => {
                    this.items = labels.map(function (el) {
                        const o = Object.assign({}, el);
                        o.text = o.name;
                        return o;
                    });
                })
            },
            getTaskLabels() {
                getTaskLabels(this.task.id).then((labels) => {
                        this.model = labels.map(function (el) {
                            const o = Object.assign({}, el);
                            o.text = o.name;
                            return o;
                        });
                    }
                )
            },
            addTaskToLabel(label) {
                addTaskToLabel(this.task.id, label)
                this.model.push(label)
                document.getElementById('labelInput').focus()
            },
            removeTaskFromLabel(item) {
                removeTaskFromLabel(this.task.id, item.id)
            },
            closeDropDownList() {
              if (typeof this.$refs.selectLabel !== 'undefined') {
                this.$refs.selectLabel.isMenuActive = false;
              }
            },
            openLabelsList() {
                this.$emit('openLabelsList')
            }  
        }
    }
</script>