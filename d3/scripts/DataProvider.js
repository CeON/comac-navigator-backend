/**
 * @fileOverview DataProvider class.
 *
 * @author Michał Oniszczuk <m.oniszczuk@icm.edu.pl>
 */

/**
 * Creates an instance of DataProvider.
 *
 * @constructor
 * @this {DataProvider}
 * @param {string} graphUri
 * @param {string} searchUri
 * @param {string} graphByIdUri
 */
function DataProvider(graphUri, searchUri, graphByIdUri) {
    this.graph = graphUri;
    this.searchAddress = searchUri;
    this.graphById = graphByIdUri;
}

DataProvider.prototype = {
  /**
   * Provides graph data. Provided graph consists of:
   *  - favourite nodes,
   *  - neighbours of favourite nodes,
   *  - links starting / ending on favourite nodes.
   * 
   * @param favouriteIds nodes to start the query from
   * @param callback     continuation, function(error, graph)
   */
  getGraphByFavouriteIds: function(favouriteIds, callback) {
    var query = favouriteIds.sort().join("|");
    DataProvider.queryJSON(this.graph, query, callback);
  },
  
  getGraphById: function(graphId, callback) {
      DataProvider.queryJSON(this.graphById, graphId, callback);
  },
  
  search: function(textQuery, callback) {
      console.log("Search invoked, query="+textQuery);
      //now inwoke the ajax:
      
      DataProvider.queryJSON(this.searchAddress, textQuery, callback);
//      this.mockSearch(cursorMark, callback);
      
  },
  
  mockSearch: function(cursorMark, callback) {
      console.log("Running mock search");
      DataProvider.queryJSON("data/searchResultsV2.json","", function(error, data) {
//          console.log('got as error from query json: ' + error);
//          console.log('got as result from query json: ' + JSON.stringify(data));
          var res = data['big'][cursorMark];
//          console.log(res);
          callback(null, res);
      });
  }
  
}

DataProvider.queryJSON = function(fileName, query, callback) {
    d3.json(fileName+"?query="+encodeURIComponent(query), function(error, data) {
    if (error !== null) {
      // propagate error
      callback(error, null);
    } else {
        var result = data;

      if (result === undefined) {
        // response undefined
        callback("Response for such list of node ids is undefined.", null);
      } else {
        // no error
        callback(error, result);
      }
    }
  });
}

