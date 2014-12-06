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

  var dataProvider = new DataProvider();
  var graphController = new GraphController(dataProvider);
  var sidebarController = new SidebarController(dataProvider, graphController);

}); // require end

