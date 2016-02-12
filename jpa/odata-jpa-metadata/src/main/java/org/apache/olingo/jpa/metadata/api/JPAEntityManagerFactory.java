package org.apache.olingo.jpa.metadata.api;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

public class JPAEntityManagerFactory {
  private static final String ENTITY_MANAGER_DATA_SOURCE = "javax.persistence.nonJtaDataSource";
  private static HashMap<String, EntityManagerFactory> emfMap;

  public static EntityManagerFactory getEntityManagerFactory(final String pUnit, final DataSource ds) {
    if (pUnit == null) {
      return null;
    }
    if (emfMap == null) {
      emfMap = new HashMap<String, EntityManagerFactory>();
    }

    if (emfMap.containsKey(pUnit)) {
      return emfMap.get(pUnit);
    } else {
      // TODO check if connection to multiple db is necessary;
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put(ENTITY_MANAGER_DATA_SOURCE, ds);
      EntityManagerFactory emf = Persistence.createEntityManagerFactory(pUnit, properties);
      emfMap.put(pUnit, emf);
      return emf;
    }

  }
}
