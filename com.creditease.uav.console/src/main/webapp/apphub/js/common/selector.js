/**
 * 实现Select组件功能
 */
function AppHubSelector(config) {
	
	_this=this;
	
	this.mconfig= {
			//组件id
			id:"",
			//父容器id
			cid:"",
			title:"",
			style:"",
			//事件
			events:{}
	};
	
	JsonHelper.merge(this.mconfig,config,true,false,true);
	
	/**
	 * 初始化
	 */
	this.init=function() {
		
		var parent=HtmlHelper.id(this.mconfig.cid);
		
		if (parent==undefined) {
			return;
		}
		
		var comp=HtmlHelper.id(this.mconfig.id);
		
		if (comp!=undefined) {
			return;
		}
		
		var sb=new StringBuffer();
		
		var style="";
		if (this.mconfig.style!="") {
			style="style='"+this.mconfig.style+"'";
		}
		
		var event="";
		if(this.mconfig.events["onchange"]!=undefined) {			
			event="onchange='"+this.mconfig.events["onchange"]+"'";
		}
		
		sb.append("<select id=\""+this.mconfig.id+"\" class=\"AppHubSelector\" title=\""+this.mconfig.title+"\" "+style+" "+event+">");
		sb.append("</select>");
		
		parent.innerHTML+=sb.toString();
		
		
		
	};
	
	/**
	 * 加载数据
	 */
	this.load=function(datas) {
		
		if(datas==undefined) {
			return;
		}
		
		var comp=HtmlHelper.id(this.mconfig.id);
		
		if (comp==undefined) {
			return;
		}
		
		var sb=new StringBuffer();
		
		for(var i=0;i<datas.length;i++) {
			var data=datas[i];
			
			var title=data["title"];
			var value=data["value"];
			
			var selected=data["select"];
			var selStr="";
			if (selected!=undefined) {
				selStr="selected=\"selected\"";
			}
			
			sb.append("<option id=\""+this.mconfig.id+"_"+value+"\" class=\"AppHubOption\" value=\""+value+"\" "+selStr+" title=\""+value+"\">"+title+"</option>");
		}
		
		comp.innerHTML=sb.toString();
		
		
	};
	
	this.value=function() {
		var comp=HtmlHelper.id(this.mconfig.id);
		
		if (comp==undefined) {
			return undefined;
		}
		
		return comp.value;
	}
	
	this.selTitle=function() {
		var comp=HtmlHelper.id(this.mconfig.id);
		
		if (comp==undefined) {
			return undefined;
		}
		
		for(var i=0;i<comp.options.length;i++) {
			if (comp.options[i].value==comp.value) {
				return comp.options[i].innerHTML;
			}
		}
		
		return undefined;
	}
}