var delmodalConfig = {
    groupid:0,
    head:"提示",
    content:"",
    callback:"clickSureCallback()"
};

function clickSureCallback(){
    delGroup_RESTClient(delmodalConfig.groupid);
}

function userDelete(id){
    delmodalConfig.groupid=id;
    delmodalConfig.content="是否确定删除[<span style='color: red'>"+id+"</span>]";
    showConfirm(delmodalConfig);
}