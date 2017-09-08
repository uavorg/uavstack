/**
 * 窗体初始化
 */
window.winmgr.build({
	id : "notifyList",
	height : "auto",
	"overflow-y" : "auto",
	order : 999,
	theme : "ListBG"
});
window.winmgr.build({
	id : "objectDiv",
	height : "auto",
	"overflow-y" : "auto",
	order : 999,
	theme : "ObjectBG"
});
window.winmgr.build({
	id : "stgyDiv",
	height : "auto",
	"overflow-y" : "auto",
	order : 999,
	theme : "StgyDiv"
});
window.winmgr.show("notifyList");

/**
 * 操作配置
 */
var actionConf = {
		actionObj:null //操作对象
}
/**
 * 下拉框UI
 * */
var selUiConf = {
		keys:[

		      {"key":"server","value":"自定义指标"},
		      {"key":"server","value":"服务端"},
		      {"key":"client","value":"客户端"},
		      {"key":"log","value":"日志"}
		      ],
		"server":[
			          ["procState","进程状态指标系"],
			          ["hostState","应用容器状态指标系"],
			          ["urlResp","服务状态指标系"],
			          ["appResp","应用状态指标系"],
			          ["serverResp","应用服务器状态指标系"]
				     ],
		"client":[
			          ["clientResp","调用状态指标系"]
			          ],
		"log":[
			       ["log","日志"]
			    ]
		,
		"userDef":[
		       ["jvm","Java虚拟机状态指标系"]
		    ]
		,
		userInput:{
			"notifyNameF":"",
			"notifyNameM":"",
			"notifyNameI":""
		}
		
}
/**
 * 初始化头部
 */
function initHeadDiv() {
	var divContent = "<!-- 头部 BEGIN -->"
			+ "<div class=\"AppHubMVCSearchBar\" >"

			+ "<button id=\"'+id+'AppManagerAdd\" class=\"btn btn-default\" type=\"button\" onclick=\"javascript:showAddDiv();\">"
			+ "<span class=\"glyphicon glyphicon-plus\"></span>"
			+ "</button>"

			+ "<input id=\"searchInput_Hidden\" type=\"hidden\" ></input>"

			+ "<input id=\"searchInput\" class=\"form-control AppHubMVCSearchBarInputText\""
			+ "type=\"text\" placeholder=\"以*结尾模糊检索\" value=\"\"></input>"

			+ "<div class=\"btn-group\">"
			+ "<button id=\"searchbtn\" type=\"button\" class=\"btn btn-default\" onclick=\"javascript:searchbtn()\">"
			+ "<span class=\"glyphicon glyphicon-search\"></span>"
			+ "</button>"

			+ "<button id=\"searchAllbtn\" type=\"button\" class=\"btn btn-default\"  onclick=\"javascript:searchbtnAll();\">"
			+ "<span class=\"glyphicon glyphicon-th\"></span>" + "</button>"
			+ "</div>" + "</div>" + "<!-- 头部 END -->";
	HtmlHelper.id("notifyList").innerHTML += divContent;
}

/**
 * 显示添加策略
 */
function showAddDiv() {

	var sb=new StringBuffer();
	sb.append( "<div class=\"titleDiv\">");
	sb.append( "<input type=\"hidden\" id=\"isOwner\" value=\"true\">");
	sb.append( "<input type=\"hidden\" id=\"owner\" value=\""+window.parent.loginUser.userId+"\">");
	sb.append( "添加策略");
	sb.append( "<div class=\"icon-signout icon-myout\" onclick=\"javascript:closeObjectDiv()\"></div>");
	sb.append( "<div class=\"icon-question-sign icon-myhelp\" onclick=\"javascript:openHelpDiv()\"></div>");
	sb.append( "</div>");
	sb.append( "</br>");
	
	sb.append( '<div class="itemContainer" style="width:100%;font-size: 14px">');


	sb.append( '<div><input class="displayMsgTitle" value="策略：" readonly="readonly"></input></div>');
	sb.append('<div">');
	sb.append('<div class="btn-group selectDiv">');
	sb.append('<button type="button" class="btn btn-default selBut selBut_must" style="color:darkgrey;" id="add_notifyNameF">选择监控组</button>');
	sb.append('<button type="button" class="btn btn-danger dropdown-toggle selBut_but" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">');
	sb.append('<span class="caret"></span>');
	sb.append('<span class="sr-only">Toggle Dropdown</span>');
	sb.append('</button>');
	sb.append('<ul class="dropdown-menu selBut_must">');
	$.each(selUiConf.keys,function(key,obj){
		sb.append('<li><a href="#" onclick="javascript:selServerChangeShow(\'add\', \''+obj.key+'\',\''+obj.value+'\')">'+obj.value+'</a></li>');
	});
	sb.append('</ul>');
	sb.append('</div>');
	sb.append('</div>');
	
	sb.append('<div>');
	sb.append('<div class="btn-group selectDiv defNone" id="add_notifyNameM_div">');
	sb.append('<button type="button" class="btn btn-default selBut" id="add_notifyNameM">选择监控组指标</button>');
	sb.append('<button type="button" class="btn btn-default dropdown-toggle selBut_but" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">');
	sb.append('<span class="caret"></span>');
	sb.append('<span class="sr-only">Toggle Dropdown</span>');
	sb.append('</button>');
	sb.append('<ul class="dropdown-menu selBut" id="add_notifyNameM_body" >');
	sb.append('</ul>');
	sb.append('</div>');
	sb.append( '<div class="defNone" id="add_appName_div"><input class="input_must" placeholder="输入应用ID" id="add_appname" onchange="javascript:appNameChange(this);"></input></div>');
	sb.append('</div>');
													
	sb.append( '<div><input placeholder="输入监控实例名或者实例组名" id="notifyNameI" onkeyup="javascript:checkNameIShow();"></input></div>');
	sb.append( "<div><textarea placeholder='如上面为组名，该框请输入实例名(可输入多个，以\",\"号分开)' id=\"notifyInstances\" style=\"display:none;\"></textarea></div>");
	

	sb.append( '<div><input class="displayMsgTitle" value="描述：" readonly="readonly"></input></div>');
	sb.append( '<div><textarea class="input_must" placeholder="输入描述" id="notifyDesc"></textarea></div>');

	sb.append( '<div class="well" id="conFatDiv">');
	sb.append( '<div><span class="well-title">条件定义</span><div class="well-title-div"><span class="glyphicon glyphicon-plus well-add" onclick="javascript:showCon(this,\'ADD\');"></span></div></div>');
	sb.append( '</div>');
	
	sb.append( '<div class="well well-only-div" id="stgyFatDiv">');
	sb.append( '<div><span class="well-title">触发策略</span><div class="well-title-div"><span class="glyphicon glyphicon-plus well-add" onclick="javascript:StgyClass.showStgyDiv(this,\'add\');"></span></div></div>');
	sb.append( '</div>');
	
	sb.append( '<div class="well" id="actionFatDiv">');
	sb.append( '<div><span class="well-title">触发动作</span><div class="well-title-div"><span class="glyphicon glyphicon-plus well-add" id="actionAddButton" onclick="javascript:showAction(this,\'ADD\');"></span></div></div>');
	sb.append( '</div>');

	sb.append( '<div><button style="width:50%;margin-top:5px;min-width:340px;" class="btn btn-primary " onclick="javascript:saveNotify(true);">保存</button></div>');
	sb.append( '<div><span id="addNotifyErrMSG" style="color:#ff0000;"></span></div>');
	
	sb.append( '</div>');

	HtmlHelper.id("objectDiv").innerHTML = sb.toString();
	window.winmgr.hide("notifyList");
	window.winmgr.show("objectDiv");
	initActionDiv();

}


