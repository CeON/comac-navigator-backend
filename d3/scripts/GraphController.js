/**
 * GraphController class
 *
 * @author Michał Oniszczuk michal.oniszczuk@gmail.com
 */


function GraphController(dataProvider) {
  this.graphView = {};
  
  
//  this.graphView.canvas = d3.select("body").selectAll("svg.graphView")
//    .attr("width", this.width)
//    .attr("height", this.height);
    //
    this.graphView.canvas  = d3.select("svg.graphView");
    var canv = this.graphView.canvas;
    this.width = parseInt(canv.style("width"));
    this.height = parseInt(canv.style("height"));
    console.log("Width and height are: "+this.width+", "+this.height);
//    .attr("height", this.height);
  this.graphView.paths   = this.graphView.canvas.append('svg:g').selectAll(".link");
  this.graphView.circles = this.graphView.canvas.append('svg:g').selectAll(".node")

  this.force = d3.layout.force()
    .charge(-200)
    .linkDistance(120)
    .size([this.width, this.height])
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

  this.graphModel =
    { nodes       : []
    , links       : []
    , favouriteIds: []
    }

  this.dataProvider = dataProvider;

  this.setFavouriteNodes(["comac:pbn_9999"]);
}


GraphController.prototype = {
  width:  960,
  height: 500,

  updateGraph: function(newFavouriteIds) {
    return function(error, graphJSON) {
      if (error === null) {
        var oldNodes = this.graphModel.nodes;
        GraphModel.updateGraphModel(newFavouriteIds, this.graphModel, graphJSON);
        GraphModel.repositionNodes(oldNodes, this.graphModel.nodes);
        this.updateGraphView();
      } else {
        console.error(
            "Failed to get graph for ids: " + newFavouriteIds +
            ". Got an error: " + error);
      }
    }
  },

  updateGraphView: function() {
    this.force
      .nodes(this.graphModel.nodes)
      .links(this.graphModel.links)
      .start();

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
      .on("mousedown", (function(d) {
        if (d.favourite) {
          this.removeFavouriteNodes([d.id]);
        } else {
          this.addFavouriteNodes([d.id]);
        }
      }).bind(this))
      .style("opacity", function(d) { if (d.favourite) return 1.0; else return 0.5});

    // remove old nodes
    this.graphView.circles.exit().remove();

    this.showNodesFirefoxHack();
  },

  showNodesFirefoxHack: function() {
    n = d3.selectAll(".node")
    n.classed("node", false)
    setTimeout(function(){n.classed("node", true)}, 0);
  },

  addFavouriteNodes: function(newNodeIds) {
    var newFavouriteIds = 
      this.graphModel.favouriteIds.concat(newNodeIds);
    this.setFavouriteNodes(newFavouriteIds);
  },

  removeFavouriteNodes: function(deletedNodeIds) {
    var newFavouriteIds =
      this.graphModel.favouriteIds
        .filter(function(id) { 
          return !arrayContains(deletedNodeIds, id);
        });
    this.setFavouriteNodes(newFavouriteIds);

    function arrayContains(xs, x) {
      return xs.indexOf(x) > -1;
    }
  },

  setFavouriteNodes: function(newFavouriteIds) {
    this.dataProvider.getGraphByFavouriteIds(
        newFavouriteIds,
        this.updateGraph(newFavouriteIds).bind(this));
  }
}

GraphController.shortenString = function(str) {
  if (str.length > 7) {
    return str.slice(0, 4) + "..";
  } else {
    return str;
  }
}


/**
 * GraphModel class
 *
 * @author Michał Oniszczuk michal.oniszczuk@gmail.com
 */


function GraphModel() {}

GraphModel.updateGraphModel = function(newFavouriteIds, graphModel, graphJSON) {
  graphModel.favouriteIds = newFavouriteIds;

  graphModel.nodes = graphJSON.nodes;
  graphModel.links = nodeIdsToReferences(graphJSON.nodes,
                                         graphJSON.links);

  function nodeIdsToReferences(nodes, links) {
    return links.map(function(link) {
      link.source = nodes.filter(function(node) {
        return node.id === link.sourceId;
      })[0];
      link.target = nodes.filter(function(node) {
        return node.id === link.targetId;
      })[0];
      link.hover = false;
      return link;
    });
  }
}

GraphModel.repositionNodes = function(oldNodesRaw, newNodes) {
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

