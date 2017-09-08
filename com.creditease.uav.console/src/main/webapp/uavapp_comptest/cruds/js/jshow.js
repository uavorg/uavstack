
var cfj = {
		   "attribute"   :  {height:800,width:'800',frontSize:14,color:'black'},
		   "showLayerNum"   :   3,
		   "model1"      :	[   
						       {keyT:"{}",onClick:"clickRoot(this);"},   
						       {keyT:"<div class='layer0'>{@key}</div>",onClick:function(){alert("b");}},   
						       {keyT:"<div class='layer2'>{@key}</div>",onClick:"clickRoot(this);"},
						       {keyT:"<div class='layer3'>{@key}</div>",onClick:"clickRoot(this);"} 
					        ],
		   "model2"	     :	[   
					           {keyT:"<div class='root'>{@key}</div>",onClick:""},   
					           {keyT:"<div class='layer0'>{@key}</div>",onClick:""},   
					           {keyT:"<div class='layer1'>{@key}</div>",onClick:""}   
			           		]
					        
};

function clickRoot(o){
};

function mouseOver(o){
};

function mouseOut(o){
};

function Json2Html(){
	var jc = new AppHubJSONVisualizer(cfj);
	//var jsonstring={time:1463551379710,host:"201603310124-",ip:"127.0.0.1",svrid:"D:/UAVIDE/tomcat/apache-tomcat-7.0.59::D:/UAVIDE/tomcat/apache-tomcat-7.0.59",tag:"P",frames:{"manager":[{"Instances":[{"id":"lib","values":{}}],"PEId":"jars"},{"Instances":[{"id":"javax.servlet.annotation.WebServlet","values":{"org.apache.catalina.manager.HTMLManagerServlet":{"des":{"name":"HTMLManager","urlPatterns":["/html/*"]}},"org.apache.catalina.manager.ManagerServlet":{"des":{"name":"Manager","urlPatterns":["/text/*"]}},"org.apache.catalina.manager.JMXProxyServlet":{"des":{"name":"JMXProxy","urlPatterns":["/jmxproxy/*"]}},"org.apache.catalina.manager.StatusManagerServlet":{"des":{"name":"Status","urlPatterns":["/status/*"]}}}},{"id":"javax.servlet.annotation.WebFilter","values":{"org.apache.catalina.filters.SetCharacterEncodingFilter":{"des":{"servletNames":[],"filterName":"SetCharacterEncoding","urlPatterns":["/*"]}},"org.apache.catalina.filters.CsrfPreventionFilter":{"des":{"servletNames":["HTMLManager","jsp"],"filterName":"CSRF","urlPatterns":[]}}}},{"id":"javax.servlet.annotation.WebListener","values":{}},{"id":"javax.jws.WebService","values":{}},{"id":"javax.xml.ws.WebServiceProvider","values":{}},{"id":"javax.ws.rs.Path","values":{}},{"id":"org.springframework.stereotype.Controller","values":{}},{"id":"webapp","values":{"appname":"Tomcat Manager Application","webapproot":"D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/manager","appurl":"http://127.0.0.1:8080/manager/"}}],"PEId":"cpt"},{"Instances":[],"PEId":"logs"}],"com.creditease.uav.console":[{"Instances":[{"id":"lib","values":{"javax.ws.rs-api-2.0.1.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/javax.ws.rs-api-2.0.1.jar","jersey-client-2.22.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/jersey-client-2.22.jar","hk2-locator-2.4.0-b31.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/hk2-locator-2.4.0-b31.jar","jersey-server-2.22.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/jersey-server-2.22.jar","javassist-3.18.1-GA.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/javassist-3.18.1-GA.jar","commons-codec-1.9.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/commons-codec-1.9.jar","com.creditease.uav.cache.redis-1.0.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/com.creditease.uav.cache.redis-1.0.jar","javax.annotation-api-1.2.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/javax.annotation-api-1.2.jar","commons-logging-1.2.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/commons-logging-1.2.jar","com.creditease.uav.base-1.0.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/com.creditease.uav.base-1.0.jar","httpasyncclient-4.1.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/httpasyncclient-4.1.jar","httpcore-4.4.1.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/httpcore-4.4.1.jar","classes":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/classes/","jersey-guava-2.22.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/jersey-guava-2.22.jar","jersey-container-servlet-2.22.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/jersey-container-servlet-2.22.jar","osgi-resource-locator-1.0.1.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/osgi-resource-locator-1.0.1.jar","hk2-api-2.4.0-b31.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/hk2-api-2.4.0-b31.jar","javax.inject-2.4.0-b31.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/javax.inject-2.4.0-b31.jar","aredis-api-1.4.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/aredis-api-1.4.jar","httpclient-4.4.1.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/httpclient-4.4.1.jar","aopalliance-repackaged-2.4.0-b31.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/aopalliance-repackaged-2.4.0-b31.jar","validation-api-1.1.0.Final.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/validation-api-1.1.0.Final.jar","httpcore-nio-4.4.1.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/httpcore-nio-4.4.1.jar","com.creditease.uav.helper-1.0.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/com.creditease.uav.helper-1.0.jar","jersey-container-servlet-core-2.22.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/jersey-container-servlet-core-2.22.jar","com.creditease.uav.fastjson-1.2.6.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/com.creditease.uav.fastjson-1.2.6.jar","jersey-media-jaxb-2.22.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/jersey-media-jaxb-2.22.jar","hk2-utils-2.4.0-b31.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/hk2-utils-2.4.0-b31.jar","com.creditease.uav.httpasync-1.0.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/com.creditease.uav.httpasync-1.0.jar","com.creditease.uav.logging-1.0.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/com.creditease.uav.logging-1.0.jar","jersey-common-2.22.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console/WEB-INF/lib/jersey-common-2.22.jar"}}],"PEId":"jars"},{"Instances":[{"id":"javax.servlet.annotation.WebServlet","values":{"com.creditease.uav.apphub.core.AppHubRestServlet":{"des":{"loadOnStartup":"1","name":"jersey-serlvet","asyncSupported":"true","urlPatterns":["/rs/*"]}}}},{"id":"javax.servlet.annotation.WebFilter","values":{}},{"id":"javax.servlet.annotation.WebListener","values":{}},{"id":"javax.jws.WebService","values":{}},{"id":"javax.xml.ws.WebServiceProvider","values":{}},{"id":"javax.ws.rs.Path","values":{"com.creditease.uav.godeye.rest.GodEyeRestService":{"methods":{"loadAppProfileList":{"anno":{"javax.ws.rs.Path":{"value":"loadAppProfileList"},"javax.ws.rs.GET":{},"javax.ws.rs.Produces":{"value":["application/json;charset=utf-8"]}},"para":["R:java.lang.String"]},"loadUavNetworkInfo":{"anno":{"javax.ws.rs.Path":{"value":"loadUavNetworkInfo"},"javax.ws.rs.GET":{},"javax.ws.rs.Produces":{"value":["application/json;charset=utf-8"]}},"para":["R:java.lang.String"]},"ping":{"anno":{"javax.ws.rs.GET":{}},"para":["R:java.lang.String"]}},"anno":{"javax.ws.rs.Path":{"value":"godeye"}}},"com.creditease.uav.manage.rest.ManageRestService":{"methods":{"addGroup":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"addGroup"}},"para":["com.creditease.uav.manage.rest.entity.GroupEntity","javax.ws.rs.container.AsyncResponse","R:void"]},"loadApps":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"loadApps"}},"para":["com.creditease.uav.manage.rest.entity.AppEntity","javax.ws.rs.container.AsyncResponse","R:void"]},"loadAllGroups":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"loadAllGroups"}},"para":["com.creditease.uav.manage.rest.entity.GroupEntity","javax.ws.rs.container.AsyncResponse","R:void"]},"loadAppByid":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"loadAppByid"}},"para":["com.creditease.uav.manage.rest.entity.AppEntity","javax.ws.rs.container.AsyncResponse","R:void"]},"loadAllApps":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"loadAllApps"}},"para":["javax.ws.rs.container.AsyncResponse","R:void"]},"addApp":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"addApp"}},"para":["com.creditease.uav.manage.rest.entity.AppEntity","javax.ws.rs.container.AsyncResponse","R:void"]},"delGroup":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"delGroup"}},"para":["com.creditease.uav.manage.rest.entity.GroupEntity","javax.ws.rs.container.AsyncResponse","R:void"]},"updateGroup":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"updateGroup"}},"para":["com.creditease.uav.manage.rest.entity.GroupEntity","javax.ws.rs.container.AsyncResponse","R:void"]},"delApp":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"delApp"}},"para":["com.creditease.uav.manage.rest.entity.AppEntity","javax.ws.rs.container.AsyncResponse","R:void"]},"updateApp":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"updateApp"}},"para":["com.creditease.uav.manage.rest.entity.AppEntity","javax.ws.rs.container.AsyncResponse","R:void"]},"loadGroupByid":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"loadGroupByid"}},"para":["com.creditease.uav.manage.rest.entity.GroupEntity","javax.ws.rs.container.AsyncResponse","R:void"]},"ping":{"anno":{"javax.ws.rs.Path":{"value":"ping"},"javax.ws.rs.GET":{}},"para":["R:java.lang.String"]}},"anno":{"javax.ws.rs.Path":{"value":"manage"}}},"com.creditease.uav.apphub.rest.GUIService":{"methods":{"jumpMainPage":{"anno":{"javax.ws.rs.Consumes":{"value":["application/x-www-form-urlencoded"]},"javax.ws.rs.Path":{"value":"jumpMainPage"},"javax.ws.rs.GET":{}},"para":["R:void"]},"loadAppMenu":{"anno":{"javax.ws.rs.Path":{"value":"loadAppMenu"},"javax.ws.rs.GET":{},"javax.ws.rs.Produces":{"value":["application/json;charset=utf-8"]}},"para":["R:java.lang.String"]},"loadMainMenu":{"anno":{"javax.ws.rs.Path":{"value":"loadMainMenu"},"javax.ws.rs.GET":{},"javax.ws.rs.Produces":{"value":["application/json;charset=utf-8"]}},"para":["R:java.lang.String"]},"loadMainAppIco":{"anno":{"javax.ws.rs.Path":{"value":"loadMainAppIco"},"javax.ws.rs.GET":{},"javax.ws.rs.Produces":{"value":["application/json;charset=utf-8"]}},"para":["javax.ws.rs.container.AsyncResponse","R:void"]},"loadUserInfo":{"anno":{"javax.ws.rs.Path":{"value":"loadUserInfo"},"javax.ws.rs.GET":{},"javax.ws.rs.Produces":{"value":["application/json;charset=utf-8"]}},"para":["R:java.lang.String"]},"login":{"anno":{"javax.ws.rs.POST":{},"javax.ws.rs.Path":{"value":"login"},"javax.ws.rs.Produces":{"value":["text/html;charset=utf-8"]}},"para":["java.lang.String","R:java.lang.String"]},"ping":{"anno":{"javax.ws.rs.GET":{}},"para":["R:java.lang.String"]},"loadAppTemp":{"anno":{"javax.ws.rs.Path":{"value":"loadAppTemp"},"javax.ws.rs.GET":{},"javax.ws.rs.Produces":{"value":["application/json;charset=utf-8"]}},"para":["R:java.lang.String"]},"jumpAppPage":{"anno":{"javax.ws.rs.Consumes":{"value":["application/x-www-form-urlencoded"]},"javax.ws.rs.Path":{"value":"jumpAppPage"},"javax.ws.rs.GET":{}},"para":["java.lang.String","R:void"]},"loadTemp":{"anno":{"javax.ws.rs.Path":{"value":"loadTemp"},"javax.ws.rs.GET":{},"javax.ws.rs.Produces":{"value":["application/json;charset=utf-8"]}},"para":["java.lang.String","R:java.lang.String"]}},"anno":{"javax.ws.rs.Path":{"value":"gui"}}},"com.creditease.uav.apphub.rest.APIService":{"methods":{"ping":{"anno":{"javax.ws.rs.GET":{}},"para":["R:java.lang.String"]}},"anno":{"javax.ws.rs.Path":{"value":"api"}}},"com.creditease.uav.appmongo.rest.MongodbServcie":{"methods":{"loadMongoCluster":{"anno":{"javax.ws.rs.Path":{"value":"loadMongoCluster"},"javax.ws.rs.GET":{},"javax.ws.rs.Produces":{"value":["application/json;charset=utf-8"]}},"para":["R:java.lang.String"]},"ping":{"anno":{"javax.ws.rs.GET":{}},"para":["R:java.lang.String"]},"executeMongoCmd":{"anno":{"javax.ws.rs.Path":{"value":"executeMongoCmd"},"javax.ws.rs.GET":{},"javax.ws.rs.Produces":{"value":["application/json;charset=utf-8"]}},"para":["R:java.lang.String"]}},"anno":{"javax.ws.rs.Path":{"value":"mongo"}}}}},{"id":"org.springframework.stereotype.Controller","values":{}},{"id":"webapp","values":{"appname":"UAV.AppHub","webapproot":"D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/com.creditease.uav.console","appurl":"http://127.0.0.1:8080/com.creditease.uav.console/"}}],"PEId":"cpt"},{"Instances":[],"PEId":"logs"}],"docs":[{"Instances":[{"id":"lib","values":{}}],"PEId":"jars"},{"Instances":[{"id":"javax.servlet.annotation.WebServlet","values":{}},{"id":"javax.servlet.annotation.WebFilter","values":{}},{"id":"javax.servlet.annotation.WebListener","values":{}},{"id":"javax.jws.WebService","values":{}},{"id":"javax.xml.ws.WebServiceProvider","values":{}},{"id":"javax.ws.rs.Path","values":{}},{"id":"org.springframework.stereotype.Controller","values":{}},{"id":"webapp","values":{"appname":"Tomcat Documentation","webapproot":"D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/docs","appurl":"http://127.0.0.1:8080/docs/"}}],"PEId":"cpt"},{"Instances":[],"PEId":"logs"}],"ROOT":[{"Instances":[{"id":"lib","values":{}}],"PEId":"jars"},{"Instances":[{"id":"javax.servlet.annotation.WebServlet","values":{}},{"id":"javax.servlet.annotation.WebFilter","values":{}},{"id":"javax.servlet.annotation.WebListener","values":{}},{"id":"javax.jws.WebService","values":{}},{"id":"javax.xml.ws.WebServiceProvider","values":{}},{"id":"javax.ws.rs.Path","values":{}},{"id":"org.springframework.stereotype.Controller","values":{}},{"id":"webapp","values":{"appname":"Welcome to Tomcat","webapproot":"D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/ROOT","appurl":"http://127.0.0.1:8080/"}}],"PEId":"cpt"},{"Instances":[],"PEId":"logs"}],"examples":[{"Instances":[{"id":"lib","values":{"classes":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/examples/WEB-INF/classes/","standard.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/examples/WEB-INF/lib/standard.jar","jstl.jar":"file:/D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/examples/WEB-INF/lib/jstl.jar"}}],"PEId":"jars"},{"Instances":[{"id":"javax.servlet.annotation.WebServlet","values":{"RequestInfoExample":{"des":{"name":"RequestInfoExample","urlPatterns":["/servlets/servlet/RequestInfoExample/*"]}},"CookieExample":{"des":{"name":"CookieExample","urlPatterns":["/servlets/servlet/CookieExample"]}},"ServletToJsp":{"des":{"name":"ServletToJsp","urlPatterns":["/servletToJsp"]}},"websocket.tc7.chat.ChatWebSocketServlet":{"des":{"name":"wsChat","urlPatterns":["/websocket/tc7/chat"]}},"compressionFilters.CompressionFilterTestServlet":{"des":{"name":"CompressionFilterTestServlet","urlPatterns":["/CompressionTest"]}},"websocket.tc7.snake.SnakeWebSocketServlet":{"des":{"name":"wsSnake","urlPatterns":["/websocket/tc7/snake"]}},"websocket.tc7.echo.EchoMessage":{"des":{"name":"wsEchoMessage","urlPatterns":["/websocket/tc7/echoMessage"]}},"HelloWorldExample":{"des":{"name":"HelloWorldExample","urlPatterns":["/servlets/servlet/HelloWorldExample"]}},"websocket.tc7.echo.EchoStream":{"des":{"name":"wsEchoStream","urlPatterns":["/websocket/tc7/echoStream"]}},"RequestHeaderExample":{"des":{"name":"RequestHeaderExample","urlPatterns":["/servlets/servlet/RequestHeaderExample"]}},"SessionExample":{"des":{"name":"SessionExample","urlPatterns":["/servlets/servlet/SessionExample"]}},"async.AsyncStockServlet":{"des":{"name":"stock","asyncSupported":"true","urlPatterns":["/async/stockticker"]}},"async.Async2":{"des":{"name":"async2","asyncSupported":"true","urlPatterns":["/async/async2"]}},"async.Async3":{"des":{"name":"async3","asyncSupported":"true","urlPatterns":["/async/async3"]}},"RequestParamExample":{"des":{"name":"RequestParamExample","urlPatterns":["/servlets/servlet/RequestParamExample"]}},"async.Async0":{"des":{"name":"async0","asyncSupported":"true","urlPatterns":["/async/async0"]}},"async.Async1":{"des":{"name":"async1","asyncSupported":"true","urlPatterns":["/async/async1"]}},"chat.ChatServlet":{"des":{"name":"ChatServlet","urlPatterns":["/servlets/chat/chat"]}}}},{"id":"javax.servlet.annotation.WebFilter","values":{"org.apache.catalina.filters.RequestDumperFilter":{"des":{"servletNames":[],"filterName":"Request Dumper Filter","urlPatterns":[]}},"org.apache.catalina.filters.SetCharacterEncodingFilter":{"des":{"servletNames":[],"filterName":"Set Character Encoding","urlPatterns":[]}},"filters.ExampleFilter":{"des":{"servletNames":[],"filterName":"Timing filter","urlPatterns":[]}},"compressionFilters.CompressionFilter":{"des":{"servletNames":[],"filterName":"Compression Filter","urlPatterns":[]}}}},{"id":"javax.servlet.annotation.WebListener","values":{"websocket.drawboard.DrawboardContextListener":{"des":{}},"listeners.SessionListener":{"des":{}},"listeners.ContextListener":{"des":{}}}},{"id":"javax.jws.WebService","values":{}},{"id":"javax.xml.ws.WebServiceProvider","values":{}},{"id":"javax.ws.rs.Path","values":{}},{"id":"org.springframework.stereotype.Controller","values":{}},{"id":"webapp","values":{"appname":"Servlet and JSP Examples","webapproot":"D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/examples","appurl":"http://127.0.0.1:8080/examples/"}}],"PEId":"cpt"},{"Instances":[],"PEId":"logs"}],"host-manager":[{"Instances":[{"id":"lib","values":{}}],"PEId":"jars"},{"Instances":[{"id":"javax.servlet.annotation.WebServlet","values":{"org.apache.catalina.manager.host.HTMLHostManagerServlet":{"des":{"name":"HTMLHostManager","urlPatterns":["/html/*"]}},"org.apache.catalina.manager.host.HostManagerServlet":{"des":{"name":"HostManager","urlPatterns":["/text/*"]}}}},{"id":"javax.servlet.annotation.WebFilter","values":{"org.apache.catalina.filters.SetCharacterEncodingFilter":{"des":{"servletNames":[],"filterName":"SetCharacterEncoding","urlPatterns":["/*"]}},"org.apache.catalina.filters.CsrfPreventionFilter":{"des":{"servletNames":["HTMLHostManager"],"filterName":"CSRF","urlPatterns":[]}}}},{"id":"javax.servlet.annotation.WebListener","values":{}},{"id":"javax.jws.WebService","values":{}},{"id":"javax.xml.ws.WebServiceProvider","values":{}},{"id":"javax.ws.rs.Path","values":{}},{"id":"org.springframework.stereotype.Controller","values":{}},{"id":"webapp","values":{"appname":"Tomcat Host Manager Application","webapproot":"D:/UAVIDE/tomcat/apache-tomcat-7.0.59/webapps/host-manager","appurl":"http://127.0.0.1:8080/host-manager/"}}],"PEId":"cpt"},{"Instances":[],"PEId":"logs"}]}};	
	//var jsonstring = {a:1};
	var jsonstring={
		    "time": 1463816026510,
		    "host": "09-201506110096",
		    "ip": "127.0.0.1",
		    "svrid": "E:/UAVIDE/tomcat/apache-tomcat-7.0.65---E:/UAVIDE/defaultworkspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0",
		    "tag": "M",
		    "frames": {
		        "cxf": [
		            {
		                "MEId": "cxfE2E",
		                "Instances": []
		            },
		            {
		                "MEId": "cxfSR",
		                "Instances": []
		            }
		        ],
		        "server": [
		            {
		                "MEId": "urlResp",
		                "Instances": [
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/rs/manage/ping",
		                        "values": {
		                            "tmax": 492,
		                            "tsum": 513,
		                            "count": 9,
		                            "RC200": 9,
		                            "tavg": 57,
		                            "tmin": 2
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/rs/godeye/profile/q/cache",
		                        "values": {
		                            "tmax": 6,
		                            "tsum": 15,
		                            "count": 3,
		                            "RC200": 3,
		                            "tavg": 5,
		                            "tmin": 4
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/uavapp_godeye/appmonitor/js/uav.appmonitor.js",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 49,
		                            "tsum": 49,
		                            "count": 1,
		                            "tavg": 49,
		                            "RC304": 1,
		                            "tmin": 49
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/appvendors/jquery/jquery.pjax.min.js",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 2,
		                            "tsum": 2,
		                            "count": 1,
		                            "tavg": 2,
		                            "RC304": 1,
		                            "tmin": 2
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/apphub/js/app.loadMenu.js",
		                        "values": {
		                            "tmax": 2,
		                            "tsum": 2,
		                            "count": 1,
		                            "RC200": 1,
		                            "tavg": 2,
		                            "tmin": 2
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/appvendors/jquery/jquery.pjax.js",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 3,
		                            "tsum": 3,
		                            "count": 1,
		                            "tavg": 3,
		                            "RC304": 1,
		                            "tmin": 3
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/uavapp_godeye/uavnetwork/main.html",
		                        "values": {
		                            "tmax": 57,
		                            "tsum": 61,
		                            "count": 4,
		                            "RC200": 4,
		                            "tavg": 15,
		                            "tmin": 1
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/apphub/js/main.loadMenu.js",
		                        "values": {
		                            "tmax": 2,
		                            "tsum": 13,
		                            "count": 10,
		                            "RC200": 10,
		                            "tavg": 1,
		                            "tmin": 1
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/main.html",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 83,
		                            "tsum": 83,
		                            "count": 1,
		                            "tavg": 83,
		                            "RC304": 1,
		                            "tmin": 83
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/rs/gui/loadTemp",
		                        "values": {
		                            "tmax": 385,
		                            "tsum": 432,
		                            "count": 13,
		                            "RC200": 13,
		                            "tavg": 33,
		                            "tmin": 3
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/rs/gui/login",
		                        "values": {
		                            "tmax": 32,
		                            "tsum": 36,
		                            "count": 2,
		                            "RC200": 2,
		                            "tavg": 18,
		                            "tmin": 4
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/appvendors/bootstrap/css/bootstrap.min.css",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 23,
		                            "tsum": 23,
		                            "count": 1,
		                            "tavg": 23,
		                            "RC304": 1,
		                            "tmin": 23
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/rs/gui/jumpAppPage",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 4,
		                            "tsum": 4,
		                            "count": 1,
		                            "tavg": 4,
		                            "RC302": 1,
		                            "tmin": 4
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/apphub/css/login.css",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 43,
		                            "tsum": 43,
		                            "count": 1,
		                            "tavg": 43,
		                            "RC304": 1,
		                            "tmin": 43
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/appvendors/bootstrap/js/bootstrap.min.js",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 4,
		                            "tsum": 4,
		                            "count": 1,
		                            "tavg": 4,
		                            "RC304": 1,
		                            "tmin": 4
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/apphub/js/login.init.js",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 84,
		                            "tsum": 84,
		                            "count": 1,
		                            "tavg": 84,
		                            "RC304": 1,
		                            "tmin": 84
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/uavapp_manage/config.properties",
		                        "values": {
		                            "tmax": 24,
		                            "tsum": 26,
		                            "count": 2,
		                            "RC200": 2,
		                            "tavg": 13,
		                            "tmin": 2
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/rs/gui/loadMainMenu",
		                        "values": {
		                            "tmax": 5,
		                            "tsum": 34,
		                            "count": 10,
		                            "RC200": 10,
		                            "tavg": 3,
		                            "tmin": 2
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/apphub/js/login.login.js",
		                        "values": {
		                            "tmax": 2,
		                            "tsum": 3,
		                            "count": 2,
		                            "RC200": 2,
		                            "tavg": 1,
		                            "tmin": 1
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/apphub/js/apphub.js",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 1,
		                            "tsum": 1,
		                            "count": 1,
		                            "tavg": 1,
		                            "RC304": 1,
		                            "tmin": 1
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/apphub/js/uav.restful.client.js",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 5,
		                            "tsum": 5,
		                            "count": 1,
		                            "tavg": 5,
		                            "RC304": 1,
		                            "tmin": 5
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/uavapp_godeye/config.properties",
		                        "values": {
		                            "tmax": 2,
		                            "tsum": 4,
		                            "count": 3,
		                            "RC200": 3,
		                            "tavg": 1,
		                            "tmin": 1
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/uavapp_comptest/config.properties",
		                        "values": {
		                            "tmax": 3,
		                            "tsum": 5,
		                            "count": 2,
		                            "RC200": 2,
		                            "tavg": 2,
		                            "tmin": 2
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/apphub/js/main.loadAppIco.js",
		                        "values": {
		                            "tmax": 2,
		                            "tsum": 11,
		                            "count": 10,
		                            "RC200": 10,
		                            "tavg": 1,
		                            "tmin": 1
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/rs/godeye/node/q/cache",
		                        "values": {
		                            "tmax": 35,
		                            "tsum": 90,
		                            "count": 11,
		                            "RC200": 11,
		                            "tavg": 8,
		                            "tmin": 4
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/",
		                        "values": {
		                            "tmax": 1,
		                            "tsum": 1,
		                            "count": 1,
		                            "RC200": 1,
		                            "tavg": 1,
		                            "tmin": 1
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/",
		                        "values": {
		                            "tmax": 65,
		                            "tsum": 65,
		                            "count": 1,
		                            "tavg": 65,
		                            "err": 1,
		                            "tmin": 65,
		                            "RC404": 1
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/rs/gui/jumpMainPage",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 5,
		                            "tsum": 5,
		                            "count": 1,
		                            "tavg": 5,
		                            "RC302": 1,
		                            "tmin": 5
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/uavapp_main/appIco.png",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 103,
		                            "tsum": 103,
		                            "count": 1,
		                            "tavg": 103,
		                            "RC304": 1,
		                            "tmin": 103
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/appvendors/bootstrap/css/font-awesome.min.css",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 5,
		                            "tsum": 5,
		                            "count": 1,
		                            "tavg": 5,
		                            "RC304": 1,
		                            "tmin": 5
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/appvendors/jquery/jquery-1.11.3.min.js",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 4,
		                            "tsum": 4,
		                            "count": 1,
		                            "tavg": 4,
		                            "RC304": 1,
		                            "tmin": 4
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/rs/gui/loadMainAppIco",
		                        "values": {
		                            "tmax": 9,
		                            "tsum": 34,
		                            "count": 10,
		                            "RC200": 10,
		                            "tavg": 3,
		                            "tmin": 2
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/rs/gui/loadAppMenu",
		                        "values": {
		                            "tmax": 9,
		                            "tsum": 9,
		                            "count": 1,
		                            "RC200": 1,
		                            "tavg": 9,
		                            "tmin": 9
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/appvendors/bootstrap/fonts/glyphicons-halflings-regular.woff2",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 62,
		                            "tsum": 62,
		                            "count": 1,
		                            "tavg": 62,
		                            "RC304": 1,
		                            "tmin": 62
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/apphub/js/common/helper.js",
		                        "values": {
		                            "warn": 1,
		                            "tmax": 5,
		                            "tsum": 5,
		                            "count": 1,
		                            "tavg": 5,
		                            "RC304": 1,
		                            "tmin": 5
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080/com.creditease.uav.console/uavapp_godeye/appmonitor/main.html",
		                        "values": {
		                            "tmax": 25,
		                            "tsum": 28,
		                            "count": 3,
		                            "RC200": 3,
		                            "tavg": 9,
		                            "tmin": 1
		                        }
		                    }
		                ]
		            },
		            {
		                "MEId": "appResp",
		                "Instances": [
		                    {
		                        "id": "http://127.0.0.1:8080/@ROOT",
		                        "values": {
		                            "tmax": 66,
		                            "tsum": 66,
		                            "count": 1,
		                            "tavg": 66,
		                            "err": 1,
		                            "tmin": 66,
		                            "RC404": 1
		                        }
		                    },
		                    {
		                        "id": "http://127.0.0.1:8080//com.creditease.uav.console@com.creditease.uav.console",
		                        "values": {
		                            "warn": 17,
		                            "tmax": 492,
		                            "tsum": 1834,
		                            "count": 114,
		                            "RC200": 97,
		                            "tavg": 16,
		                            "RC302": 2,
		                            "RC304": 15,
		                            "tmin": 1
		                        }
		                    }
		                ]
		            },
		            {
		                "MEId": "serverResp",
		                "Instances": [
		                    {
		                        "id": "http://127.0.0.1:8080",
		                        "values": {
		                            "warn": 17,
		                            "port": 8080,
		                            "tmax": 493,
		                            "tsum": 1945,
		                            "count": 115,
		                            "RC200": 97,
		                            "tavg": 16,
		                            "err": 1,
		                            "RC302": 2,
		                            "RC304": 15,
		                            "tmin": 1,
		                            "RC404": 1
		                        }
		                    }
		                ]
		            },
		            {
		                "MEId": "jvm",
		                "Instances": [
		                    {
		                        "id": "http://127.0.0.1:8080",
		                        "values": {
		                            "perm_init": 22020096,
		                            "thread_peak": 61,
		                            "thread_daemon": 36,
		                            "code_max": 50331648,
		                            "code_init": 2555904,
		                            "class_load": 14496,
		                            "heap_use": 334627928,
		                            "perm_use": 73340272,
		                            "class_unload": 0,
		                            "old_init": 85983232,
		                            "cpu_s": 26.2,
		                            "surv_max": 86507520,
		                            "surv_init": 5242880,
		                            "mgc_count": 14,
		                            "fgc_time": 465,
		                            "cpu_p": 0.1,
		                            "old_max": 1382547456,
		                            "eden_max": 521666560,
		                            "heap_commit": 827326464,
		                            "code_use": 4192704,
		                            "surv_commit": 86507520,
		                            "perm_max": 85983232,
		                            "thread_started": 163,
		                            "perm_commit": 85983232,
		                            "fgc_count": 2,
		                            "thread_live": 50,
		                            "eden_commit": 521666560,
		                            "mgc_time": 444,
		                            "eden_use": 170548728,
		                            "heap_max": 1843920896,
		                            "old_use": 123264184,
		                            "code_commit": 4325376,
		                            "surv_use": 40815016,
		                            "class_total": 14496,
		                            "heap_init": 129625536,
		                            "eden_init": 33030144,
		                            "old_commit": 219152384
		                        }
		                    }
		                ]
		            }
		        ]
		    }
		};
	var re = jc.asHtml("model1",jsonstring);
	
	var container = document.getElementById("JsonShowDiv");
	container.innerHTML += re;
};

$("document").ready(function(){
	Json2Html();
});
	
	
	
