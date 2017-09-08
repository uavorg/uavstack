var chartsConfig = {
    id:"testChartPie",
    type:"pie",
    cid:"chartsContainer",
    title:"test pie title",
    width:"auto",
    height:500,
    series:[
        {
            name:"Firefox",
            color:"#EEEE00",
            data:[]
        },
        {
            name:"IE",
            color:"#EE2200",
            data:[]
        },
        {
            name:"Chrome",
            color:"#563624",
            data:[]
        },
        {
            name:"Safari",
            color:"blue",
            data:[]
        }
    ]
};

window["appcharts"].bulid(chartsConfig);

$(function(){
    window["appcharts"].run("testChartPie",[30.2,12,50.8,8.5]);
});