/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.Real;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.IJsonNativeNumber;

/**
 * 
 * @author roach
 */
public abstract class EsPrimitiveNumber extends EsPrimitive {

	private static double double_double_operate(EsPrimitiveNumber lhs, BinaryOp bop, EsPrimitiveNumber rhs) {
		final double lv = lhs.doubleValue();
		final double rv = rhs.doubleValue();
		switch (bop) {
			case MUL:
				return lv * rv;
			case DIV:
				return lv / rv;
			case REM:
				return lv % rv;
			case ADD:
				return lv + rv;
			case SUB:
				return lv - rv;
		}
		throw new EsInterpreterException("Operator " + bop + " not supported for double,double");
	}

	private static EsPrimitiveNumber doubleOperate(EsPrimitiveNumber lhs, BinaryOp bop, EsPrimitiveNumber rhs) {
		return new EsPrimitiveNumberDouble(double_double_operate(lhs, bop, rhs));
	}

	private static EsPrimitiveNumber elapsed_elapsed_divide(EsPrimitiveNumber lhs, EsPrimitiveNumber rhs) {
		final long lv = lhs.longValue();
		final long rv = rhs.longValue();
		if (rv == 0L) return EsPrimitiveNumberNot.Instance;
		if (lv == 0L) return EsPrimitiveNumberInteger.ZERO;

		if (lv % rv == 0L) {
			final long v = lv / rv;
			return new EsPrimitiveNumberInteger(v);
		}
		final double v = ((double) lv) / rv;
		return new EsPrimitiveNumberDouble(v);
	}

	private static long long_long_operate(EsPrimitiveNumber lhs, BinaryOp bop, EsPrimitiveNumber rhs) {
		final long lv = lhs.longValue();
		final long rv = rhs.longValue();
		switch (bop) {
			case MUL:
				return lv * rv;
			case DIV:
				return lv / rv;
			case REM:
				return lv % rv;
			case ADD:
				return lv + rv;
			case SUB:
				return lv - rv;
		}
		throw new EsInterpreterException("Operator " + bop + " not supported for long,long");
	}

	private static EsPrimitiveNumber longOperate(EsPrimitiveNumber lhs, BinaryOp bop, EsPrimitiveNumber rhs) {
		if (bop == BinaryOp.DIV && rhs.longValue() == 0L) return EsPrimitiveNumberNot.Instance;
		return new EsPrimitiveNumberInteger(long_long_operate(lhs, bop, rhs));
	}

	private static Real real_real_operate(EsPrimitiveNumber lhs, BinaryOp bop, EsPrimitiveNumber rhs) {
		final Real lv = lhs.realValue();
		final Real rv = rhs.realValue();
		switch (bop) {
			case MUL:
				return Real.binaryMultiply(lv, rv);
			case DIV:
				return Real.binaryDivide(lv, rv);
			case REM:
				return Real.binaryModulo(lv, rv);
			case ADD:
				return Real.binaryPlus(lv, rv);
			case SUB:
				return Real.binaryMinus(lv, rv);
		}
		throw new EsInterpreterException("Operator " + bop + " not supported for real,real");
	}

	private static EsPrimitiveNumber realOperate(EsPrimitiveNumber lhs, BinaryOp bop, EsPrimitiveNumber rhs) {
		final Real real = real_real_operate(lhs, bop, rhs);
		return new EsPrimitiveNumberReal(real);
	}

	private static EsPrimitiveNumber temporalOperate(EsPrimitiveNumber lhs, BinaryOp bop, EsPrimitiveNumber rhs) {
		final SubType lt = lhs.subType();
		final SubType rt = rhs.subType();

		if (lt == SubType.TIME && rt == SubType.TIME) {
			if (bop == BinaryOp.SUB) return new EsPrimitiveNumberElapsed(long_long_operate(lhs, bop, rhs));
			return EsPrimitiveNumberNot.Instance;
		}

		if (lt == SubType.TIME || rt == SubType.TIME) {
			if (bop != BinaryOp.ADD && bop != BinaryOp.SUB) return EsPrimitiveNumberNot.Instance;
			return new EsPrimitiveNumberTime(long_long_operate(lhs, bop, rhs));
		}

		if (lt == SubType.ELAPSED && rt == SubType.ELAPSED) {
			if (bop == BinaryOp.DIV) return elapsed_elapsed_divide(lhs, rhs);
			return new EsPrimitiveNumberElapsed(long_long_operate(lhs, bop, rhs));
		}

		if (lt == SubType.ELAPSED && rt == SubType.INTEGER)
			return new EsPrimitiveNumberElapsed(long_long_operate(lhs, bop, rhs));

		if (lt == SubType.INTEGER && rt == SubType.ELAPSED)
			return new EsPrimitiveNumberElapsed(long_long_operate(lhs, bop, rhs));

		final double dsms = double_double_operate(lhs, bop, rhs);
		final long sms = Math.round(dsms);
		return new EsPrimitiveNumberElapsed(sms);
	}

