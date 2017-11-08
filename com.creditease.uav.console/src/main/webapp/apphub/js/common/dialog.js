function AppHubDialog(cfg) {
	
	var config={
		id:"",
		title:"",
		height:undefined,
		event:{
			onbody:function() {
				
			},
			onopen:function() {
				
			},
			onclose:function() {
				
			},
			onremove:function(){
				
			}
		},
		object:undefined
	};
	
	//merge config
	JsonHelper.merge(config, cfg,true);
	
	function init() {
		
		var inst=HtmlHelper.id(config["id"]);
		
		if (inst!=undefined) {
			return;
		}
		
		var sb=new StringBuffer();
		sb.append("<div class=\"modal fade\" id=\""+config["id"]+"\" aria-hidden=\"true\">");
		sb.append("<div class=\"modal-dialog\">");
		sb.append("<div class=\"modal-content\">");
		//header
		sb.append("<div class=\"modal-header\">");
		sb.append("<h5>"+config["title"]+"</h5>");
		sb.append("</div>");
		//body
		var height="150px";
		if (config["height"]!=undefined) {
			height=config["height"]+"px";
		}	
		sb.append("<div class=\"modal-body\" style=\"height:"+height+";color:#333;\">");
		
		sb.append(config.event["onbody"](config["id"]));
		
		sb.append("</div>");
		//foot
		sb.append("<div class=\"modal-footer\">	");
		
		var closeBtnId=config["id"]+"_closebtn";
		
		sb.append("<button id='"+closeBtnId+"' class=\"btn\" data-dismiss=\"modal\" >关闭</button>");
		
		sb.append("</div></div></div></div>");
		
		// document.body.innerHTML=sb.toString()+document.body.innerHTML;
		$(document.body).prepend(sb.toString()); // FIX miss DOM Event when rewriting 'innerHTML' directly
		
		var closeBtnElem=HtmlHelper.id(closeBtnId);
		
		closeBtnElem.onClick=function(e) {
			try {
				config.event["onclose"](config["id"]);
			}
			catch(e) {
				
			}
		};
	}
	
	this.getObj=function() {
		return config.object;
	};
	
	this.open=function(obj) {
		init();
		
		config.object=obj;
		
		try {
			config.event["onopen"](config["id"],obj);
		}
		catch(e) {
			
		}
		$("#"+config["id"]).modal();
	};
	
	this.close=function() {
		
		try {
			config.event["onclose"](config["id"]);
		}
		catch(e) {
			
		}
		
		$("#"+config["id"]).modal("hide");
	};
	
	this.remove=function() {
		
		try {
			config.event["onremove"](config["id"]);
		}
		catch(e) {
			
		}
		
		$("#"+config["id"]).remove();
	};
	
}

/**
 * AppHubDialogManager
 */
function AppHubDialogManager() {
	
	var list=new Map();

	this.build=function(_config) {
		var tl=new AppHubDialog(_config);
		list.put(_config["id"], tl);
	};
	
	this.open=function(id,obj) {
		
		var tl=list.get(id);
		if (undefined==tl) {
			return;
		}
		tl.open(obj);
	}
	
	this.getObj=function(id) {
		
		var tl=list.get(id);
		if (undefined==tl) {
			return;
		}
		
		return tl.getObj();
	}
	
	this.close=function(id) {
		var tl=list.get(id);
		if (undefined==tl) {
			return;
		}
		tl.close();
	}
	
	this.remove=function(id) {
		var tl=list.get(id);
		if (undefined==tl) {
			return;
		}
		tl.remove();
	}
}

window["appdialogmgr"]=new AppHubDialogManager();