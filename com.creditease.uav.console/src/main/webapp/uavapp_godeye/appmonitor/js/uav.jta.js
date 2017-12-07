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
	
	/* ********** jta list window ********** */
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
		
		var html="<div class=\"appDetailContent\" style='background:#333;' >" +
	        "<div class=\"topDiv\" >" +
	        "<span class=\"tagTitle\">"+appInfo["appname"]+"</span><br/>"+
	        "<span class=\"idTitle\" >"+appInfo["appurl"]+"</span>" +
	        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppJTAListWnd','destroyAppJTAListWnd','AppInstChartWnd')\"></div>" +
	        "</div></div>";
		
		html+="<div class=\"AppHubMVCSearchBar\" align='left' style='background:#eee;'>" +
			"&nbsp;<button type=\"button\" class=\"btn btn-info\" title=\"刷新列表\" onclick='appJTA.queryThreadAnalysisList(\""+sObj.hostport+"\")'>刷新列表</button>" +
			"&nbsp;<button type=\"button\" class=\"btn btn-info\" title=\"启动线程分析\" onclick='appJTA.showThreadAnalysisDialog(\""+sObj.hostport+"\",\""+sObj.ip+"\")'>启动线程分析</button>" +
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
		
		this.queryThreadAnalysisList(sObj.hostport, true);
		
		var dialog =  {
			id: 'threadAnalysisDialog',
			title: '线程分析',
			height: 160, // 160px
			event: {
				onbody: function(id){
					var html = '<div style="text-align:center;">'
							+ '<div><button class="btn btn-warning" onclick="appJTA.invokeThreadAnalysis(\''+sObj.hostport+'\',\''+sObj.ip+'\')">单次线程分析</button></div><br>'
							+ '<div class="shine2" style="background:#999;"></div>'
							+ '<br><div><font color="grey">持续<input id="jta_mta_duration" type="number" value="30", size="3" max="300" min="1">秒&nbsp;间隔<input id="jta_mta_interval" type="number" value="10", size="3" max="60" min="1">秒</font></div>'
							+ '<div><button class="btn btn-warning" onclick="appJTA.invokeMultiThreadAnalysis(\''+sObj.hostport+'\',\''+sObj.ip+'\')">多次线程分析</button></div>'
							+ '</div>';
					return html;
				}
			}
		};
		window["appdialogmgr"].remove('threadAnalysisDialog');
		window["appdialogmgr"].build(dialog);
	}
	
	/* ********** jta detail window ********** */
	this.buildAppJTADetailWnd = function(sObj) {
		var html = '';
		html="<div class=\"appDetailContent\" style='background:#333;' >" +
	        "<div class=\"topDiv\" >" +
	        "<span class=\"tagTitle\">"+sObj.appname+"</span><br/>"+
	        "<span class=\"idTitle\" >"+sObj.appurl+" &nbsp;&nbsp;时间：" + TimeHelper.getTime(parseInt(sObj.time)) + "</span>" +
	        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppJTADetailWnd','destroyAppJTADetailWnd','AppJTAListWnd')\"></div>" +
	        "</div></div>";
		
//		html += "<div class=\"AppHubMVCSearchBar\" align='left' style='background:#eee;'>" +
//				"<div id=\"threads_info\"></div>" +
//				"</div>";
		
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
				state: ['线程状态', '10%'],
				info: ['线程信息', '50%'],
				actionShow: ['线程栈', '10%']
//				actionQChain: ['查找等待', '10%']
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
		
		this.queryThreadDetail(sObj.time, sObj.ipport);
	}
	
	this.showThreadAnalysisDialog = function(hostport, ip) {
		window["appdialogmgr"].open('threadAnalysisDialog', {});
	}
	
	// 
	this.invokeThreadAnalysis = function(hostport, ip) {
		
		var apmParam = {
					supporter: 'com.creditease.uav.apm.supporters.ThreadAnalysisSupporter',
					method: 'captureJavaThreadAnalysis',
					param: ['', (new Date().getTime()) + '', ip]
				}
		this.uavhm.nodeCtrl('threadanalysis', {
					url: 'http://' + ip + ':10101/node/ctrl',
					server: 'http://' + hostport,
					user: window.parent.loginUser.userId,
					actparam: JSON.stringify(apmParam)
				}, function(){
					alert('分析线程成功，请稍后刷新列表');
					window["appdialogmgr"].close('threadAnalysisDialog');
				});
	}
	
	this.invokeMultiThreadAnalysis = function(hostport, ip){
		
		var duration = $('#jta_mta_duration').val();
		var interval = $('#jta_mta_interval').val();
		
		var apmParam = {
					supporter: 'com.creditease.uav.apm.supporters.ThreadAnalysisSupporter',
					method: 'captureJavaThreadAnalysis',
					param: ['', (new Date().getTime()) + '', ip]
				}
		this.uavhm.nodeCtrl('threadanalysis', {
					url: 'http://' + ip + ':10101/node/ctrl',
					server: 'http://' + hostport,
					user: window.parent.loginUser.userId,
					actparam: JSON.stringify(apmParam),
					multiple: 'true',
					duration: duration,
					interval: interval
				}, function(ret){
					alert('分析线程成功，请稍后刷新列表');
					window["appdialogmgr"].close('threadAnalysisDialog');
				});
	}
	
	// 
	this.queryThreadAnalysisList = function(ipport, initQuery) {
		
		var that = this;
		this.uavhm.query('qDistinct', {ipport: ipport}, 
				function(data, count){
					if(count == 0 && initQuery) {
						return;
					}
		        	that.mainList.clearTable();
		        	that.mainList.setTotalRow(parseInt(count));
		        	that.mainList.renderPagination();
		        	that.mainList.addRows(data);
				});
	}
	
	// 
	this.queryThreadDetail = function(time, ipport) {
		
		var that = this;
		this.uavhm.query('qField', {
					stime: time + '',
					etime: time + '',
					ipport: ipport,
					from: '0',
					size: '5000',
					sort: 'percpu=DESC'
				}, function(data, count){
					for(var k in data) {
		        		data[k]['actionShow'] = '<button type="button" class="btn btn-info" title="显示" onclick="appJTA.showStacktrace(this)">显示</button>';
		        		// data[k]['actionQChain'] = '<button type="button" class="btn btn-info" title="查找" onclick="appJTA.findThreadChain(\''+time+'\',\''+ipport+'\',this)">查找</button>';
		        	}
					that.detailList.clearTable();
		        	that.detailList.setTotalRow(parseInt(count));
		        	that.detailList.renderPagination();
		        	that.detailList.addRows(data);
		        	
		        	// that.findDeadlock(time, ipport);
				});
	}
	
	this.showStacktrace = function(line){
		alert($(line).parent().prevAll('.clum4')[0].innerHTML);
	}
	
	/* ********** jta deep analysis ********** */
