package com.sap.olingo.jpa.metadata.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList.IntermediateReferenceAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.CustomJPANameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPADefaultEdmNameBuilder;
import com.sap.olingo.jpa.processor.core.testmodel.DataSourceHelper;

class TestJPAEdmProvider {
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";
  private static final String[] enumPackages = { "com.sap.olingo.jpa.processor.core.testmodel" };
  private static EntityManagerFactory emf;
  private static DataSource ds;
  private JPAEdmProvider cut;
  private JPAEdmMetadataPostProcessor pp;

  @BeforeAll
  public static void setupClass() throws ODataJPAModelException {
    ds = DataSourceHelper.createDataSource(DataSourceHelper.DB_H2);
    emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME, ds);
  }

  @BeforeEach
  void setup() throws ODataException {
    cut = new JPAEdmProvider(PUNIT_NAME, emf, null, enumPackages);
  }

  @Test
  void checkReturnsDefaultNamerBuilderIfNotProvided() throws ODataException {
    assertTrue(cut.getEdmNameBuilder() instanceof JPADefaultEdmNameBuilder);
  }

  @Test
  void checkReturnsReferencesFromServiceDocument() throws ODataException {
    assertEquals(cut.getServiceDocument().getReferences(), cut.getReferences());
  }

  @Test
  void checkThrowsExceptionOnMissingNamespace() throws ODataException {
    assertThrows(NullPointerException.class, () -> new JPAEdmProvider(null, emf, null, enumPackages));
  }

  @Test
  void checkThrowsExceptionOnMissingEmf() throws ODataException {
    final EntityManagerFactory nullFactory = null;
    assertThrows(NullPointerException.class, () -> new JPAEdmProvider("Willi", nullFactory, null, enumPackages));
  }

  @Test
  void checkGetSchemas() throws ODataException {
    final JPAEdmNameBuilder nameBuilder = new CustomJPANameBuilder();
    cut = new JPAEdmProvider(emf.getMetamodel(), null, null, nameBuilder);
    final JPAServiceDocument act = cut.getServiceDocument();
    assertNotNull(act);
    assertEquals(nameBuilder, act.getNameBuilder());
  }

  @Test
  void checkGetSchemasReturnsOneSchema() throws ODataException {
    final List<CsdlSchema> act = cut.getSchemas();
    assertEquals(1, act.size());
    assertNotNull(act.get(0));
    assertEquals(PUNIT_NAME, act.get(0).getNamespace());
  }

  @Test
  void checkGetEnumReturnsNullOnUnknown() throws ODataException {
    final CsdlEnumType act = cut.getEnumType(new FullQualifiedName("Hello", "World"));
    assertNull(act);
  }

  @Test
  void checkGetEnumReturnsKnownEnum() throws ODataException {
    final CsdlEnumType act = cut.getEnumType(new FullQualifiedName(PUNIT_NAME, "AccessRights"));
    assertNotNull(act);
    assertTrue(act.isFlags());
  }

  @Test
  void checkGetComplexTypeReturnsNullOnUnknown() throws ODataException {
    final CsdlComplexType act = cut.getComplexType(new FullQualifiedName("Hello", "World"));
    assertNull(act);
  }

  @Test
  void checkGetComplexTypeReturnsKnownEnum() throws ODataException {
    final CsdlComplexType act = cut.getComplexType(new FullQualifiedName(PUNIT_NAME, "PostalAddressData"));
    assertNotNull(act);
    assertFalse(act.isAbstract());
  }

  @Test
  void checkGeEntityContainerReturnsContainer() throws ODataException {
    final CsdlEntityContainer act = cut.getEntityContainer();
    assertNotNull(act);
    assertEquals(cut.getServiceDocument().getNameBuilder().buildContainerName(), act.getName());
  }

  @Test
  void checkGetEntityContainerInfoReturnsNullOnUnknown() throws ODataException {
    final CsdlEntityContainerInfo act = cut.getEntityContainerInfo(new FullQualifiedName("Hello", "World"));
    assertNull(act);
  }

  @Test
  void checkEntityContainerInfoReturnsKnownContainer() throws ODataException {
    final String name = cut.getServiceDocument().getNameBuilder().buildContainerName();
    final FullQualifiedName fqn = new FullQualifiedName(PUNIT_NAME, name);
    final CsdlEntityContainerInfo act = cut.getEntityContainerInfo(new FullQualifiedName(PUNIT_NAME, name));
    assertNotNull(act);
    assertEquals(fqn, act.getContainerName());
  }

  @Test
  void checkEntityContainerInfoReturnsContainerIfNull() throws ODataException {
    final FullQualifiedName fqn = buildContainerFQN();
    final CsdlEntityContainerInfo act = cut.getEntityContainerInfo(null);
    assertNotNull(act);
    assertEquals(fqn, act.getContainerName());
  }

  @Test
  void checkGetEntitySetReturnsNullOnUnknownSet() throws ODataException {
    final FullQualifiedName fqn = buildContainerFQN();
    final CsdlEntitySet act = cut.getEntitySet(fqn, "Hello");
    assertNull(act);
  }

  @Test
  void checkGetEntitySetReturnsNullOnUnknownNamespace() throws ODataException {
    final FullQualifiedName fqn = new FullQualifiedName(PUNIT_NAME, "Hello");
    final CsdlEntitySet act = cut.getEntitySet(fqn, "World");
    assertNull(act);
  }

  @Test
  void checkGetEntitySetReturnsKnownSet() throws ODataException {
    final FullQualifiedName fqn = buildContainerFQN();
    final CsdlEntitySet act = cut.getEntitySet(fqn, "Persons");
    assertNotNull(act);
    assertEquals("Persons", act.getName());
  }

  @Test
  void checkGetEntityTypeReturnsNullOnUnknown() throws ODataException {
    final CsdlEntityType act = cut.getEntityType(new FullQualifiedName("Hello", "World"));
    assertNull(act);
  }

  @Test
  void checkGetEntityTypeReturnsKnownEnum() throws ODataException {
    final CsdlEntityType act = cut.getEntityType(new FullQualifiedName(PUNIT_NAME, "BusinessPartner"));
    assertNotNull(act);
    assertTrue(act.isAbstract());
  }

  @Test
  void checkGetFunctionImportReturnsNullOnUnknownContainer() throws ODataException {
    final CsdlFunctionImport act = cut.getFunctionImport(new FullQualifiedName("Hello", "World"), "Hello");
    assertNull(act);
  }

  @Test
  void checkGetFunctionImportReturnsNullOnUnknownFunction() throws ODataException {
    final FullQualifiedName fqn = buildContainerFQN();
    final CsdlFunctionImport act = cut.getFunctionImport(fqn, "Hello");
    assertNull(act);
  }

  @Test
  void checkGetFunctionImportReturnsKnownImport() throws ODataException {
    final FullQualifiedName fqn = buildContainerFQN();
    final CsdlFunctionImport act = cut.getFunctionImport(fqn, "Siblings");
    assertNotNull(act);
  }

  @Test
  void checkGetFunctionsReturnsNullOnUnknownschema() throws ODataException {
    final List<CsdlFunction> act = cut.getFunctions(new FullQualifiedName("Hallo", "Welt"));
    assertNull(act);
  }

  @Test
  void checkGetFunctionsReturnsKnownFunction() throws ODataException {
    final List<CsdlFunction> act = cut.getFunctions(new FullQualifiedName(PUNIT_NAME, "PopulationDensity"));
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals(2, act.get(0).getParameters().size());
  }

  @Test
  void checkGetAnnotationsGroupReturnsNull() throws ODataException {
    final CsdlAnnotations act = cut.getAnnotationsGroup(new FullQualifiedName(PUNIT_NAME, "Hello"), "World");
    assertNull(act);
  }

  @Test
  void checkGetTermReturnsNullOnUnknown() throws ODataException {
    final CsdlTerm act = cut.getTerm(new FullQualifiedName("Hello", "World"));
    assertNull(act);
  }

  @Test
  void checkGetTermReturnsKnownTerm() throws ODataException {
    pp = new PostProcessor();
    cut = new JPAEdmProvider(PUNIT_NAME, emf, pp, enumPackages);
    final CsdlTerm act = cut.getTerm(new FullQualifiedName("Org.OData.Measures.V1", "ISOCurrency"));
    assertNotNull(act);
  }

  @Test
  void checkTypeDefinitionReturnsNullOnUnknown() throws ODataException {
    final CsdlTypeDefinition act = cut.getTypeDefinition(new FullQualifiedName("Hello", "World"));
    assertNull(act);
  }

  @Test
  void checkGetActionsReturnsNullOnUnknown() throws ODataException {
    final List<CsdlAction> act = cut.getActions(new FullQualifiedName("Hello", "World"));
    assertNull(act);
  }

  @Test
  void checkGetActionsReturnsKnownAction() throws ODataException {
    final String[] operationPackages = { "com.sap.olingo.jpa.metadata.core.edm.mapper.testaction",
        "com.sap.olingo.jpa.processor.core.testmodel" };
    cut = new JPAEdmProvider(PUNIT_NAME, emf, null, operationPackages);
    final List<CsdlAction> act = cut.getActions(new FullQualifiedName(PUNIT_NAME, "BoundNoImport"));
    assertNotNull(act);
    assertEquals(1, act.size());
  }

  @Test
  void checkGetActionImportReturnsNullOnUnknownContainer() throws ODataException {
    final CsdlActionImport act = cut.getActionImport(new FullQualifiedName("Hello", "World"), "Dummy");
    assertNull(act);
  }

  @Test
  void checkGetActionImportReturnsNullOnUnknownAction() throws ODataException {
    final FullQualifiedName fqn = buildContainerFQN();
    final CsdlActionImport act = cut.getActionImport(fqn, "Dummy");
    assertNull(act);
  }

  @Test
  void checkGetActionImportReturnsKnownAction() throws ODataException {
    final FullQualifiedName fqn = buildContainerFQN();
    final String[] operationPackages = { "com.sap.olingo.jpa.metadata.core.edm.mapper.testaction",
        "com.sap.olingo.jpa.processor.core.testmodel" };
    cut = new JPAEdmProvider(PUNIT_NAME, emf, null, operationPackages);
    final CsdlActionImport act = cut.getActionImport(fqn, "WithImport");
    assertNotNull(act);
  }

  private FullQualifiedName buildContainerFQN() {
    final String name = cut.getServiceDocument().getNameBuilder().buildContainerName();
    final FullQualifiedName fqn = new FullQualifiedName(PUNIT_NAME, name);
    return fqn;
  }

  private class PostProcessor extends JPAEdmMetadataPostProcessor {
    @Override
    public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
        final String jpaManagedTypeClassName) {}

    @Override
    public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {}

    @Override
    public void processEntityType(final IntermediateEntityTypeAccess entity) {}

    @Override
    public void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {
      final String uri = "http://docs.oasisopen.org/odata/odata/v4.0/os/vocabularies/Org.OData.Measures.V1.xml";
      final IntermediateReferenceAccess reference = references.addReference(uri,
          "annotations/Org.OData.Measures.V1.xml");
      reference.addInclude("Org.OData.Core.V1", "Core");
    }
  }
}
