/**
 * String类库
 */
/** *******************StringBuffer *********************** */
function StringBuffer() {

	var ls = new Array();
	
	this.count=function() {
		return ls.length;
	};

	this.append = function(str) {
		ls[ls.length] = str;
		
		return this;
	};
	
	this.setbuffer=function(index,str) {
		if (index<ls.length) {
			ls[index]=str;
		}
	};
	
	this.getbuffer=function(index) {
		if (index<ls.length) {
			return ls[index];
		}
		return "";
	};

	this.toString = function() {
		return ls.join("");
	};	
}