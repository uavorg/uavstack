var  restfulUrl = "rs/gui/";
var uavGuiCacheCfg={type:"local"},uavGuiCKey="apphub.gui.cache.win.";
window["cachemgr"].init(uavGuiCacheCfg);
//获取模板信息
function loadTemp_RSClient(tempName) {
	
	var html = window["cachemgr"].get(uavGuiCKey + "temp."+tempName);
	if (html) {
		reloadSpaContent(html);
	} else {
		var url = restfulUrl + "loadTemp";
		AjaxHelper.call({
			url : url,
			data : {
				tempName : tempName
			},
			async : true,
			cache : false,
			type : "GET",
			dataType : "html",
			success : function(result) {
				result = eval("(" + result + ")");
				if (result.CODE == "SUCCESS") {
					window["cachemgr"].put(uavGuiCKey + "temp."+tempName,result.DATA);
					reloadSpaContent(result.DATA);
				}
			},
			error : function(result) {
				if (result.responseText.indexOf("请确认登录是否正常") > -1) {
					window.location.href = "main.html";
					return;
				}
				reloadErrorContent("loadTemp_RSClient", result);
			},
		});
	}

}


// 登录
function login_RSClient() {
    var loginId = $("#loginId").val();
    var loginPwd = $("#loginPwd").val();
    var captcha = window["captcha"].answer();
    if(!checkLogin()){
        return;
    }

    AjaxHelper.call({
        url: restfulUrl +"login",
        data: JSON.stringify({"loginId":loginId,"loginPwd":loginPwd,"captcha":captcha}),
        async:true,
        cache:false,
        type: "POST",
        contentType:'application/json; charset=UTF-8',
        dataType: "html",
        success: function (result) {
            result = eval("("+result+")");
            if(result.CODE == "SUCCESS"){
            	hideLoginErrorMsg();
                loadTemp_RSClient("main");
            }else{
            	window["captcha"].refresh();
            	showLoginErrorMsg(result.MSG);
            }

        },
        error:function(result){
            reloadErrorContent("login_RSClient",result);
        }
    });
    
    function checkLogin(){
        if(!loginId){
        	$("#loginId").focus();
        	showLoginErrorMsg("请输入账号");
            return false;
        }else if(!loginPwd){
        	$("#loginPwd").focus();
        	showLoginErrorMsg("请输入密码");
            return false;
        }else if(!captcha){
        	window["captcha"].focus();
        	showLoginErrorMsg("请输入验证信息");
            return false;
        }else{
        	hideLoginErrorMsg();
            return true;
        }
    }

}

//获取主页菜单
function loadMainMenu_RSClient() {
	var html = window["cachemgr"].get(uavGuiCKey + "menu.main");
	if (html) {
		var resultObj = eval("(" + html + ")");
		loadMenuView(resultObj);
	} else {

		AjaxHelper.call({
			url : restfulUrl + "loadMainMenu",
			data : {},
			async : true,
			cache : false,
			type : "GET",
			dataType : "html",
			success : function(result) {
				var resultObj = eval("(" + result + ")");
				if (resultObj.CODE == "SUCCESS") {
					window["cachemgr"].put(uavGuiCKey + "menu.main",result);
					loadMenuView(resultObj);
				}
			},
			error : function(result) {
				if (result.responseText.indexOf("请确认登录是否正常") > -1) {
					window.location.href = "main.html";
					return;
				}
				reloadErrorContent("loadMainMenu_RSClient", result);
			}
		});
	}
}

// 获取用户管理信息
function loadUserManageInfo_RSClient() {
	var uInfo = window["cachemgr"].get(uavGuiCKey + "user.manage.info");
	if (uInfo) {
		initMainAppIco(uInfo);
	} else {

		AjaxHelper.call({
			url : restfulUrl + "loadUserManageInfo",
			data : {},
			async : true,
			cache : false,
			type : "GET",
			dataType : "html",
			success : function(result) {
				if (result) {
					window["cachemgr"].put(uavGuiCKey + "user.manage.info",result);
					initMainAppIco(result);
				}

			},
			error : function(result) {
				if (result.responseText.indexOf("请确认登录是否正常") > -1) {
					window.location.href = "main.html";
					return;
				}
				reloadErrorContent("loadUserManageInfo_RSClient", result);
			},
		});

	}
}

