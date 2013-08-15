AUTHOR('craig');
PURPOSE('Unit Test');

var a1 = ['a','b','c','d'];
a1[1] = null;
delete a1[3];
var s1 = a1.join(); //a,null,c,undef
a1.compact(false); //a,null,c
var s2 = a1.join();
a1.compact(); //a,c
var s3 = a1.join();
a1[0] = null; //null,c
a1.compact(); //c
var s4 = a1.join();
a1[2] = 'dd'; //c,undef,dd
a1.compact(); //c,dd
var s5 = a1.join();
