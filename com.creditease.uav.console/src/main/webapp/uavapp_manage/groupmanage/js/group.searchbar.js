/**
 * 
 */
var searchBarPid=null;
function generateSearchBar(id){
	buf = [];
	buf.push('\n');
		
		buf.push('<button id="'+id+'Add" class="btn btn-default" type="button">');
		buf.push('<span class="glyphicon glyphicon-plus"></span>');
		buf.push('</button>');
		
		buf.push('<input id="'+id+'SearchHidden" type="Hidden" >');	
		buf.push('<input id="'+id+'Search" class="form-control AppHubMVCSearchBarInputText" type="text" placeholder="可输入组授权组检索" value="">');
		buf.push('</input>');
		
		buf.push('<div class="btn-group">');
		buf.push('<button id="'+id+'searchbtn" type="button" class="btn btn-default">');
		buf.push('<span class="glyphicon glyphicon-search"></span>');
		buf.push('</button>');
		
		buf.push('<button id="'+id+'searchAllbtn" type="button" class="btn btn-default">');
		buf.push('<span class="glyphicon glyphicon-th"></span>');
		buf.push('</button>');
		buf.push('</div>');
	buf.push('');
	
	document.getElementById(id).innerHTML = buf.join('');
};



function searchEventBind(id){
	var addButton = document.getElementById(''+id+'Add');
	var searchButton = document.getElementById(''+id+'searchbtn');	
	var searchAllButton = document.getElementById(''+id+'searchAllbtn');	
	addButton.onclick = showAddGroup;
	searchButton.onclick = searchButtonLoad;
	searchAllButton.onclick = searchAllButtonLoad;
};

function searchButtonLoad(){
	table.setPageNum(1);
	var input = $("#groupManagerHeadSearchBarSearch").val();
	$("#groupManagerHeadSearchBarSearchHidden").val(input);
	loadAllGroups_RESTClient();
}

function searchAllButtonLoad(){
	table.setPageNum(1);
	$("#groupManagerHeadSearchBarSearch").val("");
	$("#groupManagerHeadSearchBarSearchHidden").val("");
	loadAllGroups_RESTClient();
}

function initSearchBar(){
	var id = "groupManagerHeadSearchBar";
	generateSearchBar(id);
	searchEventBind(id);
	searchBarPid = id;
};
