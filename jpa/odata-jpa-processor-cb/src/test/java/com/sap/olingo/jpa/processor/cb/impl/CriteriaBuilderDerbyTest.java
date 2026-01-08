package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlFunction;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlOperator;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlParameter;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPattern;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class CriteriaBuilderDerbyTest extends CriteriaBuilderOverallTest {
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String[] enumPackages = { "com.sap.olingo.jpa.processor.core.testmodel" };
  private static EntityManagerFactory emf;
  private static JPAServiceDocument sd;
  private static JPAEdmProvider edmProvider;
  private static DataSource ds;

  @BeforeAll
  static void classSetup() throws ODataException {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_DERBY);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    edmProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, enumPackages);
    sd = edmProvider.getServiceDocument();
    sd.getEdmEntityContainer();
  }

  @BeforeEach
  void setup() {
    super.setup(emf, sd, new SqlPattern());
  }

  @Override
  @Test
  void testSimpleConcatQuery() {
    final Root<?> person = query.from(Person.class);
    final Expression<String> concat = cb.concat(cb.concat(person.get("lastName"), ","), person.get("firstName"));

    query.multiselect(person.get("iD"));
    query.where(cb.equal(concat, "Mustermann,Max"));
    ((SqlConvertible) query).asSQL(statement);
    assertEquals(
        expectedQueryConcat(),
        statement.toString().trim());

    // Test need to be skipped, as the following error occurs:
    // Internal Exception: java.sql.SQLSyntaxErrorException: Comparisons between 'LONG VARCHAR (UCS_BASIC)' and 'LONG
    // VARCHAR (UCS_BASIC)' are not supported.
  }

  @Override
  protected String expectedQueryLimitOffset() {
    return "SELECT E0.\"ID\" S0 FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE (E0.\"Type\" = ?1) OFFSET 1 ROWS FETCH NEXT 1 ROWS ONLY";
  }

  @Override
  protected String expectedQuerySubstring() {
    return "SELECT E0.\"CodeID\" S0 FROM \"OLINGO\".\"AdministrativeDivisionDescription\" E0 WHERE ((E0.\"LanguageISO\" = ?1) AND (LOWER(SUBSTR(E0.\"Name\", ?2, ?3)) = ?4))";
  }

  @Override
  protected String expectedQueryConcat() {
    return "SELECT E0.\"ID\" S0 FROM \"OLINGO\".\"BusinessPartner\" E0 WHERE ((((E0.\"NameLine2\" || ?1) || E0.\"NameLine1\") = ?2) AND (E0.\"Type\" = ?3))";
  }

  private static class SqlPattern implements ProcessorSqlPatternProvider {

    @Override
    public ProcessorSqlPattern getConcatenatePattern() {
      return new ProcessorSqlOperator(Arrays.asList(
          new ProcessorSqlParameter(VALUE_PLACEHOLDER, false),
          new ProcessorSqlParameter(" || ", VALUE_PLACEHOLDER, false)));
    }

    @Override
    public ProcessorSqlFunction getSubStringPattern() {
      return new ProcessorSqlFunction("SUBSTR", Arrays.asList(
          new ProcessorSqlParameter(VALUE_PLACEHOLDER, false),
          new ProcessorSqlParameter(COMMA_SEPARATOR, START_PLACEHOLDER, false),
          new ProcessorSqlParameter(COMMA_SEPARATOR, LENGTH_PLACEHOLDER, true)));
    }

    @Override
    public String getMaxResultsPattern() {
      return "FETCH NEXT #VALUE# ROWS ONLY";
    }

    @Override
    public String getFirstResultPattern() {
      return "OFFSET #VALUE# ROWS";
    }

    @Override
    public boolean maxResultsFirst() {
      return false;
    }
  }
}
