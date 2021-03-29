<template>
  <div v-if="tasksList && tasksList.length > 0 && getTasksToDisplay(tasksList).length === 0">
    <div class="noTasksProject">
      <div class="noTasksProjectIcon"><i class="uiIcon uiIconTask"></i></div>
      <div class="noTasksProjectLabel"><span>{{ $t('label.noTasks') }}</span></div>
    </div>
  </div>
  <div
    v-else
    class="echartsContainer"
    style="margin-bottom: 20px; min-height:60vh">
    <div
      id="myChart"
      ref="chart"
      style="margin: auto;margin-bottom: 20px;"></div>
  </div>
</template>
<script>
export default {
  props: {
    tasksList: {
      type: Array,
      default: () => []
    }
  },
  data () {
    return {
      axisTimeLength: 0,
      autoHeight: 0,
      TasksGanttdimensions: [
        'Index',
        'Task ID',
        'Start Date',
        'Due Date'
      ],
      task: {
        type: Object,
        default: () => ({}),
      }
    };
  },
  mounted() {
    setTimeout(() => {
      this.drawTasksGantt();
    }, 200);
  },
  watch: {
    tasksList () {
      setTimeout(() => {
        this.drawTasksGantt();
      }, 200);
    }
  },
  methods: {
    getTaskPriorityColor(priority) {
      switch (priority) {
      case 'HIGH':
        return '#bc4343';
      case 'NORMAL':
        return '#ffb441';
      case 'LOW':
        return '#2eb58c';
      case 'NONE':
        return '#476a9c';
      }
    },
    getTasksTitle(tasksList) {
      const taskByElement = [];
      tasksList.forEach((element) => {
        taskByElement.push(`${element[1]}~${element[4]}`);
      });
      return taskByElement;
    },
    getTasksToDisplay(tasksList) {
      const GanttTasksList = [];
      tasksList.forEach((item,index) => {
        const task =[];
        task.push(index);
        task.push(item.task.id);
        if (item.task.startDate != null) { task.push(item.task.startDate.time);}
        if (item.task.dueDate != null) { task.push(item.task.dueDate.time);}
        if (item.task.startDate != null && item.task.dueDate != null) {
          task.push(item.task.title);
          task.push(item.task.priority);
          GanttTasksList.push(task);
        }
      });
      return GanttTasksList;
    },
    renderGanttItem(params, api) {
      const categoryIndex = api.value(0);
      const timeArrival = api.coord([api.value(2), categoryIndex]);
      const timeDeparture = api.coord([api.value(3), categoryIndex]);
      const barLength = timeDeparture[0] - timeArrival[0];
      const barHeight = 10;
      const x = timeArrival[0];
      const y = timeArrival[1];
      const rectNormal = this.clipRectByRect(params, {
        x: x,
        y: y,
        width: barLength,
        height: barHeight,
      });

      return {
        type: 'group',
        children: [{
          type: 'rect',
          ignore: !rectNormal,
          shape: rectNormal,
          style: api.style({fill: this.getTaskPriorityColor(api.value(5))})
        }]
      };
    },
    renderZoomInAxis() {
      const GanttTasksList = this.getTasksToDisplay(this.tasksList);
      if ( GanttTasksList.length > 7 ) {
        return  [
          {
            type: 'slider',
            xAxisIndex: 0,
            filterMode: 'none',
            realtime: true,
            height: 10,
            bottom: 0,
            startValue: (new Date().setHours(24,0,0,0)) - 1000 * 60 * 60 * 24 * 6,
            minValueSpan: 3600 * 24 * 1000,
            maxValueSpan: 3600 * 24 * 1000 * 11,
            minSpan: 0,
            maxSpan: 11,
            handleIcon: 'path://M10.7,11.9H9.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4h1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
            handleSize: '0',
            showDetail: false,
            borderColor: 'transparent'
          },
          {
            type: 'inside',
            id: 'insideX',
            xAxisIndex: 0,
            filterMode: 'weakFilter',
            minValueSpan: 3600 * 24 * 1000,
            maxValueSpan: 3600 * 24 * 1000 * 11,
            minSpan: 0,
            maxSpan: 7,
            zoomOnMouseWheel: false,
            moveOnMouseMove: true
          },{
            type: 'slider',
            yAxisIndex: 0,
            zoomLock: true,
            width: 10,
            right: 10,
            top: 70,
            bottom: 20,
            start: 60,
            end: 100,
            handleSize: 0,
            showDetail: false,
          }, {
            type: 'inside',
            id: 'insideY',
            yAxisIndex: 0,
            start: 50,
            end: 100,
            zoomOnMouseWheel: false,
            moveOnMouseMove: true,
            moveOnMouseWheel: true
          }];
      } else {
        return  [
          {
            type: 'slider',
            xAxisIndex: 0,
            filterMode: 'none',
            realtime: true,
            height: 10,
            bottom: 0,
            startValue: (new Date().setHours(24,0,0,0)) - 1000 * 60 * 60 * 24 * 6,
            minValueSpan: 3600 * 24 * 1000,
            maxValueSpan: 3600 * 24 * 1000 * 11,
            minSpan: 0,
            maxSpan: 11,
            handleIcon: 'path://M10.7,11.9H9.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4h1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
            handleSize: '0',
            showDetail: false,
            borderColor: 'transparent'
          },
          {
            type: 'inside',
            id: 'insideX',
            xAxisIndex: 0,
            filterMode: 'weakFilter',
            minValueSpan: 3600 * 24 * 1000,
            maxValueSpan: 3600 * 24 * 1000 * 11,
            minSpan: 0,
            maxSpan: 7,
            zoomOnMouseWheel: false,
            moveOnMouseMove: true
          }];
      }
    },
    clipRectByRect(params, rect) {
      return echarts.graphic.clipRectByRect(rect, {
        x: params.coordSys.x,
        y: params.coordSys.y,
        width: params.coordSys.width,
        height: params.coordSys.height
      });
    },
    drawTasksGantt () {
      if ( this.$refs.chart ) {
        const barDv = this.$refs.chart;
        barDv.style.width=`${window.innerWidth -100 }px`;
        if (barDv) {
          const myChart = echarts.init(barDv);
          const GanttTasksList = this.getTasksToDisplay(this.tasksList);
          const intViewportHeight = window.innerHeight;
          this.autoHeight = intViewportHeight - 200;
          //this.autoHeight = 7 * 50 + 100;
          console.warn(' this.autoHeight ',  this.autoHeight );
          myChart.getDom().style.height = `${this.autoHeight  }px`;
          myChart.resize(); 
          const option={
            tooltip: {
              trigger: 'item',
              backgroundColor: '#000000', 
              formatter: function (params) {
                const lang = eXo.env.portal.language;
                return `ID: ${ params.value[1]  }<br/> 
                        Start: ${  new Date(params.value[2]).toLocaleDateString(lang, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }) }<br/>
                  Due Date: ${  new Date(params.value[3]).toLocaleDateString(lang, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }) } `;
              }
            },
            animation: true,
            toolbox: {
              left: 20,
              top: 0,
              itemSize: 20,
            },
            dataZoom: this.renderZoomInAxis(),
            grid: {
              show: true,
              left: 300,
              right: 50,
              bottom: 40,
              top: 80,
              backgroundColor: '#fff',
              borderWidth: 0
            },
            yAxis: {
              type: 'category',
              name: 'Task Name',
              nameLocation: 'start',
              inverse: true,
              offset: 0,
              nameTextStyle: {
                color: '#333333',
                align: 'right',
                fontWeight: 'bold',
                fontSize: 14,
                padding: [10, 220, 10 ,10],
                margin: 10,
                
                borderColor: 'transparent',
                shadowOffsetY: 4,
                shadowOffsetX: 4,
                borderRadius: [8, 8, 8, 8],
                shadowBlur: 2
              },
              nameGap: 6,
              scale: true,
              triggerEvent: true,
              min: 0,
              max: GanttTasksList.length-1,
              axisLine: {
                show: true,
                lineStyle: {
                  color: ['#f2f2f2']
                }
              },
              axisTick: {
                show: true,
              },
              splitLine: {
                show: false,
                lineStyle: {
                  color: ['#f2f2f2']
                }
              },
              splitArea: {
                show: true,
                areaStyle: {
                  color: ['rgba(250,250,250,0.9)','#ffffff']
                }
              },
              axisLabel: {
                margin: 300,
                show: true,
                inside: false,
                align: 'left',
                fontSize: 14,
                borderRadius: 3,
                padding: 8,
                color: '#636363',
                formatter: function(params) {
                  const taskTitle = params.split('~')[1];
                  let val='';
                  if (taskTitle.length >40){
                    val = `${taskTitle.substr(0,40)}...`;
                    return val;
                  } else {
                    return taskTitle;
                  }
                }
              },
              data: this.getTasksTitle(GanttTasksList)
            },
            xAxis: {
              type: 'time',
              position: 'top',
              minInterval: 3600 * 1000 * 24,
              maxInterval: 3600 * 1000 * 24,
              splitLine: {
                show: true,
                lineStyle: {
                  color: ['#f2f2f2']
                }
              },
              axisLine: {
                show: true,
                lineStyle: {
                  color: ['#f2f2f2']
                }
              },
              axisTick: {
                show: true,
                alignWithLabel: true
              },
              axisLabel: {
                show: true,
                inside: false,
                fontSize: 12,
                borderRadius: 3,
                color: '#636363',
                formatter: function (value) {
                  const lang = eXo && eXo.env.portal.language || 'en';
                  const axisDate = new Date(value);
                  const toDay = new Date();
                  const valueDay = axisDate.toLocaleDateString(lang, {
                    day: '2-digit',
                    month: 'long'
                  });
                  let monthLabel= '';
                  let day = '';
                  const dayString = valueDay.toString();
                  
                  if ( lang === 'en') {
                    monthLabel = dayString.split(' ')[0].substring(0,3).toUpperCase();
                    day = dayString.split(' ')[1];
                  } else {
                    monthLabel = dayString.split(' ')[1].substring(0,3).toUpperCase();
                    day = dayString.split(' ')[0];
                  }
                  if ( axisDate.setHours(0,0,0,0) === toDay.setHours(0,0,0,0)) {
                    return `{toDayStyleMonth|${  monthLabel  }}{toDayStyleDay|${ day  }}`;
                  } else {
                    return `${monthLabel} ${day}`;
                  }
                },
                rich: {
                  styleMonth: {
                    align: 'center',
                  },
                  toDayStyleMonth: {
                    color: '#ffffff',
                    backgroundColor: '#578dc9',
                    padding: [2,4],
                    borderRadius: [4, 0, 0, 4],
                  },
                  toDayStyleDay: {
                    color: '#ffffff',
                    backgroundColor: '#578dc9',
                    padding: [2,4],
                    borderRadius: [0, 4, 4, 0],
                  }
                }
              }
            },
            series: [{
              id: 'tasksData',
              type: 'custom',
              renderItem: this.renderGanttItem,
              dimensions: this.TasksGanttdimensions,
              encode: {
                x: [2, 3],
                y: 0
              },
              data: GanttTasksList,
              markLine: {
                silent: true,
                symbol: ['circle', 'none'],
                itemStyle: {
                  normal: {
                    lineStyle: {
                      type: 'dashed',
                      color: '#578dc9',
                    },
                    label: {
                      show: false,
                    }
                  }
                },
                data: [{
                  xAxis: new Date().setHours(0,0,0,0)
                }]
              }
            }]
          };
          myChart.setOption(option,true);
          myChart.on('click', params => {
            let taskId = '';
            if (params.componentType === 'yAxis') {
              taskId = params.value.split('~')[0];
            }
            else if (params.componentType === 'series') {
              taskId = params.value[1];
            }
            if (taskId) {
              this.$tasksService.getTaskById(taskId).then(data => {
                this.task = data;
                this.$root.$emit('open-task-drawer', this.task);
              });
            } 
          });
        }
      }
      
    }
  }
};
</script>