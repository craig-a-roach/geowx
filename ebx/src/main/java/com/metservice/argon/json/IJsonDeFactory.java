/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import com.metservice.argon.Binary;

/**
 * @author roach
 */
public interface IJsonDeFactory {

	public IJsonDeValue instanceNotNumber();

	public IJsonDeValue instanceNull();

	public IJsonDeArray newArray();

	public IJsonDeValue newBinary(Binary value);

	public IJsonDeValue newBoolean(boolean value);

	public IJsonDeValue newNumberDouble(double value);

	public IJsonDeValue newNumberElapsed(long ms);

	public IJsonDeValue newNumberInt(int value);

	public IJsonDeValue newNumberTime(long ts);

	public IJsonDeObject newObject();

	public IJsonDeValue newString(String zValue);
}