/**
 * 显示编辑策略
 */
function showEditNotifyDiv(jsonObjParam) {

	var selTypeSize = $("#actionTypeSel option").size();
	var key,jsonObj,isOwner=false; 
	//因为只有一对 key：value 获取key（值为id） 
	$.each(jsonObjParam,function(index,obj){
		key = index;
		jsonObj = obj;
	});
	
	if(jsonObj.owner == window.parent.loginUser.userId 
			|| window.parent.loginUser.groupId == "uav_admin"
			|| window.parent.loginUser.groupId == "vipgroup"
	){
		isOwner = true;
	}
	
	var names = key.split("@");
	var cssType = "displayMsgInput",cssRedOnly = "displayMsgInput listIndex";
	var sb=new StringBuffer();
	sb.append( "<div class=\"titleDiv\">");
	sb.append( "<input type=\"hidden\" id=\"isOwner\" value=\""+isOwner+"\">");
	sb.append( "编辑策略");
	sb.append( "<div class=\"icon-signout icon-myout\" onclick=\"javascript:closeObjectDiv()\"></div>");
	sb.append( "<div class=\"icon-question-sign icon-myhelp\" onclick=\"javascript:openHelpDiv()\"></div>");
	sb.append( "</div>");
	sb.append( "</br>");
	/**
	 * 所有渲染内容 div begin
	 */
	sb.append( '<div class="itemContainer" style="width:100%;font-size: 14px">');

	sb.append( '<div><input class="displayMsgTitle" value="归属用户：" readonly="readonly"></input></div>');
	if(window.parent.loginUser.groupId == "uav_admin"
				|| window.parent.loginUser.groupId == "vipgroup"
	){
		sb.append( '<div><input class="input_update" placeholder="输入归属用户" value="'+jsonObj.owner+'"id="owner"></input></div>');
	}else{

		sb.append( '<div><input class="'+cssRedOnly+'" placeholder="输入归属用户" value="'+jsonObj.owner+'"id="owner" readonly="readonly"></input></div>');
	}
	

	sb.append( '<div><input class="displayMsgTitle" value="策略：" readonly="readonly"></input></div>');

	//1
	var existsIns = jsonObj.instances.length>0?true:false;
	var showNameF = getSelUiConfKeysValue(names[0],names[1]);
	if(showNameF=="自定义指标"){
		cssType = "displayMsgInput listIndex_appmetrics";
	}else if(names[1] == "log"){
		showNameF = "日志";
		cssType = "displayMsgInput listIndex_log";
	}else if(names[0] == "server"){
		cssType = "displayMsgInput listIndex_server";
	}else if(names[0] == "client"){
		cssType = "displayMsgInput listIndex_client";
	}
	sb.append( '<div><input class="'+cssType+'" value='+showNameF+' readonly="readonly"></input></div>');
	selUiConf["userInput"]["notifyNameF"]=names[0];//编辑赋值，准备修改数据
	//2
	if(showNameF=="自定义指标"){
		selUiConf["userInput"]["notifyNameM"]=names[1];//编辑赋值，准备修改数据
	}else if(names[1] == "log"){
		sb.append( '<div><input class="'+cssRedOnly+'" value="应用ID:'+names[0]+'" readonly="readonly"></input></div>');
		selUiConf["userInput"]["notifyNameM"]=names[1];//编辑赋值，准备修改数据
	}else if(names[1]){
		sb.append( '<div><input class="'+cssRedOnly+'" value="'+getSelUiConfigValue(names[1])+'" readonly="readonly"></input></div>');
		selUiConf["userInput"]["notifyNameM"]=names[1];//编辑赋值，准备修改数据

	}
	//3
	if(names[2] && names[1] == "log"){
		var nNameIShow = "指定日志:"+names[2];
		sb.append( '<div><input class="'+cssRedOnly+'" value='+nNameIShow+' readonly="readonly"></input></div>');
		selUiConf["userInput"]["notifyNameI"]=names[2];//编辑赋值，准备修改数据
	}else if(names[1] == "log"){
		sb.append( '<div><input class="'+cssRedOnly+'" value="全部日志" readonly="readonly"></input></div>');
	}else if(names[2]){
		var nNameIShow = (existsIns?"实例组:":"实例:")+names[2];
		sb.append( '<div><input class="'+cssRedOnly+'" value='+nNameIShow+' readonly="readonly"></input></div>');
		selUiConf["userInput"]["notifyNameI"]=names[2];//编辑赋值，准备修改数据
	}else{
		sb.append( '<div><input class="'+cssRedOnly+'" value="全部实例" readonly="readonly"></input></div>');
	}

	if(names[2] && isOwner){
		sb.append( "<div><textarea class=\"input_update\" placeholder='如上面为组名，该框请输入实例名(可输入多个，以\",\"号分开)' id=\"notifyInstances\">"+jsonObj.instances+'</textarea></div>');
	}else if(names[2] && !isOwner){
		sb.append( "<div><textarea class=\""+cssRedOnly+"\" placeholder='如上面为组名，该框请输入实例名(可输入多个，以\",\"号分开)' id=\"notifyInstances\"  readonly=\"readonly\">"+jsonObj.instances+'</textarea></div>');
	}else if(isOwner){
		sb.append( "<div><textarea class=\"input_update\" placeholder='如上面为组名，该框请输入实例名(可输入多个，以\",\"号分开)' id=\"notifyInstances\">"+jsonObj.instances+'</textarea></div>');
	}else{
		sb.append( "<div><textarea class=\""+cssRedOnly+"\" placeholder='如上面为组名，该框请输入实例名(可输入多个，以\",\"号分开)' id=\"notifyInstances\"  readonly=\"readonly\">"+jsonObj.instances+'</textarea></div>');
	}
	

	sb.append( '<div><input class="displayMsgTitle" value="描述：" readonly="readonly"></input></div>');
	if(isOwner){
		sb.append( '<div><textarea class="input_must" placeholder="输入描述" id="notifyDesc">'+jsonObj.desc+'</textarea></div>');
	}else{
		sb.append( '<div><textarea class="'+cssRedOnly+'" placeholder="输入描述" id="notifyDesc" readonly="readonly">'+jsonObj.desc+'</textarea></div>');
	}
	 
	/**
	 * 初始化条件 begin
	 */
	sb.append( '<div class="well" id="conFatDiv">');
	sb.append( '<div>');
	sb.append( '<span class="well-title">条件定义</span>');
	sb.append( '<div class="well-title-div"><span class="glyphicon glyphicon-plus well-add"  id="whereAddButton"  onclick="javascript:showCon(this,\'ADD\');"></span></div>');
	$.each(jsonObj.conditions,function(index,obj){
		if(obj.func && obj.func.indexOf("count>")>-1){
			obj.cparam = obj.func.substr(6);
			obj.func = "count";
		}
		 
		if(!obj.id){
			/**
			 * 兼容老数据
			 */
			obj.id=StgyClass.randomId()+"_stgySpan";
		}
		var html;
		if(isOwner){
			html = '<div class="well-list">'+StgyClass.formatShowWhere(obj)+'<span id="'+obj.id+'" style="display:none">'+JSON.stringify(obj)+'</span><span class="glyphicon glyphicon-remove well-del" onclick="javascript:delThisObj(this);"></span><span class="glyphicon glyphicon-edit well-edit" onclick="javascript:showCon(this,\'EDIT\');"></span></div>';
		}else{
			html = '<div class="well-list">'+StgyClass.formatShowWhere(obj)+'<span id="'+obj.id+'" style="display:none">'+JSON.stringify(obj)+'</span><span class="glyphicon glyphicon-eye-open well-edit" onclick="javascript:showCon(this,\'EDIT\');"></span></div>';
		}
		sb.append( html);
	});
	sb.append( '</div>');
	sb.append( '</div>');
	/**
	 * 初始化条件 end
	 */
	
	/**
	 * 初始化触发策略 begin
	 */
	sb.append( '<div class="well well-only-div" id="stgyFatDiv">');

	if(isOwner){
		sb.append( '<div><span class="well-title">触发策略</span><div class="well-title-div"><span class="glyphicon glyphicon-plus well-add" onclick="javascript:StgyClass.showStgyDiv(this,\'add\');"></span></div></div>');
	}else{
		sb.append( '<div><span class="well-title">触发策略</span><div class="well-title-div"><span class="well-add" ></span></div></div>');
	}
		
	if(jsonObj.relationsHtmls){
		$.each(jsonObj.relationsHtmls,function(index,html){
			var appendHtml =  
			'<div>'+
				'<div class="well-list well-list-display">'+
					'<div name="stgy_exp_html" style="width:80%;">'+html+'</div>'+
					'<div style="width: 20%;">';

			if(isOwner){
				appendHtml+='<span class="glyphicon glyphicon-remove well-del" onclick="javascript:StgyClass.deleteStgyToAppend(this);"></span><span class="glyphicon glyphicon-edit well-edit" onclick="javascript:StgyClass.showStgyDiv(this,\'edit\');"></span>';
			}else{
				appendHtml+='<span class="glyphicon glyphicon-eye-open well-edit" onclick="javascript:StgyClass.showStgyDiv(this,\'edit\');"></span>';
			}
					appendHtml+='</div>'+
				'</div>'+
			'</div>';
			sb.append(appendHtml);
		});
	}
	sb.append( '</div>');
	/**
	 * 初始化触发策略 begin
	 */
	
	sb.append( '<div class="well" id="actionFatDiv">');
	sb.append( '<div><span class="well-title">触发动作</span><div class="well-title-div"><span class="glyphicon glyphicon-plus well-add" id="actionAddButton" onclick="javascript:showAction(this,\'ADD\');"></span></div></div>');	
	var actionSum = 0;
	if (jsonObj.action!=undefined) {
		$.each(jsonObj.action,function(index,value){
			actionSum++;
			var josnSpan ={
					"type":index,
					"value":value
			}
			
			var html ;

			if(isOwner){
				html = '<div class="well-list">'+josnSpan.type+'<span style="display:none">'+JSON.stringify(josnSpan)+'</span><span class="glyphicon glyphicon-remove well-del" onclick="javascript:delThisActionObj(this);"></span><span class="glyphicon glyphicon-edit well-edit" onclick="javascript:showAction(this,\'EDIT\');"></span></div>';
			}else{
				html = '<div class="well-list">'+josnSpan.type+'<span style="display:none">'+JSON.stringify(josnSpan)+'</span><span class="glyphicon glyphicon-eye-open well-edit" onclick="javascript:showAction(this,\'EDIT\');"></span></div>';
			}
			sb.append( '<div>');
			sb.append( html);
			sb.append( '</div>');
	
			/**
			 * 计算下拉选项
			 */
			$("#actionTypeSel option[value="+josnSpan.type+"]").remove(); 
	
		});
	}
	sb.append( '</div>');
	/**
	 * 初始化触发动作 end
	 */

	if(isOwner){
		//按钮
		sb.append( '<div><button style="width:50%;margin-top:5px;min-width:340px;" class="btn btn-primary " onclick="javascript:saveNotify(false);">修改</button></div>');
		sb.append( '<div><span id="addNotifyErrMSG" style="color:#ff0000;"></span></div>');
	}
	
	sb.append( '</div>');
	/**
	 * 所有渲染内容 div end
	 */

	HtmlHelper.id("objectDiv").innerHTML = sb.toString();
	
	/**
	 * 判断出发条件：不是归属者不能添加
	 */
	if(!isOwner){
		$("#whereAddButton").attr("class","well-add");
		$("#whereAddButton").click(function(){});
	}
	/**
	 * 判断触发动作按钮 begin(类型都已经存在值，则不能再添加)
	 * 不是归属者也不能添加
	 */
	if(actionSum==selTypeSize || !isOwner){
		$("#actionAddButton").attr("class","well-add");
		$("#actionAddButton").click(function(){});
	}
	/**
	 * 判断触发动作按钮 end
	 */
	window.winmgr.hide("notifyList");
	window.winmgr.show("objectDiv");

	initActionDiv();

}

