AUTHOR('craig');
PURPOSE('Unit Test');

var b1 = {p:'papa', q:'quebec', r:'romeo'};
var b2 = {j:'', k:null, m:'mike'};
var b3 = {c:'${p}${r}'};
var x0 = 'A${q}${r}.${p}B${q}${j}${k}${m}${c}Z';
var x1 = x0.bind(b1);
var x2 = x1.bind(b2);
var x3 = x2.bind(b3);
var x4 = x3.bind(b1);
var y0 = 'A';
var y1 = y0.bind(b1);
var z0 = '${p}';
var z1 = z0.bind(b1);
var f0 = 'SL.us008001/ST.opnl/MT.ensg_CY.<CY>/RD.<RD>/PT.grid_DF';
var f1 = f0.bind({CY:'06', RD:'20110602'}, /<\w+>/);

