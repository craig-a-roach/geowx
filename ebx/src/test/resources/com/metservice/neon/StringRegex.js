AUTHOR('craig');
PURPOSE('Unit Test');

var ew1 = 'abc'.endsWith('bd');
var ew2 = 'ABC'.endsWith('bc',true);

var m1 = /.+[.]idx/i.matches('ABC.IDX');
var m2 = /.+[.]idx/i.matches('ABC.IDX.DEF');
var m3 = RegExp('\\QABC*IDX\\E').matches('ABC*IDX');
var m4 = RegExp('d.+\\QABC*IDX\\E').matches('drw 1 u g b m d h:m ABC*IDX');

var a1 = 'a.x \n\n b.y\nc.x\n '.toLines(true, false).join();
var a2 = 'a.x \n\n b.y\nc.x\n '.toLines(true, false, null, /.+[.Y]/i).join();
var a3 = 'a.x \n\n b.y\nc.x\n '.toLines(true, false, '.+[.y]').join();
var a4 = 'a.x \n\n b*y\nc.x\n '.toLines(true, false, '\\Qb*y\\E').join();

var t1 = String('a/b').tidySuffix('/', true);
var t2 = String('a/b').tidySuffix('/', false);
var t3 = String('a/b/').tidySuffix('/', true);
var t4 = String('a/b/').tidySuffix('/', false);
var t5 = String('/').tidySuffix('/', false);


var rep1 = String('a.b.c').replace('.','/');
var rep2 = String('a.b.c').replace('.','/','g');

var xa = /fh[.](\d\d\d)_pa[.]mbr(c00|p\d\d)_tl[.]press_gr[.]onedeg/;
var ga1 = xa.capture('fh.006_pa.mbrp02_tl.press_gr.onedeg');
var sa1 = ga1[0][1] + ':' + ga1[0][2];
var ga2 = xa.capture('fh.006_pa.mbrc00_tl.press_gr.onedeg');
var sa2 = ga2[0][1] + ':' + ga2[0][2];

var xb = /([a-z])(\d)/;
var gb1 = xb.capture('a3b6');
var sb1 = gb1[0][1] + ':' + gb1[0][2] + '|' + gb1[1][1] + ':' + gb1[1][2]; 

var y1 = /:/.split(' a : b:c::d:').join();
var y2 = /:/.split('').length;