/**
 * 条件窗口操作 begin
 */

function initConditionsDiv(thisObj) {

	var sb=new StringBuffer();
	
	sb.append('<div class="modal fade conditions" id="conditionsDiv" aria-hidden="false">');
	sb.append('<div class="modal-dialog">');
	sb.append( '<div class="modal-content">');
	sb.append( '<div class="modal-header">');
	sb.append( '<h5>条件定义</h5>');
	sb.append( '</div>');
	sb.append( '<div class="modal-body">');

	sb.append( '<input id="contype"type=\"hidden\" ></input><br/>');
	sb.append( '<input id="contExpr" class=\"form-control input_must\" type=\"text\" placeholder=\"触发表达式\"></input><br/>');
	sb.append( '<input id="conRange" class=\"form-control \" type=\"text\" placeholder=\"持续时间(秒)\" onkeyup="this.value=this.value.replace(\/\\D/g,\'\')" onafterpaste="this.value=this.value.replace(\/\\D/g,\'\')"></input><br/>');
	sb.append( "<select id=\"conFunc\" onchange=\"javascript:funcChangeShow(this,'conFuncParam');\">");
	sb.append( '<option value="0">--选择聚集操作--</option>');
	sb.append( '<option value="max">最大值</option>');
	sb.append( '<option value="min">最小值</option>');
	sb.append( '<option value="sum">求和</option>');
	sb.append( '<option value="avg">平均值</option>');
	sb.append( '<option value="diff">求差</option>');
	sb.append( '<option value="count">计数</option>');
	sb.append( '</select><br/>');
	sb.append( '<input id=\"conFuncParam\" class=\"form-control input_must\" type=\"text\" placeholder=\"聚集参数值(>)\" style="display:none" onkeyup="this.value=this.value.replace(\/\\D/g,\'\')" onafterpaste="this.value=this.value.replace(\/\\D/g,\'\')"></input><br/>');
			
	sb.append( '</div>');
	sb.append( '<div class="modal-footer">');
	sb.append( '<span style="margin-right:5px;color:#ff0000;display:none;" id="conditionsErrMsg">必输项不能为空</span>');
	sb.append( '<button class="btn btn-primary " id=\"whereSaveButton\" onclick="javascript:conditionsAppend();">保存</button>');
	sb.append( '<button class="btn" data-dismiss="modal">关闭</button>' + '</div>');
	sb.append( '</div>' + '</div>' + '</div>');
	var div = document.createElement('div');
	div.innerHTML = sb.toString();
	document.body.appendChild(div);
}

