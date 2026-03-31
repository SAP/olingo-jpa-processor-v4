/**
 *
 */
package com.sap.olingo.jpa.processor.test;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.junit.jupiter.api.BeforeAll;

import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

/**
 * @author Oliver Grande
 * Created: 11.11.2019
 *
 */
public class TestEqualHashCodeMethodsErrorModel extends TestEqualHashCodeMethods {
  private static final String PUNIT_NAME = "error";

  @BeforeAll
  static void setupClass() {
    final Map<String, Object> properties = new HashMap<>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, DataSourceHelper.createDataSource(
        DataSourceHelper.DB_HSQLDB));
    final EntityManagerFactory emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
    model = emf.getMetamodel();
  }

}
