package com.metservice.cobalt;

import java.util.Date;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.DateFormatter;
import com.metservice.argon.HashCoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

public class CobaltAnalysis implements ICobaltCoordinate {

	public static final String PName_time = "t";

	@Override
	public void addTo(KmlFeatureText kft) {
		kft.addText(DateFormatter.newYMDHFromTs(ts));
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltAnalysis) {
			final CobaltAnalysis r = (CobaltAnalysis) rhs;
			return ArgonCompare.fwd(ts, r.ts);
		}
		throw new IllegalArgumentException("invalid rhs>" + rhs + "<");
	}

	@Override
	public CobaltDimensionName dimensionName() {
		return CobaltDimensionName.Analysis;
	}

	@Override
	public CobaltDimensionSet dimensionSet() {
		return CobaltDimensionSet.Analysis;
	}

	public boolean equals(CobaltAnalysis rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return ts == rhs.ts;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltAnalysis)) return false;
		return equals((CobaltAnalysis) o);
	}

	@Override
	public int hashCode() {
		return HashCoder.field(ts);
	}

	@Override
	public void saveTo(JsonObject dst) {
		dst.putTime(PName_time, ts);
	}

	@Override
	public String show() {
		return CobaltNCube.ShowCL + DateFormatter.newYMDHFromTs(ts) + CobaltNCube.ShowCR;
	}

	@Override
	public String toString() {
		return show();
	}

	public static CobaltAnalysis newInstance(JsonObject src)
			throws JsonSchemaException {
		final long ts = src.accessor(PName_time).datumTs();
		return new CobaltAnalysis(ts);
	}

	public CobaltAnalysis(Date date) {
		if (date == null) throw new IllegalArgumentException("object is null");
		this.ts = date.getTime();
	}

	public CobaltAnalysis(long ts) {
		this.ts = ts;
	}

	public final long ts;
}
