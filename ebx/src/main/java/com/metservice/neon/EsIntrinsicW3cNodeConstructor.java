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
public class EsIntrinsicW3cNodeConstructor extends EsIntrinsicConstructor {

	@Override
	protected IEsOperand eval(EsExecutionContext ecx)
			throws InterruptedException {
		return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class);
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicW3cNode(global.prototypeObject);
	}

	private static EsIntrinsicMethod method_containsText() {
		return new EsIntrinsicMethod("containsText") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).containsText();
			}
		};
	}

	private static EsIntrinsicMethod method_getChildNodes() {
		return new EsIntrinsicMethod("getChildNodes", new String[] { "includeElementContentWhitespace" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments args = ecx.activation().arguments();
				final int argCount = args.length();
				final boolean includeElementContentWhitespace = argCount > 0 ? args.primivitiveBoolean(ecx, 0)
						.toCanonicalBoolean() : false;
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).getChildNodes(ecx,
						includeElementContentWhitespace);
			}
		};
	}

	private static EsIntrinsicMethod method_getFirstChild() {
		return new EsIntrinsicMethod("getFirstChild") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).getFirstChild(ecx);
			}
		};
	}

	private static EsIntrinsicMethod method_getLastChild() {
		return new EsIntrinsicMethod("getLastChild") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).getLastChild(ecx);
			}
		};
	}

	private static EsIntrinsicMethod method_getNamespaceURI() {
		return new EsIntrinsicMethod("getNamespaceURI") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).getNamespaceURI();
			}
		};
	}

	private static EsIntrinsicMethod method_getNextSibling() {
		return new EsIntrinsicMethod("getNextSibling") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).getNextSibling(ecx);
			}
		};
	}

	private static EsIntrinsicMethod method_getParent() {
		return new EsIntrinsicMethod("getParent") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).getParent(ecx);
			}
		};
	}

	private static EsIntrinsicMethod method_getPrefix() {
		return new EsIntrinsicMethod("getPrefix") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).getPrefix();
			}
		};
	}

	private static EsIntrinsicMethod method_getText() {
		return new EsIntrinsicMethod("getText", new String[] { "spaced" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments args = ecx.activation().arguments();
				final int argCount = args.length();
				final boolean spaced = argCount > 0 ? args.primivitiveBoolean(ecx, 0).toCanonicalBoolean() : true;
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).toFlatPrimitiveString(spaced);
			}
		};
	}

	private static EsIntrinsicMethod method_hasChildNodes() {
		return new EsIntrinsicMethod("hasChildNodes") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).hasChildNodes();
			}
		};
	}

	private static EsIntrinsicMethod method_lookupNamespaceURI() {
		return new EsIntrinsicMethod("lookupNamespaceURI", new String[] { "prefix" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String qtwPrefix = ac.qtwStringValue(0);
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).lookupNamespaceURI(qtwPrefix);
			}
		};
	}

	private static EsIntrinsicMethod method_lookupPrefix() {
		return new EsIntrinsicMethod("lookupPrefix", new String[] { "namespaceURI" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final String qtwNamespaceURI = ac.qtwStringValue(0);
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).lookupPrefix(qtwNamespaceURI);
			}
		};
	}

	private static EsIntrinsicMethod method_toInnerString() {
		return new EsIntrinsicMethod("toInnerString") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).toXmlPrimitiveString(false);
			}
		};
	}

	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return thisIntrinsicObject(ecx, EsIntrinsicW3cNode.class).toXmlPrimitiveString(true);
			}
		};
	}

	public static EsIntrinsicW3cNodeConstructor newInstance() {
		return new EsIntrinsicW3cNodeConstructor();
	}

	private EsIntrinsicW3cNodeConstructor() {
		super(ClassName, NOARGS, 0);
	}

	public static final String ClassName = "W3cNode";

	public static final String PropertyName_name = "name";
	public static final String PropertyName_type = "type";
	public static final String PropertyName_value = "value";
	public static final String PropertyName_attributes = "attributes";
	public static final String PropertyName_node = "node";
	public static final String PropertyName_fatalError = "fatalError";
	public static final String PropertyName_errors = "errors";
	public static final String PropertyName_warnings = "warnings";

	public static final EsIntrinsicMethod[] Methods = { method_getNamespaceURI(), method_getPrefix(),
			method_lookupNamespaceURI(), method_lookupPrefix(), method_hasChildNodes(), method_containsText(),
			method_getFirstChild(), method_getLastChild(), method_getNextSibling(), method_getChildNodes(),
			method_getParent(), method_getText(), method_toInnerString(), method_toString() };
}
