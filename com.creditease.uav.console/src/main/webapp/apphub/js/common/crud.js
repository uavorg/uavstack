
function AppHubTable(tablesConfig){
	/**
	 *  table property
	 */
	var self=this;
	self.mconfig = {
		id:"AppManagerTable",
		pid:"AppManagerTableDiv",
		pagesize:10,
		caption:'',
		openDelete:true,
		deleteWidth:'5%',
		key:"id",
		dataSource:"",
		pagerSwitchThreshold:600,
		tableDrag:true,  /*开启拖拽功能*/
		useContentAsId: true, // 使用列的内容作为id的值
		head:{
			id         : ['appid'],
			appurl     : ['appurl'],
			createTime : ['创建时间'],
			optionTime : ['操作时间'],
			optionUser : ['操作人'],
			state      : ['状态']
		},
		/*设置列style*/
		columnStyle :{
			/**
			 * <列名>: "<style script>"
			 * 例如：stime:"font-size:12px;"
			 */
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
			    
			    允许在每行渲染时加入一些计算，添加样式
			 *  appendRowClass:function(rowData) {
				   return "appendClass";
			    }
			 */

		}
	};
	var tableBodyID=null;
	var tablePagerID = null;
	var mTablePagerID = null;
	var mTablePaginationID = null;
	var tableBody = null;
	var container = null;

	var colnum = 5;  	   //start from 0
	var keycol = 0;
	var btnperpagemin = 5;
	var btnperpage = 11;
	var totalrow = 11;  // Need to get from the server
	var totalpage = 0;
	var currentpage = 1;
	
	/*拖拽效果 begin*/
	var dragTableOffset = 30,dragTableMouserIsMove = false;
	var dragtTD = null; // 用来存储当前更改宽度的Table Cell,避免快速移动鼠标的问题
	var dragTable;
	/*拖拽效果 end*/
	
	/**
	 * property init
	 */
	function init(config){
		JsonHelper.merge(self.mconfig, tablesConfig,true,false,true);
		tableBodyID = self.mconfig["id"] + "Body";
		tablePagerID = self.mconfig["id"] + "Pager";
		mTablePagerID = tablePagerID + "Pager";
		mTablePaginationID = tablePagerID + "Pagination";
		container = document.getElementById(self.mconfig["pid"]);
		container.innerHTML = "";
	};
	
	init(tablesConfig);

	//固定绘制部分
	function renderTableFramework(){

		var sb=new StringBuffer();
		sb.append('<table style="text-align:center;" class="table mtable" id="' +self.mconfig.id+ '">\n');
		if(self.mconfig["caption"]!="") {
			sb.append("<caption id=\""+self.mconfig.id+"_caption\">"+ self.mconfig["caption"] +'</caption>');
		}
		sb.append('<thead>\n');
		sb.append('<tr>\n');

		var count = 0;
		for(var k in self.mconfig["head"]){
			count++;
		}
		if(self.mconfig["openDelete"]){
			count++;
		}

		count = 0;
		for(var k in self.mconfig["head"])
		{
			var v = self.mconfig["head"][k];
			var headId = k+"_head";
			sb.append("<th style=\"text-align:center;\" width=\"" + v[1]+ "\" class=\"clum"+ count +"\" id=\""+headId+"\" >" + v[0] + "</th>");
					
			if(self.mconfig["key"] == k){
				keycol = count;
			}
			count++;
		}
		if(self.mconfig["openDelete"]){
			sb.append('<th width="' +self.mconfig["deleteWidth"]+ '" class="clumdelete">' + " " + '</th>\n');
		}
		colnum = count;
		sb.append('</tr>\n</thead>\n');
		sb.append('<tbody id="' + tableBodyID + '"></tbody>\n');
		sb.append('</table>\n');
		sb.append('<div id="'+tablePagerID+'"></div>');
		container.innerHTML += sb.toString();
		
		//绑定head单击事件
		for(var k in self.mconfig["head"])
		{
			var headId = k+"_head";
			document.getElementById(headId).onclick=function(){self.headClickUser(this);};
		}
		
	};

	function gotoPage(pagenum){
		//console.log("goto page+ " + pagenum);
		if(pagenum > totalpage )
			return;
		currentpage = pagenum;

		//send message to the server to get information
		self.sendRequest();

		//render the table body
		self.renderPagination();
	}

	//分页事件
	function pagerBindEvent(){
		var lils = $(container).find('div>ul.pager>li.pagerCtlLeftPage');
		var lirs = $(container).find('div>ul.pager>li.pagerCtlRightPage');
		var lilcs = $(container).find('div>ul.pagination>li.pagerCtlLeftPageC');
		var lircs = $(container).find('div>ul.pagination>li.pagerCtlRightPageC');
		var libtns = $(container).find('div>ul.pagination>li.pagerCtlBtn');
		lils.each(function(i, lil){
			lil.onclick = function(){
				var newpage = currentpage - 1;
				gotoPage(newpage);
			};
		});
		lirs.each(function(i, lir){
			lir.onclick = function(){
				var newpage = currentpage + 1;
				gotoPage(newpage);
			};
		});
		lilcs.each(function(i, lilc){
			lilc.onclick = function(){
				var newpage = (parseInt((currentpage-1)/btnperpage)*btnperpage);
				gotoPage(newpage);
				//console.log(newpage);
			};
		});
		lircs.each(function(i, lirc){
			lirc.onclick = function(){
				var newpage = parseInt((currentpage-1)/btnperpage)*btnperpage+btnperpage+1;
				//console.log("right new page "+newpage);
				gotoPage(newpage);
			};
		});
		libtns.each(function(i, libtn){
			libtn.onclick = function(){
				//console.log(this.getElementsByTagName("a")[0].innerHTML);
				var newpage = parseInt(this.getElementsByTagName("a")[0].innerHTML);
				gotoPage(newpage);
			};
		});
	};

	//绘制分页
	this.renderPagination = function(){

		var sb = new StringBuffer();
		var count;
		totalpage = parseInt((totalrow-1)/parseInt(self.mconfig["pagesize"])) + 1;
		if(totalpage <= 1){
			/*清空处理*/
			document.getElementById(tablePagerID).innerHTML = "";
			return;
		}
		btnCalculate();
		//console.log("btnperpage "+btnperpage+"\n");

		//绘制箭头
		sb.append('<ul class="pager" id="'+mTablePagerID+'">\n');
		if(totalpage <= 1){  //只有一页

		}else{	//多页
			if(currentpage == 1){    //第一页
				sb.append("<li class='next pagerCtlRightPage'><a href='javascript:void(0)' class='glyphicon glyphicon-arrow-right'></a></li>\n");
			}else if(currentpage == totalpage){ //最后一页
				sb.append("<li class='previous pagerCtlLeftPage'><a href='javascript:void(0)' class='glyphicon glyphicon-arrow-left'></a></li>\n");
			}else{
				sb.append("<li class='previous pagerCtlLeftPage'><a href='javascript:void(0)' class='glyphicon glyphicon-arrow-left'></a></li>\n");
				sb.append("<li class='next pagerCtlRightPage'><a href='javascript:void(0)' class='glyphicon glyphicon-arrow-right'></a></li>\n");
			}
		}
		sb.append('</ul>\n');

		//绘制按钮
		sb.append('<ul class="pagination" id="'+mTablePaginationID+'">\n');
		if(totalpage <= btnperpage){   //能全部显示  ，不需要翻页
			for(count=1; count<=totalpage; count++){
				if(count == currentpage){
					sb.append('<li class="active pagerCtlBtn"><a href="#">' +count+ '</a></li>\n');
				}else{
					sb.append('<li class="pagerCtlBtn"><a href="#">' +count+ '</a></li>\n');
				}
			}
		}else{  //需要翻页
			if(parseInt((currentpage-1)/btnperpage) == 0){    //不需往前翻页
				for(count=1; count<=btnperpage; count++){
					if(count == currentpage){
						sb.append('<li class="active pagerCtlBtn"><a href="#">' +count+ '</a></li>\n');
					}else{
						sb.append('<li class="pagerCtlBtn"><a href="#">' +count+ '</a></li>\n');
					}
				}
				sb.append('<li class="pagerCtlRightPageC"><a href="#">&raquo;</a></li>\n');
			}else if((parseInt((currentpage-1)/btnperpage) == parseInt(totalpage/btnperpage))||(totalpage%btnperpage==0 &&(currentpage+btnperpage>=totalpage))){ //不需往后翻页
				sb.append('<li class="pagerCtlLeftPageC"><a href="#">&laquo;</a></li>');
				for(count=parseInt((totalpage-1)/btnperpage)*btnperpage+1;count<=totalpage;count++){
					if(count == currentpage){
						sb.append('<li class="active pagerCtlBtn"><a href="#">' +count+ '</a></li>\n');
					}else{
						sb.append('<li class="pagerCtlBtn"><a href="#">' +count+ '</a></li>\n');
					}
				}
			}else{	//中间，需要前后翻页
				//console.log("r" + currentpage);
				sb.append('<li class="pagerCtlLeftPageC"><a href="#">&laquo;</a></li>');
				for(count=parseInt((currentpage-1)/btnperpage)*btnperpage+1;count<=parseInt((currentpage-1)/btnperpage)*btnperpage+btnperpage;count++){
					if(count == currentpage){
						sb.append('<li class="active pagerCtlBtn"><a href="#">' +count+ '</a></li>\n');
					}else{
						sb.append('<li class="pagerCtlBtn"><a href="#">' +count+ '</a></li>\n');
					}
				}
				sb.append('<li class="pagerCtlRightPageC"><a href="#">&raquo;</a></li>\n');
			}
		}
		sb.append('</ul>\n');
		document.getElementById(tablePagerID).innerHTML = sb.toString();

		pagerBindEvent();
		pagerController();
	};

	/*Bind event*/
	function afterAdd(){
		var trs = $(container).find('table>tbody>tr.body_row');
		trs.each(function(i, tr){
			if(self.mconfig["openDelete"])
			{
				var Xbtn = tr.getElementsByTagName('span')[0];
				var XbtnP;
//				console.log(Xbtn.getAttribute("class"));
				if(Xbtn.getAttribute("class") =="glyphicon glyphicon-remove"){
					XbtnP = Xbtn.parentNode;  //td
					XbtnP.onclick = delRow;
				}

			}
			tr.onmouseover = function(){
//				this.style.background="#F5F5F5";
			};
			tr.onmouseout = function(){
//				this.style.background="white";
			};
		});
		var tds = $(container).find('table>tbody>tr.body_row>td.clumdelete');
		tds.each(function(i, td){
			td.style.background="white";
			td.onmouseover = function(){
				this.style.background="#FF0000";
			};
			td.onmouseout = function(){
				this.style.background="white";
			};
		});
		var normaltds = $(container).find('table>tbody>tr.body_row>td.normalclum');
		normaltds.each(function(i, td){
			td.onclick = cellclick;
		});

		windowSizeCtr();
		window.onresize = windowSizeCtr;
	}

	function ctrShow(num){
		$(".clum"+num).show();
	}

	function ctrHide(num){
		$(".clum"+num).hide();
	}

	function ctrHideall(){
		//console.log("hide all " + colnum);
		for(var i=0; i<colnum; i++){
			ctrHide(i);
		}
	}

	function ctrShowall(){
		for(var i=0; i<colnum; i++)
			ctrShow(i);
	}

	function showPager(){
		$("#"+mTablePagerID).show();
		$("#"+mTablePaginationID).hide();
	}

	function showPagination(){
		$("#"+mTablePaginationID).show();
		$("#"+mTablePagerID).hide();
	}

	function pagerController(){
		var width = document.documentElement.clientWidth-10;
		if(width <= parseInt(self.mconfig["pagerSwitchThreshold"])){
			showPager();
		}
		else{
			showPagination();
		}
	}

	function showSomecol(){
		var width = document.documentElement.clientWidth-10;
		var count = 0;
		for(var key in self.mconfig["cloHideStrategy"]){  //up
			var p = parseInt(key);
			//console.log(self.mconfig["cloHideStrategy"]);
			if(width <= p){
				var showcol = self.mconfig["cloHideStrategy"][key];
				count++;
				for(var k in showcol){
					
					var valk=showcol[k];
					
					if (typeof valk=="function") {
						continue;
					}
					
					ctrShow(valk);
				}
				break;
			}
			else continue;
		}
		if(count == 0){
			ctrShowall();
		}
	}

	function btnCalculate(){
		var width = document.documentElement.clientWidth-10;
		var cal = parseInt(width/50) - 3;
		if(cal < btnperpagemin){
			cal = btnperpagemin;
		}
		btnperpage = cal;
	}

	function windowSizeCtr(){
		ctrHideall();
		showSomecol();
		pagerController();
		btnCalculate();
		self.renderPagination();
	};

	/**
	 * 删除按钮响应事件
	 */
	function delRow(){
		var pNode = this.parentNode;  //tr
		self.delRowUser(pNode.getElementsByTagName("td")[keycol].id,pNode);
	};

	/**
	 * 行单击事件
	 */
	function cellclick(){
		var pNode = this.parentNode;  //tr
		self.cellClickUser(pNode.getElementsByTagName("td")[keycol].id,pNode);
	};

	/*************Functions can be accessed start*****************/
	/**
	 * 设置表格总行数
	 */
	this.setTotalRow = function(N){
		totalrow = N;
	};

	/**
	 * 初始化表格显示
	 */
	this.initTable = function(){
		renderTableFramework();   //draw table framework
//		renderPagination();		  //draw pagination
		self.sendRequest();
		if(self.mconfig.tableDrag){
			self.tableDrag();	
		}
		
	};
	
	
	this.getPagingInfo = function(){
		return {
			"pageSize":self.mconfig["pagesize"],
			"pageNum":currentpage
		}
	}
	
	this.setPageNum = function(pageNum){
		currentpage = pageNum;
	}

	/**
	 * 表格增加一行数据
	 */
	this.add = function(row){
		/*ID字段值*/
		var idColumnName = self.mconfig['key'];
		var idColumnValue = row[idColumnName];

		var sb=new StringBuffer();

		var trClass="body_row ";
		if (self.mconfig.events.appendRowClass!=undefined) {
			trClass += self.mconfig.events.appendRowClass(row);
		}
		sb.append('<tr class="'+trClass+'" id="'+idColumnValue+'">\n');
		
		var count = 0;
		for(var k in self.mconfig['head']){
			
			var colStyle="";
			
			if (self.mconfig.columnStyle[k]!=undefined) {
				colStyle="style:'"+self.mconfig.columnStyle[k]+"'";
			}
			
			var tagId = '';
			if (self.mconfig.useContentAsId){
				tagId = 'id="' + row[k] + '" ';
			}
			sb.append('<td ' + tagId + 'class="normalclum clum' + count + '" '+colStyle+'>');
			
			if (self.mconfig.events.onRow!=undefined) {
				sb.append(self.mconfig.events.onRow(count,row[k]));
			}
			else {
				sb.append(row[k]);
			}
			sb.append('</td>\n');
			count++;
		}
		if(self.mconfig["openDelete"]){
			if(isSupportDelete(row)){
				sb.append('<td class="clumdelete"><span class="glyphicon glyphicon-remove"></span></td>');
			}else{
				sb.append('<td class="clumnodelete"><span class="glyphicon glyphicon-ban-circle"></span></td>');
			}		
		}
		sb.append('</tr>\n');
		if(tableBody == null){
			tableBody = document.getElementById(tableBodyID);
		}
		tableBody.innerHTML += sb.toString();

		afterAdd();
	};
	
	/**
	 * 为可伸缩表格增加多行数据
	 */
	this.addTreeTableRows = function(rows){
		
		if (rows.length==0) {
			alert("无任何数据");
		}

		var sb = new StringBuffer();
		for(var i=0; i<rows.length; i++){
			// modify start
			var row = rows[i];
			var idColumnName = self.mconfig['key'];
			var idColumnValue = row[idColumnName];
			
			var trClass="body_row ";
			if (self.mconfig.events.appendRowClass!=undefined) {
				trClass += self.mconfig.events.appendRowClass(row);
			}
			
			var tt_id = row["spanid"];
			var tt_parent_id = row["parentid"];
			if(tt_parent_id=='N'){
				sb.append('<tr'+' data-tt-id="'+tt_id+'" class="'+trClass+'" id="'+idColumnValue+'"  style="text-align:left;">\n');
			}
			else{
				sb.append('<tr'+' data-tt-id="'+tt_id+'"'+' data-tt-parent-id="'+tt_parent_id+'"'+' class="'+trClass+'" id="'+idColumnValue+'"  style="text-align:left;">\n');
			}
			//buf.push('<tr class="body_row">\n');
			// modify end
			
			var count = 0;
			var row = rows[i];
			for(var k in self.mconfig['head']){
				
				var colStyle="";
				
				if (self.mconfig.columnStyle[k]!=undefined) {
					colStyle="style='"+self.mconfig.columnStyle[k]+"'";
				}
				
				var tagId = '';
				if (self.mconfig.useContentAsId){
					tagId = 'id="' + row[k] + '" ';
				}
				sb.append('<td title="'+row[k]+'" ' + tagId + 'class="normalclum clum' + count + '" '+colStyle+'>');
				
				if (self.mconfig.events.onRow!=undefined) {
					sb.append(self.mconfig.events.onRow(count,row[k]));
				}
				else {
					sb.append(row[k]);
				}
				sb.append('</td>\n');
				count++;
			}
			if(self.mconfig["openDelete"]){
				if(isSupportDelete(row)){
					sb.append('<td class="clumdelete"><span class="glyphicon glyphicon-remove"></span></td>');
				}else{
					sb.append('<td class="clumnodelete"><span class="glyphicon glyphicon-ban-circle"></span></td>');
				}
			}
			sb.append('</tr>\n');
		}
		if(tableBody == null){
			tableBody = document.getElementById(tableBodyID);
		}
		tableBody.innerHTML += sb.toString();
		afterAdd();
	};
	
	/**
	 * 表格增加多行数据
	 */
	this.addRows = function(rows){
		
		if (rows.length==0) {
			alert("无任何数据");
		}

		var sb = new StringBuffer();
		for(var i=0; i<rows.length; i++){
			// modify start
			var row = rows[i];
			var idColumnName = self.mconfig['key'];
			var idColumnValue = row[idColumnName];
			
			var trClass="body_row ";
			if (self.mconfig.events.appendRowClass!=undefined) {
				trClass += self.mconfig.events.appendRowClass(row);
			}
			sb.append('<tr class="'+trClass+'" id="'+idColumnValue+'">\n');
			//buf.push('<tr class="body_row">\n');
			// modify end
			
			var count = 0;
			var row = rows[i];
			for(var k in self.mconfig['head']){
				
				var colStyle="";
				
				if (self.mconfig.columnStyle[k]!=undefined) {
					colStyle="style:'"+self.mconfig.columnStyle[k]+"'";
				}
				
				var tagId = '';
				if (self.mconfig.useContentAsId){
					tagId = 'id="' + row[k] + '" ';
				}
				sb.append('<td ' + tagId + 'class="normalclum clum' + count + '" '+colStyle+'>');
				
				if (self.mconfig.events.onRow!=undefined) {
					sb.append(self.mconfig.events.onRow(count,row[k]));
				}
				else {
					sb.append(row[k]);
				}
				sb.append('</td>\n');
				count++;
			}
			if(self.mconfig["openDelete"]){
				if(isSupportDelete(row)){
					sb.append('<td class="clumdelete"><span class="glyphicon glyphicon-remove"></span></td>');
				}else{
					sb.append('<td class="clumnodelete"><span class="glyphicon glyphicon-ban-circle"></span></td>');
				}
			}
			sb.append('</tr>\n');
		}
		if(tableBody == null){
			tableBody = document.getElementById(tableBodyID);
		}
		tableBody.innerHTML += sb.toString();
		afterAdd();
	};
	
	function isSupportDelete(row){
		var del = self.mconfig["deleteCtr"];
		if(del == undefined){
			return true;
		}	
		var judgeClum = del["key"];
		var judgeValue = del["showDelete"];
		if(judgeClum == undefined || judgeValue == "" || judgeValue == undefined){
			return true;
		}
		if(row[judgeClum] == judgeValue){
			return true;
		}else{
			return false;
		}
	};
	
	/**
	 * 用户处理删除事件
	 */
	this.delRowUser = function(id){
	};

	/**
	 * 用户处理行单击事件
	 */
	this.cellClickUser = function(id){		
	};
	
	/**
	 * 用户处理字段标题单击事件,接收函数需要无参，会自动传入thisObj参数
	 * @param thisObj:头部标题this对象
	 */
	this.headClickUser = function(){		
	};
	
	

	/**
	 * 清空表格
	 */
	this.clearTable = function(){
		if(tableBody == undefined){
			tableBody = document.getElementById(tableBodyID);
		}
		tableBody.innerHTML = "";
	};

	/**
	 * 发送请求
	 */
	this.sendRequest = function(){
	};

	/**
	 * 获取ID所在列
	 */
	this.getKeyClum = function(){
		return keycol;
	};

	/**
	 * 隐藏分页功能
	 */
	this.hidePagerPagination = function(){
		$("#"+mTablePaginationID).hide();
		$("#"+mTablePagerID).hide();
	};
	
	/*拖拽效果*/
	this.tableDrag = function() {
		dragTable = document.getElementById(self.mconfig.id);
		for (var j = 0; j < dragTable.rows[0].cells.length; j++) {

			dragTable.rows[0].cells[j].onmousedown = function(_e) {
				var eX, eOffsetX, diffX;
				if (HtmlHelper.isFF()) {
					eOffsetX = _e.layerX;
					eX = _e.pageX;
					diffX = this.offsetWidth + this.offsetLeft
							- dragTableOffset;
				} else {
					eOffsetX = window.event.offsetX;
					eX = window.event.x;
					diffX = this.offsetWidth - dragTableOffset;
				}

				window.getSelection().removeAllRanges();
				if (!dragTableMouserIsMove && dragtTD == null
						&& eOffsetX > diffX) {
					// 记录单元格
					dragtTD = this;
					dragTable.style.cursor = 'e-resize';
					dragtTD.oldX = eX;
					dragtTD.oldWidth = dragtTD.offsetWidth;
				}

				if (dragTableMouserIsMove) {
					dragTableMouserIsMove = false;
				}

			};

			dragTable.rows[0].cells[j].onmousemove = function(_e) {

				var eOffsetX, diffX;
				if (HtmlHelper.isFF()) {
					eOffsetX = _e.layerX;
					diffX = this.offsetWidth + this.offsetLeft
							- dragTableOffset;
				} else {
					eOffsetX = window.event.offsetX;
					diffX = this.offsetWidth - dragTableOffset;
				}

				// 更改鼠标样式
				// console.log(this,eOffsetX,(this.offsetWidth+this.offsetLeft),this.parentNode.offsetLeft);
				// console.log(this.offsetWidth)
				if (dragTable.style.cursor == 'e-resize') {
					this.style.cursor = 'e-resize';
					dragTableMouserIsMove = true;
				} else if (eOffsetX > diffX) {
					this.style.cursor = 'e-resize';
				} else {
					this.style.cursor = 'default';
				}

			};

			dragTable.rows[0].cells[j].onmouseout = function() {
				dragTableMouserIsMove = false;
			};
		}

		document.onmouseup = function() {
			if (dragtTD) {
				dragtTD = null;
				dragTable.style.cursor = 'default';
			}

		};

		document.onmousemove = function(_e) {

			if (!dragtTD) {
				return;
			}
			// 调整宽度
			if (dragtTD != null) {

				var eX;
				if (HtmlHelper.isFF()) {
					eX = _e.pageX;
				} else {
					eX = window.event.x;
				}

				window.getSelection().removeAllRanges();
				if (dragtTD.oldWidth + (eX - dragtTD.oldX) > 0) {
					dragtTD.width = dragtTD.oldWidth + (eX - dragtTD.oldX);
				}

				// console.log(dragtTD.oldWidth,eX,dragtTD.oldX,dragtTD.width);
				// 调整列宽
				dragtTD.style.width = dragtTD.width;

				// 调整该列中的每个Cell
				dragTable = dragtTD;
				while (dragTable.tagName != 'TABLE') {
					dragTable = dragTable.parentElement;
				}

				for (j = 0; j < dragTable.rows.length; j++) {
					dragTable.rows[j].cells[dragtTD.cellIndex].width = dragtTD.width;
				}
			}
		};
	}
	
	/**
	 * set caption
	 */
	this.setCaption=function(text) {
		
		var caption=HtmlHelper.id(self.mconfig.id+"_caption");
		
		if (caption==undefined) {
			return;
		}
		
		caption.innerHTML=text;
	}
	
}


