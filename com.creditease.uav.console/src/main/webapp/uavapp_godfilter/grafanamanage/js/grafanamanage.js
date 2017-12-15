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
	id : "winRowModify",
	height : "auto",
	"overflow-y" : "auto",
	order : 998,
	theme : "win"
});

var showConfig = {
		head:"提示",
		content:""
};

var mtableConfig = {
	id:"tableList",
	pid:"winList",
	openDelete:true,
	key:"id",
	pagerSwitchThreshold:600,
	pagesize : 20,
	head:{
		    emailListName : ['应用组___应用集群', '27%'],
			createtime : ['创建时间', '27%'],
			operationtime : ['操作时间', '27%'],
			owner : ['操作人', '19%']
	    },
	cloHideStrategy:{
		1100:[0,1,2,3],
		1000:[0,2,3],
		800:[0,2,3],
		500:[0,2],
		400:[0]
	}
};

var modalConfig = {
		head:"删除",
		content:"用户输入提示信息",
		callback:"pageClass.ajaxRemoveCommit()"
};

var jsonConfig = {
		"attribute":{frontSize:14,color:'#008000'},
		"showLayerNum":3,
		"model1":[
		   			{keyT:"{}",onClick:""},   
					{keyT:"<div class='groupLayer0'>{@key}</div>",onClick:""}
				]					        
};


/**
 * 全局变量定义
 */
var addScrollTop=10; //滚动条添加点数
var profileData;//存储全局profile数据
var modifyAppGroupId;
var appurlsArray;
var checkDivFlag=0;//判定div span执行事件标志
var checkSpanFlag=0;


/**
 * TODO 渲染首页列表
 */
function initListDiv() {

	var div = new StringBuffer();
	div.append("<div id=\"srarchDiv\" class=\"AppHubMVCSearchBar\" >");

	div.append('<button class="btn btn-default" type="button" onclick="javascript:showAddDiv()">');
	div.append('<span class="glyphicon glyphicon-plus"></span>');
	div.append('</button>');
	
	div.append("<input id=\"inputValue\" class=\"form-control AppHubMVCSearchBarInputText\"");
	div.append("type=\"text\" placeholder=\"应用组___应用集群模糊检索\" value=\"\"></input>");
	
	div.append("<div class=\"btn-group\">");
	div.append("<button id=\"searchbtn\" type=\"button\" class=\"btn btn-default\" onclick=\"javascript:loadListData(false);\">");
	div.append("<span class=\"glyphicon glyphicon-search\"></span>");
	div.append("</button>");
	div.append("<button id=\"searchAllbtn\" type=\"button\" class=\"btn btn-default\"  onclick=\"javascript:loadListData(true);\">");
	div.append("<span class=\"glyphicon glyphicon-th\"></span>");
	div.append("</button>");
	div.append("</div>");
	
	div.append("</div>");
	
	HtmlHelper.id("winList").innerHTML += div.toString();
	showListDiv();
};

/**
 * 页面转换
 */
function showListDiv(){
	window.winmgr.show("winList");
	window.winmgr.hide("winRow");
};

function showRowDiv(resetTime){
	window.winmgr.show("winRow");
	window.winmgr.hide("winList");

	$(".modal-footer").slideDown();
};

function showRowModifysuccess(resetTime){
	window.winmgr.show("winList");
	window.winmgr.hide("winRowModify");

	$(".modal-footer").slideDown();
};

function showRowModifyDiv(resetTime){
	window.winmgr.show("winRowModify");
	window.winmgr.hide("winList");

	$(".modal-footer").slideDown();
};


/***
 * --------------------------------------------------
 * 创建dashboard
 *--------------------------------------------------
 */
