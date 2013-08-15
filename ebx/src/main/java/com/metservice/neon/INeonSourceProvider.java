/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;
import java.util.List;

import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
public interface INeonSourceProvider {

	public void copy(String qccPathFrom, String qccPathTo)
			throws EsSourceLoadException, EsSourceSaveException;

	public INeonSourceDescriptor descriptor(String qccPath);

	public boolean exists(String qccPath);

	public void freshSource(String qccPath, String ozSource)
			throws EsSourceSaveException;

	public void makeDirectory(String qccPath)
			throws EsSourceSaveException;

	public File ocndirHome();

	public void putSource(String qccPath, String ozSource)
			throws EsSourceSaveException;

	public void registerProbe(IArgonFileProbe probe);

	public void remove(String qccPath)
			throws EsSourceSaveException;

	public void removeDirectory(String qccPath)
			throws EsSourceSaveException;

	public void rename(String qccPathFrom, String qccPathTo)
			throws EsSourceSaveException;

	public void renameDirectory(String qccPathFrom, String qccPathTo)
			throws EsSourceSaveException;

	public String source(String qccPath)
			throws EsSourceLoadException;

	public List<? extends INeonSourceDescriptor> zlDescriptorsAsc(String zccPath);
}
