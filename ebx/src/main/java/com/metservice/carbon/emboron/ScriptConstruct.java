/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.util.ArrayList;
import java.util.List;

import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.boron.IBoronScriptResource;

/**
 * @author roach
 */
class ScriptConstruct {

	public void addResource(String qtwPath, Binary content) {
		if (qtwPath == null || qtwPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (content == null) throw new IllegalArgumentException("object is null");
		zlResources.add(new BResource(qtwPath, content));
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("lines", xlzLines);
		ds.a("resources", "resources");
		return ds.s();
	}

	public ScriptConstruct(List<String> xlzLines) {
		if (xlzLines == null) throw new IllegalArgumentException("object is null");
		this.xlzLines = xlzLines;
	}

	public final List<String> xlzLines;
	public final List<IBoronScriptResource> zlResources = new ArrayList<IBoronScriptResource>();

	private static class BResource implements IBoronScriptResource {

		@Override
		public String qccRelativePath() {
			return m_qtwPath;
		}

		@Override
		public String toString() {
			return m_qtwPath;
		}

		@Override
		public byte[] zptContent() {
			return m_content.zptReadOnly;
		}

		public BResource(String qtwPath, Binary content) {
			m_qtwPath = qtwPath;
			m_content = content;
		}

		private final String m_qtwPath;
		private final Binary m_content;
	}
}
