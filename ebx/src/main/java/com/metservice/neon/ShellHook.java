package com.metservice.neon;

import com.metservice.beryllium.BerylliumSupportId;

/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */

/**
 * @author roach
 */
class ShellHook {

	public void runEnd(boolean cleanRun) {
		final String qccSourcePath = source.qccPath();
		shell.debugger().resume(sid, qccSourcePath);
		if (cleanRun) {
			if (oProfileSample != null) {
				oProfileSample.runEnd();
				shell.profiler().addSample(sid, qccSourcePath, oProfileSample);
			}
		}
	}

	public void runStart() {
		if (oProfileSample != null) {
			oProfileSample.runStart();
		}
		if (enabledDebugging) {
			final String qccSourcePath = source.qccPath();
			shell.debugger().start(sid, qccSourcePath);
		}
	}

	public ShellHook(NeonShell sh, EsRequest rq, EsSource source, boolean dbg, ProfileSample oPrf, EsSourceHtml oHtml) {
		if (sh == null) throw new IllegalArgumentException("object is null");
		if (rq == null) throw new IllegalArgumentException("object is null");
		if (source == null) throw new IllegalArgumentException("object is null");

		this.sid = rq.idSupport();
		this.shell = sh;
		this.request = rq;
		this.source = source;
		this.enabledDebugging = dbg;
		this.oSourceHtml = oHtml;
		this.oProfileSample = oPrf;
	}

	public final BerylliumSupportId sid;
	public final NeonShell shell;
	public final EsRequest request;
	public final EsSource source;
	public final boolean enabledDebugging;
	public final EsSourceHtml oSourceHtml;
	public final ProfileSample oProfileSample;
}
