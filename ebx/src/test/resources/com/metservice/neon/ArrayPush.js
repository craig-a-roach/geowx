AUTHOR('craig');
PURPOSE('Unit Test');

var a1 = [];
a1.push('a','b',['c0','c1'],'d');
a1.push('e');

var s1 =a1[1]+'.'+a1[2][1]+'.'+a1[4]; //b.c1.e

var a2 = [];
a2.pushEach(['a','b'],'c',['d','e']);
a2.pushEach('f',[],'g',['h']);

var s2 = a2.join();