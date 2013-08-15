AUTHOR('craig');
PURPOSE('Unit Test');
function Hello(name, from) {
	this.name = name;
	this.from = from;
}
var xe = new XmlEncoder('hello', new Hello('world', ['airflo','boron','neon']));
xe.validationMethod = 'None';
var lines = ['netstat -an'];
var s = new Script(lines);
s.addResource('demo.xml',xe);
s.exitTimeout = 12s; // script has 12s to exit
if (process.isWinOS) {
	s.interpreter = 'win';
}
	
var pi = process.newProductIterator(s);
var p;
var tcpcount = 0;
var exitCode;
var cancelled = false;
var failed = false;
while (pi.hasNext()) {
	p = pi.next(4s);  // optionally cancel if have to wait more than 4s
	switch (p.type) {
	case 'StreamLine':
		if (p.isStdErr) {
			Shell.fail(p);
		} else {
			Shell.trace(p.text);
			if (/\s*TCP\s+.*/i.matches(p.text)) {
				tcpcount++;
			}
		}
		break;
	case 'StreamEnd':
		break;
	case 'ExitCode':
		exitCode = p.code;
		break;
	case 'Cancellation':
		cancelled = true;
		break;
	default:
		failed = true;
	    Shell.fail(p);
	}
}
var hastcp = tcpcount > 0;
var path = '/abc'.tidySuffix('/',true) + 'cubeState';
var oBinary = fileSystem.loadFile(path);

