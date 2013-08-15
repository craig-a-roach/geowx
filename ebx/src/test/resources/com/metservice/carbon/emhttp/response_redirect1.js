AUTHOR('craig');
PURPOSE('Unit Test');

var r = new HttpResponse();
r.redirectTemporary('http://met.com');

r.toString=function() {
	return this.statusCode + ' ' + this['Location'];
};

return r;