/**
 * 触发动作添加窗口
 */
function initActionDiv() {

	var old = document.getElementById("actionDiv");
	var isOwner = $("#isOwner").val();
	if(old){
		var node = old.parentNode;
		node.removeChild(old);
	}
	
	var sb=new StringBuffer();
	
	sb.append('<div class="modal fade actions" id="actionDiv" aria-hidden="false">');
	sb.append('<div class="modal-dialog">');
	sb.append( '<div class="modal-content">');
	sb.append( '<div class="modal-header" style="display: -webkit-box;">');
	sb.append( '<div style="width: 97%;"><h5>触发动作</h5></div>');

	sb.append( '</div>');
	sb.append( '<div class="modal-body" id="actionBodyDiv">');

	sb.append( '<input id="actiontype" type=\"hidden\" ></input><input id="actionEditType" type=\"hidden\" ></input>');
	sb.append( "<select id=\"actionTypeSel\">");
	sb.append( '<option value="sms">发送短信（填写短信号码）</option>');
	sb.append( '<option value="mail">发送邮件（填写邮箱地址）</option>');
	sb.append( '<option value="phone">电话通知（填写手机号）</option>');
	sb.append( '<option value="httpcall">Http动作（填写URL）</option>');
	sb.append( '</select>');

	if(isOwner=="true"){
		sb.append( '<textarea name="actionValue" style="resize: none;height:50px;" class=\"input_must\" placeholder=\"动作通知(,号分开)\" ></textarea>');
	}else{
		sb.append( '<textarea name="actionValue" style="resize: none;height:50px;" class=\"displayMsgInput listIndex\" readonly="readonly" placeholder=\"动作通知(,号分开)\" ></textarea>');
	}
			
	sb.append( '</div>');
	sb.append( '<div class="modal-footer">');
	sb.append( '<span style="margin-right:5px;color:#ff0000;display:none;" id="ActionErrMsg">必输项不能为空</span>');

	if(isOwner=="true"){
		sb.append( '<button class="btn btn-primary " id=\"actionSaveButton\" onclick="javascript:appendActionTextarea(this);">添加优先级</button>');
	}
	sb.append( '<button class="btn btn-primary " id=\"actionSaveButton\" onclick="javascript:actionAppend();">保存</button>');
	sb.append( '<button class="btn" data-dismiss="modal">关闭</button>' + '</div>');
	sb.append( '</div>' + '</div>' + '</div>');
	var div = document.createElement('div');
	div.innerHTML = sb.toString();
	document.body.appendChild(div);
}

