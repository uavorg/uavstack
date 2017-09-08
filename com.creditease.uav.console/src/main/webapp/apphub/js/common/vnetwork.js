/**
 * vnetwork.js is based on vis.js to build nodes & edges, the node has state &
 * workstate, the edge has workstate it is a powerful tool to build the visual
 * graph
 */
function AppNodes(_config) {

	var config = _config;
	var _visNodes = new vis.DataSet();
	
	function update(newNode,nd,typeInfo) {
		
		if(nd["workpower"]!=undefined) {
			var state = "l"+parseInt(nd["workpower"]);
							
			newNode["borderWidth"]=typeInfo["border"][state];
			newNode["borderWidthSelected"]=typeInfo["border"][state] + 2;				
		}
		else {
			newNode["borderWidth"]=typeInfo["border"]["l1"];
			newNode["borderWidthSelected"]=typeInfo["border"]["l1"] + 2;
		}

		if (nd["level"] != undefined) {
			newNode["level"] = nd["level"];
		}

		if (nd["tip"] != undefined) {
			newNode["title"] = nd["tip"];
		}

		if (nd["state"] != undefined) {
			var state = parseInt(nd["state"]);

			var stateKey;
			switch (state) {
			case 1:
				stateKey = "alive";
				break;
			case 0:
				stateKey = "dying";
				break;
			case -1:
				stateKey = "dead";
				break;
			}

			newNode["color"] = {
				background : typeInfo[stateKey],
				highlight : {
					background : typeInfo[stateKey]
				}
			};
		}

		if (nd["workstate"] != undefined) {

			var state = parseInt(nd["workstate"]);

			var stateKey;
			switch (state) {
			case 1:
				stateKey = "good";
				break;
			case 0:
				stateKey = "warn";
				break;
			case -1:
				stateKey = "err";
				break;
			case -2:
				stateKey = "dead";
				break;
			}

			if (newNode["color"] != undefined) {
				newNode["color"]["border"] = typeInfo["work"][stateKey];
				newNode["color"]["highlight"]["border"] = typeInfo["work"][stateKey];
			} else {
				newNode["color"] = {
					border : typeInfo["work"][stateKey],
					highlight : {
						border : typeInfo["work"][stateKey]
					}
				};
			}
			
		}
		
		if (nd["extattrs"]!=undefined) {			
			newNode["extattrs"]=nd["extattrs"];			
		}
	}

	// [{id:,level:,label:,itype}]
	this.addNodes = function(nodes) {

		for (var i = 0; i < nodes.length; i++) {

			var nd = nodes[i];
			var itype = nd["itype"];
			var typeInfo = config[itype];

			if (typeInfo == undefined) {
				continue;
			}
			
			//if the node exists, we should not re-add it and we just update it
			if (_visNodes.get(nd["id"])!=undefined) {
				this.updateNode(nd);
				continue;
			}

			var newNode = {
				id : nd["id"],
				label : nd["label"],
				font : typeInfo["font"],
				shape : typeInfo["shape"],
				itype:itype,
				color : {
					background : typeInfo["alive"],
					border : typeInfo["work"]["good"],
					highlight : {
						background : typeInfo["alive"],
						border : typeInfo["work"]["good"]
					}
				}
			};
			
			update(newNode,nd,typeInfo);
			
			_visNodes.add([newNode]);
		}

	};
	
	//update node
	this.updateNode=function(nd) {
		
		var typeInfo = config[nd["itype"]];

		if (typeInfo == undefined) {
			return;
		}
		
		var newNode={
			id:nd["id"]
		};
		
		update(newNode,nd,typeInfo);
		
		_visNodes.update([newNode]);
	};
	
	//remove node
	this.removeNode=function(id) {
	
		_visNodes.remove(id);
	};

	// update node tip
	this.updateNodeTip = function(_id, _tip) {

		_visNodes.update([ {
			id : _id,
			title : _tip
		} ]);
	};
	
	// update node label
	this.updateNodeLabel=function(_id,_label) {
		_visNodes.update([ {
			id : _id,
			label : _label
		} ]);
	};

	// update node state
	this.updateNodeState = function(_id, _type, state) {

		var typeInfo = config[_type];

		if (typeInfo == undefined) {
			return;
		}

		state = parseInt(state);

		var stateKey;
		switch (state) {
		case 1:
			stateKey = "alive";
			break;
		case 0:
			stateKey = "dying";
			break;
		case -1:
			stateKey = "dead";
			break;
		}

		_visNodes.update([ {
			id : _id,
			color : {
				background : typeInfo[stateKey],
				highlight : {
					background : typeInfo[stateKey]
				}
			}
		} ]);
	};

	// update node work state
	this.updateNodeWorkState = function(_id, _type, state) {

		var typeInfo = config[_type];

		if (typeInfo == undefined) {
			return;
		}

		state = parseInt(state);

		var stateKey;
		switch (state) {
		case 1:
			stateKey = "good";
			break;
		case 0:
			stateKey = "warn";
			break;
		case -1:
			stateKey = "err";
			break;
		case -2:
			stateKey = "dead";
			break;
		}

		_visNodes.update([ {
			id : _id,
			color : {
				border : typeInfo["work"][stateKey],
				highlight : {
					border : typeInfo["work"][stateKey]
				}
			}
		} ]);
	};
	
	//update node work power
	this.updateNodeWorkPower=function(_id, _type, workpower) {
		
		var typeInfo = config[_type];

		if (typeInfo == undefined) {
			return;
		}

		var state = "l"+parseInt(workpower);

		_visNodes.update([ {
			id : _id,
			borderWidth:typeInfo["border"][state],
			borderWidthSelected:typeInfo["border"][state]+2
		}]);
	};
	
	//exist node
	this.existNode=function(id) {
		var node=_visNodes.get(id);
		
		return (node==undefined)?false:true;
	};
	
	//get node
	this.getNode=function(id) {
		return _visNodes.get(id);
	};

	// get the vis DataSet
	this.getNodes = function() {
		return _visNodes;
	};
}

