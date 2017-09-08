var url = "../../rs/manage/";

function loadInfoByEmail_RESTClient(type) {
	
	if(!check()){
		queryInfoResult({"提示":"输入为空"});
		return;
	}
	
	var postUrl = url + "loadInfoByEmail";
	AjaxHelper.call({
		url : postUrl,
		data : getParam(),
		async : true,
		cache : false,
		type : "POST",
		dataType : "json",
		success : function(result) {
			queryInfoResult(result);
		},
		error : function(result) {
			console.log(result);
		}
	});
	
	function check(){
		var check = false;
		if($.trim($("#emailName_Input").val())){
			check = true;
		}
		return check;
	}
	
	function getParam(){
		var input = {
				"value":$.trim($("#emailName_Input").val()),
				"type":type
		};
		return JSON.stringify(input);
	}
}
