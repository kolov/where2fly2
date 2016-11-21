wcigModule.controller("AppController", function ($scope, $log, $window, $q,
                                                 $modal, $rootScope,
                                                 destinationsService,
                                                 itinerariesService,
                                                 slicesService,
                                                 weekendService,
                                                 airportsService,
                                                 weekendsService,
                                                 holidaysService,
                                                 configurationService,
                                                 flightsService,
                                                 inspirationService,
                                                 evtService,
                                                 eventsService) {


    var WEEKEND_OUTBOUND_DAY_OF_WEEK = 5;
    var WEEKEND_INBOUND_DAY_OF_WEEK = 7;
    // user input

    $scope.tableData = [];

    $scope.availableWeekends = []; //{in, out}

    $scope.mapCenter = [49, 8];
    $scope.mapZoom = 4;
    $scope.outboundRange = [0, 24];
    $scope.inboundRange = [0, 24];
    $scope.maxFlightDuration = 5;
    $scope.maxFlightPrice = 400;
    $scope.priceLow = 80;
    $scope.priceHigh = 450;

    $scope.inspirations = [];

    $scope.weekendTabActive = true;

    function isMobileClient() {
      var check = false;
      (function (a) {
        if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0, 4)))check = true
      })(navigator.userAgent || navigator.vendor || window.opera);
      return check;
    }

    function updateConfig(tab) {
      if ($scope.configuration) {

        $scope.period = tab;
        $scope.mapCenter = $scope.configuration[tab + '-map-center'];
        $scope.mapZoom = $scope.configuration[tab + '-map-zoom'];

        $scope.flightDurationSlider.options.min = $scope.configuration[tab + '-duration-min'];
        $scope.flightDurationSlider.options.max = $scope.configuration[tab + '-duration-max'];
        $scope.flightPriceSlider.options.min = $scope.configuration[tab + '-price-min'];
        $scope.flightPriceSlider.options.max = $scope.configuration[tab + '-price-max'];
        $scope.maxFlightDuration = $scope.configuration[tab + '-duration'];
        $scope.maxFlightPrice = $scope.configuration[tab + '-price'];
        $scope.outboundRange = $scope.configuration[tab + '-out-range'];
        $scope.inboundRange = $scope.configuration[tab + '-in-range'];
        $scope.priceLow = $scope.configuration[tab + '-price-color-low'];
        $scope.priceHigh = $scope.configuration[tab + '-price-color-high'];

        $scope.outDepartureTimeSlider.options.min = $scope.configuration[tab + '-out-departure-min'];
        $scope.outDepartureTimeSlider.options.max = $scope.configuration[tab + '-out-departure-max'];
        $scope.inDepartureTimeSlider.options.min = $scope.configuration[tab + '-in-departure-min'];
        $scope.inDepartureTimeSlider.options.max = $scope.configuration[tab + '-in-departure-max'];

        $scope.updateMinPrices();
      }


    }


// state
    $scope.knownAirports = {};


    $scope.allItineraries = {};
    $scope.allFlights = [];
    $scope.fetchedItineraryIds = {};
    $scope.fetchedFlightIds = {};
    $scope.fetchedSliceIds = {};

    $scope.currentItineraries = {};


    $scope.countOpenRequests = 0;

    $scope.weekendMinPrices = {};

    $scope.colorScaleReverse = colorScale.slice(0).reverse();

    $scope.holidays = [];
    $scope.selectedHoliday;

    $scope.destinations = {};
    var WEEKEND = 'weekend';
    var HOLIDAY = 'holiday';
    $scope.period = WEEKEND;


    $scope.spinnerOpts = {
      lines: 13 // The number of lines to draw
      , length: 56 // The length of each line
      , width: 11 // The line thickness
      , radius: 71 // The radius of the inner circle
      , scale: 1.25 // Scales overall size of the spinner
      , corners: 1 // Corner roundness (0..1)
      , color: '#000' // #rgb or #rrggbb or array of colors
      , opacity: 0.25 // Opacity of the lines
      , rotate: 0 // The rotation offset
      , direction: 1 // 1: clockwise, -1: counterclockwise
      , speed: 1 // Rounds per second
      , trail: 60 // Afterglow percentage
      , fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
      , zIndex: 2e9 // The z-index (defaults to 2000000000)
      , className: 'spinner' // The CSS class to assign to the spinner
      , top: '50%' // Top position relative to parent
      , left: '50%' // Left position relative to parent
      , shadow: false // Whether to render a shadow
      , hwaccel: false // Whether to use hardware acceleration
      , position: 'absolute' // Element positioning
    };
    $scope.spinnerTarget = document.getElementById('spinner');

    $scope.stopSpinner = function () {
      $log.debug("Stopping spinner");
      $scope.spinner.stop();
      $scope.spinner = null;
    };

    $scope.startSpinner = function () {
      $log.debug("Starting spinner");
      if (!$scope.spinner) {
        $scope.spinner = new Spinner($scope.spinnerOpts).spin($scope.spinnerTarget);
      }
    };


// get configuration
    $scope.getConfiguration = function () {
      var cfg = location.search.split('cfg=')[1];
      return configurationService.query({cfg: cfg}).$promise.then(function (data) {
        $scope.configuration = data;
        $scope.selectedSite = _.chain($scope.configuration.applications)
          .filter(function (a) {
            return a.selected;
          })
          .first()
          .value();
      });
    };

