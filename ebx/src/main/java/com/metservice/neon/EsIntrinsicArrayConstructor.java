/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * The items of the Array object are attached to the Array as the properties. Thus array[1] should yield the same result
 * as array.1
 * 
 * @jsobject Array
 * @jsnote An object that holds a collection of objects.
 * @author roach
 */
public class EsIntrinsicArrayConstructor extends EsIntrinsicConstructor {

	public static final String ClassName = "Array";
	public static final EsIntrinsicMethod[] Methods = { method_compact(), method_concat(), method_fill(), method_filter(),
			method_first(), method_forEach(), method_intersection(), method_join(), method_last(), method_lslParse(),
			method_map(), method_overlay(), method_partition(), method_push(), method_pushEach(), method_reduce(),
			method_slice(), method_sort(), method_subtract(), method_tail(), method_toMap(), method_toString() };

	@Override
	protected IEsOperand eval(EsExecutionContext ecx) {
		final EsArguments args = ecx.activation().arguments();

		final EsIntrinsicArray array;
		if (calledAsFunction(ecx)) {
			array = ecx.global().newIntrinsicArray();
		} else {
			array = (EsIntrinsicArray) ecx.thisObject();
		}

		array.put(args.zptValues());

		return array;
	}

	@Override
	public EsObject declarePrototype(EsGlobal global) {
		return new EsIntrinsicArray(global.prototypeObject);
	}

	private static EsIntrinsicMethod method_compact() {
		return new EsIntrinsicMethod("compact", new String[] { "nulls" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_compact(ecx);
			}
		};
	}

	// ECMA 15.4.4.4
	/**
	 * @jsmethod concat
	 * @jsnote Concatenates the members of this object with the defined members of each argument (if an Array), or the
	 *         argument itself (if not an Array) to produce a new array.
	 * @jsparam item A primitive value, Array or other Object
	 * @jsreturn A new array, possibly empty.
	 */
	private static EsIntrinsicMethod method_concat() {
		return new EsIntrinsicMethod("concat", new String[] { "item" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_concat(ecx);
			}
		};
	}

	/**
	 * @jsmethod fill
	 * @jsnote The array is extended if necessary, then any undefined members are replaced by a fill value.
	 * @jsparam fillValue Required. Value which should replace any undefined members.
	 * @jsparam extendLengthBy Optional. The amount by which the length of the array should be increased. Default is
	 *          0.
	 * @jsreturn this object, now filled
	 */
	private static EsIntrinsicMethod method_fill() {
		return new EsIntrinsicMethod("fill", new String[] { "fillValue", "extendLengthBy" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_fill(ecx);
			}
		};
	}

	/**
	 * @jsmethod filter
	 * @jsnote Creates a new array with all elements that pass the test implemented by the provided function. This
	 *         array is not changed.
	 * @jsnote see <a
	 *         href="http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/filter">
	 *         Mozilla Array filter</a>
	 * @jsnote The callback is not invoked on <b>undefined</b> array members.
	 * @jsparam callback Required. Function to execute for each defined member. The callback is invoked with three
	 *          arguments: the value of the member, the index of the member, and the array being traversed.
	 * @jsparam thisObject Optional. Object to use as <b>this</b> when executing callback. Default is global.
	 * @jsreturn the new, possibly empty, array.
	 */
	private static EsIntrinsicMethod method_filter() {
		return new EsIntrinsicMethod("filter", new String[] { "callback", "thisObject" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_filter(ecx);
			}
		};
	}

