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
        "lib/ZeroClipboard.min",
        "lib/d3",
        "jquery",
        "jquery.bootstrap",
        "util",
        "translations",
        "LanguageSelectorController",
        "DataProvider",
        "GraphController",
        "GraphModel",
        "SidebarController",
        "sidebar"
    ],
    function (ZeroClipboard) {
        var client = new ZeroClipboard($("#copyToClipboardButton"));
        client.on('ready', function (event) {
            console.log('ZeroClipboard is loaded');

            client.on('copy', function (event) {
                event.clipboardData.setData('text/plain', $("#shareGraphInput").attr("value"));
            });

            client.on('aftercopy', function (event) {
                console.log('Copied text to clipboard: ' + event.data['text/plain']);
            });
        });

        client.on('error', function (event) {
            console.log('ZeroClipboard error of type "' + event.name + '": ' + event.message);
            ZeroClipboard.destroy();
        });


        translations.translateAll();
        new LanguageSelectorController();

        var URLBase = "http://localhost:8080/data/";
        var dataProvider = new DataProvider(URLBase + "graph.json", URLBase + "search", URLBase + "graphById");
        var graphController = new GraphController(dataProvider, ["comac:pbn_9999"]);
        //  graphController.loadInitialGraph();
        var sidebarController = new SidebarController(dataProvider, graphController);

        $("#clearGraphConfirm").click(function () {
            graphController.clearGraph()
            console.log("Clear graph clicked.");
        });

        window.sidebar.dataProvider = dataProvider;
        window.sidebar.graphController = graphController;
        window.sidebar.init();
    }); // require end

