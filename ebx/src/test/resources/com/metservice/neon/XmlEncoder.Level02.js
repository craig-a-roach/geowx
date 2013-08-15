AUTHOR('craig');
PURPOSE('Unit Test');

function PartD(number, description) {
  this.number = number;
  this.TEXT = description;
}
function Parts(partD) {
  this.part = partD;
}
function PartQ(number, quantity) {
  this.ATTRIBUTE_number = number;
  this.ATTRIBUTE_quantity = quantity;
}
function Zip(code, part) {
  this.ATTRIBUTE_code = code;
  this.part = part;
}
function Regions(zip) {
  this.zip = zip;
} 
function PurchaseReport(period, periodEnding, regions, parts) {
  this.ATTRIBUTE_period = period;
  this.ATTRIBUTE_periodEnding = periodEnding;
  this.ELEMENT_1_regions = regions;
  this.ELEMENT_2_parts = parts;
}
var parts = new Parts([
  new PartD('872-AA', 'Lawnmower'),
  new PartD('926-AA', 'Baby Monitor'),
  new PartD('833-AA', 'Lapis Necklace'),
  new PartD('455-BX', 'Sturdy Shelves')
]);
var zip1 = new Zip('95819',[
  new PartQ('872-AA', 1),
  new PartQ('926-AA', 1),
  new PartQ('833-AA', 1),
  new PartQ('455-BX', 1)
  ]);
var zip2 = new Zip('63143',[
  new PartQ('455-BX', 4)
  ]);
var regions = new Regions([zip1, zip2]);
var pr = new PurchaseReport('P3M', '1999-12-31', regions, parts);
var xe = new XmlEncoder('purchaseReport', pr);
xe.namespaceUri = 'http://www.example.com/Report';
xe.schemaLocation = 'level02.xsd'; 
var sx = xe.toString();
Shell.trace(sx);
