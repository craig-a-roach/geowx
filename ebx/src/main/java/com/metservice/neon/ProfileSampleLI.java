/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author roach
 */
class ProfileSampleLI {

	void callDone(long ns, String qccName) {
		if (omap_Name_CallItem == null) {
			omap_Name_CallItem = new HashMap<String, ProfileSampleLCI>(4);
		}

		ProfileSampleLCI vCallItem = omap_Name_CallItem.get(qccName);
		if (vCallItem == null) {
			vCallItem = new ProfileSampleLCI();
			omap_Name_CallItem.put(qccName, vCallItem);
		}
		vCallItem.done(ns);
	}

	void done(long ns) {
		nsCum += ns;
		count++;
	}

	public int pct(ProfileSamplePI program) {
		if (program == null) throw new IllegalArgumentException("object is null");
		return UNeonProfile.pct(nsCum, program.nsCumLines);
	}

	public String zCallInfo() {
		if (omap_Name_CallItem == null || omap_Name_CallItem.isEmpty()) return "";
		final List<String> xlNames = new ArrayList<String>(omap_Name_CallItem.keySet());
		Collections.sort(xlNames);
		final StringBuilder b = new StringBuilder();
		for (final String qccName : xlNames) {
			final ProfileSampleLCI oCallItem = omap_Name_CallItem.get(qccName);
			if (oCallItem != null) {
				if (b.length() > 0) {
					b.append(", ");
				}
				b.append(qccName);
				b.append(oCallItem.qInfo());
			}
		}
		return b.toString();
	}

	private static Map<String, ProfileSampleLCI> unionMap(Map<String, ProfileSampleLCI> oLhs, Map<String, ProfileSampleLCI> oRhs) {
		if (oLhs == null) return oRhs;
		if (oRhs == null) return oLhs;

		final Map<String, ProfileSampleLCI> neo = new HashMap<String, ProfileSampleLCI>(oLhs);
		for (final Map.Entry<String, ProfileSampleLCI> e : oRhs.entrySet()) {
			final String qKey = e.getKey();
			final ProfileSampleLCI oLhsItem = oLhs.get(qKey);
			final ProfileSampleLCI oRhsItem = e.getValue();
			final ProfileSampleLCI oUnionItem = ProfileSampleLCI.union(oLhsItem, oRhsItem);
			if (oUnionItem != null) {
				neo.put(qKey, oUnionItem);
			}
		}
		return neo;
	}

	public static ProfileSampleLI union(ProfileSampleLI oLhs, ProfileSampleLI oRhs) {
		if (oLhs == null) return oRhs;
		if (oRhs == null) return oLhs;

		final ProfileSampleLI lhs = oLhs;
		final ProfileSampleLI rhs = oRhs;

		final ProfileSampleLI neo = new ProfileSampleLI();
		neo.nsCum = lhs.nsCum + rhs.nsCum;
		neo.count = lhs.count + rhs.count;
		neo.omap_Name_CallItem = unionMap(lhs.omap_Name_CallItem, rhs.omap_Name_CallItem);
		return neo;
	}

	public ProfileSampleLI() {
	}

	long nsCum;
	int count;
	Map<String, ProfileSampleLCI> omap_Name_CallItem;
}
