/**
 * @fileOverview GraphController class.
 *
 * @author Michał Oniszczuk <m.oniszczuk@icm.edu.pl>
 */

/**
 * Creates an instance of GraphController.
 *
 * @constructor
 * @this {GraphController}
 * @param {DataProvider} dataProvider
 * @param {string[]} initialNodes
 */
function GraphController(dataProvider, initialNodes) {
    this.graphView = {};


    this.graphView.canvas = d3.select("svg.graphView");
    var canv = this.graphView.canvas;
    this.width = parseInt(canv.style("width"));
    this.height = parseInt(canv.style("height"));
    console.log("Width and height are: " + this.width + ", " + this.height);
    var globg = canv.append('svg:g').attr('id', 'globalG');

    this.graphView.paths = globg.append('svg:g').selectAll(".link");
    this.graphView.circles = globg.append('svg:g').selectAll(".node")

    this.force = d3.layout.force()
        .charge(-900)
        .linkDistance(120)
        .size([this.width, this.height])
        .on("tick", (function () {
            this.graphView.paths
                .attr("x1", function (d) {
                    return d.source.x;
                })
                .attr("y1", function (d) {
                    return d.source.y;
                })
                .attr("x2", function (d) {
                    return d.target.x;
                })
                .attr("y2", function (d) {
                    return d.target.y;
                });

            this.graphView.circles
                .attr("transform", function (d, i) {

                    return "translate(" + [d.x, d.y] + ")";
                });
        }).bind(this));

    this.graphModel =
    {
        nodes: [],
        links: [],
        favouriteIds: [],
    }

    this.dataProvider = dataProvider;
    this.loadInitialGraph();


    // create the zoom listener
    var zoomListener = d3.behavior.zoom()
        .scaleExtent([0.05, 1.5])
        .on("zoom", zoomHandler);
        //.on("dblclick.zoom", null);

    // function for handling zoom event
    function zoomHandler() {
        console.log(d3.event);
        globg.attr("transform", "translate(" + d3.event.translate + ") scale(" + d3.event.scale + ")");
    }

    zoomListener(canv);


}


