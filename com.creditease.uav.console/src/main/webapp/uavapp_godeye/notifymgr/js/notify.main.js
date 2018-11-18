/**
 * 列表配置
 */
var listConfig = {
	id : "notifymgrTableada",
	pid : "notifyList",
	openDelete : false,
	key : "ntfkey",
	pagerSwitchThreshold : 600,
	pagesize : 50,
	deleteCtr : {
		key : "state",
		showDelete : "0"
	},
	head : {
		latestrecord_ts : [ '最近预警时间', '100px' ],
		appgroup : [ '应用组' , '90px' ],
		state : [ '状态', '10%' ],
		title : [ '问题摘要', '40%' ],
		description : [ '问题描述', '30%' ],
		time : [ '首次预警时间', '100px' ]	
	},
	cloHideStrategy : {
		1100 : [ 0, 1, 2, 3, 4, 5],
		800 :  [ 0, 1, 2, 3, 4],
		600 :  [ 0, 1, 2, 3],
		500:   [0, 3]
	},
	events:{
		onRow:function(index,value) {
			if (index==0||index==5) {
				var timeInfo=value.split(" ");
				value=timeInfo[0]+"<br/>"+timeInfo[1];
				return "<span style='font-size:12px;color:#009ad6;word-break:break-all;word-wrap:break-word;white-space:normal;'>"+value+"</span>";
			}
//			else if (index==1) {
//				return "<span style='font-size:12px;color:green;'>"+value+"</span>";
//			}
			else if (index==3) {
				var idx=value.indexOf("运行时预警");
				if(idx==0) {
				   return value.substring(6);
				}				
			}
			else if (index==2) {
				var color="blue";
				if(value=="新预警"){
					color  = "#EEB422";
				}else if(value=="已查看"){
					color = "#800000";
				}else if(value=="报警持续中"){
					color  = "red";
				}else if(value=="已查看&报警持续中"){
					color  = "red";
				}else if(value=="已处理"){
					color = "#B4B4B4";
 				}
				return "<span style='font-size:12px;color:"+color+";font-weight:bold;'>"+value+"</span>";
			}
			
			return value;
		},
		
		appendRowClass:function(rowData){
			var notifyLevel = rowData['args']['notifyLevel'];
			if (!notifyLevel){
				return "";
			}else if (notifyLevel === "info"){
				return "infomation";
			}else {
				return notifyLevel;
			}
		}
	}
};

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
	id : "ejectDiv",
	height : "auto",
	"overflow-y" : "auto",
	order : 999,
	theme : "ObjectBG"
});
window.winmgr.build({
	id : "descDiv",
	height : "auto",
	"overflow-y" : "auto",
	order : 999,
	theme : "ObjectBG"
});
window.winmgr.show("notifyList");

/**
 * 函数
 */
function initHeadDiv() {
	var divContent = "<!-- 头部 BEGIN -->"
			+ "<div class=\"AppHubMVCSearchBar\" >"
			+ "<input id=\"searchInput\" class=\"form-control AppHubMVCSearchBarInputText\""
			+ "type=\"text\" placeholder=\"IP,HOST,摘要,事件类型 检索\" value=\"\"></input>"

			+ "<div class=\"btn-group\">"
			+ "<button id=\"searchbtn\" type=\"button\" class=\"btn btn-default\" onclick=\"javascript:searchListClick('isInput')\">"
			+ "<span class=\"glyphicon glyphicon-search\"></span>"
			+ "</button>"

			+ "<button id=\"bestSearchbtn\" type=\"button\" class=\"btn btn-default\" onclick=\"javascript:showBSEjectDiv();\">"
			+ "<span class=\"glyphicon glyphicon-zoom-in\"></span>"
			+ "</button>"

			+ "<button id=\"searchAllbtn\" type=\"button\" class=\"btn btn-default\"  onclick=\"javascript:searchListClick('isAll');\">"
			+ "<span class=\"glyphicon glyphicon-th\"></span>" + "</button>"
			+ "</div>" + "</div>" + "<!-- 头部 END -->";
	HtmlHelper.id("notifyList").innerHTML += divContent;
}
function initListClass() {
	list.cellClickUser = showDSEjectDiv;
	list.sendRequest = loadNotify_RestfulClient;
	list.initTable();
}
function loadListData(datas, count) {
	list.clearTable();
	// 必须先显示分页
	list.setTotalRow(count);
	list.renderPagination();
	// 然后添加数据

	$.each(datas, function(index, obj) {
		obj["time"] = TimeHelper.getTime(obj["time"],'FMS');
		obj["latestrecord_ts"] = TimeHelper.getTime(obj["latestrecord_ts"],'FMS');
		
		if(obj["state"]==0){
			obj["state"] = "新预警";
		}else if(obj["state"]==10){
			obj["state"] = "报警持续中";
		}else if(obj["state"]==15){
			obj["state"] = "已查看";
		}else if(obj["state"]==20){
			obj["state"] = "已查看&报警持续中";
		}else if(obj["state"]==25){
			obj["state"] = "已处理";
		}
		
		
	});
	list.addRows(datas);
}

