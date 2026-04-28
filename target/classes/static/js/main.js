/* ============================================================
   BugTracker – Global JavaScript
   ============================================================ */

document.addEventListener('DOMContentLoaded', function () {

  /* Auto-dismiss flash alerts after 4 seconds */
  document.querySelectorAll('.alert-dismissible').forEach(function (alert) {
    setTimeout(function () {
      var bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
      bsAlert.close();
    }, 4000);
  });

  /* Activate current nav link based on path */
  var path = window.location.pathname;
  document.querySelectorAll('.navbar .nav-link').forEach(function (link) {
    var href = link.getAttribute('href');
    if (href && href !== '#' && path.startsWith(href) && href !== '/') {
      link.classList.add('active');
    }
  });

  /* Confirm delete forms */
  document.querySelectorAll('form[data-confirm]').forEach(function (form) {
    form.addEventListener('submit', function (e) {
      if (!confirm(form.dataset.confirm)) e.preventDefault();
    });
  });

  /* Tooltip init */
  document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function (el) {
    new bootstrap.Tooltip(el);
  });
});
