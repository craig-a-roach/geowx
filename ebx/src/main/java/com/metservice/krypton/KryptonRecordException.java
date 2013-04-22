/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonRecordException extends Exception {

	public KryptonRecordException(String section, long biRecReFile, long biPosReRec, int recIndex, String problem) {
		super("Unexpected '" + section + "' section layout at " + biPosReRec + " bytes into record #" + recIndex
				+ ". This record started at file position " + biRecReFile + ". " + problem);
	}
}
