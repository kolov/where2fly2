var underscore = angular.module('underscore', []);
underscore.factory('_', function () {
  return window._; // assumes underscore has already been loaded on the page
});


var statsModule = angular.module("stats", [
  'ngResource',
  'ui.bootstrap',
  'ui.bootstrap.modal',
  'ngSanitize',
  'angularLoad',
  'underscore',
  'ui.slider',
  'smart-table',
  "angular-click-outside",
  'ngMap']);

statsModule
  .config(['$sceDelegateProvider', function ($sceDelegateProvider) {
    $sceDelegateProvider.resourceUrlWhitelist(
      ['self',
        'https://www.google.com/**']);
  }]);

statsModule.config(['$httpProvider', function ($httpProvider) {
  delete $httpProvider.defaults.headers.common['X-Requested-With'];
}]);


statsModule.config(['$httpProvider', function ($httpProvider) {
  $httpProvider.defaults.useXDomain = true;
  delete $httpProvider.defaults.headers.common['X-Requested-With'];
}]);







