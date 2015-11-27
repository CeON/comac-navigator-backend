/**
 * @fileOverview ClearGraphController class.
 *
 * @author Michał Oniszczuk <m.oniszczuk@icm.edu.pl>
 */

/**
 * Creates an instance of ClearGraphController.
 *
 * @param {GraphController} graphController
 * @constructor
 * @this {ClearGraphController}
 */
function ClearGraphController(graphController) {
    $("#clearGraphConfirm").click(function () {
        graphController.clearGraph()
        console.log("Clear graph clicked.");
    });
}

