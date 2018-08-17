/**
 * TODO 窗体初始化,必须在body后面加载,因为窗口容器会追加到body
 */
window.winmgr.build({
	id : "winList",
	height : "auto",
	"overflow-y" : "auto",
	order : 998,
	theme : "win"
});

var ctrlAuthorConfig={
		"nodectrl":"UAV节点远程控制",
		"procctrl":"服务进程远程控制",
		"grafana":"情报中心管理"
};

var mtableConfig = {
	id:"tableList",
	pid:"winList",
	openDelete:true,
	key:"id",
	pagerSwitchThreshold:600,
	pagesize : 20,
	head:{
		    emailListName : ['授权组', '27%'],
			createTime : ['创建时间', '27%'],
			operationTime : ['操作时间', '27%'],
			operationUser : ['操作人', '19%']
	    },
	cloHideStrategy:{
		1100:[0,1,2,3],
		1000:[0,2,3],
		800:[0,2,3],
		500:[0,2],
		400:[0]
	}
};

var modalConfig = {
		head:"删除",
		content:"用户输入提示信息",
		callback:"PageClass.ajaxRemoveCommit()"
};

var jsonConfig = {
		   "attribute"   :  {frontSize:14,color:'#008000'},
		   "showLayerNum"   :   3,
		   "model1"      :	[   
						       {keyT:"{}",onClick:""},   
						       {keyT:"<div class='groupLayer0'>{@key}</div>",onClick:""}
					        ]
					        
};
/**
 * TODO 渲染首页列表
 */
function initListDiv() {

	var div = new StringBuffer();
	div.append("<div id=\"srarchDiv\" class=\"AppHubMVCSearchBar\" >");

	div.append('<button class="btn btn-default" type="button" onclick="javascript:showAddDiv()">');
	div.append('<span class="glyphicon glyphicon-plus"></span>');
	div.append('</button>');
	
	div.append("<input id=\"inputValue\" class=\"form-control AppHubMVCSearchBarInputText\"");
	div.append("type=\"text\" placeholder=\"授权组模糊检索\" value=\"\"></input>");
	
	div.append("<div class=\"btn-group\">");
	div.append("<button id=\"searchbtn\" type=\"button\" class=\"btn btn-default\" onclick=\"javascript:loadListData(false);\">");
	div.append("<span class=\"glyphicon glyphicon-search\"></span>");
	div.append("</button>");
	div.append("<button id=\"searchAllbtn\" type=\"button\" class=\"btn btn-default\"  onclick=\"javascript:loadListData(true);\">");
	div.append("<span class=\"glyphicon glyphicon-th\"></span>");
	div.append("</button>");
	div.append("</div>");
	
	div.append("</div>");
	
	HtmlHelper.id("winList").innerHTML += div.toString();
	showListDiv();
};
function initGroupSelect(id,type) {
	var gorupsSet = PageClass.groups.toArray();

	$("#"+id).empty();
	var opStr = new StringBuffer();
	$.each(gorupsSet, function(index, groupName) {
		
		if("edit"==type){
			/**
			 * 编辑页面，检查是否数据为最新
			 */
			//授权组数据是否还存在profile授权中（JAppGroup 设置的值）
			var existJAppGroup = PageClass.groupsMap.get(groupName);
			if(!existJAppGroup){
				/**
				 * 如果已经不存在：跳过不显示，当用户编辑时，也同逻辑。
				 * 
				 * 在用户点击保存时，没显示的不会被选中，则也不会被提交，达到删除的效果。
				 */
				return true;
			}
		}


		var isSel=false; //用户选中
		if(DataBindClass.userEditInfo[groupName]){
			isSel=true;
		};

		opStr.append('<span>');

		if(isSel){
			opStr.append('<input type="checkbox" onclick="javascript:DataBindClass.groupDataBindSet(this,\''+type+'\');" value="'+groupName+'" checked ></input>');
		}else{
			opStr.append('<input type="checkbox" onclick="javascript:DataBindClass.groupDataBindSet(this,\''+type+'\');" value="'+groupName+'"> </input>');
		}
		opStr.append('<span onclick="javascript:DataBindClass.groupDataBindSet(this,\''+type+'\');">'+groupName+'</span>');
		
		if(isSel){
			opStr.append('<span onclick="javascript:DataBindClass.groupData2CtrlAuthorShow(this,\''+type+'\');" class="glyphicon glyphicon-cog" >授权设置</span>');
		}
		opStr.append('</span>');
		opStr.append('<br/>');
	});

	$("#"+id).append(opStr.toString());
	
};
/*初始化checkbox,带响应渲染*/
function initctrlAuthorCheckbox(type,groupName){

	var checkboxId = type+"CtrlAuthor";
	var checkboxName = type+"checkbox";

	var groupCtrlAuthor = DataBindClass.userEditInfo[groupName];
	
	var div = new StringBuffer();
	$.each(ctrlAuthorConfig,function(key,value){
	
		div.append('<span>');
		var checked = false;
		if(groupCtrlAuthor){
			$.each(groupCtrlAuthor,function(ctrlKey,ctrlValue){
				if(key==ctrlKey){
					checked=true; //不判断值，因为存在就是ture，不存在不会有key
					return;
				}
			});
		}
		
		if(checked){
			div.append('<input type="checkbox" value="'+key+'" onclick="javascript:DataBindClass.ctrlAuthorBindSet(this,\''+type+'\');" checked="checked" />');
		}else{
			div.append('<input type="checkbox" value="'+key+'" onclick="javascript:DataBindClass.ctrlAuthorBindSet(this,\''+type+'\');" />');
		}
		 
		div.append('<span onclick="javascript:DataBindClass.ctrlAuthorBindSet(this,\''+type+'\');">'+value+'</span>');
		
		div.append('</span><br/>');
	});

	$("#"+checkboxId).html(div.toString());
};

