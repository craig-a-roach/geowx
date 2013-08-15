AUTHOR('craig');
PURPOSE('Unit Test');

function Span(cls, content) {
  this.TAG = 'span';
  this.ATTRIBUTE_class = cls;
  this.TEXT = content;
}

function HeavyRain(content) {
  this.ATTRIBUTE_id = 'HeavyRain';
  this.NODELIST = content;
}
function StrongWind(content) {
  this.id = 'StrongWind';
  this.NODELIST = content;
}
function Snow(content) {
  this.id = 'Snow';
  this.NODELIST = content;
}

function Region(name, areas, category) {
  this.ATTRIBUTE_name = name;
  this.areas = areas; 
  this.category = category;
}

function Sww(region) {
  this.region = region;
}

XmlEncoder.prototype.validationMethod = 'None';
var otago1 = new HeavyRain(['Intense bursts about ',
new Span('place', 'High Country'),
' later\nthis afternoon, gradually moving ',
new Span('place', 'south'),
' towards\nevening.']);
var otago2 = new StrongWind([{TAG:'b', TEXT:'85kts'}, 'later in the ', {TAG:'b', TEXT:'morning'}]);
var manawatu1 = new Snow([]); 
var manawatu2 = new StrongWind(null); 
var otago = new Region('Otago', 'High Country and Lakes', [otago1, otago2]);
var manawatu = new Region('Manawatu', null, [manawatu1, manawatu2]);
var doc = new Sww([otago, manawatu]);
var xe = new XmlEncoder('sww', doc);
var sx = xe.toString();



