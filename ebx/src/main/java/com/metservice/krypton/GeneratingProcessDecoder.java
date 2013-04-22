/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Binary;

/**
 * @author roach
 */
class GeneratingProcessDecoder extends ResourceFolderDecoder<GeneratingProcessTable> {

	@Override
	GeneratingProcessTable newTable(String qccTableKey) {
		return new GeneratingProcessTable(qccTableKey);
	}

	@Override
	boolean parseTableSource(GeneratingProcessTable target, Binary source) {
		return parseDelimitedTableSource(target, source);
	}

	public CodeDescription select(String source, int code)
			throws KryptonTableException, KryptonCodeException {
		final GeneratingProcessTable table = loadResourceTable(CKrypton.FileWMO);
		return table.select(probe, source, code);
	}

	protected GeneratingProcessDecoder(IKryptonProbe probe) {
		super(probe, "generatingProcess", "decode/genproc/", CKrypton.FileSuffix_TXT);
	}
}
