AUTHOR('craig');
PURPOSE('Unit Test');

function HtmlTabGroup(gid) {
	this.TAG = 'div';
	this.ID = gid;
	this._menu = [];
	this.NODELIST = [{TAG:'ul', NODELIST:this._menu}];
	this.tab = function(tid, tlabel, tnode) {
		var ma = {TAG: 'a', href:'#'+tid, TEXT:tlabel};
		this._menu.push({TAG:'li',NODELIST:ma});
		this.NODELIST.push({TAG: 'div', ID:tid, NODELIST:tnode});
		return this;
	};
}
var b1 = [{TAG:'span', TEXT:'B'},{TAG:'span', TEXT:'1'}];
var tg = new HtmlTabGroup('tabMain')
.tab('panelA','Alpha',   {TAG:'ul', NODELIST:[{TAG:'li', TEXT:'A1'},{TAG:'li', TEXT:'A2'}]})
.tab('panelB','Bravo',   {TAG:'ul', NODELIST:[{TAG:'li', NODELIST:b1},{TAG:'li', TEXT:'B2'}]})
.tab('panelC','Charlie', {TAG:'ul', NODELIST:[{TAG:'li', TEXT:'C1'},{TAG:'li', TEXT:'C2'}]})
;
var he = new HtmlEncoder(tg);
he.indent = 1;
var sh = he.toString();

