package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;

class TestScalarDbFunctions {

  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  protected static EntityManagerFactory emf;
  protected Map<String, List<String>> headers;
  protected static JPADefaultEdmNameBuilder nameBuilder;
  protected static DataSource ds;

  @BeforeAll
  public static void setupClass() {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
  }

  @Test
  void testFilterOnFunctionAndProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Area=$it/Area,Population=$it/Population)  mul 1000000 gt 1000 and ParentDivisionCode eq 'BE255'&orderBy=DivisionCode)");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
    assertEquals("35002", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterOnFunction() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Area=$it/Area,Population=$it/Population) gt 1");
    helper.assertStatus(200);
  }

  private static Stream<Arguments> provideFunctionQueries() {
    return Stream.of(
        arguments("FunctionAndMultiply",
            "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Area=Area,Population=Population)  mul 1000000 gt 100",
            59),
        arguments("FunctionWithFixedValue",
            "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Area=13079087,Population=$it/Population)  mul 1000000 gt 1000",
            29),
        arguments("FunctionComputedValue",
            "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Area=Area div 1000000,Population=Population) gt 1000",
            7),
        arguments("FunctionMixParamOrder",
            "AdministrativeDivisions?$filter=com.sap.olingo.jpa.PopulationDensity(Population=Population,Area=Area) mul 1000000 gt 1000",
            7),
        arguments("FunctionMixParamOrder",
            "AdministrativeDivisions?$filter=com.sap.olingo.jpa.ConvertToQkm(Area=$it/Area) gt 100",
            4));
  }

  @ParameterizedTest
  @MethodSource("provideFunctionQueries")
  void testFilterOnFunctionAndMultiply(final String text, final String queryString, final int numberOfResults)
      throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, queryString);
    helper.assertStatus(200);

    final ArrayNode divisions = helper.getValues();
    assertEquals(numberOfResults, divisions.size(), text);
  }

  @Test
  void testFilterOnFunctionNested() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        """
            AdministrativeDivisions?$filter=\
            com.sap.olingo.jpa.PopulationDensity(Area=\
            com.sap.olingo.jpa.ConvertToQkm(Area=$it/Area),Population=$it/Population) gt 1000""");
    helper.assertStatus(200);
    final ArrayNode jobs = helper.getValues();
    assertEquals(7, jobs.size());
  }
}
