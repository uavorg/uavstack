var src = window.location.href;
var index = src.indexOf("desc.html?") + 10;
var param = src.substr(index);

window.winmgr.build({
	id : "descDiv",
	height : "auto",
	"overflow-y" : "auto",
	order : 999,
	theme : "ObjectBG"
});
window.winmgr.show("descDiv");

var paramObject = {
	"url" : param,
	"type" : "link"
};

viewNotify_RestfulClient(paramObject);