// get origin and destination airports
    $scope.getAirports = function () {
      var promises = [];
      // get originairports
      _.each($scope.configuration.origins, function (origin) {
        var p = airportsService.query({code: origin.code}).$promise;
        promises.push(p);
        p.then(function (airport) {
          $scope.knownAirports[airport.code] = airport;
        });
      });
      //get desitantion airports
      _.each($scope.configuration.groups, function (group) {
        _.each($scope.configuration.origins, function (origin) {
          $scope.destinations[origin.code] = [];
          var p = destinationsService.query({groups: group, origins: origin.code}).$promise;
          promises.push(p);
          p.then(function (data) {
            _.each(data, function (airport) {
              $scope.knownAirports[airport.code] = airport;
              $scope.destinations[origin.code].push(airport.code);
            });
          });
        });
      });
      return $q.all(promises);
    };


    $scope.weekendsTabPresent = function () {
      return $scope.configuration &&
        $scope.configuration.periods &&
        _.contains($scope.configuration.periods, 'weekend');
    };

    $scope.getDates = function () {
      var promises = [];
      // query & show available dates
      var p = holidaysService.query({groups: $scope.configuration.groups}).$promise;
      p.then(function (data) {
        _.each(data, function (d) {
          var ix = 1;
          $scope.holidays.push(
            {
              selected: false,
              in: moment.utc(d['end-date'], 'YYYY-MM-DD'),
              out: moment.utc(d['start-date'], 'YYYY-MM-DD'),
              name: d.name,
              id: ix++
            }
          );
          if ($scope.holidays.length) {
            $scope.selectedHoliday = $scope.holidays[0];
          }
        });
      });

      promises.push(p);

      var p = weekendsService.query({origins: 'AMS'}).$promise;
      p.then(function (data) {
          var ix = 1;
          _.each(data, function (d) {
            $scope.availableWeekends.push(
              {
                selected: false,
                in: moment.utc(d.in, 'YYYY-MM-DD'),
                out: moment.utc(d.out, 'YYYY-MM-DD'),
                id: ix++
              }
            );
          });
          if ($scope.availableWeekends.length > 0) {
            $scope.availableWeekends[0].selected = true;
          }
        }
      );
      promises.push(p);

      return $q.all(promises);
    };


    $scope.fetchItinerariesSelectedWeekends = function () {
      $log.debug('fetchItinerariesSelectedDates called');
      var promises = [];
      _.each($scope.availableWeekends, function (weekend) {
        if (weekend.selected) {
          var outboundDate = moment.utc(weekend['out']).format("YYYY-MM-DD");
          var inboundDate = moment.utc(weekend['in']).format("YYYY-MM-DD");
          promises.push($scope.fetchItinerariesP(outboundDate, inboundDate, $scope.configuration['weekend-around']));
        }
      });
      return $q.all(promises);
    };

    $scope.weekendSelectionChanged = function (ix) {
      if ($scope.availableWeekends[ix].selected) {
        $log.debug('Adding ' + $scope.availableWeekends[ix].id);

        var outboundDate = moment.utc($scope.availableWeekends[ix]['out']).format("YYYY-MM-DD");
        var inboundDate = moment.utc($scope.availableWeekends[ix]['in']).format("YYYY-MM-DD");
        $scope.fetchItinerariesP(outboundDate, inboundDate, $scope.configuration['weekend-around'])
          .then($scope.updateMinPrices);
      } else {
        $scope.updateMinPrices();
      }
    };


    $scope.originSelectionChanged = function (ix) {
      var promises = [];
      if ($scope.configuration.origins[ix].active) {
        _.each($scope.availableWeekends, function (weekend) {
          if (weekend.selected) {
            var outboundDate = moment.utc(weekend['out']).format("YYYY-MM-DD");
            var inboundDate = moment.utc(weekend['in']).format("YYYY-MM-DD");
            promises.push($scope.fetchItinerariesP(outboundDate, inboundDate, $scope.configuration['weekend-around']));
          }
        });
      }
      $q.all(promises).then($scope.updateMinPrices);
    };


    $scope.deccountOpenRequests = function () {
      $scope.countOpenRequests--;
      if ($scope.countOpenRequests == 0) {
        $scope.stopSpinner();
      }
    };

    $scope.inccountOpenRequests = function () {
      $scope.countOpenRequests++;
      $log.debug('Open request: ' + $scope.countOpenRequests);
      if ($scope.countOpenRequests == 1) {
        $scope.startSpinner();
      }
    };

    $scope.fetchItinerariesSelectedHoliday = function () {
      var outboundDate = moment.utc($scope.selectedHoliday['out']).format("YYYY-MM-DD");
      var inboundDate = moment.utc($scope.selectedHoliday['in']).format("YYYY-MM-DD");
      $scope.fetchItinerariesP(outboundDate, inboundDate, $scope.configuration['holiday-around'])
        .then($scope.updateMinPrices);
    };

    $scope.holidaySelectionChanged = function (ix) {
      if ($scope.holidays && $scope.holidays.length > ix) {
        $scope.selectedHoliday = $scope.holidays[ix];
        $scope.fetchItinerariesSelectedHoliday();
      }
    };

    $scope.fetchItinerariesSelectedDates = function () {
      if ($scope.period == WEEKEND) {
        return $scope.fetchItinerariesSelectedWeekends();
      } else if ($scope.period == HOLIDAY) {
        return $scope.fetchItinerariesSelectedHoliday();
      }
    };

    $scope.fetchItinerariesP = function (outboundDate, inboundDate, around) {
      var promises = [];
      _.each($scope.configuration.origins, function (origin) {
        if (origin.active) {
          _.each($scope.configuration.groups, function (group) {
            if (group == 'transavia') {
              promises.push($scope.fetchTransaviaP(origin.code, outboundDate, inboundDate, around));
            } else if (group == 'holiday') {
              promises.push($scope.fetchQpx(origin.code, outboundDate, inboundDate, around));
            }
          });
        }
      });
      return $q.all(promises);
    };

    $scope.fetchQpx = function (origin, outboundDate, inboundDate, around) {

      var id = '' + outboundDate + '/' + inboundDate;
      if (!$scope.fetchedItineraryIds[id]) {

        $scope.inccountOpenRequests();

        var p = itinerariesService.query({
          origin: origin,
          group: 'qpx',
          outboundDate: outboundDate,
          inboundDate: inboundDate,
          around: around
        }).$promise;
        p.then(function (dailyOptions) {
          $scope.fetchedItineraryIds[id] = true;
          if (dailyOptions.length) {
            _.each(dailyOptions, function (dailyOption) {
              _.each(dailyOption.itineraries, function (itinerary) {
                $scope.addItinerary(dailyOption, itinerary);
              });
            });
          } else {
            $log.debug('No itineraries for ' + outboundDate + '/' + inboundDate);
          }
          $scope.deccountOpenRequests();

        }, function (err) {
          $log.debug('Error: ' + err);
          $scope.deccountOpenRequests();
        });
        return p;
      } else {
        return $q.all([]);
      }
    };

    $scope.fetchTransaviaP = function (origin, outboundDate, inboundDate, around) {

      $log.debug('fetchTransavia called');
      var promises = [];
      var flightsId = 'from/' + origin + '/' + outboundDate;
      if (!$scope.fetchedFlightIds[flightsId]) {

        $scope.inccountOpenRequests();

        var p = flightsService.query({
          origin: origin,
          dir: 'from',
          date: outboundDate,
          group: 'transavia',
          around: around
        }).$promise;
        p.then(function (flights) {
          $scope.fetchedFlightIds[flightsId] = true;
          if (flights.length) {
            _.each(flights, function (flightsOnDate) {
              $scope.addFlights(flightsOnDate);
            });
          } else {
            $log.debug('No flights for ' + outboundDate);
          }
          $scope.deccountOpenRequests();

        });
        p.catch(function (err) {
          $log.debug('Error: ' + err);
          $scope.deccountOpenRequests();
        });
        promises.push(p);
      }

      var idTo = 'to/' + origin + '/' + inboundDate;
      if (!$scope.fetchedFlightIds[flightsId]) {

        $scope.inccountOpenRequests();

        var p = flightsService.query({
          origin: origin,
          dir: 'to',
          date: inboundDate,
          group: 'transavia',
          around: around
        }).$promise;
        p.then(function (flights) {
          $log.debug('fetchTransavia response');
          $scope.fetchedFlightIds[idTo] = true;
          if (flights.length) {
            _.each(flights, function (flightsOnDate) {
              $scope.addFlights(flightsOnDate);
            });
          } else {
            $log.debug('No flights to ' + origin + '/' + inboundDate);
          }
          $scope.deccountOpenRequests();
        });
        p.catch(function (err) {
          $log.debug('Error: ' + err);
          $scope.deccountOpenRequests();
        });
        promises.push(p);
      }

      return $q.all(promises);

    };


    $scope.addItinerary = function (dailyOption, itinerary) {
      if (!$scope.allItineraries[dailyOption.origin]) {
        $scope.allItineraries[dailyOption.origin] = {};
      }
      var coll = $scope.allItineraries[dailyOption.origin];
      if (!coll[dailyOption.destination]) {
        coll[dailyOption.destination] = {};
      }
      coll = coll[dailyOption.destination];
      if (!coll[dailyOption.outDate]) {
        coll[dailyOption.outDate] = {};
      }
      coll = coll[dailyOption.outDate];
      if (!coll[dailyOption.inDate]) {
        coll[dailyOption.inDate] = [];
      }
      coll = coll[dailyOption.inDate];

      itinerary.outDepartureDateTime = moment.utc(itinerary.outDepartureDateTime);
      itinerary.outArrivalDateTime = moment.utc(itinerary.outArrivalDateTime);
      itinerary.inDepartureDateTime = moment.utc(itinerary.inDepartureDateTime);
      itinerary.inArrivalDateTime = moment.utc(itinerary.inArrivalDateTime);

      coll.push(itinerary);
    };

    $scope.addFlights = function (flightsOnDate) {
      _.each(flightsOnDate.flights, function (flight) {
        flight.departureDateTime = moment.utc(flight.departureDateTime);
        flight.arrivalDateTime = moment.utc(flight.arrivalDateTime);
        flight.origin = flightsOnDate.origin;
        flight.destination = flightsOnDate.destination;
        $scope.allFlights.push(flight);
      });
    };

    $scope.airportMarkers = [];
    $scope.itineraryMarkers = [];
