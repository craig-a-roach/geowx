AUTHOR('craig');
PURPOSE('Unit Test');

var gj = "fj".findFunction();
var xj = gj();

var gk = 'fk'.findFunction(fdef);
var xk = gk();

var xp = "fp".findFunction()('xy');


function fa() {
	return "abc";
}

function fj() {
	return "jkl";
}

function fp(a) {
	return "pqr" + a;
}

function fdef() {
	return "???";
}