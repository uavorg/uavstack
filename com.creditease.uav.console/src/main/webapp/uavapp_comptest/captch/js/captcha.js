var cObj = {
		url : WebHelper.proNameUrl()+"/rs/gui/vc/new",
		appendId : "captchDiv"
}

$(function(){
	window["captcha"].build(cObj);
});

function getAnswer(){
	$("#answer").html(window["captcha"].answer());
}