function showAddDiv(){
	$("#saveTrCheckbox").hide();
	setHtmlText("addErrorMsg", "");
	$("#addEmailListName").val("");

	//渲染应用组——应用集群
	var obj = pageClass.allGroupsIdMap;
	var div = new StringBuffer();
	
	div.append( "<div class=\"headDiv\">");
	div.append( "<div class=\"icon-signout icon-myout\" onclick=\"javascript:showListDiv()\" \"></div>");
	div.append("<button id=\"searchbtn\" type=\"button\" class=\"icon-myout_right\" onclick=\"javascript:creatAppgroupAppid();\" >");
	div.append("<span class=\"glyphicon glyphicon-saved\"></span>");
	div.append("</button>");
	div.append( "</div>");
	
	div.append(getAppGroupIdDiv(obj));
	var getDashboardData=pageClass.emailList;
	
	HtmlHelper.id("winRow").innerHTML = div.toString();
	//渲染  应用组和应用集群之前已创建的状态
	for(var keyIndex in getDashboardData){
		var appgr=keyIndex.substring(0,keyIndex.indexOf("___"))+"ex";//加一个ex 区分原来name另做它用
		var appi=appgr+keyIndex.substring(keyIndex.indexOf("___")+3);//加上应用组名  防止不同应用组中 应用集群重名 导致ID名一样 无法更改样式
		appgr=pageClass.encodeForJqueryFormat(appgr);
		appi=pageClass.encodeForJqueryFormat(appi);
		
		$("#"+appi).attr("class","titleDiv3 beforeSelect");
		$("#"+appgr).attr("class","titleDiv2 beforeTitleSelect");
	}
	
	showRowDiv(true); 
	
	
	// 渲染应用组-应用集群组件 供选择（有可能为空）
	function getAppGroupIdDiv(obj){
		
		//没有组件则不渲染
		if("{}"==obj){
			return;
		}
			
		var AppGroupIdCont = appGroupSort(obj);//排序 
		var sb = new StringBuffer(); 
		var index = 0;
		sb.append( "<div class=\"titleAddDiv\">选择[应用组___应用集群]创建监控面板</div>");
		sb.append("<div id='AppgroupAppidContMainDiv'>");
		//组件循环
		$.each(AppGroupIdCont,function(name,subobj){
			sb.append("<div style=\"margin-bottom:5px;\">");
			//组件标题
			sb.append("<div  id='"+name+"ex"+"'  class=\"titleDiv2\" onclick=\"javascript:checkAllSelectOnclick(this,'"+name+"ex"+"')\">"+name+"");
			sb.append("<span class=\"glyphicon glyphicon-check\" style=\"z-index:998;float:right;font-size: 20px;background-color: #eeeeee; border-radius: 3px;border:solid 0.5px #eeeeee;\" onclick=\"javascript:checkSlectedDiv()\"></span>");
			sb.append("</div>");
			sb.append("<div id='"+name+"' style='display:none' >");
			//组件内容
			$.each(subobj,function(index,url){
				sb.append("<div id='"+name+"ex"+url+"'   class=\"titleDiv3\" onclick='javascript:setUrlCssChange(this,\"AppgroupAppidContMainDiv\",\"servicesJee\")'>"+url+"</div>");
			});
			sb.append("</div>");
			
			sb.append("</div>");
		});

		sb.append("</div>");
		
		return sb.toString();
	}
	
};

function creatAppgroupAppid(){
	
	var AppGroupIdCont = $("#AppgroupAppidContMainDiv").find("div");
	var serviceJee = $("#servicesJee").find("div");
	
	if(checkUserSel(AppGroupIdCont)){
	
		findCheckUserAppgroupAppid();
		pageClass.ajaxCreateDashboard();//post请求到后端  然后ajaxGroup load数据 页面显示
	}else{
		showConfig.content="请选择应用组___应用集群！";
		showDialog(showConfig);
	}	
};


//查找选中的应用组-应用集群 利用[]进行存储
function findCheckUserAppgroupAppid(){
	
	var div = new StringBuffer();
	pageClass.AllCreateDashboard=[];
	var appUrl = $("#appurlInput").val();
	var AppGroupIdCont = $("#AppgroupAppidContMainDiv").find("div");
	//应用组 组件url
	if ((checkUserSel(AppGroupIdCont))) {
		
		var AppGroupIdDivs = $("#AppgroupAppidContMainDiv").find(".titleDiv2");
		$.each(AppGroupIdDivs,function(index,server){ 
			
			//选中组件
			if(server.className.indexOf("userSelect") >=0){
				//此处不可用innerHTML 包含span的全选元素 
				var serverValue = server.innerText;
				//格式化(避免jquery获取不到)
				serverValue = pageClass.encodeForJqueryFormat(serverValue);

				//选中组件下的DIV获取
				var urldivs = $("#"+serverValue).find(".titleDiv3");
			
				$.each(urldivs,function(index2,url){ 
					//选中组件URL
					if(url.className.indexOf("userSelect") >=0&&server.className.indexOf("beforeSelect")<0){

						pageClass.oneCreateDashboard.appgroup=server.innerText;
						pageClass.oneCreateDashboard.appid=url.innerHTML;
						//找到该appgroups-appid 对应的appurls
						pageClass.oneCreateDashboard.appurls=pageClass.allAppGroupIdUrlMap.get(server.innerText+"___"+url.innerHTML);
						//将单个应用组-应用集群扔到 pageClass.AllCreateDashboard：{}中   再对pageClass.oneCreateDashboard清空 
						pageClass.AllCreateDashboard.push(pageClass.oneCreateDashboard);
						pageClass.oneCreateDashboard={"appgroup":"","appid":"","appurls":[]};
					}
				});	
			}
		});
	}
};


/***
 * --------------------------------------------------
 * 展示dashboard内容      
 * （应用实例、服务组件、客户端组件）
 *--------------------------------------------------
 */
