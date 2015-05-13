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

}


window.sidebar.doSearch = function () {
    var query = d3.select("#search-input").property('value');
    window.sidebar.dataProvider.search(query, '*', function (error, data) {
        if (error) {
            console.log(error);
        } else {
            window.sidebar.newSearchResults(data, query);
        }
    });
}


window.sidebar.newSearchResults = function (data, query) {
    window.sidebar.updateLastSearch(query, data.nextCursorMark);
    d3.select("#search-results").selectAll("*").remove();
    //add the tocuments:
    console.log("Appending the data...");
    window.sidebar.appendSearchResultEntries(data.response.docs);
    //now set next link:

    window.sidebar.updateLoadMoreLink(data.response.docs.length<data.response.numFound);
}


window.sidebar.updateLoadMoreLink = function (hasMore) {
    if (hasMore) {
        d3.select("#search-follow").selectAll("*").remove();
        d3.select("#search-follow").
                append("a").
                text("Load more results");
        d3.select("#search-follow").
                on('click', window.sidebar.searchMore);

    } else {
        d3.select("#search-follow").selectAll("*").remove();
        d3.select("#search-follow").text("No more results.");
    }
}


window.sidebar.updateLastSearch = function (query, token) {
    var search = {
        token: token,
        query: query
    };
    d3.select("#search-results").data([search]);
}

window.sidebar.searchMore = function () {
    //identify query and the token
    var lastSearch = d3.select("#search-results").data()[0];
    console.log(lastSearch);
    window.sidebar.dataProvider.search(lastSearch.query, lastSearch.token, function (error, data) {
        window.sidebar.appendSearchResults(data);
    });
}

window.sidebar.appendSearchResults = function (data) {
    console.log("Trying to append search results...");
//    console.log(data);

    var results = data.response;
    var nextToken = data.nextCursorMark;
    var prevSearch = d3.select("#search-results").data()[0];
    window.sidebar.updateLastSearch(prevSearch.query, nextToken);
    docs = data.response.docs;
    var hasMore = true;
    console.log("Selection: "+d3.selectAll("#search-results div").each(function(d, i){console.log(d)}));
    console.log("Current size: "+d3.selectAll("#search-results div").size());
    console.log("New array size = "+docs.length);
    console.log("Num found="+results.numFound);
    if(d3.select("#search-results").selectAll("div").size()/2+docs.length==results.numFound || docs.length==0) {
        hasMore=false;
    }
        
    window.sidebar.updateLoadMoreLink(hasMore);
    if (results.nextCursorMark == prevSearch.token || docs.length == 0) {
        //no more results
        console.log("No more results to append...");
    } else {
        window.sidebar.appendSearchResultEntries(docs);

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


window.sidebar.onDragDiv=function(d, i) {
    console.log("Dragging started.");
    d3.event.dataTransfer.setData("text", d.id);
}

function allowDrop(ev) {
    ev.preventDefault();
}


function drop(ev) {
    ev.preventDefault();
    var data = ev.dataTransfer.getData("text");
    ev.target.appendChild(document.getElementById(data));
}