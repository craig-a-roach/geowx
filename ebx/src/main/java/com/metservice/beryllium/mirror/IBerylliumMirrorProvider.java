/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import java.util.List;

import com.metservice.argon.Binary;
import com.metservice.beryllium.BerylliumBinaryHttpPayload;

/**
 * @author roach
 */
public interface IBerylliumMirrorProvider {

	public void commit(String qccWipPath);

	public List<String> commitPathsAsc(List<String> zlWipPathsAsc);

	public BerylliumBinaryHttpPayload createPayload(String qccPath);

	public List<String> discoverDemandPathsAsc(String qcctwFromPath);

	public String discoverHiexPath();

	public List<String> discoverWipPathsAsc();

	public void onCommitComplete(IBerylliumMirrorCommitTask task);

	public void onHttpRetry();

	public void onSaveComplete(IBerylliumMirrorSaveTask task);

	public void onSynchronizationPoint();

	public void save(String qccPath, Binary content, long tsLastModified);
}
