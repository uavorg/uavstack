/**
 * MVC framework for one signle page
 */
function AppHubMVC(_mvcConfig) {

	var mvcConfig={
		//init all
		init:function(){},
		//data processing from service
		datas: {
			/**
			 * data id:{
			 *    url:function() {
			 *    	return url;
			 *    }, 
			 *    method: GET/POST/DELETE
			 *    body: function() {
			 *      return body;
			 *    }
			 *    rtype: response type
			 *    interval: interval time to get the data			 
			 *    init: if get the data when init   
			 *    prepare: function(data) {
			 *       //preparing data before calling data model
			 *       return one data obj;
			 *    },
			 *    error:function(data) {
			 *    }
			 * }
			 */
		},
		search:{
			/**
			 * search bar
			 */
		},
		models:{
			/**
			 * model description
			 * model id:{
			 *   field:type or {type:"int/string/",valid:function(val) {
			 *       return true / false;
			 *   }}
			 * }
			 */
		},
		mdatas:{
			/**
			 * the final model data map
			 */
		}		
	};
	
	//merge config
	JsonHelper.merge(mvcConfig, _mvcConfig,true);
	
	//when run starts, sync data from rest service
	function startSyncData() {
		
		for(var dataKey in mvcConfig.datas) {
			
			var dataConfig=mvcConfig.datas[dataKey];
			
			dataConfig=buildDataSyncFunc(dataKey,dataConfig);
			
			if (true==dataConfig["init"]||undefined==dataConfig["init"]) {
				dataConfig["syncDataFunc"]();
			}
			
			if (dataConfig["interval"]!=undefined) {
				setInterval(dataConfig["syncDataFunc"],dataConfig["interval"]);
			}		
		}
		
		//loader.show();
	}
	//handle data to mapping models
	function handleModels(data,dataKey,noNeedPrepare) {
		var d=data;
		
		var dataConfig=mvcConfig["datas"][dataKey];
		
		var views=dataConfig["views"];
		
    	//check if we can update views this time
    	if (undefined!=views&&false!=views["lock"]) {
    		LogHelper.debug(this, "Can't update data now");
    		return;
    	}
    	
    	//load global view data change map
    	views=dataConfig["views"]={lock:true};
    	
		//prepare data before modeling
    	if (dataConfig["prepare"]!=undefined&&true!=noNeedPrepare) {
    		d=dataConfig["prepare"](data);
    	}
    	
    	//store the latest incoming data for search
    	dataConfig["ldata"]=d;
    	
    	LogHelper.debug(this, d);
    	
    	//read models's id
    	var modelsSeq=dataConfig["models"];
    	if (undefined==modelsSeq) {
    		return;
    	}
    	
    	//load global model data map
    	var mdatas=mvcConfig["mdatas"];
    	
    	var searchObj=mvcConfig["search"];
    	
    	var searchKeyword=searchObj["keyword"];
    	
    	for(var i=0;i<modelsSeq.length;i++) {
    		var modelId=modelsSeq[i];
    		
    		var mObj=mvcConfig["models"][modelId];
    		//check if model obj exists
    		if (mObj==undefined) {
    			LogHelper.err(this,"Model["+modelId+"] NO Exist.");
    			continue;
    		}
    		
    		//load current model data
    		var mdata=mdatas[modelId];
    		
    		if (mdata==undefined) {
    			mdata={};
    			mdatas[modelId]=mdata;
    		}
    		
    		//create view model obj
    		views[modelId]={
    			onnew:{},
    			onupdate:{},
    			ondel:{}
    		};  
    		
    		//get pkey
			var pkey=mObj["pkey"];
			
			if (undefined==pkey) {
				LogHelper.err(this,"Model["+modelId+"]'s PKey NO Exist.");
				break;
			}
			
			//get filter keys
			var fkeys=mObj["fkeys"];
			
			//map for exist pkeyvalue in new coming data
			var pkeyMap=new Map();
			
			//for new & update & delete
    		for(var j in d) {
    			
    			var dObj=d[j];
    			
    			if (typeof dObj =="function") {
    				continue;
    			}
    			
    			//create newObj
    			var newObj={};
    			
    			//record the obj keys
    			var objKeys=[];
    			
    			for(var attr in mObj) {
    				
    				if (attr=="pkey"||attr=="isupdate"||attr=="onnew"||attr=="onupdate"||attr=="ondel"||attr=="fkeys"||attr=="onrefresh") {
    					continue;
    				}
    				
    				objKeys[objKeys.length]=attr;
    				
    				var attrObj=mObj[attr];
    				
    				var mappingkey;
    				var allowEmpty=false;
    				if (typeof attrObj == "string") {
    					mappingkey=attrObj;
    					
    				}
    				else {
    					mappingkey=attrObj["key"];
    					allowEmpty=attrObj["allowEmpty"];
    				}
    				
    				if (true==attrObj["keepLatest"]) {
    					keepLatestKeys[keepLatestKeys.length]=mappingkey;
    				}
    				
    				var mappingKeyArray=mappingkey.split("/");
    				
    				var tmp=dObj;
    				
    				for(var m=0;m<mappingKeyArray.length;m++) {
    					
    					var curTmpKey=mappingKeyArray[m];
    					
    					if (tmp==undefined) {
    						LogHelper.err(this,"Model["+modelId+"]'s Mapping Keys["+mappingkey+"]'s value NO Exist:"+dObj);
    						break;
    					}
    					
    					tmp=tmp[curTmpKey];
    				}
    				
    				if (tmp!=undefined||true==allowEmpty) {
    					newObj[attr]=tmp;
    				}
    			}
    			
    			/**
    			 * filter key search for the new data object
    			 */
    			if (undefined!=fkeys&&fkeys.length>0) {
    				
    				if (undefined!=searchKeyword&&""!=searchKeyword&&"@ALL"!=searchKeyword) {
    					
	    				var ismeet=false;
	    				
		    			for(var fi=0;fi<fkeys.length;fi++) {
		    				var fkey=fkeys[fi];
		    				var value=newObj[fkey];
		    				
		    				if (undefined==value) {
		    					continue;
		    				}
		    				//meet the search condition
		    				if (value.indexOf(searchKeyword)>-1) {
		    					ismeet=true;
		    					break;
		    				}
		    			}
		    			
		    			//if can't meet condition, go next data
		    			if (!ismeet) {
		    				continue;
		    			}
    				}
    				else if ("@ALL"==searchKeyword) {
    					searchObj["keyword"]=undefined;
    				}
    			}
 
    			//get pkey value
    			var pkeyValue=dObj[pkey];
    			
    			//put the exist pkeyvalue obj in pkeymap
    			pkeyMap.put(pkeyValue,dObj);
    			
    			//read current obj with pkey value
    			var curObj=mdata[pkeyValue];
    			
    			if (curObj==undefined) {
    				//NEW DATA
    				mdata[pkeyValue]=newObj;
    				
    				views[modelId].onnew[pkeyValue]=newObj;
    				
    				/**
    				 * support 'create' method for a field
    				 */
    				for(var key in newObj) {
    					if (mObj[key]["create"]) {
    						newObj[key]=mObj[key]["create"](newObj);
    					}
    				}
    				
    				continue;
    			}
    			
    			//check if need update
    			var needUpdate=true;    			
    			if (mObj["isupdate"]!=undefined) {
    				needUpdate=mObj["isupdate"](curObj,newObj);
    			}
    			
    			//clone the current object
    			var cloneCurObj=JsonHelper.clone(curObj);
    			
    			for(var ki=0;ki<objKeys.length;ki++) {
    				
    				var objkey=objKeys[ki];
    				
    				var mObjAttr=mObj[objkey];
    				
    				if (typeof mObjAttr == "object") {
    					/**
    					 * customized update for a field
    					 */
    					if (undefined!=mObjAttr["update"]) {
    						
    						//put the cloneCurObj into update. and update the curObj
    						curObj[objkey]=mObjAttr["update"](cloneCurObj,newObj);
    						
    						continue;
    					}
    				}
    				
    				/**
    				 * common update for a field
    				 */
    				if (needUpdate==true) {
    					curObj[objkey]=newObj[objkey];
    				}
    			}

    			//UPDATE DATA
    			views[modelId].onupdate[pkeyValue]=curObj;
    			
    			mdata[pkeyValue]=curObj;
    		}
    		
    		//find delete data
    		for(var key in mdata) {
    			if (!pkeyMap.contain(key)) {
    				//DELETE DATA
    				views[modelId].ondel[key]=mdata[key];
    				
    				//remove data from model data
    				delete mdata[key];
    			}
    		}
    	}

		setTimeout(function(){
			handleViews(views,modelsSeq);
		});
	}
	
	function handleViews(views,modelsSeq) {
		var models=mvcConfig["models"];
		
		//views["lock"]=true;
		
		try {
			
			for(var i=0;i<modelsSeq.length;i++) {
				var key=modelsSeq[i];
				var viewModel=views[key];
				var mObj=models[key];
				
				handleViewsEvent(viewModel,mObj,key,"onnew");
				handleViewsEvent(viewModel,mObj,key,"onupdate");
				handleViewsEvent(viewModel,mObj,key,"ondel");
				//after all specific operations, we can get the latest model's data need
				var onrefresh=mObj["onrefresh"];
				
				if (onrefresh!=undefined) {
					try {
						onrefresh(mvcConfig.mdatas[key]);
					}catch(e){
						LogHelper.err(this, "CALL Model["+key+"] Method["+event+"] FAIL.",e);
					}
				}
				
			}
			
		}
		catch(e) {
			LogHelper.err(this, "Views Update Fail.",e);
		}
		
		views["lock"]=false;
	}
	
	function handleViewsEvent(viewModel,mObj,modelId,event)	{
		
		var vEvent=viewModel[event];
		var mEventFunc=mObj[event];
		
		if (vEvent!=undefined&&mEventFunc!=undefined) {
			for(var key in vEvent) {
				try {
					mEventFunc(vEvent[key]);
				}catch(e){
					LogHelper.err(this, "CALL Model["+modelId+"] Method["+event+"] FAIL.",e);
				}
			}
		}
	}
	
	function handleError(data,dataConfig) {
		var errFunc=dataConfig["error"];
    	
    	if (errFunc!=undefined) {
    		errFunc(data);
    	}
	}
	
	//init sync data as a js function then use it in future
	function buildDataSyncFunc(dataKey,dataConfig) {
		
		if (dataConfig["syncDataFunc"]==undefined) {
			
			dataConfig["syncDataFunc"]=function() {
				
				var bodyData=(dataConfig["data"]==undefined)?"":dataConfig["data"]();
				
				/**
				 * NOTE: POST method must have a data body
				 */
				if ((undefined==bodyData||bodyData=="")&&"POST"==dataConfig["method"]) {
					return;
				}
				
				//TODO: currently use JQuery Ajax 暂时吧
				AjaxHelper.call({
		            url: dataConfig["url"](),
		            data: bodyData,
		            async: true,
		            cache: false,
		            type: dataConfig["method"],
		            dataType: dataConfig["rtype"],
		            success: function (data) {
		            	setTimeout(handleModels(data,dataKey));
		            	
		            },
		            error: function (data) {
		            	setTimeout(handleError(data,dataConfig));
		            },
		        });
			};
		}
		
		return dataConfig;
	}
	//search datas
	function doSearch(isall) {
		//set search keyword
		var sObj=mvcConfig["search"];
		
		if (isall) {
			sObj["keyword"]="@ALL";
		}
		else {
			var keyword=sObj["searchbar"].getkeyword();
			
			if (""!=keyword) {
				sObj["keyword"]=keyword;
			}
		}
		
		var modelId=sObj["model"];
		
		//refresh all datas which bind with the modelId
		var datas=mvcConfig["datas"];
		
		for(var dataKeyId in datas) {
			
			var dataConfig=datas[dataKeyId];
			
			var models=dataConfig["models"];
			
			for (var i=0;i<models.length;i++) {
				
				if (models[i]==modelId) {
					//process data
					handleModels(dataConfig["ldata"],dataKeyId,true);
				}
			}
		}
	}
	
	//start app MVC
	this.run=function() {

		var isStartSync=mvcConfig.init();

		this.controller=mvcConfig["controller"];
		
		var modelId=mvcConfig["search"]["model"];

		if (undefined!=modelId) {
			
			mvcConfig["search"]["searchbar"]=new AppHubMVCSearchBar({
				id:"AppHubMVCSearchBar",
				search:function(isall) {
					doSearch(isall);
				},
				tip:mvcConfig["search"]["tip"]
			});
		}
		
		if (false==isStartSync) {
			return;
		}
		
		startSyncData();
	};
	
	// start sync data
	this.startSync=function() {
		startSyncData();
	};
	
	//refresh data if need
	this.refresh=function(dataId) {
		
		dataConfig=mvcConfig.datas[dataId];
		
		if (dataConfig==undefined) {
			return;
		}
		
		dataConfig=buildDataSyncFunc(dataId,dataConfig);
		
		dataConfig["syncDataFunc"]();
	};
	
	//return model data for model id
	this.mdata=function(modelId) {
		
		if (modelId==undefined) {
			return mvcConfig.mdatas;
		}
		
		return mvcConfig.mdatas[modelId];
	};
}

