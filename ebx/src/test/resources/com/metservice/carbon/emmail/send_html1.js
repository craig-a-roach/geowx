AUTHOR('craig');
PURPOSE('Unit Test');

var s1 = {TAG:'span', CLASS:'s', TEXT:'s1'};
var s2 = {TAG:'span', CLASS:'s', TEXT:'s2'};
var d = {TAG:'div', id:'d1', NODELIST:[s1,s2]};
var he = new HtmlEncoder(d);
he.indent = 1;

mail.configureSmtp('alpha', 'hurricrane.met.co.nz:1','rock','wordpass');
mail.configureSmtp('gamma');

var alpha1 = mail.sender('alpha').setFrom('rock@carbon.met.co.nz');

alpha1.setRatePolicy({min:2m,max:10m});
alpha1.setCcList('support@met.com');
alpha1.push('Overdue',he,'dev@met.com;infra@met.com','ccb@met.com');
alpha1.setToList('ops2@met.co.nz;;ops1@met.co.nz;');
alpha1.push('Unreachable','Site XY','',null, {min:3m});
alpha1.push('NotFound','Run 18Z');
mail.sender('alpha').push('Site Unresponsive','Run 16','dev@met.com;infra@met.com','ccb@met.com');

var rp1max = alpha1.ratePolicy().max;
var from1 = alpha1.from();
var cc1 = alpha1.ccList();
