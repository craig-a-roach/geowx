AUTHOR('ben');
PURPOSE('Unit Test');
IMPORT('lib/DeepThought');

var label_rule = [
	new LabelRule("process::raw"),
	new LabelRule("model::ECMWF"),
	new LabelRule("domain::global"),
	new LabelRule("type::single")];

var dataingestjob = new Dataingestjob(
		"false", "UnitTest/JobRunner/DataIngestJob01/caf/ecmwf_single_${station}_${predictor}_${base-datetime-utc}.caf",
		"f10", "false", "true", "10",
		"TestData/JobRunner/netcdf/flags/uk_hires_ec_det_${base-hour-utc}Z.${base-date6-utc}${base-hour-utc}",
		"43200", "0", label_rule, "TestData/JobRunner/netcdf/uk_hires/*.nc", "81");
var parameters = new Parameters("1", "DataIngestJob", "2008-03-22 00:00:00.000",
		"UnitTest/JobRunner/DataIngestJob01/", dataingestjob);
var domain = new Domain("global", "Global Domain");
var model = new Model("ECMWF", "Test Model 01");

var station = [
   	new Station("UK1", "United Kingdom 1", "00:00"),
   	new Station("DEN1", "Some Station", "00:00"),
   	new Station("DEN2", "Some Station", "00:00"),
   	new Station("SPA1", "Some Station", "00:00")];

var station_region = [
   	new StationRegion("UK1", "55.5", "54.5", "-6.25", "-0.6"),
   	new StationRegion("DEN1", "57.2", "54.87", "7.84", "9.57"),
   	new StationRegion("DEN2", "56.15", "54.84", "10.8", "12.7"),
   	new StationRegion("SPA1", "49.82", "47.49", "-4.89", "-3.63")];

var data = new Data(domain, model, station, station_region);
var dpt = new Dpt(parameters, data);

var xmlEncoder = new XmlEncoder("dpt", dpt);
xmlEncoder.dtdLocation = "../DataIngestJob.dtd";
xmlEncoder.validationMethod = "DTD";
var output = xmlEncoder.toString();
