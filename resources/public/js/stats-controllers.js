statsModule.controller("StatsController", function ($scope, $log, $window,
                                                 $modal, $rootScope,
                                                 eventsService) {



  $scope.events = eventsService.query();


});
