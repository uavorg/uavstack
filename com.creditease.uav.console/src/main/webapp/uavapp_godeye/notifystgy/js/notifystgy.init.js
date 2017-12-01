function initListClass() {
	list.cellClickUser = showEditDiv;
	list.delRowUser = userDelete;
	list.sendRequest = loadNotifyStgy_RestfulClient;
	list.initTable();
}

/**
 * 列表配置
 */
var listConfig = {
	id : "notifystgyTableada",
	pid : "notifyList",
	openDelete : true,
	deleteCtr : (window.parent.loginUser.groupId == "uav_admin"
				|| window.parent.loginUser.groupId == "vipgroup" )? {}:{
		key : "owner",
		showDelete :window.parent.loginUser.userId
	},
	key : "key",
	pagerSwitchThreshold : 600,
	pagesize : 50,
	head : {
		key : [ 'key', '20%' ],
		keyFormat : [ '策略', '20%' ],
		desc : [ '描述', '40%' ],
		owner : [ '归属用户', '20%' ],
		uptime : [ '修改时间', '20%' ]
	},
	cloHideStrategy : {
		19999 : [ 1, 2, 3, 4],//给个比较大的范围，默认隐藏掉key列
		1100 : [ 1, 2, 3, 4],
		1000 : [ 1, 2, 4],
		800 : [ 1, 2, 4 ],
		500 : [ 1, 2 ],
		400 : [ 1 ]
	}
};

/**
 * 列表对象
 */
var list = new AppHubTable(listConfig);
$(document).ready(function() {
	initHeadDiv();
	initListClass();
});

