AUTHOR('craigr');
PURPOSE('Start Agent');

var vshell = {debug: vdebug, directives: jreDirectives};
var vdaemon = {stdErrEmit: true, stdErrReport: true};
var vpolicy = {assumeHealthyAfter: 45s, restartLimit: 3};

var script = new Script(['java -jar bogus.jar']);
script.addResource('release.props', 'test=true');
if (process.isWinOS) {
	script.interpreter = 'win';
}
var exc = process.executeDaemon(script, vdaemon, vpolicy);
if (exc.exitCode > 0) throw exc.stdErrReport;
//if (exc.exitCode > 0) throw exc.diagnostic();
