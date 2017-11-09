var url = "../../db/";

var nameConfig = {
		head:"验证失败",
		content:"请输入名称"
};
var nameMultiConfig = {
		head:"添加失败",
		content:"此名称已存在"
};
var urlConfig = {
		head:"验证失败",
		content:"请输入正确格式的URL"
};
function deleteDb_RESTClient(id) {
	var getPagingInfo = table.getPagingInfo();
	var pageNum = getPagingInfo.pageNum;
	var pageSize = getPagingInfo.pageSize;
	var data = {
			"name" : id
		};
		AjaxHelper.call({
			url : url + "delDb",
			data : JSON.stringify(data),
			async : true,
			cache : false,
			type : "POST",
			contentType : "application/json; charset=utf-8",
			success : function(data) {
				if (data) {
					data = eval(data);
					//当前页数据为空 自动退一页
					if(data.length == (pageNum-1)*pageSize) {
						table.setPageNum(pageNum-1);
					}
					ajaxGetdatas();
					
				} else {
					alert("删除OpenTSDB实例"+id+"返回值为空");
				}
			},
			error : function(data) {
				alert("删除OpenTSDB实例"+id+"失败:"+data);
			},
		});
}

function modifyDb_RESTClient(data) {
	
	var data = {
			"name" : data.name,
			"url": data.url,
			"version": data.version
		};
		AjaxHelper.call({
			url : url + "modifyDb",
			data : JSON.stringify(data),
			async : true,
			cache : false,
			type : "POST",
			contentType : "application/json; charset=utf-8",
			success : function(data) {
				if (data) {
					ajaxGetdatas();
				} else {
					alert("修改OpenTSDB实例"+data.name+"返回值为空");
				}
			},
			error : function(data) {
				alert("修改OpenTSDB实例"+data.name+"失败:"+data);
			},
		});
}

function addDb_RESTClient(data) {
	
	//验证
	var name = data.name;
	var url_add = data.url;
	var version = data.version;
	
	if(null == name || name == "") {
		showDialog(nameConfig);
		return;
	}
	
	var matchString = "(https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
	if(url_add.match(matchString) == null) {
		showDialog(urlConfig);
		return;
	}

	var datas = {
			"name" : name,
			"url": url_add,
			"version": version
		};
		AjaxHelper.call({
			url : url + "addDb",
			data : JSON.stringify(datas),
			async : true,
			cache : false,
			type : "POST",
			contentType : "application/json; charset=utf-8",
			success : function(data) {
				
				if(data == "multi_name") {
					showDialog(nameMultiConfig);
					return;
				}
				
				data = eval(data);
				if (data) {
					ajaxGetdatas();
				} else {
					alert("添加OpenTSDB实例"+name+"返回值为空");
				}
			},
			error : function(data) {
				alert("添加OpenTSDB实例"+name+"失败:"+data);
			},
		});
}

function queryDbById_RESTClient(db_name) {
	
	var data = {
			"name" : db_name
		};
		AjaxHelper.call({
			url : url + "queryDb",
			data : JSON.stringify(data),
			async : true,
			cache : false,
			type : "POST",
			contentType : "application/json; charset=utf-8",
			success : function(data) {
				data = eval(data);
				if (data) {
					showDbById(data);
				} else {
					alert("加载OpenTSDB实例"+db_name+"为空");
				}
			},
			error : function(data) {
			},
		});
}

function loadDbs_RESTClient() {
	
	var datas = new Array(),number=10;
	AjaxHelper.call({
		url : url + "loadDbs",
		async : true,
		cache : false,
		type : "GET",
		contentType : "application/json; charset=utf-8",
		success : function(data) {
		
			data = eval(data);
			showDb(data,datas);
		},
		error : function(data) {
			alert("加载OpenTSDB实例列表失败: "+data);
		},
	});
}
	
function searchDbs_RESTClient(input) {

	var getPagingInfo = table.getPagingInfo();
	var datas = new Array(),number=10;
	
	var data = {
		"url" : input,
		"pageindex" : getPagingInfo.pageNum,
		"pagesize" : getPagingInfo.pageSize
	};
	AjaxHelper.call({
		url : url + "searchDbs",
		data : JSON.stringify(data),
		async : true,
		cache : false,
		type : "POST",
		contentType : "application/json; charset=utf-8",
		success : function(data) {
			data = eval(data);
			showDb(data,datas);
		},
		error : function(data) {
			alert("查询失败："+data);
		},
	});
};

function userClickRow(key){
	showDialog(modalConfig);
};
var pageNum;
var pageSize;
function getPageParam(){
	var getPagingInfo = table.getPagingInfo();
	pageNum = getPagingInfo.pageNum;   
	pageSize = getPagingInfo.pageSize
	console.log("pageNum:"+pageNum);
	console.log("pageSize:"+pageSize);
}

function showDb(data,datas) {
	var href = location.href;
	href = href.substring(0, href.indexOf("uavapp_baseclassmgt"));
	for(var i=0; i<data.length; i++) {
		var row={
			id:data[i].name,
			dburl:"<a onclick=loadDescDiv('"+href+"','"+data[i].url+"')>"+data[i].url+"</a>",
			type:data[i].type, 
			access:data[i].access,
			version:data[i].version,
	};
	datas.push(row);
	}
	
	var count = datas.length;
	
	//清空数据
	table.clearTable();
	//必须先显示分页
	table.setTotalRow(count);
	getPageParam();
	//然后添加数据
	$.each(datas, function(index, obj) {
		if(index >=(pageNum-1)*pageSize && index<pageNum*pageSize) {
			table.add(obj);
		}
		
	});
}

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
}

function showDescDiv() {
	window.winmgr.show("descDiv");
	window.winmgr.hide("notifyList");
}

function showNotifyList() {
	window.winmgr.hide("descDiv");
	window.winmgr.show("notifyList");
}