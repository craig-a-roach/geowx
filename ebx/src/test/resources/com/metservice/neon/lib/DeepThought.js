AUTHOR('ben');
PURPOSE('DeepThought Job Runner Interface');

function Dataingestjob(activate_post_processing, caf_file_dropoff_location, caf_output_variable,
		delete_ingested, do_station_interpolation, flag_file_check_interval, flag_file_path,
		flag_file_timeout, ingest_days_back, label_rule, source_path, station_set_id) {
	this.activate_post_processing = activate_post_processing;
	this.caf_file_dropoff_location = caf_file_dropoff_location;
	this.caf_output_variable = caf_output_variable;
	this.delete_ingested = delete_ingested;
	this.do_station_interpolation = do_station_interpolation;
	this.flag_file_check_interval = flag_file_check_interval;
	this.flag_file_path = flag_file_path;
	this.flag_file_timeout = flag_file_timeout;
	this.ingest_days_back = ingest_days_back;
	this.label_rule = label_rule;
	this.source_path = source_path;
	this.station_set_id = station_set_id;
}

function Parameters(job_instance_id, job_class, base_time, data_root_path, dataingestjob) {
	this.job_instance_id = job_instance_id;
	this.job_class = job_class;
	this.base_time = base_time;
	this.data_root_path = data_root_path;
	this.dataingestjob = dataingestjob;
}

function Domain(name, description) {
	this.name = name;
	this.description = description;
}

function Model(name, description) {
	this.name = name;
	this.description = description;
}

function Station(station_id, name, offset_to_utc) {
	this.station_id = station_id;
	this.name = name;
	this.offset_to_utc = offset_to_utc;
}

function StationRegion(station_id, north, south, west, east) {
	this.station_id = station_id;
	this.north = north;
	this.south = south;
	this.west = west;
	this.east = east;
}

function Data(domain, model, station, station_region) {
	this.domain = domain;
	this.model = model;
	this.station = station;
	this.station_region = station_region;
}

function Dpt(parameters, data) {
	this.parameters = parameters;
	this.data = data;
}

function LabelRule(text) {
	this.TEXT = text;
}
