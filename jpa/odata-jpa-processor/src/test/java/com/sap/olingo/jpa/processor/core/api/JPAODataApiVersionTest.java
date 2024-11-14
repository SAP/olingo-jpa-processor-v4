package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAApiVersion;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

class JPAODataApiVersionTest {
  private static final String VERSION_ID = "V12";
  private static final String PATH = "test/v12";
  private static DataSource dataSource;
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private JPAApiVersion version;
  private ProcessorSqlPatternProvider sqlPattern;
  private JPAODataApiVersion cut;
  private EntityManagerFactory emf;

  @BeforeAll
  public static void classSetup() {
    dataSource = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
  }

  @BeforeEach
  void setup() throws ODataException {
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, dataSource);
    sqlPattern = mock(ProcessorSqlPatternProvider.class);
    version = mock(JPAApiVersion.class);
    when(version.getId()).thenReturn(VERSION_ID);
    when(version.getRequestMappingPath()).thenReturn(PATH);
    when(version.getEntityManagerFactory()).thenReturn(emf);

    cut = new JPAODataApiVersion(version, new JPADefaultEdmNameBuilder(PUNIT_NAME), List.of(), sqlPattern);
  }

  @Test
  void testGetId() {
    assertEquals(VERSION_ID, cut.getId());
  }

  @Test
  void testGetMappingPath() {
    assertEquals(PATH, cut.getMappingPath());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testGetEntityManagerFactory() {
    try {
      final Class<? extends EntityManagerFactory> wrapperClass = (Class<? extends EntityManagerFactory>) Class
          .forName("com.sap.olingo.jpa.processor.cb.api.EntityManagerFactoryWrapper");
      if (wrapperClass != null) {
        assertTrue(cut.getEntityManagerFactory().getClass().isAssignableFrom(wrapperClass));
      } else {
        assertEquals(emf, cut.getEntityManagerFactory());
      }
    } catch (final ClassNotFoundException e) {
      assertEquals(emf, cut.getEntityManagerFactory());
    }
  }

  @Test
  void testGetEdmProvider() {
    assertNotNull(cut.getEdmProvider());
  }
}
