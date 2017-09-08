/**
 * 通用Util类库
 */
/**
 * TimeHelper 时间日期处理
 */
var TimeHelper={
	
	/**
	 * str转date
	 * @param str
	 * @returns {Date}
	 */
	toDate:function(str) {
		var d = new Date(Date.parse(str.replace(/-/g, "/")));
		return d;
	},
	/**
	 * YYYY-MM-dd HH:mm:ss
	 * @param timestamp
	 * @returns {String}
	 */
    getTime:function(timestamp,format) {
    	
    	var date;
    	if (timestamp==undefined) {
    		date=new Date();
    	}
    	else {
    		date=new Date(timestamp);
    	}
    	
    	//by default full time:second
    	if (undefined==format) {
    		return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate()+" "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
    	}
    	
    	switch(format) {
    	   //full time but with <br/> between date and time
    	   case "FMSN":
    		   return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate()+"<br/>"+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+"."+date.getMilliseconds();
    	  //full time:m seconds
    	  case "FMS":
    		  return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate()+" "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+"."+date.getMilliseconds();
    	  //full time:minute
    	  case "FM":
    		  return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate()+" "+date.getHours()+":"+date.getMinutes();
    	  //full time:hour
    	  case "FH":
    		  return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate()+" "+date.getHours();
    	  //full time:date
    	  case "FD":
    		  return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate();
    	  //current time:seconds
    	  case "CS":
    		  return date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
    		//current time:seconds
    	  case "CMS":
    		  return date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+"."+date.getMilliseconds();
    	  case "UNIX":
    		  return date.getFullYear()+"/"+(date.getMonth()+1)+"/"+date.getDate()+"-"+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
    	  //full time:second
    	  case "FS":
    	  default:
    		  return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate()+" "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
    	}
    }	
};
/**
 * 负责JSON数据的处理
 */
var JsonHelper={
	/**
	 * use obj2's attr to override obj1's attr
	 * @param obj1
	 * @param obj2
	 * @param isAppend     
	 * @param allowEmpty  when TRUE if the obj2's val is "" or undefined, the obj1's val will be override 
	 * @param isReplace   object 替换处理 ，默认(false)递归处理 
	 */
	merge:function(obj1,obj2,isAppend,allowEmpty,isReplace) {
		
		if (obj2==undefined) {
			return;
		}
	
		for(var key in obj1) {

			var val=obj2[key];
			/**
			 * 问题：变量val为布尔量值false时，val==""将判断为true，并跳过布尔变量赋值,导致赋值失败
			 * 解决方法：添加变量类型判断，当变量不为布尔量时，才执行
			 * 搜索KEY : 20160426_VAL01
			 *  原始代码：if ((undefined==val||""==val)&&true!=allowEmpty) {
			 */
			if ((undefined==val||""==val) && (typeof val != "boolean" ) && true!=allowEmpty) {
				continue;
			}

			var old=obj1[key];
			if ((typeof old == "object" ) && (isReplace==undefined||isReplace==false)) {
				this.merge(old, val, isAppend,isReplace);
				obj1[key]=old;
			}
			else {
				obj1[key]=val;
			}
			
		}
		
		if (true!=isAppend) {
			return;
		}
		
		for(var key in obj2) {
			if (obj1[key]==undefined) {
				
				var val=obj2[key];
				/**
				 * 问题：变量val为布尔量值false时，val==""将判断为true，并跳过布尔变量赋值,导致赋值失败
				 * 解决方法：添加变量类型判断，当变量不为布尔量时，才执行
				 * 搜索KEY : 20160426_VAL01
				 *  原始代码：if ((undefined==val||""==val)&&true!=allowEmpty) {
				 */
				if ((undefined==val||""==val) && (typeof val != "boolean" ) && true!=allowEmpty) {
					continue;
				}
				
				obj1[key]=obj2[key];
			}
		}
	},
	clone: function(obj) {
		var co={};
		
		for(var key in obj) {
			
			var val=obj[key];
			
			if (val instanceof Array ) {
				var ca=[];
				for (var i=0;i<val.length;i++) {
					ca[i]=this.clone(val[i]);
				}
				co[key]=ca;
			}
			else if (typeof val == "object" ) {
				var cv=this.clone(val);
				co[key]=cv;
			}
			else {
				co[key]=val;
			}
		}
		
		return co;
	},
	asHTML : function(obj) {
		
		var sb=new StringBuffer();
		
		for (key in obj) {
			
			var val=obj[key];
			
			if (val==undefined) {
				continue;
			}
			
			if (typeof val == "object") {
				sb.append("<div>"+key+"</div>");
				sb.append(JsonHelper.asHTML(val));
			}
			else if (typeof val == "array") {
				sb.append("<div>"+key+"</div>");
				for (var i=0;i<val.length;i++) {
					sb.append(JsonHelper.asHTML(val[i]));
				}
			}
			else if (typeof val == "function") {
				//ignore
			}
			else {
				sb.append("<div>"+key+":"+val+"</div>");
			}
		}
		
		return sb.toString();
	}
};
/**
 * HTML元素操作
 */
