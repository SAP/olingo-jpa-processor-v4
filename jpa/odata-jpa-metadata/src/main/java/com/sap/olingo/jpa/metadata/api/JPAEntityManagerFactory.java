package com.sap.olingo.jpa.metadata.api;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAEntityManagerFactory {
  private static final String ENTITY_MANAGER_DATA_SOURCE = "jakarta.persistence.nonJtaDataSource";
  private static Map<String, Map<Integer, EntityManagerFactory>> emfMap;

  private JPAEntityManagerFactory() {
    throw new IllegalStateException("JPAEntityManagerFactory class");
  }

  public static EntityManagerFactory getEntityManagerFactory(final String pUnit, final Map<String, Object> ds) {
    if (pUnit == null) {
      return null;
    }
    if (emfMap == null) {
      emfMap = new HashMap<>();
    }
    final Integer dsKey = ds.hashCode();
    if (emfMap.containsKey(pUnit)) {
      final Map<Integer, EntityManagerFactory> dsMap = emfMap.get(pUnit);
      EntityManagerFactory emf = dsMap.get(dsKey);

      if (emf != null) {
        return emf; 
      }
      emf = Persistence.createEntityManagerFactory(pUnit, ds);
      dsMap.put(dsKey, emf);
      return emf;

    } else {
      final Map<Integer, EntityManagerFactory> dsMap = new HashMap<>();
      emfMap.put(pUnit, dsMap);
      final EntityManagerFactory emf = Persistence.createEntityManagerFactory(pUnit, ds);
      dsMap.put(dsKey, emf);
      return emf;
    }
  }

  public static EntityManagerFactory getEntityManagerFactory(final String pUnit, final DataSource ds) {
    final Map<String, Object> properties = new HashMap<>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, ds);
    return getEntityManagerFactory(pUnit, properties);
  }
}
