var src = window.location.href;
var index = src.indexOf("desc.html?") + 10;
var ntfkeyTime = src.substr(index);

window.winmgr.build({
	id : "descDiv",
	height : "auto",
	"overflow-y" : "auto",
	order : 999,
	theme : "ObjectBG"
});
window.winmgr.show("descDiv");

var paramObject = {
	"action" : "view",
	"ntfkey" : ntfkeyTime,
	"type" : "link"
};

updateNotify_RestfulClient(paramObject);
