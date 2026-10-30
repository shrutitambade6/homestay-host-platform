/*
 * Immersive colour themes: Forest, Ocean, Sunset, Dark.
 *
 * Loaded in <head> (not deferred) so the saved theme is set on <html> before
 * the page paints: no flash of the default colours. The choice is kept per
 * browser in localStorage; with nothing saved, the theme follows the
 * operating system's light/dark setting.
 *
 * The picker (fragments/layout :: themePicker) is a button plus a listbox:
 * mouse, touch and keyboard (arrows, Home/End, Enter/Space, Escape) all work.
 */
(function () {
  "use strict";
  var KEY = "homestay-theme";
  var THEMES = ["forest", "ocean", "sunset", "dark"];
  var NAMES = { forest: "Forest", ocean: "Ocean", sunset: "Sunset", dark: "Dark" };
  var root = document.documentElement;

  function saved() {
    try {
      var t = window.localStorage.getItem(KEY);
      return THEMES.indexOf(t) >= 0 ? t : null;
    } catch (e) {
      return null; // storage blocked (private window, strict settings)
    }
  }

  function current() {
    return root.getAttribute("data-theme") || "forest";
  }

  function apply(theme, animate) {
    if (animate && current() !== theme) {
      root.classList.remove("theme-swap");
      void root.offsetWidth; // restart the fade-in animation
      root.classList.add("theme-swap");
      window.setTimeout(function () { root.classList.remove("theme-swap"); }, 500);
    }
    root.setAttribute("data-theme", theme);
  }

  var prefersDark = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
  apply(saved() || (prefersDark ? "dark" : "forest"), false);

  function setupPicker(picker) {
    var button = picker.querySelector(".theme-btn");
    var menu = picker.querySelector(".theme-menu");
    var options = Array.prototype.slice.call(picker.querySelectorAll(".theme-option"));
    var thumb = picker.querySelector("[data-current-thumb]");
    var name = picker.querySelector("[data-current-name]");

    function sync() {
      var theme = current();
      options.forEach(function (o) {
        o.setAttribute("aria-selected", o.getAttribute("data-value") === theme ? "true" : "false");
      });
      thumb.className = "thumb thumb-" + theme;
      name.textContent = NAMES[theme];
    }

    function focusOption(i) {
      options.forEach(function (o) { o.classList.remove("is-active"); });
      var o = options[(i + options.length) % options.length];
      o.classList.add("is-active");
      o.focus();
    }

    function selectedIndex() {
      for (var i = 0; i < options.length; i++) {
        if (options[i].getAttribute("data-value") === current()) return i;
      }
      return 0;
    }

    function open() {
      menu.hidden = false;
      button.setAttribute("aria-expanded", "true");
      focusOption(selectedIndex());
    }

    function close(returnFocus) {
      if (menu.hidden) return;
      menu.hidden = true;
      button.setAttribute("aria-expanded", "false");
      if (returnFocus) button.focus();
    }

    function choose(option) {
      var theme = option.getAttribute("data-value");
      apply(theme, true);
      try { window.localStorage.setItem(KEY, theme); } catch (e) { /* not remembered */ }
      sync();
      close(true);
    }

    button.addEventListener("click", function () { menu.hidden ? open() : close(true); });
    button.addEventListener("keydown", function (e) {
      if (e.key === "ArrowDown" || e.key === "ArrowUp") { e.preventDefault(); open(); }
    });

    options.forEach(function (o, i) {
      o.addEventListener("click", function () { choose(o); });
      o.addEventListener("mousemove", function () { if (document.activeElement !== o) focusOption(i); });
      o.addEventListener("keydown", function (e) {
        switch (e.key) {
          case "ArrowDown": e.preventDefault(); focusOption(i + 1); break;
          case "ArrowUp": e.preventDefault(); focusOption(i - 1); break;
          case "Home": e.preventDefault(); focusOption(0); break;
          case "End": e.preventDefault(); focusOption(options.length - 1); break;
          case "Enter": case " ": e.preventDefault(); choose(o); break;
          case "Escape": e.preventDefault(); close(true); break;
          case "Tab": close(false); break;
        }
      });
    });

    document.addEventListener("click", function (e) { if (!picker.contains(e.target)) close(false); });
    sync();
  }

  document.addEventListener("DOMContentLoaded", function () {
    Array.prototype.forEach.call(document.querySelectorAll("[data-theme-picker]"), setupPicker);
  });
})();
