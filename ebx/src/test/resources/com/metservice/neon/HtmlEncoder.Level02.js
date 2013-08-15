AUTHOR('craig');
PURPOSE('Unit Test');

var t0 = {TEXT:'abc'};
var t1 = {TAG:'span', ID:'t1', CLASS:'t', TEXT:'e<f\u00BA>g'};
var t2 = {TEXT:'h&lt;i&gt;', ESCAPE:false};
var t4 = {TEXT:'<span id="t4" title="t&amp;u">\nj&lt;k&amp;\n</span>', ESCAPE:false};
var t5 = {TAG:'ul', ID:'u3', ENCODE_LF:'<br>', ENCODE_SP:'&nbsp;', NODELIST:
	[{TAG:'li', CLASS:'ka', TEXT:'1  x\n1  y'},
	 {TAG:'li', CLASS:'kb', TEXT:'2  x\n2  y\n2 z'},
	 {TAG:'li', TEXT:'3<'}]
	};

var p = {TAG: 'p', NODELIST:[t0,t1,t2,'j&k',t4, t5]};
var he = new HtmlEncoder(p);
var sh = he.toString();
