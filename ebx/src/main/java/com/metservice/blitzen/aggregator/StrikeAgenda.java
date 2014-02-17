/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

/**
 * @author roach
 */
class StrikeAgenda {

	private void ensure() {
		final int exCap = m_ids.length;
		if (m_count < exCap) return;
		final int[] save = m_ids;
		final int neoCap = exCap * 3 / 2;
		m_ids = new int[neoCap];
		System.arraycopy(save, 0, m_ids, 0, exCap);
	}

	public void add(int id) {
		ensure();
		m_ids[m_count] = id;
		m_count++;
	}

	public int count() {
		return m_count;
	}

	public int id(int index) {
		return m_ids[index];
	}

	public boolean isEmpty() {
		return m_count == 0;
	}

	public int pop() {
		if (m_count == 0) throw new IllegalStateException("agenda is empty");
		m_count--;
		return m_ids[m_count];
	}

	public StrikeAgenda() {
	}
	private int m_count;
	private int[] m_ids = new int[16];
}
