
/**
 * TODO 窗体初始化,必须在body后面加载,因为窗口容器会追加到body
 */
window.winmgr.build({
	id : "notifyList",
	height : "auto",
	"overflow-y" : "auto",
	order : 999,
	theme : "ListBG"
});
window.winmgr.build({
	id : "descDiv",
	height : "auto",
	"overflow-y" : "auto",
	order : 999,
	theme : "ObjectBG"
});
window.winmgr.show("notifyList");

function initBody() {
	var div = new StringBuffer();
	
	div.append("<div class=\"AppHubMVCSearchBar\" >");
	div.append('<button id="AppManagerAdd" class="btn btn-default" type="button" onclick="addOpenTSDB()">');
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
}

function searchEvent(){
	table.setPageNum(1);
	var input = $("#AppManagerSearchBar_keyword").val();
	searchDbs_RESTClient(input);
};

function searchAllEvent(){
	table.setPageNum(1);
	var input = "";
	searchDbs_RESTClient(input);
};

/**
 * 列表配置
 */
var index_id;
var listConfig = {
		
		id:"list",
		pid:"notifyList",
		openDelete:true,
		key:"id",
		pagerSwitchThreshold:600,
		pagesize : 5,
		head:{
				id         : ['实例名称', '15%'],
				dburl      : ['URL'],
				version    : ['版本号', '15%']
		    },
		cloHideStrategy:{
			1000:[0,1,2],
			500:[0,1],
			400:[1]
		},
		events:{
			onRow : function(index, value) {
					switch(index) {
					case 0:
						index_id = value;
					case 2:
						return setOnClick(index_id, value);
						break;
					case 1:
						return value;
					}
					
				}
			
		}
};
function setOnClick(id, value) {
	if(null == value || value == "") {
		value = "NULL";
	}
	var res = "<div onclick=\"userClickRow('"+id+"')\">"+value+"</div>";
	return res;
}

function changeFrameHeight() {

	 var ifm= document.getElementById("opentsdb_frame"); 
	 try{
	 var bHeight = ifm.contentWindow.document.body.scrollHeight;
	 var dHeight = ifm.contentWindow.document.documentElement.scrollHeight;
	 var height = Math.max(bHeight, dHeight);
	 ifm.height = height;
	 }catch (ex){}
	 window.setInterval("changeFrameHeight()", 100);
	 window.onresize=function(){
	     changeFrameWidth();  

	} 
}

function changeFrameWidth() {
	var ifm= document.getElementById("opentsdb_frame");
	var head = document.getElementById("opentsdb_head");
	 try{
	 var wWidth = window.document.body.offsetWidth;
	 var bWidth = ifm.contentWindow.document.body.scrollWidth;
	 var dWidth = ifm.contentWindow.document.documentElement.scrollWidth;
	 var width = Math.max(bWidth, dWidth, wWidth);
	 ifm.width = width;
	 }catch (ex){}
}

function changeFrameHeightAndWidth() {
	changeFrameHeight();
	changeFrameWidth();
}
var table= new AppHubTable(listConfig);
$(document).ready(function() {
	initBody();
	table.delRowUser = userDelete;						//Config the delete function of user
	table.sendRequest = ajaxGetdatas;
	table.initTable();
});
