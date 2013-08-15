AUTHOR('craig');
PURPOSE('Unit Test');

var r = new HttpResponse(307);
r['Location'] = 'http://met.com';

r.toString=function() {
	return this.statusCode + ' ' + this['Location'];
};

return r;