function canladerBindEvent(){
	$(".form_datetime_start").datetimepicker({
        format: "yyyy-mm-dd hh:ii",
        minuteStep: 5,
        todayBtn: true,
        autoclose: true
    });
    $(".form_datetime_start").on('show', function(ev){
    	var hideBg = '<div id="hideBg" style="position: fixed;background-color: #777;width:100%;height: 100%;filter:alpha(opacity=60);opacity:0.6;display:block;z-Index: 1000;"></div>';
    	$('body').append(hideBg);
    	//document.body.innerHTML += hideBg;
    	
    });
    $(".form_datetime_start").on('hide', function(ev){
    	document.getElementById("hideBg").style.display="none";
    	var removeObj = document.getElementById("hideBg");
    	removeObj.parentNode.removeChild(removeObj); 
    });
    
    $(".form_datetime_end").datetimepicker({
        format: "yyyy-mm-dd hh:ii",
        minuteStep: 5,
        todayBtn: true,
        autoclose: true
    });
    $(".form_datetime_end").on('show', function(ev){
    	var hideBg = '<div id="hideBg" style="position: fixed;background-color: #777;width:100%;height: 100%;filter:alpha(opacity=60);opacity:0.6;display:block;z-Index: 1000;"></div>';
    	$('body').append(hideBg);
    });
    $(".form_datetime_end").on('hide', function(ev){
    	document.getElementById("hideBg").style.display="none";
    	var removeObj = document.getElementById("hideBg");
    	removeObj.parentNode.removeChild(removeObj); 
    });
}

function addNotifyEvent(list){
	var buf = [];
	buf.push('<option>事件类型</option>\n');
	for(var i=0; i<list.length; i++){
		buf.push('<option>'+ list[i] +'</option>\n');
	}
	HtmlHelper.id("eventSelector").innerHTML = buf.join('');
}

function getNotifyEvent(){
	getNotifyEventList_RestfulClient();
} 

var isBSEject = false;
function showBSEjectDiv() {
	var divContent = "	<!-- 高级查询   BEGIN -->"
			+ '<div class="title-head"><span>高级查询</span>'
			+ "<div class=\"icon-signout icon-myout\" onclick=\"javascript:closeEjectDiv()\"></div>"
			+ "</div>"
			+"<br/>"
			+ '<div class="itemContainer" style="width:95%;font-size: 14px">'
			+ '<div><input  class="itemDisplayRight" id="bestInputIP" placeholder="IP"></input></div>'
			//+ '<div><span class="bestNotifyMessage"></span></div>'
			+ '<div><input  class="itemDisplayRight" id="bestInputHOST"  placeholder="HOST"></input></div>'
			+ '<div><input  class="itemDisplayRight" id="bestTextAreaAbstract" placeholder="问题摘要"></input></div>'
			+ '<div><textarea  class="itemDisplayRightT" id="bestTextAreaDescription" style="resize: none;border-radius: 4px;" rows="2" placeholder="问题描述"></textarea></div>'
			+ '<div><div  class="itemDisplayRight bestTableRight" style="display:inline-block"><div class="form-group"><select id="eventSelector" class="form-control"></select></div></div></div>'
			+ '<div><div class="itemDisplayRight bestTableRight" style="display: inline-block;">'
			+ '	 <div class="input-append date form_datetime_start" style="display:flex">'
			+ '		<input id="bestInputStartTime" style="width: 100%;border-radius: 4px;    height: 34px;" value="" readonly="" placeholder="起始时间">'
			+ '		<button style=""><span style="width:20" class="add-on"><i class="icon-th"></i></span></button>'
			+ '	 </div></div></div>'
			+ '<div><div class="itemDisplayRight bestTableRight" style="display: inline-block;">'
			+ '	 <div class="input-append date form_datetime_end" style="display:flex">'
			+ '    	<input id="bestInputEndTime"  style="width: 100%;border-radius: 4px;    height: 34px;" value="" readonly=""  placeholder="结束时间">'
			+ '    	<button style=""><span style="" class="add-on"><i class="icon-th"></i></span></button>'
			+ '	 </div></div></div>'
			+ '<div>'
			+ "	<button type=\"button\" class=\"btn btn-default itemDisplayRight\" onclick=\"javascript:searchListClick('isBest');\">"
			+ "		<span class=\"glyphicon glyphicon-zoom-in\"></span>"
			+ "	</button>"
			+ '</div>'
			+ '<div>'
			+ "	<button type=\"button\" class=\"btn btn-default itemDisplayRight\" onclick=\"javascript:searchListClear();\">"
			+ "		<span>清空条件</span>"
			+ "	</button>"
			+ '</div>'
			+ '</div>'
			+ '</div>'
			+ '</div>'
			+ "<!-- 高级查询   END -->";
	if(!isBSEject){
		HtmlHelper.id("ejectDiv").innerHTML = divContent;
		canladerBindEvent();
		getNotifyEvent();
		isBSEject = true;
	}	
	window.winmgr.hide("notifyList");
	window.winmgr.show("ejectDiv");

}

