
var mtableConfig = {
	id:"AppManagerTableada",
	pid:"AppManagerTableDiv",
	openDelete:true,
	key:"id",
	pagerSwitchThreshold:600,
	pagesize : 10,
	head:{
			id         : ['appid', '15%'],
			appurl     : ['appurl', '30%'],
			createTime : ['创建时间'],
			optionTime : ['操作时间', '15%'],
			optionUser : ['操作人', '10%'],
			state      : ['状态', '5%']
	    },
	cloHideStrategy:{
		1100:[0,1,2,3,4,5],
		1000:[0,1,4,5],
		800:[0,1,5],
		500:[0,1],
		400:[0]
	},
	events:{
		/**
		 *  允许在每个cell渲染时加入一些操作，比如改变一下字体什么的
		 * 	onRow:function(index,value) {
			   return value;
		    }
		 */
		appendRowClass:function(rowData){
			if(rowData["state"]==1){
				return "rowCss";
			}else{
				return "";
			}
		}

	}
};

function userDelete(key){
	console.log("table key:"+key);
};

function userClickRow(key){
	console.log("table key:"+key);
};
function userClickHead(obj){
	console.log(obj);
}

function ajaxGetdatas()
{

	var datas = new Array(),number=10;
	for(var i=0; i<number; i++){
		var row={
				id:"id_"+parseInt(Math.random()*100),
				appurl:"http://apphub.test.com",
				createTime:'2016-01-01 12:11:11', 
				optionTime:'2016-01-01 12:11:11',
				optionUser:'system',
				state:parseInt((Math.random()*10)%2)	//测试数据只有1和0，便于行样式判断看效果，依据字段值改变行样式
		};
		datas.push(row);
	}	
	 

	table.clearTable();  //清除数据
	table.setTotalRow(datas.length * 2); //测试数据，看分页效果，所有*2
	getPageParam();
	//此处做分页，得到当前页的datas，渲染当前页面数据，超过pagesize也会都显示
	$.each(datas,function(index,obj){
		table.add(obj);			
	});
		
//	table.addRows(datas); //table.add(row);	2选一
};
function getPageParam(){
	var getPagingInfo = table.getPagingInfo();
	var pageNum = getPagingInfo.pageNum;   
	var pageSize = getPagingInfo.pageSize
	//listObj.setPageNum(1); 设置
	console.log("pageNum:"+pageNum);
	console.log("pageSize:"+pageSize);
}

var table = new AppHubTable(mtableConfig);			//Config table
$("document").ready(function(){
	table.delRowUser = userDelete;						//Config the delete function of user
	table.cellClickUser = userClickRow;					//Config the cellClick function of user
	table.headClickUser = userClickHead;				//Config the headColumnClick function of user
	table.sendRequest = ajaxGetdatas;
	table.initTable();	
});
	
	
	
