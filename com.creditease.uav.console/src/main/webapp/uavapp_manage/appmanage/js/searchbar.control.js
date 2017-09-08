var hash=0;

function showErrMsg(val, selector){
	if(selector == 1){
		var v = document.getElementById("errorMsg");
		v.style.display="";
		v.innerText = val;
	}else if(selector == 2){
		var v = document.getElementById("errorMsgEdit");
		v.style.display="";
		v.innerText = val;
	}

};

function searchBarAddEvent(){
	InitRegisterForm();
	commitInit();
	$('#AppRegisterModal').modal({backdrop: 'static', keyboard: false});
};

function clickSave(id) {
	var appurl = document.getElementById("AddUrlAppUrl").value;
	showErrMsg("",1);
	appurl = HtmlHelper.inputXSSFilter(appurl);
	var urlCheck = checkURL(appurl);
	switch (urlCheck) {
	case 1:
		showErrMsg("访问路径为空", 1);
		break;
	case 2:
		showErrMsg("访问路径请以http或https开始", 1);
		break;
	case 3:
		showErrMsg("访问路径没有IP或域名", 1);
		break;
	case 4:
		showErrMsg("访问路径没有以uavapp_开始的应用", 1);
		break;
	case 0:
		break;
	}

	if(urlCheck != 0){
		return;
	}
	
	var configpath = document.getElementById("AddUrlAppConfigPath").value;
	configpath = HtmlHelper.inputXSSFilter(configpath);
	var configCheck = checkURL(configpath);
	switch (configCheck) {
	case 2:
		showErrMsg("配置路径路径请以http或https开始", 1);
		break;
	case 3:
		showErrMsg("配置路径没有IP或域名", 1);
		break;
	case 4:
		showErrMsg("配置路径没有以uavapp_开始的应用", 1);
		break;
	case 0:
		break;
	}

	if (urlCheck == 0 && (configCheck == 0 || configCheck==1)) {
		var appid = hashCode(appurl, 6, 10);
		addApp_RESTClient(appid, appurl, configpath);
	}
}

function clickEdit(id){
	$("#RefreshButton").hide();
	var text = document.getElementById("AppEditURL");
	var val = document.getElementById(id);
	var textArea =document.getElementById("AppEditURL");
	
	if(val.value == "edit"){
		val.value = "save";
		val.innerHTML= "保存";
		var textArea =document.getElementById("AppEditURL");
		textArea.removeAttribute("disabled");
		var confPObj =document.getElementById("AppEditConfigPath");
		confPObj.removeAttribute("disabled");
	} else if (val.value == "save") {
		var newUrl = HtmlHelper.inputXSSFilter(textArea.value);

		var urlCheck = checkURL(newUrl);
		switch (urlCheck) {
		case 1:
			showErrMsg("访问路径为空", 2);
			break;
		case 2:
			showErrMsg("访问路径请已http或https开始", 2);
			break;
		case 3:
			showErrMsg("访问路径没有IP或域名", 2);
			break;
		case 4:
			showErrMsg("应用名请以uavapp_开始", 2);
			break;
		case 0:
			break;
		}
		
		if(urlCheck != 0){
			return;
		}
		
		var configpath = document.getElementById("AppEditConfigPath").value;
		configpath = HtmlHelper.inputXSSFilter(configpath);
		var configCheck = checkURL(configpath);
		switch (configCheck) {
		case 2:
			showErrMsg("配置路径路径请以http或https开始", 2);
			break;
		case 3:
			showErrMsg("配置路径没有IP或域名", 2);
			break;
		case 4:
			showErrMsg("配置路径没有以uavapp_开始的应用", 2);
			break;
		case 0:
			break;
		}

		if (urlCheck == 0 && (configCheck == 0 || configCheck == 1)) {
			val.value = "edit";
			val.innerHTML = "编辑";
			textArea.setAttribute("disabled", "true");
			var headStr = HtmlHelper.inputXSSFilter(document
					.getElementById("AppEditId").innerHTML);
			modifyApp_RESTClient(headStr, newUrl,configpath);
		}
	}
};

function onEditUrl(){
	var h = document.getElementById('AppEditId');
	var appEidtRealURLLablel = document.getElementById("AppEditRealURL");
	var appEditURL = document.getElementById("AppEditURL");
	//console.log(appEditURL.value);
	appEidtRealURLLablel.innerText = appEditURL.value + "/uavapp_"+h.innerHTML;
	InitErrMsg();
};

function refreshAppById(){
	var textArea1 = document.getElementById("AppEditURL");
	var textArea2 = document.getElementById("AppEditConfigPath");
	var newUrl = HtmlHelper.inputXSSFilter(textArea1.value);
	var configPath = HtmlHelper.inputXSSFilter(textArea2.value);
	var headStr = HtmlHelper.inputXSSFilter(document.getElementById("AppEditId").innerHTML);
	modifyApp_RESTClient(headStr, newUrl,configPath);
};
