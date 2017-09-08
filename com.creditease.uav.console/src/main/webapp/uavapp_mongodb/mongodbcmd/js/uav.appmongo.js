var strView="<div class=\"row\">"+
             "<div class=\"AppHubMVCSearchBar\"> <div class='btn-group'></div> </div>"+
             "<div class=\"col-sm-4\"  style=\"height: 4000px;\" ><div id=\"treeview\" class=\"\" style=\"width: 260px; \"></div></div>"+
             "<div id=\"panel_input\" data-panel_type=\"css\" style=\"margin:15px;\"><input type=\"text\" id=\"input\" onkeypress=\"EnterPress(event)\" onkeydown=\"EnterPress()\" placeholder=\"请输入命令\"style=\"width: 500px;  font-weight: 300\"></div>"+
             "<p><b><font color=\"  #00009C \">执行结果:</font></b></p>  "+
             "<div id=\"panel_output\" ><textarea id=\"outresult\" rows=\"15\" cols=\"600\" name=\"outresult\"style=\"width: 500px; position: fixed\"></textarea></div>"+
             "</div>";

var inputcmd;


var clusterNode = [ {
	text : 'Mongo Cluster',
	href : '#parent1',
	tags : [ '3' ],
	icon:"glyphicon glyphicon-cloud",
	nodes : [ {
		text : 'Mongos',
		href : '#child1',
		tags : [ '2' ],
		icon:"glyphicon glyphicon-unchecked",
		state : {
			expanded : true
		},
		nodes : []
	}, {
		text : 'Configs',
		href : '#child1',
		tags : [ '2' ],
		icon:"glyphicon glyphicon-unchecked",
		state : {
			expanded : true
		},
		nodes : []
	}, {
		text : 'Shards',
		href : '#child1',
		tags : [ '2' ],
		icon:"glyphicon glyphicon-unchecked",
		state : {
			expanded : true
		},
		nodes : []
	} ]
} ];

function EnterPress(e){ //传入 event
	   if (e.keyCode == 13) {
		inputcmd = $("#input").val();
		executeCmd();
	}   
} 

function showview(str){
	document.body.innerHTML+=str;
}

var initView = function() {
	showview(strView);
};

var $showcomponent = initView();


var loadTree = function() {
	
	console.log("clusterNode::" + clusterNode);

	return $('#treeview').treeview({
		expandIcon : "glyphicon glyphicon-th",
		collapseIcon : "glyphicon glyphicon-th-list",
		nodeIcon : "glyphicon glyphicon-folder-close",
		color : "black",
		backColor : "dark",
		onhoverColor : "write",
		borderColor : "black",
		showBorder : false,
		showTags : true,
		highlightSelected : true,
		selectedColor : "blue",
		selectedBackColor : "grey",
		data : clusterNode
	});
};

var initSelectableTree = function() {
	return $('#treeview-selectable').treeview(
			{
				data : clusterNode,
				multiSelect : $('#chk-select-multi').is(':checked'),
				onNodeSelected : function(event, node) {
					$('#selectable-output').prepend(
							'<p>' + node.text + ' was selected</p>');
				},
				onNodeUnselected : function(event, node) {
					$('#selectable-output').prepend(
							'<p>' + node.text + ' was unselected</p>');
				}
			});
};

var $selectableTree = initSelectableTree();

var findSelectableNodes = function() {
	return $selectableTree.treeview('search', [ $('#input-select-node').val(),
			{
				ignoreCase : false,
				exactMatch : false
			} ]);
};
var selectableNodes = findSelectableNodes();

$('#chk-select-multi:checkbox').on('change', function() {
	console.log('multi-select change');
	$selectableTree = initSelectableTree();
	selectableNodes = findSelectableNodes();
});

// Select/unselect/toggle nodes
$('#input-select-node').on('keyup', function(e) {
	selectableNodes = findSelectableNodes();
	$('.select-node').prop('disabled', !(selectableNodes.length >= 1));
});

$('#btn-select-node.select-node').on('click', function(e) {
	$selectableTree.treeview('selectNode', [ selectableNodes, {
		silent : $('#chk-select-silent').is(':checked')
	} ]);
});

$('#btn-unselect-node.select-node').on('click', function(e) {
	$selectableTree.treeview('unselectNode', [ selectableNodes, {
		silent : $('#chk-select-silent').is(':checked')
	} ]);
});

$('#btn-toggle-selected.select-node').on('click', function(e) {
	$selectableTree.treeview('toggleNodeSelected', [ selectableNodes, {
		silent : $('#chk-select-silent').is(':checked')
	} ]);
});

var $expandibleTree = $('#treeview-expandible').treeview(
		{
			data : clusterNode,
			onNodeCollapsed : function(event, node) {
				$('#expandible-output').prepend(
						'<p>' + node.text + ' was collapsed</p>');
			},
			onNodeExpanded : function(event, node) {
				$('#expandible-output').prepend(
						'<p>' + node.text + ' was expanded</p>');
			}
		});

var findExpandibleNodess = function() {
	return $expandibleTree.treeview('search', [ $('#input-expand-node').val(),
			{
				ignoreCase : false,
				exactMatch : false
			} ]);
};
var expandibleNodes = findExpandibleNodess();

// Expand/collapse/toggle nodes
$('#input-expand-node').on('keyup', function(e) {
	expandibleNodes = findExpandibleNodess();
	$('.expand-node').prop('disabled', !(expandibleNodes.length >= 1));
});

