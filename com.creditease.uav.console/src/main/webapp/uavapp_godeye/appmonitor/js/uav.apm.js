/**
 * 应用性能管理工具箱：调用链，慢操作啥的
 * @param app
 */
function APMTool(app) {
	
	var _this=this;
	
	var apmLoader=new AppHubMVCLoader("APM_Loader");
	
	 /**
	  * 页面点击跳转过程中用于传递信息的全局参数
	  */
	this.ivcInfo = {
		comeFrom:""
	}
	
	/**
	 * 调用MA启动/停止MOF上的功能
	 */
	this.callMOFTool=function(toolType, intent, ctrlObj,mofAction,mofActParam) {
		
		var data={intent:"ctrlmof",request:{url:ctrlObj["nodeURL"],server:ctrlObj["serverURL"],file:ctrlObj["file"]+ctrlObj["toolTag"],root:ctrlObj["collectRoot"],appuuid:ctrlObj["appUUID"],mq:ctrlObj["mqTopic"],collectact:ctrlObj["collectAct"],action:mofAction,actparam:mofActParam}};
		
		AjaxHelper.call({
            url: '../../rs/godeye/node/ctrl',
            data: StringHelper.obj2str(data),
            cache: false,
            type: 'POST',
            dataType: 'html',
            timeout: 5000,
            success: function(result){
                var obj = StringHelper.str2obj(result);
                var res = obj["rs"];
                if (obj=="ERR"||res=="ERR") {
                    alert("MOF工具["+toolType+"]操作["+intent+"]失败:"+result);
                }
                else {
                    alert("MOF工具["+toolType+"]操作["+intent+"]成功:"+((obj["msg"]==undefined)?"":obj["msg"]));
                }
            },
            error: function(result){
                alert("MOF工具["+toolType+"]操作["+intent+"]失败:" + result);
            }
        });
	};
	//TODO ----------------------------------------------------------公共--------------------------------------------------------------------
	/**
	 * buildAppAPMCfgWnd
	 */
	this.buildAppAPMCfgWnd=function(sObj) {
		
		var title="";
		switch(sObj["func"]) {
		    case"ivc":
			   title="调用链策略管理";
			   break;
		}
		
		var html="";
		
		var winmode=sObj["winmode"];
		
		if (winmode!="standalone") {
			html="<div class=\"appDetailContent Dark\">" +
	        "<div class=\"topDiv\">" +
	        "<span class=\"tagTitle\">"+title+"</span><br/>"+
	       "<span class=\"idTitle\">"+sObj["appid"]+"</span>" +
	        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppAPMCfgWnd','destroyAppAPMCfgWnd','AppInstChartWnd')\"></div>"+
	        "</div></div>";
		}
		
		//renew draw playground
		html+="<div id='AppAPMCfgWnd_Ctn' style='position:relative;'></div>";

		return html;
	};
	/**
	 * runAppAPMCfgWnd
	 */
	this.runAppAPMCfgWnd=function(sObj) {
		switch(sObj["func"]) {
	    	case"ivc":
	    		_this.runIVCCfgWnd(sObj);
	    		break;
	    	
		}
	};
	/**
	 * destroyAppAPMCfgWnd
	 */
	this.destroyAppAPMCfgWnd=function() {
		
	};
	 
	
	// TODO -------------------------------调用链配置窗口---------------------------------------------------
	/**
	 * 创建日志配置窗口GUI
	 */
	this.buildAppIVCCfgWnd=function(sObj) {
		
		this.IVCfgInfo={};
		
		if (sObj!=undefined) {
			this.IVCfgInfo=sObj;
		}
				
		html="<div class=\"appDetailContent\" style='background:#333;' >" +
        "<div class=\"topDiv\" >" +
        "<span class=\"tagTitle\">调用链配置</span><br/>"+
        "<span class=\"idTitle\" >"+this.IVCfgInfo["appurl"]+"</span>" +
        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppIVCCfgWnd','destroyAppIVCCfgWnd','AppIVCWnd')\"></div>" +	            
        "</div></div>";
		
		html+="<div class=\"AppHubMVCSearchBar\" align='left'>"
		    +"&nbsp;<button  class=\"btn btn-primary\" style=\"width:120px;\" onclick='appAPM.ivcStart(\""+this.IVCfgInfo["appuuid"]+"\")'>启动调用链</button>"
		    +"&nbsp;<button  class=\"btn btn-danger\" style=\"width:120px;\"  onclick='appAPM.ivcStop(\""+this.IVCfgInfo["appuuid"]+"\")'>关闭调用链</button>"
	        +"</div>";
		html+="<div class=\"AppHubMVCSearchBar\" align='left'>"
		    +"&nbsp;<button  class=\"btn btn-primary\" style=\"width:120px;\" onclick='appAPM.ivcDataStart(\""+this.IVCfgInfo["appuuid"]+"\")'>启动重调用链</button>"
		    +"&nbsp;<button  class=\"btn btn-danger\" style=\"width:120px;\"  onclick='appAPM.ivcDataStop(\""+this.IVCfgInfo["appuuid"]+"\")'>关闭重调用链</button>"
	        +"</div>";
		html+="<div class=\"AppHubMVCSearchBar\" align='left'>"
		    +"&nbsp;<button  class=\"btn btn-primary\" style=\"width:120px;\" onclick='appAPM.logTraceStart(\""+this.IVCfgInfo["appuuid"]+"\")'>启动日志关联</button>"
		    +"&nbsp;<button  class=\"btn btn-danger\" style=\"width:120px;\"  onclick='appAPM.logTraceStop(\""+this.IVCfgInfo["appuuid"]+"\")'>关闭日志关联</button>"
	        +"</div>";
	
		return html;
	};
	
	/**
	 * 初始化调用链配置窗口
	 */
	this.runAppIVCCfgWnd=function(sObj) {
		
	};
	
	/**
	 * 
	 */
	this.destroyAppIVCCfgWnd=function() {
		
	};
	
	//TODO -----------------------------------------------------------调用链-----------------------------------------------------------------
	/**
	 * 当前应用的信息
	 */
	this.appInfo;
    /**
	 * TODO：MAIN列表配置
	 */
	var mainListConfig = {
		id : "AppIVCWnd_MainList",
		pid : "AppIVCWnd_TContainer",
		caption:"&nbsp",
		openDelete : false,
		key : "traceid",
		pagerSwitchThreshold : 600,
		pagesize : 100,
		deleteCtr : {
			key : "state",
			showDelete : "0"
		},
		head : {
			traceid : [ '', '1%' ],
			stime : [ '开始时间', '90px' ],
			url : [ '服务URL', '25%' ],
			//eptype : [ '类型', '10%' ],
			epinfo : [ '节点信息', '10%' ],
			"class" : [ '服务类', '20%' ],
			method : [ '方法', '10%' ],
			state : [ '状态', '50px' ],
			cost : [ '耗时ms', '70px' ]
		},
		cloHideStrategy : {
			1000 : [0, 1, 2, 3, 4, 5, 6, 7],
			500 : [ 1, 2,6,7 ],
			300:[1,2],
		},
		events : {
			onRow : function(index, value) {
				
				switch(index) {
				case 0:
					return "<span style='display:none;'>"+value+"</span>";
				case 1:
					return "<div style='word-break:break-all;word-wrap:break-word;white-space:normal;color:#009ad6;'>"+TimeHelper.getTime(value,"FMSN")+"</div>";
				case 2:
					return "<div style='word-break:break-all;word-wrap:break-word;white-space:normal;color:#444;font-size:10px;'>"+value+"</div>";
				case 3:
				case 4:
				case 5:
					return "<div style='word-break:break-all;word-wrap:break-word;white-space:normal;'>"+value+"</div>";
				case 6:
					return _this.UI_list_state(value,true);
				case 7:
					return _this.UI_list_cost(value);
				}
				
				return value;
			}
		}
	};
	
	/**
	 * TODO TRACE列表配置
	 */
	var traceListConfig = {
		id : "AppIVCTraceWnd_TraceList",
		pid : "AppIVCTraceWnd_TContainer",
		openDelete : false,
		key : "traceid",
		caption:"&nbsp",
		pagerSwitchThreshold : 600,
		pagesize : 1000,
		deleteCtr : {
			key : "state",
			showDelete : "0"
		},
		head : {
			spanid:['执行顺序','90px'],
			stime : [ '开始时间', '90px' ],
			url : [ '服务/调用URL', '25%' ],
			eptype : [ '类型', '40px' ],
			epinfo : [ '节点信息', '10%' ],
			"class" : [ '所在类', '20%' ],
			method : [ '方法', '10%' ],
			ipport: ['IP&端口','10%'],
			appid:['应用标识','10%'],
			state : [ '状态', '50px' ],
			cost : [ '耗时ms', '70px' ],
			traceid : [ '', '1px' ],
			logLink : [ '日志关联', '50px' ]
		},
		cloHideStrategy : {
			1000 : [ 0, 1, 2, 3, 4, 5, 6, 7, 8,9,10,11 ],
			800 :   [ 0, 1, 2, 3, 4, 5, 6, 7, 8,9,10,11  ],
			500 : [ 0,1, 2,9,10,11 ],
			300:[0,1,2],
		},
		columnStyle :{
			stime:"padding:0px;",
			url:"padding:0px;",
			eptype:"padding:0px;",
			epinfo:"padding:0px;",
			"class":"padding:0px;",
			method:"padding:0px;",
			ipport:"padding:0px;"
		},
		events : {
			onRow : function(index, value) {
				
				switch(index) {
				case 0:
					var level=value.split(".");
					return "<span style=''>"+level.length+"</span>";
				case 11:
					return "<span style='display:none'>"+value+"</span>";
				case 1:
					return "<div class=\"ivcCell\" style='color:#009ad6;display:inline-block;' onclick=\"javascript:appAPM.jumpAppIVCDataWnd(this)\">"+TimeHelper.getTime(value,"FMSN")+"</div>";
				case 2:
					return "<div class=\"ivcCell\" style='color:#444;font-size:10px;' onclick=\"javascript:appAPM.jumpAppIVCDataWnd(this)\" >"+value+"</div>";
				case 3:
					return "<div class=\"ivcCell\" style='color:#444;font-size:10px;' onclick=\"javascript:appAPM.jumpAppIVCDataWnd(this)\" >"+value+"</div>";
				case 4:
					return "<div class=\"ivcCell\" style='color:#444;font-size:10px;' onclick=\"javascript:appAPM.jumpAppIVCDataWnd(this)\" >"+value+"</div>";
				case 5:
					return "<div class=\"ivcCell\" style='color:#444;font-size:10px;' onclick=\"javascript:appAPM.jumpAppIVCDataWnd(this)\" >"+value+"</div>";
				case 6:
					return "<div class=\"ivcCell\" style='color:#444;font-size:10px;' onclick=\"javascript:appAPM.jumpAppIVCDataWnd(this)\" >"+value+"</div>";
				case 7:				
					return "<div class=\"ivcCell\" onclick=\"javascript:appAPM.jumpAppIVCDataWnd(this)\" >"+value+"</div>";
				case 8:
					var color="";
					if (value.indexOf(_this.appInfo["appid"])>-1) {
						color="font-weight:bold;color:#36648B;";
					}
					return "<div style='word-break:break-all;word-wrap:break-word;white-space:normal;"+color+"' onclick=\"javascript:appAPM.jumpAppIVCDataWnd(this)\" >"+value+"</div>";
				case 9:
					return _this.UI_list_state(value,true);
				case 10:
					return _this.UI_list_cost(value);
				case 12:
					return "<div class=\"icon-link\" style=\"cursor:pointer\" title=\"提取关联日志\" onclick=\"javascript:appAPM.showLogSelect(this)\"></div>";
				}
				
				return value;
			}
		}
	};
	
	
	this.showLogSelect = function(sObj){
		
		var params = {};
		params.comeFrom = this.ivcInfo["comeFrom"];
		var pNode = sObj.parentNode.parentNode;
		params.ctn = "\"uav_" + pNode.id + "\"";
		params.ipport = pNode.getElementsByTagName("td")[7].id;
		params.appid = pNode.getElementsByTagName("td")[8].id;
		var appurl = "http://"+params.ipport+"/"+params.appid+"/";
		AjaxHelper.call({
            url: '../../rs/godeye/profile/q/cache',
            data: {"fkey":"appurl","fvalue":appurl},
            cache: false,
            type: 'GET',
            dataType: 'html',
            timeout: 30000,
            success: function(result){
                var obj = StringHelper.str2obj(result);
                var res = obj["rs"];
                var datas = StringHelper.str2obj(res);
                var data;
                for(var key in datas){
                	data = datas[key];
                }
                var logdata = StringHelper.str2obj(data)["logs.log4j"];
                var logObj = StringHelper.str2obj(logdata);
                for(var key in logObj){
                	if(key.lastIndexOf("/") > 0){
                		key = key.substring(key.lastIndexOf("/") + 1);
                	}
                	$("#logSelector").append("<option value='"+ key +"' width='%97'> "+ key +"</option>");
                }
                if($("#logSelector option").size()>0){
                	$("#selectLogWinfooter").append( '<button class="btn btn-primary " onclick=\'javascript:appAPM.jumpLogRollWnd('+ JSON.stringify(params) +')\';">确定</button>');
                }else{
                	$("#logSelector").append("<option value='无日志文件' width='%97'>无日志文件</option>");
                }
                $("#selectLogWinfooter").append( '<button class="btn" data-dismiss="modal" onclick="javascript:appAPM.clearLogSelector()">关闭</button></div>');
            },
            error: function(result){
            	$("#logSelector").append("<value='获取日志文件失败' width='%97'>");
            	$("#selectLogWinfooter").append( '<button class="btn" data-dismiss="modal" onclick="javascript:appAPM.clearLogSelector()">关闭</button></div>');
            }
         });
		
		$("#selectLogWin").modal({backdrop:false});
	}
	
	/**
	 * 根据value展示state的不同UI
	 */
	this.UI_list_state=function(value,isAlert) {
		
		var vinfo=value.split("?");
		
		var state=vinfo[0];
		
		var msg="";
		var msgClik="";
		var point="";
		if (vinfo.length==2) {
			msg=vinfo[1];
		}
		if(isAlert){
			var alertMsg = msg;
			msg="";
			msgClik=" onclick=\"alert('"+alertMsg+"')\" title=\"点击查看状态信息\" ";
			point="cursor:pointer;";
		}
		
		try {
			var st=parseInt(state);
			if (st>=500) {
				state="<span  style='width:45px;color:red;font-weight:bold;"+point+"' "+msgClik+">"+state+" "+msg+"</span>";
			}
			else if (st<500&&st>=400) {
				state="<span  style='font-weight:bold;color:#EEB422;"+point+"'  "+msgClik+">"+state+" "+msg+"</span>";
			}
			else if (st==-1) {
				state="<span  style='width:45px;color:red;font-weight:bold;"+point+"'  "+msgClik+" >ERR"+" "+msg+"</span>";
			}
			else if (state=="1") {
				state="<span  style='color:#A2CD5A;"+point+"'  "+msgClik+">OK"+" "+msg+"</span>";
			}
			else {
				state="<span  style='color:#A2CD5A;"+point+"'  "+msgClik+">"+state+" "+msg+"</span>";
			}
		}catch(e) {
			
		}
		
		
		return state;
	};
	
	/**
	 * 根据value展示cost的不同UI
	 */
	this.UI_list_cost=function(value) {
		
		var state=value;
		try {
			var st=parseInt(value);
			
			if (st>=2000) {
				state="<span  style='color:red;font-weight:bold;' >"+value+"</span>";
			}
			else if (st<2000&&st>=1000) {
				state="<span  style='font-weight:bold;color:#EEB422'>"+value+"</span>";
			}
			else {
				state="<span  style='color:#A2CD5A'>"+value+"</span>";
			}
			
		}catch(e) {
			
		}
		
		return state;
	};

	/**
	 * MAIN列表对象
	 */
	this.mainList = undefined;
	/**
	 * TRACE列表对象
	 */
	this.traceList=undefined;
	
	/**
	 * 调用链查看窗口的按钮
	 */
	this.buildIVCWndButton=function(jsonObj,appInstMOId,isJse) {
		
		if (isJse==true) {
			return "";
		}
		
		var sObj={svrid:jsonObj["svrid"],appuuid:appInstMOId,appid:jsonObj["appid"],appname:jsonObj["appname"],appurl:jsonObj["appurl"],appgp:jsonObj["appgroup"],ip:jsonObj.ip,isJse:isJse};
		
		var sObjStr=JSON.stringify(sObj);
		
		var sb=new StringBuffer();
		sb.append("<div class='contentDiv' >");
		sb.append("<span class=\"componentExpandButton componentExpandButtonStyle3\" style='font-size:14px;' onclick='app.controller.showWindow("+sObjStr+",\"AppIVCWnd\",\"buildAppIVCWnd\",\"runAppIVCWnd\")'>调用链跟踪</span>");
		sb.append("</div>");
		
		return sb.toString();
	};
	
	/**
	 * TODO 调用链查看窗口UI
	 */
	this.buildAppIVCWnd=function(sObj) {
		
		//设置当前应用信息
		this.appInfo={
				appuuid:"",
				appid:"",
				appurl:"",
				appname:""
		};
		
		if (sObj!=undefined) {
			this.appInfo=sObj;
			this.appInfo["appname"]=(sObj["appname"]==undefined||sObj["appname"]==""||sObj["appname"]=="undefined")?sObj["appid"]:sObj["appname"];
		}
		
		var html="";
		
		var winmode=this.appInfo["winmode"];
		//App模式
		if (winmode!="standalone") {
			var sObjStr=JSON.stringify(sObj);
			html="<div class=\"appDetailContent\" style='background:#333;' >" +
	        "<div class=\"topDiv\" >" +
	        "<span class=\"tagTitle\">"+this.appInfo["appname"]+"</span><br/>"+
	        "<span class=\"idTitle\" >"+this.appInfo["appurl"]+"</span>" +
	        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppIVCWnd','destroyAppIVCWnd','AppInstChartWnd')\"></div>" +
	        "<div class=\"glyphicon glyphicon-cog\" onclick='javascript:app.controller.showWindow("+sObjStr+",\"AppIVCCfgWnd\",\"buildAppIVCCfgWnd\",\"runAppIVCCfgWnd\")'></div>"+
	        "</div></div>";
			
			html+="<div class=\"AppHubMVCSearchBar AppIVCWnd_AppMode\" align='left' style='background:#eee;'>";
		}
		//全局模式
		else {
			html="<div class=\"AppHubMVCSearchBar AppIVCWnd_GlobalMode\">"
			html+= "<div id=\"AppIVCWnd_Selectors\">应用实例 </div>";
		}
		
		html+="<div class=\"\">时间 <div class=\"\" style='display:inline-block;'>" 
    		+	"<input id='AppIVCWnd_TimeRange' type=\"text\" class=\"form_datetime\" style=\"width:100px;font-size:11px;height:27px;\" readonly placeholder='全部' title=\"选择时间区段\" />"
    		+ "<span id='AppIVCWnd_TimeRangeSelectorCtn'></span>"
    		+ "<button type=\"button\" class=\"btn btn-default\" title=\"清除时间段，代表全部时间\" onclick='appAPM.cleanTimeRange()'>"
			+ "<span class=\"glyphicon glyphicon-remove\"></span>"
			+ "</button>"
			
			+ "<button id=\"searchbtn\" type=\"button\" class=\"btn btn-default\" title=\"快速搜索\n不输入时间范围，则默认搜索最近1分钟调用链\" onclick='appAPM.callIVCQuery(\"qsearch\",{appuuid:\""+this.appInfo["appuuid"]+"\",appurl:\""+this.appInfo["appurl"]+"\",appid:\""+this.appInfo["appid"]+"\"});'>"
			+ "<span class=\"glyphicon glyphicon-search\"></span>"
			+ "</button>"
			
            + "</div></div>";
    	
//    	    + "<div>搜索 <input id=\"AppIVCWnd_KeyWord\" class=\"form-control AppHubMVCSearchBarInputText\""
//			+ " type=\"text\" title=\"1. 可输入多个关键字，用空格分隔，代表或连接，即任一关键字匹配\n2.多个关键字用+号连接，代表所有关键字匹配\n3.空格和+号可同时使用，或连接优先\n4. 可在关键字两头加*，代表启用模糊匹配，例如*com.creditease*，则所有包含com.creditease字符串都会被匹配\" placeholder=\"关键字\" value=\"\"></input>"

		html+="&nbsp;<button type=\"button\" class=\"btn btn-info\" title=\"查看最近1分钟内的服务请求\" onclick='appAPM.callIVCQuery(\"lst1min\",{appuuid:\""+this.appInfo["appuuid"]+"\",appurl:\""+this.appInfo["appurl"]+"\",appid:\""+this.appInfo["appid"]+"\"})'>L1min</button>"+
				"&nbsp;<button type=\"button\" class=\"btn btn-info\" title=\"查看1小时内最慢100条的服务请求\" onclick='appAPM.callIVCQuery(\"slow100in1hr\",{appuuid:\""+this.appInfo["appuuid"]+"\",appurl:\""+this.appInfo["appurl"]+"\",appid:\""+this.appInfo["appid"]+"\"})'>s100in1hr</button>" +
		        "&nbsp;<button type=\"button\" class=\"btn btn-info\" title=\"查看24小时内最近100条的服务请求\"  onclick='appAPM.callIVCQuery(\"lst100\",{appuuid:\""+this.appInfo["appuuid"]+"\",appurl:\""+this.appInfo["appurl"]+"\",appid:\""+this.appInfo["appid"]+"\"})'>L100</button>" +
		        "&nbsp;<button type=\"button\" class=\"btn btn-info\" title=\"查看24小时内最慢100条的服务请求\" onclick='appAPM.callIVCQuery(\"slow100\",{appuuid:\""+this.appInfo["appuuid"]+"\",appurl:\""+this.appInfo["appurl"]+"\",appid:\""+this.appInfo["appid"]+"\"})'>s100</button>" ;
		
		html+="<div  id='AppIVCWnd_TContainer' style='font-size:12px;color:black;'></div>";
		
		return html;
	};
	
	/**
	 * 调用链查看窗口初始化
	 */
	this.runAppIVCWnd=function(sObj) {
		
		var winmode=sObj["winmode"];
		//全局模式
		if (winmode=="standalone") {
			this.appSelector=new AppHubSelector({
				id:"AppIVCWnd_AppSelector",
				cid:"AppIVCWnd_Selectors",
				title:"请选择应用集群",
				style:"font-size:12px;width:120px",
				events:{
					onchange:"appAPM.onChangeAppSelector()"
				}
			});
			
			this.appInstSelector=new AppHubSelector({
				id:"AppIVCWnd_AppInstSelector",
				cid:"AppIVCWnd_Selectors",
				title:"请选择应用实例",
				style:"font-size:12px;width:120px"
			});
			
			this.appSelector.init();
			this.appInstSelector.init();
			this.callAppProfile(sObj);
		}
		
		//mainlist
		this.mainList= new AppHubTable(mainListConfig);
		
		this.mainList.initTable();
		
		this.mainList.cellClickUser = function(id,pNode) {
			//comeFrom表示该请求的来源位置，用于后面的返回定位
			app.controller.showWindow({traceid:id,comeFrom:"IVC"},"AppIVCTraceWnd","buildAppIVCTraceWnd","runAppIVCTraceWnd");
		};
		
		//init datetime picker		
		 $('.form_datetime').datetimepicker({
				language : 'zh-CN',
				autoclose : true,
				minuteStep : 1,
				minView: 1,
				format:"yyyy-mm-dd hh",
				todayBtn : true
			});
		
		 //init timetrange selector
		 this.timeRangeSelector=new AppHubSelector({
				id:"AppIVCWnd_TimeRangeSelector",
				cid:"AppIVCWnd_TimeRangeSelectorCtn",
				title:"时间单位",
				style:"font-size:12px;width:50px",
				events:{
					onchange:"appAPM.onChangeTimeRangeSelector()"
				}
			});
		 this.timeRangeSelector.init();
		 this.timeRangeSelector.load([{title:"日期",value:"D"},{title:"小时",value:"H",select:true},{title:"分钟",value:"M"}]);
		 
		 //init time sort selector
		 this.timeSortSelector=new AppHubSelector({
				id:"AppIVCWnd_TimeSortSelector",
				cid:"AppIVCWnd_TimeRangeSelectorCtn",
				title:"时间排序方式",
				style:"font-size:12px;width:50px"
			});
		 this.timeSortSelector.init();
		 this.timeSortSelector.load([{title:"降序",value:"DESC", select:true},{title:"升序",value:"ASC"}]);
	};
	

	
	/**
	 * 调用链Trace窗口UI
	 */
	this.buildAppIVCTraceWnd=function(sObj) {
		if(this.appInfo==undefined){
			//设置当前应用信息
			this.appInfo={
					appuuid:"",
					appid:"",
					appurl:"",
					appname:""
			};
		}
		var html=
		"<div class=\"appDetailContent\"  style='background:#333;' >" +
        	"<div class=\"topDiv\">" +
        		"<span class=\"tagTitle\">调用链跟踪</span><br/>"+
        		"<span class=\"idTitle traceid\" style='font-size:8px;color:gray;'>"+sObj["traceid"]+"</span>";
		if(sObj!=undefined && "comeFrom" in sObj){
			this.ivcInfo["comeFrom"] = sObj["comeFrom"];
			if(sObj["comeFrom"]=="NewLog"){
				html +=
					"<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppIVCTraceWnd','destroyAppIVCTraceWnd','AppNewLogRollWnd')\"></div>" +
					"<div class=\"icon-link\" style=\"cursor:pointer\" title=\"转到调用链搜索\" onclick=\"app.controller.showWindow({'winmode':'standalone','appname':'"+sObj["appname"]+"','appuuid':'"+sObj["appuuid"]+"'},'AppIVCWnd','buildAppIVCWnd','runAppIVCWnd');\"></div>";
			}else{
				html +=
					"<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppIVCTraceWnd','destroyAppIVCTraceWnd','AppIVCWnd')\"></div>";
			}
		}else{
			html +=
					"<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppIVCTraceWnd','destroyAppIVCTraceWnd','AppIVCWnd')\"></div>";
		}
		html+=
        	"</div>"+
        "</div>"+
		"<div  id='AppIVCTraceWnd_TContainer' style='font-size:12px;color:black;'>" +
		"</div>";
		
		var sb=new StringBuffer();
		
		sb.append('<div class="modal fade" id="selectLogWin" aria-hidden="false">');
		sb.append('<div class="modal-dialog">');
		sb.append( '<div class="modal-content">');
		sb.append( '<div class="modal-header" style="display: -webkit-box;">');
		sb.append( '<div style="width: 97%;"><h5>日志文件选择</h5></div>');

		sb.append( '</div>');
		sb.append( '<div class="modal-body" id="actionBodyDiv">');
		sb.append( "<select id=\"logSelector\">");
		sb.append( '</select>');
	
		sb.append( '</div>');
		sb.append( '<div class="modal-footer"  id ="selectLogWinfooter">');
		sb.append( '</div>' + '</div>' + '</div>');
		html += sb.toString();
		
		
		return html;
	};
	
	this.clearLogSelector=function(){
		$("#selectLogWin").modal('hide');
		$("#logSelector").empty();
		$("#selectLogWinfooter").empty();
	}
	
	/**
	 * 调用链Trace窗口初始化
	 */
	this.runAppIVCTraceWnd=function(sObj) {

		this.traceList=new AppHubTable(traceListConfig);
		
		this.traceList.initTable();
		
		this.callIVCQuery("trace",sObj);
	};
	/**
	 * 调用链Trace窗退出
	 */
	this.destroyAppIVCTraceWnd=function(){
		
	}
	
	/**
	 * 跳转到日志搜索结果页
	 */
	this.jumpLogRollWnd = function(param){
//		var pNode = sObj.parentNode.parentNode;
//		var param = StringHelper.str2obj(params);
//		param.ipport = pNode.getElementsByTagName("td")[7].id;;
//		param.appid = pNode.getElementsByTagName("td")[8].id;
		param.intent="qContent";
		param.logfile = HtmlHelper.id("logSelector").value;
		param.logtype = HtmlHelper.id("logSelector").value.replace(".","_") + "_def";
		$("#selectLogWin").modal('hide');
		$("#logSelector").empty();
		$("#selectLogWinfooter").empty();
		
		app.controller.showWindow(param,"AppNewLogRollWnd","buildAppNewLogRollWnd","runAppNewLogRollWnd");
	};
	
	/**
	 * 跳转到重调用链明细页
	 */
	this.jumpAppIVCDataWnd = function(sObj){
		var pNode = sObj.parentNode.parentNode;
		var spanid = pNode.getElementsByTagName("td")[0].id;
		var stime = pNode.getElementsByTagName("td")[1].id;
		var url = pNode.getElementsByTagName("td")[2].id;
		var epinfo = pNode.getElementsByTagName("td")[4].id;
		var clazz = pNode.getElementsByTagName("td")[5].id;
		var method = pNode.getElementsByTagName("td")[6].id;
		var ipport = pNode.getElementsByTagName("td")[7].id;
		var appid = pNode.getElementsByTagName("td")[8].id;
		var state = pNode.getElementsByTagName("td")[9].id;
		var traceid = pNode.id;
		var parentid = $(pNode).attr("data-tt-parent-id");
		
		app.controller.showWindow({traceid:traceid,stime:stime,url:url,epinfo:epinfo,clazz:clazz,method:method,ipport:ipport,appid:appid,state:state,spanid:spanid,parentid:parentid},"AppIVCDataWnd","buildAppIVCDataWnd","runAppIVCDataWnd");
	};
	
	/**
	 * 重调用链窗口UI
	 */
	this.buildAppIVCDataWnd=function(sObj) {
		
		var html="<div class=\"appDetailContent\"  style='background:#333;' >" +
        "<div class=\"topDiv\">" +
        "<span class=\"tagTitle\">调用环节明细</span><br/>"+
        "<span class=\"idTitle\" style='font-size:8px;color:gray;'>"+sObj["traceid"]+"</span>" +
        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppIVCDataWnd','destroyAppIVCDataWnd','AppIVCTraceWnd')\"></div>" +
        "<div class=\"icon-refresh\" onclick='javascript:app.controller.runAppIVCDataWnd({traceid:\""+sObj["traceid"]+"\",appid:\""+sObj["appid"]+"\",epinfo:\""+sObj["epinfo"]+"\",spanid:\""+sObj["spanid"]+"\",clazz:\""+sObj["clazz"]+"\",ipport:\""+sObj["ipport"]+"\",method:\""+sObj["method"]+"\",parentid:\""+sObj["parentid"]+"\",stime:\""+sObj["stime"]+"\",state:\""+sObj["state"]+"\",url:\""+sObj["url"]+"\"})'></div>" +
        "</div></div>";
		
		var sb=new StringBuffer();
		sb.append("<div class=\"contentDiv\"><div class=\"shine2\"></div>" +
        "                <div class=\"kv\">" +
        "                    <span class=\"kvField\">时间</span><span>：</span>"+ TimeHelper.getTime(Number(sObj["stime"]),"FMS") +"</span>"+
        "                </div>" +
        "                <div class=\"kv\">" +
        "                    <span class=\"kvField\">服务/调用URL</span><span>：</span>"+ sObj["url"] +"</span>"+
        "                </div>" +
        "                <div class=\"kv\">" +
        "                    <span class=\"kvField\">节点信息</span><span>：</span>"+ sObj["epinfo"] +"</span>"+
        "                </div>" +
        "                <div class=\"kv\">" +
        "                    <span class=\"kvField\">所在类</span><span>：</span>"+ sObj["clazz"] +"</span>"+
        "                </div>" +
        "                <div class=\"kv\">" +
        "                    <span class=\"kvField\">方法</span><span>：</span>"+ sObj["method"] +"</span>"+
        "                </div>" +
        "                <div class=\"kv\">" +
        "                    <span class=\"kvField\">IP端口</span><span>：</span>"+ sObj["ipport"] +"</span>"+
        "                </div>" +
        "                <div class=\"kv\">" +
        "                    <span class=\"kvField\">应用标识</span><span>：</span>"+ sObj["appid"] +"</span>"+
        "                </div>" +
        "                <div class=\"kv\">" +
        "                    <span class=\"kvField\">状态</span><span>：</span>"+ _this.UI_list_state(sObj["state"],false) +"</span>"+
        "                </div>" +
        "				 <span class=\"componentExpandButton\" onclick=\"app.controller.openClose('AppIVCDataWnd_TraceMetaData')\">元数据</span>" +
        "				 <div id='AppIVCDataWnd_TraceMetaData' class='componentFTab' style='display:none;'>" +
        "                	<div class=\"kv\">" +
        "                    	<span class=\"kvField\">spanId</span><span>：</span>"+ sObj["spanid"] +"</span>"+
        "                	</div>"
        );
		if(sObj["parentid"]==undefined){
			sObj["parentid"] = "无";
		}
		sb.append(
		"                	<div class=\"kv\">" +
		"                    	<span class=\"kvField\">parentid</span><span>：</span>"+ sObj["parentid"] +"</span>"+
		"                	</div>" +
		"				 </div>"
				);
		
		//IVCData Container
		sb.append("<div id='AppIVCDataWnd_TContainer_ContentDiv'></div></div>");
		
		html+="<div  id='AppIVCDataWnd_TContainer' style='font-size:16px;color:#efefef;'>"+sb.toString()+"</div>";
		return html;
	};
	
	/**
	 * 调用链Trace窗口初始化
	 */
	this.runAppIVCDataWnd=function(sObj) {
		this.callIVCDataQuery("qParams",sObj);
		
	};
	
	/**
	 * 加载单条重调用链数据
	 */
	this.loadIVCData = function(data,sObj) {
		
		var sb=new StringBuffer();
		var isEmpty = true;
		if(data!=undefined&&data.length!=0){
			var jsonObj = {};
			jsonObj = data[0];
			if (jsonObj["rpc_req_head"]!=undefined) {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"rpc_req_head"+"')\">请求头信息</span>");
				sb.append("<div id='rpc_req_head' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["rpc_req_head"]+"</div>");
				isEmpty = false;
			}
			if (jsonObj["rpc_req_body"]!=undefined) {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"rpc_req_body"+"')\">请求内容</span>");
				sb.append("<div id='rpc_req_body' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["rpc_req_body"]+"</div>");
				isEmpty = false;
			}
			if (jsonObj["rpc_rsp_head"]!=undefined) {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"rpc_rsp_head"+"')\">响应头信息</span>");
				sb.append("<div id='rpc_rsp_head' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["rpc_rsp_head"]+"</div>");
				isEmpty = false;
			}
			if (jsonObj["rpc_rsp_body"]!=undefined) {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"rpc_rsp_body"+"')\">响应内容</span>");
				sb.append("<div id='rpc_rsp_body' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["rpc_rsp_body"]+"</div>");
				isEmpty = false;
			}
			if (jsonObj["rpc_rsp_exception"]!=undefined) {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"rpc_rsp_exception"+"')\">响应异常信息</span>");
				sb.append("<div id='rpc_rsp_exception' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["rpc_rsp_exception"]+"</div>");
				isEmpty = false;
			}
			if (jsonObj["sql_req"]!=undefined&&jsonObj["sql_req"]!="{}") {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"sql_req"+"')\">上送sql</span>");
				sb.append("<div id='sql_req' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["sql_req"]+"</div>");
				isEmpty = false;
			}
			if (jsonObj["sql_ret"]!=undefined&&jsonObj["sql_ret"]!="{}") {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"sql_ret"+"')\">影响行数</span>");
				sb.append("<div id='sql_ret' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["sql_ret"]+"</div>");
				isEmpty = false;
			}
			if (jsonObj["method_req"]!=undefined&&jsonObj["method_req"]!="{}") {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"method_req"+"')\">方法入参</span>");
				sb.append("<div id='method_req' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["method_req"]+"</div>");
				isEmpty = false;
			}
			if (jsonObj["method_ret"]!=undefined&&jsonObj["method_ret"]!="{}") {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"method_ret"+"')\">方法出参</span>");
				sb.append("<div id='method_ret' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["method_ret"]+"</div>");
				isEmpty = false;
			}
			if (jsonObj["mq_head"]!=undefined) {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"mq_head"+"')\">MQ头信息</span>");
				sb.append("<div id='mq_head' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["mq_head"]+"</div>");
				isEmpty = false;
			}
			if (jsonObj["mq_body"]!=undefined) {
				sb.append("<span class=\"componentExpandButton\" onclick=\"app.controller.openClose('"+"mq_body"+"')\">MQ消息体</span>");
				sb.append("<div id='mq_body' class='componentFTab' style='display:none;'>");
				sb.append(jsonObj["mq_body"]+"</div>");
				isEmpty = false;
			}
		}
		
		if(isEmpty){
			var errMsg = "无数据，请刷新重试";
			var epinfo = sObj["epinfo"].split(",")[0];
			//当前支持的类型
			var epinfos = ["http.service","apache.http.Client","apache.http.AsyncClient","mq.service","rabbitmq.client","jdbc.client","method","dubbo.provider","dubbo.consumer"];
			if($.inArray(epinfo, epinfos)==-1){
				errMsg = "不支持的数据类型";
			}
			sb.append("<span class=\"componentExpandButton\" >"+errMsg+"</span>");
		}
		HtmlHelper.id("AppIVCDataWnd_TContainer_ContentDiv").innerHTML=sb.toString();
		
	}
	
	/**
	 * 加载MAINList的数据，显示该应用的服务调用链数据
	 */
	this.loadIVCList = function(list,datas,count,totalCount,params) {
		
		list.clearTable();
		if(params!=undefined&&"appname" in params&&"appuuid" in params){
			_this.appInfo["appid"] = params["appname"];
			list.setCaption("<div align='center'>应用实例:<font color='#333'>"+params["appname"]+"("+params["appuuid"].split("/")[2]+")</font>，总数：<font color='red'>"+totalCount+"</font></div>");
		}else{
			list.setCaption("<div align='center'>应用实例:<font color='#333'>"+this.appInfo["appname"]+"("+this.appInfo["appuuid"].split("/")[2]+")</font>，总数：<font color='red'>"+totalCount+"</font></div>");
		}
		
		// 必须先显示分页
		list.setTotalRow(count);
		list.renderPagination();		
		list.addTreeTableRows(datas);
		
		// only AppIVCTraceWnd_TraceList need treetable
		if (list.mconfig.id=="AppIVCTraceWnd_TraceList") {
			var td=$("#AppIVCTraceWnd_TraceList").treetable({
				expandable : true,
				indent: 6
			});
			//diaplay the Root Node
			$("#AppIVCTraceWnd_TraceList").treetable("expandNode","1");
		}
	};
	// TODO ----------------------------------调用链查看窗口：全局模式专有---------------------
	this.profileDAO=new UAVProfileDAO();
	
	/**
	 * 全局视图需要单独取该用户可查看的profile信息
	 */
	this.callAppProfile=function(sObj) {
		this.profileDAO.callAppProfile(function(jsonData) {
			_this.loadAppSelector(jsonData);
			if(sObj != undefined){
				if(sObj["appname"]){
					$("select#AppIVCWnd_AppSelector").val(sObj["appname"]); 
					appAPM.onChangeAppSelector();
				}
				if(sObj["appuuid"]){
					$("select#AppIVCWnd_AppInstSelector").val(sObj["appuuid"]);
				}
			}
		});
	};
	
	/**
	 * onChangeAppSelector
	 */
	this.onChangeAppSelector=function() {
		
		this.loadAppInstSelector();
	};	
	/**
	 * loadAppSelector
	 */
	this.loadAppSelector=function(jsonData) {
		
		var collectObj=this.profileDAO.loadAppSelector(jsonData, ["appinsts"], function(id,map,data){
			
			if (id=="appinsts") {
				var appinsts=map.get(data["appid"]);
				
				if (!map.contain(data["appid"])) {
					appinsts=new Set();				
					map.put(data["appid"],appinsts);
				}
				
				appinsts.add(_this.profileDAO.getAppUUID(data));
			}
		});
		
		var datas=collectObj["options"];
		this.appInfo["appinsts"]=collectObj["info"]["appinsts"];
		this.appSelector.load(datas);
		this.loadAppInstSelector();
	};
	
	/**
	 * loadAppInstSelector
	 */
	this.loadAppInstSelector=function() {
		
		var selAppID=this.appSelector.value();
		
		if (selAppID==undefined) {
			return;
		}
		
		var list=this.appInfo["appinsts"].get(selAppID);
		
		var options=[];
		
		//options[options.length]={title:"全部实例",value:"all",select:true};
		
		for(var i=0;i<list.count();i++) {
			var option={};
			option["title"]=list.get(i).split("/")[2];
			option["value"]=list.get(i);
			
			options[options.length]=option;
		}
				
		this.appInstSelector.load(options);
	};
	// TODO ---------------------------------SPAN排序---------------------------------------------
	/**
	 * 用于trace span 排序的数据结构
	 */
	function TNode(id) {
		this.spanid=id;
		this.list=new List();        //spanid相同的哪些span
		this.childs=new Map(); //每个span的子节点
	};
	
	/**
	 * 按M叉树先根遍历输出
	 */
	function traceOut(datas,node) {
		
		for(var i=0;i<node.list.count();i++) {
			datas[datas.length]=node.list.get(i);
		}
		
		/**
		 * 同层，要按字符串进行一次排序就对了
		 */
		node.childs.mapValues.sort(
	        function(sA,sB) {
	        	var a=sA.spanid;
	        	var b=sB.spanid;
	        	if(a.length==b.length){
		              return a.localeCompare(b);
		          }else{
		              return a.length-b.length;
		          }
	        }
		);
		
		for(var i=0;i<node.childs.mapValues.count();i++) {			
			var cnode=node.childs.mapValues.get(i);			
			traceOut(datas,cnode);
		}
	}
	
	/**
	 * 调用链按spanid进行排序
	 */
	this.sortTrace=function(intent,datas) {
		
		if (intent!="trace") {
			return datas;
		}
				
		/**
		 * 构造M叉树
		 */
		var root=new TNode(datas[0].spanid);
        root.list.add(datas[0]);
        
		var nodeMap=new Map();		
		nodeMap.put(datas[0].spanid, root);

		for(var i=1;i<datas.length;i++) {
			
			var spanid=datas[i].spanid;
			var pid=datas[i].parentid;
			
			var curNode=nodeMap.get(spanid);
			
			if (curNode==undefined) {
				curNode=new TNode(spanid);
				nodeMap.put(spanid, curNode);
			}
			
			curNode.list.add(datas[i]);
			
			if(pid!="N") {
				var pNode=nodeMap.get(pid);
				if (pNode!=undefined&&!pNode.childs.contain(spanid)) {
					pNode.childs.put(spanid,curNode);
				}
			}
		}
		
		var newdatas=[];
		
		/**
		 * 按M叉树先根遍历输出
		 */
		traceOut(newdatas,root);
		
		apmLoader.hide();
		
		return newdatas;
	};
	// TODO ---------------------------------访问查询服务---------------------------------------------
	
	/**
	 * 访问调用链查询服务
	 */
	this.callIVCQuery=function(intent,pObj) {
		
		var data={intent:"qApp",request:{}};
		
		var dataList;
		
		var appuuid;
		var winmode = '';
		if(this.appInfo!=undefined&&"winmode" in this.appInfo){
			winmode = this.appInfo["winmode"];
		}
		//全局模式
		if ("standalone"==winmode) {
			this.appInfo["appid"]=this.appSelector.value();
			this.appInfo["appuuid"]=this.appInstSelector.value();
			this.appInfo["appname"]=this.appSelector.selTitle();
			appuuid=this.appInfo["appuuid"];
		}
		else {
			appuuid=pObj["appuuid"];
		}
		
		switch(intent) {
			/**
			 * 完整查询 
			 */
			case "qsearch":
				dataList=this.mainList;
				
				var timeRange=_this.getCurTimeRange();
				
				if (timeRange==undefined) {
					var tmp=new Date().getTime();
					timeRange={etime:tmp,stime:tmp-60000};
				}
				
				var etime=timeRange["etime"];
				var stime=timeRange["stime"];
				data["request"]["appuuid"]=appuuid;
				data["request"]["stime"]=stime+"";
				data["request"]["etime"]=etime+"";
				data["request"]["eptype"]="E,S";
				data["request"]["from"]=0+"";
				data["request"]["size"]=500+"";
				data["request"]["indexdate"]=timeRange["indexdate"];
				
				var timeSort=this.timeSortSelector.value();
				if (timeSort=="DESC") {
					data["request"]["sort"]="stime=DESC";
				}
				else {
					data["request"]["sort"]="stime=ASC";
				}
				break;		   
			case "lst1min":
				dataList=this.mainList;
				
				var etime=new Date().getTime();
				var stime=etime-60000;
				data["request"]["appuuid"]=appuuid;
				data["request"]["stime"]=stime+"";
				data["request"]["etime"]=etime+"";
				data["request"]["eptype"]="E,S";
				data["request"]["from"]=0+"";
				data["request"]["size"]=100+"";
				break;
			case "lst100":
				dataList=this.mainList;
				
				var etime=new Date().getTime();
				var stime=etime-24*60*60000;
				data["request"]["appuuid"]=appuuid;
				data["request"]["stime"]=stime+"";
				data["request"]["etime"]=etime+"";
				data["request"]["eptype"]="E,S";
				data["request"]["from"]=0+"";
				data["request"]["size"]=100+"";
				break;
			case "slow100":
				dataList=this.mainList;
				
				var etime=new Date().getTime();
				var stime=etime-24*60*60000;
				data["request"]["appuuid"]=appuuid;
				data["request"]["stime"]=stime+"";
				data["request"]["etime"]=etime+"";
				data["request"]["eptype"]="E,S";
				data["request"]["from"]=0+"";
				data["request"]["size"]=100+"";
				data["request"]["sort"]="cost=DESC";
				break;
			case "slow100in1hr":
				dataList=this.mainList;
				
				var etime=new Date().getTime();
				var stime=etime-60*60000;
				data["request"]["appuuid"]=appuuid;
				data["request"]["stime"]=stime+"";
				data["request"]["etime"]=etime+"";
				data["request"]["eptype"]="E,S";
				data["request"]["from"]=0+"";
				data["request"]["size"]=100+"";
				data["request"]["sort"]="cost=DESC";
				break;
			case "trace":
				data["intent"]="qTrace";
				dataList=this.traceList;
				data["request"]["traceid"]=pObj["traceid"];
				data["request"]["from"]=0+"";
				data["request"]["size"]=1000+"";
				data["request"]["sort"]="spanid=ASC,stime=ASC,epinfo=ASC";
				break;
		}
		
		var dataStr=JSON.stringify(data);
		
		console.log("IVC REQ>> "+dataStr);
		
		AjaxHelper.call({
            url: '../../rs/apm/ivc/q',
            data: dataStr,
            cache: false,
            type: 'POST',
            dataType: 'html',
            timeout: 30000,
            success: function(result){
            	console.log("IVC RESP>> "+result);
            	
                var obj = StringHelper.str2obj(result);
                var res = obj["rs"];
                if (obj=="ERR"||res=="ERR") {
                    alert("调用链查询操作["+intent+"]失败:"+result);
                }
                else {
                    
                    var datas=eval(res);
                    var count=parseInt(obj["count"]);
                    if (intent=="trace") {
                    	apmLoader.show("调用链共"+datas.length+"记录，正生成分析树...",280);
                    	setTimeout(function() {
                    		datas=_this.sortTrace(intent,datas);
                    		_this.loadIVCList(dataList, datas, datas.length,count,pObj);
                    	},1);
                    }
                    else {
                    	_this.loadIVCList(dataList, datas, datas.length,count);
                    }
                }
            },
            error: function(result){
                alert("调用链查询操作["+intent+"]失败:" + result);
            }
        });
	};
	
	/**
	 * 访问重调用链查询服务
	 */
	this.callIVCDataQuery=function(intent,pObj) {
		
		var data={intent:intent,request:{}};
		var dataList;
		data["request"]["traceid"]=pObj["traceid"];
		data["request"]["spanid"]=pObj["spanid"];
		data["request"]["epinfo"]=pObj["epinfo"].split(",")[0];
		data["request"]["appid"]=pObj["appid"];
		
		var dataStr=JSON.stringify(data);
		
		console.log("IVC REQ>> "+dataStr);
		
		AjaxHelper.call({
            url: '../../rs/apm/ivcdata/q',
            data: dataStr,
            cache: false,
            type: 'POST',
            dataType: 'html',
            timeout: 30000,
            success: function(result){
            	console.log("IVC RESP>> "+result);
            	
                var obj = StringHelper.str2obj(result);
                var res = obj["rs"];
                if (obj=="ERR"||res=="ERR") {
                    alert("调用链详情查询操作["+intent+"]失败:"+result);
                }
                else {
                    
                    var data = eval(res);
                    _this.loadIVCData(data,pObj);
                }
            },
            error: function(result){
                alert("调用链详情查询操作["+intent+"]失败:" + result);
            }
        });
	};
	
	//TODO -----------------------------------------------调用链配置窗口------------------------------------------
	/**
	 * 调用链配置窗口初始化
	 */
	this.runIVCCfgWnd=function(sObj) {
		
	};
	
	//TODO -----------------------------------------------调用链时间控制------------------------------------------
	/**
	 * 改变时间区段的单位
	 */
	this.onChangeTimeRangeSelector=function() {
		
		var val=this.timeRangeSelector.value();
		
		if (val=="D") {
			$('.form_datetime').datetimepicker("remove");
			HtmlHelper.id("AppIVCWnd_TimeRange").value="";
			$('.form_datetime').datetimepicker({
				language : 'zh-CN',
				autoclose : true,
				minuteStep : 1,
				minView: 2,
				format:"yyyy-mm-dd",
				todayBtn : true
			});
		}
		else if (val=="H") {
			$('.form_datetime').datetimepicker("remove");
			HtmlHelper.id("AppIVCWnd_TimeRange").value="";
			$('.form_datetime').datetimepicker({
				language : 'zh-CN',
				autoclose : true,
				minuteStep : 1,
				minView: 1,
				format:"yyyy-mm-dd hh",
				todayBtn : true
			});
		}
		else if (val=="M") {
			$('.form_datetime').datetimepicker("remove");
			HtmlHelper.id("AppIVCWnd_TimeRange").value="";
			$('.form_datetime').datetimepicker({
				language : 'zh-CN',
				autoclose : true,
				minuteStep : 1,
				minView: 0,
				format:"yyyy-mm-dd hh:ii",
				todayBtn : true
			});
		}
		
	};
	
	/**
	 * 清除时间选择
	 */
	this.cleanTimeRange=function() {
		HtmlHelper.id("AppIVCWnd_TimeRange").value="";
	};
	
	/**
	 * 获取当前的时间区段
	 */
	this.getCurTimeRange=function() {
		
		var timeRange=HtmlHelper.id("AppIVCWnd_TimeRange").value;
		var timeUnit=this.timeRangeSelector.value();
		
		//全部时间
		if (timeRange=="") {
			return undefined;
		}
		
		var date;
		var plus;
		switch(timeUnit) {
		case "D":
			date=TimeHelper.toDate(timeRange+" 00:00:00");
			plus=24*3600*1000;
			break;
		case "H":
			date=TimeHelper.toDate(timeRange+":00:00");
			plus=3600*1000;
			break;
		case "M":
			date=TimeHelper.toDate(timeRange+":00");
			plus=60*1000;
			break;
		}
		
		var startTime=date.getTime();
		var endTime=startTime+plus;
		var reqDate=TimeHelper.getTime(startTime,"FD");
		
		return {stime:startTime,etime:endTime,indexdate:reqDate};
	};
	
	this.buildCtrlObj = function(appuuid){
		var ctrlObj={
				nodeURL:"",
				serverURL:"",
				file:"",
				appUUID:"",
				toolTag:"",
				mqTopic:"",
				collectAct:""
			};
			ctrlObj["appUUID"]=appuuid;
			var appInfo=appuuid.split("---");
			var urlInfo=appuuid.split("/");
			ctrlObj["serverURL"]=urlInfo[0]+"//"+urlInfo[2];
			var ipport=urlInfo[2].split(":");
			ctrlObj["nodeURL"]="http://"+ipport[0]+":10101/node/ctrl";
			ctrlObj["file"]=appInfo[1]+"_"+ipport[1]+"_";
		return ctrlObj;
	}
	
	this.ivcStart=function(appuuid) {
		var ctrlObj = this.buildCtrlObj(appuuid);
		ctrlObj["toolTag"]="ivc";
		ctrlObj["mqTopic"]="JQ_IVC";
		ctrlObj["collectAct"]="collectdata.add";
		ctrlObj["collectRoot"]="com.creditease.uav.invokechain.logroot";
		_this.callMOFTool("调用链", "start",ctrlObj,"startSupporter","[\"com.creditease.uav.apm.supporters.InvokeChainSupporter\"]");
	};
	
	this.ivcStop=function(appuuid) {
		var ctrlObj = this.buildCtrlObj(appuuid);
		ctrlObj["toolTag"]="ivc";
		ctrlObj["mqTopic"]="JQ_IVC";
		ctrlObj["collectAct"]="collectdata.del";
		ctrlObj["collectRoot"]="com.creditease.uav.invokechain.logroot";
		_this.callMOFTool("调用链", "stop",ctrlObj,"stopSupporter","[\"com.creditease.uav.apm.supporters.InvokeChainSupporter\"]");
	};
	
	this.ivcCfg=function(appuuid) {
		var ctrlObj = this.buildCtrlObj(appuuid);
		$("#MonitorConfigDialog").modal('hide');
		app.controller.showWindow({winmode:"embed",func:"ivc",appuuid:instID,appurl:appInfo[0],appid:appInfo[1]},"AppAPMCfgWnd","buildAppAPMCfgWnd","runAppAPMCfgWnd");
	};
	
	this.ivcDataStart=function(appuuid) {
		var ctrlObj = this.buildCtrlObj(appuuid);
		ctrlObj["toolTag"]="ivcdat";
		ctrlObj["mqTopic"]="JQ_SLW";
		ctrlObj["collectAct"]="collectdata.add";
		ctrlObj["collectRoot"]="com.creditease.uav.ivcdat.logroot";
		_this.callMOFTool("重调用链", "start",ctrlObj,"startSupporter","[\"com.creditease.uav.apm.supporters.SlowOperSupporter\"]");
	};
	
	this.ivcDataStop=function(appuuid) {
		var ctrlObj = this.buildCtrlObj(appuuid);
		ctrlObj["toolTag"]="ivcdat";
		ctrlObj["mqTopic"]="JQ_SLW";
		ctrlObj["collectAct"]="collectdata.del";
		ctrlObj["collectRoot"]="com.creditease.uav.ivcdat.logroot";
		_this.callMOFTool("重调用链", "stop",ctrlObj,"stopSupporter","[\"com.creditease.uav.apm.supporters.SlowOperSupporter\"]");
	};
	
	this.logTraceStart=function(appuuid) {
		var ctrlObj = this.buildCtrlObj(appuuid);
		ctrlObj["toolTag"]="logTrace";
		_this.callMOFTool("调用链日志追踪", "start",ctrlObj,"startSupporter","[\"com.creditease.uav.apm.supporters.LogTraceSupporter\"]");
	};
	
	this.logTraceStop=function(appuuid) {
		var ctrlObj = this.buildCtrlObj(appuuid);
		ctrlObj["toolTag"]="logTrace";
		_this.callMOFTool("调用链日志追踪", "stop",ctrlObj,"stopSupporter","[\"com.creditease.uav.apm.supporters.LogTraceSupporter\"]");
	};
	
	
}


