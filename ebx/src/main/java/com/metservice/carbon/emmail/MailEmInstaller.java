/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emmail;

import com.metservice.beryllium.BerylliumSmtpManager;
import com.metservice.beryllium.BerylliumSmtpUrl;
import com.metservice.neon.EmAbstractInstaller;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
public class MailEmInstaller extends EmAbstractInstaller {

	private static void registerDefault(BerylliumSmtpManager imp) {
		assert imp != null;
		final BerylliumSmtpUrl url = BerylliumSmtpUrl.newInstance(CDefault.Host, CDefault.UserName);
		imp.register(CDefault.ConfigurationId, url, CDefault.Password, CDefault.Secure);
	}

	@Override
	public void install(EsExecutionContext ecx)
			throws InterruptedException {
		putView(ecx, CProp.GlobalMail, m_em);
	}

	public MailEmInstaller(BerylliumSmtpManager imp) {
		if (imp == null) throw new IllegalArgumentException("object is null");
		registerDefault(imp);
		m_em = new MailEm(imp);
	}

	private final MailEm m_em;
}
