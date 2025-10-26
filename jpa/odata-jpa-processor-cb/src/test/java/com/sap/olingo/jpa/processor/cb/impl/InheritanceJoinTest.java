package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.JoinType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinAccount;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinCurrentAccount;

class InheritanceJoinTest extends BuilderBaseTest {
  private AliasBuilder ab;
  private CriteriaBuilderImpl cb;
  private InheritanceJoin<InheritanceByJoinCurrentAccount, InheritanceByJoinAccount> cut;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    ab = new AliasBuilder();
    cb = new CriteriaBuilderImpl(sd, new SqlDefaultPattern());
    final var et = sd.getEntity(InheritanceByJoinCurrentAccount.class);
    final ProcessorCriteriaQuery<Tuple> query = cb.createTupleQuery();

    cut = new InheritanceJoin<>(et, query.from(InheritanceByJoinCurrentAccount.class), ab, cb);
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
