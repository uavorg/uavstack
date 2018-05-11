//渲染主页菜单
function loadMenuView(menu_txt){
	var menuList=document.getElementById("menuList");
	var obj=eval("("+menu_txt.DATA+")");
	for(var j=0;j<obj.menu.length;j++){
		var para=document.createElement("li");
		var para1=document.createElement("a");
		para1.innerHTML=obj.menu[j].title;
		para1.setAttribute("href",obj.menu[j].url);
		para.appendChild(para1);
		menuList.appendChild(para);
	}

	$("#mainTitle").text(obj.title);
}

function loadUserSetView(){
	var userInfo = document.createElement("a");
	userInfo.innerHTML= "&nbsp;" + loginUser.userId;
	userInfo.setAttribute("href","#");
	userInfo.setAttribute("class","icon-user");

	var para=document.createElement("li");
	para.appendChild(userInfo);

	var menuList=document.getElementById("menuList");
	menuList.appendChild(para);
}

function loadAppicos(appObj)
{
	var content=document.getElementById("contentList");
	length = 0;
	$.each(appObj,function(appid,appInfo){
		
		appInfo = eval("("+appInfo+")");
		
		//app img
		var appImg=document.createElement("img");
		appImg.setAttribute("src",appInfo.url+"/appIco.png");
		appImg.setAttribute("class","appIco");

		var appA=document.createElement("a");
		appA.setAttribute("href","javascript:junmpApp('"+appid+"');");
		appA.setAttribute("target","_top");
		appA.appendChild(appImg);

		//app text
		var appText=document.createElement("span");
		appText.innerHTML = appInfo.title;

		//app
		var app=document.createElement("div");
		app.setAttribute("class","appDiv");
		app.appendChild(appA);
		app.appendChild(document.createElement("p"));
		app.appendChild(appText);

		content.appendChild(app);
		
		length++;
	});

}

function change()
{
	var width = document.body.clientWidth-10;
	var num=0;
	var floatNum=0;
	var paddingSum=0;

	/*
		间距计算；若设备为平板或PC，则为大图标；否之，则图标相应缩小；
		包裹块大小为120px,若图标数量过多，且屏幕分辨率大于1200px,则每行最多10个图标；
		若图标数量不足以填满整行，则间距按需分配
	*/
	if(width>=758){						
		if(width>=1200 && length>=10){
			num=10;
			paddingSum=width-10*120;
		}
		else if(length*120 >= width){
			floatNum=width/120
			num=parseInt(floatNum);
			paddingSum=width-num*120;
		}
		else{
			num=length;
			paddingSum=width-num*120;
		}

	}
	else{
		if(length*76 >= width){
			floatNum=width/76
			num=parseInt(floatNum);
			paddingSum=width-num*76;
		}
		else{
			num=length;
			paddingSum=width-num*76;
		}

	}
	var padding=paddingSum/(2*num);
	padding=padding+"px";
	$(".square").css({"margin-left":padding,"margin-right":padding});

}

function show()
{
	var width = document.body.clientWidth-10;
	var num=0;
	var floatNum=0;
	var paddingSum=0;
	if(width>=758){
		if(width>=1200){
			num=10;
			paddingSum=width-10*120;
		}
		else if(length*120 >= width){
			floatNum=width/120
			num=parseInt(floatNum);
			//alert(num);
			paddingSum=width-num*120;
		}
		else{
			num=length;
			paddingSum=width-num*120;
		}

	}
	else{
		if(length*76 >= width){
			floatNum=width/76
			num=parseInt(floatNum);
			paddingSum=width-num*76;
		}
		else{
			num=length;
			paddingSum=width-num*76;
		}

	}

	var padding=paddingSum/(2*num);
	padding=padding+"px";
}

var length;//获取图标的个数，图表个数和间距计算有相应的联系
function initMainAppIco(data){ //rs 接口获取数据后初始化
	var appObj=eval("("+data+")");
	loadAppicos(appObj);
	show();
	change();
	window.onresize = change;
}

function junmpApp(appid){
	window["cachemgr"].put(uavGuiCKey + "junmpApp",appid);
	window.location.href = "rs/gui/jumpAppPage";
}

$("document").ready(
		/**
		 * userlogger记录,通过用ping提交到filter  begin
		 * 同时会进行会话check（ping逻辑）
		 */
		guiPing_RSClient(mainTempInit(),"jumpmain","","主页")
		/**
		 * userlogger记录,通过用ping提交到filter  end
		 * 
		 * 
		 */
	
);

function mainTempInit(){
	/**
	 * 按main导航右侧展示顺序
	 */
	loadUserSetView();
	loadMainMenu_RSClient();
	loadUserManageInfo_RSClient();
}