// map data
    $scope.mapOptions = {
      scrollwheel: false
    };


    $scope.dateOptions = {
      formatYear: 'yy',
      startingDay: 1
    };

    $scope.currencyFormatting = function (value) {
      return value.toString() + " EUR"
    };


    var ua = navigator.userAgent;

    $scope.showBrowserWarning = (-1 == ua.indexOf('Chrome'))
      && (-1 == ua.indexOf('Firefox'))
      && (-1 == ua.indexOf('Safari'))
    ;


    $scope.$on('mapInitialized', function (event, map) {
      console.log("Map initialized");
      google.maps.event.addListener(map, 'click',
        function (ev) {
          $log.debug("clicked " + ev);
        });
    });

    function itineraryTimeMatchesUserSelection(it) {
      if ($scope.period == WEEKEND) {
        var outTimeMatches = _.some($scope.availableWeekends, function (weekend) {
          return weekend.selected
            && timeBetween(it.outDepartureDateTime, weekend.out, $scope.outboundRange);
        });
        return outTimeMatches && _.some($scope.availableWeekends, function (weekend) {
            return weekend.selected
              && timeBetween(it.inDepartureDateTime, weekend.in, $scope.inboundRange);
          });
      } else if ($scope.period == HOLIDAY) {
        return timeBetween(it.outDepartureDateTime, $scope.selectedHoliday.out, $scope.outboundRange)
          && timeBetween(it.inDepartureDateTime, $scope.selectedHoliday.in, $scope.inboundRange);
      } else throw "error";
    }


    function outFlightTimeMatchesUserSelection(flight) {

      if ($scope.period == WEEKEND) {
        return _.some($scope.availableWeekends, function (weekend) {
          var result = weekend.selected
            && timeBetween(flight.departureDateTime, weekend.out, $scope.outboundRange);
          return result;
        });
      } else if ($scope.period == HOLIDAY) {
        return $scope.selectedHoliday
          && timeBetween(flight.departureDateTime, $scope.selectedHoliday.out, $scope.outboundRange);
      } else throw "error";
    }

    function inFlightTimeMatchesUserSelection(flight) {
      if ($scope.period == WEEKEND) {
        return _.some($scope.availableWeekends, function (weekend) {
          var result = weekend.selected
            && timeBetween(flight.arrivalDateTime, weekend.in, $scope.inboundRange);
          return result
        });
      } else if ($scope.period == HOLIDAY) {
        return $scope.selectedHoliday
          && timeBetween(flight.arrivalDateTime, $scope.selectedHoliday.in, $scope.inboundRange);
      } else throw "error";
    }

    function selectFlightsOut(dest) {
      return _.filter($scope.allFlights, function (flight) {
        return _.some($scope.configuration.origins, function (o) {
            return o.active && o.code == flight.origin;
          })
          && flight.destination == dest
          && outFlightTimeMatchesUserSelection(flight)
          && $scope.maxFlightDuration * 60 > flight.duration;
      });
    }

    function selectFlightsIn(dest) {
      return _.filter($scope.allFlights, function (flight) {
        return flight.origin == dest
          && _.some($scope.configuration.origins, function (o) {
            return o.active && o.code == flight.destination;
          })
          && inFlightTimeMatchesUserSelection(flight)
          && $scope.maxFlightDuration * 60 > flight.duration;
      });
    }

    $scope.selectFromFlights = function () {

      var dests = [];

      _.each($scope.configuration.origins, function (origin) {
        if (origin.active) {
          _.each($scope.allFlights, function (flight) {
            dests.push(flight.destination);
          });
        }
      });

      dests = _.uniq(dests);


      var result = [];

      _.each(dests, function (dest) {
          var flightsOut = selectFlightsOut(dest);
          var flightsIn = selectFlightsIn(dest);


          if (flightsOut.length > 0 && flightsIn.length > 0) {

            // add one result for each destination
            var flightPrice = _.min(_.map(flightsIn, function (f) {
                return f.price;
              }))
              + _.min(_.map(flightsOut, function (f) {
                return f.price;
              }));
            
            if ($scope.maxFlightPrice >= flightPrice) {
              result.push({
                  minPrice: flightPrice,
                  destination: dest
                }
              );
            }
          }
        }
      )
      ;

      return result;
    };

    $scope.selectItinerariesToDestQpx = function (dest) {
      var durationInMinutes = $scope.maxFlightDuration * 60;
      var itinerariesToDest = [];
      _.each(_.keys($scope.allItineraries), function (origin) {
        if (_.some($scope.configuration.origins, function (o) {
            return o.code == origin;
          })) {
          _.each(_.keys($scope.allItineraries[origin][dest]), function (outdate) {
            _.each(_.keys($scope.allItineraries[origin][dest][outdate]), function (indate) {
              _.each($scope.allItineraries[origin][dest][outdate][indate], function (it) {
                if (durationInMinutes >= it.outDuration
                  && durationInMinutes >= it.inDuration
                  && itineraryTimeMatchesUserSelection(it)
                  && $scope.maxFlightPrice >= it.price) {
                  itinerariesToDest.push(it);
                }
              });
            });
          });
        }
      });
      return itinerariesToDest;
    };


    $scope.selectItinerariesQpx = function () {
      var dests = [];
      _.each(_.keys($scope.allItineraries), function (o) {
        if (_.some($scope.configuration.origins, function (origin) {
            return origin.active && (origin.code == o);
          })) {
          _.each(_.keys($scope.allItineraries[o]), function (dest) {
            dests.push(dest);
          });
        }
      });

      var result = [];

      _.each(_.uniq(dests), function (dest) {
        var itinerariesToDest = $scope.selectItinerariesToDestQpx(dest);
        if (itinerariesToDest.length) {
          itinerariesToDest = _.sortBy(itinerariesToDest, function (it) {
            return it.price;
          });
          // add one result for each destination
          result.push({
            minPrice: itinerariesToDest[0].price,
            currency: itinerariesToDest[0].currency,
            originAirport: itinerariesToDest[0].originAirport,
            destination: dest,
            itineraries: itinerariesToDest
          });
        }
      });
      return result;
    };

    function airportName(code) {
      if ($scope.knownAirports[code]) {
        return $scope.knownAirports[code].long_name;
      } else {
        return code;
      }
    }


    function fetchSlicesQpxP(dest) {
      var promises = [];
      _.each($scope.configuration.origins, function (originData) {
        var origin = originData.code;
        if ($scope.allItineraries[origin]
          && $scope.allItineraries[origin][dest]) {
          _.each(_.keys($scope.allItineraries[origin][dest]), function (outDate) {
            _.each(_.keys($scope.allItineraries[origin][dest][outDate]), function (inDate) {
              var itineraryId = origin + '-' + dest + '-' + outDate + '-' + inDate;
              if (!$scope.fetchedSliceIds[itineraryId]) {
                var its = $scope.allItineraries[origin][dest][outDate][inDate];
                var p = slicesService.query({
                  origin: origin,
                  destination: dest,
                  outboundDate: outDate,
                  group: 'qpx',
                  inboundDate: inDate
                }).$promise;
                p.then(function (data) {
                  for (var i = 0; i < data.length; i++) {
                    its[i].slices = data[i];
                  }
                  $scope.fetchedSliceIds[itineraryId] = true;
                });
                promises.push(p);
              }
            });
          });
        }

      });
      return $q.all(promises);
    }

    function createItinerariesTransavia(dest) {

      var result = [];
      var flightsOut = selectFlightsOut(dest);
      var flightsIn = selectFlightsIn(dest);
      _.each(flightsOut, function (flightOut) {
        _.each(flightsIn, function (flightIn) {
          var slices = [
            [{
              carrier: flightOut.carrier,
              number: flightOut.number,
              origin: flightOut.origin,
              destination: flightOut.destination,
              departureTime: flightOut.departureDateTime,
              arrivalTime: flightOut.arrivalDateTime,
              duration: flightOut.duration
            }],
            [{
              carrier: flightIn.carrier,
              number: flightIn.number,
              origin: flightIn.origin,
              destination: flightIn.destination,
              departureTime: flightIn.departureDateTime,
              arrivalTime: flightIn.arrivalDateTime,
              duration: flightIn.duration
            }]
          ];
          result.push({
            group: 'transavia',
            price: flightOut.price + flightIn.price,
            outDepartureDateTime: flightOut.departureDateTime,
            outArrivalDateTime: flightOut.arrivalDateTime,
            outDuration: flightOut.duration,
            inDepartureDateTime: flightIn.departureDateTime,
            inArrivalDateTime: flightIn.arrivalDateTime,
            inDuration: flightIn.duration,
            slices: slices
          });
        });
      });
      return result;
    }


    function createTableRows(itineraries) {
      var result = [];
      _.each(itineraries, function (it) {
        result.push({
          isSummary: true,
          group: it.group,
          price: it.price,
          outDepartureDateTime: it.outDepartureDateTime,
          outArrivalDateTime: it.outArrivalDateTime,
          outDuration: it.outDuration,
          inDepartureDateTime: it.inDepartureDateTime,
          inArrivalDateTime: it.inArrivalDateTime,
          inDuration: it.inDuration,
          slice: it.slices,
          origin: it.slices[0][0].origin,
          destination: it.slices[1][0].origin
        })
        ;
        result.push({
          isSummary: false,
          slice: it.slices
        });
      });
      return result;

    }


    $scope.clearItineraryMarkers = function () {
      _.each($scope.itineraryMarkers, function (circle) {
        circle.setMap(null);
      });
      $(".map-marker-label").remove();
      $scope.itineraryMarkers = [];
    };

    $scope.createInitialTableRowsP = function (dest) {
      var transaviaItineraries = createItinerariesTransavia(dest);
      return fetchSlicesQpxP(dest)
        .then(function () {
          var qpxItineraries = $scope.selectItinerariesToDestQpx(dest);
          var itineraries = transaviaItineraries.concat(qpxItineraries);
          $scope.currentItineraries = {
            destination: dest,
            itineraries: itineraries
            // rows: createTableRows(itineraries) will be created by sort
          };
          $scope.sortingOrder = 'down';
          $scope.flightsSortedBy = 'price';
          $scope.sortFlightsBy('price');
        });
    };

    $scope.updateMinPrices = function () {

      $log.debug('updateMinPrices called');
      $scope.uevt({
        action: 'itineraries',
        outboundRange: $scope.outboundRange,
        inboundRange: $scope.inboundRange,
        maxFlightDuration: $scope.maxFlightDuration,
        maxFlightPrice: $scope.maxFlightPrice
      });

      $scope.clearItineraryMarkers();


      var pricesPerDestination = $scope.selectFromFlights().concat($scope.selectItinerariesQpx());

      _.each(pricesPerDestination, function (destinationPriceInfo) {
        var priceColor = calculatePriceColor(destinationPriceInfo.minPrice, $scope.priceLow, $scope.priceHigh);
        var dest = $scope.knownAirports[destinationPriceInfo.destination];

        if (dest && dest.location && dest.location.lat) {
          var itineraryMarker = new google.maps.Marker({

            position: new google.maps.LatLng(dest.location.lat, dest.location.lng),
            map: $scope.map,
            icon: {
              path: google.maps.SymbolPath.CIRCLE,
              fillOpacity: 0.5,
              fillColor: priceColor,
              strokeOpacity: 1.0,
              strokeColor: '#000000',
              strokeWeight: 0.6,

              scale: 12 //pixels
            },
            label: destinationPriceInfo.minPrice + '&euro;',
            shape: airportClickShape,
            zIndex: 2,
            cursor: 'hand'
          });
          $scope.itineraryMarkers.push(itineraryMarker);
          var infowindow;
          google.maps.event.addListener(itineraryMarker, 'mouseover',
            function (ev) {
              infowindow = new google.maps.InfoWindow({
                content: 'Click on the marker to see all flights to ' + airportName(dest.code),
                map: $scope.map,
                position: new google.maps.LatLng(dest.location.lat, dest.location.lng)
              });
              infowindow.open($scope.map, this);
            });
          google.maps.event.addListener(itineraryMarker, 'mouseout',
            function (ev) {
              infowindow.close();
            });
          google.maps.event.addListener(itineraryMarker, 'click',
            function (ev) {
              infowindow.close();
              $scope.updateInspirations(dest.code);
              $scope.uevt({action: 'dest.view', 'dest': dest.code});
              fetchSlicesQpxP(dest.code).then(function () {
                $scope.createInitialTableRowsP(dest.code)
                  .then(function () {


                    var x = ev.pixel.x;
                    var y = ev.pixel.y;
                    if (x > 100) {
                      x = 100;
                    }
                    if (x < 20) {
                      x = 20;
                    }
                    if (y > 100) {
                      y = 100;
                    }
                    if (y < 100) {
                      y = 100;
                    }
                    $('#itinerariesInfo').css({
                      top: y,
                      left: x, 'z-index': 99, visibility: 'visible'
                    })
                  });
              });
            });
        } // EACH pricePerDestination
      });

      $scope.expand = function (ix, destcode, it) {
        var st = !!!$scope.currentItineraries.rows[ix + 1].opened;
        $scope.uevt({action: st ? 'open' : 'close', 'dest': destcode, 'it-id': it.id});
        $scope.currentItineraries.rows[ix].opened = st;
        $scope.currentItineraries.rows[ix + 1].opened = st;
      };
    };


    $scope.expand = function (ix, destcode, it) {
      var st = !!!$scope.currentItineraries.rows[ix + 1].opened;
      $scope.uevt({action: st ? 'open' : 'close', 'dest': destcode, 'it-id': it.id});
      $scope.currentItineraries.rows[ix].opened = st;
      $scope.currentItineraries.rows[ix + 1].opened = st;
    };


    $scope.clearMarkers = function () {
      _.each($scope.airportMarkers, function (m) {
        m.setMap(null);
      });
      $(".map-marker-label").remove();
      $scope.airportMarkers = [];
    };

    $scope.updateOriginsAndDestinations = function () {

      $scope.clearMarkers();

      _.each($scope.configuration.origins, function (origin) {
        if ($scope.knownAirports[origin.code]) {
          var o = $scope.knownAirports[origin.code];
          var airportMarker = new google.maps.Marker({
            position: new google.maps.LatLng(o.location.lat, o.location.lng),
            map: $scope.map,
            icon: goldStar,
            //label: airport.code,
            shape: airportClickShape,
            zIndex: 2
          });

          $scope.airportMarkers.push(airportMarker);
        }
      });


      _.each($scope.configuration.origins, function (origin) {
          var dests = $scope.destinations[origin.code];
          _.each(dests, function (destcode) {
            var dest = $scope.knownAirports[destcode];
            if (!dest || !dest.location || !dest.location.lat) {
              console.log("error: bad destination " + dest.code);
              return;
            }

            var destMarker = new google.maps.Marker({
              position: new google.maps.LatLng(dest.location.lat, dest.location.lng),
              map: $scope.map,
              icon: imageDest,
              //label: airport.code,
              shape: airportClickShape,
              zIndex: 2
            });
            $scope.airportMarkers.push(destMarker);
          });
        }
      );
    };


    function getTimeFrame() {
      if ($scope.period === WEEKEND) {
        var selected = _.filter($scope.availableWeekends, function (w) {
          return w.selected;
        });
        if (!selected || selected.length == 0) {
          selected = $scope.availableWeekends;
        }
        var first = selected[0];
        var last = selected[selected.length - 1];
        return {
          minDate: moment(first.out).subtract(1, 'y').format('x'),
          maxDate: moment(last.in).subtract(1, 'y').format('x')
        };
      } else {
        return {
          minDate: moment($scope.selectedHoliday.out).format('x'),
          maxDate: moment($scope.selectedHoliday.in).format('x')
        };
      }
    }

    $scope.updateInspirations = function (code) {
      var timeFrame = getTimeFrame();
      var params = {minDate: timeFrame.minDate, maxDate: timeFrame.maxDate};
      if (code) {
        params.code = code;
      }
      return inspirationService.query(params).$promise.then(function (data) {
        $scope.inspirations = data;
      });
    };


    $scope.fetchInitialData = function () {
      $scope.getConfiguration()
        .then($scope.getDates)
        .then(function () {
          updateConfig($scope.configuration.periods[0]);
        })
        // .then($scope.weekendsTabSelected)
        .then($scope.getAirports)
        .then($scope.updateOriginsAndDestinations)

        .then($scope.fetchItinerariesSelectedDates)
        .then($scope.updateMinPrices)
        .then($scope.updateInspirations);
    };


    $scope.isMobile = isMobileClient();

    if ($scope.isMobile) {
      $modal.open({
        templateUrl: '/static/pages/mobile-warning.html',
        size: 'sm',
        controller: "WarningController",
        resolve: {
          fetchInitialData: function () {
            return $scope.fetchInitialData;
          }
        }
      });
    } else {
      $scope.fetchInitialData();
    }


    $scope.outboundStartTime = function () {
      return $scope.period == WEEKEND
        ? dayOfWeekFrom(WEEKEND_OUTBOUND_DAY_OF_WEEK, $scope.outboundRange[0])
        : dateFrom($scope.selectedHoliday.out, $scope.outboundRange[0]);
    };
    $scope.outboundEndTime = function () {
      return $scope.period == WEEKEND
        ? dayOfWeekFrom(WEEKEND_OUTBOUND_DAY_OF_WEEK, $scope.outboundRange[1])
        : dateFrom($scope.selectedHoliday.out, $scope.outboundRange[1]);
    };
    $scope.inboundStartTime = function () {
      return $scope.period == WEEKEND
        ? dayOfWeekFrom(WEEKEND_INBOUND_DAY_OF_WEEK, $scope.inboundRange[0])
        : dateFrom($scope.selectedHoliday.in, $scope.inboundRange[0]);
    };
    $scope.inboundEndTime = function () {
      return $scope.period == WEEKEND
        ? dayOfWeekFrom(WEEKEND_INBOUND_DAY_OF_WEEK, $scope.inboundRange[1])
        : dateFrom($scope.selectedHoliday.in, $scope.inboundRange[1]);
    };

    $scope.outDepartureTimeSlider = {
      'options': {
        start: function (event, ui) {
          $scope.closeItinerariesInfo();
        }
        ,
        stop: function (event, ui) {
          $scope.updateMinPrices();
        }
        ,
        min: -24,
        max: +48,
        step: 1,
        range: true
      }
    };
    $scope.inDepartureTimeSlider = {
      'options': {
        start: function (event, ui) {
          $scope.closeItinerariesInfo();
        }
        ,
        stop: function (event, ui) {
          $scope.updateMinPrices();
        }
        ,
        min: -24,
        max: +48,
        step: 1,
        range: true
      }
    };
    $scope.flightDurationSlider = {
      'options': {
        start: function (event, ui) {
          $scope.closeItinerariesInfo();
        }
        ,
        stop: function (event, ui) {
          $scope.updateMinPrices();
        }
        ,
        min: 1,
        max: 12,
        step: .5,
        range: false
      }
    };
    $scope.flightPriceSlider = {
      'options': {
        start: function (event, ui) {
          $scope.closeItinerariesInfo();
        }
        ,
        stop: function (event, ui) {
          $scope.updateMinPrices();
        }
        ,
        min: 60,
        max: 1200,
        step: 20,
        range: false
      }
    };


    $scope.flightDurationStr = function (n) {
      return '' + Math.floor(n / 60) + 'h' + (n % 60) + 'm';
    };

    $scope.flightTimeStr = function (s) {
      return moment.utc(s).format('HH:mm');
    };

    $scope.flightDateStr = function (s) {
      return moment.utc(s).format('DD-MMM');
    };

    $scope.closeItinerariesInfo = function () {
      $('#itinerariesInfo').css({visibility: 'hidden'});
    };

    $scope.airportName = function (code) {
      if (!$scope.knownAirports[code] || !$scope.knownAirports[code].long_name) {
        return code;
      }
      var r = $scope.knownAirports[code].long_name;
      if (r.length > 21) {
        return r.substring(0, 19) + '...';
      } else {
        return r;
      }
    };

    $scope.uevt = function (evt) {
      evtService.post(evt);
      $window.ga('send', 'pageview', {page: evt.action});
    };


    $scope.buy = function (destcode, it) {
      $scope.uevt({action: 'buy', 'dest': destcode, 'it-id': it.id});
      $modal.open({
        templateUrl: '/static/pages/buy.html',
        controller: 'DlgController',
        size: 'sm',
        resolve: {
          title: function () {
            return '- ';
          },
          itinerary: function () {
            return it;
          }
        }
      });
    };

    $scope.sortFlightsBy = function (fld) {
      $scope.flightsSortedBy = fld;
      if (fld == 'outDeparture' || fld == 'inDeparture') {
        $scope.currentItineraries.itineraries = _.sortBy($scope.currentItineraries.itineraries, function (it) {
          return moment.utc(it[fld]);
        });
      } else {
        $scope.currentItineraries.itineraries = _.sortBy($scope.currentItineraries.itineraries, function (it) {
          return it[fld];
        });
      }


      if ($scope.sortingOrder == 'up') {
        $scope.sortingOrder = 'down';
        $scope.currentItineraries.itineraries.reverse();
      } else {
        $scope.sortingOrder = 'up';
      }
      $scope.currentItineraries.rows = createTableRows($scope.currentItineraries.itineraries);
    };

    $scope.carrier = function (slices) {
      var result = new Set();
      _.each(slices, function (sl) {
        _.each(sl, function (seg) {
          result.add(seg.carrier);
        })
      });

      if (result.size == 1) {
        return result.keys().next().value;
      } else {
        var s = '';
        var iter = result.keys();
        var n = iter.next();
        while (!n.done) {
          if (s.length) {
            s += '/';
          }
          s += n.value;
          n = iter.next();
        }
        return s;
      }
    };

    $scope.itineraryAge = function (it) {
      return '' + Math.floor((new Date().getTime() - it.timestamp) / 3600000) + 'h';
    };

    $scope.isDirect = function (it) {
      return _.filter(it.slices, function (sl) {
          return sl.length > 1;
        }).length == 0;
    };


    $scope.weekendsTabSelected = function () {
      updateConfig('weekend');
    };


    $scope.holidaysTabSelected = function () {
      updateConfig('holiday');
      $scope.holidaySelectionChanged(0);
    };

    $scope.zoomTo = function (loc) {
      if (loc == 'caribbean') {
        $scope.mapCenter = [12, -68];
        $scope.mapZoom = 5;
      } else if (loc == 'se-asia') {
        $scope.mapCenter = [6, 120];
        $scope.mapZoom = 4;
      } else if (loc == 'world') {
        $scope.mapCenter = $scope.configuration['holiday-map-center'];
        $scope.mapZoom = $scope.configuration['holiday-map-zoom'];
      }
    };

    $scope.getLastEvents = function () {
      $scope.events = eventsService.query();
    };

    $scope.appChanged = function (href) {
      window.location.href = href;
    };

  }
)
;

