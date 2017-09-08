function InitErrMsg(){
	var v = document.getElementById("errorMsg");
	v.innerText = "";
	v = document.getElementById("errorMsgEdit");
	v.innerText = "";
};

function InitRegisterForm(){
	//var appIdLablel = document.getElementById("AppIdLabel");
	//var appDirLablel = document.getElementById("AppDirLabel");
	//var accessPathLabel = document.getElementById("AccessPathLabel");
	var sel = document.getElementById("AddUrlAppUrl");
	
	InitErrMsg();
	//appIdLablel.innerText = "";
	//accessPathLabel.innerText = "";
	//appDirLablel.innerText = "";
	sel.value = "";
	$("#AddUrlAppConfigPath").val("");
};

function InitEditModalForm(){
	var textArea =document.getElementById("AppEditURL");
	textArea.setAttribute("disabled","true");
	var confPObj =document.getElementById("AppEditConfigPath");
	confPObj.setAttribute("disabled","true");
	if(confPObj.value == undefined || "undefined" == confPObj.value){
		confPObj.value="";
	}
	var val = document.getElementById("EditSaveButton");
	val.value = "edit";
	val.innerHTML= "编辑";
	InitErrMsg();
};

var iscommit = true;
function commitInit(){
    iscommit=true;
}

function checkURL(url){
	var regexIp = /((([1-9]?|1\d)\d|2([0-4]\d|5[0-5]))\.){3}(([1-9]?|1\d)\d|2([0-4]\d|5[0-5]))/;
	var regexDomain  = /[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z][-a-zA-Z]{0,62})+\.?/;
	var checkStr = "uavapp_";

	if(!url){
		return 1;		
	}
//	else if(url.indexOf("http://") == -1 && url.indexOf("https://") == -1){
//		return 2;
//	}else if(!regexIp.test(url) && !regexDomain.test(url)){
//		return 3;
//	}
	else if(url.indexOf(checkStr) == -1){
		return 4;
	}else if(url.indexOf(checkStr)!=-1 && (url.substring(url.indexOf(checkStr)+checkStr.length).length ==0)){
		return 4;
	}else{
		return 0;
	}	
};