	/**
	 * @jsmethod first
	 * @jsnote Returns the first member of the array.
	 * @jsreturn If the array is empty, returns <b>undefined</b>, otherwise the member at array index 0.
	 */
	private static EsIntrinsicMethod method_first() {
		return new EsIntrinsicMethod("first") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_first(ecx);
			}
		};
	}

	// Mozilla JavaScript 1.6
	/**
	 * @jsmethod forEach
	 * @jsnote Executes a provided function once per defined array member. This array is not changed.
	 * @jsnote see <a
	 *         href="http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/forEach"
	 *         >Mozilla Array forEach</a>
	 * @jsnote The callback is not invoked on <b>undefined</b> array members.
	 * @jsparam callback Required. Function to execute for each defined member. The callback is invoked with three
	 *          arguments: the value of the member, the index of the member, and the array being traversed.
	 * @jsparam thisObject Optional. Object to use as <b>this</b> when executing callback. Default is global.
	 * @jsreturn this object
	 */
	private static EsIntrinsicMethod method_forEach() {
		return new EsIntrinsicMethod("forEach", new String[] { "callback", "thisObject" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_forEach(ecx);
			}
		};
	}

	/**
	 * @jsmethod intersection
	 * @jsparam rhs Required. An array.
	 * @jsparam commonProperties Optional. The properties that are common to both arrays. The properties given here
	 *          define what should be compared to produce the return value.
	 * @jsparam binaryfn Optional. A function which takes two arguments; a matching left-hand array member and
	 *          right-hand array member. The result of the function will be added to the new array.
	 * @jsreturn A new array where each item has two properties 'lhs' and 'rhs'. These properties contain the original
	 *           items that matched on the commonProperties.
	 */
	private static EsIntrinsicMethod method_intersection() {
		return new EsIntrinsicMethod("intersection", new String[] { "rhs", "commonProperties", "binaryfn" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_intersection(ecx);
			}
		};
	}

	// ECMA 15.4.4.5
	/**
	 * @jsmethod join
	 * @jsnote Joins the elements of an array together
	 * @jsparam separator Optional string separator between elements.
	 * @jsreturn A string object.
	 */
	private static EsIntrinsicMethod method_join() {
		return new EsIntrinsicMethod("join", new String[] { "separator" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_join(ecx);
			}
		};
	}

	/**
	 * @jsmethod last
	 * @jsnote Returns the last member of the array, or the member at a specified offset from the last member.
	 * @jsparam offset Optional. Default is 0.
	 * @jsreturn the member at array index (length - 1 - offset). If the array is empty, or offset &gt;= length,
	 *           return value is <b>undefined</b>.
	 */
	private static EsIntrinsicMethod method_last() {
		return new EsIntrinsicMethod("last", new String[] { "offset" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_last(ecx);
			}
		};
	}

	private static EsIntrinsicMethod method_lslParse() {
		return new EsIntrinsicMethod("lslParse", new String[] { "timezone", "sort" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_lslParse(ecx);
			}
		};
	}

	/**
	 * @jsmethod map
	 * @jsnote Creates a new array with the results of calling a provided function on every member in this array. This
	 *         array is not changed.
	 * @jsnote see <a
	 *         href="http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Global_Objects/Array/map">Mozilla
	 *         Array map</a>
	 * @jsnote The callback is not invoked on <b>undefined</b> array members.
	 * @jsparam callback Required. Function to execute for each defined member. The callback is invoked with three
	 *          arguments: the value of the member, the index of the member, and the array being traversed.
	 * @jsparam thisObject Optional. Object to use as <b>this</b> when executing callback. Default is global.
	 * @jsreturn the new, possibly empty, array.
	 */
	private static EsIntrinsicMethod method_map() {
		return new EsIntrinsicMethod("map", new String[] { "callback", "thisObject" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_map(ecx);
			}
		};
	}

	/**
	 * @jsmethod overlay
	 * @jsnote Overlays the members of this object with the defined members of each argument (if an Array), or the
	 *         argument itself (if not an Array) to produce a new array. This array is not changed.
	 * @jsnote Arguments are overlayed left to right, with the first argument considered to be the bottom (least
	 *         significant), and the last argument considered to the top (most significant).
	 * @jsnote As each layer is applied, a defined member can only be overlayed by another defined member.
	 * @jsnote <code>[].overlay(['a0','a1','a2'],['b0','b1'],'c0',[])</code> is <code>['c0','b1','a2']</code>
	 * @jsnote Examples:
	 * @jsnote Given these three arrays:<br/>
	 *         <code>var xa = []; xa[1] = 'a1'; xa[3] = 'a3';</code><br/>
	 *         <code>var xb = []; xb[0] = 'b0'; xb[1] = 'b1';</code><br/>
	 *         <code>var xc = []; xc[3] = 'c3'; xc[4] = 'c4';</code><br/>
	 *         Then...<br/>
	 *         <code>xa.overlay(xb, xc)</code> is <code>['b0','b1',undefined,'c3','c4']</code><br/>
	 *         <code>[].overlay(xc, xb, xa)</code> is <code>['b0','a1',undefined,'a3','c4']</code><br/>
	 * @jsparam item A primitive value, Array or other Object
	 * @jsreturn A new array, possibly empty.
	 */
	private static EsIntrinsicMethod method_overlay() {
		return new EsIntrinsicMethod("overlay", new String[] { "item" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_overlay(ecx);
			}
		};
	}

	/**
	 * @jsmethod partition
	 * @jsnote The array can be partitioned into a left and right side using a predicate function, or an array index.
	 *         The array order is preserved on both sides. This array is not changed.
	 *         <ul>
	 *         <li>If using a predicate function, the left-hand partition is a new array containing those members
	 *         starting at array index 0 for which the predicate function returns false, while the right-hand
	 *         partition is a new array containing the first member for which the predicate function returns true, and
	 *         all subsequent members. The method will not call the predicate with an <b>undefined</b> member, but
	 *         instead assumes it would return false.</li>
	 *         <li>
	 *         If using an array index <i>n</i>, the left-hand partition is a new array containing members
	 *         0..<i>n</i>-1 inclusive, while the right-hand partition contains the members starting at index <i>n</i>
	 *         through to the end of the array. If <i>n</i> is negative, it is interpreted as relative to the length
	 *         of the array. If <i>n</i> is 0, the left-hand partition is always an empty array, and the right-hand is
	 *         a copy of this array. If <i>n</i> is &gt;= the length of the array, the left-hand partition is a copy
	 *         of this array, and the right-hand is an empty array.</li>
	 *         </ul>
	 * @jsnote Examples:
	 * @jsnote <code>['a','b','c','d'].partition(1).rhs</code> is <code>['b','c','d']</code>
	 * @jsnote <code>['a','b','c','d'].partition(-1).rhs</code> is <code>['d']</code>
	 * @jsnote <code>['a','b','c','d'].partition(function(x){return x == 'c';}).lhs</code> is <code>['a','b']</code>
	 * @jsparam predicate Required. If using an array index, it indexes the first (0th) member of the right-hand side.
	 *          If using a predicate function, it is passed three arguments; an array member, its index, and the array
	 *          being traversed. The result of the function will be interpreted as a boolean value.
	 * @jsparam thisObject Optional. An object to use as <b>this</b> when executing predicate. Default is global.
	 * @jsreturn an object containing two array properties 'lhs' and 'rhs'.
	 */
	private static EsIntrinsicMethod method_partition() {
		return new EsIntrinsicMethod("partition", new String[] { "predicate", "thisObject" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_partition(ecx);
			}
		};
	}

	// ECMA 15.4.4.7
	/**
	 * @jsmethod push
	 * @jsnote adds one or more objects to the end of the array
	 * @jsparam object An object to add to the array, may be repeated.
	 * @jsreturn the new length of the array
	 */
	private static EsIntrinsicMethod method_push() {
		return new EsIntrinsicMethod("push") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_push(ecx, false);
			}
		};
	}

	private static EsIntrinsicMethod method_pushEach() {
		return new EsIntrinsicMethod("pushEach") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_push(ecx, true);
			}
		};
	}

	/**
	 * @jsmethod reduce
	 * @jsnote Apply a function simultaneously against two values of the array (from left-to-right) so as to reduce it
	 *         to a single value. This array is not changed.
	 * @jsnote see <a
	 *         href="http://developer.mozilla.org/en/Core_JavaScript_1.5_Reference/Objects/Array/reduce">Mozilla Array
	 *         reduce</a>
	 * @jsnote The callback is not invoked on <b>undefined</b> array members.
	 * @jsparam callback Required. Function to execute for each defined member. The callback is invoked with four
	 *          arguments: the progress value, the current value, the index of the current value, and the array being
	 *          traversed.
	 * @jsparam initialValue Optional. Value to use as the progress value in the first call of the callback. If
	 *          supplied, the first callback invocation will use initialValue as the progress value, and the first
	 *          defined member of the array as the current value. If not supplied, the first callback will use the
	 *          first defined member as the progress value, and the second defined member as the current value.
	 * @jsreturn the value returned by the final invocation of the callback. If the array is empty, or all members are
	 *           undefined, the callback will never be invoked and the return value will be the initialValue if
	 *           supplied; however, no initialValue is supplied, the method will throw an api exception.
	 */
	private static EsIntrinsicMethod method_reduce() {
		return new EsIntrinsicMethod("reduce", new String[] { "callback", "initialValue" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_reduce(ecx);
			}
		};
	}

	// ECMA 15.4.4.10
	/**
	 * @jsmethod slice
	 * @jsparam start Optional. An integer giving the first index of the subarray, if negative then indicates the
	 *          start index from the end of the array. Defaults to 0.
	 * @jsparam end Optional. An integer giving the last index of the subarray, if negative then indicates the end
	 *          index from the end of the array. Defaults to the end of the array.
	 * @jsnote Examples:
	 * @jsnote <code>array.slice()</code> will return the full array.
	 * @jsnote <code>array.slice(2)</code> will return part of the array from index 2 to the end.
	 * @jsnote <code>array.slice(2,4)</code> will return part of the array from index 2 to index 4.
	 * @jsreturn A new array that is a section of the array.
	 */
	private static EsIntrinsicMethod method_slice() {
		return new EsIntrinsicMethod("slice", new String[] { "start", "end" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_slice(ecx);
			}
		};
	}

	// ECMA 15.4.4.11
	/**
	 * @jsmethod sort
	 * @jsnote The comparison function should have two parameters and return return -1 if the first argument is less
	 *         than the second, +1 if the first is greater than the second or 0 if they are equal.
	 * @jsparam comparefn A comparison function. Optional. If not given then a string comparison is used.
	 * @jsreturn the object, now sorted.
	 */
	private static EsIntrinsicMethod method_sort() {
		return new EsIntrinsicMethod("sort", new String[] { "comparefn" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_sort(ecx);
			}
		};
	}

	/**
	 * @jsmethod subtract
	 * @jsparam rhs Required. An array.
	 * @jsparam commonProperties Optional The properties that are common to both arrays. The properties given here
	 *          define what should be compared to produce the return value.
	 * @jsreturn A new array consisting of those members of this array that are <i>not</i> members of rhs array.
	 *           Equality between members of the arrays is inferred from the commonProperties.
	 */
	private static EsIntrinsicMethod method_subtract() {
		return new EsIntrinsicMethod("subtract", new String[] { "rhs", "commonProperties" }, 1) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_subtract(ecx);
			}
		};
	}

	private static EsIntrinsicMethod method_tail() {
		return new EsIntrinsicMethod("tail") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_tail(ecx);
			}
		};
	}

	private static EsIntrinsicMethod method_toMap() {
		return new EsIntrinsicMethod("toMap", new String[] { "errorSuffix" }, 0) {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_toMap(ecx);
			}
		};
	}

	// ECMA 15.4.4.2
	/**
	 * @jsmethod toString
	 * @jsnote Is the same as the join method
	 * @jsreturn A string representation of the array
	 */
	private static EsIntrinsicMethod method_toString() {
		return new EsIntrinsicMethod("toString") {

			@Override
			protected IEsOperand eval(EsExecutionContext ecx)
					throws InterruptedException {
				return UNeonArray.array_join(ecx);
			}
		};
	}

	public static EsIntrinsicArrayConstructor newInstance() {
		return new EsIntrinsicArrayConstructor();
	}

	/**
	 * @jsconstructor Array
	 * @jsparam values zero or more values to initialize the array
	 */
	private EsIntrinsicArrayConstructor() {
		super(ClassName, new String[] { "values" }, 0);
	}
}
