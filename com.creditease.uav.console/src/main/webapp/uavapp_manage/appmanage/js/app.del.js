var delmodalConfig = {
    appid:0,
    head:"提示",
    content:"",
    callback:"clickSureCallback()"
};

function clickSureCallback(){
	deleteApp_RESTClient(delmodalConfig.appid);
}

function userDelete(id,trObj){
    delmodalConfig.appid=id;
    delmodalConfig.content="是否确定删除[<span style='color: red'>"+trObj.getElementsByTagName("td")[0].id+"</span>]";
    showConfirm(delmodalConfig);
}