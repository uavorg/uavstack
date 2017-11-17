var url = "../../rs/db/";
var list = null;

var delmodalConfig = {
    head     :"提示",
    content  :"",
    callback :"clickSureCallback_del()"
};

var addmodalConfig = {
	head     :"添加",
	content  :"",
	callback :"clickSureCallback_add()"
};

var modifymodalConfig = {
	head     :"修改",
	content  :"",
	callback :"clickSureCallback_modify()"
};

var opentsdbmodalConfig = {
	name     :'0',
	url      :'0',
	version  :'0'
};

function ListManage() {
	
	var pageNum;
	var pageSize;
	
	this.deleteDb_RESTClient = function(name) {
		
		var data={"name":name};
		var subUrl = "delDb";
		lm.invokeAjaxFunction(subUrl, JSON.stringify(data), "POST", lm.delSuccessFunc, lm.delErrorFunc);
    }
	
	this.modifyDb_RESTClient = function(data) {
		
		//验证
		var name = data.name;
		var url_modify = data.url;
		var version_modify = data.version;
		
		var matchString = "(https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
		if(url_modify.match(matchString) == null) {
			showErrMsg("请输入正确格式的URL","modify", data);
			return;
		}
		
		var datas = {
			"name"     : name,
			"url"      : url_modify,
			"version"  : version_modify
		};
		
		var subUrl = "modifyDb";
		lm.invokeAjaxFunction(subUrl, JSON.stringify(datas), "POST", lm.modifySuccessFunc, lm.modifyErrorFunc);
	}
	
	this.addDb_RESTClient = function(data) {
		
		//验证
		var name_add = data.name;
		var url_add = data.url;
		var version_add = data.version;
		
		if(null == name_add || name_add == "") {
			showErrMsg("请输入名称", "add", data);
			return;
		}
		
		var matchString = "(https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
		if(url_add.match(matchString) == null) {
			showErrMsg("请输入正确格式的URL", "add", data);
			return;
		}
		
		var datas = {
			"name"      : name_add,
			"url"       : url_add,
			"version"   : version_add
		};
		
		var subUrl = 'addDb';
		lm.invokeAjaxFunction(subUrl, JSON.stringify(datas), "POST", lm.addSuccessFunc, lm.addErrorFunc);
	}
	
	this.queryDbById_RESTClient = function(db_name) {
		
		var subUrl = "queryDb";
		var data = { "name" : db_name };
		lm.invokeAjaxFunction(subUrl, data, "GET", lm.querySuccessFunc, lm.queryErrorFunc);
	}
	
	this.loadDbs_RESTClient = function() {
		
		var subUrl = "loadDbs";
		lm.invokeAjaxFunction(subUrl, null, "GET", lm.loadSuccessFunc, lm.loadErrorFunc);
	}
	
	this.showOpenTSDBById = function(data) {
		
		document.getElementById("OpenTSDBModifyName").value = data[0].name;
		document.getElementById("OpenTSDBModifyURL").value = data[0].url;
		document.getElementById("OpenTSDBModifyVersion").value = data[0].version;
		$('#OpenTSDBModifyModal').modal({
			backdrop : 'static',
			keyboard : false});
		 
	}
	
	this.showOpenTSDBList = function(data, list) {
		
		list = new Array();
		var href = location.href;
		href = href.substring(0, href.indexOf("uavapp_baseclassmgt"));
		for(var i=0; i<data.length; i++) {
			var row={
				id       :data[i].name,
				dburl    :"<a onclick=loadDescDiv('"+href+"','"+data[i].url+"') class='pointer'>"+data[i].url+"</a>",
				version  :data[i].version,
			};
			list.push(row);
		}
		
		var count = list.length;

		//清空数据
		table.clearTable();
		//必须先显示分页
		table.setTotalRow(count);
		lm.getPageParam();
		//然后添加数据
		$.each(list, function(index, obj) {
			if(index >=(pageNum-1)*pageSize && index<pageNum*pageSize) {
				table.add(obj);
			}
			
		});
	}
	
	this.getPageParam = function() {
		
		var getPagingInfo = table.getPagingInfo();
		pageNum = getPagingInfo.pageNum;   
		pageSize = getPagingInfo.pageSize
	}
	
	this.searchDbs_RESTClient = function(input) {
		
		
		var subUrl = "searchDbs";
		var data = { "url" : input};
		lm.invokeAjaxFunction(subUrl, data, "GET", lm.loadSuccessFunc, lm.searchErrorFunc);
	}
	
	this.delSuccessFunc = function(data) {
		var result = eval(data);
		//当前页数据为空 自动退一页
		if(result.length == (pageNum-1)*pageSize) {
			table.setPageNum(pageNum-1);
		}
		lm.showOpenTSDBList(result,list);
		closeModal("del");
	}
	
	this.delErrorFunc = function(data) {
		showErrMsg("删除OpenTSDB实例"+name+"失败","del", data);
	}
	
	this.modifySuccessFunc = function(data) {
		var result = eval(data);
		lm.showOpenTSDBList(result,list);
		closeModal("modify");
	}
	
	this.modifyErrorFunc = function(data) {
		showErrMsg("修改OpenTSDB实例"+name+"失败","modify", data);
	}
	
	this.addSuccessFunc = function(data) {
		var result = eval(data);
		if(result.error == "conflict_name") {
			showErrMsg("此名称已存在", "add", result);
			return;
		}
		if(parseInt(pageNum) == 0) {
			table.setPageNum(1);
			pageNum = table.getPagingInfo().pageNum;
		}
		
		lm.showOpenTSDBList(result,list);
		closeModal("add");
	}
	
	this.addErrorFunc = function(data) {
		showErrMsg("添加OpenTSDB实例失败", "add", data);
	}
	
	this.queryErrorFunc = function(data) {
		showErrMsg("加载OpenTSDB实例失败", null, data);
	}
	
	this.loadErrorFunc = function(data) {
		showErrMsg("加载OpenTSDB实例列表失败", null, data);
	}
	
	this.querySuccessFunc = function(data) {
		var result = eval(data);
		lm.showOpenTSDBById(result);
	}
	
	this.loadSuccessFunc = function(data) {
		var result = eval(data);
		lm.showOpenTSDBList(result);
	}
	
	this.searchErrorFunc = function(data) {
		showErrMsg("查询失败", null, data);
	}
	
	this.invokeAjaxFunction = function(subUrl, data, type, successFunc, errorFunc) {
		AjaxHelper.call({
			url         : url + subUrl,
			data        : data,
			async       : true,
			cache       : false,
			type        : type,
			contentType : "application/json; charset=utf-8",
			success : function(data) {
				successFunc(data);
			},
			error : function(data) {
				errorFunc(data);
			},
		});
	}
	
};

var lm = new ListManage();

function clickSureCallback_del(){
	
	var id = document.getElementById("OpenTSDBDelName").innerHTML;
	lm.deleteDb_RESTClient(id);
};

function clickSureCallback_modify() {
	
	opentsdbmodalConfig.name = document.getElementById("OpenTSDBModifyName").value;
	opentsdbmodalConfig.url = document.getElementById("OpenTSDBModifyURL").value;
	opentsdbmodalConfig.version = document.getElementById("OpenTSDBModifyVersion").value;
	lm.modifyDb_RESTClient(opentsdbmodalConfig);
	
};

function clickSureCallback_add(){
	
	opentsdbmodalConfig.name = document.getElementById("OpenTSDBAddName").value;
	opentsdbmodalConfig.url = document.getElementById("OpenTSDBAddURL").value;
	opentsdbmodalConfig.version = document.getElementById("OpenTSDBAddVersion").value;
	lm.addDb_RESTClient(opentsdbmodalConfig);
	
};