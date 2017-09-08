/**
 * 窗口管理系统
 */
function AppHubWindow() {
	
	var winObj={
		id:"",
		title:"",
		order:999,
		theme:"BGDark",
		state:{
			isHide:true
		},
		events:{
			onresize:function(w,h) {
				
			}
		}
	};
	
	this.build=function(_winObj) {
		
		JsonHelper.merge(winObj, _winObj,true);
		
		var sb=new StringBuffer();
		
		sb.append("<div id='"+winObj["id"]+"' class='AppHubWindow "+winObj["theme"]+"' style='z-index:"+winObj["order"]+"'>");
		
		sb.append("</div>");
		
		document.body.innerHTML+=sb.toString();
		
		var ws=HtmlHelper.body("clientWidth")+"px";
		
		HtmlHelper.css(winObj["id"], {width:ws});
		
		var top=0;
		if (undefined!=winObj["top"]) {
			top=winObj["top"];
			HtmlHelper.css(winObj["id"], {top:top+"px"});
		}
		
		var height=winObj["height"];
		var noTopHeight;
		if (undefined!=height&&height.indexOf("auto")>-1) {
			var hparam=height.split(":");
			
			var detaH=0;
			if (hparam.length==2) {
				detaH=parseInt(hparam[1]);
			}
			noTopHeight=HtmlHelper.body("clientHeight")-top+detaH;
			var hs=noTopHeight+"px";
			HtmlHelper.css(winObj["id"], {height:hs});
		}
		
		if (undefined!=winObj["background"]) {
			HtmlHelper.css(winObj["id"], {background:winObj["background"]});
		}
		
		if (undefined!=winObj["overflow"]) {
			HtmlHelper.css(winObj["id"], {"overflow":winObj["overflow"]});
		}
		else {		
			if (undefined!=winObj["overflow-y"]) {
				HtmlHelper.css(winObj["id"], {"overflow-y":winObj["overflow-y"]});
			}
			if (undefined!=winObj["overflow-x"]) {
				HtmlHelper.css(winObj["id"], {"overflow-x":winObj["overflow-x"]});
			}		
		}		
	};
	
	this.content=function(content) {
		
		if (typeof content == "string") {
			HtmlHelper.id(winObj["id"]).innerHTML=content;
		}
		else {
			HtmlHelper.id(winObj["id"]).appendChild(content);
		}
	};
	
	this.get=function(key) {
		return winObj[key];
	};
	
	this.resize=function(w,h) {
		var ws=(w<=0)?"auto":w+"px";
		
		HtmlHelper.css(winObj["id"], {width:ws});
		
		var top=0;
		if (undefined!=winObj["top"]) {
			top=winObj["top"];
		}
		
		var height=winObj["height"];
		var noTopHeight;
		if (undefined!=height&&height.indexOf("auto")>-1) {
			var hparam=height.split(":");
			
			var detaH=0;
			if (hparam.length==2) {
				detaH=parseInt(hparam[1]);
			}
			
			noTopHeight=h-top+detaH;
			var hs=noTopHeight+"px";
			HtmlHelper.css(winObj["id"], {height:hs});
		}
		
		if (winObj.events["onresize"]!=undefined) {
			winObj.events["onresize"](w,h,noTopHeight);
		}
	};
	
	this.show=function() {
		winObj.state.isHide=false;
		HtmlHelper.css(winObj["id"], {display:"block"});

		if (winObj.events["onresize"]!=undefined) {
			
			var top=0;
			if (undefined!=winObj["top"]) {
				top=winObj["top"];
			}
			var h=parseInt(HtmlHelper.body("clientHeight"));
			
			winObj.events["onresize"](parseInt(HtmlHelper.body("clientWidth")),h,h-top);
		}
	};
	
	this.hide=function() {
		winObj.state.isHide=true;
		HtmlHelper.css(winObj["id"], {display:"none"});
	};
	
	this.isHide=function() {
		return winObj.state.isHide;
	}
}

/**
 * 窗口管理器
 */
function AppHubWindowManager() {
	
	var windows=new Map();
	//build a window
	this.build=function(_winObj) {
		
		var win=new AppHubWindow();
		win.build(_winObj);
		windows.put(win.get("id"), win);
	};
	//show window: build first
	this.show=function(id) {
		
		if (!windows.contain(id)) {
			return;
		}
		
		for(var i=0;i<windows.mapNames.count();i++) {
			
			var cid=windows.mapNames.get(i);
			if (cid==id) {
				windows.get(cid).show();
			}
			else {
				windows.get(cid).hide();
			}
		}
	};
	//hide window: build first
	this.hide=function(id) {
		
		if (!windows.contain(id)) {
			return;
		}
		
		windows.get(id).hide();
	};
	//resize all windows
	this.resize=function(w,h) {
		
		if (windows.mapNames==undefined||windows.mapNames["count"]==undefined) {
			return;
		}
		
		for(var i=0;i<windows.mapNames.count();i++) {
			
			var cid=windows.mapNames.get(i);
			windows.get(cid).resize(w,h);
		}
	};
	//set content to window: build first
	this.content=function(id,content) {
		
		if (!windows.contain(id)) {
			return;
		}	
		
		windows.get(id).content(content);
	};
	
	//check if the window is hide
	this.isHide=function(id) {
		if (!windows.contain(id)) {
			return true;
		}
		
		return windows.get(id).isHide();
	};
}

//global window manager for one single page
window["winmgr"]=new AppHubWindowManager();

HtmlHelper.addEvent(window, "resize", function(e) {
	
	var w=HtmlHelper.body("clientWidth");
	var h=HtmlHelper.body("clientHeight");
	
	window["winmgr"].resize(w, h);
});