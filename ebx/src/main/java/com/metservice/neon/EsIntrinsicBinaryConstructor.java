/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;
import java.util.List;

import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.ArgonZipItem;
import com.metservice.argon.Binary;

/**
 * @jsobject Binary
 * @jsnote An intrinsic object, representing a blob of binary data
 * @author roach
 */
public class EsIntrinsicBinaryConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "Binary";
	public static final String PropertyName_item = "item";
	public static final String PropertyName_items = "items";
	public static final String PropertyName_errorQuota = "errorQuota";
	public static final String PropertyName_errorIO = "errorIO";
	public static final String PropertyName_name = "fileName";
	public static final String PropertyName_lastModifiedAt = "lastModifiedAt";
	public static final String PropertyName_content = "content";

	public static final EsIntrinsicMethod[] Methods = { method_byteCount(), method_decodeUtf8(), method_decodeIso8859(),
			method_decodeAscii(), method_decodeJsonUtf8(), method_decodeZip(), method_encodeGZip(), method_decodeGZip(),
			method_newW3cDom(), method_newW3cDomValidated(), method_toString() };

	private static EsIntrinsicMethod method_byteCount() {
		return new EsIntrinsicMethod("byteCount") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final Binary content = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
				return new EsPrimitiveNumberInteger(content.byteCount());
			}
		};
	}

	private static EsIntrinsicMethod method_decodeAscii() {
		return new EsIntrinsicMethod("decodeAscii") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final Binary content = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
				return new EsPrimitiveString(content.newStringASCII());
			}
		};
	}

	/**
	 * @jsmethod decodeGZip
	 * @jsparam quota - Integer, optional. The maximum decoded size of an item.
	 * @jsreturn An object with these properties; item - a Binary object containing the decoded content. It is defined
	 *           only if decode was successful. errorQuota - an error message defined if the decode was unsucessful
	 *           because it would result in the quota being exceeded. errorIO - an error message defined if the decode
	 *           was unsucessful because the source Binary object is not a well-formed GZIP stream.
	 */
	private static EsIntrinsicMethod method_decodeGZip() {
		return new EsIntrinsicMethod("decodeGZip", new String[] { "quota" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments args = ecx.activation().arguments();
				final int argc = args.length();
				final int bcQuota = argc == 0 ? Integer.MAX_VALUE : args.primitiveNumber(ecx, 0).intVerified();
				final Binary binary = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
				final EsIntrinsicObject result = ecx.global().newIntrinsicObject();
				try {
					final EsIntrinsicBinary item = ecx.global().newIntrinsicBinary(binary.newGZipDecoded(bcQuota));
					result.add(PropertyName_item, EsProperty.newDefined(item));
				} catch (final ArgonQuotaException ex) {
					result.add(PropertyName_errorQuota, EsProperty.newDefined(new EsPrimitiveString(ex.getMessage())));
				} catch (final ArgonStreamReadException ex) {
					result.add(PropertyName_errorIO, EsProperty.newDefined(new EsPrimitiveString(ex.toString())));
				}
				return result;
			}
		};
	};

	private static EsIntrinsicMethod method_decodeIso8859() {
		return new EsIntrinsicMethod("decodeIso8859") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final Binary content = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
				return new EsPrimitiveString(content.newStringISO8859());
			}
		};
	}

	private static EsIntrinsicMethod method_decodeJsonUtf8() {
		return new EsIntrinsicMethod("decodeJsonUtf8", new String[] { "validate" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final Binary content = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final boolean validate = ac.defaulted(0) ? true : ac.booleanValue(0);
				final String zContent = content.newStringUTF8();
				return EsIntrinsicJsonDecoder.decode(ecx, zContent, validate);
			}
		};
	}

	private static EsIntrinsicMethod method_decodeUtf8() {
		return new EsIntrinsicMethod("decodeUtf8") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final Binary content = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
				return new EsPrimitiveString(content.newStringUTF8());
			}
		};
	}

	/**
	 * @jsmethod decodeZip
	 * @jsparam quota - Integer, optional. The maximum decoded size of an item.
	 * @jsreturn An object with these properties;
	 *           <ul>
	 *           <li>items - an array of objects, defined only if decode was successful. Each member of the array has
	 *           these properties:
	 *           <ul>
	 *           <li>name (String)</li>
	 *           <li>lastModifiedAt (TimeNumber)</li>
	 *           <li>content (Binary)</li>
	 *           </ul>
	 *           </li>
	 *           <li>errorQuota - an error message defined if the decode was unsuccessful because it would result in
	 *           the quota being exceeded.</li>
	 *           <li>errorIO - an error message defined if the decode was unsuccessful because the source Binary
	 *           object is not a well-formed ZIP archive.</li>
	 *           </ul>
	 */
	private static EsIntrinsicMethod method_decodeZip() {
		return new EsIntrinsicMethod("decodeZip", new String[] { "quota" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsActivation activation = ecx.activation();
				final EsArguments arguments = activation.arguments();
				final int argCount = arguments.length();
				final int bcQuota = argCount == 0 ? Integer.MAX_VALUE : arguments.primitiveNumber(ecx, 0).intVerified();
				final Binary binary = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
				final EsIntrinsicObject result = ecx.global().newIntrinsicObject();
				try {
					final List<ArgonZipItem> zlZipItemsAscName = binary.newZipDecodedAscName(bcQuota);
					final int itemCount = zlZipItemsAscName.size();
					final IEsOperand[] zptItems = new IEsOperand[itemCount];
					for (int i = 0; i < itemCount; i++) {
						final ArgonZipItem zipItem = zlZipItemsAscName.get(i);
						final EsIntrinsicObject item = ecx.global().newIntrinsicObject();
						item.add(PropertyName_name, EsProperty.newDefined(new EsPrimitiveString(zipItem.qccFileName)));
						item.add(PropertyName_lastModifiedAt,
								EsProperty.newDefined(new EsPrimitiveNumberTime(zipItem.lastModifiedAt)));
						item.add(PropertyName_content,
								EsProperty.newDefined(ecx.global().newIntrinsicBinary(zipItem.content)));
						zptItems[i] = item;
					}
					final EsIntrinsicArray items = ecx.global().newIntrinsicArray(zptItems);
					result.add(PropertyName_items, EsProperty.newDefined(items));
				} catch (final ArgonQuotaException ex) {
					result.add(PropertyName_errorQuota, EsProperty.newDefined(new EsPrimitiveString(ex.getMessage())));
				} catch (final ArgonStreamReadException ex) {
					result.add(PropertyName_errorIO, EsProperty.newDefined(new EsPrimitiveString(ex.toString())));
				}
				return result;
			}
		};
	}

	/**
	 * @jsmethod encodeGZip
	 * @jsreturn A new gzip encoded binary object.
	 */
	private static EsIntrinsicMethod method_encodeGZip() {
		return new EsIntrinsicMethod("encodeGZip") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				try {
					final Binary content = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
					return ecx.global().newIntrinsicBinary(content.newGZipEncoded());
				} catch (final ArgonStreamWriteException ex) {
					throw new EsIntrinsicException(ex);
				}
			}
		};
	}

	private static EsIntrinsicMethod method_newW3cDom() {
		return new EsIntrinsicMethod("newW3cDom") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final Binary content = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
				return ecx.global().newIntrinsicW3cDom(ecx, content, false);
			}
		};
	}

	private static EsIntrinsicMethod method_newW3cDomValidated() {
		return new EsIntrinsicMethod("newW3cDomValidated") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final Binary content = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
				return ecx.global().newIntrinsicW3cDom(ecx, content, true);
			}
		};
	}

	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final Binary content = thisIntrinsicObject(ecx, EsIntrinsicBinary.class).value();
				return new EsPrimitiveString(content.toString());
			}
		};
	}

	public static EsIntrinsicBinaryConstructor newInstance() {
		return new EsIntrinsicBinaryConstructor();
	}

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		final EsMethodAccessor ac = new EsMethodAccessor(ecx);
		Binary value = Binary.Empty;
		if (!ac.defaulted(0)) {
			final String qtwResourceName = ac.qtwStringValue(0);
			final EsGlobal global = ecx.global();
			final File ocndirHome = global.sourceProvider.ocndirHome();
			if (ocndirHome == null) throw new EsApiCodeException("Resource loading not supported by source provider");
			final String qtwResourceNameRe;
			if (qtwResourceName.startsWith("/")) {
				final String ztwResourceNameRe = qtwResourceName.substring(1).trim();
				if (ztwResourceNameRe.length() == 0) {
					final String m = "Malformed resource name '" + qtwResourceName + "'";
					throw new EsApiCodeException(m);
				}
				qtwResourceNameRe = ztwResourceNameRe;
			} else {
				final String qccSourcePath = global.shellHook.source.qccPath();
				final int posLastFwd = qccSourcePath.lastIndexOf('/');
				if (posLastFwd < 0) {
					qtwResourceNameRe = qtwResourceName;
				} else {
					final String ztwSourceContainer = qccSourcePath.substring(0, posLastFwd).trim();
					qtwResourceNameRe = ztwSourceContainer + "/" + qtwResourceName;
				}
			}
			final File binFile = new File(ocndirHome, qtwResourceNameRe);
			try {
				final Binary oLoaded = Binary.createFromFile(binFile, CNeon.QuotaBinaryBc);
				if (oLoaded == null) {
					final String m = "Resource '" + qtwResourceName + "' not found; absolute path was '" + binFile + "'";
					throw new EsApiCodeException(m);
				}
				value = oLoaded;
			} catch (final ArgonQuotaException ex) {
				throw new EsApiCodeException(ex);
			} catch (final ArgonStreamReadException ex) {
				throw new EsApiCodeException(ex);
			}
		}
		final EsIntrinsicBinary neo;
		if (calledAsFunction(ecx)) {
			neo = ecx.global().newIntrinsicBinary(value);
		} else {
			neo = ecx.thisObject(ClassName, EsIntrinsicBinary.class);
			neo.setValue(value);
		}
		return neo;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicBinary(global.prototypeObject);
	}

	/**
	 * @jsconstructor Binary
	 */
	private EsIntrinsicBinaryConstructor() {
		super(ClassName, NOARGS, 0);
	}
}
