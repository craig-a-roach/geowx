AUTHOR('craig');
PURPOSE('Unit Test');

function Product(id, qty) {
  this.id = id;
  this.qty = qty;
}
function Region(code, product) {
  this.code = code;
  this.product = product;
}
function PurchaseReport(year, region) {
  this.year = year;
  this.region = region; 
}
var regionN = new Region('N', [new Product('A', '6'),new Product('B', '13')]); 
var regionS = new Region('S', [new Product('A', '3'),new Product('B', '17')]); 
var pr = new PurchaseReport('2010', [regionN, regionS]);
var xe = new XmlEncoder('purchaseReport', pr);
xe.validationMethod = 'None';
xe.defaultForm = 'Attribute';
var sx = xe.toString();
