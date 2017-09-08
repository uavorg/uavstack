var delmodalConfig = {
	key:0,
    head:"提示",
    content:"",
    callback:"clickSureCallback()"
};

function clickSureCallback(){
	removeNotify_RestfulClient(delmodalConfig.key);
}

function userDelete(key){
    delmodalConfig.key=key;
    delmodalConfig.content="是否确定删除[<span style='color: red'>"+key+"</span>]";
    showConfirm(delmodalConfig);
}