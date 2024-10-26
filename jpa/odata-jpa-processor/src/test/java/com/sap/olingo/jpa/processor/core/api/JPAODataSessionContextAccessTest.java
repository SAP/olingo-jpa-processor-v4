package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  void testDefaultGetErrorProcessor() {
    assertNull(cut.getErrorProcessor());
  }

  @Test
  void testDefaultGetVersion() {
    assertNull(cut.getApiVersion("Test"));
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
    public JPAODataDatabaseOperations getOperationConverter() {
      return null;
    }

    @Override
    public List<EdmxReference> getReferences() {
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

    @Override
    public JPAODataApiVersionAccess getApiVersion(final String id) {
      return null;
    }
  }
}
