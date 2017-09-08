var mtableConfig = {
	id : "feebackTableada",
	pid : "feebackTableadaDiv",
	openDelete : false,
	key : "key",
	pagerSwitchThreshold : 600,
	pagesize:10,
	head : {
		key:['','0%'],
		time : [ '反馈时间', '25%' ],
		data : [ '内容' ,'50%']
	},
	cloHideStrategy : {
		10000 : [ 1,2],
		1100 : [ 1,2],
		1000 : [ 1, 2],
		800 : [ 1, 2],
		500 : [ 1, 2 ],
		400 : [ 2 ]
	},
	events:{
		  onRow:function(index,value) {
			  if(index==2){
				  value = "<span style=\"float:left;\">"+value+"</span>";
			  }
			  
			   return value;
		   }
	}
};
window.winmgr.build({
	id : "HeadWin",
	height : "auto",
	"overflow-y" : "auto",
	order : 999,
	theme : "HeadBG"
});
var modalConfig = {
		head:"提交成功",
		content:""
};
var showDataConfig = {
		head:"",
		content:""
};
/**
 * 初始化头部
 */
function initHeadDiv() {
	var div = new StringBuffer();
	

	div.append("<!-- 头部 BEGIN -->");
	div.append("<div class=\"AppHubMVCSearchBar\" >");
	div.append("<textarea Maxlength=\"600\" rows=\"9\" cols=\"100\"  id=\"feedbackInput\" class=\"form-control AppHubMVCSearchBarInputText\"");
	div.append("placeholder=\"您的反馈，将有效提升我们的产品质量，以及为您提供更好的服务。\" ></textarea >");

	div.append("<br/>");
	div.append("<div class=\"btn-group\">");
	div.append("<button id=\"searchbtn\" type=\"button\" class=\"btn btn-default\" onclick=\"javascript:submit()\">");
	div.append("<span class=\"glyphicon glyphicon-saved\"></span>");
	div.append("</button>");

	div.append("</div>");
	div.append("</div>" );
	

	div.append("<div id=\"feebackTableadaDiv\"></div>" );
	
	HtmlHelper.id("HeadWin").innerHTML = div.toString();
	window.winmgr.show("HeadWin");
}

function showDataById(key){

	$.each(pageDatas,function(index,obj){
		if(obj.key==key){
			showDataConfig.head=obj.uid+" "+obj.time;
			showDataConfig.content=obj.data;
			return false;
		}
	});
	showDialog(showDataConfig);
}

function submit(){
	var data = $("#feedbackInput").val().trim();
	if(data){
		showDialog(modalConfig);
		var json={"data":data};
		AjaxHelper.call({
			url : "../../rs/godeye/feedback/save",
			data : JSON.stringify(json),
			async : true,
			cache : false,
			type : "POST",
			success : function(result) {
				console.log(result);
				loadAll_RESTClient();
			},
			error : function(result) {
				console.log(result);
			}
		});
		
		$("#feedbackInput").val("继续编辑："+data);
	}
	
}


function loadAll_RESTClient() {
	
	var getPagingInfo = table.getPagingInfo();
	var json = {
		"checkuser":"true",
		"pageindex" : getPagingInfo.pageNum,
		"pagesize" : getPagingInfo.pageSize
	};

	AjaxHelper.call({
		url : "../../rs/godeye/feedback/query",
		data : JSON.stringify(json),
		async : true,
		cache : false,
		type : "POST",
		dataType : "json",
		success : function(result) {
			result = eval("(" + result["rs"] + ")");
			if (result.code == "1") {
				//当前页数据为空，自动退一页
				if(result.data.length==0 && getPagingInfo.pageNum>1){
					table.setPageNum(--getPagingInfo.pageNum);
					loadAll_RESTClient();
				}else{
					pageDatas=result.data;
					loadAll_Count_RESTClient();	
				}
				
			} else {
				console.log(result);
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}


function loadAll_Count_RESTClient() {
	var json = {
			"checkuser":"true"
		};
	AjaxHelper.call({
		url : "../../rs/godeye/feedback/query/count",
		data : JSON.stringify(json),
		dataType : "json",
		async : true,
		cache : false,
		type : "POST",
		success : function(result) {
			result = eval("(" + result["rs"] + ")");
			if (result.code == "1") {
				var count = result.data[0]["count"];
				//清空数据
				table.clearTable();
				//必须先显示分页
				table.setTotalRow(count);
				table.renderPagination();
				//然后添加数据
				table.addRows(pageDatas);
			} else {
				console.log(result);
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}

var table,pageDatas;
function initTable() {
	table = new AppHubTable(mtableConfig);
	table.cellClickUser = showDataById;
	table.sendRequest = loadAll_RESTClient;
	table.initTable();
}

$(document).ready(function(){
	initHeadDiv();
	initTable();
});