function showModifyAppInstanceServicesClientsDiv(id){
	//加一次判定，如之前创建了dashboard,但该应用组——应用集群下线，对其修改无效，加提示
	var ExistGroup=id.substring(0,id.indexOf("___"));
	var ExistId=id.substring(id.indexOf("___")+3);
	if(!pageClass.allGroupsIdMapVal.contain(ExistGroup)){
		showConfig.content="该应用组不存在";
		showDialog(showConfig);
		return;
	}
	else{
		var checkAppidExist=false;
		var tempIdValueSet=pageClass.allGroupsIdMapVal.get(ExistGroup);
		var tempIdValueSetLs=tempIdValueSet.ls;
		for(var i in tempIdValueSetLs){
			if(!tempIdValueSetLs.hasOwnProperty(i)) {
				continue;
			}
			if(tempIdValueSetLs[i]==ExistId){
				checkAppidExist=true;	
				break;
			}
		}
		if(!checkAppidExist){
			showConfig.content="该应用组___应用集群不存在！";
			showDialog(showConfig);
			return;
		}
		
	}
	// 加载应用实例 服务组件 客户端组件 

	//通过appgroup-appid 找到对应的appurls  
	//再遍历各appurl 找到对应的cpt.services cpt.clients  
	//再将各服务URL去掉实例名扔到 set中 求相对url  
	//最后渲染实例组件 服务组件  客户端组件
	
	//假定在同一个集群下  服务组件 客户端组件都是一样  取第一个
	//for(var index in appurlsArray)
	modifyAppGroupId=id;
	appurlsArray=pageClass.allAppGroupIdUrlMap.get(modifyAppGroupId);
	var obj = eval("("+profileData[modifyAppGroupId.substring(0,modifyAppGroupId.indexOf("___"))+"@"+appurlsArray[0]]+")");

	var div = new StringBuffer();
	div.append( "<div class=\"headDiv\">");
	div.append( "<div class=\"icon-signout icon-myout\" onclick=\"javascript:showListDiv()\" \"></div>");
	div.append("<button id=\"searchbtn\" type=\"button\" class=\"icon-myout_right\" onclick=\"javascript:modifyAppInstanceServiceClient();\" >");
	div.append("<span class=\"glyphicon glyphicon-saved\"></span>");
	div.append("</button>");
	div.append( "</div>");
	
	//依次渲染  全部的应用实例、服务组件、客户端组件
	div.append(getAppInstanceDiv(appurlsArray));
	div.append(getServicesDiv(obj,appurlsArray));
	div.append(getClientsDiv(obj));
	
	HtmlHelper.id("winRowModify").innerHTML = div.toString();
	
	//渲染应用实例 服务组件 客户端组件	之前选中的状态
	var getDashboardData=pageClass.emailList;
	var getDashboardDataAppgroupId=eval("("+getDashboardData[modifyAppGroupId]+")");
	var SelectedAppurl=getDashboardDataAppgroupId.appurls;
	if((typeof SelectedAppurl=='string')&&SelectedAppurl.constructor==String){
		SelectedAppurl=eval("("+getDashboardDataAppgroupId.appurls+")");
	}
	
	var SelectedService=eval("("+ getDashboardDataAppgroupId.service+")");
	var SelectedClient=eval("("+ getDashboardDataAppgroupId.client+")");
	
	for(var index in SelectedAppurl){
		if(SelectedAppurl.hasOwnProperty(index)){
			var tempurlId=SelectedAppurl[index].replace(/(^\s*)/g,"");
			tempurlId = pageClass.encodeForJqueryFormat(tempurlId);
			$("#"+tempurlId).attr("class","titleDiv2 userSelect");		
		}	
	}
	
	for(var indexservice in SelectedService){
		
		var tempArray=SelectedService[indexservice];
		for(var relativeurl in tempArray ){
			if(tempArray.hasOwnProperty(relativeurl)){
				var tempRelativeurlId=tempArray[relativeurl];
				tempRelativeurlId = pageClass.encodeForJqueryFormat(tempRelativeurlId);
				$("#"+tempRelativeurlId+"subservice").attr("class","titleDiv3 userSelect userSelectUrl");		
			}
		}
		
		var tempServiceId=indexservice;
		 tempServiceId = pageClass.encodeForJqueryFormat(tempServiceId);
		$("#"+tempServiceId+"service").attr("class","titleDiv2 userSelect");
	}
	
	for(var indexclient in SelectedClient){
		
		var tempArray=SelectedClient[indexclient];
		for(var clienturl in tempArray ){
			
			if(tempArray.hasOwnProperty(clienturl)){
				var tempClienturlId=tempArray[clienturl];
				tempClienturlId = pageClass.encodeForJqueryFormat(tempClienturlId);
				$("#"+tempClienturlId+"subclient").attr("class","titleDiv3 userSelect userSelectUrl");	
			}
		}
		
		indexclient = pageClass.encodeForJqueryFormat(indexclient);
		$("#"+indexclient+"client").attr("class","titleDiv2 userSelect");
	}
	
	showRowModifyDiv(true);
		
	 //渲染  应用实例组件
	function getAppInstanceDiv(appurlsArray){
		var AppInstance={};
		for(var i=0 ;i<appurlsArray.length;i++){
			AppInstance[appurlsArray[i]]=[];
		}
		//没有组件则不渲染
		if("{}"==AppInstance){
			return;
		}
		
		var sb = new StringBuffer(); 
		var index = 0;
		sb.append( "<div class=\"titleDiv\">应用实例</div>");

		sb.append("<div id='AppInstanceContMainDiv'>");
		//组件循环
		$.each(AppInstance,function(name,subobj){
			sb.append("<div style=\"margin-bottom:5px;\">");
			//组件标题
			sb.append("<div id='"+name+"' class=\"titleDiv2\" onclick='javascript:setUrlCssChange_instance(this,\"AppInstanceContMainDiv\",\"servicesJee\")'> "+name+"</div>");//setUrlCssChange(obj,divId,showUpId)
			sb.append("</div>");
		});
		sb.append("</div>");
		
		return sb.toString();
	}
	
	 //渲染服务组件 有可能为空
	function getServicesDiv(obj,appurlsArray){
		var services =obj["cpt.services"];
		//没有组件则不渲染
		if("{}"==services){
			return;
		}
		services =eval("("+ services+")");
		var appurlsArray0len=appurlsArray[0].length-1;
		for(var keyIndex in services){
			var tempArray=services[keyIndex];
			for(var arrayindex in tempArray){
				if(!tempArray.hasOwnProperty(arrayindex)){
					continue;
				}
				var tempval=tempArray[arrayindex]+"";
				//服务组件url 与appurl一样长 相对url 使用"/"
				if(tempval.length<=appurlsArray0len){
					tempArray[arrayindex]="/";
				}
				else{
					tempArray[arrayindex]=tempval.substring(appurlsArray0len);
				}

			}
			services[keyIndex]=tempArray;//求得相对URL 覆盖
		}
		
		var sb = new StringBuffer(); 
		var index = 0;
		sb.append( "<div class=\"titleDiv\">服务组件</div>");

		sb.append("<div id='serviceContMainDiv'>");
		//组件循环
		$.each(services,function(name,subobj){
			sb.append("<div style=\"margin-bottom:5px;\">");
			//组件标题
			sb.append("<div id='"+name+"service"+"' class=\"titleDiv2\" onclick=\"javascript:setCssChangeById('"+name+"')\"> ["+subobj.length+"]"+name+"</div>");
			sb.append("<div id='"+name+"' style='display:none' >");
			//组件内容
			$.each(subobj,function(index,url){
				sb.append("<div id='"+url+"subservice"+"' class=\"titleDiv3\" onclick='javascript:setUrlCssChange(this,\"serviceContMainDiv\",\"servicesJee\")'>"+url+"</div>");
			});
			sb.append("</div>");
			
			sb.append("</div>");
		});

		sb.append("</div>");
		
		return sb.toString();
	}
	
	
	// 渲染客户端组件（向后调用的客户端）  有可能为空
	 
	function getClientsDiv(obj){
		var source = obj["cpt.clients"];//取全
		//没有则不渲染
		if("[]"==source){
			return;
		}

		var clients = eval("("+source+")");
		var sb = new StringBuffer(); 
		var index = 0;
		sb.append( "<div class=\"titleDiv\">客户端组件</div>");
	
		sb.append("<div id='clientContMainDiv'>");
		//循环
		$.each(clients,function(index,subobj){
			 
			var url = subobj.id;
			var urls = subobj.values.urls;
			sb.append("<div style=\"margin-bottom:5px;\">");
			//标题
			sb.append("<div id='"+url+"client"+"' class=\"titleDiv2\" onclick=\"javascript:setCssChangeById('"+url+"')\">"+url+"</div>");
			sb.append("<div id='"+url+"' style='display:none' >");
			
			if(url.indexOf("http:")==-1){
				//非http的客户端，直接使用url
				sb.append("<div id='"+url+"subclient"+"' class=\"titleDiv3\" onclick='javascript:setUrlCssChange(this,\"clientContMainDiv\",\"clientJee\")'>"+url+"</div>");	
			}else{
				//http urls 拼接处理
				//内容
				$.each(urls,function(uIndex,uObj){
					var showUrl = url + uIndex;
					sb.append("<div id='"+showUrl+"subclient"+"' class=\"titleDiv3\" onclick='javascript:setUrlCssChange(this,\"clientContMainDiv\",\"clientJee\")'>"+showUrl+"</div>");
				});
			}
			
			sb.append("</div>");
			
			sb.append("</div>");
		});

		sb.append("</div>");
		
		return sb.toString();
	}
	
};


