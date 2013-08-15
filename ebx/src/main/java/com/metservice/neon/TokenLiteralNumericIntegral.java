/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights
 * reserved. No part of this work may be stored in a retrievable system,
 * transmitted  or reproduced in any way without the prior written
 * permission of the Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * 
 * @author roach
 */
class TokenLiteralNumericIntegral extends TokenLiteralNumeric
{
  public final int value;
  TokenLiteralNumericIntegral(int lineIndex, int startIndex, int value)
  {
    super(lineIndex, startIndex);
    this.value = value;
  }
  
  public String toScript() {
    return Integer.toString(value);
  }
  public String toString() {
    return toScript();
  }
}
