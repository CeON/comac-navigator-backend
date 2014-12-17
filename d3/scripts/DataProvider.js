/**
 * DataProvider class
 *
 * @author Michał Oniszczuk michal.oniszczuk@gmail.com
 */


function DataProvider() {
}

DataProvider.prototype = {
  /**
   * Provides the initial graph. Such graph consists of:
   *  - favourite nodes,
   *  - neighbours of favourite nodes,
   *  - links starting / ending on favourite nodes.
   * 
   * @param favouriteIds nodes to start the query from
   * @param callback     continuation, function(error, graph)
   */
  getGraphByFavouriteIds: function(favouriteIds, callback) {
    // TODO hardcoded data, implement REST service
    d3.json("data.json", function(error, graphs) {
      if (error !== null) {
        // propagate error
        callback(error, null);
      } else {
        var query = favouriteIds.sort().join("|");
        var graph = graphs[query];

        if (graph === undefined) {
          // response undefined
          callback("Response for such list of node ids is undefined.", null);
        } else {
          // no error
          callback(error, graph);
        }
      }
    });
  }
}

