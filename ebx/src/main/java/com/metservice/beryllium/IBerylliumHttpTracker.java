/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
public interface IBerylliumHttpTracker {

	public void awaitOutcome()
			throws InterruptedException;

	public void awaitOutcome(Elapsed awaitInterval)
			throws InterruptedException;

	public void raiseCompleteMalformedResponse()
			throws InterruptedException;

	public void raiseStatusUnexpected(int status)
			throws InterruptedException;

	public void raiseUnresponsive()
			throws InterruptedException;

	public boolean tryOutcome()
			throws InterruptedException;

	public boolean tryOutcome(Elapsed awaitInterval)
			throws InterruptedException;
}
