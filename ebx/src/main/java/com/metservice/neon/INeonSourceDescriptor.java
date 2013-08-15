/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
public interface INeonSourceDescriptor extends Comparable<INeonSourceDescriptor> {

	public boolean isAssure();

	public boolean isWip();

	public String qccNode();

	public long tsLastModified();

	public NeonSourceDescriptorType type();
}
