var underscore = angular.module('underscore', []);
underscore.factory('_', function () {
  return window._; // assumes underscore has already been loaded on the page
});


var wcigModule = angular.module("wcig", [
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

wcigModule
  .config(['$sceDelegateProvider', function ($sceDelegateProvider) {
    $sceDelegateProvider.resourceUrlWhitelist(
      ['self',
        'https://www.google.com/**']);
  }]);

wcigModule.config(['$httpProvider', function ($httpProvider) {
  delete $httpProvider.defaults.headers.common['X-Requested-With'];
}]);


wcigModule.config(['$httpProvider', function ($httpProvider) {
  $httpProvider.defaults.useXDomain = true;
  delete $httpProvider.defaults.headers.common['X-Requested-With'];
}]);







