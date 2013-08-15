/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @jsobject Math
 * @jsnote a static object used to access mathematical functions and constants.
 * @jsproperty E The mathematical constant e.
 * @jsproperty LN10 Constant. The value of the natural logarithm of 10.
 * @jsproperty LN2 Constant. The value of the natural logarithm of 2.
 * @jsproperty PI The constant Pi.
 * @jsproperty SQRT1_2 Constant. The square root of 0.5.
 * @jsproperty SQRT2 Constant. The square root of 2.
 * @author roach
 */
public class EsIntrinsicMathSingleton extends EsIntrinsicSingleton {

	public static final String Name = "Math";
	private static final String[] ARGS_X = { "x" };
	private static final String[] ARGS_X_Y = { "x", "y" };
	private static final String[] ARGS_Y_X = { "y", "x" };

	private static final double LN10 = Math.log(10.0);
	private static final double LN2 = Math.log(2.0);
	private static final double SQRT1_2 = Math.sqrt(0.5);
	private static final double SQRT2 = Math.sqrt(2.0);

	public static final EsIntrinsicMethod[] Methods = { method_abs(), method_acos(), method_asin(), method_atan(),
			method_atan2(), method_ceil(), method_cos(), method_exp(), method_floor(), method_log(), method_pow(),
			method_random(), method_sin(), method_sqrt(), method_tan(), method_roundAway(), method_roundPositive(),
			method_step(), method_max(), method_min() };

	private static long lround(double d, boolean awayFromZero) {
		final boolean neg = d < 0.0;
		final long lr;
		if (awayFromZero && neg) {
			lr = -((long) Math.floor(-d + 0.5d));
		} else {
			lr = (long) Math.floor(d + 0.5d);
		}
		return lr;
	}

