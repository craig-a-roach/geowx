/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;


/**
 * @author roach
 */
public interface IKryptonEditionTable<T extends IKryptonEditionTable<T>> {

	public T newSum(T rhs);
}