function searchListClear(){
	$("#searchInput").val("");
	$("#bestInputIP").val("");
	$("#bestInputHOST").val("");
	$("#eventSelector").val("事件类型");
	$("#bestInputEndTime").val("");
	$("#bestInputStartTime").val("");
	$("#bestTextAreaDescription").val("");
	$("#bestTextAreaAbstract").val("");
}

function showDSEjectDiv(id,obj) {
	var ntfkey = obj["id"];
	var time = obj.getElementsByTagName('td')[5].id;
	var paramObject = {"action":"view","ntfkey":ntfkey,"time":time,"type":"mgr"};
	updateNotify_RestfulClient(paramObject);
	window.winmgr.hide("notifyList");
	window.winmgr.show("descDiv");
}

function closeEjectDiv() {
	window.winmgr.hide("ejectDiv");
	window.winmgr.show("notifyList");
}
function closeDescDiv() {
	loadNotify_RestfulClient();
	window.winmgr.hide("descDiv");
	window.winmgr.show("notifyList");
}
function searchListClick(type) {
	if ("isBest" == type) {
		// 清空查询框
		$("#searchInput").val("");
	} else if ("isAll" == type) {
		// 清空查询框
		$("#searchInput").val("");
		$("#bestInputIP").val("");
		$("#bestInputHOST").val("");
		$("#eventSelector").val("事件类型");
		$("#bestInputEndTime").val("");
		$("#bestInputStartTime").val("");
		$("#bestTextAreaDescription").val("");
		$("#bestTextAreaAbstract").val("");
		isBSEject = false;
	} else if("isInput" == type){
		$("#bestInputIP").val("");
		$("#bestInputHOST").val("");
		$("#eventSelector").val("事件类型");
		$("#bestInputEndTime").val("");
		$("#bestInputStartTime").val("");
		$("#bestTextAreaDescription").val("");
		$("#bestTextAreaAbstract").val("");
		isBSEject = false;
	}
	// 查询按钮默认第一页查询
	list.setPageNum(1);
	loadNotify_RestfulClient();
	closeEjectDiv();
}
function getPageParams() {
	var jsonParam = new Object();
	// 分页参数
	var getPagingInfo = list.getPagingInfo();
	jsonParam["pageindex"] = getPagingInfo.pageNum,
	jsonParam["pagesize"] = getPagingInfo.pageSize

	var searchInput = $("#searchInput").val();
	if (searchInput) {
		jsonParam["search"] = searchInput;
	}
	
	if ($("#bestInputIP").val()) {
		jsonParam["ip"] = $("#bestInputIP").val();
	}
	if ($("#bestInputHOST").val()) {
		jsonParam["host"] = $("#bestInputHOST").val();
		//console.log(jsonParam["host"]);
	}
	if ($("#eventSelector").val() && $("#eventSelector").val() != "事件类型") {
		jsonParam["event"] = $("#eventSelector").val();
		//console.log(jsonParam["event"]);
	}
	if ($("#bestInputEndTime").val()) {
		var temper=$("#bestInputEndTime").val(); 
		var dt = new Date(temper.replace(/-/,"/")) 
		jsonParam["endTime"] = dt.getTime();
	}else{
		var endTime = new Date().getTime();
		jsonParam["endTime"] = endTime;
	}
	if ($("#bestInputStartTime").val()) {
		var temper=$("#bestInputStartTime").val(); 
		var dt = new Date(temper.replace(/-/,"/")) 
		jsonParam["startTime"] = dt.getTime();
	}
	if ($("#bestTextAreaDescription").val()) {
		jsonParam["description"] = $("#bestTextAreaDescription").val();
	}
	if ($("#bestTextAreaAbstract").val()) {
		jsonParam["abstract"] = $("#bestTextAreaAbstract").val();
	}

	return JSON.stringify(jsonParam);
}


/**
 * 列表对象
 */
var list = new AppHubTable(listConfig);

$(document).ready(function() {
	initHeadDiv();
	initListClass();
});
