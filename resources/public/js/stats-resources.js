statsModule.factory('eventsService', ['$resource', function ($resource) {
  return $resource('/v1/events', {},
    {
      'query': {isArray: true}
    });
}]);


