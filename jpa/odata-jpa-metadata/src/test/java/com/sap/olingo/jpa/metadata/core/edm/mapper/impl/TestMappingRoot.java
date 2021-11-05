package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

import org.junit.jupiter.api.BeforeAll;

import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

public class TestMappingRoot {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  protected static final String ERROR_PUNIT = "error";
  protected static EntityManagerFactory emf;
  protected static EntityManagerFactory errorEmf;
  protected static JPADefaultEdmNameBuilder nameBuilder;
  protected static JPADefaultEdmNameBuilder errorNameBuilder;
  protected static Set<EntityType<?>> etList;
  public static final String BUPA_CANONICAL_NAME = "com.sap.olingo.jpa.processor.core.testmodel.BusinessPartner";
  public static final String ORG_CANONICAL_NAME = "com.sap.olingo.jpa.processor.core.testmodel.Organization";
  public static final String ADDR_CANONICAL_NAME = "com.sap.olingo.jpa.processor.core.testmodel.PostalAddressData";
  public static final String COMM_CANONICAL_NAME = "com.sap.olingo.jpa.processor.core.testmodel.CommunicationData";
  public static final String ADMIN_CANONICAL_NAME =
      "com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision";

  @BeforeAll
  public static void setupClass() {
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HSQLDB));
    errorEmf = JPAEntityManagerFactory.getEntityManagerFactory(ERROR_PUNIT, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HSQLDB));
    nameBuilder = new JPADefaultEdmNameBuilder(PUNIT_NAME);
    errorNameBuilder = new JPADefaultEdmNameBuilder(ERROR_PUNIT);
    etList = emf.getMetamodel().getEntities();
  }

  @SuppressWarnings("unchecked")
  <T> EntityType<T> getEntityType(final Class<T> type) {
    for (final EntityType<?> entityType : etList) {
      if (entityType.getJavaType().equals(type)) {
        return (EntityType<T>) entityType;
      }
    }
    return null;
  }
}
