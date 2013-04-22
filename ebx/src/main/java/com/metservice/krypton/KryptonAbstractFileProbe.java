/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.File;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Ds;
import com.metservice.argon.json.JsonEncoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;
import com.metservice.cobalt.CobaltDimensionException;

/**
 * @author roach
 */
public abstract class KryptonAbstractFileProbe implements IKryptonFileProbe {

	protected static final String SkipCode = "SkipCode";
	protected static final String SkipRecord = "SkipRecord";
	protected static final String SkipFile = "SkipFile";
	protected static final String SkipResource = "SkipResource";

	protected static final String GridFileDimension = "GridFileDimension";
	protected static final String GridFileCode = "GridFileCode";
	protected static final String CodeNotFound = "CodeNotFound";
	protected static final String GridFileRead = "GridFileRead";
	protected static final String GridFileRecord = "GridFileRecord";
	protected static final String GridFileTable = "GridFileTable";
	protected static final String GridFileUnpack = "GridFileUnpack";
	protected static final String GridFileUnsupported = "GridFileUnsupported";
	protected static final String ParameterResourceParse = "ParameterResourceParse";
	protected static final String ResourceNotFound = "ResourceNotFound";
	protected static final String ResourceParse = "ResourceParse";
	protected static final String ResourceQuota = "ResourceQuota";
	protected static final String ResourceRead = "ResourceRead";
	protected static final String Software = "Software";

	protected abstract void emit(String subject, String ozContainment, Ds oOperator, String ozDeveloper);

	@Override
	public void codeNotFound(String source, String type, String resourceKey, int code) {
		final Ds op = Ds.g().a("source", source).a("type", type);
		op.a("code", code).a("resource", resourceKey);
		emit(CodeNotFound, SkipCode, op, null);
	}

	@Override
	public void gridFileCode(File gridFile, int recordIndex, int subIndex, KryptonCodeException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath()).a("recordIndex", recordIndex).a("subIndex", subIndex);
		emit(GridFileCode, SkipRecord, op, Ds.message(ex));
	}

	@Override
	public void gridFileCode(File gridFile, int recordIndex, int subIndex, String problem) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath()).a("recordIndex", recordIndex).a("subIndex", subIndex);
		emit(GridFileCode, SkipRecord, op, problem);
	}

	@Override
	public void gridFileDimension(File gridFile, int recordIndex, int subIndex, CobaltDimensionException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath()).a("recordIndex", recordIndex).a("subIndex", subIndex);
		emit(GridFileDimension, SkipRecord, op, Ds.message(ex));
	}

	@Override
	public void gridFileRead(File gridFile, KryptonReadException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath());
		emit(GridFileRead, SkipFile, op, Ds.message(ex));
	}

	@Override
	public void gridFileRead(File gridFile, String problem) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath());
		emit(GridFileRead, SkipFile, op, problem);
	}

	@Override
	public void gridFileRecord(File gridFile, KryptonRecordException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath());
		emit(GridFileRecord, SkipRecord, op, Ds.message(ex));
	}

	@Override
	public void gridFileTable(File gridFile, int recordIndex, int subIndex, KryptonTableException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath()).a("recordIndex", recordIndex).a("subIndex", subIndex);
		emit(GridFileTable, SkipRecord, op, Ds.message(ex));
	}

	@Override
	public void gridFileUnpack(File gridFile, int recordIndex, int subIndex, KryptonUnpackException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath()).a("recordIndex", recordIndex).a("subIndex", subIndex);
		emit(GridFileUnpack, SkipRecord, op, Ds.message(ex));
	}

	@Override
	public void gridFileUnsupported(File gridFile, int recordIndex, int subIndex, KryptonUnsupportedException ex) {
		final Ds op = Ds.g().a("path", gridFile.getAbsolutePath()).a("recordIndex", recordIndex).a("subIndex", subIndex);
		emit(GridFileUnsupported, SkipRecord, op, Ds.message(ex));
	}

	@Override
	public void parameterResourceParse(String resourceKey, JsonObject oError) {
		final Ds op = Ds.g().a("resource", resourceKey);
		final String dev = oError == null ? "nil" : JsonEncoder.Debug.encode(oError);
		emit(ParameterResourceParse, SkipResource, op, dev);
	}

	@Override
	public void parameterResourceParse(String resourceKey, JsonSchemaException ex) {
		final Ds op = Ds.g().a("resource", resourceKey);
		emit(ParameterResourceParse, SkipResource, op, Ds.message(ex));
	}

	@Override
	public void resourceNotFound(String type, String resourceKey) {
		final Ds op = Ds.g().a("type", type).a("resource", resourceKey);
		emit(ResourceNotFound, SkipResource, op, null);
	}

	@Override
	public void resourceParse(String type, String resourceKey, String problem) {
		final Ds op = Ds.g().a("type", type).a("resource", resourceKey);
		emit(ResourceParse, SkipResource, op, problem);
	}

	@Override
	public void resourceQuota(String type, String resourceKey, ArgonQuotaException ex) {
		final Ds op = Ds.g().a("type", type).a("resource", resourceKey);
		emit(ResourceQuota, SkipResource, op, Ds.message(ex));
	}

	@Override
	public void resourceRead(String type, String resourceKey, ArgonStreamReadException ex) {
		final Ds op = Ds.g().a("type", type).a("resource", resourceKey);
		emit(ResourceRead, SkipResource, op, Ds.message(ex));
	}

	@Override
	public void software(String attempted, String ozContainment, Throwable cause) {
		final Ds op = Ds.g().a("action attempted", attempted);
		emit(Software, ozContainment, op, Ds.message(cause));
	}

	protected KryptonAbstractFileProbe() {
	}
}
