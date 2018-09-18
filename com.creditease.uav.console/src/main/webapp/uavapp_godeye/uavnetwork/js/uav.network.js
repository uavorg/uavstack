/**
 * CPU & 内存 Chart定义
 */
var cpuChartCfg = {
	id:"cpuChart",
	type:"dline",
	cid:"contentDiv",
	title:"",
	width:"auto",   //一般是数字，使用auto代表可以随着容器cid，改变宽度
	height:200,
	legend:{
		enabled:true,
		verticalAlign:"top"
    },
	yAxis:{
    	title:"",
    	max:100,
    	min:0,
    	tickInterval:10
    },
    xAxis:{
    	title:"",
    	type:"datetime"
    },
    dline:{        //dynamic line的特有配置
    	timeformat:"CS",    //时间显示方式，前提是xAxis的类型是datetime, refer to TimeHelper support format
    	maxpoints:10,       //最多显示多少个点
    	interval:15000      //每个点的间隔，如果是时间，单位为毫秒；如果是数字，就是整数
    },
    series:[
        {
        	name:"CPU (%)",
        	color:"#f47920",
        	data:[]
        },
        {
        	name:"系统消耗内存 (%)",
        	color:"#7fb80e",
        	data:[]
        },
        {
        	name:"服务进程消耗总内存 (%)",
        	color:"#ADD8E6",
        	data:[]
        }
    ]
};

var memChartCfg = {
		id:"memChart",
		type:"dline",
		cid:"contentDiv",
		title:"",
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
	    	type:"datetime"
	    },
	    dline:{        //dynamic line的特有配置
	    	timeformat:"CS",    //时间显示方式，前提是xAxis的类型是datetime, refer to TimeHelper support format
	    	maxpoints:10,       //最多显示多少个点
	    	interval:15000      //每个点的间隔，如果是时间，单位为毫秒；如果是数字，就是整数
	    },
	    series:[	        
	        {
	        	name:"系统消耗内存 (GB)",
	        	color:"#7fb80e",
	        	data:[]
	        },
	        {
	        	name:"服务进程消耗总内存 (GB)",
	        	color:"#ADD8E6",
	        	data:[]
	        }
	    ]
	};
/**
 * 连接数信息 Chart定义
 */
var connChartCfg = {
		id:"connChart",
		type:"dline",
		cid:"contentDiv",
		title:"",
		width:"auto",
		height:200,
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
	    	timeformat:"CS",
	    	maxpoints:10,
	    	interval:15000
	    },
	    series:[
	        {
	        	name:"连接数",
	        	color:"#EEEE00",
	        	data:[]
	        }
	    ]
	};

/**
 * 流量信息 Chart定义
 */
var fluxChartCfg = {
		id:"fluxChart",
		type:"dline",
		cid:"contentDiv",
		title:"",
		width:"auto",
		height:200,
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
	    	timeformat:"CS",
	    	maxpoints:10,
	    	interval:15000
	    },
	    series:[
	        {
	        	name:"入口流量 (KB)",
	        	color:"#f47920",
	        	data:[]
	        },
	        {
	        	name:"出口流量 (KB)",
	        	color:"#d93a49",
	        	data:[]
	        }
	    ]
	};

/**
 * 磁盘io信息 Chart定义
 */
	var diskIoChartCfg = {
		id:"diskIoChart",
		type:"dline",
		cid:"contentDiv",
		title:"",
		width:"auto",
		height:200,
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
	    	timeformat:"CS",
	    	maxpoints:10,
	    	interval:15000
	    },
	    series:[]
	};


/**
 * 进程cpu内存比信息 Chart定义
 */
var processChartCfg = {
	id:"processChart",
	type:"dline",
	cid:"ProcessChartWndDiv",
	title:"",
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
    	type:"datetime"
    },
    dline:{        //dynamic line的特有配置
    	timeformat:"CS",    //时间显示方式，前提是xAxis的类型是datetime, refer to TimeHelper support format
    	maxpoints:10,       //最多显示多少个点
    	interval:15000      //每个点的间隔，如果是时间，单位为毫秒；如果是数字，就是整数
    },
    series:[
        {
        	name:"CPU",
        	color:"#f47920",
        	data:[]
        },
        {
        	name:"内存(%)",
        	color:"#7fb80e",
        	data:[]
        }
    ]
};
/**
 * 进程内存信息 Chart定义
 */
var processMemChartCfg = {
	id:"processMemChart",
	type:"dline",
	cid:"ProcessChartWndDiv",
	title:"",
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
    	type:"datetime"
    },
    dline:{        //dynamic line的特有配置
    	timeformat:"CS",    //时间显示方式，前提是xAxis的类型是datetime, refer to TimeHelper support format
    	maxpoints:10,       //最多显示多少个点
    	interval:15000      //每个点的间隔，如果是时间，单位为毫秒；如果是数字，就是整数
    },
    series:[

        {
        	name:"内存(MB)",
        	color:"#7fb80e",
        	data:[]
        }
    ]
};
/**
 * 进程连接数信息 Chart定义
 */
var processConnChartCfg = {
		id:"processConnChart",
		type:"dline",
		cid:"ProcessChartWndDiv",
		title:"",
		width:"auto",
		height:200,
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
	    	timeformat:"CS",
	    	maxpoints:10,
	    	interval:15000
	    },
	    series:[]
	};

/**
 * 进程流量信息 Chart定义
 */
var processFluxChartCfg = {
		id:"processFluxChart",
		type:"dline",
		cid:"ProcessChartWndDiv",
		title:"",
		width:"auto",
		height:200,
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
	    	timeformat:"CS",
	    	maxpoints:10,
	    	interval:15000
	    },
	    series:[]
	};

/**
 * 进程磁盘IO信息 Chart定义
 */
var processDiskIoChartCfg = {
	id:"processDiskIoChart",
	type:"dline",
	cid:"ProcessChartWndDiv",
	title:"",
	width:"auto",   
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
    	type:"datetime"
    },
    dline:{        
    	timeformat:"CS",    
    	maxpoints:10,       
    	interval:15000      
    },
    series:[

        {
        	name:"读磁盘速率(KB/s)",
        	color:"#d93a49",
        	data:[]
        },
        {
        	name:"写磁盘速率(KB/s)",
        	color:"#ffc20e",
        	data:[]
        }
    ]
};


var colors = [ "#d93a49", "#f47920", "#1d953f", "#840228", "#843900",
		"#411445", "#ffc20e", "#009ad6", "#563624", "#f173ac" ];

/**
 * TODO: data cache
 */
var dataCacheCfg={
	type:"session"
};

var dataCache={};

/**
 * TODO: UserFilterCache
 */
var UserFilterCache={
		all:false,
		nomapping:false,
		datas:[],
		ctrls:{},
		init:function(){
			var uFilterInfo = window["cachemgr"].get("godeye.uav.network.user.filter.info");
			if (uFilterInfo) {
				UserFilterCache.initCtrls(uFilterInfo);
			}else{
				AjaxHelper.call({
					url : "../../rs/godeye/filter/group/user/q",
					async : true,
					cache : false,
					type : "GET",
					dataType : "html",
					success : function(result) {
						window["cachemgr"].put("godeye.uav.network.user.filter.info",result);
						UserFilterCache.initCtrls(result);
					},
					error : function(result) {
						console.log(result);
					}
				});
			}
		},
		initCtrls:function(_datas){
			if("ALL"==_datas){
				UserFilterCache.all=true;
			}else if("NOMAPPING"==_datas){
				UserFilterCache.nomapping=true;
			}else{
				_datas=eval("("+_datas+")");
				UserFilterCache.datas=_datas;
				var r = {};
				$.each(_datas,function(index,obj){
					JsonHelper.merge(r,obj.groupList,true);
				});
				UserFilterCache.ctrls=r;
			}
		},
		checkNodeAuthor:function(){
			
			return false;
		},
		checkProcessAuthor:function(){
			return false;
		}
}

//TODO: MAC tab list config
var macTabListConfig={
	id:"macTabList",
	cid:"MacList",
	pkey:"ip",
	style:"Light",
	width:230,
	templ:function(nodeDiv,jsonObj) {
		app.controller.buildMacBox(nodeDiv,jsonObj);
	},
	group:{
		getId:function(d) {
			return d["group"];
		},
		match:function(groups,curdata) {
			
			var gp=curdata["group"];
			
			if (gp!=undefined&&gp!=""&&groups[gp]!=undefined) {
				return groups[gp];
			}
			else {
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
			}
			
			return undefined;
		}
	}
};

/**
 * TODO：ultron create container control panel
 */
var createUContainerDialog={
	id:"createUContainerDialog",
	title:"创建容器",
	height:190,
	event:{
		onbody:function(id) {
			var sb=new StringBuffer();
			sb.append('<div style="text-align:center;">'); 
			sb.append('<select id="' + id + '_select" class="form-control"></select>');
			sb.append('<button id="' + id + '_create_btn" class="btn btn-primary" style="width:100px;">创建</button>');
			sb.append('&nbsp;&nbsp;&nbsp;&nbsp;');
			sb.append('<button id="' + id + '_run_btn" class="btn btn-primary" style="width:100px;" >一键启动</button>');
			sb.append('</div>');
			return sb.toString();
		},
		onopen:function(id,obj) {
			var containersStr = obj["dockerContainer"];
			var containers;
			if (containersStr) {
				containers = eval("("+containersStr+")");
			}
			
            var selectId = "#" + id + '_select';
            $(selectId).empty();
            
            if(containers != undefined){
            	for (var key in containers) {
            		var container = eval("("+containers[key]+")");
            		var containerType = container["containerType"];
            		if("ultron" == containerType) {
            			var name = container["name"];
            			$(selectId).append('<option value ="' + name + '">' + "create by ["+name+"]" + '</option>');
            		}
            	}
            }
            
            $('#' + id + '_create_btn').on('click', function(event){
                var containerName = $(selectId).val();
                if(!containerName){
                    alert('无可用容器!');
                    return;
                }

                var data = {
                    intent: 'createUContainer',
                    request: {
                        url: obj['url'],
                        containerName: containerName,
                        event: "create"
                    }
                };
                
                AjaxHelper.call({
                    url: '../../rs/godeye/node/ctrl',
                    data: StringHelper.obj2str(data),
                    cache: false,
                    type: 'POST',
                    dataType: 'html',
                    timeout: 5000,
                    success: function(result){
                        var obj = StringHelper.str2obj(result);
                        var rs = obj['rs'];
                        var body = obj['body'];
                        
                        if ("ERR"==rs || undefined==rs) {
                        	body = undefined==body?"":":"+body;
                            alert("创建容器失败" + body);
                        }
                        else {
                            alert("发送创建容器命令成功!");
                        }
                        
                    },
                    error: function(result){
                        alert("创建容器失败:"+result);
                    }
                });
                
                window["appdialogmgr"].close("createContainerDialog");
            });
            
            $('#' + id + '_run_btn').on('click', function(event){
                var containerName = $(selectId).val();
                if(!containerName){
                    alert('无可用容器!');
                    return;
                }

                var data = {
                    intent: 'runUContainer',
                    request: {
                        url: obj['url'],
                        containerName: containerName,
                        event: "run"
                    }
                };
                
                AjaxHelper.call({
                    url: '../../rs/godeye/node/ctrl',
                    data: StringHelper.obj2str(data),
                    cache: false,
                    type: 'POST',
                    dataType: 'html',
                    timeout: 5000,
                    success: function(result){
                        var obj = StringHelper.str2obj(result);
                        var rs = obj['rs'];
                        var body = obj['body'];
                        
                        if ("ERR"==rs || undefined==rs) {
                        	body = undefined==body?"":":"+body;
                            alert("一键启动失败" + body);
                        }
                        else {
                            alert("发送一键启动命令成功!");
                        }
                        
                    },
                    error: function(result){
                        alert("一键启动失败："+result);
                    }
                });
                
                window["appdialogmgr"].close("createContainerDialog");
            });

		},
		onclose:function(id) {
			
		}
	}
}; 

/**
 * TODO: system properties control panel
 */
var sysProSetDialog={
	id:"sysProSetDialog",
	title:"设置JVM系统属性",
	height:190,
	event:{
		onbody:function(id,obj) {
			var sb=new StringBuffer();
			sb.append("<div style='text-align:center;'><label id='"+id+"_target'></label>");
			sb.append("<input id='"+id+"_key' type='text' class='form-control' placeholder='属性Key' /><input id='"+id+"_value' type='text' class='form-control' placeholder='属性Value' />");
			sb.append("<button class='btn btn-primary' style='width:200px;' onclick='app.controller.cp_syspro.setSystemPro()'>设置</button></div>");
			return sb.toString();
		},
		onopen:function(id,obj) {
			HtmlHelper.id(id+"_target").innerHTML=obj["name"]+"("+obj["ip"]+")";
		},
		onclose:function(id) {
			
		}
	}
};

/**
 * TODO: node config dialog
 */
var nodeConfigDialog = {
	id : 'nodeConfigDialog',
	title : '设置节点配置',
	height : 190,
	event : {
		onbody:function(id) {
			var sb=new StringBuffer();
			sb.append("<div style='text-align:center;'><label id='"+id+"_target'></label>");
			sb.append("<input id='"+id+"_key' type='text' class='form-control' placeholder='配置Key' readOnly='readOnly' /><input id='"+id+"_value' type='text' class='form-control' placeholder='配置Value' />");
			sb.append("<button class='btn btn-primary' style='width:200px;' onclick='app.controller.doSetNodeConfig()'>设置</button></div>");
			return sb.toString();
		},
		onopen : function(id, obj) {
			HtmlHelper.id(id+"_target").innerHTML=obj["name"]+"("+obj["ip"]+")";
			HtmlHelper.id(id+"_key").value=obj["key"];
			var target=HtmlHelper.id("NodeConfigWnd_D_"+obj["key"]);
			HtmlHelper.id(id+"_value").value=target.innerHTML;
		},
		onclose : function() {

		}
	}
};

