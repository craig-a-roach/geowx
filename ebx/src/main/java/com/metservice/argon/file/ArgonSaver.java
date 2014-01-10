/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Iterator;

import com.metservice.argon.ArgonLockException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
public class ArgonSaver {

	public void save(IArgonFileProbe probe, File destFile, boolean lock, Iterator<byte[]> iArray)
			throws ArgonPermissionException, ArgonLockException, ArgonStreamWriteException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (destFile == null) throw new IllegalArgumentException("object is null");
		if (iArray == null) throw new IllegalArgumentException("object is null");

		final FOut fout = FOut.newInstance(probe, destFile, lock);
		final ByteBuffer buffer = m_bufferPool.get();
		try {
			int biSrc = 0;
			int bcSrcRem = 0;
			byte[] ozptSource = null;
			boolean moreArrays = true;
			while (moreArrays) {
				buffer.clear();
				int bcDestRem = buffer.remaining();
				while (bcDestRem > 0 && moreArrays) {
					if (ozptSource == null) {
						if (iArray.hasNext()) {
							ozptSource = iArray.next();
							bcSrcRem = ozptSource.length;
							biSrc = 0;
						} else {
							moreArrays = false;
						}
					}
					if (ozptSource != null) {
						final int bcXfer = Math.min(bcSrcRem, bcDestRem);
						buffer.put(ozptSource, biSrc, bcXfer);
						biSrc += bcXfer;
						bcSrcRem -= bcXfer;
						bcDestRem -= bcXfer;
						if (bcSrcRem == 0) {
							ozptSource = null;
						}
					}
				}
				buffer.flip();
				fout.save(probe, buffer);
			}
		} finally {
			fout.close(probe);
		}
	}

	public ArgonSaver(int bcBufferCapacity) {
		m_bufferPool = new ThreadLocalBufferPool(bcBufferCapacity, true);
	}

	private final ThreadLocalBufferPool m_bufferPool;
}
