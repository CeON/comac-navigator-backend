/**
 * @fileOverview LanguageSelectorController class.
 *
 * @author Michał Oniszczuk <m.oniszczuk@icm.edu.pl>
 */

/**
 * Creates an instance of LanguageSelectorController.
 *
 * @constructor
 * @this {LanguageSelectorController}
 */
function LanguageSelectorController() {
    $("#languageSelector").click(function () {
        translations.toggleLanguage();
    });
}

