/**
 * Collection集合类库
 */
/*********************Array Delete*************************/
Array.prototype.del = function(n) {

	if (n < 0) {
		return this;
	} else if (n >= 0 && n < this.length - 1) {
		return this.slice(0, n).concat(this.slice(n + 1, this.length));
	} else if (n == this.length - 1) {

		return this.slice(0, n);
	}
};
/***************************Queue*********************************/
function Queue(max) {
	this.items=new List(max);
	
	this.count=function() {
		return this.items.count();
	};
	
	this.add=function(obj) {
		this.items.add(obj);
	};
	
	this.remove=function(index) {
		this.items.remove(index);
	};
	
	this.get=function(index) {
		return this.items.get(index);		
	};
	
	this.pop=function() {
		var obj=this.items.get(0);
		this.items.remove(0);
		return obj;
	};
	
	this.popi=function(index) {
		var obj=this.items.get(index);
		this.items.remove(index);
		return obj;
	};
	
	this.rpop=function() {
		var len=this.items.count();
		var obj=this.items.get(len-1);
		this.items.remove(len-1);
		return obj;
	};
	
	this.reset=function() {
		var ls=this.items.toArray();
		this.items=new List();
		return ls;
	};
}

/** *******************Map************************ */
function Map(max) {
	this.mapNames = new List(max);
	this.mapValues = new List(max);

	this.put = function(name, value) {
		
		for ( var i = 0; i < this.mapNames.count(); i = i + 1) {
			if (name == this.mapNames.get(i)) {
				this.mapValues.set(i, value);
				return;
			}
		}
		
		this.mapNames.add(name);
		this.mapValues.add(value);

	};

	this.set = function(name, value) {		
		this.put(name, value);
	};

	this.contain = function(name) {
		for ( var i = 0; i < this.mapNames.count(); i = i + 1) {
			if (name == this.mapNames.get(i)) {
				return true;
			}
		}

		return false;
	};

	this.get = function(name) {
		for ( var i = 0; i < this.mapNames.count(); i = i + 1) {
			if (name == this.mapNames.get(i)) {

				return this.mapValues.get(i);
			}
		}
		return undefined;
	};

	this.getNames = function() {

		return this.mapNames;
	};

	this.clear = function() {

		this.mapNames.clear();
		this.mapValues.clear();

	};

	this.count = function() {

		return this.mapNames.count();
	};

	this.remove = function(name) {

		for ( var i = 0; i < this.mapNames.count(); i = i + 1) {
			if (name == this.mapNames.get(i)) {
				this.mapNames.remove(i);
				this.mapValues.remove(i);
			}
		}
	};
	
	this.toString=function() {
		var sb=new StringBuffer();
		sb.append("{");
		for ( var i = 0; i < this.mapNames.count(); i = i + 1) {
			
			var mapval=this.mapValues.get(i);
			
			if (mapval instanceof Map) {
				mapval=mapval.toString();
				sb.append("'"+this.mapNames.get(i)+"':"+mapval);
			}
			else if (mapval instanceof List) {
				mapval=COMMON.json.ArrayToString(mapval.toArray());
				sb.append("'"+this.mapNames.get(i)+"':"+mapval);
			}
			else if (mapval instanceof Array) {
				mapval=COMMON.json.ArrayToString(mapval);
				sb.append("'"+this.mapNames.get(i)+"':"+mapval);
			}
			else if (typeof(mapval)=="object") {
				mapval=COMMON.json.ObjectToString(mapval);
				sb.append("'"+this.mapNames.get(i)+"':"+mapval);
			}
			else {
				sb.append("'"+this.mapNames.get(i)+"':'"+mapval+"'");
			}		
			
			if (i<this.count()-1){
				sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	};
}

/** *******************List *********************** */
function List(max) {

	this.max=(max==undefined)?100000000:max;
	this.ls = new Array();
	this.num = 0;

	this.add = function(obj) {
		
		if (this.num+1>max) {
			this.remove(0);
		}
		
		this.ls[this.num] = obj;
		this.num = this.num + 1;
	};
	
	this.insert=function(index,obj) {
		
		if (this.num+1>max) {
			this.remove(0);
		}
		
		var newLs=[];
		
		for(var i=0;i<this.ls.length+1;i++) {
			
			if (i<index) {
				newLs[newLs.length]=this.ls[i];
			}
			else if (i==index) {
				newLs[newLs.length]=obj;
			}
			else if (i>index) {
				newLs[newLs.length]=this.ls[i-1];
			}
		}
		
		this.ls=newLs;
		this.num=newLs.length;
	};

	this.remove = function(index) {

		if (index < this.num && index >= 0) {
			if (index < this.num - 1) {
				for ( var i = index + 1; i < this.num; i = i + 1) {
					this.ls[i - 1] = this.ls[i];
				}
				this.ls[this.num - 1] = undefined;
				this.ls = this.ls.del(this.num - 1);
			} else {
				this.ls[index] = undefined;
				this.ls = this.ls.del(index);
			}
			this.num = this.num - 1;
		}

	};

	this.clear = function() {
		this.ls = undefined;
		this.ls = new Array();
		this.num = 0;
	};

	this.set = function(index, obj) {

		if (index < this.num && index >= 0) {
			this.ls[index] = obj;
		}

	};

	this.get = function(index) {
		if (index < this.num && index >= 0) {
			return this.ls[index];
		} else {

			return undefined;
		}
	};

	this.count = function() {
		return this.num;
	};

	this.join = function(spr) {
		return this.ls.join(spr);
	};
	
	this.addall=function(array) {
		for(var i=0;i<array.length;i++) {
			this.add(array[i]);
		}
	};
	
	this.toArray=function() {
		return this.ls;
	};
	
	this.sort=function(sortFunc) {
		if (sortFunc!=undefined) {
			this.ls.sort(sortFunc);
		}
		else {
			this.ls.sort();
		}
	};
	
	this.getLast=function() {
		if (this.ls.length>0) {
			return this.ls[this.ls.length-1];
		}
		
		return undefined;
	};
}


/** *******************Set (重复的数据不会添加，先入为主) *********************** */
function Set(max) {
	List.call(this);
	this.max = (max == undefined) ? 100000000 : max;
	this.exists = {};

	this.add = function(obj) {

		if (this.exists[obj] && this.exists[obj] == 1) {
			return;
		}
		
		this.ls[this.num] = obj;
		this.num = this.num + 1;
		this.exists[obj] = 1;

	};

	this.set = function(index, obj) {

		if (this.exists[obj] && this.exists[obj] == 1) {
			return;
		}

		if (index < this.num && index >= 0) {
			this.ls[index] = obj;
		}

	};
	
	this.clear = function() {
		this.ls = undefined;
		this.ls = new Array();
		this.num = 0;
		this.exists = {};
	};
	
	this.remove = function(index) {

		if (index < this.num && index >= 0) {
			var removeObj = this.ls[index];
			if (index < this.num - 1) {
				for ( var i = index + 1; i < this.num; i = i + 1) {
					this.ls[i - 1] = this.ls[i];
				}
				this.ls[this.num - 1] = undefined;
				this.ls = this.ls.del(this.num - 1);
			} else {
				this.ls[index] = undefined;
				this.ls = this.ls.del(index);
			}
			this.num = this.num - 1;

			this.exists[removeObj]=0;
		}

	};
}