/**
 * MVC Builder
 */
function AppHubMVCBuilder() {
	
	this.build=function(_mvcConfig) {
		var mvc=new AppHubMVC(_mvcConfig);
		return mvc;
	};
}
/**
 * MCV loader
 */
function AppHubMVCLoader(id) {
	
	this.cid=id;
	
	var str="<div id=\""+id+"\" class=\"AppHubMVCLoading\">加载中...</div>";
	
	document.body.innerHTML+=str;
	
	this.show=function(text,width) {
		
		if (text!=undefined) {
			HtmlHelper.id(this.cid).innerHTML=text;
		}
		
		if (width!=undefined) {
			HtmlHelper.css(this.cid,{width:width+"px"});
		}
		
		HtmlHelper.css(this.cid,{display:"block"});
	};
	
	this.hide=function() {
		HtmlHelper.css(this.cid,{display:"none"});
	};
}
/**
 * search bar
 */
function AppHubMVCSearchBar(_config) {
	
	var config={
		id:"",	
	};
	
	JsonHelper.merge(config, _config, true);
	
	var tip=(config["tip"])?config["tip"]:"";
	
	var str="<div id=\""+config["id"]+"\" class=\"AppHubMVCSearchBar\">";
	
	str+="	<input id='"+config["id"]+"_keyword' type='text' class='form-control AppHubMVCSearchBarInputText' value='' placeholder='"+tip+"'/>"+
			"	<div class='btn-group'>" +
			"<button id='"+config["id"]+"_searchbtn'  type='button' class='btn btn-default' >" +
					"<span class='glyphicon glyphicon-search'></span>" +
					"</button>" +
			"	<button id='"+config["id"]+"_searchallbtn'  type='button' class='btn btn-default' >" +
					"<span class=' glyphicon glyphicon-th'></span>" +
					"</button>" +
					"</div>"+
			"</div>";
	
	document.body.innerHTML+=str;
	
	if (true==config["bottom"]) {
		HtmlHelper.css(config["id"],{position:"absolute",bottom:"0px",zIndex:20000});
	}

	HtmlHelper.id(config["id"]+"_searchbtn").onclick=function(e) {
		if (config["search"]!=undefined) {
			config["search"](false);			
		}
	};
	
	HtmlHelper.id(config["id"]+"_searchallbtn").onclick=function(e) {
		if (config["search"]!=undefined) {
			HtmlHelper.id(config["id"]+"_keyword").value="";
			config["search"](true);
		}
	};
	
	//get key word
	this.getkeyword=function() {
		return HtmlHelper.id(config["id"]+"_keyword").value;
	};
}

//global app mvc builder for one single page
window["appmvc"]=new AppHubMVCBuilder();
