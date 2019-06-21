package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class TestSchemaReader {
  private SchemaReader cut;

  @BeforeEach
  public void setup() {
    cut = new SchemaReader();
  }

  @Test
  public void testReadXMLWithNullPath() throws ODataJPAModelException, IOException {
    final String nullString = null;
    assertNull(cut.getSchemas(nullString));
  }

  @Test
  public void testReadXMLWithEmptyPath() throws ODataJPAModelException, IOException {
    final String emptyString = new String();
    assertNull(cut.getSchemas(emptyString));
  }

  @Test
  public void testReadXMLWithNullURI() throws ODataJPAModelException, IOException {
    final URI nullURI = null;
    assertNull(cut.getSchemas(nullURI));
  }

  @Test
  public void TestGetNamespaceFromPath() throws IOException, ODataJPAModelException {
    Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    CsdlSchema schema = act.get("Org.OData.Core.V1");
    assertEquals("Org.OData.Core.V1", schema.getNamespace());
  }

  @Test
  public void TestGetAliasFromPath() throws IOException, ODataJPAModelException {
    Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    CsdlSchema schema = act.get("Org.OData.Core.V1");
    assertEquals("Core", schema.getAlias());
  }

  @Test
  public void TestGetTermsFromPath() throws IOException, ODataJPAModelException {
    Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    CsdlSchema schema = act.get("Org.OData.Core.V1");
    assertEquals(15, schema.getTerms().size());
  }

  @Test
  public void TestGetTypeDefinitionFromPath() throws IOException, ODataJPAModelException {
    Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    CsdlSchema schema = act.get("Org.OData.Core.V1");
    assertEquals(1, schema.getTypeDefinitions().size());
    assertNotNull(schema.getTypeDefinition("Tag"));
    assertEquals("Edm.Boolean", schema.getTypeDefinition("Tag").getUnderlyingType());
  }

  @Test
  public void TestGetTypeDefinitions() throws IOException, ODataJPAModelException {
    final CsdlSchema act = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1");
    assertEquals(3, act.getTypeDefinitions().size());

    assertNotNull(act.getTypeDefinition("TestTypeDecimal"));
    CsdlTypeDefinition actType = act.getTypeDefinition("TestTypeDecimal");
    assertEquals(10, actType.getPrecision());
    assertEquals(5, actType.getScale());
    assertNull(actType.getSrid());
    assertNull(actType.getMaxLength());

    actType = act.getTypeDefinition("TestTypeGeo");
    assertNull(actType.getPrecision());
    assertNull(actType.getScale());
    assertEquals(SRID.valueOf("variable"), actType.getSrid());
    assertNull(actType.getMaxLength());
    assertTrue(actType.isUnicode());

    actType = act.getTypeDefinition("TestTypeString");
    assertNull(actType.getPrecision());
    assertNull(actType.getScale());
    assertNull(actType.getSrid());
    assertEquals(256, actType.getMaxLength());
    assertFalse(actType.isUnicode());
  }

  @Test
  public void TestGetEnumSchemaFromPath() throws IOException, ODataJPAModelException {
    Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    CsdlSchema schema = act.get("Org.OData.Core.V1");
    assertEquals(1, schema.getEnumTypes().size());
    assertNotNull(schema.getEnumType("Permission"));
    assertEquals(3, schema.getEnumType("Permission").getMembers().size());
    assertEquals("3", schema.getEnumType("Permission").getMember("ReadWrite").getValue());
  }

  @Test
  public void TestThrowsExceptionOnUnknownPath() throws IOException, ODataJPAModelException {
    assertThrows(ODataJPAModelException.class, () -> {
      cut.getSchemas("annotations/Org.OData.Core.V2.xml");
    });
  }

  @Test
  public void TestThrowsExceptionOnEmptyXML() throws IOException, ODataJPAModelException {

    assertThrows(IOException.class, () -> {
      cut.getSchemas("annotations/empty.xml");
    });
  }

  @Test
  public void TestGetSimpleComplexTypes() throws IOException, ODataJPAModelException {
    final Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml");
    assertEquals(2, act.size());
    assertTrue(act.containsKey("Org.OData.Capabilities.V1"));
    final CsdlSchema actSchema = act.get("Org.OData.Capabilities.V1");
    final CsdlComplexType actCt = actSchema.getComplexType("UpdateRestrictionsType");
    assertNotNull(actCt);
    assertNull(actCt.getBaseType());
    assertNull(actCt.getBaseTypeFQN());
    assertFalse(actCt.isAbstract());
    assertFalse(actCt.isOpenType());
    assertEquals("UpdateRestrictionsType", actCt.getName());
    assertEquals(2, actCt.getProperties().size());
    assertEquals(0, actCt.getNavigationProperties().size());
    assertEquals(0, actCt.getAnnotations().size());
  }

  @Test
  public void TestGetDeepComplexTypes() throws IOException, ODataJPAModelException {
    final CsdlComplexType actCt = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getComplexType("TestType");
    assertNotNull(actCt);
    assertEquals(5, actCt.getProperties().size());
    assertTrue(actCt.isAbstract());
    assertTrue(actCt.isOpenType());
    assertNotNull(actCt.getBaseType());
    assertEquals("Core.Unknow", actCt.getBaseType());
    assertEquals("Core.Unknow", actCt.getBaseTypeFQN().getFullQualifiedNameAsString());
    assertEquals(0, actCt.getAnnotations().size()); // Annotations are ignored
  }

  @Test
  public void TestGetSimpleProperty() throws IOException, ODataJPAModelException {
    final CsdlComplexType actCt = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getComplexType("TestType");
    final CsdlProperty actProperty = actCt.getProperty("Deletable");
    assertNotNull(actProperty);
    assertEquals("Deletable", actProperty.getName());
    assertEquals("Edm.Boolean", actProperty.getType());
    assertEquals("true", actProperty.getDefaultValue());
    assertNull(actProperty.getMaxLength());
    assertNull(actProperty.getScale());
    assertNull(actProperty.getSrid());
    assertNull(actProperty.getPrecision());
    assertTrue(actProperty.isUnicode());
  }

  @Test
  public void TestGetDecimalProperty() throws IOException, ODataJPAModelException {
    final CsdlComplexType actCt = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getComplexType("TestType");
    final CsdlProperty actProperty = actCt.getProperty("TestDecimals");
    assertNotNull(actProperty);
    assertEquals("TestDecimals", actProperty.getName());
    assertEquals("Edm.Decimal", actProperty.getType());
    assertNull(actProperty.getDefaultValue());
    assertNull(actProperty.getMaxLength());
    assertEquals(5, actProperty.getScale());
    assertNull(actProperty.getSrid());
    assertEquals(10, actProperty.getPrecision());
    assertTrue(actProperty.isUnicode());
  }

  @Test
  public void TestGetStringProperty() throws IOException, ODataJPAModelException {
    final CsdlComplexType actCt = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getComplexType("TestType");
    final CsdlProperty actProperty = actCt.getProperty("TestString");
    assertNotNull(actProperty);
    assertEquals("TestString", actProperty.getName());
    assertEquals("Edm.String", actProperty.getType());
    assertNull(actProperty.getDefaultValue());
    assertEquals(256, actProperty.getMaxLength());
    assertNull(actProperty.getScale());
    assertNull(actProperty.getSrid());
    assertNull(actProperty.getPrecision());
    assertFalse(actProperty.isUnicode());
  }

  @Test
  public void TestGetGeoProperty() throws IOException, ODataJPAModelException {
    final CsdlComplexType actCt = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getComplexType("TestType");
    final CsdlProperty actProperty = actCt.getProperty("TestGeo");
    assertNotNull(actProperty);
    assertEquals("TestGeo", actProperty.getName());
    assertEquals("Edm.GeometryPoint", actProperty.getType());
    assertNull(actProperty.getDefaultValue());
    assertNull(actProperty.getMaxLength());
    assertNull(actProperty.getScale());
    assertEquals(SRID.valueOf("3857"), actProperty.getSrid());
    assertNull(actProperty.getPrecision());
    assertTrue(actProperty.isUnicode());
    assertFalse(actProperty.isCollection());
  }

  @Test
  public void TestGetCollectionProperty() throws IOException, ODataJPAModelException {
    final CsdlComplexType actCt = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getComplexType("TestType");
    final CsdlProperty actProperty = actCt.getProperty("NonDeletableNavigationProperties");
    assertNotNull(actProperty);
    assertEquals("NonDeletableNavigationProperties", actProperty.getName());
    assertEquals("Edm.NavigationPropertyPath", actProperty.getType());
    assertTrue(actProperty.isCollection());
  }

  @Test
  public void TestGetEnum() throws IOException, ODataJPAModelException {
    final CsdlSchema act = cut.getSchemas("annotations/Org.OData.Capabilities.V1.xml")
        .get("Org.OData.Capabilities.V1");
    assertEquals(4, act.getEnumTypes().size());
  }

  @Test
  public void TestGetEnumNotAsFlags() throws IOException, ODataJPAModelException {
    final CsdlEnumType actEnum = cut.getSchemas("annotations/Org.OData.Aggregation.V1.xml")
        .get("Org.OData.Aggregation.V1").getEnumType("RollupType");
    assertNotNull(actEnum);
    assertEquals(3, actEnum.getMembers().size());
    assertNotNull(actEnum.getMember("MultipleHierarchies"));
    assertNull(actEnum.getUnderlyingType());
    assertFalse(actEnum.isFlags());
  }

  @Test
  public void TestGetEnumAsFlags() throws IOException, ODataJPAModelException {
    final CsdlEnumType actEnum = cut.getSchemas("annotations/Org.OData.Capabilities.V1.xml")
        .get("Org.OData.Capabilities.V1").getEnumType("IsolationLevel");
    assertNotNull(actEnum);
    assertEquals(1, actEnum.getMembers().size());
    assertNotNull(actEnum.getMember(1));
    assertNull(actEnum.getUnderlyingType());
    assertTrue(actEnum.isFlags());
    final CsdlEnumMember actMember = actEnum.getMember(1);
    assertEquals("Snapshot", actMember.getName());
    assertEquals("1", actMember.getValue());
  }

  @Test
  public void TestGetFunctions() throws IOException, ODataJPAModelException {
    final CsdlSchema act = cut.getSchemas("annotations/Org.OData.Aggregation.V1.xml")
        .get("Org.OData.Aggregation.V1");
    assertEquals(5, act.getFunctions().size());
  }

  @Test
  public void TestGetFunctionAttributes() throws IOException, ODataJPAModelException {
    final List<CsdlFunction> act = cut.getSchemas("annotations/Org.OData.Aggregation.V1.xml")
        .get("Org.OData.Aggregation.V1").getFunctions("isleaf");
    assertEquals(1, act.size());
    final CsdlFunction actFunc = act.get(0);
    assertEquals("isleaf", actFunc.getName());
    assertEquals(2, actFunc.getParameters().size());
    assertTrue(actFunc.isBound());
    assertFalse(actFunc.isComposable());
    assertNotNull(actFunc.getReturnType());
  }

  @Test
  public void TestGetFunctionParameter() throws IOException, ODataJPAModelException {
    final List<CsdlFunction> act = cut.getSchemas("annotations/Org.OData.Aggregation.V1.xml")
        .get("Org.OData.Aggregation.V1").getFunctions("isancestor");

    final CsdlFunction actFunc = act.get(0);
    assertEquals(4, actFunc.getParameters().size());
    final CsdlParameter actMendatory = actFunc.getParameter("Entity");
    assertNotNull(actMendatory);
    assertEquals("Entity", actMendatory.getName());
    assertEquals("Edm.EntityType", actMendatory.getType());
    assertFalse(actMendatory.isNullable());

    final CsdlParameter actNullable = actFunc.getParameter("MaxDistance");
    assertTrue(actNullable.isNullable());
  }

  @Test
  public void TestGetFunctionParameterFacet() throws IOException, ODataJPAModelException {
    final List<CsdlFunction> act = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getFunctions("TestTheRest1");

    final CsdlFunction actFunc = act.get(0);
    final CsdlParameter actDecimal = actFunc.getParameter("Dec");
    assertEquals("Edm.Decimal", actDecimal.getTypeFQN().getFullQualifiedNameAsString());
    assertEquals(10, actDecimal.getPrecision());
    assertEquals(5, actDecimal.getScale());
    assertTrue(actDecimal.isNullable());

    final CsdlParameter actGeo = actFunc.getParameter("Geo");
    assertEquals(SRID.valueOf("3857"), actGeo.getSrid());
    assertTrue(actGeo.isNullable());
    assertFalse(actGeo.isCollection());

    final CsdlParameter actString = actFunc.getParameter("Text");
    assertEquals("Edm.String", actString.getType());
    assertTrue(actString.isCollection());
    assertEquals(512, actString.getMaxLength());
  }

  @Test
  public void TestGetFunctionReturnType() throws IOException, ODataJPAModelException {
    final List<CsdlFunction> act = cut.getSchemas("annotations/Org.OData.Aggregation.V1.xml")
        .get("Org.OData.Aggregation.V1").getFunctions("isancestor");

    final CsdlFunction actFunc = act.get(0);
    final CsdlReturnType actReturn = actFunc.getReturnType();
    assertNotNull(actReturn);
    assertEquals("Edm.Boolean", actReturn.getType());
    assertFalse(actReturn.isNullable());
    assertNull(actReturn.getMaxLength());
    assertNull(actReturn.getScale());
    assertNull(actReturn.getSrid());
    assertNull(actReturn.getPrecision());
    assertFalse(actReturn.isCollection());
  }

  @Test
  public void TestGetFunctionReturnTypeFacet() throws IOException, ODataJPAModelException {
    final List<CsdlFunction> act = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getFunctions("TestTheRest1");

    final CsdlFunction actFunc = act.get(0);
    assertEquals("timeslices", actFunc.getEntitySetPath());
    final CsdlReturnType actReturn = actFunc.getReturnType();
    assertNotNull(actReturn);
    assertEquals("Edm.Decimal", actReturn.getTypeFQN().getFullQualifiedNameAsString());
    assertEquals(10, actReturn.getPrecision());
    assertEquals(5, actReturn.getScale());
    assertTrue(actReturn.isNullable());
    assertTrue(actReturn.isCollection());
    assertEquals(20, actReturn.getMaxLength());
    assertEquals(SRID.valueOf("3857"), actReturn.getSrid());
  }

  @Test
  public void TestGetActionss() throws IOException, ODataJPAModelException {
    final List<CsdlAction> act = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getActions();
    assertEquals(2, act.size());
  }

  @Test
  public void TestGetActionParameter() throws IOException, ODataJPAModelException {
    final List<CsdlAction> act = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getActions("UpsertTimeExample");

    final CsdlAction actAction = act.get(0);
    assertEquals(3, actAction.getParameters().size());
    assertTrue(actAction.isBound());
    assertEquals("UpsertTimeExample", actAction.getName());
    assertEquals("timeslices", actAction.getEntitySetPath());
    final CsdlParameter actParam = actAction.getParameter("timeslices");
    assertEquals("Edm.EntityType", actParam.getTypeFQN().getFullQualifiedNameAsString());
    assertTrue(actParam.isCollection());
    assertFalse(actParam.isNullable());
  }

  @Test
  public void TestGetActionReturnType() throws IOException, ODataJPAModelException {
    final List<CsdlAction> act = cut.getSchemas("annotations/Org.Olingo.Test.V1.xml")
        .get("Org.OData.Capabilities.V1").getActions("UpsertTimeExample");

    final CsdlAction actAction = act.get(0);
    assertNotNull(actAction.getReturnType());
    final CsdlReturnType actReturn = actAction.getReturnType();
    assertFalse(actReturn.isNullable());
    assertTrue(actReturn.isCollection());
  }
}