/**
 * 显示添加条件窗口
 * @param thisObj
 * @param type
 */
function showCon(thisObj,type){
	actionConf.actionObj=thisObj.parentNode;
	$("#contype").val(type);
	//clear
	$("#contExpr").val("");
	$("#conRange").val("");
	$("#conFunc").val("0");
	$("#conFuncParam").val("");
	$("#conFuncParam").hide();
	$("#conditionsErrMsg").hide();
	//还原只读
	$("#whereSaveButton").show();
	$("#contExpr").removeAttr("readonly");
	$("#conRange").removeAttr("readonly");
	$("#conFunc").removeAttr("disabled");
	$("#conFuncParam").removeAttr("readonly");
	
	if("EDIT" == type){
		var jsonValue = JSON.parse(thisObj.parentNode.getElementsByTagName("span")[0].textContent);
		$("#contExpr").val(jsonValue.expr);
		$("#conRange").val(jsonValue.range);
		$("#conFunc").val((null == jsonValue.func?0:jsonValue.func));
		if("count" == jsonValue.func){
			$("#conFuncParam").val(jsonValue.cparam);
			$("#conFuncParam").show();
		}
		//不是归属用户，则只读
		var isOwner = $("#isOwner").val();
		if(isOwner!="true"){
			$("#whereSaveButton").hide();
			$("#contExpr").attr("readonly","readonly");
			$("#conRange").attr("readonly","readonly");
			$("#conFunc").attr("disabled","disabled");
			$("#conFuncParam").attr("readonly","readonly");

			//只读CSS
			$("#contExpr").attr("class","displayMsgInput listIndex");
			$("#conRange").attr("class","displayMsgInput listIndex");
			$("#conFuncParam").attr("class","displayMsgInput listIndex");
		}else{
			//还原默认CSS
			$("#contExpr").attr("class","form-control input_must");
			$("#conRange").attr("class","form-control");
			$("#conFuncParam").attr("class","form-control input_must");
			
		}
	}else{
		//默认CSS
		$("#contExpr").attr("class","form-control input_must");
		$("#conRange").attr("class","form-control");
		$("#conFuncParam").attr("class","form-control input_must");
	}
    $("#conditionsDiv").modal({backdrop: 'static', keyboard: false});
	$("#conditionsDiv").modal();
	

}

function funcChangeShow(thisObj,showId){
	if("count" == thisObj.value){
		$("#"+showId).show();
	}else{
		$("#"+showId).hide();
	}
}

function selServerChangeShow(type,value,text){
	var selId = type+"_notifyNameF";
	$("#"+selId).html(text);
	$("#"+selId).css("padding-left","2px");
	$("#"+type+"_notifyNameF").css("color","black");

	if(text == "自定义指标"){
		$("#"+type+"_appName_div").hide();
		$("#"+type+"_notifyNameM_div").attr("class","btn-group selectDiv defNone");
		selUiConf["userInput"]["notifyNameF"]=value;
		selUiConf["userInput"]["notifyNameM"]="jvm";
		$("#notifyNameI").attr("placeholder","输入监控实例名或者实例组名");
	}else if(value=="log"){
		$("#"+type+"_appName_div").show();
		$("#"+type+"_notifyNameM_div").attr("class","btn-group selectDiv defNone");
		$("#notifyNameI").attr("placeholder","输入指定日志");
		selUiConf["userInput"]["notifyNameF"]="";
		selUiConf["userInput"]["notifyNameM"]=value;
	}else{
		$("#"+type+"_appName_div").hide();
		$("#"+type+"_notifyNameM_div").attr("class","btn-group selectDiv");
		$("#notifyNameI").attr("placeholder","输入监控实例名或者实例组名");
		$("#"+type+"_notifyNameM").css("color","darkgrey");
		$("#"+type+"_notifyNameM").html("选择监控组指标");
		document.getElementById(type+"_notifyNameM_body").innerHTML ="";
		$.each(selUiConf[value],function(index,obj){
			var li = document.createElement("li");
			var a = document.createElement("a");
			a.innerHTML=obj[1];
			a.onclick=function(){selIndexChangeShow(type,obj[0],obj[1]);};
			a.href="#";
			li.appendChild(a);
			document.getElementById(type+"_notifyNameM_body").appendChild(li);
		});
		selUiConf["userInput"]["notifyNameF"]=value;
	}
	
}
function selIndexChangeShow(type,value,text){
	 $("#"+type+"_notifyNameM").html(text);
	 $("#"+type+"_notifyNameM").css("padding-left","2px");
	 $("#"+type+"_notifyNameM").css("color","black");
	 selUiConf["userInput"]["notifyNameM"]=value;
}
function appNameChange(obj){
	 selUiConf["userInput"]["notifyNameF"]=obj.value;
}
function getSelUiConfigValue(indexValue){
	var result ="";
	$.each(selUiConf.keys,function(value,obj1){
		$.each(selUiConf[obj1.key],function(index,obj2){
			if(obj2[0]==indexValue){
				result=obj2[1];
				return false;
			}
		});
		if(result!==""){
			return false;
		}
	});
	
	return result;
}
function getSelUiConfKeysValue(a, b) {
	var result = "";
	if (b == "log") {
		result="日志";
	} else if (a == "server" && b == "jvm") {
		result="自定义指标";
	} else if (a == "server") {
		result="服务端";
	} else if (a == "client") {
		result="客户端";
	}
	return result;
}
function conditionsAppend(){	
	if(checkFunc()){
		var jsonObject = {"expr":HtmlHelper.inputXSSFilter($("#contExpr").val()),"range":HtmlHelper.inputXSSFilter($("#conRange").val()),"func":HtmlHelper.inputXSSFilter($("#conFunc").val()),"cparam":HtmlHelper.inputXSSFilter($("#conFuncParam").val())};
		appendConditions(jsonObject);
		$("#conditionsDiv").modal('hide');
	}
}

