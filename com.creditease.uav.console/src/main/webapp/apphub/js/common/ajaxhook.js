//ajax hook
!function (ob) {	
    ob.hookAjax = function (funs) {
        window._ahrealxhr = window._ahrealxhr || XMLHttpRequest;
        XMLHttpRequest = function () {
            this.xhr = new window._ahrealxhr;
            for (var attr in this.xhr) {
                var type = "";
                try {
                    type = typeof this.xhr[attr]
                } catch (e) {}
                if (type === "function") {
                    this[attr] = hookfun(attr);
                } else {
                    Object.defineProperty(this, attr, {
                        get: getFactory(attr),
                        set: setFactory(attr)
                    })
                }
            }
        }

        function getFactory(attr) {
            return function () {
                return this.hasOwnProperty(attr + "_")?this[attr + "_"]:this.xhr[attr];
            }
        }

        function setFactory(attr) {
            return function (f) {
                var xhr = this.xhr;
                var that = this;
                if (attr.indexOf("on") != 0) {
                    this[attr + "_"] = f;
                    return;
                }
                if (funs[attr]) {
                    xhr[attr] = function () {
                        funs[attr](that) || f.apply(xhr, arguments);
                    }
                } else {
                    xhr[attr] = f;
                }
            }
        }

        function hookfun(fun) {
            return function () {
                var args = [].slice.call(arguments)
                if (funs[fun] && funs[fun].call(this, args, this.xhr)) {
                    return;
                }
                return this.xhr[fun].apply(this.xhr, args);
            }
        }
        return window._ahrealxhr;
    }
    ob.unHookAjax = function () {
        if (window._ahrealxhr)  XMLHttpRequest = window._ahrealxhr;
        window._ahrealxhr = undefined;
    }
}(window)
/**
 * BrowserMonitor
 */
