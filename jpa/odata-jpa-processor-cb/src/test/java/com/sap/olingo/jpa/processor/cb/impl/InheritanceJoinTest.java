package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.JoinType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.cb.exceptions.InternalServerError;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinAccount;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinCompoundSub;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinCompoundSuper;
import com.sap.olingo.jpa.processor.core.testmodel.InheritanceByJoinCurrentAccount;

class InheritanceJoinTest extends BuilderBaseTest {
  private AliasBuilder aliasBuilder;
  private CriteriaBuilderImpl cb;
  private InheritanceJoin<InheritanceByJoinCurrentAccount, InheritanceByJoinAccount> cut;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    aliasBuilder = new AliasBuilder();
    cb = new CriteriaBuilderImpl(sd, new SqlDefaultPattern());
    final var et = sd.getEntity(InheritanceByJoinCurrentAccount.class);
    final ProcessorCriteriaQuery<Tuple> query = cb.createTupleQuery();

    cut = new InheritanceJoin<>(et, query.from(InheritanceByJoinCurrentAccount.class), aliasBuilder, cb);
  }

  @Test
  void testGetJoinType() {
    assertEquals(JoinType.INNER, cut.getJoinType());
  }

  @Test
  void testGetAttributeNotImplemented() {
    assertThrows(NotImplementedException.class, () -> cut.getAttribute());
  }

  @Test
  void testGetOnThrowsInternalErrorOnException() throws ODataJPAModelException {
    final ProcessorCriteriaQuery<Tuple> query = cb.createTupleQuery();
    final JPAEntityType subType = mock(JPAEntityType.class);
    final JPAEntityType superType = sd.getEntity(InheritanceByJoinAccount.class);
    when(subType.getInheritanceInformation()).thenThrow(ODataJPAModelException.class);
    when(subType.getBaseType()).thenReturn(superType);
    cut = new InheritanceJoin<>(subType, query.from(InheritanceByJoinCurrentAccount.class), aliasBuilder, cb);
    assertThrows(InternalServerError.class, () -> cut.getOn());
  }

  @Test
  void testGetOnReturnsList() throws ODataJPAModelException {
    final ProcessorCriteriaQuery<Tuple> query = cb.createTupleQuery();
    final JPAEntityType subType = sd.getEntity(InheritanceByJoinCompoundSub.class);
    InheritanceJoin<InheritanceByJoinCompoundSub, InheritanceByJoinCompoundSuper> join = new InheritanceJoin<>(subType,
        query.from(InheritanceByJoinCompoundSub.class), aliasBuilder, cb);
    var act = join.getOn();
    assertNotNull(act);
    var statement = new StringBuilder();
    ((SqlConvertible) act).asSQL(statement);
    assertTrue(statement.toString().contains("E0.\"PartCode\" = E1.\"DivisionCode\""));
  }

  @Test
  void testGetSuperTypeThrowsInternalErrorOnException() throws ODataJPAModelException {
    final ProcessorCriteriaQuery<Tuple> query = cb.createTupleQuery();
    final JPAEntityType subType = mock(JPAEntityType.class);

    when(subType.getBaseType()).thenThrow(ODataJPAModelException.class);
    assertThrows(InternalServerError.class, () -> new InheritanceJoin<>(subType, query.from(
        InheritanceByJoinCurrentAccount.class), aliasBuilder, cb));
  }
}
