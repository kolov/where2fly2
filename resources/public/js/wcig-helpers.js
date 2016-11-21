var weekDayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

function dayOfWeekFrom(startDay, offsetInHours) {
  if (offsetInHours > 0 && offsetInHours % 24 == 0) {
    var ixDay = (startDay + (offsetInHours / 24 - 1)) % 7;
    var hours = 24;
  } else {
    var ixDay = Math.trunc(((offsetInHours / 24) + startDay + 7)) % 7;
    var hours = (offsetInHours + 168) % 24;
  }
  return weekDayNames[ixDay] + ' ' + hours + ':00';

}

function dateFrom(dt, offsetInHours) {
  return moment.utc(dt).add( offsetInHours, 'hour').format('DD-MMM HH:mm');
}
function timeBetween(flightTime, day, range) {
  return flightTime.isAfter(moment.utc(day).add(range[0], 'hours'))
    && flightTime.isBefore(moment.utc(day).add(range[1], 'hours'));
}