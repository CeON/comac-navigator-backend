/**
 * @fileOverview GraphModel class.
 *
 * @author Michał Oniszczuk <m.oniszczuk@icm.edu.pl>
 */

/**
 * Creates an instance of GraphModel.
 *
 * @constructor
 * @this {GraphModel}
 */
function GraphModel() {
}

GraphModel.updateGraphModel = function (graphModel, graphJSON) {
    //graphModel.favouriteIds = newFavouriteIds;
    graphModel.nodes = graphJSON.nodes;
    graphModel.favouriteIds = graphModel.nodes
        .filter(function (node) {
            return node.favourite;
        })
        .map(function (node) {
            return node.id;
        });

    graphModel.graphId = graphJSON.graphId;
    graphModel.links = nodeIdsToReferences(graphJSON.nodes, graphJSON.links);

    function nodeIdsToReferences(nodes, links) {
        return links.map(function (link) {
            link.source = nodes.filter(function (node) {
                return node.id === link.sourceId;
            })[0];
            link.target = nodes.filter(function (node) {
                return node.id === link.targetId;
            })[0];
            link.hover = false;
            return link;
        });
    }
}

GraphModel.repositionNodes = function (oldNodesRaw, newNodes) {
    var oldNodes = asDictionaryByIds(oldNodesRaw);
    newNodes.forEach(repositionSingleNode(oldNodes));

    function asDictionaryByIds(xs) {
        return xs.reduce(function (dict, item) {
            dict[item.id] = item;
            return dict;
        }, {});
    }

    function repositionSingleNode(oldNodes) {
        return function (newNode) {
            var newNodeId = newNode.id;
            if (oldNodes.hasOwnProperty(newNodeId)) {
                var copyFrom = oldNodes[newNodeId];
                newNode.x = copyFrom.x;
                newNode.y = copyFrom.y;
            }
        }
    }
}