$('#btn-expand-node.expand-node').on('click', function(e) {
	var levels = $('#select-expand-node-levels').val();
	$expandibleTree.treeview('expandNode', [ expandibleNodes, {
		levels : levels,
		silent : $('#chk-expand-silent').is(':checked')
	} ]);
});

$('#btn-collapse-node.expand-node').on('click', function(e) {
	$expandibleTree.treeview('collapseNode', [ expandibleNodes, {
		silent : $('#chk-expand-silent').is(':checked')
	} ]);
});

$('#btn-toggle-expanded.expand-node').on('click', function(e) {
	$expandibleTree.treeview('toggleNodeExpanded', [ expandibleNodes, {
		silent : $('#chk-expand-silent').is(':checked')
	} ]);
});

// Expand/collapse all
$('#btn-expand-all').on('click', function(e) {
	var levels = $('#select-expand-all-levels').val();
	$expandibleTree.treeview('expandAll', {
		levels : levels,
		silent : $('#chk-expand-silent').is(':checked')
	});
});

$('#btn-collapse-all').on('click', function(e) {
	$expandibleTree.treeview('collapseAll', {
		silent : $('#chk-expand-silent').is(':checked')
	});
});

var $checkableTree = $('#treeview-checkable').treeview(
		{
			data : clusterNode,
			showIcon : false,
			showCheckbox : true,
			onNodeChecked : function(event, node) {
				$('#checkable-output').prepend(
						'<p>' + node.text + ' was checked</p>');
			},
			onNodeUnchecked : function(event, node) {
				$('#checkable-output').prepend(
						'<p>' + node.text + ' was unchecked</p>');
			}
		});

var findCheckableNodess = function() {
	return $checkableTree.treeview('search', [ $('#input-check-node').val(), {
		ignoreCase : false,
		exactMatch : false
	} ]);
};
var checkableNodes = findCheckableNodess();

// Check/uncheck/toggle nodes
$('#input-check-node').on('keyup', function(e) {
	checkableNodes = findCheckableNodess();
	$('.check-node').prop('disabled', !(checkableNodes.length >= 1));
});

$('#btn-check-node.check-node').on('click', function(e) {
	$checkableTree.treeview('checkNode', [ checkableNodes, {
		silent : $('#chk-check-silent').is(':checked')
	} ]);
});

$('#btn-uncheck-node.check-node').on('click', function(e) {
	$checkableTree.treeview('uncheckNode', [ checkableNodes, {
		silent : $('#chk-check-silent').is(':checked')
	} ]);
});

$('#btn-toggle-checked.check-node').on('click', function(e) {
	$checkableTree.treeview('toggleNodeChecked', [ checkableNodes, {
		silent : $('#chk-check-silent').is(':checked')
	} ]);
});

// Check/uncheck all
$('#btn-check-all').on('click', function(e) {
	$checkableTree.treeview('checkAll', {
		silent : $('#chk-check-silent').is(':checked')
	});
});

$('#btn-uncheck-all').on('click', function(e) {
	$checkableTree.treeview('uncheckAll', {
		silent : $('#chk-check-silent').is(':checked')
	});
});

// 获取MongoCluster
function loadMongoCluster() {
	AjaxHelper.call({
		url : "../../rs/mongo/" + "loadMongoCluster",
		data : {},
		async : true,
		cache : false,
		type : "GET",
		dataType : "html",
		success : function(data) {

			initMongoCluster(data);
		},
		error : function(data) {
			console.log(data);
		},
	});
}

function initMongoCluster(data) { // rs 接口获取数据后初始化

	var clusterObject = eval("(" + data + ")");
	var shardvalue = clusterObject.shard;
	var configvalue = clusterObject.config;
	var mongosvalue = clusterObject.mongs;
	var mongsArray = mongosvalue.split(";");
	var configArray = configvalue.split(";");
	var shardArray = shardvalue.split(";");

	var node = clusterNode[0].nodes;
	var datasM = new Array();
	for ( var i = 0; i < mongsArray.length; i++) {

		datasM[i] = new Object({
			"text" : mongsArray[i],
			"href" : '#grandchild1',
			"nodeIcon" : "glyphicon glyphicon-folder-close",
			"tags" : [ '1' ]
		});
	}
	node[0].nodes = datasM;
	var datasC = new Array();
	for ( var i = 0; i < configArray.length; i++) {

		datasC[i] = new Object({
			"text" : configArray[i],
			"href" : '#grandchild1',
			"nodeIcon" : "glyphicon glyphicon-folder-close",
			"tags" : [ '1' ]
		});
	}
	node[1].nodes = datasC;
	var datasS = new Array();
	for ( var i = 0; i < shardArray.length; i++) {

		datasS[i] = new Object({
			"text" : shardArray[i],
			"href" : '#grandchild1',
			"nodeIcon" : "glyphicon glyphicon-folder-close",
			"tags" : [ '1' ]
		});
	}
	node[2].nodes = datasS;
	loadTree();
}

//执行命令
function executeCmd() {
	AjaxHelper.call({
		url : "../../rs/mongo/" + "executeMongoCmd"+"?"+"input="+inputcmd,
		data : {},
		async : true,
		cache : false,
		type : "GET",
		dataType : "html",
		success : function(data) {

			getResult(data);
		},
		error : function(data) {
			console.log(data);
		},
	});
}

function getResult(data) { // rs 接口获取数据后初始化
	
	document.getElementById('outresult').value = data;
}


$("document").ready(loadMongoCluster())
