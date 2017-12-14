/**
 * TODO  指标数据
 * 顺序和显示有关
 */
IndexData = {
	jee : {
		"jeeRTC" : {
			"tmax" : "最长响应时间",
			"tmin" : "最短响应时间",
			"tavg" : "平均响应时间",
			"tsum" : "响应时间总和"
		},
		"jeeQPS" : {
			"count" : "总计数",
			"err" : "错误计数",
			"warn" : "警告计数",
			"RC" : "响应码"// RC200\302\500 等，由解析自动赋值 ：必须值有
		}
	},
	jse : {
		"jvmCpu" : {
			"cpu_s" : "系统占用率",
			"cpu_p" : "进程占用率"
		},
		"jvmThread" : {
			"thread_live" : "活跃线程计数",
			"thread_daemon" : "守护线程计数",
			"thread_peak" : "线程峰值计数",
			"thread_started" : "线程启动计数"
		},
		"jvmClass" : {
			"class_load" : "类加载计数",
			"class_unload" : "类卸载计数",
			"class_total" : "全部类计数"
		},
		"jvmGc" : {
			"mgc_count" : "MinorGC计数",
			"mgc_time" : "MinorGC时间",
			"fgc_count" : "FullGC计数",
			"fgc_time" : "FullGC时间"
		},
		"jvmHeap" : {
			"heap_use" : "已使用堆",
			"heap_init" : "初始化堆",
			"heap_commit" : "已提交堆",
			"heap_max" : "最大堆"
		},
		"jvmNoHeap" : {
			"noheap_use" : "已使用非堆",
			"noheap_init" : "初始化非堆",
			"noheap_commit" : "已提交非堆",
			"noheap_max" : "最大非堆"
		},
		"jvmPerm" : {
			"perm_use" : "已使用持久代",
			"perm_init" : "初始化持久代",
			"perm_commit" : "已提交持久代",
			"perm_max" : "最大持久代"
		},
		"jvmCode" : {
			"code_use" : "已使用代码缓存",
			"code_init" : "初始化代码缓存",
			"code_commit" : "已提交代码缓存",
			"code_max" : "最大代码缓存"
		},
		"jvmEden" : {
			"eden_use" : "Eden已使用",
			"eden_init" : "初始化Eden",
			"eden_commit" : "已提交Eden",
			"eden_max" : "最大Eden"
		},
		"jvmSurv" : {
			"surv_use" : "Surv已使用",
			"surv_init" : "初始化Surv",
			"surv_commit" : "已提交Surv",
			"surv_max" : "最大Surv"
		},
		"jvmOld" : {
			"old_use" : "老生代",
			"old_init" : "初始化老生代",
			"old_commit" : "已提交老生代",
			"old_max" : "最大老生代"
		}
	},
	
	host : {
		"hostIoDisk" : {
			"io.disk" :"磁盘使用"
		},
		"hostSys" : {
			"conn.cur" :"系统服务连接数",
			"cpu.avgload" : "系统cpu平均占用百分比",
			"cpu.freemem" :"系统空闲内存",
			"cpu.load" :"系统cpu占用百分比",
		    "cpu.maxmem" :"系统最大内存"
		},				
	},
	
	
	proc : {
		"procConn" : {
			"conn" :"进程连接数",
			"conn_port" :"进程端口连接数"
				
		},
		"procCpuMem" : {
			"mem" : "进程占用内存",
			"memRate" :"进程内存占用百分比",
			"cpu" :"进程占用cpu百分比",
		},
		"procIo" : {
			"in" :"进程入口流量",
			"out" :"进程出口流量",
			"in_port" :"进程端口入口流量",
			"out_port" :"进程端口出口流量",
		},	
		"procDisk" : {
			"disk_read" :"进程读磁盘速度",
			"disk_write" :"进程写磁盘速度",
		},
	},
	
	groupDesc : {
		"jeeRTC" : "响应时间(ms)",
		"jeeQPS" : "访问计数",
		"jvmCpu" : "Cpu(%)",
		"jvmThread" : "线程计数",
		"jvmClass" : "Class计数",
		"jvmGc" : "Gc计数",
		"jvmHeap" : "Heap-堆使用(MB)",
		"jvmNoHeap" : "非Heap-非堆使用(MB)",
		"jvmPerm" : "Perm-持久代使用(MB)",
		"jvmCode" : "Code-代码缓存使用(MB)",
		"jvmEden" : "Eden使用(MB)",
		"jvmSurv" : "Surv使用(MB)",
		"jvmOld" : "Old-老生代使用(MB)",
		"hostIoDisk" : "容器磁盘信息" , 
		"hostSys":"容器系统信息" ,
		"procConn" : "进程连接数", 
		"procCpuMem" : "进程CPU与内存", 
		"procIo" : "进程出入口流量", 
		"procDisk" : "进程读写磁盘速度" 
	}
};
/**
 * TODO body初始化
 */
function initBody(){
	var div = new StringBuffer();
	// 时间控件初始化
	div.append("<div class=\"modal fade\" id=\"userTimeSelDiv\" aria-hidden=\"true\">");
	div.append("<div class=\"modal-dialog\">");
	div.append("<div class=\"modal-content\">");
	div.append("<div class=\"modal-header\">");
	div.append("<h5>时间范围</h5>");
	div.append("</div>");
	div.append("<div class=\"modal-body\">");
	div.append("<!--  编辑区域 BEGIN -->");

	div.append('<div>');
	div.append('<span>时间单位：</span>');
	div.append('<div class="btn-group radio" data-toggle="buttons">');
	div.append('<label class="btn btn-primary" onclick="javascript:initTimeControl(2);">');
	div.append('<input type="radio" name="options" id="optionsDay" value="day" /> 日');
	div.append('</label>');
	div.append('<label class="btn btn-primary" onclick="javascript:initTimeControl(1);">');
	div.append('<input type="radio" name="options" id="optionsHour" value="hour" /> 时');
	div.append('</label>');
	div.append('<label class="btn btn-primary" onclick="javascript:initTimeControl(0);">');
	div.append('<input type="radio" name="options" id="optionsMinute" value="minute" /> 分');
	div.append('</label>');
	div.append('</div>');
	div.append('</div>');
	
	div.append('<div class="control-group">');
	div.append('<div class="controls input-append date form_datetime_start"');
	div.append('data-date-format="yyyy-mm-dd hh:ii:ss">开始时间：');
	div.append('<input size="19" type="text" placeholder="开始时间" readonly id="startTime">');
	div.append('<span class="add-on"><i class="icon-th"></i></span>');
	div.append('</div>');
	div.append('</div>');
	
	div.append('<div class="control-group">');
	div.append('<div class="controls input-append date form_datetime_end"');
	div.append('data-date-format="yyyy-mm-dd hh:ii:ss">结束时间：');
	div.append('<input size="19" type="text" placeholder="结束时间" readonly id="endTime">');
	div.append('<span class="add-on"><i class="icon-th"></i></span>');
	div.append('</div>');
	div.append('</div>');
		
	div.append("<!--  编辑区域 END -->");
	div.append("</div>");
	div.append("<div class=\"modal-footer\">");
	div.append("<span class=\"errMsg\" id=\"msg\"></span>");
	div.append("<button class=\"btn btn-primary\" onclick=\"javascript:PageClass.ajaxGMonitor();\" >查看</button>");
	div.append("<button class=\"btn\" data-dismiss=\"modal\">返回</button>");
	div.append("</div>");
	div.append("</div>");
	div.append("</div>");
	div.append("</div>");
	$(document.body).append(div.toString());

};
/**
 * 初始化时间控件
 */
