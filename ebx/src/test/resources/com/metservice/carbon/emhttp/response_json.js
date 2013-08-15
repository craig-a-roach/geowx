AUTHOR('craig');
PURPOSE('Unit Test');

var r = new HttpResponse({a:'alpha', b:[3,5,7], c:true});
var c = r.content;
var ct = r['Content-Type'];
r.setExpires(Number('20101231T1100Z'));
r.statusCode = 203;

r.toString=function() {
	return this.content.decodeAscii();
};
return r;