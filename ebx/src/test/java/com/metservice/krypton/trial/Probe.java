/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.trial;

import java.io.File;

import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Ds;
import com.metservice.argon.json.JsonEncoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;
import com.metservice.krypton.IKryptonProbe;
import com.metservice.krypton.KryptonCodeException;
import com.metservice.krypton.KryptonReadException;
import com.metservice.krypton.KryptonRecordException;
import com.metservice.krypton.KryptonTableException;
import com.metservice.nickel.NickelProbeErr;
import com.metservice.nickel.NickelProbeLimiter;

/**
 * @author roach
 */
class Probe implements IKryptonProbe {

	@Override
	public void codeNotFound(String source, String type, String resourceKey, int code) {
		final Ds op = Ds.g().a("source", source).a("type", type);
		op.a("code", code).a("resource", resourceKey);
		m_err.emit("CodeNotFound", "SkipCode", op);
	}

	public void cubeFileDecode(File cubeStateFile, ArgonFormatException ex) {
		final Ds op = Ds.g().a("path", cubeStateFile.getAbsolutePath());
		m_err.emit("CubeFileDecode", "ResetState", op, Ds.message(ex));
	}

	public void cubeFileDecode(File cubeStateFile, JsonSchemaException ex) {
		final Ds op = Ds.g().a("path", cubeStateFile.getAbsolutePath());
		m_err.emit("CubeFileDecode", "ResetState", op, Ds.message(ex));
	}

	public void cubeFileKmlRender(File cubeStateFile, Throwable ex) {
		final Ds op = Ds.g().a("sourcePath", cubeStateFile.getAbsolutePath());
		m_err.emit("CubeFileKmlRender", "SkipView", op, Ds.message(ex));
	}

	public void emitEpilogue() {
		m_err.emitElided();
	}

	public void gridFileCode(File gridFile, int recordIndex, KryptonCodeException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath()).a("recordIndex", recordIndex);
		m_err.emit("GridFileCode", "SkipRecord", op, Ds.message(ex));
	}

	public void gridFileCode(File gridFile, int recordIndex, String problem) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath()).a("recordIndex", recordIndex);
		m_err.emit("GridFileCode", "SkipRecord", op, problem);
	}

	public void gridFileRead(File gridFile, KryptonReadException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath());
		m_err.emit("GridFileRead", "SkipFile", op, Ds.message(ex));
	}

	public void gridFileRecord(File gridFile, KryptonRecordException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath());
		m_err.emit("GridFileRecord", "SkipRecord", op, Ds.message(ex));
	}

	public void gridFileTable(File gridFile, int recordIndex, KryptonTableException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath()).a("recordIndex", recordIndex);
		m_err.emit("GridFileTable", "SkipRecord", op, Ds.message(ex));
	}

	@Override
	public void parameterResourceParse(String resourceKey, JsonObject oError) {
		final Ds op = Ds.g().a("resource", resourceKey);
		final String dev = oError == null ? "nil" : JsonEncoder.Debug.encode(oError);
		m_err.emit("ParameterResourceParse", "SkipResource", op, dev);
	}

	@Override
	public void parameterResourceParse(String resourceKey, JsonSchemaException ex) {
		final Ds op = Ds.g().a("resource", resourceKey);
		m_err.emit("ParameterResourceParse", "SkipResource", op, Ds.message(ex));
	}

	@Override
	public void resourceNotFound(String type, String resourceKey) {
		final Ds op = Ds.g().a("type", type).a("resource", resourceKey);
		m_err.emit("ResourceNotFound", "SkipResource", op, null);
	}

	@Override
	public void resourceParse(String type, String resourceKey, String problem) {
		final Ds op = Ds.g().a("type", type).a("resource", resourceKey);
		m_err.emit("ResourceParse", "SkipResource", op, problem);
	}

	@Override
	public void resourceQuota(String type, String resourceKey, ArgonQuotaException ex) {
		final Ds op = Ds.g().a("type", type).a("resource", resourceKey);
		m_err.emit("ResourceQuota", "SkipResource", op, Ds.message(ex));
	}

	@Override
	public void resourceRead(String type, String resourceKey, ArgonStreamReadException ex) {
		final Ds op = Ds.g().a("type", type).a("resource", resourceKey);
		m_err.emit("ResourceRead", "SkipResource", op, Ds.message(ex));
	}

	@Override
	public void software(String attempted, String ozContainment, Throwable cause) {
		final Ds op = Ds.g().a("attempted", attempted);
		m_err.emit("Software", ozContainment, op, Ds.message(cause));
	}

	Probe(int reportLimit) {
		final NickelProbeLimiter limiter = new NickelProbeLimiter(reportLimit);
		m_err = new NickelProbeErr("krypton.trial", limiter);
	}

	private final NickelProbeErr m_err;
}
