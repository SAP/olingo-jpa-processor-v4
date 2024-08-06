package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;

class JPAODataSessionContextAccessTest {
  private JPAODataSessionContextAccess cut;

  @BeforeEach
  void setup() {
    cut = new JPAODataSessionContextAccessDouble();
  }

  @Test
  void testDefaultGetEntityManagerFactory() {
    final Optional<? extends EntityManagerFactory> act = cut.getEntityManagerFactory();

    assertNotNull(act);
    assertTrue(act.isEmpty());
  }

  @Test
  void testDefaultGetErrorProcessor() {
    assertNull(cut.getErrorProcessor());
  }

  @Test
  void testDefaultGetMappingPath() {
    assertTrue(cut.getMappingPath().isEmpty());
  }

  @Test
  void testDefaultGetBatchProcessorFactory() {
    assertNull(cut.getBatchProcessorFactory());
  }

  @Test
  void testDefaultUseAbsoluteContextURL() {
    assertFalse(cut.useAbsoluteContextURL());
  }

  private static class JPAODataSessionContextAccessDouble implements JPAODataSessionContextAccess {

    @Override
    public JPAODataDatabaseProcessor getDatabaseProcessor() {
      return null;
    }

    @Override
    public JPAEdmProvider getEdmProvider() throws ODataException {
      return null;
    }

    @Override
    public JPAODataDatabaseOperations getOperationConverter() {
      return null;
    }

    @Override
    public List<EdmxReference> getReferences() {
      return null;
    }

    @Override
    public List<String> getPackageName() {
      return null;
    }

    @Override
    public JPAODataPagingProvider getPagingProvider() {
      return null;
    }

    @Override
    public List<AnnotationProvider> getAnnotationProvider() {
      return null;
    }

    @Override
    public JPAODataQueryDirectives getQueryDirectives() {
      return null;
    }

    @Override
    public ProcessorSqlPatternProvider getSqlPatternProvider() {
      return null;
    }
  }
}
