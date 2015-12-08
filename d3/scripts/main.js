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
        "config",
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

        var dataProvider = new DataProvider(
            config.URLBase + "graph.json",
            config.URLBase + "search",
            config.URLBase + "graphById");
        var graphController = new GraphController(dataProvider, ["comac:pbn_9999"]);
        var sidebarController = new SidebarController(dataProvider, graphController);

        new CopyToClipboardController();
        new LanguageSelectorController();
        new ClearGraphController(graphController);

        window.sidebar.dataProvider = dataProvider;
        window.sidebar.graphController = graphController;
        window.sidebar.init();

        graphController.sidebarController = window.sidebar;
    }); // require end

