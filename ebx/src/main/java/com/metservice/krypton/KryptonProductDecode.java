/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.cobalt.CobaltMember;
import com.metservice.cobalt.CobaltParameter;
import com.metservice.cobalt.ICobaltPrognosis;
import com.metservice.cobalt.ICobaltSurface;

/**
 * @author roach
 */
public class KryptonProductDecode {

	public KryptonProductDecode(CobaltParameter parameter, ICobaltPrognosis prognosis, ICobaltSurface surface,
			CobaltMember member, IKryptonName generatingProcess) {
		if (parameter == null) throw new IllegalArgumentException("object is null");
		if (prognosis == null) throw new IllegalArgumentException("object is null");
		if (member == null) throw new IllegalArgumentException("object is null");
		if (generatingProcess == null) throw new IllegalArgumentException("object is null");
		this.parameter = parameter;
		this.prognosis = prognosis;
		this.surface = surface;
		this.member = member;
		this.generatingProcess = generatingProcess;
	}
	public final CobaltParameter parameter;
	public final ICobaltPrognosis prognosis;
	public final ICobaltSurface surface;
	public final CobaltMember member;
	public final IKryptonName generatingProcess;
}