/***
 * --------------------------------------------------
 * 修改dashboard内容       
 * （应用实例、服务组件、客户端组件）
 *--------------------------------------------------
 */
function modifyAppInstanceServiceClient(){
	
	var AppInstanceCont = $("#AppInstanceContMainDiv").find("div");
	var serviceCont = $("#serviceContMainDiv").find("div");
	var clientCont = $("#clientContMainDiv").find("div");
	var AppInstanceContFlag=checkUserSel(AppInstanceCont);
	var serviceContFlag=checkUserSel(serviceCont);
	var clientContFlag=checkUserSel(clientCont);
	
	pageClass.modifyAppInstance=[];  //清空上次修改选项
	pageClass.modifyAppService={};
	pageClass.modifyAppClient={};
	
	//获取当前选中的应用实例
	if(AppInstanceContFlag){
		findCheckUserAppInstance();
	}
	//获取当前选中的服务组件
	if(serviceContFlag){
		findCheckUserAppServices();
	}
	//获取当前选中的客户端组件
	if(clientContFlag){
		findCheckUserAppClients();
	}
	//应用实例 服务组件 客户端组件 都没选上  提示
	if(!(serviceContFlag||clientContFlag||AppInstanceContFlag)){
		showConfig.content="请选择要修改的组件！";
		showDialog(showConfig);  
		return;
	}
	
	var tempModifyValue={
		"createtime":"",
		"updatetime":"",
		"owner":"",
		"appurls":[],
		"service":{},
		"client":{}
	};
	
	tempModifyValue.appurls=pageClass.modifyAppInstance;
	//至少选择一个应用实例
	if(tempModifyValue.appurls.length==0){
		showConfig.content="请至少选择一个应用实例！";
		showDialog(showConfig);
		return;
	}
	tempModifyValue.service=pageClass.modifyAppService;
	tempModifyValue.client=pageClass.modifyAppClient;
	
	pageClass.modifyData.appgroup=modifyAppGroupId.substring(0,modifyAppGroupId.indexOf("___"));
	pageClass.modifyData.appid=modifyAppGroupId.substring(modifyAppGroupId.indexOf("___")+3);
	pageClass.modifyData.value=	tempModifyValue;
	//发起修改请求
	pageClass.ajaxEditCommit();
};

