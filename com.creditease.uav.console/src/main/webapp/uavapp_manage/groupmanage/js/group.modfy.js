var viewData;
function showViewGroup(result){
    viewData = result.data[0];
    $("#viewGroupDiv").modal({backdrop: 'static', keyboard: false});

    initMody();
    $("#viewGroupid").html(viewData.groupid);
    loadAllApps_RESTClient(function(result){showViewAppids(result);});

}

function initMody(){
    setErrorMsg("modfyErrorMsg","");
    $("#vmButton").text("编辑");
    $("#viewAppids").attr("disabled","disabled");
}

function showViewAppids(result){
    var appids = viewData.appids;
    $("#viewAppids").empty();
    var objDatas = result.data;
    $.each(objDatas,function(index,obj){
        var opTextStr = formatAppInfo(obj);
        if( obj.state==1 && appids.indexOf(obj.appid)!=-1){
            var opStr = "<option value='"+obj.appid+"'>"+opTextStr+"</option>";
            $("#viewAppids").append(opStr);
        }
    });
}


function showModfyAppids(result){

    var oldAppids = viewData.appids;
    
    $("#viewAppids").empty();
    var objDatas = result.data;
    $.each(objDatas,function(index,obj){
    	if(obj.state!=1){
    		return true;
    	}
        var opTextStr = formatAppInfo(obj);
        var opStr;
        

        if(oldAppids.indexOf(obj.appid)!=-1){
        	opStr = "<option value='"+obj.appid+"' selected='selected' >"+opTextStr+"</option>";
        }else{
        	opStr = "<option value='"+obj.appid+"'>"+opTextStr+"</option>";
        }
        
        $("#viewAppids").append(opStr);
    });
}




function vmButtonSwitch(){
    var vmbText = $("#vmButton").text();
    if("编辑"==vmbText){
        loadAllApps_RESTClient(function(result){showModfyAppids(result);});
        $("#viewAppids").removeAttr("disabled");
        $("#vmButton").text("保存");
    }else if("保存"==vmbText){
        saveModfy();
    }


    function saveModfy(){

        var groupid = $("#viewGroupid").html();
        var appids = $("#viewAppids").val();
        if(thisCheck()){
            updateGroup_RESTClient(groupid,getformatAppids(appids));
        }

        function thisCheck(){
            var c = false;
            if(!appids){
                setErrorMsg("modfyErrorMsg","未授权APP");
            }else{
                c = true;
                setErrorMsg("modfyErrorMsg","");

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
}

