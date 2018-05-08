function showAddGroup(){
    setErrorMsg("addErrorMsg","");
    document.getElementById("addGroupFrom").reset();
    loadAllApps_RESTClient(function(result){loadAppids(result);});
    $("#addGroupDiv").modal({backdrop: 'static', keyboard: false});
}

function loadAppids(result){
    $("#addAppids").empty();
    var objDatas = result.data;
    $.each(objDatas,function(index,obj){
    	if(obj.state==1){
            var opStr = "<option value='"+obj.appid+"'>"+formatAppInfo(obj)+"</option>";
            $("#addAppids").append(opStr);
    	}
    });
}


function addGroupSubmit(){
    //重复提交控制
    if(!iscommit){return;}
    iscommit=false;
    //重复提交控制
    var groupid = $("#addGroupid").val().trim();
    $("#addGroupid").val(groupid);
    groupid = HtmlHelper.inputXSSFilter(groupid);
    var appids = HtmlHelper.inputXSSFilter($("#addAppids").val());
    
    var ldapkey = $("#addLdapKey").val().trim();
 	$("#addKey").val(ldapkey);
 	 ldapkey = HtmlHelper.inputXSSFilter(ldapkey);
 	 
    if(thisCheck()){
        appids = getformatAppids(appids);
        addGroup_RESTClient(groupid,ldapkey,appids);
    }else{
        commitEnd();
    }

    function thisCheck(){
        var c = false;
        if(!groupid){
            setErrorMsg("addErrorMsg","未输入授权组");
        }else if(!appids){
            setErrorMsg("addErrorMsg","未授权APP");
        }else{
            c = true;
            setErrorMsg("addErrorMsg","");

        }

        return c;
    }

    function getformatAppids(appids){
        var result="";
        var isBegin = true;
        $.each(appids,function(index,appid){
            if(!isBegin){
                result += ",";
            }
            result +=appid;
            isBegin = false;
        });
        return result
    }
}
