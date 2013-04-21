/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import com.metservice.argon.ArgonFormatException;

/**
 * @author roach
 */
public interface ISaxJsonFactory {

	public boolean allowMultipleChildren(String ename, String pename);

	public IJsonNative createAttribute(String aname, String zValue, String ename)
			throws NumberFormatException, ArgonFormatException, JsonSchemaException;

	public IJsonNative createText(String zValue, String ename)
			throws NumberFormatException, ArgonFormatException, JsonSchemaException;

	public String getTextAttributeName(String ename);

	public boolean isSimpleText(String ename);

	public String propertyName(String uri, String localName);

	public void validateAttributes(String ename, String[] anames)
			throws JsonSchemaException;
}
