var mtableConfig = {
	id : "feebackTableada",
	pid : "feebackTableadaDiv",
	openDelete : false,
	key : "key",
	pagerSwitchThreshold : 600,
	pagesize:20,
	head : {
		key:['','0%'],
		time : [ '反馈时间', '25%' ],
		data : [ '内容' ,'50%'],
		uid : [ '反馈用户', '25%' ]
	},
	cloHideStrategy : {
		10000 : [ 1,2, 3],
		1100 : [ 1,2, 3],
		1000 : [ 1, 2,3],
		800 : [ 1, 2,3 ],
		500 : [ 2 ,3],
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
var modalConfig = {
		head:"",
		content:""
};

function showDataById(key){

	$.each(pageDatas,function(index,obj){
		if(obj.key==key){
			modalConfig.head=obj.uid+" "+obj.time;
			modalConfig.content=obj.data;
			return false;
		}
	});
	showDialog(modalConfig);
}

function loadAll_RESTClient() {
	
	var getPagingInfo = table.getPagingInfo();
	var json = {
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
	AjaxHelper.call({
		url : "../../rs/godeye/feedback/query/count",
		data : "{}",
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


$("document").ready(function() {
	initTable();
});