/**
 * 
 * @param appendId  ：window.winmgr.build对象ID
 * @param datas
 */
function loadDescDiv(appendId,datas){

	var appendObj =  HtmlHelper.id(appendId);
	/**
	 * init head
	 */
	var sb=new StringBuffer();
	sb.append("<div class=\"title-head\">");
	sb.append("<span>预警详情</span>");
	sb.append("<div class=\"icon-signout icon-myout\" onclick=\"javascript:closeDescDiv()\"></div>");
	sb.append( "</div>");
		
	
	if(null == datas){
		return;
	}

	/***
	 * 因为是同一预警，默认获取第一个输出相同信息
	 */
	var mainObj = datas[0];
	var nodeAttr ={
		"ip":{"n":"IP","v":mainObj['ip']},	
		"host":{"n":"HOST","v":mainObj['host']},		
		"state":{"n":"状态","v":""},
		"latTs":{"n":"最近触发报警动作时间","v":""},
		"viewTs":{"n":"预警浏览时间","v":""},
		"count":{"n":"报警次数","v":datas.length},
		"retry":{"n":"触发报警动作次数(邮件/短信)","v":""},
		"title":{"n":"问题摘要","v":mainObj['title']},
		"eventid":{"n":"事件类型","v":mainObj['eventid']}
	}
	
	/**
	 * 渲染容器
	 */
	sb.append("<div class=\"publicheadDiv\">");
	sb.append("<ul>");
	sb.append("<li><span >"+nodeAttr.ip.n+"</span><span class=\"colon\">：</span><span class=\"tValue\">"+nodeAttr.ip.v+"("+nodeAttr.host.v+")</span></li>");
	sb.append( "<li><span >"+nodeAttr.title.n+"</span><span class=\"colon\">：</span><span class=\"tValue\">"+nodeAttr.title.v+"</span></li>");
	sb.append( "<li><span >"+nodeAttr.eventid.n+"</span><span class=\"colon\">：</span><span class=\"tValue\">"+nodeAttr.eventid.v+"</span></li>");
	sb.append("<li><span >"+nodeAttr.count.n+"</span><span class=\"colon\">：</span><span class=\"tValue\">"+nodeAttr.count.v+"</span></li>");
	sb.append("<li id=\"stateLi\"><span >"+nodeAttr.state.n+"</span><span class=\"colon\">：</span><span class=\"tValue\" id=\"stateValue\"></span></li>");
	sb.append("<li id=\"viewTsLi\"><span >"+nodeAttr.viewTs.n+"</span><span class=\"colon\">：</span><span class=\"tValue\" id=\"viewTsValue\"></span></li>");
	sb.append("<li><span >"+nodeAttr.retry.n+"</span><span class=\"colon\">：</span><span class=\"tValue\" id=\"retryValue\"></span></li>");
	sb.append("<li><span >"+nodeAttr.latTs.n+"</span><span class=\"colon\">：</span><span class=\"tValue\" id=\"latTsValue\"></span></li>");
	
	
	sb.append( "</ul>");
	sb.append( "</div>");
	
	appendObj.innerHTML=sb.toString();
	
	
	sb=new StringBuffer();
	
	var display = "block";
	var index=1;
	$.each(datas,function(id,obj){
		
		var objattr ={
				"time":{"n":"预警时间","v":TimeHelper.getTime(obj['time'],'FMS')},	
				"state":{"n":"状态","v":obj['state']},
				"retry":{"n":"报警次数","v":obj['retry']},
				"firstRecord":{"n":"第一条记录","v":obj['firstrecord']},
				"latTs":{"n":"最近触发报警动作时间","v":obj['latest_ts'] ? TimeHelper.getTime(obj['latest_ts'],'FMS') : ""},
				"viewTs":{"n":"预警浏览时间","v":obj['view_ts'] ? TimeHelper.getTime(obj['view_ts'],'FMS'): ""},
				"nodename":{"n":"UAV节点进程","v":obj['args']['nodename']},
				"nodeuuid":{"n":"UAV节点ID","v":obj['args']['nodeuuid']},
				"component":{"n":"报警组件","v":obj['args']['component']},	
				"feature":{"n":"报警组件Feature","v":obj['args']['feature']},
				"desc":{"n":"问题描述","v":obj['description']}
			};

			
		/**
		 * 如果args没有映射，则自动填充属性和值显示
		 */
		var autoAddHtml = "";
		$.each(obj['args'],function(index,value){
			if(!nodeAttr[index] && !objattr[index]){
				autoAddHtml += "<li><span class=\"argsSubTitle\">"+index+"</span><span class=\"colon\">：</span><span class='argsSubValue'>"+value+"</span></li>";
			}
		});
				
		var issueDesc=objattr.desc.v.replace(/\n/g,"<br/>");

		sb.append(" <ul>");
		sb.append(" <div class=\"listDiv\">");
		
		sb.append("	<li onclick=\"javascript:showDesc(this)\" class=\"title\" >["+index+"]&nbsp;"+objattr.time.v+"</li>");
		sb.append( " <div style=\"display:"+display+";\">");
		sb.append( " <li><span class=\"argsTitle\">来源</span></li>");
		sb.append("	<li><span class=\"argsSubTitle\">"+objattr.nodename.n+"</span></span><span class=\"colon\">：</span><span>"+objattr.nodename.v+"("+objattr.nodeuuid.v+")</span></li>");
		sb.append( "<li><span class=\"argsSubTitle\" >"+objattr.component.n+"</span><span class=\"colon\">：</span><span>"+objattr.feature.v+"."+objattr.component.v+"</span></li>");
		sb.append( " <li><span class=\"argsTitle\">"+objattr.desc.n+"</span><div class=\"listBdesc\">"+issueDesc+"</div></li>");
		sb.append( " <div class=\"args\">" /*用于后续扩充收缩功能*/);
		sb.append( "<li class=\"argsTitle\"><span>上下文信息</span></li>");
		sb.append(autoAddHtml);
		sb.append( " </div>");
		sb.append( "</div>");
		sb.append( "</div>");
		sb.append( "</ul>");
		
		display = "none";
		
		if(objattr.firstRecord){
			setState(objattr);
		}
		
		index++;
	});
	
	appendObj.innerHTML+=sb.toString();
	
}

function setState(objattr){
	var state = objattr.state.v;
	if(0==state){
		$("#stateValue").text("新预警");
	}else if(10==state){
		$("#stateValue").text("报警持续中");
	}else if(15==state){
		$("#stateValue").text("已查看");
	}else if(state==20){
		$("#stateValue").text("已查看&报警持续中");
	}

	$("#retryValue").text(objattr.retry.v);
	$("#latTsValue").text(objattr.latTs.v);
	$("#viewTsValue").text(objattr.viewTs.v);
}

function showDesc(obj){
	var objDiv = obj.parentNode;
	var listbodyDiv = objDiv.getElementsByTagName("div")[0];
	if("block" == listbodyDiv.style.display){
		listbodyDiv.style.display = "none";
	}else{
		listbodyDiv.style.display = "block";
	}
}