	// ECMA 15.8.2.1
	/**
	 * @jsmethod abs
	 * @jsparam x A Number. Required.
	 * @jsreturn The absolute value of x. The return value is the same sub-type as x.
	 */
	private static EsIntrinsicMethod method_abs() {
		return new EsIntrinsicMethod("abs", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsPrimitiveNumber x = ecx.activation().arguments().primitiveNumber(ecx, 0);
				return x.abs();
			}
		};
	}

	// ECMA 15.8.2.2
	/**
	 * @jsmethod acos
	 * @jsparam x A Double. Required.
	 * @jsreturn The arccosine of x as a double
	 */
	private static EsIntrinsicMethod method_acos() {
		return new EsIntrinsicMethod("acos", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.acos(x));
			}
		};
	}

	// ECMA 15.8.2.3
	/**
	 * @jsmethod asin
	 * @jsparam x A Double. Required.
	 * @jsreturn The arcsine of x as a double
	 */
	private static EsIntrinsicMethod method_asin() {
		return new EsIntrinsicMethod("asin", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.asin(x));
			}
		};
	}

	// ECMA 15.8.2.4
	/**
	 * @jsmethod atan
	 * @jsparam x A Double. Required.
	 * @jsreturn The arctan of x as a double.
	 */
	private static EsIntrinsicMethod method_atan() {
		return new EsIntrinsicMethod("atan", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.atan(x));
			}
		};
	}

	// ECMA 15.8.2.5
	/**
	 * @jsmethod atan
	 * @jsparam x A Double. Required.
	 * @jsreturn The arctan of x as a double.
	 */
	private static EsIntrinsicMethod method_atan2() {
		return new EsIntrinsicMethod("atan2", ARGS_Y_X, 2) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double y = ac.doubleValue(0);
				final double x = ac.doubleValue(1);
				return new EsPrimitiveNumberDouble(Math.atan2(y, x));
			}
		};
	}

	// ECMA 15.8.2.6
	/**
	 * @jsmethod ceil
	 * @jsparam x A Double. Required.
	 * @jsreturn The smallest double that is an integer value and is greater than or equal to x.
	 */
	private static EsIntrinsicMethod method_ceil() {
		return new EsIntrinsicMethod("ceil", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.ceil(x));
			}
		};
	}

	// ECMA 15.8.2.7
	/**
	 * @jsmethod cos
	 * @jsparam x A Double. Required. An angle in radians.
	 * @jsreturn The cosine of x as a double.
	 */
	private static EsIntrinsicMethod method_cos() {
		return new EsIntrinsicMethod("cos", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.cos(x));
			}
		};
	}

	// ECMA 15.8.2.8
	/**
	 * @jsmethod exp
	 * @jsparam x A Double. Required.
	 * @jsreturn e raised to the power of x as a double.
	 */
	private static EsIntrinsicMethod method_exp() {
		return new EsIntrinsicMethod("exp", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.exp(x));
			}
		};
	}

	// ECMA 15.8.2.9
	/**
	 * @jsmethod floor
	 * @jsparam x A Double. Required.
	 * @jsreturn The largest double that is an integer value and is less than or equal to x.
	 */
	private static EsIntrinsicMethod method_floor() {
		return new EsIntrinsicMethod("floor", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.floor(x));
			}
		};
	}

	// ECMA 15.8.2.10
	/**
	 * @jsmethod log
	 * @jsparam x A Double. Required.
	 * @jsreturn The natural logarithm of x as a double.
	 */
	private static EsIntrinsicMethod method_log() {
		return new EsIntrinsicMethod("log", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.log(x));
			}
		};
	}

	/**
	 * @jsmethod max
	 * @jsparam ... Variable number of arguments.
	 * @jsreturn The argument with the maximum value.
	 */
	private static EsIntrinsicMethod method_max() {
		return new EsIntrinsicMethod("max", ARGS_X_Y, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments args = ecx.activation().arguments();
				final int argc = args.length();
				EsPrimitiveNumber oMax = null;
				for (int i = 0; i < argc; i++) {
					final IEsOperand yop = args.operand(i);
					final EsType yt = yop.esType();
					if (yt.isDatum) {
						final EsPrimitiveNumber y = yop.toNumber(ecx);
						if (!y.isNaN() && (oMax == null || oMax.isLessThan(y))) {
							oMax = y;
						}
					}
				}
				return oMax == null ? EsPrimitiveNumberNot.Instance : oMax;
			}
		};
	}

	/**
	 * @jsmethod min
	 * @jsparam ... Variable number of arguments.
	 * @jsreturn The argument with the minimum value.
	 */
	private static EsIntrinsicMethod method_min() {
		return new EsIntrinsicMethod("min", ARGS_X_Y, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsArguments args = ecx.activation().arguments();
				final int argCount = args.length();
				EsPrimitiveNumber oMin = null;
				for (int i = 0; i < argCount; i++) {
					final IEsOperand yop = args.operand(i);
					final EsType yt = yop.esType();
					if (yt.isDatum) {
						final EsPrimitiveNumber y = yop.toNumber(ecx);
						if (!y.isNaN() && (oMin == null || y.isLessThan(oMin))) {
							oMin = y;
						}
					}
				}
				return oMin == null ? EsPrimitiveNumberNot.Instance : oMin;
			}
		};
	}

	// ECMA 15.8.2.13
	/**
	 * @jsmethod pow
	 * @jsparam x A Double. Required.
	 * @jsparam y A Double. Required.
	 * @jsreturn x raised to the power of y as a double.
	 */
	private static EsIntrinsicMethod method_pow() {
		return new EsIntrinsicMethod("pow", ARGS_X_Y, 2) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				final double y = ac.doubleValue(1);
				return new EsPrimitiveNumberDouble(Math.pow(x, y));
			}
		};
	}

	// ECMA 15.8.2.14
	/**
	 * @jsmethod random
	 * @jsreturn A randomly chosen number with positive sign, greater than or equal to 0.0, but less than 1.0
	 * 
	 */
	private static EsIntrinsicMethod method_random() {
		return new EsIntrinsicMethod("random") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return new EsPrimitiveNumberDouble(Math.random());
			}
		};
	}

	/**
	 * @jsmethod roundAway
	 * @jsparam x A Number
	 * @jsreturn The value of the argument rounded away from zero.
	 */
	private static EsIntrinsicMethod method_roundAway() {
		return new EsIntrinsicMethod("roundAway", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final EsPrimitiveNumber x = ac.esPrimitiveNumber(0);
				return round(x, true);
			}
		};
	}

	/**
	 * @jsmethod roundPositive
	 * @jsparam x A Number, the value to round.
	 * @jsreturn The result of the number rounded towards positive infinity.
	 */
	private static EsIntrinsicMethod method_roundPositive() {
		return new EsIntrinsicMethod("roundPositive", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final EsPrimitiveNumber x = ac.esPrimitiveNumber(0);
				return round(x, false);
			}
		};
	}

	// ECMA 15.8.2.16
	/**
	 * @jsmethod sin
	 * @jsparam x A Double. Required.
	 * @jsreturn The sine of x as a double.
	 * 
	 */
	private static EsIntrinsicMethod method_sin() {
		return new EsIntrinsicMethod("sin", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.sin(x));
			}
		};
	}

	// ECMA 15.8.2.17
	/**
	 * @jsmethod sqrt
	 * @jsparam x A Double. Required.
	 * @jsreturn The square root of x as a double.
	 */
	private static EsIntrinsicMethod method_sqrt() {
		return new EsIntrinsicMethod("sqrt", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.sqrt(x));
			}
		};
	}

	/**
	 * @jsmethod step
	 * @jsparam x Number. The operand. Required.
	 * @jsparam step Number. The step factor. Required.
	 * @jsreturn The value closest to x which is also a multiple of step. If step is an IntegerNumber (and x is within
	 *           the integer range), the return value will also be an IntegerNumber.
	 */
	private static EsIntrinsicMethod method_step() {
		return new EsIntrinsicMethod("step", new String[] { "x", "step" }, 2) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final EsPrimitiveNumber x = ac.esPrimitiveNumber(0);
				final EsPrimitiveNumber s = ac.esPrimitiveNumber(1);
				return step(x, s);
			}
		};
	}

	// ECMA 15.8.2.18
	/**
	 * @jsmethod tan
	 * @jsparam x A Double. Required.
	 * @jsreturn The tangent of x as a double.
	 */
	private static EsIntrinsicMethod method_tan() {
		return new EsIntrinsicMethod("tan", ARGS_X, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				final EsMethodAccessor ac = new EsMethodAccessor(ecx);
				final double x = ac.doubleValue(0);
				return new EsPrimitiveNumberDouble(Math.tan(x));
			}
		};
	}

	static EsPrimitiveNumber round(EsPrimitiveNumber x, boolean awayFromZero) {
		assert x != null;
		final EsPrimitiveNumber.SubType subType = x.subType();
		if (subType == EsPrimitiveNumber.SubType.DOUBLE || subType == EsPrimitiveNumber.SubType.REAL) {
			final double d = x.doubleValue();
			final long lr = lround(d, awayFromZero);
			if (lr >= Integer.MIN_VALUE && lr <= Integer.MAX_VALUE) return new EsPrimitiveNumberInteger((int) lr);
			return new EsPrimitiveNumberDouble(lr);
		}
		return x;
	}

	static EsPrimitiveNumber step(EsPrimitiveNumber x, EsPrimitiveNumber s) {
		assert x != null;
		assert s != null;
		final EsPrimitiveNumber.SubType subTypeStep = s.subType();
		final double xd = x.doubleValue();
		final double sd = s.doubleValue();
		final double yd = sd == 0.0 ? xd : lround(xd / sd, true) * sd;
		if (subTypeStep == EsPrimitiveNumber.SubType.INTEGER) {
			final long ylr = lround(yd, true);
			if (ylr >= Integer.MIN_VALUE && ylr <= Integer.MAX_VALUE) return new EsPrimitiveNumberInteger((int) ylr);
		}
		if (subTypeStep == EsPrimitiveNumber.SubType.ELAPSED) {
			final long ylr = lround(yd, true);
			return new EsPrimitiveNumberElapsed(ylr);
		}
		return new EsPrimitiveNumberDouble(yd);
	}

	public static EsIntrinsicMathSingleton declare(EsIntrinsicObject prototype) {
		final EsIntrinsicMathSingleton self = new EsIntrinsicMathSingleton(prototype);
		self.installConstant("E", Math.E); // ECMA 15.8.1.1
		self.installConstant("LN10", LN10); // ECMA 15.8.1.2
		self.installConstant("LN2", LN2); // ECMA 15.8.1.3
		self.installConstant("PI", Math.PI); // ECMA 15.8.1.6
		self.installConstant("SQRT1_2", SQRT1_2); // ECMA 15.8.1.7
		self.installConstant("SQRT2", SQRT2); // ECMA 15.8.1.8
		return self;
	}

	private EsIntrinsicMathSingleton(EsIntrinsicObject prototype) {
		super(prototype, Name);
	}
}
