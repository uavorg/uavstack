var url = "../../rs/godeye/notify/";

function loadNotify_RestfulClient(){
	var urlAdd;
	urlAdd = "q/hm";
	if(isBSEject){
		urlAdd="q/best/hm";
		//console.log(urlAdd);
	}
	//urlAdd = "q/hm";
	AjaxHelper.call({
		url : url + urlAdd,
		data : getPageParams(),
		async : true,
		cache : false,
		type : "POST",
		dataType : "html",
		success : function(result) {
			var jsonData = eval("(" + result + ")");
			loadCountNotify_RestfulClient(eval("(" +jsonData["rs"]+ ")"));
		},
		error : function(result) {
			console.log(result);
		}
	});
	
}

function loadCountNotify_RestfulClient(rows){
	var urlAdd;
	urlAdd = "q/count/hm";
	if(isBSEject){
		urlAdd="q/best/count/hm";
	}
	//urlAdd = "q/count/hm";
	AjaxHelper.call({
		url : url + urlAdd,
		data : getPageParams(),
		async : true,
		cache : false,
		type : "POST",
		dataType : "html",
		success : function(result) {
			var jsonData = eval("(" + result + ")");
			var count = eval("(" +jsonData["rs"]+ ")")[0]["count"];
			loadListData(rows,count);
		},
		error : function(result) {
			console.log(result);
		}
	});
}

function updateNotify_RestfulClient(paramObject){
	AjaxHelper.call({
		url : url + "update/hm",
		data : JSON.stringify(paramObject),
		async : true,
		cache : false,
		type : "POST",
		dataType : "html",
		success : function(result) {
			if("T"==result){
				loadNotifyDesc_RestfulClient(paramObject);
			}else{
				loadDescDiv("descDiv",null);
			}
			
		},
		error : function(result) {
			console.log(result);
		}
	});
}

function loadNotifyDesc_RestfulClient(paramObject){
	AjaxHelper.call({
		url : url + "q/desc/hm",
		data : JSON.stringify(paramObject),
		async : true,
		cache : false,
		type : "POST",
		dataType : "html",
		success : function(result) {
			var jsonData = eval("(" + result + ")");
			loadDescDiv("descDiv",jsonData);
		},
		error : function(result) {
			console.log(result);
		}
	});
}

function getNotifyEventList_RestfulClient(){
	AjaxHelper.call({
		url : url + "q/event/hm",
		async : true,
		cache : false,
		type : "POST",
		dataType : "html",
		success : function(result) {
			var jsonData = eval("(" + result + ")");
			var list = eval("(" +jsonData["rs"]+ ")");
			addNotifyEvent(list[0]["result"]);
		},
		error : function(result) {
			console.log(result);
		}
	});
}
