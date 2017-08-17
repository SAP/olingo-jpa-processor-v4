package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import org.junit.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.DayOfWeek;

public class TestIntermediateEnumerationType extends TestMappingRoot {
  private IntermediateEnumerationType cut;

  @Test
  public void checkGet() throws ODataJPAModelException {
    cut = new IntermediateEnumerationType(new JPAEdmNameBuilder(PUNIT_NAME), DayOfWeek.class);
    cut.getEdmItem();
  }
}