function AppEdges(_config) {

	var config = _config;
	var _visEdges = new vis.DataSet();

	function getId(from, to) {
		return from + "->" + to;
	}
	
	function update(newEdge,edge) {
		
		if (edge["width"] != undefined) {
			newEdge["width"] = edge["width"];
		}

		if (edge["tip"] != undefined) {
			newEdge["title"] = edge["tip"];
		}
		
		if (edge["dead"]!=undefined) {
			newEdge["dashes"]=edge["dead"];			
		}

		if (edge["workstate"] != undefined) {

			state = parseInt(edge["workstate"]);

			var stateKey;
			switch (state) {
			case 1:
				stateKey = "good";
				break;
			case 0:
				stateKey = "warn";
				break;
			case -1:
				stateKey = "err";
				break;
			case -2:
				stateKey = "dead";
				break;
			}

			newEdge["color"] = config["work"][stateKey];
			newEdge["highlight"] = config["work"][stateKey];
		} else {
			newEdge["color"] = config["work"]["dead"];
			newEdge["highlight"] = config["work"]["dead"];
		}
		
		if (edge["extattrs"]!=undefined) {			
			newEdge["extattrs"]=edge["extattrs"];			
		}
		
		edge["arrows"]=config["arrows"];
	}

	// {from:,to:,}
	this.addEdges = function(edges) {

		for (var i = 0; i < edges.length; i++) {
			
			var edge = edges[i];
			
			var edgeId=getId(edge["from"], edge["to"]);
			
			//if the edge exists, we should not re-add it
			if (_visEdges.get(edgeId)!=undefined) {
				this.updateEdge(edge);
				continue;
			}
			
			edges[i] = {
				id : edgeId,
				from : edge["from"],
				to : edge["to"],
				arrows : config["arrows"]
			};

			update(edges[i],edge);

			_visEdges.add([edges[i]]);
		}

	};
	
	//remove edge
	this.removeEdge=function(id) {
		_visEdges.remove(id);
	}
	
	//update edge
	this.updateEdge=function(nd) {
		
		var edge={
			id:getId(nd["from"], nd["to"])
		};
		
		update(edge,nd);
		
		_visEdges.update([edge]);
	};

	// change work state
	this.updateEdgeWorkState = function(_from, _to, state) {

		state = parseInt(state);

		var stateKey;
		switch (state) {
		case 1:
			stateKey = "good";
			break;
		case 0:
			stateKey = "warn";
			break;
		case -1:
			stateKey = "err";
			break;
		case -2:
			stateKey = "dead";
			break;
		}

		var ve = {
			id : getId(_from, _to),
			color : {
				color : config["work"][stateKey],
				highlight : config["work"][stateKey]
			}
		};

		_visEdges.update([ ve ]);
	};

	// change width
	this.updateEdgeWidth = function(_from, _to, _width) {
		var ve = {
			id : getId(_from, _to),
			width : _width
		};

		_visEdges.update([ ve ]);
	};

	// update edge tip
	this.updateEdgeTip = function(_from, _to, _tip) {
		var ve = {
			id : getId(_from, _to),
			title : _tip
		};

		_visEdges.update([ ve ]);
	};
	
	//get edge
	this.getEdge=function(id) {
		
		return _visEdges.get(id);
	};
	
	this.getEdgeByFromTo=function(_from,_to) {
		var id=getId(_from, _to);
		return this.getEdge(id);
	};

	// get edges
	this.getEdges = function() {

		return _visEdges;
	};
}

