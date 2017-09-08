/***
 *  AppHubChart 通用Chart组件
 *  支持DynamicLine
 *  api依赖js：helper.js、jquery-x.x.x.js（此API基于2.1.4开发）
 * @param _chartsConfig  : json赋值
 * @constructor
 */
function AppHubChart(_chartsConfig){
	
	/**
	 * Base High Chart Class
	 */
	function BaseHighChart() {
		
		this.chart;  //chart object
		this.config; //chart configuration
		//init chart config
		this.init=function(cfg,initData) {
			//require impl
			this.config={};
		};
		//draw chart
		this.draw = function(reset) {
			if (true == reset) {
				var chartObj = this.config.chart.appHubChartObj;
				switch (chartObj) {
					case "stock":
						this.chart = new Highcharts.StockChart(this.config);
						break;
					default:
						this.chart = new Highcharts.Chart(this.config);
						break;
				}
			}
			this.chart.redraw();
		};
		//set chart size
		this.setSize=function(w,h) {
			if (this.chart) {
				this.chart.setSize(w,h,false);
			}
		};
		this.setDataLabels=function(){
			var type = this.config.chart.type;
			if(type == "spline"||type=="column"||type=="area"){
				var cid=chartObj.cfg["cid"];
				var width = HtmlHelper.width(cid);
				this.config.plotOptions.series.dataLabels.enabled = width<415 ?false:true;
			}
		};
		this.setDistance =function(){
			var type = this.config.chart.type;
			if(type == "pie"){
				var cid=chartObj.cfg["cid"];
				var width = HtmlHelper.width(cid);
				this.config.plotOptions.pie.dataLabels.distance = width<415 ?-30:20;
			}
		};
		//add new data to chart
		this.addData=function(arrayData) {
			var cs = this.chart.series;
	        for(var i=0;i<cs.length;i++) {
	        	var seriesObj=cs[i];
	        	if(arrayData[i]){
	        		var arrayDataS=arrayData[i];
	        		for(var j=0;j<arrayDataS.length;j++) {
		                var point = this.getPointObj(arrayDataS[j]);
		                seriesObj.addPoint(point,false,true);
	        		}
	            }
	        }
		};
		//get chart point
		this.getPointObj=function(dataObj) {
	        var point = new Array();
            point[0] = dataObj.x;
            point[1] = parseFloat(dataObj.y);
            return point;
		};
		//windows.onresize  set
		this.resizeSet = function(){
			if ("auto"==chartObj.cfg["width"]) {
				HtmlHelper.addEvent(window, "resize", function(e) {
					var chartId=chartObj.cfg["id"];
					var cid=chartObj.cfg["cid"];
					var width = HtmlHelper.width(cid);
					window["appcharts"].resize(width, undefined, chartId);
				});
			}
		};
		//windows.onresize dataLabels set
		this.initDataLabelsSet=function(){
			if ("auto"==chartObj.cfg["width"]) {
				var cid=chartObj.cfg["cid"];
				var width = HtmlHelper.width(cid);

				var chartType = chartObj.cfg["type"];
				switch (chartType) {
				case "stock":
					chartObj.chart.config.plotOptions.series.dataLabels.enabled = false;
					break;
				default:
					chartObj.chart.config.plotOptions.series.dataLabels.enabled = width<415 ?false:true;
					break;
				}
			}
		};
		//get config set width
		this.getCfgWidth= function(){
			var width=HtmlHelper.width(chartObj.cfg["cid"]);
			if (undefined!=chartObj.cfg["width"]&&"auto"!=chartObj.cfg["width"]) {
				width=parseInt(chartObj.cfg["width"]);
			}
			return width;
		};
		//设置分辨带
		this.setPlotBands=function(){
			var pbObj = chartObj.cfg.plotBands;
			for(var i=0;i<pbObj.length;i++){
				var obj = pbObj[i];
				var option = {
					from:obj[0],
					to:obj[1],
					color: obj[3] ? obj[3] : (i%2==0?'rgba(68, 170, 213, 0.1)': 'rgba(0, 0, 0, 0)'),
					label:{
						text:obj[2],
					}
				};
				chartObj.chart.config.yAxis.plotBands[i]=option;
			}
		};
		//init highcharts  Options
		this.initHighcharts=function(){
			/**
			 * 是否使用UTC时间轴缩放
			 */
			Highcharts.setOptions({
				global: {
					useUTC: false
				}
			});
		};

	};


	/**
	 * StaticLine Class
	 */
	function StaticLine() {
		BaseHighChart.call(this);
		/**
		 * 独特的配置项
		 */
		this.myconfig={
			timeformat:"FS",
			inverted:false
		};

		//@Override
		this.init=function(cfg,initData) {
			this.initHighcharts();
			this.resizeSet();
			JsonHelper.merge(this.myconfig, cfg["spline"], true);

			/**
			 * highcharts ChartOptions
			 */
			this.config = {
				chart: {
					renderTo: cfg["id"],
					type: cfg["type"],
					width:  this.getCfgWidth(),
					height: cfg["height"] ? cfg["height"] : 400,
					inverted:  cfg["inverted"] ,  /**轴反转*/
					animation: Highcharts.svg, /** don't animate in old IE*/
					appHubChartObj:"staticLine",
					drawAction:true  /*重绘动作参数*/
				},
				title: {
					text: cfg["title"],
					align:(cfg["titleAlign"])?cfg["titleAlign"]:"center",
				    style:{
				    	"fontSize":"12px"
				    }
				},
				subtitle: {
					text: cfg["subtitle"]
				},
				xAxis: {
					reversed: false,   /**轴反转：向上方向*/
					type:cfg.xAxis["type"],
					title: {
						text: cfg.xAxis["title"]
					}
				},
				yAxis: {
					title: {
						text: cfg.yAxis["title"]
					},
					plotLines: [{
						value: 0,
						width: 1,
						color: '#808080'
					}]
					,plotBands:[]
				},
				tooltip: {
				},
				plotOptions: {
					series: {
						dataLabels: {
							enabled: true
						}
					}
				},
				legend:cfg["legend"],
				series: []
			};
			
			// set x labels
			if (cfg.xAxis["labels"]) {
				this.config["xAxis"]["labels"]=cfg.xAxis["labels"];
			}
			
			// set y labels
			if (cfg.yAxis["labels"]) {
				this.config["yAxis"]["labels"]=cfg.yAxis["labels"];
			}
			
			/**
			 * if there is categories, then set it
			 */
			if (cfg.xAxis["categories"]) {
				this.config["xAxis"]["categories"]=cfg.xAxis["categories"];
			}
			var datas = new Array();
			for(var i=0;i<initData.length;i++){
				datas[i] = new Object({
					"name": cfg.series[i]["name"],
					"data":initData[i],
					"color":cfg.series[i]["color"]
				});
			};
			this.config.series = datas;

			this.setPlotBands();
			this.initDataLabelsSet();

		};


	};


	/**
	 * Dynamic Line Class
	 */
	function DynamicLine() {

		BaseHighChart.call(this);
		
        /**
         * 独特的配置项
         */
        this.myconfig={
    		timeformat:"FS",
        	maxpoints:10,
        	interval:5000
        };
        
        //@Override
		this.getPointObj=function(dataObj) {
	        var point = new Array();
	        var isTime=(this.config["xAxis"]["type"]=="datetime")?true:false;
	        var timeformat=this.myconfig["timeformat"];
            point[0] = (isTime)?TimeHelper.getTime(dataObj.x,timeformat):dataObj.x;
            point[1] = parseFloat(dataObj.y);
	        return point;
		};
		//@Override
		this.init=function(cfg,initData) {
			this.initHighcharts();
			this.resizeSet();
			JsonHelper.merge(this.myconfig, cfg["dline"], true);
	        /**
	         * highcharts ChartOptions
	         */
	       this.config = {
	            chart: {
	                renderTo: cfg["id"],
	                type: "spline",
	                width:  this.getCfgWidth() ,
	                height: cfg["height"] ? cfg["height"] : 400,
					inverted:  cfg["inverted"] ,  /**轴反转*/
	                animation: Highcharts.svg, /** don't animate in old IE*/
					appHubChartObj:"dynamicLine",
					drawAction:true  /*重绘动作参数*/
	            },
	            title: {
	                text: cfg["title"],
					align:(cfg["titleAlign"])?cfg["titleAlign"]:"center",
					style:{
				    	"fontSize":"12px"
				    }
	            },
	            subtitle: {
	                text: cfg["subtitle"]
	            },
	            xAxis : {
		            type:cfg["xAxis"].type,
					reversed: false,   /**轴反转：向上方向*/
		            title: {
		                text: cfg["xAxis"].title
		            },
		            //tickPixelInterval:(cfg["xAxis"].xTickPixel > 0 ? cfg["xAxis"].xTickPixel : 10),
		            allowDecimals: false, /**不显示小数点*/
		            labels: {
		                /**
		                 *  style设置：window.onresize计算赋值，此处声明，后续实现
		                 */
		            }
	        	},
	        	yAxis : {
		            title: {
		                text: cfg["yAxis"].title
		            },
		            tickInterval:cfg["yAxis"].tickInterval>0 ?cfg["yAxis"].tickInterval:10,
		            //tickPixelInterval: cfg["yAxis"].yTickPixel > 0 ? cfg["yAxis"].yTickPixel : 10,
		            allowDecimals: false,/**不显示小数点*/
		            labels: {
		                /**
		                 *  style设置：window.onresize计算赋值，此处声明，后续实现
		                 */
		            },
					plotBands:[],
		            max:cfg["yAxis"]["max"],
		            min:cfg["yAxis"]["min"]
		        },
	            tooltip: {
	            },
			   plotOptions: {
				   series: {
					   dataLabels: {
						   enabled: true
					   }
				   }
			   },
	            legend:cfg["legend"],
	            exporting: {
	                enabled: false
	            },
	            series: cfg["series"]
	        };

	        
	        var initXDataArray=initData[0];
	        
	        var maxpoints=this.myconfig["maxpoints"];
	        
	        var interval=this.myconfig["interval"];
	        
	        var timeformat=this.myconfig["timeformat"];
	        
	        var gapNumber=maxpoints-initXDataArray.length;
	        
	        var firstXData=initXDataArray[0]["x"]-interval*gapNumber;
	        
	        var isTime=(cfg["xAxis"].type=="datetime")?true:false;
	        
	        var cate=[];
	        for(var i=0;i<maxpoints;i++) {
	        	var cXData;
	        	if (i<gapNumber) {
	        		cXData=firstXData+i*interval;
	        	}
	        	else {
	        		cXData=initXDataArray[i-gapNumber]["x"];
	        	}
	        	cate[i]=(isTime)?TimeHelper.getTime(cXData,timeformat):cXData;
	        }
	        
	        this.config.xAxis["categories"]=cate;
	        
	        for (var i=0;i<this.config.series.length;i++) {
	        	var ser=this.config.series[i];
	        	
	        	for(var j=0;j<maxpoints;j++) {
	        		
	        		if (j<gapNumber) {
	        			ser.data[j]=[cate[j],0];
	        		}
	        		else {
	        			
	        			if (initData[i]==undefined) {
	        				break;
	        			}
	        			
	        			var cYData=initData[i][j-gapNumber]["y"];
	        			
	        			ser.data[j]=[cate[j],parseFloat(cYData)];
	        		}
	        	}

	        }

			this.setPlotBands();
			this.initDataLabelsSet();
		};

    };


	/**
	 * Pie Class
	 */
	function Pie() {

		BaseHighChart.call(this);

		/**
		 * 独特的配置项
		 */
		this.myconfig={
		};

		//@Override
		this.init=function(cfg,initData) {
			this.initHighcharts();
			this.resizeSet();
			JsonHelper.merge(this.myconfig, cfg["pie"], true);
			/**
			 * highcharts ChartOptions
			 */
			this.config = {
				chart: {
					renderTo: cfg["id"],
					type: "pie",
					plotBackgroundColor: null,
					plotBorderWidth: null,
					plotShadow: false,
					height: cfg["height"] ? cfg["height"] : 300,
					appHubChartObj:"pie",
					drawAction:true  /*重绘动作参数*/
				},
				title: {
					text: cfg["title"],
					align:(cfg["titleAlign"])?cfg["titleAlign"]:"center",
					style:{
				    	"fontSize":"12px"
				    }
				},
				tooltip: {
					pointFormat: '<b>{point.percentage:.1f}%</b>'
				},
				plotOptions: {
					pie: {
						allowPointSelect: true,
						cursor: 'pointer',
						dataLabels: {
							enabled: true,
							color: '#000000',
							connectorColor: '#000000',
							format: '<b>{point.name}</b><br/> {point.percentage:.1f} %'
							,distance: this.getCfgWidth()<415?-30:20
						}
					}
				},
				series: [
					{type: 'pie',
					//name: 'Browser share',
					data: []
					}
				]
			};

			var ser=this.config.series;
			var datas = new Array();
			for(var i=0;i<initData.length;i++){

				datas[i] = new Object({
					"name": cfg.series[i]["name"],
					"y":initData[i],
					"color":cfg.series[i]["color"]
				});
			}
			ser[0].data = datas;


		};

	};
    
	/**
	 * TODO  带导航条图表Class
	 */
	function Stock() {

		BaseHighChart.call(this);
		/**
		 * 独特的配置项
		 */
		this.myconfig={
		};
		
		//@Override
		this.init = function(cfg, initData) {
			this.initHighcharts();
			this.resizeSet();
			JsonHelper.merge(this.myconfig, cfg["pie"], true);
			/**
			 * highcharts ChartOptions
			 */
			this.config = {
				chart : {
					renderTo : cfg["id"],
					type : "spline",
					width : this.getCfgWidth(),
					height : cfg["height"] ? cfg["height"] : 400,
					inverted : cfg["inverted"],
					/** 轴反转 */
					animation : Highcharts.svg,
					/** don't animate in old IE */
					appHubChartObj : "stock",
					drawAction : false
				/* 重绘动作参数 */
				},
				title : {
					text : cfg["title"],
					align : (cfg["titleAlign"]) ? cfg["titleAlign"] : "center",
					style : {
						"fontSize" : "12px"
					}
				},
				credits: {
					enabled: false  //去除右下角广告链接
			    },
			    scrollbar:{
			          //  buttonArrowColor: 'bule',  //时间滚轴按钮颜色
			            rifleColor: 'red',
			            enabled:true
			    },
				rangeSelector : { //选择时间范围
					enabled:false
				},
				navigator:{
					maskInside:false,
					margin: 10,
					height: 25
				},
				legend : {
					enabled : true,
					verticalAlign : "top"
				},
				yAxis : {
				},
				plotOptions : {
					series : {
						dataLabels : {
							enabled : false //initDataLabelsSet 会计算为 false，此处声明不会起作用，只是作为显明有此属性
						}
					}
				},
	            tooltip: {
	                pointFormat: '<span style="color:{series.color}">{series.name} </span><b>{point.y}</b> <br/>',
	                valueDecimals: 2, //小数点精确
	                hideDelay:0,  //提示框隐藏延时。当鼠标移出图标后，数据提示框会在设定的延迟时间后消失。 默认值：500.
	                split: true,  //提示框是否分离
	                backgroundColor: {
	                    linearGradient: [0, 0, 0, 60],
	                    stops: [
	                        [0, '#FFFFFF'],
	                        [1, '#E0E0E0']
	                    ]
	                },
	                borderWidth: 1,
	                borderColor: '#AAA',
	                xDateFormat: '%Y-%m-%d %H:%M:%S.%L'
	            },
				series : []
			};

			var datas = new Array();
			for (var i = 0; i < initData.length; i++) {

				datas[i] = new Object({
					"name" : cfg.series[i]["name"],
					"data" : initData[i],
					"color" : cfg.series[i]["color"]
				});
			}

			this.config.series = datas;

			this.setPlotBands();
			this.initDataLabelsSet();
		};

	};
    
	
    // ------------------------------------以下代码通用，与HighCharts实现没有任何关系---------------------------------------
	var chartObj;
	
	var rawCfg={
		chart:undefined,     //chart object
		cfg:{
		    	type:"line", //chart type
		    	cid:"",      //chart container id
		    	title:"",    //chart title
		        subtitle:"", //chart subtitle
		        width:0,     //chart width
		        height:0,    //chart height
				inverted:false, //chart 轴反转
				plotBands:[],  //分辨带 /** [["150","200","压力","color"]]*/
		        legend:{     //chart legend equals to HCharts Legend
		        	enabled:true,       //enable
		            align:"center"     //legend align
		        },
		        yAxis:{
		        	title:"",          //y title
		        	type:"linear"      //y data type
		        },
		        xAxis:{
		        	title:"",          //x title
		        	type:"datetime",   //x data type
		        },
		        series:[               //chart series equals to HCharts Series
		                /**
		                 * {
		                 * name:<series name>
		                 * data:[<init data>]
		                 * }
		                 */
		        ]
		}
	};
	
	chartObj=JsonHelper.clone(rawCfg);
    
    /** merge config : param init */
    JsonHelper.merge(chartObj.cfg, _chartsConfig,true);

    /**
     * 创建容器
     */
    function initContainer(){
        //create chart layer
    	var chartsContainer = document.createElement("div");
        chartsContainer.id = chartObj.cfg["id"];
        HtmlHelper.id(chartObj.cfg["cid"]).appendChild(chartsContainer);
    };

    function draw(arrayData,reset) {
    	
    	if (chartObj.chart==undefined) { 
	    	var chartType=chartObj.cfg["type"];

	    	switch(chartType) {
				case "spline":
				case "area":
				case "column":
					chartObj.chart=new StaticLine();
					break;
	    		case "dline":
	    			chartObj.chart=new DynamicLine();
	    			break;
				case "pie":
					chartObj.chart=new Pie();
					break;
				case "stock":
					chartObj.chart=new Stock();
					break;
	    	}
	    	
	    	chartObj.chart.init(chartObj.cfg,arrayData);
    	}
    	else {
    		chartObj.chart.addData(arrayData);
    	}

    	chartObj.chart.draw(reset);
    }
    
    this.resize=function(w,h) {
    	if (chartObj.chart) {
    		chartObj.chart.setDataLabels();
    		chartObj.chart.setDistance();
			/**
			 * 注意重绘 BEGIN
			 */
    		
    		var action = chartObj.chart.config.chart["drawAction"];
			chartObj.chart.draw(action);
			/**
			 * 注意重绘 END
			 */
			var height=(h)?h:chartObj.cfg["height"];
			chartObj.chart.setSize(w,height);
    	}
    };

    this.destroy=function() {
    	HtmlHelper.del(chartObj.cfg["id"]);
    	chartObj.chart=undefined;
    };

    /**
     * 添加显示点数据，渲染
     * @param arrayData :  [{x,y},{x,y}...]
     *                   : x ,y 必选
     *                    
     */
    this.run = function(arrayData){
    	
        if(!arrayData){
            return;
        }
        
        if (chartObj.chart==undefined) {
        	initContainer();
            draw(arrayData,true);
    	}
        else {       
	        draw(arrayData,false);
        }
    };
    
    /**
     * allow to reload configuration
     * @param cfg
     */
    this.reload=function(cfg) {
    	this.destroy();
    	/** merge config : param init */
    	chartObj=JsonHelper.clone(rawCfg);
        JsonHelper.merge(chartObj.cfg, _chartsConfig,true);
    }
};

