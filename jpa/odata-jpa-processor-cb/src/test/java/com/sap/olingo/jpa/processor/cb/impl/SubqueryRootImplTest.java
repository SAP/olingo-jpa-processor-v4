package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;

class SubqueryRootImplTest {
  private SubqueryRootImpl<?> cut;

  private ProcessorSubquery<?> processor;
  private JPAServiceDocument sd;
  private AliasBuilder aliasBuilder;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    processor = mock(ProcessorSubquery.class);
    sd = mock(JPAServiceDocument.class);
    aliasBuilder = new AliasBuilder();
    cut = new SubqueryRootImpl<>(processor, aliasBuilder, sd);
  }

  @Test
  void testHashCode() {
    assertNotEquals(31, cut.hashCode());
  }

  @Test
  void testGetModelThrowsException() {
    assertThrows(NotImplementedException.class, () -> cut.getModel());
  }

  @Test
  void testEquals() throws ODataJPAModelException {
    final ProcessorSubquery<?> otherQuery = mock(ProcessorSubquery.class);
    final SubqueryRootImpl<?> other = new SubqueryRootImpl<>(otherQuery, aliasBuilder, sd);
    assertEquals(cut, cut);
    assertNotEquals(cut, null); // NOSONAR
    assertNotEquals(cut, other);
  }

  @Test
  void testEqualsOtherClass() {
    final FromImpl<?, ?> other = mock(FromImpl.class);
    assertNotEquals(cut, other);
  }
}
