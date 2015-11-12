/**
 * @fileOverview Application's main function.
 *
 * @author Michał Oniszczuk <m.oniszczuk@icm.edu.pl>
 */

require(
    [ "lib/d3"
    , "lib/jquery"
    , "util"
    , "translations"
    , "LanguageSelectorController"
    , "DataProvider"
    , "GraphController"
    , "GraphModel"
    , "SidebarController"
    , "sidebar"
    ], function()
{
  translations.translateAll();
  new LanguageSelectorController();

  var URLBase = "http://localhost:8080/data/";
  var dataProvider = new DataProvider(URLBase+"graph.json", URLBase+"search",URLBase+"graphById");
  var graphController = new GraphController(dataProvider, ["comac:pbn_9999"]);
//  graphController.loadInitialGraph();
  var sidebarController = new SidebarController(dataProvider, graphController);

  window.sidebar.dataProvider = dataProvider;  
  window.sidebar.graphController = graphController;
  window.sidebar.init();
}); // require end