/**
 * Chart管理器
 */
function AppHubChartsMgr(){
	
	var charts=new Map();
	//build chart
    this.bulid = function(_chartsConfig){
    	
    	if (undefined==_chartsConfig||undefined==_chartsConfig["type"]) {
    		return;
    	}
    	
    	if (!charts.contain(_chartsConfig["id"])) {
    		var chart=new AppHubChart(_chartsConfig);
    		charts.put(_chartsConfig["id"],chart);
    	}
    	else {
    		var chart=charts.get(_chartsConfig["id"]);
    		chart.reload(_chartsConfig);
    	}
    };
    //run chart
    this.run=function(chartId,arrayData) {
    	if (charts.contain(chartId)) {
    		var chart=charts.get(chartId);
    		chart.run(arrayData);
    	}
    };
    //reset chart
    this.reset=function(chartId) {
    	if (charts.contain(chartId)) {
    		var chart=charts.get(chartId);
    		chart.destroy();
    	}
    };
    //destroy chart
    this.destroy=function(chartId) {
    	if (charts.contain(chartId)) {
    		var chart=charts.get(chartId);
    		chart.destroy();
    		charts.remove(chartId);
    	}
    };
    //resize chart
    this.resize=function(w,h,chartId) {
    	if (undefined==chartId) {
    		for(var i=0;i<charts.mapValues.count();i++) {
    			var chart=charts.mapValues.get(i);
    			chart.resize(w,h);
    		}
    	}
    	else {
    		if (charts.contain(chartId)) {
        		var chart=charts.get(chartId);
        		chart.resize(w,h);
        	}
    	}

    };
}
window["appcharts"]=new AppHubChartsMgr();