/**
 * TODO: node upgrade control panel
 */
var preUpgradeDialog = {
    id: 'preUpgradeDialog',
    title: '升级',
    hight: 50,
    softwareId:"",
    event: {
        onbody: function(id) {
        	
        	var html ="";
        	if(preUpgradeDialog.softwareId=="uavmof"){
        		html = '<div style="text-align:center;">' 
                    + '<select id="' + id + '_select" class="form-control"></select>'
                    + '<input id="' + id + '_targetDir" class="form-control" value="" placeholder="升级目标目录，如：/app/uav/uavmof"></input>'
                    + '<button id="' + id + '_btn" class="btn btn-primary" style="width:200px" >升级</button>'
                    + '</div>';
        	}else {
        		html = '<div style="text-align:center;">' 
                    + '<select id="' + id + '_select" class="form-control"></select>'
                    + '<button id="' + id + '_btn" class="btn btn-primary" style="width:200px" >升级</button>'
                    + '</div>';
        	}
            
            return html;
        },
        onopen: function(id, obj) {
        	
                var selectId = '#' + id + '_select';
                var targetDirId ='#' + id + '_targetDir';
                $(selectId).empty();
                
                AjaxHelper.call({
                    url:"../../rs/godeye/node/upgrade/list/" + preUpgradeDialog.softwareId,
                    async : true,
                    cache : false,
                    type : "GET",
                    dataType : "html",
                    success : function(result) {
                        var data = eval("(" + result + ")");
                        var list = data.files;
                        if(list != undefined){
                        	list.sort().reverse();
                            for(var i = 0; i < list.length; i++){
                                $(selectId).append('<option value ="' + list[i] + '">' + list[i] + '</option>');
                            }
                        }
                    },
                    error : function(result) {
                    	alert("获取升级包列表失败： " + result);
                    }
                });
                
                $('#' + id + '_btn').on('click', function(event){
                    var pkg = $(selectId).val();
                    if(!pkg){
                        alert('无可用版本');
                        return;
                    }
                    /**
                     * uav自己升级，参数
                     */
                    var data = {
                            intent: 'upgrade',
                            request: {
                                url: obj['url'],
                                uav: 1,
                                softwareId: preUpgradeDialog.softwareId,
                                softwarePackage: pkg
                            }
                    };
                    
                    /**
                     * 第三方升级,参数处理 begin
                     */
                	if(preUpgradeDialog.softwareId=="uavmof"){
                        var targetDirValue =$.trim($(targetDirId).val());
                        if(!targetDirValue){
                            alert('无升级目标目录');
                            return;
                        }
                        data.request.uav=0;
                        data.request.targetDir=targetDirValue;
                	}
                	 /**
                     * 第三方升级,参数处理 end
                     */
                    
                    AjaxHelper.call({
                        url: '../../rs/godeye/node/ctrl',
                        data: StringHelper.obj2str(data),
                        cache: false,
                        type: 'POST',
                        dataType: 'html',
                        timeout: 5000,
                        success: function(result){
                            var obj = StringHelper.str2obj(result);
                            if (obj=="ERR") {
                                alert("升级失败：" + result);
                            }
                            else {
                                alert("发送升级命令成功！");
                            }
                            
                        },
                        error: function(result){
                            alert("升级失败： " + result);
                        }
                    });
                    
                    window["appdialogmgr"].close("preUpgradeDialog");
                });
        	
            
        },
        onclose: function() {
        }
    }
}

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
		showWindowDelay:function(ip,winId,func,callback,tag,proc) {
			
			var info={ip:ip,winId:winId,func:func,callback:callback,tag:tag};
			
			if (proc!=undefined) {
				if (proc["pid"]!=undefined) {
					info["pid"]=proc["pid"];
				}
				else {
					info["port"]=proc["port"];
				}
			}
			
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
			window["openlink_"+tag]=undefined;
			if (tag=="procinfo") {
				
				var pid=info["pid"];
				//need find pid by port
				if (pid==undefined) {
					var macInfo=app.mdata("macinfo")[info["ip"]];
					
					var procs=eval("("+macInfo["node.procs"]+")");
					
					for(var key in procs) {
						
						var proc=procs[key];
						
						var ports=proc["ports"];
						
						for(var i=0;i<ports.length;i++) {
							if (ports[i]==info["port"]) {
								pid=proc["pid"];
								break;
							}
						}
					}
				}
				
				app.controller.showProcessWindow(info["ip"],pid,info["winId"],info["func"],info["callback"]);
			}
			else {
				app.controller.showWindow(info["ip"],info["winId"],info["func"],info["callback"]);
			}
			
			
		}
};

/**
 * TODO: MVC框架实例定义
 */
