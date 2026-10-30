/*
 * Colour theme switcher. Loaded in <head> (not deferred) so the saved theme is
 * applied before the page paints: no flash of the default colours.
 * The choice is stored per browser in localStorage; with nothing saved the
 * theme follows the system light/dark setting.
 */
(function () {
  var KEY = "homestay-theme";
  var THEMES = ["forest", "ocean", "sunset", "dark"];

  function saved() {
    try {
      var t = window.localStorage.getItem(KEY);
      return THEMES.indexOf(t) >= 0 ? t : null;
    } catch (e) {
      return null; // storage blocked (private mode etc.)
    }
  }

  function apply(theme) {
    document.documentElement.setAttribute("data-theme", theme);
  }

  var prefersDark = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
  apply(saved() || (prefersDark ? "dark" : "forest"));

  document.addEventListener("DOMContentLoaded", function () {
    var current = document.documentElement.getAttribute("data-theme");
    document.querySelectorAll("select.theme-select").forEach(function (select) {
      select.value = current;
      select.addEventListener("change", function () {
        apply(select.value);
        try { window.localStorage.setItem(KEY, select.value); } catch (e) { /* not saved */ }
      });
    });
  });
})();
