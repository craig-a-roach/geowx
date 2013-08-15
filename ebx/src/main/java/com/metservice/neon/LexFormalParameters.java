/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights
 * reserved. No part of this work may be stored in a retrievable system,
 * transmitted  or reproduced in any way without the prior written
 * permission of the Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author roach
 */
class LexFormalParameters extends Lex
{
  public LexFormalParameter[] zptFormalParameters() {
    return m_zptFormalParameters;
  }
  
  public static LexFormalParameters newInstance(TokenReader tr)
  throws EsSyntaxException
  {
    final List<LexFormalParameter> zlFormalParameters = new ArrayList<LexFormalParameter>();
    final LexFormalParameter oFirst = LexFormalParameter.createInstance(tr);
    if (oFirst != null)
    {
      zlFormalParameters.add(oFirst);
      while(tr.current().isPunctuator(Punctuator.COMMA))
      {
        tr.consume();
        zlFormalParameters.add(LexFormalParameter.newInstance(tr));
      }
    }
    return new LexFormalParameters(zlFormalParameters);
  }
  
  private static final LexFormalParameter[] EMPTY = new LexFormalParameter[0]; 
  
  private final LexFormalParameter[] m_zptFormalParameters;
  
  private LexFormalParameters(List<LexFormalParameter> zlFormalParameters)
  {
    assert zlFormalParameters != null;
    m_zptFormalParameters = zlFormalParameters.toArray(EMPTY);
  }

  @Override
  public String toScript()
  {
    StringBuffer b = new StringBuffer();
    for (int i=0; i < m_zptFormalParameters.length; i++)
    {
      if (i > 0) {
        b.append(",");
      }
      b.append(m_zptFormalParameters[i].toScript());
    }
    return b.toString();
  }
}

