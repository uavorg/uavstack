/**
 * 新日志检索
 * @param app
 */
function NewLogTool(app) {
	
	var _this=this;
	
	this.appInfo={};
	
	this.LogInfo = {
		comeFrom : ""
	};
	
	/**
	 * TODO：日志搜索列表配置
	 */
	var mainListConfig = {
		id : "AppNewLogWnd_MainList",
		pid : "AppNewLogWnd_TContainer",
		openDelete : false,
		key : "l_timestamp",
		pagerSwitchThreshold : 600,
		pagesize : 100,
		caption:"<div align='center'></div>",
		deleteCtr : {
			key : "state",
			showDelete : "0"
		},
		head : {
			ipport:["应用实例","120px"],
			l_timestamp : [ '时间戳', '90px' ],
			l_num : [ '行号', '90px' ],
			content: [ '日志', '90%' ]		
		},
		cloHideStrategy : {
			1000 : [0, 1, 2,3 ],
			500 : [ 1, 2,3 ],
			300:[1,3],
		},
		events : {
			onRow : function(index, value) {
				
				switch(index) {
				case 0:
					  var info=value.split(":");
					  if (info.length==2) {
						  value="<font color='#009ad6'>"+info[0]+"</font><br/>端口&nbsp;"+info[1];
					  }
					  return value;
				case 1:
					return "<div style='word-break:break-all;word-wrap:break-word;white-space:normal;color:#009ad6;'>"+TimeHelper.getTime(value,"FMSN")+"</div>";
				case 2:
					return "<font color='#FF7F00'>"+value+"</font>";
				case 3:
					return "<div align='left' style='word-break:break-all;word-wrap:break-word;white-space:normal;'>"+value+"</div>";
				}
				
				return value;
			}
		}
	};
	//TODO -----------------------------------日志搜索窗口-----------------------------------------
	
	/**
	 * buildAppNewLogWnd
	 */
	this.buildAppNewLogWnd=function(sObj) {
		
		this.appInfo={};
		
		if (sObj!=undefined) {
			this.appInfo=sObj;
		}
		
		/**
		 * 应用实例模式
		 */
		if(this.appInfo["logid"]!=undefined) {
			//logtype
			var logInfo=this.appInfo["logid"].split("/");
			this.appInfo["logtype"]=logInfo[logInfo.length-1].replace(".","_");
			this.appInfo["logfile"]=logInfo[logInfo.length-1];
			//ipport
			var ipportInfo=sObj["appurl"].split("/");
			this.appInfo["ipport"]=ipportInfo[2];
		}
		
		var html="";
		
		var winmode=this.appInfo["winmode"];
		
		//钻取视图模式
		if (winmode!="standalone") {
			
			var sObjStr=JSON.stringify(sObj);
			
			html="<div class=\"appDetailContent\" style='background:#333;' >" +
	        "<div class=\"topDiv\" >" +
	        "<span class=\"tagTitle\">"+this.appInfo["appid"]+"</span><br/>"+
	        "<span class=\"idTitle\" >"+this.appInfo["logid"]+"</span>" +
	        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppNewLogWnd','destroyAppNewLogWnd','AppInstChartWnd')\"></div>" +
	        "<div class=\"glyphicon glyphicon-cog\" onclick='javascript:app.controller.showWindow("+sObjStr+",\"AppNewLogCfgWnd\",\"buildAppNewLogCfgWnd\",\"runAppNewLogCfgWnd\")'></div>"+	            
	        "</div></div>";
		}
		
		
		//钻取视图模式
		if (winmode!="standalone") {
			html+="<div class=\"AppHubMVCSearchBar NewLogSearchBar_AppMode\">";
			//html+= "<button id=\"searchbtn\" type=\"button\" class=\"btn btn-info\" onclick='appLog.callLogQuery(\"lst100\",{appid:\""+this.appInfo["appid"]+"\",logtype:\""+this.appInfo["logtype"]+"\"});'>最近100</button>&nbsp;"
		}
		//全局视图
		else {
			html+="<div class=\"AppHubMVCSearchBar NewLogSearchBar_GlobalMode\">"
			html+= "<div id=\"AppNewLogWnd_Selectors\">日志 </div>";
		}
		//公共的搜索按钮
	    html+="<div class=\"\">时间 <div class=\"\" style='display:inline-block;'>" 
	    		+	"<input id='AppNewLogWnd_TimeRange' type=\"text\" class=\"form_datetime\" style=\"width:100px;font-size:11px;height:27px;\" readonly placeholder='全部' title=\"选择时间区段\" />"
	    		+ "<span id='AppNewLogWnd_TimeRangeSelectorCtn'></span>"
	    		+ "<button type=\"button\" class=\"btn btn-default\" title=\"清除时间段，代表全部时间\" onclick='appLog.cleanTimeRange()'>"
				+ "<span class=\"glyphicon glyphicon-remove\"></span>"
				+ "</button>"
	            + "</div></div>"
	    	
	    	    + "<div>搜索 <input id=\"AppNewLogWnd_KeyWord\" class=\"form-control AppHubMVCSearchBarInputText\""
				+ " type=\"text\" title=\"1. 可输入多个关键字，用空格分隔，代表或连接，即任一关键字匹配\n2.多个关键字用+号连接，代表所有关键字匹配\n3.空格和+号可同时使用，或连接优先\n4. 可在关键字两头加*，代表启用模糊匹配，例如*com.creditease*，则所有包含com.creditease字符串都会被匹配\" placeholder=\"关键字\" value=\"\"></input>"
				
				+ "<div class=\"btn-group\">"
				+ "<button id=\"searchbtn\" type=\"button\" class=\"btn btn-default\" title=\"快速搜索\n无关键字默认搜索最近100条日志\" onclick='appLog.callLogQuery(\"qsearch\");'>"
				+ "<span class=\"glyphicon glyphicon-search\"></span>"
				+ "</button>"
				
//				+ "<button id=\"bestSearchbtn\" type=\"button\" class=\"btn btn-default\" title=\"设置日志搜索相关功能\" onclick=\"\">"
//				+ "<span class=\"glyphicon glyphicon-cog\"></span>"
//				+ "</button>"
//				+ "<button id=\"bestSearchbtn\" type=\"button\" class=\"btn btn-default\" title=\"打开帮助\" onclick=\"\">"
//				+ "<span class=\"glyphicon glyphicon-question-sign\"></span>"
//				+ "</button>"
				+ "</div></div>";
		html+="</div>";
		//日志内容
		html+="<div  id='AppNewLogWnd_TContainer' style='font-size:12px;color:black;'></div>";
		
		return html;
	};
	
	/**
	 * runAppNewLogWnd
	 */
	this.runAppNewLogWnd=function(sObj) {

		//全局视图，需要读取该用户可以查看的应用集群
		var winmode=this.appInfo["winmode"];
		
		if (winmode=="standalone") {
			//init selectors
			this.appSelector=new AppHubSelector({
				id:"AppNewLogWnd_AppSelector",
				cid:"AppNewLogWnd_Selectors",
				title:"请选择应用集群",
				style:"font-size:12px;width:120px",
				events:{
					onchange:"appLog.onChangeAppSelector()"
				}
			});
			
			this.ipSelector=new AppHubSelector({
				id:"AppNewLogWnd_IPSelector",
				cid:"AppNewLogWnd_Selectors",
				title:"请选择应用实例",
				style:"font-size:12px;width:120px"
			});
			
			this.logSelector=new AppHubSelector({
				id:"AppNewLogWnd_LogSelector",
				cid:"AppNewLogWnd_Selectors",
				title:"请选择日志文件",
				style:"font-size:12px;width:110px"
			});
			
			this.appSelector.init();
			this.ipSelector.init();
			this.logSelector.init();
			_this.callAppProfile(sObj);
		}
		
		
		
		//init search list
		this.mainList= new AppHubTable(mainListConfig);
		
		this.mainList.initTable();
		
		/**
		 * 点击打开日志滚动窗口
		 */
		this.mainList.cellClickUser = function(id,pNode) {
			app.controller.showWindow({l_timestamp:id, node:pNode , comeFrom:"NewLog",logfile:appLog.appInfo["logfile"]},"AppNewLogRollWnd","buildAppNewLogRollWnd","runAppNewLogRollWnd");
		};
		
		/**
		 * 分页事件
		 */
		this.mainList.sendRequest=function() {
			
			var pInfo=_this.mainList.getPagingInfo();
			
			console.log("%o",pInfo);
			
			var start=(pInfo.pageNum-1)*pInfo.pageSize;
			
			_this.callLogQuery("qsearch",{from:start,size:pInfo.pageSize});
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
				id:"AppNewLogWnd_TimeRangeSelector",
				cid:"AppNewLogWnd_TimeRangeSelectorCtn",
				title:"时间单位",
				style:"font-size:12px;width:50px",
				events:{
					onchange:"appLog.onChangeTimeRangeSelector()"
				}
			});
		 this.timeRangeSelector.init();
		 this.timeRangeSelector.load([{title:"日期",value:"D"},{title:"小时",value:"H",select:true},{title:"分钟",value:"M"}]);
		 
		 //init time sort selector
		 this.timeSortSelector=new AppHubSelector({
				id:"AppNewLogWnd_TimeSortSelector",
				cid:"AppNewLogWnd_TimeRangeSelectorCtn",
				title:"时间排序方式",
				style:"font-size:12px;width:50px"
			});
		 this.timeSortSelector.init();
		 this.timeSortSelector.load([{title:"降序",value:"DESC", select:true},{title:"升序",value:"ASC"}]);
		 
	};
	
	//TODO -----------------------------------------------日志搜索API---------------------------------------------------------------
	
	/**
	 * 改变时间区段的单位
	 */
	this.onChangeTimeRangeSelector=function() {
		
		var val=this.timeRangeSelector.value();
		
		if (val=="D") {
			$('.form_datetime').datetimepicker("remove");
			HtmlHelper.id("AppNewLogWnd_TimeRange").value="";
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
			HtmlHelper.id("AppNewLogWnd_TimeRange").value="";
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
			HtmlHelper.id("AppNewLogWnd_TimeRange").value="";
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
	 * 获取当前的时间区段
	 */
	this.getCurTimeRange=function() {
		
		var timeRange = "";
		if(HtmlHelper.id("AppNewLogWnd_TimeRange")!=undefined){
			timeRange=HtmlHelper.id("AppNewLogWnd_TimeRange").value;
		}
		
		//全部时间
		if (timeRange=="") {
			return undefined;
		}
		var timeUnit=this.timeRangeSelector.value();
		
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
	
	/**
	 * getCurTimeRangeText
	 */
	this.getCurTimeRangeText=function() {
		
		var timeRange=HtmlHelper.id("AppNewLogWnd_TimeRange").value;
		
		if (timeRange=="") {
			return "全部";
		}
		
		return timeRange;
	};
	
	/**
	 * 清除时间选择
	 */
	this.cleanTimeRange=function() {
		HtmlHelper.id("AppNewLogWnd_TimeRange").value="";
	};
	
	/**
	 * 加载MAINList的数据，显示数据
	 */
	this.loadLogList = function(list,datas,count,totalCount) {
	
		list.clearTable();
		list.setCaption("<div align='center'>应用:<font color='#333'>"+this.appInfo["appid"]+"</font>，文件:<font color='#333'>"+this.appInfo["logfile"]+"</font>，时间:<font color='#333'>"+this.getCurTimeRangeText()+"</font>，总数:<font color='red'>"+totalCount+"</font>，排序:<font color='#333'>时间降序</font></div>");
		// 必须先显示分页
		list.setTotalRow(totalCount);
		list.renderPagination();
		
		$.each(datas, function(index, obj) {
			var keywords=_this.getHighlightKeywords();
			obj["content"]=_this.rendLog(obj["content"], keywords);
		});
		
		list.addRows(datas);
		
	};
	
	/**
	 * 对每行日志进行渲染
	 * 1. 将<>转换成HTML encoding 便于显示
	 * 2. 高亮关键字，如果有
	 */
	this.rendLog=function(content,keywords) {
		
		//去除调用链的traceId
		if(content.slice(0,4)=='uav_'){
			var traceid = content.split(" ")[0].slice(4);
			content = content.substring(traceid.length+4,content.length);
		}
		if (keywords==undefined||keywords.length==0) {
			return content.replace(/</g,"&lt;").replace(/>/g,"&gt;");
		}
		
		content=content.replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g, "&quot;").replace(/'/g, "&apos;");
		
		for(var i=0;i<keywords.length;i++) {
			
			if(keywords[i] == ""){
				continue;
			}
			var keyword=keywords[i];
						
			var toSplit=undefined;
			if (content.indexOf(keyword)>-1) {
				toSplit=keyword;
			}
			else
			if (content.indexOf(keyword.toUpperCase())>-1) {
				toSplit=keyword.toUpperCase();
			}
			else
			if (content.indexOf(keyword.toLowerCase())>-1) {
				toSplit=keyword.toLowerCase();
			}
			
			if (toSplit==undefined) {
				continue;
			}
			
			var lines=content.split(toSplit);

			content=lines.join("<span class='LogHighLightKeyword'>"+toSplit+"</span>");
		}
		
		return content;
	};
	
	/**
	 * 关键字
	 * 如果用空格分开多个关键字，则默认为或的关系
	 * 
	 * 如果某些关键字是以+连接，代表与关系
	 * 
	 * 注意这里只支持或优先的操作
	 * 
	 * 举例：
	 * <kwd1>+<kwd2> <kwd3> <kwd4>
	 * 
	 * 则代表的是意思是（kwd1 and kwd2 同时存在）或kwd3存在或kwd4存在
	 * 
	 */
	/**
	 * 获取关键字给服务段处理
	 */
	this.getKeyword=function() {
		
		var keys="";
		
		if (HtmlHelper.id("AppNewLogWnd_KeyWord")!=undefined&&HtmlHelper.id("AppNewLogWnd_KeyWord").value!="") {
			keys=HtmlHelper.id("AppNewLogWnd_KeyWord").value;
		}
		
		return keys;
	}
	
	/**
	 * 高亮显示的关键字
	 */
	this.getHighlightKeywords=function() {
		
		var keywords=[];
		
		/**
		 * 快速搜索
		 */
		var keys=this.getKeyword();		
		
		if (keys=="") {
			return keywords;
		}	

		var orkwds=keys.split(" ");
		
		for(var i=0;i<orkwds.length;i++) {
			
			var andkwds=orkwds[i].split("+");
			
			for(var j=0;j<andkwds.length;j++) {
				
				keywords[keywords.length]=andkwds[j];
			}
		}
		
		return keywords;
	};
	
	/**
	 * 访问日志查询服务: 搜索
	 */
	this.callLogQuery=function(intent,cursor) {
		
		var data={intent:"qContent",request:{}};
		
		var dataList;
		/**
		 * Step 1: 取快速查询的关键字
		 */
		var keyword=this.getKeyword();
		
		/**
		 * Step 2: 默认使用_def, 但是稍后可以允许用户自己选自己的Rule
		 */
		var currentLogRuleName="_def";
		
		//全局模式
		var winmode=this.appInfo["winmode"];
		
		if(winmode=="standalone") {
			var pObj=this.getAppLogSelectValue();
			
			if (pObj["logtype"]=="none") {
				alert("抱歉没有可搜索的日志文件！");
				return;
			}
			
			this.appInfo["appid"]=pObj["appid"];
			this.appInfo["logtype"]=pObj["logtype"].replace(".","_");
			this.appInfo["logfile"]=pObj["logtype"];
			if (pObj["ipport"]!=undefined) {
				this.appInfo["ipport"]=pObj["ipport"];
			}
			else {
				this.appInfo["ipport"]=undefined;
			}
		}
		
		/**
		 * Step 3: 检查是否有ipport
		 */
		var ipport="";
		
		if (this.appInfo["ipport"]!=undefined) {
			ipport=this.appInfo["ipport"];
		}
		
		//取appid和logtype
		data["request"]["appid"]=this.appInfo["appid"];
		data["request"]["logtype"]=this.appInfo["logtype"]+currentLogRuleName;	
		
		/**
		 * Step 4: 时间区段
		 */
		var timeRange=this.getCurTimeRange();
		
		/**
		 * Step 5: 排序方式
		 */
		var timeSort=this.timeSortSelector.value();
		
		switch(intent) {
			case "qsearch":
				dataList=this.mainList;
				if (keyword!="") {
					data["request"]["ctn"]=keyword;
				}
				
				if (ipport!="") {
					data["request"]["ipport"]=ipport;
				}
				
				if (timeRange!=undefined) {
					data["request"]["indexdate"]=timeRange["indexdate"]+"";
					data["request"]["stime"]=timeRange["stime"]+"";
					data["request"]["etime"]=timeRange["etime"]+"";
				}
				
				if (timeSort=="DESC") {
					data["request"]["sort"]="l_timestamp=DESC,l_num=DESC";
				}
				else {
					data["request"]["sort"]="l_timestamp=ASC,l_num=ASC";
				}
				
				if (cursor==undefined) {
					data["request"]["from"]=0+"";
					data["request"]["size"]=100+"";
				}
				else {
					data["request"]["from"]=cursor.from+"";
					data["request"]["size"]=cursor.size+"";
				}
				break;
		}
		
		var dataStr=JSON.stringify(data);
		
		console.log("NEWLOG REQ>> "+dataStr);
		
		AjaxHelper.call({
            url: '../../rs/godeye/newlog/q',
            data: dataStr,
            cache: false,
            type: 'POST',
            dataType: 'html',
            timeout: 30000,
            success: function(result){
            	console.log("NEWLOG RESP>> "+result);
                var obj = StringHelper.str2obj(result);
                var res = obj["rs"];
                if (obj=="ERR"||res=="ERR") {
                    alert("日志查询操作["+intent+"]失败:"+result);
                }
                else {
                    if (res==undefined) {
                    	alert("没有搜索到该日志相关内容，可能原因：请检查日志归集是否已经开启！");
                    	return;
                    }
                    
                    var datas=eval(res);
                    var totalCount=parseInt(obj["count"]);
                    _this.loadLogList(dataList, datas, datas.length,totalCount);
                }
            },
            error: function(result){
                alert("日志查询操作["+intent+"]失败:" + result);
            }
        });
	};
	
	//TODO ---------------------------------------------------全局视图专有------------------------------------------------------------
	
	this.profileDAO=new UAVProfileDAO();
	/**
	 * 全局视图需要单独取该用户可查看的profile信息
	 */
	this.callAppProfile=function(sObj) {
		this.profileDAO.callAppProfile(function(jsonData) {
			_this.loadAppSelector(jsonData);
			if(sObj != undefined){
				if(sObj["appid"]){
					$("select#AppNewLogWnd_AppSelector").val(sObj["appid"]); 
					appLog.onChangeAppSelector();
				}
				if(sObj["ipport"]){
					$("select#AppNewLogWnd_IPSelector").val(sObj["ipport"]);
				}
				if(sObj["logfile"]){
					$("select#AppNewLogWnd_LogSelector").val(sObj["logfile"]);
				}
				if(sObj["ctn"]){
					$("#AppNewLogWnd_KeyWord").val("\"" + sObj["ctn"] + "\"");
				}
			}
		});
	};
	
	/**
	 * onChangeAppSelector
	 */
	this.onChangeAppSelector=function() {
		this.loadIPSelector();
		this.loadLogSelector();
	};
	
	/**
	 * loadAppSelector
	 */
	this.loadAppSelector=function(jsonData) {
		
		var collectObj=this.profileDAO.loadAppSelector(jsonData, ["apploglist","appips"], function(id,map,data){
			/**
			 * 收集IPPORT
			 */
			if (id=="appips") {
				var ipports=map.get(data["appid"]);
				
				if (!map.contain(data["appid"])) {
					ipports=new Set();				
					map.put(data["appid"],ipports);
				}
				
				if (data["appurl"]!=undefined) {
					ipports.add(data["appurl"].split("/")[2]);
				}
			}
			/**
			 * 收集log
			 */
			else if (id=="apploglist") {
				
				var logProfileStr=data["logs.log4j"];
				
				if (logProfileStr==undefined||logProfileStr=="") {
					return;
				}
				
				var loglist=map.get(data["appid"]);
				
				if (!map.contain(data["appid"])) {
					loglist=new Set();				
					map.put(data["appid"],loglist);
				}
				
				//logid
				var logProfile=eval("("+logProfileStr+")");
				
				for(var logid in logProfile) {
					
					var logInfo=logid.split("/");
					var logFile=logInfo[logInfo.length-1];
					
					loglist.add(logFile);
				}	
			}
			
		});
		
		var datas=collectObj["options"];
		this.appInfo["apploglist"]=collectObj["info"]["apploglist"];
		this.appInfo["appips"]=collectObj["info"]["appips"];
		
		this.appSelector.load(datas);
		this.loadIPSelector();
		this.loadLogSelector();
	};
	
	/**
	 * loadIPSelector
	 */
	this.loadIPSelector=function() {
		
		var selAppID=this.appSelector.value();
		
		if (selAppID==undefined) {
			return;
		}
		
		var ipports=this.appInfo["appips"].get(selAppID);
		
		var options=[];
		
		options[options.length]={title:"全部实例",value:"all",select:true};
		
		for(var i=0;i<ipports.count();i++) {
			var option={};
			option["title"]=ipports.get(i);
			option["value"]=ipports.get(i);
			
			options[options.length]=option;
		}
				
		this.ipSelector.load(options);
	};
	
	/**
	 * loadLogSelector
	 */
	this.loadLogSelector=function() {
		
		var selAppID=this.appSelector.value();
		
		if (selAppID==undefined) {
			return;
		}
		
		var loglist=this.appInfo["apploglist"].get(selAppID);
		
		var options=[];
		
		for(var i=0;i<loglist.count();i++) {
			var option={};
			option["title"]=loglist.get(i);
			option["value"]=loglist.get(i);
			
			options[options.length]=option;
		}
		
		if (options.length==0) {
			options[options.length]={title:"无日志文件",value:"none",select:true};
		}
		
		this.logSelector.load(options);
	};
	
	/**
	 * 获得选择那个应用集群和那个日志
	 */
	this.getAppLogSelectValue=function() {
		var appid=this.appSelector.value();
		var logtype=this.logSelector.value();
		var ipport=this.ipSelector.value();
		
		var pObj={appid:appid,logtype:logtype};
		
		if (ipport!="all") {
			pObj["ipport"]=ipport;
		}
		
		return pObj;
	};
	
	// TODO ----------------------------- 日志滚动窗口------------------------------
	/**
	 * 滚动窗口的元数据对象
	 */
	this.logRollInfo={};	
	/**
	 * 创建日志滚动窗口
	 */
	this.buildAppNewLogRollWnd=function(sObj) {
		
		this.logRollInfo={
				//每次滚动的大小
				rollsize:100,
				//用于存储本地页面的pod，每个pod里面包含一组日志，最大size为rollsize
				pods:new List(),
				//pod的最大个数，超过个数就要移除pod
				podMax:10,
				//保存当前已经下载的日志最大范围
				curSLine:999999999999,
				curELine:0,
				_lock:false
		};
		if(sObj!=undefined&&"ipport" in sObj&&"appid" in sObj&&"logfile" in sObj&&"ctn" in sObj){
			this.logRollInfo["ipport"]=sObj.ipport;
			this.logRollInfo["appid"]=sObj.appid;
			this.logRollInfo["logtype"]=sObj.logfile;
			this.logRollInfo["ctn"]=sObj.ctn;
		}else{
			this.logRollInfo["ipport"]=sObj["node"].getElementsByTagName("td")[0].id;
			this.logRollInfo["appid"]=this.appInfo["appid"];
			this.logRollInfo["logtype"]=this.appInfo["logtype"];
		}
		console.log(this.logRollInfo);
		/**
		 * 记录从那个时间戳以及哪行进来的
		 */
		this.logRollInfo["entry"]={
				timestamp:sObj["l_timestamp"],
				line:(sObj["node"]!=undefined)?parseInt(sObj["node"].getElementsByTagName("td")[2].id):''
		};
				
		var html="<div id=\"AppNewLogRollWnd_Top\" class=\"appDetailContent\" style='background:#333;' >" +
        "<div class=\"topDiv\" >" +
        "<span class=\"tagTitle\">"+this.logRollInfo["appid"]+"("+this.logRollInfo["ipport"]+")</span><br/>"+
        "<span class=\"idTitle\" >"+sObj["logfile"]+"</span>";
        if(sObj!=undefined&&"comeFrom" in sObj){
        	this.LogInfo["comeFrom"] = sObj["comeFrom"];
        	if(sObj["comeFrom"] == "IVC"){
        		html +=
        			"<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppNewLogRollWnd','destroyAppNewLogRollWnd','AppIVCTraceWnd')\"></div>" +
        			"<div class=\"icon-link\" style=\"cursor:pointer\" title=\"转到日志搜索\" onclick=\"app.controller.showWindow({'winmode':'standalone','logfile':'"+sObj["logfile"]+"','appid':'"+sObj["appid"]+"','ctn':'"+sObj["ctn"].replace("\"","").replace("\"","")+"\','ipport':'"+sObj["ipport"]+"'},'AppNewLogWnd','buildAppNewLogWnd','runAppNewLogWnd');\"></div>";
        	}else{
            	html+=
            		"<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppNewLogRollWnd','destroyAppNewLogRollWnd','AppNewLogWnd')\"></div>";
            }
        }else{
        	html+=
        		"<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppNewLogRollWnd','destroyAppNewLogRollWnd','AppNewLogWnd')\"></div>";
        }
		//控制面板
//		html+="<div class=\"AppHubMVCSearchBar\" style=\"padding-top:0px;height:40px;\">";
//		html+="</div>";
		//向前滚动
		html+="</div></div><div id='NewLogRollButton_UP' class=\"btn btn-success NewLogRollButton\" onclick='appLog.callLogRollQuery(\"qroll\",{action:\"up\"})'>";
		html+="<span class=\"glyphicon glyphicon-chevron-up\"></span>向前滚动日志&nbsp;<span id=\"AppNewLogRollWnd_CurSLine\" style='font-size:12px;color:blue;'></span></div>";
		//日志内容
		html+="<div  id='AppNewLogRollWnd_TContainer' class=\"NewLogRollContentCtn\" ></div>";
		//向后滚动
		html+="<div id='NewLogRollButton_DOWN'  class=\"btn btn-success NewLogRollButton\"  onclick='appLog.callLogRollQuery(\"qroll\",{action:\"down\"})'>";
		html+="<span class=\"glyphicon glyphicon-chevron-down\"></span>向后滚动日志&nbsp;<span id=\"AppNewLogRollWnd_CurELine\" style='font-size:12px;color:blue;'></span></div>";
		return html;
	};
	
	/**
	 * 初始化日志滚动窗口
	 */
	this.runAppNewLogRollWnd=function(sObj) {
		
		var wndHeight=parseInt(HtmlHelper.id("AppNewLogRollWnd").style.height);
		
		this.onResizeAppRollWnd(0, wndHeight);
		
		this.callLogRollQuery("qroll",{line:this.logRollInfo["entry"]["line"]});
	};
	
	/**
	 * 日志滚动窗口随窗口高度变化而变化
	 */
	this.onResizeAppRollWnd=function(w,h,noTopHeight) {
		
		try {
			console.log(w+" "+h+" "+noTopHeight);
			var logCtn=HtmlHelper.id("AppNewLogRollWnd_TContainer");
			var deta=HtmlHelper.height("AppNewLogRollWnd_Top",false);
			
			//日志滚动区域的真是高度
			logCtn.style.height=h-deta-0-30*2+"px";
			
			if (w>0) {
				logCtn.style.width=w+"px";
			}
		}catch(e) {}
	};
	
	// TODO--------------------------------------------日志滚动视图-------------------------------------------------------
	/**
	 * 读取要滚动的日志数据
	 */
	this.loadRollLog=function(datas,lineRange,action) {
		
		if (datas.length==0) {
			alert("没有查询到任何日志");
			return;
		}

		/**
		 * Step 1：创建LogPod
		 */
		this.buildLogPod(datas,action);
		
		/**
		 * Step 2: 记录这次成功的日志的开始和结束
		 */
		if(lineRange.sline==undefined||this.logRollInfo["curSLine"]>=lineRange.sline) {
			this.logRollInfo["curSLine"]=datas[0]["l_num"];
			HtmlHelper.id("AppNewLogRollWnd_CurSLine").innerHTML=this.logRollInfo["curSLine"];
		}
		
		if (lineRange.eline==undefined||this.logRollInfo["curELine"]<=lineRange.eline) {
			this.logRollInfo["curELine"]=datas[datas.length-1]["l_num"];
			HtmlHelper.id("AppNewLogRollWnd_CurELine").innerHTML=this.logRollInfo["curELine"];
		}
	
		
	};
	
	/**
	 * 创建LogPod
	 */
	this.buildLogPod=function(datas,action) {
		
		var podID=datas[0]["l_num"];
		
		/**
		 * 达到Pod上限时候，要根据是向前还是向后，删除对应的pod，并改变curSLine或curELine的值
		 * 这样做是避免浏览器被撑爆
		 * 默认给予用户1000条日志的空间，10个LogPod，每个100条日志，常规来讲够用了
		 */
		var podNum=this.logRollInfo.pods.count();
		
		if (podNum==this.logRollInfo.podMax) {
			
			if (action==undefined||action=="down") {
				var podSLine=this.logRollInfo.pods.get(0);
				this.logRollInfo.pods.remove(0);
				HtmlHelper.del("NewLogPod_"+podSLine);
				if(this.logRollInfo["fromIVC"] == true){
					this.logRollInfo["curSLine"] = this.logRollInfo.pods.get(0);
				}else{
					this.logRollInfo["curSLine"]=podSLine+this.logRollInfo["rollsize"]+1;
				}
				HtmlHelper.id("AppNewLogRollWnd_CurSLine").innerHTML=this.logRollInfo["curSLine"];
			}
			else {
				var podSLine=this.logRollInfo.pods.getLast();
				this.logRollInfo.pods.remove(this.logRollInfo.pods.count()-1);
				HtmlHelper.del("NewLogPod_"+podSLine);
				this.logRollInfo["curELine"]=podSLine-1;
				HtmlHelper.id("AppNewLogRollWnd_CurELine").innerHTML=this.logRollInfo["curELine"];
			}
		}
		
		var sb=new StringBuffer();
		
		sb.append("<div id=\"").append("NewLogPod_"+podID).append("\">");
		
		for(var i=0;i<datas.length;i++) {
			var data=datas[i];
			
			this.buildLogLine(sb,data);
		}
		
	    sb.append("</div>");
	    
	    var logCtn=HtmlHelper.id("AppNewLogRollWnd_TContainer");
	    
	    if (action==undefined||action=="down") {
	    	logCtn.innerHTML+=sb.toString();
	    	 //save podid
		    this.logRollInfo.pods.add(podID);
		    
		    if(action==undefined) {
		    	this.scrollRollLog(undefined);
		    }
		    else {
		    	this.scrollRollLog(true);
		    }
	    }
	    else {
	    	logCtn.innerHTML=sb.toString()+logCtn.innerHTML;
	    	 //save podid
		    this.logRollInfo.pods.insert(0, podID);
		    
		    this.scrollRollLog(false);
	    }
	};
	
	/**
	 * 创建日志行
	 */
	this.buildLogLine=function(logBuffer,data) {
		
		var content=data["content"];
		//为拥有traceId的日志添加超链接
		if(content.slice(0,4)=='uav_'){
			var traceid = content.split(" ")[0].slice(4);
			content = content.substring(traceid.length+4,content.length);
			var params = {};
			params.traceid = traceid;
			params.appuuid = "http://"+this.logRollInfo["ipport"]+"/"+this.logRollInfo["appid"]+"---"+this.logRollInfo["appid"];
			params.comeFrom = this.LogInfo["comeFrom"];
			params.appname = this.logRollInfo["appid"];
			params.ipport = this.logRollInfo["ipport"];
			params.appid = this.logRollInfo["appid"];
			params = JSON.stringify(params);
			var keywords=this.getHighlightKeywords();
			content=this.rendLog(content, keywords);
			content = "<a href='javascript:void(0)' onclick='app.controller.showWindow("+params+",\"AppIVCTraceWnd\",\"buildAppIVCTraceWnd\",\"runAppIVCTraceWnd\")'>"+content+"</a>";
		}else{
			var keywords=this.getHighlightKeywords();
			content=this.rendLog(content, keywords);
		}
		
		
		var lnum=data["l_num"];
		var highlight="";
		if (lnum==this.logRollInfo["entry"].line) {
			highlight="background:yellow;"
		}
		
		logBuffer.append("<div id='NewLogLine_"+lnum+"' align='left' style='height:18px;"+highlight+"'>")
		//行号
		.append("<span class='NewLogRollContentNum'>").append(data["l_num"]).append("</span>")
		//日志内容
		.append("<div class='NewLogRollContentLog'>").append(content).append("</div></div>");
	};
	
	/**
	 * 滑动日志视图
	 */
	this.scrollRollLog=function(isDown) {
		var logCtnWndObj=HtmlHelper.id("AppNewLogRollWnd_TContainer");
		if (isDown==undefined) {
			var top=HtmlHelper.id("NewLogLine_"+this.logRollInfo["entry"].line).offsetTop;
			logCtnWndObj.scrollTop=top-16*5;
		}
		else	if (isDown==true) {
			var scrollY=HtmlHelper.height("AppNewLogRollWnd_TContainer",true);	
			logCtnWndObj.scrollTop=scrollY;
		}
		else if (isDown==false) {
			logCtnWndObj.scrollTop=0;
		}
	};
	
	// TODO ------------------------------------------ 日志滚动API---------------------------------------------------------
	/**
	 * 滚动日志范围的控制，按行进行计算，根据rollsize计算，一次滚动多少条
	 */
	this.getNextRollRange=function(line,action) {
		
		var rollsize=this.logRollInfo["rollsize"];
		
		/**
		 * 用行定位滚动区域
		 */
		if (line!=undefined) {
			var lineNum=parseInt(line);
			
			var pNum=rollsize/2;
			/**
			 * 把定位行放在中间，由于rollsize是偶数，所以定位行放在前半边的最后一个，而后半边要+1，因为加1的那行不会被查询出来
			 */
			var initElline=lineNum+pNum+1;
			
			var initSline=lineNum-(pNum-1);
				
			return {sline:initSline,eline:initElline};
		}
		
		var sline;
		var eline;
		
		if (action=="up") {
			eline=this.logRollInfo["curSLine"];
			sline=eline-rollsize-1;	
			//如果从调用链界面进来，则向上翻页的时候不用sline
			if(this.logRollInfo["fromIVC"] == true){
				sline = NaN;
			}
		}
		else if (action=="down") {
			sline=this.logRollInfo["curELine"]+1;
			eline=sline+rollsize+1;
			//如果从调用链界面进来，则向下翻页的时候不用eline
			if(this.logRollInfo["fromIVC"] == true){
				eline = NaN;
			}
		}
		
		return {sline:sline,eline:eline};
	};
	
	/**
	 * 访问日志查询服务: 搜索
	 */
	this.callLogRollQuery=function(intent,pObj) {
		
		if (this.logRollInfo["_lock"]==true) {
			alert("请勿频繁点击");
			return;
		}
		
		this.logRollInfo["_lock"]=true;
		
		var data={intent:"qContent",request:{}};
		/**
		 * Step 1: 默认使用_def, 但是稍后可以允许用户自己选自己的Rule
		 */
		var currentLogRuleName="_def";
		
		//取appid和logtype,ipport
		data["request"]["appid"]=this.logRollInfo["appid"];
		data["request"]["logtype"]=this.logRollInfo["logtype"]+currentLogRuleName;
		data["request"]["ipport"]=this.logRollInfo["ipport"];
		if(this.logRollInfo["ctn"] != undefined && this.logRollInfo["ctn"] != "");{
			data["request"]["ctn"]=this.logRollInfo["ctn"];
		}
		//第一次进入页面的line为“ ”说明是从IVC界面进来的
		if(this.logRollInfo["entry"]["line"] == ""){
			this.logRollInfo["fromIVC"] = true;
		}
		/**
		 * Step 2: 定位拉去的行范围，按行号
		 */
		var lineRange;
		if (pObj["line"]!=undefined) {
			lineRange=this.getNextRollRange(pObj["line"]);
		}
		else {
			lineRange=this.getNextRollRange(undefined,pObj["action"]);
		}
		
		if(!isNaN(lineRange["sline"])){
			data["request"]["sline"]=lineRange["sline"]+"";
		}
		if(!isNaN(lineRange["eline"])){
			data["request"]["eline"]=lineRange["eline"]+"";
		}
		
		/**
		 * Step 3: 时间区段
		 */
		var timeRange=this.getCurTimeRange();
		
		switch(intent) {
			case "qroll":
				if (timeRange!=undefined) {
					data["request"]["indexdate"]=timeRange["indexdate"]+"";
					data["request"]["stime"]=timeRange["stime"]+"";
					data["request"]["etime"]=timeRange["etime"]+"";
				}else{
					 //如果在日志搜索时没有选择时间区间，则在newLogRoll窗口中将仅通过sline和eline限定，此时会将符合行号范围的历史数据全部加载出来，
					 //为了避免此种情况这里设定一个时间区间，将日志范围限制到当日0点到现在区间，这里仅是一个workaround 
					if(this.logRollInfo["fromIVC"] != true){
						data["request"]["stime"]=new Date().setHours(0,0,0,0)+"";
						data["request"]["etime"]=new Date().getTime()+"";
					}
				}
				data["request"]["sort"]="l_timestamp=ASC,l_num=ASC";
				data["request"]["from"]=0+"";
				data["request"]["size"]=1000+"";
				//如果当前页面来源于IVC那么通过pagesize控制拉去日志的数量
				if(this.logRollInfo["fromIVC"] == true){
					var traceid = data["request"]["ctn"];
					var indexdate = TimeHelper.getTime(Number(traceid.split('_')[3]),"FD");
					data["request"]["indexdate"] = indexdate;
					data["request"]["size"]=100+"";
				}
				break;
		}
		
		var dataStr=JSON.stringify(data);
		
		console.log("NEWLOG ROLL REQ>> "+dataStr);
		
		AjaxHelper.call({
            url: '../../rs/godeye/newlog/q',
            data: dataStr,
            cache: false,
            type: 'POST',
            dataType: 'html',
            timeout: 30000,
            success: function(result){
            	console.log("NEWLOG ROLL RESP>> "+result);
                var obj = StringHelper.str2obj(result);
                var res = obj["rs"];
                if (obj=="ERR"||res=="ERR") {
                	_this.logRollInfo["_lock"]=false;
                    alert("日志查询操作["+intent+"]失败:"+result);
                }
                else {
                    
                    
                    if (res==undefined) {
                    	_this.logRollInfo["_lock"]=false;
                    	alert("没有搜索到该日志相关内容，可能原因：请检查日志归集是否已经开启！");
                    	return;
                    }
                    
                    var datas=eval(res);
                    try {
                    	if (datas.length > 0) {
	                    	if(isNaN(lineRange.sline)){
	                    		lineRange.sline = datas[0].l_num;
	                    	}
	                    	if(isNaN(lineRange.eline)){
	                    		lineRange.eline = datas[datas.length - 1].l_num;
	                    	}
	                    	if(_this.logRollInfo["entry"]["line"] == ""){
	                    		_this.logRollInfo["entry"]["line"] = datas[0].l_num;
	                    	}
                    	}
                    	_this.loadRollLog(datas,lineRange,pObj["action"]);
                    }catch(e) {
            		
                    }
                    
                    _this.logRollInfo["_lock"]=false;
                }
            },
            error: function(result){
            	_this.logRollInfo["_lock"]=false;
                alert("日志查询操作["+intent+"]失败:" + result);
            }
        });
	};
	
	// TODO -------------------------------日志配置窗口---------------------------------------------------
	//日志配置信息
	this.logCfgInfo;
	
	/**
	 * 创建日志配置窗口GUI
	 */
	this.buildAppNewLogCfgWnd=function(sObj) {
		
		this.logCfgInfo={};
		
		if (sObj!=undefined) {
			this.logCfgInfo=sObj;
		}
				
		html="<div class=\"appDetailContent\" style='background:#333;' >" +
        "<div class=\"topDiv\" >" +
        "<span class=\"tagTitle\">日志配置</span><br/>"+
        "<span class=\"idTitle\" >"+this.logCfgInfo["logid"]+"</span>" +
        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppNewLogCfgWnd','destroyAppNewLogCfgWnd','AppNewLogWnd')\"></div>" +	            
        "</div></div>";
		
		html+="<div class=\"AppHubMVCSearchBar\" align='left'>"
			    +"&nbsp;<button  class=\"btn btn-primary\" style=\"width:80px;\" onclick='appLog.enableLogCatch(true)'>启动归集</button>"
			    +"&nbsp;<button  class=\"btn btn-danger\" style=\"width:80px;\"  onclick='appLog.enableLogCatch(false)'>关闭归集</button>"
		        +"</div>";
		
		return html;
	};
	
	/**
	 * 初始化日志配置窗口
	 */
	this.runAppNewLogCfgWnd=function(sObj) {
		
	};
	
	/**
	 * 
	 */
	this.destroyAppNewLogCfgWnd=function(sObj) {
		
	};
	
	// TODO -------------------------------日志配置API----------------------------------------------------
	/**
	 * 日志抓取开关
	 */
	this.enableLogCatch=function(enable) {
		
		var sObj={
				svrid:_this.logCfgInfo["svrid"],
				appid:_this.logCfgInfo["appid"],
				logid:_this.logCfgInfo["logid"],
			    ip:_this.logCfgInfo["ip"]
		}
		
		var filter = "";
		sObj["msg"]="关闭归集";
		if (enable==true) {
			filter = ".*";
			sObj["msg"]="启用归集";
		}
		
		sObj["filter"]=filter;
				
		this.callLogCfg(sObj);
	};
	
	/**
	 * 下发策略
	 */
	this.callLogCfg= function(sObj){

		/**
		 * 策略
		 */
		var strategy = [{
				servid: sObj["svrid"],
				appid:sObj["appid"],
				logid: sObj["logid"],
				filter: sObj["filter"],
				separator: "\\n",
				fields: '{"content":1}'
		}];
		
		/**
		 * 构造请求
		 */
		var data = {
				intent: 'logstragety',
				request: {
					url: "http://"+sObj["ip"]+":10101/node/ctrl",
					strategy: JSON.stringify(strategy)
				}
		}
		
		/**
		 * 设置提示
		 */
		var msg="";
		if (sObj["msg"]!=undefined) {
			msg="["+sObj["msg"]+"]";
		}
		
		AjaxHelper.call({
            url: '../../rs/godeye/node/ctrl',
            data: StringHelper.obj2str(data),
            cache: false,
            type: 'POST',
            dataType: 'html',
            timeout: 5000,
            success: function(result){
            	var ret = JSON.parse(result);
            	if(ret.rs == 'ERR'){
            		alert(ret.msg);
            		return;
            	}
            	alert("设置"+msg+"日志配置成功！");
            },
            error: function(result){
                alert("请求"+msg+"日志配置失败： " + result);
            }
        });
	};
}