function initMain() {
	
	if (HtmlHelper.isIE()==true) {
		alert("很抱歉，发现您正在使用IE浏览器，请更换成其他现代浏览器！");
	}
	else {
		var tempName = "login";
		var userInfo = window["cachemgr"].get(uavGuiCKey + "user.manage.info");
		if(null != userInfo){
			tempName = "main";
		}
		//后台校验会话
		if(tempName == "main"){
			guiPing_RSClient(loadTemp_RSClient(tempName));
		}else{
			loadTemp_RSClient(tempName);
		}
		
	}
}

function initApp() {
	if (HtmlHelper.isIE()==true) {
		alert("很抱歉，发现您正在使用IE浏览器，请更换成其他现代浏览器！");
	}
	else {
		var userInfo = window["cachemgr"].get(uavGuiCKey + "user.manage.info");
		if(null == userInfo){

			window.location.href = "main.html";
		}else{
			loadTemp_RSClient("app");
		}
	}
}


/**
 * ping一下请求，看是否session校验通过
 * @param param
 */
function guiPing_RSClient(func,type,url,desc) {
	
	var ajaxUrl = "ping";
	if(type){
		/**
		 * type : 决定后台是否check这次操作进行logger输出
		 * type ： jumpapp
		 */
		ajaxUrl +="?urltype="+type+"&url="+url+"&desc="+encodeURI(encodeURI(desc), "utf-8");
	}
	
	 AjaxHelper.call({
	        url: ajaxUrl ,
	        data: {},
	        async: true,
	        cache: false,
	        type: "GET",
	        dataType: "html",
	        success: function (result) {
	        	func;
	        },
	        error: function (result) {
	            reloadErrorContent("guiPing_RSClient",result);
	        },
	    });
}


/**
 * 获取apphub信息（信息经过加密）
 * 同步方法，调用后会将结果写在window
 */
function loadApphubInfoByAES_RSClient() {
	
	 AjaxHelper.call({
	        url: restfulUrl +"loadApphubInfoByAES" ,
	        data: {},
	        async: false,
	        cache: false,
	        type: "GET",
	        dataType: "html",
	        success: function (result) { 
				var resultObj = eval("(" + result + ")");
				if (resultObj.CODE == "SUCCESS") {
		        	window["apphubAesInfo"] = resultObj.DATA;
				}
	        },
	        error: function (result) {
	            reloadErrorContent("loadApphubInfoByAES_RSClient",result);
	        },
	    });
}

function loginOut_RSClient(){
	 AjaxHelper.call({
	        url: restfulUrl +"loginOut",
	        data: {},
	        async: true,
	        cache: false,
	        type: "POST",
	        dataType: "html",
	        success: function (result) {
	       	 	window.localStorage.clear();
	       	 	window.sessionStorage.clear();
	        	loadTemp_RSClient('loginOut');
	        },
	        error: function (result) {
	            reloadErrorContent("loginOut_RSClient",result);
	        },
	    });
	
}

/**
 * 重新加载SPA模板域
 * @param html     :innerHTML
 */
function reloadSpaContent(html){
    $("#spaContent").empty();  //模板域
    $("#spaContent").html(html);
}



/**
 * 重新加载指定模板域
 * @param id
 * @param html     :innerHTML
 */
function reloadContent(id,html){
    $("#"+id).empty();  //模板域
    $("#"+id).html(html);
}



/**
 * 重新加载ERROR错误信息，显示至SPA模板域
 * @param functionName     : error function name
 * @param data             : error obj
 */
function reloadErrorContent(functionName,data){
	var errorMsg = new StringBuffer();
    errorMsg.append("<div style='background-color: #f2dede;margin: 10px;padding: 10px;border:1px solid'>");
    errorMsg.append("<strong>错误:");
    errorMsg.append(data.status);
    errorMsg.append("</strong><br/>");
    errorMsg.append("<span class='btn btn-success' onclick='javascript:window.location.href=\"main.html\";'>返回登录页面</span>");
    errorMsg.append("<div style=\"display:none;\">" );
    errorMsg.append("<hr style='color: white'/>" );
    errorMsg.append("error funciton ："+functionName+"<br/>error message :<br/>"+StringHelper.obj2str(data.responseText));
    errorMsg.append("</div>");
    errorMsg.append("</div>");
    /**
     * 此处console.log：用于调试排查。勿删
     */
    console.log(functionName+"\n"+StringHelper.obj2str(data));
    reloadSpaContent(errorMsg.toString());
}
