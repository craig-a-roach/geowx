AUTHOR('craig');
PURPOSE('Unit Test');

function Product(id, qty) {
  this.NSiso_id = id;
  this.NSiso_qty = qty;
}
function Region(code, product) {
  this.ATTRIBUTE_code = code;
  this.ELEMENT_NSiso_product = product;
}
function PurchaseReport(year, region) {
  this.ATTRIBUTE_NSxmlns_xsd = 'http://www.w3.org/2001/XMLSchema';
  this.ATTRIBUTE_NSxmlns_iso = 'http://www.iso.org/2009/ProductSchema';
  this.ATTRIBUTE_NSxsd_year = year;
  this.region = region; 
}
var regionN = new Region('N', [new Product('A', '6'),new Product('B', '13')]); 
var regionS = new Region('S', [new Product('A', '3'),new Product('B', '17')]); 
var pr = new PurchaseReport('2010', [regionN, regionS]);
var xe = new XmlEncoder('purchaseReport', pr);
xe.namespaceUri = 'http://www.example.com/Report';
var sx = xe.toString();
