/**
 * 验证码
 * **/
function Captcha(){
	
	var cfg = {
			url : "",
			appendId : "",
			placeholder:"输入右边结果",
			width:150,
			height:34,
			errNum:1  //错误次数
	}
	
	this.build = function(_cfg){
		
		JsonHelper.merge(cfg, _cfg,false);

		var inputId = cfg.appendId+parseInt(Math.random() * (9999 - 1 + 1) + 1);
		cfg["inputId"]=inputId;
		
		var html=new StringBuffer();
		html.append("<input type='text' id=\""+inputId+"\" class=\"form-control \" style=\"width:"+cfg.width+"px;height:"+cfg.height+"px;display:inline;  \" placeholder=\""+cfg.placeholder+"\"   onkeyup=\"this.value=this.value.replace(\/\\D/g,\'\')\" onafterpaste=\"this.value=this.value.replace(\/\\D/g,\'\')\"   />");
		html.append("<img id=\""+cfg.appendId+"Img\" style=\"width:"+cfg.width+"px;height:"+cfg.height+"px;\" src='"+getNoCacheUrl()+"' onclick=\"javascript:window['captcha'].refresh();\"  />");
				
		HtmlHelper.id(cfg.appendId).innerHTML=html.toString();
		
		$('#'+cfg.appendId+'Img').error(function(){
		    var errNumber = cfg.errNum;
		    if(errNumber <= 10){
		    	console.log("验证码失败次数("+errNumber+")，重新刷新。");
			    cfg.errNum = ++errNumber;
			    window['captcha'].refresh();
		    }else{
		    	console.log("验证码失败次数("+errNumber+")过多，将不再重试。");
		    }
		});
	}
	
	this.refresh =function(){
		document.getElementById(cfg.appendId+"Img").src=getNoCacheUrl();
	}
	
	this.answer = function(){
		return HtmlHelper.inputXSSFilter($("#"+cfg.inputId).val());
	}
	
	this.focus = function(){
		$("#"+cfg.inputId).focus();
	}

	function getNoCacheUrl(){
		return cfg.url.indexOf("?") >0? cfg.url+"&"+parseInt(Math.random() * (9999 - 1 + 1) + 1) : cfg.url+"?"+parseInt(Math.random() * (9999 - 1 + 1) + 1);
	}
	
}

window["captcha"]=new Captcha();
