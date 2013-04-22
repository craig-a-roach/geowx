/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.xenon;

import java.net.URL;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.Ds;
import com.metservice.argon.file.ArgonUrlManagement;

/**
 * @author roach
 */
public class XenonCanvasFactoryCfg {

	private static URL newThemeHome(Builder b)
			throws XenonCfgSyntaxException {
		try {
			return ArgonUrlManagement.newUrl(b.themeHomeUrlSpec);
		} catch (final ArgonApiException ex) {
			final String m = "Malformed theme home URL spec..." + ex.getMessage();
			throw new XenonCfgSyntaxException(m);
		}
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static XenonCanvasFactoryCfg newInstance(Builder b)
			throws XenonCfgSyntaxException {
		if (b == null) throw new IllegalArgumentException("object is null");
		final URL themeHome = newThemeHome(b);
		return new XenonCanvasFactoryCfg(themeHome);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("themeHome", themeHome);
		return ds.s();
	}

	private XenonCanvasFactoryCfg(URL themeHome) {
		assert themeHome != null;
		this.themeHome = themeHome;
	}
	public final URL themeHome;

	public static class Builder {

		@Override
		public String toString() {
			final Ds ds = Ds.o("XenonCanvasFactoryCfg.Builder");
			ds.a("themeHomeSpec", themeHomeUrlSpec);
			return ds.s();
		}

		private Builder() {
		}
		public String themeHomeUrlSpec = CDefault.ThemeHomeUrlSpec;
	}

}
