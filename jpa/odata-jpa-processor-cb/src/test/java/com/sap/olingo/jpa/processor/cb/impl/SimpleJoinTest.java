package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.persistence.Tuple;
import javax.persistence.criteria.JoinType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAJoinColumn;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;

class SimpleJoinTest extends BuilderBaseTest {
  private SimpleJoin<BusinessPartner, BusinessPartnerRole> cut;
  private AliasBuilder ab;
  private CriteriaBuilderImpl cb;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    ab = new AliasBuilder();
    cb = new CriteriaBuilderImpl(sd, new ParameterBuffer());
    final ProcessorCriteriaQuery<Tuple> q = cb.createTupleQuery();
    final JPAAssociationPath path = sd.getEntity(BusinessPartner.class).getAssociationPath("Roles");
    cut = new SimpleJoin<>(path, JoinType.INNER, q.from(BusinessPartner.class), ab, cb);

    final JPAJoinColumn joinColumn = mock(JPAJoinColumn.class);
    when(joinColumn.getName()).thenReturn("One");
    when(joinColumn.getReferencedColumnName()).thenReturn("Two");
    cut.createOn(Arrays.asList(joinColumn));
  }

  @Test
  void testGetOn() {
    assertNotNull(cut.getOn());
  }

  @Test
  void testGetJoinType() {
    assertEquals(JoinType.INNER, cut.getJoinType());
  }

  @Test
  void testGetAttributeNotImplemented() {
    assertThrows(NotImplementedException.class, () -> cut.getAttribute());
  }

}
