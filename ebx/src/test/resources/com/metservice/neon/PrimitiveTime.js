AUTHOR('craig');
PURPOSE('Unit Test');

var t1 = Number('20100409T0315Z00');
var t2 = Number('20100409T0630Z');
var qtr = (3h / 16) + (1h / 16);
var bq1 = qtr == 15m;
var bq2 = qtr == 900s;
var e21 = t2 - t1;
var d21h = e21 / 1h;
var i21q = e21 / qtr;
var t3 = t1 + (d21h * 1h);
var t4 = t1 + (i21q * 15m);
var b3 = t2 == t3;
var b4 = t2 == t4;
var yy = 400d;
var p1 = 12h / 3h;
var p2 = 12h / 13s;
