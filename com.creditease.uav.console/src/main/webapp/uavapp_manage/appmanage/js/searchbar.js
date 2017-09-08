/**
 * 
 */
var searchBarPid=null;
function generateSearchBar(id){
	buf = [];
	buf.push('\n');
		
		buf.push('<button id="'+id+'AppManagerAdd" class="btn btn-default" type="button">');
		buf.push('<span class="glyphicon glyphicon-plus"></span>');
		buf.push('</button>');
		
		buf.push('<input id="'+id+'AppManagerSearchBar_keyword_Hidden" type="hidden">');
		buf.push('<input id="'+id+'AppManagerSearchBar_keyword" class="form-control AppHubMVCSearchBarInputText" type="text" placeholder="可输入URL检索" value="">');
		buf.push('</input>');
		
		buf.push('<div class="btn-group">');
		buf.push('<button id="'+id+'AppManagerSearchBar_searchbtn" type="button" class="btn btn-default">');
		buf.push('<span class="glyphicon glyphicon-search"></span>');
		buf.push('</button>');;

		buf.push('<button id="'+id+'AppManagerSearchBar_searchAllbtn" type="button" class="btn btn-default">');
		buf.push('<span class="glyphicon glyphicon-th"></span>');
		buf.push('</button>');
		buf.push('</div>');
	buf.push('');
	
	document.getElementById(id).innerHTML = buf.join('');
};

function searchEventBind(id){
	var container = document.getElementById(id);
	var addButton = document.getElementById(''+id+'AppManagerAdd');
	var keyInput = document.getElementById(''+id+'AppManagerSearchBar_keyword');
	var searchButton = document.getElementById(''+id+'AppManagerSearchBar_searchbtn');
	var searchAllButton = document.getElementById(''+id+'AppManagerSearchBar_searchAllbtn');
	
	addButton.onclick = searchBarAddEvent;
	searchButton.onclick = searchEvent;
	searchAllButton.onclick = searchAllEvent;
};

function searchEvent(){
	table.setPageNum(1);
	var input = $("#AppManagerHeadSearchBarAppManagerSearchBar_keyword").val();
	$("#AppManagerHeadSearchBarAppManagerSearchBar_keyword_Hidden").val(input);
	userSendRequest();
};

function searchAllEvent(){
	table.setPageNum(1);
	$("#AppManagerHeadSearchBarAppManagerSearchBar_keyword").val("");
	$("#AppManagerHeadSearchBarAppManagerSearchBar_keyword_Hidden").val("");
	userSendRequest();
};

function initSearchBar(id){
	generateSearchBar(id);
	searchEventBind(id);
	searchBarPid = id;
};
