/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.metservice.argon.text.ArgonJoiner;

/**
 * @author roach
 */
public class EsSourceMeta {

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Authors: ").append(qAuthors).append('\n');
		sb.append("Purpose: ").append(qPurpose);
		return sb.toString();
	}

	private static String qAuthors(Set<String> xsAuthors) {
		final List<String> zlAuthors = new ArrayList<String>(xsAuthors);
		Collections.sort(zlAuthors);
		final String zAuthors = ArgonJoiner.zComma(zlAuthors);
		return zAuthors.length() == 0 ? "anonymous" : zAuthors;
	}

	public EsSourceMeta(Set<String> xsAuthors, String qPurpose) {
		if (xsAuthors == null) throw new IllegalArgumentException("object is null");
		if (qPurpose == null || qPurpose.length() == 0) throw new IllegalArgumentException("string is null or empty");
		this.xsAuthors = xsAuthors;
		this.qPurpose = qPurpose;
		this.qAuthors = qAuthors(xsAuthors);
	}

	public final Set<String> xsAuthors;
	public final String qPurpose;
	public final String qAuthors;
}
