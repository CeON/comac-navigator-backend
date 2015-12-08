/**
 * @fileOverview TODO merge this file with SidebarController class.
 *
 * @author Aleksander Nowiński <a.nowinski@icm.edu.pl>
 * @author Michał Oniszczuk <m.oniszczuk@icm.edu.pl>
 */

window.sidebar = {};

window.sidebar.init = function () {
    console.log("Loading button...");

    $("#search-button").click(function (event) {
        event.preventDefault();
        console.log("Button pressed");
        window.sidebar.doSearch();
    });
    //do the same for the search text field:
    $("#search-input").keyup(function (e) {
        if (e.keyCode === 13) {
            console.log("Enter pressed.");
            window.sidebar.doSearch();
        }
    });
    d3.select("#mainGraph").attr("droppable", true).on('dragenter', function (d) {
        console.log('dragenter');
        d3.event.preventDefault();
        d3.event.stopPropagation();
    }).on('dragover', function (d) {
        console.log('drag');
        d3.event.preventDefault();
        d3.event.stopPropagation();
    }).on('dragend', function (d) {
        console.log('dragEnd');
    }).on('drop', function (d) {
        var id = d3.event.dataTransfer.getData("text");
        console.log('droped ' + id);
        window.sidebar.graphController.addFavouriteNodes([id]);
        d3.event.preventDefault();
    });

};

window.sidebar.showNodeInfo = function (node) {
    console.log("Show node info for " + node.id);
    $('#infoTab')
        .empty()
        .html('<h4>' + node.name + '</h4>');
    $('a[href="#infoTab"]').tab('show');
    //alert(nodeId);
};

window.sidebar.doSearch = function () {
    var query = d3.select("#search-input").property('value');
    //now clear results and append search div:

    d3.select("#search-results").selectAll("*").remove();
    //append searching text:
    $("#search-results").html("<div class='loading_message'>" +
        "<p data-i18n='searching'></p>" +
        "<p><img src='images/preloader.gif'></img></p>" +
        "</div>");
    translations.translateAll();
    $("#search-follow").empty();
    //$("#search-help").empty();

    window.sidebar.dataProvider.search(query, function (error, data) {
        if (error) {
            $("#search-results").empty();
            $("#search-follow").html(
                "<div class='alert alert-danger'>" +
                "<span class='glyphicon glyphicon-remove'></span> &nbsp;" +
                "<span data-i18n='searchError'></span></div>");
            translations.translateAll();
            console.log(error);
        } else {
            window.sidebar.newSearchResults(data, query);
        }
    });
}


window.sidebar.newSearchResults = function (data, query) {
//    window.sidebar.updateLastSearch(query, data.nextCursorMark);
    d3.select("#search-results").selectAll("*").remove();
    //add the tocuments:
    console.log("Appending the data...");
    window.sidebar.appendSearchResultEntries(data.response.docs);
    //now set next link:

    window.sidebar.setHasMoreLabel(data.response.hasMoreResults);
}

window.sidebar.setHasMoreLabel = function (hasMore) {
    if (hasMore) {
        $("#search-follow").html(
            "<div class='alert alert-info'>" +
            "<span class='glyphicon glyphicon-info-sign'></span> &nbsp;" +
            "<span data-i18n='hasMoreResults'></span></div>");
    } else {
        $("#search-follow").html(
            "<div class='alert alert-success'>" +
            "<span class='glyphicon glyphicon-ok'></span> &nbsp;" +
            "<span data-i18n='noMoreResults'></span></div>");
    }
    translations.translateAll();
};


window.sidebar.appendSearchResultEntries = function (documents) {
    var oldData = d3.select("#search-results").selectAll("div")
        .data();
    var newData = oldData.concat(documents);
//    console.log();
    var diventer = d3.select("#search-results").selectAll("div")
        .data(newData)
        .enter().append("div");
    diventer.attr("class", function (d) {
        return "search-result " + d.type
    }).on('dragstart', window.sidebar.onDragDiv).attr("draggable", "true");
    diventer
        .on("click", function (d) {
            console.log("Clicked an entry to add: " + d.id);
            window.sidebar.graphController.addFavouriteNodes([d.id]);
        })
    diventer
        .append("div")
        .attr("draggable", "true")
        .html(function (d) {
                return "<div class='title'>" +
                    "<span class='glyphicon glyphicon-arrow-right pull-right'></span>" +
                    d.name +
                    "</div>";
            }
        );

};


window.sidebar.onDragDiv = function (d, i) {
    console.log("Dragging started.");
    d3.event.dataTransfer.setData("text", d.id);
};

function allowDrop(ev) {
    ev.preventDefault();
}


function drop(ev) {
    ev.preventDefault();
    console.log("Drop....");
//    var data = ev.dataTransfer.getData("text");
//    ev.target.appendChild(document.getElementById(data));
}
