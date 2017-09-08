function getNowMs() {
	var s=new Date();
	var ms=s.getTime();
    return ms;
}

function getNowFormatDate() {
    var date = new Date();
    var seperator1 = "-";
    var seperator2 = ":";
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
            + " " + date.getHours() + seperator2 + date.getMinutes()
            + seperator2 + date.getSeconds();
    return currentdate;
}

function hashCode(str, m, n) {
	var result;
	var pre;
	var back;
	var currenttime = getNowFormatDate();
	var ms = getNowMs();
	str = str + currenttime;
    var h = 0;
    var len = str.length;
    var t = 2147483648;
    for (var i = 0; i < len; i++) {
        h = 31 * h + str.charCodeAt(i);
        if(h > 2147483647) h %= t;
    }
    var sh = "" + h;
    var sms = "" + ms;
    var str = ""; 
    if(sh.length >=m){
    	pre = m;
    	back = n-m;
    }else{
    	pre = sh.length;
    	back = n - sh.length;
    }
    var start = (sms.length-back)>0?(sms.length-back):sms.length;
    str = sh.substring(0,pre) + sms.substring(start,sms.length);
    //console.log("sh "+sh +" sms "+ sms+ "str "+str);
    return str;
}