var testChartSpline_Config = {
		id:"testChartSpline",
		type:"spline",
		cid:"chartsContainer",
		title:"",
		width:"auto",
		height:300,
		inverted:false,/* 轴反转曲线 ： 默认：false*/
  		plotBands:[["0.23","50","畅通"],["50","100","正常"],["100","150","高频"],["150","200","压力","red"]],
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"ytitle",
	    },
	    xAxis:{
	    	title:"xtitle",
	    	type:"datetime",
	    	labels: {
		    	formatter:function(){
		    		return Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.value);
		    	}
	    	}
	    },
	    spline: {
			timeformat: "FS",
		},
	    series:[
	        {
	        	name:"CPU",
	        	color:"#EEEE00",
	        	data:[]
	        },
	        {
	        	name:"内存",
	        	color:"#EE2200",
	        	data:[]
	        },
			{
				name:"磁盘",
				color:"#563624",
				data:[]
			}
	    ]
	};
var testChartSpline2_Config = {
		id:"testChartSpline2",
		type:"spline",
		cid:"chartsContainer2",
		title:"",
		width:"auto",
		height:300,
		inverted:false,/* 轴反转曲线 ： 默认：false*/
  		plotBands:[["0.23","50","畅通"],["50","100","正常"],["100","150","高频"],["150","200","压力","red"]],
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"ytitle",
	    },
	    xAxis:{
	    	title:"xtitle",
	    	type:"datetime",
	    	labels: {
//		    	formatter:function(){
//		    		return Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.value);
//		    	}
	    	},
	    	categories: ['Apples', 'Bananas', 'Oranges']
	    },
	    spline: {
			timeformat: "FS",
		},
	    series:[
	        {
	        	name:"CPU_2",
	        	color:"#EEEE00",
	        	data:[]
	        },
	        {
	        	name:"内存_2",
	        	color:"#EE2200",
	        	data:[]
	        },
			{
				name:"磁盘_2",
				color:"#563624",
				data:[]
			}
	    ]
	};

var testChartArea_Config = {
		id:"testChartArea",
		type:"area",
		cid:"chartsContainerArea",
		title:"",
		width:"auto",
		height:300,
		inverted:false,/* 轴反转曲线 ： 默认：false*/
  		plotBands:[["0.23","50","畅通"],["50","100","正常"],["100","150","高频"],["150","200","压力","red"]],
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"ytitle",
	    },
	    xAxis:{
	    	title:"xtitle",
	    	type:"datetime"
	    },
	    spline: {
			timeformat: "FS",
		},
	    series:[
	        {
	        	name:"CPU",
	        	color:"#EEEE00",
	        	data:[]
	        },
	        {
	        	name:"内存",
	        	color:"#EE2200",
	        	data:[]
	        },
			{
				name:"磁盘",
				color:"#563624",
				data:[]
			}
	    ]
	};

var testChartBar_Config = {
		id:"testChartBar",
		type:"column",
		cid:"chartsContainerBar",
		title:"",
		width:"auto",
		height:300,
		inverted:false,/* 轴反转曲线 ： 默认：false*/
  		plotBands:[["0.23","50","畅通"],["50","100","正常"],["100","150","高频"],["150","200","压力","red"]],
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"ytitle",
	    },
	    xAxis:{
	    	title:"xtitle",
	    	type:"datetime"
	    },
	    spline: {
			timeformat: "FS",
		},
	    series:[
	        {
	        	name:"CPU",
	        	color:"#EEEE00",
	        	data:[]
	        },
	        {
	        	name:"内存",
	        	color:"#EE2200",
	        	data:[]
	        },
			{
				name:"磁盘",
				color:"#563624",
				data:[]
			}
	    ]
	};

window["appcharts"].bulid(testChartSpline_Config);
window["appcharts"].bulid(testChartSpline2_Config);
window["appcharts"].bulid(testChartArea_Config);
window["appcharts"].bulid(testChartBar_Config);

$(function(){

	var params = [
//			[10,20,50,5,5],
//			[6,12,54,12.3,8],
//			[23,43,56,86.6,170]
//			
//			 [{"x":new Date().getTime(), "y": Math.random() * 100}],
//		     [{"x":new Date().getTime(), "y": Math.random() * 100-20}],
//			 [{"x":new Date().getTime(), "y": Math.random() * 100-30}]
		];
	
	for(var i=0;i<3;i++) {
		params[i]=[];
		var time=new Date().getTime();
		for(var j=0;j<5;j++) {
			params[i][j]={"x":time-j*5000, "y": Math.floor(Math.random() * 100)
					};
			
		}		
	}

	window["appcharts"].run("testChartSpline",params);
	window["appcharts"].run("testChartArea",params);
	window["appcharts"].run("testChartBar",params);
	
	var params2 =[];
	for(var i=0;i<3;i++) {
		params2[i]=[];
		var time=new Date().getTime();
		for(var j=0;j<5;j++) {
			params2[i][j]=Math.floor(Math.random() * 100);
			
		}		
	}
	window["appcharts"].run("testChartSpline2",params2);
	
	
	
	//重新刷新数据
	  setInterval(function () {
		  //重绘1
		  var params2=[],size = Math.floor(Math.random() * 10)+1;
			for(var i=0;i<3;i++) {
				params2[i]=[];
				var time=new Date().getTime();
				for(var j=0;j<size;j++) {
					params2[i][j]={"x":time-j*5000, "y": Math.floor(Math.random() * 100)
							};
					
				}		
			}

			window["appcharts"].reset("testChartSpline");
			window["appcharts"].run("testChartSpline",params2);
			
	    }, 3000);
	  
	
});