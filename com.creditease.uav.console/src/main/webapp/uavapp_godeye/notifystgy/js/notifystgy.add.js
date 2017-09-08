function saveNotify(checkIsExist){

	var commitInfo={},saveIsOk=false,saveKey;
	
	
	if(checktSaveInfo()){
		saveIsOk=true;
	}
	
	if(checkIsExist && saveIsOk){
		checkUpNotifyStgy_RestfulClient(saveKey,commitInfo);
	}else if(saveIsOk){
		updateNotify_RestfulClient(commitInfo);
	}
	
	
	
	function checktSaveInfo(){
		$("#addNotifyErrMSG").text("");
		var fName = HtmlHelper.inputXSSFilter(selUiConf["userInput"]["notifyNameF"]);
		var mName = HtmlHelper.inputXSSFilter(selUiConf["userInput"]["notifyNameM"]);
		var iName = HtmlHelper.inputXSSFilter(selUiConf["userInput"]["notifyNameI"]);
		var instances = HtmlHelper.inputXSSFilter($("#notifyInstances").val());
		var desc = HtmlHelper.inputXSSFilter($("#notifyDesc").val());
		var owner = HtmlHelper.inputXSSFilter($("#owner").val());

		if(fName=="log" && mName.length==0){
			$("#addNotifyErrMSG").text("必输项不能为空");
			return false;
		}else
		if(!fName || !desc){
			$("#addNotifyErrMSG").text("必输项不能为空");
			return false;
		}else if(iName && !mName){
			$("#addNotifyErrMSG").text("指标名不能为空");
			return false;
		}
		
		var scope = "F";
		if(mName){
			scope="M";
			fName+="@"+mName;
		}
		if(iName){
			scope="I";
			fName+="@"+iName;
		} 
		
		var conditions = new Array(),actions = {};
		var div = document.getElementById("objectDiv");
		var spans = div.getElementsByTagName("span");
		$.each(spans,function(index,obj){
			if (obj.id.indexOf("_stgySpan") >= 0) {//策略
				try{
					obj.textContent = HtmlHelper.inputXSSFilter(obj.textContent);
					var json = JSON.parse(obj.textContent);
					/**
					 * 策略
					 */
					var cJson = {
						"expr" : json.expr
					};
					if (json.range) {
						cJson["range"] = parseInt(json.range);
					}
					if ("0" != json.func && "count" == json.func) {
						cJson["func"] = "count>" + json.cparam;
					} else if ("0" != json.func) {
						cJson["func"] = json.func;
					}
					
					cJson["id"]=json.id;
					conditions.push(cJson);
					
				}catch(e){
					console.log(e);
				}
				
			}else if(typeof obj.textContent == "string" && obj.textContent.indexOf("type")>=0){//动作
				try{
					obj.textContent = HtmlHelper.inputXSSFilter(obj.textContent);
					var json = JSON.parse(obj.textContent);
					/**
					 * 动作
					 */
					if(json.type){
						/**
						 * 因为后台需要的是：一个string里面是个数组 ~
						 */
						var result="[",fi=false;
						$.each(json.value,function(index,value){
							if(fi){
								result+=",";
							}
							result+="\"";
							result+=value;
							result+="\"";
							fi=true;
						});
						result+="]";
						actions[json.type]=result; 
					}
					
				}catch(e){
					console.log(e);
				}
			}
		});

		
		if(conditions.length ==  0){
			$("#addNotifyErrMSG").text("条件不能为空");
			return false;
		}
		
		/**
		 * 策略处理 begin
		 */
		//校验
		var stgyExpHtmlObjs = document.getElementsByName("stgy_exp_html"),stgyExpCheck=true;
		if(stgyExpHtmlObjs.length==0){
			$("#addNotifyErrMSG").text("触发策略不能为空");
			return false;
		}
		$.each(stgyExpHtmlObjs,function(index,obj){
			if(obj.innerHTML.indexOf("whereStgyEdit-delete")>=0){
				stgyExpCheck=false;
				return;
			}
		});
		if(!stgyExpCheck){
			$("#addNotifyErrMSG").text("触发策略存在错误");
			return false;
		}

		//赋值
		var relations=[],relationsHtmls = [];
		$.each(stgyExpHtmlObjs,function(index,obj){
			var relation = obj.innerText;
			var resultHtml = obj.innerHTML;
			$.each(conditions,function(number,obj){
				//格式化，获取表达式公式
				var checkStr = StgyClass.formatShowWhere(obj);
				relation = relation.replaceAll(checkStr,"["+number+"]"); //替换成下标			
			});
			
			relations[index]= relation;//赋值表达式
			relationsHtmls[index]= resultHtml;//赋值用户操作html
		});
		
		/**
		 * 策略处理 end
		 */
		 
		
		/**
		 * 格式判断，区分分隔符中间为空的内容 begin
		 */
		var instancesArray = new Array();
		var instancesValues = instances.split(",");
		$.each(instancesValues,function(index,value){
			value = $.trim(value);
			if(value!=""){
				instancesArray.push(value);
			}
		});
		if(!instancesArray || instancesArray.length==0){
			instances = [];
		}else{
			instances = instancesArray;
		}
		/**
		 * 格式判断，区分分隔符中间为空的内容 end
		 */
		
		var result={
				"instances":instances,
				"desc":desc,
				"conditions":conditions,
				"scope":scope,
				"action":actions,
				"owner":owner,
				"relations":relations,
				"relationsHtmls":relationsHtmls
		}
		commitInfo[fName]=result;
		saveKey = fName;
		
		console.log(result);

		return true;
	}
}

function add_IsExistErrorMSG(){
	$("#addNotifyErrMSG").text("策略已经存在");
}

String.prototype.replaceAll = function(s1,s2){
	return this.replace(new RegExp(s1, 'g'), s2);
}
