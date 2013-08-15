AUTHOR('craig');
PURPOSE('Unit Test');

function Station(stationId, name, offsetToUTC) {
  this.ELEMENT_1_station_id = stationId;
  this.ELEMENT_2_name = name;
  this.ELEMENT_3_offset_to_utc = offsetToUTC;
}  
function Predictor(name, equationString) {
	this.ELEMENT_1_name = name;
	this.ELEMENT_2_equation_string = equationString;
}
function PredictorSet(name, member) {
	this.ELEMENT_1_name = name;
	this.ELEMENT_2_member = member;
}
function PrimitivePredictor(name, process, source, category, domainName, variable) {
  this.ELEMENT_1_name = name;
  this.ELEMENT_2_process = process;
  this.ELEMENT_3_source = source;
  this.ELEMENT_4_category = category;
  this.ELEMENT_5_domain_name = domainName;
  this.ELEMENT_6_variable = variable; 
}
function VBaggingjob(action, baseStep, equationOutputPath, forecastName, predictand, psname) {
	this.action = action;
	this.base_step = baseStep;
	this.base_time = 'from-schedule';
	this.equation_output_path_CDATA = equationOutputPath;
	this.forecast_name = forecastName;
	this.input_smearing = '0.001';
	this.minimum_width = '0.333';
	this.number_of_gaussians = '10';
	this.predictand = predictand;
	this.predictor_set_name = psname;
	this.NOXML_station_set_id = '88';
}
function Data(predictor, predictorSet, primitivePredictor, station) {
	this.ELEMENT_1_predictor = predictor;
	this.ELEMENT_2_predictor_set = predictorSet;
	this.ELEMENT_3_primitive_predictor = primitivePredictor;
	this.ELEMENT_4_station = station;
}
function Parameters(jobInstanceId, baseTime, dataRootPath, vbaggingJob) {
	this.ELEMENT_1_job_instance_id = jobInstanceId;
	this.ELEMENT_2_job_class = 'VBaggingJob';
	this.ELEMENT_3_base_time = baseTime;
	this.ELEMENT_4_data_root_path_CDATA = dataRootPath;
	this.ELEMENT_5_vbaggingjob = vbaggingJob;
}
function Dpt(parameters, data) {
	this.ELEMENT_1_parameters = parameters;
	this.ELEMENT_2_data = data;
}

XmlEncoder.prototype.validationMethod = 'DTD';
XmlEncoder.prototype.indent = 1;
var drp = 'UnitTest/JobRunner/VBaggingJob01/';
var eqop = 'UnitTest/JobRunner/VBaggingJob01/equations/';
var psname = 'ZieglerFinal';
var pr1 = new Predictor('Constant','1');
var pr2 = new Predictor('ECMWFdetf10', '1');
var ps1 = new PredictorSet(psname,[{name:'Constant', type:'predictor'}, {name:'ECMWFdetf102', type:'primitive'}]);
var pp1 = new PrimitivePredictor('ECMWFdetf10','raw','ECMWF','single','global','f10');
var pp2 = new PrimitivePredictor('ECMWFdetf102','raw','ECMWF','single','global','f10');
var stn1 = new Station('UK1','United Kingdom 1','00:00'); 
var stn2 = new Station('SPA1','Spain 1','00:00'); 
var vbj = new VBaggingjob('update', '720', eqop, 'FTFWindEC', 'ECMWFdetf10', psname);
var p = new Parameters('1', '2010-03-10 00:00:00.000', drp, vbj);
var d = new Data([pr1, pr2], [ps1], [pp1, pp2], [stn1, stn2]);
var dpt = new Dpt(p, d);
var xe = new XmlEncoder('dpt', dpt);
xe.dtdLocation = 'VBaggingJob.dtd';
var sx = xe.toString();