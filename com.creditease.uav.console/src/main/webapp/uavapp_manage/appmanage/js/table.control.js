function addAppInfo(data) {
	var appinfo = data[0];
	var h = document.getElementById('AppEditId');
	var createTime = document.getElementById("AppEditCreateTime");
	var optionTime = document.getElementById("AppEditOptionTime");
	var optionUser = document.getElementById("AppEditOptionUser");
	var editUrl = document.getElementById("AppEditURL");
	

	var formatAppName = appinfo["appurl"].substring(appinfo["appurl"].indexOf("uavapp_"));
	document.getElementById('AppName').innerHTML = formatAppName;
	document.getElementById('AppEditId').innerHTML = appinfo["appid"];
	document.getElementById("AppEditCreateTime").innerText = appinfo["createtime"];
	document.getElementById("AppEditOptionTime").innerText = appinfo["operationtime"];
	document.getElementById("AppEditOptionUser").innerText = appinfo["operationuser"];
	document.getElementById("AppEditURL").value = appinfo["appurl"];
	document.getElementById("AppEditConfigPath").value = appinfo["configpath"];

	InitEditModalForm();
	$("#RefreshButton").show();
	$('#AppEditModal').modal({
		backdrop : 'static',
		keyboard : false
	});
}

function userClickRow(v) {
	queryAppById_RESTClient(v);
};

function preProcess(rows) {
	var newrows = rows;
	for (var i = 0; i < newrows.length; i++) {
		for ( var key in newrows[i]) {
			var rObj = newrows[i];
			if (key == 'appurl') {
				var oldurl = rObj[key];
				//console.log("oldurl "+oldurl);
				var newurl = oldurl.substring(oldurl.indexOf("uavapp_"));
				rObj[key] = newurl;
			}
		}
	}
	return newrows;
}



function userSendRequest() {
	loadApps_RESTClient();
};
