package com.sap.olingo.jpa.processor.core.api;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.processor.ErrorProcessor;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataQueryDirectives.JPAODataQueryDirectivesImpl;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseProcessorFactory;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

public final class JPAODataServiceContext implements JPAODataSessionContextAccess {
  /**
   *
   */
  private static final Log LOGGER = LogFactory.getLog(JPAODataServiceContext.class);
  private List<EdmxReference> references = new ArrayList<>();
  private final JPAODataDatabaseOperations operationConverter;
  private JPAEdmProvider jpaEdm;
  private final JPAODataDatabaseProcessor databaseProcessor;
  private final JPAEdmMetadataPostProcessor postProcessor;
  private final String[] packageName;
  private final ErrorProcessor errorProcessor;
  private final JPAODataPagingProvider pagingProvider;
  private final Optional<? extends EntityManagerFactory> emf;
  private final String namespace;
  private final String mappingPath;
  private final JPAODataBatchProcessorFactory<JPAODataBatchProcessor> batchProcessorFactory;
  private final boolean useAbsoluteContextURL;
  private final List<AnnotationProvider> annotationProvider;
  private final JPAODataQueryDirectives queryDirectives;

  public static JPAODataServiceContextBuilder with() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  private JPAODataServiceContext(final Builder builder) {

    operationConverter = builder.operationConverter;
    databaseProcessor = builder.databaseProcessor;
    references = builder.references;
    postProcessor = builder.postProcessor;
    packageName = builder.packageName;
    errorProcessor = builder.errorProcessor;
    pagingProvider = builder.pagingProvider;
    jpaEdm = builder.jpaEdm;
    emf = builder.emf;
    namespace = builder.namespace;
    mappingPath = builder.mappingPath;
    batchProcessorFactory = (JPAODataBatchProcessorFactory<JPAODataBatchProcessor>) builder.batchProcessorFactory;
    useAbsoluteContextURL = builder.useAbsoluteContextURL;
    annotationProvider = Arrays.asList(builder.annotationProvider);
    queryDirectives = builder.queryDirectives;
  }

  @Override
  public JPAODataDatabaseProcessor getDatabaseProcessor() {
    return databaseProcessor;
  }

  @Override
  public JPAEdmProvider getEdmProvider() throws ODataException {
    return jpaEdm;
  }

  public JPAEdmProvider getEdmProvider(@Nonnull final EntityManager em) throws ODataException {
    if (jpaEdm == null) {
      Objects.nonNull(em);
      jpaEdm = new JPAEdmProvider(this.namespace, em.getMetamodel(), postProcessor, packageName, annotationProvider);
    }
    return jpaEdm;
  }

  @Override
  public Optional<? extends EntityManagerFactory> getEntityManagerFactory() {
    return emf;
  }

  @Override
  public ErrorProcessor getErrorProcessor() {
    return this.errorProcessor == null ? new JPADefaultErrorProcessor() : this.errorProcessor;
  }

  @Override
  public JPAODataDatabaseOperations getOperationConverter() {
    return operationConverter;
  }

  @Override
  public List<String> getPackageName() {
    return Arrays.asList(packageName);
  }

  @Override
  public JPAODataPagingProvider getPagingProvider() {
    return pagingProvider;
  }

  @Override
  public List<EdmxReference> getReferences() {
    return references;
  }

  @Override
  public String getMappingPath() {
    return mappingPath;
  }

  @Override
  public boolean useAbsoluteContextURL() {
    return useAbsoluteContextURL;
  }

  @Override
  public JPAODataBatchProcessorFactory<JPAODataBatchProcessor> getBatchProcessorFactory() {
    return batchProcessorFactory;
  }

  @Override
  public List<AnnotationProvider> getAnnotationProvider() {
    return annotationProvider;
  }

  @Override
  public JPAODataQueryDirectives getQueryDirectives() {
    return queryDirectives;
  }

  static class Builder implements JPAODataServiceContextBuilder {

    private String namespace;
    private List<EdmxReference> references = new ArrayList<>();
    private JPAODataDatabaseOperations operationConverter = new JPADefaultDatabaseProcessor();
    private JPAODataDatabaseProcessor databaseProcessor;
    private JPAEdmMetadataPostProcessor postProcessor;
    private String[] packageName;
    private ErrorProcessor errorProcessor;
    private JPAODataPagingProvider pagingProvider;
    private Optional<? extends EntityManagerFactory> emf = Optional.empty();
    private DataSource dataSource;
    private JPAEdmProvider jpaEdm;
    private JPAEdmNameBuilder nameBuilder;
    private String mappingPath;
    private JPAODataBatchProcessorFactory<?> batchProcessorFactory;
    private boolean useAbsoluteContextURL = false;
    private AnnotationProvider[] annotationProvider;
    private JPAODataQueryDirectivesImpl queryDirectives;

