AUTHOR('craig');
PURPOSE('Unit test of nested loops');

var x1 = ["b", "c", "a"];
var x2 = ["p", "q", "r"];
var x3 = {g:"golf", f:"fox", e:"echo"};

var y = [];
for (var j1 in x1) {
	for (var j2 in x2) {
		for (var j3 in x3) {
			y.push(x1[j1] + ':' + x2[j2] + ':' + x3[j3]);
		}
	}
}

var ys = y.join();
