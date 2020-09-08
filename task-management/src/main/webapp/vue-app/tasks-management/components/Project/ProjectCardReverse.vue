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
    <div style="margin:auto;">
      <div :id="'echartProjectTasks'+project.id" style="width:300px; height:220px; margin:auto;"></div>
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
        option : {
          tooltip: {
            trigger: 'item',
            formatter: '{b}:<br/> {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            right: '5%',
            top: '15%',
            data: ['Done', 'To Do', 'Doing', 'Blocked', 'Completed']
          },
          graphic: {
            type: 'text',
            left: '22%',
            top: '42%',
            style: {
              text: '21 tasks left',
              font: '14px arial',
              fill: '#4d5466',
              width: 30,
              height: 30
            }
          },
            series: [
              {
                type: 'pie',
                center: ['35%', '45%'],
                radius: ['65%', '60%'],
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
                  {value: 335, name: 'Done'},
                  {value: 310, name: 'To Do'},
                  {value: 234, name: 'Doing'},
                  {value: 135, name: 'Blocked'},
                  {value: 1548, name: 'Completed'}
                ]
              }
            ]
          }
        }
    },
    mounted() {
      window.setTimeout(() => {
          this.initChart(this.option);
        },200);
    },
    methods :{
      initChart(option,id) {
          const chart = echarts.init($(`#echartProjectTasks${this.project.id}`)[0]);
          chart.setOption(option, true);
      }
    }
  }
</script>