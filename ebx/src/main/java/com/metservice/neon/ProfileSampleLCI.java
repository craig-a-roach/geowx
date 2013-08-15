/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;


/**
 * @author roach
 */
class ProfileSampleLCI {
	void done(long ns) {
		nsCum += ns;
		count++;
	}

	String qInfo() {
		final StringBuilder b = new StringBuilder();
		b.append("*");
		b.append(count);
		b.append("=");
		b.append(UNeonProfile.qTiming(nsCum));
		return b.toString();
	}

	static ProfileSampleLCI union(ProfileSampleLCI oLhs, ProfileSampleLCI oRhs) {
		if (oLhs == null) return oRhs;
		if (oRhs == null) return oLhs;

		final ProfileSampleLCI lhs = oLhs;
		final ProfileSampleLCI rhs = oRhs;

		final ProfileSampleLCI neo = new ProfileSampleLCI();
		neo.nsCum = lhs.nsCum + rhs.nsCum;
		neo.count = lhs.count + rhs.count;

		return neo;
	}

	public ProfileSampleLCI() {
	}
	long nsCum;
	int count;
}
