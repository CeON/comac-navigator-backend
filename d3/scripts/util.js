/**
 * @fileOverview Utility functions object.
 *
 * @author Michał Oniszczuk <m.oniszczuk@icm.edu.pl>
 */

/**
 * Utility functions.
 *
 * @namespace
 */
var util = {
    /**
     * Gets query string value for a given key.
     *
     * @param {string} key use this key to fetch a query string value
     * @returns {string} query string value for a given key
     */
    getQueryStringValue: function (key) {
        var value = null;
        var url = window.location.search.substr(1);
        keyValues = url.split(/[\?&]+/);
        for (i = 0; i < keyValues.length; i++) {
            keyValue = keyValues[i].split("=");
            if (keyValue[0] == key) {
                value = keyValue[1];
            }
        }
        return value;
    },
};

