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
	
	/* ********** TODO jta list window ********** */
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
			'&nbsp;<button id="showAnalysis" type="button" class="btn btn-info" onclick="">查看分析结果</button>' + 
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
			useContentAsId: false,
			key: 'time',
			pagerSwitchThreshold: 600,
			pagesize: 100,
			deleteCtr: {
				key: 'state',
				showDelete: '0'
			},
			head : {
				ckbox: ['', '10%'],
				time : [ '时间', '30%' ],
				threadcount: ['线程数', '20%'],
				user : [ '使用者', '40%' ]
			},
			cloHideStrategy : {
				1000: [0, 1, 2, 3, 4, 5, 6, 7],
				500: [ 1, 2,6,7 ],
				300: [1,2],
			},
			events : {
				onRow : function(index, value) {
					if (index == 1) {
						return TimeHelper.getTime(parseInt(value));
					}
					return value;
				}
			}
		};
		
		this.mainList = new AppHubTable(mainListConfig);
		
		this.mainList.initTable();
		
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
		
		// bind event
		HtmlHelper.id('showAnalysis').onclick = function(){
			var dumpTimes = [];
			$('td.normalclum.clum0 > input', $('#AppJTAListWnd_MainListBody').children()).each(function(idx, el){
				if(el.checked) {
					dumpTimes.push($(el).attr('hiddenValue'));
				}
			});
			
			if(dumpTimes.length == 0) {
				return;
			}
			
			if(dumpTimes.length == 1) {
				// jta detail window
				app.controller.showWindow({
						appname: sObj.appname,
						appurl: sObj.appurl,
						time: dumpTimes[0],
						ipport: sObj.hostport
					}, 'AppJTADetailWnd', 'buildAppJTADetailWnd', 'runAppJTADetailWnd');
				return;
			}
			
			// jta multi dump window
			app.controller.showWindow({
					appname: sObj.appname,
					appurl: sObj.appurl,
					times: dumpTimes.sort(), // sort
					ipport: sObj.hostport
				}, 'AppJTAMultiDumpWnd', 'buildAppJTAMultiDumpWnd', 'runAppJTAMultiDumpWnd');
		}
	}
	
	
	/* ********** TODO jta detail window ********** */
	this.buildAppJTADetailWnd = function(sObj) {
		var html = '';
		html="<div class=\"appDetailContent\" style='background:#333;' >" +
	        "<div class=\"topDiv\" >" +
	        "<span class=\"tagTitle\">"+sObj.appname+"</span><br/>"+
	        "<span class=\"idTitle\" >"+sObj.appurl+"</span>" +
	        "<div class=\"icon-signout\" onclick=\"javascript:app.controller.closeWindow('AppJTADetailWnd','destroyAppJTADetailWnd','AppJTAListWnd')\"></div>" +
	        "</div></div>";
		
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
				info: ['线程信息', '40%'],
				actionShow: ['线程栈', '10%'],
				actionQChain: ['查找等待', '10%']
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
				function(rs, count){
					if(count == 0 && initQuery) {
						return;
					}
					var data = eval(rs);
					for (var k in data) {
						data[k]['ckbox'] = '<input type="checkbox" hiddenValue="'+data[k]['time']+'" >';
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
				}, function(rs, count){
					var data = eval(rs);
					for(var k in data) {
		        		data[k]['actionShow'] = '<button type="button" class="btn btn-info" title="显示" onclick="appJTA.showStacktrace(this)">显示</button>';
		        		data[k]['actionQChain'] = '<button type="button" class="btn btn-info" title="查找" onclick="appJTA.findThreadChain(\''+time+'\',\''+ipport+'\',this)">查找</button>';
		        	}
					that.detailList.clearTable();
		        	that.detailList.setTotalRow(parseInt(count));
		        	that.detailList.renderPagination();
		        	that.detailList.addRows(data);
		        	
		        	that.queryDumpInfo(time, ipport, that.detailList);
				});
	}
	
	this.showStacktrace = function(line) {
		this.showMsgWnd($(line).parent().prevAll('.clum4')[0].innerHTML, 'AppJTADetailWnd');
	}
	
	/* ********** TODO jta deep analysis ********** */
	this.buildJTAMultiDumpWnd = function(sObj){
		var html = '<div class="appDetailContent Dark">'
				+ '<div class="topDiv"><span class="tagTitle">'+sObj.appname+'</span><br><span class="idTitle">'+sObj.appurl+'</span>'
				+ '<div class="icon-signout" onclick="javascript:app.controller.closeWindow(\'AppJTAMultiDumpWnd\', \'destroyAppJTAMultiDumpWnd\', \'AppJTAListWnd\');"></div>'
				+ '</div></div>';
		html += '<div class="AppHubMVCSearchBar" align="left">&nbsp;<button id="multiDumpGraphBtn" class="btn btn-info">查看线程依赖图</button></div>';
		html += '<div id="AppJTAMultiDumpWnd_TContainer" style="font-size:12px;color:black;"></div>';
		return html;
	}
	
	this.runJTAMultiDumpWnd = function(sObj){
		var times = sObj.times;
		var dynamicHead = {
				ckbox: ['', '5%'],
				thread: ['线程号', '5%']
		};
		for(var i = 0; i < times.length; i++) {
			dynamicHead[times[i] + ''] = ['<button class="btn btn-default">'+TimeHelper.getTime(parseInt(times[i]))+'</button>', '15%']
		}
		var multiDumpConfig = {
				id: 'AppJTAMultiDumpWnd_List',
                pid: 'AppJTAMultiDumpWnd_TContainer',
                caption: '&nbsp',
                openDelete: false,
                key: 'thread',
                useContentAsId: false,
                pagerSwitchThreshold: 2000,
                pagesize: 1000,
                deleteCtr: {
                    key: 'state',
                    showDelete: '0'
                },
                head: dynamicHead,
                cloHideStrategy: {
                    1000: [0, 1, 2, 3, 4, 5, 6, 7],
                    500: [1, 2, 6, 7],
                    300: [1, 2]
                }
		}
		
		this.multiDumpList = new AppHubTable(multiDumpConfig);
		this.multiDumpList.initTable();
		var that = this;
		this.multiDumpList.headClickUser = function(head) {
			var headId = head.id;
			if(headId != 'thread_head') {
				that.showDumpGraph({
						multiple: false,
						time: headId.substring(0, headId.length - 5),
						ipport: sObj.ipport,
						appname: sObj.appname,
						appurl: sObj.appurl
					});
			}
		}
		
		this.queryMultiDumpInfo(times, sObj.ipport);
		
		HtmlHelper.id('multiDumpGraphBtn').onclick = function() {
			var pickedThreads = [];
			$('td.normalclum.clum0 > input', $('#AppJTAMultiDumpWnd_ListBody').children()).each(function(idx, el){
				if(el.checked) {
					pickedThreads.push($(el).attr('hiddenValue'));
				}
			});
			that.showDumpGraph({
					multiple: true,
					times: sObj.times, // sort
					ipport: sObj.ipport,
					threadIds: pickedThreads,
					appname: sObj.appname,
					appurl: sObj.appurl
				});
		}
	}
	
	this.queryMultiDumpInfo = function(times, ipport) {
		var that = this;
		this.uavhm.query('queryMultiDumpInfo', {
					ipport: ipport,
					times: JSON.stringify(times),
					from: '0',
					size: '5000',
					sort: 'percpu=DESC'
				},function(rs){
					var data = eval(rs);
					for(var i = 0; i < data.length; i++) {
						var threadInfo = data[i];
						for(var k in threadInfo) {
							if(threadInfo.hasOwnProperty(k) && k != 'thread') {
								if(threadInfo[k] != undefined && threadInfo[k] != "") {
									var tstate = JSON.parse(threadInfo[k]).threadState || 'UNKNOWN';
									var color = that.colorState(tstate);
									threadInfo[k] = '<span style="background:'+color+'">' + tstate + '</span>';
								}
							}
						}
						data[i]['ckbox'] = '<input type="checkbox" hiddenValue="'+data[i]['thread']+'" >';
					}
					that.multiDumpList.clearTable();
		        	that.multiDumpList.setTotalRow(data.length);
		        	that.multiDumpList.renderPagination();
		        	that.multiDumpList.addRows(data);
				});
	}
	
	this.queryDumpInfo = function(time, ipport, detailList) {
		
		var that = this;
		this.uavhm.query('queryDumpInfo', {
					stime: time + '',
					etime: time + '',
					ipport: ipport,
					from: '0',
					size: '5000'
				}, function(rs){
					var data = JSON.parse(rs);
					var info = '<div  align="center">'
							 + '时间：<font color="#333">' + TimeHelper.getTime(parseInt(time)) + '</font>，'
							 + 'CPU：<font color="#333">' + data.cpu + '％</font>；'
							 + '线程数：<font color="#333">'+data.threadCount+'</font>，'
							 + 'RUNNABLE：<font color="#333">'+data.runnableCount+'</font>，'
							 + 'BLOCKED：<font color="#333">'+data.blockedCount+'</font>，'
							 + 'WAITING：<font color="#333">'+data.waitingCount+'</font>。';
					if(data.deadlock && data.deadlock.length > 0) {
						info += '<br><button id="jta_show_deadlock" class="btn btn-danger">有死锁</button>'; 
					}else {
						info += '<br>无死锁';
					}
					info += '</div>';
					detailList.setCaption(info);
					
					if(data.deadlock && data.deadlock.length > 0) {
						$('#jta_show_deadlock').on('click', function() {
							that.showDeadlockMsg(data.deadlock);
						});
					}
				});
	}
	
	this.showDeadlockMsg = function(deadlock) {
		var msg = '发现' + deadlock.length + '个线程处于死锁中：\n';
		var deadlockInfo = deadlock.join('\n=============================================================\n');
		this.showMsgWnd(msg + deadlockInfo, 'AppJTADetailWnd');
	}
	
	this.findThreadChain = function(time, ipport, line){
	
		var that = this;
		var threadId = $(line).parent().prevAll('.clum0')[0].innerHTML;
		this.uavhm.query('queryThreadChain', {
					stime: time + '',
					etime: time + '',
					ipport: ipport,
					from: '0',
					size: '5000',
					threadId: threadId
				}, function(rs, count){
					var data = eval(rs);
					if(data.length <= 1) {
						alert('无依赖线程');
					} else {
						var msg = data.join('\n=============================================================\n');
						that.showMsgWnd(msg, 'AppJTADetailWnd');
					}
				});
	}
	
	this.buildAppJTAGraphWnd = function(sObj) {
		var html = '<div class="appDetailContent Dark">'
				+ '<div class="topDiv"><span class="tagTitle">' + sObj.appname + '</span><br><span class="idTitle">' + sObj.appurl + '</span>'
				+ '<div class="icon-signout" onclick="javascript:app.controller.closeWindow(\'AppJTAGraphWnd\', \'destroyAppJTAGraphWnd\', \'AppJTAMultiDumpWnd\');"></div><br>'
				+ '</div></div>';
		html += '<div id="jta_graph"></div>';
		return html;
	}
	
	this.runAppJTAGraphWnd = function(sObj) {
		var that = this;
		if(sObj.multiple) {
			this.uavhm.query('queryMultiDumpGraph', {
				times: JSON.stringify(sObj.times.sort()), // sort
				ipport: sObj.ipport,
				threadIds: JSON.stringify(sObj.threadIds),
				from: '0',
				size: '5000'
			}, function(rs){
				var data = JSON.parse(rs);
				var nodes = that.transformThreadtoNode(data.nodes);
				that.drawThreadGraph(nodes, data.edges);
			});
		} else {
			this.uavhm.query('queryThreadDigraph', {
				stime: sObj.time + '',
				etime: sObj.time + '',
				ipport: sObj.ipport,
				from: '0',
				size: '5000',
			}, function(rs){
				var data = JSON.parse(rs);
				var nodes = that.transformThreadtoNode(data.nodes);
				that.drawThreadGraph(nodes, data.edges);
			});
		}
	}
	
	this.drawThreadGraph = function(nodes, edges) {
		var visNodes = new vis.DataSet(nodes);
		var visEdges = new vis.DataSet(edges);
		var options = {
				autoResize: true,
				height: '100%',
				width: '100%',
				edges: {
					color: 'grey',
					arrows: {
						to: {enabled: true, scaleFactor: 1, type: 'arrow'}
					}
				}
		}
		var container = HtmlHelper.id('jta_graph');
		var threadGraph = new vis.Network(container, {nodes: visNodes, edges: visEdges}, options);
		var that = this;
		threadGraph.on('doubleClick', function(param) {
			if(param.nodes.length == 0) {
				return;
			}
			var msg = visNodes.get(param.nodes[0]).msg;
			if(msg != undefined) {
				that.showMsgWnd(msg, 'AppJTAGraphWnd');
			}
		});
	}
	
	this.transformThreadtoNode = function(threadNodes) {
		var nodes = [];
		for(var i = 0; i < threadNodes.length; i++) {
			var n = threadNodes[i];
			var color = '#dddee0'; // lite grey
			if(n.type == 'Thread') {
				color = this.colorState(n.state);
				nodes.push({id: n.id, label: n.name, shape: 'box', color: color, title: n.tip, msg: n.msg});
			} else {
				color = '#dcdcdc'; // grey
				nodes.push({id: n.id, label: 'lock', shape: 'circle', color: '#dcdcdc', title: n.tip, msg: n.msg});
			}
		}
		return nodes;
	}
	
	// switch color 
	this.colorState = function(state) {
		var color = '#dddee0'; // lite grey
		if(state == 'RUNNABLE') {
			color = '#329646'; // green
		}else if(state == 'BLOCKED') {
			color = '#aa4632'; // red
		}else if(state == 'WAITING' || state == 'TIMED_WAITING') {
			color = '#327daa'; // blue
		}
		return color;
	}
	
	this.resize = function(w, h, noTopHeight) {
		var container = HtmlHelper.id('jta_graph');
		container.style.height=noTopHeight+"px";
	}
	
	/**
	 * 线程图谱
	 */
	this.showDumpGraph = function(o) {
		app.controller.showWindow(o, 'AppJTAGraphWnd', 'buildAppJTAGraphWnd', 'runAppJTAGraphWnd');
	}
	
	/**
	 * 展示消息
	 */
	this.showMsgWnd = function(msg, backWnd) {
		var obj = {
				msg: msg,
				backWnd: backWnd
		}
		app.controller.showWindow(obj, 'AppJTAMsgWnd', 'buildAppJTAMsgWnd', 'runAppJTAMsgWnd');
	}

	
	/* ********** TODO jta message window ********** */
	this.buildJTAMsgWnd = function(sObj) {
		var backWnd = sObj.backWnd;
		var html = '<div class="appDetailContent Dark">'
				 + '<div class="topDiv"><br>'
				 + '<div class="icon-signout" onclick="javascript:app.controller.closeWindow(\'AppJTAMsgWnd\', \'destroyAppJTAMsgWnd\', \''+backWnd+'\');"></div><br>'
				 + '</div></div>';
		html += '<div id="jta_msg_content" align="left"></div>';
		return html;
	}
	
	this.runJTAMsgWnd = function(sObj) {
		var msg = sObj.msg;
		msg = msg.replace('\n', '<br>');
		HtmlHelper.id('jta_msg_content').innerHTML = '<font color="black">' + msg + '</font>';
	}
	
	this.uavhm = {
			
		queryURL: '../../rs/apm/jta/q',
		nodeCtrlURL: '../../rs/godeye/node/ctrl',
		
		query: function(intent, params, then) {
			this.call(this.queryURL, 
					{intent: intent, request: params}, 
					function(ret) {
						if(!ret.rs || ret.rs == 'ERR') {
							alert('请求处理失败');
							return;
						}
						if(ret.rs == 'NO_INDEX') {
			        		alert('没有搜索到该应用线程相关内容');
			        		return;
			        	}
						then(ret.rs, ret.count); 
					});
		},
		
		nodeCtrl: function(intent, params, then) {
			this.call(this.nodeCtrlURL, 
					{intent: intent, request: params}, 
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
			            	var obj = JSON.parse(resp);
			            	then(obj);
			            },
			            error: fail || function(o) {
			            	console.log(o);
			            	alert('请求失败，可能是网络异常');
			            }
					});
		}
	}
}