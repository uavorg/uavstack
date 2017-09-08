function AppHubJSONVisualizer(_cfg) {  
  
    var cfg={  
		   "indentation" :  20    ,            //缩进px
		   "attribute"   :  {height:1000,width:'',frontSize:14,color:'black'},
		   "showLayerNum"   :   1,
		   "modelDefault":	[   
		                  	   {keyT:"<div class='defaultRoot'>{@key}</div>",onClick:"clickRootDefault(this);",mouseOver:"mouseOverDefault(this);",mouseOut:"mouseOutDefault(this);"},   
						       {keyT:"<div class='defaultLayer0'>{@key}</div>",onClick:"clickRootDefault(this);",mouseOver:"mouseOverDefault(this);",mouseOut:"mouseOutDefault(this);"},   
						       {keyT:"<div class='defaultLayer1'>{@key}</div>",onClick:"clickRootDefault(this);",mouseOver:"mouseOverDefault(this);",mouseOut:"mouseOutDefault(this);"},
						       {keyT:"<div class='defaultLayer2'>{@key}</div>",onClick:"clickRootDefault(this);",mouseOver:"mouseOverDefault(this);",mouseOut:"mouseOutDefault(this);"},
						       {keyT:"<div class='defaultLayer3'>{@key}</div>",onClick:"clickRootDefault(this);",mouseOver:"mouseOverDefault(this);",mouseOut:"mouseOutDefault(this);"},
						       {keyT:"<div class='defaultLayer4'>{@key}</div>",onClick:"clickRootDefault(this);",mouseOver:"mouseOverDefault(this);",mouseOut:"mouseOutDefault(this);"}
						       
					        ],
			"classDefault":    {keyT:"<div class='defaultLayer'>{@key}</div>",onClick:"clickRootDefault(this);",mouseOver:"mouseOverDefault(this);",mouseOut:"mouseOutDefault(this);"}
    };  
    JsonHelper.merge(cfg,_cfg,true,false,true);
  
    this.asHtml=function(tmpid, jsonObj) {
    	var obj;
    	var attribute = cfg["attribute"]; 
    	if(cfg["showLayerNum"] <= 0){
    		cfg["showLayerNum"] = 1;
    	}
    	if(cfg[tmpid] == null){
    		tmpid = "modelDefault";
    	}
    	if(attribute["frontSize"]){
    		cfg["indentation"] = attribute["frontSize"];
    	}
    	var str = toHtml(tmpid, jsonObj, 0);
    	var style = "style='text-align:left;width:" + attribute["width"]+"px;height:"+attribute["height"]+"px;font-size:"+attribute["frontSize"]+"px;color:"+attribute["color"]+"'";
    	str = "<div class='jsonOutter' " +style+ ">" + str + "</div>";
    	return str;
	}  
    
    function getLayerModelInfo(tmpid, layer){
    	var model = cfg[tmpid];
    	var layerModel;
    	var m;
    	if(layer >= model.length){
    		m = cfg["classDefault"];
    		layerModel = m["keyT"];
    	}else{
    		m =  model[layer];
    		layerModel = m["keyT"];
    	}
    	var preString = layerModel.substring(0, layerModel.indexOf('{'));
    	var afterString = layerModel.substring(layerModel.indexOf('}')+1);
    	var clickFunc = '';
		var overFunc = '';
		var outFunc = '';
    	if(typeof m["onClick"] == 'function'){
    		clickFunc = '('+m["onClick"]+')()';
    	}else{
    		clickFunc = m["onClick"];
    	}
    	if(typeof m["mouseOver"] == 'function'){
    		overFunc = '('+m["mouseOver"]+')()';
    	}else{
    		overFunc = m["mouseOver"];
    	}
    	if(typeof m["mouseOut"] == 'function'){
    		outFunc = '('+m["mouseOut"]+')()';
    	}else{
    		outFunc = m["mouseOut"];
    	}
		var flag = layerModel.indexOf('class=');
    	var subS = layerModel.substring(flag+7);
    	var flag2 = subS.indexOf("'");
    	var layerClass = subS.substring(0,flag2);	
		var re = {
				"preString":preString,
				"afterString":afterString,
				"clickFunc":clickFunc,
				"overFunc":overFunc,
				"outFunc":outFunc,
				"layerClass":layerClass
		};
		return re;
    };
    
    function toHtml(tmpid, jsonObj, layer){
    	var s = '';
    	var obj = jsonObj;
    	var indentation = (layer==0)?0:cfg["indentation"];
    	var re = '';

    	var t = typeOf(obj);
    	if(layer == 0){
    		re = "<div style='padding-left:" + 0 +"px;text-align:left'>";
    	}else{
    		re = "<div style='padding-left:" + cfg["indentation"] +"px;text-align:left'>";
    	}
    	var subRe = "<div style='padding-left:" + cfg["indentation"] +"px;text-align:left'>";
    	var arrayImg = '<span class="exptionPicture glyphicon glyphicon-minus-sign" onclick="expImgClickedArray(this);"></span>';
    	var arrayImgPlus = '<span class="exptionPicture glyphicon glyphicon-plus-sign" onclick="expImgClickedArray(this);">...</span>';
		if(t == 'boolean'){
			var layerInfo = getLayerModelInfo(tmpid, layer);
			var layerCon;
			var func = " onclick='"+layerInfo["clickFunc"] + "' " + "onmouseover='" +layerInfo["overFunc"]+ "' " + "onmouseout='" +layerInfo["outFunc"]+"' " ;
			if(jsonObj){
				layerCon = "<span class='userContent layercontent"+layer+"'"+func+">" + "<span class='directContent'>" + "true"  + "</span></span>";
			}else{
				layerCon = "<span class='userContent layercontent"+layer+"'"+func+">" + "<span class='directContent'>"+"false"  + "</span></span>";
			}
			var userLayerCon = layerInfo["preString"] + layerCon + layerInfo["afterString"];
			s = re + userLayerCon  + "</div>";
		}else if(t == 'array'){
			var s1 = '';
			var style2 = '';
			if((layer+1) >= cfg["showLayerNum"]){
				style2 = '<span class="exptionPicture glyphicon glyphicon-plus-sign" onclick="expImgClickedArray(this);">...</span><div style="display: none;">';
			}else{
				style2 = '<span class="exptionPicture glyphicon glyphicon-minus-sign" onclick="expImgClickedArray(this);"></span><div>';
			}
			if(obj.length == 0){
				s1 = '<ul class="arrayList"><li display="block"><div style="text-align: left;">' + " "+ '</div></li></ul>';
			}else{
				for(var i = 0; i < obj.length; i++){
					var arrayS = '';
					var newLayer = layer + 1;
					/* var dot = '';
					if(i != obj.length-1){
						dot = ',';
					}else{
						dot = '';
					}*/
					var objT = typeof obj[i];
					if(objT == 'object'){
						var layerInfo = getLayerModelInfo(tmpid, layer);
						//arrayS= '<li><div style="text-align: left;" class="' +layerInfo["layerClass"]+ '">{' +style2 +toHtml(tmpid, obj[i], layer+1)+ '</div>}</div></li>';
						arrayS= '<li><div style="text-align: left;" class="ArrayObjectContainer">{' +style2 +toHtml(tmpid, obj[i], layer+1)+ '</div>}</div></li>';
					}else{
						var layerInfo = getLayerModelInfo(tmpid, layer);
						var func = " onclick='"+layerInfo["clickFunc"] + "' " + "onmouseover='" +layerInfo["overFunc"]+ "' " + "onmouseout='" +layerInfo["outFunc"]+"' " ;
						var layerCon = "<span class='userContent layercontent"+newLayer+"'"+func+ "id='" +obj[i]+"'>" + "<span class='directContent'>"+ obj[i]  + "</span></span>";
						var userLayerCon = layerInfo["preString"] + layerCon + layerInfo["afterString"];
						arrayS= '<li><div style="text-align: left;">' +userLayerCon +'</div></li>';
					}
					s1 += arrayS;
				}
			}
			s = '<ul class="arrayList">'+ s1 + "</ul>";
		}
		else if(t != 'object'){
			var layerInfo = getLayerModelInfo(tmpid, layer);
			var func = " onclick='"+layerInfo["clickFunc"] + "' " + "onmouseover='" +layerInfo["overFunc"]+ "' " + "onmouseout='" +layerInfo["outFunc"]+"' " ;
			var layerCon = "<span class='userContent layercontent"+newlayer+"'"+func+ "id='" +obj+"'>" + "<span class='directContent'>"+ obj  + "</span></span>";
			var userLayerCon = layerInfo["preString"] + layerCon + layerInfo["afterString"];
			s = re + userLayerCon  + "</div>";
		}
		else if(t == 'object'){
			var key;
			var len = obj.length;
			var count = 0;
			for (key in obj) {
				count++;
				if(count > len)
					continue;
				
				var newlayer = layer + 1;
				var s1 = '';
				var t1 = typeof obj[key];
				if (t1 === 'object'){
					var o = obj[key];	
					s1 = toHtml(tmpid, o, newlayer);
				}else{
					var layerInfo = getLayerModelInfo(tmpid, newlayer);
					var func = " onclick='"+layerInfo["clickFunc"] + "' " + "onmouseover='" +layerInfo["overFunc"]+ "' " + "onmouseout='" +layerInfo["outFunc"]+"' " ;
					var layerCon = "<span class='userContent layercontent"+newlayer+"'"+func+ "id='" +key+"'>" + "<span class='directContent'>"+String(obj[key])  + "</span></span>";
					var userLayerCon = layerInfo["preString"] + layerCon + layerInfo["afterString"];
					s1 = subRe + userLayerCon  + "</div>";
				}
				
				if(t === 'object'){
					var layerInfo = getLayerModelInfo(tmpid, layer);
					var func = " onclick='"+layerInfo["clickFunc"] + "' " + "onmouseover='" +layerInfo["overFunc"]+ "' " + "onmouseout='" +layerInfo["outFunc"]+"' " ;
					var imgStr = "<span class='exptionPicture glyphicon glyphicon-minus-sign' onclick='expImgClicked(this);'></span>"
					var displayCtr = '';
					if(layer >= cfg["showLayerNum"]-1){
						displayCtr = 'style="display: none;"';
						imgStr = "<span class='exptionPicture glyphicon glyphicon-plus-sign' onclick='expImgClicked(this);'></span>"
					}
					var layerContent = "<span class='userContentTag userContent layercontent"+layer+"'"+func+ "id='" +key+"'>"+"<span class='directContent'>"+key+"&nbsp;" +":"  + "</span></span>";
					
					var n =  re + "<div class='ctrImgContainer"+ layer + ' ' +layerInfo["layerClass"]+"'>"+imgStr + "<div style='display:inline-block'>"+layerInfo["preString"] + layerContent + layerInfo["afterString"] + "</div></div>" + "<div class='ctrNextLayerContainer" + layer + "'" + displayCtr + ">"+s1+"</div>"+ "</div>";					
					s += n;
				} 			
			}
		}
		return s;
    };
    
    function typeOf(value) {
		var t = typeof value;
		if (value === null)
			t = 'null';      
		if (t === 'object' && value.constructor === Array)
			t = 'array';     
		return t;
    };
}

function expImgClickedArray(o){
	var sib = o.nextSibling;
	if(sib.style.display == '' || sib.style.display == 'block'){
		sib.style.display="none";
		o.setAttribute('class','exptionPicture glyphicon glyphicon-plus-sign');
		o.innerText = "...";
	}
	else{
		sib.style.display="block";
		o.setAttribute('class','exptionPicture glyphicon glyphicon-minus-sign');
		o.innerText = "";
	}
}

function expImgClicked(o){
	var pN = o.parentNode;
	var sib = pN.nextSibling;
	if(sib.style.display == '' || sib.style.display == 'block'){
		sib.style.display="none";
		o.setAttribute('class','exptionPicture glyphicon glyphicon-plus-sign');
	}
	else{
		sib.style.display="block";
		o.setAttribute('class','exptionPicture glyphicon glyphicon-minus-sign');
	}
}

function clickRootDefault(v){
	//console.log(v);
};

function mouseOverDefault(v){
	//console.log(v);
};

function mouseOutDefault(v){
	//console.log(v);
};