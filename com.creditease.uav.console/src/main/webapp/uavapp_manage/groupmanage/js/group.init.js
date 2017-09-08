$(document).ready(function() {
	initSearchBar();
});


function setErrorMsg(id, msg) {
	$("#" + id).text(msg);
}

//处理重复提交
var iscommit = true;
function commitEnd() {
	iscommit = true;
}

function formatAppInfo(obj) {
	//return getGroupStateStr(obj.state) + "&nbsp;&nbsp;" + getIpOrDnByUrl()
	//		+ "&nbsp;&nbsp;" + getUavAppNameByUrl();
	return  getUavAppNameByUrl()+ "&nbsp;&nbsp;" + getIpOrDnByUrl();
	function getIpOrDnByUrl() {
		var indexA = obj.appurl.indexOf("://") + 3;
		var result = obj.appurl.substr(indexA);

		indexA = result.indexOf("/");
		return result.substr(0, indexA);
	}

	function getUavAppNameByUrl() {
		var indexA = obj.appurl.indexOf("uavapp_");
		var result = obj.appurl.substr(indexA);
		return result;
	}

	function getGroupStateStr(state) {
		var s = "未定义";
		if (state == 1) {
			s = "正常";
		} else if (state == 0) {
			s = "删除";
		}

		return s;
	}
}