function checkFunc(){

	var result = true;
	if(!$("#contExpr").val()){
		result = false;
	}else if("count" == $("#conFunc").val() && !$("#conFuncParam").val()){
		result = false;
	}
	
	if(result){
		$("#conditionsErrMsg").hide();
	}else{
		$("#conditionsErrMsg").show();
	}
	
	return result;
}
function appendConditions(jsonObj) {
	var type = $("#contype").val();
	if("ADD"==type){
		var newNode = document.createElement("div");
		var stgyDivId = StgyClass.randomId()+"_stgySpan";
		html = '<div class="well-list">'+getHtmlAndSetId(stgyDivId)+'</div>';
		newNode.innerHTML = html;
		actionConf.actionObj.parentNode.appendChild(newNode);
	}else if("EDIT"==type){
		var oldId = actionConf.actionObj.getElementsByTagName("span")[0].id;
		actionConf.actionObj.innerHTML= getHtmlAndSetId(oldId);
		StgyClass.updateWhereToStgyAppend(jsonObj);
	}
	
	function getHtmlAndSetId(stgyDivId){
		jsonObj.id = stgyDivId;//赋值id
		var html = StgyClass.formatShowWhere(jsonObj)+'<span id="'+jsonObj.id+'" style="display:none">'+JSON.stringify(jsonObj)+'</span><span class="glyphicon glyphicon-remove well-del" onclick="javascript:delThisObj(this);"></span><span class="glyphicon glyphicon-edit well-edit" onclick="javascript:showCon(this,\'EDIT\');"></span>';
		return html;
	}
}
/**
 * 条件窗口操作 end
 */


/**
 * 触发动作操作 begin
 */
function showAction(thisObj,type){
	actionConf.actionObj=thisObj.parentNode;
	$("#actiontype").val(type);
	$.each($("textarea[name='actionValue']"),function(index,obj){
		if(index>0){
			removeActonTextarea(obj);
		}else{
			obj.value="";
		}
	});
    $("#actionDiv").modal({backdrop: 'static', keyboard: false});
	$("#actionDiv").modal();
	//还原只读
	$("#actionSaveButton").show();
	$("#actionValue").removeAttr("readonly");

	
	if("EDIT"==type){
		//不是归属用户，则只读
		var isOwner = $("#isOwner").val();
		$("#actionTypeSel").hide();
		var spanJson = JSON.parse(thisObj.parentNode.getElementsByTagName('span')[0].textContent);

		$.each(spanJson.value,function(index,value){
			/**
			 * 第一次循环：追加渲染
			 */
			if(index>0){
				appendActionTextarea();
			}
		});
		var actionTextValues = $("textarea[name='actionValue']");
		$.each(spanJson.value,function(index,value){
			/**
			 * 第二次循环：赋值
			 */
			actionTextValues[index].value=value;
		});

		$("#actionEditType").val(spanJson.type);
		
		if(isOwner!="true"){
			$("#actionSaveButton").hide();
		}
		
	}else{
		$("#actionTypeSel").show();
	}
	
	
}

function actionAppend(){

	$("#ActionErrMsg").hide();
	
	if(checkAction()){
		appendActions();
		$("#actionDiv").modal('hide');
	}else{
		$("#ActionErrMsg").show();	
	}
}

function appendActionTextarea(thisObj){
	var isOwner = $("#isOwner").val();

	var html = '';
	if(isOwner == "true"){
		html = '<textarea name="actionValue" style="resize: none;height:50px;" class=\"input_update\" placeholder=\"动作通知(,号分开)\" ></textarea>';
		html+= '<div class=\"action-textarea-delete\" onclick=\"removeActonTextarea(this)\"><span class=\"glyphicon glyphicon-remove\"></span></div>';
	}else{
		html = '<textarea name="actionValue" style="resize: none;height:50px;" class=\"displayMsgInput listIndex\" readonly="readonly" placeholder=\"动作通知(,号分开)\" ></textarea>';
	}
	
	var newNode = document.createElement("div");
	newNode.innerHTML = html;
	document.getElementById("actionBodyDiv").appendChild(newNode);
}
function removeActonTextarea(thisObj){
	var node = thisObj.parentNode.parentNode;
	node.removeChild(thisObj.parentNode);
}