wcigModule.controller("WarningController", function ($modalInstance, $scope, fetchInitialData) {

  $scope.ok = function () {
    fetchInitialData();
    $modalInstance.close();
  };


});

wcigModule.controller("DlgController", function ($scope, $modalInstance, itinerary, evtService) {


  $scope.close = function () {
    $modalInstance.close();
  };

  $scope.itinerary = itinerary;

  $scope.cancel = function () {
    $modalInstance.dismiss();
  };

  $scope.linkEbookers = function (it) {
    return 'http://www.ebookers.nl/psi?type=air&triptype=roundtrip&origin=AMS'
      + '&dest=' + destcode
      + '&departdate=' + moment.utc(it.outDepartureDateTime).format('YYYY-MM-DD')
      + '&departspan=Anytime'
      + '&returndate=' + moment.utc(it.inDepartureDateTime).format('YYYY-MM-DD')
      + '&returnspan=Anytime&cabin=C&adults=2';
  };
  $scope.linkTransavia = function (it) {
    return 'https://www.transavia.com/nl-NL/bookingtool/flights/deeplink?'
      + 'as=' + it.destination
      + '&ds=' + it.origin
      + '&ap=1'
      + '&cp=0'
      + '&od=' + moment.utc(it.outDepartureDateTime).format('DD')
      + '&om=' + +moment.utc(it.outDepartureDateTime).format('MM')
      + '&oy=' + +moment.utc(it.outDepartureDateTime).format('YYYY')
      + '&id=' + moment.utc(it.inDepartureDateTime).format('DD')
      + '&im=' + +moment.utc(it.inDepartureDateTime).format('MM')
      + '&iy=' + +moment.utc(it.inDepartureDateTime).format('YYYY')
      + '&utm_source=API&utm_medium=where2fly2&r=true';
  };
  $scope.linkWtc = function (it) {
    return 'http://www.worldticketcenter.nl/trade/?tt=505_12_228148_'
      + '&r=https%3A%2F%2Fengine.wtc.nl%2Fresults%3Fairtrade_init%3D1%26affiliate%3Dwtcnl%26channel%3DSABREWSWTC'
      + '%26vertrek1%3DAmsterdam%28AMS%29'
      + '%26bestemming1%3D' + it.destination + '%28' + it.destination + '%29'
      + '%26departureDate%3D' + moment.utc(it.outDepartureDateTime).format('DD-MM-YYYY')
      + '%26returnDate%3D' + moment.utc(it.inDepartureDateTime).format('DD-MM-YYYY')
      + '%26vertrek2%3D' + it.destination + '%28' + it.destination + '%29'
      + '%26bestemming2%3DAmsterdam%28AMS%29'

      //   + '%26Airline%3D'
      + '%26EcoBusFir%3Deco'
      + '%26NumberAdults%3D1'
      + '%26NumberChildren%3D0'
      + '%26NumberInfants%3D0'
      + '%26roundtrip%3D1'
      + '%26airtrade%3D1';
  };

  $scope.buy = function () {
    var url = $scope.itinerary.group === 'transavia' ? $scope.linkTransavia($scope.itinerary) : $scope.linkWtc($scope.itinerary);
    evtService.post({action: 'buy', link: url});
    $modalInstance.dismiss();
    window.open(url, '_blank');
  };


});


