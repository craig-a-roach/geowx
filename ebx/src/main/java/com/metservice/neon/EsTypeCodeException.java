/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;


/**
 * 
 * @author roach
 */
public class EsTypeCodeException extends EsCodeException {

	public EsTypeCodeException(String problem) {
		super(Category.Type, problem);
	}

	public EsTypeCodeException(Throwable ex) {
		super(Category.Type, (ex == null ? "No Cause Specified" : ex.getMessage()));
	}
}
