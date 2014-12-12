/**
 * GraphController class
 *
 * @author Michał Oniszczuk michal.oniszczuk@gmail.com
 */


function GraphController(dataProvider) {
  this.graphView = {};
  this.graphView.canvas = d3.select("body").selectAll("svg.graphView")
    .attr("width", this.width)
    .attr("height", this.height);
  this.graphView.paths   = this.graphView.canvas.append('svg:g').selectAll(".link");
  this.graphView.circles = this.graphView.canvas.append('svg:g').selectAll(".node")

  this.force = d3.layout.force()
    .charge(-600)
    //.charge(function(node) {
    //  if (node.favourite)
    //    return -500;;
    //  else
    //    return -1600;
    //})
    .linkDistance(120)
    //.linkDistance(function(link) {
    //  if (link.favourite)
    //    return 100;
    //  else
    //    return 150;
    //})
    .size([this.width, this.height]);

  this.graphModel =
    { nodes       : null
    , links       : null
    , favouriteIds: ["paper_2", "paper_3", "author_1"]
    }

  this.dataProvider = dataProvider;

  dataProvider.getGraphByFavouriteIds(
      this.graphModel.favouriteIds,
      this.showInitialGraph.bind(this));

  //this.graphModel.nodes.map(function(d) { d.favourite = false; return d; });
  //setTimeout(this.restart.bind(this), 1000);
  setTimeout((function() {this.addFavouriteNodes(["paper_0"])}).bind(this), 1000);
}


GraphController.prototype = {
  width:  960,
  height: 500,

  showInitialGraph: function(error, graph) {
    //console.log(JSON.stringify(graph))

    this.graphModel.nodes = graph.nodes;
    this.graphModel.links = GraphController.nodeIdsToReferences(
                                                  this.graphModel.nodes,
                                                  graph.links);

    //console.log(this.graphModel.links[0]);
    //console.log(JSON.stringify(this.graphModel.nodes));
    //console.log(JSON.stringify(this.graphModel.links));

    this.force
      .nodes(this.graphModel.nodes)
      .links(this.graphModel.links)
      .on("tick", (function() {
        this.graphView.paths
          .attr("x1", function(d) { return d.source.x; })
          .attr("y1", function(d) { return d.source.y; })
          .attr("x2", function(d) { return d.target.x; })
          .attr("y2", function(d) { return d.target.y; });

        this.graphView.circles
          .attr("transform", function(d, i) {
              return "translate(" + [ d.x,d.y ] + ")"
          });
      }).bind(this));

    this.restart();
    this.showNodesFirefoxHack();
  },

  addFavouriteNodes: function(newNodeIds) {
    this.graphModel.favouriteIds =
      this.graphModel.favouriteIds.concat(newNodeIds);
    this.dataProvider.getGraphByFavouriteIds(
        this.graphModel.favouriteIds, 
        this.showChangedGraph.bind(this));

    // this.graphModel.addFavouriteNodes(newNodeIds);
    // this.restart();
  },

  showChangedGraph: function(error, graph) {
    var oldNodes = this.graphModel.nodes;

    this.graphModel.nodes = graph.nodes;
    this.graphModel.links = GraphController.nodeIdsToReferences(
                                                  this.graphModel.nodes,
                                                  graph.links);
    GraphController.repositionNodes(oldNodes, graph.nodes);

    this.force
      .nodes(this.graphModel.nodes)
      .links(this.graphModel.links)
    this.restart();
    this.showNodesFirefoxHack();
  },

  showNodesFirefoxHack: function() {
    n = d3.selectAll(".node")
    n.classed("node", false)
    setTimeout(function(){n.classed("node", true)}, 0);
  },

  restart: function() {
    // paths (links)
    this.graphView.paths = this.graphView.paths
      .data(this.graphModel.links)

    // add new links
    this.graphView.paths
      .enter().append("line")
        .attr("class", function( d) {
              return "link " + d.type;
          })
        .classed("hover", function(d) { return d.hover });
        //.style("stroke-width", function(d) { return Math.sqrt(d.value); });

    // update existing & new links
    this.graphView.paths
        .style("opacity", function(d) { if (d.favourite) return 1.0; else return 0.3})

    // remove old links
    this.graphView.paths.exit().remove();


    // circles (nodes)
    this.graphView.circles = this.graphView.circles
      .data(this.graphModel.nodes, function(d) { return d.id; })

    // add new nodes
    var g = this.graphView.circles
      .enter().append("g");

    g
        .attr({
          "transform" : function( d) {
              return "translate("+ [d.x,d.y] + ")";
          },
          "class"     : function( d) {
              return "node " + d.type;
          }
        })
        .call(this.force.drag);

    g.append("circle")
        .attr("r", 30)
        .on("mouseover", function() {
            d3.select(this.parentNode).classed("hover", true);
        })
        .on("mouseout", function() { 
            d3.select(this.parentNode).classed("hover", false);
        })
      .append("title")
        .text(function(d) { return d.name; });

    g.append("text")
        .attr({
            'text-anchor'   : 'middle',
            y               : 4
        })
        .text(function(d) {
            return GraphController.shortenString(d.name);
        });

    // update existing & new nodes
    this.graphView.circles
      .style("opacity", function(d) { if (d.favourite) return 1.0; else return 0.5})

    // remove old nodes
    this.graphView.circles.exit().remove();

    this.force.start();
  }
}

GraphController.shortenString = function(str) {
  if (str.length > 8) {
    return str.slice(0, 5) + "..";
  } else {
    return str
  }
}

GraphController.nodeIdsToReferences = function(nodes, links) {
  return links.map(function(link) {
    link.source = nodes.filter(function(node) {
      return node.id === link.sourceId;
    })[0];;
    link.target = nodes.filter(function(node) {
      return node.id === link.targetId;
    })[0];
    link.hover = false;
    return link;
  });
}

GraphController.repositionNodes = function(oldNodesRaw, newNodes) {
  var oldNodes = asDictionaryByIds(oldNodesRaw);
  newNodes.forEach(repositionSingleNode(oldNodes));

  function asDictionaryByIds(xs) {
    return xs.reduce(function (dict, item) {
      dict[item.id] = item;
      return dict;
    }, {});
  }

  function repositionSingleNode(oldNodes) {
    return function(newNode) {
      var newNodeId = newNode.id;
      if(oldNodes.hasOwnProperty(newNodeId)) {
        var copyFrom = oldNodes[newNodeId];
        newNode.x = copyFrom.x;
        newNode.y = copyFrom.y;
      }
    }
  }
}

