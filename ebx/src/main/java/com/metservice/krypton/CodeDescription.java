/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.ArgonNumber;

/**
 * @author roach
 */
class CodeDescription implements IKryptonName {

	public boolean equals(CodeDescription rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return code == rhs.code;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CodeDescription)) return false;
		return equals((CodeDescription) o);
	}

	@Override
	public int hashCode() {
		return code;
	}

	@Override
	public String qcctwName() {
		return qcctwDesc;
	}

	@Override
	public String toString() {
		return ArgonNumber.intToDec3(code) + ": " + qcctwDesc;
	}

	public CodeDescription(int code, String qcctwDesc) {
		if (qcctwDesc == null || qcctwDesc.length() == 0) throw new IllegalArgumentException("string is null or empty");
		this.code = code;
		this.qcctwDesc = qcctwDesc;
	}
	public final int code;
	public final String qcctwDesc;
}
