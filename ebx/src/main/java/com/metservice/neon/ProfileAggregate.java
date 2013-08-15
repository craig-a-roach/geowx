/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
class ProfileAggregate {

	public ProfileAggregate newAggregate(ProfileSample neo) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		final boolean consistent = lineCount == neo.lineCount && checksum == neo.checksum;
		if (!consistent) return newInitial(neo);
		final ProfileSamplePI union = ProfileSamplePI.union(program, neo.program);
		return new ProfileAggregate(lineCount, checksum, sourceHtml, union);
	}

	public static ProfileAggregate newInitial(ProfileSample neo) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		return new ProfileAggregate(neo.lineCount, neo.checksum, neo.sourceHtml, neo.program);
	}

	private ProfileAggregate(int lineCount, int checksum, EsSourceHtml sourceHtml, ProfileSamplePI program) {
		assert sourceHtml != null;
		assert program != null;
		this.lineCount = lineCount;
		this.checksum = checksum;
		this.sourceHtml = sourceHtml;
		this.program = program;
	}
	final int lineCount;
	final int checksum;
	final EsSourceHtml sourceHtml;
	final ProfileSamplePI program;
}
