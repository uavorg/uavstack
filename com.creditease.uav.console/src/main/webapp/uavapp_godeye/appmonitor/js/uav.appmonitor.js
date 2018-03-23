/**
 * TODO: AppChart Configuration 
 */
//app chart
var appQPSCfg = {
		id:"appQPSChart",
		type:"spline",
		cid:"AppChartWnd_CT",
		title:"访问计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};

var jseappQPSCfg=JsonHelper.clone(appQPSCfg);
jseappQPSCfg.title="启动线程计数";

var appRTCfg = {
		id:"appRTChart",
		type:"spline",
		cid:"AppChartWnd_CT",
		title:"响应时间(ms)",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:1
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};

var jseappRTCfg=JsonHelper.clone(appRTCfg);
jseappRTCfg.title="Heap使用(M)";

var appErrCfg = {
		id:"appErrChart",
		type:"spline",
		cid:"AppChartWnd_CT",
		title:"错误计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};
/**
 * TODO: AppInstChart Configuration
 */
//app inst chart
var appInstQPSCfg = {
		id:"appInstQPSChart",
		type:"spline",
		cid:"AppInstChartWnd_CT",
		title:"访问计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[
	            {
	            	name:"总计数",
	            	data:[]
	            },
	            {
	            	name:"错误计数",
	            	data:[]
	            },
	            {
	            	name:"警告计数",
	            	data:[]
	            }
	            ],
        seriesMap:{
        	"count":0,
        	"err":1,
        	"warn":2
        }
	};

var jseappInstQPSCfg=JsonHelper.clone(appInstQPSCfg);
jseappInstQPSCfg.title="线程计数";
jseappInstQPSCfg.series=[
{
	name:"启动线程计数",
	data:[]
}
                         ];

jseappInstQPSCfg.seriesMap={
	"thread_started":0
};

var appInstRTCfg = {
		id:"appInstRTChart",
		type:"spline",
		cid:"AppInstChartWnd_CT",
		title:"响应时间(ms)",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[
					{
						name:"最长响应时间",
						data:[]
					},
					{
						name:"最短响应时间",
						data:[]
					},
					{
						name:"平均响应时间",
						data:[]
					}
	            ],
        seriesMap:{
        	"tmax":0,
        	"tmin":1,
        	"tavg":2
        }
	};

var jseappInstRTCfg=JsonHelper.clone(appInstRTCfg);
jseappInstRTCfg.title="Heap使用(M)";
jseappInstRTCfg.series=[
{
	name:"Heap",
	data:[]
}
                         ];

jseappInstRTCfg.seriesMap={
	"heap_use":0
};

var appInstErrCfg = {
		id:"appInstErrChart",
		type:"spline",
		cid:"AppInstChartWnd_CT",
		title:"响应代码计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};
/**
 * TODO: AppServiceURLChart Configuration
 */
var appURLQPSCfg = {
		id:"appURLQPSChart",
		type:"spline",
		cid:"AppServiceURLChartWnd_CT",
		title:"访问计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};

var appURLRTCfg = {
		id:"appURLRTChart",
		type:"spline",
		cid:"AppServiceURLChartWnd_CT",
		title:"响应时间(ms)",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};

var appURLErrCfg = {
		id:"appURLErrChart",
		type:"spline",
		cid:"AppServiceURLChartWnd_CT",
		title:"错误计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};


/**
 * TODO: AppClientURLChart Configuration
 */
var appClientURLQPSCfg = {
		id:"appClientURLQPSChart",
		type:"spline",
		cid:"AppClientURLChartWnd_CT",
		title:"访问计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};

var appClientURLRTCfg = {
		id:"appClientURLRTChart",
		type:"spline",
		cid:"AppClientURLChartWnd_CT",
		title:"响应时间(ms)",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};

var appClientURLErrCfg = {
		id:"appClientURLErrChart",
		type:"spline",
		cid:"AppClientURLChartWnd_CT",
		title:"错误计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};

/**
 * TODO: AppServerChart Configuration
 */
var appServerQPSCfg={
	id:"appServerQPSChart",
	type:"spline",
	cid:"AppServerChartWnd_CT",
	title:"访问计数",
	titleAlign:"left",
	width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
	height:200,
	legend:{
		enabled:true,
		verticalAlign:"top"
    },
	yAxis:{
    	title:"",
    	min:0,
    	tickInterval:10
    },
    xAxis:{
    	title:"",
    	type:"datetime",
    	labels:{
    		formatter:function(){
    			return TimeHelper.getTime(this.value,"CS");
    		}
    	}
    },
    series:[{
    	name:"总计数",
    	data:[]
    },
    {
    	name:"错误计数",
    	data:[]
    },
    {
    	name:"警告计数",
    	data:[]
    }],
    seriesMap:{
    	"count":0,
    	"err":1,
    	"warn":2
    }
};

var appServerRTCfg={
		id:"appServerRTChart",
		type:"spline",
		cid:"AppServerChartWnd_CT",
		title:"响应时间(ms)",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[
				{
					name:"最长响应时间",
					data:[]
				},
				{
					name:"最短响应时间",
					data:[]
				},
				{
					name:"平均响应时间",
					data:[]
				}
        ],
	    seriesMap:{
	    	"tmax":0,
	    	"tmin":1,
	    	"tavg":2
	    }
	};

var appServerErrCfg={
		id:"appServerErrChart",
		type:"spline",
		cid:"AppServerChartWnd_CT",
		title:"错误计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
	};

/**
 * TODO: AppServerChart JVM Configuration
 */
var appJVMHeapCfg={
	id:"appJVMHeapChart",
	type:"spline",
	cid:"AppServerChartWnd_CT",
	title:"Heap使用(MB)",
	titleAlign:"left",
	width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
	height:200,
	legend:{
		enabled:true,
		verticalAlign:"top"
    },
	yAxis:{
    	title:"",
    	min:0,
    	tickInterval:10
    },
    xAxis:{
    	title:"",
    	type:"datetime",
    	labels:{
    		formatter:function(){
    			return TimeHelper.getTime(this.value,"CS");
    		}
    	}
    },
    series:[
        {
            name:"Heap内存",
    		data:[]
	    },
	    {
            name:"非Heap内存",
    		data:[]
	    },
	    {
	    	name:"Eden",
	    	data:[]
	    },
	    {
	    	name:"Old",
	    	data:[]
	    },
	    {
	    	name:"Survivor",
	    	data:[]
	    },
	    {
	    	name:"Code",
	    	data:[]
	    },
	    {
	    	name:"Perm",
	    	data:[]
	    }
    ],
    seriesMap:{
    	"heap_use":0,
    	"noheap_use":1,
    	"eden_use":2,
    	"old_use":3,
    	"surv_use":4,
    	"code_use":5,
    	"perm_use":6
    }
};

var appJVMGCCfg={
		id:"appJVMGCChart",
		type:"spline",
		cid:"AppServerChartWnd_CT",
		title:"GC计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[
				{
					name:"FullGC",
					data:[]
				},
				{
					name:"MinorGC",
					data:[]
				},
				{
					name:"FullGC时间",
					data:[]
				},
				{
					name:"MinorGC时间",
					data:[]
				}
        ],
	    seriesMap:{
	    	"fgc_count":0,
	    	"mgc_count":1,
	    	"fgc_time":2,
	    	"mgc_time":3,
	    }
	};

var appJVMThreadCfg={
		id:"appJVMThreadChart",
		type:"spline",
		cid:"AppServerChartWnd_CT",
		title:"线程计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[
				{
					name:"alive",
					data:[]
				},
				{
					name:"daemon",
					data:[]
				},
				{
					name:"peak",
					data:[]
				},
				{
					name:"started",
					data:[]
				}
        ],
	    seriesMap:{
	    	"thread_live":0,
	    	"thread_daemon":1,
	    	"thread_peak":2,
	    	"thread_started":3,
	    }
	};

var appJVMCPUCfg={
		id:"appJVMCPUChart",
		type:"spline",
		cid:"AppServerChartWnd_CT",
		title:"CPU(%)",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10,
	    	max:100
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[
				{
					name:"进程占用率",
					data:[]
				},
				{
					name:"系统占用率",
					data:[]
				},
	     ],
	     seriesMap:{
		    	"cpu_p":0,
		    	"cpu_s":1
	     }
	};

var appJVMClsCfg={
		id:"appJVMClsChart",
		type:"spline",
		cid:"AppServerChartWnd_CT",
		title:"Class计数",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:"",
	    	min:0,
	    	tickInterval:10
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[
				{
					name:"load",
					data:[]
				},
				{
					name:"unload",
					data:[]
				},
				{
					name:"total",
					data:[]
				}
        ],
	    seriesMap:{
	    	"class_load":0,
	    	"class_unload":1,
	    	"class_total":2
	    }
	};

//TODO: AppCustMetricCfg
var appCustMetricCfg={
		id:"",
		type:"spline",
		cid:"AppCustMetricWnd_CT",
		title:"",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:200,
		legend:{
			enabled:true,
			verticalAlign:"top"
	    },
		yAxis:{
	    	title:""
	    },
	    xAxis:{
	    	title:"",
	    	type:"datetime",
	    	labels:{
	    		formatter:function(){
	    			return TimeHelper.getTime(this.value,"CS");
	    		}
	    	}
	    },
	    series:[]
};

/**
 * TODO: AppTabListConfig
 */
//app tab list config
var appTabListConfig={
	id:"appTabList",
	cid:"AppList",
	pkey:function(d) {
		return d["appgroup"]+":"+d["appid"];
	},
	style:"Light",
	width:280,
	templ:function(nodeDiv,jsonObj) {
		app.controller.buildAppNode(nodeDiv,jsonObj);
	},
	group:{
		getId:function(d) {
			return d["appgroup"];
		},
		click:function(groupId,e) {
			//open app group stream window
			app.controller.showWindow({appgroup:groupId},"AppGroupTopWnd","buildAppGroupTopWnd","runAppGroupTop");
		},
		match:function(groups,curdata,dataObjs) {
			var gp=curdata["appgroup"];
			
			//the data has appgroup
			if(gp!=undefined&&gp!=""&&groups[gp]!=undefined) {
				return groups[gp];
			}
			//the data has not appgroup
			else {
				//1. find if there is a data with the same ip in groupObjs
				var ip=curdata["ip"];
				
				for (var key in groups) {
					var groupData=groups[key];
					
				    for(var i=0;i<groupData.members.count();i++) {
				    	var it=groupData.members.mapValues.get(i);
				    	if (it["ip"]==ip) {
							return groupData;
						}
				    }
				}
				
				//if 1 fails, find all datas, see if there is a data with the same ip who has appgroup
				for (var key in dataObjs) {
					
					var d=dataObjs[key];
					
					var dgp=d["appgroup"];
					var dip=d["ip"];
					
					if (dgp!=undefined&&dgp!=""&&dip==ip) {
						return dgp;
					}
				}
			}
			
			return undefined;
		}
	}
};

/**
 * TODO:记录各个Monitor的状态，重要数据结构
 */
var monitorCfg={
	//the standard start time
	startTime:undefined,
	//note: auto refresh is default, when the window use time line change, the auto refresh is stopped, when quit the window, the auto refresh is restarted
	isautorefresh:true,	
	//for customed query
	customquery:{
	},
	//current metrics
	metrics:{
		//for JEE
		perf:[
		      "tmax","tsum","tavg","tmin","count","err","warn","RC","AC","EXT"
		      ],
		//for JSE
		jseperf:[
		         "thread_started","heap_max","heap_use"
		         ],
		// common jvm
		jvm:[
				"perm_init",    "thread_peak",		"thread_daemon",
				"code_max",     "code_init",		"class_load",
				"heap_use",	    "perm_use",			"class_unload",
				"old_init",	    "cpu_s",			"surv_max",
				"surv_init",    "mgc_count",		"fgc_time",
				"cpu_p",		"old_max",			"eden_max",
				"heap_commit",	"code_use",			"surv_commit",
				"perm_max",		"thread_started",	"perm_commit",
				"fgc_count",	"thread_live",		"eden_commit",
				"mgc_time",		"eden_use",			"heap_max",
				"old_use",		"code_commit",		"surv_use",
				"class_total",	"heap_init",		"eden_init",
				"old_commit",   "noheap_use",       "noheap_init",
				"noheap_commit","noheap_max"
		     ]
	},
	//app monitor data state
	app:{
		//static config
		syncInterval:60000,
		//note:check if use the customeized query monitor data
		isusecustomized:false,
		//autogroup
		ipToAppGroup:{}
	},
	//app service url data state
	url:{
		//static config
		syncInterval:60000,
		urls:[],
		ip:"",
		svrid:"",
		//note:check if use the customeized query monitor data
		isusecustomized:false,
	},
	//app client data state
	client:{
		//static config
		syncInterval:60000,
		urls:[],
		//note:check if use the customeized query monitor data
		isusecustomized:false,
	},
	//app server data state
	server:{
		//static config
		syncInterval:60000,
		ip:"",
		svrid:"",
		inst:"",
		//note:check if use the customeized query monitor data
		isusecustomized:false,
	},
	//app log data state
	log:{
		//static config
		syncInterval:5000,
		//mark the running state is play or stop
		play:false,
		//page size
		psize:50,
		//is first time
		isfirsttime:true,
		//auto scroll
		isautoscroll:true,
		//logbuffer
		logbuffer:1000,
		//logcount
		logcount:0,
		//logpodCount
		logpodcount:0,
		//logcurpod,
		logcurpod:0
	},
	//app customized metrics
	cust: {
		//static config
		syncInterval:60000,
		ip:"",
		svrid:"",
		inst:"",
		//the customized metrics
		metrics:{},
		//note:check if use the customeized query monitor data
		isusecustomized:false,
	}
};

/**
 * TODO： open link
 */
var openLink={

		/**
		 * 延迟打开window
		 * @param ip
		 * @param winId
		 * @param func
		 * @param callback
		 * @param tag
		 */
		showWindowDelay:function(ip,winId,func,callback,tag) {
			var info={ip:ip,winId:winId,func:func,callback:callback,tag:tag};
			window["openlink_"+tag]=info;
		},
		
		/**
		 * 触发点执行打开窗口
		 * @param tag
		 */
		runWindowDelay:function(tag) {
			if (window["openlink_"+tag]==undefined) {
				return;
			}
			
			var info=window["openlink_"+tag];
			app.controller.showWindow(info["ip"],info["winId"],info["func"],info["callback"]);
			window["openlink_"+tag]=undefined;
		}
};

/**
 * TODO:应用画像与监控
 */
var mvcObj={
	init:function() {
		//build appList window
		window.winmgr.build({
			id:"AppList",
			order:1,
			height:"auto:0",
			"overflow-y":"auto",
			theme:"BGLight",
			top:50
		});
		//build appList window
		window.winmgr.build({
			id:"GlobalEyeTopWnd",
			order:2,
			height:"auto:0",
			"overflow-y":"auto",
			theme:"BGLight",
			top:0,
			events:{
				onresize:function(w,h,noTopHeight) {
					//resize appstream playground
					appStream.resize(w,h,noTopHeight);
				}
			}
		});
		//build app topology window
		window.winmgr.build({
			id:"AppTopoWnd",
			height:"auto",
			"overflow-y":"auto",
			order:999
		});
		//build app chart window
		window.winmgr.build({
			id:"AppChartWnd",
			height:"auto",
			"overflow-y":"auto",
			order:998
		});
		//build app instance window
		window.winmgr.build({
			id:"AppInstChartWnd",
			height:"auto",
			"overflow-y":"auto",
			order:997
		});
		//build app service url window
		window.winmgr.build({
			id:"AppServiceURLChartWnd",
			height:"auto",
			"overflow-y":"auto",
			order:996
		});
		
		//build app server window
		window.winmgr.build({
			id:"AppServerWnd",
			height:"auto",
			"overflow-y":"auto",
			order:994
		});		
		//build app customized metrics window
		window.winmgr.build({
			id:"AppCustMetricWnd",
			height:"auto",
			"overflow-y":"auto",
			order:993
		});
		//build app cluster topology window
		window.winmgr.build({
			id:"AppClusterTopWnd",
			height:"auto",
			theme:"BGLight",
			"overflow-y":"auto",
			order:994,
			events:{
				onresize:function(w,h,noTopHeight) {
					//resize appstream playground
					appStream.resize(w,h,noTopHeight);
				}
			}
		});
		//build app cluster topology window
		window.winmgr.build({
			id:"AppGroupTopWnd",
			height:"auto",
			theme:"BGLight",
			"overflow-y":"auto",
			order:995,
			events:{
				onresize:function(w,h,noTopHeight) {
					//resize appstream playground
					appStream.resize(w,h,noTopHeight);
				}
			}
		});
		//build AppClientURLChartWnd
		window.winmgr.build({
			id:"AppClientURLChartWnd",
			height:"auto",
			"overflow-y":"auto",
			order:996			
		});
		//build AppProxyEdgeDetailWnd
		window.winmgr.build({
			id:"AppProxyEdgeDetailWnd",
			height:"auto",
			"overflow-y":"auto",
			order:997			
		});
		//build AppUnknowEdgeDetailWnd
		window.winmgr.build({
			id:"AppUnknowEdgeDetailWnd",
			height:"auto",
			"overflow-y":"auto",
			order:998	
		});
		//build AppIVCWnd
		window.winmgr.build({
			id:"AppIVCWnd",
			height:"auto",
			"overflow-y":"auto",
			theme:"BGLight",
			order:999
		});
		//build AppIVCTraceWnd
		window.winmgr.build({
			id:"AppIVCTraceWnd",
			height:"auto",
			"overflow-y":"auto",
			theme:"BGLight",
			order:1000
		});
		//build AppIVCDataWnd
		window.winmgr.build({
			id:"AppIVCDataWnd",
			height:"auto",
			"overflow-y":"auto",
			theme:"BGDark",
			order:1050
		});
		//build AppIVCCfgWnd
		window.winmgr.build({
			id:"AppIVCCfgWnd",
			height:"auto",
			"overflow-y":"auto",
			theme:"BGLight",
			order:1050
		});
		//build AppAPMCfgWnd
		window.winmgr.build({
			id:"AppAPMCfgWnd",
			height:"auto",
			"overflow-y":"auto",
			theme:"BGLight",
			order:1000
		});
		//build AppNewLogWnd
		window.winmgr.build({
			id:"AppNewLogWnd",
			height:"auto",
			"overflow-y":"auto",
			theme:"BGLight",
			order:1001
		});
		//build AppNewLogRollWnd
		window.winmgr.build({
			id:"AppNewLogRollWnd",
			height:"auto",
			"overflow-y":"auto",
			theme:"BGLight",
			order:1002,
			events:{
				onresize:function(w,h,noTopHeight) {
					appLog.onResizeAppRollWnd(w,h,noTopHeight);
				}
			}
		});
		//build AppNewLogWnd
		window.winmgr.build({
			id:"AppNewLogCfgWnd",
			height:"auto",
			"overflow-y":"auto",
			theme:"BGLight",
			order:1003
		});
		// build Thread Analysis list window
		window.winmgr.build({
			id: 'AppJTAListWnd',
			height: 'auto',
			'overflow-y': 'auto',
			theme: 'BGLight',
			order: 1004
		});
		// build Thread Analysis Detail Window
		window.winmgr.build({
			id: 'AppJTADetailWnd',
			height: 'auto',
			'overflow-y': 'auto',
			theme: 'BGLight',
			order: 1005
		});
		// build Thread Analysis Multi Dump Window 
		window.winmgr.build({
			id: 'AppJTAMultiDumpWnd',
			height: 'auto',
			'overflow-y': 'auto',
			theme: 'BGLight',
			order: 1006
		});
		// build Thread Analysis Graph Window
		window.winmgr.build({
			id: 'AppJTAGraphWnd',
			height: 'auto',
			'overflow-y': 'auto',
			theme: 'BGLight',
			order: 1007,
			events:{
				onresize:function(w,h,noTopHeight) {
					appJTA.resize(w,h,noTopHeight);
				}
			}
		});
		// build Thread Analysis Message Windows
		window.winmgr.build({
			id: 'AppJTAMsgWnd',
			height: 'auto',
			'overflow-y': 'auto',
			theme: 'BGLight',
			order: 1008
		});
		
		//View Change
		var view=HtmlHelper.getQParam("view");
		//for 服务星云
		if (view=="g") {
			window.winmgr.show("GlobalEyeTopWnd");
			setTimeout(function() {
				app.controller.showWindow({backWndId:"GlobalEyeTopWnd"},"GlobalEyeTopWnd","buildGlobalEyeTopWnd","runGlobalEyeTop");
			},500);
		}
		//for APM编辑
		else if (view=="apmcfg") {
			window.winmgr.show("AppAPMCfgWnd");
			var func=HtmlHelper.getQParam("func");
			setTimeout(function() {
				app.controller.showWindow({"func":func,"winmode":"standalone"},"AppAPMCfgWnd","buildAppAPMCfgWnd","runAppAPMCfgWnd");
			},500);
			//not start sync
			return false;
		}
		//for 调用链检索
		else if (view=="ivcsearch") {
			window.winmgr.show("AppIVCWnd");
			setTimeout(function() {
				app.controller.showWindow({"winmode":"standalone"},"AppIVCWnd","buildAppIVCWnd","runAppIVCWnd");
			},500);
			//not start sync
			return false;
		}
		// for 新日志检索
		else if (view=="newlogsearch") {
			window.winmgr.show("AppNewLogWnd");
			setTimeout(function() {
				app.controller.showWindow({"winmode":"standalone"},"AppNewLogWnd","buildAppNewLogWnd","runAppNewLogWnd");
			},500);
			//not start sync
			return false;
		}
		// for 应用实例监控
		else if (view=="appinst") {
			window.winmgr.show("AppInstWnd");
			var param=HtmlHelper.getQParams()["param"];
			param=decodeURI(param);
			var params=param.split(",");
			var id=params[0];
			var instid=params[1];
			var isJse=params[2];
			var appInfo={id:id,instid:instid,isJse:isJse};
			openLink.showWindowDelay(appInfo,"AppInstChartWnd","buildAppInstChart","runAppInstChart","monitorapp");
		}
		//for 应用监控
		else {
			window.winmgr.show("AppList");	
		}
		
		//init app tab list
		window.tablistmgr.build(appTabListConfig);
		//init monitorcfg dialog
		this.controller.initMonitorCfgDialog();
	},
	/**
	 * TODO: datas
	 */
	datas:{
		//sync Profile Data
		"app.profile":{
			url:function() {
				return "../../rs/godeye/profile/q/cache";
			},
			method:"GET",
			rtype:"html",
			init:true,
			interval:60000,
			prepare:function(data) {
				
				 var jsonData = eval("(" + data + ")");
				 jsonData = eval("(" + jsonData["rs"] + ")");
				 var nodeArray=[];
				 var ungroupNodes=[];
				 var ipToAppGroup={};
				 
				 //Step 1: tide obj to array and figure out those ungroup node and the mapping of IP to AppGroup
				 for (var key in jsonData) {
					 
					 var nodeInfoObj=eval("(" + jsonData[key]+ ")");
					 nodeInfoObj["id"]=key;
					 
					 var appgroup=nodeInfoObj["appgroup"];
					 var appid=nodeInfoObj["appid"];
					 
					 if (appgroup==undefined||appgroup=="") {
						 ungroupNodes[ungroupNodes.length]=nodeInfoObj;
					 }
					 else {
						 var ip=nodeInfoObj["ip"];
						 if(ipToAppGroup[ip]==undefined){
							 ipToAppGroup[ip]=[];
						 }
							 						 
						 ipToAppGroup[ip][appid]=appgroup; 
					 }					 
					 
					 nodeArray[nodeArray.length]=nodeInfoObj;
				 }
				 
				 //Step 2: help those ungroup nodes to get the right AppGroup
				 for(var i=0;i<ungroupNodes.length;i++) {
					 var ip=ungroupNodes[i]["ip"];
					 if (ipToAppGroup[ip]!=undefined) {
						 ungroupNodes[i]["appgroup"]=ipToAppGroup[ip][appid];
						 ungroupNodes[i]["appgpid"]=ungroupNodes[i]["appgroup"]+":"+appid;
					 }
				 }
				 
				 //Step 3: set the global ipToAppGroup, as we need use it group the monitor data
				 monitorCfg.app.ipToAppGroup=ipToAppGroup;
				 
				 return nodeArray;
			},
			models:["profile","monitor.appgp"]
		},
		//sync Monitor Data : Server & JVM
	    "server.monitor": {
	    	url:function() {
				return "../../rs/godeye/monitor/q/hm";
			},
			//POST DATA BODY
			data:function() {
				return app.controller.buildAppMonitorQuery("server");
			},
			method:"POST",
			rtype:"html",
			init:false,
			//interval:15000,
			prepare:function(data) {
				LogHelper.debug(this,"INCOMING-->data="+data);
				return app.controller.prepareMonitorData(data);
			},
			models:["monitor.server"]
	    },
		//sync Monitor Data : Application
	    "app.monitor": {
	    	url:function() {
				return "../../rs/godeye/monitor/q/hm";
			},
			//POST DATA BODY
			data:function() {
				return app.controller.buildAppMonitorQuery("app");
			},
			method:"POST",
			rtype:"html",
			init:false,
			//interval:monitorCfg.app.syncInterval,
			prepare:function(data) {
				LogHelper.debug(this,"INCOMING-->data="+data);
				return app.controller.prepareMonitorData(data,"app");
			},
			error:function(data) {
				LogHelper.debug(this,"INCOMING ERR-->data="+StringHelper.obj2str(data));
			},
			models:["monitor.app"]
	    },
	    //sync Monitor Data : Service URL
	    "url.monitor": {
	    	url:function() {
				return "../../rs/godeye/monitor/q/hm";
			},
			//POST DATA BODY
			data:function() {
				return app.controller.buildAppMonitorQuery("url");
			},
			method:"POST",
			rtype:"html",
			init:false,
			//interval:15000,
			prepare:function(data) {
				LogHelper.debug(this,"INCOMING-->data="+data);
				return app.controller.prepareMonitorData(data);
			},
			models:["monitor.url"]
	    },
	     //sync Monitor Data : Client URL
	    "client.monitor": {
	    	url:function() {
				return "../../rs/godeye/monitor/q/hm";
			},
			//POST DATA BODY
			data:function() {
				return app.controller.buildAppMonitorQuery("client");
			},
			method:"POST",
			rtype:"html",
			init:false,
			//interval:15000,
			prepare:function(data) {
				LogHelper.debug(this,"INCOMING-->data="+data);
				return app.controller.prepareMonitorData(data);
			},
			models:["monitor.client"]
	    },
	    //sync Monitor Data : Customized Metrics
	    "cust.monitor": {
	    	url:function() {
				return "../../rs/godeye/monitor/q/hm";
			},
			//POST DATA BODY
			data:function() {
				return app.controller.buildAppMonitorQuery("cust");
			},
			method:"POST",
			rtype:"html",
			init:false,
			//interval:15000,
			prepare:function(data) {
				LogHelper.debug(this,"INCOMING-->data="+data);
				return app.controller.prepareMonitorData(data);
			},
			models:["monitor.cust"]
	    }
	},
	search:{
		//bind model to search
		model:"profile",
		tip:"以应用组,应用名或应用ID检索"
	},
	/**
	 * TODO: models
	 */
	models:{
		//app profile info
		"profile":{
			//-------------数据事件--------------
			isupdate:function(oldObj,newObj) {
				return true;
			},
			onnew:function(dObj) {				
				app.controller.onnew(dObj);
			},
			onupdate:function(dObj) {
				app.controller.onupdate(dObj);
			},
			ondel:function(dObj) {
				app.controller.ondel(dObj);
			},
			//获得所有最新数据后
			onrefresh:function(pdlist) {
				
				//do groups
				window["tablistmgr"].doGroups("appTabList");
				/**
				 * NOTE: only monitorCfg.isautorefresh=true, refresh the windows
				 */
				if (monitorCfg.isautorefresh==true) {
					//refresh app monitor data
					setTimeout(function(){
						app.refresh("app.monitor");
						},10);
				}
			},
			//--------------元数据定义-----------------
			//primary key
			pkey:"id",
			//filter keys for search
			fkeys:["appgroup","appid","appname"],
			//--------------字段映射-------------------
			id:"id",
			appgpid:{key:"appid",
				create:function(newObj) {
					
					return newObj["appgroup"]+":"+newObj["appid"];
				},
				update:function(oldObj,newObj) {

					return newObj["appgroup"]+":"+newObj["appid"];
				}
			},
			//client need this as mark tag
			hostport:{key:"appurl",
				create:function(newObj) {
					var appurl=newObj["appurl"];
					
					if (appurl==undefined) {
						return "";
					}
					
					return appurl.split("/")[2];
				},
				update:function(oldObj,newObj) {
					var appurl=newObj["appurl"];
					
					if (appurl==undefined) {
						appurl=oldObj["appurl"];
						if (appurl==undefined) {
							return "";
						}
					}
					
					return appurl.split("/")[2];
				}
			},
			appid:"appid",
			ip:"ip",
			svrid:"svrid",
			host:"host",
			time:"time",
			appname:"appname",
			appdes:"appdes",
			appgroup:"appgroup",
			appmetrics:"appmetrics",
			webapproot:"webapproot",
			mofmeta:"mofmeta",
			//原生的appurl，带有最后一个/,与cache保持一致
			o_appurl:"appurl",
			appurl:{key:"appurl",
				/**
				 * in order to match the monitor data, remove the last "/" for appurl
				 */
				formatAppURL:function(newObj) {
					var appurl=newObj["appurl"];
					
					if (appurl==undefined) {
						return appurl;
					}
					 
					 var appinfo=appurl.split("/");
					 
					 if (appinfo.length>4) {
						 return appurl.substring(0,appurl.length-1); 
					 }
					 else {
						 return appurl;
					 }
				},
				create:function(newObj) {
					
					return this.formatAppURL(newObj);
					
				},
				update:function(oldObj,newObj) {

					return this.formatAppURL(newObj);
				}
			},
			"jars.lib":"jars.lib",
			"logs.log4j":"logs.log4j",
			"cpt.servlets":"cpt.servlets",
			"cpt.filters":"cpt.filters",
			"cpt.listeners":"cpt.listeners",
			"cpt.jaxws":"cpt.jaxws",
			"cpt.jaxwsP":"cpt.jaxwsP",
			"cpt.jaxrs":"cpt.jaxrs",
			"cpt.springmvc":"cpt.springmvc",
			"cpt.springmvcRest":"cpt.springmvcRest",
			"cpt.struts2":"cpt.struts2",
			 state:"state",
			"cpt.services":"cpt.services",
			"cpt.clients":"cpt.clients",
			"cpt.mscp.http":"cpt.mscp.http",
			"cpt.mscp.timework":"cpt.mscp.timework",
			"cpt.dubbo.provider":"cpt.dubbo.provider",
			"service.ports":{
				key:"cpt.services",
				buildServicePorts:function(obj) {
					
					var services=eval("("+obj["cpt.services"]+")");
					
					var sports={};
					var hasPort=false;
					for(var skey in services) {
						
						var sUrls=services[skey];
						
						for(var i=0;i<sUrls.length;i++) {
							var url=sUrls[i];
														
							var urlInfo=url.split(":");
							
							//if is not url
							if (urlInfo.length<3) {
								continue;
							}
							
							var pIndex=urlInfo[2].indexOf("/");
							var port="NA";
							//dubbo
							if (pIndex==-1) {
								port=urlInfo[2];
							}
							//http/https
							else {
								port=urlInfo[2].substring(0,pIndex);
							}
							
							var sport=urlInfo[0]+":"+urlInfo[1]+":"+port;
							
							sports[sport]={port:port,ip:urlInfo[1].substring(2),appid:obj["appid"],compid:skey};
							hasPort=true;
						}
					}
					
					/**
					 * if there is no servlets profiling (may be something wrong), we need use appurl as the main service port
					 */
					if (hasPort==false&&obj["appurl"]!=undefined&&obj["appurl"].indexOf("http")==0) {
						var urlInfo=obj["appurl"].split(":");
						if (urlInfo.length<3) {
							return undefined;
						}
						var pIndex=urlInfo[2].indexOf("/");
						var port=urlInfo[2].substring(0,pIndex);
						var sport=urlInfo[0]+":"+urlInfo[1]+":"+port;
						sports[sport]={port:port,ip:urlInfo[1].substring(2),appid:obj["appid"],compid:"main"};
						hasPort=true;
					}
					
					if (hasPort==false) {
						return undefined;
					}
					
					return sports;
				},
				create:function(newObj) {
					return this.buildServicePorts(newObj);
				},
				update:function(oldObj,newObj) {
					if (newObj==undefined) {
						return this.buildServicePorts(oldObj);
					}
					else {
						return this.buildServicePorts(newObj);
					}
				}
			}
		},
		//app monitor data
		"monitor.server": {
			//-------------数据事件--------------
			isupdate:function(oldObj,newObj) {
				return true;
			},
			//获得所有最新数据后
			onrefresh:function(pdlist) {
				app.controller.updateAppServerMOState(pdlist);
			},
			//--------------元数据定义-----------------
			//primary key
			pkey:"id",
			//--------------字段映射-------------------
			id:"id",
			ip:"ip",
			pgid:"pgid",
			metric:{
				key:"metric",
				/**
				 * NOTE: when the object is new
				 */
				create:function(newObj) {
					return app.controller.prepareStatData(newObj);
				},
				/**
				 * NOTE: when the object is update
				 */
				update:function(oldObj,newObj) {
					return app.controller.prepareStatData(newObj);
				}
			},
			//时间段内的吞吐量,目前按照QPM
			tps:"",
			//时间段内的响应时间
			tavg:"",
			//1min时间段内的响应时间
			mavg:"",
			//时间段内的错误个数
			err:"",
			//时间段内的访问次数
			count:"",
			//刷新时间戳
			timestamp:""
		},
		//app group(including all app inst for one appid) monitor data
		"monitor.appgp":{
			//-------------数据事件--------------
			isupdate:function(oldObj,newObj) {
				return true;
			},
			//--------------元数据定义-----------------
			//primary key
			pkey:"id",
			//--------------字段映射-------------------
			id:{key:"appid",
				create:function(newObj) {
					
					return newObj["appgroup"]+":"+newObj["appid"];
				},
				update:function(oldObj,newObj) {

					return newObj["appgroup"]+":"+newObj["appid"];
				}
			},
			appgroup:"appgroup",
			appid:"appid",
			ip:"ip",
			svrid:"svrid",
			//应用实例个数
			inum:"inum",
			time:"time",
			//JEE
			count:"count",
			err:"err",
			tavg:"tavg",
			tps:"tps",
			//JSE
			jse_tpm:"jse_tpm",
			jse_hpm:"jse_hpm",
			jse_heapmax:"jse_heapmax",
			//刷新时间戳
			timestamp:""
		},
		//app monitor data
		"monitor.app": {
			//-------------数据事件--------------
			isupdate:function(oldObj,newObj) {
				return true;
			},
			onnew:function(dObj) {				
				
			},
			onupdate:function(dObj) {
				
			},
			ondel:function(dObj) {
				
			},
			//获得所有最新数据后
			onrefresh:function(pdlist) {
				//刷新AppBox的UI
				app.controller.updateAppNodeMOState(pdlist);
				
				//启动图谱的Edge的刷新
				appStream.startAppEdgeMoUpdate();
				
				//完成openlink事件
				openLink.runWindowDelay("monitorapp");
			},
			//--------------元数据定义-----------------
			//primary key
			pkey:"id",
			//--------------字段映射-------------------
			//instance id
			id:"id",
			//appgroup
			appgroup:"appgroup",
			//ip
			ip:"ip",
			//server id
			pgid:"pgid",
			// metrics
			metric:{
				key:"metric",
				/**
				 * NOTE: when the object is new
				 */
				create:function(newObj) {
					return app.controller.prepareStatData(newObj);
				},
				update:function(oldObj,newObj) {
					return app.controller.prepareStatData(newObj);
				}
			},
			//JEE
			//时间段内的吞吐量,目前按照QPM
			tps:"tps",
			//时间段内的响应时间
			tavg:"tavg",
			//时间段内的错误个数
			err:"err",
			//时间段内的访问次数
			count:"count",
			//当前分钟响应时间
			mavg:"mavg",
			//JSE
			jse_tpm:"jse_tpm",
			jse_hpm:"jse_hpm",
			jse_heapmax:"jse_heapmax",
			//刷新时间戳
			timestamp:""
			
		},
		//app monitor serivce
		"monitor.url": {
			//-------------数据事件--------------
			isupdate:function(oldObj,newObj) {
				return true;
			},
			//获得所有最新数据后
			onrefresh:function(pdlist) {
				//刷新AppURL的UI
				app.controller.updateAppURLMOState(pdlist);
			},
			//--------------元数据定义-----------------
			//primary key
			pkey:"id",
			//--------------字段映射-------------------
			id:"id",
			ip:"ip",
			pgid:"pgid",
			metric:{
				key:"metric",	
				/**
				 * NOTE: when the object is new
				 */
				create:function(newObj) {
					return app.controller.prepareStatData(newObj);
				},
				update:function(oldObj,newObj) {
					return app.controller.prepareStatData(newObj);
				}
			},
			//时间段内的吞吐量,目前按照QPM
			tps:"",
			//时间段内的响应时间
			tavg:"",
			//1min时间段内的响应时间
			mavg:"",
			//时间段内的错误个数
			err:"",
			//时间段内的访问次数
			count:"",
			//刷新时间戳
			timestamp:""	
		},
		//app monitor client
		"monitor.client": {
			//-------------数据事件--------------
			isupdate:function(oldObj,newObj) {
				return true;
			},
			//获得所有最新数据后
			onrefresh:function(pdlist) {
				//刷新AppURL的UI
				app.controller.updateAppClientURLMOState(pdlist);
				
				//刷新图谱的Edge
				appStream.onAppEdgeMoUpdate(pdlist);
			},
			//--------------元数据定义-----------------
			//primary key
			pkey:"id",
			//--------------字段映射-------------------
			id:"id",
			ip:"ip",
			pgid:"pgid",
			metric:{
				key:"metric",	
				/**
				 * NOTE: when the object is new
				 */
				create:function(newObj) {
					var mc=app.controller.prepareStatData(newObj);
//					
//					//take the tps>0 as the latest timestamp
//					if (newObj["tps"]>0) {
//						newObj["timestamp"]=new Date().getTime();
//					}			
//					
					return mc;
				},
				update:function(oldObj,newObj) {
					var mc=app.controller.prepareStatData(newObj);
//					
//					if (oldObj["tps"]!=newObj["tps"]&&newObj["tps"]>0) {
//						newObj["timestamp"]=new Date().getTime();
//					}
//					else {
//						if (oldObj["timestamp"]!=undefined) {
//							newObj["timestamp"]=oldObj["timestamp"];
//						}
//					}
//					
					return mc;
				}
			},
			//刷新时间戳
			timestamp:"",			
			//时间段内的吞吐量,目前按照QPM
			tps:"",
			//时间段内的响应时间
			tavg:"",
			//1min时间段内的响应时间
			mavg:"",
			//时间段内的错误个数
			err:"",
			//时间段内的访问次数
			count:""				
		},
		//app customized metrics monitor
		"monitor.cust": {
			//-------------数据事件--------------
			isupdate:function(oldObj,newObj) {
				return true;
			},
			//获得所有最新数据后
			onrefresh:function(pdlist) {
				//刷新App Cust Metric的UI
				app.controller.updateAppCustMetricMOState(pdlist);
			},
			//--------------元数据定义-----------------
			//primary key
			pkey:"id",
			//--------------字段映射-------------------
			id:"id",
			ip:"ip",
			pgid:"pgid",
			metric:{
				key:"metric",	
				/**
				 * NOTE: when the object is new
				 */
				create:function(newObj) {
					return app.controller.prepareStatData(newObj);
				},
				update:function(oldObj,newObj) {
					return app.controller.prepareStatData(newObj);
				}
			}
		}
	},
	/**
	 * TODO: controller
	 */
	controller:{
		//----------------------COMMON--------------------------
		/**
		 * TODO: delay open
		 */
		showWindowDelay:function(iObj,winId,func,callback,tag) {
			var info={iObj:iObj,winId:winId,func:func,callback:callback,tag:tag};
			window["openlink_"+tag]=info;
		},
		/**
		 * TODO: controller COMMON
		 */
		scrollYLocation:0,
	    showWindow:function(iObj,winId,func,callback) {
	    	this.scrollYLocation=HtmlHelper.id("AppList")["scrollTop"];
	    	
	    	var content=this[func](iObj);
	    	window.winmgr.content(winId,content);
		    window.winmgr.show(winId);
	    	HtmlHelper.css("AppList",{"scrollTop":0});
	    	
			if(callback){
			   this[callback](iObj);
			}
	    },
	    closeWindow:function(winId,callback,backToWndId) {
	    	window.winmgr.hide(winId);	
	    	if(backToWndId==undefined) {
	    		window.winmgr.show("AppList");
	    	}
	    	else {
	    		window.winmgr.show(backToWndId);
	    	}
	    	HtmlHelper.css("AppList",{"scrollTop":this.scrollYLocation});
			if(callback){
			   this[callback]();
			}
	    },
	    openClose:function(id) {
	    	var obj=HtmlHelper.id(id);
	    	if (obj.style.display=="none") {
	    		obj.style.display="block";
	    		return true;
	    	}
	    	else {
	    		obj.style.display="none";
	    		return false;
	    	}
	    },
	    //-----------App Profile List----------------
		/**
		 * TODO: App Profile List
		 */
		onnew:function(d) {
			window.tablistmgr.load("appTabList", [d]);
			appStream.onAppNew(d);
		},
		onupdate:function(d) {
			window.tablistmgr.load("appTabList", [d]);
			appStream.onAppUpdate(d);
		},
		ondel:function(d) {
			
			var isJse=this.isJSE(d.appurl);
			
			var appInstURL;
			// JSE
			if (isJse==true) {
				appInstURL=d.appurl;
			}
			// JEE
			else {
				appInstURL=d.appurl+"---"+d.appid;
			}
			
			//delete instance
			HtmlHelper.del(appInstURL);
			//delete app if no instance exists
			var insts=HtmlHelper.id(d.appgpid+"_insts");
			if (insts!=undefined&&insts.innerHTML=="") {
				
				window.tablistmgr.del("appTabList",[d.appgpid]);
			}
			
			appStream.onAppDel(d);
		},	
		//----------------------App Profile COMMON--------------------------
		/**
		 * TODO: App Profile COMMON
		 */
		//追加appname tags，不同实例可能名字不同
		getAppTags:function(id,appname) {
			var tags=HtmlHelper.id(id);
			
			if (undefined==tags) {
				return appname;
			}
			
			var t=HtmlHelper.decode(tags.innerHTML);			
			
			if (t.indexOf(appname)==-1) {
				return t+","+appname;
			}
			else {
				return t;
			}
		},
		getAppInstIPPort:function(appurl) {
			var ed=appurl.lastIndexOf("/");
			var st=0;
			return appurl.substring(st,ed);
		},
		//get node state css class
	    getNodeStateCss : function(state,active){
	    	
	        if(!state){
	            return "dying";
	        }
	        var i = parseInt(state);
	        
	        if(i == 0){
	        	if (active==true) {
	        		return "dyingA";
	        	}
	        	else
	        		return "dying";
	        }else if(i>0){
	        	if (active==true) {
	        		return "liveA";
	        	}
	        	else {
	        		return "live";
	        	}
	        }else if(i<0){
	        	if (active==true) {
	        		return "deadA";
	        	}
	        	else {
	        		return "dead";
	        	}
	        }
	    },
	    //change perf value
		changePerfVal : function(id,moRate,value,tag){
	        
			//柱子显示
			var div = HtmlHelper.id(id+"_T");
	        if (div!=undefined) {
		        var color;
		        if(moRate.level==0){
		            color ="#00FF33";
		        }else if(moRate.level==1){
		            color ="orange";
		        }else if(moRate.level==2){
		            color ="red";
		        }
		        
		        div.style.height = (moRate.rate*100)+"%";
		        div.style.backgroundColor = color;
	        }
	        
	        //数字显示
	        var divT = HtmlHelper.id(id);
	        if (divT!=undefined) {
		        var tagStr=(tag==undefined)?"":tag;
		        divT.innerHTML= (value == undefined)? "-":value+tagStr;
	        }
	    },
	    //----------------------App Monitor COMMON----------------
	    
	    /**
		 * TODO: App Monitor COMMON
		 */
	    /**
	     * Convert MonitorData to MVC objects
	     */
	    prepareMonitorData:function(data,type) {
	    	
	    	var jsonData = eval("("+data+")");
	    	
	    	jsonData=jsonData["rs"];
	    	
	    	jsonData=eval(jsonData);
			 
			 /**
			  * build the dataMap based on instId
			  */
			 var dataMap={};
			 
			 for (var i=0;i<jsonData.length;i++) {
				 
				 var md=jsonData[i];
				 
				 var id=md["tags"]["instid"];
				 var ip=md["tags"]["ip"];
				 var pgid=md["tags"]["pgid"];	
				 
				 var metric=md["metric"];
				 var dps=md["dps"];
				 
				 var appO=dataMap[id];
				 
				 if (appO==undefined) {
					 appO={
							 id:id,
							 ip:ip,
							 pgid:pgid,
							 metric:{}
					 };
					 
					 /**
					  * Application need auto group
					  */
					 if (type=="app") {
						 var appid;
						 var index=id.lastIndexOf("---");

						 // for jee,mscp,springboot
						 if(index!=-1){
							 appid=id.substring(index+3,id.length);
						 }

						 // for jse
						 if(appid==undefined){
							 index=id.lastIndexOf("-");
							 appid=id.substring(0,index).split("/").pop();
						 }

						 var appgroup=monitorCfg.app.ipToAppGroup[appO["ip"]][appid];
						 
						 if (appgroup!=undefined) {
							 appO["appgroup"]=appgroup;
						 }
					 }
					 
					 dataMap[id]=appO;
				 }
				 /**
				  * NOTE: RC is for RC400,RC500,RC404....
				  */
				 if(metric.indexOf(".RC")>-1||metric.indexOf(".AC")>-1) {
					 metric=md["tags"]["ptag"]; 
				 }else {
					 metric=metric.split(".")[1];
				 }
				 
				 appO["metric"][metric]=dps;					 
			 }
			 
			 return dataMap;
	    },	
	    /**
		 * 计算monitorCfg.app.syncInterval 时间段内的吞吐量，响应时间，错误个数
		 */
	    prepareStatData:function(newObj) {
	    	
	    	if (newObj==undefined) {
	    		return newObj;
	    	}
	    	
			var mc=newObj["metric"];
			
			if (mc==undefined) {
				return newObj;
			}
			
			/**
			 * JEE
			 */
			//错误个数,累积值
			if (mc["err"]!=undefined) {
				newObj["err"]=app.controller.getMODeta(mc["err"]);
			}
			
			//响应时间，平均值
			if (mc["tavg"]!=undefined) {
				newObj["tavg"]=app.controller.getMOAvgDeta(mc["tavg"]);	
			}
			
			//访问次数
			if (mc["count"]!=undefined) {
				var tps=app.controller.getMODeta(mc["count"]);
				newObj["count"]=tps;
				//吞吐量,目前按照QPM（标准值）
				newObj["tps"]=Math.floor(tps*60000/monitorCfg.app.syncInterval);
				
				if(mc["tsum"]!=undefined&&tps>0) {
					var tsumgap=app.controller.getMODeta(mc["tsum"]);
					newObj["mavg"]=Math.floor(tsumgap/tps);
				}
				
				if (newObj["tps"]>0) {
					newObj["timestamp"]=app.controller.getMOAccessTS(mc["count"]);
				}
			}
			
			/**
			 * JSE
			 */
			//单位时间的线程增发量
			if (mc["thread_started"]!=undefined) {
			    var tpm=app.controller.getMODeta(mc["thread_started"]);
				//吞吐量,目前按照QPM（标准值）
				newObj["jse_tpm"]=Math.floor(tpm*60000/monitorCfg.app.syncInterval);
			}
			//单位时间内Heap改变量
			if (mc["heap_use"]!=undefined) {
				var hpm=app.controller.getMODeta(mc["heap_use"],true);
				newObj["jse_hpm"]=Math.floor(hpm*60000/monitorCfg.app.syncInterval);
				newObj["jse_hpm"]=CommonHelper.getB2Human(newObj["jse_hpm"],false,2);
				newObj["jse_heapmax"]=CommonHelper.getB2Human(app.controller.getOneMOData(mc["heap_max"],0),false,2);
			}
			
			return mc;
	    },
	    //help to encode as OpenTSDB required
	    encodeForOpenTSDB:function(s) {
	    	s=s.replace(/:/g,"/u003a").replace(/#/g, "/u0023").replace(/%/g, "/u0025").replace(/\+/g, "/u002B")
            .replace(/;/g, "/u003b").replace(/=/g, "/u003d").replace(/!/g, "/u0021").replace(/@/g, "/u0040")
            .replace(/\$/g, "/u0024").replace(/\^/g, "/u005e").replace(/&/g, "/u0026").replace(/\*/g, "/u002a")
            .replace(/\(/g, "/u0028").replace(/\)/g, "/u0029")
            .replace(/\{/g, "/u007b").replace(/\}/g, "/u007d").replace(/\[/g, "/u005b").replace(/\]/g, "/u005d")
            .replace(/\\/g, "/u005c").replace(/\|/g, "/u007c").replace(/"/g, "/u0022").replace(/'/g, "/u0027")
            .replace(/</g, "/u003c").replace(/,/g, "/u002c").replace(/>/g, "/u003e").replace(/\?/g, "/u003f");
	    	return s;
	    },
	    //get app group model data
	    getAppGPData:function(appgpid) {
	    	
	    	var appgps=app.mdata("monitor.appgp");
	    	
	    	for (var key in appgps) {
	    		var appgp=appgps[key];
	    		if (appgp["id"]==appgpid) {
	    			return appgp;
	    		}
	    	}
	    	
	    	return undefined;
	    },
	    /**
	     * NOTE: the very beginning start time is when you enter the app,
	     * then the app uses this time as the base start time,
	     * then after a certain syncInterval, the new start time is auto generated.
	     * so we can keep every metrics are viewed in the same time range, current time span is 1 minute
	     */
	    getCurrentTimeRange:function(type) {
	    	
	    	var timeRange={
	    		startTime:0,
	    		endTime:0
	    	}
	    	
	    	var startTime=monitorCfg.startTime;
	    	
	    	if (monitorCfg[type].startTime!=undefined) {
	    		startTime=monitorCfg[type].startTime;
	    	}
	    	
	    	if (startTime==undefined) {
	    		startTime=new Date().getTime();
	    	}
	    	else {
	    		var timespan=new Date().getTime()-startTime;
	    		
	    		if (timespan>=monitorCfg[type].syncInterval) {
	    			startTime+=monitorCfg[type].syncInterval;
	    		}
	    	}
	    	
	    	timeRange.startTime=startTime-monitorCfg[type].syncInterval;
	    	timeRange.endTime=startTime;
	    	
	    	if (monitorCfg[type].startTime!=undefined) {
	    		monitorCfg[type].startTime=startTime;
	    	}
	    	else {
	    		monitorCfg.startTime=startTime;
	    	}
	    	
	    	return timeRange;
	    },
	    /**
	     * for App Monitor Query 
	     */
	    buildAppMonitorQuery:function(type) {
	    	/**
	    	 * Step 1: get all app profile data
	    	 */
	    	var timeRange=this.getCurrentTimeRange(type);
	    	
	    	//OPENTSDB query
	    	var query={
	    		"start": timeRange.startTime,
	    		"end":  timeRange.endTime,
	    		"queries": []
	    	};
	    	
	    	var pdata={};
	    	
	    	if (type=="app") {
	    		pdata=app.mdata("profile");
	    		
	    		/**
	    		 * check if one app instance window is open and try to query history data
	    		 */
	    		if (monitorCfg.app["id"]!=undefined&&monitorCfg["app"].isusecustomized==true) {
	    			pdata=[pdata[monitorCfg.app["id"]]];
	    		}
	    	}
	    	else if(type=="url") {
	    		pdata=monitorCfg.url.urls;
	    	}
	    	else if (type=="client") {
	    		pdata=monitorCfg.client.urls;
	    	}
	    	else if (type=="server") {
	    		pdata=[monitorCfg.server.inst];
	    	}
	    	else if (type=="cust") {
	    		pdata=[monitorCfg.cust.inst];
	    	}
	    	
	    	if (pdata==undefined) {
	    		return undefined;
	    	}
	    	
	    	for (var key in pdata) {
	    		
	    		var appObj=pdata[key];
	    		
	    		if (typeof appObj=="function") {
	    			continue;
	    		}
	    		
	    		var ip="";
    			var pgid="";
    			var instid="";
    			
    			/**
    			 * NOTE: JEE & JSE are different
    			 */
	    		if (type=="app") {
	    			
	    			ip=this.encodeForOpenTSDB(appObj["ip"]);
	    			pgid=this.encodeForOpenTSDB(appObj["svrid"]);
	    			
	    			var appurl=appObj["appurl"];
	    			
	    			var isJse=this.isJSE(appurl);
	    			
	    			//JSE
	    			if (isJse==true) {
	    				
	    				instid=this.encodeForOpenTSDB(appurl);
	    				
	    				this.buildOpenTSDBQuery(monitorCfg.metrics.jseperf, "jvm", query, ip, pgid, instid);
	    				
	    				continue;
	    			}
	    			//JEE
	    			else {
	    				var appid=appObj["appid"];
		    			
		    			instid=this.encodeForOpenTSDB(appurl+"---"+appid);
	    			}
	    		}
	    		else if (type=="url") {
	    			
	    			instid=this.encodeForOpenTSDB(appObj);
	    			pgid=this.encodeForOpenTSDB(monitorCfg.url.svrid);
	    			ip=this.encodeForOpenTSDB(monitorCfg.url.ip);
	    		}
	    		else if (type=="client") {
	    			
	    			instid=this.encodeForOpenTSDB(appObj["url"]);
	    			ip=this.encodeForOpenTSDB(appObj["ip"]);
	    			pgid=this.encodeForOpenTSDB(appObj["svrid"]);
	    			
	    		}
	    		else if(type=="server") {
	    			instid=this.encodeForOpenTSDB(appObj);
	    			pgid=this.encodeForOpenTSDB(monitorCfg.server.svrid);
	    			ip=this.encodeForOpenTSDB(monitorCfg.server.ip);    			
	    		}
	    		else if (type=="cust") {
	    			instid=this.encodeForOpenTSDB(appObj);
	    			pgid=this.encodeForOpenTSDB(monitorCfg.cust.svrid);
	    			ip=this.encodeForOpenTSDB(monitorCfg.cust.ip);
	    			
	    			//build customized metrics query
	    			for(var mc in monitorCfg.cust.metrics) {
	    				
	    				var subquery={
		    		            "aggregator": "avg",
		    		            "metric": "jvm."+mc,
		    		            "tags": {
		    		                "ip": ip,
		    		                "pgid": pgid,
		    		                "instid":instid
		    		            }
	    				};
	    				
	    				query["queries"].push(subquery);
	    			}
	    			
	    			continue;
	    		}
	    		
	    		this.buildOpenTSDBQuery(monitorCfg.metrics.perf, type+"Resp", query, ip, pgid, instid);
	    			    		
	    		/**
	    		 * add jvm metrics for app server
	    		 */
	    		if(type!="server") {
	    			continue;
	    		}
	    		
	    		this.buildOpenTSDBQuery(monitorCfg.metrics.jvm, "jvm", query, ip, pgid, instid);
	    	}
	    	
	    	var q= JSON.stringify(query);
    	
	    	return q;
	    },
	    /**
	     * 创建OpenTSDBQuery
	     */
	    buildOpenTSDBQuery:function(metrics,mprefix,query,ip,pgid,instid) {
	    	
	    	for(var i=0;i<metrics.length;i++) {
    			
    			var metric=metrics[i];
    			
    			if(mprefix=="clientResp"&&metric=="RC"||mprefix!="clientResp"&&metric=="AC"){
    				continue;
    			}
    			
	    		var subquery={
	    		            "aggregator": "avg",
	    		            "metric": mprefix+"."+metric,
	    		            "tags": {
	    		                "ip": ip,
	    		                "pgid": pgid,
	    		                "instid":instid
	    		            }
	    		};
	    		
	    		if (metric=="RC"||metric=="AC"||metric=="EXT") {
	    			subquery.tags["ptag"]="*";
	    		}
	    		
	    		query["queries"].push(subquery);
    		}
	    },
	    /**
	     * 获得一个值即可
	     */
	    getOneMOData:function(mcData,index) {
	    	if (!mcData) {
				return 0;
			}
	    	
	    	var i=0;
	    	for(var timeStampStr in mcData) {
	    		
	    		if (i==index) {
	    			return mcData[timeStampStr];
	    		}
	    		
	    		i++;
	    		
	    	}
	    	
	    	return 0;
	    },
	    /**
	     * 找到性能指标变化的最后一个值，它的时间戳就是最近访问时间
	     */
	    getMOAccessTS:function(mcData) {
	    	if (mcData==undefined) {
	    		return "";
	    	}
	    	
	    	var lastTS="";
	    	var lastVal=-1;
	    	
	    	for(var timeStampStr in mcData) {
	    		
	    		var curVal=mcData[timeStampStr];
	    		
	    		if (curVal!=lastVal) {
	    			lastVal=curVal;
	    			lastTS=parseInt(timeStampStr);
	    		}
	    	}
	    	
	    	if (lastTS!="") {
	    		lastTS=TimeHelper.getTime(lastTS);
	    	}
	    	
	    	return lastTS;
	    },
	    /**
		 * 找到最新和最旧的值相减即可 以及最后一个值
		 */
	    getMODetaWithLast:function(mcData,allowMinus) {
	    	
	    	if (!mcData) {
				return [0,0];
			}
			
			var firstTimeStamp=-1;
			var lastTimeStamp=-1;
			var index=-1;
			for(var timeStampStr in mcData) {
				
				var timeStamp=parseInt(timeStampStr);
				
				index++;
				
				if (index==0) {
					firstTimeStamp=timeStamp;
					lastTimeStamp=timeStamp;
					
					continue;
				}
				
				
				if (timeStamp>lastTimeStamp) {
					lastTimeStamp=timeStamp;
				}
				
				if (timeStamp<firstTimeStamp) {
					firstTimeStamp=timeStamp;
				}
			}
			
			if (lastTimeStamp==-1&&firstTimeStamp==-1) {
				return [0,0];
			}
			
			var last=mcData[lastTimeStamp+""];
			var deta=last-mcData[firstTimeStamp+""];	
			
			/**
			 * NOTE: there should be a "跳变"(such as a Server restart) lead that deta <0
			 * 
			 */
			if (deta<0&&(allowMinus==undefined||allowMinus==false)) {
				deta=0;
			}
			
			return [deta,last];
	    },
	    /**
		 * 找到最新和最旧的值相减即可
		 */
	    getMODeta:function(mcData,allowMinus) {
			return this.getMODetaWithLast(mcData, allowMinus)[0];
		},
		
		/**
		 * 取平均值
		 */
		getMOAvgDeta:function(mcData) {
			if (!mcData) {
				return 0;
			}
			
			var sum=0;
			var count=0;
			for(var timeStampStr in mcData) {
				sum+=mcData[timeStampStr];
				count++;
			}
			if (count>0&&sum>0) {
				var avg=Math.round(sum/count);
			
				return avg;
			}
			else {
				return 0;
			}
		},
		
		/**
		 * 获取吞吐量，错误，响应时间的百分比 以及 警示级别: 0正常，1警告，2严重
		 */
		getMORate:function(type,val,valTotal) {
			
			var moRate={
					rate:0,
					level:0,
					value:val,
					total:valTotal
			};
			
			var rate,level;
			
			if (type=="err") {
				if (valTotal>0) {
					rate=val/valTotal;
				}
				else {
					rate=0;
				}
				
				if (rate<=0.02) {
					level=0;
				}
				else if (rate>0.02&&rate<=0.05) {
					level=1;
				}
				else if (rate>0.05) {
					level=2;
				}
			}
			
			if (type=="tavg") {
				//10s is very bad
				rate=val/10000;	
				
				if (val<=2000) {
					level=0;
				}
				else if (val>2000&&val<=5000) {
					level=1;
				}
				else if (val>5000) {
					level=2;
				}
			}
			
			if (type=="mavg") {
				//5s is very bad
				rate=val/5000;	
				
				if (val<=1000) {
					level=0;
				}
				else if (val>1000&&val<=2000) {
					level=1;
				}
				else if (val>2000) {
					level=2;
				}
			}
			
			if (type=="tps") {
				rate=val/5000;	
				
				if (val<=600) {
					level=0;
				}
				else if (val>600&&val<=3000) {
					level=1;
				}
				else if (val>3000) {
					level=2;
				}
			}
			
			if (type=="jse_tpm") {
				rate=val/200;	
				
				if (val<=20) {
					level=0;
				}
				else if (val>20&&val<=100) {
					level=1;
				}
				else if (val>100) {
					level=2;
				}
			}
			
			if (type=="jse_hpm") {
				rate=val/valTotal;
				
				if (rate<=0.1) {
					level=0;
				}
				else if (rate>0.1&&rate<=0.3) {
					level=1;
				}
				else if (rate>0.3) {
					level=2;
				}
			}
			
			if (rate>1) {
				rate=1;
			}
			
			moRate["rate"]=rate;
			moRate["level"]=level;
			
			return moRate;
		},
		/**
		 * NOTE: 将OpenTSDB的数据格式转换成Array形式
		 */
		convertDPStoArray:function(dps,formatter) {
			
			if (dps==undefined) {
				return [];
			}
			
			var array=[];
			var index=0;
			for(var key in dps) {
				
				
				var timestamp=parseInt(key);
				
				/**
				 * NOTE: as the OPENTSDB only has 10 digits, we have to * 1000, the normal ms is 13 digits
				 */
				if (key.length==10) {
					timestamp=timestamp*1000;
				}
				
				var yvalue=(formatter!=undefined)?formatter(index,dps[key]):dps[key];
				
				var elem={x:timestamp,y:yvalue};
				
				array[index]=elem;
				
				index++;
			}
			return array;
		},
		/**
		 * safe to get the perf data
		 */
		getPerfData:function(perfObj,perfMetric) {
			return (perfObj==undefined||perfObj[perfMetric]==undefined)?"-":perfObj[perfMetric];
		},
		/**
		 * 为Chart准备监控数据，并自动装配
		 */
		prepareChartData:function(chartCfg,chartDataHolder,moData,formatter) {
			for(var key in chartCfg.seriesMap) {
				var index=chartCfg.seriesMap[key];
				chartDataHolder[index]=this.convertDPStoArray(moData["metric"][key],formatter);
			}
		},
		/**
		 * 格式化Chart数据
		 */
		formatChartData:function(data,formatter) {
			
		},
		/**
		 * check if the app is JSE
		 */
		isJSE:function(tag) {
			
			if (tag==undefined) {
				return "";
			}
			//http is for JEE Application, config is for MSCP Application
			return (tag.indexOf("jse:")>-1||(tag.indexOf("http")==-1&&tag.indexOf("---")==-1&&tag.indexOf("config")==-1))?true:false;
		},
		/**
		 * get app tags and insts by appgpid
		 */
		getProfileTagsAndInsts:function(appgpid) {
			
			var mdata=app.mdata("profile");
			
			var appinsts=[];
			
			var tags="";
			
			for(var key in mdata) {
				var appinst=mdata[key];
				if (appinst.appgpid==appgpid) {
					appinsts[appinsts.length]=appinst;
					
					if (tags.indexOf(appinst.appname)==-1) {
						tags+=appinst.appname;
					}
				}
			}
			
			return {tags:tags,appinsts:appinsts};
		},
		/**
		 * get profile data by appgroup
		 */
		getProfileByAppGroup:function(appgroup) {
			
			var mdata=app.mdata("profile");
			
			var appinsts=[];
			
			for(var key in mdata) {
				var appinst=mdata[key];
				if (appgroup!=undefined) {
					if (appinst.appgroup==appgroup) {
						appinsts[appinsts.length]=appinst;
					}
				}
				else {
					appinsts[appinsts.length]=appinst;
				}
			}
			
			return appinsts;
		},
		/**
		 * 构建 AC，RC的面板
		 */
		buildRCorACPanel:function(urlMO,action,container) {
			
			var actionSB=new StringBuffer();
			for(var mkey in urlMO["metric"]) {
	        	
	        	if (mkey.indexOf(action)!=0) {
	        		continue;
	        	}
	        	
	        	var tpsData=app.controller.getMODetaWithLast(urlMO["metric"][mkey]);
				
				//吞吐量,目前按照QPM（标准值）
	        	var tps=Math.floor(tpsData[0]*60000/monitorCfg.client.syncInterval);
	        	
	        	actionSB.append(mkey.substring(2)+":"+tps+"("+tpsData[1]+")&nbsp;");
	        }
			
			var acCt=HtmlHelper.id(container);
			
			acCt.innerHTML=actionSB.toString();
		},
		/**
		 *  通过client url获取client信息，其中svr比较有用，包含用户信息
		 */
		getAppClientInfo:function(appid,url) {
			
		},
		//----------------------Monitor Config Dialog--------------------------
		/**
		 * TODO: Monitor Config Dialog
		 */
		/**
		 * show MonitorCfgDialog
		 */
		showMonitorCfgDialog:function(dType,isJse,sObj) {
			
			var viewBtn=HtmlHelper.id("MonitorConfigDialog_ViewBtn");
			
			viewBtn.onclick=function() {
				app.controller.runMonitorChartData(dType);
			};
			
			$("#MonitorConfigDialog").modal();
		},		
		/**
		 * close MonitorCfgDialog
		 */
		closeMonitorCfgDialog:function() {
			
		},
		/**
		 * run customized monitor chart data
		 */
		runMonitorChartData:function(dType) {
			
			var date=new Date();
			date.setHours(HtmlHelper.value("MonitorConfigDialog_STHour"));
			date.setMinutes(HtmlHelper.value("MonitorConfigDialog_STMin"));
			
			monitorCfg[dType].isusecustomized=true;
			monitorCfg[dType].startTime=date.getTime();
			
			switch(dType) {
			   case "app":
				   monitorCfg.isautorefresh=false;
				   app.refresh("app.monitor");
				   break;
			   case "server":
				   app.refresh("server.monitor");
				   break;
			   case "url":
				   app.refresh("url.monitor");
				   break;
			   case "cust":
				   app.refresh("cust.monitor");
				   break;
			   case "client":
				   app.refresh("client.monitor");
				   break;
			}
		},
		/**
		 * quit the customized run monitor chart data
		 */
		quitMonitorChartData:function(dType) {
			
			if (dType=="app") {
				monitorCfg.app["id"]=undefined;
			}
			
			if (monitorCfg[dType].isusecustomized==false) {
				return;
			}
			
			monitorCfg[dType].isusecustomized=false;
			
			//reset the starttime
			monitorCfg[dType].startTime=undefined;
			
			if (dType=="app") {
				monitorCfg.startTime=undefined;
				//reset isautorefresh
			   monitorCfg.isautorefresh=true;
			   
			   app.refresh("app.monitor");
			}
			
		},
		/**
		 * init LogCfgDialog
		 */
		initMonitorCfgDialog:function() {
			var stHour=HtmlHelper.id("MonitorConfigDialog_STHour");
			var stMin=HtmlHelper.id("MonitorConfigDialog_STMin");
			
			for(var i=0;i<24;i++) {
				var option=HtmlHelper.newElem("option",{value:i});
				option.innerHTML=i;
				stHour.appendChild(option);
			}
			
			for(var i=0;i<60;i++) {
				var option=HtmlHelper.newElem("option",{value:i});
				option.innerHTML=i;
				stMin.appendChild(option);
			}
		},
	    //----------------------App Node--------------------------
		/**
		 * TODO:App Node
		 */
		//build app node
		buildAppNode:function(nodeDiv,jsonObj) {
			//top
			this.buildTopContent(nodeDiv,jsonObj);
			//app perf
			this.buildMiddleContent(nodeDiv,jsonObj);
			//app instances
			this.buildAppInsts(nodeDiv,jsonObj);
		},
		buildTopContent:function(nodeDiv,jsonObj) {
			
			var id=jsonObj.appgpid+"_top";
			var titleId=jsonObj.appgpid+"_tags";
			var top=HtmlHelper.id(id);
			
			if (undefined==top) {
				
				var isJse=this.isJSE(jsonObj.appurl);
				
				var tagTitleCls="tagTitleNode";
				
				if (isJse==true) {
					tagTitleCls="tagTitleNodeJSE";
				}
				
				var sb=new StringBuffer();
				sb.append("<div id=\""+id+"\" class=\"topDiv\">");
				sb.append("<span class=\""+tagTitleCls+"\" id='"+titleId+"'>"+this.getAppTags(titleId,jsonObj.appname)+"</span><br/>");
				sb.append("<span class=\"idTitleNode\">"+jsonObj.appid+"</span>");
				
				sb.append("<div class=\"icon-list\" onclick='javascrip:app.controller.showWindow({appid:\""+jsonObj.appid+"\",appgpid:\""+jsonObj.appgpid+"\"},\"AppTopoWnd\",\"buildAppDetail\")'></div>");
				if (isJse==false) {
					sb.append("<div class=\"glyphicon glyphicon-random\" onclick='javascrip:app.controller.showWindow({appid:\""+jsonObj.appid+"\",appgroup:\""+jsonObj.appgroup+"\",appgpid:\""+jsonObj.appgpid+"\",isJse:"+isJse+"},\"AppClusterTopWnd\",\"buildAppClusterTopWnd\",\"runAppClusterTop\")'></div>");
				}
				sb.append("</div>");
				nodeDiv.innerHTML+=sb.toString();
			} else {
				var title=HtmlHelper.id(titleId);
				title.innerHTML=this.getAppTags(titleId,jsonObj.appname);
			}
		},
		buildMiddleContent:function(nodeDiv,jsonObj) {
			
			var id=jsonObj.appgpid+"_middle";
			var mid=HtmlHelper.id(id);
			
			var qpsid=id+"_QPS";
			var rtid=id+"_RT";
			var errid=id+"_ERR";
			
			var isJse=this.isJSE(jsonObj.appurl);
			
			var f1="QPM";
			if (isJse==true) {
				f1="TPM";
			}
			
			var f2="响应";
			if (isJse==true) {
				f2="HPM";
			}
			
			var f3="错误";
			if (isJse==true) {
				f3="-";
			}			
			
			if (undefined==mid) {
				var middle= "<div id=\""+id+"\" class=\"middleDiv\"  onclick='javascript:app.controller.showWindow({appid:\""+jsonObj.appid+"\",appgpid:\""+jsonObj.appgpid+"\"},\"AppChartWnd\",\"buildAppChart\",\"runAppChart\")'>" +
				            "    <div class=\"cpuTitle\">"+f1+"<span class='osRate' id='"+qpsid+"'>-</span></div>" +
				            "    <div class=\"memoryTitle\">"+f2+"<span class='osRate'  id='"+rtid+"'>-</span></div>" +
				            "    <div class=\"connTitle\">"+f3+"<span class='osRate'  id='"+errid+"'>-</span></div>" +
				            "        <table>" +
				            "            <tr>" +
				            "                <td>" +
				            "                    <div id=\""+qpsid+"_T\"></div>" +
				            "                </td>" +
				            "                <td>" +
				            "                    <div id=\""+rtid+"_T\"></div>" +
				            "                </td>" +
				            "                <td>" +
				            "                    <div id=\""+errid+"_T\"></div>" +
				            "                </td>" +
				            "            </tr>" +
				            "        </table>" +
				            "</div>";
				nodeDiv.innerHTML+=middle;
			}

		},		
		//build app instances
		buildAppInsts:function(nodeDiv,jsonObj) {
			
			var id=jsonObj.appgpid+"_insts";
			
			var insts=HtmlHelper.id(id);
			
			if (undefined==insts) {
				nodeDiv.innerHTML+="<div id=\""+id+"\"></div>";
				insts=HtmlHelper.id(id);
			}
			
			var isJse=this.isJSE(jsonObj.appurl);
			
			var appInstURL;
			// JSE
			if (isJse==true) {
				appInstURL=jsonObj.appurl;
			}
			// JEE
			else {
				appInstURL=jsonObj.appurl+"---"+jsonObj.appid;
			}
			
			var instance=HtmlHelper.id(appInstURL);
						
			if (undefined==instance) {
				var sb=new StringBuffer();
				
				var appInfo={id:jsonObj.id,instid:appInstURL,isJse:isJse};
				
				var appInfoStr=StringHelper.obj2str(appInfo);
				
				sb.append("<div id=\""+appInstURL+"\" class=\"appInstance "+this.getNodeStateCss(jsonObj.state)+"\" onclick='javascript:app.controller.showWindow("+appInfoStr+",\"AppInstChartWnd\",\"buildAppInstChart\",\"runAppInstChart\")'>");
				//sb.append("<span id=\""+appInstURL+"_light\" class=\"appInstLight dark\">&nbsp;</span>");
				sb.append("<span class='appInstanceInfo'>"+jsonObj.appurl+"</span>");
				sb.append("<span id='"+appInstURL+"_MOState'></span>");
				sb.append("</div>");
				insts.innerHTML+=sb.toString();
			}
			else {
				instance.setAttribute("class","appInstance "+this.getNodeStateCss(jsonObj.state));
				instance.innerHTML=//"<span id=\""+appInstURL+"_light\" class=\"appInstLight dark\">&nbsp;</span>" +
						"<span class='appInstanceInfo'>"+jsonObj.appurl+"</span>"+"<span id='"+appInstURL+"_MOState'></span>";
			}
		},
		/**
		 * update AppNode MoitorData
		 */
		updateAppNodeMOState:function(appMODataMap) {
			/**
			 * Step 1: refresh App Instance Monitor Data
			 */
			var appgpInitMap={};
			var appType={};
			
			//scan all app inst
			for(var key in appMODataMap) {
			
				var appMOData=appMODataMap[key];
				
				//get the appid
				var isJse=this.isJSE(key);
				
				var appid;
				
				// JSE
				if (isJse==true) {
					var lindex=key.lastIndexOf("/");
					var tmpKey=key.substring(lindex+1);
					var tmpKeyInfo=tmpKey.split("-");
					appid=tmpKeyInfo[0];
				}
				// JEE
				else {
					var idInfo=key.split("---");
					appid=idInfo[1];
				}
				
				var apppgid=((appMOData["appgroup"]==undefined)?"":appMOData["appgroup"])+":"+appid;
				
				if (isJse==true) {
					appType[apppgid]="JSE";
				}
				else {
					appType[apppgid]="JEE";
				}
				
				/**
				 * Step 1.1:update app inst monitor state
				 */
				this.updateAppInstMOState(appMOData,isJse);
				
				var appgp=this.getAppGPData(apppgid);
				
				if(appgp==undefined) {
					continue;
				}
				
				if (appgpInitMap[apppgid]==undefined) {
					appgpInitMap[apppgid]={
							// JEE
							err:0,
							count:0,
							tps_sum:0,
							tavg_sum:0,							
							i_count:[],
							i_err:[],
							i_tavg:[],
							// JSE
							jse_tpm:0,
							jse_hpm:0,
							jse_heapmax:0,
							i_jse_tpm:[],
							i_jse_hpm:[],
							// COMMON
							i_series:[],
							i_num:0
					};
				}
				
				/**
				 * Step 1.2: stat app level monitor state
				 */	
				appgpInitMap[apppgid]["i_num"]+=1;
				
				if (isJse==true) {
					
					//App Thread Started in 1 min
					if (appMOData["jse_tpm"]!=undefined) {
						appgpInitMap[apppgid].jse_tpm+=appMOData["jse_tpm"];
					}
					//App Heap Change in 1 min
					if (appMOData["jse_hpm"]!=undefined) {
						appgpInitMap[apppgid].jse_hpm+=appMOData["jse_hpm"];
						appgpInitMap[apppgid].jse_heapmax+=appMOData["jse_heapmax"];
					}
				}
				else {
					//App Node Err
					if (appMOData["err"]!=undefined) {
						appgpInitMap[apppgid].err+=appMOData["err"];
					}
					//App Node count
					if (appMOData["count"]!=undefined) {
						appgpInitMap[apppgid].count+=appMOData["count"];
					}
					
					//App Node avg response time
					if (appMOData["tavg"]!=undefined) {
						appgpInitMap[apppgid].tavg_sum+=appMOData["tavg"];
					}
					//App Node TPS
					if (appMOData["tps"]!=undefined) {
						appgpInitMap[apppgid].tps_sum+=appMOData["tps"];
					}
				}
								
				/**
				 * Step 1.3: app chart instance monitor records
				 */
				var index=appgpInitMap[apppgid].i_series.length+1;
				appgpInitMap[apppgid].i_series[index-1]={
						name: index,
						data:[],
						instid:key
				};
				
				// JSE
				if (isJse==true) {
					var appInstTPMMO= this.convertDPStoArray(appMOData["metric"]["thread_started"]);
					var appInstHPMMO= this.convertDPStoArray(appMOData["metric"]["heap_use"],function(index,d){
						return CommonHelper.getB2Human(d,false,2);
					});
					
					appgpInitMap[apppgid].i_jse_tpm[index-1]=appInstTPMMO;
					appgpInitMap[apppgid].i_jse_hpm[index-1]=appInstHPMMO;
				}
				// JEE
				else {

					var appInstCountMO= this.convertDPStoArray(appMOData["metric"]["count"]);
					var appInstAVGMO= this.convertDPStoArray(appMOData["metric"]["tavg"]);
					var appInstErrMO= this.convertDPStoArray(appMOData["metric"]["err"]);
					/**
					 * put the data into the right series array order
					 */
					appgpInitMap[apppgid].i_count[index-1]=appInstCountMO;
					appgpInitMap[apppgid].i_tavg[index-1]=appInstAVGMO;
					appgpInitMap[apppgid].i_err[index-1]=appInstErrMO;	
				}							
			}
			
			//scan all appgroup to set the stat value
			var appgps=app.mdata("monitor.appgp");
			
			for(var key in appgps) {
				
				var appgp=appgps[key];
				var apppgid=appgp["appgroup"]+":"+appgp["appid"];
				var appid=appgp["appid"];
				
				if (appgpInitMap[apppgid]==undefined) {
					continue;
				}
				
				/**
				 * Step 1: compute App Group Stat Data
				 */
				// JSE
				if (appType[apppgid]=="JSE") {
					
					appgp["i_num"]=appgpInitMap[apppgid].i_num;
					appgp["i_series"]=appgpInitMap[apppgid].i_series;
					appgp["jse_tpm"]=Math.floor(appgpInitMap[apppgid].jse_tpm/appgp["i_num"]);
					appgp["jse_hpm"]=Math.floor(appgpInitMap[apppgid].jse_hpm/appgp["i_num"]);
					appgp["jse_heapmax"]=appgpInitMap[apppgid].jse_heapmax;
					appgp["i_jse_tpm"]=appgpInitMap[apppgid].i_jse_tpm;
					appgp["i_jse_hpm"]=appgpInitMap[apppgid].i_jse_hpm;
					
					/**
					 * Step 2: refresh App Node 
					 */
					var tpmMORate=this.getMORate("jse_tpm",appgp["jse_tpm"]);
					var hpmMORate=this.getMORate("jse_hpm",appgp["jse_hpm"],appgp["jse_heapmax"]);
					
					/**
					 * AppNode 性能显示
					 */
					this.changePerfVal(apppgid+"_middle_QPS",tpmMORate,appgp["jse_tpm"]);
					this.changePerfVal(apppgid+"_middle_RT",hpmMORate,appgp["jse_hpm"]);
					
					/**
					 * Step 3: refresh AppChartWnd's App Stat Data
					 */
					var existElem=HtmlHelper.id(apppgid+"_appchart_QPS");
					
					if(existElem==undefined) {
						continue;
					}
					
					/**
					 * AppChart App性能显示
					 */
					this.changePerfVal(apppgid+"_appchart_QPS",tpmMORate,appgp["jse_tpm"]);
					this.changePerfVal(apppgid+"_appchart_RT",hpmMORate,appgp["jse_hpm"]);
					
					/**
					 * AppChart App 图表显示：显示各个实例的原值
					 */
					app.controller.runAppChart({appgpid:apppgid,appid:appid});
				}
				// JEE
				else {
					appgp["err"]=appgpInitMap[apppgid].err;
					appgp["count"]=appgpInitMap[apppgid].count;
					appgp["i_num"]=appgpInitMap[apppgid].i_num;
					appgp["tavg"]=Math.floor(appgpInitMap[apppgid].tavg_sum/appgp["i_num"]);
					appgp["tps"]=appgpInitMap[apppgid].tps_sum;//Math.floor(appgpInitMap[apppgid].tps_sum/appgp["i_num"]);
					appgp["i_count"]=appgpInitMap[apppgid].i_count;
					appgp["i_tavg"]=appgpInitMap[apppgid].i_tavg;
					appgp["i_err"]=appgpInitMap[apppgid].i_err;
					appgp["i_series"]=appgpInitMap[apppgid].i_series;
					
					/**
					 * Step 2: refresh App Node 
					 */
					var tpsMORate=this.getMORate("tps",appgp["tps"]);
					var avgMORate=this.getMORate("tavg",appgp["tavg"]);
					var errMORate=this.getMORate("err",appgp["err"],appgp["count"]);
					/**
					 * AppNode 性能显示
					 */
					this.changePerfVal(apppgid+"_middle_QPS",tpsMORate,appgp["tps"]);
					this.changePerfVal(apppgid+"_middle_RT",avgMORate,appgp["tavg"]);
					this.changePerfVal(apppgid+"_middle_ERR",errMORate,appgp["err"]);		
									
					/**
					 * Step 3: refresh AppChartWnd's App Stat Data
					 */
					var existElem=HtmlHelper.id(apppgid+"_appchart_QPS");
					
					if(existElem==undefined) {
						continue;
					}
					
					/**
					 * AppChart App性能显示
					 */
					this.changePerfVal(apppgid+"_appchart_QPS",tpsMORate,appgp["tps"]);
					this.changePerfVal(apppgid+"_appchart_RT",avgMORate,appgp["tavg"]);
					this.changePerfVal(apppgid+"_appchart_ERR",errMORate,appgp["err"]);
					
					/**
					 * AppChart App 图表显示：显示各个实例的原值
					 */
					app.controller.runAppChart({appgpid:apppgid,appid:appid});
				}
			}
		},
		/**
		 * Update App Instance Monitor State
		 */
		updateAppInstMOState: function(appMOObj,isJse){
			/**
			 * AppNode Instance 性能显示 3个Box
			 */
			var container=HtmlHelper.id(appMOObj["id"]+"_MOState");
			/**
			 * AppChart AppInstance性能显示
			 */
			/*
			 * only process the App in open wnd
			 */
			var appchartPerf=HtmlHelper.id(appMOObj["id"]+"_appchart_QPS");
			/**
			 * AppInstChart AppInstance性能显示
			 */
			/*
			 * only process the App Instance in open wnd
			 */
			var appinstchartPerf=HtmlHelper.id(appMOObj["id"]+"_appinstchart_QPS");
			
			// JSE
			if(isJse==true) {
				var jse_tpm =  appMOObj["jse_tpm"];
	    		var jse_hpm = appMOObj["jse_hpm"];
	    		var jse_heapmax= appMOObj["jse_heapmax"];
	    		var tpmMORate=this.getMORate("jse_tpm",jse_tpm);
	    		var hpmMORate=this.getMORate("jse_hpm",jse_hpm,jse_heapmax);
	    		
	    		if (container!=undefined) {
	    			container.innerHTML=this.buildAppInstMoState(hpmMORate,"HPM: ")+
									    this.buildAppInstMoState(tpmMORate,"TPM: ");
	    		}
	    		
	    		if(appchartPerf!=undefined) {
					
					this.changePerfVal(appMOObj["id"]+"_appchart_QPS",tpmMORate,jse_tpm);
					this.changePerfVal(appMOObj["id"]+"_appchart_RT",hpmMORate,jse_hpm);
				}
	    		
	    		if(appinstchartPerf!=undefined) {
					//step 1: monitor state
					this.changePerfVal(appMOObj["id"]+"_appinstchart_QPS",tpmMORate,jse_tpm);
					this.changePerfVal(appMOObj["id"]+"_appinstchart_RT",hpmMORate,jse_hpm);
					
					//step 2: monitor records data
					app.controller.runAppInstChart(appMOObj["id"]);
				}
			}
			// JEE
			else {
				var tps = appMOObj["tps"];
	    		var tavg = appMOObj["tavg"];
	    		var err = appMOObj["err"];
	    		var count = appMOObj["count"];
	    		var mavg=appMOObj["mavg"];
	    		
	    		
	    		var tpsMORate=this.getMORate("tps",tps);
	    		var errMORate=this.getMORate("err",err,count);
	    		var avgMORate=this.getMORate("tavg",tavg);
	    		var mavgMORate=this.getMORate("mavg",mavg);
	    		
	    		if (container!=undefined) {
	    			
	    			//this helps to show the active JEE app instance
	    			if (tps>0) {
	    				var instance=HtmlHelper.id(appMOObj["id"]);
	    				instance.setAttribute("class",instance.className+"A");
	    			}
	    			
		    		container.innerHTML=this.buildAppInstMoState(errMORate,"错误数: ")+
		    							this.buildAppInstMoState(avgMORate,"响应时间: ")+
		    							this.buildAppInstMoState(tpsMORate,"QPM: ");
	    		}
	    		
	    		if(appchartPerf!=undefined) {
					
					this.changePerfVal(appMOObj["id"]+"_appchart_QPS",tpsMORate,tps);
					this.changePerfVal(appMOObj["id"]+"_appchart_RT",avgMORate,tavg);
					this.changePerfVal(appMOObj["id"]+"_appchart_ERR",errMORate,err);
				}
	    		
	    		if(appinstchartPerf!=undefined) {
					//step 1: monitor state
					this.changePerfVal(appMOObj["id"]+"_appinstchart_QPS",tpsMORate,tps);
					this.changePerfVal(appMOObj["id"]+"_appinstchart_RT",avgMORate,tavg);
					this.changePerfVal(appMOObj["id"]+"_appinstchart_RTAVG",mavgMORate,mavg);
					this.changePerfVal(appMOObj["id"]+"_appinstchart_ERR",errMORate,err);
					
					//step 2: monitor records data
					app.controller.runAppInstChart(appMOObj["id"]);
				}
	    		
	    		//on app service stream monitor data update
	    		appStream.onAppMoUpdate({id:appMOObj["id"],tps:tps,err:err,tavg:tavg,mavg:mavg,tpsR:tpsMORate,errR:errMORate,tavgR:avgMORate,timestamp:appMOObj["timestamp"],isJse:isJse});
	    		
			}		
	    },
	    // 生成实例的状态方块
	    buildAppInstMoState:function(moRate,title) {
	    	var qpsStateCls;
	    	var targetTitle="";
	    	
	    	if (title!=undefined) {
	    		targetTitle=title+moRate.value;
	    	}
	    	
	    	if(moRate.level==0){
    			qpsStateCls = '';
    		}else if(moRate.level==1){
    			qpsStateCls = 'insts-state-mid';
    		}else{
    			qpsStateCls = 'insts-state-high';
    		}
	    	
	    	return '<span class="insts insts-state ' + qpsStateCls + '" title="'+targetTitle+'"></span>';
	    },
	    // 生成性能统计结果Panel
	    buildPerfPanelMoState:function(pid,perfName,perfObj,perfMetric) {
	    	
	    	if (perfMetric==undefined) {
	    		
	    		if(perfObj==undefined) {
	    		   perfObj="-";
	    		}
	    		
	    		return perfName+"<span class='osRate' id='"+pid+"'>"+perfObj+"</span>&nbsp;&nbsp;";
	    	}
	    	
	    	return perfName+"<span class='osRate' id='"+pid+"'>"+this.getPerfData(perfObj,perfMetric)+"</span>&nbsp;&nbsp;";
	    },
		//----------------------App Chart--------------------------
		/**
		 * TODO:App Chart
		 */
		chartTimer:undefined,
	    lastClientTimestamp:-1,
		buildAppChart: function(obj) {
			
			var appgpid=obj.appgpid;
			var appid=obj.appid;
			
			var html=  "<div class=\"appDetailContent\" >" +
            "<div class=\"topDiv\">" +
            "<span class=\"tagTitle\">&nbsp;</span><br/>"+
            "<span class=\"idTitle\">"+appid+"</span>" +
            "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppChartWnd','destroyAppChart')\"></div>" +
            "<div class=\"icon-refresh\" onclick='javascript:app.controller.runAppChart({appgpid:\""+appgpid+"\",appid:\""+appid+"\"})'></div>"+
            "<div class=\"glyphicon glyphicon-cog\" onclick='javascript:app.controller.showMonitorCfgDialog(\"app\")'></div>"+
            "</div></div>";
			
			 var appgp=this.getAppGPData(appgpid);
			 
	         //---App Stat content
	         html+="<div class='contentDiv'>" +
	         		"<div class=\"shine2\"></div>" +
	         		"<span class='title'>应用性能</span>"+
	         		"<div class='indexItemContent'>";
	         
	         var isJse=this.isJSE(appgp.svrid);
	         
	         // JSE 
	         if (isJse==true) {
	        	 html+=	this.buildPerfPanelMoState(appgpid+"_appchart_QPS","TPM",appgp,"jse_tpm")+
	        	 this.buildPerfPanelMoState(appgpid+"_appchart_RT","HPM",appgp,"jse_hpm");
	         		//buildPerfPanelMoState(appid+"_appchart_ERR","错误",appgp,"err");
	         }
	         // JEE 
	         else {
	        	 html+=	this.buildPerfPanelMoState(appgpid+"_appchart_QPS","QPM",appgp,"tps")+
	        	 this.buildPerfPanelMoState(appgpid+"_appchart_RT","响应时间(ms)",appgp,"tavg")+
	        	 //this.buildPerfPanelMoState(appgpid+"_appchart_ERR","错误",appgp,"err")+
	        	 this.buildPerfPanelMoState(appgpid+"_appchart_ERR","错误",appgp,"err");
	        	 //this.buildPerfPanelMoState(appgpid+"_appchart_TS","访问时间",appgp,"timestamp");
	         }
	         
	         html+="</div></div>" +
	         		"<div class='contentDiv' id='AppChartWnd_CT' >" +
	        		"<div class=\"shine2\"></div>"+
	        		"<span class='title'>应用实例性能</span>";
	        //---App Instance Stat content
	        var pdata=appgp.i_series;
	        
	        //get all app instance
	        var sb=new StringBuffer();
	        
	        for(var id in pdata) {
	        	
	        	var jo=pdata[id];
	        	
	        	if (typeof jo == "function") {
	        		continue;
	        	}
	        	
	        	//add app instance state content
	        	sb.append(this.buildAppChartInstStatContent(jo,isJse));
	        	
	        }
	        
	        html+=sb.toString();
	        html+="</div>" +
	        	  "</div>";
        
	        return html;
		},
		/**
		 * 生成App instance 性能显示
		 */
		buildAppChartInstStatContent:function(appInstPO,isJse) {
			
			var sb=new StringBuffer();
			
			var appInstId=appInstPO["instid"];
			
			var appInstMO=app.mdata("monitor.app")[appInstId];
			
			sb.append("<div class='indexItem'>");
			sb.append("<div class='indexItemHead'><span class='indexItemTag'>"+appInstPO["name"]+"</span><span class='indexItemId'>"+appInstPO["instid"]+"</span></div>");
			sb.append("<div class='indexItemContent'>");
			
			// JSE 
			if (isJse==true) {
				sb.append(this.buildPerfPanelMoState(appInstId+"_appchart_QPS","TPM",appInstMO["jse_tpm"])+
						this.buildPerfPanelMoState(appInstId+"_appchart_RT","HPM",appInstMO["jse_hpm"]));
	         		//buildPerfPanelMoState(appid+"_appchart_ERR","错误",appgp,"err");
	         }
			// JEE 
	         else {
	        	 sb.append(this.buildPerfPanelMoState(appInstId+"_appchart_QPS","QPM",appInstMO["tps"])+
	        			 this.buildPerfPanelMoState(appInstId+"_appchart_RT","全程平均响应(ms)",appInstMO["tavg"])+
	        			 this.buildPerfPanelMoState(appInstId+"_appchart_RT","当前分钟响应(ms)",appInstMO["mavg"])+
	        			 this.buildPerfPanelMoState(appInstId+"_appchart_ERR","错误",appInstMO["err"])+
	        			 this.buildPerfPanelMoState(appInstId+"_appchart_TS","访问时间",appInstMO["timestamp"])		 
	        	 );
	         }
			sb.append("</div></div>");
			
			return sb.toString();
		},		
		runAppChart : function(obj){
			
			var appgpid=obj.appgpid;
			var appid=obj.appid;
			
			var appgp=this.getAppGPData(appgpid);
			
			var isJse=this.isJSE(appgp.svrid);
			
			// JSE
			if (isJse==true) {
				jseappQPSCfg.series=appgp.i_series;
				jseappRTCfg.series=appgp.i_series;
				
				//build app chart
				window["appcharts"].bulid(jseappQPSCfg);
				window["appcharts"].bulid(jseappRTCfg);
				
				/**
				 * AppChart App 图表显示：显示各个实例的原值
				 */
				window["appcharts"].run("appQPSChart",appgp.i_jse_tpm);
				window["appcharts"].run("appRTChart",appgp.i_jse_hpm);
			}
			// JEE
			else {
				appQPSCfg.series=appgp.i_series;
				appRTCfg.series=appgp.i_series;
				appErrCfg.series=appgp.i_series;
				
				//build app chart
				window["appcharts"].bulid(appQPSCfg);
				window["appcharts"].bulid(appRTCfg);
				window["appcharts"].bulid(appErrCfg);
				
				/**
				 * AppChart App 图表显示：显示各个实例的原值
				 */
				window["appcharts"].run("appQPSChart",appgp.i_count);
				window["appcharts"].run("appRTChart",appgp.i_tavg);
				window["appcharts"].run("appErrChart",appgp.i_err);
			}
			
		},
		destroyAppChart : function() {
			if (this.chartTimer!=undefined) {
				window.clearInterval(this.chartTimer);
			}
			window["appcharts"].destroy("appQPSChart");
			window["appcharts"].destroy("appRTChart");
			window["appcharts"].destroy("appErrChart");
			
			//quit customerized chart data
			this.quitMonitorChartData("app");			
		},
		//----------------------App Inst Chart--------------------------
		/**
		 * TODO:App Instance Chart
		 */
		buildAppInstChart: function(obj) {
			
			var mdata=app.mdata("profile");
			
			var jsonObj=mdata[obj.id];
			
			if(jsonObj==undefined) {
				setTimeout(function() {
					openLink.runWindowDelay("monitorapp");
				},1000);				
				return;
			}
			
			var isJse=this.isJSE(jsonObj.appurl);
			
			if (isJse==true) {
				appInstMOId=jsonObj.appurl;
			}
			else {
				appInstMOId=jsonObj.appurl+"---"+jsonObj.appid;
			}
			
			var appInstMO=app.mdata("monitor.app")[appInstMOId];
			
			if (appInstMO==undefined) {
				console.log(appInstMOId+" monitor data is null~~~");
			}
			
			var sObjStr=StringHelper.obj2str(obj);
			
			var backWndId="";
			
			if (obj["backWndId"]!=undefined) {
				backWndId=",'"+obj["backWndId"]+"'";
			}
		   
			//save current app instance id
		   monitorCfg.app["id"]=obj.id;
			
			/**
			 * AppInstChart Head
			 */
	        var html="<div class=\"appDetailContent\" >" +
	            "<div class=\"topDiv\">" +
	            "<span class=\"tagTitle\">"+jsonObj.appurl+"</span><br/>"+
	            "<span class=\"idTitle\">"+jsonObj.appid+"</span>" +
	            "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppInstChartWnd','destroyAppInstChart'"+backWndId+")\"></div>" +
	            "<div class=\"icon-refresh\" onclick='javascript:app.controller.runAppInstChart("+sObjStr+")'></div>"+
	            "<div class=\"glyphicon glyphicon-cog\" onclick='javascript:app.controller.showMonitorCfgDialog(\"app\","+isJse+","+sObjStr+")'></div>"+	            
	            "</div></div>";
	        /**
			 * AppInstChart AppInst性能显示
			 */
	        html+="<div class='contentDiv' id='AppInstChartWnd_CT' >" +
	        		"<div class=\"shine2\"></div>" +
			        "<span class='title'>应用实例性能</span>"+
		     		"<div>";
	        // JSE 
	        if (isJse==true) {
	        	html+=this.buildPerfPanelMoState(appInstMOId+"_appinstchart_QPS","TPM",appInstMO,"jse_tpm")+
						this.buildPerfPanelMoState(appInstMOId+"_appinstchart_RT","HPM",appInstMO,"jse_hpm");
	         		//buildPerfPanelMoState(appid+"_appchart_ERR","错误",appgp,"err");
	         }
	        // JEE
	         else {
	        	 html+=this.buildPerfPanelMoState(appInstMOId+"_appinstchart_QPS","QPM",appInstMO,"tps")+
	        			 this.buildPerfPanelMoState(appInstMOId+"_appinstchart_RT","全程平均响应(ms)",appInstMO,"tavg")+
	        			 this.buildPerfPanelMoState(appInstMOId+"_appinstchart_RTAVG","当前分钟响应(ms)",appInstMO,"mavg")+
	        			 this.buildPerfPanelMoState(appInstMOId+"_appinstchart_ERR","错误",appInstMO,"err")+
	        			 this.buildPerfPanelMoState(appInstMOId+"_appinstchart_TS","访问时间",appInstMO,"timestamp");
	         }
	        
		     html+="</div>" +
		     		"</div>";
		     
		     /**
		      *调用链查看窗口
		      */
		     html+=appAPM.buildIVCWndButton(jsonObj,appInstMOId,isJse);
		     
		     /**
		      * 线程分析查看窗口
		      */
		     html += appJTA.buildJTAWndButton(jsonObj, appInstMOId, isJse);
		    
		     /**
		      * App Customized Metrics查看窗口
		      */
		     html+="<div class='contentDiv' >"+this.buildAppCustomMetricsWndContent(jsonObj,isJse)+
	            "</div>";
		    
	        /**
	         * AppInst Chart 服务器/JVM查看窗口
	         */
	        html+="<div class='contentDiv' >"+this.buildAppInstServerWndContent(jsonObj)+
            "</div>";
	        
	        /**
	         * 主机（应用容器）查看窗口
	         */
	        html+="<div class='contentDiv' >"+this.buildAppMachineWndContent(jsonObj,appInstMOId,isJse)+
            "</div>";
	        
	        /**
	         * 应用进程查看窗口
	         */
	        html+="<div class='contentDiv' >"+this.buildAppProcessWndContent(jsonObj,appInstMOId,isJse)+
            "</div>";
	        
	        
	        /**
	         * AppInst Chart 日志组件按钮列表，可以打开日志组件查看窗口
	         */
	        html+="<div class='contentDiv' >"+this.buildAppInstLogWndContent(jsonObj,jsonObj["logs.log4j"])+
            "</div>";
	        
	        /**
	         * AppInst Chart 服务组件按钮列表，可以打开服务组件Chart窗口
	         */
	        html+="<div class='contentDiv' >"+this.buildAppInstServiceChartContent(jsonObj,jsonObj["cpt.services"])+
	            "</div>";
	        
	        /**
	         * AppInst Chart 客户端组件按钮列表，可以打开客户端组件Chart窗口
	         */
	        html+="<div class='contentDiv' >"+this.buildAppInstClientChartContent(jsonObj,jsonObj["cpt.clients"])+
	            "</div>";
	        
	       
	        html+="</div>";
	        return html;
		},
		/**
		 * buildAppMachineWndContent
		 */
		buildAppMachineWndContent:function(jsonObj,appInstMOId,isJse) {
			var ip=jsonObj["ip"];
			var sb=new StringBuffer();
			var param=encodeURI(jsonObj["id"]+","+appInstMOId+","+isJse);
			sb.append("<span class=\"componentExpandButton componentExpandButtonStyle1\" style='font-size:14px;' onclick='window.parent.jumpUrl(\"uavapp_godeye/uavnetwork/main.html?view=macchart&from=uavapp_godeye/appmonitor/main.html&fview=appinst&fparam="+param+"&ip="+ip+"\",\"应用容器监控\")'><span class='titleGray'>应用容器&nbsp;"+ip+"</span>");
			
			return sb.toString();
		},
		/**
		 * buildAppProcessWndContent
		 */
		buildAppProcessWndContent:function(jsonObj,appInstMOId,isJse) {
			var ip=jsonObj["ip"];
			var proc;
			//jse can get the pid directly
			if (isJse) {
				var jseInfo=jsonObj["appurl"].split("-");
				proc="pid="+jseInfo[jseInfo.length-1];
			}
			//jee can get the port
			else {
				var ipport=app.controller.getAppInstIPPort( jsonObj["appurl"]);
				proc="port="+ipport.split(":")[2];
			}
			
			var param=encodeURI(jsonObj["id"]+","+appInstMOId+","+isJse);
			
			var sb=new StringBuffer();
			
			sb.append("<span class=\"componentExpandButton componentExpandButtonStyle1\" style='font-size:14px;' onclick='window.parent.jumpUrl(\"uavapp_godeye/uavnetwork/main.html?view=procchart&from=uavapp_godeye/appmonitor/main.html&fview=appinst&fparam="+param+"&ip="+ip+"&"+proc+"\",\"应用容器监控\")'><span class='titleGray'>应用进程</span>");
			
			return sb.toString();
		},
		/**
		 * App Customized Metrics
		 */
		buildAppCustomMetricsWndContent:function(jsonObj,isJse) {
			
			var metrics=jsonObj["appmetrics"];
			
			if (metrics=="{}"||metrics=="") {
				return "";
			}
			
			var serverMOId=jsonObj["appurl"];
			
			// JSE
			if (isJse==true) {
				serverMOId=serverMOId;
			}
			// JEE
			else {
							
				var st = serverMOId.indexOf("//");
	
		        var httpS = serverMOId.substring(0, st);
	
		        var temp = serverMOId.substring(st + 2);
	
		        st = temp.indexOf("/");
				
		        serverMOId = httpS + "//" + temp.substring(0, st);
			}
			
			var metricsObj=eval("("+metrics+")");
			
			var sb=new StringBuffer();
			
			var title="自定义指标";
			
			var obj={moid:serverMOId,svrid:jsonObj["svrid"],ip:jsonObj.ip,isJse:isJse,metrics:metrics};
	        
	        var objStr=StringHelper.obj2str(obj);
			
			sb.append("<span class=\"componentExpandButton componentExpandButtonStyle1\" style='font-size:14px;' onclick='app.controller.showWindow("+objStr+",\"AppCustMetricWnd\",\"buildAppCustMetricWnd\",\"runAppCustMetricChart\")'><span class='titleGray'>"+title+"&nbsp;</span>"+Object.getOwnPropertyNames(metricsObj).length+"个指标</span>");
			
			return sb.toString();
		},
		/**
		 * AppInstChart 服务&JVM Window
		 */
		buildAppInstServerWndContent:function(jsonObj) {
			var sb=new StringBuffer();
			
			var serverMOId=jsonObj["appurl"];
			
			var isJse=this.isJSE(serverMOId);
			
			var title;
			
			// JSE
			if (isJse==true) {
				serverMOId=serverMOId;
				title="JVM虚拟机";
			}
			// JEE
			else {
							
				var st = serverMOId.indexOf("//");
	
		        var httpS = serverMOId.substring(0, st);
	
		        var temp = serverMOId.substring(st + 2);
	
		        st = temp.indexOf("/");
				
		        serverMOId = httpS + "//" + temp.substring(0, st);
		        
		        title="应用服务器";
			}
				        
	        var obj={moid:serverMOId,svrid:jsonObj["svrid"],ip:jsonObj.ip,isJse:isJse};
	        
	        var objStr=StringHelper.obj2str(obj);
	        
			sb.append("<span class=\"componentExpandButton componentExpandButtonStyle1\" style='font-size:14px;' onclick='app.controller.showWindow("+objStr+",\"AppServerWnd\",\"buildAppServerWnd\",\"runAppServerChart\")'><span class='titleGray'>"+title+"&nbsp;</span>"+jsonObj["svrid"]+"</span>");
			
			return sb.toString();
		},
		/**
		 * AppInstChart 服务组件打开按钮
		 */
		buildAppInstServiceChartContent:function(jsonObj,cptservicesStr) {
			
			var cptservices=eval("("+cptservicesStr+")");
			
			var sb=new StringBuffer();
			
			for(var cptservice in cptservices) {
			
				var serviceURLs=cptservices[cptservice];
				
				//don't show the servicComp in appInstChart when serviceurl begin with @ (means there is no servlet-mapping for this serviceComp)
				if (serviceURLs.length==0||serviceURLs[0].indexOf('@')==0){
					continue;
				}
				
				var sObj={name:cptservice,urls:serviceURLs,ip:jsonObj.ip,svrid:jsonObj.svrid};
				
				var sObjStr=StringHelper.obj2str(sObj);
				
				sb.append("<span class=\"componentExpandButton\" style='font-size:14px;' onclick='app.controller.showWindow("+sObjStr+",\"AppServiceURLChartWnd\",\"buildAppServiceURLChartWnd\",\"runAppServiceURLChart\")'><span class='titleGray'>服务组件&nbsp;</span>"+cptservice+"</span>")
			}
			
			return sb.toString();
		},
		/**
		 * AppInstChart 客户端组件打开按钮
		 */
		buildAppInstClientChartContent:function(jsonObj,cptClientStr) {
			var cptClients=eval(cptClientStr);
			
			var sb=new StringBuffer();
			
			for(var i=0;i<cptClients.length;i++) {
			
				var client=cptClients[i];
				
				var urls=[];
				
				for (var path in client["values"].urls) {
					
					var urlAttrs=client["values"].urls[path];
					
					//only http need contact path
					var url=client["id"];
					if (client["id"].indexOf("http:")==0) {
						url+=path;
					}
					urls[urls.length]=url;
				}
				
				var clientStr=JSON.stringify(client);
				
				var sObj={name:client["id"],urls:urls,clientStr:clientStr,appid:jsonObj.appid,ip:jsonObj.ip,svrid:jsonObj.svrid,hostport:jsonObj.hostport,backWndId:"AppInstChartWnd"};
				
				var sObjStr=StringHelper.obj2str(sObj);
				
				sb.append("<span class=\"componentExpandButton componentExpandButtonStyle2\" style='font-size:14px;' onclick='app.controller.showWindow("+sObjStr+",\"AppClientURLChartWnd\",\"buildAppClientURLChartWnd\",\"runAppClientURLChart\")'><span class='titleGray'>客户端组件&nbsp;</span>"+client["id"]+"</span>")
			}
			
			return sb.toString();
		},
		/**
		 * AppInstChart 日志打开按钮
		 */
		buildAppInstLogWndContent:function(jsonObj,d) {
			
			var logs=eval("("+d+")");
			
			var sb=new StringBuffer();
			
			for (var key in logs) {
				
				var sObj={ip:jsonObj.ip,svrid:jsonObj.svrid,appid:jsonObj.appid,logid:key,appurl:jsonObj.appurl};
				
				var sObjStr=StringHelper.obj2str(sObj);
				
				sb.append("<span class=\"componentExpandButton componentExpandButtonStyle0\" style='font-size:14px;' onclick='app.controller.showWindow("+sObjStr+",\"AppNewLogWnd\",\"buildAppNewLogWnd\",\"runAppNewLogWnd\")'><span class='titleGray'>日志组件&nbsp;</span>"+key+"</span>")
		    }
			return sb.toString();
		},
		runAppInstChart : function(instObj){
			
			var appInstId=instObj;
			
			if (typeof instObj == "object") {
				appInstId=instObj["instid"];
			}
			
			var appMOObj=app.mdata("monitor.app")[appInstId];
			
			if (appMOObj==undefined) {
				return;
			}
			
			var isJse=this.isJSE(appMOObj.id);
			
			// JSE
			if (isJse==true) {
				/**
				 * AppInstChart 图表显示：显示各个性能指标原值 
				 */
				var appInstMOChartData={
					thread:new Array(1),
					heap:new Array(1)
				};
				
				appInstMOChartData.thread[jseappInstQPSCfg.seriesMap["thread_started"]]=this.convertDPStoArray(appMOObj["metric"]["thread_started"]);
				appInstMOChartData.heap[jseappInstRTCfg.seriesMap["heap_use"]]=this.convertDPStoArray(appMOObj["metric"]["heap_use"],function(index,d){
					return CommonHelper.getB2Human(d,false,2);
				});
				
				//build app inst chart
				window["appcharts"].bulid(jseappInstQPSCfg);
				window["appcharts"].bulid(jseappInstRTCfg);
				
				var width = HtmlHelper.width("AppInstChartWnd");
				
				//run app inst chart data
		        window["appcharts"].run("appInstQPSChart",appInstMOChartData.thread);
				window["appcharts"].run("appInstRTChart",appInstMOChartData.heap);
				window["appcharts"].resize(width, undefined, "appInstQPSChart");
				window["appcharts"].resize(width, undefined, "appInstRTChart");
			}
			// JEE
			else {
			
				/**
				 * AppInstChart 图表显示：显示各个性能指标原值 
				 */
				var appInstMOChartData={
					qps:new Array(3),
					rt:new Array(3),
					rc:[]
				};
				/**
				 * 访问计数数据
				 */
				appInstMOChartData.qps[appInstQPSCfg.seriesMap["count"]]=this.convertDPStoArray(appMOObj["metric"]["count"]);
				appInstMOChartData.qps[appInstQPSCfg.seriesMap["err"]]=this.convertDPStoArray(appMOObj["metric"]["err"]);
				appInstMOChartData.qps[appInstQPSCfg.seriesMap["warn"]]=this.convertDPStoArray(appMOObj["metric"]["warn"]);
				
				/**
				 * 响应时间数据
				 */
				appInstMOChartData.rt[appInstRTCfg.seriesMap["tmax"]]=this.convertDPStoArray(appMOObj["metric"]["tmax"]);
				appInstMOChartData.rt[appInstRTCfg.seriesMap["tmin"]]=this.convertDPStoArray(appMOObj["metric"]["tmin"]);
				appInstMOChartData.rt[appInstRTCfg.seriesMap["tavg"]]=this.convertDPStoArray(appMOObj["metric"]["tavg"]);
				
				/**
				 * 响应代码计数数据
				 */
		        var series=[];
		        
		        for(var key in appMOObj["metric"]) {
		        	
		        	if (key.indexOf("RC")!=0) {
		        		continue;
		        	}
		        	
		        	series[series.length]={
		        		name:key,
		        		data:[]
		        	};
		        	
		        	appInstMOChartData.rc[appInstMOChartData.rc.length]=this.convertDPStoArray(appMOObj["metric"][key]);
		        }
		        
		        /**
		         * set RC series
		         */
		        appInstErrCfg.series=series;
		        
		        //build app inst chart
				window["appcharts"].bulid(appInstQPSCfg);
				window["appcharts"].bulid(appInstRTCfg);
				window["appcharts"].bulid(appInstErrCfg);
				
				var width = HtmlHelper.width("AppInstChartWnd");
				
				//run app inst chart data
		        window["appcharts"].run("appInstQPSChart",appInstMOChartData.qps);
		        window["appcharts"].resize(width, undefined, "appInstQPSChart");
				window["appcharts"].run("appInstRTChart",appInstMOChartData.rt);
				window["appcharts"].resize(width, undefined, "appInstRTChart");
				window["appcharts"].run("appInstErrChart",appInstMOChartData.rc);
				window["appcharts"].resize(width, undefined, "appInstErrChart");
			
			}
		},
		destroyAppInstChart : function() {
			if (this.chartTimer!=undefined) {
				window.clearInterval(this.chartTimer);
			}
			window["appcharts"].destroy("appInstQPSChart");
			window["appcharts"].destroy("appInstRTChart");
			window["appcharts"].destroy("appInstErrChart");
			
			//quit cutomzied chart data
			this.quitMonitorChartData("app");
		},
		//----------------------App Service URL Chart---------------
		/**
		 * TODO:App Service URL Chart
		 */
		buildAppServiceURLChartWnd:function(sObj) {
			
			/**
			 * Step 1: build App Service URL content
			 */
			var sObjStr=StringHelper.obj2str(sObj);
			
			var html="<div class=\"appDetailContent\" >" +
            "<div class=\"topDiv\">" +
            "<span class=\"tagTitle\">"+sObj.name+"</span><br/>"+
            "<span class=\"idTitle\">服务组件</span>" +
            "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppServiceURLChartWnd','destroyAppURLChart','AppInstChartWnd')\"></div>" +
            "<div class=\"icon-refresh\" onclick='javascript:app.controller.runAppServiceURLChart("+sObjStr+")'></div>"+
            "<div class=\"glyphicon glyphicon-cog\" onclick='javascript:app.controller.showMonitorCfgDialog(\"url\")'></div>"+
            "</div></div>";
			
			html+="<div class='contentDiv' id='AppServiceURLChartWnd_CT'><div class=\"shine2\"></div>";
			var sb=new StringBuffer();
			
			var index=1;
	        var series=[];
	        var seriesMap={};
			
			for (var i=0;i<sObj.urls.length;i++) {
				
				var url=sObj.urls[i];	
				/**
				 * App Service URL 性能显示
				 */
				sb.append("<div class='indexItem'>");
				sb.append("<div class='indexItemHead'><span class='indexItemTag'>"+(i+1)+"</span><span class='indexItemId'>"+url.replace("#","")+"</span></div>");
				sb.append("<div class='indexItemContent'>");
				sb.append("QPM<span class='osRate' id='"+url+"_appservicechart_QPS'>-</span>&nbsp;&nbsp;" +
		         		"全程平均响应(ms)<span class='osRate' id='"+url+"_appservicechart_RT'>-</span>&nbsp;&nbsp;" +
		         		"当前分钟响应(ms)<span class='osRate' id='"+url+"_appservicechart_RTAVG'>-</span>&nbsp;&nbsp;" +
		         		"错误<span class='osRate' id='"+url+"_appservicechart_ERR'>-</span>&nbsp;&nbsp;"+
						"访问时间<span class='osRate' id='"+url+"_appservicechart_TS'>-</span>");
				sb.append("<br/>响应<span class='osRate' id='"+url+"_appservicechart_RC'>-</span>");
				sb.append("</div></div>");
				
				/**
				 * App Service URL 图表Series
				 */
				//prepare series
	        	series[series.length]={
	        			name:(i+1),
	    	        	data:[],
	    	        	_id:url
	        	};
	        	seriesMap[url]=series.length-1;
			}
			
			html+=sb.toString()+"</div>";
			
			/**
			 * Step 2: prepare URL series
			 */
	        /**
	         * set app url chart series
	         */
	        appURLQPSCfg.series=series;
	        appURLRTCfg.series=series;
	        appURLErrCfg.series=series;
	        /**
	         * set app url chart series order map
	         */
	        appURLQPSCfg.seriesMap=seriesMap;
	        appURLRTCfg.seriesMap=seriesMap;
	        appURLErrCfg.seriesMap=seriesMap;
	        
			return html;
		},
		runAppServiceURLChart : function(sObj){
			/**
			 * Step 1: set the monitorCfg.url data to monitor query
			 */
			monitorCfg.url.ip=sObj.ip;
			monitorCfg.url.svrid=sObj.svrid;
			
			//url以"#"结尾意味着需要去除后缀，
			for(var i=0;i<sObj.urls.length;i++){
			    
				if(!sObj.urls[i].endsWith("#")){
				    continue;
				}
				
				var lastSeparatorIndex = sObj.urls[i].lastIndexOf("/");
				var suffixIndex = sObj.urls[i].lastIndexOf(".");
				//最后一个分隔符'/'之后的点认为是后缀
				if(suffixIndex > lastSeparatorIndex){
					sObj.urls[i] = sObj.urls[i].substring(0,suffixIndex);
				}
			}
			monitorCfg.url.urls=sObj.urls;
			/**
	         * Step 2: refresh service url
	         */
	        app.refresh("url.monitor");
		},
		/**
		 * 刷新service url的UI显示
		 */
		updateAppURLMOState:function(urlMOs) {
			
			/**
			 * Step 1: AppURLChart 图表显示：显示各个性能指标原值 
			 */
			var appURLMOChartData={
				qps:new Array(appURLQPSCfg.series.length),
				rt:new Array(appURLQPSCfg.series.length),
				rc:new Array(appURLQPSCfg.series.length)
			};
			
			//scan all app inst
			for(var key in urlMOs) {
			
				var urlMO=urlMOs[key];
				
				//正常情况下返回结果的key应该在appURLQPSCfg.seriesMap，若不在说明做了后缀的截取，需要恢复
				if(!(key in appURLQPSCfg.seriesMap)){
					for(var seriesMapKey in appURLQPSCfg.seriesMap){
						//按照去除后缀的逻辑把seriesMapKey里的后缀去掉，若与当前key相同说明是同一个url
						var lastSeparatorIndex = seriesMapKey.lastIndexOf("/");
						var suffixIndex = seriesMapKey.lastIndexOf(".");
						if(suffixIndex > lastSeparatorIndex && key == seriesMapKey.substring(0,suffixIndex)){
							//将key替换成含有后缀的形式，保证数据可以正确设置
							key = seriesMapKey;
							//将urlMO["id"]替换成含有后缀形式，保证数据可以正常显示
							urlMO["id"]=key;
							break;
						}
					}
				}	
				var tps = urlMO["tps"];
	    		var tavg = urlMO["tavg"];
	    		var err = urlMO["err"];
	    		var count = urlMO["count"];
	    		var mavg=urlMO["mavg"];
	    		
	    		var tpsMORate=this.getMORate("tps",tps);
	    		var errMORate=this.getMORate("err",err,count);
	    		var avgMORate=this.getMORate("tavg",tavg);
	    		var mavgMORate=this.getMORate("mavg",mavg);
				
	    		/**
	    		 * Step 1: update app server mostate
	    		 */
				this.changePerfVal(urlMO["id"]+"_appservicechart_QPS",tpsMORate,tps);
				this.changePerfVal(urlMO["id"]+"_appservicechart_RT",avgMORate,tavg);
				this.changePerfVal(urlMO["id"]+"_appservicechart_RTAVG",mavgMORate,mavg);
				this.changePerfVal(urlMO["id"]+"_appservicechart_ERR",errMORate,err);
				this.changePerfVal(urlMO["id"]+"_appservicechart_TS",undefined,urlMO["timestamp"]);
				
				/**
				 * Step 2: monitor data
				 */
				appURLMOChartData.qps[appURLQPSCfg.seriesMap[key]]=this.convertDPStoArray(urlMO["metric"]["count"]);
				appURLMOChartData.rt[appURLRTCfg.seriesMap[key]]=this.convertDPStoArray(urlMO["metric"]["tavg"]);
				appURLMOChartData.rc[appURLErrCfg.seriesMap[key]]=this.convertDPStoArray(urlMO["metric"]["err"]);
				
				/**
				 * Step 3: RC
				 */
				this.buildRCorACPanel(urlMO,"RC",key+"_appservicechart_RC");
			}
			
			//build app chart
			window["appcharts"].bulid(appURLQPSCfg);
			window["appcharts"].bulid(appURLRTCfg);
			window["appcharts"].bulid(appURLErrCfg);
			
			//run app inst chart data
	        window["appcharts"].run("appURLQPSChart",appURLMOChartData.qps);
			window["appcharts"].run("appURLRTChart",appURLMOChartData.rt);
			window["appcharts"].run("appURLErrChart",appURLMOChartData.rc);
			
		},
		destroyAppURLChart:function() {

			//destroy app chart
			window["appcharts"].reset(appURLQPSCfg);
			window["appcharts"].reset(appURLRTCfg);
			window["appcharts"].reset(appURLErrCfg);
			
			this.quitMonitorChartData("url");
		},
		//----------------------App Server Wnd----------------------
		/**
		 * TODO:App Server Wnd
		 */
		buildAppServerWnd:function(sObj) {
			
			/**
			 * Step 1: init app server chart UI
			 */
			var sObjStr=StringHelper.obj2str(sObj);
			
			var html="<div class=\"appDetailContent\" >" +
            "<div class=\"topDiv\">" +
            "<span class=\"tagTitle\">"+sObj.moid+"</span><br/>"+
            "<span class=\"idTitle\">"+""+"</span>" +
            "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppServerWnd','destroyAppServerChart','AppInstChartWnd')\"></div>" +
            "<div class=\"icon-refresh\" onclick='javascript:app.controller.runAppServerChart("+sObjStr+")'></div>"+
            "<div class=\"glyphicon glyphicon-cog\" onclick='javascript:app.controller.showMonitorCfgDialog(\"server\")'></div>"+
            "</div></div>";
			
			html+="<div class='contentDiv' id='AppServerChartWnd_CT'><div class=\"shine2\"></div>";
			
			// JSE
			if (sObj.isJse==true) {
				html+="<span class=\"title\">JVM虚拟机</span><span>：</span>"+sObj.svrid;
			}
			// JEE
			else {
				html+="<span class=\"title\">服务器实例</span><span>：</span>"+sObj.svrid;
				html+="<div class='indexItemContent'>QPM<span class='osRate' id='"+sObj.moid+"_appserverchart_QPS'>-</span>&nbsp;&nbsp;" +
	     		"全程平均响应(ms)<span class='osRate' id='"+sObj.moid+"_appserverchart_RT'>-</span>&nbsp;&nbsp;" +
	     		"当前分钟时间(ms)<span class='osRate' id='"+sObj.moid+"_appserverchart_RTAVG'>-</span>&nbsp;&nbsp;" +
	     		"错误<span class='osRate' id='"+sObj.moid+"_appserverchart_ERR'>-</span>&nbsp;&nbsp;"+
				"访问时间<span class='osRate' id='"+sObj.moid+"_appserverchart_TS'>-</span></div>";
			}			
			html+="</div>";	
			
			return html;
		},
		runAppServerChart:function(sObj) {
						
			/**
			 * Step 1: refresh app server monitor data
			 */
			//set the current app server inst
			monitorCfg.server.ip=sObj.ip;
			monitorCfg.server.svrid=sObj.svrid;
			monitorCfg.server.inst=sObj.moid;
			monitorCfg.server.isJse=sObj.isJse;
			
			//get the current app server monitor data
			app.refresh("server.monitor");
		},
		/**
		 * refresh app server chart UI
		 */
		updateAppServerMOState:function(serverMOs) {
			
			for(var key in serverMOs) {
				
				var serverMO=serverMOs[key];
				
				//Only for JEE
				if (monitorCfg.server.isJse==false) {
				
					var tps = serverMO["tps"];
		    		var tavg = serverMO["tavg"];
		    		var err = serverMO["err"];
		    		var count = serverMO["count"];
		    		var mavg = serverMO["mavg"];
		    		
		    		var tpsMORate=this.getMORate("tps",tps);
		    		var errMORate=this.getMORate("err",err,count);
		    		var avgMORate=this.getMORate("tavg",tavg);
		    		var mavgMORate=this.getMORate("mavg",mavg);
					
		    		/**
		    		 * Step 1: update app server mostate
		    		 */
					this.changePerfVal(serverMO["id"]+"_appserverchart_QPS",tpsMORate,tps);
					this.changePerfVal(serverMO["id"]+"_appserverchart_RT",avgMORate,tavg);
					this.changePerfVal(serverMO["id"]+"_appserverchart_RTAVG",mavgMORate,mavg);
					this.changePerfVal(serverMO["id"]+"_appserverchart_ERR",errMORate,err);
					this.changePerfVal(serverMO["id"]+"_appserverchart_TS",undefined,serverMO["timestamp"]);
					
					/**
					 * Step 2: build app server chart
					 */
					/**
					 * AppServerChart 图表显示：显示各个性能指标原值 
					 */
					var appServerMOChartData={
						qps:new Array(3),
						rt:new Array(3),
						rc:[]
					};
					
					/**
					 * 访问计数数据
					 */
					appServerMOChartData.qps[appServerQPSCfg.seriesMap["count"]]=this.convertDPStoArray(serverMO["metric"]["count"]);
					appServerMOChartData.qps[appServerQPSCfg.seriesMap["err"]]=this.convertDPStoArray(serverMO["metric"]["err"]);
					appServerMOChartData.qps[appServerQPSCfg.seriesMap["warn"]]=this.convertDPStoArray(serverMO["metric"]["warn"]);
					
					/**
					 * 响应时间数据
					 */
					appServerMOChartData.rt[appServerRTCfg.seriesMap["tmax"]]=this.convertDPStoArray(serverMO["metric"]["tmax"]);
					appServerMOChartData.rt[appServerRTCfg.seriesMap["tmin"]]=this.convertDPStoArray(serverMO["metric"]["tmin"]);
					appServerMOChartData.rt[appServerRTCfg.seriesMap["tavg"]]=this.convertDPStoArray(serverMO["metric"]["tavg"]);
					
					/**
					 * 响应代码计数数据
					 */
			        var series=[];
			        
			        for(var key in serverMO["metric"]) {
			        	
			        	if (key.indexOf("RC")!=0) {
			        		continue;
			        	}
			        	
			        	series[series.length]={
			        		name:key,
			        		data:[]
			        	};
			        	
			        	appServerMOChartData.rc[appServerMOChartData.rc.length]=this.convertDPStoArray(serverMO["metric"][key]);
			        }
			        //set RC series
			        appServerErrCfg.series=series;				
	
					window["appcharts"].bulid(appServerQPSCfg);
					window["appcharts"].bulid(appServerRTCfg);
					window["appcharts"].bulid(appServerErrCfg);
					//run app inst chart data
			        window["appcharts"].run("appServerQPSChart",appServerMOChartData.qps);
					window["appcharts"].run("appServerRTChart",appServerMOChartData.rt);
					window["appcharts"].run("appServerErrChart",appServerMOChartData.rc);
				}
				
				/**
				 * Step 3 jvm chart
				 */
				var appJVMMOChartData={
						heap:new Array(7),
						gc:new Array(4),
						thd:new Array(4),
						cpu:new Array(2),
						cls:new Array(3)
					};
				//heap
				this.prepareChartData(appJVMHeapCfg, appJVMMOChartData["heap"], serverMO, function(index,value){
						return CommonHelper.getB2Human(value,false,2);
					});
				//gc
				this.prepareChartData(appJVMGCCfg, appJVMMOChartData["gc"], serverMO);
				//thread
				this.prepareChartData(appJVMThreadCfg, appJVMMOChartData["thd"], serverMO);
				//cls
				this.prepareChartData(appJVMClsCfg, appJVMMOChartData["cls"], serverMO);
				//cpu
				this.prepareChartData(appJVMCPUCfg, appJVMMOChartData["cpu"], serverMO,function(index,value) {
					return Math.ceil(value*100)/100;//Math.ceil(value*10000/cpuSystem[index]["y"])/100;
				});
			
				window["appcharts"].bulid(appJVMHeapCfg);
				window["appcharts"].bulid(appJVMGCCfg);
				window["appcharts"].bulid(appJVMThreadCfg);
				window["appcharts"].bulid(appJVMCPUCfg);
				window["appcharts"].bulid(appJVMClsCfg);
				
				window["appcharts"].run("appJVMHeapChart",appJVMMOChartData.heap);
				window["appcharts"].run("appJVMGCChart",appJVMMOChartData.gc);
				window["appcharts"].run("appJVMThreadChart",appJVMMOChartData.thd);
				window["appcharts"].run("appJVMCPUChart",appJVMMOChartData.cpu);
				window["appcharts"].run("appJVMClsChart",appJVMMOChartData.cls);
			}
		},
		destroyAppServerChart : function() {
			
			//only for JEE
			if (monitorCfg.server.isJse==false) {
				window["appcharts"].reset("appServerQPSChart");
				window["appcharts"].reset("appServerRTChart");
				window["appcharts"].reset("appServerErrChart");
			}
			
			window["appcharts"].reset("appJVMHeapChart");
			window["appcharts"].reset("appJVMGCChart");
			window["appcharts"].reset("appJVMThreadChart");
			window["appcharts"].reset("appJVMCPUChart");
			window["appcharts"].reset("appJVMClsChart");
			
			this.quitMonitorChartData("server");
		},				
		//----------------------App Detail--------------------------
		/**
		 * TODO:App Detail
		 */
		buildAppDetail:function(obj) {
			
			var appgpid=obj.appgpid;
			
			var res=this.getProfileTagsAndInsts(appgpid);
			
			var str=" <div class=\"appDetailContent\" >" +
			          this.buildAppDetailTop(res["appinsts"][0],res["tags"])+
			          this.buildAppinstProfiles(res["appinsts"])+
			          "</div>";
			
			return str;            
		},
		buildAppDetailTop:function(jsonObj,tags) {
			return " <div class=\"topDiv\">" +
			"<span class=\"tagTitle\">"+tags+"</span><br/>"+
            "<span class=\"idTitle\">"+jsonObj.appid+"</span>" +
            "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppTopoWnd')\"></div>" +
            "</div>";
		},
		buildAppinstProfiles:function(appinsts) {
			var sb=new StringBuffer();
			for(var i=0;i<appinsts.length;i++) {
				sb.append(this.buildAppinstProfile(appinsts[i]));
			}
			
			return sb.toString();
		},
		buildAppinstProfile:function(jsonObj) {
			
			//get the app service urls
			var cptservices=eval("("+jsonObj["cpt.services"]+")");
			
			var sb=new StringBuffer();
			
			 
			sb.append("       <div class=\"contentDiv\"><div class=\"shine2\"></div>" +
            "                <span class=\"title\">应用实例</span><span class=\"timeTitle\">"+TimeHelper.getTime(jsonObj["time"])+"</span><br/>" +
            "                <div class=\"kv\">" +
            "                    <span class=\"kvField\">容器</span><span>：</span>"+jsonObj["ip"] +"&nbsp;<span style='font-size:14px;color:#bbbbbb;'>["+jsonObj["host"]+"]</span>"+
            "                </div>" +
            "                <div class=\"kv\">" +
            "                    <span class=\"kvField\">URL</span><span>：</span>"+jsonObj["appurl"] +
            "                </div>"+
            "                <div class=\"kv\">" +
            "                    <span class=\"kvField\">名称</span><span>：</span>"+jsonObj["appname"] +
            "                </div>" +
            "                <div class=\"kv\">" +
            "                    <span class=\"kvField\">描述</span><span>：</span>"+jsonObj["appdes"] +
            "                </div>" +
            "                <div class=\"kv\">" +
            "                    <span class=\"kvField\">应用组名</span><span>：</span>"+jsonObj["appgroup"] +
            "                </div>" +
            "                <div class=\"kv\">" +
            "                    <span class=\"kvField\">服务器/JVM</span><span>：</span>"+jsonObj["svrid"] +
            "                </div>" +
            "               <div class=\"kv\">" +
            "                  <span class=\"kvField\">路径</span><span>：</span>"+jsonObj["webapproot"] +
            "               </div>");
			
			sb.append( "<div class=\"kv\"><span class=\"kvField\">自定义监控指标</span><span>：</span>"+jsonObj["appmetrics"] +"</div>");
			
			sb.append( "<div class=\"kv\"><span class=\"kvField\">MOF设置</span><span>：</span>"+jsonObj["mofmeta"] +"</div>");
			

            if (jsonObj["cpt.clients"]!=undefined&&jsonObj["cpt.clients"]!="[]") {
            	sb.append("<div class=\"kv\"><span class=\"kvField\">客户端组件</span><span>：</span>");
            	
            	sb.append(this.getAppProfileDetail.cpt_clients(jsonObj.id,jsonObj["cpt.clients"]));
            	sb.append("</div>");
            }
			
            sb.append("<div class=\"kv\"><span class=\"kvField\">服务组件</span><span>：</span>");
            //----------------------MSCP Application-----------------
            
            this.buildComponentProfile(sb,jsonObj,"cpt.mscp.http","profile.info.mscphttp","组件[MSCP.Http]",cptservices,"_detail_cpt_mscphttp","cpt_service");

            //----------------------Dubbo Application-----------------
            this.buildComponentProfile(sb,jsonObj,"cpt.dubbo.provider","profile.info.dubboprovider","组件[Dubbo.Provider]",cptservices,"_detail_cpt_dubboprovider","cpt_service");

            //----------------------JEE Application---------------------
            this.buildComponentProfile(sb,jsonObj,"cpt.servlets","profile.info.servlet","组件[Servlets]",cptservices,"_detail_cpt_servlets","cpt_service");
        
            this.buildComponentProfile(sb,jsonObj,"cpt.jaxws","profile.info.jaxws","组件[JAXWS]",cptservices,"_detail_cpt_jaxws","cpt_service");     

            this.buildComponentProfile(sb,jsonObj,"cpt.jaxwsP","profile.info.jaxwsp","组件[JAXWSProvider]",cptservices,"_detail_cpt_jaxwsp","cpt_service");

            this.buildComponentProfile(sb,jsonObj,"cpt.jaxrs","profile.info.jaxrs","组件[JAXRS]",cptservices,"_detail_cpt_jaxrs","cpt_service");

            this.buildComponentProfile(sb,jsonObj,"cpt.springmvc","profile.info.springmvc","组件[SpringMVC]",cptservices,"_detail_cpt_springmvc","cpt_service");

            this.buildComponentProfile(sb,jsonObj,"cpt.springmvcRest","profile.info.springmvcrest","组件[SpringMVCRest]",cptservices,"_detail_cpt_springmvcRest","cpt_service");

            this.buildComponentProfile(sb,jsonObj,"cpt.struts2","profile.info.struts2","组件[Struts2]",cptservices,"_detail_cpt_struts2","cpt_service");

            sb.append("</div>");
            
            sb.append("<div class=\"kv\"><span class=\"kvField\">其他组件</span><span>：</span>");
            
            if (jsonObj["logs.log4j"]!=undefined&&jsonObj["logs.log4j"]!="{}") {
            	sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+jsonObj.id+"_detail_logs')\">日志配置</span><div style='display:none;' id='"+jsonObj.id+"_detail_logs'>"+this.getAppProfileDetail.cpt_log(jsonObj.id,jsonObj["logs.log4j"])+"</div>");
            }
            this.buildComponentProfile(sb,jsonObj,"jars.lib","","类库","","_detail_lib","lib")

            
            //---------------------MSCP Application---------------
            this.buildComponentProfile(sb,jsonObj,"cpt.mscp.timework","profile.info.mscptimeworker","组件[MSCP.TimeWork]","","_detail_cpt_mscptimework","cpt")

            //---------------------JEE Application--------------------
            this.buildComponentProfile(sb,jsonObj,"cpt.filters","profile.info.filter","组件[Filters]","","_detail_cpt_filters","cpt")

            this.buildComponentProfile(sb,jsonObj,"cpt.listeners","profile.info.listener","组件[Listeners]","","_detail_cpt_listeners","cpt")

            sb.append("</div>");
            
            
            
            sb.append("</div>");
			return sb.toString();
		},
		//build component element
		buildComponentProfile:function(sb,jsonObj,key,targetStr,titleStr,cptservices,detailStr,methodName){
			 if (jsonObj[key]!=undefined&&jsonObj[key]=="@LAZY") {
	                
	            	var obj={appgroup:jsonObj["appgroup"],appurl:jsonObj["o_appurl"],id:jsonObj["id"],target:targetStr,title:titleStr,cpt:cptservices,detail:detailStr};
	            	var objstr=JSON.stringify(obj);
	            	sb.append("<span class=\"componentExpandButton\" onclick='app.controller.getAppProfileDetail."+methodName+"("+objstr+");'>"+titleStr+"</span><div style='display:none;' id='"+jsonObj.id+detailStr+"'></div>");
	            	
	         }
		},
		getAppProfileDetail:{
			//----------------------fields--------------------
			//映射字段，便于人类读取
			cptkeys:{
				'des':"部署描述符",
				'anno':"注解描述符",
				'dyn':"动态添加",
				'methods':"服务方法",
				'para':'方法参数',
				'engine':'服务引擎'
			},
			//----------------------common------------------------
			//call app profile detail
			callAppDetail:function(target,field,callback) {
				/**
				 * 构造请求
				 */
				var data = {
						intent: 'profile.detail',
						request: {
							target:target,
							field:field
						}
				}
				
				AjaxHelper.call({
		            url: '../../rs/godeye/profile/q/detail',
		            data: StringHelper.obj2str(data),
		            cache: false,
		            type: 'POST',
		            dataType: 'html',
		            timeout: 5000,
		            success: function(result){
		            	console.log("PROFILE DETAIL RESP>> "+result);
		                var obj = StringHelper.str2obj(result);
		                var res = obj["rs"];
		                if (obj=="ERR"||res=="ERR") {
		                	
		                    alert("应用画像查询["+intent+"]失败:"+result);
		                }
		                else {
		                    callback(res);
		                }
		            },
		            error: function(result){
		            	 alert("应用画像查询["+intent+"]失败:" + result);
		            }
		        });
			},
			getCptKeyName:function(fkey) {
				var k=this.cptkeys[fkey];
				if (k==undefined) {
					return fkey;
				}
				else {
					return k;
				}
			},
			buildServiceURLs:function(key,cptservices) {
				
				var cptservice=cptservices[key];
				
				if (cptservice==undefined) {
					return "";
				}
				
				var sb=new StringBuffer();
				
				for(var i=0;i<cptservice.length;i++) {
					
					sb.append("<div class='kvSubField'>"+cptservice[i].replace("#","")+"</div>");
					
				}
				
				return sb.toString();
				
			},
			buildCptItem:function(val) {
				
				if (val instanceof Array) {
					var str="";
					for (var i=0;i<val.length;i++) {
						var fval=val[i];						
						str+="<div class='componentFTab'>"+this.buildCptItem(fval)+"</div>";
					}
					return str;
				}
				else if (typeof val == "function" ) {
					//ignore
					return "";
				}
				else if (typeof val == "object") {
					var str="";
					for(var fkey in val) {
						var fname=this.getCptKeyName(fkey);
						str+="<div class='componentfeature'>"+fname+"</div>";
						var fval=val[fkey];
						str+="<div class='componentFTab'>"+this.buildCptItem(fval)+"</div>";
					}
					return str;
				}
				else {
					return "<div class='componentFTab'>"+val+"</div>";
				}
			},
			//-----------------------public-----------------------						
			//show log config
			log:function(d) {
				var logs=eval("("+d+")");
				return JsonHelper.asHTML(logs);
			},
			//show lib config: LAZY加载
			lib:function(jsonObj) {
				
				var isDisplay=app.controller.openClose(jsonObj.id+"_detail_lib");

				var detailCtn=HtmlHelper.id(jsonObj.id+"_detail_lib");
				
				if (isDisplay==true&&detailCtn.innerHTML=="") {
					var libkey=jsonObj["appgroup"]+"@"+jsonObj["appurl"];
					this.callAppDetail("profile.info.jarlib", libkey, function(result) {
						
						try {
							var detail=eval("("+result+")");
							
							var libs=eval("("+detail[libkey]+")");
							
							var sb=new StringBuffer();
							for(var key in libs) {
								sb.append("<div><span class=\"kvSubField\">"+key+"</span></div>");
							}
							detailCtn.innerHTML=sb.toString();
						}catch(e) {
							detailCtn.innerHTML="加载类库失败："+e;
						}
					});
				}
			},
			//服务组件显示
			cpt_service:function(objstr) {
				
				var isDisplay=app.controller.openClose(objstr.id+objstr.detail);

				var detailCtn=HtmlHelper.id(objstr.id+objstr.detail);
				
				if (isDisplay==true&&detailCtn.innerHTML=="") {
					var libkey=objstr["appgroup"]+"@"+objstr["appurl"];
					var target=objstr.target;
					this.callAppDetail(target, libkey, function(result) {
						
						try {
							var detail=eval("("+result+")");
							
							var libs=eval("("+detail[libkey]+")");
							
							var sb=new StringBuffer();
							for(var key in libs) {
								sb.append("<div class='componentName'>"+key+"</div>");
								sb.append("<div class='componentTab'>");
								sb.append("<div class='componentTabHead' onclick=\"app.controller.openClose('"+objstr.id+"-"+key+"_urls')\">服务URL</div>");
								sb.append("<div id='"+objstr.id+"-"+key+"_urls' class='componentFTab' style='display:none;'>");
								sb.append(app.controller.getAppProfileDetail.buildServiceURLs(key,objstr.cpt));
								sb.append("</div>");
								sb.append("</div>");
								sb.append("<div class='componentTab'>");
								sb.append("<div class='componentTabHead' onclick=\"app.controller.openClose('"+objstr.id+"-"+key+"_profile')\">服务画像</div>");
								var vals=libs[key];
								sb.append("<div id='"+objstr.id+"-"+key+"_profile' class='componentFTab' style='display:none;'>"+app.controller.getAppProfileDetail.buildCptItem(vals)+"</div>");
								sb.append("</div>");
							}
							detailCtn.innerHTML=sb.toString();
						}catch(e) {
							detailCtn.innerHTML="加载"+objstr.title+"失败："+e;
						}
					});
				}
			},
			
			//其他组件显示
			cpt:function(objstr) {
				
				var isDisplay=app.controller.openClose(objstr.id+objstr.detail);
					detailCtn=HtmlHelper.id(objstr.id+objstr.detail);
			
				if (isDisplay==true&&detailCtn.innerHTML=="") {
					var libkey=objstr["appgroup"]+"@"+objstr["appurl"];
					var target=objstr.target;
					this.callAppDetail(target, libkey, function(result) {
						
						try {
							var detail=eval("("+result+")");
							
							var libs=eval("("+detail[libkey]+")");
							
							var sb=new StringBuffer();
							for(var key in libs) {
								sb.append("<div class='componentName'>"+key+"</div>");
								sb.append("<div class='componentTab'>");
								sb.append("<div class='componentTabHead' onclick=\"app.controller.openClose('"+objstr.id+"-"+key+"_profile')\">组件画像</div>");
								var vals=libs[key];
								sb.append("<div id='"+objstr.id+"-"+key+"_profile' class='componentFTab' style='display:none;'>"+app.controller.getAppProfileDetail.buildCptItem(vals)+"</div>");
								sb.append("</div>");
							}
							detailCtn.innerHTML=sb.toString();
						}catch(e) {
							detailCtn.innerHTML="加载"+objstr.title+"失败："+e;
						}
					});
				}
			},
			cpt_log:function(appinstId,d) {
				
				var cpt=eval("("+d+")");
				var sb=new StringBuffer();
				
				for (var key in cpt) {
					sb.append("<div class='componentName'>"+key+"</div>");
					sb.append("<div class='componentTab'>");
					sb.append("<div class='componentTabHead' onclick=\"app.controller.openClose('"+appinstId+"-"+key+"_profile')\">组件画像</div>");
					var vals=cpt[key];
					sb.append("<div id='"+appinstId+"-"+key+"_profile' class='componentFTab' style='display:none;'>"+this.buildCptItem(vals)+"</div>");
					sb.append("</div>");
				}
				
				return sb.toString();
			},
			//客户端组件显示
			cpt_clients:function(appinstId,d) {
				
				var clients=eval("("+d+")");
				
				var clientsMap=new Map();
				
				for(var i=0;i<clients.length;i++) {
					
					var client=clients[i];
					var vals=client["values"];
					var type=vals["type"];
					
					var list=clientsMap.get(type);
					
					if (list==undefined) {
						list=new List();
						clientsMap.put(type,list);
					}
					
					list.add(client);				
				}
				
				var sb=new StringBuffer();
				
				for(var i=0;i<clientsMap.count();i++) {
					
					var type=clientsMap.mapNames.get(i);
					
					sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+appinstId+"_detail_cpt_clients_"+type+"')\">组件["+type+"]</span><div style='display:none;' id='"+appinstId+"_detail_cpt_clients_"+type+"'>");
	            	
					var cl=clientsMap.get(type);
					
					for(var j=0;j<cl.count();j++) {
						var client=cl.get(j);
						var vals=client["values"];
						
						var dn=vals["dn"];
						
						if (dn!=undefined) {
							dn="&nbsp;("+dn+")";
						}
						else {
							dn="";
						}
						
						sb.append("<div class='componentName'>"+client["id"]+dn+"</div>");
						sb.append("<div class='componentTab'>");
						sb.append("<div class='componentTabHead' onclick=\"app.controller.openClose('"+appinstId+"-"+client["id"]+"_profile')\">组件画像<span class=\"timeTitle\">"+TimeHelper.getTime(vals["ts"])+"</span></div>");
						sb.append("<div id='"+appinstId+"-"+client["id"]+"_profile' class='componentFTab' style='display:none;'>");
						
						var urls=vals["urls"];
						
						for(var path in urls) {
							
							var pathObj=urls[path];
							
							sb.append("<div class='componentfeature'>"+path+"<span class=\"timeTitle\">"+TimeHelper.getTime(pathObj["ts"])+"</span></div>");
							
							for(var action in pathObj) {
								
								if (action=="ts") {
									continue;
								}
								
								sb.append("<div class='componentFTab'>"+action+"<span class=\"timeTitle\">"+TimeHelper.getTime(pathObj[action])+"</span></div>");
							}
						}
						
						sb.append("</div>");
						sb.append("</div>");
					}
					sb.append("</div>");
					
				}
				
				return sb.toString();
			}
		},
		//----------------------App Customized Metrics--------------------------
		//TODO: Customized Metrics
		//build AppCustMetricWnd
		buildAppCustMetricWnd:function(sObj) {
			
			var sObjStr=StringHelper.obj2str(sObj);

			var html=  "<div class=\"appDetailContent\" >" +
            "<div class=\"topDiv\">" +
            "<span class=\"tagTitle\">"+sObj.moid+"</span><br/>"+
            "<span class=\"idTitle\">自定义指标</span>" +
            "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppCustMetricWnd','destroyAppCustMetricWnd','AppInstChartWnd')\"></div>" +
            "<div class=\"icon-refresh\" onclick='javascript:app.controller.runAppCustMetricChart("+sObjStr+")'></div>"+
            "<div class=\"glyphicon glyphicon-cog\" onclick='javascript:app.controller.showMonitorCfgDialog(\"cust\")'></div>"+
            "</div></div>";
			
			html+="<div class='contentDiv' id='AppCustMetricWnd_CT'><div class=\"shine2\"></div>";
			
			return html;
		},
		//run app custom metric chart
		runAppCustMetricChart:function(sObj) {
			
			var metrics=sObj["metrics"];
			
			/**
			 * Step 1: refresh app cust metrics data
			 */
			//set the current app server inst
			monitorCfg.cust.metrics=eval("("+metrics+")");
			monitorCfg.cust.ip=sObj.ip;
			monitorCfg.cust.svrid=sObj.svrid;
			monitorCfg.cust.inst=sObj.moid;
			
			//get the current app cust metrics data
			app.refresh("cust.monitor");

		},
		//update app cust mo state
		updateAppCustMetricMOState:function(custMOs) {
			
			for(var custId in custMOs) {
				
				var custMO=custMOs[custId];
				
				for(var mc in monitorCfg.cust.metrics) {
					
					var mChartCfg=JsonHelper.clone(appCustMetricCfg);
					
					mChartCfg.id="AppCustMetricWnd_"+mc;
					mChartCfg.series[mChartCfg.series.length]={
						name:mc,
						data:[]
					};
					
					var data=this.convertDPStoArray(custMO["metric"][mc]);
					
					window["appcharts"].bulid(mChartCfg);
					
					window["appcharts"].run("AppCustMetricWnd_"+mc,[data]);
				}	
			}
		},
		//destroy AppCustMetricWnd
		destroyAppCustMetricWnd:function() {
			
			for(var mc in monitorCfg.cust.metrics) {
				
				window["appcharts"].reset("AppCustMetricWnd_"+mc);
			}
			
			this.quitMonitorChartData("cust");
		},
		//----------------------App Cluster Stream--------------------------
		//TODO App Cluster Stream
		buildAppClusterTopWnd:function(sObj) {
			return appStream.buildAppClusterTopWnd(sObj);
		},
		runAppClusterTop:function(sObj) {
			appStream.start(sObj,{view:"app",appgroup:sObj["appgroup"],appid:sObj["appid"]});
		},
		//----------------------App Group Stream-----------------------------
		//TODO App Group Stream
		buildAppGroupTopWnd:function(sObj) {
			return appStream.buildAppGroupTopWnd(sObj);
		},
		runAppGroupTop:function(sObj) {
			appStream.start(sObj,{view:"appgroup",appgroup:sObj["appgroup"]});
		},
		//----------------------App Global Eye-------------------------------
		//TODO App Global Eye
		buildGlobalEyeTopWnd:function(sObj) {
			return appStream.buildGlobalEyeTopWnd();
		},
		runGlobalEyeTop:function(sObj) {
			appStream.start(sObj,{view:"global"});
		},
		//-----------------------------AppProxyEdgeDetailWnd-------------------------------------
		//TODO AppProxyEdgeDetailWnd
		buildAppProxyEdgeDetailWnd:function(sObj) {
			return appStream.buildAppProxyEdgeDetailWnd(sObj);
		},
		//------------------------------AppUnknowEdgeDetailWnd-------------------------------
		//TODO AppUnknowEdgeDetailWnd
		buildAppUnknowEdgeDetailWnd:function(sObj) {
			return  appStream.buildAppUnknowEdgeDetailWnd(sObj);
		},
		//TODO-----------------------------App Client URL Chart--------------------------------------
		// App Client URL Chart
		buildAppClientURLChartWnd:function(sObj) {
			/**
			 * Step 1: build App Client URL content
			 */	
			var sObjStr=StringHelper.obj2str(sObj);
			
			var backWndId="";
			
			if (sObj["backWndId"]!=undefined) {
				backWndId=",'"+sObj["backWndId"]+"'";
			}
			
			var title=sObj.name;
			
			var html="<div class=\"appDetailContent\" >" +
            "<div class=\"topDiv\">" +
            "<span class=\"tagTitle\">"+title+"</span><br/>"+
            "<span class=\"idTitle\">客户端组件</span>" +
            "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppClientURLChartWnd','destroyAppClientURLChartWnd'"+backWndId+")\"></div>" +
            "<div class=\"icon-refresh\" onclick='javascript:app.controller.runAppClientURLChart("+sObjStr+")'></div>"+
            "<div class=\"glyphicon glyphicon-cog\" onclick='javascript:app.controller.showMonitorCfgDialog(\"client\")'></div>"+
            "</div></div>";
			
			html+="<div class='contentDiv' id='AppClientURLChartWnd_CT'><div class=\"shine2\"></div>";
			
			var client=eval("("+sObj["clientStr"]+")");
			
			var sb=new StringBuffer();
			
			var index=0;
	        var series=[];
	        var seriesMap={};
			
			for (var path in client["values"].urls) {
				
				var urlAttrs=client["values"].urls[path];
				
				var url=client["id"];
				if (client["id"].indexOf("http:")==0) {
					url+=path;
				}
								
				/**
				 * App Client URL 性能显示
				 */
				sb.append("<div class='indexItem'>");
				sb.append("<div class='indexItemHead'><span class='indexItemTag'>"+(index+1)+"</span><span class='indexItemId'>"+url+"</span></div>");
				sb.append("<div class='indexItemContent'>");
				sb.append("QPM<span class='osRate' id='"+url+"_appclientchart_QPS'>-</span>&nbsp;&nbsp;" +
		         		"全程平均响应(ms)<span class='osRate' id='"+url+"_appclientchart_RT'>-</span>&nbsp;&nbsp;" +
		         		"当前分钟响应(ms)<span class='osRate' id='"+url+"_appclientchart_RTAVG'>-</span>&nbsp;&nbsp;" +
		         		"错误<span class='osRate' id='"+url+"_appclientchart_ERR'>-</span>&nbsp;&nbsp;"+
		         		"访问时间<span id='"+url+"_appclientchart_TS' class='osRate' >"+TimeHelper.getTime(urlAttrs["ts"])+"</span>");
				sb.append("<br/>动作<span class='osRate' id='"+url+"_appclientchart_AC'>-</span>");
				sb.append("</div>");
				
				/**
				 * App Client URL 图表Series
				 */
				//prepare series
	        	series[series.length]={
	        			name:(index+1),
	    	        	data:[],
	    	        	_id:url
	        	};
	        	seriesMap[url]=series.length-1;
	        	
	        	/**
	        	 * 扩展处理
	        	 */
	        	//JDBC Connection Pool
	        	if(url.indexOf("jdbc:")>-1) {
	        		var buttonHtml=appDBPool.buildDBPoolButton({url:url,clientID:sObj.hostport+"#"+sObj.appid+"#"+url});
	        		sb.append(buttonHtml);
	        	}
	        	
	        	sb.append("</div>");
	        	
	        	index++;
			}
			
			html+=sb.toString()+"</div>";
			
			/**
			 * Step 2: prepare Client series
			 */
	        /**
	         * set app client chart series
	         */
	        appClientURLQPSCfg.series=series;
	        appClientURLRTCfg.series=series;
	        appClientURLErrCfg.series=series;
	        /**
	         * set app client chart series order map
	         */
	        appClientURLQPSCfg.seriesMap=seriesMap;
	        appClientURLRTCfg.seriesMap=seriesMap;
	        appClientURLErrCfg.seriesMap=seriesMap;
			
			return html;
		},
		runAppClientURLChart:function(sObj) {
			
			var urls=[];
			
			for(var i=0;i<sObj.urls.length;i++) {
				urls[i]={
						ip:sObj.ip,
						svrid:sObj.svrid,
						url:sObj.hostport+"#"+sObj.appid+"#"+sObj.urls[i]
				};
			}
			
			this.runAppClientURLMO(urls);
		},
		runAppClientURLMO:function(urls) {
			/**
			 * Step 1: set the monitorCfg.client data to monitor query
			 */
			monitorCfg.client.urls=urls;
			
			/**
	         * Step 2: refresh service url
	         */
	        app.refresh("client.monitor");
		},
		updateAppClientURLMOState:function(urlMOs) {
			
			if (window.winmgr.isHide("AppClientURLChartWnd")) {
				return;
			}
			
			/**
			 * Step 1: AppClientURLChart 图表显示：显示各个性能指标原值 
			 */
			var appClientURLMOChartData={
				qps:new Array(appClientURLQPSCfg.series.length),
				rt:new Array(appClientURLQPSCfg.series.length),
				rc:new Array(appClientURLQPSCfg.series.length)
			};
			
			//scan all app client inst
			for(var key in urlMOs) {
			
				var urlMO=urlMOs[key];
				
				var clientUrl=key.split("#")[2];
				
				var tps = urlMO["tps"];
	    		var tavg = urlMO["tavg"];
	    		var err = urlMO["err"];
	    		var count = urlMO["count"];
	    		var mavg=urlMO["mavg"];
	    		
	    		var tpsMORate=this.getMORate("tps",tps);
	    		var errMORate=this.getMORate("err",err,count);
	    		var avgMORate=this.getMORate("tavg",tavg);
	    		var mavgMORate=this.getMORate("mavg",mavg);
	    		
	    		/**
	    		 * Step 1: update app client mostate
	    		 */
				this.changePerfVal(clientUrl+"_appclientchart_QPS",tpsMORate,tps);
				this.changePerfVal(clientUrl+"_appclientchart_RT",avgMORate,tavg);
				this.changePerfVal(clientUrl+"_appclientchart_RTAVG",mavgMORate,mavg);
				this.changePerfVal(clientUrl+"_appclientchart_ERR",errMORate,err);
				
				//update access timestamp
				if (urlMO["timestamp"]!=undefined) {
					//var time=TimeHelper.getTime(urlMO["timestamp"]);
					this.changePerfVal(clientUrl+"_appclientchart_TS",undefined,urlMO["timestamp"]);
				}
				
				/**
				 * Step 2: monitor data
				 */
				appClientURLMOChartData.qps[appClientURLQPSCfg.seriesMap[clientUrl]]=this.convertDPStoArray(urlMO["metric"]["count"]);
				appClientURLMOChartData.rt[appClientURLRTCfg.seriesMap[clientUrl]]=this.convertDPStoArray(urlMO["metric"]["tavg"]);
				appClientURLMOChartData.rc[appClientURLErrCfg.seriesMap[clientUrl]]=this.convertDPStoArray(urlMO["metric"]["err"]);
				
				/**
				 * Step 3: action data
				 */
				this.buildRCorACPanel(urlMO,"AC",clientUrl+"_appclientchart_AC");
				
				/**
				 * Step 4: 扩展处理
				 */
				//JDBC Connection Pool
	        	if(clientUrl.indexOf("jdbc:")>-1) {
	        		appDBPool.openDBPoolChart({url:clientUrl,clientID:key},false);
	        	}
			}
			
			//build app client chart
			window["appcharts"].bulid(appClientURLQPSCfg);
			window["appcharts"].bulid(appClientURLRTCfg);
			window["appcharts"].bulid(appClientURLErrCfg);
			
			//run app client chart data
	        window["appcharts"].run("appClientURLQPSChart",appClientURLMOChartData.qps);
			window["appcharts"].run("appClientURLRTChart",appClientURLMOChartData.rt);
			window["appcharts"].run("appClientURLErrChart",appClientURLMOChartData.rc);
		},
		destroyAppClientURLChartWnd:function() {
			//destroy app chart
			window["appcharts"].reset(appClientURLQPSCfg);
			window["appcharts"].reset(appClientURLRTCfg);
			window["appcharts"].reset(appClientURLErrCfg);
			
			this.quitMonitorChartData("client");
		},
		//----------------------Invoke Chain Window--------------------------
        /**
         * TODO Invoke Chain Window
         */
		buildAppIVCWnd:function(sObj) {
			return appAPM.buildAppIVCWnd(sObj);
		},
		runAppIVCWnd:function(sObj) {
			appAPM.runAppIVCWnd(sObj);
		},
		destroyAppIVCWnd:function() {
			
		},
		//----------------------Invoke Chain Trace Window--------------------------
        /**
         * TODO Invoke Chain Trace Window
         */
		buildAppIVCTraceWnd:function(sObj) {
			return appAPM.buildAppIVCTraceWnd(sObj);
		},
		runAppIVCTraceWnd:function(sObj) {
			appAPM.runAppIVCTraceWnd(sObj);
		},
		destroyAppIVCTraceWnd:function() {
			
		},
		//----------------------Invoke Chain data Window--------------------------
        /**
         * TODO Invoke Chain data Window
         */
		buildAppIVCDataWnd:function(sObj) {
			return appAPM.buildAppIVCDataWnd(sObj);
		},
		runAppIVCDataWnd:function(sObj) {
			appAPM.runAppIVCDataWnd(sObj);
		},
		destroyAppIVCDataWnd:function() {
			
		},
		//----------------------APM Cfg Window--------------------------
        /**
         * TODO APM Cfg Window
         */
		buildAppAPMCfgWnd:function(sObj) {
			return appAPM.buildAppAPMCfgWnd(sObj);
		},
		runAppAPMCfgWnd:function(sObj) {
			appAPM.runAppAPMCfgWnd(sObj);
		},
		destroyAppAPMCfgWnd:function() {
			appAPM.destroyAppAPMCfgWnd();
		},
		//----------------------App NewLog Wnd-------------------
		/**
		 * TODO : App NewLog Wnd for Log Search
		 */
		buildAppNewLogWnd:function(sObj) {
			return appLog.buildAppNewLogWnd(sObj);
		},
		runAppNewLogWnd:function(sObj) {
			appLog.runAppNewLogWnd(sObj);
		},
		destroyAppNewLogWnd:function() {
			
		},
		//----------------------App NewLogRoll Wnd-------------------
		/**
		 * TODO : App NewLogRoll Wnd for Log Rolling
		 */
		buildAppNewLogRollWnd:function(sObj) {
			return appLog.buildAppNewLogRollWnd(sObj);
		},
		runAppNewLogRollWnd:function(sObj) {
			appLog.runAppNewLogRollWnd(sObj);
		},
		destroyAppNewLogRollWnd:function() {
			
		},
		// TODO -------------------------------调用链配置窗口---------------------------------------------------
		/**
		 * buildAppIVCCfgWnd
		 */
		buildAppIVCCfgWnd:function(sObj) {
			return appAPM.buildAppIVCCfgWnd(sObj);
		},
		
		/**
		 * runAppIVCCfgWnd
		 */
		runAppIVCCfgWnd:function(sObj) {
			appAPM.runAppIVCCfgWnd(sObj);
		},
		
		/**
		 * destroyAppIVCCfgWnd
		 */
		destroyAppIVCCfgWnd:function() {
			
		},
		// TODO -------------------------------日志配置窗口---------------------------------------------------
		/**
		 * buildNewLogCfgWnd
		 */
		buildAppNewLogCfgWnd:function(sObj) {
			return appLog.buildAppNewLogCfgWnd(sObj);
		},
		
		/**
		 * runNewLogCfgWnd
		 */
		runAppNewLogCfgWnd:function(sObj) {
			appLog.runAppNewLogCfgWnd(sObj);
		},
		
		/**
		 * destroyNewLogCfgWnd
		 */
		destroyAppNewLogCfgWnd:function(sObj) {
			
		},
		//----------------------DB Inst Chart--------------------------
        /**
         * TODO db inst chart
         */
        buildDbInstChart: function(dbObj) {
            var backWndId="";
            if (dbObj["backWndId"]!=undefined) {
                backWndId=",'"+dbObj["backWndId"]+"'";
            }
            
            var user = dbObj.schema;
            if(user != ""){
            	user = "用户名:" + user;
            }
            
            // head
            var html="<div class=\"appDetailContent\" >" +
                "<div class=\"topDiv\">" +
                "<span class=\"tagTitle\">"+dbObj.uri+"</span><br/>"+
                "<span class=\"idTitle\">"+user+"</span>" +
                "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppInstChartWnd','destroyAppInstChart'"+backWndId+")\"></div>" +
                "<div class=\"icon-refresh\" onclick='javascript:app.controller.runDbInstChart("+JSON.stringify(dbObj)+")'></div>"+    
                "</div></div>";
            
            // db info
            var model = dbObj.model;
            for(var i = 0; i < dbObj.node.length; i++){
                var node = dbObj.node[i];
                var nodeIdx = node+'_'+i;
                html += "<div class='contentDiv'>";
                html += "<span class=\"idTitle\">"+node+"</span>";
                html += "<div class=\"shine2\"></div><span class='title'>系统指标</span><div>";
				for(k in model.os){
				    html += "<div class=\"kv\"><span class=\"kvField\">"+model.os[k]+"</span><span>：</span>" + "<span id='"+nodeIdx+"_os_"+k+"'>-</span>" + "</div>";
				}
                html += "<div class='contentDiv'><div class=\"shine2\"></div><span class='title'>实例指标</span><div>";
                for(k in model.proc){
                    html += "<div class=\"kv\"><span class=\"kvField\">"+model.proc[k]+"</span><span>：</span>" + "<span id='"+nodeIdx+"_proc_"+k+"'>-</span>" + "</div>";
                }
                if(dbObj.model.schema){
                    html += "<div class='contentDiv'><div class=\"shine2\"></div><span class='title'>操作指标</span><div>";
                    for(k in model.schema){
                        html += "<div class=\"kv\"><span class=\"kvField\">"+model.schema[k]+"</span><span>：</span>" + "<span id='"+nodeIdx+"_schema_"+k+"'>-</span>" + "</div>";
                    }
                    
                    html += "<span class=\"componentExpandButton\" ondblclick=\"app.controller.loadDbSlowOptions('slowsql_"+nodeIdx+"', '"+node+"', '"+dbObj.dbname+"', '"+dbObj.type+"', '"+dbObj.schema+"')\">慢操作["+dbObj.dbname+"]</span><div style='display:none;' id='slowsql_"+nodeIdx+"'></div>";
                }
                html += "<br>"
            }
            
            return html;
        },
        runDbInstChart : function(dbObj) {
        	var end = Math.ceil(new Date().getTime() / 1000);
        	var start = end - 3600;
        	var reqs=[];
        	var models=[];
        	var prefix = dbObj.type + '://';
            for(var idx = 0; idx < dbObj.node.length; idx++){
                var node = dbObj.node[idx];
                var nodeIdx = node+'_'+idx;
                var ip = node.substring(0, node.indexOf(':'));
                var model = dbObj.model;
                
                var ec = [];
                var key;
                for(key in model.os){
                	ec.push({endpoint: prefix + ip, counter: key});
                }
                for(key in model.proc){
                	ec.push({endpoint: prefix + node, counter: key});
                }
                if(model.schema){
                    for(key in model.schema){
                    	ec.push({endpoint: prefix + node, counter: key});
                    }
                }
                reqs[reqs.length] = {
                	start: start,
                	end: end,
                	cf: 'AVERAGE',
                	endpoint_counters: ec
                };
                models[models.length]={model:model,nodeIdx:nodeIdx};
            }
            
            if (reqs.length>0) {
            	this.accessDbInstData(reqs, 0,models);
            }
        },
        accessDbInstData:function(reqs,index,models) {
        	
        	if (index>reqs.length-1) {
        		return;
        	}
        	
        	AjaxHelper.call({
                url: "../../rs/godeye/monitor/q/dba",
                data: JSON.stringify(reqs[index]),
                async: true,
                cache: false,
                type: "post",
                dataType: "html",
                success: function (data) {
                	// {"message": "endpoint convert error", "code": 20000}
                    var ret = JSON.parse(data);
                    if(ret.code){ // dba server error...
                    	if(ret.code == 20002){
                    		alert("该数据源性能数据不存在。\n注：数据源性能数据由系统部DBA提供，如有疑问请联系dba.list@creditease.cn");
                    	}
                    	app.controller.accessDbInstData(reqs,index+1,models);
                    	return;
                    }
                    var j, k, key;
                    j = 0;

                    var model=models[index]["model"];
                    var nodeIdx=models[index]["nodeIdx"];
                    for (var type in model) {
                    	var tm=model[type];
                    	for(key in tm){
                            var val = "";
                            var o = ret[j++];
                            if(o.Values){
                                val = o.Values[o.Values.length - 1].value;
                            }
                            if(val!=undefined&&val!="") {
                            	HtmlHelper.id(nodeIdx + '_'+type+'_' + key).innerHTML = val;
                            }
                        }
                    }
                    
                    app.controller.accessDbInstData(reqs,index+1,models);
                },
                error: function (data) {
                    alert("访问数据源性能数据服务失败！请检查网络连接是否正常。");
                }
            });
        },
        
        loadDbSlowOptions: function(id, node, dbname, type, schema){
        	app.controller.openClose(id);
        	var el = HtmlHelper.id(id);
        	if(!el.innerHTML){
        		var req = {
            		ip: node.substring(0, node.indexOf(':')),
            		port: parseInt(node.substring(node.indexOf(':') + 1)),
            		dbname: dbname,
            		type: type,
            		schema: [schema]
            	};
            	AjaxHelper.call({
            		url: "../../rs/godeye/monitor/q/slowsql",
                    data: JSON.stringify(req),
                    async: true,
                    cache: false,
                    type: "post",
                    dataType: "html",
                    success: function (data) {
                    	var ret = JSON.parse(data);
                    	if(ret.code){ // dba server error...
                    		app.controller.openClose(id);
                    		console.log(data);
                    		alert("访问数据源性能数据服务失败！\n注：数据源性能数据由系统部DBA提供，如有疑问请联系dba.list@creditease.cn");
                        	return;
                        }
                    	var html = '<div class="kv">';
                		for(var i = 0; i < ret.length; i++){
                			var dbtime = new Date(ret[i].time * 1000).toLocaleString();
                		    html += '<div class="kvField"><span class="kvSubField" styel="display:inline-block;width:130px;">' + ret[i].schema + '</span><span>：</span>&nbsp;&nbsp;'
                		    html += '慢SQL数量：<span class="kvSubValue">' + ret[i].count + '</span>,&nbsp;&nbsp;数据库时间：<span class="kvSubValue">' + dbtime + '</span></div>';
                		}
                		html += '</div>'
                    	var el = HtmlHelper.id(id);
                    	el.innerHTML = html;
                    },
                    error: function (data) {
                        alert("访问数据源性能数据服务失败！请检查网络连接是否正常。");
                    }
            	});
        	}
        	
        },
        /**
         *  dba datasource model template
         **/
        getDbInstModel : function(type, schemaName){
            var model = {
                oracle: {
                    os: {
                        'cpu.idle': 'cpu.idle',
                        'cpu.busy': 'cpu.busy',
                        'mem.memtotal': 'mem.memtotal',
                        'mem.memused': 'mem.memused'
                    },
                    proc: {
                        'cpu.idle': 'cpu.idle',
                        'cpu.busy': 'cpu.busy',
                        'mem.memtotal': 'mem.memtotal',
                        'mem.memused': 'mem.memused',
                        'DB.Sess.total': 'conn.total',
                        'DB.Sess_active.total': 'conn.active'
                    },
                    schema: {
                        // 
                    }
                },
                mysql: {
                    os: {
                        'cpu.idle': 'cpu.idle',
                        'cpu.busy': 'cpu.busy',
                        'mem.memtotal': 'mem.memtotal',
                        'mem.memused': 'mem.memused'
                    },
                    proc: {
                        'cpu.idle': 'cpu.idle',
                        'MY.mysqld.cpu.usedper': 'cpu.busy',
                        'mem.memtotal': 'mem.memtotal',
                        'MY.mysqld.mem.usedper': 'mem.memused',
                        'DB.Sess.total': 'conn.total',
                        'DB.Sess_active.total': 'conn.active'
                    },
                    schema: {
                        // 
                    }
                },
                redis: {
                    os: {
                        'cpu.idle': 'cpu.idle',
                        'cpu.busy': 'cpu.busy',
                        'mem.memtotal': 'mem.memtotal',
                        'mem.memused': 'mem.memused'
                    },
                    proc: {
                        'cpu.idle': 'cpu.idle',
                        'cpu.busy': 'cpu.busy',
                        'redis.used_memory_rss': 'redis.used_memory_rss',
                        'redis.connected_clients': 'redis.connected_clients'
                    }
                },
                mongo: {
                    os: {
                        'cpu.idle': 'cpu.idle',
                        'cpu.busy': 'cpu.busy',
                        'mem.memused': 'mem.memused',
                        'mem.memused': 'mem.memtotal'
                    },
                    proc: {
                        'cpu.idle': 'cpu.idle',
                        'cpu.busy': 'cpu.busy',
                        'connections_current': 'connections_current'
                    }
                }
            }
            
            model.oracle.schema['DB.sess.schema/schema='+schemaName] = 'conn.total';
            model.oracle.schema['DB.sess_active.schema/schema='+schemaName] = 'conn.active';
            
            model.mysql.schema['MY.sess.schema/schema='+schemaName] = 'conn.total';
            model.mysql.schema['MY.sess_active.schema/schema='+schemaName] = 'conn.active';
            
            return model[type];
        },
        
        /**
         * TODO 
         * Java Thread Analysis 
         */
        // -------------------- thread analysis list window --------------------
        buildAppJTAListWnd: function(sObj) {
            return appJTA.buildAppJTAListWnd(sObj);
        },
        runAppJTAListWnd: function(sObj) {
            appJTA.runAppJTAListWnd(sObj);
        },
        destroyAppJTAListWnd: function() {
            // ignore
        },
        // -------------------- thread analysis detail window --------------------
        buildAppJTADetailWnd: function(sObj) {
            return appJTA.buildAppJTADetailWnd(sObj);
        },
        runAppJTADetailWnd: function(sObj) {
            appJTA.runAppJTADetailWnd(sObj);
        },
        destroyAppJTADetailWnd: function() {
            // ignore
        },
        // -------------------- thread analysis multi dump window --------------------
        buildAppJTAMultiDumpWnd: function(sObj) {
            return appJTA.buildJTAMultiDumpWnd(sObj);
        },
        runAppJTAMultiDumpWnd: function(sObj) {
            appJTA.runJTAMultiDumpWnd(sObj);
        },
        destroyAppJTAMultiDumpWnd: function() {
            // ignore
        },
        // -------------------- thread analysis graph window --------------------
        buildAppJTAGraphWnd: function(sObj) {
            return appJTA.buildAppJTAGraphWnd(sObj);
        },
        runAppJTAGraphWnd: function(sObj) {
            appJTA.runAppJTAGraphWnd(sObj);
        },
        destroyAppJTAGraphWnd: function(sObj) {
            // ignore
        },
        // -------------------- thread analysis graph window --------------------
        buildAppJTAMsgWnd: function(sObj) {
        	return appJTA.buildJTAMsgWnd(sObj);
        },
        runAppJTAMsgWnd: function(sObj) {
        	appJTA.runJTAMsgWnd(sObj);
        },
        destroyAppJTAMsgWnd: function() {
        	// ignore
        }
	}	
};

var app = window["appmvc"].build(mvcObj);
var appStream=new AppServiceStream(app);
var appAPM=new APMTool(app);
var appLog=new NewLogTool(app);
var appDBPool=new DBPoolTool(app);
var appJTA = new JTATool(app);

$(document).ready(function(){
	app.run();
});