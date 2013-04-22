/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievnumable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.xenon;

import java.awt.Color;

import com.metservice.argon.CodedEnumTable;
import com.metservice.argon.ICodedEnum;

/**
 * @author roach
 */
enum BasicColorName implements ICodedEnum {
	white("white", Color.white),
	black("black", Color.black),
	lightGray("lightGray", Color.lightGray),
	gray("gray", Color.gray),
	darkGray("darkGray", Color.darkGray),
	red("red", Color.red),
	pink("pink", Color.pink),
	orange("orange", Color.orange),
	yellow("yellow", Color.yellow),
	green("green", Color.green),
	magenta("magenta", Color.magenta),
	cyan("cyan", Color.cyan),
	blue("blue", Color.blue);

	public static final CodedEnumTable<BasicColorName> Table = new CodedEnumTable<>(BasicColorName.class, false,
			BasicColorName.values());

	public Color color() {
		return m_color;
	}

	@Override
	public String qCode() {
		return m_qCode;
	}

	private BasicColorName(String qCode, Color c) {
		m_qCode = qCode;
		m_color = c;
	}
	private final String m_qCode;
	private final Color m_color;
}
