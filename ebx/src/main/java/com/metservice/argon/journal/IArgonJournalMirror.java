/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.journal;

import com.metservice.argon.Binary;

/**
 * @author roach
 */
public interface IArgonJournalMirror {

	public void commit(ArgonJournalTx tx);

	public void saved(ArgonJournalTx tx, Binary source);
}
