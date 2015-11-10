/**
 * LanguageSelectorController class
 *
 * @author Michał Oniszczuk michal.oniszczuk@gmail.com
 */


function LanguageSelectorController() {
  $("#languageSelector").html(
      "<a href='?lang=" + translations.getText("languageSelectorLink") +
      "'> " + translations.getText("languageSelectorText") +
      "</a>"
      );
}

