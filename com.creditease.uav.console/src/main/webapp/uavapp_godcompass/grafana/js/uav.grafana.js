var grafanaUrl = "";

function autoLogin(){
	//获取用户信息
	AjaxHelper.call({
		url : "../../rs/godeye/filter/group/user/q",
		async : true,
		cache : false,
		type : "GET",
        dataType: 'html',
		success : function(result) {
			register(result);
		},
		error : function(result) {
			showBodyMsg(result);
		}
	});
	
	//用户注册
	function register(userInfo){		
		PageHelper.showLoading();
		AjaxHelper.call({
			url : "../../rs/grafana/register",
			data : userInfo,
			async : true,
			cache : false,
			type : "POST",
			dataType : "json",
			success : function(result) {
				if(result.code=="success"){
					grafanaLogin(result);	
				}else{
					showBodyMsg(JSON.stringify(result));
				}
			},
			error : function(result) {
				showBodyMsg(result);
			}
		});
		
	}
	
	
	//跨域登录
	function grafanaLogin(loginInfo){
		  var postUrl = loginInfo.data.url+"/login";
		  var iframeid = "appCrossdomainIframe";
		  var form =$("<form action='"+postUrl+"' contentType='application/json' method='post' >" +
			        "<input type='hidden' name='user' value='"+loginInfo.data.loginid+"'/> " +
			        "<input type='hidden' name='password' value='"+loginInfo.data.password+"'/> " +
			        "</form> ");
		  
		  $("#"+iframeid).remove();
		  $("body").append("<iframe id='"+iframeid+"' name='"+iframeid+"' style='display: none'></iframe>");
		  $("#"+iframeid).contents().find('body').html(form);
		  $("#"+iframeid).contents().find('form').submit();

		  //如果有组织，默认跳转到一个组织，否则直接登录
		  var switchOrgId = loginInfo.data.switchOrgId;
		  if("null" == switchOrgId || null == switchOrgId){
			  grafanaUrl = loginInfo.data.url;
		  }else{
			  grafanaUrl = loginInfo.data.url+"/profile/switch-org/"+switchOrgId;
		  }
		 
		  setTimeout(function(){grafanaJumpUrl();},300);
	}
	
}

/**
 * helper api :实现temp.js中的功能（代码一样） begin
 */
function grafanaJumpUrl(){
	window.frames.location.href=grafanaUrl;
}

/**
 * helper api :实现temp.js中的功能（代码一样） end
 */

function showBodyMsg(obj){
	console.log(obj);
	$("#bodyMsg").text(obj);
}


$(document).ready(function(){
	autoLogin();
});