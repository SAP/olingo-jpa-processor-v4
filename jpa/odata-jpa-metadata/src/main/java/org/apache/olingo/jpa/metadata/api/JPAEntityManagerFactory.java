package org.apache.olingo.jpa.metadata.api;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

public class JPAEntityManagerFactory {
  private static final String ENTITY_MANAGER_DATA_SOURCE = "javax.persistence.nonJtaDataSource";
  // private static Map<String, EntityManagerFactory> emfMap;
  private static Map<String, Map<DataSource, EntityManagerFactory>> emfMap;

  public static EntityManagerFactory getEntityManagerFactory(final String pUnit, final DataSource ds) {
    if (pUnit == null) {
      return null;
    }
    if (emfMap == null) {
      emfMap = new HashMap<String, Map<DataSource, EntityManagerFactory>>();
    }

    if (emfMap.containsKey(pUnit)) {
      Map<DataSource, EntityManagerFactory> dsMap = emfMap.get(pUnit);
      EntityManagerFactory emf = dsMap.get(ds);
      if (emf != null)
        return emf;
      return createFactory(pUnit, ds, dsMap);
    } else {

      final Map<DataSource, EntityManagerFactory> dsMap = new HashMap<DataSource, EntityManagerFactory>();
      emfMap.put(pUnit, dsMap);
      return createFactory(pUnit, ds, dsMap);
    }

  }

  private static EntityManagerFactory createFactory(final String pUnit, final DataSource ds,
      final Map<DataSource, EntityManagerFactory> dsMap) {

    final Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, ds);
    final EntityManagerFactory emf = Persistence.createEntityManagerFactory(pUnit, properties);
    dsMap.put(ds, emf);
    return emf;
  }
}
