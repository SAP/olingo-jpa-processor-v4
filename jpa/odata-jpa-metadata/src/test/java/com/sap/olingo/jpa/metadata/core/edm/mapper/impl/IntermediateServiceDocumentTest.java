package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateServiceDocumentTest extends TestMappingRoot {

  private JPAServiceDocument cut;

  static Stream<Arguments> getEntityTypeByFqn() {
    return Stream.of(
        arguments(new FullQualifiedName("com.sap.olingo.jpa.BusinessPartner"), false),
        arguments(new FullQualifiedName("com.sap.olingo.jpa.Dummy"), true),
        arguments(new FullQualifiedName("dummy.BusinessPartner"), true));
  }

  static Stream<Arguments> getEntityTypeByEsName() {
    return Stream.of(
        arguments("BusinessPartners", false),
        arguments("Dummy", true));
  }

  static Stream<Arguments> getEnumType() {
    return Stream.of(
        arguments("com.sap.olingo.jpa.AccessRights", false),
        arguments("com.sap.olingo.jpa.Dummy", true),
        arguments("Unknown.AccessRights", true));

  }

  static Stream<Arguments> getEntityTypeBySingletonName() {
    return Stream.of(
        arguments("CurrentUser", false),
        arguments("Dummy", true));
  }

  @BeforeEach
  void setup() throws ODataJPAModelException {
    cut = new IntermediateServiceDocument(PUNIT_NAME, emf.getMetamodel(), null,
        new String[] { "com.sap.olingo.jpa.processor.core.testmodel" });
  }

  @Test
  void checkServiceDocumentCanBeCreated() throws ODataJPAModelException {
    assertNotNull(new IntermediateServiceDocument(PUNIT_NAME, emf.getMetamodel(), null,
        new String[] { "com.sap.olingo.jpa.processor.core.testmodel" }));
  }

  @Test
  void checkServiceDocumentGetSchemaList() throws ODataJPAModelException {
    assertEquals(1, cut.getEdmSchemas().size(), "Wrong number of schemas");
  }

  @Test
  void checkServiceDocumentGetContainer() throws ODataJPAModelException {
    assertNotNull(cut.getEdmEntityContainer(), "Entity Container not found");
  }

  @Test
  void checkServiceDocumentGetContainerFromSchema() throws ODataJPAModelException {

    final List<CsdlSchema> schemas = cut.getEdmSchemas();
    final CsdlSchema schema = schemas.get(0);
    assertNotNull(schema.getEntityContainer(), "Entity Container not found");
  }

  @Test
  void checkServiceDocumentGetEntitySetsFromContainer() throws ODataJPAModelException {
    final CsdlEntityContainer container = cut.getEdmEntityContainer();
    assertNotNull(container.getEntitySets(), "Entity Container not found");
  }

  @Test
  void checkHasEtagReturnsTrueOnVersion() throws ODataJPAModelException {
    final EdmBindingTarget target = mock(EdmBindingTarget.class);
    final EdmEntityType et = mock(EdmEntityType.class);
    when(target.getEntityType()).thenReturn(et);
    when(et.getFullQualifiedName()).thenReturn(new FullQualifiedName(PUNIT_NAME, "BusinessPartner"));

    assertTrue(cut.hasETag(target));
  }

  @Test
  void checkHasEtagReturnsFalseWithoutVersion() throws ODataJPAModelException {
    final EdmBindingTarget target = mock(EdmBindingTarget.class);
    final EdmEntityType et = mock(EdmEntityType.class);
    when(target.getEntityType()).thenReturn(et);
    when(et.getFullQualifiedName()).thenReturn(new FullQualifiedName(PUNIT_NAME, "Country"));

    final JPAServiceDocument svc = new IntermediateServiceDocument(PUNIT_NAME, emf.getMetamodel(), null, null);
    assertFalse(svc.hasETag(target));
  }

  @ParameterizedTest
  @MethodSource("getEnumType")
  void checkGetEnumType(final String enumName, final boolean isNull) throws ODataJPAModelException {
    if (isNull)
      assertNull(cut.getEnumType(enumName));
    else
      assertNotNull(cut.getEnumType(enumName));
  }

  @ParameterizedTest
  @MethodSource("getEntityTypeByEsName")
  void checkGetEntityTypeByEsName(final String esName, final boolean isNull) throws ODataJPAModelException {
    if (isNull)
      assertNull(cut.getEntity(esName));
    else
      assertNotNull(cut.getEntity(esName));
  }

  @ParameterizedTest
  @MethodSource("getEntityTypeBySingletonName")
  void checkGetEntityTypeBySingletonName(final String singletonName, final boolean isNull)
      throws ODataJPAModelException {
    if (isNull)
      assertNull(cut.getEntity(singletonName));
    else
      assertNotNull(cut.getEntity(singletonName));
  }

  @ParameterizedTest
  @MethodSource("getEntityTypeByFqn")
  void checkGetEntityTypeByFqn(final FullQualifiedName etFqn, final boolean isNull)
      throws ODataJPAModelException {
    if (isNull)
      assertNull(cut.getEntity(etFqn));
    else
      assertNotNull(cut.getEntity(etFqn));
  }

  @Test
  void checkGetEntityTypeByEdmType() throws ODataJPAModelException {
    final EdmEntityType type = mock(EdmEntityType.class);
    when(type.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(type.getName()).thenReturn("BusinessPartner");
    assertNotNull(cut.getEntity(type));
  }

  @Test
  void checkGetEntityTypeByEdmTypeReturnNullOnUnkown() throws ODataJPAModelException {
    final EdmEntityType type = mock(EdmEntityType.class);
    when(type.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(type.getName()).thenReturn("Unknown");
    assertNull(cut.getEntity(type));
  }

  @Test
  void checkGetEntityTypeByEdmTypeReturnNullOnUnkownSchema() throws ODataJPAModelException {
    final EdmEntityType type = mock(EdmEntityType.class);
    when(type.getNamespace()).thenReturn("Unknown");
    when(type.getName()).thenReturn("BoundNoImport");
    assertNull(cut.getEntity(type));
  }

  @Test
  void checkGetComplexTypeByEdmType() throws ODataJPAModelException {
    final EdmComplexType type = mock(EdmComplexType.class);
    when(type.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(type.getName()).thenReturn("CommunicationData");
    assertNotNull(cut.getComplexType(type));
  }

  @Test
  void checkGetComplexTypeByEdmTypeReturnNullOnUnkown() throws ODataJPAModelException {
    final EdmComplexType type = mock(EdmComplexType.class);
    when(type.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(type.getName()).thenReturn("Unknown");
    assertNull(cut.getComplexType(type));
  }

  @Test
  void checkGetComplexTypeByEdmTypeReturnNullOnUnkownSchema() throws ODataJPAModelException {
    final EdmComplexType type = mock(EdmComplexType.class);
    when(type.getNamespace()).thenReturn("Unknown");
    when(type.getName()).thenReturn("BoundNoImport");
    assertNull(cut.getComplexType(type));
  }

  @Test
  void checkGetAction() throws ODataJPAModelException {
    final EdmAction action = mock(EdmAction.class);
    when(action.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(action.getName()).thenReturn("BoundNoImport");
    final JPAServiceDocument svc = new IntermediateServiceDocument(PUNIT_NAME, emf.getMetamodel(), null,
        new String[] { "com.sap.olingo.jpa.metadata.core.edm.mapper.testaction" });
    assertNotNull(svc.getAction(action));
  }

  @Test
  void checkGetActionReturnNullOnUnknownAction() throws ODataJPAModelException {
    final EdmAction action = mock(EdmAction.class);
    when(action.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(action.getName()).thenReturn("Unknown");

    assertNull(cut.getAction(action));
  }

  @Test
  void checkGetActionReturnNullOnUnknownSchema() throws ODataJPAModelException {
    final EdmAction action = mock(EdmAction.class);
    when(action.getNamespace()).thenReturn("Unknown");
    when(action.getName()).thenReturn("BoundNoImport");

    assertNull(cut.getAction(action));
  }

  @Test
  void checkGetFunction() throws ODataJPAModelException {
    final EdmFunction function = mock(EdmFunction.class);
    when(function.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(function.getName()).thenReturn("ConvertToQkm");
    assertNotNull(cut.getFunction(function));
  }

  @Test
  void checkGetFunctionReturnNullOnUnkownFunction() throws ODataJPAModelException {
    final EdmFunction function = mock(EdmFunction.class);
    when(function.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(function.getName()).thenReturn("Unknown");
    assertNull(cut.getFunction(function));
  }

  @Test
  void checkGetFunctionReturnNullOnUnkownSchema() throws ODataJPAModelException {
    final EdmFunction function = mock(EdmFunction.class);
    when(function.getNamespace()).thenReturn("Unknown");
    when(function.getName()).thenReturn("BoundNoImport");
    assertNull(cut.getFunction(function));
  }

  @Test
  void checkHasMediaETagNotSupported() {
    final EdmBindingTarget target = mock(EdmBindingTarget.class);
    assertFalse(cut.hasMediaETag(target));
  }

  @Test
  void checkGetEntityTypeReturnsSetCustomName() throws ODataJPAModelException {
    cut = createCutWithCustomNameBuilder();
    assertNotNull(cut.getEntity("Business_Partner".toUpperCase()));
  }

  @Test
  void checkGetEntityTypeReturnsEdmTypCustomName() throws ODataJPAModelException {
    cut = createCutWithCustomNameBuilder();
    final EdmType edmType = mock(EdmType.class);
    when(edmType.getName()).thenReturn("Business_Partner");
    when(edmType.getNamespace()).thenReturn("test");

    assertNotNull(cut.getEntity(edmType));
  }

  @Test
  void checkGetPropertyReturnsCustomName() throws ODataJPAModelException {
    cut = createCutWithCustomNameBuilder();
    assertEquals("creationDateTime", cut.getEntity("Business_Partner".toUpperCase()).getAttribute("creationDateTime")
        .get().getExternalName());
  }

  @Test
  void checkGetComplexTypeReturnsCustomName() throws ODataJPAModelException {
    cut = createCutWithCustomNameBuilder();
    final EdmComplexType type = mock(EdmComplexType.class);
    when(type.getNamespace()).thenReturn("test");
    when(type.getName()).thenReturn("T_CommunicationData");
    assertNotNull(cut.getComplexType(type));
  }

  @Test
  void checkGetContainerReturnsCustomName() throws ODataJPAModelException {
    cut = createCutWithCustomNameBuilder();
    assertEquals("service_container", cut.getEdmEntityContainer().getName());
  }

  @Test
  void checkGetEnumReturnsCustomName() throws ODataJPAModelException {
    cut = createCutWithCustomNameBuilder();
    assertNotNull(cut.getEnumType("test.E_AccessRights"));
  }

  @Test
  void checkGetActionCustomName() throws ODataJPAModelException {
    cut = createCutWithCustomNameBuilder();
    final EdmAction action = mock(EdmAction.class);
    when(action.getNamespace()).thenReturn("test");
    when(action.getName()).thenReturn("O_BoundNoImport");
    assertNotNull(cut.getAction(action));
  }

  @Test
  void checkGetFunctionCustomName() throws ODataJPAModelException {
    cut = createCutWithCustomNameBuilder();
    final EdmFunction function = mock(EdmFunction.class);
    when(function.getNamespace()).thenReturn("test");
    when(function.getName()).thenReturn("O_sum");
    assertNotNull(cut.getFunction(function));
  }

  @Test
  void checkGetClaimsReturnsAllClaims() throws ODataJPAModelException {
    final Map<String, JPAProtectionInfo> act = cut.getClaims();
    assertNotNull(act);
    assertTrue(act.containsKey("BuildingNumber"));
  }

  @Test
  void checkGetEntityTypeOfSingleton() throws ODataJPAModelException {
    final FullQualifiedName fqn = new FullQualifiedName(PUNIT_NAME, "CurrentUser");
    final JPAEntityType act = cut.getEntity(fqn);
    assertNotNull(act);
  }

  private IntermediateServiceDocument createCutWithCustomNameBuilder() throws ODataJPAModelException {
    return new IntermediateServiceDocument(new CustomJPANameBuilder(), emf.getMetamodel(), null,
        new String[] { "com.sap.olingo.jpa.processor.core.testmodel",
            "com.sap.olingo.jpa.metadata.core.edm.mapper.testaction" });
  }

}
