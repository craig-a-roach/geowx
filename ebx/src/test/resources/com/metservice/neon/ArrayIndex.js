AUTHOR('craig');
PURPOSE('Unit Test');

var xmap = new Array();
xmap['a'] = 'Alpha';
xmap['b'] = 'Bravo';
xmap['f'] = 'Foxtrot';

var kz = '';
var x1 = null;
if (xmap[kz]) {
	x1 = 'bad';
}

var x2 = null;
xmap[kz] = 'Zulu';
if (xmap[kz]) {
	x2 = 'good';
}
