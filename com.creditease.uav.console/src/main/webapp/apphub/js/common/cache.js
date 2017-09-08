/**
 * AppHub Client Cache 客户端缓存控制
 */
function AppHubClientCache() {
	
	var cfg={
			type:"local" //local is the LocalStorage / session is the SessionStorage
		};
	
	var cache;
	
	/**
	 * init
	 */
	this.init=function(_cfg) {
		try{
			JsonHelper.merge(cfg, _cfg, true);
			
			cache=(cfg.type=="local")?window.localStorage:window.sessionStorage;
		}catch(e){
			console.log(e);
		}
	};
	
	/**
	 * put cache
	 */
	this.put=function(key,value) {
		
		if (undefined==key||undefined==value) {
			return;
		}

		try{
			cache.setItem(key, value);
		}catch(e){
			console.log(e);
		}
	};
	
	/**
	 * get cache
	 */
	this.get=function(key) {
		
		if (undefined==key) {
			return undefined;
		}
		
		var obj;
		try{
			obj = cache.getItem(key);	
		}catch(e){
			console.log(e);
		}
	
		return 	obj;
	};
	
	/**
	 * clear cache
	 */
	this.clear=function() {
		try{
		 cache.clear();
		}catch(e){
			console.log(e);
		}
	
	};
}

window["cachemgr"]=new AppHubClientCache();