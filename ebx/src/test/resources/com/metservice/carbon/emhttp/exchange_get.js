AUTHOR('craig');
PURPOSE('Unit Test');

var h = httpRequest['If-Modified-Since'];
var qx = httpRequest.newParameterObject();
var qy = httpRequest.newParameterObject({pc:'c0', pb:'b3', pd:['d0','d1']}, ['pa']);

var rx = qx.pa+':'+qx.pb;
var ry = qy.pb+':'+qy.pd;
var rn = qx.pn == null ? 'NULL' : qx.pn;
var r = new HttpResponse('saved x('+rx+') y('+ry+') n('+rn+')');
r.setLastModified(h + 1d);
r['Cache-Control'] = 'max-age=310';
r['Server'] = 'Carbon';
return r;