	public static boolean canRelate(EsPrimitiveNumber lhs, EsPrimitiveNumber rhs) {
		if (lhs == null) throw new IllegalArgumentException("lhs is null");
		if (rhs == null) throw new IllegalArgumentException("rhs is null");

		if (lhs.isNaN() || rhs.isNaN()) return false;

		final SubType ltype = lhs.subType();
		final SubType rtype = rhs.subType();
		return (ltype == SubType.TIME || rtype == SubType.TIME) ? ltype == rtype : true;
	}

	public static EsPrimitiveNumber operate(EsPrimitiveNumber lhs, BinaryOp bop, EsPrimitiveNumber rhs) {
		if (lhs == null) throw new IllegalArgumentException("lhs is null");
		if (rhs == null) throw new IllegalArgumentException("rhs is null");

		if (lhs.isNaN()) return rhs;
		if (rhs.isNaN()) return lhs;

		final SubType ltype = lhs.subType();
		final SubType rtype = rhs.subType();

		switch (ltype) {
			case INTEGER: {
				switch (rtype) {
					case INTEGER:
						return longOperate(lhs, bop, rhs);
					case TIME:
						return temporalOperate(lhs, bop, rhs);
					case ELAPSED:
						return temporalOperate(lhs, bop, rhs);
					case REAL:
						return realOperate(lhs, bop, rhs);
					case DOUBLE:
						return doubleOperate(lhs, bop, rhs);
					default:
				}
			}
			break;

			case TIME: {
				switch (rtype) {
					case INTEGER:
						return temporalOperate(lhs, bop, rhs);
					case TIME:
						return temporalOperate(lhs, bop, rhs);
					case ELAPSED:
						return temporalOperate(lhs, bop, rhs);
					case REAL:
						return temporalOperate(lhs, bop, rhs);
					case DOUBLE:
						return temporalOperate(lhs, bop, rhs);
					default:
				}
			}
			break;

			case ELAPSED: {
				switch (rtype) {
					case INTEGER:
						return temporalOperate(lhs, bop, rhs);
					case TIME:
						return temporalOperate(lhs, bop, rhs);
					case ELAPSED:
						return temporalOperate(lhs, bop, rhs);
					case REAL:
						return temporalOperate(lhs, bop, rhs);
					case DOUBLE:
						return temporalOperate(lhs, bop, rhs);
					default:
				}
			}
			break;

			case REAL: {
				switch (rtype) {
					case INTEGER:
						return realOperate(lhs, bop, rhs);
					case TIME:
						return temporalOperate(lhs, bop, rhs);
					case ELAPSED:
						return temporalOperate(lhs, bop, rhs);
					case REAL:
						return realOperate(lhs, bop, rhs);
					case DOUBLE:
						return doubleOperate(lhs, bop, rhs);
					default:
				}
			}
			break;

			case DOUBLE: {
				switch (rtype) {
					case INTEGER:
						return doubleOperate(lhs, bop, rhs);
					case TIME:
						return temporalOperate(lhs, bop, rhs);
					case ELAPSED:
						return temporalOperate(lhs, bop, rhs);
					case REAL:
						return doubleOperate(lhs, bop, rhs);
					case DOUBLE:
						return doubleOperate(lhs, bop, rhs);
					default:
				}
			}
			break;

			default:
		}

		throw new EsInterpreterException("Binary Operation on " + lhs.subType() + "," + rhs.subType() + " not supported");
	}

	public abstract EsPrimitiveNumber abs();

	@Override
	public final IJsonNative createJsonNative() {
		return newJsonNativeNumber();
	}

	public final EsPrimitiveNumber dividedBy(Real r, double d) {
		if (subType() == SubType.REAL) return new EsPrimitiveNumberReal(Real.binaryDivide(realValue(), r));
		return new EsPrimitiveNumberDouble(doubleValue() / d);
	}

	public abstract double doubleValue();

	@Override
	public final EsType esType() {
		return EsType.TNumber;
	}

	public abstract int intVerified();

	public abstract boolean isLessThan(EsPrimitiveNumber rhsNumber);

	public abstract boolean isNaN();

	public abstract long longValue();

	public abstract EsPrimitiveNumber negated();

	public abstract IJsonNativeNumber newJsonNativeNumber();

	public abstract Real realValue();

	public abstract boolean sameNumberValue(EsPrimitiveNumber rhsNumber);

	public abstract SubType subType();

	@Override
	public final boolean toCanonicalBoolean() {
		return subType() != SubType.NAN;
	}

	public abstract String toCanonicalString(com.metservice.argon.DecimalMask decimalMask);

	@Override
	public final EsPrimitiveNumber toNumber(EsExecutionContext ecx) {
		return this;
	}

	@Override
	public final EsObject toObject(EsExecutionContext ecx) {
		return ecx.global().newIntrinsicNumber(this);
	}

	public final int uint31Verified() {
		return isNaN() ? 0 : Math.abs(intVerified());
	}

	protected EsPrimitiveNumber() {
	}

	public static enum BinaryOp {
		MUL, DIV, REM, ADD, SUB
	}

	public static enum SubType {
		NAN, INTEGER, TIME, ELAPSED, REAL, DOUBLE;
	}
}
