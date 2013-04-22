/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public interface IKryptonProbe {

	public void codeNotFound(String source, String type, String resourceKey, int code);

	public void parameterResourceParse(String resourceKey, JsonObject oError);

	public void parameterResourceParse(String resourceKey, JsonSchemaException ex);

	public void resourceNotFound(String type, String resourceKey);

	public void resourceParse(String type, String resourceKey, String problem);

	public void resourceQuota(String type, String resourceKey, ArgonQuotaException ex);

	public void resourceRead(String type, String resourceKey, ArgonStreamReadException ex);

	public void software(String attempted, String ozContainment, Throwable cause);
}
