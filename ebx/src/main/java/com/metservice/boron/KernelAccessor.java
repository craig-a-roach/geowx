/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
class KernelAccessor {

	private String msg(String op) {
		return "Space object  '" + id + "' is in " + m_state + " state; " + op + " not allowed";
	}

	public void onCooling() {
		m_state = State.Cooling;
	}

	public void onDown() {
		m_state = State.Down;
	}

	public void onEnding() {
		m_state = State.Ending;
	}

	public void onStarting()
			throws BoronApiException {
		if (m_state != State.Init) throw new BoronApiException(msg("start"));

		m_state = State.Starting;
	}

	public void onUp() {
		m_state = State.Up;
	}

	@Override
	public String toString() {
		return id + " is " + m_state;
	}

	public void verifyUp(String op)
			throws BoronApiException {
		if (m_state != State.Up && m_state != State.Cooling) throw new BoronApiException(msg(op));
	}

	public void verifyUpNewTran(String op)
			throws BoronApiException {
		if (m_state != State.Up) throw new BoronApiException(msg(op));
	}

	public KernelAccessor(BoronSpaceId id) {
		assert id != null;
		this.id = id;
	}

	final BoronSpaceId id;
	private volatile State m_state = State.Init;

	enum State {
		Init, Starting, Up, Cooling, Ending, Down;
	}
}