function HtmlEncoder(){

    this.REGX_HTML_ENCODE = /"|&|'|<|>|[\x00-\x20]|[\x7F-\xFF]|[\u0100-\u2700]/g;

    this.REGX_HTML_DECODE = /&\w+;|&#(\d+);/g;

    this.REGX_TRIM = /(^\s*)|(\s*$)/g;

    this.HTML_DECODE = {
        "&lt;" : "<",
        "&gt;" : ">",
        "&amp;" : "&",
        "&nbsp;": " ",
        "&quot;": "\"",
        "©": ""

        // Add more
    };

    this.encodeHtml = function(s){
        s = (s != undefined) ? s : this.toString();
        return (typeof s != "string") ? s :
            s.replace(this.REGX_HTML_ENCODE,
                      function($0){
                          var c = $0.charCodeAt(0), r = ["&#"];
                          c = (c == 0x20) ? 0xA0 : c;
                          r.push(c); r.push(";");
                          return r.join("");
                      });
    };

    this.decodeHtml = function(s){
        var HTML_DECODE = this.HTML_DECODE;

        s = (s != undefined) ? s : this.toString();
        return (typeof s != "string") ? s :
            s.replace(this.REGX_HTML_DECODE,
                      function($0, $1){
                          var c = HTML_DECODE[$0];
                          if(c == undefined){
                              // Maybe is Entity Number
                              if(!isNaN($1)){
                                  c = String.fromCharCode(($1 == 160) ? 32:$1);
                              }else{
                                  c = $0;
                              }
                          }
                          return c;
                      });
    };

    this.trim = function(s){
        s = (s != undefined) ? s : this.toString();
        return (typeof s != "string") ? s :
            s.replace(this.REGX_TRIM, "");
    };


    this.hashCode = function(){
        var hash = this.__hash__, _char;
        if(hash == undefined || hash == 0){
            hash = 0;
            for (var i = 0, len=this.length; i < len; i++) {
                _char = this.charCodeAt(i);
                hash = 31*hash + _char;
                hash = hash & hash; // Convert to 32bit integer
            }
            hash = hash & 0x7fffffff;
        }
        this.__hash__ = hash;

        return this.__hash__;
    };

};

