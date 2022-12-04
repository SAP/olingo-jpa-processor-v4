package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

class ODataJPAQueryContextTest {

  private JPAVisitor jpaVisitor;
  private JPAEntityType et;
  private From<?, ?> from;
  private CriteriaBuilder cb;
  private ODataJPAQueryContext cut;

  @BeforeEach
  void setUp() throws Exception {
    jpaVisitor = mock(JPAVisitor.class);
    from = mock(From.class);
    et = mock(JPAEntityType.class);
    cb = mock(CriteriaBuilder.class);
    when(jpaVisitor.getEntityType()).thenReturn(et);
    when(jpaVisitor.getCriteriaBuilder()).thenReturn(cb);
    doReturn(from).when(jpaVisitor).getRoot();
    cut = new ODataJPAQueryContext(jpaVisitor);
  }

  @Test
  void testGetEntityType() {
    assertEquals(et, cut.getEntityType());
  }

  @Test
  void testGetFrom() {
    assertEquals(from, cut.getFrom());
  }

  @Test
  void testGetCriteriaBuilder() {
    assertEquals(cb, cut.getCriteriaBuilder());
  }
}
