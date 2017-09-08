var jsonConfig = {
		   "attribute"   :  {frontSize:14,color:'#008000'},
		   "showLayerNum"   :   10,
		   "model1"      :	[   
						       {keyT:"{}",onClick:""},   
						       {keyT:"<div class='jsonValue'>{@key}</div>",onClick:""},
						       {keyT:"<div class='jsonTitle'>{@key}</div>",onClick:""},
						       {keyT:"<div class='jsonValue'>{@key}</div>",onClick:""}
					        ]
					        
};

function initHead(id){

	var div = new StringBuffer();

	div.append('<input id="emailName_Input" class="form-control AppHubMVCSearchBarInputText" type="text" placeholder="EMAIL" value="">');
	div.append('</input>');
	
	div.append('<div class="btn-group">');
	
	div.append('<button  type="button" class="btn btn-default" onclick="javascript:loadInfoByEmail_RESTClient(\'list\')">');
	div.append('<span class="glyphicon glyphicon-envelope"></span>');
	div.append('</button>');
	 	
	div.append('<button type="button" class="btn btn-default" onclick="javascript:loadInfoByEmail_RESTClient(\'user\')">');
	div.append('<span class="glyphicon glyphicon-user"></span>');
	div.append('</button>');;
	
	div.append('</div>');
	
	document.getElementById("queryInfoHead").innerHTML = div.toString();
};

function queryInfoResult(json){

	document.getElementById("queryInfoResult").innerHTML = jsonObj.asHtml("model1",json);;
}


var jsonObj = new AppHubJSONVisualizer(jsonConfig);
$("document").ready(function(){
	initHead();
});