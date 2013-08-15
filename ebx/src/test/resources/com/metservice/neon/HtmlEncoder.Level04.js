AUTHOR('craig');
PURPOSE('Unit Test');

var s1 = new HtmlEncoder({TAG: 'a', HREF:'/alpha', TEXT:'a1'}).toString();
var s2 = new HtmlEncoder({TAG: 'a', HREF:{_PATH:'/alpha', f:'V&F', g:'V G', h:['H:1','H/2']}, TEXT:'a2'}).toString();
var s3 = new HtmlEncoder({TAG: 'a', HREF:'/alpha?y', TEXT:'a3'}).toString();
var s4 = new HtmlEncoder({TAG: 'a', HREF:{_PATH:'/alpha', _FRAGMENT:'beta'}, TEXT:'a4'}).toString();
var s5 = new HtmlEncoder({TAG: 'a', HREF:{_HOST:'webserv1', _PORT:8080, _PATH:'/alpha beta'}, TEXT:'a5'}).toString();
