/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights
 * reserved. No part of this work may be stored in a retrievable system,
 * transmitted  or reproduced in any way without the prior written
 * permission of the Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
public interface INeonProbeFormatter {
	public String console(long ts, String type, String keyword, String message);

	public String jmx(long ts, String type, String keyword, String message);

	public String logger(long ts, String type, String keyword, String message);
}
