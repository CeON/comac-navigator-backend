/**
 * GraphController class
 *
 * @author Michał Oniszczuk michal.oniszczuk@gmail.com
 */


function GraphController(dataProvider) {
  this.favouriteIds = ["paper_2", "paper_3", "author_1"];

  this.graphView = d3.select("body").selectAll(".graphView")
    .attr("width", this.width)
    .attr("height", this.height);

  this.force = d3.layout.force()
    .charge(-800)
    //.charge(function(node) {
    //  if (node.favourite)
    //    return -500;;
    //  else
    //    return -1600;
    //})
    .linkDistance(100)
    //.linkDistance(function(link) {
    //  if (link.favourite)
    //    return 100;
    //  else
    //    return 150;
    //})
    .size([this.width, this.height]);

  this.computedGraph =
    { nodes   : null
    , links   : null
    , paths   : this.graphView.selectAll(".link")
    , circles : this.graphView.selectAll(".node")
    }

  dataProvider.getGraphByFavouriteIds(this.favouriteIds, this.showInitialGraph.bind(this));

  //this.computedGraph.nodes.map(function(d) { d.favourite = false; return d; });
  //setTimeout(this.restart.bind(this), 1000);
}


GraphController.prototype = {
  width:  960,
  height: 500,

  showInitialGraph: function(error, graph) {
    //console.log(JSON.stringify(graph))

    this.computedGraph.nodes = graph.nodes;
    this.computedGraph.links = GraphController.nodeIdsToReferences(
                                                  this.computedGraph.nodes,
                                                  graph.links);

    //console.log(this.computedGraph.links[0]);
    //console.log(JSON.stringify(this.computedGraph.nodes));
    //console.log(JSON.stringify(this.computedGraph.links));

    this.force
      .nodes(this.computedGraph.nodes)
      .links(this.computedGraph.links)
      .on("tick", (function() {
        this.computedGraph.paths
          .attr("x1", function(d) { return d.source.x; })
          .attr("y1", function(d) { return d.source.y; })
          .attr("x2", function(d) { return d.target.x; })
          .attr("y2", function(d) { return d.target.y; });

        this.computedGraph.circles
          .attr("transform", function(d, i) {
              return "translate(" + [ d.x,d.y ] + ")"
          });
      }).bind(this));

    this.restart();
    this.force.start();
    this.showNodesFirefoxHack();
  },

  showNodesFirefoxHack: function() {
    n = d3.selectAll(".node")
    n.classed("node", false)
    setTimeout(function(){n.classed("node", true)}, 0);
  },

  restart: function() {
    // paths (links)
    this.computedGraph.paths = this.computedGraph.paths
      .data(this.computedGraph.links)

    // add new links
    this.computedGraph.paths
      .enter().append("line")
        .attr("class", function( d) {
              return "link " + d.type;
          })
        .classed("hover", function(d) { return d.hover });
        //.style("stroke-width", function(d) { return Math.sqrt(d.value); });

    // update existing & new links
    this.computedGraph.paths
        .style("opacity", function(d) { if (d.favourite) return 1.0; else return 0.3})

    // remove old links
    this.computedGraph.paths.exit().remove();


    // circles (nodes)
    this.computedGraph.circles = this.computedGraph.circles
      .data(this.computedGraph.nodes, function(d) { return d.id; })

    // add new nodes
    var g = this.computedGraph.circles
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
    this.computedGraph.circles
      .style("opacity", function(d) { if (d.favourite) return 1.0; else return 0.5})

    // remove old nodes
    this.computedGraph.circles.exit().remove();
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
