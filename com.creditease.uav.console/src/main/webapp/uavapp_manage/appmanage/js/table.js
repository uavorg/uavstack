
var table;
var mtableConfig = {
	id:"AppManagerTableea",
	pid:"AppManagerTableDiv",
	openDelete:true,
	key:"appid",
	pagesize:20,
	pagerSwitchThreshold:600,
	head:{
			appurl       : ['应用名', '30%'],
			appid        : ['APPID', '15%'],
			createtime   : ['创建时间'],
			operationtime : ['操作时间', '15%'],
			operationuser : ['操作人', '10%']
	    },
	cloHideStrategy:{
		1100:[0,1,2,3,4,5],
		1000:[0,1,4,5],
		800:[0,1,5],
		500:[0,1],
		400:[0]
	}
};

function GenerateTable()
{
	table = new AppHubTable(mtableConfig);			    //Config table
	table.setTotalRow(0);								//Set the total rows of the datasource
	table.delRowUser = userDelete;						//Config the delete function of user
	table.cellClickUser = userClickRow;					//Config the cellClick function of user
	table.sendRequest = userSendRequest;				//Config the request table data function
	table.initTable();									//Draw table

};

$("document").ready(function(){
	initSearchBar("AppManagerHeadSearchBar");
	GenerateTable();
});