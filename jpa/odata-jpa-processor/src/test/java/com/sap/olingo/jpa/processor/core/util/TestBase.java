package com.sap.olingo.jpa.processor.core.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

public class TestBase {

  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  public static final String[] enumPackages = { "com.sap.olingo.jpa.processor.core.testmodel" };
  protected static EntityManagerFactory emf;
  protected TestHelper helper;
  protected Map<String, List<String>> headers;
  protected static JPAEdmNameBuilder nameBuilder;
  protected static DataSource dataSource;
  protected HashMap<String, From<?, ?>> joinTables;

  @BeforeAll
  public static void setupClass() {
    dataSource = DataSourceHelper.createDataSource(DataSourceHelper.DB_H2);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, dataSource);
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
  }
  
  @AfterAll
  public static void teardownClass() throws SQLException {
    emf.close();
    dataSource.getConnection().close();
  }

  protected void createHeaders() {
    headers = new HashMap<>();
    final List<String> languageHeaders = new ArrayList<>();
    languageHeaders.add("de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4");
    headers.put("accept-language", languageHeaders);
  }

  protected void addHeader(final String header, final String value) {
    final List<String> newHeader = new ArrayList<>();
    newHeader.add(value);
    headers.put(header, newHeader);
  }

  protected TestHelper getHelper() throws ODataException {
    if (helper == null)
      helper = new TestHelper(emf, PUNIT_NAME);
    return helper;
  }

  protected void fillJoinTable(final Root<?> joinRoot) {
    Join<?, ?> join = joinRoot.join("locationName", JoinType.LEFT);
    joinTables.put("LocationName", join);
    join = joinRoot.join("address", JoinType.LEFT);
    join = join.join("countryName", JoinType.LEFT);
    joinTables.put("Address/CountryName", join);
    join = joinRoot.join("address", JoinType.LEFT);
    join = join.join("regionName", JoinType.LEFT);
    joinTables.put("Address/RegionName", join);
  }
}