function initTimeControl(_minView){
	//初始化CSS
	$("#msg").html("");
    var times = $("[name='options']");
    $.each(times,function(index,obj){
    	if(obj.value=="minute"){
    		obj.parentNode.className="btn btn-primary active";
    	}else{
    		obj.parentNode.className="btn btn-primary";
    	}
    });
    
	//清空	
	$("#startTime").val("");
	$("#endTime").val("");
	$('.form_datetime_start').datetimepicker('remove');
	$('.form_datetime_end').datetimepicker('remove');
	/**
	 * 时间控件格式化设置
	 * _minView(0-2:分、时、天)
	 */
	var _format;
	if(_minView==0){
		_format = "yyyy-mm-dd hh:ii";
	}else if(_minView==1){
		_format = "yyyy-mm-dd hh";
	}else if(_minView==2){
		_format = "yyyy-mm-dd";
	}else{
		_format = "yyyy-mm-dd hh:ii:ss";
	}

	$('.form_datetime_start').datetimepicker({
		format:_format,
		minView:_minView,
		language : 'zh-CN',
		autoclose : true,
		minuteStep : 1, // 分钟间隔设置
		todayBtn : true
//		endDate:new Date()
	});
	
	$('.form_datetime_end').datetimepicker({
		format:_format,
		minView:_minView,
		language : 'zh-CN',
		autoclose : true,
		minuteStep : 1,
		todayBtn : true
//		endDate:new Date()
	});
	initTimeInputValue(_minView);
	//时间控件操作，附带用户体验效果
	$(".form_datetime_start").on('show', function(ev){
		$('.form_datetime_start').datetimepicker('setEndDate', new Date());
		$('.form_datetime_end').datetimepicker('setEndDate', new Date());
   		$(".modal-body").slideUp();
   		$(".modal-footer").slideUp();
	});
	$(".form_datetime_start").on('hide', function(ev){
		$(".modal-body").slideDown();
		$(".modal-footer").slideDown();
	});
	$(".form_datetime_end").on('show', function(ev){
		$('.form_datetime_start').datetimepicker('setEndDate', new Date());
		$('.form_datetime_end').datetimepicker('setEndDate', new Date());
   		$(".modal-body").slideUp();
   		$(".modal-footer").slideUp();
	});
	$(".form_datetime_end").on('hide', function(ev){
		$(".modal-body").slideDown();
		$(".modal-footer").slideDown();
	});
};
function initTimeInputValue(type){

	var sTime,eTime = new Date();
	if(type==0){
		sTime = new Date(new Date().setMinutes(eTime.getMinutes()-2));
	}else if(type==1){
		sTime = new Date(new Date().setHours(eTime.getHours()-2));
	}else if(type==2){
		sTime = new Date(new Date().setDate(eTime.getDate()-2));
	}else{
		sTime = eTime;
	}
	
	$('.form_datetime_start').datetimepicker('update', sTime);
	$('.form_datetime_end').datetimepicker('update', eTime);
}
/**
 * TODO 窗体初始化,必须在body后面加载,因为窗口容器会追加到body
 */
window.winmgr.build({
	id : "winList",
	height : "auto",
	"overflow-y" : "auto",
	order : 998,
	theme : "win"
});
window.winmgr.build({
	id : "winRow",
	height : "auto",
	"overflow-y" : "auto",
	order : 998,
	theme : "win"
});
window.winmgr.build({
	id : "winchart",
	height : "auto",
	"overflow-y" : "auto",
	order : 1999,
	theme : "win"
});
var modalConfig = {
		head:"提示",
		content:"请选择指标"
};
/**
 * TODO 图表配置
 */
var maxThreshold=60*10;//最大点数，超过点数则报错不显示   ，60点（如:查询粒度为分钟，则为1小时数据）
var addScrollTop=10; //滚动条添加点数
var JeeRTC_ChartCfg={		
		id:"JeeRTC_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jeeRTC"],
		indexType:"jeeRTC",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		indexData:IndexData.jee["jeeRTC"]
//	    , //动态配置，后续的都如此，见showChartByFilterMontiorData
	};

var JeeQPS_ChartCfg={
		id:"JeeQPS_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jeeQPS"],
		indexType:"jeeQPS",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jee["jeeQPS"]
	};
var JseThread_ChartCfg={
		id:"JseThread_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jvmThread"],
		indexType:"jvmThread",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmThread"]
	};

var JseClass_ChartCfg={
		id:"JseClass_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jvmClass"],
		indexType:"jvmClass",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmClass"]
	};

var JsePerm_ChartCfg={
		id:"JsePerm_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jvmPerm"],
		indexType:"jvmPerm",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmPerm"]
	};

var JseCode_ChartCfg={
		id:"jvmJseCode_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jvmCode"],
		indexType:"jvmCode",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmCode"]
	};

var JseHeap_ChartCfg={
		id:"JseHeap_ChartCfg",
		type:"stock",
		cid:"jvmJse_ChartCID",
		title:IndexData.groupDesc["jvmHeap"],
		indexType:"jvmHeap",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmHeap"]
	};


var JseNoHeap_ChartCfg={
		id:"JseNoHeap_ChartCfg",
		type:"stock",
		cid:"jvmJse_ChartCID",
		title:IndexData.groupDesc["jvmNoHeap"],
		indexType:"jvmNoHeap",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmNoHeap"]
	};

var JseOld_ChartCfg={
		id:"JseOld_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jvmOld"],
		indexType:"jvmOld",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmOld"]
	};

var JseSurv_ChartCfg={
		id:"JseSurv_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jvmSurv"],
		indexType:"jvmSurv",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmSurv"]
	};

var JseEden_ChartCfg={
		id:"JseEden_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jvmEden"],
		indexType:"jvmEden",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmEden"]
	};

var JseCpu_ChartCfg={
		id:"JseCpu_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jvmCpu"],
		indexType:"jvmCpu",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmCpu"]
	};


var JseGc_ChartCfg={
		id:"JseGc_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["jvmGc"],
		indexType:"jvmGc",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.jse["jvmGc"]
	};
var Appmetrics_ChartCfg={		
		id:"Appmetrics_ChartCfg",
		type:"stock",
		cid:"",
		title:"自定义指标", 
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		//indexData:{"SERVICE_SMS_SEND_WILL_REAL_NUM":"title"} 使用者赋值
//	    , //动态配置，后续的都如此，见showChartByFilterMontiorData
	};

//容器指标
var hostSys_ChartCfg={
		id:"hostSys_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["hostSys"],
		indexType:"hostSys",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.host["hostSys"]
	};

var hostIoDisk_ChartCfg={
		id:"hostIoDisk_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["hostIoDisk"],
		indexType:"hostIoDisk",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.host["hostIoDisk"]
	};

//进程指标
var procConn_ChartCfg={
		id:"procConn_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["procConn"],
		indexType:"procConn",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.proc["procConn"]
	};
var procCpuMem_ChartCfg={
		id:"procCpuMem_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["procCpuMem"],
		indexType:"procCpuMem",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.proc["procCpuMem"]
	};
var procIo_ChartCfg={
		id:"procIo_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["procIo"],
		indexType:"procIo",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.proc["procIo"]
	};
var procDisk_ChartCfg={
		id:"procDisk_ChartCfg",
		type:"stock",
		cid:"",
		title:IndexData.groupDesc["procDisk"],
		indexType:"procDisk",
		titleAlign:"left",
		width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
		height:300,
		turbothreshold:maxThreshold, 
		indexData:IndexData.proc["procDisk"]
	};

/**
 * 列表配置
 */
var listConfig = {
	id : "list",
	pid : "winList",
	openDelete : false,
	key : "appinstid",
	pagerSwitchThreshold : 600,
	pagesize : 50,
	head : {
		number : [ '序号', '10%' ],
		appname : ['应用名称', '30%'],
		//ip : ['IP', '10%'],
		appurl : [ '应用URI', '50%' ],
		appgroup : [ '应用组', '20%' ],
		appinstid:['','0%']
	},
	cloHideStrategy : {
		1100 : [ 0, 1, 2],
		800 :  [ 0, 1, 2],
		600 :  [ 0, 1, 2],
		500:   [1,2]
	}
};
var listObj= new AppHubTable(listConfig);
var profileData,montiorData,urlMoData,nodeData,procInstid;
/**
 * TODO 当前功能操作类
 */
