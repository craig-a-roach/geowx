AUTHOR('craig');
PURPOSE('Unit Test');

var ab = Binary('res/a.txt');
var abm = ab.decodeUtf8().toLines().toMap();
var ab0 = abm.b.x2;
var ab1 = abm.a.x1;
var ab2 = abm.b.x3;
var ab3 = abm.c.x3;

var bb = Binary('res/b.txt');
var bbm = bb.decodeUtf8().toLines().toMap('$err');
var bb0 = (bbm['NCEP dev'].timeoutElapsed / 5m);
var bb1 = bbm['NCEP dev'].enabledFlag ? "Y" : "N";
var bb2 = bbm.NCEP.agentCount;
var bb3 = bbm.bogus.agentCount$err;
var bb4 = bbm.bogus.enabledFlag;
