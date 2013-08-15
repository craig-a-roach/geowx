AUTHOR('craig');
PURPOSE('Unit Test');

var gmt = Timezone('GMT');
var akl = Timezone('Pacific/Auckland');
var e1 = gmt.equivalentTo('utc');
var e2 = gmt.equivalentTo(akl); 
var e3 = akl.equivalentTo("GMT+12"); 

var tm = Timemask('[YEAR][MONZ][DOMZ] [H24Z][MINZ].[SECZ]');
var fa = gmt.timeFactors('20100409T0915Z');
var fa6 = fa.alignToInterval(6h, 'floor').toString(tm);




