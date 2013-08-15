AUTHOR('craig');
PURPOSE('Unit Test');
IMPORT('lib/Constants');

var TimemaskStd = Timemask('[H24Z][MINZ] [DOWN], [DOMZ]-[MONC]-[YEAR] [TZC]');
var gf = String('6-Mar-2010 09:30').toTime('(\\d+)-(\\w+)-(\\d+)\\s+(\\d+):(\\d+)',[3,2,1,4,5], TimezoneNZ);
var sgmt1 = gf.toString(TimemaskStd);
var sgmt2 = gf.toString(TimemaskStd, null);
var snz = gf.toString(TimemaskStd, TimezoneNZ);

var TimemaskSec = Timemask('[H24Z]:[MINZ]:[SECZ] [DOWC], [DOMZ]-[MONC]-[YEAR] [TZC]');
var hr = /\w+,\s+(\d+)\s+(\w{3})\s+(\d{4})\s+(\d+):(\d+):(\d+)\s+(.+)/;
var hf1 = String('Thu, 08 Dec 2011 02:25:18 GMT').toTime(hr,[3,2,1,4,5,6,7]);
var hf2 = String('Thu, 08 Dec 2011 02:25:18 PST').toTime(hr,[3,2,1,4,5,6,7]);
var hf3 = String('Thu, 08 Dec 2011 02:25:18 NZ').toTime(hr,[3,2,1,4,5,6,7]);
var hf4 = String('Thu, 08 Dec 2011 02:25:18 +13').toTime(hr,[3,2,1,4,5,6,7]);
var shmt1 = hf1.toString(TimemaskSec);
var shmt2 = hf2.toString(TimemaskSec);
var shmt3 = hf3.toString(TimemaskSec);
var shmt4 = hf4.toString(TimemaskSec);

var linuxA = /(\w+)\s*(\d+)\s+((?:\d{1,2}:\d\d)|\d{4})/;
var x1f = String('Mar  5  17:21').toTime(linuxA, null, TimezoneNZ, gf);
var sx1mt = x1f.toString(TimemaskStd);
var x2f = String('Dec  5  17:21').toTime(linuxA, null, TimezoneNZ, gf);
var sx2mt = x2f.toString(TimemaskStd);
var x3f = String('Mar  5  2008').toTime(linuxA, null, TimezoneNZ, gf);
var sx3mt = x3f.toString(TimemaskStd);

var linuxB = /((?:\d{1,2}:\d\d)|\d{4})\s*(\w+)\s*(\d+)/;
var y1f = String('17:21Mar5').toTime(linuxB, [2,3,1], TimezoneNZ, gf); 
var symt = y1f.toString(TimemaskStd);

var linuxStat = /(\d+)-(\d+)-(\d+)\s+(\d+):(\d+):(\d+)[.](\d+)\s+([+-]\d+)/;
var z1f = '2011-06-16 05:23:32.082655352 +1200'.toTime(linuxStat).toString(TimemaskStd);
var z2f = '2011-06-15 12:23:32.082655352 -0500'.toTime(linuxStat).toString(TimemaskStd);
var z3f = '20110616 05:23:32.082655352 -0500'.toTime(linuxStat);
var z3ft = z3f ? z3f : 'bad';

