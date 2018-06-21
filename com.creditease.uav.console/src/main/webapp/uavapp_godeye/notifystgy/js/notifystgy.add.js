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
					if(!json.type||json.type=="stream"){
						var cJson = {
								"expr" : json.expr,
								"type" :"stream"
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
					}else{
						conditions.push(json);
					}
					
					
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

		
		if(conditions.length == 0 && mName != "procCrash"){
			$("#addNotifyErrMSG").text("条件不能为空");
			return false;
		}
		
		/**
		 * 策略处理 begin
		 */
		//校验
		var stgyExpHtmlObjs = document.getElementsByName("stgy_exp_html"),stgyExpCheck=true;
		var stgyConvergences = document.getElementsByName("stgy_convergence_html");
		if(stgyExpHtmlObjs.length==0 && mName != "procCrash"){
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
		var relations=[],relationsHtmls = [],convergences=[];
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
			convergences[index] = stgyConvergences[index].innerText;
		});
		/**
		 * 策略处理 end
		 */
		 
		
		/**
		 * 格式判断，区分分隔符中间为空的内容 begin
		 */
		var instancesArray = new Array();
		var instancesValues = instances.split(",");
		
		/**
		 * 解决jdbc://orcale预警策略配置，由于画像数据带","导致预警策略instances保存不正确的问题
		 * 比如:127.0.0.1:8080#smsgateway#jdbc:oracle://localhost1:1521,localhost2:1521/username会保存为2个instances
		 * 一个是127.0.0.1:8080#smsgateway#jdbc:oracle://localhost1:1521
		 * 一个是localhost2:1521/username
		 * 
		 * 所以需要针对客户端url中带","进行特殊处理，如果通过","分割后的instances不是已客户端协议开头，则需要追溯到上一个instances进行追加，
		 * 如果所有的instances都不是已客户端协议开头，则保留原逻辑进行保存
		 */
		if(fName.indexOf("client")==0){
			// 客户端支持的"协议"
			var protocol={
					"http":"http",
					"https":"https",
					"jdbc":"jdbc",
					"mq":"mq",
					"redis":"redis",
					"elasticsearch":"elasticsearch",
					"mongo":"mongo"
					};
			
			var invalidIndexs = new Array();
			
			$.each(instancesValues,function(index,value){
				value = value.trim();
				var valueArr = value.split("#");
				var client = valueArr[valueArr.length-1];
				var p = client.split(":")[0];
				if(protocol[p]==undefined){
					invalidIndexs.push(index);
				}
			});
			
			if(invalidIndexs.length>0 && invalidIndexs.length!=instancesValues.length){
				$.each(invalidIndexs,function(index,value){
					var ins = instancesValues[value-1];
					var valueArr = ins.split("#");
					var client = valueArr[valueArr.length-1];
					var p = client.split(":")[0];
					if(ins!=undefined && protocol[p]!=undefined){
						instancesValues[value] = ins+","+instancesValues[value];
						instancesValues[value-1] = "";
					}
				});
			}
		}
		
		$.each(instancesValues,function(index,value){
			value = value.trim();
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
				"relationsHtmls":relationsHtmls,
				"convergences":convergences
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
