<template>
  <v-layout
    row
    mx-0
    class="white">
    <v-flex
      d-flex
      xs12
      pl-3>
      <v-layout
        row
        mx-0>
        <v-flex
          d-flex
          xs6>
          <v-list-item-content
            class="py-0"
            style="max-width: 350px ">
            <a
              ref="tooltip"
              :title="task.title"
              class="taskTitle"
              @click="openTaskDrawer()">
              <v-list-item-title
                v-text="task.title"/>
              <v-list-item-subtitle><div class="color-title">{{ dateFormatter(task.dueDate) }}</div></v-list-item-subtitle>
            </a>
            <task-drawer 
              v-if="drawer"
              :drawer="drawer"
              :task="task" 
              @closeDrawer="onCloseDrawer"/>
          </v-list-item-content>
        </v-flex>
        <v-flex
          v-if="(task.status != null)"
          mt-n2
          d-flex
          xs5
          justify-end
          align>
          <v-card
            :title="task.status.project.name"
            :color="task.status.project.color"
            flex
            width="200"
            class="pa-2 my-3 projectCard text-center flexCard taskTitle"
            flat
            outlined>
            <span>{{ task.status.project.name }}</span>
          </v-card>
          <v-card
            :style="{ borderColor: task.status.project.color }"
            width="18"
            height="21"
            class="pa-2 my-3 flagCard"
            flat
            outlined
            center>
            <v-icon
              :color="getTaskPriorityColor(task.priority)"
              class="ml-n1">mdi-flag-variant</v-icon>
          </v-card>
        </v-flex>
      </v-layout>
    </v-flex>
  </v-layout>
</template>

<script>
    export default {
        props: {
            task: {
                type: Object,
                default: () => {
                    return {};
                }
            },
        },  
        data() {
            return {
                drawer: false,
            }
        },
        methods: {
            dateFormatter(dueDate) {
                if (dueDate) {
                    const date = new Date(dueDate.time);
                    const day = date.getDate();
                    const month = date.getMonth()+1;
                    const year = date.getFullYear();
                    const formattedTime = `${day  }-${  month  }-${  year}`;
                    return formattedTime
                } else {
                    return "Due date"
                }
            },
            getTaskPriorityColor(priority) {
                switch(priority) {
                    case "HIGH":
                        return "#bc4343";
                    case "NORMAL":
                        return "#ffb441";
                    case "LOW":
                        return "#2eb58c";
                    case "NONE":
                        return "#578dc9";
                }
            },
            openTaskDrawer() {
                this.drawer = true;
                document.body.style.overflow = this.drawer ? 'hidden' : 'auto';
            },
            onCloseDrawer: function(drawer){
                this.drawer = drawer;
                document.body.style.overflow = 'auto';
            },
        }
    }
</script>