AUTHOR('craig');
PURPOSE('Unit Test');

var liveDomainUser = 'roach';
var liveDomainPassword = 'AUTOcoot53';
var liveTO = 'craig.roach@metservice.com';
var liveFROM = 'roach@metservice.com';

var s1 = {TAG:'span', style:'color:red', TEXT:'hello'};
var s2 = {TAG:'span', style:'color:green', TEXT:'email'};
var d = {TAG:'div', style:'font-family:sans-serif; font-size:36pt', NODELIST:[s1,s2]};
var he = new HtmlEncoder(d);
he.indent = 2;

mail.configureSmtp('kelburn', 'hurricrane.met.co.nz',liveDomainUser,liveDomainPassword);
var kelburn = mail.sender('kelburn').setFrom(liveFROM);
kelburn.push('UnitTest',he,liveTO);

mail.sender().push('Greeting','Zen',liveTO);
