var delmodalConfig = {
		id:0,
    head:"提示",
    content:"",
    callback:"clickSureCallback()"
};

function clickSureCallback(){
    delGroup_RESTClient(delmodalConfig.id);
}

function userDelete(id){
    delmodalConfig.id=id;
    delmodalConfig.content="是否确定删除[<span style='color: red'>"+id+"</span>]";
    showConfirm(delmodalConfig);
}