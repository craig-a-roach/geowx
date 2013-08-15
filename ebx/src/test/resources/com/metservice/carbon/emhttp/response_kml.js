AUTHOR('craig');
PURPOSE('Unit Test');


var x = '<?xml version="1.0" encoding="UTF-8"?>\n<kml xmlns="http://www.opengis.net/kml/2.2"><Document/>';
var xb = x.encodeUtf8();
var r = new HttpResponse(xb, 'application/vnd.google-earth.kml+xml');
var ct = r['Content-Type'];
return r;