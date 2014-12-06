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
    d3.json("data.json", function(error, graph) {
      if (error === null) {
        // no error, continue
        callback(error, graph[favouriteIds.sort().join("|")]);
      } else {
        // propagate error
        callback(error, graph);
      }
    });
  }
}

