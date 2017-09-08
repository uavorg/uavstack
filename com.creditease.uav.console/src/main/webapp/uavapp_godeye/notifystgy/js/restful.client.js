var url = "../../rs/godeye/notify/";

function searchbtn(){
	list.setPageNum(1);

	var input = $("#searchInput").val();
	$("#searchInput_Hidden").val(input);
	loadNotifyStgy_RestfulClient();
}
function searchbtnAll(){
	
	$("#searchInput_Hidden").val("");
	$("#searchInput").val("");
	searchbtn();
}

function loadNotifyStgy_RestfulClient(){
	var getPagingInfo = list.getPagingInfo();
	AjaxHelper.call({
		url : url + "q/stgy/hm",
		data : getParam(),
		async : true,
		cache : false,
		type : "POST",
		dataType : "html",
		success : function(result) {

			var resultObj = eval("("+result+")");
			var jsons = resultObj.rs;
			//当前页数据为空，自动退一页
			if(Object.keys(jsons).length==0 && getPagingInfo.pageNum >1){
				list.setPageNum(--getPagingInfo.pageNum);
				loadNotifyStgy_RestfulClient();
			}else{
				//清空数据
				list.clearTable();
				//必须先显示分页
				list.setTotalRow(resultObj.count);
				list.renderPagination();
				//然后添加数据
				$.each(jsons, function(index, obj) {
					var json = JSON.parse(obj);
					var addObj = new Object();
					addObj["key"]=index;
					addObj["keyFormat"]=fotmatIndex(index,json);
					addObj["desc"]=json.desc;
					addObj["owner"]=json.owner;
					addObj["uptime"]=TimeHelper.getTime(json.uptime,"FMS");
					list.add(addObj);
				});
			}
			
		},
		error : function(result) {
			console.log(result);
		}
	});
	
	function getParam(){
		var params = {
				"inputValue": $("#searchInput_Hidden").val(),
				"pageindex" : getPagingInfo.pageNum,
				"pagesize" : getPagingInfo.pageSize
		}
		$("#searchInput").val(params.inputValue);
		return JSON.stringify(params);
	}
	
	function fotmatIndex(value,json){
		
		var result = new StringBuffer();
		var names = value.split("@");
		var a = names[0]==undefined?"":names[0];
		var b = names[1]==undefined?"":names[1];
		var c = names[2]==undefined?"":names[2];
		var existsIst = json.instances.length>0?true:false;
		
		//1
		if(b == "log"){
			result.append("<div class='listIndex listIndex_log'>");
			result.append("日志");
		}else if(a == "server" && b == "jvm"){	
			result.append("<div class='listIndex listIndex_appmetrics'>");
			result.append("自定义指标");
		}else if(a == "server"){
			result.append("<div class='listIndex listIndex_server'>");
			result.append("服务端");
		}else if(a == "client"){
			result.append("<div class='listIndex listIndex_client'>");
			result.append("客户端");
		}
		result.append("<hr/>");
		result.append("</div>");
		
		result.append("<div class='listIndex'>");
		//2
		if(b == "log"){
			result.append("应用ID:");
			result.append(a);
		}else if(b.length>0){
			result.append(getSelUiConfigValue(b));
		}
		result.append("<hr/>");
		//3
		if(c.length>0 && b!="log" && existsIst){
			result.append("实例组:");
			result.append(c);
		}else if(c.length>0 && b!="log" && !existsIst){
			result.append("实例:");
			result.append(c);
		}else if(c.length>0 && b=="log"){
			result.append("指定日志:");
			result.append(c);
		}else if(c.length==0 && b=="log"){
			result.append("全部日志");
		}else{
			result.append("全部实例");
		}

		result.append("</div>");
		return result.toString();
	}
}

function getNotifyStgy_RestfulClient(key){
	AjaxHelper.call({
		url : url + "get/stgy/hm",
		data : key,
		async : true,
		cache : false,
		type : "POST",
		dataType : "html",
		success : function(result) {
		
			/**
			 * 对旧数据兼容 begin
			 */
			var changeData = {}, result= JSON.parse(result);
			
			$.each(result,function(key,objValue){
				var vObj = JSON.parse(objValue);
				if(vObj.action){
					/**
					 * 清洗触发动作数据
					 */
					$.each(vObj.action,function(actionKey,actionValue){
						if(actionValue.indexOf("[")<0){
							/**
							 * 旧数据兼容
							 */
							var arry=[];
							arry[0]=actionValue;
							vObj.action[actionKey]=arry;
						}else{
							/**
							 * 新数据因为是：string里面的数组，因此过滤
							 */
							vObj.action[actionKey]=JSON.parse(actionValue);	
						}
							
					});
				}
				changeData[key]=vObj;
			});
			
			/**
			 * 对旧数据兼容 end
			 */
			showEditDivIsReadOnly(changeData);			
		},
		error : function(result) {
			console.log(result);
		}
	});
}

/**
 * 校验修改预警策略：存在则不提交
 * @param key
 */
function checkUpNotifyStgy_RestfulClient(key,commitInfo){
	AjaxHelper.call({
		url : url + "get/stgy/hm",
		data : key,
		async : true,
		cache : false,
		type : "POST",
		dataType : "html",
		success : function(result) {
			if(result=="{}"){
				updateNotify_RestfulClient(commitInfo);
			}else{
				add_IsExistErrorMSG();
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}


function updateNotify_RestfulClient(json){
	AjaxHelper.call({
		url : url + "up/stgy/hm",
		data : JSON.stringify(json),
		async : true,
		cache : false,
		type : "POST",
		dataType : "html",
		success : function(result) {
			if("T"==result){
				closeObjectDiv();
				loadNotifyStgy_RestfulClient();
			}else{
				console.log("添加/修改:失败");
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}

function removeNotify_RestfulClient(key){
	AjaxHelper.call({
		url : url + "del/stgy/hm",
		data : key,
		async : true,
		cache : false,
		type : "POST",
		dataType : "html",
		success : function(result) {
			if("T"==result){
				loadNotifyStgy_RestfulClient();
			}else{
				console.log("删除:失败");
			}
		},
		error : function(result) {
			console.log(result);
		}
	});
}