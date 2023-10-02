package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

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

  protected void testNotImplemented(final Method method, final Object cut) throws IllegalAccessException {
    try {
      invokeMethod(method, cut);
    } catch (final InvocationTargetException e) {
      assertTrue(e.getCause() instanceof NotImplementedException);
      return;
    }
    fail();
  }

  protected Object invokeMethod(final Method method, final Object cut) throws IllegalAccessException,
      InvocationTargetException {
    if (method.getParameterCount() >= 1) {
      final Class<?>[] params = method.getParameterTypes();
      final List<Object> paramValues = new ArrayList<>(method.getParameterCount());
      for (int i = 0; i < method.getParameterCount(); i++) {
        if (params[i] == char.class)
          paramValues.add(' ');
        else
          paramValues.add(null);
      }
      return method.invoke(cut, paramValues.toArray());
    } else {
      return method.invoke(cut);
    }
  }

  protected Object invokeMethod(final Method method, final Object cut, final Object... paramValues)
      throws IllegalAccessException,
      InvocationTargetException {
    if (method.getParameterCount() >= 1) {
      return method.invoke(cut, paramValues);
    } else {
      return method.invoke(cut);
    }
  }
}
