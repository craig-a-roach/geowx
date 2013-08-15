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
class TokenLiteralNumericDouble extends TokenLiteralNumeric
{
  public final double value;
  TokenLiteralNumericDouble(int lineIndex, int startIndex, double value)
  {
    super(lineIndex, startIndex);
    this.value = value;
  }
  
  public String toScript() {
    return Double.toString(value);
  }
  public String toString() {
    return toScript();
  }
}
