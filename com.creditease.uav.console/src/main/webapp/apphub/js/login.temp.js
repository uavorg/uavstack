document.onkeydown = function() {
	var event = arguments[0] || window.event;
	var currentKey = event.charCode || event.keyCode;
	if (event.keyCode == 13) {
		login_RSClient();
	}
};
$("#loginId").focus();

var captchaCfg = {
		url : WebHelper.proNameUrl()+"/rs/gui/vc/new",
		appendId : "loginCaptchDiv",
		placeholder:"验证信息"
		
}

$(function(){
	window["captcha"].build(captchaCfg);
});

function showLoginErrorMsg(msg){
	$("#loginErrorMsg").html(msg);
    $("#loginErrorMsg").show();
}

function hideLoginErrorMsg(){
	$("#loginErrorMsg").html("");
    $("#loginErrorMsg").hide();
}