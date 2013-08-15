AUTHOR('craig');
PURPOSE('Unit Test');

var a1 = ['a','b','c','d','e'];
var a1_all = a1.slice();
var a1_1 = a1.slice(1);
var a1_13 = a1.slice(1,3);
var a1_1m1 = a1.slice(1,-1);
var a1_bad = a1.slice(0,6);

var a1_t = a1.tail();
var a1_tt = a1.tail().tail();
var a1_02t = a1.slice(0,2).tail();
var a1_e = a1.slice(0,2).tail().tail();