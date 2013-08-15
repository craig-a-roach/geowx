AUTHOR('craig');
PURPOSE('Unit Test');

var t = {TAG: 'title', TEXT:'Radio Group'};
var cs1 = {TAG:'link', rel:'stylesheet', type:'text/css', href:'../styles/core.css'};
var cs2 = {TAG:'style', type:'text/css', TEXT:Binary('res/button.css'), ESCAPE:false};
var sc1 = {TAG:'script', type:'text/javascript', src:'../scripts/jquery-1.4.js'};
var sc2 = {TAG:'script', type:'text/javascript', TEXT:Binary('res/button.js'), ESCAPE:false};
var head = {TAG:'head', NODELIST:[t,cs1,cs2,sc1,sc2]};

var label = {TAG:'label', FOR:'radioYes', TEXT:'Your answer?'};
var in1 = {TAG:'input', type:'radio', name:'rg', id:'radioYes', value:'yes', checked:'checked'};
var in2 = {TAG:'input', type:'radio', name:'rg', id:'radioNo', value:'no', checked:null};
var in3 = {TAG:'input', type:'radio', name:'rg', id:'radioMaybe', value:'maybe', checked:null};

var div1 = {TAG:'div',NODELIST:[label,in1,'Yes',in2,'No',in3,'Maybe']};

var btn = {TAG:'button', type:'button', id:'testButton', CLASS:'green90x24', TEXT:'Which?'};
var div2 = {TAG:'div', NODELIST:btn};
var div3 = {TAG:'div', id:'result'};
var form = {TAG:'form', NODELIST:[div1,div2,div3]};


var body = {TAG:'body', NODELIST:form};
var html = {TAG:'html', NODELIST:[head,body]};

var he = new HtmlEncoder(html);
var sh = he.toString();