function BrowserMonitor() {
	
	var monitor=this;
	
	var browser={
        ip:"",
        location:""
	};
	
	//--------------------------ajax hook-------------------------------
	//Ajax callback Event
	this.ajaxhook=function() {
		return {
			//hook callbacks
		    onreadystatechange:function(xhr){
		    	monitor.ajax_doCap("onreadystatechange",xhr.xhr);
		    },
		    onload:function(xhr){
		    	monitor.ajax_doCap("onload",xhr.xhr);
		    },
		    //hook function
		    open:function(args,xhr){
		    	monitor.ajax_preCap(xhr,args);
		    },
		    send:function(args,xhr) {
		    	monitor.ajax_doCap("send",xhr,args);
		    }
		};
	};
	/**
	 * spanMap: record every ajax request & response
	 */
	var spanMap={};
	
	this.ajax_preCap=function(xhr,args) {
		
		var st=new Date().getTime();
		var span={};
		span["ip"]=browser.ip;
		span["loc"]=browser.location;
		span["traceId"]="_"+st;
		span["startTime"]=st;
		span["cost"]=-1;
		span["url"]=args[1];
		span["event"]="ajx-"+args[0].toLowerCase();
		span["info"]=args[2];
		
		spanMap[span["traceId"]]=span;
		
		xhr._spankey=span["traceId"];
		
		console.log("preCap: "+span["traceId"]+",url=%s,async=%s",span.url, span.info);
	};
	
	this.ajax_doCap=function(intent,xhr,args) {
		
		var span=spanMap[xhr._spankey];
		var isEndSpan=false;
		if (intent=="send"&&undefined!=span&&span["info"]==false) {
			isEndSpan=true;
		}
		//async mode for ajax
		else if (intent=="onload") {
			isEndSpan=true;
		}
		else if (intent=="onreadystatechange") {

			if (xhr.readyState!=4) {
				return;
			}
			
			isEndSpan=true;
		}
		
		if (span==undefined) {
			return;
		}
		
		if (isEndSpan==true) {
			var cost=new Date().getTime()-span.startTime;
			span["cost"]=cost;
			
			console.log("doCap: "+span["traceId"]+",%s,%s,%O",span.url,span.cost,span);
			
			this.post(span);
			
			delete spanMap[xhr._spankey];
		}
	};
	//--------------------------page hook-------------------------------
	var pageSpan={};
	
	this.pageHook=function() {
		pageSpan["startTime"]=new Date().getTime();
		
		var t=setInterval(function(){
				if(document.readyState=="complete"){
					clearInterval(t);
					var cost=new Date().getTime()-pageSpan.startTime;
					pageSpan["cost"]=cost;
					pageSpan["event"]="load";
					pageSpan["ip"]=browser.ip;
					pageSpan["loc"]=browser.location;
					pageSpan["url"]=window.location.href;
					
					console.log("page load: cost="+cost+",%O",pageSpan);
					
					monitor.post(pageSpan);
					
					//add other page window events
					var script=document.createElement("script");
					script.type= 'text/javascript'; 
					script.innerHTML="__browserMonitor.addWndEvents();";					
					document.body.appendChild(script);
				}
			},5);
	};
	
	//addWndEvents
	this.addWndEvents=function() {
		
		var owu=window.onunload;
		
		window.onunload=function() {
			if (owu!=undefined) {
				owu();
			}
			monitor.onunload();
		};
		
		var owe=window.onerror;
		
		window.onerror=function(sMessage,sUrl,sLine) {
			if (owe!=undefined) {
				owe();
			}
			monitor.onerror(sMessage,sUrl,sLine);
		};
	};
	//-----------------------------JS Error hook-------------------------
	this.onerror=function(sMessage,sUrl,sLine) {
		
		var jserrSpan={
			  url:window.location.href,
			  ip:browser.ip,
			  loc:browser.location,
			  startTime:new Date().getTime(),
			  event:"jserr",
			  cost:0,
			  info:sUrl+","+sLine+","+sMessage
		};
		
		console.log("javascript error: %s,%O",sMessage,jserrSpan);
		this.post(jserrSpan);
		return false;
	};
	//------------------------------Page Leave--------------------------
	this.onunload=function(){     
		var cost=new Date().getTime()-pageSpan.startTime;
		pageSpan["cost"]=cost;
		pageSpan["event"]="unload";
		console.log("page leave: cost="+cost+",%O",pageSpan);
		this.post(pageSpan);
	};
	//-----------------------------Browser IP track----------------------
	this.ipTrack=function() {
		var head= document.getElementsByTagName('head')[0];
		var script=document.createElement("script");
		script.type= 'text/javascript'; 
		script.src="http://pv.sohu.com/cityjson?ie=utf-8";
		script.onreadystatechange= function () { 
			if (this.readyState == 'complete') {
				browser.ip=returnCitySN["cip"];
				browser.location=returnCitySN["cname"];
				console.log("ip track: %s,%s",browser.ip,browser.location);
			}						
		};
		script.onload= function(){ 
			browser.ip=returnCitySN["cip"];
			browser.location=returnCitySN["cname"];
			console.log("ip track: %s,%s",browser.ip,browser.location);
		};
		head.appendChild(script);
	};
	//----------------------------submit tech---------------------------
	this.post = function(data) {

		var pdata = data["startTime"] + ";" + data["event"] + ";"
				+ data["cost"] + ";" + data["ip"] + ";" + data["loc"] + ";"
				+ data["url"] + ";"
				+ ((data["info"] != undefined) ? data["info"] : "");

		var request = new window._ahrealxhr;
		var url = "http://localhost:8280/com.creditease.uav/apm/uem";
		if ("withCredentials" in request) {
			// Firefox 3.5 and Safari 4
			request.open('post', url, true);
			request.send(pdata);
		} else if (XDomainRequest) {
			// IE8
			var xdr = new XDomainRequest();
			xdr.open("post", url);
			xdr.send(pdata);
		}
		else {
			alert(1);
			this.postX(data, pdata);
		}
	};
	
	this.postX=function(data,pdata) {
		  
		  // Add the iframe with a unique name
		  var uniqueString = "__uem_xpost_"+data["startTime"];
		  
		  var  iframe=document.createElement("iframe");
		  iframe.name = uniqueString;
		  iframe.id=uniqueString;

		  iframe.style.display = "none";
		  document.body.appendChild(iframe);
		  
		 // construct a form with hidden inputs, targeting the iframe
		  var form = document.createElement("form");
		  form.id=uniqueString+"_f";
		  form.target = uniqueString;
		  form.action = "http://localhost:8080/com.creditease.uav/apm/uem";
		  form.method = "POST";

		  // repeat for each parameter
		  var input = document.createElement("input");
		  input.type = "hidden";
		  input.name = "data";
		  input.value =pdata;
		  form.appendChild(input);

		  document.body.appendChild(form);
		  form.submit();
		  
		  setTimeout(function() {
			  try {
				  var tform=document.getElementById(uniqueString+"_f");
				  document.body.removeChild(tform);
				  var ifr=document.getElementById(uniqueString);
				  document.body.removeChild(ifr);
			  }catch(e) {
				  
			  }
		  },1000);
	};
}

var __browserMonitor=new BrowserMonitor();
//ip track
__browserMonitor.ipTrack();
//hook page load
__browserMonitor.pageHook();
//hook ajax open & callback
hookAjax(__browserMonitor.ajaxhook());