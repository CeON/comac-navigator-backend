/**
 * @fileOverview Application's main function.
 *
 * @author Michał Oniszczuk <m.oniszczuk@icm.edu.pl>
 */

requirejs.config({
    paths: {
        "jquery": "lib/jquery",
        "jquery.bootstrap": "lib/bootstrap.min"
    },
    shim: {
        "jquery.bootstrap": {
            deps: ["jquery"]
        }
    }
});

require(
    [
        "CopyToClipboardController",
        "LanguageSelectorController",
        "ClearGraphController",
        "lib/d3",
        "jquery",
        "jquery.bootstrap",
        "util",
        "translations",
        "DataProvider",
        "GraphController",
        "GraphModel",
        "SidebarController",
        "sidebar"
    ],
    function (CopyToClipboardController) {
        translations.translateAll();

        var URLBase = "http://localhost:8080/data/";
        var dataProvider = new DataProvider(URLBase + "graph.json", URLBase + "search", URLBase + "graphById");
        var graphController = new GraphController(dataProvider, ["comac:pbn_9999"]);
        //  graphController.loadInitialGraph();
        var sidebarController = new SidebarController(dataProvider, graphController);

        new CopyToClipboardController();
        new LanguageSelectorController();
        new ClearGraphController(graphController);

        window.sidebar.dataProvider = dataProvider;
        window.sidebar.graphController = graphController;
        window.sidebar.init();
    }); // require end

