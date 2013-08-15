AUTHOR('craig');
PURPOSE('Unit Test');

var gx = 10;

function fa(x, y) {
	return adder(x, y);
}

function fb(x, y) {
	var s = new Subtracter(x, y);
	return new Formatter(s.operate());
}

function fc(x, y) {
	return gx + x + y;
}

function adder(x, y) {
	return x + y;
}

function Subtracter(x, y) {
	this.a = x;
	this.b = y;
	this.operate = function() {
		return this.a - this.b;
	};
}

function Formatter(x) {
	this.datum = x;
	this.toString = function() {
		return this.datum == 1 ? 'one' : this.datum.toString();
	};
}