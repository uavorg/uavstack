
var modalConfig = {
	head:"用户输入标题",
	content:"用户输入验证信息",
	callback:"clickSureCallback()"
};

function clickSureCallback(){
	console.log('adfadfadf');
};

$("document").ready(function(){
	showConfirm(modalConfig);
});
	
	
	
