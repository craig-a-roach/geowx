/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.HashSet;
import java.util.Set;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
class TestImpProbe implements IKryptonProbe {

	@Override
	public void codeNotFound(String source, String type, String resourceKey, int code) {
		m_set.add("codeNotFound");
	}

	public boolean contains(String subject) {
		return m_set.contains(subject);
	}

	@Override
	public void parameterResourceParse(String resourceKey, JsonObject oError) {
		m_set.add("parameterResourceParse");
	}

	@Override
	public void parameterResourceParse(String resourceKey, JsonSchemaException ex) {
		m_set.add("parameterResourceParse");
	}

	@Override
	public void resourceNotFound(String type, String resourceKey) {
		m_set.add("resourceNotFound");
	}

	@Override
	public void resourceParse(String type, String resourceKey, String problem) {
		m_set.add("resourceParse");
	}

	@Override
	public void resourceQuota(String type, String resourceKey, ArgonQuotaException ex) {
		m_set.add("resourceQuota");
	}

	@Override
	public void resourceRead(String type, String resourceKey, ArgonStreamReadException ex) {
		m_set.add("resourceRead");
	}

	@Override
	public void software(String attempted, String ozContainment, Throwable cause) {
		m_set.add("software");
	}

	public TestImpProbe() {
	}

	private final Set<String> m_set = new HashSet<String>();
}
