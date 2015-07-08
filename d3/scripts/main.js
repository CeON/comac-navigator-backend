/**
 * Application's main
 *
 * @author Michał Oniszczuk michal.oniszczuk@gmail.com
 */

require(
    [ "lib/d3"
    , "lib/jquery"
    , "DataProvider"
    , "GraphController"
    , "SidebarController"
    , "sidebar"
    ], function()
{

  var dataProvider = new DataProvider("http://localhost:8080/data/graph.json", "http://localhost:8080/data/search");
  var graphController = new GraphController(dataProvider, ["comac:pbn_9999"]);
  var sidebarController = new SidebarController(dataProvider, graphController);

  window.sidebar.dataProvider = dataProvider;  
  window.sidebar.graphController = graphController;
  window.sidebar.init();
}); // require end

