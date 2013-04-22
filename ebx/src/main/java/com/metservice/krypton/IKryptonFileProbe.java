/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.File;

import com.metservice.cobalt.CobaltDimensionException;

/**
 * @author roach
 */
public interface IKryptonFileProbe extends IKryptonProbe {

	public void gridFileCode(File gridFile, int recordIndex, int subIndex, KryptonCodeException ex);

	public void gridFileCode(File gridFile, int recordIndex, int subIndex, String problem);

	public void gridFileDimension(File gridFile, int recordIndex, int subIndex, CobaltDimensionException ex);

	public void gridFileRead(File gridFile, KryptonReadException ex);

	public void gridFileRead(File gridFile, String problem);

	public void gridFileRecord(File gridFile, KryptonRecordException ex);

	public void gridFileTable(File gridFile, int recordIndex, int subIndex, KryptonTableException ex);

	public void gridFileUnpack(File gridFile, int recordIndex, int subIndex, KryptonUnpackException ex);

	public void gridFileUnsupported(File gridFile, int recordIndex, int subIndex, KryptonUnsupportedException ex);
}
