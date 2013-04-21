/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.journal;

import java.io.File;

/**
 * @author roach
 */
public interface IArgonJournalListener {
	public void saved(String qccType, long serial, File entryFile);
}
