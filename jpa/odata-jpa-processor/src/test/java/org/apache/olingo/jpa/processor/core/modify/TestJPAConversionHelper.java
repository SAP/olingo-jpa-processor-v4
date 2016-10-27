package org.apache.olingo.jpa.processor.core.modify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescriptionKey;
import org.apache.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import org.apache.olingo.jpa.processor.core.testmodel.BusinessPartnerRoleKey;
import org.apache.olingo.jpa.processor.core.testmodel.DateConverter;
import org.apache.olingo.jpa.processor.core.testmodel.Organization;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestJPAConversionHelper {
  private JPAConversionHelper cut;

  @Before
  public void setUp() throws Exception {
    cut = new JPAConversionHelper();
  }

  @Test
  public void testInstanceNull() {

    try {
      cut.buildGetterMap(null);
    } catch (ODataJPAProcessorException e) {
      assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  public void testInstanceWithoutGetter() throws ODataJPAProcessorException {

    Map<String, Object> act = cut.buildGetterMap(new DateConverter());
    assertNotNull(act);
    assertEquals(1, act.size());
    assertNotNull(act.get("class"));
  }

  @Test
  public void testInstanceWithGetter() throws ODataJPAProcessorException {
    BusinessPartnerRole role = new BusinessPartnerRole();
    role.setBusinessPartnerID("ID");

    Map<String, Object> act = cut.buildGetterMap(role);
    assertNotNull(act);
    assertEquals(4, act.size());
    assertEquals("ID", act.get("businessPartnerID"));
  }

  @Test
  public void testSameInstanceWhenReadingTwice() throws ODataJPAProcessorException {
    BusinessPartnerRole role = new BusinessPartnerRole();

    Map<String, Object> exp = cut.buildGetterMap(role);
    Map<String, Object> act = cut.buildGetterMap(role);

    assertTrue(exp == act);
  }

  @Test
  public void testDifferentInstanceWhenReadingDifferntInstance() throws ODataJPAProcessorException {

    Map<String, Object> exp = cut.buildGetterMap(new BusinessPartnerRole());
    Map<String, Object> act = cut.buildGetterMap(new BusinessPartnerRole());

    assertFalse(exp == act);
  }

  @Test
  public void testConvertEmptyInputStream() throws UnsupportedEncodingException {
    ODataRequest request = mock(ODataRequest.class);
    EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);

    InputStream is = new ByteArrayInputStream("".getBytes("UTF-8"));
    when(request.getBody()).thenReturn(is);
    try {
      cut.convertInputStream(OData.newInstance(), request, ContentType.APPLICATION_JSON, edmEntitySet);
    } catch (ODataJPAProcessorException e) {
      assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testConvertInputStream() throws UnsupportedEncodingException, ODataJPAProcessorException,
      EdmPrimitiveTypeException {

    ODataRequest request = mock(ODataRequest.class);
    EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
    EdmEntityType edmEntityType = mock(EdmEntityType.class);
    EdmProperty edmPropertyId = mock(EdmProperty.class);
    EdmPrimitiveType edmTypeId = mock(EdmPrimitiveType.class);

    FullQualifiedName fqn = new FullQualifiedName("test", "Organisation");
    FullQualifiedName fqnString = new FullQualifiedName("test", "Organisation");

    List<String> propertyNames = new ArrayList<String>();
    propertyNames.add("ID");

    when(edmTypeId.getFullQualifiedName()).thenReturn(fqnString);
    when(edmTypeId.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);
    when(edmTypeId.getName()).thenReturn("String");
    when(edmTypeId.valueOfString(Matchers.anyString(), Matchers.anyBoolean(), Matchers.anyInt(), Matchers.anyInt(),
        Matchers.anyInt(), Matchers.anyBoolean(), (Class<String>) Matchers.anyVararg())).thenReturn("35");

    when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
    when(edmEntityType.getFullQualifiedName()).thenReturn(fqn);
    when(edmEntityType.getPropertyNames()).thenReturn(propertyNames);
    when(edmEntityType.getProperty("ID")).thenReturn(edmPropertyId);
    when(edmPropertyId.getName()).thenReturn("ID");
    when(edmPropertyId.getType()).thenReturn(edmTypeId);

    InputStream is = new ByteArrayInputStream("{\"ID\" : \"35\"}".getBytes("UTF-8"));
    when(request.getBody()).thenReturn(is);

    Entity act = cut.convertInputStream(OData.newInstance(), request, ContentType.APPLICATION_JSON, edmEntitySet);
    assertEquals("35", act.getProperty("ID").getValue());
  }

  @Test
  public void testConvertPropertiesEmptyList() throws ODataJPAProcessException {
    List<Property> odataProperties = new ArrayList<Property>();
    JPAStructuredType st = mock(JPAStructuredType.class);

    Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(0, act.size());
  }

  @Test
  public void testConvertPropertiesUnknownValueType() {
    List<Property> odataProperties = new ArrayList<Property>();
    JPAStructuredType st = mock(JPAStructuredType.class);
    Property propertyID = mock(Property.class);

    when(propertyID.getValueType()).thenReturn(ValueType.ENUM);
    when(propertyID.getName()).thenReturn("ID");
    when(propertyID.getValue()).thenReturn("35");
    odataProperties.add(propertyID);

    try {
      cut.convertProperties(OData.newInstance(), st, odataProperties);
    } catch (ODataJPAProcessException e) {
      assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  public void testConvertPropertiesConvertException() throws ODataJPAModelException {
    List<Property> odataProperties = new ArrayList<Property>();
    JPAStructuredType st = mock(JPAStructuredType.class);
    Property propertyID = mock(Property.class);

    when(propertyID.getValueType()).thenReturn(ValueType.PRIMITIVE);
    when(propertyID.getName()).thenReturn("iD");
    when(propertyID.getValue()).thenReturn("35");
    odataProperties.add(propertyID);
    when(st.getPath(Matchers.anyString())).thenThrow(new ODataJPAModelException(new NullPointerException()));
    try {
      cut.convertProperties(OData.newInstance(), st, odataProperties);
    } catch (ODataJPAProcessException e) {
      assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  public void testConvertPropertiesOnePrimitiveProperty() throws ODataJPAProcessException, ODataJPAModelException {
    List<Property> odataProperties = new ArrayList<Property>();
    JPAStructuredType st = mock(JPAStructuredType.class);
    Property propertyID = mock(Property.class);
    JPAAttribute attribute = mock(JPAAttribute.class);
    JPAPath path = mock(JPAPath.class);
    CsdlProperty edmProperty = mock(CsdlProperty.class);

    when(st.getPath("ID")).thenReturn(path);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.getInternalName()).thenReturn("iD");

    Answer<?> a = (new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        return String.class;
      }
    });
    when(attribute.getType()).thenAnswer(a);
    when(attribute.getProperty()).thenReturn(edmProperty);
    when(edmProperty.getMaxLength()).thenReturn(100);
    when(propertyID.getValueType()).thenReturn(ValueType.PRIMITIVE);
    when(propertyID.getName()).thenReturn("ID");
    when(propertyID.getValue()).thenReturn("35");
    odataProperties.add(propertyID);

    Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("35", act.get("iD"));
  }

  @Test
  public void testConvertPropertiesOneComplexProperty() throws ODataJPAProcessException, ODataJPAModelException {
    List<Property> odataProperties = new ArrayList<Property>();
    JPAStructuredType st = mock(JPAStructuredType.class);
    Property propertyID = mock(Property.class);
    JPAAttribute attribute = mock(JPAAttribute.class);
    JPAPath pathID = mock(JPAPath.class);
    CsdlProperty edmProperty = mock(CsdlProperty.class);

    when(st.getPath("ID")).thenReturn(pathID);
    when(pathID.getLeaf()).thenReturn(attribute);
    when(attribute.getInternalName()).thenReturn("iD");

    Answer<?> a = (new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        return String.class;
      }
    });
    when(attribute.getType()).thenAnswer(a);
    when(attribute.getProperty()).thenReturn(edmProperty);
    when(edmProperty.getMaxLength()).thenReturn(100);
    when(propertyID.getValueType()).thenReturn(ValueType.PRIMITIVE);
    when(propertyID.getName()).thenReturn("ID");
    when(propertyID.getValue()).thenReturn("35");
    odataProperties.add(propertyID);

    ComplexValue cv = new ComplexValue();
    List<JPAElement> addressPathElements = new ArrayList<JPAElement>();
    JPAElement addressElement = mock(JPAElement.class);
    addressPathElements.add(addressElement);
    when(addressElement.getInternalName()).thenReturn("address");

    Property propertyAddress = mock(Property.class);
    when(propertyAddress.getValueType()).thenReturn(ValueType.COMPLEX);
    when(propertyAddress.getName()).thenReturn("Address");
    when(propertyAddress.getValue()).thenReturn(cv);
    odataProperties.add(propertyAddress);
    JPAPath pathAddress = mock(JPAPath.class);
    when(st.getPath("Address")).thenReturn(pathAddress);
    when(pathAddress.getPath()).thenReturn(addressPathElements);
    JPAAttribute attributeAddress = mock(JPAAttribute.class);
    when(st.getAttribute("address")).thenReturn(attributeAddress);

    Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(2, act.size());
    assertTrue(act.get("address") instanceof Map<?, ?>);
  }

  @Test
  public void testConvertSimpleKeyToLocation() throws ODataJPAProcessorException, SerializerException,
      ODataJPAModelException {
    final List<JPAPath> keyPath = new ArrayList<JPAPath>();

    Organization newPOJO = new Organization();
    newPOJO.setID("35");
    ODataRequest request = mock(ODataRequest.class);
    when(request.getRawBaseUri()).thenReturn("localhost.test");
    EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
    JPAEntityType et = mock(JPAEntityType.class);
    when(et.getKeyPath()).thenReturn(keyPath);

    addKeyAttribute(keyPath, "ID", "iD");

    OData odata = mock(OData.class);
    UriHelper uriHelper = new UriHelperSpy(UriHelperSpy.SINGLE);
    when(odata.createUriHelper()).thenReturn(uriHelper);

    String act = cut.convertKeyToLocal(odata, request, edmEntitySet, et, newPOJO);
    assertEquals("localhost.test/Organisation('35')", act);
  }

  @Test
  public void testConvertCompoundKeyToLocation() throws ODataJPAProcessorException, SerializerException,
      ODataJPAModelException {
    final List<JPAPath> keyPath = new ArrayList<JPAPath>();

    BusinessPartnerRoleKey primaryKey = new BusinessPartnerRoleKey();
    primaryKey.setBusinessPartnerID("35");
    primaryKey.setRoleCategory("A");

    ODataRequest request = mock(ODataRequest.class);
    when(request.getRawBaseUri()).thenReturn("localhost.test");
    EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
    JPAEntityType et = mock(JPAEntityType.class);
    when(et.getKeyPath()).thenReturn(keyPath);

    addKeyAttribute(keyPath, "BusinessPartnerID", "businessPartnerID");
    addKeyAttribute(keyPath, "RoleCategory", "roleCategory");

    OData odata = mock(OData.class);
    UriHelper uriHelper = new UriHelperSpy(UriHelperSpy.COMPOUND_KEY);
    when(odata.createUriHelper()).thenReturn(uriHelper);

    String act = cut.convertKeyToLocal(odata, request, edmEntitySet, et, primaryKey);
    assertEquals("localhost.test/BusinessPartnerRoles(BusinessPartnerID='35',RoleCategory='A')", act);
  }

  @Test
  public void testConvertEmbeddedIdToLocation() throws ODataJPAProcessorException, SerializerException,
      ODataJPAModelException {
    List<JPAPath> keyPath = new ArrayList<JPAPath>();

    AdministrativeDivisionDescriptionKey primaryKey = new AdministrativeDivisionDescriptionKey();
    primaryKey.setCodeID("NUTS1");
    primaryKey.setCodePublisher("Eurostat");
    primaryKey.setDivisionCode("BE1");
    primaryKey.setLanguage("fr");

    ODataRequest request = mock(ODataRequest.class);
    when(request.getRawBaseUri()).thenReturn("localhost.test");

    EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
    JPAEntityType et = mock(JPAEntityType.class);
    when(et.getKeyPath()).thenReturn(keyPath);
    JPAPath key = mock(JPAPath.class);
    keyPath.add(key);
    JPAAttribute keyAttribute = mock(JPAAttribute.class);
    when(keyAttribute.getExternalName()).thenReturn("Key");
    when(keyAttribute.isComplex()).thenReturn(true);
    when(keyAttribute.isKey()).thenReturn(true);
    when(key.getLeaf()).thenReturn(keyAttribute);

    JPAStructuredType st = mock(JPAStructuredType.class);
    when(keyAttribute.getStructuredType()).thenReturn(st);
    keyPath = new ArrayList<JPAPath>();
    when(st.getPathList()).thenReturn(keyPath);

    addKeyAttribute(keyPath, "CodeID", "codeID");
    addKeyAttribute(keyPath, "CodePublisher", "codePublisher");
    addKeyAttribute(keyPath, "DivisionCode", "divisionCode");
    addKeyAttribute(keyPath, "Language", "language");

    OData odata = mock(OData.class);
    UriHelper uriHelper = new UriHelperSpy(UriHelperSpy.EMBEDDED_ID);
    when(odata.createUriHelper()).thenReturn(uriHelper);

    String act = cut.convertKeyToLocal(odata, request, edmEntitySet, et, primaryKey);
    assertEquals(
        "localhost.test/AdministrativeDivisionDescriptions(DivisionCode='BE1',CodeID='NUTS1',CodePublisher='Eurostat',Language='fr')",
        act);

  }
  // AdministrativeDivisionDescription

  private void addKeyAttribute(List<JPAPath> keyPath, String externalName, String internalName) {
    JPAPath key;
    JPAAttribute keyAttribute;
    key = mock(JPAPath.class);
    keyPath.add(key);
    keyAttribute = mock(JPAAttribute.class);
    when(keyAttribute.getExternalName()).thenReturn(externalName);
    when(keyAttribute.getInternalName()).thenReturn(internalName);
    when(key.getLeaf()).thenReturn(keyAttribute);
  }

  private class UriHelperSpy implements UriHelper {
    public static final String EMBEDDED_ID = "EmbeddedId";
    public static final String COMPOUND_KEY = "CompoundKey";
    public static final String SINGLE = "SingleID";
    private final String mode;

    public UriHelperSpy(String mode) {
      this.mode = mode;
    }

    @Override
    public String buildContextURLSelectList(EdmStructuredType type, ExpandOption expand, SelectOption select)
        throws SerializerException {
      fail();
      return null;
    }

    @Override
    public String buildContextURLKeyPredicate(List<UriParameter> keys) throws SerializerException {
      fail();
      return null;
    }

    @Override
    public String buildCanonicalURL(EdmEntitySet edmEntitySet, Entity entity) throws SerializerException {
      if (mode.equals(EMBEDDED_ID)) {
        assertEquals(4, entity.getProperties().size());
        int found = 0;
        for (final Property property : entity.getProperties()) {
          if (property.getName().equals("DivisionCode") && property.getValue().equals("BE1"))
            found++;
          else if (property.getName().equals("Language") && property.getValue().equals("fr"))
            found++;
        }
        assertEquals("Not all key attributes found", 2, found);
        return "AdministrativeDivisionDescriptions(DivisionCode='BE1',CodeID='NUTS1',CodePublisher='Eurostat',Language='fr')";
      } else if (mode.equals(COMPOUND_KEY)) {
        assertEquals(2, entity.getProperties().size());
        int found = 0;
        for (final Property property : entity.getProperties()) {
          if (property.getName().equals("BusinessPartnerID") && property.getValue().equals("35"))
            found++;
          else if (property.getName().equals("RoleCategory") && property.getValue().equals("A"))
            found++;
        }
        assertEquals("Not all key attributes found", 2, found);
        return "BusinessPartnerRoles(BusinessPartnerID='35',RoleCategory='A')";
      } else if (mode.equals(SINGLE)) {
        assertEquals(1, entity.getProperties().size());
        assertEquals("35", entity.getProperties().get(0).getValue());
        return "Organisation('35')";
      }
      fail();
      return null;

    }

    @Override
    public String buildKeyPredicate(EdmEntityType edmEntityType, Entity entity) throws SerializerException {
      fail();
      return null;
    }

    @Override
    public UriResourceEntitySet parseEntityId(Edm edm, String entityId, String rawServiceRoot)
        throws DeserializerException {
      fail();
      return null;
    }

  }

}
