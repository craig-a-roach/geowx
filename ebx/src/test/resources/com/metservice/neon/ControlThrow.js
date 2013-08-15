AUTHOR('craig');
PURPOSE('Unit test of nested throws');

function F1(x) {
	if (x <= 0)
		throw 'Value of x ('+x+') is non-positive';
	return x + 1;
}


function F2(x) {
	if (x < 10) return F1(x);
	return x;
}

var y1 = F2(5);
var y2 = new F2(-5);
var y3 = F2(4);
return y3;