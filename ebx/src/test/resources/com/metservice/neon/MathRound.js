AUTHOR('craig');
PURPOSE('Unit Test');

var step01 = Math.step(25, 6);
var step02 = Math.step(24, 6);
var step03 = Math.step(23, 6);
var step04 = Math.step(28, 6);

var step11 = Math.step(25h, 6h);
var step12 = Math.step(24h, 6h);
var step13 = Math.step(23h, 6h);
var step14 = Math.step(28h, 6h);

var r01 = Math.roundAway(25h / 6h) * 6h;
var r02 = Math.roundAway(24h / 6h) * 6h;
var r03 = Math.roundAway(23h / 6h) * 6h;
var r04 = Math.roundAway(28h / 6h) * 6h;