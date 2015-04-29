/**
 * Application's main
 *
 * @author Michał Oniszczuk michal.oniszczuk@gmail.com
 */

require(
    [ "lib/d3"
    , "DataProvider"
    , "GraphController"
    , "SidebarController"
    ], function()
{

  var dataProvider = new DataProvider("http://localhost:8080/data/graph.json");
  var graphController = new GraphController(dataProvider, ["comac:pbn_9999"]);
  var sidebarController = new SidebarController(dataProvider, graphController);

}); // require end

