/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.trial;

import com.metservice.argon.CSystemExit;
import com.metservice.krypton.KryptonCentre;

/**
 * @author roach
 */
class Outcome {

	public void emitReport() {
		if (m_oCentre != null) {
			System.out.println("Provider=" + m_oCentre);
		}
		System.out.println("Files: good=" + m_filesGood + ", partial=" + m_filesPartial + ", skipped=" + m_filesSkipped);
		System.out.println("Records: good=" + m_recordsGood + ", skipped=" + m_recordsSkipped);
	}

	public int exitCode() {
		if (m_filesSkipped > 0) return CSystemExit.IOError;
		if (m_recordsSkipped > 0) return CExit.GribFileMalformed;
		return CSystemExit.OK;
	}

	public KryptonCentre getCentre() {
		return m_oCentre;
	}

	public void goodFile() {
		m_filesGood++;
	}

	public void goodRecord() {
		m_recordsGood++;
	}

	public void partialFile() {
		m_filesPartial++;
	}

	public void skipFile() {
		m_filesSkipped++;
	}

	public void skipRecord() {
		m_recordsSkipped++;
	}

	public String validateRecordCentre(KryptonCentre centre) {
		if (centre == null) throw new IllegalArgumentException("object is null");
		if (m_oCentre == null) {
			m_oCentre = centre;
			return null;
		}
		if (m_oCentre.equals(centre)) return null;
		return "Centre mismatch; file(set) centre is  '" + m_oCentre + "', record is  '" + centre + "'";
	}

	public Outcome() {
	}
	private KryptonCentre m_oCentre;
	private int m_filesGood;
	private int m_filesPartial;
	private int m_filesSkipped;
	private int m_recordsGood;
	private int m_recordsSkipped;
}
