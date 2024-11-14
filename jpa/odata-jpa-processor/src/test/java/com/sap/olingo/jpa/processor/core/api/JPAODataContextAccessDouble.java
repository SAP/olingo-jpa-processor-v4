package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import jakarta.persistence.EntityManagerFactory;

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
  private final DataSource dataSource;
  private final JPAAbstractDatabaseProcessor processor;
  private final JPAODataPagingProvider pagingProvider;
  private final AnnotationProvider annotationProvider;
  private JPAODataQueryDirectives directives;
  private final ProcessorSqlPatternProvider sqlPatternProvider;
  private final JPAODataApiVersionAccess version;

  public JPAODataContextAccessDouble(final JPAEdmProvider edmProvider, final EntityManagerFactory emf,
      final DataSource dataSource,
      final JPAODataPagingProvider provider, final AnnotationProvider annotationProvider,
      final ProcessorSqlPatternProvider sqlPatternProvider, final String... packages) {
    super();
    this.dataSource = dataSource;
    this.processor = new JPADefaultDatabaseProcessor();
    this.pagingProvider = provider != null ? provider : new JPADefaultPagingProvider();
    this.annotationProvider = annotationProvider;
    this.sqlPatternProvider = sqlPatternProvider;
    this.version = new JPAODataApiVersion(JPAODataApiVersionAccess.DEFAULT_VERSION, edmProvider, emf);
    try {
      this.directives = JPAODataServiceContext.with()
          .setEntityManagerFactory(emf)
          .setPUnit(edmProvider.getEdmNameBuilder().getNamespace())
          .useQueryDirectives()
          .maxValuesInInClause(3)
          .build()
          .build()
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
  public JPAODataDatabaseProcessor getDatabaseProcessor() {
    try {
      return new JPAODataDatabaseProcessorFactory().create(dataSource);
    } catch (final SQLException e) {
      fail();
    }
    return null;
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

  @Override
  public JPAODataApiVersionAccess getApiVersion(final String id) {
    return version;
  }
}