function checkAction(){

	var actionTextValues = $("textarea[name='actionValue']");
	
	var checkValue =  $.trim(actionTextValues[0].value); //只校验第一个必须输入
	var type = $("#actiontype").val();
	if("ADD"==type && !checkValue){
		return false;
	}else if(!checkValue){
		return false;
	}

	var result = false;
	var actionValues = new Array();
	$.each(actionTextValues,function(index,obj){
		var values = format(obj.value);
		obj.value = values;
		if(values!=""){
			actionValues.push(values);
			result = true;
		}
	});
	
	if(!result){
		$("#ActionErrMsg").show();	
	}

	return result;
	
	function format(_values){
		var result = new Array();
		var values = _values.split(",");
		$.each(values,function(index,value){
			value = $.trim(value);
			if(value!=""){
				result.push(value);
			}
		});
		
		if(!result || result.length==0){
			return "";
		}else{
			return result.join(",");
		}
	}
}

function appendActions() {
	var type = $("#actiontype").val();
	var html = getAppendHtml(type);
	
	
	if("ADD"==type){
		var newNode = document.createElement("div");
		newNode.innerHTML = html;
		actionConf.actionObj.parentNode.appendChild(newNode);
		
		/**
		 * 计算下拉菜单：删除当前选项
		 */
		$("#actionTypeSel option[value="+$("#actionTypeSel").val()+"]").remove(); 
		
		/**
		 * 计算是否还有添加类型：添加按钮控制
		 */
		var actionTypeSelect = document.getElementById("actionTypeSel");
		if(actionTypeSelect.length == 0){
			$("#actionAddButton").attr("class","well-add");
			$("#actionAddButton").click(function(){});
		}

		
	}else if("EDIT"==type){
		actionConf.actionObj.parentNode.innerHTML = html;
	}
	
	function getAppendHtml(){
		var result = new Array();
		var actionTextValues = $("textarea[name='actionValue']");
		$.each(actionTextValues,function(index,obj){
			result.push(obj.value);
		});
		
		var actionType ="";
		if(type=="ADD"){
			actionType = $("#actionTypeSel").val();
		}else if(type=="EDIT"){
			actionType = $("#actionEditType").val();
		}
		
		var jsonObj = {type:HtmlHelper.inputXSSFilter(actionType),value:HtmlHelper.inputXSSFilter(result)};
		var html = '<div class="well-list">'+jsonObj.type+'<span style="display:none">'+JSON.stringify(jsonObj)+'</span><span class="glyphicon glyphicon-remove well-del" onclick="javascript:delThisActionObj(this);"></span><span class="glyphicon glyphicon-edit well-edit" onclick="javascript:showAction(this,\'EDIT\');"></span></div>';
		return html;
	}
}
/**
 * 触发动作操作 end
 */


function delThisObj(thisObj) {
	var node = thisObj.parentNode.parentNode;
	node.removeChild(thisObj.parentNode);
	StgyClass.deleteWhereToStgyAppend(thisObj);
}

function delThisActionObj(thisObj) {
	/**
	 * 还原当前选项
	 */
	var spanJson = JSON.parse(thisObj.parentNode.getElementsByTagName('span')[0].innerHTML);
	$('#actionTypeSel').append("<option value='"+spanJson.type+"'>"+spanJson.type+"</option>"); 
	$("#actionAddButton").attr("class","glyphicon glyphicon-plus well-add");
	$("#actionAddButton").click(function(){showAction(this,'ADD')});
	/**
	 * 删除显示
	 */
	var node = thisObj.parentNode.parentNode;
	node.removeChild(thisObj.parentNode);
}

function checkNameIShow(){
	if($("#notifyNameI").val()){
		$("#notifyInstances").show();
	}else{
		$("#notifyInstances").val("");
		$("#notifyInstances").hide();
	}
	selUiConf["userInput"]["notifyNameI"] = $("#notifyNameI").val();
}

function closeObjectDiv() {
	window.winmgr.hide("objectDiv");
	window.winmgr.show("notifyList");
}

function openHelpDiv() {
 	window.open("file/help.htm","apphub.help");	
}

/**
 * 策略表达式处理类
 */
