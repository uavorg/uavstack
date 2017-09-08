/**
 * Java Thread Analysis Tool App
 */
function JTATool(app) {
	
	this.buildJTAWndButton = function(jsonObj, appInstMOId, isJse) {
		if(isJse) {
			return '';
		}
		
		var sObj = {
			appname: jsonObj.appname,
			appid: jsonObj.appid,
			appurl: jsonObj.appurl,
			hostport: jsonObj.hostport,
			ip:jsonObj.ip
		}
		
		var sObjStr=JSON.stringify(sObj);
		
		var html = "<div class='contentDiv' >"
			+ "<span class=\"componentExpandButton componentExpandButtonStyle3\" style='font-size:14px;' "
				+ "onclick='app.controller.showWindow("+sObjStr+",\"AppJTAListWnd\",\"buildAppJTAListWnd\",\"runAppJTAListWnd\")'>线程分析</span>"
			+ "</div>";
		return html;
	}
	
	this.buildAppJTAListWnd = function(sObj) {
		var appInfo = {
			appuuid: "",
			appid: "",
			appurl: "",
			appname: ""
		};
		
		if (sObj != undefined) {
			appInfo = sObj;
			appInfo["appname"] = (sObj["appname"] == undefined || sObj["appname"] == "" || sObj["appname"] == "undefined") 
							     ? sObj["appid"] : sObj["appname"];
		}
		
		var html = '';
		html="<div class=\"appDetailContent\" style='background:#333;' >" +
	        "<div class=\"topDiv\" >" +
	        "<span class=\"tagTitle\">"+appInfo["appname"]+"</span><br/>"+
	        "<span class=\"idTitle\" >"+appInfo["appurl"]+"</span>" +
	        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppJTAListWnd','destroyAppJTAListWnd','AppInstChartWnd')\"></div>" +
	        "</div></div>";
		
		html+="<div class=\"AppHubMVCSearchBar\" align='left' style='background:#eee;'>" +
			"&nbsp;<button type=\"button\" class=\"btn btn-info\" title=\"启动分析\" onclick='appJTA.invokeThreadAnalysis(\""+sObj.hostport+"\",\""+sObj.ip+"\")'>启动分析</button>"+
			"&nbsp;<button type=\"button\" class=\"btn btn-info\" title=\"刷新列表\" onclick='appJTA.queryThreadAnalysisList(\""+sObj.hostport+"\")'>刷新列表</button>" +
	        
			"</div>";
		html+="<div id='AppJTAListWnd_TContainer' style='font-size:12px;color:black;'></div>";

		return html;
	}
	
	this.runAppJTAListWnd = function(sObj) {
		var mainListConfig = {
			id: 'AppJTAListWnd_MainList',
			pid: 'AppJTAListWnd_TContainer',
			caption: '&nbsp',
			openDelete: false,
			key: 'time',
			pagerSwitchThreshold: 600,
			pagesize: 100,
			deleteCtr: {
				key: 'state',
				showDelete: '0'
			},
			head : {
				time : [ '时间', '30%' ],
				threadcount: ['线程数', '30%'],
				user : [ '使用者', '40%' ]
			},
			cloHideStrategy : {
				1000: [0, 1, 2, 3, 4, 5, 6, 7],
				500: [ 1, 2,6,7 ],
				300: [1,2],
			},
			events : {
				onRow : function(index, value) {
					if (index == 0) {
						return TimeHelper.getTime(parseInt(value));
					}
					return value;
				}
			}
		};
		
		this.mainList = new AppHubTable(mainListConfig);
		
		this.mainList.initTable();
		
		this.mainList.cellClickUser = function(id, pNode) {
			app.controller.showWindow({
					appname: sObj.appname,
					appurl: sObj.appurl,
					time: pNode.getElementsByTagName('td')[0].id,
					ipport: sObj.hostport
				}, 
				'AppJTADetailWnd', 'buildAppJTADetailWnd', 'runAppJTADetailWnd');
		}
		
	}
	
	this.buildAppJTADetailWnd = function(sObj) {
		var html = '';
		html="<div class=\"appDetailContent\" style='background:#333;' >" +
	        "<div class=\"topDiv\" >" +
	        "<span class=\"tagTitle\">"+sObj.appname+"</span><br/>"+
	        "<span class=\"idTitle\" >"+sObj.appurl+" &nbsp;&nbsp;时间：" + TimeHelper.getTime(parseInt(sObj.time)) + "</span>" +
	        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppJTADetailWnd','destroyAppJTADetailWnd','AppJTAListWnd')\"></div>" +
	        "</div></div>";
		
//		html+= "<div class=\"AppHubMVCSearchBar\">";
//		html+="</div>";
		
		html+="<div id='AppJTADetailWnd_TContainer' style='font-size:12px;color:black;'></div>";
		return html;
	}
	
	// 
	this.runAppJTADetailWnd = function(sObj) {
		var detailListConfig = {
			id: 'AppJTADetailWnd_DetailList',
			pid: 'AppJTADetailWnd_TContainer',
			caption: '&nbsp',
			openDelete: false,
			key: 'tid',
			useContentAsId: false,
			pagerSwitchThreshold: 600,
			pagesize: 100,
			deleteCtr: {
				key: 'state',
				showDelete: '0'
			},
			head : {
				tid: ['线程号', '10%'],
				percpu: ['CPU（％）', '10%'],
				permem: ['内存（％）', '10%'],
				state: ['线程状态', '20%'],
				info: ['线程信息', '50%']
			},
			cloHideStrategy : {
				1000: [0, 1, 2, 3, 4, 5, 6, 7],
				500: [ 1, 2,6,7 ],
				300: [1,2],
			},
			events : {
				onRow : function(index, value) {
					var content = value;
					if(index == 1 || index == 2) {
						content = Math.round(value * 100) / 100;
					}
					else if(index == 4) {
						content = content.replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g, "&quot;").replace(/'/g, "&apos;");
					}
					return content;
				}
			}
		}
		
		this.detailList= new AppHubTable(detailListConfig);
		
		this.detailList.initTable();
		
		this.detailList.cellClickUser = function(id, pNode) {
			var infoEl = pNode.getElementsByTagName('td')[4];
			alert(infoEl.innerHTML);
		}
		
		this.queryThreadDetail(sObj.time, sObj.ipport);
	}
	
	// 
	this.invokeThreadAnalysis = function(hostport, ip) {
		
		var apmParam = {
			supporter: 'com.creditease.uav.apm.supporters.ThreadAnalysisSupporter',
			method: 'captureJavaThreadAnalysis',
			param: ['', (new Date().getTime()) + '', ip]
		}
		
		var req = {
			intent: 'threadanalysis',
			request: {
				url: 'http://' + ip + ':10101/node/ctrl',
				server: 'http://' + hostport,
				user: window.parent.loginUser.userId,
				actparam: JSON.stringify(apmParam)
			}
		}
		
		AjaxHelper.call({
			url: '../../rs/godeye/node/ctrl',
            data: JSON.stringify(req),
            cache: false,
            type: 'POST',
            dataType: 'html',
            timeout: 10000,
			success: function(resp) {
				var ret = JSON.parse(resp);
				if(ret.rs == 'OK') {
					alert('分析线程成功，请稍后刷新列表');
				} else {
					console.log(resp);
					if(ret.msg) {
						alert(ret.msg);
					}else{
						alert('启动分析出错');
					}
				}
			},
			error: function(resp) {
				console.log(resp);
				alert('启动线程分析异常');
			}
		});
	}
	
	// 
	this.queryThreadAnalysisList = function(ipport) {
		
		var req = {
			intent: 'qDistinct',
			request: {
				ipport: ipport
			}
		}
		
		var that = this;
		AjaxHelper.call({
			url: '../../rs/apm/jta/q',
			data: JSON.stringify(req),
			cache: false,
            type: 'POST',
            dataType: 'html',
            timeout: 30000,
            success: function(resp) {
            	var rt = JSON.parse(resp);
            	if(!rt.rs || rt.rs=='ERR') {
            		alert('获取线程分析结果失败');
            		return;
            	}
            	
            	if(rt.rs == 'NO_INDEX') {
            		alert('没有搜索到该应用线程相关内容');
            		return;
            	}
            	
            	var data = eval(rt.rs);
            	that.mainList.clearTable();
            	that.mainList.setTotalRow(parseInt(rt.count));
            	that.mainList.renderPagination();
            	that.mainList.addRows(data);
            },
            error: function(resp) {
            	console.log('error >>> ' + resp);
            	alert('获取线程分析结果失败');
            }
		})
	}
	
	// 
	this.queryThreadDetail = function(time, ipport) {
		var req = {
			intent: 'qField',
			request: {
				stime: time + '',
				etime: time + '',
				ipport: ipport,
				from: '0',
				size: '5000',
				sort: 'percpu=DESC'
			}
		}
		
		var that = this;
		AjaxHelper.call({
            url: '../../rs/apm/jta/q',
            data: JSON.stringify(req),
            cache: false,
            type: 'POST',
            dataType: 'html',
            timeout: 30000,
            success: function(resp){
            	var rt = JSON.parse(resp);
            	if(!rt.rs || rt.rs=='ERR') {
            		alert('查询线程分析失败');
            		return;
            	}
            	
            	var data = eval(rt.rs);
            	for(var k in data) {
            		if(k == 'info') {
            			data[k] = data[k].replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g, "&quot;").replace(/'/g, "&apos;");
            		}
            	}
            	
            	that.detailList.clearTable();
            	that.detailList.setTotalRow(parseInt(rt.count));
            	that.detailList.renderPagination();
            	that.detailList.addRows(data);
            },
            error: function(resp){
            	console.log('error >>> ' + resp);
            	alert('查询线程分析失败');
            }
        });
		
	}
	
}