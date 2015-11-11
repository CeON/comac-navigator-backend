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
  $("#languageSelector").html(
      "<a href='?lang=" + translations.getText("languageSelectorLink") +
      "'> " + translations.getText("languageSelectorText") +
      "</a>"
      );
}

