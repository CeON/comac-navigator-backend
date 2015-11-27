/**
 * @fileOverview CopyToClipboardController class.
 *
 * @author Michał Oniszczuk <m.oniszczuk@icm.edu.pl>
 */

define("CopyToClipboardController", function (require) {
    var ZeroClipboard = require("lib/ZeroClipboard.min");

    /**
     * Creates an instance of CopyToClipboardController.
     *
     * @constructor
     * @this {CopyToClipboardController}
     */
    function CopyToClipboardController() {
        var zeroClipboard = new ZeroClipboard($("#copyToClipboardButton"));
        zeroClipboard.on('ready', function (event) {
            console.log('ZeroClipboard is loaded');

            zeroClipboard.on('copy', function (event) {
                event.clipboardData.setData('text/plain', $("#shareGraphInput").attr("value"));
            });

            zeroClipboard.on('aftercopy', function (event) {
                console.log('Copied text to clipboard: ' + event.data['text/plain']);
            });
        });

        zeroClipboard.on('error', function (event) {
            console.log('ZeroClipboard error of type "' + event.name + '": ' + event.message);
            ZeroClipboard.destroy();
        });
    }

    return CopyToClipboardController;
});
