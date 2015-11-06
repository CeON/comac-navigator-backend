/**
 * Utility functions.
 *
 * @author Michał Oniszczuk m.oniszczuk@icm.edu.pl
 */


/**
 * Gets query string value for a given key.
 * 
 * @param key use this key to fetch a query string value
 * @returns query string value for a given key
 */
function getQueryStringValue(key) {
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
}

