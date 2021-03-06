/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class GeneratingProcessTable extends Code8Table<CodeDescription> implements IDescriptiveResourceTable<GeneratingProcessTable> {

	public void putDescription(int code, String qcctwValue) {
		put(code, new CodeDescription(code, qcctwValue));
	}

	public GeneratingProcessTable(String qccKey) {
		super(CodeDescription.class, qccKey);
	}
}
