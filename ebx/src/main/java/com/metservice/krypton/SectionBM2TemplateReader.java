/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
abstract class SectionBM2TemplateReader extends SectionBM2Reader {

	public abstract IKryptonBitmapSource newBitmapSource();

	protected SectionBM2TemplateReader(SectionBM2Reader base, int templateNo) {
		super(base);
		m_templateNo = templateNo;
	}
	protected final int m_templateNo;
}