//应用实例组件 appurls 选中判定
function findCheckUserAppInstance(){
	
	var div = new StringBuffer();
	
	var appUrl = $("#appurlInput").val();
	var AppInstanceCont = $("#AppInstanceContMainDiv").find("div");
	//应用组 组件url
	if ((checkUserSel(AppInstanceCont))) {
		
		var AppInstancedivs = $("#AppInstanceContMainDiv").find(".titleDiv2");
		$.each(AppInstancedivs,function(index,server){ 
			
			//选中组件
			if(server.className.indexOf("userSelect") >=0){
			
				var serverValue = server.innerText;
				pageClass.modifyAppInstance.push(serverValue);
				
			}
		});	
	}
};

//服务端组件 选中判定
function findCheckUserAppServices(){
	var div = new StringBuffer();
	
	var appUrl = $("#appurlInput").val();
	var serviceCont = $("#serviceContMainDiv").find("div");
	//应用组 组件url
	if ((checkUserSel(serviceCont))) {
		
		var servicedivs = $("#serviceContMainDiv").find(".titleDiv2");
		$.each(servicedivs,function(index,server){ 
			
			//选中组件
			if(server.className.indexOf("userSelect") >=0){
				
				var serverValue = server.innerHTML.substring(server.innerHTML.indexOf("]")+1);
				var tempserverValue=server.innerHTML.substring(server.innerHTML.indexOf("]")+1);
				//格式化(避免jquery获取不到)
				serverValue = pageClass.encodeForJqueryFormat(serverValue);

				//选中组件下的DIV获取
				var urldivs = $("#"+serverValue).find(".titleDiv3");
				var urlId = 0;
				var relativeServiceUrls=new Array();
				$.each(urldivs,function(index2,url){ 
					//选中组件URL
					if(url.className.indexOf("userSelect") >=0){

						relativeServiceUrls.push(url.innerHTML);	
					}
				});
				
				pageClass.modifyAppService[tempserverValue]=relativeServiceUrls;
			}
		});
		
	}
};

//客户端组件 选中判定
function findCheckUserAppClients(){
	var div = new StringBuffer();
	
	var appUrl = $("#appurlInput").val();
	var clientCont = $("#clientContMainDiv").find("div");
	//应用组 组件url
	if ((checkUserSel(clientCont))) {
		
		var clientdivs = $("#clientContMainDiv").find(".titleDiv2");
		$.each(clientdivs,function(index,server){ 
			
			//选中组件
			if(server.className.indexOf("userSelect") >=0){
				
				var serverValue = server.innerHTML.substring(server.innerHTML.indexOf("]")+1);

				//格式化(避免jquery获取不到)
				serverValue = pageClass.encodeForJqueryFormat(serverValue);
				var tempserverValue=server.innerHTML.substring(server.innerHTML.indexOf("]")+1);
				//选中组件下的DIV获取
				var urldivs = $("#"+serverValue).find(".titleDiv3");
				var urlId = 0;
				var ClientUrls=new Array();
				$.each(urldivs,function(index2,url){ 
					//选中组件URL
					if(url.className.indexOf("userSelect") >=0){

						ClientUrls.push(url.innerHTML);
						
					}
				});
				
				pageClass.modifyAppClient[tempserverValue]=ClientUrls;
			}
		});
		
	}
};

