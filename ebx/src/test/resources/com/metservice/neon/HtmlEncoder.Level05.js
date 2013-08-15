AUTHOR('craig');
PURPOSE('Unit Test');

var s1 = '{user:"met", timeout:30000M, passive:true, files:[{nm:"f1.txt",lm:1234567890Z,sz:4.2E3,tmo:null}, {nm:"f2.txt",lm:2468024680Z,sz:5.1E3,tmo:5}]}';
var a1 = JsonDecoder(s1).toObject();

var x1 = new HtmlEncoder(a1).toString();
var x2 = new HtmlEncoder({TAG:'div', CLASS:'info', TEXT:a1, DATUM_CLASS:'directive', DATUM_JSON_DEPTH:1}).toString();
