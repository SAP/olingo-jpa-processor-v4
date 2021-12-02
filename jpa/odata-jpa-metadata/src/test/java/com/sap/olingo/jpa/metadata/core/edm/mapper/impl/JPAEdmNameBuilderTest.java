package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class JPAEdmNameBuilderTest {
  private JPADefaultEdmNameBuilder cut;

  @BeforeEach
  public void setup() throws ODataJPAModelException {

  }

  @Test
  void CheckBuildContainerNameSimple() {
    cut = new JPADefaultEdmNameBuilder("cdw");
    assertEquals("CdwContainer", cut.buildContainerName());
  }

  @Test
  void CheckBuildContainerNameComplex() {
    cut = new JPADefaultEdmNameBuilder("org.apache.olingo.jpa");
    assertEquals("OrgApacheOlingoJpaContainer", cut.buildContainerName());
  }
}
