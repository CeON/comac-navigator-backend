/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


window.sidebar = {
};

window.sidebar.init = function () {
    console.log("Loading button...");

    $("#search-button").click(function (event) {
        event.preventDefault();
        console.log("Button pressed");
        window.sidebar.doSearch();
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
        console.log('droped '+id);
        window.sidebar.graphController.addFavouriteNodes([id]);
        d3.event.preventDefault();
    });

}


window.sidebar.doSearch = function () {
    var query = d3.select("#search-input").property('value');
    window.sidebar.dataProvider.search(query, function (error, data) {
        if (error) {
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
        d3.select("#search-follow").selectAll("*").remove();
        d3.select("#search-follow").
                append("span").
                text("There more results. Try to narrow query");

    } else {
        d3.select("#search-follow").selectAll("*").remove();
        d3.select("#search-follow").append("span").text("No more results.");
    }
}


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
    diventer.append("div")
            .text(function (d) {
                return d.name;
            }).classed("title", true).attr("draggable", "true");

}


window.sidebar.onDragDiv = function (d, i) {
    console.log("Dragging started.");
    d3.event.dataTransfer.setData("text", d.id);
}

function allowDrop(ev) {
    ev.preventDefault();
}


function drop(ev) {
    ev.preventDefault();
    console.log("Drop....");
//    var data = ev.dataTransfer.getData("text");
//    ev.target.appendChild(document.getElementById(data));
}