/***
 * --------------------------------------------------
 * 删除dashboard       
 *--------------------------------------------------
 */
function showDelDiv(id){
	
	pageClass.removeParam.appgroup=id.substring(0,id.indexOf("___"));
	pageClass.removeParam.appid=id.substring(id.indexOf("___")+3);
	modalConfig.content="是否<span class='confirmMsg'>删除[</span>"+id+"<span class='confirmMsg'>]</span>?";
	showConfirm(modalConfig);
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
 * 应用实例  列表选中
 */
function setUrlCssChange_instance(obj,divId,showUpId){

	//应用实例组件选中
	if(obj.className=="titleDiv2"){
		obj.className = "titleDiv2 userSelect";
	}else{
		obj.className = "titleDiv2";
	}

	document.getElementById("winRow").scrollTop += addScrollTop; //滚动设置
};


/**
 * 服务端组件 客户端组件 应用组——应用集群  用户选中列表中的URL CSS切换（含URL的标题CSS切换）
 * setUrlCssChange
 */
function setUrlCssChange(obj,divId,showUpId){
	if(obj.className.indexOf("beforeSelect")>0){
		showConfig.content="该应用集群已存在！";
		showDialog(showConfig);
		return;
	}
	//组件url CSS切换
	if(obj.className=="titleDiv3"){
		obj.className = "titleDiv3 userSelect userSelectUrl";
	}else{
		obj.className = "titleDiv3";
		checkSpanFlag=0;
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


//对应用组__应用集群 字典序排序
function AppGroup_idSort(groupJson,key){
    
    for(var j=1,jl=groupJson.length;j < jl;j++){
        var temp = groupJson[j],
            val  = temp[key],
            i    = j-1;
        while(i >=0 && groupJson[i][key]>val){
            groupJson[i+1] = groupJson[i];
            i = i-1;    
        }
        groupJson[i+1] = temp;
        
    }
    return groupJson;
};


function setHtmlText(id, msg) {
	$("#" + id).text(msg);
}

//排序
function appGroupSort(str){
	var temp={};
	Object.keys(str).sort().forEach(function(k){temp[k]=str[k]});
	return temp;
};

//div事件和span内事件展开区分   即全选和展开
function checkSlectedDiv(){
	checkDivFlag=1;
};

function checkAllSelectOnclick(obj,id){
	
	var tempid=id;
	//全选
	if(checkDivFlag==1){
		id=pageClass.encodeForJqueryFormat(id);
		if(checkSpanFlag==0){
			if($("#"+id).hasClass("beforeTitleSelect")){
				$("#"+id).attr("class","titleDiv2  beforeTitleSelect userSelect");	
			}
			else{
				$("#"+id).attr("class","titleDiv2 userSelect");
			}
			
			var appgName=tempid.substring(0,tempid.length-2);
			var appidName=pageClass.allGroupsIdMap[appgName];
			for(var i in appidName){
				if(!appidName.hasOwnProperty(i)){
					continue;
				}
				var appidTempId=appgName+"ex"+appidName[i];
				appidTempId=pageClass.encodeForJqueryFormat(appidTempId);
			
				if(!($("#"+appidTempId).hasClass("beforeSelect"))){	
					$("#"+appidTempId).attr("class","titleDiv3 userSelect userSelectUrl");
				}
			}
		
			checkSpanFlag=1;
		}
		else{
		
			if($("#"+id).hasClass("beforeTitleSelect")){
				$("#"+id).attr("class","titleDiv2  beforeTitleSelect");
			}
			else{
				$("#"+id).attr("class","titleDiv2 ");
			}
		
			var appgName=tempid.substring(0,tempid.length-2);
			var appidName=pageClass.allGroupsIdMap[appgName];
			for(var i in appidName){
			
				if(!appidName.hasOwnProperty(i)){
					continue;
				}
				var appidTempId=appgName+"ex"+appidName[i];
				appidTempId=pageClass.encodeForJqueryFormat(appidTempId);
			
				if(!($("#"+appidTempId).hasClass("beforeSelect"))){
					$("#"+appidTempId).attr("class","titleDiv3");	
				}
			
			}
		
			checkSpanFlag=0;
		}
	
	}
	else{
		//展开
		setCssChangeById(tempid.substring(0,tempid.length-2));
	}
	
	checkDivFlag=0;
};


/***
 * --------------------------------------------------
 * 加载dashboard数据       
 *--------------------------------------------------
 */
function loadListData(reset,show,result){
	if(reset){
		$("#inputValue").val("");
		tableObj.setPageNum(1);  
	}
	
	pageClass.ajaxGdashboard(run);

	function run(){
		var filterResult = [],showIndex=0;
		
		//去前后空格
		var input = $("#inputValue").val().trim();
		$("#inputValue").val(input);
		var filter = input.length>0?true:false;
	
		
		var tempEmailList=pageClass.emailList;
		$.each(tempEmailList,function(key,str){
			var obj = JSON.parse(str);
			if(
					(!filter || (filter && key.toUpperCase().indexOf(input.toUpperCase())>=0))
					){
				
				filterResult[showIndex++]={
						"emailListName":key,
						"createtime":obj.createtime,
						"operationtime":obj.updatetime,
						"owner":obj.owner,
				}
			}
		});
		filterResult=AppGroup_idSort(filterResult,"emailListName");
		pageClass.pageEmailList = getPageData(filterResult);
		tableObj.clearTable(); 
		tableObj.setTotalRow(filterResult.length);
		tableObj.addRows(pageClass.pageEmailList);
		//将创建监控面板完返回时 保证页面数据加载完（因为异步调用）
		if(show=="show"){
			var failDataArray=eval("("+result.data+")");
			if(failDataArray.length){
				showConfig.content=result.msg+":";
				for(var index in failDataArray){
					if(!failDataArray.hasOwnProperty(index)){
						continue;
					}
					showConfig.content+=" ";
					showConfig.content+=failDataArray[index];
				}
				showDialog(showConfig);
			}
			showListDiv();
		}
		if(show=="modifyFail"){
			showConfig.content="该dashboard不存在，已删除，请重新创建！";
			showDialog(showConfig);
			showRowModifysuccess(true);
		}
		if(show=="modifyOk"){
			showConfig.content="修改成功！";
			showDialog(showConfig);
			showRowModifysuccess(true);
		}
		
			
	}
	
	/**
	 * 当前页不足，自动退一页
	 */
	function getPageData(array){
		var getPagingInfo = tableObj.getPagingInfo();
		var pageNum = getPagingInfo.pageNum;   
		var pageSize = getPagingInfo.pageSize
		var begin = (pageNum-1)*pageSize;
		var end = begin+pageSize;
		var result = array.slice(begin,end);
		
		if(pageNum>1 && result.length==0){
			tableObj.setPageNum(--pageNum);  
			return getPageData(array);
		}else{
			return result;
		}
	}
	
};

/***
 * --------------------------------------------------
 * TODO 页面与后台交互类      
 *--------------------------------------------------
 */
var pageClass = {
	allAppGroupIdUrlMap:new Map(),//存所有的应用组 应用集群 appurl
	allGroupsIdMap : {}, //所有的APPGroup-appid数据  	
	allGroupsIdMapVal : new Map(),//所有的APPGroup-appid数据  此时value为Set
	emailList:null,         //所有列表数据
	pageEmailList : [],     //当前页面的列表数据,页面手动分页
	
	removeParam:{
		"appgroup" : "",
		"appid" : "",
	},
	AllCreateDashboard:[],//存多个应用组-应用集群
	oneCreateDashboard:{
		"appgroup":"",
		"appid":"",
		"appurls":[]
	},//单个应用组-应用集群 创建样式
	modifyAppInstance:[], //存应用实例  appurls
	modifyAppService:{
		//存服务端组件名    “服务组件名”:[“url相对路径”]
	},
	modifyAppClient:{
		//存客户端组件名    “客户端组件名”:[“客户端url路径”]
	},
	modifyData:{
		"appgroup":"",
		"appid":"",
		"value":""
	},
	//getdashboards
	ajaxGdashboard : function(func) {
		var input = $("#inputValue").val();
		AjaxHelper.call({
			url : "../../rs/grafana/dashboard/getdashboards",   
			async : true,
			cache : false,
			type : "GET",
			dataType : "json",
			success : function(result) {
				pageClass.emailList = eval("("+result.data+ ")");
				if(func){
					func();
				}
			},
			error : function(result) {
				console.log(result);
			}
		});
	},
	ajaxGProfile : function(selectId,type) {
		AjaxHelper.call({
			url : "../../rs/godeye/filter/profile/q/cache",
			async : true,
			cache : false,
			type : "GET",
			dataType : "html",
			success : function(result) {
				if (result) {
					result = eval("(" + result + ")");
					result = eval("(" + result["rs"] + ")");
					profileData=result;//全局profile数据
					loadListData(false);
					pageClass.setGroups(result);
					
				} else {
					console.log("result is empty");
				}
			},
			error : function(result) {
				console.log(result);
			}
		});
	},
	//dashboardCreate
	ajaxCreateDashboard : function() {
			AjaxHelper.call({
				url : "../../rs/grafana/dashboard/create",
				data : JSON.stringify(pageClass.AllCreateDashboard),
				async : true,
				cache : false,
				type : "POST",
				dataType : "json",
				success : function(result) {
					if (result.code == "00") {
						$("#addGroupFilterDiv").modal('hide');
						loadListData(false,"show",result);
						
					}
					else {
						setHtmlText("addErrorMsg", result.msg);
					}
				},
				error : function(result) {
					console.log(result);
				}
			});
	},

	ajaxEditCommit : function() {
		
			AjaxHelper.call({
				url : "../../rs/grafana/dashboard/modify",
				data : JSON.stringify(pageClass.modifyData),
				async : true,
				cache : false,
				type : "POST",
				dataType : "json",
				success : function(result) {
					if (result.code == "00") {
						$("#editGroupFilterDiv").modal('hide');
						loadListData(false,"modifyOk");
						
					}
					else if(result.code == "01") {
						loadListData(false,"modifyFail");	
					}
					else {
						setHtmlText("addErrorMsg", result.msg);
					}
				},
				error : function(result) {
					console.log(result);
				}
			});
		
	},
	
	ajaxRemoveCommit : function() {
		AjaxHelper.call({
			url : "../../rs/grafana/dashboard/delete",  //  /grafana/dashboard/delete
			data : JSON.stringify(pageClass.removeParam),
			async : true,
			cache : false,
			type : "POST",
			dataType : "json",
			success : function(result) {
				loadListData(false);
			},
			error : function(result) {
				console.log(result);
			}
		});
	},
	
	setGroups : function(profileObj) {
		
		pageClass.allGroupsIdMapVal.clear();
		pageClass.allAppGroupIdUrlMap.clear();//清空
		for ( var key in profileObj) {
			var index = key.indexOf("@");
			if (index > 0) {
				var groupName = key.substring(0, index);
				//解析 获取应用组-应用集群 （appGroup-appid）
				var tempVarProfile=eval("(" + profileObj[key] + ")");	
				var appidName;
				
				for(var subvalue in tempVarProfile){	

					if(subvalue=="appid"){
						appidName=tempVarProfile[subvalue];
					}
					//获得应用组 应用集群 appurls。 过滤jse应用集群
					if(subvalue=="appurl"&&tempVarProfile[subvalue].substring(0,4)=="http"){
						var appurl=tempVarProfile[subvalue];
						if(!pageClass.allAppGroupIdUrlMap.contain(groupName+"___"+appidName)){
						
							var temparray=new Array();
							temparray.push(appurl);
							pageClass.allAppGroupIdUrlMap.put(groupName+"___"+appidName,temparray);
						}
						else{
						
							var temparray=pageClass.allAppGroupIdUrlMap.get(groupName+"___"+appidName);
							temparray.push(appurl);
							pageClass.allAppGroupIdUrlMap.put(groupName+"___"+appidName,temparray);
						}
						//获得应用组 应用集群
						if (!pageClass.allGroupsIdMapVal.contain(groupName)){  //键不存在
						
							var groupSet=new Set();
							groupSet.add(appidName);
							pageClass.allGroupsIdMapVal.put(groupName,groupSet);
						}
						else{
						
							var groupSeted=pageClass.allGroupsIdMapVal.get(groupName);
							groupSeted.add(appidName);
							pageClass.allGroupsIdMapVal.put(groupName,groupSeted);
						}
				}
					
			}
		}	
	}
		//将value由set转成Array 才能转成json
		var mapTojson=JSON.stringify(pageClass.allGroupsIdMapVal.mapNames.toArray());
		mapTojson=eval("(" + mapTojson + ")");	
		for(var i in mapTojson){
			
			if(!mapTojson.hasOwnProperty(i)) {
				continue;
			}
			 var temp=mapTojson[i];
			 var tempset=pageClass.allGroupsIdMapVal.get(temp);
			 var setToarray=tempset.toArray();
			 pageClass.allGroupsIdMap[temp]=setToarray;	
		}	
		
	},
		
    encodeForJqueryFormat:function(value){
		//格式化(避免jquery获取不到)
		value = value.replace(/\./g,"\\.");
		value = value.replace(/\//g,"\\/");
		value = value.replace(/\:/g,"\\:");
		value = value.replace(/\,/g,"\\,");
		value = value.replace(/\?/g,"\\?");
		value = value.replace(/\=/g,"\\=");
		value = value.replace(/\+/g,"\\+");
		value = value.replace(/\(/g,"\\(");
		value = value.replace(/\)/g,"\\)");
		value = value.replace(/\_/g,"\\_");
		value = value.replace(/\-/g,"\\-");
		value = value.replace(/\~/g,"\\~");
		value = value.replace(/\——/g,"\\——");
		value = value.replace(/\&amp;/g,"\\&");
		return value;
    }
};


var tableObj = new AppHubTable(mtableConfig);
var jsonObj = new AppHubJSONVisualizer(jsonConfig);
//TODO js入口
$(document).ready(function() {
	
	initListDiv();
	tableObj.sendRequest = pageClass.ajaxGProfile;
	tableObj.cellClickUser = showModifyAppInstanceServicesClientsDiv;	
	tableObj.delRowUser = showDelDiv;
	tableObj.initTable();
});
