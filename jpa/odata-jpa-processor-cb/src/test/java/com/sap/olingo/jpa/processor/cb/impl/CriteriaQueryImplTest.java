package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorSelection;
import com.sap.olingo.jpa.processor.cb.exceptions.InternalServerError;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

class CriteriaQueryImplTest extends BuilderBaseTest {
  private CriteriaQueryImpl<Object> cut;
  private CriteriaBuilder cb;

  @BeforeEach
  void setup() throws ODataJPAModelException {
    cb = new CriteriaBuilderImpl(sd, new ParameterBuffer());
    cut = new CriteriaQueryImpl<>(Object.class, sd, cb);
  }

  @Test
  void testSetDistinctTrue() {
    cut.distinct(true);
    assertTrue(cut.isDistinct());
  }

  @Test
  void testSetDistinctFalse() {
    cut.distinct(false);
    assertFalse(cut.isDistinct());
  }

  @Test
  void testCreateSelectFromAttribute() {
    final Root<?> adminDivision = cut.from(AdministrativeDivision.class);
    final CriteriaQuery<Object> act = cut.multiselect(adminDivision.get("codeID"));
    assertNotNull(act);
  }

  @Test
  void testCreateOrderByWithArray() {
    final Root<?> adminDivision = cut.from(AdministrativeDivision.class);
    final CriteriaQuery<Object> act = cut.orderBy(cb.desc(adminDivision.get("codeID")));
    assertNotNull(act.getOrderList());
    assertEquals(1, act.getOrderList().size());
  }

  @Test
  void testCreateOrderByWithList() {
    final Root<?> adminDivision = cut.from(AdministrativeDivision.class);
    final CriteriaQuery<Object> act = cut.orderBy(Arrays.asList(cb.desc(adminDivision.get("codeID")), cb.asc(
        adminDivision.get("divisionCode"))));
    assertNotNull(act.getOrderList());
    assertEquals(2, act.getOrderList().size());
  }

  @Test
  void testResetOrderByWithArray() {
    final Root<?> adminDivision = cut.from(AdministrativeDivision.class);
    final Order[] nullArray = null;
    CriteriaQuery<Object> act = cut.orderBy(Arrays.asList(cb.desc(adminDivision.get("codeID")), cb.asc(adminDivision
        .get("divisionCode"))));
    act = cut.orderBy(nullArray);
    assertNotNull(act.getOrderList());
    assertEquals(0, act.getOrderList().size());
  }

  @Test
  void testResetOrderByWithList() {
    final Root<?> adminDivision = cut.from(AdministrativeDivision.class);
    final List<Order> nullList = null;
    CriteriaQuery<Object> act = cut.orderBy(Arrays.asList(cb.desc(adminDivision.get("codeID")), cb.asc(adminDivision
        .get("divisionCode"))));
    act = cut.orderBy(nullList);
    assertNotNull(act.getOrderList());
    assertEquals(0, act.getOrderList().size());
  }

  @Test
  void testWithBaseClass() {
    final StringBuilder statement = new StringBuilder();
    final Root<?> act = cut.from(Organization.class);
    cut.multiselect(act.get("iD"));
    assertEquals("SELECT E0.\"ID\" S0 FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE (E0.\"Type\" = ?1)",
        cut.asSQL(statement).toString());
  }

  @Test
  void testGroupBy() {
    final StringBuilder statement = new StringBuilder();
    final Root<?> act = cut.from(Organization.class);
    cut.groupBy(act.get("aBCClass"), act.get("name2"));
    cut.multiselect(act.get("aBCClass"), act.get("name2"));
    assertEquals(
        "SELECT E0.\"ABCClass\" S0, E0.\"NameLine2\" S1 FROM \"OLINGO\".\"BusinessPartner\" E0 "
            + "WHERE (E0.\"Type\" = ?1) GROUP BY E0.\"ABCClass\", E0.\"NameLine2\"",
        cut.asSQL(statement).toString());
  }

  @Test
  void testReplaceGroupByEmpty() {
    final Expression<?>[] nullArray = null;
    final StringBuilder statement = new StringBuilder();
    final Root<?> act = cut.from(Organization.class);
    cut.groupBy(act.get("aBCClass"), act.get("name2"));
    cut.multiselect(act.get("aBCClass"), act.get("name2"));
    cut.groupBy(nullArray);
    assertEquals(
        "SELECT E0.\"ABCClass\" S0, E0.\"NameLine2\" S1 FROM \"OLINGO\".\"BusinessPartner\" E0 "
            + "WHERE (E0.\"Type\" = ?1)",
        cut.asSQL(statement).toString());
  }

  @Test
  void testHaving() {
    final StringBuilder statement = new StringBuilder();
    final Root<?> act = cut.from(Organization.class);
    cut.having(cb.gt(cb.count(act.get("iD")), 1));
    cut.groupBy(act.get("name2"));
    cut.multiselect(act.get("aBCClass"), act.get("name2"));
    assertEquals(
        "SELECT E0.\"ABCClass\" S0, E0.\"NameLine2\" S1 FROM \"OLINGO\".\"BusinessPartner\" E0 "
            + "WHERE (E0.\"Type\" = ?2) "
            + "GROUP BY E0.\"NameLine2\" "
            + "HAVING (COUNT(E0.\"ID\") > ?1)",
        cut.asSQL(statement).toString());
  }

  @Test
  void testDefaultImplementationOnPathWrapper() {
    final Root<?> act = cut.from(Organization.class);
    cut.multiselect(act.get("aBCClass"), act.get("name2"));
    final Selection<Object> selection = cut.getSelection();
    final List<Entry<String, JPAPath>> resolvedSelections = ((ProcessorSelection<?>) selection).getResolvedSelection();
    assertNotNull(resolvedSelections.get(0).getValue());
  }

  @Test
  void testFromRethrowsException() throws ODataJPAModelException {
    final JPAServiceDocument serviceDocument = mock(JPAServiceDocument.class);
    when(serviceDocument.getEntity(any(Class.class))).thenThrow(ODataJPAModelException.class);
    cut = new CriteriaQueryImpl<>(Object.class, serviceDocument, cb);
    assertThrows(InternalServerError.class, () -> cut.from(AdministrativeDivision.class));
  }
}
