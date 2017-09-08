var chartsConfig = {
		id:"testChartDline",
		type:"dline",
		cid:"chartsContainer",
		title:"",
		width:"auto",
		height:300,
		inverted:false,/* 轴反转曲线 ： 默认：false*/
		plotBands:[["0.23","20","畅通"],["20","50","正常"],["50","80","高频"],["80","1000","压力","red"]],
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime"
	    },
	    dline:{
	    	timeformat:"FS", //refer to TimeHelper
	    	maxpoints:9,
	    	interval:15000
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
	        }
	    ]
	};

window["appcharts"].bulid(chartsConfig);

$(function(){
	
	window["appcharts"].run("testChartDline",[
	     [{"x":new Date().getTime(), "y": Math.random() * 100}],
	     [{"x":new Date().getTime(), "y": Math.random() * 100-20}]
	 ]);
	
    setInterval(function () {
    	window["appcharts"].run("testChartDline",[
            [{"x":new Date().getTime(), "y": Math.random() * 100}],
            [{"x":new Date().getTime(), "y": Math.random() * 100-20}]
        ]);
    }, 3000);
});