var HtmlHelper={
	isIE:function() {
		 if(navigator.userAgent.indexOf("MSIE") != -1 ||navigator.appName == "Microsoft Internet Explorer") {
			 return true;
		 }
		 else {
			 return false;
		 }
	},
	isIE6:function() {
			return (navigator.userAgent.toLowerCase().indexOf("msie 6.0")==-1)?false:true;
	},
	isIE7:function() {
		return (navigator.userAgent.toLowerCase().indexOf("msie 7.0")==-1)?false:true;
	},
	isIE8:function () {
		return (navigator.userAgent.toLowerCase().indexOf("msie 8.0")==-1)?false:true;
	},
	isNN:function () {
		return navigator.userAgent.indexOf("Netscape") != -1;
	},
	isOpera:function () {
		return navigator.appName.indexOf("Opera") != -1;
	},
	isFF:function() {
		return navigator.userAgent.indexOf("Firefox") != -1;
	},
	isChrome:function() {
		return navigator.userAgent.indexOf("Chrome") > -1;
	},
	isiOS:function() {
		return navigator.userAgent.indexOf("iPad") > -1||navigator.userAgent.indexOf("iPhone") > -1;
	},
	isMobile:function() {
		return navigator.userAgent.indexOf("Mobile") > -1;
	},
	//get the html obj
	id:function(_id) {
		var elem= document.getElementById(_id);
		
		if (elem==null) {
			return undefined;
		}
		
		return elem;
	},
	//operate the html obj's style
	css:function(_id,cssObj) {
		var o=_id;
		if (typeof _id == "string") {
			o=this.id(_id);
		}
		for(var key in cssObj) {
			o.style[key]=cssObj[key];                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
		}
		return o;
	},
	//get the elem value
	value:function(_id) {
		var elem=this.id(_id);
		
		if (elem==null) {
			return undefined;
		}
		
		var type=elem.getAttribute("type");
		
		if (type!=undefined&&type=="checkbox") {
			
			return elem.checked;
		}
		
		return elem.value;
	},
	//get/set body's attr
	//no use body.scrollTop as iOS not support that
	body:function(attr,value) {
		
		if (undefined==value) {
			return document.body[attr];
		}
		else {
			document.body[attr]=value;
		}		
	},
	//get width of the html tag
	width:function(_id) {
		var o=_id;
		if (typeof _id == "string") {
			o=this.id(_id);
		}
		
		var w=(o.style.width)?parseInt(o.style.width):0;
		
		if (w==0) {
			w=o.offsetWidth;
		}
		
		return w;
	},
	//get height of the html tag
	height:function(_id,needScrollHeight) {
		var o=_id;
		if (typeof _id == "string") {
			o=this.id(_id);
		}
		
		if (needScrollHeight==true) {
			return o.scrollHeight;
		}		
		
		var h=(o.style.height)?parseInt(o.style.height):0;
		
		if (h==0) {
			h=o.offsetHeight;
		}
		
		
		
		return h;
	},
	//del the html tag obj
	del:function(_id) {
		var t=this.id(_id);
		if (undefined!=t) {
			t.parentNode.removeChild(t);
		}
	},
	//add new event func to one html tag event
	addEvent:function(object, type, callback) {
	    if (object == null || typeof(object) == 'undefined') return;
	    if (object.addEventListener) {
	        object.addEventListener(type, callback, false);
	    } else if (object.attachEvent) {
	        object.attachEvent("on" + type, callback);
	    } else {
	        object["on"+type] = callback;
	    }
	},
	//create new element
	newElem:function(tagName,attrs) {
		var elem=document.createElement(tagName);
		
		if (attrs!=undefined) {
			for(attr in attrs) {
				elem.setAttribute(attr, attrs[attr]);
			}
		}
		
		return elem;
	},
	/* XSS 过滤*/
	inputXSSFilter : function(s) {
		if(typeof s != 'string'){
			return s;
		};

		s = s.replace("&lt;", "");
		s = s.replace("&lt;/", "");
		s = s.replace("&gt;", "");
		s = s.replace("<script>", "");
		s = s.replace("</script>", "");
		s = s.replace("javascript:", "");
		s = s.replace("alert(", "");
		s = s.replace("console.", "");
		return s;
	},
	htmlEncoder:new HtmlEncoder(),
	encode:function(s) {
		return this.htmlEncoder.encodeHtml(s);
	},
	decode:function(s) {
		return this.htmlEncoder.decodeHtml(s);
	},
	enable:function(_id,check) {
		this.id(_id).setAttribute("disabled",!check);
	},
	//get the url ? query param
	getQParam:function(name) {
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		var r = window.location.search.substr(1).match(reg);
		if (r != null) return unescape(r[2]); return undefined;
	} 
};
/**
 * 日志输出
 */
