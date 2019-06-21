package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
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

//  csdlSchema.setEnumTypes(asEnumTypes());
//  csdlSchema.setComplexTypes(asComplexTypes());
//  csdlSchema.setTypeDefinitions(asTypeDefinitions());
}