function AppClusterLayout(_config) {

	var visOptions = {
		layout : {
			randomSeed : undefined,
			hierarchical : {
				enabled : true,
				direction : 'LR',
				nodeSpacing : 150,
				levelSeparation : 320,
				parentCentralization : false
			}
		},
		edges : {
			"smooth" : {
				"forceDirection" : "none"
			}
		},
		physics : {
			enabled : true,
			solver : "barnesHut",
			"barnesHut" : {
				"gravitationalConstant" : -6000
			},
			"minVelocity" : 0.75,
			"timestep" : 0.2
		}
	};

	var config = {
		cid : "",
		layout: "top", //top: topology, net: network
		events: {
			ondbclick:undefined
		},
		node : {
			app : {
				shape : "box",
				border : {
					l1:3,
					l2:6,
					l3:9,
				},
				font : {
					face : '微软雅黑',
					size : 12,
					color : '#fff',
					align : 'left'
				},
				alive : "rgba(71,107,36,1)",
				dying : "rgba(205,38,38,1)",
				dead : "rgba(105,105,105,1)",
				work : {
					good : "#A2CD5A",
					warn : "#FFA500",
					err : "rgba(255,0,0,1)",
					dead : "rgba(139,139,139,1)"
				}
			},
			serviceport : {
				shape : "circle",
				border : {
					l1:3,
					l2:6,
					l3:9,
				},
				font : {
					face : '微软雅黑',
					size : 12,
					color : '#fff',
					align : 'left'
				},
				alive : "rgba(71,107,36,1)",
				dying : "rgba(205,38,38,1)",
				dead : "rgba(105,105,105,1)",
				work : {
					good : "#A2CD5A",
					warn : "#FFA500",
					err : "rgba(255,0,0,1)",
					dead : "rgba(139,139,139,1)"
				}
			},
			db : {
				shape : "database",
				border : {
					l1:3,
					l2:6,
					l3:9
				},
				font : {
					face : '微软雅黑',
					size : 12,
					color : '#fff',
					align : 'left'
				},
				alive : "#48D1CC",
				dying : "#48D1CC",
				dead : "#48D1CC",
				work : {
					good : "#fff",
					warn : "#fff",
					err : "#fff",
					dead : "#fff"
				}
			},
			mq : {
				shape : "box",
				border : {
					l1:3,
					l2:6,
					l3:9
				},
				font : {
					face : '微软雅黑',
					size : 12,
					color : '#fff',
					align : 'left'
				},
				alive : "#BA55D3",
				dying : "#BA55D3",
				dead : "#BA55D3",
				work : {
					good : "#668B8B"
				}
			},
			proxy : {
				shape : "box",
				border : {
					l1:3,
					l2:6,
					l3:9
				},
				font : {
					face : '微软雅黑',
					size : 12,
					color : '#fff',
					align : 'left'
				},
				alive : "#48D1CC",
				dying : "#48D1CC",
				dead : "#48D1CC",
				work : {
					good : "#fff",
					warn : "#fff",
					err : "#fff",
					dead : "#fff"
				}
			},
			browser : {
				shape : "circle",
				border : {
					l1:3,
					l2:6,
					l3:9
				},
				font : {
					face : '微软雅黑',
					size : 12,
					color : '#fff',
					align : 'left'
				},
				alive : "#48D1CC",
				dying : "#48D1CC",
				dead : "#48D1CC",
				work : {
					good : "#fff",
					warn : "#fff",
					err : "#fff",
					dead : "#fff"
				}
			},
			unknown : {
				shape : "box",
				border : {
					l1:3,
					l2:6,
					l3:9
				},
				font : {
					face : '微软雅黑',
					size : 12,
					color : '#fff',
					align : 'left'
				},
				alive : "#333",
				work : {
					good : "#668B8B"
				}
			}
		},
		edge : {
			arrows : 'to',
			work : {
				good : "#A2CD5A",
				warn : "#FFA500",
				err : "rgba(255,0,0,1)",
				dead : "rgba(139,139,139,1)"
			}
		}
	};

	JsonHelper.merge(config, _config, true);

	var appNodes = new AppNodes(config["node"]);
	var appEdges = new AppEdges(config["edge"]);
	var network;

	this.addNodes = function(nodes) {
		appNodes.addNodes(nodes);
	};

	this.addEdges = function(edges) {
		appEdges.addEdges(edges);
	}

	this.draw = function() {

		if (network == undefined) {
			var data = {
				nodes : appNodes.getNodes(),
				edges : appEdges.getEdges()
			};
			var container = HtmlHelper.id(config["cid"]);
			
			if (config["layout"]=="net") {
				visOptions["layout"]["hierarchical"]["enabled"]=false;
			}
			
			network = new vis.Network(container, data, visOptions);
			
			//double click
			network.on("doubleClick", function (params) {
				if (config["events"]["ondbclick"]!=undefined) {
					config["events"]["ondbclick"](params);
				}
			});
		} else {

		}
	};

	this.nodes = function() {
		return appNodes;
	};

	this.edges = function() {
		return appEdges;
	};
}