var StgyClass = {
	datas:{//数据原型
		where:new Array()
	},
	initDatas:function(){//初始化数据原型
		StgyClass.datas.where = new Array();
	},
	checkWhereExists : function() {
		/**
		 * 判断条件是否存在
		 */
		StgyClass.initDatas();
		var conditions = new Array(), exists = false;
		var div = document.getElementById("objectDiv");
		var spans = div.getElementsByTagName("span");
		$.each(spans, function(index, obj) {
			 if(obj.id && obj.id.indexOf("_stgySpan")>=0){
				 exists=true
				 /**
				  * 同时将条件数据打包
				  */
				 StgyClass.datas.where.push(JSON.parse(obj.textContent));
			 }
		});

		return exists;
	},
	showStgyDiv : function(thisObj,type) {

		var isOwner = $("#isOwner").val();
		/**
		 * 显示策略编辑(弹出新元素)
		 */
		actionConf.actionObj=thisObj.parentNode;
		
		var sb = new StringBuffer();
		sb.append("<div class=\"titleDiv\">");
		sb.append("触发策略");
		sb.append("<div class=\"icon-signout icon-myout\" onclick=\"javascript:StgyClass.closeStgyDiv()\"></div>");
		sb.append("</div>");

		if(StgyClass.checkWhereExists()){ //渲染触发策略页面
			sb.append( '<div class="edit-div-where">');
			$.each(StgyClass.datas.where,function(index,data){

				if(isOwner=="true"){	
					sb.append( '<button id="'+data.id+'" type="button" class="btn btn-success" onclick="javascript:StgyClass.appendWhereToStgy(this)">'+StgyClass.formatShowWhere(data)+'</button>');
				}else{	
					sb.append( '<button id="'+data.id+'" type="button" class="btn btn-success" >'+StgyClass.formatShowWhere(data)+'</button>');
				}
			});
			sb.append( '</div>');
			

			if(isOwner=="true"){
				sb.append( '<div id="stgy_exp" class="edit-div input_must" contenteditable="true"   placeholder="点击条件，编写运算符:(&&)||">');
			}else{
				sb.append( '<div id="stgy_exp" class="edit-div"  placeholder="点击条件，编写运算符:(&&)||">');
			}
			
			if(type=="edit"){
				sb.append(thisObj.parentNode.parentNode.getElementsByTagName("div")[0].innerHTML);
			}
			sb.append( '</div>');
			
			if(isOwner=="true"){
				sb.append( '<div><button style="width:95%;margin-top:5px;min-width:340px;" class="btn btn-primary " onclick="javascript:StgyClass.saveStgyToAppend(\''+type+'\');">保存</button></div>');
			}
			
		}else{
			sb.append("<div class=\"titleDiv-stgy-nodata\">没有可用条件</div>");
		}

		HtmlHelper.id("stgyDiv").innerHTML = sb.toString();
		window.winmgr.hide("objectDiv");
		window.winmgr.show("stgyDiv");

	},
	closeStgyDiv : function() {
		/**
		 * 关闭策略编辑(关闭元素)
		 */
		window.winmgr.hide("stgyDiv");
		window.winmgr.show("objectDiv");
	},
	appendWhereToStgy:function(thisObj) {
		/**
		 * 在策略编辑:将选中条件追加到策略表达式
		 */
		var whereId = thisObj.id + "_exp";

		var html = '&nbsp;<span name=\''+whereId+'\' class=\'whereStgyEdit whereStgyEdit-success\' contentEditable=\'false\'>&nbsp;'
				+ thisObj.innerText + '&nbsp;</span>&nbsp;';
		/**
		 * 必须要focus一下目标元素，不然会跟随光标而追加html内容。
		 */
		document.getElementById("stgy_exp").focus();
		var sel = window.getSelection();
		if (sel.getRangeAt && sel.rangeCount) {
			var range = sel.getRangeAt(0);
			range.deleteContents();

			var el = document.createElement("div");
			el.innerHTML = html;
			var frag = document.createDocumentFragment(), node, lastNode;
			while ((node = el.firstChild)) {
				lastNode = frag.appendChild(node);
			}
			range.insertNode(frag);

			// Preserve the selection
			if (lastNode) {
				range = range.cloneRange();
				range.setStartAfter(lastNode);
				range.collapse(true);
				sel.removeAllRanges();
				sel.addRange(range);
			}
		}
	},
	saveStgyToAppend : function(type){
		/**
		 * 策略编辑,保存按钮:关闭编辑,并且将策略结果追加到页面
		 */
		var html = document.getElementById("stgy_exp").innerHTML;
		
		if(html.length>0 && type=="add"){
			html =  '<div class="well-list well-list-display">'+
						'<div name="stgy_exp_html" style="width:80%;">'+html+'</div>'+
						'<div style="width: 20%;">'+
						'<span class="glyphicon glyphicon-remove well-del" onclick="javascript:StgyClass.deleteStgyToAppend(this);"></span><span class="glyphicon glyphicon-edit well-edit" onclick="javascript:StgyClass.showStgyDiv(this,\'edit\');"></span>';
						'</div>'+
						'</div>';
				
			var newNode = document.createElement("div");
			newNode.innerHTML = html;
			actionConf.actionObj.parentNode.appendChild(newNode);
			
		}else if(html.length>0 && type=="edit"){
			actionConf.actionObj.parentNode.getElementsByTagName("div")[0].innerHTML = html;
		}
		
		StgyClass.closeStgyDiv();
		
	},
	deleteStgyToAppend : function(thisObj){
		/**
		 * 删除策略结果
		 */
		var node = thisObj.parentNode.parentNode.parentNode;
		node.removeChild(thisObj.parentNode.parentNode);
	},
	updateWhereToStgyAppend : function(json){
		/**
		 *修改策略结果
		 */
		var updateId = json.id+"_exp";
		var stgys = $("span[name='"+updateId+"']");
		$.each(stgys,function(index,obj){
			obj.innerHTML = "&nbsp;"+StgyClass.formatShowWhere(json)+"&nbsp;";
		});
	},
	deleteWhereToStgyAppend : function(thisObj){
		/**
		 * 删除条件时: 给对应策略添加删除线
		 */
		var divId = thisObj.parentNode.getElementsByTagName("span")[0].id+"_exp";
		var stgys = $("span[name='"+divId+"']");
		$.each(stgys,function(index,obj){
			obj.className = "whereStgyEdit whereStgyEdit-delete";
		});
		
	},
	/**
	 * 格式化条件，显示格式(除去id不显示)
	 * 
	 * @param json
	 * @returns {String}
	 */
	formatShowWhere : function(json) {
		
		if(!json){
			return "";
		}
		
		var result = json.expr;
				
		if(json.range && json.range!=""){
			result += ","+json.range;
		}
		
		if(json.func && json.func!=0 && json.func=="count"){
			result += ","+json.func+">"+json.cparam;
		}else if(json.func && json.func!=0){
			result += ","+json.func;
		}
		
		return result;
	},
	randomId : function(x, y) {
		if (!x) {
			x = 9999;
		}
		if (!y) {
			y = 1;
		}
		var d = [ "a", "b", "c", "d", "e", "f", "g", "h", "i" ];

		var rand = parseInt(Math.random() * (x - y + 1) + y)
				+ d[parseInt(Math.random() * d.length + 0)]
				+ parseInt(Math.random() * 1000)
				+ d[parseInt(Math.random() * d.length + 0)]
				+ parseInt(Math.random() * 1000);
		return rand;
	}
}