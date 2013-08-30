/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import com.metservice.argon.management.IArgonSpaceId;

/**
 * @author roach
 */
public interface IArgonSensorMap {

	public IArgonSensor findSensor(ArgonSensorId id);

	public ArgonSensorId getSensorId(int index);

	public int sensorCount();

	public ArgonServiceId serviceId();

	public IArgonSpaceId spaceId();
}
