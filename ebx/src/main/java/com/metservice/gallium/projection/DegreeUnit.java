/*
 * Copyright 2006 Jerry Huxtable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class DegreeUnit extends Unit {

	private static final AngleFormat Format = new AngleFormat(AngleFormat.ddmmssPattern, true);

	@Override
	public String format(double n) {
		return Format.format(n) + " " + abbreviation;
	}

	@Override
	public String format(double n, boolean abbrev) {
		if (abbrev) return Format.format(n) + " " + abbreviation;
		return Format.format(n);
	}

	@Override
	public String format(double x, double y, boolean abbrev) {
		if (abbrev) return Format.format(x) + "/" + Format.format(y) + " " + abbreviation;
		return Format.format(x) + "/" + Format.format(y);
	}

	@Override
	public double parse(String s)
			throws NumberFormatException {
		try {
			return Format.parse(s).doubleValue();
		} catch (final java.text.ParseException e) {
			throw new NumberFormatException(e.getMessage());
		}
	}

	public DegreeUnit() {
		super("degree", "degrees", "deg", 1);
	}
}
