/**
 * app stream is the plugin of appmonitor.js
 * it helps to show the application cluster service stream
 */
function AppServiceStream(app) {
	
	//common 
	var acLayout;
	
	//acLayout model;
	var acData={
		//ac layout scope including app, appgroup, gview
		scope:undefined,
		//appOut shows the app client's access to the targets
		appOut:{
			
		},		
		//cache the client traverse state
		clientCache:{
	 	
		},		
		//appIn shows the app in-call source
		appIn:{
			
		},
		//app proxy from client
		appProxy:{
			
		},
		//app proxy from iplink
		srcProxy:{
			
		}
	};
	
	/**
	 * get match app profile
	 */
	this.getMatchAppProfiles=function(target,target_urls) {
		
		var allNodeProfile=app.mdata("profile");
		
		var matchedProfiles=[];
		
		for(var profileKey in allNodeProfile) {
			
			//match 1.1.1: if profileKey contains the target
			if (profileKey.indexOf(target)>-1) {
				matchedProfiles[matchedProfiles.length]=allNodeProfile[profileKey];
				break;
			}
			
			var tprofile=allNodeProfile[profileKey];
			
			//match 1.1.2  if appid match target, in case webapp's contextPath is '/'
			var nodeAppid=tprofile["appurl"]+tprofile["appid"];
			if(nodeAppid.indexOf(target)>-1){
				matchedProfiles[matchedProfiles.length]=allNodeProfile[profileKey];
				break;				
			}
			
			//match 1.2: if one of the component services match the target						
			var cpt_services=tprofile["cpt.services"];
			if (cpt_services!=undefined&&cpt_services.indexOf(target)>-1) {
				matchedProfiles[matchedProfiles.length]=tprofile;
				break;
			}			

			//temporary remove, should consider how to solve the multiple ip issue  
//			if (cpt_services!=undefined&&target_urls!=undefined) {
//				
//				//match 1.3: if target url  in the cpt_services  
//				for(var target_url in target_urls) {
//										
//					if (cpt_services.indexOf(target_url)>-1) {					
//						matchedProfiles[matchedProfiles.length]=tprofile;
//					}
//				}	
//			}	
			
//			//for key match
//			if (target_urls==undefined) {
//				
//				//match 1.1: if profileKey contains the target
//				if (profileKey.indexOf(target)>-1) {
//					matchedProfiles[matchedProfiles.length]=allNodeProfile[profileKey];
//					break;
//				}
//				//match 1.2: if one of the component services match the target
//				else {
//					var cpt_services=tprofile["cpt.services"];
//					if (cpt_services!=undefined&&cpt_services.indexOf(target)>-1) {
//						matchedProfiles[matchedProfiles.length]=allNodeProfile[profileKey];
//						break;
//					}
//				}
//				
//				continue;
//			}
//			
//			//for deep match
//			if (profileKey.indexOf(target)==-1) {
//				//match 1.3: if one of the component services match the target
//				var cpt_services=tprofile["cpt.services"];
//				if (cpt_services!=undefined&&cpt_services.indexOf(target)>-1) {
//					matchedProfiles[matchedProfiles.length]=allNodeProfile[profileKey];
//					break;
//				}
//				
//				continue;
//			}
//			
//			var tprofile=allNodeProfile[profileKey];
//			
//			for(var target_url in target_urls) {
//				
//				//match 2.1: appid if in the target url
//				if (target_url.indexOf(tprofile["appid"])>-1) {					
//					matchedProfiles[matchedProfiles.length]=tprofile;
//				}
//			}			
		}		
		
		return matchedProfiles;
	};
	
	/**
	 * get app info
	 */
	this.getAppInfo=function(appInst) {
		
		var isJse=app.controller.isJSE(appInst["appurl"]);
		
		var appInstMOId;
		//JEE
		if (isJse==false) {
			appInstMOId=appInst["appurl"]+"---"+appInst["appid"];
		}
		//JSE
		else {
			appInstMOId=appInst["appurl"];
		}
		
		return {id:appInstMOId,isJse:isJse};
	};
	
	/**
	 * get application tip
	 */
	this.getAppTip=function(nState,isJse) {
		
		//JEE
		if (isJse==false) {
			
			if (nState["tps"]==undefined) {
				return "性能指标未知";
			}
			else {
				
				var ts=(nState["timestamp"]==undefined)?"-":nState["timestamp"];
				
				return "QPM:"+nState.tps+" 全程平均响应:"+nState.tavg+" 当前分钟响应:"+((nState.mavg==undefined)?"-":nState.mavg)+" 错误:"+nState.err+" 访问时间:"+ts;
			}
		}
		//JSE
		else {
			
		}
	}
	
	/**
	 * get the app node state
	 */
	this.getAppNodeState=function(appMOObj) {
		
		var nState={
				workpower:1,
				workstate:-2
		};
		
		var tpsMORate;
		var errMORate;
		var avgMORate;
		
		if (appMOObj["tps"]==undefined) {
			
			var appInstMOId=appMOObj["id"];
			
			var appMonitorData=app.mdata("monitor.app");
			
			if (appMonitorData==undefined) {
				return nState;
			}
			
			var appInstMO=appMonitorData[appInstMOId];
			
			if (appInstMO==undefined) {
				return nState;
			}
			
			var tps = appInstMO["tps"];
			var tavg = appInstMO["tavg"];
			var err = appInstMO["err"];
			var count = appInstMO["count"];
			var mavg=appInstMO["mavg"];
			
			nState["tps"]=tps;
			nState["tavg"]=tavg;
			nState["mavg"]=mavg;
			nState["err"]=err;
			nState["timestamp"]=appInstMO["timestamp"];
			
			tpsMORate=app.controller.getMORate("tps",tps);
			errMORate=app.controller.getMORate("err",err,count);
			avgMORate=app.controller.getMORate("tavg",tavg);
		}
		else {
			tpsMORate=appMOObj["tpsR"];
			errMORate=appMOObj["errR"];
			avgMORate=appMOObj["tavgR"];
		}	
		
		//workPower: border width
		nState["workpower"]=tpsMORate["level"]+1;
		
		//workState: border color
		var workStateVal=errMORate["level"]+avgMORate["level"];
		
		if (workStateVal==0) {
			nState["workstate"]=1;
		}
		else if (workStateVal==1) {
			nState["workstate"]=0;
		}
		else if (workStateVal>1){
			nState["workstate"]=-1;
		}
				
		return nState;
	}
	
	/**
	 * TODO build buildAppProxyEdgeDetail window
	 */
	this.buildAppProxyEdgeDetailWnd=function(sObj) {
		
		var idInfo=sObj.id.split("->");
		
		var sb=new StringBuffer();
		
		var backWndId="";
		
		if (sObj["backWndId"]!=undefined) {
			backWndId=",'"+sObj["backWndId"]+"'";
		}
		
		var isProxy=sObj["isProxy"];
		var title=(isProxy==true)?"代理服务":"直接访问";
		
		sb.append("<div class=\"appDetailContent\">" +
        "<div class=\"topDiv\">" +
        "<span class=\"tagTitle\">"+idInfo[0]+"</span><br/>"+
        "<span class=\"idTitle\">"+title+"</span>" +
        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppProxyEdgeDetailWnd',undefined"+backWndId+")\"></div>" +
        //"<div class=\"icon-refresh\" onclick='javascript:app.controller.runAppClusterTop()'></div>"+
        "</div></div>");
		
		//renew draw playground
		sb.append("<div class=\"contentDiv\"><div class=\"shine2\"></div>");
		
		var ipSrcObj=sObj["ipSrcObj"];

		var clients=ipSrcObj["clients"];
		
		if(clients!=undefined) {
		
			sb.append("<div class=\"kv\">" +
		            "<span class=\"kvField\">来源</span><span>：</span>");
			
			var tmpSb=new StringBuffer();
			var tmpAppIndex=0;
			var tmpUser=new StringBuffer();
			var tmpUserIndex=0;
			
			for(var src in clients) {
				
				if(src.indexOf("user://")==0) {
					tmpUser.append("<div class='componentName'>"+src+"<span class=\"timeTitle\">"+TimeHelper.getTime(clients[src])+"</span></div>");
					tmpUserIndex++;
				}
				else {
					tmpAppIndex++;
					tmpSb.append("<div class='componentName'>"+src+"<span class=\"timeTitle\">"+TimeHelper.getTime(clients[src])+"</span></div>");
				}
			}
			
			//来源 only for proxy
			if (isProxy==true) {
				sb.append("<div class='componentExpandButton' onclick=\"app.controller.openClose('"+sObj.id+"_proxy_pf')\"><span class='titleGray'>应用</span>&nbsp;"+tmpAppIndex+"个</div>");
				sb.append("<div id='"+sObj.id+"_proxy_pf' class='componentFTab' style='display:none;'>");
				sb.append(tmpSb.toString()+"</div>");
			}
			
			sb.append("<div class='componentExpandButton' onclick=\"app.controller.openClose('"+sObj.id+"_proxy_user')\"><span class='titleGray'>用户</span>&nbsp;"+tmpUserIndex+"个</div>");
			sb.append("<div id='"+sObj.id+"_proxy_user' class='componentFTab' style='display:none;'>");
			sb.append(tmpUser.toString()+"</div>");
			
			sb.append("</div>");
		}
		
		//目标
		sb.append("<div class=\"kv\">" +
	            "<span class=\"kvField\">目标应用</span><span>：</span>"+idInfo[1]+
	            "</div>");
		
		//目标URL
		var urls=ipSrcObj["urls"];
		tmpSb=new StringBuffer();
		tmpAppIndex=0;
		
		for(var url in urls) {
			tmpAppIndex++;
			tmpSb.append("<div class='componentName'>"+url+"<span class=\"timeTitle\">"+TimeHelper.getTime(urls[url])+"</span></div>");
		}
		sb.append("<div class='componentExpandButton' onclick=\"app.controller.openClose('"+sObj.id+"_proxy_url')\"><span class='titleGray'>服务</span>&nbsp;"+tmpAppIndex+"个</div>");
		sb.append("<div id='"+sObj.id+"_proxy_url' class='componentFTab' style='display:none;'>");
		sb.append(tmpSb.toString()+"</div>");
		
		sb.append("</div>");		
		
		return sb.toString();
	};
	
	/**
	 *  TODO build buildAppUnknowEdgeDetailWnd window
	 */
	this.buildAppUnknowEdgeDetailWnd=function(sObj) {
		var idInfo=sObj.id.split("->");
		
		var sb=new StringBuffer();
		
		var backWndId="";
		
		if (sObj["backWndId"]!=undefined) {
			backWndId=",'"+sObj["backWndId"]+"'";
		}
		
		var uapps=sObj["uapps"];
		
		var title="未知应用集合("+Object.getOwnPropertyNames(uapps).length+")";
		
		sb.append("<div class=\"appDetailContent\">" +
		        "<div class=\"topDiv\">" +
		        "<span class=\"tagTitle\">"+idInfo[0]+"</span><br/>"+
		        "<span class=\"idTitle\">"+title+"</span>" +
		        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppUnknowEdgeDetailWnd',undefined"+backWndId+")\"></div>" +
		        
		        "</div></div>");
				
		//renew draw playground 
		sb.append("<div class=\"contentDiv\"><div class=\"shine2\"></div>");
		
		for(var ipSrc in uapps) {
			
			var srcInfo=uapps[ipSrc];
			
			var extattrs=srcInfo["ext"];
			
			var ts=extattrs["ts"];
			
			sb.append("<div class='componentExpandButton' onclick=\"app.controller.openClose('"+ipSrc+"_unknown_apps_access')\"><span class='titleGray'>"+ipSrc+"</span>&nbsp;"+TimeHelper.getTime(ts)+"</div>");
			sb.append("<div id='"+ipSrc+"_unknown_apps_access' class='componentFTab' style='display:none;'>");
			
			var urls=srcInfo["info"]["urls"];
			
			for (var url in urls) {
				sb.append("<div class='componentName'>"+url+"<span class=\"timeTitle\">"+TimeHelper.getTime(urls[url])+"</span></div>");
			}
			
			sb.append("</div>");
		}
		
		sb.append("</div>");		
		
		return sb.toString();
	};
	
	/**
	 * TODO build Global Eye window
	 */
	this.buildGlobalEyeTopWnd=function() {
		acData["profile"]={appinsts:app.controller.getProfileByAppGroup()};
		acData["layout"]="net";		
		acData["wndid"]="GlobalEyeTopWnd";
		
		var html="<div id='GlobalEyeTopWnd_Ctn' ></div>";
		
		return html;
	};
	
	/**
	 * TODO build AppClusterTopWnd window
	 */
	this.buildAppClusterTopWnd=function(sObj) {
		
		var appgpid=sObj.appgpid;
		
		acData["profile"]=app.controller.getProfileTagsAndInsts(appgpid);
		acData["layout"]="top";
		acData["wndid"]="AppClusterTopWnd";
		
		var sObjStr=StringHelper.obj2str(sObj);
		
		var html=//this.buildAlertNtfyPlugin("AppClusterTopWnd",sObj)+
		"<div class=\"appDetailContent Dark\">" +
        "<div class=\"topDiv\">" +
        "<span class=\"tagTitle\">"+acData["profile"]["tags"]+"</span><br/>"+
        "<span class=\"idTitle\">"+sObj.appid+"</span>" +
        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppClusterTopWnd')\"></div>" +
        "<div class=\"icon-refresh\" onclick='javascript:app.controller.runAppClusterTop("+sObjStr+")'></div>"+
        "</div></div>";
		
		//renew draw playground
		html+="<div id='AppClusterTopWnd_Ctn' style='position:relative;'></div>";
		
		return html;
	};
	
	/**
	 * TODO build AppGroupTop window
	 */
	this.buildAppGroupTopWnd=function(sObj) {
		
		acData["profile"]={appinsts:app.controller.getProfileByAppGroup(sObj.appgroup)};
		acData["layout"]="net";		
		acData["wndid"]="AppGroupTopWnd";
		
		var sObjStr=StringHelper.obj2str(sObj);
				
		var html="<div class=\"appDetailContent Dark\">" +
        "<div class=\"topDiv\">" +
        "<span class=\"tagTitle\">"+"应用组服务流图谱"+"</span><br/>"+
        "<span class=\"idTitle\">"+sObj.appgroup+"</span>" +
        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppGroupTopWnd')\"></div>" +
        "<div class=\"icon-refresh\" onclick='javascript:app.controller.runAppGroupTop("+sObjStr+")'></div>"+
        "</div></div>";
		
		//renew draw playground
		html+="<div id='AppGroupTopWnd_Ctn' ></div>";
		
		return html;
	};
	
	/**
	 * resize the draw playground
	 */
	this.resize=function(w,h,noTopHeight) {
		
		var ctn=HtmlHelper.id(acData["wndid"]+"_Ctn");
		
		if (ctn!=undefined) {
			if (acData["wndid"]=="GlobalEyeTopWnd") {
				ctn.style.height=noTopHeight+"px";
			}
			else {
				ctn.style.height=noTopHeight-50+"px";
			}
			
		}
	};
		
	/**
	 * start drawing
	 */
	this.start=function(sObj,scope) {
		
		/**
		 * step 1: build the app nodes & its clients
		 */
		var _this=this;
		
		//renew app cluster layout
		acLayout=new AppClusterLayout({cid:acData["wndid"]+"_Ctn",
			layout:acData["layout"],
			events:{
				ondbclick:function(params) {
					//for app node dbclick
					if (_this.onAppDbclick(params)==true) {
						return;
					}
					//for app edge dbclick
					_this.onAppEdgeDbclick(params);
				}
			}
		});
		
		//set the aclayout scope
		acData["scope"]=scope;
		acData["appOut"]={};
		acData["clientCache"]={};
		acData["appIn"]={};
		acData["appProxy"]={};
		acData["srcProxy"]={};
		
		//build app Insts
		var appInsts=acData["profile"]["appinsts"];
		
		for(var i=0;i<appInsts.length;i++) {
			
			var appInst=appInsts[i];
			//don't do clientAccess of MonitorAgent in none-UAV group. 
			if(appInst["appid"]=="MonitorAgent"&&appInst["appgroup"]!="UAV"){
				this.onAppNew(appInst,{needClientAccess:false});
			}else{
				this.onAppNew(appInst,{needClientAccess:true});
			}
			
		}	
				
		acLayout.draw();

		/**
		 * step 2: client perf data
		 */
		this.startAppEdgeMoUpdate();
	};
	
	/**
	 * TODO: app node
	 * update drawing when monitor data update
	 */
	this.onAppMoUpdate=function(appMOObj) {
		
		/**
		 * check if the TopWnd is open
		 */
		var isTopWndHide=window.winmgr.isHide(acData["wndid"]);
		if (isTopWndHide==true) {
			return;
		}
		
		/**
		 * check if the node is in the view
		 */
		if (acLayout.nodes().existNode(appMOObj["id"])==false) {
			return;
		}

		var nState=this.getAppNodeState(appMOObj);
		
		var nodeUpdate={
			id:appMOObj["id"],
			itype:"app",
			tip:this.getAppTip(appMOObj,appMOObj["isJse"]),
			workstate:nState["workstate"],
			workpower:nState["workpower"]
		};
		
		acLayout.nodes().updateNode(nodeUpdate);
	};
	
	/**
	 * when a new app is in profile list
	 * {
	 * needClientAccess:  是否显示客户端调用的节点（遍历）
	 * allowOtherApps:    是否显示其他的应用实例
	 * slevel:                     确定App实例的层级，默认是2
	 * targetFilters:         
	 * fromWhere:           调用onAppNew的函数是谁
	 * matchNodeUrl:      提供一个url去匹配符合条件的节点
	 * }
	 */
	this.onAppNew=function(appInst,options) {
		
		/**
		 * check if the TopWnd is open
		 */
		var isTopWndHide=window.winmgr.isHide(acData["wndid"]);
		if (isTopWndHide==true) {
			return undefined;
		}
		
		//FIX: in case of undefined options
		if (options==undefined) {
			options={};
		}
		
		/**
		 * check if the scope is in view: app cluster
		 */
		if (acData["scope"]["view"]=="app") {
			
			if (true!=options["allowOtherApps"]&&(acData["scope"]["appgroup"]!=appInst["appgroup"]||acData["scope"]["appid"]!=appInst["appid"])) {
				return undefined;
			}	
		}
		/**
		 * check if the scope is in view: app group
		 */
		else if (acData["scope"]["view"]=="appgroup") {
			
			if (true!=options["allowOtherApps"]&&(acData["scope"]["appgroup"]!=appInst["appgroup"])){
				return undefined;
			}			
		}
		
		/**
		 * check if the node is in the view
		 */
		var appO=this.getAppInfo(appInst);
			
		var state=parseInt(appInst["state"]);	
		
		var appurl=appInst["appurl"];
		
		if (appurl==undefined) {
			return undefined;
		}
	    
		//get node state
		var nState=this.getAppNodeState({id:appO.id});
			
		/**
		 * service ports: nodes for different ports
		 */
		var servicePorts=appInst["service.ports"];
		var serviceNode={};
		//匹配的node，用于return，JEE程序只有一个node，MSCP因为有多个port，可能会有一个主node，多个非主node
		var matchNode;
		//需要显示client的node，JEE程序只有一个node，MSCP因为有多个port，可能会有一个主node，多个非主node，但是只在主node上显示client
		var needClientNode;
		
		if (servicePorts!=undefined) {
			for(var surl in servicePorts) {
								
				//add new node for this service port
				var sinfo=servicePorts[surl];
				
				//1) none main port service node
				var sOid=surl+"---"+appInst["appid"];
				var fullCompName=sinfo["compid"].split(":");
				var compInfo=fullCompName[0].split(".");
				var serviceCompName=compInfo[compInfo.length-1];
				var tip=appInst["appgroup"]+":"+appInst["appname"]+"，服务组件:"+serviceCompName+"，服务端口:"+sinfo["port"];
				
				var description="\n"+serviceCompName;
				
				//2) main port service node
				var isMain=(appurl.indexOf(surl)==0)?true:false;
				if (isMain==true) {
					tip=this.getAppTip(nState,appO.isJse);
					description="";
					sOid=appO["id"];
				}
				
				/**
				 * if the node already exist
				 */
				var appInstPortNode;
				
				if (acLayout.nodes().existNode(sOid)==true) {
					
					appInstPortNode= acLayout.nodes().getNode(sOid);
					
					appInstPortNode["extattrs"]["tfilters"]=options["targetFilters"];
				}
				/**
				 * if the node not exist
				 */
				else {
					appInstPortNode={id:sOid,label:appInst["appgroup"]+":"+appInst["appname"]+"\n"+surl+description,tip:tip,itype:"app",state:state,workstate:nState["workstate"],workpower:nState["workpower"],extattrs:{tfilters:options["targetFilters"],poid:appInst["id"],isJse:appO.isJse}};
					
					if (acData["scope"]["view"]=="app") {
						//set app level
						if (options["slevel"]==undefined) {
							appInstPortNode["level"]=2;
						}
						else {
							appInstPortNode["level"]=options["slevel"];
						}
					}
					
					acLayout.addNodes([appInstPortNode]);
				}
				
				if (isMain==true) {
					serviceNode["main"]=appInstPortNode;
				}
				else {
					serviceNode[surl]=appInstPortNode;
				}
				
				/**
				 * 根据matchNodeUrl来匹配是哪个service node
				 */
				if (options["matchNodeUrl"]!=undefined&&options["matchNodeUrl"].indexOf(surl)>-1) {
					matchNode=appInstPortNode;
				}
			}
			
			needClientNode=serviceNode["main"];
			
			/**
			 * 如果没有matchNodeUrl，那么就return 主node
			 */
			if (options["matchNodeUrl"]==undefined&&matchNode==undefined) {
				matchNode=serviceNode["main"];
			}
		}
		/**
		 *  no service application
		 */
		else {
			
			var appInstNode;
			
			if (acLayout.nodes().existNode(appO.id)==true) {
				
				appInstNode= acLayout.nodes().getNode(appO.id);
				
				appInstNode["extattrs"]["tfilters"]=options["targetFilters"];
			}
			else {
				
				var appInfo=appurl.split("/");
				
				appInstNode={id:appO.id,label:appInst["appgroup"]+":"+appInst["appname"]+"\n"+appInfo[2],itype:"app",state:state,workstate:nState["workstate"],workpower:nState["workpower"],extattrs:{tfilters:options["targetFilters"],poid:appInst["id"],isJse:appO.isJse}};
				
				//set app tip
				appInstNode["tip"]=this.getAppTip(nState,appO.isJse);
				
				if (acData["scope"]["view"]=="app") {
					//set app level
					if (options["slevel"]==undefined) {
						appInstNode["level"]=2;
					}
					else {
						appInstNode["level"]=options["slevel"];
					}
				}
						
				acLayout.addNodes([appInstNode]);	
			}
			
			needClientNode=appInstNode;
			
			matchNode=appInstNode;
		}
		
		/**
		 * app client
		 */
		 
		//the needClientNode is undefine when appurl and cpt.services's ip is different(case container)
		if (true==options["needClientAccess"]&&needClientNode!=undefined) {
			 this.onAppClientUpdate(needClientNode,appInst,options["fromWhere"]);
		}
		
		return matchNode;
	};
	
	/**
	 * when an app is updated in profile list
	 */
	this.onAppUpdate=function(appInst) {
		/**
		 * check if the TopWnd is open
		 */
		var isTopWndHide=window.winmgr.isHide(acData["wndid"]);
		if (isTopWndHide==true) {
			return;
		}
		
		/**
		 * check if the node is in the view
		 */
		var appO=this.getAppInfo(appInst);
		
		//update app node state
		var state=parseInt(appInst["state"]);	
		
		var appurl=appInst["appurl"];
		
		var servicePorts=appInst["service.ports"];
		
		var needClientNode;
		/**
		 * services application
		 */
		if(servicePorts!=undefined) {
			
			for(var surl in servicePorts) {
				
				var isMain=(appurl.indexOf(surl)==0)?true:false;
				
				var sOid=surl+"---"+appInst["appid"];
				
				if (isMain==true) {
					sOid=appO["id"];
				}
				
				var appPortNode=acLayout.nodes().getNode(sOid);
				
				if (appPortNode==undefined) {
					continue;
				}
				
				acLayout.nodes().updateNodeState(sOid, "app", state);
				
				if (isMain==true) {
					needClientNode=appPortNode;
				}
			}
		}
		/**
		 * no service application
		 */
		else {
			needClientNode=acLayout.nodes().getNode(appO["id"]);
			
			if (needClientNode==undefined) {
				return;
			}
			
			acLayout.nodes().updateNodeState(appO["id"], "app", state);
		}
		
		//update app client state
		if (needClientNode!=undefined&&(appInst["appid"]!="MonitorAgent"||appInst["appgroup"]=="UAV")) {
			this.onAppClientUpdate(needClientNode, appInst);
		}
	};
		
	/**
	 * when an app is deleted from profile list
	 */
	this.onAppDel=function(appInst) {
		/**
		 * check if the TopWnd is open
		 */
		var isTopWndHide=window.winmgr.isHide(acData["wndid"]);
		if (isTopWndHide==true) {
			return;
		}
		
		/**
		 * check if the node is in the view
		 */
		var appO=this.getAppInfo(appInst);
		
		var servicePorts=appInst["service.ports"];
		
		var appurl=appInst["appurl"];
		
		/**
		 * service application
		 */
		if(servicePorts!=undefined) {
			
			for(var surl in servicePorts) {

				var isMain=(appurl.indexOf(surl)==0)?true:false;
				
				var sOid=surl+"---"+appInst["appid"];
				
				if (isMain==true) {
					sOid=appO["id"];
				}
				
				if (acLayout.nodes().existNode(sOid)==false) {
					continue;
				}
				
				acLayout.nodes().removeNode(sOid);
			}
		}
		/**
		 * no service application
		 */
		else {
			if (acLayout.nodes().existNode(appO["id"])==false) {
				return;
			}
			
			acLayout.nodes().removeNode(appO["id"]);
		}
	};
	
	/**
	 * when an app is dbclicked
	 */
	this.onAppDbclick=function(params) {
		
		if (params["nodes"].length==0) {
			return false;
		}
		
		var nodeInfo=acLayout.nodes().getNode(params["nodes"][0]);
		
		var extattrs=nodeInfo["extattrs"];
		
		//for app node
		if (nodeInfo["itype"]=="app") {
			
			var appInfo={id:extattrs["poid"],instid:nodeInfo["id"],isJse:extattrs["isJse"],backWndId:acData["wndid"]};
			
			app.controller.showWindow(appInfo,"AppInstChartWnd","buildAppInstChart","runAppInstChart");
			
			return true;
		}
		//for proxy
		else if (nodeInfo["itype"]=="proxy") {
			return true;		
		}	
		//for unknown node
		else if (nodeInfo["itype"]=="unknown") {
			return true;			
		}	
		//for db node
		else if (nodeInfo["itype"]=="db") {
			/*
			 * disabled db source action
			 *  
			var uri = nodeInfo.id;
            
            if(uri.indexOf('jdbc') == 0){
                var type = uri.substring(5, uri.indexOf('://'));
                var dbname = uri.substr(uri.lastIndexOf('/') + 1);
            }else{
                var type = uri.substring(0, uri.indexOf('://'));
                var dbname = "";
            }
            
            var nstart = uri.indexOf('://') + 3;
            var nend = uri.lastIndexOf('/');
            if(nend - nstart > 1){
                var nodestr = uri.substring(nstart, nend);
            }else{
                var nodestr = uri.substr(nstart);
            }
            
            var schema="";
            try {
            	schema=extattrs["vals"]["svr"].split("@")[1];
            	if (schema==undefined) {
            		schema="";
            	}
            }
            catch(e) {}
            
            var obj = {
                uri: uri,
                label: nodeInfo.label,
                backWndId: acData["wndid"],
                type: type,
                schema: schema,
                dbname: dbname,
                node: nodestr.split(','),
                model: app.controller.getDbInstModel(type, schema),
                extattrs:extattrs
            }
            app.controller.showWindow(obj, 'AppInstChartWnd', 'buildDbInstChart', 'runDbInstChart');
            */
            return true;
		}	
		
		return false;
	};
	

	/**
	 * TODO: app client
	 * for a client target new or update
	 */
	this.onAppClientUpdate=function(appInstNode,appInst,fromWhere) {
		
		/**
		 * NOTE: need get the main service node as only support to show client node on service main node
		 */
		//var appInstNode=serviceNode["main"];
		/**
		 * in scope app, we should not update the app node not in level 2,as they are IN or OUT nodes
		 */
		if (acData["scope"]["view"]=="app") {
			if (appInstNode["level"]!=2&&appInstNode["level"]!=0) {
				return;
			}
		}
		
		//cache the node which has done clientUpdate, if done then return 
		var clientCache = acData["clientCache"];
	 	if(clientCache[appInst["id"]]==true){
	 		return;
	 	}
		//do not cache the node generated by iplink, because the node will do several clientUpdate
		else if(appInstNode["extattrs"]["tfilters"]==undefined){
	 		clientCache[appInst["id"]]=true;
	 	}			 			
	 	
		var clientStr=appInst["cpt.clients"];
		
		if (clientStr==undefined) {
			return;
		}
		
		//get app inst target filters
		var targetFilters=appInstNode["extattrs"]["tfilters"];
		
		//step 1: check if the app inst has client data set
		var appOut=acData["appOut"][appInstNode["id"]];
		
		//if none, just create one
		if (appOut==undefined) {
			acData["appOut"][appInstNode["id"]]={};
			appOut=acData["appOut"][appInstNode["id"]];
		}
		
		//step 2: go through every clients 
		var clients=eval("("+clientStr+")");
		
		for(var i=0;i<clients.length;i++) {
			
			var client=clients[i];
			
			var target=client["id"];
			var vals=client["values"];
			var cstate=vals["state"];
			var cts=vals["ts"];
			
			//edge workstate
			var dead=false;
			
			//this means the client access to this target over 1 min
			if (parseInt(cstate)<1) {
				dead=true;
			}
			
			//step 2.1: check if the client data exists, if yes we do update
			var existClientData=appOut[target];
			
			if (existClientData!=undefined) {
				
				var targetNode=existClientData["node"];
				
				//update edge
				var edge=this.onAppEdgeUpdate(appInstNode, targetNode,{dead:dead,ts:cts,extattrs:{client:client,appInst:appInst,ts:cts}});
				
				//cache client data
				appOut[target]={node:targetNode,edge:edge};
				
				continue;
			}
			
			//step 2.2: if there is no such client data, then should rematch and create
			//http client
			var targetNode;
			if (target.indexOf("http")==0) {
				
				var matchedProfiles=this.getMatchAppProfiles(target,vals["urls"]);
				
				/**
				 * NOTE: 有可能指向http://xx.xx.xx.xx:8080上，但访问了不同的JEE应用，所以可能出现匹配多个应用的情况
				 */
				if (matchedProfiles.length>0) {
					
					for(var m=0;m<matchedProfiles.length;m++) {
						
						var matchProfile=matchedProfiles[m];
						
						//filter those we don't want show client targets
						if (targetFilters!=undefined&&this.checkClientFilter(matchProfile["appurl"],targetFilters)==false) {
							continue;
						}						
						
						//only app cluster to limit the none level 2 app node to get the clients
						var serviceNode;
						if (acData["scope"]["view"]=="app") {
							targetNode=this.onAppNew(matchProfile,{matchNodeUrl:target,needClientAccess:false,allowOtherApps:true,slevel:3});
						}
						else {
							targetNode=this.onAppNew(matchProfile,{matchNodeUrl:target,needClientAccess:true,allowOtherApps:true});
						}
						
						if(targetNode!=undefined) {
							var edge=this.onAppEdgeUpdate(appInstNode, targetNode,{workstate:1,dead:dead,ts:cts,extattrs:{client:client,appInst:appInst,ts:cts,urls:vals["urls"]}});
						
							//cache client data
							appOut[target]={node:targetNode,edge:edge};
						}
					}
					
					continue;
				}
				
				//filter those we don't want show client targets
				if (targetFilters!=undefined&&this.checkClientFilter(target,targetFilters)==false) {
					continue;
				}
				
				//match response header "Server"
				var server=vals["svr"];				
				if (server!=undefined) {
					if (server.indexOf("nginx")>-1||server.indexOf("Apache/")>-1) {
						targetNode=this.createNoneAppNode(target, "代理服务:"+server+"\n"+target, "proxy", 1, 1, 1, 1);
						
						//record the proxy server
						acData["appProxy"][target]=targetNode;
					}
					else {
						targetNode=this.createNoneAppNode(target, "未知http服务:"+server+"\n"+target, "unknown", 1, 1, 1, 3);
					}
				}
				//if no match, then add an unknow app node
				else {
					targetNode=this.createNoneAppNode(target, "未知http服务\n"+target, "unknown", 1, 1, 1, 3);
				}
			}
			//jdbc client/redis/mongo
			else if (target.indexOf("jdbc:")==0||target.indexOf("redis:")==0||target.indexOf("mongo:")==0) {
				
				//filter those we don't want show client targets
				if (targetFilters!=undefined&&this.checkClientFilter(target,targetFilters)==false) {
					continue;
				}
				
				var title=this.getDBTitle(target);
				
				targetNode=this.createNoneAppNode(target, title, "db", 1, 1, 1, 4,{vals:vals});
			}
			//mq
			else if (target.indexOf("mq:")==0) {
				
				//filter those we don't want show client targets
				if (targetFilters!=undefined&&this.checkClientFilter(target,targetFilters)==false) {
					continue;
				}
				
				var title=this.getDBTitle(target);
				
				targetNode=this.createNoneAppNode(target, title, "mq", 1, 1, 1, 4);
			}
				
			var edge=this.onAppEdgeUpdate(appInstNode, targetNode,{workstate:1,dead:dead,ts:cts,extattrs:{client:client,appInst:appInst,ts:cts,urls:vals["urls"]}});
			
			//cache client data
			appOut[target]={node:targetNode,edge:edge};
		}		
	};
	
	this.checkClientFilter=function(target,targetFilters) {
		
		if (targetFilters!=undefined) {
			
			for(var tf=0;tf<targetFilters.length;tf++) {
				
				if (target.indexOf(targetFilters[tf])>-1) {
					return true;
				}
			}							
			
		}
		
		return false;
	};
	
	/**
	 * DB title
	 */
	this.getDBTitle=function(target) {
		
		var title;
		
		var info=target.split("?");
		info=info[0].split("/");
		var ips=info[2].split(",");
		var ipStr="";
		for (var i=0;i<ips.length;i++) {
			ipStr+="\n"+ips[i];
		}			
		 
		//mysql
		if (target.indexOf("jdbc:mysql")==0) {
			
			title="MySQL数据源:";
		}
		//oracle
		else if (target.indexOf("jdbc:oracle")==0) {
			
			title="Oracle数据源:";
		}
		//redis
		else if (target.indexOf("redis:")==0) {
			
			title="Redis数据源:";
		}
		//mongo
		else if (target.indexOf("mongo:")==0) {
			
			title="Mongo数据源:";
		}
		//mq
		else if (target.indexOf("mq:")==0) {
			
			title="mq队列:";
		}
		
		title+=((info[3]!=undefined)?info[3]:"")+ipStr;
		
		return title;
	};
	
	
	/**
	 * TODO: app client edge
	 * on app edge new creation or update
	 */
	this.onAppEdgeUpdate=function(appInstNode,targetNode,info) {
		
		var tip="";
		
		if (info["ts"]!=undefined) {
			tip+="最近访问:"+TimeHelper.getTime(info["ts"]);			
		}
		
		var newEdge={from:appInstNode["id"],to:targetNode["id"],tip:tip,width:1,dead:info["dead"],workstate:info["workstate"],extattrs:info["extattrs"]};
		
		acLayout.edges().addEdges([newEdge]);
		
		return newEdge;
	};
	
	this.startAppEdgeMoUpdate=function() {
		
		/**
		 * check if the TopWnd is open
		 */
		var isTopWndHide=window.winmgr.isHide(acData["wndid"]);
		if (isTopWndHide==true) {
			return;
		}
		
		if (acData["scope"]["view"]=="1app") {
			this.startIPLinkUpdate();
		}
		else {
			this.startIPLinkUpdate();//this.startAppClientEdgeMoUpdate();
		}
	};
	
	//real get the client perf data
	this.startAppClientEdgeMoUpdate=function() {
		
		//only refresh those client target in appOut
		var urls=[];
	
		for(var appOid in acData["appOut"]) {
			
			var appOut=acData["appOut"][appOid];
			
			for(var target in appOut) {
				
				var extattrs=appOut[target]["edge"]["extattrs"];
				if (extattrs==undefined) {
					continue;
				}
				var client=extattrs["client"];
				
				if (client==undefined) {
					continue;
				}				
				var appInst=extattrs["appInst"];
				
				for (var path in client["values"].urls) {
					
					var urlAttrs=client["values"].urls[path];
					
					//only http need contact path
					var url=client["id"];
					if (client["id"].indexOf("http:")==0) {
						url+=path;
					}
					
					urls[urls.length]={
							ip:appInst["ip"],
							svrid:appInst["svrid"],
							url:appInst["hostport"]+"#"+appInst["appid"]+"#"+url
					};
				}
				
			}
		}

		app.controller.runAppClientURLMO(urls);
	};
	
	this.getAppEdgeState=function(edge,eData) {
		
		var tsTip="";
		if (edge["extattrs"]["ts"]!=undefined) {
			tsTip=" 最近访问:"+TimeHelper.getTime(edge["extattrs"]["ts"]);
		}
		
		if (eData["tps"]>0) {
			edge["dead"]=false;
			if (eData["ts"]!=undefined) {
				tsTip=" 最近访问:"+TimeHelper.getTime(eData["ts"]);
			}
		}
		else if (eData["tps"]==0) {
			edge["dead"]=true;
		}
		
		edge["tip"]="QPM:"+eData["tps"]+" 全程平均响应:"+eData["tavg"]+" 当前分钟响应:"+((eData["mavg"]==undefined)?"-":eData["mavg"])+" 错误:"+eData["err"]+tsTip;
		
		var errMORate=eData["errMORate"];
		var avgMORate=eData["mavgMORate"];
		
		var workStateVal=errMORate["level"]+avgMORate["level"];
		
		if (workStateVal==0) {
			edge["workstate"]=1;
		}
		else if (workStateVal==1) {
			edge["workstate"]=0;
		}
		else if (workStateVal>1){
			edge["workstate"]=-1;
		}
		
		if (eData["tpsMORate"]!=undefined) {
			edge["width"]=eData["tpsMORate"]["level"]*3+1;
		}
	};
	
	this.onAppEdgeMoUpdate=function(urlMOs) {
		
		/**
		 * check if the TopWnd is open
		 */
		var isTopWndHide=window.winmgr.isHide(acData["wndid"]);
		if (isTopWndHide==true) {
			return;
		}
		
		var edgeData={};
		
		for(var key in urlMOs) {
			
			var keyInfo=key.split("#");
			
			var edgeUrl=keyInfo[2];
			if (edgeUrl.indexOf("http")==0) {
				var info=edgeUrl.split("/");
				edgeUrl=info[0]+"//"+info[2];
			}
			
			var edgeId=keyInfo[0]+"#"+keyInfo[1]+"#"+edgeUrl;
			
			if(edgeData[edgeId]==undefined) {
			   edgeData[edgeId]={tps:0,tavg:0,mavg:0,err:0,count:0,num:0,ts:undefined};
			}
			
			var urlMO=urlMOs[key];
			
			edgeData[edgeId]["tps"]+=urlMO["tps"];
			edgeData[edgeId]["tavg"]+=urlMO["tavg"];
			edgeData[edgeId]["err"]+=urlMO["err"];
			edgeData[edgeId]["count"]+=urlMO["count"];
			if (urlMO["mavg"]!=undefined) {
				edgeData[edgeId]["mavg"]+=urlMO["mavg"];
			}
			edgeData[edgeId]["num"]+=1;
			
			if (urlMO["timestamp"]!=undefined) {
				if (edgeData[edgeId]["ts"]==undefined) {
					edgeData[edgeId]["ts"]=urlMO["timestamp"];
				}
				else {
					if (urlMO["timestamp"]>edgeData[edgeId]["ts"]){
						edgeData[edgeId]["ts"]=urlMO["timestamp"];
					}				
				}
			}
		}
		
		for(var appOid in acData["appOut"]) {
			
			var appOut=acData["appOut"][appOid];
			
			var appOInfo=appOid.split("---");
			
			var targetPrefix;
			
			//JEE
			if (appOInfo.length==2) {
				targetPrefix=appOInfo[0].split("/")[2]+"#"+appOInfo[1]+"#";
			}
			//JSE
			else {
				var tmpInfo=appOInfo[0].split("/");
				targetPrefix=tmpInfo[2]+"#"+tmpInfo[3]+"#";
			}
			
			for(var target in appOut) {
				
				var eData=edgeData[targetPrefix+target];
				
				if (eData==undefined) {
					continue;
				}
				
				eData["tpsMORate"]=app.controller.getMORate("tps",eData["tps"]);
				eData["errMORate"]=app.controller.getMORate("err",eData["err"],eData["count"]);
				
				eData["tavg"]=eData["tavg"]/eData["num"];
				
				eData["avgMORate"]=app.controller.getMORate("tavg",eData["tavg"]);
				
				eData["mavg"]=eData["mavg"]/eData["num"];
				
				eData["mavgMORate"]=app.controller.getMORate("mavg",eData["mavg"]);
	    		
	    		var edge=appOut[target]["edge"];
	    		
	    		this.getAppEdgeState(edge,eData);
	    		
	    		acLayout.edges().updateEdge(edge);
			}
		}
	};
	
	/**
	 * on app edge db click
	 */
	this.onAppEdgeDbclick=function(params) {
		
		if (params["edges"].length==0) {
			return;
		}
		
		var edgeInfo=acLayout.edges().getEdge(params["edges"][0]);
		
		var extattrs=edgeInfo["extattrs"];
		
		if (extattrs==undefined) {
			return;
		}
		
		//Type 1: App Client shows AppClientURLChartWnd
		var client=extattrs["client"];
		
		if (client!=undefined) {
			
			var urls=[];
			
			var appInst=extattrs["appInst"];
			
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
			
			var sObj={name:client["id"],urls:urls,clientStr:clientStr,appid:appInst.appid,hostport:appInst.hostport,ip:appInst.ip,svrid:appInst.svrid,backWndId:acData["wndid"]};
			
			app.controller.showWindow(sObj,"AppClientURLChartWnd","buildAppClientURLChartWnd","runAppClientURLChart");
			
			return;
		}
		
		//Type 2.1 : Iplink edge (proxy, browser)
		var ipSrcObj=extattrs["ipSrcObj"];
		
		if (ipSrcObj!=undefined) {
			
			var isProxy=(true==extattrs["isProxy"])?true:false;
			
			var sObj={ipSrcObj:ipSrcObj,id:edgeInfo.id,backWndId:acData["wndid"],isProxy:isProxy};
			
			app.controller.showWindow(sObj,"AppProxyEdgeDetailWnd","buildAppProxyEdgeDetailWnd");
			
			return;
		}
		
		//Type 2.2: Iplink edge (unknown applications)
		var uApps=extattrs["uapps"];
		
		if (uApps!=undefined) {
			
			var sObj={uapps:uApps,id:edgeInfo.id,backWndId:acData["wndid"]};
			
			app.controller.showWindow(sObj,"AppUnknowEdgeDetailWnd","buildAppUnknowEdgeDetailWnd");
			
			return;
		}
	};	
	
	/**
	 * TODO： IP Link
	 * on app ip link update	
	 * 
	 */	
	this.startIPLinkUpdate=function() {
		
		var appInsts=acData["profile"]["appinsts"];

		var appIplinkIds=[];
		
		for(var i=0;i<appInsts.length;i++) {
			
			var appInst=appInsts[i];
			
			//prepare app ip link ids
			//if (acData["scope"]["view"]=="app") {
				appIplinkIds[appIplinkIds.length]=appInst["appgroup"]+"@"+appInst["o_appurl"];
			//}
		}	
		
		var appIplinkQueryStr=StringHelper.obj2str(appIplinkIds);
		
		var _this=this;
		
		AjaxHelper.call({
            url: "../../rs/godeye/profile/q/iplink",
            data: appIplinkQueryStr,
            async: true,
            cache: false,
            type: "post",
            dataType: "html",
            success: function (data) {
//            	console.log(">>>>>>>>>"+data);
            	
            	var jsonData = eval("("+data+")");
    	    	jsonData=jsonData["rs"];
    	    	jsonData=eval("("+jsonData+")");
    	    	
    	    	_this.onAppIPLinkUpdate(jsonData);
    	    	//run real client perf data
    	    	_this.startAppClientEdgeMoUpdate();
            },
            error: function (data) {
            	console.log("Access IPLink Data Fail. Please check the network is available");
            	//run real client perf data to ensure the iplink not get but client data can still be done
    	    	_this.startAppClientEdgeMoUpdate();
            },
        });	
	};
	
	this.onAppIPLinkUpdate=function(ipLnkMap) {
		
		var appInsts=acData["profile"]["appinsts"];
		
		for(var i=0;i<appInsts.length;i++) {
			
			var appInst=appInsts[i];
			
			//don't do ipLink of MonitorAgent in none-UAV group. 
			if(appInst["appid"]=="MonitorAgent"&&appInst["appgroup"]!="UAV"){
				continue;
			}

			var appO=this.getAppInfo(appInst);
			
			//step 1: check if the app inst has in-call data set
			var appIn=acData["appIn"][appO["id"]];
			
			//if none, just create one
			if (appIn==undefined) {
				acData["appIn"][appO["id"]]={};
				appIn=acData["appIn"][appO["id"]];
			}
			
			//step 2: iplink matching
			var iplnk=ipLnkMap[appInst["appgroup"]+"@"+appInst["o_appurl"]];
			
			for(var ipSrc in iplnk) {
				
				if (ipSrc.indexOf("-ts")>-1) {
					continue;
				}
				
				var targetNode;
				
				var targetEdge;
								
				var sts=parseInt(iplnk[ipSrc+"-ts"]);
				
				var dead=this.getIPLinkState(sts);
				
				var ipSrcObj=eval("("+iplnk[ipSrc]+")");
						
				//create node
				targetNode=this.createIPLinkNode(ipSrc,ipSrcObj,appInst,{dead:dead,ts:sts});
				
				if (targetNode==undefined) {
					continue;
				}
				
				//should go to appOut not in appIn
				if (targetNode["itype"]=="app") {					
					continue;
				}
				
				//browser/application node edge without proxy or proxy node edge
				var edgeExattrs={ipSrcObj:ipSrcObj,ts:sts};
				
				//unknown application node edge
				if (targetNode["extattrs"]["uapps"]!=undefined) {
					edgeExattrs={uapps:targetNode["extattrs"]["uapps"],ts:targetNode["extattrs"]["uappsts"]};
				}
				
				targetEdge=this.onAppEdgeUpdate(targetNode,{id:appO.id},{workstate:-2,dead:dead,ts:sts,extattrs:edgeExattrs});
				
				appIn[ipSrc]={node:targetNode,edge:targetEdge};
			}
		}
		
		//real match proxy
		for(var proxy in acData["srcProxy"]) {
			
			var pObj=acData["srcProxy"][proxy];
			
			var ipSrc=pObj["ipSrc"];
			
			var sprofiles=pObj["sprofiles"];
			
			var proxyNode=undefined;
			
			var targetEdge=undefined;
			
			//step 1: match the appProxy exist in graph
			for(var target in acData["appProxy"]) {
				
				if (target.indexOf(proxy)>-1) {
					
					proxyNode=acData["appProxy"][target];
					
					/**
					 * 通过Proxy后方的App可能有多个
					 */
					for(var tm=0;tm<sprofiles.length;tm++) {
						
						var sprofile=sprofiles[tm]["pf"];
						
						var dead=sprofiles[tm]["ext"]["dead"];
						
						var sts=sprofiles[tm]["ext"]["ts"];
						
						var ipSrcObj=sprofiles[tm]["ipSrcObj"];
						
						var targetAppO=this.getAppInfo(sprofile);
						
						var appIn=acData["appIn"][targetAppO["id"]];
						
						targetEdge=this.onAppEdgeUpdate(proxyNode,{id:targetAppO.id},{workstate:-2,dead:dead,ts:sts,extattrs:{ipSrcObj:ipSrcObj,ts:sts,isProxy:true}});
						
						appIn[ipSrc]={node:proxyNode,edge:targetEdge};
					}				
					
					break;
				}
			}
			
			//step 2: no match appProxy and link them
			if (proxyNode==undefined) {
				
				proxyNode=this.createNoneAppNode(ipSrc,"代理服务\n"+ipSrc,"proxy",1,1,1,1);
				
				/**
				 * 通过Proxy后方的App可能有多个
				 */
				for(var tm=0;tm<sprofiles.length;tm++) {
					
					var sprofile=sprofiles[tm]["pf"];

					var dead=sprofiles[tm]["ext"]["dead"];
					
					var sts=sprofiles[tm]["ext"]["ts"];
					
					var ipSrcObj=sprofiles[tm]["ipSrcObj"];
					
					var targetAppO=this.getAppInfo(sprofile);
					
					var appIn=acData["appIn"][targetAppO["id"]];
				
					targetEdge=this.onAppEdgeUpdate(proxyNode,{id:targetAppO.id},{workstate:-2,dead:dead,ts:sts,extattrs:{ipSrcObj:ipSrcObj,ts:sts,isProxy:true}});
								
					appIn[ipSrc]={node:proxyNode,edge:targetEdge};
				}
			}
			
			//step 3:add those unknow src nodes through proxy
			for(var m=0;m<pObj["src"].length;m++) {
				
				var srcNode=pObj["src"][m];
				
				var edgeExt;
				var sts;
				var dead;
				
				//unknown applications
				if(srcNode["extattrs"]["uapps"]!=undefined) {
					sts=srcNode["extattrs"]["uappsts"];
				}
				//browser
				else {
					sts=srcNode["extattrs"]["ts"];
				}
				
				dead=this.getIPLinkState(sts);
				edgeExt={ts:sts};
				
				this.onAppEdgeUpdate(srcNode,proxyNode,{workstate:-2,dead:dead,ts:sts,extattrs:edgeExt});
			}
		}
	};
	
	//get IP Link State
	this.getIPLinkState=function(ts) {
		var curTime=new Date().getTime();
		
		if (curTime-ts>60000) {
			return true;
		}
		else {
			return false;
		}		
	};
		
	//create ip link node
	this.createIPLinkNode=function(ipSrc,ipSrcObj,targetProfile,ext) {
		
		var targetNode;
		
		//unknow application
		if (ipSrc.indexOf("app://")==0) {
			
			var appO=this.getAppInfo(targetProfile);
			
			var id="目标:"+appO.id;
			
			//未知应用 node
			//targetNode=this.createNoneAppNode(ipSrc,"未知应用\n"+ipSrc,"unknown",1,1,1,0,ext);
			
			if(acLayout.nodes().existNode(id)==true) {
				
				targetNode=acLayout.nodes().getNode(id);
				
				targetNode["extattrs"]["uapps"][ipSrc]={info:ipSrcObj,ext:ext};
				
				var curTS=targetNode["extattrs"]["uappsts"];
				
				if (curTS==undefined) {
					targetNode["extattrs"]["uappsts"]=ext["ts"];
				}
				else {
					if (ext["ts"]-curTS>0) {
						targetNode["extattrs"]["uappsts"]=ext["ts"];
					}
				}
				
				var appNum=Object.getOwnPropertyNames(targetNode["extattrs"]["uapps"]).length;
				
				acLayout.nodes().updateNodeLabel(id,"未知应用集合("+appNum+")\n目标:"+targetProfile["appurl"]);
			}
			else {
				
				var nodeExt={uapps:{},uappsts:ext["ts"]};
				nodeExt["uapps"][ipSrc]={info:ipSrcObj,ext:ext};				
				targetNode=this.createNoneAppNode(id,"未知应用集合(1)\n目标:"+targetProfile["appurl"],"unknown",1,1,1,0,nodeExt);
			}
		}
		//pure user
		else if (ipSrc.indexOf("user://")==0) {
			
			if (ext["proxy"]==undefined) {
				return undefined;
			}
			
			//to create only 1 node for all users through this proxy
			var pbrowser="browser:"+ext["proxy"];
			
			targetNode=this.createNoneAppNode(pbrowser,"浏览器用户","browser",1,1,1,0,ext);
		}
		//browser
		else if (ipSrc.indexOf("browser://")==0) {
			
			targetNode=this.createNoneAppNode(ipSrc,"浏览器用户","browser",1,1,1,0,ext);
		}
		//proxy
		else if (ipSrc.indexOf("proxy://")==0) {
			
			var proxyAddr="//"+ipSrc.substring(8);
			
			//反向代理 node
			var srcPObj=acData["srcProxy"][proxyAddr];
			
			if (srcPObj==undefined) {
				acData["srcProxy"][proxyAddr]={sprofiles:[{pf:targetProfile,ext:ext,ipSrcObj:ipSrcObj}],src:[],ipSrc:ipSrc};
				srcPObj=acData["srcProxy"][proxyAddr];
			}
			else {
				srcPObj["sprofiles"][srcPObj["sprofiles"].length]={pf:targetProfile,ext:ext,ipSrcObj:ipSrcObj};
			}			
			
			//访问源	
			if (ipSrcObj!=undefined) {	
				
				var src=srcPObj["src"];
				
				var ipClients=ipSrcObj["clients"];
				
				for(var ipSrcClient in ipClients) {
					
					var ts=ipClients[ipSrcClient];
					
					var ipSrcClientNode=this.createIPLinkNode(ipSrcClient,undefined,targetProfile,{ts:ts,proxy:proxyAddr});
					
					//only make an edge for none app node, as app node has client edge
					if (ipSrcClientNode!=undefined&&ipSrcClientNode["itype"]!="app") {
						src[src.length]=ipSrcClientNode;
					}
				}
			}
		}
		//application
		else {
			var tProfiles=this.getMatchAppProfiles(ipSrc);
			
			if (tProfiles.length>0) {
				
				var tfilters=[targetProfile["appurl"]];
				
				if (ext!=undefined) {
					tfilters[tfilters.length]=ext["proxy"];
				}
				
				targetNode=this.onAppNew(tProfiles[0], {needClientAccess:true, allowOtherApps:true,slevel:0,targetFilters:tfilters});
			}
			else {
				//未知应用 node
				var appO=this.getAppInfo(targetProfile);
				
				var id="目标:"+appO.id;
				
				if(acLayout.nodes().existNode(id)==true) {
					
					targetNode=acLayout.nodes().getNode(id);
					
					targetNode["extattrs"]["uapps"][ipSrc]={info:ipSrcObj,ext:ext};
					
					var curTS=targetNode["extattrs"]["uappsts"];
					
					if (curTS==undefined) {
						targetNode["extattrs"]["uappsts"]=ext["ts"];
					}
					else {
						if (ext["ts"]-curTS>0) {
							targetNode["extattrs"]["uappsts"]=ext["ts"];
						}
					}
					
					var appNum=Object.getOwnPropertyNames(targetNode["extattrs"]["uapps"]).length;
					
					acLayout.nodes().updateNodeLabel(id,"未知应用集合("+appNum+")\n目标:"+targetProfile["appurl"]);
				}
				else {
					
					var nodeExt={uapps:{},uappsts:ext["ts"]};
					nodeExt["uapps"][ipSrc]={info:ipSrcObj,ext:ext};				
					targetNode=this.createNoneAppNode(id,"未知应用集合(1)\n目标:"+targetProfile["appurl"],"unknown",1,1,1,0,nodeExt);
				}
				//targetNode=this.createNoneAppNode(ipSrc,"未知应用\n"+ipSrc,"unknown",1,1,1,0,ext);
			}
		}
		
		return targetNode;
	};
	
	//--------------------------------COMMON API--------------------------------------
	/**
	 * TODO: COMMON API
	 */
	this.createNoneAppNode=function(id,label,itype,state,workState,workPower,slevel,ext) {
		//未知应用 node
		targetNode={id:id,label:label,itype:itype,state:state,workState:workState,workPower:workPower,extattrs:ext};
		
		//only app cluster set level
		if (acData["scope"]["view"]=="app") {
			targetNode["level"]=slevel;
		}
		
		acLayout.addNodes([targetNode]);
		
		return targetNode;
	};
	//---------------------------------More Plugins-------------------------------------
	/**
	 * TODO: alert notification plugin
	 */
	this.buildAlertNtfyPlugin=function(wndId,sObj) {
		
		var cid=wndId+"_NtfyCtn";
		
		var sb=new StringBuffer();
		sb.append("<div class='pluginAlertNtfy' ><span class='glyphicon glyphicon-cog' onclick=\"app.controller.openClose('"+cid+"')\" title='查看服务流报警事件'></span></div>");
		sb.append("<div id='"+cid+"' class='pluginAlertNtfyContainer' style='display:none;'>");
		sb.append("<div class='pluginAlertNtfyContainerTitle'>&nbsp;</div>");
		sb.append("<div class='pluginAlertNtfyContainerContent'></div>");
		sb.append("</div>");
		
		return sb.toString();
	};
}