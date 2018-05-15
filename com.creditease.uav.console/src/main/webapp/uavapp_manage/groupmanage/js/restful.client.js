var url = "../../rs/manage/";

function loadAllApps_RESTClient(func) {
	var postUrl = url + "loadAllApps";
	AjaxHelper.call({
		url : postUrl,
		data : {},
		async : true,
		cache : false,
		type : "POST",
		dataType : "json",
		success : function(result) {
			result = eval("(" + result["rs"] + ")");
			func(result);
		},
		error : function(result) {
			console.log(result);
		}
	});
}

function addGroup_RESTClient(groupid, ldapkey,appids) {

	var json = {
			"groupid" : groupid,
			"ldapkey":ldapkey
	};
	var postUrl = url + "loadGroupByInfo"; //先查询是否存在
	AjaxHelper.call({
		url : postUrl,
		data : JSON.stringify(json),
		async : true,
		cache : false,
		type : "POST",
		dataType : "json",
		success : function(result) {
			result = eval("(" + result["rs"] + ")");
			if (result.code == "1") {
				if (result.data.length > 0) {
					setErrorMsg("addErrorMsg", "已经存在");
				} else {
					submitAddGroup();
				}
			} else {
				setErrorMsg("addErrorMsg", "添加失败");
			}
			commitEnd();
		},
		error : function(result) {
			console.log(result);
			commitEnd();
		}
	});

	function submitAddGroup() {
		var json = {
			"groupid" : groupid,
			"ldapkey": ldapkey,
			"appids" : appids
		};
		var postUrl = url + "addGroup";
		AjaxHelper.call({
			url : postUrl,
			data : JSON.stringify(json),
			async : true,
			cache : false,
			type : "POST",
			dataType : "json",
			success : function(result) {
				result = eval("(" + result["rs"] + ")");
				if (result.code == "1") {
					$("#addGroupDiv").modal('hide');
					loadAllGroups_RESTClient();
				} else {
					setErrorMsg("addErrorMsg", "添加失败");
				}
				commitEnd();
			},
			error : function(data) {
				console.log(data);
				commitEnd();
			}
		});
	}

}

function loadAllGroups_RESTClient() {
	
	var getPagingInfo = table.getPagingInfo();
	var inputValue = $("#groupManagerHeadSearchBarSearchHidden").val();
	$("#groupManagerHeadSearchBarSearch").val(inputValue);

	var json = {
		"groupid" : inputValue,
		"pageindex" : getPagingInfo.pageNum,
		"pagesize" : getPagingInfo.pageSize
	};
	var postUrl = url + "loadAllGroups";
	AjaxHelper.call({
		url : postUrl,
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
					loadAllGroups_RESTClient();
				}else{
					loadAllGroups_Count_RESTClient(result.data);	
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


function loadAllGroups_Count_RESTClient(groupDatas) {
	
	var inputValue = $("#groupManagerHeadSearchBarSearch").val();

	var json = {
		"paramVlue" : inputValue,
		"type"    : "GROUP"
	};
	var postUrl = url + "loadCount";
	AjaxHelper.call({
		url : postUrl,
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
				$.each(groupDatas, function(index, obj) {
					table.add(obj);
				});
				//页面全部变量赋值
				mtableConfig.tableListData=groupDatas;
			} else {
				console.log(result);
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}

function delGroup_RESTClient(id) {
	var json = {
			"id" : id
		};
	 
	$.each(mtableConfig.tableListData, function(index, obj) {

		if(obj["_id"]==id){ 
			json["groupid"]=obj["groupid"];
			json["ldapkey"]=obj["ldapkey"]==null?"":obj["ldapkey"];
		}
	}); 
	
	var postUrl = url + "delGroup";
	AjaxHelper.call({
		url : postUrl,
		data : JSON.stringify(json),
		async : true,
		cache : false,
		type : "POST",
		dataType : "json",
		success : function(result) {
			result = eval("(" + result["rs"] + ")");
			if (result.code == "1") {
				loadAllGroups_RESTClient();
			} else {
				console.log(result)
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}

function loadGroupById_RESTClient(id) {
	var json = {
			"id" : id
	};
	var postUrl = url + "loadGroupByid";
	AjaxHelper.call({
		url : postUrl,
		data : JSON.stringify(json),
		async : true,
		cache : false,
		type : "POST",
		dataType : "json",
		success : function(result) {
			result = eval("(" + result["rs"] + ")");
			if (result.code == "1") {
				showViewGroup(result);
			} else {
				console.log(result)
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}

function updateGroup_RESTClient(id, groupId,ldapKey,appids) {
	var json = {
			"id" : id,
			"groupid":groupId,
			"ldapkey":ldapKey,
			"appids" : appids
	};
	var postUrl = url + "updateGroup";
	AjaxHelper.call({
		url : postUrl,
		data : JSON.stringify(json),
		async : true,
		cache : false,
		type : "POST",
		dataType : "json",
		success : function(result) {
			result = eval("(" + result["rs"] + ")");
			if (result.code == "1") {
				$("#viewGroupDiv").modal('hide');
				loadAllGroups_RESTClient();
			} else {
				console.log(result);
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}
