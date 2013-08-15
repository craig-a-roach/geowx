AUTHOR('craig');
PURPOSE('Unit Test');

function comma() {
	var s = '';
	for (var i=0; i < arguments.length; i++) {
		if (s) {
			s+= ',';
		}
		s+= arguments[i];
	}
	return s;
}

var s1 = comma('a','b','c','d','e');
