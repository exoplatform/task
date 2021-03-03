<template>
  <div id="echartContent" style="margin: auto; width: 100%;">
    <div 
      id="myChart" 
      ref="chart"
      style="margin: auto; height: 500px;"></div>
  </div>
</template>
<script>
export default {
  props: {
    tasksList: {
      type: Array,
      default: () => []
    },
    lang: {
      type: String,
      default: function() {
        return eXo.env.portal.language;
      },
    },
  },
  data () {
    return {
      dateFormat: {
        month: 'long',
        day: 'numeric'
      },
    };
  },
  mounted() {
    setTimeout(() => {
      this.drawLine();
    }, 200);
  },
  methods: {
    setDates(test) {
      try {
        test.sort((a, b) => {
          if (a.isCurrent) {
            return -1;
          }
          if (b.isCurrent) {
            return 1;
          }
          if (!a.startDate && !b.startDate ) {
            return 0;
          }
          if (!a.startDate) {
            return 1;
          }
          if (!b.startDate) {
            return -1;
          }
          return new Date(b.startDate).getTime() - new Date(a.startDate).getTime();
        });
      } catch (e) {
        console.error('Error sorting experiences', e); // eslint-disable-line no-console
      }
    },
    getTasksTitle(tasksList) {
      const taskByElement = [];
      tasksList.forEach((element) => {
        taskByElement.push(element.title);
      });
      return taskByElement;
    },
    getTasksStartDate(tasksList) {
      const taskByDate = [];
      tasksList.forEach((element) => {
        taskByDate.push(element.startDate);
      });
      return taskByDate;
    },
    dateFormatter(date, index) {
      return this.$dateUtil.formatDateObjectToDisplay(new Date(date + (86400000*index)), this.dateFormat, this.lang);

    },
    drawLine () {
      const barDv = this.$refs.chart;
      barDv.style.width=`${window.innerWidth -100 }px`;
      if (barDv) {
        const myChart = echarts.init(barDv);
        const GanttTasksList = [];
        const numberOfDays = [];
        const tasksDate = [];
        let startDayTime = '';
        this.tasksList.forEach((item) => {
          const task = {
            'title': '',
            'startDate': '',
            'endDate': '',
            'startDateTime': '',
            'DaysNumber': ''
          };
          if (item.task.startDate != null) {
            const startDate = this.$dateUtil.formatDateObjectToDisplay(new Date(item.task.startDate.time), this.dateFormat, this.lang);
            task.startDate = startDate;
            task.startDateTime = item.task.startDate.time;
          }
          if (item.task.endDate != null) {
            const endDate = this.$dateUtil.formatDateObjectToDisplay(new Date(item.task.endDate.time), this.dateFormat, this.lang);
            task.endDate = endDate;
          }
          if (item.task.startDate != null && item.task.endDate != null) {
            task.title= item.task.title;
            numberOfDays.push(Math.round((item.task.endDate.time - item.task.startDate.time) / 86400000)-1);
            task.DaysNumber = Math.round((item.task.endDate.time - item.task.startDate.time) / 86400000);
            GanttTasksList.push(task);
          }
        });
        this.setDates(GanttTasksList);
        startDayTime = GanttTasksList.reverse()[0].startDateTime;
        for (let step=0;  step < 14; step++ ) {
          tasksDate.push(this.$dateUtil.formatDateObjectToDisplay(new Date(startDayTime + (86400000*step)), this.dateFormat, this.lang));
        }
        const startTaskDelay = [];
        const tasksToDisplay = [];
        const delayToDisplay = [];
        GanttTasksList.forEach((item) => {
          for (let step=0;  step < 14; step++ ) {
            if (item && item.startDate === tasksDate[step]) {
              startTaskDelay.push(step);
              tasksToDisplay.push(item);
              delayToDisplay.push(item.DaysNumber-1);
              break;
            }
          }
        });
        const option={
          grid: {
            left: 250,
            right: 20,
            bottom: 20,
            top: 70,
          },
          tooltip: {
            trigger: 'item',
            axisPointer: {
              type: 'none'
            }
          },
          yAxis: {
            type: 'category',
            min: 0,
            max: tasksToDisplay.length-1,
            scale: true,
            triggerEvent: true,
            axisLine: {
              show: false
            },
            axisTick: {
              show: false,
            },
            axisLabel: {
              margin: 250,
              show: true,
              inside: false,
              align: 'left',
              fontSize: 14,
              formatter: function(params) {
                let val='';
                if (params.length >36){
                  val = `${params.substr(0,36)}...`;
                  return val;
                } else {
                  return params;
                }
              }
            },
            data: this.getTasksTitle(tasksToDisplay.reverse())
          },
          xAxis: {
            type: 'value',
            position: 'top',
            min: 0,
            max: 6,
            splitNumber: 7,
            splitLine: {
              show: true,
              lineStyle: {
                color: '#f6f8fa'
              }
            },
            /*name: "Tasks",
              nameLocation: "start",
              nameTextStyle: {
                align: "left",
                padding: [0, 0, 0, 250]
              },*/
            axisLine: {
              show: false
            },
            axisTick: {
              show: false,
            },
            axisLabel: {
              show: true,
              align: 'center',
              inside: false,
              fontSize: 13,
              fontWeight: 'bold',
              backgroundColor: '#f6f8fa',
              borderRadius: 3,
              padding: 8,
              interval: 0,
              formatter: function (value,index) {
                const tasksByDate = [];
                tasksToDisplay.forEach((element) => {
                  tasksByDate.push(element.startTime);
                });
                return tasksDate[index];
              }
            }
          },
          series: [
            {
              name: 'planDate',
              type: 'bar',
              stack: 'planTasks',
              barCategoryGap: '2',
              barBorderRadius: [3, 3, 3, 3],
              barWidth: 15,
              itemStyle: {
                normal: {
                  borderColor: 'rgba(0,0,0,0)',
                  color: 'rgba(0,0,0,0)'
                },
                emphasis: {
                  borderColor: 'rgba(0,0,0,0)',
                  color: 'rgba(0,0,0,0)'
                }
              },
              data: startTaskDelay.reverse()
            },
            {
              name: 'planTasks',
              type: 'bar',
              stack: 'planTasks',
              barWidth: 70,
              barCategoryGap: '2%',
              itemStyle: {
                normal: {
                  label: {
                    show: false,
                    position: 'right',
                    textStyle: {
                      fontSize: 14
                    }
                  },
                  color: '#ffcc02',
                }
              },
              data: delayToDisplay.reverse()
            },
          ]
        };
        myChart.setOption(option,true);
        myChart.resize();
      }
    }
  }
};
</script>