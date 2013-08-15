AUTHOR('craig');
PURPOSE('Unit Test');


var s1 = '{user:"met", timeout:30000M, passive:true, files:[{nm:"f1.txt",lm:1234567890Z,sz:4.2E3,tmo:null}, {nm:"f2.txt",lm:2468024680Z,sz:5.1E3,tmo:5}]}';
var a1 = JsonDecoder(s1).toObject();
var x1a = a1.timeout.toString();
var x1b = a1.files[0].nm;
var x1 = JsonEncoder(a1).toString();
var a2 = JsonDecoder(s1).decode();
var x2a = a2.result.timeout.toString();

var t1 = '{user:"met", files:["f1.txt","f2.txt"}}';
var b1 = JsonDecoder(t1).decode();
var y1 = b1.error;

var u1 = {user:"met", timeout:30s, passive:true, files:[{nm:"f0.txt",sz:16},null,{nm:"f2.txt",sz:32}]};
u1.files[5] = {nm: "f5.txt"};
var c1 = JsonEncoder(u1).toBinary();
var z1 = c1.decodeJsonUtf8().result.timeout.toString();
var w1 = uin.files[2].nm;

var s2 = '';
var y2 = s2.encodeUtf8().decodeJsonUtf8().error ? 'empty' : 'parsed';
