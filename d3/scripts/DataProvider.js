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
     * Provided graph consists of:
     * <ul>
     *     <li> favourite nodes,
     *     <li> neighbours of favourite nodes,
     *     <li> links starting / ending on favourite nodes.
     * </ul>
     * @callback DataProvider~graphCallback
     * @param {string} error may be null if there was no error
     * @param graphJSON
     */

    /**
     * Gets a graph which is constructed starting from a given list of
     * favourite nodes ids.
     * @param {string[]} favouriteIds nodes to start the query from
     * @param {DataProvider~graphCallback} callback continuation
     */
    getGraphByFavouriteIds: function (favouriteIds, callback) {
        var query = favouriteIds.sort().join("|");
        DataProvider.queryJSON(this.graph, query, callback);
    },

    /**
     * Gets a graph by its id.
     * @param {string} graphId
     * @param {DataProvider~graphCallback} callback
     */
    getGraphById: function (graphId, callback) {
        DataProvider.queryJSON(this.graphById, graphId, callback);
    },

    /**
     * @callback DataProvider~searchCallback
     * @param {string} error may be null if there was no error
     * @param searchResultsJSON
     */

    /**
     * Gets search results.
     * @param {string} textQuery
     * @param {DataProvider~searchCallback} callback
     */
    search: function (textQuery, callback) {
        console.log("Search invoked, query=" + textQuery);
        //now inwoke the ajax:

        DataProvider.queryJSON(this.searchAddress, textQuery, callback);

    },
}

DataProvider.queryJSON = function (fileName, query, callback) {
    d3.json(fileName + "?query=" + encodeURIComponent(query), function (error, data) {
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

