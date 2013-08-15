/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * 
 * @author roach
 */
public class EsScopeChain {
	/**
	 * Resolve reference to an object property.
	 * 
	 * @see ECMA 10.1.4
	 * @param qccPropertyName
	 *              [<i>1+ char</i>]
	 * @return [<i>never null</i>]
	 */
	public EsReference resolve(String qccPropertyName) {
		if (qccPropertyName == null || qccPropertyName.length() == 0)
			throw new IllegalArgumentException("qccPropertyName is empty");
		for (int i = m_xptResolvers.length - 1; i >= 0; i--) {
			final EsObject resolver = m_xptResolvers[i];
			if (resolver.esHasProperty(qccPropertyName)) return new EsReference(resolver, qccPropertyName);
		}
		return new EsReference(null, qccPropertyName);
	}

	public EsScopeChain(EsObject resolver) {
		m_xptResolvers = new EsObject[1];
		m_xptResolvers[0] = resolver;
	}

	public EsScopeChain(EsScopeChain base, EsObject resolver) {
		final int baseDepth = base.m_xptResolvers.length;
		m_xptResolvers = new EsObject[baseDepth + 1];
		for (int i = 0; i < baseDepth; i++) {
			m_xptResolvers[i] = base.m_xptResolvers[i];
		}
		m_xptResolvers[baseDepth] = resolver;
	}

	private final EsObject[] m_xptResolvers;
}
