package com.sap.olingo.jpa.processor.core.modify;

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

import javax.persistence.AttributeConverter;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassifiaction;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRightsConverter;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.DateConverter;

public class TestJPACUDRequestHelper {
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
    assertEquals(5, act.size());
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

    List<String> propertyNames = new ArrayList<>();
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
    List<Property> odataProperties = new ArrayList<>();
    JPAStructuredType st = mock(JPAStructuredType.class);

    Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(0, act.size());
  }

  @Test
  public void testConvertPropertiesUnknownValueType() {
    List<Property> odataProperties = new ArrayList<>();
    JPAStructuredType st = mock(JPAStructuredType.class);
    Property propertyID = mock(Property.class);

    when(propertyID.getValueType()).thenReturn(ValueType.COLLECTION_ENUM);
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
    List<Property> odataProperties = new ArrayList<>();
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
    List<Property> odataProperties = new ArrayList<>();
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
  public void testConvertPropertiesOneEnumPropertyWithoutConverter() throws ODataJPAProcessException,
      ODataJPAModelException {

    List<Property> odataProperties = new ArrayList<>();
    JPAStructuredType st = mock(JPAStructuredType.class);
    Property propertyID = mock(Property.class);
    JPAAttribute attribute = mock(JPAAttribute.class);
    JPAPath path = mock(JPAPath.class);
    CsdlProperty edmProperty = mock(CsdlProperty.class);

    when(st.getPath("ABCClass")).thenReturn(path);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.getInternalName()).thenReturn("aBCClass");

    Answer<?> a = (new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        return ABCClassifiaction.class;
      }
    });
    when(attribute.getType()).thenAnswer(a);
    when(attribute.getProperty()).thenReturn(edmProperty);
    when(attribute.isEnum()).thenReturn(true);
    when(propertyID.getValueType()).thenReturn(ValueType.ENUM);
    when(propertyID.getName()).thenReturn("ABCClass");
    when(propertyID.getValue()).thenReturn(1);
    odataProperties.add(propertyID);

    Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals(ABCClassifiaction.B, act.get("aBCClass"));
  }

  @Test
  public void testConvertPropertiesOneEnumPropertyWithConverter() throws ODataJPAProcessException,
      ODataJPAModelException {
    List<Property> odataProperties = new ArrayList<>();
    JPAStructuredType st = mock(JPAStructuredType.class);
    Property propertyID = mock(Property.class);
    JPAAttribute attribute = mock(JPAAttribute.class);
    JPAPath path = mock(JPAPath.class);
    CsdlProperty edmProperty = mock(CsdlProperty.class);

    when(st.getPath("AccessRights")).thenReturn(path);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.getInternalName()).thenReturn("accessRights");

    Answer<?> a = (new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) {
        return AccessRights.class;
      }
    });
    when(attribute.getType()).thenAnswer(a);
    when(attribute.getProperty()).thenReturn(edmProperty);
    when(attribute.getConverter()).thenAnswer(new Answer<AttributeConverter<?, ?>>() {
      @Override
      public AttributeConverter<?, ?> answer(InvocationOnMock invocation) throws Throwable {
        return new AccessRightsConverter();
      }
    });
    when(edmProperty.getMaxLength()).thenReturn(100);
    when(propertyID.getValueType()).thenReturn(ValueType.ENUM);
    when(propertyID.getName()).thenReturn("AccessRights");
    when(propertyID.getValue()).thenReturn((short) 8);
    odataProperties.add(propertyID);

    Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals(AccessRights.Delete, act.get("accessRights"));
  }

  @Test
  public void testConvertPropertiesOneComplexProperty() throws ODataJPAProcessException, ODataJPAModelException {
    List<Property> odataProperties = new ArrayList<>();
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
    List<JPAElement> addressPathElements = new ArrayList<>();
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

  // AdministrativeDivisionDescription

}
