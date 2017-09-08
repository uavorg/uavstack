/**
 * 
 */
function DBPoolTool(app) {
	var _this=this;
	
	// TODO ---------------------------------------数据连接池-----------------------------------------------
	
	/**
	 * 创建DBPool显示Button
	 */
	this.buildDBPoolButton=function(sObj) {
		
		var sb=new StringBuffer();
		
		sb.append("<span class=\"componentExpandButton componentExpandButtonStyle2\" style='font-size:14px;' onclick='appDBPool.openDBPoolChart("+JSON.stringify(sObj)+",true)'>数据库连接池</span>");
		sb.append("<div style='display:none;' id='"+sObj.url+"_DBPoolChartCtn'><div id='"+sObj.url+"_DBPoolChart_Title'></div></div>");
		
		return sb;
	};
	
	/**
	 * 打开数据连接Chart
	 */
	this.openDBPoolChart=function(sObj,needOpenClose) {
		
		if (needOpenClose==true) {
			app.controller.openClose(sObj.url+"_DBPoolChartCtn");
		}
		
		var clientData=app.mdata("monitor.client");
		
		if (clientData==undefined) {
			return;
		}
		
		var titleComp=HtmlHelper.id(sObj.url+"_DBPoolChart_Title");
		
		//根据ClientID提取监控数据
		var moData=clientData[sObj.clientID];
		
		var metrics=moData["metric"];
		
		var titleSB=new StringBuffer();
		
		var dbpTypes=new Map();
		
		//展示扩展数据
		for(var metric in metrics) {
			
			/**
			 * 只有扩展指标才是DBPool相关数据
			 */
			if (metric.indexOf("EXT")!=0) {
				continue;
			}
			
			/**
			 * 把每个Datasource分开显示
			 */
			var displayName=metric.substr(4);
			
			var dbpType=displayName.split("_")[0];
			
			var dbpDisplay=dbpTypes.get(dbpType);
			
			if (dbpDisplay==undefined) {
				dbpDisplay=new StringBuffer();
				dbpTypes.put(dbpType,dbpDisplay);
			}
			
			var metricDataList=metrics[metric];
			
			//为title提取最近的一个值
			var latestData=app.controller.getMODetaWithLast(metricDataList,false)[1];
			
			dbpDisplay.append("<div class='indexItemContent'>"+displayName+":<span class='osRate'>"+latestData+"</span></div>");
		}
		
		
		//显示datasource
		for(var i=0;i<dbpTypes.mapValues.count();i++) {
			var dbpDisplay=dbpTypes.mapValues.get(i);
			titleSB.append(dbpDisplay.toString());
		}
		
		//设置summary
		var titleStr=titleSB.toString();
		
		if (titleStr=="") {
			titleStr="无数据连接池监控数据";
		}
 		
		titleComp.innerHTML=titleStr;
	};
}