function showListDiv(){
	window.winmgr.show("winList");
};
function showAddDiv(){
	$("#saveTrCheckbox").hide();
	setHtmlText("addErrorMsg", "");
	$("#addEmailListName").val("");
	DataBindClass.userEditInfo={};
	
	initGroupSelect("addGroupList","save");
	
    $("#addGroupFilterDiv").modal({backdrop: 'static', keyboard: false});
    
};
function showEditDiv(id){

	PageClass.ajaxGProfile();
	

	//init
	$("#editTrTitle").hide();
	$("#editTrBody").hide();
	$("#editTrCheckbox").hide();
	$("#showTr").show();
	$("#editBut").html("编辑");
	$("#editBut").attr("onclick","editSaveButtonSwitch(this)");
	
	setHtmlText("editErrorMsg", "");
	DataBindClass.userEditInfo={};
	
	document.getElementById("editEmailListName").innerHTML = id;
	
	//手动当前页面过滤
	$.each(PageClass.pageEmailList,function(index,obj){
		var emailListName = obj.emailListName;
		//获取选中行
		if(emailListName == id){
			var groupListStr = {};
			//填充选中数据
			$.each(obj.groupList,function(groupName,ctrlAuthorObj){

				//授权组数据是否还存在profile授权中（JAppGroup 设置的值）
				var existJAppGroup = PageClass.groupsMap.get(groupName);
				if(!existJAppGroup){
					/**
					 * 跳过不显示，当用户编辑时，也同逻辑。
					 * 
					 * 在用户点击保存时，没显示的不会被选中，则也不会被提交，达到删除的效果。
					 */
					return true;
				}

				DataBindClass.addGroupInfo(groupName); // 数据填充
				var ctrlStr = new StringBuffer();
				$.each(ctrlAuthorObj,function(ctrlName,ctrlValue){
					ctrlStr.append(ctrlAuthorConfig[ctrlName]);
					ctrlStr.append("：√"); //存在就是true，不存在不会有ctrlName
					ctrlStr.append("<br/>");
				
					DataBindClass.addCtrlAuthorInfo(groupName,ctrlName); //  数据填充，存在就是true，不存在不会有ctrlName
				});
				groupListStr[groupName]=ctrlStr.toString();
			});
			
			var userSelectInfoShowJson = {
					"授权组名称":obj.emailListName,
					"创建时间":obj.createTime,
					"操作时间":obj.operationTime,
					"操作用户":obj.operationUser,
					"GROUP映射":groupListStr
			};

			
			document.getElementById("showTrDiv").innerHTML = jsonObj.asHtml("model1",userSelectInfoShowJson);
			return;
		}
	});
	
    $("#editGroupFilterDiv").modal({backdrop: 'static', keyboard: false});

};
function editSaveButtonSwitch(obj){
	if(obj.innerHTML=="编辑"){
		showEditSaveButton(obj);
	}else if(obj.innerHTML=="保存"){
		PageClass.ajaxEditCommit();
	}
};
function showEditSaveButton(obj){    
	
	obj.innerHTML="保存";
	console.log(obj);
	$("#showTr").hide();
	$("#editGroupList").empty(); //提前做处理，提高用户体验
	
	PageClass.ajaxGProfile();
	initGroupSelect("editGroupList","edit");
	
	$("#editTrTitle").show();
	$("#editTrBody").show();
	
};
function showDelDiv(id){
	PageClass.removeParam.emailListName=id;
	modalConfig.content="是否<span class='confirmMsg'>删除[</span>"+id+"<span class='confirmMsg'>]</span>?";
	showConfirm(modalConfig);
};


function setHtmlText(id, msg) {
	$("#" + id).text(msg);
}


