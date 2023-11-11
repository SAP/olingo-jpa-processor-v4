package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.processor.ErrorProcessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.api.example.JPAExamplePagingProvider;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

class JPAODataServiceContextBuilderTest {
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String[] enumPackages = { "com.sap.olingo.jpa.processor.core.testmodel" };

  private JPAODataSessionContextAccess cut;
  private static DataSource ds;

  @BeforeAll
  public static void classSetup() {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_HSQLDB);
  }

  @Test
  void checkBuilderAvailable() {
    assertNotNull(JPAODataServiceContext.with());
  }

  @Test
  void checkSetDatasourceAndPUnit() throws ODataException {
    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .build();

    assertNotNull(cut.getEdmProvider());
  }

  @Test
  void checkEmptyArrayOnNoPackagesProvided() throws ODataException {
    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .build();

    assertNotNull(cut.getPackageName());
    assertEquals(0, cut.getPackageName().size());
  }

  @Test
  void checkArrayOnProvidedPackages() throws ODataException {
    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setTypePackage("org.apache.olingo.jpa.bl", "com.sap.olingo.jpa.processor.core.testmodel")
        .build();

    assertNotNull(cut.getPackageName());
    assertEquals(2, cut.getPackageName().size());
  }

  @Test
  void checkReturnsProvidedPagingProvider() throws ODataException {
    final JPAODataPagingProvider provider = new JPAExamplePagingProvider(Collections.emptyMap());
    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setPagingProvider(provider)
        .build();

    assertNotNull(cut.getPagingProvider());
    assertEquals(provider, cut.getPagingProvider());
  }

  @Test
  void checkEmptyListOnNoReferencesProvided() throws ODataException {

    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .build();

    assertNotNull(cut.getReferences());
    assertTrue(cut.getReferences().isEmpty());
  }

  @Test
  void checkReturnsReferencesProvider() throws ODataException, URISyntaxException {

    final List<EdmxReference> references = new ArrayList<>(1);
    references.add(new EdmxReference(new URI("http://exapmle.com/odata4/v1")));
    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setReferences(references)
        .build();

    assertEquals(1, cut.getReferences().size());
  }

  @Test
  void checkReturnsOperation() throws ODataException {

    final JPAODataDatabaseOperations operations = new JPADefaultDatabaseProcessor();
    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setOperationConverter(operations)
        .build();

    assertNotNull(cut.getOperationConverter());
    assertEquals(operations, cut.getOperationConverter());
  }

  @Test
  void checkReturnsDatabaseProcessor() throws ODataException {

    final JPAODataDatabaseProcessor processor = new JPADefaultDatabaseProcessor();
    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setDatabaseProcessor(processor)
        .build();

    assertNotNull(cut.getDatabaseProcessor());
    assertEquals(processor, cut.getDatabaseProcessor());
  }

  @Test
  void checkJPAEdmContainsPostProcessor() throws ODataException {

    final JPAEdmMetadataPostProcessor processor = new TestEdmPostProcessor();
    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setTypePackage(enumPackages)
        .setMetadataPostProcessor(processor)
        .build();

    assertNotNull(cut.getEdmProvider());
    final CsdlEntityType act = cut.getEdmProvider().getEntityType(new FullQualifiedName(PUNIT_NAME, "BusinessPartner"));
    assertEquals(1L, act.getAnnotations().stream().filter(a -> a.getTerm().equals("Permissions")).count());
  }

  @Test
  void checkReturnsErrorProcessor() throws ODataException {
    final ErrorProcessor processor = new JPADefaultErrorProcessor();
    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setErrorProcessor(processor)
        .build();

    assertNotNull(cut.getErrorProcessor());
    assertEquals(processor, cut.getErrorProcessor());
  }

  @Test
  void checkThrowsExceptionOnDBConnectionProblem() throws ODataException, SQLException {
    final DataSource dsSpy = spy(ds);
    when(dsSpy.getConnection()).thenThrow(SQLException.class);
    assertThrows(ODataException.class, () -> JPAODataServiceContext.with()
        .setDataSource(dsSpy)
        .setPUnit(PUNIT_NAME)
        .build());

  }

  @Test
  void checkJPAEdmContainsDefaultNameBuilder() throws ODataException, SQLException {

    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setTypePackage(enumPackages)
        .build();
    final JPAEdmProvider act = cut.getEdmProvider();
    assertNotNull(act);
    assertNotNull(act.getEdmNameBuilder());
    assertTrue(act.getEdmNameBuilder() instanceof JPADefaultEdmNameBuilder);
  }

  @Test
  void checkJPAEdmContainsCustomNameBuilder() throws ODataException, SQLException {

    final JPAEdmNameBuilder nameBuilder = mock(JPAEdmNameBuilder.class);
    when(nameBuilder.getNamespace()).thenReturn("unit.test");
    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setTypePackage(enumPackages)
        .setEdmNameBuilder(nameBuilder)
        .build();
    final JPAEdmProvider act = cut.getEdmProvider();
    assertNotNull(act);
    assertNotNull(act.getEdmNameBuilder());
    assertEquals(nameBuilder, act.getEdmNameBuilder());
  }

  @Test
  void checkReturnsMappingPath() throws ODataException {
    final String exp = "test/v1";

    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setRequestMappingPath(exp)
        .build();

    assertEquals(exp, cut.getMappingPath());
  }

  @Test
  void checkReturnsBatchProcessorFactory() throws ODataException {
    final TestBatchProcessorFactory exp = new TestBatchProcessorFactory();

    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setBatchProcessorFactory(exp)
        .build();

    assertEquals(exp, cut.getBatchProcessorFactory());
  }

  @Test
  void checkReturnsDefaultBatchProcessorFactoryIfNotProvided() throws ODataException {

    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .build();

    assertTrue(cut.getBatchProcessorFactory() instanceof JPADefaultBatchProcessorFactory);
  }

  @Test
  void checkReturnsFalseAsDefaultForUseAbsoluteContextURL() throws ODataException {

    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .build();

    assertFalse(cut.useAbsoluteContextURL());
  }

  @Test
  void checkReturnsTrueForUseAbsoluteContextURLIfSet() throws ODataException {

    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setUseAbsoluteContextURL(true)
        .build();

    assertTrue(cut.useAbsoluteContextURL());
  }

  @Test
  void checkReturnsFalseForUseAbsoluteContextURLIfSet() throws ODataException {

    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setUseAbsoluteContextURL(false)
        .build();

    assertFalse(cut.useAbsoluteContextURL());
  }

  @Test
  void checkReturnsEmptyAnnotationProviderList() throws ODataException {

    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setUseAbsoluteContextURL(false)
        .build();

    assertTrue(cut.getAnnotationProvider().isEmpty());
  }

  @Test
  void checkReturnAnnotationProviderList() throws ODataException {

    final AnnotationProvider provider1 = mock(AnnotationProvider.class);
    final AnnotationProvider provider2 = mock(AnnotationProvider.class);

    cut = JPAODataServiceContext.with()
        .setDataSource(ds)
        .setPUnit(PUNIT_NAME)
        .setUseAbsoluteContextURL(false)
        .setAnnotationProvider(provider1, provider2)
        .build();

    assertEquals(2, cut.getAnnotationProvider().size());
    assertTrue(cut.getAnnotationProvider().contains(provider1));
    assertTrue(cut.getAnnotationProvider().contains(provider2));
  }

  private class TestEdmPostProcessor implements JPAEdmMetadataPostProcessor {

    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {
      // Default shall do nothing
    }

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
      // Default shall do nothing
    }

    @Override
    public void provideReferences(final IntermediateReferenceList references) {
      // Default shall do nothing
    }

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {
      if (entity.getExternalName().equals("BusinessPartner")) {
        final CsdlAnnotation annotation = new CsdlAnnotation();
        annotation.setTerm("Permissions");
        annotation.setExpression(new CsdlConstantExpression(ConstantExpressionType.EnumMember, "3"));
        entity.addAnnotations(Collections.singletonList(annotation));
      }
    }
  }

  private class TestBatchProcessorFactory implements JPAODataBatchProcessorFactory<JPAODataBatchProcessor> {

    @Override
    public JPAODataBatchProcessor getBatchProcessor(@Nonnull final JPAODataSessionContextAccess serviceContext,
        @Nonnull final JPAODataRequestContextAccess requestContext) {
      return null;
    }
  }
}
