<template>
  <v-card class="tasksCardItem">
    <div class="projectDetailsReverse pa-3">
      <i
        icon
        small
        class="uiIconInformation taskInfoIcon reverseInfoIcon d-flex"
        @click="$emit('flip')">
      </i>
      <div class="projectDetails d-flex">
        <p class="font-weight-bold ma-auto ">{{ $t('label.projectDetail') }}</p>
      </div>
    </div>
    <div class="echartStatsContent d-flex align-center px-2" style="margin:auto;">
      <div :id="'echartProjectTasks'+project.id" style="width:160px; height:200px;"></div>
      <div class="projectTasksTotalNumber">
        <span class="totalNumber font-weight-bold">{{ totalLeftTasks }}</span>
        <span class="text-body-2 totalLabel">{{ $t('exo.tasks.label.leftTasks') }}</span>
      </div>
      <div class="projectStatusNumber pl-4">
        <p class="d-flex justify-space-between mb-1 taskToDoLabel">
          <span class="caption">{{ $t('exo.tasks.status.todo') }}</span>
          <span>{{ getStatusValue('ToDo') }}</span>
        </p>
        <p class="d-flex justify-space-between mb-1 taskInProgressLabel">
          <span class="caption">{{ $t('exo.tasks.status.inprogress') }}</span>
          <span>{{ getStatusValue('InProgress') }}</span>
        </p>
        <p class="d-flex justify-space-between mb-1 taskWaitingOnLabel">
          <span class="caption">{{ $t('exo.tasks.status.waitingon') }}</span>
          <span>{{ getStatusValue('WaitingOn') }}</span>
        </p>
        <p class="d-flex justify-space-between mb-1 taskDoneLabel">
          <span class="caption">{{ $t('exo.tasks.status.done') }}</span>
          <span>{{ getStatusValue('Done') }}</span>
        </p>
      </div>
    </div>
  </v-card>
</template>
<script>
  export default {
    props: {
      project: {
        type: Object,
        default: null,
      }
    },
    data() {
      return {
        totalLeftTasks: 0,
        option : {
          tooltip: {
            trigger: 'item',
            formatter: '{b}:<br/> {c} ({d}%)'
          },
            series: [
              {
                type: 'pie',
                center: ['50%', '50%'],
                radius: ['80%', '75%'],
                avoidLabelOverlap: false,
                label: {
                  show: false,
                  position: 'center'
                },
                emphasis: {
                  label: {
                    show: false,
                    fontSize: '15',
                    fontWeight: 'bold'
                  }
                },
                labelLine: {
                  show: false
                },
                data: [
                  {value: this.getStatusValue('ToDo') , name: this.$t('exo.tasks.status.todo'),},
                  {value: this.getStatusValue('InProgress'), name: this.$t('exo.tasks.status.inprogress')},
                  {value: this.getStatusValue('WaitingOn'), name: this.$t('exo.tasks.status.waitingon')},
                  {value: this.getStatusValue('Done') , name: this.$t('exo.tasks.status.done')}
                ],
              }
            ],
          color: ['#476a9c', '#ffb441', '#bc4343', '#2eb58c']
          }
        }
    },
    mounted() {
      window.setTimeout(() => {
          this.initChart(this.option);
        },200);
    },
    created() {
      this.totalLeftTasks = this.getStatusValue('ToDo') + this.getStatusValue('InProgress') + this.getStatusValue('WaitingOn');
    },
    methods :{
      initChart(option,id) {
          const chart = echarts.init($(`#echartProjectTasks${this.project.id}`)[0]);
          chart.setOption(option, true);
      },
      getStatusValue(status) {
        const statusTaskNumber = this.project.statusStats.filter( elem => elem.status === status);
        return statusTaskNumber && statusTaskNumber.length ? statusTaskNumber[0].taskNumber : 0;
      }
    }
  }
</script>