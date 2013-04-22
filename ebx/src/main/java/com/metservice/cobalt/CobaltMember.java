package com.metservice.cobalt;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

public class CobaltMember implements ICobaltCoordinate {

	public static final String PName_id = "id";

	public static final CobaltMember Singleton = new CobaltMember(-1);
	public static final CobaltMember Control = new CobaltMember(0);

	@Override
	public void addTo(KmlFeatureText kft) {
		if (id < 0) {
			kft.addText("-");
		} else {
			kft.addTextInt(id);
		}
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltMember) {
			final CobaltMember r = (CobaltMember) rhs;
			return ArgonCompare.fwd(id, r.id);
		}
		throw new IllegalArgumentException("invalid rhs>" + rhs + "<");
	}

	@Override
	public CobaltDimensionName dimensionName() {
		return CobaltDimensionName.Member;
	}

	@Override
	public CobaltDimensionSet dimensionSet() {
		return CobaltDimensionSet.Member;
	}

	public boolean equals(CobaltMember rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return id == rhs.id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltMember)) return false;
		return equals((CobaltMember) o);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public void saveTo(JsonObject dst) {
		dst.putInteger(PName_id, id);
	}

	@Override
	public String show() {
		return id < 0 ? "-" : ("#" + id);
	}

	@Override
	public String toString() {
		return show();
	}

	public static CobaltMember newInstance(int id) {
		if (id < 0) return Singleton;
		if (id == 0) return Control;
		return new CobaltMember(id);
	}

	public static CobaltMember newInstance(JsonObject src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		return newInstance(src.accessor(PName_id).datumInteger());
	}

	private CobaltMember(int id) {
		this.id = id;
	}

	public final int id;
}
