<template>
  <div class="echartsContainer" style="height: 70vh;margin-bottom: 20px;">
    <div v-if="getTasksToDisplay(tasksList).length <= 2" class="oneTaskToDisplay">
      <div
        id="myChart"
        ref="chart"
        style="margin: auto; height: 200px; margin-bottom: 20px;" ></div>
    </div>
    <div v-else-if="getTasksToDisplay(tasksList).length > 2 && getTasksToDisplay(tasksList).length <= 5" class="oneTaskToDisplay">
      <div
        id="myChart"
        ref="chart"
        style="margin: auto; height: 350px; margin-bottom: 20px;" ></div>
    </div>
    <div v-else-if="getTasksToDisplay(tasksList).length > 5 && getTasksToDisplay(tasksList).length <= 9" class="oneTaskToDisplay">
      <div
        id="myChart"
        ref="chart"
        style="margin: auto; height: 400px; margin-bottom: 20px;" ></div>
    </div>
    <div
      v-else
      id="myChart"
      ref="chart"
      style="margin: auto; height: 70vh;margin-bottom: 20px;" ></div>
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
        TasksGanttdimensions: [
          "Index",
          "Task ID",
          "Start Date",
          "Due Date"
        ],
      }
    },
    mounted() {
        setTimeout(() => {
          this.drawTasksGantt();
        }, 200);
    },
    methods: {
      getTasksTitle(tasksList) {
        const taskByElement = [];
        tasksList.forEach((element) => {
          taskByElement.push(element[4]);
        });
        return taskByElement;
      },
      getTasksToDisplay(tasksList) {
        const GanttTasksList = [];
        tasksList.forEach((item,index) => {
          const task =[];
          task.push(index);
          task.push(item.task.id);
          if(item.task.startDate != null) { task.push(item.task.startDate.time);}
          if(item.task.dueDate != null) { task.push(item.task.dueDate.time);}
          if(item.task.startDate != null && item.task.dueDate != null) {
            task.push(item.task.title);
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
        const barHeight = 20;
        const x = timeArrival[0];
        const y = timeArrival[1] - barHeight;
        const rectNormal = this.clipRectByRect(params, {
          x: x, y: y, width: barLength, height: barHeight
        });
        return {
          type: 'group',
          children: [{
            type: 'rect',
            ignore: !rectNormal,
            shape: rectNormal,
            style: api.style({fill: '#ffcc02'})
          }]
        };
      },
      renderZoomInAxis() {
        const GanttTasksList = this.getTasksToDisplay(this.tasksList);
        if(GanttTasksList.length > 9) {
          return  [
              {
                type: 'slider',
                xAxisIndex: 0,
                filterMode: 'weakFilter',
                height: 20,
                bottom: 0,
                start: 0,
                end: 26,
                handleIcon: 'path://M10.7,11.9H9.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4h1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
                handleSize: '0',
                showDetail: false
              }, {
              type: 'inside',
              id: 'insideX',
              xAxisIndex: 0,
              filterMode: 'weakFilter',
              start: 0,
              end: 26,
              zoomOnMouseWheel: false,
              moveOnMouseMove: false
            },
              {
                type: 'slider',
                yAxisIndex: 0,
                zoomLock: true,
                width: 10,
                right: 10,
                top: 70,
                bottom: 20,
                start: 0,
                end: 50,
                handleSize: 0,
                showDetail: false,
              }, {
              type: 'inside',
              id: 'insideY',
              yAxisIndex: 0,
              start: 0,
              end: 50,
              zoomOnMouseWheel: false,
              moveOnMouseMove: true,
              moveOnMouseWheel: true
            }]
        } else {
          return [
              {
                type: 'slider',
                xAxisIndex: 0,
                filterMode: 'weakFilter',
                height: 20,
                bottom: 0,
                start: 0,
                end: 26,
                handleIcon: 'path://M10.7,11.9H9.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4h1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
                handleSize: '0',
                showDetail: false
              }, {
              type: 'inside',
              id: 'insideX',
              xAxisIndex: 0,
              filterMode: 'weakFilter',
              start: 0,
              end: 26,
              zoomOnMouseWheel: false,
              moveOnMouseMove: false
            },
          ]
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
        const barDv = this.$refs.chart;
        barDv.style.width=`${window.innerWidth -100 }px`;
        if (barDv) {
          const myChart = echarts.init(barDv);
          const GanttTasksList = this.getTasksToDisplay(this.tasksList);
          const option={
            tooltip: {
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
              left: 250,
              right: 20,
              bottom: 20,
              top: 70,
              backgroundColor: '#fff',
              borderWidth: 0
            },
            yAxis: {
              type: "category",
              name: "Tasks",
              nameLocation: "start",
              inverse: true,
              nameTextStyle: {
                color: "#333333",
                align: "right",
                backgroundColor: "#e2e9ef",
                padding: [10, 120, 10 ,100],
                borderWidth: 5,
                borderColor: 'transparent',
                shadowOffsetY: 4,
                shadowOffsetX: 4,
                borderRadius: [8, 8, 8, 8],
                shadowBlur: 2
              },
              nameGap: 6,
              scale:true,
              triggerEvent: true,
              min: 0,
              max: GanttTasksList.length-1,
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
                align: "left",
                fontSize: 14,
                borderRadius: 3,
                padding: 8,
                formatter: function(params) {
                  let val="";
                  if(params.length >30){
                    val = `${params.substr(0,30)}...`;
                    return val;
                  }else{
                    return params;
                  }
                }
              },
              data: this.getTasksTitle(GanttTasksList)
            },
            xAxis: {
              type: "time",
              position: "top",
              splitLine: {
                show: false,
              },
              axisLine: {
                show: false
              },
              axisTick: {
                show: false,
              },
              axisLabel: {
                show: true,
                align: "left",
                inside: false,
                fontSize: 13,
                fontWeight: "bold",
                borderRadius: 3,
                padding: 8,
                formatter: function (value) {
                  return echarts.format.formatTime('dd-MM-yyyy', value);
                }
              }
            },
            series: [{
              id: 'flightData',
              type: 'custom',
              renderItem: this.renderGanttItem,
              dimensions: this.TasksGanttdimensions,
              encode: {
                x: [2, 3],
                y: 0,
                tooltip: [1, 2, 3]
              },
              tooltip: {
                backgroundColor: 'rgba(50,50,50,0.8)',
              },
              data: GanttTasksList
              }
            ]
          };
          myChart.setOption(option,true);
        }
      }
    }
  }
</script>