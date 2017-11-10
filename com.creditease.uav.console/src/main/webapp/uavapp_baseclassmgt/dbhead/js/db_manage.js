var delmodalConfig = {
    id          :0,
    head        :"提示",
    content     :"",
    callback    :"clickSureCallback()"
}

var addmodalConfig = {
	id          :0,
	head        :"添加",
	content     :"",
	callback    :"clickSureCallback_add()"
}

var modifymodalConfig = {
		id       :0,
		head     :"修改",
		content  :"",
		callback :"clickSureCallback_modify()"
	}

var opentsdb = {
		name     :'0',
		url      :'0',
		version  :'0'
}

function clickSureCallback(){
	deleteDb_RESTClient(delmodalConfig.id);
}

function userDelete(id,trObj){
    delmodalConfig.id=id;
    delmodalConfig.content="是否确定删除[<span style='color: red'>"+trObj.getElementsByTagName("td")[0].id+"</span>]";
    showConfirm(delmodalConfig);
}

function userClickRow(v) {
	queryDbById_RESTClient(v);
}

function showDbById(data) {
	modifymodalConfig.id = data[0].name;
	modifymodalConfig.content="<label class='editor-label'>实例名称</label>: <input type='text' class='editor-input' id='opentsdb_name' disabled='true' value='"+data[0].name+"'><br>";
	modifymodalConfig.content+="<label class='editor-label'>URL</label>: <input type='text' class='editor-input' id='opentsdb_url' value='"+data[0].url+"'><br>";
	modifymodalConfig.content+="<label class='editor-label'>版本号</label>: <input type='text' class='editor-input' id='opentsdb_version' value='"+data[0].version+"'><br>";
	showConfirm(modifymodalConfig);
}

function clickSureCallback_modify() {
	opentsdb.name = document.getElementById("opentsdb_name").value;
	opentsdb.url = document.getElementById("opentsdb_url").value;
	opentsdb.version = document.getElementById("opentsdb_version").value;
	modifyDb_RESTClient(opentsdb);
}

function ajaxGetdatas() {
	loadDbs_RESTClient();
}

function addOpenTSDB() {
	addmodalConfig.content="<label class='editor-label'>实例名称</label>: <input type='text' class='editor-input' id='opentsdb_name'><br>";
	addmodalConfig.content+="<label class='editor-label'>URL</label>: <input type='text' class='editor-input' id='opentsdb_url'><br>";
	addmodalConfig.content+="<label class='editor-label'>版本号</label>: <input type='text' class='editor-input' id='opentsdb_version'><br>";
	showConfirm(addmodalConfig);
}


function clickSureCallback_add(){
	opentsdb.name = document.getElementById("opentsdb_name").value;
	opentsdb.url = document.getElementById("opentsdb_url").value;
	opentsdb.version = document.getElementById("opentsdb_version").value;
	addDb_RESTClient(opentsdb);
}