var PageClass = {
	ajaxGProfile:function() {
		AjaxHelper.call({
			url : "../../rs/godeye/profile/q/cache",
			async : true,
			cache : false,
			type : "GET",
			dataType : "html",
			success : function(result) {
				if(result){
					profileData = eval("(" + result + ")");
					profileData = eval("(" + profileData["rs"] + ")");
					PageClass.loadListData(false,false);
				}else{
					console.log("result is empty");
				}
			},
			error : function(result) {
				console.log(result);
			}
		});
	},
	ajaxGMonitor:function(){
		var param = this.getMontiorReq();
		if(param){
			$(".modal-footer").hide();
			console.log("ajaxGMonitor request->\r\n",param);
			AjaxHelper.call({
				url : "../../rs/godeye/monitor/q/hm/db",
				data : param,
				async : true,
				cache : false,
				type : "POST",
				dataType : "html",
				success : function(result) {
					console.log("ajaxGMonitor response->\r\n",result);
					loadChartDiv();
					loadChartData(result);
					/**
					 * default show
					 */
					loadAppChartData();
					loadJvmChartData();
					loadServiceChartData();
					
					loadHostChartData();
					loadProcChartData();
					loadAppmetricsChartData();
					/*
					 * 服务端组件ULR 、 客户端组件URL 不在这显示，由用户点击触发
					 */
					
				},
				error : function(result) {
					console.log(result);
				}
			});
		}
	},
  
	ajaxGNodeInfoByIp:function(appUrl) {
		var ip = getIpByAppurl(appUrl);
		console.log("ajaxGMonitor request->\r\n",ip);
		AjaxHelper.call({
			url : "../../rs/godeye/node/q/cache",
			data : {"fkey" : "ip","fvalue" : ip},
			async : true,
			cache : false,
			type : "GET",
			dataType : "html",
			success : function(result) {
				if(result){
					console.log("ajaxGNodeInfoByIp response->\r\n",result);
					nodeData = eval("(" + result + ")");
					nodeData = eval("(" + nodeData["rs"] + ")");
					
					loadRowDiv(appUrl);
					
				}else{
					console.log("result is empty");
				}
			},
			error : function(result) {
				console.log(result);
			}
		  });
},
  
	searchbtnLoadListData:function(inputReset,pageReset) {
		//用户输入值保存，只有按下查询按钮才会有效。因此用一个隐藏input保存有效值
		var inputValue = $("#inputValue").val();
		 $("#inputValueHidden").val(inputValue);
		PageClass.loadListData(inputReset,pageReset);
	},
	loadListData:function(inputReset,pageReset) {

		var inputValue;
		if (inputReset) { 
			inputValue = "";
			$("#inputValueHidden").val("");
		}else{
			inputValue = $("#inputValueHidden").val();
		}

		$("#inputValue").val(inputValue);
		
		if(pageReset){
			listObj.setPageNum(1);
		}

		// 数据组装
		var datas = new Array(), number = 0;
		for ( var key in profileData) {
			var dataObj = eval("("+profileData[key]+")");
			var appnameValue = dataObj["appname"];
			var ipValue = dataObj["ip"];
			
			appnameValue = appnameValue?appnameValue:"";
			ipValue = ipValue?ipValue:"";
			
			/*
			 * 检索匹配(模糊、不区分大小写)
			 */
			if (inputValue &&
					key.toUpperCase().indexOf(inputValue.toUpperCase()) == -1 &&
					appnameValue.toUpperCase().indexOf(inputValue.toUpperCase()) == -1 &&
					ipValue.toUpperCase().indexOf(inputValue.toUpperCase()) == -1 
					) {
				continue;
			}
			
			var appInfo=key.split("@");
			
			datas[number++] = {
				number : number,
				appname : appnameValue,
				//ip :  ipValue,
				appurl : appInfo[1],
				appgroup: appInfo[0],
				appinstid: key
			};
		}

		// 结果分页
		var getPagingInfo = listObj.getPagingInfo();
		var pageNum = getPagingInfo.pageNum;
		pageNum--; // 默认0开始
		var pageSize = getPagingInfo.pageSize
		var pageBegin = pageNum * pageSize, pageEnd = pageBegin + pageSize, result = new Array();
		number = 0;
		$.each(datas, function(index, obj) {
			if (pageBegin <= index && index < pageEnd) {
				result[number++] = obj;
			}
		});

		// 必须先显示分页
		listObj.clearTable();
		listObj.setTotalRow(datas.length);
		listObj.renderPagination();
		listObj.addRows(result);
	},
	
	

	getMontiorReq:function(){
		/**
		 * 时间 计算/格式化 
		 */
		$("#msg").html("");
		var startTime = $("#startTime").val();
		var endTime = $("#endTime").val();
		if(!startTime || !endTime){
			$("#msg").html("请选择时间范围");
			return null;
		}
		
		//默认填充
		var dayLength = "yyyy-mm-dd".length,hourLength = "yyyy-mm-dd hh".length,minuteLength = "yyyy-mm-dd hh:ii".length;
		var suffix;
		if(startTime.length==dayLength){
			suffix = " 00:00:00:000";
		}else if(startTime.length==hourLength){
			suffix = ":00:00:000";
		}else if(startTime.length==minuteLength){
			suffix = ":00:000";
		}
		startTime += suffix;
		endTime += suffix;

		var sTimeLong = new Date(this.getDateForStringDate(startTime)).getTime();
		var eTimeLong = new Date(this.getDateForStringDate(endTime)).getTime();

		if (sTimeLong >= eTimeLong) {
			$("#msg").html("开始时间必须小于结束时间");
			return null;
		}

		var times = $("[name='options']"), timeCompany;
		$.each(times, function(index, obj) {
			if (obj.parentNode.className.indexOf("active") != -1) {
				timeCompany = obj.value;
				return;
			}
		});
		
		var diffTime = eTimeLong - sTimeLong ;
		if(  
			(timeCompany =="minute" && (diffTime/(1000*60)) > maxThreshold) ||
			(timeCompany =="hour" && (diffTime/(1000*60*60)) > maxThreshold) ||  
			(timeCompany =="day" && (diffTime/(1000*60*60*24)) > maxThreshold)    
			){
			
			$("#msg").html("页面无法渲染,最大点数："+maxThreshold);
			return null;
		}
		/**
		 * 指标
		 */
		var queries;
		var ip=$("#ipInput").val();
		var svrid=$("#svridInput").val();
		var appurl=$("#appurlInput").val();
		var appid=$("#appidInput").val();
		var isJse = appurl.indexOf("http:") == -1?true:false;
		
		//opentsdb 语法
		var sample;
		switch(timeCompany){
			case "minute": sample ="1m-avg";break;
			case "hour": sample ="1h-avg";break;
			case "day": sample ="1d-avg";break;
		}
		
		if(isJse){
			//JVM 选择指标获取
			var tag = {
					"ip":this.encodeForOpenTSDB(ip),
					"pgid":this.encodeForOpenTSDB(svrid),
					"instid":this.encodeForOpenTSDB(appurl)
			}
			var data = {
					 "aggregator":"avg",
					 "downsample":sample,
					 "metric":"jvm.",
					 "tags":tag
			};
			
			queries = this.appendSelectInfo("appServerJvm",data,queries);
			
			//容器 选择指标获取
			var hostTag = {
					"instid":this.encodeForOpenTSDB(ip)
					
			}
			var hostData = {
					 "aggregator":"avg",
					 "downsample":sample,
					 "metric":"hostState.os.",
					 "tags":hostTag
			};
			
			queries = this.appendSelectInfo("HostEntity",hostData,queries);

			//进程 选择指标获取
			var procTag = {
				"instid":this.encodeForOpenTSDB(procInstid)
			}
			var procData = {
					 "aggregator":"avg",
					 "downsample":sample,
					 "metric":"procState.",
					 "tags":procTag
			};
			
			queries = this.appendSelectInfo("ProcEntity",procData,queries);
			
		}else{
			//应用实例  选择指标获取
			
			var appEJeeId = getAppInstId(appurl,appid);
			var appEJeeTag = {
					"ip":this.encodeForOpenTSDB(ip),
					"pgid":this.encodeForOpenTSDB(svrid),
					"instid":this.encodeForOpenTSDB(appEJeeId)
			}
			var appEJeeData = {
					 "aggregator":"avg",
					 "downsample":sample,
					 "metric":"appResp.",
					 "tags":appEJeeTag
			};
			queries = this.appendSelectInfo("appEntityJee",appEJeeData,queries);

			//应用服务器  选择指标获取
			var appSJeeId = getServerInstId(appurl);
			var appSJeeTag = {
					"ip":this.encodeForOpenTSDB(ip),
					"pgid":this.encodeForOpenTSDB(svrid),
					"instid":this.encodeForOpenTSDB(appSJeeId)
			}
			var appSJeeData = {
					 "aggregator":"avg",
					 "downsample":sample,
					 "metric":"serverResp.",
					 "tags":appSJeeTag
			};
			queries = this.appendSelectInfo("appServerJee",appSJeeData,queries);
			
			var appSJvmTag = {
					"ip":this.encodeForOpenTSDB(ip),
					"pgid":this.encodeForOpenTSDB(svrid),
					"instid":this.encodeForOpenTSDB(appSJeeId)
			}
			var appSJvmData = {
					 "aggregator":"avg",
					 "downsample":sample,
					 "metric":"jvm.",
					 "tags":appSJvmTag
			};
			queries = this.appendSelectInfo("appServerJvm",appSJvmData,queries);
			
			//容器 选择指标获取
			var hostTag = {
					"instid":this.encodeForOpenTSDB(ip)
										
			}
			var hostData = {
					 "aggregator":"avg",
					 "downsample":sample,
					 "metric":"hostState.os.",
					 "tags":hostTag
			};
			
			queries = this.appendSelectInfo("HostEntity",hostData,queries);
			
			//进程选择指标获取
			var procTag = {
					"instid":this.encodeForOpenTSDB(procInstid)
					
			}
			var procData = {
					 "aggregator":"avg",
					 "downsample":sample,
					 "metric":"procState.",
					 "tags":procTag
			};
			
			queries = this.appendSelectInfo("ProcEntity",procData,queries);	
				
			/**
			 * 组件URL指标
			 */
			var servicedivs =  $("#serviceContMainDiv").find(".titleDiv3");
			$.each(servicedivs,function(index,obj){ //选中组件URL
				if(obj.className.indexOf("userSelect") >=0){
					var instid=PageClass.encodeForOpenTSDB(obj.innerHTML);
					var appUrlTag = {
							"ip":PageClass.encodeForOpenTSDB(ip),
							"pgid":PageClass.encodeForOpenTSDB(svrid),
							"instid":instid
					}					
					
					var appUrlData = {
							 "aggregator":"avg",
							 "downsample":sample,
							 "metric":"urlResp.",
							 "tags":appUrlTag
					};

					//spec code for pathparam ,use filter to query				
					var reg=new RegExp("\\{[^/]+\\}","g");
					
					//if url contains "{xxx}" 
					if(reg.test(instid)){
						
						//bulid a regexp for query 
						var instRegexp=instid.replace(reg,"[^/]+");
						instRegexp+="$";
						
						var appUrlFilter = {
								 "type":"regexp",
							  	 "tagk":"instid",
	                  			 "filter":instRegexp,
	                  			 "groupBy":false
						}
						
						appUrlData["filters"]=[appUrlFilter];
					}										
					//pathparam spec code end 
					
					queries = PageClass.appendSelectInfo("servicesJee",appUrlData,queries);
					
				}
			});
		
		}
			
		/**
		 * 客户端URL指标
		 */
		var servicedivs =  $("#clientContMainDiv").find(".titleDiv3");
		$.each(servicedivs,function(index,obj){ //选中组件URL
			if(obj.className.indexOf("userSelect") >=0){
								
				var instid = getClienttId(appurl,appid,ip,obj.innerHTML);
				var appUrlTag = {
						"ip":PageClass.encodeForOpenTSDB(ip),
						"pgid":PageClass.encodeForOpenTSDB(svrid),
						"instid":PageClass.encodeForOpenTSDB(instid)
				}
				var appUrlData = {
						 "aggregator":"avg",
						 "downsample":sample,
						 "metric":"clientResp.",
						 "tags":appUrlTag
				};
				

				queries = PageClass.appendSelectInfo("clientJee",appUrlData,queries);
				
			}
		});
		
		/**
		 * 自定义指标 就是JVM指标
		 */
		var appMetrics = $("#appmetrics").find("div");

		//ID format
		var appmetricsInstid = appurl;
		if(appmetricsInstid.indexOf(appid) >= 0){
			appmetricsInstid = appmetricsInstid.substring(0,appmetricsInstid.indexOf(appid)-1);
		}
		
		$.each(appMetrics,function(index,obj){
			if(obj.className.indexOf("userSelect") >=0){
				var appSJeeTag = {
						"ip":PageClass.encodeForOpenTSDB(ip),
						"pgid":PageClass.encodeForOpenTSDB(svrid),
						"instid":PageClass.encodeForOpenTSDB(appmetricsInstid)
				}
				var appMetrics = {
						 "aggregator":"avg",
						 "downsample":sample,
						 "metric":"jvm."+obj.childNodes[2].innerText,
						 "tags":appSJeeTag
				};
				queries.push(appMetrics);
			}
		});
		
		/**
		 * 数据封装 opentsdb 语法
		 */
		var request ={ 
	    		"start":sTimeLong,
	    		"end":eTimeLong,
	    		"queries": queries
	    	}
		

		var maxQueries = 100;
		if (queries.length > maxQueries) {
			$("#msg").html("页面无法渲染,最多指标选项：" + maxQueries);
			return null;
		}
		return JSON.stringify(request);
	},
	/**
	 * 追加选中指标信息
	 * @param tsDivId       ：指标所在div id
	 * @param dataObj       ：opentsdb语法数据
	 * @param resultArray   ：追加对象
	 * return resultArray   :结果
	 */
	appendSelectInfo:function(tsDivId,dataObj,resultArray){
		if(!resultArray){
			resultArray = new Array();
		}
		
		var objs = $("#"+tsDivId).find("div");
		$.each(objs,function(num,obj){//选择指标遍历
			if(obj.className.indexOf("userSelect") >=0){
				
				//被选中指标，数据封装
				var newData = new Object();
				newData = JsonHelper.clone(dataObj);
				var metric=obj.childNodes[2].innerText;
				newData["metric"]+=metric;				
				if(newData["metric"].indexOf("RC")>-1
						||newData["metric"].indexOf("io.disk")>-1
						||metric.indexOf("conn_port")>-1
						||metric.indexOf("in_port")>-1||metric.indexOf("out_port")>-1) {
					newData.tags["ptag"]="*";
				}
				
				if(metric!="tmax"&&metric!="tmin"&&metric!="tavg"){
					newData["aggregator"]="sum";
				}
				resultArray.push(newData);
			}
		});
		return resultArray;
	},
	encodeForOpenTSDB:function(s) { 
		if(s==undefined) {
			return;
		}
		// DataStoreHelper.class
    	s=s.replace(/:/g,"/u003a");
    	s=s.replace(/#/g,"/u0023");
    	s=s.replace(/#/g,"/u0023");
    	s=s.replace(/\&amp;/g, "/u0026");
    	s=s.replace(/\?/g, "/u003f");
    	s=s.replace(/=/g, "/u003d");
    	s=s.replace(/,/g, "/u002c");
    	s=s.replace(/\+/g, "/u002B");
    	s=s.replace(/%/g, "/u0025");
    	s=s.replace(/@/g, "/u0040");
    	s=s.replace(/!/g, "/u0021");
    	s=s.replace(/\|/g, "/u007c");
    	return s;
    },
    encodeForJqueryFormat:function(value){
		//格式化(避免jquery获取不到)
		value = value.replace(/\./g,"\\.");
		value = value.replace(/\//g,"\\/");
		value = value.replace(/\:/g,"\\:");
		value = value.replace(/\,/g,"\\,");
		value = value.replace(/\?/g,"\\?");
		value = value.replace(/\=/g,"\\=");
		value = value.replace(/\&amp;/g,"\\&");
		return value;
    },
    /**
     * 解决 ie，火狐浏览器不兼容new Date(s)
     * @param strDate
     * 返回 date对象
     * add by zyf at 2015年11月5日
     */
    getDateForStringDate:function(strDate){
    	//切割年月日与时分秒称为数组
    	var s = strDate.split(" "); 
    	var s1 = s[0].split("-"); 
    	var s2 = s[1].split(":");
    	if(s2.length==2){
    		s2.push("00");
    	}
    	return new Date(s1[0],s1[1]-1,s1[2],s2[0],s2[1],s2[2]);
    }
    
};

/**
 * TODO 渲染首页列表
 */
function loadListDiv() {

	var div = new StringBuffer();
	div.append("<div id=\"srarchDiv\" class=\"AppHubMVCSearchBar\" >");

	div.append("<input id=\"inputValueHidden\" type=\"hidden\" ></input>");
	div.append("<input id=\"inputValue\" class=\"form-control AppHubMVCSearchBarInputText\"");
	div.append("type=\"text\" placeholder=\"字段模糊检索\" value=\"\"></input>");
	
	div.append("<div class=\"btn-group\">");
	div.append("<button id=\"searchbtn\" type=\"button\" class=\"btn btn-default\" onclick=\"javascript:PageClass.searchbtnLoadListData(false,true);\">");
	div.append("<span class=\"glyphicon glyphicon-search\"></span>");
	div.append("</button>");
	div.append("<button id=\"searchAllbtn\" type=\"button\" class=\"btn btn-default\"  onclick=\"javascript:PageClass.loadListData(true,true);\">");
	div.append("<span class=\"glyphicon glyphicon-th\"></span>");
	div.append("</button>");
	div.append("</div>");
	
	div.append("</div>");
	
	HtmlHelper.id("winList").innerHTML += div.toString();
	showListDiv();
};

/**
 * TODO 渲染选中操作明细
 */
function loadRowDiv(appUrl){

	var obj = eval("("+profileData[appUrl]+")");
	console.log("profile row data->\r\n",obj);
	var isJse = appUrl.indexOf("http")>=0?false:true;
	var div = new StringBuffer();
	div.append( "<div class=\"headDiv\">");

	div.append( "<input type=\"hidden\" value=\""+obj.ip+"\" id=\"ipInput\">");
	div.append( "<input type=\"hidden\" value=\""+obj.svrid+"\" id=\"svridInput\">");
	div.append( "<input type=\"hidden\" value=\""+obj.appurl+"\" id=\"appurlInput\">");
	div.append( "<input type=\"hidden\" value=\""+obj.appid+"\" id=\"appidInput\">");
	
	div.append("<button id=\"searchbtn\" type=\"button\" class=\"btn btn-default\" onclick='javascript:showTimeDiv();' >");
	div.append("<span class=\"glyphicon glyphicon-saved\"></span>");
	div.append("</button>");
	div.append( "<div class=\"icon-signout icon-myout\" onclick=\"javascript:showListDiv()\" \"></div>");
	div.append( "</div>");

	var procIsExistText, ipInstid, ipIsExistText;
	procInstid = getProcInstid(obj["appurl"],obj["ip"]);
	ipInstid = obj["ip"];
	if(procInstid == undefined) {
		procIsExistText = "无匹配的进程信息！"
	}
	else {
		procIsExistText = procInstid;
	}
	if(ipInstid == undefined) {
		ipIsExistText = "无匹配的容器信息！"
	}
	else {
		ipIsExistText = ipInstid;
	}
	
	if(!isJse){
		div.append("<div class=\"titleDiv\" id=\"appEntityDiv\">应用实例：");
		div.append("<span id=\"appEntitySpan\" >"+obj["appurl"]+"</span>");
		div.append("</div>");
		div.append(getJEELabel("appEntityJee"));
		
		div.append("<div class=\"titleDiv\">");
		div.append("应用服务器：");
		div.append("<span id=\"appServerSpan\" >"+obj["svrid"]+"</span>");
		div.append("</div>");
		div.append(getJEELabel("appServerJee"));
		div.append("<hr/>");
		div.append(getJSELabel("appServerJvm"));
		
		//应用容器
		div.append("<div class=\"titleDiv\" id=\"HostEntityDiv\">应用容器：");
		div.append("<span id=\"HostEntitySpan\" >"+ipIsExistText+"</span>");
		div.append("</div>");
		if(ipInstid != undefined) {
			div.append(getHostLabel("HostEntity"));
		}
		else {
			div.append("<div>无匹配的容器信息！</div>");	
		}
		
		//进程		
		div.append("<div class=\"titleDiv\" id=\"ProcEntityDiv\">应用进程：");
		div.append("<span id=\"ProcEntitySpan\" >"+procIsExistText+"</span>");
		div.append("</div>");
		if(procInstid != undefined) {
			div.append(getProcLabel("ProcEntity"));
		}
		
		else {
			div.append("<div>无匹配的进程信息！</div>");
		}
		
	}else{
		div.append("<div class=\"titleDiv\">");
		div.append("JVM：");
		div.append("<span id=\"appServerSpan\" >"+obj["svrid"]+"</span>");
		div.append("</div>");
		div.append(getJSELabel("appServerJvm"));
		
		//容器		
		div.append("<div class=\"titleDiv\" id=\"HostEntityDiv\">应用容器：");
		div.append("<span id=\"HostEntitySpan\" >"+ipIsExistText+"</span>");
		div.append("</div>");	
		if(ipInstid != undefined) {
			div.append(getHostLabel("HostEntity"));
		}
		else {
			div.append("<div>无匹配的容器信息！</div>");	
		}
		
		//进程		
		div.append("<div class=\"titleDiv\" id=\"ProcEntityDiv\">应用进程：");
		div.append("<span id=\"ProcEntitySpan\" >"+procIsExistText+"</span>");
		div.append("</div>");			
		if(procInstid != undefined) {
			div.append(getProcLabel("ProcEntity"));
		}		
		else {
			div.append("<div>无匹配的进程信息！</div>");
		}
		
	}
	
	div.append(getServicesDiv(obj));
	div.append(getClientsDiv(obj));
	div.append(getAppmetrics(obj));
	
	
	HtmlHelper.id("winRow").innerHTML = div.toString();
	showRowDiv(true);
	
	/**
	 * 渲染jee指标
	 */
	function getJEELabel(id,display,Excludes){
		var sb = new StringBuffer();
		
		if(!display){
			display = "block";
		}
		
		sb.append("<div id='"+id+"' class='labelDiv' style='display:"+display+"' >");
		$.each(IndexData.jee,function(name,list){
			$.each(list,function(index,title){
				//排除判断
				if(Excludes){
					var excludesCheck = false;
					$.each(Excludes,function(eIndex,eValue){
						if(index==eValue){
							excludesCheck = true;
							return;
						}
					});
					if(excludesCheck){
						return;
					}
				}
				sb.append("<div class='labelDivIndex' onclick='javascript:labelCssChange(this);'>");

				sb.append("<div class='title'>");
				sb.append(title);
				sb.append("</div>");

				sb.append("<hr/>");
				
				sb.append("<div class='value'>");
				sb.append(index);
				sb.append("</div>");
				
				sb.append("</div>");
			});
		});
		sb.append("</div>");
		
		return sb.toString();
	}
	
	/**
	 * 渲染jvm指标
	 */
	function getJSELabel(id,display){
		var sb = new StringBuffer();

		if(!display){
			display = "block";
		}
	
		sb.append("<div id='"+id+"' class='labelDiv' style='display:"+display+"' >");
		
		$.each(IndexData.jse,function(name,list){
			$.each(list,function(index,title){

				sb.append("<div class='labelDivIndex' onclick='javascript:labelCssChange(this);'>");

				sb.append("<div class='title'>");
				sb.append(title);
				sb.append("</div>");

				sb.append("<hr/>");
				
				sb.append("<div class='value'>");
				sb.append(index);
				sb.append("</div>");
				
				sb.append("</div>");
				
			});
			sb.append("<hr/>");
			
		});
		sb.append("</div>");
		
		return sb.toString();
	}
	
	//渲染应用容器指标
	function getHostLabel(id,display){
		var sb = new StringBuffer();

		if(!display){
			display = "block";
		}
	
		sb.append("<div id='"+id+"' class='labelDiv' style='display:"+display+"' >");
		
		$.each(IndexData.host,function(name,list){
			$.each(list,function(index,title){

				sb.append("<div class='labelDivIndex' onclick='javascript:labelCssChange(this);'>");

				sb.append("<div class='title'>");
				sb.append(title);
				sb.append("</div>");

				sb.append("<hr/>");
				
				sb.append("<div class='value'>");
				sb.append(index);
				sb.append("</div>");
				
				sb.append("</div>");
				
			});
			sb.append("<hr/>");
			
		});
		sb.append("</div>");
		
		return sb.toString();
	}
	
	//渲染进程指标
	function getProcLabel(id,display){
		var sb = new StringBuffer();

		if(!display){
			display = "block";
		}
	
		sb.append("<div id='"+id+"' class='labelDiv' style='display:"+display+"' >");
		
		$.each(IndexData.proc,function(name,list){
			$.each(list,function(index,title){

				sb.append("<div class='labelDivIndex' onclick='javascript:labelCssChange(this);'>");

				sb.append("<div class='title'>");
				sb.append(title);
				sb.append("</div>");

				sb.append("<hr/>");
				
				sb.append("<div class='value'>");
				sb.append(index);
				sb.append("</div>");
				
				sb.append("</div>");
				
			});
			sb.append("<hr/>");
			
		});
		sb.append("</div>");
		
		return sb.toString();
	}
	
	/***
	 * 渲染服务组件
	 * 有可能为空
	 */
	function getServicesDiv(obj){
		var source = obj["cpt.services"];
		//没有组件则不渲染
		if("{}"==source){
			return;
		}
		
		var services = eval("("+source+")");
		var sb = new StringBuffer(); 
		var index = 0;
		sb.append( "<div class=\"titleDiv\">服务组件</div>");
		sb.append(getJEELabel("servicesJee","none"));

		sb.append("<div id='serviceContMainDiv'>");
		//组件循环
		$.each(services,function(name,obj){
			sb.append("<div style=\"margin-bottom:5px;\">");
			//组件标题
			sb.append("<div class=\"titleDiv2\" onclick=\"javascript:setCssChangeById('"+name+"')\"> ["+obj.length+"]"+name+"</div>");
			sb.append("<div id='"+name+"' style='display:none' >");
			//组件内容
			$.each(obj,function(index,url){
				sb.append("<div class=\"titleDiv3\" onclick='javascript:setUrlCssChange(this,\"serviceContMainDiv\",\"servicesJee\")'>"+url+"</div>");
			});
			sb.append("</div>");
			
			sb.append("</div>");
		});

		sb.append("</div>");
		
		return sb.toString();
	}
	
	
	/**
	 * 渲染客户端组件（向后调用的客户端）
	 * 有可能为空
	 */
	function getClientsDiv(obj){
		var source = obj["cpt.clients"];
		//没有则不渲染
		if("[]"==source){
			return;
		}

		var clients = eval("("+source+")");
		var sb = new StringBuffer(); 
		var index = 0;
		sb.append( "<div class=\"titleDiv\">客户端组件</div>");
		sb.append(getJEELabel("clientJee","none",["warn","RC"]));
	
		
		sb.append("<div id='clientContMainDiv'>");
		//循环
		$.each(clients,function(index,obj){
			 
			var url = obj.id;
			var urls = obj.values.urls;
			sb.append("<div style=\"margin-bottom:5px;\">");
			//标题
			sb.append("<div class=\"titleDiv2\" onclick=\"javascript:setCssChangeById('"+url+"')\">"+url+"</div>");
			sb.append("<div id='"+url+"' style='display:none' >");
			
			if(url.indexOf("http:")==-1){
				//非http的客户端，直接使用url
				sb.append("<div class=\"titleDiv3\" onclick='javascript:setUrlCssChange(this,\"clientContMainDiv\",\"clientJee\")'>"+url+"</div>");	
			}else{
				//http urls 拼接处理
				//内容
				$.each(urls,function(uIndex,uObj){
					var showUrl = url + uIndex;
					sb.append("<div class=\"titleDiv3\" onclick='javascript:setUrlCssChange(this,\"clientContMainDiv\",\"clientJee\")'>"+showUrl+"</div>");
				});
			}
			
			sb.append("</div>");
			
			sb.append("</div>");
		});

		sb.append("</div>");
		
		return sb.toString();
	}
	
	/**
	 * 渲染自定义指标
	 */
	function getAppmetrics(obj){
		var source = obj["appmetrics"];
		//没有则不渲染
		if("{}"==source){
			return;
		}
		
		var appmetrics = eval("("+source+")");
		var sb = new StringBuffer(); 
		sb.append( "<div class=\"titleDiv\">自定义指标</div>");
		

		sb.append("<div id='appmetrics' class='labelDiv' style='display:block' >");
		$.each(appmetrics,function(name,obj){
				sb.append("<div class='labelDivIndex' onclick='javascript:labelCssChange(this);'>");

				sb.append("<div class='title'>");
				sb.append("&nbsp;");//目前没有中文解释
				sb.append("</div>");

				sb.append("<hr/>");
				
				sb.append("<div class='value'>");
				sb.append(name);
				sb.append("</div>");
				
				sb.append("</div>");
		});
		sb.append("</div>");
		
		return sb.toString();
	}
	
};
/**
 * TODO 图表渲染
 *
 */
function loadChartDiv(){
	
	var div = new StringBuffer();
	var title = $("#startTime").val()+"&nbsp;至&nbsp;"+$("#endTime").val();
	var appESpan = $("#appEntitySpan").html();
	var appSSpan = $("#appServerSpan").html();
	
	var appHSpan = $("#HostEntitySpan").html();
	var appPSpan = $("#ProcEntitySpan").html();
	
	var appUrl = $("#appurlInput").val();
	var appId = $("#appidInput").val();
	var ip = $("#ipInput").val();
		
	var isJee = appUrl.indexOf("http") == -1?false:true;
	
	var appEjee = $("#appEntityJee").find("div");
	var appSjee = $("#appServerJee").find("div");
	var appSjvm = $("#appServerJvm").find("div");
		
	var appHost = $("#HostEntity").find("div");	
	var appProc = $("#ProcEntity").find("div");	
	
	
	var serviceCont = $("#serviceContMainDiv").find("div");
	var serviceJee = $("#servicesJee").find("div");
	var clientCont = $("#clientContMainDiv").find("div");
	var clientJee = $("#clientJee").find("div");
	var appmetrics = $("#appmetrics").find("div");
	
	div.append( "<div class=\"headDiv\">");
	div.append( "<span id=\"chartTitle\">"+title+"</span>");
	div.append( "<div class=\"icon-signout icon-myout\" onclick=\"javascript:showRowDiv(false)\" \"></div>");
	div.append( "</div>");

	/**
	 * 渲染已经选择的标题 begin
	 */
	//应用实例
	if(checkUserSel(appEjee)){
		div.append("<div class=\"titleDiv titileCursor\">应用实例：");
		div.append("<span>"+appESpan+"</span>");
		div.append("</div>");
		div.append("<div id=\"app_ChartCID_MSG\">没有相关数据</div>");
		div.append("<div id=\"app_ChartCID\"></div>");
		div.append("<hr/>");

	}
	
	//应用服务器\jvm
	if(checkUserSel(appSjee) || checkUserSel(appSjvm)){
		div.append( "<div class=\"titleDiv titileCursor\">");
		div.append(isJee?"应用服务器：":"JVM:");
		div.append("<span>"+appSSpan+"</span>");
		div.append("</div>");
		div.append("<div id=\"server_ChartCID_MSG\">没有相关数据</div>");
		div.append("<div id=\"server_ChartCID\"></div>");
		div.append("<hr/>");
	}
	
	//应用容器
	if(checkUserSel(appHost)){
		div.append("<div class=\"titleDiv titileCursor\">应用容器：");
		div.append("<span>"+appHSpan+"</span>");
		div.append("</div>");
		div.append("<div id=\"host_ChartCID_MSG\">没有相关数据</div>");
		div.append("<div id=\"host_ChartCID\"></div>");
		div.append("<hr/>");

	}
	//进程
	if(checkUserSel(appProc)){
		div.append("<div class=\"titleDiv titileCursor\">应用进程：");
		div.append("<span>"+appPSpan+"</span>");
		div.append("</div>");
		div.append("<div id=\"proc_ChartCID_MSG\">没有相关数据</div>");
		div.append("<div id=\"proc_ChartCID\"></div>");
		div.append("<hr/>");

	}
	
	//组件url
	if ((checkUserSel(serviceCont) && checkUserSel(serviceJee))) {
		div.append("<div class=\"titleDiv titileCursor\">服务组件</div>");

		var servicedivs = $("#serviceContMainDiv").find(".titleDiv2");
		$.each(servicedivs,function(index,server){ 
			
			//选中组件
			if(server.className.indexOf("userSelect") >=0){
				
				var serverValue = server.innerHTML.substring(server.innerHTML.indexOf("]")+1);
				
				div.append("<div onclick=\"javascript:setCssChangeById('"+serverValue+"_');\" class=\""+server.className+"\">"+serverValue+"</div>");
				div.append("<div style='display:none' id='"+serverValue+"_' >");

				//格式化(避免jquery获取不到)
				serverValue = PageClass.encodeForJqueryFormat(serverValue);

				//选中组件下的DIV获取
				var urldivs = $("#"+serverValue).find(".titleDiv3");
				var urlId = 0;
				$.each(urldivs,function(index2,url){ 
					//选中组件URL
					if(url.className.indexOf("userSelect") >=0){
						div.append("<div class=\""+url.className+"\" onclick=\"javascript:loadServiceUrlChartData('"+url.innerHTML+"',"+urlId+");\" >"+url.innerHTML+"</div>");	
						div.append("<div id='"+url.innerHTML+"_ChartCID'></div>");
						urlId += 1;
					}
				});

				div.append("</div>");
			}
		});
	}


	//客户端url
	if ((checkUserSel(clientCont) && checkUserSel(clientJee))) {
		
		div.append("<div class=\"titleDiv titileCursor\">客户端组件</div>");

		var clientdivs = $("#clientContMainDiv").find(".titleDiv2");
		$.each(clientdivs,function(index,client){ 
			
			//选中
			if(client.className.indexOf("userSelect") >=0){
				var value = client.innerHTML;

				div.append("<div onclick=\"javascript:setCssChangeById('"+value+"_');\" class=\""+client.className+"\">"+value+"</div>");
				div.append("<div style='display:none' id='"+value+"_' >");
				
				//格式化(避免jquery获取不到)
				value = PageClass.encodeForJqueryFormat(value);
				
				//选中组件下的DIV获取
				var urldivs = $("#"+value).find(".titleDiv3");

				$.each(urldivs,function(index2,url){ 
					//选中组件URL
					if(url.className.indexOf("userSelect") >=0){

						var instid = getClienttId(appUrl,appId,ip,url.innerHTML);
						div.append("<div class=\""+url.className+"\" onclick=\"javascript:loadClientUrlChartData('"+instid+"');\" >"+url.innerHTML+"</div>");	
						div.append("<div id='"+instid+"_ChartCID'></div>");
					}
				});

				div.append("</div>");
			}
		});
	}

	//应用服务器\jvm
	if(checkUserSel(appmetrics)){
		div.append( "<div class=\"titleDiv titileCursor\">");
		div.append("自定义指标");
		div.append("</div>");
		div.append("<div id=\"appmetrics_ChartCID_MSG\">没有相关数据</div>");
		div.append("<div id=\"appmetrics_ChartCID\"></div>");
		div.append("<hr/>");
	}
	
	/**
	 * 渲染已经选择的标题 end
	 */
	
	HtmlHelper.id("winchart").innerHTML = div.toString();
	showChartDiv();
};
/**
 * TODO 图表数据加载
 */
function loadChartData(data) {

	try {
		/**
		 * 数据封装 begin
		 */
		var jsonData = eval("(" + data + ")");

		jsonData = jsonData["rs"];
		jsonData = eval(jsonData);
		var dataMap = {};
		var urlId=0;
		var urlDataList = [];
		var metricTag;
		for (var i = 0; i < jsonData.length; i++) {

			var md = jsonData[i];

			var id = md["tags"]["instid"];
			var ip = md["tags"]["ip"];
			var pgid = md["tags"]["pgid"];

			var metric = md["metric"];
			var dps = md["dps"];
			
			//special process for serviceurl ,course for pathparam url there is no instid in aggregate results  
			if(metric.indexOf("urlResp")>-1){
				
				//用 metricTag识别不同的url，使用查询顺序index作为url唯一标识 
				if(metricTag == undefined){
					metricTag = metric;
				}else if(metricTag == metric) {
					urlId++;
				}
				
				var urlO = urlDataList[urlId];
				if (urlO == undefined) {
					urlO = {
						id : id,
						ip : ip,
						pgid : pgid,
						metric : {}
					};
					urlDataList[urlId] = urlO;
				}
				/**
				 * NOTE: RC is for RC400,RC500,RC404....
				 */
				if (metric.indexOf(".RC") > -1) {
					metric = md["tags"]["ptag"];
				} else {
					metric = metric.split(".")[1];
				}
				
				if(metric!=undefined){
					urlO["metric"][metric] = dps;
				}								
			}
			else {
				// 相同ID聚集结果 begin
				var appO = dataMap[id];
				if (appO == undefined) {
					appO = {
						id : id,
						ip : ip,
						pgid : pgid,
						metric : {}
					};
					dataMap[id] = appO;
				}
				// 相同ID聚集结果 end
				
				/**
				 * NOTE: RC is for RC400,RC500,RC404....
				 */
				if ((metric.indexOf(".RC") > -1)) {
					metric = md["tags"]["ptag"];
				}

				else if(metric.indexOf("hostState.") > -1) {
					
					if(metric.indexOf("io.disk") > -1) {
						metric = md["tags"]["ptag"];
					}
					else {
						metric = metric.split(".")[2]+"."+ metric.split(".")[3];
					}					
				}				
				else if(metric.indexOf("procState.") > -1){
					if(metric.indexOf("conn_port")> -1||metric.indexOf("in_port")> -1||metric.indexOf("out_port") > -1) {
						metric = md["tags"]["ptag"];
					}
					else {
						metric = metric.split(".")[1];
					}		
					
				}
				else {
					metric = metric.split(".")[1];
				}
				appO["metric"][metric] = dps;
			}			
		}
		
		montiorData = dataMap;
		urlMoData = urlDataList;
	} catch (e) {
		console.log(e);
	}
};
/**
 * 图表数据渲染，存在数据则渲染，否则图表不显示 begin
 */
function loadAppChartData() {
	try {
		var appurl = $("#appurlInput").val();
		var appid = $("#appidInput").val();
		var key = getAppInstId(appurl, appid);
		var appendDivId = "app_ChartCID";

		// 找到instid数据
		var result = montiorData[key];
		if (!result) {
			return;
		} else {
			var errMsgId = appendDivId + "_MSG";
			$("#" + errMsgId).hide();
		}

		var metric = result["metric"];

		showChartByFilterMontiorData(metric, JeeRTC_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JeeQPS_ChartCfg, appendDivId);

		highchartsTooltipBind("app_ChartCID");
	} catch (e) {
		console.log(e);
	}
};
function loadJvmChartData() {
	try {
		var key = $("#appurlInput").val();
		var appendDivId = "server_ChartCID";

		// 找到instid数据
		var result = montiorData[key];
		if (!result) {
			return;
		} else {
			var errMsgId = appendDivId + "_MSG";
			$("#" + errMsgId).hide();
		}

		var metric = result["metric"];

		showChartByFilterMontiorData(metric, JseCpu_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseThread_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseClass_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseGc_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseHeap_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseNoHeap_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JsePerm_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseCode_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseEden_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseSurv_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseOld_ChartCfg, appendDivId);

		highchartsTooltipBind("server_ChartCID");
	} catch (e) {
		console.log(e);
	}

};
function loadServiceChartData() {
	try {
		var appurl = $("#appurlInput").val();
		var appid = $("#appidInput").val();
		var key = getServerInstId(appurl);
		var appendDivId = "server_ChartCID";

		// 找到instid数据
		var result = montiorData[key];
		if (!result) {
			return;
		} else {
			var errMsgId = appendDivId + "_MSG";
			$("#" + errMsgId).hide();
		}

		var metric = result["metric"];

		showChartByFilterMontiorData(metric, JeeRTC_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JeeQPS_ChartCfg, appendDivId);

		showChartByFilterMontiorData(metric, JseCpu_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseThread_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseClass_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseGc_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseHeap_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseNoHeap_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JsePerm_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseCode_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseEden_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseSurv_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JseOld_ChartCfg, appendDivId);

		highchartsTooltipBind("server_ChartCID");
	} catch (e) {
		console.log(e);
	}
};

function loadHostChartData() {
	try {
		
		var key = $("#ipInput").val();
		var appendDivId = "host_ChartCID";

		// 找到instid数据
		var result = montiorData[key];
		if (!result) {
			return;
		} else {
			var errMsgId = appendDivId + "_MSG";
			$("#" + errMsgId).hide();
		}

		var metric = result["metric"];
		
		showChartByFilterMontiorData(metric, hostIoDisk_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, hostSys_ChartCfg, appendDivId);		

		highchartsTooltipBind("host_ChartCID");
	} catch (e) {
		console.log(e);
	}
};
function loadProcChartData() {
	try {
		
		var key = procInstid;
		
		var appendDivId = "proc_ChartCID";

		// 找到instid数据
		var result = montiorData[key];
		if (!result) {
			return;
		} else {
			var errMsgId = appendDivId + "_MSG";
			$("#" + errMsgId).hide();
		}

		var metric = result["metric"];

		showChartByFilterMontiorData(metric, procConn_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, procCpuMem_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, procIo_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, procDisk_ChartCfg, appendDivId);

		highchartsTooltipBind("proc_ChartCID");
	} catch (e) {
		console.log(e);
	}
};

/**
 * 服务端组件URL
 * @param url
 * @param index: for pathparam, there is no instid in result, so we use index to map correct url.    
 */
function loadServiceUrlChartData(url,urlId) {
	try {	
		//根据排列顺序查找url数据
		result = urlMoData[urlId];
		
		var appendDivId = url + "_ChartCID";

		if (!result) {
			if (HtmlHelper.id(appendDivId)) {
				HtmlHelper.id(appendDivId).innerHTML = "没有相关数据";
			}
			return;
		}

		var metric = result["metric"];

		showChartByFilterMontiorData(metric, JeeRTC_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JeeQPS_ChartCfg, appendDivId);
		document.getElementById("winRow").scrollTop += addScrollTop; // 滚动设置

		highchartsTooltipBind(appendDivId);
	} catch (e) {
		console.log(e);
	}
};
/**
 * 客户端组件URL
 * @param url
 */
function loadClientUrlChartData(url) {
	try {
		var result = montiorData[url];
		var appendDivId = url + "_ChartCID";

		if (!result) {
			if (HtmlHelper.id(appendDivId)) {
				HtmlHelper.id(appendDivId).innerHTML = "没有相关数据";
			}
			return;
		}

		var metric = result["metric"];

		showChartByFilterMontiorData(metric, JeeRTC_ChartCfg, appendDivId);
		showChartByFilterMontiorData(metric, JeeQPS_ChartCfg, appendDivId);

		highchartsTooltipBind(appendDivId);
	} catch (e) {
		console.log(e);
	}
};

function loadAppmetricsChartData(url) {
	try {
		var appurl = $("#appurlInput").val();
		var appid = $("#appidInput").val();
		var key = appurl;
		if(key.indexOf(appid) >= 0){
			key = key.substring(0,key.indexOf(appid)-1);
		}
		
		var appendDivId = "appmetrics_ChartCID";

		// 找到instid数据
		var result = montiorData[key];
		if (!result) {
			return;
		} else {
			var errMsgId = appendDivId + "_MSG";
			$("#" + errMsgId).hide();
		}

		var metric = result["metric"];

		var jsonIndexData = new Object();
		//赋值图表,显示标题 begin , 已经选中的自定指标则视为图表显示数据
		var appMetrics = $("#appmetrics").find("div");
		$.each(appMetrics,function(index,obj){
			if(obj.className.indexOf("userSelect") >=0){
				jsonIndexData[obj.childNodes[2].innerText]=obj.childNodes[2].innerText;
			}
		});
		Appmetrics_ChartCfg.indexData=jsonIndexData;
		//赋值图表,显示标题 end
		
		showChartByFilterMontiorData(metric, Appmetrics_ChartCfg, appendDivId);
		
		highchartsTooltipBind(appendDivId);
	} catch (e) {
		console.log(e);
	}
}

/**
 * 图表数据渲染，存在数据则渲染，否则图表不显示 end
 */
/**
 * TODO 过滤montior结果，获取指定指标数据,然后渲染chart
 */
function showChartByFilterMontiorData(metric,chartConfig,appendCid){

	var series=new Array(),seriesMap={},result =new Array(),serIndex=0;
	var indexs = chartConfig["indexData"];

	$.each(metric, function(key, dps) {
		var list = new Array();
		var isRC = key.indexOf("RC")>-1 && indexs["RC"]?true:false; //RC自动填充，不进行IndexData映射
		
		var isIoDisk = key.indexOf("io.disk")>-1 && indexs["io.disk"]?true:false; //IoDisk自动填充，不进行IndexData映射
		var isConnPort = key.indexOf("conn_")>-1 && indexs["conn_port"]?true:false; //ConnPort自动填充，不进行IndexData映射
		var isInPort = key.indexOf("in_")>-1 && indexs["in_port"]?true:false; //InPort自动填充，不进行IndexData映射
		var isOutPort = key.indexOf("out_")>-1 && indexs["out_port"]?true:false; //OutPort自动填充，不进行IndexData映射
		
		if (indexs[key] || isRC|| isIoDisk|| isConnPort|| isInPort|| isOutPort) {// 找到需要加载的指标数据 
			var n = 0;
			for ( var time in dps) {
				list[n++] = {
					"x" : parseInt(time) * 1000,
					"y" : formatYValue(dps[time],chartConfig["indexType"])
				};
			}
			//chart动态数据显示
			var sName = isRC|| isIoDisk|| isConnPort|| isInPort|| isOutPort?key:indexs[key];
			series.push({name:sName,data:[]});
			seriesMap[key]=serIndex;
			
			result.push(list);
			serIndex++;
		}
		
	});
		
	if (result.length>0) {
		var nowConfig = JsonHelper.clone(chartConfig);
		nowConfig["cid"]=appendCid;
		var nowId = nowConfig.id+"-"+nowConfig.cid; //保证唯一
		nowConfig["id"]=nowId;
		
		nowConfig["series"]=series;
		nowConfig["seriesMap"]=seriesMap;
		
		window["appcharts"].destroy(nowConfig.id);
		window["appcharts"].bulid(nowConfig);
		window["appcharts"].run(nowConfig.id, result);
		
	}
};
function formatYValue(value, indexName) {

	switch (indexName) {
	case "jvmPerm":
	case "jvmCode":
	case "jvmHeap":
	case "jvmNoHeap":
	case "jvmOld":
	case "jvmSurv":
	case "jvmEden":
		value = CommonHelper.getB2Human(value, false, 2);
		break;
	case "jvmCpu":
		value = Math.ceil(value*100)/100;
		break;
	}

	return value;
}
function showListDiv(){
	window.winmgr.show("winList");
	window.winmgr.hide("winRow");
};
function showRowDiv(resetTime){
	window.winmgr.show("winRow");
	window.winmgr.hide("winList");
	if(resetTime){
		initTimeControl(0);
	}
	$(".modal-footer").slideDown();
};
function showTimeDiv(){
	var appEjee = $("#appEntityJee").find("div");
	var appSjee = $("#appServerJee").find("div");
	var appSjvm = $("#appServerJvm").find("div");
	var serviceCont = $("#serviceContMainDiv").find("div");
	var serviceJee = $("#servicesJee").find("div");
	
	var appHost = $("#HostEntity").find("div");
	var appProc = $("#ProcEntity").find("div");
	
	var clientCont = $("#clientContMainDiv").find("div");
	var clientJee = $("#clientJee").find("div");
	var appMetrics = $("#appmetrics").find("div");
	if(checkUserSel(appEjee) || checkUserSel(appSjee) || checkUserSel(appSjvm) 
			|| (checkUserSel(serviceCont) && checkUserSel(serviceJee))
			|| (checkUserSel(clientCont) && checkUserSel(clientJee)
			|| checkUserSel(appMetrics)||checkUserSel(appHost)||checkUserSel(appProc))
					
	){
		$("#userTimeSelDiv").modal({backdrop: 'static', keyboard: false});
	}else{
		showDialog(modalConfig);
	}
};
function showChartDiv(){
	window.winmgr.show("winchart");
};

/**
 * 指标选中CSS切换
 */
function labelCssChange(obj){
	
	var title = obj.childNodes[0];
	if(title.className == "title"){
		title.className = "titleSelect";
	}else{
		title.className = "title";
	}
	
	if(obj.className == "labelDivIndex"){
		obj.className = "labelDivIndex userSelect";
	}else{
		obj.className = "labelDivIndex";
	}
};
/**
 * CSS切换隐藏显示切换
 */
function setCssChangeById(id){
	//组件显示隐藏切换
	var display = document.getElementById(id).style.display;
	if(display=="none"){
		document.getElementById(id).style.display = "block";

	}else{
		document.getElementById(id).style.display = "none";
	} 
	document.getElementById("winRow").scrollTop +=addScrollTop; //滚动设置
};

/**
 * 用户选中列表中的URL CSS切换（含URL的标题CSS切换）
 * setUrlCssChange
 */
function setUrlCssChange(obj,divId,showUpId){
	//组件url CSS切换
	if(obj.className=="titleDiv3"){
		obj.className = "titleDiv3 userSelect userSelectUrl";
	}else{
		obj.className = "titleDiv3";
	}
	
	//父标题CSS切换
	var ffTitleDiv = obj.parentNode.parentNode.getElementsByTagName("div")[0];
	//检测组件所有Url是否有被选中
	var fDiv = obj.parentNode;
	var divs = fDiv.getElementsByTagName("div");
	if(checkUserSel(divs)){
		//标题渲染
		ffTitleDiv.className="titleDiv2 userSelect";
	}else{
		ffTitleDiv.className="titleDiv2";
	}
	 
	//组件指标显示隐藏切换
	var divs = $("#"+divId).find("div");
	if(checkUserSel(divs)){
		$("#"+showUpId).slideDown();
	}else{
		$("#"+showUpId).slideUp();
	}
	
	document.getElementById("winRow").scrollTop += addScrollTop; //滚动设置
};
/**
 * 判断是否存在用户选中
 * @param objs
 * @returns {Boolean}
 */
function checkUserSel(objs){
	var exists = false;
	$.each(objs,function(index,obj){
		if(obj.className.indexOf("userSelect") >=0){
			exists = true;
			return;
		}
	});
	return exists;
};

// TODO js入口
$(document).ready(function() {
	initBody();
	loadListDiv();
	//list事件绑定
	listObj.cellClickUser = PageClass.ajaxGNodeInfoByIp;	
	listObj.sendRequest = PageClass.ajaxGProfile;
	listObj.initTable();
	
});


//=================================== highchart 同步(绑定)光标提示  begin ===================================
function highchartsTooltipBind(divId){
	$("[id='"+divId+"']").bind('mousemove touchmove touchstart', function(e) {
		var i;
		for (i = 0; i < Highcharts.charts.length; i = i + 1) {
			var chart = Highcharts.charts[i];
			var event = chart.pointer.normalize(e.originalEvent);

			var pointsT = [];
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

//=================================== highchart 同步(绑定)光标提示  end ===================================

function  getAppInstId(appurl,appid){
	var appinfo=appurl.split("/");
	//根据appurl是否包含目录路径判断是否需要去除最后的斜杠
	if(appinfo.length>4){	
		appurl = appurl.substring(0,appurl.length-1);
	}
	return appurl+"---"+appid;
}

function getServerInstId(appurl){
	var tempStrs = appurl.split(":");
	var temp = tempStrs[2];
	temp = temp.substring(0,temp.indexOf("/"));
	return tempStrs[0]+":"+tempStrs[1]+":"+temp;
}


function getClienttId(appurl,appid,ip,clientHtmlId){
	var instid = appurl;
	if(instid.indexOf(ip)>=0){
		instid = instid.substring(instid.indexOf(ip));
	}
	//去掉最后的"/"
	if(instid.substring(instid.length-1)=="/"){
		instid = instid.substring(0,instid.length-1);
	}
	if(instid.indexOf(appid)>=0){
		instid = instid.substring(0,instid.indexOf(appid)-1);
	}
	
	
	instid =   instid +"#"+ appid +"#"+clientHtmlId;
	return instid;
}
function getIpByAppurl(appUrl) {
	var ip;
	var isJse = appUrl.indexOf("http:") == -1?true:false;
	if(isJse) {
		var jseInfo1=appUrl.split("//");
		var jseInfo2=jseInfo1[1].split("/");
		ip = jseInfo2[0];
		
	}
	else {
		var jseInfo1=appUrl.split("//");
		var jseInfo2=jseInfo1[1].split(":");
		ip = jseInfo2[0];
	}
	return ip;
}
function getProcInstid(appurl,ip) {
	var isJse = appurl.indexOf("http:") == -1?true:false;
	var port,pid,pname,infoinfo,jsepid,result;
	var result;
	for ( var key in nodeData) {
		var dataObj = eval("("+nodeData[key]+")");
		 
		var infoValue = dataObj["info"];
		 
		var ipValue = dataObj["ip"];
		if(ip!=ipValue) {
			continue;			
		}
		infoinfo = infoValue;		
		 
		var procs=eval("("+infoinfo["node.procs"]+")");
		 
		if(procs==undefined) {
			continue;
		}
		else{
			break;
		}
	}
	if (isJse) {
		var jseInfo=appurl.split("-");
		jsepid=jseInfo[jseInfo.length-1];
		
		for(var key in procs) {								
			 if(jsepid==key) {
				 pid=key;
				 pname=procs[key]["name"];
				 pname = pname.indexOf("java")>-1?procs[key]["tags"]["main"]:pname;				 				 
				 result= ip+"_"+pname+"_"+pid;
				 break;
			 }
		}
	}
	else {
		var jseInfo1=appurl.split(":");
		var jseInfo2=jseInfo1[2].split("/");
		port = jseInfo2[0];		
		for(var key in procs) {		
			var proc=procs[key];		
			var ports=proc["ports"];
			var flag = false;
			for(var i=0;i<ports.length;i++) {
				if (ports[i]==port) {
					pid=proc["pid"];
					pname=proc["name"];
					pname = pname.indexOf("java")>-1?procs[key]["tags"]["main"]:pname;
					flag = true;
					result=  ip+"_"+pname+"_"+pid;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
	}
	return result;
}
