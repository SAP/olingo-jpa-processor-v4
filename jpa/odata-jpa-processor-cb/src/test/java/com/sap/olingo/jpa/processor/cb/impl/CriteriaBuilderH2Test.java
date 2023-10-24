package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

class CriteriaBuilderH2Test extends CriteriaBuilderOverallTest {
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String[] enumPackages = { "com.sap.olingo.jpa.processor.core.testmodel" };
  private static EntityManagerFactory emf;
  private static JPAServiceDocument sd;
  private static JPAEdmProvider edmProvider;
  private static DataSource ds;

  @BeforeAll
  public static void classSetup() throws ODataException {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_H2);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    edmProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, enumPackages);
    sd = edmProvider.getServiceDocument();
    sd.getEdmEntityContainer();
  }

  @BeforeEach
  public void setup() {
    super.setup(emf, sd);
  }

  @Test
  void testRowNumberSupport() {

    final ProcessorCriteriaQuery<Tuple> cq = cb.createTupleQuery();
    final ProcessorSubquery<?> nuts3RowQuery = cq.subquery(AdministrativeDivision.class);
    final ProcessorSubquery<?> nuts2Query = nuts3RowQuery.subquery(AdministrativeDivision.class);
    final ProcessorSubquery<?> nuts2RowQuery = nuts2Query.subquery(AdministrativeDivision.class);
    final ProcessorSubquery<AdministrativeDivision> nuts1 = nuts2RowQuery.subquery(AdministrativeDivision.class);

    final Root<?> nuts3Root = cq.from(nuts3RowQuery);
    final Root<AdministrativeDivision> nuts3RowRoot = nuts3RowQuery.from(AdministrativeDivision.class);
    final Root<AdministrativeDivision> nuts2Root = nuts2Query.from(nuts2RowQuery);
    final Root<AdministrativeDivision> nuts2RowRoot = nuts2RowQuery.from(AdministrativeDivision.class);
    final Root<AdministrativeDivision> nuts1Root = nuts1.from(AdministrativeDivision.class);

    nuts1.multiselect(nuts1Root.get("codePublisher"), nuts1Root.get("codeID"), nuts1Root.get("divisionCode"));
    nuts1.where(cb.equal(nuts1Root.get("codeID"), "NUTS1"));
    nuts1.orderBy(cb.asc(nuts1Root.get("codePublisher")), cb.asc(nuts1Root.get("codeID")), cb.asc(nuts1Root.get(
        "divisionCode")));
    nuts1.setMaxResults(4);
    nuts1.setFirstResult(1);

    final Expression<Long> nuts2RowNo = createRowNumber(nuts2RowRoot);
    nuts2RowQuery.multiselect(nuts2RowRoot, nuts2RowNo);
    nuts2RowQuery.where(cb.in(Arrays.asList(nuts2RowRoot.get("codePublisher"), nuts2RowRoot.get("parentCodeID"),
        nuts2RowRoot.get("parentDivisionCode")), nuts1));

    nuts2Query.multiselect(nuts2Root.get("codePublisher"), nuts2Root.get("codeID"), nuts2Root.get("divisionCode"));
    nuts2Query.where(cb.lt(nuts2Root.get("row_no"), 3));

    final Expression<Long> nuts3RowNo = createRowNumber(nuts3RowRoot);
    nuts3RowQuery.multiselect(nuts3RowRoot, nuts3RowNo);
    nuts3RowQuery.where(cb.in(Arrays.asList(nuts3RowRoot.get("codePublisher"), nuts3RowRoot.get("parentCodeID"),
        nuts3RowRoot.get("parentDivisionCode")), nuts2Query));

    cq.multiselect(nuts3Root.get("codePublisher"), nuts3Root.get("codeID"), nuts3Root.get("divisionCode").alias(
        "divisionCode"));
    cq.where(cb.and(cb.gt(nuts3Root.get("row_no"), 1),
        cb.lt(nuts3Root.get("row_no"), 3)));

    final TypedQuery<Tuple> typedQuery = em.createQuery(cq);
    // typedQuery.setMaxResults(4);
    final List<Tuple> result = typedQuery.getResultList();
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("BE212", result.get(0).get("divisionCode"));
  }

  private Expression<Long> createRowNumber(final Root<AdministrativeDivision> root) {
    return (Expression<Long>) cb.rowNumber()
        .orderBy(cb.asc(root))
        .partitionBy(root.get("codePublisher"), root.get("parentCodeID"), root.get("parentDivisionCode"))
        .alias("row_no");
  }
}