    private Builder() {
      super();
    }

    @Override
    public JPAODataSessionContextAccess build() throws ODataException {
      try {
        if (nameBuilder == null) {
          LOGGER.trace("No name-builder provided, use JPADefaultEdmNameBuilder");
          nameBuilder = new JPADefaultEdmNameBuilder(namespace);
        }
        if (annotationProvider == null || annotationProvider.length == 0) {
          LOGGER.trace("No annotation provider provided, use default factory to create one");
          annotationProvider = new AnnotationProvider[] {};
        }
        if (packageName == null)
          packageName = new String[0];
        if (!emf.isPresent() && dataSource != null && namespace != null)
          emf = Optional.ofNullable(JPAEntityManagerFactory.getEntityManagerFactory(namespace, dataSource));
        createEmfWrapper();
        if (emf.isPresent() && jpaEdm == null)
          jpaEdm = new JPAEdmProvider(emf.get().getMetamodel(), postProcessor, packageName, nameBuilder, Arrays.asList(
              annotationProvider));
        if (databaseProcessor == null) {
          LOGGER.trace("No database-processor provided, use JPAODataDatabaseProcessorFactory to create one");
          databaseProcessor = new JPAODataDatabaseProcessorFactory().create(dataSource);
        }
        if (batchProcessorFactory == null) {
          LOGGER.trace("No batch-processor-factory provided, use default factory to create one");
          batchProcessorFactory = new JPADefaultBatchProcessorFactory();
        }
        if (pagingProvider == null)
          pagingProvider = new JPADefaultPagingProvider();
        if (queryDirectives == null)
          useQueryDirectives().build();
      } catch (SQLException | PersistenceException e) {
        throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
      return new JPAODataServiceContext(this);
    }

    /**
     * A database processor allows database specific implementations for search and odata function with function import
     * that are implemented as database functions.<br>
     * In case no database processor is provided and non could be determined via an data source
     * {@link JPADefaultDatabaseProcessor} is used.
     * @param databaseProcessor
     * @return
     */
    @Override
    public JPAODataServiceContextBuilder setDatabaseProcessor(final JPAODataDatabaseProcessor databaseProcessor) {
      this.databaseProcessor = databaseProcessor;
      return this;
    }

    /**
     * The data source is used to create an entity manager factory if not provided, see
     * {@link Builder#setEntityManagerFactory(EntityManagerFactory)}, and to determine the type of
     * database used to select an integrated database processor, in case the database processor was not set via
     * {@link Builder#setDatabaseProcessor(JPAODataDatabaseProcessor)}}.
     * @param dataSource
     * @return
     */
    @Override
    public JPAODataServiceContextBuilder setDataSource(final DataSource dataSource) {
      this.dataSource = dataSource;
      return this;
    }

    /**
     * Allows to provide an Olingo error processor. The error processor allows to enrich an error response. See
     * <a
     * href=
     * "http://docs.oasis-open.org/odata/odata-json-format/v4.0/errata03/os/odata-json-format-v4.0-errata03-os-complete.html#_Toc453766668"
     * >JSON Error Response</a> or
     * <a
     * href=
     * "http://docs.oasis-open.org/odata/odata-atom-format/v4.0/cs02/odata-atom-format-v4.0-cs02.html#_Toc372792829">Atom
     * Error Response</a>.
     * @param errorProcessor
     */
    @Override
    public JPAODataServiceContextBuilder setErrorProcessor(final ErrorProcessor errorProcessor) {
      this.errorProcessor = errorProcessor;
      return this;
    }

    /**
     *
     * @param postProcessor
     * @return
     */
    @Override
    public JPAODataServiceContextBuilder setMetadataPostProcessor(final JPAEdmMetadataPostProcessor postProcessor) {
      this.postProcessor = postProcessor;
      return this;
    }

    /**
     *
     * @param jpaOperationConverter
     * @return
     */
    @Override
    public JPAODataServiceContextBuilder setOperationConverter(final JPAODataDatabaseOperations jpaOperationConverter) {
      this.operationConverter = jpaOperationConverter;
      return this;
    }

    /**
     * Register a provider that is able to decides based on a given query if the server like to return only a sub set of
     * the requested results as well as a $skiptoken.
     * @param provider
     */
    @Override
    public JPAODataServiceContextBuilder setPagingProvider(final JPAODataPagingProvider provider) {
      this.pagingProvider = provider;
      return this;
    }

    /**
     * The name of the persistence-unit to be used. It is taken to create a entity manager factory
     * ({@link Builder#setEntityManagerFactory(EntityManagerFactory)}), if not provided and
     * as namespace of the OData service, in case the default name builder shall be used.
     * @param pUnit
     * @return
     */
    @Override
    public JPAODataServiceContextBuilder setPUnit(final String pUnit) {
      this.namespace = pUnit;
      return this;
    }

    /**
     *
     * @param references
     * @return
     */
    @Override
    public JPAODataServiceContextBuilder setReferences(final List<EdmxReference> references) {
      this.references = references;
      return this;
    }

    /**
     * Name of the top level package to look for
     * <ul>
     * <li>Enumeration Types
     * <li>Java class based Functions
     * </ul>
     * @param packageName
     */
    @Override
    public JPAODataServiceContextBuilder setTypePackage(final String... packageName) {
      this.packageName = packageName;
      return this;
    }

    @Override
    public JPAODataServiceContextBuilder setRequestMappingPath(final String mappingPath) {
      this.mappingPath = mappingPath;
      return this;
    }

    /**
     * Set an externally created entity manager factory.<br>
     * This is necessary e.g. in case a spring based service shall run without a <code>persistance.xml</code>.
     * @param emf
     * @return
     */
    @Override
    public JPAODataServiceContextBuilder setEntityManagerFactory(final EntityManagerFactory emf) {
      this.emf = Optional.of(emf);
      return this;
    }

    /**
     * Set a custom EDM name builder {@link JPAEdmNameBuilder}. If non is provided {@link JPADefaultEdmNameBuilder} is
     * used, which uses the provided persistence-unit name ({@link JPAODataServiceContext.Builder#setPUnit}) as
     * namespace.
     * @param nameBuilder
     * @return
     */
    @Override
    public JPAODataServiceContextBuilder setEdmNameBuilder(final JPAEdmNameBuilder nameBuilder) {
      this.nameBuilder = nameBuilder;
      return this;
    }

    @Override
    public <T extends JPAODataBatchProcessor> JPAODataServiceContextBuilder setBatchProcessorFactory(
        final JPAODataBatchProcessorFactory<T> batchProcessorFactory) {
      this.batchProcessorFactory = batchProcessorFactory;
      return this;
    }

    /**
     * Some clients, like Excel, require context url's with an absolute path. The default generation of relative paths
     * can be overruled.<br>
     * @see <a href="https://issues.apache.org/jira/browse/OLINGO-787">Issue OLINGO-787</a>
     * @param useAbsoluteContextURL
     * @return
     */
    @Override
    public JPAODataServiceContextBuilder setUseAbsoluteContextURL(final boolean useAbsoluteContextURL) {
      this.useAbsoluteContextURL = useAbsoluteContextURL;
      return this;
    }

    @Override
    public JPAODataServiceContextBuilder setAnnotationProvider(final AnnotationProvider... annotationProvider) {
      this.annotationProvider = annotationProvider;
      return this;
    }

    @SuppressWarnings("unchecked")
    private void createEmfWrapper() {
      if (emf.isPresent()) {
        try {
          final Class<? extends EntityManagerFactory> wrapperClass = (Class<? extends EntityManagerFactory>) Class
              .forName("com.sap.olingo.jpa.processor.cb.api.EntityManagerFactoryWrapper");
          if (jpaEdm == null)
            jpaEdm = new JPAEdmProvider(emf.get().getMetamodel(), postProcessor, packageName, nameBuilder, Arrays
                .asList(annotationProvider));
          emf = Optional.of(wrapperClass.getConstructor(EntityManagerFactory.class,
              JPAServiceDocument.class).newInstance(emf.get(), jpaEdm.getServiceDocument()));
          LOGGER.trace("Criteria Builder Extension found. It will be used");
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException e) {
          LOGGER.debug("Exception thrown while trying to create instance of emf wrapper", e);
        } catch (final ClassNotFoundException e) {
          // No Criteria Extension: everything is fine
          LOGGER.trace("No Criteria Builder Extension found: use provided Entity Manager Factory");
        } catch (final ODataException e) {
          LOGGER.debug("Exception thrown while trying to create EdmProvider", e);
        }
      }
    }

    @Override
    public JPAODataQueryDirectivesBuilder useQueryDirectives() {
      return JPAODataQueryDirectives.with(this);
    }

    public JPAODataServiceContextBuilder setQueryDirectives(final JPAODataQueryDirectivesImpl queryDirectives) {
      this.queryDirectives = queryDirectives;
      return this;
    }
  }
}