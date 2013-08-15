/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.Binary;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.IJsonString;
import com.metservice.argon.json.JsonString;
import com.metservice.argon.json.JsonType;
import com.metservice.argon.text.ArgonCharsetFactory;
import com.metservice.argon.text.ArgonJoiner;
import com.metservice.argon.text.ArgonTextVector;

/**
 * 
 * @author roach
 */
public class EsIntrinsicTextDocument extends EsObject implements IJsonString {

	public static final String COMPASS_ABOVE = "above";
	public static final String COMPASS_BELOW = "below";
	public static final String COMPASS_LEFT = "left";
	public static final String COMPASS_RIGHT = "right";
	public static final String ALIGN_LEFT = "left";
	public static final String ALIGN_CENTRE = "centre";
	public static final String ALIGN_RIGHT = "right";

	private static Align toAlign(String zAlign) {
		assert zAlign != null;
		if (zAlign.length() == 0) return Align.Auto;

		final String qlcAlign = zAlign.toLowerCase();
		if (qlcAlign.startsWith("l")) return Align.Left;
		if (qlcAlign.startsWith("c")) return Align.Centre;
		if (qlcAlign.startsWith("r")) return Align.Right;
		if (qlcAlign.startsWith("n")) return Align.None;
		throw new EsApiCodeException("Unsupported alignment '" + zAlign + "'; valid options are left, centre, right or none");
	}

	private static Charset toCharset(String zCharset) {
		assert zCharset != null;
		try {
			return ArgonCharsetFactory.selectDefaultUTF8(zCharset);
		} catch (final ArgonApiException ex) {
			throw new EsApiCodeException(ex);
		}
	}

	private static Compass toCompass(String zCompass, Compass defaultCompass) {
		assert zCompass != null;
		assert defaultCompass != null;
		if (zCompass.length() == 0) return defaultCompass;

		final String qlcCompass = zCompass.toLowerCase();
		if (qlcCompass.startsWith("b")) return Compass.Below;
		if (qlcCompass.startsWith("a")) return Compass.Above;
		if (qlcCompass.startsWith("l")) return Compass.Left;
		if (qlcCompass.startsWith("r")) return Compass.Right;
		throw new EsApiCodeException("Unsupported orientation '" + zCompass
				+ "'; valid options are above, below, left or right");
	}

	private void add(EsExecutionContext ecx, IEsOperand value, Compass compass, int width, Align align)
			throws InterruptedException {
		final ArgonTextVector oVector = createTextVector(ecx, value, width, align);
		if (oVector != null) {
			m_zlTiles.add(new Tile(oVector, compass));
		}
	}

	private ArgonTextVector align(ArgonTextVector oVector, int width, Align align, Align autoAlign) {
		if (oVector == null) return null;

		final Align hardAlign = (align == Align.Auto) ? autoAlign : align;

		switch (hardAlign) {
			case Left: {
				oVector.leftAlign();
			}
			break;

			case Centre: {
				oVector.centreAlign(width);
			}
			break;

			case Right: {
				oVector.rightAlign(width);
			}
			break;
			default:
		}

		return oVector;
	}

	private ArgonTextVector createTextVector(EsExecutionContext ecx, IEsOperand value, int width, Align align)
			throws InterruptedException {
		final EsType valueType = value.esType();
		if (valueType == EsType.TObject) {
			final EsObject valueObject = value.toObject(ecx);

			if (valueObject instanceof EsIntrinsicTextDocument)
				return ((EsIntrinsicTextDocument) valueObject).newTextVector();

			if (valueObject instanceof EsIntrinsicArray) {
				final EsIntrinsicArray array = (EsIntrinsicArray) valueObject;
				final int arrayLength = array.length();
				if (arrayLength == 0) return null;

				final String[] xptArrayElements = new String[arrayLength];
				for (int i = 0; i < arrayLength; i++) {
					final IEsOperand arrayElement = array.getByIndex(i);
					final EsType arrayElementType = arrayElement.esType();
					if (arrayElementType == EsType.TBoolean || arrayElementType == EsType.TNumber
							|| arrayElementType == EsType.TString || arrayElementType == EsType.TObject) {
						xptArrayElements[i] = arrayElement.toCanonicalString(ecx);
					} else {
						xptArrayElements[i] = "";
					}
				}
				final String csv = ArgonJoiner.zCsv(xptArrayElements);
				final ArgonTextVector vector = new ArgonTextVector();
				vector.addPlainText(csv);
				return vector;
			}

			final ArgonTextVector vector = new ArgonTextVector();
			vector.addCRLFText(valueObject.toCanonicalString(ecx));
			return wrapAlign(vector, width, align, Align.Left);
		}

		if (valueType == EsType.TBoolean || valueType == EsType.TNumber) {
			final ArgonTextVector vector = new ArgonTextVector();
			vector.addPlainText(value.toCanonicalString(ecx));
			return align(vector, width, align, Align.Right);
		}

		if (valueType == EsType.TString) {
			final ArgonTextVector vector = new ArgonTextVector();
			vector.addCRLFText(value.toCanonicalString(ecx));
			return wrapAlign(vector, width, align, Align.Left);
		}

		return null;
	}

