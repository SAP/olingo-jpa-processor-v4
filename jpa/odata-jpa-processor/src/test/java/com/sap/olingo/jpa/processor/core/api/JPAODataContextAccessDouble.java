package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.apache.olingo.commons.api.edmx.EdmxReference;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseProcessorFactory;

public class JPAODataContextAccessDouble implements JPAODataSessionContextAccess {
  private final JPAEdmProvider edmProvider;
  private final DataSource ds;
  private final JPAODataDatabaseOperations context;
  private final String[] packageNames;
  private final JPAODataPagingProvider pagingProvider;
  private final AnnotationProvider annotationProvider;

  public JPAODataContextAccessDouble(final JPAEdmProvider edmProvider, final DataSource ds,
      final JPAODataPagingProvider provider, final AnnotationProvider annotationProvider, final String... packages) {
    super();
    this.edmProvider = edmProvider;
    this.ds = ds;
    this.context = new JPADefaultDatabaseProcessor();
    this.packageNames = packages;
    this.pagingProvider = provider;
    this.annotationProvider = annotationProvider;
  }

  @Override
  public List<EdmxReference> getReferences() {
    fail();
    return null;
  }

  @Override
  public JPAODataDatabaseOperations getOperationConverter() {
    return context;
  }

  @Override
  public JPAEdmProvider getEdmProvider() {
    return edmProvider;
  }

  @Override
  public JPAODataDatabaseProcessor getDatabaseProcessor() {
    try {
      return new JPAODataDatabaseProcessorFactory().create(ds);
    } catch (final SQLException e) {
      fail();
    }
    return null;
  }

  @Override
  public List<String> getPackageName() {
    return Arrays.asList(packageNames);
  }

  @Override
  public JPAODataPagingProvider getPagingProvider() {
    return pagingProvider;
  }

  @Override
  public List<AnnotationProvider> getAnnotationProvider() {
    return Collections.singletonList(annotationProvider);
  }
}
