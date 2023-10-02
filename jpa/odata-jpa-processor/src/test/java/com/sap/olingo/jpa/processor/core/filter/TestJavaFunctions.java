package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

public class TestJavaFunctions {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected TestHelper helper;
  protected Map<String, List<String>> headers;
  protected static JPADefaultEdmNameBuilder nameBuilder;
  protected static DataSource ds;

  @BeforeAll
  public static void setupClass() throws ODataJPAModelException {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    emf.getProperties();
  }

  @Test
  void testFilterOnFunction() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds,
        "TemporalWithValidityPeriods?$filter=com.sap.olingo.jpa.At(Date=2022-12-01)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
    final ArrayNode jobs = helper.getValues();
    assertEquals(2, jobs.size());
  }

  @Test
  void testFilterOnFunctionViaNavigation() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, ds,
        "Persons('99')/Jobs?$filter=com.sap.olingo.jpa.At(Date=2022-12-01)",
        "com.sap.olingo.jpa.processor.core.testobjects");
    helper.assertStatus(200);
    final ArrayNode jobs = helper.getValues();
    assertEquals(1, jobs.size());
    assertEquals("2022-11-01", jobs.get(0).get("ValidityStartDate").asText());
  }
}