	private ArgonTextVector wrapAlign(ArgonTextVector oVector, int width, Align align, Align autoAlign) {
		if (oVector == null) return null;

		final ArgonTextVector neo = width <= 0 ? oVector : oVector.newWrapped(width);
		return align(neo, width, align, autoAlign);
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	public void add(EsExecutionContext ecx, IEsOperand value, String zCompass, int width, String zAlign)
			throws InterruptedException {
		if (value == null) throw new IllegalArgumentException("object is null");
		if (zCompass == null) throw new IllegalArgumentException("object is null");
		if (zAlign == null) throw new IllegalArgumentException("object is null");
		final Compass compass = toCompass(zCompass, Compass.Below);
		final Align align = toAlign(zAlign);
		add(ecx, value, compass, width, align);
	}

	public void align(String zAlign) {
		if (zAlign == null) throw new IllegalArgumentException("object is null");

		final int tileCount = m_zlTiles.size();
		if (tileCount == 0) return;

		final Align align = toAlign(zAlign);
		final int widthVector = width();
		for (int i = 0; i < tileCount; i++) {
			final ArgonTextVector vector = m_zlTiles.get(i).textVector;
			switch (align) {
				case Left: {
					vector.leftAlign();
				}
				break;

				case Centre: {
					vector.centreAlign(widthVector);
				}
				break;

				case Right: {
					vector.rightAlign(widthVector);
				}
				break;
				default:
			}
		}
	}

	public void border(String zCompass, String zLine, String zCornerA, String zCornerB) {
		if (zCompass == null) throw new IllegalArgumentException("object is null");
		if (zLine == null) throw new IllegalArgumentException("object is null");
		if (zCornerA == null) throw new IllegalArgumentException("object is null");
		if (zCornerB == null) throw new IllegalArgumentException("object is null");

		final Compass compass = toCompass(zCompass, Compass.Left);
		if (m_oBorderTuple == null) {
			m_oBorderTuple = new BorderTuple();
		}
		switch (compass) {
			case Left: {
				m_oBorderTuple.setLeft(zCornerA, zLine, zCornerB);
			}
			break;
			case Right: {
				m_oBorderTuple.setRight(zCornerA, zLine, zCornerB);
			}
			break;
			case Above: {
				m_oBorderTuple.setAbove(zCornerA, zLine, zCornerB);
			}
			break;
			case Below: {
				m_oBorderTuple.setBelow(zCornerA, zLine, zCornerB);
			}
			break;
		}
	}

	@Override
	public IJsonNative createJsonNative() {
		return JsonString.newInstance(jsonDatum());
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicTextDocument(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicTextDocumentConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TString;
	}

	@Override
	public String jsonDatum() {
		return newString("\n");
	}

	public Binary newBinary(String zLineFeed, String zCharset)
			throws InterruptedException {
		return Binary.newFromString(toCharset(zCharset), newString(zLineFeed));
	}

	public String newString(String zLineFeed) {
		if (zLineFeed == null) throw new IllegalArgumentException("object is null");

		final ArgonTextVector vector = newTextVector();
		return vector.zFormat(zLineFeed);
	}

	public ArgonTextVector newTextVector() {
		final int tileCount = m_zlTiles.size();
		if (tileCount == 0) return new ArgonTextVector();

		final ArgonTextVector vector = m_zlTiles.get(0).textVector.newCopy();
		for (int i = 1; i < tileCount; i++) {
			final Tile tile = m_zlTiles.get(i);
			switch (tile.compass) {
				case Above: {
					vector.addAbove(tile.textVector);
				}
				break;
				case Below: {
					vector.addBelow(tile.textVector);
				}
				break;
				case Left: {
					vector.addLeft(tile.textVector);
				}
				break;
				case Right: {
					vector.addRight(tile.textVector);
				}
				break;
			}
		}

		if (m_oPadding != null) {
			vector.padLeft(m_oPadding.left);
			vector.padRight(m_oPadding.right);
			vector.padAbove(m_oPadding.above);
			vector.padBelow(m_oPadding.below);
		}

		if (m_oBorderTuple != null) {
			if (m_oBorderTuple.oLeft != null) {
				final Border b = m_oBorderTuple.oLeft;
				vector.borderLeft(b.zCornerA, b.zLine, b.zCornerB);
			}
			if (m_oBorderTuple.oRight != null) {
				final Border b = m_oBorderTuple.oRight;
				vector.borderRight(b.zCornerA, b.zLine, b.zCornerB);
			}
			if (m_oBorderTuple.oAbove != null) {
				final Border b = m_oBorderTuple.oAbove;
				vector.borderAbove(b.zCornerA, b.zLine, b.zCornerB);
			}
			if (m_oBorderTuple.oBelow != null) {
				final Border b = m_oBorderTuple.oBelow;
				vector.borderBelow(b.zCornerA, b.zLine, b.zCornerB);
			}
		}

		return vector;
	}

	public void pad(int left, int right, int above, int below) {
		if (m_oPadding == null) {
			m_oPadding = new Padding();
		}
		m_oPadding.left = left;
		m_oPadding.right = right;
		m_oPadding.above = above;
		m_oPadding.below = below;
	}

	@Override
	public String show(int depth) {
		return newString("\n");
	}

	public EsPrimitiveString toPrimitiveString(String zLineFeed) {
		return new EsPrimitiveString(newString(zLineFeed));
	}

	public int width() {
		final int tileCount = m_zlTiles.size();
		int width = 0;
		for (int i = 0; i < tileCount; i++) {
			final ArgonTextVector vector = m_zlTiles.get(i).textVector;
			width = Math.max(width, vector.width());
		}
		return width;
	}

	public EsIntrinsicTextDocument(EsObject prototype) {
		super(prototype);
	}
	private final List<Tile> m_zlTiles = new ArrayList<Tile>(4);
	private Padding m_oPadding;

	private BorderTuple m_oBorderTuple;

	private static enum Align {
		Left, Centre, Right, None, Auto
	}

	private static class Border {

		void set(String zCornerA, String zLine, String zCornerB) {
			this.zCornerA = zCornerA;
			this.zLine = zLine;
			this.zCornerB = zCornerB;
		}

		public Border() {
		}

		String zCornerA = "";
		String zLine = "";
		String zCornerB = "";
	}

	private static class BorderTuple {

		void setAbove(String zCornerA, String zLine, String zCornerB) {
			if (oAbove == null) {
				oAbove = new Border();
			}
			oAbove.set(zCornerA, zLine, zCornerB);
		}

		void setBelow(String zCornerA, String zLine, String zCornerB) {
			if (oBelow == null) {
				oBelow = new Border();
			}
			oBelow.set(zCornerA, zLine, zCornerB);
		}

		void setLeft(String zCornerA, String zLine, String zCornerB) {
			if (oLeft == null) {
				oLeft = new Border();
			}
			oLeft.set(zCornerA, zLine, zCornerB);
		}

		void setRight(String zCornerA, String zLine, String zCornerB) {
			if (oRight == null) {
				oRight = new Border();
			}
			oRight.set(zCornerA, zLine, zCornerB);
		}

		public BorderTuple() {
		}

		Border oLeft;
		Border oRight;
		Border oAbove;
		Border oBelow;
	}

	private static enum Compass {
		Above, Below, Left, Right
	}

	private static class Padding {

		public Padding() {
		}
		int left;
		int right;
		int above;
		int below;
	}

	private static class Tile {

		Tile(ArgonTextVector textVector, Compass compass) {
			assert textVector != null;
			assert compass != null;
			this.textVector = textVector;
			this.compass = compass;
		}
		final ArgonTextVector textVector;
		final Compass compass;
	}
}
