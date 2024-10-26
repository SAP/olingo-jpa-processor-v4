package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.EntityType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;

class CriteriaUpdateImplTest extends BuilderBaseTest {
  private EntityType<AdministrativeDivision> entity;
  private CriteriaUpdate<AdministrativeDivision> cut;
  private CriteriaBuilder cb;
  private ProcessorSqlPatternProvider sqlPattern;

  @BeforeEach
  void setup() {
    final var parameterBuffer = new ParameterBuffer();
    sqlPattern = new SqlDefaultPattern();
    cb = new CriteriaBuilderImpl(sd, parameterBuffer, sqlPattern);
    cut = new CriteriaUpdateImpl<>(sd, cb, parameterBuffer, AdministrativeDivision.class, sqlPattern);
  }

  @Test
  void testGetRootReturnsValue() {
    assertNotNull(cut.getRoot());
  }

  @Test
  void testGetFromReturnsRoot() {
    assertEquals(cut.getRoot(), cut.from(AdministrativeDivision.class));
    assertEquals(cut.getRoot(), cut.from(entity));
  }

  @Test
  void testWhereWithExpression() {
    final var sql = new StringBuilder();
    final var root = cut.getRoot();
    final Path<Object> publisher = root.get("codePublisher");

    final var act = cut.where(cb.equal(publisher, "ISO"));
    assertEquals(cut, act);
    ((SqlConvertible) cut).asSQL(sql);
    assertTrue(sql.toString().contains("WHERE (E0.\"CodePublisher\" = ?1)"));
  }

  @Test
  void testWhereWithPredicateThrowsException() {
    final Predicate predicate = mock(Predicate.class);
    assertThrows(NotImplementedException.class, () -> cut.where(new Predicate[] { predicate }));
  }

  @Test
  void testSetWithPathAndValue() {
    final var sql = new StringBuilder();
    final var root = cut.getRoot();
    final Path<String> publisher = root.get("codePublisher");

    final var act = cut.set(publisher, "DIN");
    assertEquals(cut, act);
    ((SqlConvertible) cut).asSQL(sql);
    assertTrue(sql.toString().contains("SET E0.\"CodePublisher\" = ?1"));
  }

  @Test
  void testGetRestrictionThrowsException() {
    assertThrows(NotImplementedException.class, () -> cut.getRestriction());
  }

  @Test
  void testSubqueryNotNull() {
    assertNotNull(cut.subquery(AdministrativeDivision.class));
  }
}
