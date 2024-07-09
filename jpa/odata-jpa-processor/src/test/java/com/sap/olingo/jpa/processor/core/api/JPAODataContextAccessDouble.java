package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.core.database.JPAAbstractDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseProcessorFactory;

public class JPAODataContextAccessDouble implements JPAODataSessionContextAccess {
  private final JPAEdmProvider edmProvider;
  private final DataSource dataSource;
  private final JPAAbstractDatabaseProcessor processor;
  private final String[] packageNames;
  private final JPAODataPagingProvider pagingProvider;
  private final AnnotationProvider annotationProvider;
  private JPAODataQueryDirectives directives;
  private final ProcessorSqlPatternProvider sqlPatternProvider;

  public JPAODataContextAccessDouble(final JPAEdmProvider edmProvider, final DataSource dataSource,
      final JPAODataPagingProvider provider, final AnnotationProvider annotationProvider,
      final ProcessorSqlPatternProvider sqlPatternProvider, final String... packages) {
    super();
    this.edmProvider = edmProvider;
    this.dataSource = dataSource;
    this.processor = new JPADefaultDatabaseProcessor();
    this.packageNames = packages;
    this.pagingProvider = provider != null ? provider : new JPADefaultPagingProvider();
    this.annotationProvider = annotationProvider;
    this.sqlPatternProvider = sqlPatternProvider;
    try {
      this.directives = JPAODataServiceContext.with().useQueryDirectives().maxValuesInInClause(3).build().build()
          .getQueryDirectives();
    } catch (final ODataException e) {
      this.directives = null;
    }
  }

  @Override
  public List<EdmxReference> getReferences() {
    fail();
    return null;
  }

  @Override
  public JPAODataDatabaseOperations getOperationConverter() {
    return processor instanceof JPAODataDatabaseOperations
        ? (JPAODataDatabaseOperations) processor
        : new JPADefaultDatabaseProcessor();
  }

  @Override
  public JPAEdmProvider getEdmProvider() {
    return edmProvider;
  }

  @Override
  public JPAODataDatabaseProcessor getDatabaseProcessor() {
    try {
      return new JPAODataDatabaseProcessorFactory().create(dataSource);
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

  @Override
  public JPAODataQueryDirectives getQueryDirectives() {
    return directives;
  }

  @Override
  public ProcessorSqlPatternProvider getSqlPatternProvider() {
    return sqlPatternProvider;
  }
}
