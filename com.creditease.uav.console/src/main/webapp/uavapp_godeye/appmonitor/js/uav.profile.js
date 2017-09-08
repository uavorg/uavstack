/**
 * 发现profile数据经常被共同使用，就提取一个公共
 */
function UAVProfileDAO() {

	/**
	 * callAppProfile
	 */
	this.callAppProfile=function(callbackFunction,callbackError) {
		
		AjaxHelper.call({
            url: "../../rs/godeye/profile/q/cache",
            cache: false,
            type: 'GET',
            dataType: 'html',
            timeout: 5000,
            success: function(data){
            	var jsonData = eval("(" + data + ")");
            	data= jsonData["rs"];
				 jsonData = eval("(" +data + ")");
				 
				 console.log("AppProfile>> %o", jsonData);
				 
				 if (callbackFunction!=undefined) {
					 callbackFunction(jsonData);
				 }
            },
            error: function(result){
            	if (callbackError!=undefined) {
            		callbackError(result);
            	}            	
            }
        });
	};
	
	/**
	 * 为selector联动收集profile相关信息
	 */
	this.loadAppSelector=function(jsonData,collectInfoMapIds,collectFunction) {
		
		var appSet=new Map();
		
		//应用集群列表显示
		var datas=[];
		
		//收集哪些联动信息
		var collectInfo={};
		
		for(var i=0;i<collectInfoMapIds.length;i++) {
			collectInfo[collectInfoMapIds[i]]=new Map();
		}
		
		for(var key in jsonData) {
			
			if (key.indexOf("jse")>-1) {
				continue;
			}
			
			var data=eval("("+jsonData[key]+")");
			
			/**
			 * 应用集群
			 */
			var appid=data["appid"];
			
			if (!appSet.contain(appid)) {
				
				//appid
				var appname=data["appname"];
				
				var option={};
				
				option["value"]=appid;
				option["title"]=(appname!=undefined&&appname!="")?appname:appid;
				
				appSet.put(appid,"");
				
				if (datas.length==0) {
					option["select"]=true;
				}
				
				datas[datas.length]=option;
			}
			
			//联动信息收集
			for(var key in collectInfo) {
				var cMap=collectInfo[key];
				if (collectFunction!=undefined) {
					collectFunction(key,cMap,data);
				}
			}
		}
		
		return {options:datas,info:collectInfo};
	};
	
	/**
	 * 获取appuuid
	 */
	this.getAppUUID=function(jsonObj) {
		
		var isJse=this.isJSE(jsonObj.appurl);
		
		if (isJse==true) {
			appInstMOId=this.formatAppURL(jsonObj.appurl);
		}
		else {
			appInstMOId=this.formatAppURL(jsonObj.appurl)+"---"+jsonObj.appid;
		}
		return appInstMOId;
	};
	
	/**
	 * isJSE
	 */
	this.isJSE=function(tag) {
		
		if (tag==undefined) {
			return "";
		}
		//http is for JEE Application, config is for MSCP Application
		return (tag.indexOf("jse:")>-1||(tag.indexOf("http")==-1&&tag.indexOf("---")==-1&&tag.indexOf("config")==-1))?true:false;
	};
	
	/**
	 * 标准化appurl
	 */
	this.formatAppURL=function(appurl) {
		
		if (appurl==undefined) {
			return appurl;
		}
		 
		 var appinfo=appurl.split("/");
		 
		 if (appinfo.length>4) {
			 return appurl.substring(0,appurl.length-1); 
		 }
		 else {
			 return appurl;
		 }
	}
}