function loadListData(reset){
	if(reset){
		$("#inputValue").val("");
		tableObj.setPageNum(1);  
	}
	
	PageClass.ajaxGGroup(run);

	function run(){
		var filterResult = [],showIndex=0;
		
		//去前后空格
		var input = $("#inputValue").val().trim();
		$("#inputValue").val(input);
		var filter = input.length>0?true:false;
		
		$.each(PageClass.emailList,function(key,str){
			var obj = JSON.parse(str);
			if(obj.state==1 && 
					(!filter || (filter && key.toUpperCase().indexOf(input.toUpperCase())>=0))
					){
				
				filterResult[showIndex++]={
						"emailListName":key,
						"createTime":obj.createTime,
						"operationTime":obj.operationTime,
						"operationUser":obj.operationUser,
						"groupList":obj.groupList,
						"ctrlAuthor":obj.ctrlAuthor
				}
			}
		});

		PageClass.pageEmailList = getPageData(filterResult);
		tableObj.clearTable(); 
		tableObj.setTotalRow(filterResult.length);
		tableObj.addRows(PageClass.pageEmailList);
	}
	
	/**
	 * 当前页不足，自动退一页
	 */
	function getPageData(array){
		var getPagingInfo = tableObj.getPagingInfo();
		var pageNum = getPagingInfo.pageNum;   
		var pageSize = getPagingInfo.pageSize
		var begin = (pageNum-1)*pageSize;
		var end = begin+pageSize;
		var result = array.slice(begin,end);
		
		if(pageNum>1 && result.length==0){
			tableObj.setPageNum(--pageNum);  
			return getPageData(array);
		}else{
			return result;
		}
	}
	
};


