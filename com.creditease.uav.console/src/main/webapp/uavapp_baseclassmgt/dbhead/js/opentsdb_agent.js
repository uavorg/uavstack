/**
 * TODO 窗体初始化,必须在body后面加载,因为窗口容器会追加到body
 */
window.winmgr.build({
	id           : "notifyList",
	height       : "auto",
	"overflow-y" : "auto",
	order        : 999,
	theme        : "ListBG"
});

window.winmgr.build({
	id           : "descDiv",
	height       : "auto",
	"overflow-y" : "auto",
	order        : 999,
	theme        : "ObjectBG"
});

window.winmgr.show("notifyList");

function initBody() {
	
	var div = new StringBuffer();
	
	div.append("<div class=\"AppHubMVCSearchBar\" >");
	div.append('<button id="AppManagerAdd" class="btn btn-default" type="button" onclick="addOpenTSDBInfo()">');
	div.append('<span class="glyphicon glyphicon-plus"></span>');
	div.append('</button>');
	
	div.append('<input id="AppManagerSearchBar_keyword_Hidden" type="hidden">');
	div.append('<input id="AppManagerSearchBar_keyword" class="form-control AppHubMVCSearchBarInputText" type="text" placeholder="可输入URL检索" value="">');
	div.append('</input>');
	
	div.append('<div class="btn-group">');
	div.append('<button id="AppManagerSearchBar_searchbtn" type="button" class="btn btn-default" onclick="searchEvent()">');
	div.append('<span class="glyphicon glyphicon-search"></span>');
	div.append('</button>');

	div.append('<button id="AppManagerSearchBar_searchAllbtn" type="button" class="btn btn-default" onclick="searchAllEvent()">');
	div.append('<span class="glyphicon glyphicon-th"></span>');
	div.append('</button>');
	div.append('</div>');
	div.append('</div>');
	
	HtmlHelper.id("notifyList").innerHTML += div.toString();
	
};

function loadDescDiv(href, ip) {
	
	var url = href+"db2?url="+ip;
	var sb=new StringBuffer();
	sb.append("<div class=\"title-head\" id='opentsdb_head' style='width:100%'>");
	sb.append("<span>"+ip+"</span>");
	sb.append("<div class=\"icon-signout icon-myout\" onclick=\"javascript:showNotifyList()\"></div>");
	sb.append("</div>");
	sb.append("<iframe src='"+url+"',frameborder='0' scrolling='no' height='100%' width='100%' onload='changeFrameHeightAndWidth()' id='opentsdb_frame'></iframe>");
	$('#descDiv').html(sb.toString());
	showDescDiv();
};

function showDescDiv() {
	
	window.winmgr.show("descDiv");
	window.winmgr.hide("notifyList");
};

function showNotifyList() {
	
	window.winmgr.hide("descDiv");
	window.winmgr.show("notifyList");
};

function showErrMsg(val,id,data) {
	
	var v;
	console.log(val);
	if (id == "add") {
		v = document.getElementById("errorAddMsg");
	} else if(id == "modify") {
		v = document.getElementById("errorModifyMsg");
	} else if(id == "del"){
		v = document.getElementById("errorDelMsg");
	} else if(id == null){
		alert(val);
		return;
	} else {
		return;
	}
	v.style.display="";
	v.innerText = val;
};

function initErrMsg(id) {
	
	var v;
	if (id == "add") {
		v = document.getElementById("errorAddMsg");
	} else if(id == "modify") {
		v = document.getElementById("errorModifyMsg");
	} else if(id == "del"){
		v = document.getElementById("errorDelMsg");
	} else {
		return;
	}
	v.style.display="none";
	v.innerText = "";
};

function closeModal(id) {
	
	initErrMsg(id);
	if(id == "add") {
		$('#OpenTSDBAddModal').modal('hide');
		document.getElementById("OpenTSDBAddName").value = "";
		document.getElementById("OpenTSDBAddURL").value = "";
		document.getElementById("OpenTSDBAddVersion").value = "";
	} else if(id == "modify") {
		$('#OpenTSDBModifyModal').modal('hide');
	} else if(id == "del") {
		$('#OpenTSDBDelModal').modal('hide');
	}
};

var index_id;

var listConfig = {
		
		id:"list",
		pid:"notifyList",
		openDelete:true,
		key:"id",
		pagerSwitchThreshold:600,
		pagesize : 5,
		head:{
			id         : ['实例名称', '25%'],
			dburl      : ['URL'],
			version    : ['版本编号', '25%']
		},
		cloHideStrategy:{
			1000  :[0,1,2],
			500   :[0,1],
			400   :[0,1]
		},
		events:{
			onRow : function(index, value) {
						switch(index) {
							case 0:
								index_id = value;
							case 2:
								return clickTab(index_id, value);
							case 1:
								return value;
						}
					
					}
			
		}
};

function clickTab(id, value) {
	
	if(null == value || value == "") {
		value = "NULL";
	}
	var res = "<div onclick=\"clickTabForQuery('"+id+"')\">"+value+"</div>";
	return res;
};

function changeFrameHeight() {

	 var ifm= document.getElementById("opentsdb_frame"); 
	 try{
		 var bHeight = ifm.contentWindow.document.body.scrollHeight;
		 var dHeight = ifm.contentWindow.document.documentElement.scrollHeight;
		 var height = Math.max(bHeight, dHeight);
		 ifm.height = height;
	 }catch (ex){
		 
	 }
	 window.setInterval("changeFrameHeight()", 100);
	 
	 window.onresize=function(){
	     changeFrameWidth();  
	} 
	 
};

function changeFrameWidth() {
	
	var ifm= document.getElementById("opentsdb_frame");
	try{
		var bWidth = ifm.contentWindow.document.body.scrollWidth;
		var dWidth = ifm.contentWindow.document.documentElement.scrollWidth;
		var width = Math.max(bWidth, dWidth);
		ifm.width = width;
	 }catch (ex){
		 
	 }
};

function changeFrameHeightAndWidth() {
	
	changeFrameHeight();
	changeFrameWidth();
};

function deleteOpenTSDBById(id,trObj){
	
	document.getElementById("OpenTSDBDelName").innerText = trObj.getElementsByTagName("td")[0].id;
	$('#OpenTSDBDelModal').modal({
		backdrop : 'static',
		keyboard : false});
};

function loadOpenTSDBList() {
	lm.loadDbs_RESTClient();
};

function clickTabForQuery(v) {
	lm.queryDbById_RESTClient(v);
};

function addOpenTSDBInfo() {
	
	$('#OpenTSDBAddModal').modal({
		backdrop : 'static',
		keyboard : false});
};

function searchEvent(){
	
	table.setPageNum(1);
	var input = $("#AppManagerSearchBar_keyword").val();
	lm.searchDbs_RESTClient(input);
};

function searchAllEvent(){
	
	table.setPageNum(1);
	var input = "";
	lm.searchDbs_RESTClient(input);
};

var table= new AppHubTable(listConfig);

$(document).ready(function() {
	
	initBody();
	table.delRowUser = deleteOpenTSDBById;						
	table.sendRequest = loadOpenTSDBList;
	table.initTable();
});