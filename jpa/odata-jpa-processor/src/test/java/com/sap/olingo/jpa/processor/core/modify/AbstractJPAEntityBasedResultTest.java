package com.sap.olingo.jpa.processor.core.modify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPAResultConverter;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.util.TestBase;

abstract class AbstractJPAEntityBasedResultTest extends TestBase {
  JPAResultConverter converter;

  @BeforeEach
  void setup() throws ODataException {
    getHelper();
    converter = mock(JPAResultConverter.class);
  }

  @Test
  void testConvertEntityResult() throws ODataJPAModelException, ODataApplicationException {
    final var et = helper.getJPAEntityType(AdministrativeDivision.class);
    final var division = createAdministrativeDivision();

    final var act = createResult(et, division);

    assertEquals(1, act.getResults().size());
    final var actRoot = act.getResult(JPAExpandResult.ROOT_RESULT_KEY).get(0);
    assertNotNull(actRoot);
    assertEquals("ISO", actRoot.get("CodePublisher"));
  }

  abstract Object createAdministrativeDivision();

  abstract JPACreateResult createResult(final JPAEntityType et, final Object result) throws ODataJPAModelException,
      ODataApplicationException;

}
