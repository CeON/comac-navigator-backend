/**
 * Translations object.
 *
 * @author Michał Oniszczuk m.oniszczuk@icm.edu.pl
 */


translations = {
  selectedLanguage: getQueryStringValue("lang") || "en",

  getText: function (key) {
    return this.texts[key][this.selectedLanguage];
  },

  texts: {
    languageSelectorText: {
      en: "Zmień język interfejsu na polski.",
      pl: "Change interface language to english."
    },
    languageSelectorLink: {
      en: "pl",
      pl: "en"
    },
    searchButton: {
      en: "Search",
      pl: "Szukaj"
    },
    searchingMessage: {
      en: "Searching...",
      pl: "Szukam..."
    },
    searchErrorMessage: {
      en: "Unable to load data, server error.",
      pl: "Błąd serwera, nie można było załadować danych."
    },
    hasMoreResultsMessage: {
      en: "There are more results. Try to narrow query",
      pl: "Jest więcej wyników. Spróbuj zawęzić zapytanie"
    },
    sidebarHelpMessage: {
      en: "To add the desired result to the graph, " +
          "please drag and drop it on a canvas on the right.",
      pl: "Aby dodać żądany wynik do grafu, " +
          "przeciągnij go na obszar pracy po prawej."
    },
  },
};


