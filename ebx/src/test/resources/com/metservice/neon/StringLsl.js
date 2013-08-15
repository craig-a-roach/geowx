AUTHOR('craig');
PURPOSE('Unit Test');

var s1 = '-rw-rw-r--  1 3107  1153  3205246132 Jun 14 05:06 fh.0228_pa.p20_lt.press_gr.2p5deg'+
'\n-rw-rw-r--  1 3107  1153  248013 Jul 25 03:21 fh.0228_pa.p19_lt.press_gr.onedeg' +
'\n-rw-rw-r--  1 3107  1153  4901 Jun 10 2011 fh.0228_pa.p20_lt.press_gr.onedeg.idx' +
'\n-rw-rw-r--  1 3107  1153  124567 Feb 8 2010 fh.0228_pa.p20_lt.press_gr.onedeg'+
'\ndrw-rw-r--  1 3107  1153  4096 Dec 25 05:26 arc.grib';

var lsl = s1.toLines().lslParse();
var m0 = lsl[0].lastModified.toString();
var m1 = lsl[1].lastModified.toString();
var m2 = lsl[2].lastModified.toString();
var m3 = lsl[3].lastModified.toString();
var m4 = lsl[4].lastModified.toString();

var lslt = s1.toLines().lslParse(Timezone('GMT'), 't');
var t4 = lslt[4].lastModified.toString();

var lsln = s1.toLines().lslParse(null, 'n');
var n4 = lsln[4].name;

var lslz = s1.toLines().lslParse(null, 's');
var z4 = lslz[4].size;