var mvcObj={
	init:function() {
		
		//init cache
		window["cachemgr"].init(dataCacheCfg);
		
		this.controller.recoverDataCache();
		
		//build mac detail window
		window.winmgr.build({
			id:"MacList",
			order:1,
			height:"auto:0",
			"overflow-y":"auto",
			theme:"BGLight",
			top:50
		});
		//build mac detail window
		window.winmgr.build({
			id:"MacDetailWnd",
			height:"auto",
			"overflow-y":"auto",
			order:999
		});
		//build mac chart window
		window.winmgr.build({
			id:"MacChartWnd",
			height:"auto",
			"overflow-y":"auto",
			order:998
		});
		//build process chart window
		window.winmgr.build({
			id:"ProcessChartWnd",
			height:"auto",
			"overflow-y":"auto",
			order:997
		});
		//build node config window
		window.winmgr.build({
			id:"NodeConfigWnd",
			height:"auto",
			"overflow-y":"auto",
			order:996
		});
		window.winmgr.build({
			id:"MacUContainerWnd",
			height:"auto",
			"overflow-y":"auto",
			order:995
		});

	
		//init mac chart
		window["appcharts"].bulid(cpuChartCfg);
		window["appcharts"].bulid(memChartCfg);
		window["appcharts"].bulid(connChartCfg);
		window["appcharts"].bulid(fluxChartCfg);
		window["appcharts"].bulid(diskIoChartCfg);
		
		//init process chart
		window["appcharts"].bulid(processChartCfg);
		window["appcharts"].bulid(processMemChartCfg);
		window["appcharts"].bulid(processConnChartCfg);
		window["appcharts"].bulid(processFluxChartCfg);
		window["appcharts"].bulid(processDiskIoChartCfg);
		//init control panel dialog
		window["appdialogmgr"].build(sysProSetDialog);
		window["appdialogmgr"].build(createUContainerDialog);
		window["appdialogmgr"].build(nodeConfigDialog);
		
		//init user filter info
		UserFilterCache.init();
		
		//OpenLink
		var view=HtmlHelper.getQParam("view");
		
		//function 1: OS metrics chart
		if ("macchart"==view) {
			var tip=HtmlHelper.getQParam("ip");
			openLink.showWindowDelay(tip,'MacChartWnd','buildMacChart','createMacChart','macinfo');
		}
		//function 2: process metrics chart
		else if ("procchart"==view) {
			var tip=HtmlHelper.getQParam("ip");
			var proc={pid:HtmlHelper.getQParam("pid"),port:HtmlHelper.getQParam("port")};
			openLink.showWindowDelay(tip,'ProcessChartWnd','buildProcessChart','createProcessChart','procinfo',proc);
		}
		
		//init machine list tab
		window.tablistmgr.build(macTabListConfig);
		
		//show MacList
		window.winmgr.show("MacList");
	},
	datas:{
		"uavnetwork":{
			url:function() {
				return "../../rs/godeye/node/q/cache";
			},
			method:"GET",
			rtype:"html",
			interval:15000,
			prepare:function(data) {
				 var jsonData = eval("(" + data+ ")");
				 jsonData=eval("(" +jsonData["rs"]+ ")");
				 var nodeArray=[];
				 for (var key in jsonData) {
					 var nodeInfoObj=eval("(" + jsonData[key]+ ")");
					 nodeArray[nodeArray.length]=nodeInfoObj;
				 }
				 return nodeArray;
			},
			models:["macinfo","nodeinfo"]
		}
	},
	search:{
		//bind model to search
		model:"macinfo",
		tip:"以应用组,标签,IP或Host检索"
	},
	models:{
		//mac info
		"macinfo":{
			//-------------数据事件--------------
			isupdate:function(oldObj,newObj) {
				
				var ot=oldObj["clientTimestamp"];
				
				if (ot==undefined) {
					return true;
				}
				
				var isupdate=newObj["clientTimestamp"]>ot;
				
				return isupdate;
			},
			onnew:function(dObj) {				
				window.tablistmgr.load("macTabList", [dObj]);
				//app.controller.addMacBox(dObj);				
				app.controller.updateMacPerf(dObj);
			},
			onupdate:function(dObj) {
				window.tablistmgr.load("macTabList", [dObj],false);
				app.controller.updateMacPerf(dObj);
			},
			ondel:function(dObj) {
				
				window.tablistmgr.del("macTabList", [dObj.ip]);
				//app.controller.delMacBox(dObj);
			},
			//获得所有最新数据后
			onrefresh:function(pdlist) {
				//do groups
				window["tablistmgr"].doGroups("macTabList");
				
				//openlink事件
				openLink.runWindowDelay("macinfo");		
				openLink.runWindowDelay("procinfo");	
			},
			//--------------元数据定义-----------------
			//primary key
			pkey:"ip",
			//filter keys for search
			fkeys:["group","ip","node.tags","host"],
			//--------------字段映射-------------------
			"host":{key:"host"},    
	        "ip":{key:"ip"},	        
	        "os.type":{key:'info/os.type'},
	        "os.arch":{key:'info/os.arch'},
	        "os.mac":{key:'info/os.mac'},
	        "os.cpu.number":{key:'info/os.cpu.number'},
	        "os.cpu.load":{key:'info/os.cpu.load'},
	        "os.cpu.avgload":{key:'info/os.cpu.avgload'},
	        "os.cpu.maxmem":{key:'info/os.cpu.maxmem'},
	        "os.cpu.freemem":{key:'info/os.cpu.freemem'},
	        "os.conn.cur":{key:'info/os.conn.cur'},
	        "os.io.disk":{key:'info/os.io.disk'},
	        "os.netcard":{key:'info/os.netcard'},
	        "os.java.vm":{key:'info/os.java.vm'},   
	        "os.java.ver":{key:'info/os.java.ver'},
	        "os.java.home":{key:'info/os.java.home'},
	        "clientTimestamp":{key:"clientTimestamp"},  
	        "serverTimestamp":{key:"serverTimestamp"},
	        "node.procs":{
	        	   		  key:"info/node.procs",
	        	          /**
	        	           * if define the update method, this field will use this method to update
	        	           * this is the customized way to update a specific field
	        	           */
	        	   		  timestamp:0,
	        	          update:function(oldObj,newObj) {
	        	        	  
	        	        	  
	        	        	  var np=newObj["node.procs"];
	        	        	  var op=oldObj["node.procs"];
	        	        	  var ot=oldObj["clientTimestamp"];
	        	        	  var nt=newObj["clientTimestamp"];
	        	        	  
        	        		  if (this.timestamp==0) {
        	        			  this.timestamp=ot;
        	        		  }
        	        		  
	        	        	  if(op==undefined) {
	        	        		  this.timestamp=nt;
	        	        		  return np;
	        	        	  }
	        	        	  
	        	        	  if (nt-ot>0&&np!=undefined) {
	        	        		  this.timestamp=nt;
	        	        		  return np;	        	        		  
	        	        	  }
	        	        	  else {
	        	        		  
	        	        		  var curTime=new Date().getTime();
	        	        		  
	        	        		  if (op!=undefined&&np==undefined&&curTime-this.timestamp>300000) {
	        	        			  return undefined;
	        	        		  }
	        	        		  else {
	        	        			  return op;
	        	        		  }
	        	        	  }
	                      }
	        },
	        "node.tags":{
	        			key:"info/node.tags",
			        	update:function(oldObj,newObj) {
			        		  var np=newObj["node.tags"];
			  	        	  var op=oldObj["node.tags"];
			  	        	  
			  	        	  if(op==undefined) {
			  	        		  return np;
			  	        	  }
			  	        	  
			  	        	  var ot=oldObj["clientTimestamp"];
			  	        	  var nt=newObj["clientTimestamp"];
			  	        	  
			  	        	  if (nt-ot>0&&np!=undefined) {	        	        		  
			  	        		  return np;	        	        		  
			  	        	  }
			  	        	  else {
			  	        		  return op;
			  	        	  }
			        	}
	        },
	        "group":{
	        	key:"group",
	        	update:function(oldObj,newObj) {
	        		var np=newObj["group"];
	  	        	var op=oldObj["group"];
	  	        	if (np!=undefined&&np!=""){
	  	        		return np;
	  	        	}
	  	        	else {
	  	        		return op;
	  	        	}
	        	}
	        }
		},
		//node info
        "nodeinfo":{
        	//-------------数据事件--------------
        	onnew:function(dObj) {
        		app.controller.addNodeBox(dObj);
			},
			onupdate:function(dObj) {
				app.controller.updateNodeBox(dObj);
			},
			ondel:function(dObj) {
				app.controller.delNodeBox(dObj);
			},
			
			//--------------元数据定义-----------------
			pkey:"id",
			//--------------字段映射-------------------
			"id":"id",
        	"ip":"ip",
            "name":"name",
            "group":"group",
            "node.profile":'info/node.profile',
            "node.feature":'info/node.feature',
            "node.root":'info/node.root',
            "node.state":'info/node.state',
            "node.hbserver":'info/node.hbserver',
            "clientTimestamp":"clientTimestamp",
            "serverTimestamp":"serverTimestamp",
            "node.pid":"info/node.pid",
            "node.procs":"info/node.procs",
            "node.services":"info/node.services",
            "node.version":"info/node.version",
            "node.docker.info": "info/node.docker.info",
            "node.docker.container": "info/node.docker.container"
        }
	},
	controller:{
	    //--------------------------------COMMON-------------------------------------------
		/**
		 * TODO: COMMON
		 */
	    javaProNames:{
	    	"org.apache.catalina.startup.Bootstrap":"Tomcat",
	    	"org.apache.zookeeper.server.quorum.QuorumPeerMain":"ZooKeeper",
	    	"com.eviware.soapui.SoapUI":"SoapUI",
	    	"com.alibaba.rocketmq.broker.BrokerStartup":"RocketMQ-Broker",
	    	"com.alibaba.rocketmq.namesrv.NamesrvStartup":"RocketMQ-NamingServer",
	    	"net.opentsdb.tools.TSDMain":"OpenTSDB",
	    	"org.apache.hadoop.hbase.master.HMaster":"HBase",
	    	"org.apache.hadoop.hdfs.server.namenode.NameNode":"Hadoop.NameNode",
	    	"org.apache.hadoop.hdfs.server.datanode.DataNode":"Hadoop.DataNode",
	    	"org.apache.hadoop.hdfs.qjournal.server.JournalNode":"Hadoop.JournalNode",
	    	"org.apache.hadoop.yarn.server.nodemanager.NodeManager":"Hadoop.NodeManager",
	    	"org.apache.hadoop.yarn.server.resourcemanager.ResourceManager":"Hadoop.ResourceManager",
	    	"org.apache.hadoop.hbase.regionserver.HRegionServer":"HBase.RegionServer",
	    	"org.apache.hadoop.hbase.zookeeper.HQuorumPeer":"ZooKeeper",
	    	"org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode":"Hadoop.SecNameNode",
	    	"org.elasticsearch.bootstrap.Elasticsearch":"Elasticsearch"
	    },
		scrollYLocation:0,		
	    showWindow:function(ip,winId,func,callback) {
	    	this.scrollYLocation=HtmlHelper.id("MacList")["scrollTop"];
	    	var content=this[func](ip);
	    	window.winmgr.content(winId,content);
		    window.winmgr.show(winId);
	    	HtmlHelper.css("MacList",{"scrollTop":0});
	    	
			if(callback){
			   this[callback](ip);
			}
	    },
	    closeWindow:function(winId,callback,backToWndId) {
	    	window.winmgr.hide(winId);	
	    	if(backToWndId==undefined) {
	    		window.winmgr.show("MacList");
	    	}
	    	else {
	    		window.winmgr.show(backToWndId);
	    	}
	    	HtmlHelper.css("MacList",{"scrollTop":this.scrollYLocation});
			if(callback){
			   this[callback]();
			}
	    },
	    openClose:function(id) {
	    	var obj=HtmlHelper.id(id);
	    	if (obj.style.display=="none") {
	    		obj.style.display="block";
	    	}
	    	else {
	    		obj.style.display="none";
	    	}
	    },
	    showProcessWindow:function(ip,pid,winId,func,callback) {
	    	this.scrollYLocation=HtmlHelper.id("MacList")["scrollTop"];
	    	var content=this[func](ip,pid);
	    	window.winmgr.content(winId,content);
		    window.winmgr.show(winId);
	    	HtmlHelper.css("MacList",{"scrollTop":0});
	    	
			if(callback){
			   this[callback](ip,pid);
			}
	    },
	 
	    //get java program name
	    getJavaProName:function(obj) {
	    	
	    	var name=obj["name"];
	    	var tags=obj["tags"];
	    	
	    	if (name.indexOf("java")==-1) {
	    		return name;
	    	}
	    	
	    	if (tags==undefined) {
	    		return name;
	    	}
	    	
	    	var pname=this.javaProNames[tags["main"]];
	    	if (pname!=undefined) {
	    		return pname;
	    	}
	    	else {
	    		return name;
	    	}
	    },
	    //handler node.procs
	    handleProcess:function(procs,handler) {
	    	
	    	var count=0;
	    	
	    	for (var key in procs) {
	    	
	    		var proc=procs[key];
	    		
		    	var name=proc["name"];

	    		count++;
	    		
	    		if ((name.indexOf("java")>-1&&(proc["tags"]!=undefined&&(proc["tags"]["main"]=="com.creditease.agent.SystemStarter"||proc["tags"]["main"]=="com.creditease.mscp.boot.MSCPBoot")))
		    		|| (proc["tags"]!=undefined && (undefined!=proc["tags"]["containerType"]))){ 
		    	   continue;
		    	}
	    		handler(proc);
	    		
	    	}
	    	
	    	return count;
	    },
	    //handle container
	    handleContainer:function(nodeObj,handler){
	    	var containers = eval("("+nodeObj["node.docker.container"]+")");
	    	for(var key in containers){
	    		var containerStr = containers[key];
	    		var container = eval("("+containerStr+")")
	    		if(undefined != container["containerType"]) {
	    			handler(container);
	    		}
	    	}
	    },
	    //get host name or tags
	    getHostTagOrName:function(jsonObj) {
	    	
	    	var tags=jsonObj["node.tags"];
	    	
	    	if (undefined==tags||""==tags) {
	    		return jsonObj["host"];
	    	}
	    	
			//if only has 监控代理程序 then display host name
	    	if (tags=="监控代理程序,") {
	    		return jsonObj["host"];
	    	}
	    				
	    	return tags;
	    },
		//------------------------------MAC---------------------------------------
	    /**
	     * TODO: MAC BOX
	     */
		//add a new MAC Box
		buildMacBox:function(nodeDiv,jsonObj) {
			var divContent =
	            "<div id='"+jsonObj.ip+"'>\n" +
	            "    <div class=\"topDiv\">\n" +
	            "        <span class=\"hostTitle\" id='"+jsonObj.ip+"_hostTag'>"+this.getHostTagOrName(jsonObj)+"</span><br/>\n" +
	            "        <span class=\"ipTitle\">"+jsonObj.ip+"</span>\n" +
	            "        <div class=\"icon-list\" onclick=\"javascript:app.controller.showWindow(\'"+jsonObj.ip+"\','MacDetailWnd','buildMacDetail')\"></div>\n" +
	            "    </div>\n" +            
	            "    <div class=\"middleDiv\" onclick=\"javascript:app.controller.showWindow(\'"+jsonObj.ip+"\','MacChartWnd','buildMacChart','createMacChart')\">\n" +
	            "    <div class=\"cpuTitle\">CPU<span class='osRate' id='"+jsonObj.ip+"_cpuRate'></span></div>\n" +
	            "    <div class=\"memoryTitle\">内存<span class='osRate'  id='"+jsonObj.ip+"_memRate'></span></div>\n" +
	            "    <div class=\"connTitle\">连接<span class='osRate'  id='"+jsonObj.ip+"_connRate'></span></div>\n" +
	            "        <table >\n" +
	            "            <tr>\n" +
	            "                <td>\n" +
	            "                    <div id=\""+jsonObj.ip+"_cpuRateT\"></div>\n" +
	            "                </td>\n" +
	            "                <td>\n" +
	            "                    <div id=\""+jsonObj.ip+"_memRateT\"></div>\n" +
	            "                </td>\n" +
	            "                <td>\n" +
	            "                    <div id=\""+jsonObj.ip+"_connRateT\"></div>\n" +
	            "                </td>\n" +
	            "            </tr>\n" +
	            "        </table>\n" +
	            "    </div>\n" +
	            "    <div >" +
	            "    <div class='processButton' onclick='app.controller.showNodeProcs(\""+jsonObj.ip+"\")' title=\"点击展开/收起服务进程列表\">服务进程:<span id='"+jsonObj.ip+"_procs_num' class='osRate'>0</span></div>" +
	            "        <div id='"+jsonObj.ip+"_procs' class='processHolder'></div>" +
	            "    </div> "+
	            "<div class=\"bottomDiv\" id=\""+jsonObj.ip+"_nodes\"></div>\n"+
	            "</div>";

			nodeDiv.innerHTML=divContent;
			//HtmlHelper.id("MacList").innerHTML+=divContent;
		},
		//change mac CPU & Mem
		updateMacPerf :function(jsonObj) {
			//cpu
	        var cpuPro = jsonObj["os.cpu.load"];
	        this.changePerfVal(jsonObj.ip+"_cpuRate", cpuPro);
	        //mem
	        var memoryMax = jsonObj["os.cpu.maxmem"];
	        var memoryFreeme = jsonObj["os.cpu.freemem"];
	        //var memoryPro = Math.ceil(((memoryMax - memoryFreeme) / memoryMax) * 100);

	        var procMemSum=0;
	        // sum real connections from proc's conn
	        if(jsonObj['node.procs']){
	    		var procs = JSON.parse(jsonObj['node.procs']);
	    		var procConnSum = 0;
	    		
	    		for (var k in procs){
	    			
	    			if(procs[k].tags!=undefined&&procs[k].tags.mem!=undefined){ 
	    				procMemSum+=parseInt(procs[k].tags.mem)*1024;
	    			}
	    		}
	    	}
	        var memoryPro = 0 ;
	        if(procMemSum == 0)
	        {
	        	memoryPro = Math.ceil(((memoryMax - memoryFreeme) / memoryMax) * 100);
	        }else
	        {
	            memoryPro=Math.ceil((procMemSum/ memoryMax) * 100);
	        }
	        
	        this.changePerfVal(jsonObj.ip+"_memRate",memoryPro);
	      
	        
	        //connections
	        var curconn=jsonObj["os.conn.cur"];
	        // sum real connections from proc's conn
	        if(jsonObj['node.procs']){
	    		var procs = JSON.parse(jsonObj['node.procs']);
	    		var sum = 0;
	    		for (var k in procs){
	    			
	    			if(procs[k].tags!=undefined&&procs[k].tags.conn!=undefined){
	    				sum += parseInt(procs[k].tags.conn);
	    			}
	    		}
	    		if(sum!=0){
	    			curconn = sum;
	    		}
	    	}
	        
	        if (curconn==0) {
	        	curconn=jsonObj["os.conn.cur"];
	        }
		    var maxconn=2000;
		    var connPro=(curconn>2000)?100:(curconn/maxconn)*100;	        
		    this.changePerfVal(jsonObj.ip+"_connRate",connPro,curconn);
	        //load node procs
	        this.loadNodeProcs(jsonObj);
	        
	        //update host tag
	        var hostTag=HtmlHelper.id(jsonObj.ip+"_hostTag");
	        hostTag.innerHTML=this.getHostTagOrName(jsonObj);
		},
	    //show process on the node's host machine
	    loadNodeProcs: function(jsonObj) {
	    	
	    	var procContainer=HtmlHelper.id(jsonObj.ip+"_procs");
	    	
	    	var procs=eval("("+jsonObj["node.procs"]+")");
	    	
	    	var sb=new StringBuffer();
	    	
	    	var count=0;
	    	
	    	if (undefined!=procs) {
	    		var that = this;
		    	var handler=function(proc) {
		    		var procState = that.buildProcStateHtm(proc);
		    		sb.append("<div class='process' onclick=\"javascript:app.controller.showProcessWindow(\'"+jsonObj.ip+"\',\'"+proc["pid"]+"\','ProcessChartWnd','buildProcessChart','createProcessChart')\"><span class='processInfo'><span class='pid'>["+proc["pid"]+"]</span>&nbsp;"+app.controller.getJavaProName(proc)+"</span>"+procState+"</div>");
		    	};
		    	
		    	count=this.handleProcess(procs,handler);
	    	}
	    	
	    	HtmlHelper.id(jsonObj.ip+"_procs_num").innerHTML=count;

	    	procContainer.innerHTML=sb.toString();	    	
	    },
	    showNodeProcs:function(ip) {
	    	var procContainer=HtmlHelper.id(ip+"_procs");
	    	if(procContainer.style.display=="none") {
	    		HtmlHelper.css(ip+"_procs",{display:"block"}); 
	    	}
	    	else {
	    		HtmlHelper.css(ip+"_procs",{display:"none"}); 
	    	}
	    	
	    },
	    buildProcStateHtm: function(proc){
	    	
	    	var htm = '';

	    	if (proc==undefined) {
	    		return htm;
	    	}
	    	
	    	var pcpu, mem, pmem, conn;
	    	
	    	var cpuStateCls, memStateCls, connStateCls;
	    	
	    	if(proc.tags && proc.tags.cpu && proc.tags.mem){
	    		pcpu = proc.tags.cpu;
	    		mem = proc.tags.mem;
	    		pmem = proc.tags.memRate;
	    		conn = proc.tags.conn;
	    		if(pcpu < 40){
	    			cpuStateCls = '';
	    		}else if(pcpu >= 40 && pcpu < 70){
	    			cpuStateCls = 'proc-state-mid';
	    		}else{
	    			cpuStateCls = 'proc-state-high';
	    		}
	    		if(pmem < 40){
	    			memStateCls = '';
	    		}else if(pmem >=40 && pmem < 70){
	    			memStateCls = 'proc-state-mid';
	    		}else{
	    			memStateCls = 'proc-state-high';
	    		}
	    		if(conn <= 0){
	    			connStateCls = '';
	    		}else{
	    			var pconn = conn / 1000 * 100;
	    			if(pconn < 40){
	    				connStateCls = '';
	    			}else if(pconn >= 40 && pconn < 70){
	    				connStateCls = 'proc-state-mid';
	    			}else{
	    				connStateCls = 'proc-state-high';
	    			}
	    		}
	    		
	    		htm = '<span class="proc proc-state ' + connStateCls + '" title=\"连接数: '+conn+'\"></span>' // conn
	    			 +'<span class="proc proc-state ' + memStateCls + '" title=\"内存占用率: '+pmem+'\"></span>'  // mem
    				 +'<span class="proc proc-state ' + cpuStateCls + '" title=\"CPU占用率: '+pcpu+'\"></span>'; // cpu
	    	}
	    	return htm;
	    },
		//change perf value
		changePerfVal : function(id,proportion,value){
	        var div = HtmlHelper.id(id+"T");
	        
	        if (div==undefined) {
	        	return;
	        }
	        
	        var color;
	        if(proportion<40){
	            color ="#00FF33";
	        }else if(proportion<70){
	            color ="orange";
	        }else if(proportion >100){
	            proportion = 100;
	            color ="red";
	        }else{
	            color ="red";
	        }
	        div.style.height = proportion+"%";
	        div.style.backgroundColor = color;
	        
	        var divT = HtmlHelper.id(id);
	        if (id.indexOf("_connRate")>-1) {
	        	divT.innerHTML= value == undefined ? "-":value;
	        }
	        else {
	        	divT.innerHTML=proportion+"%";
	        }
	    },
	    delMacBox:function(jsonObj) {
	    	HtmlHelper.del(jsonObj.ip);
	    },
	    //-----------------------------NODE--------------------------------
	    /**
	     * TODO: NODE BOX
	     */
	    //add NODE BOX
	    addNodeBox:function(jsonObj) {
	    	var procState = '';
	    	if(jsonObj['node.pid']){
	    		var procs;
	    		if(jsonObj['node.procs']){
	    			procs = eval("("+jsonObj['node.procs']+")");
	    		}else{
	    			var macinfo=app.mdata("macinfo");
	    			var node=macinfo[jsonObj["ip"]];
	    			var procsStr='{}';
	    			if(node){
	    				procsStr=node["node.procs"];
	    			}
	    			if (procsStr) {
	    				procs = eval("("+procsStr+")");
	    			}
	    		}
	    		if (procs) {
	    			procState = this.buildProcStateHtm(procs[jsonObj['node.pid']]);
	    		}
	    	}

	    	var stateCss = this.getNodeStateCss(jsonObj["node.state"]);
	    	var nodeContent ="<div id=\""+jsonObj.id+"\" class=\"nodeprocess "+stateCss+"\" onclick=\"javascript:app.controller.showProcessWindow(\'"+jsonObj.ip+"\',\'"+jsonObj["node.pid"]+"\','ProcessChartWnd','buildProcessChart','createProcessChart')\"><span class='processInfo'><span class='pid'>["+jsonObj["node.pid"]+"]</span>&nbsp;"+jsonObj.name+"</span>"+procState+"</div>";
		    var nodeDiv = HtmlHelper.id(jsonObj.ip+"_nodes");
		    if (nodeDiv!=undefined) {
		        nodeDiv.innerHTML += nodeContent;
		    }
		        
	    },
	    //update NODE BOX
	    updateNodeBox :  function(jsonObj){
	        
	        if(HtmlHelper.id(jsonObj.id)==undefined){
	           this.addNodeBox(jsonObj);
	           return;
	        };

	        var procState = '';
	    	if(jsonObj['node.pid']){
	    		var procs;
	    		if(jsonObj['node.procs']){
	    			procs = eval("("+jsonObj['node.procs']+")");
	    		}else{
	    			var macinfo=app.mdata("macinfo");
	    			var node=macinfo[jsonObj["ip"]];
	    			var procsStr='{}';
	    			if(node){
	    				procsStr=node["node.procs"];
	    			}
	    			if (procsStr) {
	    				procs = eval("("+procsStr+")");
	    			}
	    		}
	    		if (procs) {
	    			procState = this.buildProcStateHtm(procs[jsonObj['node.pid']]);
	    		}
	    	}
	        var stateCss = this.getNodeStateCss(jsonObj["node.state"]);
	        var nodeContent ="<span class='processInfo'><span class='pid'>["+jsonObj["node.pid"]+"]</span>&nbsp;"+jsonObj.name +"</span>"+ procState;
	        var nodeDiv = HtmlHelper.id(jsonObj.id);
	        nodeDiv.className="nodeprocess "+stateCss;
	        nodeDiv.innerHTML = nodeContent;
	    },
	    //delete NODE BOX
	    delNodeBox : function(jsonObj) {
	    	HtmlHelper.del(jsonObj.id);
	    },
	    //get node state css class
	    getNodeStateCss : function(state){
	    	
	        if(!state){
	            return "dying";
	        }
	        var i = parseInt(state);
	        
	        if(i == 0){
	            return "dying";
	        }else if(i>0){
	            return "live";
	        }else if(i<0){
	            return "dead";
	        }
	    },

	    //--------------------------------MAC Detail---------------------------------------	  
	    /**
	     * TODO : MAC DETAIL
	     */
		buildMacDetail:function(ip) {
			var dObj=app.mdata("macinfo")[ip];
			//detail
			var divContent=this.buildMacDetailTopContent(dObj);
			//node info		
			var nodes=app.mdata("nodeinfo");
			//the monitoragent node info
			var maNodeInfo;
			for (var nodeKey in nodes) {
				var nodeObj=nodes[nodeKey];
				if (nodeObj["ip"]==ip) {
					divContent+=this.buildMacDetailNodeInfo(nodeObj);
					
					if (nodeObj["node.profile"].indexOf("ma")==0||nodeObj["name"]=="监控代理程序") {
						maNodeInfo=nodeObj;
					}
				}
			}
			//process info
			var procs=eval("("+dObj["node.procs"]+")");
			
			var handler=function(proc) {
				//we should deliver the maNodeInfo to the process info panel as we need ctrl the process via MonitorAgent
				divContent+=app.controller.buildMacProcessInfo(proc,maNodeInfo);
			};
			
			this.handleProcess(procs,handler);

			divContent+=this.buildMacDetailEndContent();
	        return divContent;
		},
		//Ultron container only
		buildUContainerDetail:function(ip) {
			var dObj=app.mdata("macinfo")[ip];
			//node info		
			var nodes=app.mdata("nodeinfo");
			var maNodeInfo;
			var nodeObj;
			for (var nodeKey in nodes) {
				var aNodeObj=nodes[nodeKey];
				if (aNodeObj["ip"]==ip) {
					if (aNodeObj["node.profile"].indexOf("ma")==0||aNodeObj["name"]=="监控代理程序") {
						maNodeInfo=aNodeObj;
						continue;
					}
					
					if (aNodeObj["node.profile"].indexOf("ua")==0||aNodeObj["name"]=="Ultron代理程序") {
						nodeObj=aNodeObj;
					}
				}
			}
			
			//detail
			var divContent=this.buildUContainerDetailTopContent(nodeObj);
			
			var handler=function(containerObj) {
				//we should deliver the maNodeInfo to the process info panel as we need ctrl the process via MonitorAgent
				divContent+=app.controller.buildUContainerDetailInfo(containerObj,nodeObj,maNodeInfo);
			};
			
			this.handleContainer(nodeObj,handler);

			divContent+=this.buildMacDetailEndContent();
	        return divContent;
		},
		buildMacDetailEndContent :function() {
			return "</div></div>";
		},
		//process info
		buildMacProcessInfo:function(jsonObj,maNodeInfo) {
			var str="<div class=\"contentDiv\"><div class=\"shine2\"></div>" +
					"<span class=\"title\">服务进程</span>" +
					"<div class=\"kv\">" +
			        "   <span class=\"kvField\">名称</span><span>：</span>"+this.getJavaProName(jsonObj) +"&nbsp;<span style='font-size:14px;color:#bbbbbb;'>["+jsonObj["pid"]+"]</span>" +
			        "</div>"+
			        "<div class=\"kv\">" +
			        "   <span class=\"kvField\">端口</span><span>：</span>"+jsonObj["ports"] +
			        "</div>";

			var tags=jsonObj["tags"];
			
			var isWatching=false;
			
			if (undefined!=tags) {

				if(undefined!=tags["starttime"]) {
					var startTime = parseFloat(tags["starttime"]);
					str+= "<div class=\"kv\"><div class=\"kvField\">启动时间<span>：<span class='kvSubValue'>"+TimeHelper.getTime(startTime)+"</span></div></div>";
				}			
				str+= "<div class=\"kv\"><div class=\"kvField\">属性<span>：</span></div>";
				
				for(var key in tags) {
					str+="<div style='margin-left:5px;'><span class=\"kvSubField\">"+key+"</span> : <span style='font-size:14px;color:#bbbbbb;'>"+tags[key]+"</span></div>";
					
					if (key=="watch"&&tags[key].toLowerCase()=="true") {
						isWatching=true;
					}
				}			
				
				str+="</div>";
			}
			
			/**
             * build control panel
             */
            str+="<div class=\"kv\">"+this.buildProcCtrlPanel(jsonObj,maNodeInfo,isWatching)+"</div></div>";
						
			return str;
		},
		//node info
		buildMacDetailNodeInfo :  function(jsonObj){
			var str= "            <div class=\"contentDiv\"><div class=\"shine2\"></div>" +
	            "                <span class=\"title\">节点进程</span><span style='font-size:14px;color:#bbbbbb;'>["+jsonObj["id"]+"]</span><span class=\"timeTitle\">"+TimeHelper.getTime(jsonObj["clientTimestamp"])+"</span><br/>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">名称</span><span>：</span>"+jsonObj["name"] +"&nbsp;<span style='font-size:14px;color:#bbbbbb;'>["+jsonObj["node.pid"]+"]</span>"+
	            "                </div>" +	
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">组</span><span>：</span>"+jsonObj["group"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">配置</span><span>：</span>"+jsonObj["node.profile"] +
	            "                </div>" +
                    "               <div class=\"kv\">" +
                    "                  <span class=\"kvField\">能力</span><span>：</span>"+this.formatter.feature(jsonObj["node.feature"],jsonObj) +
	            "              </div>" +
	            "               <div class=\"kv\">" +
	            "                  <span class=\"kvField\">Http服务</span><span>：</span>"+this.formatter.services(jsonObj["node.services"]) +
	            "              </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">所属心跳地址</span><span>：</span>"+jsonObj["node.hbserver"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">路径</span><span>：</span>"+jsonObj["node.root"] +
	            "                </div>" +
		    "                <div class=\"kv\">" +
                    "                    <span class=\"kvField\">版本</span><span>：</span>"+jsonObj["node.version"] +
                    "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">端口</span><span>：</span>";
			
				var macinfo=app.mdata("macinfo");
				var procs=eval("("+macinfo[jsonObj["ip"]]["node.procs"]+")");
				var pid=jsonObj["node.pid"];
				for (var key in procs) {
					
					if (pid==key) {
						str+=procs[key]["ports"];
						break;
					}					
				}
	            str+="</div>";
	            
	            /**
	             * build control panel
	             */
	            str+="<div class=\"kv\">"+this.buildNodeCtrlPanel(jsonObj)+"</div></div>";
	            
	            return str;
	    },
	    //Ultron container info
	    buildUContainerDetailInfo : function(containerObj,nodeObj,maNodeInfo){
	    	var str="<div class=\"contentDiv\"><div class=\"shine2\"></div>" +
				"<span class=\"title\">Container</span>" +
				"<div class=\"kv\">" +
				"   <span class=\"kvField\">名称</span><span>：</span>"+containerObj["name"]+"&nbsp;<span style='font-size:14px;color:#bbbbbb;'>["+containerObj["pid"]+"]</span>" +
				"</div>"+
				"<div class=\"kv\">" +
				"   <span class=\"kvField\">状态</span><span>：</span>"+containerObj["status"] +
				"</div>";
	    	
			str+= "<div class=\"kv\"><div class=\"kvField\">属性<span>：</span></div>";
				
			for(var key in containerObj) {
				str+="<div style='margin-left:5px;'><span class=\"kvSubField\">"+key+"</span> : <span style='font-size:14px;color:#bbbbbb;'>"+containerObj[key]+"</span></div>";
			}			
				
			str+="</div>";
			
			/**
             * build control panel
             */
            str+="<div class=\"kv\">"+this.buildUContainerCtrlPanel(containerObj,nodeObj,maNodeInfo)+"</div></div>";
						
			return str;
	    },
	    buildUContainerCtrlPanel : function(containerObj,nodeObj,maNodeInfo) {
	    	
	    	if(UserFilterCache.nomapping){
	    		return;//权限设置
	    	}
	    	
	    	var containerType = containerObj["containerType"];
	    	var name = containerObj["name"];
	    	var nodeCtrlUrl=this.getNodeCtrlUrl(nodeObj);
	    	
	    	//权限设置
	    	var groupName = maNodeInfo.group;
	    	var ctrlAuthor = false;
	    	if(UserFilterCache.ctrls[groupName]!=undefined && UserFilterCache.ctrls[groupName]["procctrl"]!=undefined && UserFilterCache.ctrls[groupName]["procctrl"]=="T"){
	    		ctrlAuthor=true;
	    	}
	    	
	    	var sb = new StringBuffer();
	    	if((UserFilterCache.all || ctrlAuthor) && "ultron" == containerType){
	    		sb.append("<button class=\"btn btn-danger\" onclick='app.controller.openUContainerCtrlConfirm(\"startUContainer\",\""+name+"\",\""+nodeCtrlUrl+"\")'>启动容器</button>");
	    		sb.append("&nbsp;<button class=\"btn btn-danger\" onclick='app.controller.openUContainerCtrlConfirm(\"stopUContainer\",\""+name+"\",\""+nodeCtrlUrl+"\")'>停止容器</button>");
	    		sb.append("&nbsp;<button class=\"btn btn-danger\" onclick='app.controller.openUContainerCtrlConfirm(\"removeUContainer\",\""+name+"\",\""+nodeCtrlUrl+"\")'>删除容器</button>");
	    	}
	    	return sb.toString();
	    },
	    
	    buildUContainerDetailTopContent : function(nodeObj){
	         var dockerInfo = eval("("+nodeObj["node.docker.info"]+")");
	    	 return  " <div class=\"funcDiv2\" >\n" +
	            "        <div class=\"topDiv\">\n" +
	            "            <span class=\"hostTitle\">容器管理</span><br/>\n" +
	            "            <span class=\"ipTitle\">"+nodeObj["ip"]+"</span>\n" +
	            "            <div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('MacUContainerWnd',undefined,'MacDetailWnd')\"></div>\n" +
	            "        </div>\n"+
	            "        <div>\n" +
	            "            <div class=\"contentDiv\"><div class=\"shine2\"></div>" +
	            "                <span class=\"title\">Docker</span><br/>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">docker客户端版本</span><span>：</span>"+ dockerInfo["client.version"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">docker客户端API版本</span><span>：</span>"+ dockerInfo["client.api.version"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">客户端go语言版本</span><span>：</span>"+ dockerInfo["client.go.version"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">docker服务端版本</span><span>：</span>"+ dockerInfo["server.version"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">docker服务端API版本</span><span>：</span>"+ dockerInfo["server.api.version"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">服务端go语言版本</span><span>：</span>"+ dockerInfo["server.go.version"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">Container个数</span><span>：</span>"+ dockerInfo["containers"] +
	            "                </div>" +
	            "            </div>" ;
	    },
	    
		buildMacDetailTopContent : function(resultObj){
	        return  " <div class=\"funcDiv2\" >\n" +
	            "        <div class=\"topDiv\">\n" +
	            "            <span class=\"hostTitle\">"+resultObj["host"]+"</span><br/>\n" +
	            "            <span class=\"ipTitle\">"+resultObj["ip"]+"</span>\n" +
	            "            <div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('MacDetailWnd')\"></div>\n" +
	            "        </div>\n" +
	            "        <div>\n" +
	            "            <div class=\"contentDiv\"><div class=\"shine2\"></div>" +
	            "                <span class=\"title\">系统</span><br/>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">操作系统</span><span>：</span>"+resultObj["os.type"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">系统架构</span><span>：</span>"+resultObj["os.arch"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">MAC地址</span><span>：</span>"+resultObj["os.mac"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">CPU数量</span><span>：</span>"+resultObj["os.cpu.number"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">内存最大值</span><span>：</span>"+CommonHelper.getB2Human(resultObj["os.cpu.maxmem"],true)+"&nbsp;("+resultObj["os.cpu.maxmem"]+")" +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">磁盘</span><span>：</span>"+this.formatter.disk(resultObj) +
	            "                </div>" +
	            "				 <div class=\"kv\">" +
	            "                    <span class=\"kvField\">网卡</span><span>：</span>"+this.formatter.netcard(resultObj) +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">标签</span><span>：</span>"+resultObj["node.tags"] +
	            "                </div>" +
	            "            </div>" +
	            "            <div class=\"contentDiv\"><div class=\"shine2\"></div>" +
	            "                <span class=\"title\">JAVA</span><br/>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">JVM</span><span>：</span>"+resultObj["os.java.vm"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">版本</span><span>：</span>"+resultObj["os.java.ver"] +
	            "                </div>" +
	            "                <div class=\"kv\">" +
	            "                    <span class=\"kvField\">路径</span><span>：</span>"+resultObj["os.java.home"] +
	            "                </div>" +
	            "            </div>";
	    },
	    formatter: {
	    	disk:function(resultObj) {
	    		 var diskStr = resultObj["os.io.disk"];
	    		 var os = resultObj["os.type"]; 
	    		 diskStr=diskStr.replace(/:\\/g,":");
	    		 var disk=eval("("+diskStr+")");
	    		 var sb=new StringBuffer();
	    		 
	    		 for(path in disk) {
	    		 	if(path.indexOf('/')==0 || os.indexOf('Windows') == 0){
		    			 var total="<span class='kvSubValue'>"+CommonHelper.getKB2Human(disk[path]["total"],true)+"</span>";
		    			 var free="<span class='kvSubValue'>"+CommonHelper.getKB2Human(disk[path]["free"],true)+"</span>";
		    			 var use="<span class='kvSubValue'>"+CommonHelper.getKB2Human(disk[path]["use"],true)+"</span>";
		    			 var useRate="<span class='kvSubValue'>"+disk[path]["useRate"]+"</span>";
		    			 
		    			 if(disk[path]["totalInode"]!=undefined){
		    				 //linux
		    				 var totalInode="<span class='kvSubValue'>"+disk[path]["totalInode"]+"</span>";
			    			 var useInode="<span class='kvSubValue'>"+disk[path]["useInode"]+"</span>";
			    			 var freeInode="<span class='kvSubValue'>"+disk[path]["freeInode"]+"</span>";
			    			 var useRateInode="<span class='kvSubValue'>"+disk[path]["useRateInode"]+"</span>";
		    				 sb.append("<div class='kvField'>"+"<span class='kvSubField' style='display:inline-block;width:130px;'>"+path+"</span>"+":&nbsp;使用率"+useRate+",&nbsp;使用量"+use+",&nbsp;剩余量"+free+",&nbsp;总量"+total+";&nbsp;&nbsp;&nbsp;inode使用率"+useRateInode+",&nbsp;inode使用量"+useInode+",&nbsp;inode剩余量"+freeInode+",&nbsp;inode总量"+totalInode+"</div>");
		    				  }
		    			 else{
		    				 //windows
		    				 sb.append("<div class='kvField'>"+"<span class='kvSubField' style='display:inline-block;width:130px;'>"+path+"</span>"+":&nbsp;使用率"+useRate+",&nbsp;使用量"+use+",&nbsp;剩余量"+free+",&nbsp;总量"+total+"</div>");		 
		    			 }
	    			}
	    		 }
	    		 
	    		 return sb.toString();
	    	},
	    	netcard:function(resultObj) {
	
	    		 var netcardStr = resultObj["os.netcard"];
	    		 var netcards=eval("("+netcardStr+")");
	    		 var sb=new StringBuffer();
	    		 
	    		 for(netcard in netcards) {
	    			 sb.append("<div class='kvField'>"+"<span class='kvSubField' style='display:inline-block;width:130px;'>"+netcard+"</span>:");
	    			 var i=0;
	    			 for(ip in netcards[netcard]){
	    				 var ipInfo="<span class='kvSubValue'>"+ip+"</span>";
			    	     var mask="<span class='kvSubValue'>"+netcards[netcard][ip]["mask"]+"</span>";
			    		 var bcast="<span class='kvSubValue'>"+netcards[netcard][ip]["bcast"]+"</span>";
			    		 
			    		 if(i!=0){
			    			 sb.append("<div class='kvField'>"+"<span class='kvSubField' style='display:inline-block;width:130px;'></span> "); 
			    		 }
			    		 i++;
			    		 sb.append("&nbsp;IP "+ipInfo+",&nbsp;子网掩码 "+mask+",&nbsp;广播地址 "+bcast+"</div>");
	    			 }
	    		 }
	    		 
	    		 return sb.toString();
	    	},
	    	feature:function(f,nodeObj) {
	    		var fts=eval("("+f+")");
	    		
	    		var nodeCtrlUrl=app.controller.getNodeCtrlUrl(nodeObj);
	    		
	    		var nodeInfo={id:nodeObj["id"],name:nodeObj["name"],url:nodeCtrlUrl,ip:nodeObj["ip"]};
				
	    		var sb=new StringBuffer();
	    		for(ft in fts) {
	    			
	    			nodeInfo["feature"]=ft;
	    			
	    			var nodeObjStr=StringHelper.obj2str(nodeInfo);
	    			
	    			 sb.append("<div class='kvField'>"+"<span class='kvSubField' style='display:inline-block;width:130px;'>"+ft+"</span>"+":&nbsp;"+StringHelper.obj2str(fts[ft]));
	    			 
	    			 /*
	    			  * TODO: the feature hot start & stop, not finished
	    			 sb.append("<div align='right'>");	    			 
	    			 sb.append("<button class=\"btn btn-warning\" onclick='app.controller.openNodeCtrlConfirm(\"fstart\","+nodeObjStr+")'>重启</button>");
	    			 sb.append("&nbsp;");
	    			 sb.append("<button class=\"btn btn-danger\" onclick='app.controller.openNodeCtrlConfirm(\"fstop\","+nodeObjStr+")'>停止</button>");
	    			 sb.append("</div>")
	    			 */
	    			 sb.append("</div>");
	    		}
	    		return sb.toString();
	    	},
	    	services:function (s) {
	    		var svs=eval("("+s+")");
	    		var sb=new StringBuffer();
	    		
	    		for(sv in svs) {
	    			sb.append("<div class='kvField'>"+"<span class='kvSubField' style='display:inline-block;'>"+sv+"</span>"+":&nbsp;"+svs[sv]+"</div>");

	    		}
	    		return sb.toString();
	    	}
	    },

	    //--------------------------------MAC Perf Chart---------------------------------------
	    /**
	     * TODO: MAC Chart
	     */
	    chartTimer:undefined,
	    lastClientTimestamp:-1,
		buildMacChart: function(ip) {
			var dObj=app.mdata("macinfo")[ip];
			var diskStr=dObj["os.io.disk"];
			diskStr=diskStr.replace(/:\\/g,":");
	    	var disks=eval("("+diskStr+")");			
			var diskSeries=[];
			var i=0;
			for( disk in disks){
				if(disk.indexOf('/')==0){
					continue;
				}
				var diskColor;
				if(i<10){
					diskColor=colors[i];
				}else{
					diskColor='#'+('00000'+(Math.random()*0x1000000<<0).toString(16)).substr(-6);
				}
				if(disk.indexOf('/')==0){
					continue;
				}
				var diskIoReadSeries={
					name:disk+"_read(KB/s)",
		        	color:diskColor,
		        	data:[]
				};
				i++;
				if(i<10){
					diskColor=colors[i];
				}else{
					diskColor='#'+('00000'+(Math.random()*0x1000000<<0).toString(16)).substr(-6);
				}
				var diskIoWriteSeries={
					name:disk+"_write(KB/s)",
		        	color:diskColor,
		        	data:[]
				};
				diskSeries.push(diskIoReadSeries);
				diskSeries.push(diskIoWriteSeries);
				i++;
			}
			diskIoChartCfg.series=diskSeries;
			window["appcharts"].bulid(diskIoChartCfg);
			var divContent=this.buildMacChartContent(dObj);
			return divContent;
		},
		buildMacChartContent : function(resultObj){
			
			var backScript="app.controller.closeWindow('MacChartWnd','destroyMacChart')";
			
			//if open link
			var view=HtmlHelper.getQParam("view");
			
			if (view!=undefined) {
				var from=HtmlHelper.getQParam("from");
				var fview=HtmlHelper.getQParam("fview");
				var fparam=HtmlHelper.getQParams()["fparam"];
				var burl=from+"?view="+fview+"&param="+fparam;
				backScript="window.parent.jumpUrl('"+burl+"','应用监控')";
			}
			
	        return  " <div class=\"funcDiv2\" >\n" +
	            "        <div class=\"topDiv\">\n" +
	            "            <span class=\"hostTitle\">"+resultObj["host"]+"</span><br/>\n" +
	            "            <span class=\"ipTitle\">"+resultObj["ip"]+"</span>\n" +
	            "            <div class=\"icon-signout\" onclick=\"javascript:"+backScript+"\"></div>\n" +
	            "        </div>\n" +
	            "            <div class='contentDiv' id='contentDiv' ><div class=\"shine2\"></div>" +
	            "        </div>" +
	            "     </div>";
		},
		runMacChart: function(ip,forceUpdate,needCache) {
			
			var datas = app.mdata().macinfo[ip];
			var clientTimestamp=datas["clientTimestamp"];
			
			if (true!=forceUpdate) {
				//only update when there is an update data
				if (this.lastClientTimestamp>-1) {
					if (clientTimestamp<=this.lastClientTimestamp) {
						return;
					}
				}
			}
			
			this.lastClientTimestamp=clientTimestamp;
			//cpu
			var cpuPro = datas["os.cpu.load"];
			//mem
			var memoryMax = datas["os.cpu.maxmem"];
			var memoryFreeme = datas["os.cpu.freemem"];
			var memoryUsed=(memoryMax - memoryFreeme);
			var memoryPro = Math.ceil((memoryUsed/ memoryMax) * 100);
			
			//connections
	        var curconn=datas["os.conn.cur"];
	        //process total mem
	        var procMemSum=0;
	        //process total flux
	        var inFluxSum=0;
	        var outFluxSum=0;

	        // sum real connections from proc's conn
	        if(datas['node.procs']){
	    		var procs = JSON.parse(datas['node.procs']);
	    		var procConnSum = 0;
	    		
	    		for (var k in procs){
	    			if(procs[k].tags.conn){
	    				procConnSum += parseInt(procs[k].tags.conn);
	    			}
	    			
	    			if(procs[k].tags.mem){ 
	    				procMemSum+=parseInt(procs[k].tags.mem)*1024;
	    			}

	    			if(procs[k].tags["in"]){ 
	    				inFluxSum+=parseFloat(procs[k].tags["in"]);
	    			}

	    			if(procs[k].tags.out){ 
	    				outFluxSum+=parseFloat(procs[k].tags.out);
	    			}
	    		}
	    		inFluxSum=inFluxSum.toFixed(2);
	    		if(procConnSum!=0){
	    			curconn = procConnSum;
	    		}
	    	}
	        
	        var procMemSumRate=Math.ceil((procMemSum/ memoryMax) * 100);

	        /**;
	         * prepare DATA
	         */
			var gbMemUsed=CommonHelper.getB2Human(memoryUsed,false,3);
			var gbProcMemUsed=CommonHelper.getB2Human(procMemSum,false,3);
			
	        var curData={
	        		cpuProData:[{"x":clientTimestamp, "y": cpuPro}],
	        		memProData:[{"x":clientTimestamp, "y": memoryPro}],
	        		procMemRateData:[{"x":clientTimestamp, "y": procMemSumRate}],
	        		memUseData:[{"x":clientTimestamp, "y": gbMemUsed}],
	        		memProcUseData:[{"x":clientTimestamp, "y": gbProcMemUsed}],
	        		connData:[{"x":clientTimestamp, "y": curconn}],
	        		fluxData:[[{"x":clientTimestamp, "y": inFluxSum}],[{"x":clientTimestamp, "y": outFluxSum}]],
	        		diskIoData:[]
	        }
			
			/**
	         * cache disk DATA
	         */
	        var diskStr=datas["os.io.disk"];
			diskStr=diskStr.replace(/:\\/g,":");
	    	var disks=eval("("+diskStr+")");

	        for( disk in disks){
	        	if(disk.indexOf('/')==0){
	        		continue;
	        	}
	    		var yVal=disks[disk]["disk_read"];
	    		if(yVal!=undefined){
	    			var pData=[{"x":clientTimestamp, "y": yVal}];
	    			curData.diskIoData[curData.diskIoData.length]=pData;
	    			this.putDataCache(ip+"_"+disk+"_read", pData);
	    		}
	    		yVal=disks[disk]["disk_write"];
	    		if(yVal!=undefined){
	    			var pData=[{"x":clientTimestamp, "y": yVal}];
	    			curData.diskIoData[curData.diskIoData.length]=pData;
	    			this.putDataCache(ip+"_"+disk+"_write", pData);
	    		}
	    	}
	        /**
	         * cache DATA
	         */
	        this.putDataCache(ip+"_cpurate", curData.cpuProData);
	        this.putDataCache(ip+"_memrate", curData.memProData);
	        this.putDataCache(ip+"_procmemrate", curData.procMemRateData);
	        this.putDataCache(ip+"_mem", curData.memUseData);
	        this.putDataCache(ip+"_procmem", curData.memProcUseData);
	        this.putDataCache(ip+"_conn", curData.connData);
	        this.putDataCache(ip+"_in", curData.fluxData[0]);
	        this.putDataCache(ip+"_out", curData.fluxData[1]);
	        this.flushDataCache();
	        
	        if (needCache==true) {
	        	/**
	        	 * use cache DATA
	        	 */
	        	curData={
		        		cpuProData:this.getDataCache(ip+"_cpurate"),
		        		memProData:this.getDataCache(ip+"_memrate"),
		        		procMemRateData:this.getDataCache(ip+"_procmemrate"),
		        		memUseData:this.getDataCache(ip+"_mem"),
		        		memProcUseData:this.getDataCache(ip+"_procmem"),
		        		connData:this.getDataCache(ip+"_conn"),
		        		fluxData:[this.getDataCache(ip+"_in"),this.getDataCache(ip+"_out")],
		        		diskIoData:[]
	        	}

	        	for(disk in disks){
					if(disk.indexOf('/')==0){
	        			continue;
	        		}
		    		var pData=this.getDataCache(ip+"_"+disk+"_read");
		    		curData.diskIoData[curData.diskIoData.length]=pData;
		    	    pData=this.getDataCache(ip+"_"+disk+"_write");
		    		curData.diskIoData[curData.diskIoData.length]=pData;		    		
	    		}
	        }
	        
			window["appcharts"].run("cpuChart",[
				curData.cpuProData,
				curData.memProData,
				curData.procMemRateData
			]);			
			window["appcharts"].run("memChart",[
 				curData.memUseData,
 				curData.memProcUseData
 			]);
			window["appcharts"].run("connChart",[
			    curData.connData
			]);
			window["appcharts"].run("fluxChart",curData.fluxData);
			window["appcharts"].run("diskIoChart",curData.diskIoData);
		},
		createMacChart : function(ip){
			
			var _this=this;
			
			_this.runMacChart(ip,true,true);
			
			this.chartTimer = setInterval(function () {
				_this.runMacChart(ip);
			}, 5000);
		},
		destroyMacChart : function() {
			if (this.chartTimer!=undefined) {
				window.clearInterval(this.chartTimer);
			}
			window["appcharts"].reset("cpuChart");
			window["appcharts"].reset("memChart");
			window["appcharts"].reset("connChart");
			window["appcharts"].reset("fluxChart");
			window["appcharts"].reset("diskIoChart");
		},
		//--------------------------------Process Chart---------------------------------------
		/**
		 * TODO:Process Chart
		 */
		buildProcessChart: function(ip,pid) {
			var dObj=app.mdata("macinfo")[ip];
			var divContent=this.buildProcessChartContent(dObj,pid);
			return divContent;
		},
		buildProcessChartContent : function(resultObj,pid){
			var procs=eval("("+resultObj["node.procs"]+")");
			var proc=procs[pid];
			
			if (proc==undefined) {
				alert("进程号["+pid+"]的进程在["+resultObj["ip"]+"]不存在！")
				return;
			}
			
			var ports=proc["ports"];
			var connSeries=[
	        	{
		        	name:"进程连接数",
		        	color:"#EEEE00",
		        	data:[]
	        	}
	   		];
	   		var fluxSeries=[
	        	{
		        	name:"入口流量(KB/s)",
		        	color:"#EEEE00",
		        	data:[]
	        	},
	        	{
		        	name:"出口流量(KB/s)",
		        	color:"#EEEA00",
		        	data:[]
	        	}

	   		];
			for(var i=0;i<ports.length;i++){
				var port=ports[i];
				var portColor;
				if(i<10){
					portColor=colors[i];
				}else{
					portColor='#'+('00000'+(Math.random()*0x1000000<<0).toString(16)).substr(-6);
				}
				var portConnSeries={
					name:port,
		        	color:portColor,
		        	data:[]
				};
				var portFluxInSeries={
					name:"in_"+port,
		        	color:portColor,
		        	data:[]
				};
				var portFluxOutSeries={
					name:"out_"+port,
		        	color:portColor,
		        	data:[]
				};
				connSeries.push(portConnSeries);
				fluxSeries.push(portFluxInSeries);
				fluxSeries.push(portFluxOutSeries);
			}
			processConnChartCfg.series=connSeries;
			processFluxChartCfg.series=fluxSeries;
			
			window["appcharts"].bulid(processConnChartCfg);
			window["appcharts"].bulid(processFluxChartCfg);
			
			var backScript="javascript:app.controller.closeWindow('ProcessChartWnd','destroyProcessChart')";
			
			//if open link
			var view=HtmlHelper.getQParam("view");
			
			if (view!=undefined) {
				var from=HtmlHelper.getQParam("from");
				var fview=HtmlHelper.getQParam("fview");
				var fparam=HtmlHelper.getQParams()["fparam"];
				var burl=from+"?view="+fview+"&param="+fparam;
				backScript="window.parent.jumpUrl('"+burl+"','应用监控')";
			}
			
			return  " <div class=\"funcDiv2\" >\n" +
	            "        <div class=\"topDiv\">\n" +
	            "            <span class=\"hostTitle\">"+resultObj["ip"]+"</span><br/>\n" +
	            "            <span class=\"ipTitle\">"+"<span class='pid'>["+pid+"]</span>&nbsp;"+app.controller.getJavaProName(proc)+"</span>\n" +       
	            "            <div class=\"icon-signout\" onclick=\""+backScript+"\"></div>\n" +
	            "        </div>\n" +
	            "            <div class='ProcessChartWndDiv' id='ProcessChartWndDiv' ><div class=\"shine2\"></div>" +
	            "        </div>" +
	            "     </div>";
		},
		runProcessChart: function(ip,pid,forceUpdate,needCache) {
			
			var datas = app.mdata().macinfo[ip];
			var procs=eval("("+datas["node.procs"]+")");
			var clientTimestamp=datas["clientTimestamp"];
			var proc=procs[pid];
			if (true!=forceUpdate) {
				//only update when there is an update data
				if (this.lastClientTimestamp>-1) {
					if (clientTimestamp<=this.lastClientTimestamp) {
						return;
					}
				}
			}
			
			this.lastClientTimestamp=clientTimestamp;
			var procInfo = proc["tags"];
			
			//cpu
			var cpuRate=procInfo["cpu"];
			//mem
			var mem=procInfo["mem"];
			//memRate
			var memRate=procInfo["memRate"];
			//conn
			var conn=procInfo["conn"];
			//flux
			var in_proc=procInfo["in"];
			var out_proc=procInfo["out"];
			//diskIo
			var disk_read=procInfo["disk_read"];
			var disk_write=procInfo["disk_write"];

			var memHuman=CommonHelper.getKB2Human(mem,false,2);
			
			var connPData=[{"x":clientTimestamp, "y": conn}];
			var inData=[{"x":clientTimestamp, "y": in_proc}];
			var outData=[{"x":clientTimestamp, "y": out_proc}];
			var diskReadData=[{"x":clientTimestamp, "y": disk_read}];
			var diskWriteData=[{"x":clientTimestamp, "y": disk_write}];

			var curData={
	        		cpuProData:[{"x":clientTimestamp, "y": cpuRate}],
	        		memProData:[{"x":clientTimestamp, "y": memRate}],
	        		memUseData:[{"x":clientTimestamp, "y": memHuman}],
	        		connData:[connPData],
	        		fluxData:[inData,outData],
	        		diskData:[diskReadData,diskWriteData]
	        };
			
			this.putDataCache(ip+"_"+pid+"_conn", connPData);
			this.putDataCache(ip+"_"+pid+"_in", inData);
			this.putDataCache(ip+"_"+pid+"_out", outData);			
			
			var ports=proc["ports"];
			
			for(var i=0;i<ports.length;i++){
				var port=ports[i];
				
				var yVal=procInfo["conn_"+port];				                
				if (yVal!=undefined) {
					var pData=[{"x":clientTimestamp, "y":yVal}];
					curData.connData[curData.connData.length]=pData;
					/**
					 * cache DATA port
					 */
					this.putDataCache(ip+"_"+pid+"_conn_"+port, pData);
				}

				var yVal=procInfo["in_"+port];				              
				if (yVal!=undefined) {
					var inData=[{"x":clientTimestamp, "y":yVal}];
					curData.fluxData[curData.fluxData.length]=inData;
					/**
					 * cache DATA port
					 */
					this.putDataCache(ip+"_"+pid+"_in_"+port, inData);
				}

				var yVal=procInfo["out_"+port];				             
				if (yVal!=undefined) {
					var outData=[{"x":clientTimestamp, "y":yVal}];
					curData.fluxData[curData.fluxData.length]=outData;
					/**
					 * cache DATA port
					 */
					this.putDataCache(ip+"_"+pid+"_out_"+port, outData);
				}
			}
	        
	        /**
	         * cache DATA
	         */
	        this.putDataCache(ip+"_"+pid+"_cpurate", curData.cpuProData);
	        this.putDataCache(ip+"_"+pid+"_memrate", curData.memProData);
	        this.putDataCache(ip+"_"+pid+"_mem", curData.memUseData);
	        this.putDataCache(ip+"_"+pid+"_read", diskReadData);
			this.putDataCache(ip+"_"+pid+"_write", diskWriteData);
	        
	        this.flushDataCache();
	        
	        if (needCache==true) {
	        	/**
	        	 * use cache DATA
	        	 */
	        	curData={
		        		cpuProData:this.getDataCache(ip+"_"+pid+"_cpurate"),
		        		memProData:this.getDataCache(ip+"_"+pid+"_memrate"),
		        		memUseData:this.getDataCache(ip+"_"+pid+"_mem"),
		        		connData:[this.getDataCache(ip+"_"+pid+"_conn")],
		        		fluxData:[this.getDataCache(ip+"_"+pid+"_in"),this.getDataCache(ip+"_"+pid+"_out")],
		        		diskData:[this.getDataCache(ip+"_"+pid+"_read"),this.getDataCache(ip+"_"+pid+"_write")]
	        	};
	        	
	        	for(var i=0;i<ports.length;i++){
	        		
	        		var port=ports[i];
	        		
	        		var pData=this.getDataCache(ip+"_"+pid+"_conn_"+port);
	        		var inData=this.getDataCache(ip+"_"+pid+"_in_"+port);
	        		var outData=this.getDataCache(ip+"_"+pid+"_out_"+port);
	        		if (pData==undefined) {
	        			continue;
	        		}
	        		
	        		curData.connData[curData.connData.length]=pData;
	        		curData.fluxData[curData.fluxData.length]=inData;
	        		curData.fluxData[curData.fluxData.length]=outData;
	        	}
	        }
	        
	        window["appcharts"].run("processChart",[
				curData.cpuProData,
				curData.memProData
			]);	
			
			window["appcharts"].run("processMemChart",[
			    curData.memUseData
			]);
			window["appcharts"].run("processConnChart",curData.connData);
			window["appcharts"].run("processFluxChart",curData.fluxData);
			window["appcharts"].run("processDiskIoChart",curData.diskData);
		},
		createProcessChart : function(ip,pid){
			
			var _this=this;
			
			_this.runProcessChart(ip,pid,true,true);
			
			this.chartTimer = setInterval(function () {
				_this.runProcessChart(ip,pid);
			}, 5000);
		},
		destroyProcessChart : function() {
			if (this.chartTimer!=undefined) {
				window.clearInterval(this.chartTimer);
			}
			window["appcharts"].reset("processChart");
			window["appcharts"].reset("processMemChart");
			window["appcharts"].reset("processConnChart");
			window["appcharts"].reset("processFluxChart");
			window["appcharts"].reset("processDiskIoChart");
		},
		//--------------------------Data Cache----------------------------
		/**
		 * TODO: DATA CACHE
		 */
		putDataCache:function(key,data) {
			
			if (dataCache[key]==undefined) {
				dataCache[key]=new List(10);
			}
			
			var ls=dataCache[key];
			
			for(var i=0;i<data.length;i++) {
				
				var check=false;
				
				for(var j=0;j<ls.count();j++) {
					var cd=ls.get(j);
					
					if (cd.x==data[i].x) {
						check=true;
						break;
					}
				}
				
				if (check==false) {
					dataCache[key].add(data[i]);
				}
			}			
		},
		getDataCache:function(key) {
			if (dataCache[key]==undefined) {
				return undefined;
			}
			
			return dataCache[key].toArray();
		},
		recoverDataCache:function() {
			
			var str=window["cachemgr"].get("godeye.uav.network.dcache");
			
			if (str==undefined) {
				return;
			}
			
			var tmp=eval("("+str+")");
			
			for(var key in tmp) {
				dataCache[key]=new List(10);
				dataCache[key].addall(tmp[key]);
			}
		},
		flushDataCache:function() {
			
			var tmp={};
			
			for(var key in dataCache) {
				
				tmp[key]=dataCache[key].toArray();
				
			}
			
			var str=JSON.stringify(tmp);//StringHelper.obj2str(tmp);
			
			window["cachemgr"].put("godeye.uav.network.dcache",str);
		},
		//--------------------------Control Panel----------------------------
		/**
		 * TODO: Control Panel
		 */
		//the following proc name should not be guide or kill
		specProcOperations: {
			"sshd":{guide:false,kill:false,apm:false},
			"cupsd":{guide:false,kill:false,apm:false}
		},
		//get node ctrl url
		getNodeCtrlUrl:function(nodeObj) {
			var services=eval("("+nodeObj["node.services"]+")");
			
			var nodeCtrlUrl;
			
			for(var skey in services) {
				if (skey=="nodeoperagent-NodeOperHttpServer-/node/ctrl") {
					nodeCtrlUrl=services[skey];
					break;
				}
			}
			
			return nodeCtrlUrl;
		},
		//node
		buildNodeCtrlPanel:function(nodeObj) {
			
			if(UserFilterCache.nomapping){
				return;//权限设置
			}
		
			var sb=new StringBuffer();
			
			var nodeCtrlUrl=this.getNodeCtrlUrl(nodeObj);
			
			var nodeObjStr=StringHelper.obj2str({id:nodeObj["id"],name:nodeObj["name"],url:nodeCtrlUrl,ip:nodeObj["ip"],pid:nodeObj["node.pid"],pname:nodeObj["name"],profile:nodeObj["node.profile"],
                                                version:nodeObj["node.version"] == undefined ? "UNKNOW" : nodeObj["node.version"],nodeRoot:nodeObj["node.root"],
                                                dockerContainer:nodeObj["node.docker.container"] == undefined ? "{}" : nodeObj["node.docker.container"]});
			//权限设置
			var groupName = nodeObj.group;
			var ctrlAuthor = false;
			if(UserFilterCache.ctrls[groupName]!=undefined && UserFilterCache.ctrls[groupName]["nodectrl"] !=undefined && UserFilterCache.ctrls[groupName]["nodectrl"]=="T"){
				ctrlAuthor=true;
			}
			
			if(UserFilterCache.all || ctrlAuthor){
				//设置系统属性
				sb.append("<button class=\"btn btn-info\" onclick='app.controller.openNodeCtrlPanel(\"sysProSetDialog\","+nodeObjStr+")'>修改属性</button>");

				//重启Node
				sb.append("&nbsp;<button class=\"btn btn-info\" onclick='app.controller.showWindow("+nodeObjStr+",\"NodeConfigWnd\",\"buildNodeConfigWnd\",\"loadNodeConfigWnd\")'>配置管理</button>");
				sb.append("&nbsp;<button class=\"btn btn-danger\" onclick='app.controller.openNodeCtrlConfirm(\"killproc\","+nodeObjStr+")'>重启节点</button>");
				sb.append("&nbsp;<button class=\"btn btn-danger\" onclick='app.controller.openNodeCtrlConfirm(\"stopuav\","+nodeObjStr+")'>停止节点</button>");
				sb.append("&nbsp;<button class=\"btn btn-danger\" onclick='app.controller.openNodeCtrlPanel(\"preUpgradeDialog\","+nodeObjStr+",\"\",\"升级节点\")'>升级节点</button>");
				
				if("监控代理程序"==nodeObj["name"]){
					sb.append("&nbsp;<button class=\"btn btn-danger\" onclick='app.controller.openNodeCtrlConfirm(\"installmof\","+nodeObjStr+")'>安装MOF</button>");
					sb.append("&nbsp;<button class=\"btn btn-danger\" onclick='app.controller.openNodeCtrlConfirm(\"uninstallmof\","+nodeObjStr+")'>卸载MOF</button>");
					sb.append("&nbsp;<button class=\"btn btn-danger\" onclick='app.controller.openNodeCtrlPanel(\"preUpgradeDialog\","+nodeObjStr+",\"uavmof\",\"升级MOF\")'>升级MOF</button>");
				}
				
				if("Ultron代理程序"==nodeObj["name"]){
					
					nodeObjStr=StringHelper.obj2str({url:nodeCtrlUrl,dockerContainer:nodeObj["node.docker.container"] == undefined ? "{}" : nodeObj["node.docker.container"]});
					
					sb.append("&nbsp;<button class=\"btn btn-info\" onclick='app.controller.openNodeCtrlPanel(\"createUContainerDialog\","+nodeObjStr+")'>创建容器</button>");
					sb.append("&nbsp;<button class=\"btn btn-info\" onclick='app.controller.showWindow(\""+nodeObj['ip']+"\",\"MacUContainerWnd\",\"buildUContainerDetail\")'>容器管理</button>");
				}
			
			}
			return sb.toString();
		},

		//process
		buildProcCtrlPanel:function(procObj,maNodeInfo,isWatching) {
			
			var sb=new StringBuffer();
			
			if(undefined!=procObj["tags"]["containerType"]){
				return sb.toString();
			}
			
			if(UserFilterCache.nomapping){
				return sb.toString();//权限设置
			}
			
			var procName=this.getJavaProName(procObj);
			
			var procOperations=(this.specProcOperations[procName]!=undefined)?this.specProcOperations[procName]:{guide:true,kill:true,apm:true};
			
			var sb=new StringBuffer();
			
			//get node ctrl url;
			var nodeCtrlUrl=this.getNodeCtrlUrl(maNodeInfo);
			
			var nodeObjStr=StringHelper.obj2str({id:maNodeInfo["id"],name:maNodeInfo["name"],url:nodeCtrlUrl,ip:maNodeInfo["ip"],pid:procObj["pid"],pname:procName,ports:procObj["ports"]});
			
			//sb.append("<div>"+nodeCtrlUrl+"</div>");
			
			//权限设置
			var groupName = maNodeInfo.group;
			var ctrlAuthor = false;
			if(UserFilterCache.ctrls[groupName]!=undefined && UserFilterCache.ctrls[groupName]["procctrl"] != undefined && UserFilterCache.ctrls[groupName]["procctrl"]=="T"){
				ctrlAuthor=true;
			}
			
			if(UserFilterCache.all || ctrlAuthor){
				if (procOperations["guide"]==true) {
					//设置值守
					if (isWatching==false) {
						sb.append("<button class=\"btn btn-info\" onclick='app.controller.openNodeCtrlConfirm(\"watch\","+nodeObjStr+")'>启用值守</button>");
					}
					else {
						sb.append("&nbsp;<button class=\"btn btn-warning\" onclick='app.controller.openNodeCtrlConfirm(\"unwatch\","+nodeObjStr+")'>停止值守</button>");
					}
				}
			}
			
			if(UserFilterCache.all || ctrlAuthor){
				if (procOperations["kill"]==true) {
					//停止
					sb.append("&nbsp;<button class=\"btn btn-danger\" onclick='app.controller.openNodeCtrlConfirm(\"killproc\","+nodeObjStr+")'>停止进程</button>");
					//重启
					sb.append("&nbsp;<button class=\"btn btn-danger\" onclick='app.controller.openNodeCtrlConfirm(\"restart\","+nodeObjStr+")'>重启进程</button>");
				}
			}
			
			return sb.toString();
		},
		//node ctrl
		openNodeCtrlPanel:function(id,nodeObj,softwareId,title) {
			//upgrade
			if(id=="preUpgradeDialog"){
	        	/**
	        	 * softwareId 适配 begin
	        	 */
            	if(softwareId==""){
            		var system = app.mdata("macinfo")[nodeObj.ip]["os.type"].toUpperCase();
        			var checkKey = "Windows".toUpperCase();
        			if(system.indexOf(checkKey)>=0 && nodeObj.nodeRoot.indexOf("healthmanager")>=0){
        				softwareId = "uavhm";
        			}else if(system.indexOf(checkKey)>=0 && nodeObj.nodeRoot.indexOf("agent")>=0){
        				softwareId = "uavagent";
        			}else{ //目录格式化查找
                		softwareId = nodeObj.nodeRoot.substr(nodeObj.nodeRoot.lastIndexOf('/') + 1);
        			}
            	}
            	/**
            	 * softwareId 适配 end
            	 */
				preUpgradeDialog.softwareId=softwareId;
				preUpgradeDialog.title=title+"<br/>ip："+nodeObj.ip+"<br/>softwareId："+softwareId;
				window["appdialogmgr"].remove("preUpgradeDialog");
				window["appdialogmgr"].build(preUpgradeDialog);
			}
			
			window["appdialogmgr"].open(id,nodeObj);
		},
		//node ctrl confirm
		openNodeCtrlConfirm:function(action,nodeObj) {
			
			var confirmMsg;
			var sucMsg;
			var failMsg;
			var target=nodeObj["name"]+"("+nodeObj["ip"]+")";
			var timeout=15000;
			
			var q={intent:action,request:{url:nodeObj["url"]}};
			
			var callback;
			//重启Node
			switch(action) {
			
				case "fstop":
					target="Feature["+nodeObj["feature"]+"],"+target;
					confirmMsg="确定要停止"+target+"?";
					sucMsg="停止"+target+"成功!";
					failMsg="停止"+target+"失败:";
					q["request"]["feature"]=nodeObj["feature"];
					break;
				case "fstart":
					target="Feature["+nodeObj["feature"]+"],"+target;
					confirmMsg="确定要重启"+target+"?";
					sucMsg="重启"+target+"成功!";
					failMsg="重启"+target+"失败:";
					q["request"]["feature"]=nodeObj["feature"];
					break;
				case "kill":
					confirmMsg="确定要重启节点["+target+"]?";
					sucMsg="停止节点["+target+"]成功，请关注重启结果是否完成!";
					failMsg="停止节点["+target+"]失败:";
					timeout=5000;
					break;
				case "killproc":
					target=nodeObj["pname"]+"["+nodeObj["pid"]+"]"+"("+nodeObj["ip"]+")";
					confirmMsg="确定要停止进程["+target+"]?";
					sucMsg="停止进程["+target+"]成功!";
					failMsg="停止进程["+target+"]失败:";
					q["request"]["pid"]=nodeObj["pid"];
					break;
				case "watch":
					target=nodeObj["pname"]+"["+nodeObj["pid"]+"]"+"("+nodeObj["ip"]+")";
					confirmMsg="确定要值守进程["+target+"]?";
					q["request"]["pid"]=nodeObj["pid"];
					callback=function(obj) {
						
						if (obj=="ERR") {
							alert("进程值守["+target+"]启用错误!");
							return;
						}
																		
						var res=obj["rs"];
						
						switch(res) {						
						case "1":
							alert("进程值守["+target+"]启用成功!");
							break;
						case "2":
							alert("该进程["+target+"]已经处于值守状态.");
							break;
						case "-1":
							alert("该进程["+target+"]不存在，无法启用值守!");
							break;
						default:
							if (obj["msg"]!=undefined) {
								alert(obj["msg"]);
							}
							break;
						}
					};
					break;
				case "unwatch":
					target=nodeObj["pname"]+"["+nodeObj["pid"]+"]"+"("+nodeObj["ip"]+")";
					confirmMsg="确定要停止值守进程["+target+"]?";
					sucMsg="进程值守["+target+"]停止成功!";
					failMsg="进程值守["+target+"]停止失败:";
					q["request"]["pid"]=nodeObj["pid"];
					callback=function(obj) {
						
						if (obj=="ERR") {
							alert("进程值守["+target+"]启用错误!");
							return;
						}
						
						var res=obj["rs"];
						
						switch(res) {						
						case "1":
							alert("进程值守["+target+"]停止成功!");
							break;
						case "-1":
							alert("该进程["+target+"]不在值守之列，所以不需要停止值守");
							break;
						default:
							if (obj["msg"]!=undefined) {
								alert(obj["msg"]);
							}
							break;
						}
					};
					break;
				case 'restart':
                    target=nodeObj["pname"]+"["+nodeObj["pid"]+"]"+"("+nodeObj["ip"]+")";
                    confirmMsg="确定要重启进程["+target+"]?";
                    sucMsg="停止节点["+target+"]成功，请关注重启结果是否完成!";
                    failMsg="停止节点["+target+"]失败:";
                    q["request"]["pid"]=nodeObj["pid"];
                    callback = function(obj){
                        if (obj=="ERR") {
                            alert("进程["+target+"]重启失败!");
                            return;
                        }
                        var res = obj["rs"];
                        if(!res){
                            alert("MonitorAgent不支持进程重启，请升级到最新版本!");
                            return;
                        }
                        switch(res) {
                        case "1":
                            alert("进程["+target+"]重启成功!");
                            break;
                        case "0":
                            alert("进程["+target+"]不存在!");
                            break;
                        case "-1":
                            alert("进程["+target+"]重启失败!");
                            break;
                        default:
							if (obj["msg"]!=undefined) {
								alert(obj["msg"]);
							}
							break;
                        }
                    }
                    break;
				case "stopuav":
					target=nodeObj["pname"]+"["+nodeObj["pid"]+"]"+"("+nodeObj["ip"]+")";
					confirmMsg="确定要停止进程["+target+"]?";
					sucMsg="停止进程["+target+"]成功!";
					failMsg="停止进程["+target+"]失败:";
					q["request"]["pid"]=nodeObj["pid"];
					q["request"]["profile"]=nodeObj["profile"];
					break;
				case "installmof":
					target=nodeObj["pname"]+"["+nodeObj["pid"]+"]"+"("+nodeObj["ip"]+")";
					confirmMsg="确定要安装MOF["+target+"]?";
					sucMsg="["+target+"]安装MOF成功!";
					failMsg="["+target+"]安装MOF失败:";
					q["request"]["container"]="tomcat";
					break;
				case "uninstallmof":
					target=nodeObj["pname"]+"["+nodeObj["pid"]+"]"+"("+nodeObj["ip"]+")";
					confirmMsg="确定要卸载MOF["+target+"]?";
					sucMsg="["+target+"]卸载MOF成功!";
					failMsg="["+target+"]卸载MOF失败:";
					q["request"]["container"]="tomcat";
					break;

			}			
			
			if (confirm(confirmMsg)) {
				
				var qStr=StringHelper.obj2str(q);
				
				AjaxHelper.call({
					url:"../../rs/godeye/node/ctrl",
					data: qStr,
					cache: false,
			        type: "post",
			        dataType: "html",
			        timeout:timeout,
			        success: function (data) {
			        	
			        	var obj=StringHelper.str2obj(data);
			        	var res = obj["rs"];
			        	if(callback!=undefined) {
			        		callback(obj);
			        	    return;
			        	}
			        	
			        	if (obj=="ERR"||res=="ERR") {
			        		alert(failMsg+data);
			        	}
			        	else {
			        		alert(sucMsg);
			        	}
			        },
			        error: function (data) {
			           alert(failMsg+data);
			        }
				});
			}
		},
		//container control
		openUContainerCtrlConfirm:function(action,name,url){
	    	
	    	var confirmMsg;
	    	var sucMsg;
	    	var failMsg;
	    	var data={intent:action,request:{url:url,containerName:name}};
	    	
	    	switch(action){
	    	case "startUContainer":
	    		confirmMsg="确定要启动" + name +"?";
	    		sucMsg="启动容器" + name +"发送命令成功!";
	    		failMsg="启动容器" + name +"发送命令失败";
	    		data["request"]["event"]="start";
	    		break;
	    	case "stopUContainer":
	    		confirmMsg="确定要停止" + name + "?";
	    		sucMsg="停止容器" + name +"发送命令成功!";
	    		failMsg="停止容器" + name +"发送命令失败";
	    		data["request"]["event"]="stop";
	    		break;
	    	case "removeUContainer":
	    		confirmMsg="确定要删除" + name + "?";
	    		sucMsg="删除容器" + name +"发送命令成功!";
	    		failMsg="删除容器" + name +"发送命令失败";
	    		data["request"]["event"]="remove";
	    	}
	    	var dataStr=StringHelper.obj2str(data);
	    	if(confirm(confirmMsg)){
	    		
	    		AjaxHelper.call({
	    			url:"../../rs/godeye/node/ctrl",
		    		data:dataStr,
		    		cache:false,
		    		type:'post',
		    		dataType:'html',
		    		timeout:5000,
		    		success:function(result){
			        	var obj=StringHelper.str2obj(result);
			        	var res = obj["rs"];
		    			if(obj=="ERR"){
		    				alert(failMsg);
		    			}else if(res=="ERR"){
		    				var body = undefined == obj["body"]?"":":"+obj["body"];
		    				alert(failMsg+body);
		    			}else{
		    				alert(sucMsg);
		    			}
		    		},
		    		error:function(result){
		    			alert(failMsg+ ":" + result);
		    		}
	    		});
	    	}
	    },
		//---------------Control Panel:System Properties------------------
		cp_syspro:{
			setSystemPro:function() {
				var obj=window["appdialogmgr"].getObj("sysProSetDialog");
				var kid=HtmlHelper.value("sysProSetDialog_key");
				var kval=HtmlHelper.value("sysProSetDialog_value");
				
				if (kid=="") {
					alert("请输入属性Key");
					return;
				}
				
				if (kval=="") {
					if (!confirm("属性Value为空代表删除该属性，确定吗？")) {
						return;
					}
				}
				
				var target=obj["name"]+"("+obj["ip"]+")";
				
				var q={intent:"chgsyspro",request:{url:obj["url"]}};
				
				q.request[kid]=kval;
				
				var qStr=StringHelper.obj2str(q);
				
				AjaxHelper.call({
					url:"../../rs/godeye/node/ctrl",
					data: qStr,
					cache: false,
			        type: "post",
			        dataType: "html",
			        success: function (data) {
			        	var obj=StringHelper.str2obj(data);
			        	if (obj=="ERR") {
			        		alert("设置"+target+"系统属性["+kid+"]="+kval+"失败："+data);
			        	}
			        	else {
			        		alert("设置"+target+"系统属性["+kid+"]="+kval+"成功！");
			        	}
			        },
			        error: function (data) {
			           alert("设置"+target+"系统属性["+kid+"]="+kval+"失败："+data);
			        }
				});
				
				window["appdialogmgr"].close("sysProSetDialog");
			}
		},
		//--------------------------Node Config Window ---------------------------------
		//data structure to store the data on the page
		data_nodeConfigForUpdate:undefined,
		/**
		 * TODO: Node Config Window
		 */
		buildNodeConfigWnd: function(nodeCfgObj) {
			var html=new StringBuffer();
			
			var nodeCfgStr=StringHelper.obj2str(nodeCfgObj);
				
			html.append("<div class=\"topDiv\">" +
			"<span class=\"tagTitle\">"+nodeCfgObj["ip"]+"</span><br/>"+
            "<span class=\"idTitle\">"+nodeCfgObj["name"]+"</span>" +
            "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('NodeConfigWnd',undefined,'MacDetailWnd')\"></div>" +
            "<div class=\"icon-ok-sign\" onclick='javascript:app.controller.saveNodeConfigUpdate("+nodeCfgStr+")'></div>" +
            "</div>");
			
			html.append("<div class=\"contentDiv\"><div class=\"shine2\"></div>");
			html.append("<div id='NodeConfigWnd_PList'></div>");
			html.append("</div>");
			
			return html.toString();
		},
		//load node config window
		loadNodeConfigWnd:function(nodeCfgObj) {
			
			var q={intent:"loadnodepro",request:{url:nodeCfgObj["url"]}};
			
			var qStr=StringHelper.obj2str(q);
			
			AjaxHelper.call({
				url : "../../rs/godeye/node/ctrl",
				async : true,
				cache : false,
				type : "POST",
				data : qStr,
				dataType : "html",
				success : function(result) {
					var obj = StringHelper.str2obj(result);
					var res = obj["rs"];
					var configObj = StringHelper.str2obj(res);
					app.controller.showNodeConfigList(nodeCfgObj,configObj);
				},
				error : function(result) {
					alert("获取节点配置信息失败： " + result);
				}
			});
		},
		//show node config list
		showNodeConfigList:function(nodeCfgObj,configObj) {
			
			var data={feature:{},resource:{},profile:{}};
			
			for ( var key in configObj) {
				var keyinfo=key.split(".");
				var feature=keyinfo[1];
				var tag;
				var name;
				//feature
				if (keyinfo[0]=="feature"||keyinfo[0]=="resource") {
					tag=keyinfo[0];
					var prefix=keyinfo[0]+"."+keyinfo[1];
					name=key.substring(prefix.length+1);
				}
				//global meta
				else {
					feature=keyinfo[0];
					tag="profile";
					name=key;
				}
				
				if (data[tag][feature]==undefined) {
					data[tag][feature]={};
				}
				
				data[tag][feature][key]={name:name,val:configObj[key]};
			}
			this.data_nodeConfigForUpdate={};
			
			var sb = new StringBuffer();
			
			this.buildConfigList(nodeCfgObj,"Profile配置","P",data["profile"],sb);
			this.buildConfigList(nodeCfgObj,"Resource配置","R",data["resource"],sb);
			this.buildConfigList(nodeCfgObj,"Feature配置","F", data["feature"],sb);
			
			var cfgList = HtmlHelper.id("NodeConfigWnd_PList");
			cfgList.innerHTML = sb.toString();
		},
		//build configuration list
		buildConfigList:function(nodeCfgObj,fName,tag,fCfg,sb) {
			sb.append("<div>"+fName+"</div>");			
			for( var feature in fCfg) {
				sb.append("<span class='componentExpandButton' onclick=\"app.controller.openClose('NodeConfigWnd_PList_"+tag+"_"+feature+"')\">"+feature+"</span>")
				sb.append("<div style='display:none;' id='NodeConfigWnd_PList_"+tag+"_"+feature+"'>");
				var fvals=fCfg[feature];
				for(var key in fvals) {
					var info=fvals[key];
					var pObj={name:nodeCfgObj["name"],ip:nodeCfgObj["ip"],key:key};
					var pObjStr=StringHelper.obj2str(pObj);
					sb.append("<div class='componentTab' onclick='app.controller.openConfigDialog("+pObjStr+")'><span class='key'>" + info["name"] + "</span>=<span id='NodeConfigWnd_D_"+key+"'>"
							+  info["val"]+ "</span><span class='value_update' id='NodeConfigWnd_S_"+key+"'></span><span id='NodeConfigWnd_O_"+key+"' style='display:none'>"+info["val"]+"</span></div>");
				}
				sb.append("</div>");
			}
		},
		openConfigDialog:function(pObj) {
			window["appdialogmgr"].open("nodeConfigDialog",pObj);
		},
		doSetNodeConfig:function() {
			var keyInput=HtmlHelper.id("nodeConfigDialog_key");
			var valueInput=HtmlHelper.id("nodeConfigDialog_value");
			var key=keyInput.value;
			
			this.doSetConfigField(key, valueInput.value);
			
			window["appdialogmgr"].close("nodeConfigDialog");
			
		},
		doSetConfigField:function(key,value,isUpdateOriginalValue) {
			
			var target=HtmlHelper.id("NodeConfigWnd_D_"+key);
			var org=HtmlHelper.id("NodeConfigWnd_O_"+key);
			var tstate=HtmlHelper.id("NodeConfigWnd_S_"+key);
			target.innerHTML=value;
			
			//if isUpdateOriginalValue==true, then update original value
			if (true==isUpdateOriginalValue) {
				org.innerHTML=value;
			}
			
			//if the modified value != original value, then record the udpate
			if (org.innerHTML!=target.innerHTML) {
				tstate.innerHTML="(未保存修改)";
				target.className ="value_update";
				//temp save the updated value
				this.data_nodeConfigForUpdate[key]=value;
			}
			//if the modified value == original value, then no need for update
			else {
				tstate.innerHTML="";
				target.className ="";
				delete this.data_nodeConfigForUpdate[key];
			}
		},
		//saveNodeConfigUpdate
		saveNodeConfigUpdate:function(nodeCfgObj) {
			
			if (this.data_nodeConfigForUpdate==undefined||Object.getOwnPropertyNames(this.data_nodeConfigForUpdate).length==0) {
				alert("无任何配置变更！");
				return;
			}
			
			var q={intent:"chgnodepro",request:this.data_nodeConfigForUpdate};
			q["request"]["url"]=nodeCfgObj["url"];
			
			var qStr=JSON.stringify(q);
			
			AjaxHelper.call({
				url : "../../rs/godeye/node/ctrl",
				async : true,
				cache : false,
				type : "POST",
				data : qStr,
				dataType : "html",
				success : function(result) {
					var obj = StringHelper.str2obj(result);
					var res = obj["rs"];
					
					if(res=="ERR") {
						alert(obj["msg"]);
					}
					else {
						alert("更新节点配置成功!");
					}
					delete app.controller.data_nodeConfigForUpdate["url"];
					for(var key in app.controller.data_nodeConfigForUpdate) {
						var value=app.controller.data_nodeConfigForUpdate[key];						
						app.controller.doSetConfigField(key,value,true);
					}
				},
				error : function(result) {
					alert("更新节点配置失败： " + result);
				}
			});
		}
	}
};

var app = window["appmvc"].build(mvcObj);

$(document).ready(function(){
	app.run();
});

