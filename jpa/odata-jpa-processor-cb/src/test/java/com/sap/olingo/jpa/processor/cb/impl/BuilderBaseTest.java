package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

abstract class BuilderBaseTest {
  protected static final String PUNIT_NAME = "com.sap.olingo.jpa";
  protected static final String[] enumPackages = { "com.sap.olingo.jpa.processor.core.testmodel" };
  protected static EntityManagerFactory emf;
  protected static JPAServiceDocument sd;
  protected static JPAEdmProvider edmProvider;
  protected static JPAEdmNameBuilder nameBuilder;
  protected static DataSource ds;

  @BeforeAll
  public static void classSetup() throws ODataException {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
    edmProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, enumPackages);
    sd = spy(edmProvider.getServiceDocument());
    sd.getEdmEntityContainer();
  }

  protected void testNotImplemented(final Method m, final Object cut) throws IllegalAccessException {
    try {
      invokeMethod(m, cut);
    } catch (final InvocationTargetException e) {
      assertTrue(e.getCause() instanceof NotImplementedException);
      return;
    }
    fail();
  }

  protected Object invokeMethod(final Method m, final Object cut) throws IllegalAccessException,
      InvocationTargetException {
    if (m.getParameterCount() >= 1) {
      final Class<?>[] params = m.getParameterTypes();
      final List<Object> paramValues = new ArrayList<>(m.getParameterCount());
      for (int i = 0; i < m.getParameterCount(); i++) {
        if (params[i] == char.class)
          paramValues.add(' ');
        else
          paramValues.add(null);
      }
      return m.invoke(cut, paramValues.toArray());
    } else {
      return m.invoke(cut);
    }
  }

  protected Object invokeMethod(final Method m, final Object cut, final Object... paramValues)
      throws IllegalAccessException,
      InvocationTargetException {
    if (m.getParameterCount() >= 1) {
      return m.invoke(cut, paramValues);
    } else {
      return m.invoke(cut);
    }
  }
}
