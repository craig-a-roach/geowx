AUTHOR('craig');
PURPOSE('Unit Test');

var u;
var x = null;
var s = '';
var z = 0;
var d = 0.0;

function F1() { return "called F1"; }

var ou = (isdefined u) ? 'have' : 'missing';
var qu = (u) ? 'have' : 'missing';

var ox = (isdefined x) ? 'have' : 'missing';
var qx = (x) ? 'have' : 'missing';

var os = (isdefined s) ? 'have' : 'missing';
var qs = (s) ? 'have' : 'missing';

var oz = (isdefined z) ? 'have' : 'missing';
var qz = (u) ? 'have' : 'missing';

var od = (isdefined d) ? 'have' : 'missing';
var qd = (d) ? 'have' : 'missing';

var oF1 = (isdefined F1) ? 'have' : 'missing';
var qF1 = (F1) ? 'have' : 'missing';

var oF2 = (isdefined F2) ? 'have' : 'missing';
var qF2 = (F2) ? 'have' : 'missing';


var A1 = new A(7);
var A2 = new A(7, null);
var A3 = new A(7, 0);

Shell.trace(A1);
Shell.trace(A2);
Shell.trace(A3);

var VA1 = A1.a2;
var VA2 = (A2.a2 == null) ? 'NULL' : a2;
var VA3 = A3.a2;

function A(a1, a2) {
  this.a1 = a1;
  this.a2 = (isdefined a2) ? a2 : 'default 2';
  this.toString = toString_A;
}

function toString_A() {
	return this.a1 + "/" + this.a2;
}
