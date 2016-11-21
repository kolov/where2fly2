var markerSize = {x: 16, y: 16};

if (google  ) {
  google.maps.Marker.prototype.setLabel = function (label) {
    this.label = new MarkerLabel({
      map: this.map,
      marker: this,
      text: label
    });
    this.label.bindTo('position', this, 'position');
  };
}
var MarkerLabel = function (options) {
  this.setValues(options);
  this.span = document.createElement('span');
  this.span.className = 'map-marker-label';
};

MarkerLabel.prototype = $.extend(new google.maps.OverlayView(), {
  onAdd: function () {
    this.getPanes().overlayImage.appendChild(this.span);
    var self = this;
    this.listeners = [
      google.maps.event.addListener(this, 'position_changed', function () {
        self.draw();
      })];
  },
  draw: function () {
    var text = String(this.get('text'));
    var position = this.getProjection().fromLatLngToDivPixel(this.get('position'));
    this.span.innerHTML = text;
    this.span.style.left = (position.x - (markerSize.x / 2)) - (text.length * 3) + 10 + 'px';
    this.span.style.top = (position.y - markerSize.y + 14) + 'px';
  }
});

var goldStar = {
  path: 'M 125,5 155,90 245,90 175,145 200,230 125,180 50,230 75,145 5,90 95,90 z',
  fillColor: 'black',
  fillOpacity: 0.8,
  scale: 0.06,
  strokeColor: 'gold',
  strokeWeight: 0.6
};

var airportClickShape = {
  coords: [1, 1, 1, 20, 18, 20, 18, 1],
  type: 'poly'
};

var imageDest = {
  url: '/static/img/beach.png',
  size: new google.maps.Size(16, 16),
  origin: new google.maps.Point(0, 0),
  anchor: new google.maps.Point(0, 0)
};

var colorScale = [
  '#FF0000',
  '#FF1000',
  '#FF2000',
  '#FF3000',
  '#FF4000',
  '#FF5000',
  '#FF6000',
  '#FF7000',
  '#FF8000',
  '#FF9000',
  '#FFA000',
  '#FFB000',
  '#FFC000',
  '#FFD000',
  '#FFE000',
  '#FFF000',
  '#FFFF00', //max, step by 15
  '#F0FF00', //cheat, start with a -15 to simplify the rest
  '#E0FF00',
  '#D0FF00',
  '#C0FF00',
  '#B0FF00',
  '#A0FF00',
  '#90FF00',
  '#80FF00',
  '#70FF00',
  '#60FF00',
  '#50FF00',
  '#40FF00',
  '#30FF00',
  '#20FF00',
  '#10FF00'
];

function calculatePriceColor(price, colorLow, colorHigh) {
  var cix = ( (price - colorLow) / (colorHigh - colorLow));

  cix = colorScale.length - 1 - Math.round(colorScale.length * cix);
  if (cix > colorScale.length - 1) {
    cix = colorScale.length - 1;
  }
  if (cix < 0) {
    cix = 0;
  }
  var result = colorScale[cix];
  return result;
}

function toDateStr(datestring) {
  var dt = new Date(datestring);
  return '' + dt.getUTCFullYear() + '-' + (dt.getUTCMonth() + 1)
    + '-' + dt.getUTCDate();
}



