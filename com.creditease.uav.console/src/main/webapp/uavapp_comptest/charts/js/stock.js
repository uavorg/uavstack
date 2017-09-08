var Stock_ChartCfg={
		id:"stockId_ChartCfg",
		type:"stock",
		cid:"chartsContainer",
		width:"auto",
		height:400,
		title:"标题1",
		titleAlign:"left",
		  series:[
			        {
			        	name:"CPU",
			        	color:"#b2d235",
			        	data:[]
			        },
			        {
			        	name:"内存",
			        	color:"#426ab3",
			        	data:[]
			        },
					{
						name:"磁盘",
						color:"#228fbd",
						data:[]
					}
			    ]
};


var Stock_ChartCfg2={
		id:"stockId_ChartCfg2",
		type:"stock",
		cid:"chartsContainer",
		width:"auto",
		height:400,
		title:"标题2",
		titleAlign:"left",
		  series:[
			        {
			        	name:"CPU2",
			        	color:"#ed1941",
			        	data:[]
			        },
			        {
			        	name:"内存2",
			        	color:"#fcaf17",
			        	data:[]
			        },
					{
						name:"磁盘",
						color:"#228fbd",
						data:[]
					}
			    ]
};
$(function() {

	//造数据 begin
	var nowTime = new Date();
	var times = []; 
	for (var timeSize = 100; timeSize > 0; timeSize--) {
		times[timeSize] = new Date(new Date().setMinutes(nowTime.getMinutes()- timeSize)).getTime()
	}


	var datas = [];
	for (var index =0;index<3;index++) {
		var data = [], size = 0;
		
		for ( var timeLong in times) {
			data[size++] = [ timeLong, parseInt(Math.random() * 1000) ];
		}
		datas[index]=data;
	}
	
	//造数据 end
	window["appcharts"].bulid(Stock_ChartCfg);
	window["appcharts"].run("stockId_ChartCfg",datas);
	

	window["appcharts"].bulid(Stock_ChartCfg2);
	window["appcharts"].run("stockId_ChartCfg2",datas);
	


	// ===================== 同步（绑定）时间轴 begin ===================== 
	var openSynTime = true;
	if(openSynTime){
		Highcharts.Point.prototype.highlight = function (pointsT,event) {
	
			try{
				this.onMouseOver(); // Show the hover marker
				this.series.chart.xAxis[0].drawCrosshair(event, this); // Show the
				this.series.chart.tooltip.refresh(pointsT, event); // Show the tooltip	
			}catch(exception){
				//因为event触发，因此忽略一些不处理的错误 
	//			console.log("this is catch exception:",exception);
			}										
		};
	
		$('#chartsContainer').bind('mousemove touchmove touchstart', function(e) {
	
			var i;
			for (i = 0; i < Highcharts.charts.length; i = i + 1) {
				var chart = Highcharts.charts[i];
				var event = chart.pointer.normalize(e.originalEvent);
	
				var pointsT = [] ;
				var lastObj = new Object();
				$.each(chart.series, function(n, seriesObj) {
					try {
						if (n == chart.series.length-1) {  
							return;
						}
						
						if(seriesObj.visible){//可见的
							var point =  seriesObj.searchPoint(event, true);
							pointsT.push(point);
							lastObj = point;
						} 
							 
						
					} catch (exception) {
						//因为event触发，因此忽略一些不处理的错误 
	//					console.log("this is catch exception:",exception);
					}
				});

				if(!jQuery.isEmptyObject(lastObj)){
					lastObj.highlight(pointsT, e);
				}
			}
		});

	}
	// ===================== 同步（绑定）光标轴 end ===================== 
	
	


	// ===================== 同步（绑定）光标轴 begin ===================== 
	var openSynTooltip = false;
	if(openSynTooltip){
		//待开发

	}
	// ===================== 同步（绑定）时间轴 end ===================== 
	
	
});