GraphController.prototype = {
    width: 960,
    height: 500,

    updateURL: function (graphId) {
        var queryString;
        if (graphId) {
            queryString = "?graph=" + graphId;
        }
        else {
            queryString = "?"
        }
        console.log("queryString: ", queryString);
        window.history.pushState("Anything", "Title", queryString);
        $("#shareGraphInput").attr("value", window.location);
    },

    updateGraph: function () {
        return function (error, graphJSON) {
            if (error === null) {
                var oldNodes = this.graphModel.nodes;
                GraphModel.updateGraphModel(this.graphModel, graphJSON);
                GraphModel.repositionNodes(oldNodes, this.graphModel.nodes);
                this.updateGraphView();
                this.updateURL(graphJSON.graphId);
            } else {
                console.error(
                    "Failed to get graph. Got an error: " + error);
            }
        }
    },

    updateGraphView: function () {
        this.force
            .nodes(this.graphModel.nodes)
            .links(this.graphModel.links)
            .start();

        // paths (links)
        this.graphView.paths = this.graphView.paths
            .data(this.graphModel.links);

        // add new links
        this.graphView.paths
            .enter().append("line")
            .attr("class", function (d) {
                return "link " + d.type;
            })
            .classed("hover", function (d) {
                return d.hover
            });
        //.style("stroke-width", function(d) { return Math.sqrt(d.value); });

        // update existing & new links
        this.graphView.paths
            .style("opacity", function (d) {
                if (d.favourite) return 1.0; else return 0.3
            })

        // remove old links
        this.graphView.paths.exit().remove();


        // circles (nodes)
        this.graphView.circles = this.graphView.circles
            .data(this.graphModel.nodes, function (d) {
                return d.id;
            });

        this.graphView.circles.select("g").transition().duration(1000).attr("transform", function (d) {
            if (!d.importance) {
                d.importance = 1;
            }
            return "scale(" + d.importance + ")";
        })

        // add new nodes
        var g = this.graphView.circles
            .enter().append("g");

        g.attr({
                "transform": function (d) {
                    return " translate(" + [d.x, d.y] + ")";
                },
                "class": function (d) {
                    return "node " + d.type;
                }
            })
            .call(this.force.drag);
        //    this.force.drag.on("dragstart", function() {
        //  d3.event.sourceEvent.stopPropagation(); // silence other listeners
        //});
        var inner = g.append("g");
        inner.attr("transform", function (d) {
            if (!d.importance)
                d.importance = 0.7 + 0.6 * Math.random();
            return "scale(" + d.importance + ")";
        })
        inner.append("rect")
            .attr("width", 120)
            .attr("x", -60)
            .attr("height", 30)
            .attr("y", -15)
            .attr("rx", 10)
            .attr("ry", 10)
            .on("mouseover", function () {
                d3.select(this.parentNode.parentNode).classed("hover", true);
            })
            .on("mouseout", function () {
                d3.select(this.parentNode.parentNode).classed("hover", false);
            })
            .append("title")
            .text(function (d) {
                return d.name;
            });

        inner.append("text")
            .attr({
                'text-anchor': 'middle',
                y: 4
            })
            .text(function (d) {
                return GraphController.shortenString(d.name);
            });

        // update existing & new nodes
        var clickedOnce = false;
        var timer;

        function dist(a, b) {
            return Math.sqrt(Math.pow(a[0] - b[0], 2), Math.pow(a[1] - b[1], 2));
        }

        this.graphView.circles
            .on("dblclick", (function () {
                d3.event.stopPropagation();
            }))
            .on("click", (function (d) {
                if (clickedOnce) {
                    clickedOnce = false;
                    clearTimeout(timer);
                    this.onDoubleClick(d);
                } else {
                    timer = setTimeout((function () {
                        this.onSingleClick(d);
                        clickedOnce = false;
                    }).bind(this), 300);
                    clickedOnce = true;
                }
            }).bind(this))
            .style("opacity", function (d) {
                if (d.favourite) return 1.0; else return 0.3
            })
            .on("mousedown", function () {
                d3.event.stopPropagation();
            });
        // remove old nodes
        this.graphView.circles.exit().remove();
        //this.showNodesFirefoxHack();
    },

    showNodesFirefoxHack: function () {
        n = d3.selectAll(".node")
        n.classed("node", false)
        setTimeout(function () {
            n.classed("node", true)
        }, 0);
    },

    onSingleClick: function (d) {
        this.sidebarController.showNodeInfo(d);
    },

    onDoubleClick: function (d) {
        if (d.favourite) {
            this.removeFavouriteNodes([d.id]);
        } else {
            this.addFavouriteNodes([d.id]);
        }
    },

    addFavouriteNodes: function (newNodeIds) {
        var newFavouriteIds =
            this.graphModel.favouriteIds.concat(newNodeIds);
        this.setFavouriteNodes(newFavouriteIds);
    },

    removeFavouriteNodes: function (deletedNodeIds) {
        var newFavouriteIds =
            this.graphModel.favouriteIds
                .filter(function (id) {
                    return !arrayContains(deletedNodeIds, id);
                });
        this.setFavouriteNodes(newFavouriteIds);

        function arrayContains(xs, x) {
            return xs.indexOf(x) > -1;
        }
    },

    setFavouriteNodes: function (newFavouriteIds) {
        this.dataProvider.getGraphByFavouriteIds(
            newFavouriteIds,
            this.updateGraph().bind(this));
    },

    clearGraph: function () {
        this.setFavouriteNodes([]);
    },

    //uses url to locate graph for page. If there is no 'graph' request key in url, then empty
    //graph is loaded.
    loadInitialGraph: function () {
        console.log("Loading initial graph...");
        var graphId = util.getQueryStringValue("graph");

        //now request initial graphs:
        if (graphId) {
            this.dataProvider.getGraphById(graphId,
                this.updateGraph().bind(this));
        } else {
            this.clearGraph();
        }
    }

}

GraphController.shortenString = function (str) {
    if (str.length > 17) {
        return str.slice(0, 14) + "..";
    } else {
        return str;
    }
}

