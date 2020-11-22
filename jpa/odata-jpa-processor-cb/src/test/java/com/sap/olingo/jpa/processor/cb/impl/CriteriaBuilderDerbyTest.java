package com.sap.olingo.jpa.processor.cb.impl;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

@Disabled
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
    super.setup(emf, sd);
  }
}
