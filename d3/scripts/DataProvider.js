/**
 * DataProvider class
 *
 * @author Michał Oniszczuk michal.oniszczuk@gmail.com
 */


function DataProvider(graphUri,searchUri) {
    this.graph = graphUri;
    this.searchAddress = searchUri;
}

DataProvider.prototype = {
  // TODO hardcoded data in *.json files, implement a REST service

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
//
//  /**
//   * Provides search results. Provided results (document, author, etc) match the query text.
//   * 
//   * @param text     the search query
//   * @param callback continuation, function(error, results)
//   */
//  getSearchResultsByText: function(text, callback) {
//    DataProvider.queryJSON("data/searchResults.json", text, callback);
//  },
//  
  
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
  d3.json(fileName+"?query="+query, function(error, data) {
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

