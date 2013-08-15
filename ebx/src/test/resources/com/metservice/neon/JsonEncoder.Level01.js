AUTHOR('craig');
PURPOSE('Unit Test');

function File(path, name, timeout) {
	this.path = path;
	this.name = name;
	this.timeout = timeout;
}

var f0 = new File('grib','f1.txt');
var x = {user:'met',pwd:null,timeout:30, passive: true, files:[f0,new File('grib','f2.txt', 17)]};
var je = new JsonEncoder(x);
var sx = je.toString();