//TODO 页面与后台交互类
PageClass = {
	groups : new Set(),     //所有group数据
	groupsMap : new Map(),     //所有group数据(key value 形式)
	emailList:null,         //所有列表数据(含映射group)
	pageEmailList : [],     //当前页面的列表数据(含映射group),页面手动分页
	submitParam : {
		"emailListName" : "",
		"groupList" : "",
		"ctrlAuthor":""
	},
	removeParam:{
		"emailListName" : "",
	},
	ajaxGGroup : function(func) {
		var input = $("#inputValue").val();
		AjaxHelper.call({
			url : "../../rs/godeye/filter/group/query",
			async : true,
			cache : false,
			type : "GET",
			dataType : "json",
			success : function(result) {
				PageClass.emailList = result;
				if(func){
					func();
				}
			},
			error : function(result) {
				console.log(result);
			}
		});
	},
	ajaxGProfile : function() {
		AjaxHelper.call({
			url : "../../rs/godeye/filter/profile/q/cache",
			async : false,
			cache : false,
			type : "GET",
			dataType : "html",
			success : function(result) {
				if (result) {
					result = eval("(" + result + ")");
					result = eval("(" + result["rs"] + ")");
					PageClass.setGroups(result);
				} else {
					console.log("result is empty");
				}
			},
			error : function(result) {
				console.log(result);
			}
		});
	},
	ajaxSaveCommit : function() {
		if (PageClass.submitCheck("save")) {
			AjaxHelper.call({
				url : "../../rs/godeye/filter/group/save",
				data : JSON.stringify(PageClass.submitParam),
				async : true,
				cache : false,
				type : "POST",
				dataType : "json",
				success : function(result) {
					if (result.code == "00") {
						$("#addGroupFilterDiv").modal('hide');
						loadListData(false,true);
					} else {
						setHtmlText("addErrorMsg", result.msg);
					}
				},
				error : function(result) {
					console.log(result);
				}
			});
		}
	},
	ajaxEditCommit : function() {
		if (PageClass.submitCheck("edit")) {
			AjaxHelper.call({
				url : "../../rs/godeye/filter/group/edit",
				data : JSON.stringify(PageClass.submitParam),
				async : true,
				cache : false,
				type : "POST",
				dataType : "json",
				success : function(result) {
					if (result.code == "00") {
						$("#editGroupFilterDiv").modal('hide');
						loadListData(false,true);
					} else {
						setHtmlText("addErrorMsg", result.msg);
					}
				},
				error : function(result) {
					console.log(result);
				}
			});
		}
	},
	ajaxRemoveCommit : function() {
		AjaxHelper.call({
			url : "../../rs/godeye/filter/group/remove",
			data : JSON.stringify(PageClass.removeParam),
			async : true,
			cache : false,
			type : "POST",
			dataType : "json",
			success : function(result) {
				loadListData(false);
			},
			error : function(result) {
				console.log(result);
			}
		});
	},
	setGroups : function(profileObj) {
		PageClass.groups.clear();
		
		PageClass.groupsMap.clear();
		for ( var key in profileObj) {
			var index = key.indexOf("@");
			if (index > 0) {
				var groupName = key.substring(0, index);
				PageClass.groups.add(groupName);
				PageClass.groupsMap.set(groupName,"existJAppGroup");
			}
		}
	},
	submitCheck : function(type) {
		var emailLName,result=false;
		
		if(type=="save"){
			emailLName = $("#addEmailListName").val().trim();
			$("#addEmailListName").val(emailLName);
			emailLName = HtmlHelper.inputXSSFilter(emailLName);
			result = thisCheck("addErrorMsg");
		}else if(type=="edit"){
			emailLName = $("#editEmailListName").text().trim();
			emailLName = HtmlHelper.inputXSSFilter(emailLName);
			result = thisCheck("editErrorMsg");
		}
				
		if(result){
			PageClass.submitParam = {
				"emailListName" : emailLName,
				"groupList" : DataBindClass.userEditInfo
			}
		}
		return result;
			
		function thisCheck(id) {
			var c = false;

			if (!emailLName) {
				setHtmlText(id, "未输入授权组名称");
			} else if (Object.keys(DataBindClass.userEditInfo).length==0) {
				setHtmlText(id, "未映射GROUP");
			} else {
				c = true;
				setHtmlText(id, "");
			}
			return c;
		}

	}
};
DataBindClass={
		userEditInfo:{    //用户编辑邮箱组数据(含group映射\group授权),add\edit页面
			/**
			 * 数据结构：不授权则不赋值，赋值则一定为true
			 * "groupListName":{"ctrlAuthorName":"value"}
			 * 例：
			  	"UAV":{"nodectrl":"T","procctrl":"T"},
			    "电签":{"nodectrl":"T"}，
			    "HUBBLE":{}
			 * 
			 */
		},
		groupDataBindSet:function(obj,type){  //group数据bind设置
			$("#"+type+"TrCheckbox").hide();
			var checkbox;
			if(obj.type=="checkbox"){
				checkbox = obj;
			}else{
				checkbox = obj.parentNode.getElementsByTagName('input')[0];
				checkbox.checked=checkbox.checked?false:true;
			}
		
			if(checkbox.checked==true){
				var span = document.createElement("span");
				span.setAttribute("class","glyphicon glyphicon-cog");
				span.setAttribute("onclick","javascript:DataBindClass.groupData2CtrlAuthorShow(this,\'"+type+"\');");
				span.innerText="授权设置";
				obj.parentNode.appendChild(span);
				
				DataBindClass.addGroupInfo(checkbox.value);
			}else{
				var removeObj = obj.parentNode.getElementsByTagName('span')[1];
				obj.parentNode.removeChild(removeObj);

				DataBindClass.removeGroupInfo(checkbox.value);
			}
			
		},
		groupData2CtrlAuthorShow:function(obj,type){ //group授权渲染
			var checkbox = obj.parentNode.getElementsByTagName('input')[0];
			$("#"+type+"CtrlAuthorTitle").html(checkbox.value);
			initctrlAuthorCheckbox(type,checkbox.value);
			$("#"+type+"TrCheckbox").slideDown();
		},
		ctrlAuthorBindSet:function(obj,type){ //group授权bind设置
			var checkbox;
			if(obj.type=="checkbox"){
				checkbox = obj;
			}else{
				checkbox = obj.parentNode.getElementsByTagName('input')[0];
				checkbox.checked=checkbox.checked?false:true;
			}
			
			var groupName = obj.parentNode.parentNode.parentNode.getElementsByTagName('div')[0].innerHTML;
			var ctrlAuthorValue = checkbox.value;
			if(checkbox.checked==true){
				DataBindClass.addCtrlAuthorInfo(groupName,ctrlAuthorValue);
			}else{
				DataBindClass.removeCtrlAuthorInfo(groupName,ctrlAuthorValue);
			}
			
		},			
		addGroupInfo:function(groupName){
			var newCtrlAuthor = {};//所有授权动作默认为空
			DataBindClass.userEditInfo[groupName]=newCtrlAuthor;
		},
		removeGroupInfo:function(groupName){
			delete DataBindClass.userEditInfo[groupName];
		},			
		addCtrlAuthorInfo:function(groupName,ctrlAuthorName){
			var obj = DataBindClass.userEditInfo[groupName];
			delete obj[ctrlAuthorName];
			obj[ctrlAuthorName]="T";
		},
		removeCtrlAuthorInfo:function(groupName,ctrlAuthorName){
			var obj = DataBindClass.userEditInfo[groupName];
			delete obj[ctrlAuthorName];
		}			
};
var tableObj = new AppHubTable(mtableConfig);
var jsonObj = new AppHubJSONVisualizer(jsonConfig);
//TODO js入口
$(document).ready(function() {
	initListDiv();
	tableObj.sendRequest = function(){loadListData(false);};
	tableObj.cellClickUser = showEditDiv;	
	tableObj.delRowUser = showDelDiv;
	tableObj.initTable();
});
