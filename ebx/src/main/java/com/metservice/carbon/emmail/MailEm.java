/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emmail;

import com.metservice.beryllium.BerylliumSmtpManager;
import com.metservice.neon.EmViewObject;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
class MailEm extends EmViewObject {

	public BerylliumSmtpManager imp() {
		return m_imp;
	}

	@Override
	public void putProperties(EsExecutionContext ecx)
			throws InterruptedException {
	}

	public MailEm(BerylliumSmtpManager imp) {
		super(MailEmClass.Instance);
		if (imp == null) throw new IllegalArgumentException("object is null");
		m_imp = imp;
	}

	private final BerylliumSmtpManager m_imp;
}
