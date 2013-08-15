AUTHOR('craig');
PURPOSE('Unit Test');

var q = httpRequest.newParameterObject();

var r = new HttpResponse({a:q.pa, b:[q.pb.length, 4], c:true});
r['Server'] = 'Carbon';
return r;