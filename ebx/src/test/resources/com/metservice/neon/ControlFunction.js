AUTHOR('craigr');
PURPOSE('Model Run');

var g = {};
g.aux = { _trackMap: {} };
g.aux.track = function(trackName) {
	var t = this._trackMap[trackName];
	if (t) return t;
	t = {};
	t.init = function(val) {
		this._value = val;
	  	return this._value;
	};
	t.increment = function(ival) {
		this._value += ival;
	  	return this._value;
	};
	t.valueOf = function() {
		return this._value;  
	};
	this._trackMap[trackName] = t;
	return t;
};


var g_prognosis = g.aux.track('prognosis');
var g_counter = g.aux.track('counter');

g_prognosis.init(1h);
g_counter.init(17);

p1a = g_prognosis.valueOf();
p1b = g.aux.track('prognosis').valueOf(); 
c1 = g_counter.valueOf();
c2 = g_counter.increment(3);
c3 = g.aux.track('counter').valueOf();




