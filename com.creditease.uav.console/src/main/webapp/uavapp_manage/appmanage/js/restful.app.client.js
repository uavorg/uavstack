var url = "../../rs/manage/";

function addApp_RESTClient(id, inputurl,configpath) {

	if (!iscommit) {
		return;
	}
	iscommit = false;

	var data = {
		"appurl" : inputurl,
		"appid" : id,
		"configpath":configpath
	};

	AjaxHelper.call({
		url : url + "loadAppByid",
		data : JSON.stringify(data),
		async : true,
		cache : false,
		type : "POST",
		contentType : "application/json; charset=utf-8",
		success : function(data) {
			data = eval("(" + data["rs"] + ")");
			if (data.code == "1") {
				if (data.data.length > 0) {
					iscommit = true;
					showErrMsg("appid已经存在", 1);
				} else {
					addApp();
				}

			} else {
				iscommit = true;
				showErrMsg("添加失败", 1);
			}

		},
		error : function(data) {
			console.log(data);
			showErrMsg("添加失败", 1);
		}
	});

	function addApp() {
		AjaxHelper.call({
			url : url + "addApp",
			data : JSON.stringify(data),
			async : true,
			cache : false,
			type : "POST",
			contentType : "application/json; charset=utf-8",
			success : function(data) {
				data = eval("(" + data["rs"] + ")");
				if (data.code == "1") {
					//showErrMsg("添加成功", 1);
					userSendRequest();
					$("#AppRegisterModal").modal('hide');
				}

			},
			error : function(data) {
				console.log(data);
				showErrMsg("添加失败", 1);
			}
		});
	}

}

function modifyApp_RESTClient(id, newUrl,configPath) {
	var data = {
		"appurl" : newUrl,
		"appid" : id,
		"configpath":configPath
	};

	AjaxHelper.call({
		url : url + "updateApp",
		data : JSON.stringify(data),
		async : true,
		cache : false,
		type : "POST",
		contentType : "application/json; charset=utf-8",
		success : function(data) {
			data = eval("(" + data["rs"] + ")");
			if (data.code == "1") {
				userSendRequest();
				$("#AppEditModal").modal('hide');
				//showErrMsg("修改成功", 2);
			} else {
				console.log(data);
			}
		},
		error : function(data) {
			console.log(data);
			showErrMsg("修改失败", 2);
		},
	});
};

function deleteApp_RESTClient(id) {
	//console.log(v);
	var data = {
		"appid" : id
	};
	AjaxHelper.call({
		url : url + "delApp",
		data : JSON.stringify(data),
		async : true,
		cache : false,
		type : "POST",
		contentType : "application/json; charset=utf-8",
		success : function(data) {
			data = eval("(" + data["rs"] + ")");
			if (data.code == "1") {
				userSendRequest();
			} else {
				console.log(data);
			}
		},
		error : function(data) {
			console.log(data);
		},
	});
};

function loadApps_RESTClient() {

	var getPagingInfo = table.getPagingInfo();
	var inputValue = $("#AppManagerHeadSearchBarAppManagerSearchBar_keyword_Hidden").val();
	$("#AppManagerHeadSearchBarAppManagerSearchBar_keyword").val(inputValue);

	var data = {
		"appurl" : inputValue,
		"pageindex" : getPagingInfo.pageNum,
		"pagesize" : getPagingInfo.pageSize
	};

	//console.log("enter loadApps");
	AjaxHelper.call({
		url : url + "loadApps",
		data : JSON.stringify(data),
		async : true,
		cache : false,
		type : "POST",
		contentType : "application/json; charset=utf-8",
		success : function(data) {
			data = eval("(" + data["rs"] + ")");
			if (data.code == "1") {
				//当前页数据为空，自动退一页
				if(data.data.length==0 && getPagingInfo.pageNum>1 ){
					table.setPageNum(--getPagingInfo.pageNum);
					loadApps_RESTClient();
				}else{
					loadApps_Count_RESTClient(data.data);
				}
			} else {
				console.log(data);
			}
		},
		error : function(data) {
			console.log(data);
		},
	});
	//console.log("out loadApps");
};



function loadApps_Count_RESTClient(appDatas) {

	var inputValue = document.getElementById(searchBarPid
			+ "AppManagerSearchBar_keyword").value;

	var data = {
		"paramVlue" : inputValue,
		"type"   : "APP" 
	};

	AjaxHelper.call({
		url : url + "loadCount",
		data : JSON.stringify(data),
		async : true,
		cache : false,
		type : "POST",
		contentType : "application/json; charset=utf-8",
		success : function(data) {
			data = eval("(" + data["rs"] + ")");
			if (data.code == "1") {
				var count = data.data[0]["count"];
				//必须先分页展示  
				table.setTotalRow(count);
				table.renderPagination();
					
				//然后填充数据
				table.clearTable();
				var processed = preProcess(appDatas);
				table.addRows(processed);
			} else {
				console.log(data);
			}
		},
		error : function(data) {
			console.log(data);
		},
	});
	//console.log("out loadApps");
};


function queryAppById_RESTClient(id) {
	var data = {
		"appid" : id
	};

	AjaxHelper.call({
		url : url + "loadAppByid",
		data : JSON.stringify(data),
		async : true,
		cache : false,
		type : "POST",
		contentType : "application/json; charset=utf-8",
		success : function(data) {
			data = eval("(" + data["rs"] + ")");
			if (data.code == "1") {
				showErrMsg("加载app信息失败", 2);
				addAppInfo(data.data);
			} else {
				showErrMsg("加载app信息失败", 2);
			}

		},
		error : function(data) {
			console.log(data);
			showErrMsg("加载app信息失败", 2);
		}
	});

};
