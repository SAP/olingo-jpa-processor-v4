package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class JPAServiceDocumentFactoryTest {
  private JPAServiceDocumentFactory cut;

  @BeforeEach
  void setup() {
    cut = new JPAServiceDocumentFactory();
  }

  @Test
  void testAsUserGroupRestricted() throws ODataJPAModelException {
    final var sd = mock(JPAServiceDocument.class);

    assertEquals(sd, cut.asUserGroupRestricted(sd, List.of(), true));
  }
}