/*
 * 对话框 BEGIN
 * 
 */

function generateDialog(config, id)
{
	var sb=new StringBuffer();
	sb.append('<div class="modal fade" id="'+ id+'" aria-hidden="true">\n');
	sb.append('<div class="modal-dialog">\n');
	sb.append('<div class="modal-content">\n');
	sb.append('<div class="modal-header">\n');
	sb.append('<h5>'+config["head"]+'</h5>\n');
	sb.append('</div>\n');
	sb.append('<div class="modal-body">\n');
	sb.append(config["content"]+'\n');
	sb.append('</div>\n');
	sb.append('<div class="modal-footer">\n');
	sb.append('<button class="btn btn-primary" data-dismiss="modal">确定</button>\n');
	sb.append('</div>\n');
	sb.append('</div>\n');
	sb.append('</div>\n');
	sb.append('</div>\n');

	//document.getElementsByTagName("body")[0].innerHTML += buf.join('');
	//解决页面卡死问题
	$('body').append(sb.toString());
};


function showDialog(config){
	var Num="";
	for(var i=0;i<6;i++)
	{
		Num+=Math.floor(Math.random()*10);
	}

	var id = "ModalID"+Num;
	generateDialog(config, id);

	$("#"+id).on("hide.bs.modal", function() {
		var node = document.getElementById(id);
		node.parentNode.removeChild(node);
	});

	$("#"+id).modal({backdrop: 'static', keyboard: false});
};