var LogHelper={
	isdebug:true,
	info:function(obj,msg) {
		var caller=this.info.caller;
		this.format("INFO",caller,msg);
	},
	debug:function(obj,msg,e) {
		if (this.isdebug) {
			var caller=this.debug.caller;
			this.format("DEBUG",caller,msg,e);
		}
	},
	err:function(obj,msg,e) {
		var caller=this.err.caller;
		this.format("ERR",caller,msg,e);
	},
	format:function(type,caller,msg,e) {
//		console.log("["+TimeHelper.getTime()+"]	"+type+"	"+this._funcName(caller)+"	"+msg+"	");
		if (e!=undefined) {
			console.log(e);
		}
		
	},
	_funcName:function(func) {
		var name = undefined;
	    if ( typeof func == 'function' || typeof func == 'object' ) {
	        name = ('' + func).match(/function\s*([\w\$]*)\s*\(/);
	    }
	    return name && name[1];
	}
};
/**
 * 负责对象和string之间的互操作
 */
var StringHelper={
    //转换成驼峰格式
	toTF:function(str) {
		  var re=/-(\w)/g;
		  str=str.replace(re,function($0,$1){
		    return $1.toUpperCase();
		  });
		  return str;
	},
	//string to obj	
	str2obj:function(s) {
		var obj;
		try {
			if (s.indexOf("[")==0) {
				obj=eval(s);
			}
			else {
				obj=eval("("+s+")");
			}
		}catch(e) {
			try {
				obj=JSON.parse(s);
			}
			catch(e1) {
				console.log(e1);
				obj="ERR";
			}
		}
		return obj;
		
	},
	//obj to string
	obj2str:function(o) {
		
		try {
			var r=new Array();
			if(typeof o=="string"){
				return "\""+o.replace(/([\'\"\\])/g,"\\$1").replace(/(\n)/g,"\\n").replace(/(\r)/g,"\\r").replace(/(\t)/g,"\\t")+"\"";
			}
			if(typeof o=="object"){
				if(!o.sort){
					for(var i in o){
						r.push(i+":"+this.obj2str(o[i]));
					}
					if(!!document.all&&!/^\n?function\s*toString\(\)\s*\{\n?\s*\[native code\]\n?\s*\}\n?\s*$/.test(o.toString)){
						r.push("toString:"+o.toString.toString());
					}
					r="{"+r.join()+"}";
				}else{
					for(var i=0;i<o.length;i++){
						r.push(this.obj2str(o[i]));
					}
					r="["+r.join()+"]";
				}
				return r;
			}
			return o.toString();
			
		}catch(e) {
			try {
				return JSON.stringify(o);
			}catch(e1) {
				
			}
		}
		
		return undefined;
	}
};

/**
 * CommonHelper is some common business logic
 */
var CommonHelper={
		
	getKB2Human:function(mem,needUnit,fixedLevel) {
		return this.getB2Human(mem*1000,needUnit,fixedLevel);
	},
	/**
	 * 转换bytes成人类可读的数
	 * @param memInBytes  bytes
	 * @param needUnit  是否需要单位
	 * @param fixedLevel 是否固定在某个单位上，如0表示B，1表示KB，2表示MB，3表示GB等
	 * @returns
	 */		
	getB2Human:function(memInBytes,needUnit,fixedLevel) {
		
		if (( (memInBytes>0&&memInBytes<1024) && fixedLevel==undefined) ||fixedLevel==0) {
			if(true==needUnit) {
				return memInBytes+"B";
			}
			else
			 return memInBytes;
		}
		else if (((memInBytes>=1024&&memInBytes<1024*1024)&&fixedLevel==undefined) ||fixedLevel==1) {
			var tmp=Math.round(memInBytes*100/1024)/100;
			if(true==needUnit) {
				return tmp+"KB";
			}
			else {
				return tmp;
			}
		}
		else if(((memInBytes>=1024*1024&&memInBytes<1024*1024*1024)&&fixedLevel==undefined)||fixedLevel==2) {
			var tmp=Math.round(memInBytes*100/(1024*1024))/100;
			if(true==needUnit) {
				return tmp+"MB";
			}
			else {
				return tmp;
			}
		}
		else if((memInBytes>=1024*1024*1024&&fixedLevel==undefined)||fixedLevel==3) {
			var tmp=Math.round(memInBytes*100/(1024*1024*1024))/100;
			if(true==needUnit) {
				return tmp+"GB";
			}
			else{
				return tmp;
			}
		}
		else {
			return 0;
		}
	}
};

/**
 * AJAX Helper
 */
var AjaxHelper={
	call:function(cfg) {
		var timeOutId;
		$.ajax({
			url:cfg.url,
			type:cfg.type,
			async:(cfg.async == false ? false : true),
			cache:cfg.cache,
			data:cfg.data,
			dataType:cfg.dataType,
			contentType:cfg.contentType,
			beforeSend:function () {
				timeOutId = setTimeout(function(){PageHelper.showLoading();},1000);
		    },
			success:function(resp){
				AjaxHelper.checkSession("S",resp,cfg);
			},
			complete:function(){
				clearTimeout(timeOutId);
				PageHelper.hideLoading();
			},
			error:function(resp){
				AjaxHelper.checkSession("E",resp,cfg)
			}
		});
	},
	checkSession:function(type,resp,cfg){
		/**
		 * SessionAsyncFilter:会话校验
		 */
		var checkResp = resp;
		if("E"==type){
			checkResp = resp.responseText;
		}
		
		if (checkResp && checkResp.length > 18
				&& checkResp.substring(0, 18) == "SESSION_CHECK_FAIL") {
			
			if(window["cachemgr"]){
				window["cachemgr"].clear();
			}
			
			if (window != top) {
				// 跳出iframe
				top.location.href = checkResp.split(',')[1];
			} else {
				window.location.href = checkResp.split(',')[1];
			}
		} else {
			if("S"==type){
				cfg.success(resp);
			}else if("E"==type){
				cfg.error(resp);
			}	
		}
	}
};


/**
 * 工程路径
 */
var WebHelper = {
	proNameUrl : function() {
		var curWwwPath = window.document.location.href;
		var pathName = window.document.location.pathname;
		var pos = curWwwPath.indexOf(pathName);
		var localhostPaht = curWwwPath.substring(0, pos);
		var projectName = pathName.substring(0,
				pathName.substr(1).indexOf('/') + 1);
		return (localhostPaht + projectName);
	}
}

var PageHelper = {
	showLoading : function(text) {
		try {
			
			var lodingDiv;
			//兼容iframe
			if (window != top) {
				lodingDiv = top.document.getElementById("lodingDiv");
			} else {
				lodingDiv = window.document.getElementById("lodingDiv");
			}
			
			if (lodingDiv) {
				lodingDiv.style.display = "block";
			} else {
				var loadingUrl = WebHelper.proNameUrl()+ "/apphub/img/mvc/loading.gif";
				var style = new StringBuffer();
								
				style.append("@-o-keyframes progress-bar-stripes {");
				style.append("from {");
				style.append("background-position: 0 0");
				style.append("}");
				style.append("to {");
				style.append("background-position: 40px 0");
				style.append("}");
				style.append("}");

				style.append("@keyframes progress-bar-stripes {");
				style.append("from {");
				style.append("background-position: 40px 0");
				style.append("}");
				style.append("to {");
				style.append("background-position: 0 0");
				style.append("}");
				style.append("}");

				style.append(".progress {");
				style.append("margin-bottom: 20px;");
				style.append("overflow: hidden;");
				style.append("background-color: #f7f7f7;");
				style.append("background-image: linear-gradient(to bottom,#f5f5f5,#f9f9f9);");
				style.append("background-repeat: repeat-x;");

				style.append("border-radius: 4px;");
				style.append("box-shadow: inset 0 1px 2px rgba(0,0,0,0.1)");
				style.append("}");

				style.append(".progress .bar {");
				style.append("float: left;");
				style.append("height: 100%;");
				style.append("font-size: 12px;");
				style.append("color: #fff;");
				style.append("text-align: center;");
				style.append("}");

				style.append(".progress-striped .bar {");
				style.append("background-color: #149bdf;");
				style.append("background-image: linear-gradient(45deg,rgba(255,255,255,0.15) 25%,transparent 25%,transparent 50%,rgba(255,255,255,0.15) 50%,rgba(255,255,255,0.15) 75%,transparent 75%,transparent);");
				style.append("background-size: 40px 40px");
				style.append("}");

				style.append(".progress.active .bar {");
				style.append("animation: progress-bar-stripes 0.8s linear infinite");
				style.append("}");

				style.append(".lodingDiv{");
				style.append("width:160px;");
				style.append("height:20px;");
				style.append("}");
				
				
				style.append(".lodingDiv{");
				style.append("width:160px;");
				style.append("height:20px;");
				style.append("position: absolute;");
				style.append("top:46%;");
				style.append("left:44.5%;");
				style.append("}");
				//手机样式 
				style.append("@media");
				style.append("(max-width: 760px),(min-device-width: 768px) and (max-device-width: 1024px)");
				style.append("{");
				style.append(".lodingDiv{");
				style.append("top:50%;");
				style.append("left:30%;");
				style.append("}");
				style.append("}");
				//手机横屏
				style.append("@media (-webkit-min-device-pixel-ratio: 2)and (orientation:landscape)");
				style.append("{");
				style.append(".lodingDiv{");
				style.append("top:40%;");
				style.append("left:40%;");
				style.append("}");
				style.append("}");
				
				var lodingDiv = document.createElement("div");
				lodingDiv.setAttribute("id", "lodingDiv");
				lodingDiv.setAttribute("class", "progress progress-striped active lodingDiv");
				var lodingBodyDiv = document.createElement("div");
				lodingBodyDiv.setAttribute("style", "padding-top:2px;width: 100%;");
				lodingBodyDiv.setAttribute("class", "bar");
				lodingBodyDiv.innerHTML = (text!=undefined)?text:"Loading...";
				lodingDiv.style.zIndex=1000000;
				lodingDiv.appendChild(lodingBodyDiv);

				var body;
				if (window != top) {
					body = top.document.getElementById("spaContent");
				} else {
					body = window.document.getElementById("spaContent");
				}
				if(!body){
					body = document.getElementsByTagName('body')[0];
				}
				body.appendChild(lodingDiv);
				var styleText = document.createTextNode(style.toString());
			 	var styleElement = document.createElement("style");
			 	styleElement.type="text/css";
			 	styleElement.appendChild(styleText);
			 	body.appendChild(styleElement);
			}
		} catch (e) {
			console.log(e);
		}

	},
	hideLoading : function() {
		try {
			var lodingDiv;
			if (window != top) {
				lodingDiv = top.document.getElementById("lodingDiv");
			} else {
				lodingDiv = window.document.getElementById("lodingDiv");
			}
			if (lodingDiv) {
				lodingDiv.style.display = "none";
			}
		} catch (e) {
			console.log(e);
		}

	}
}