/*	
	this.findDeadlock = function(time, ipport) {
		
		this.uavhm.query('findDeadlock', {
					stime: time + '',
					etime: time + '',
					ipport: ipport,
					from: '0',
					size: '5000'
				}, function(data, count){
					// TODO
				});
	}
	
	this.findThreadChain = function(time, ipport, line){
	
		var threadId = $(line).parent().prevAll('.clum0')[0].innerHTML;
		this.uavhm.query('queryThreadChain', {
					stime: time + '',
					etime: time + '',
					ipport: ipport,
					from: '0',
					size: '5000',
					threadId: threadId
				}, function(data, count){
					// TODO
				});
	}
*/
	
	this.uavhm = {
			
		queryURL: '../../rs/apm/jta/q',
		nodeCtrlURL: '../../rs/godeye/node/ctrl',
		
		query: function(intent, params, then) {
			this.call(this.queryURL, {intent: intent, request: params}, 
					function(ret) {
						if(!ret.rs || ret.rs == 'ERR') {
							alert('请求处理失败');
							return;
						}
						if(ret.rs == 'NO_INDEX') {
			        		alert('没有搜索到该应用线程相关内容');
			        		return;
			        	}
						then(eval(ret.rs), ret.count); 
					});
		},
		
		nodeCtrl: function(intent, params, then) {
			this.call(this.nodeCtrlURL, {intent: intent, request: params}, 
					function(ret) {
						if(ret.rs != 'OK') {
							alert(ret.msg ? ret.msg: '启动分析出错');
						} else {
							then(ret);
						}
					});
		},
		
		call: function(url, req, then, fail) {
			AjaxHelper.call({
						url: url,
						data: JSON.stringify(req),
						cache: false,
			            type: 'POST',
			            dataType: 'html',
			            timeout: 30000,
			            success: function(resp) {
			            	then(JSON.parse(resp));
			            },
			            error: fail || function(o) {
			            	console.log(o);
			            	alert('请求失败，可能是网络异常');
			            }
					});
		}
	}
}