/*
 * 对话框 END
 *
 */

/*
 * 验证框 BEGIN
 *
 */

function generateConfirm(config, id)
{
	var sb = new StringBuffer();
	sb.append('<div class="modal fade" id="'+ id+'" aria-hidden="true">\n');
	sb.append('<div class="modal-dialog">\n');
	sb.append('<div class="modal-content">\n');
	sb.append('<div class="modal-header">\n');
	sb.append('<h5>'+config["head"]+'</h5>\n');
	sb.append('</div>\n');
	sb.append('<div class="modal-body">\n');
	sb.append(config["content"]+'\n');
	sb.append('</div>\n');
	sb.append('<div class="modal-footer">\n');
	sb.append('<button class="btn btn-primary" data-dismiss="modal" onclick="'+config["callback"] +'">确定</button>\n');
	sb.append('<button class="btn" data-dismiss="modal">取消</button>\n');
	sb.append('</div>\n');
	sb.append('</div>\n');
	sb.append('</div>\n');
	sb.append('</div>\n');

	//document.getElementsByTagName("body")[0].innerHTML += buf.join('');
	//解决页面卡死问题
	$('body').append(sb.toString());
};

function showConfirm(config){
	var Num="";
	for(var i=0;i<6;i++)
	{
		Num+=Math.floor(Math.random()*10);
	}

	var id = "ModalID"+Num;
	generateConfirm(config, id);
	$("#"+id).on("hide.bs.modal", function() {
		var node = document.getElementById(id);
		node.parentNode.removeChild(node);
	});

	$("#"+id).modal({backdrop: 'static', keyboard: false});
};

/*
 * 验证框 END
 *
 */


