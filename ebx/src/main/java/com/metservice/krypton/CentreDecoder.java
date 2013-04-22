/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Binary;
import com.metservice.argon.text.ArgonNumber;

/**
 * @author roach
 */
class CentreDecoder extends ResourceFolderDecoder<CentreTable> {

	@Override
	CentreTable newTable(String qccTableKey) {
		return new CentreTable(qccTableKey);
	}

	@Override
	boolean parseTableSource(CentreTable target, Binary source) {
		return parseDelimitedTableSource(target, source);
	}

	public CodeDescription find(String source, int centre, int subCentre)
			throws KryptonTableException, KryptonCodeException {
		if (subCentre <= 0) return null;
		final String qccCentreKey = CKrypton.FilePrefix_Centre + ArgonNumber.intToDec3(centre);
		final CentreTable table = loadResourceTable(qccCentreKey);
		return table.select(probe, source, subCentre);
	}

	public CodeDescription select(String source, int centre)
			throws KryptonTableException, KryptonCodeException {
		final CentreTable table = loadResourceTable(CKrypton.FileWMO);
		return table.select(probe, source, centre);
	}

	public CentreDecoder(IKryptonProbe probe) {
		super(probe, "centre", "decode/centre/", CKrypton.FileSuffix_TXT);
	}
}
