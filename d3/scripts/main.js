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

  var dataProvider = new DataProvider();
  var graphController = new GraphController(dataProvider);
  var sidebarController = new SidebarController(dataProvider, graphController);

  window.sidebar.dataProvider = dataProvider;  
  window.sidebar.init();
}); // require end

