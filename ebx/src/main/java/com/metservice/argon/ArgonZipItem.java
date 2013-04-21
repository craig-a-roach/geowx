/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Date;


/**
 * @author roach
 */
public class ArgonZipItem implements Comparable<ArgonZipItem> {

	public int compareTo(ArgonZipItem r) {
		return qccFileName.compareTo(r.qccFileName);
	}

	@Override
	public String toString() {
		return qccFileName;
	}

	ArgonZipItem(String qccFileName, Date lastModifiedAt, Binary content) {
		assert qccFileName != null && qccFileName.length() > 0;
		assert lastModifiedAt != null;
		assert content != null;
		this.qccFileName = qccFileName;
		this.lastModifiedAt = lastModifiedAt;
		this.content = content;
	}

	public final String qccFileName;

	public final Date lastModifiedAt;

	public final Binary content;
}
