wcigModule.factory('usersService', ['$resource', function ($resource) {
  return $resource('/v1/user', {},
    {
      'query': {isArray: false}
    });
}]);

wcigModule.factory('weekendsService', ['$resource', function ($resource) {
  return $resource('/v1/weekends', {},
    {
      'query': {isArray: true},
      params: {
        origins: '@origins'
      }
    });
}]);

wcigModule.factory('holidaysService', ['$resource', function ($resource) {
  return $resource('/v1/holidays', {},
    {
      'query': {isArray: true},
      params: {
        origins: '@origins',
        groups: '@groups'
      }
    });
}]);

wcigModule.factory('inspirationService', ['$resource', function ($resource) {
  return $resource('/v1/inspiration', {},
    {
      'query': {isArray: true},
      params: {
        code: '@code',
        minDate: '@minDate',
        maxDate: '@maxDate'
      }
    });
}]);


wcigModule.factory('airportsService', ['$resource', function ($resource) {
  return $resource('/v1/airport/:code', {},
    {
      'query': {
        isArray: false,
        params: {
          fields: '@code'
        }
      }
    });
}]);


wcigModule.factory('configurationService', ['$resource', function ($resource) {
  return $resource('/v1/config', {},
    {
      'query': {
        params: {
          cfg: '@cfg'
        },
        isArray: false
      }
    });
}]);

wcigModule.factory('destinationsService', ['$resource', function ($resource) {
  return $resource('/v1/destinations/:scope', {},
    {
      'query': {
        isArray: true,
        params: {
          groups: '@groups',
          origins: '@origins'
        }
      }
    });
}]);

wcigModule.factory('itinerariesService', ['$resource', function ($resource) {
  return $resource('/v1/itineraries/origin/:origin/group/:group/out/:outboundDate/in/:inboundDate?around=:around&dests=:dests',
    {},
    {
      'query': {
        isArray: true,
        params: {
          origin: '@origin',
          group: '@group',
          outboundDate: '@outboundDate',
          inboundDate: '@inboundDate',
          around: '@around',
          dests: '@dests'
        }
      }
    });
}]);

wcigModule.factory('slicesService', ['$resource', function ($resource) {
  return $resource('/v1/slices/origin/:origin/destination/:destination/group/:group/out/:outboundDate/in/:inboundDate',
    {},
    {
      'query': {
        isArray: true,
        params: {
          origin: '@origin',
          destination: '@destination',
          group: '@group',
          outboundDate: '@outboundDate',
          inboundDate: '@inboundDate'
        }
      }
    });
}]);

wcigModule.factory('flightsService', ['$resource', function ($resource) {
  return $resource('/v1/flights/:dir/:origin/group/:group/date/:date?around=:around&dests=:dests',
    {},
    {
      'query': {
        isArray: true,
        params: {
          origin: '@origin',
          dir: '@dir',
          group: '@group',
          date: '@date',
          around: '@around',
          dests: '@dests'
        }
      }
    });
}]);


wcigModule.factory('envService', ['$resource', function ($resource) {
  return $resource('/v1/env', {},
    {
      'query': {isArray: false}
    });
}]);

wcigModule.factory('weekendService', ['$resource', function ($resource) {
  return $resource('/v1/weekend/:origin/:outboundDate/:inboundDate', {},
    {
      'query': {
        isArray: true,
        params: {
          origin: '@origin',
          outboundDate: '@outboundDate',
          inboundDate: '@inboundDate'
        }
      }
    });
}]);

wcigModule.factory('evtService', ['$resource', function ($resource) {
  return $resource('/v1/evt', {},
    {
      'post': {
        method: "POST",
        isArray: false,
        params: {evt: '@evt'}
      }
    });
}]);

wcigModule.factory('eventsService', ['$resource', function ($resource) {
  return $resource('/v1/events', {},
    {
      'query': {isArray: true}
    });
}]);