AUTHOR('craig');
PURPOSE('Unit Test');

var xa = Jvm.callStatic('com.metservice.neon.TestTargetJvmA', 'fa', 'String[]', ['x1','x2'], "Integer", 3, "int", 2);
var xb = Jvm.callStatic('com.metservice.neon.TestTargetJvmA', 'fb', 'boolean', true, 'String', 'abc', 'double', 3.14);
var xc = Jvm.callStatic('com.metservice.neon.TestTargetJvmA', 'fc');

