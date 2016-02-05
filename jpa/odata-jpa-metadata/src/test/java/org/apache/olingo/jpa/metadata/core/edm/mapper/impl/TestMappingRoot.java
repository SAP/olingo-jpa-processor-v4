package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import javax.persistence.EntityManagerFactory;

import org.apache.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import org.apache.olingo.jpa.processor.core.testmodel.DataSourceHelper;
import org.junit.BeforeClass;

public class TestMappingRoot {
  protected static final String PUNIT_NAME = "org.apache.olingo.jpa";
  protected static EntityManagerFactory emf;
  public static final String BuPa_CANONICAL_NAME = "org.apache.olingo.jpa.processor.core.testmodel.BusinessPartner";
  public static final String Addr_CANONICAL_NAME = "org.apache.olingo.jpa.processor.core.testmodel.PostalAddressData";
  public static final String Comm_CANONICAL_NAME = "org.apache.olingo.jpa.processor.core.testmodel.CommunicationData";

  @BeforeClass
  public static void setupClass() {
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_H2));
  }
}