AUTHOR('craig');
PURPOSE('Unit Test');

var s1 = {TAG:'span', CLASS:'s', TEXT:'s1'};
var s2 = {TAG:'span', CLASS:'s', TEXT:'s2'};
var d = {TAG:'div', id:'d1', NODELIST:[s1,s2]};
var he = new HtmlEncoder(d);
he.charset = 'iso-8859-1';
he.indent = 1;
var r = new HttpResponse(he);
var ct = r['Content-Type'];
r.setExpires(Number('20101231T1100Z'));

r.toString=function() {
	return this.content.decodeUtf8();
};
return r;