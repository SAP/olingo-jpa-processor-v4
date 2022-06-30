package com.sap.olingo.jpa.processor.core.modify;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.AttributeConverter;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.query.EdmBindingTargetInfo;
import com.sap.olingo.jpa.processor.core.testmodel.ABCClassification;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRights;
import com.sap.olingo.jpa.processor.core.testmodel.AccessRightsConverter;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.DateConverter;

class TestJPACUDRequestHelper {
  private static final String COMMENT_INT_PROPERTY_NAME = "comment";
  private static final String COMMENT_EXT_PROPERTY_NAME = "Comment";
  private static final String INHOUSE_EXT_PROPERTY_NAME = "InhouseAddress";
  private JPAConversionHelper cut;
  private List<UriResource> uriResourceParts;
  private ODataRequest request;
  private List<String> headers;

  @BeforeEach
  void setUp() throws Exception {
    request = mock(ODataRequest.class);
    headers = new ArrayList<>(1);
    uriResourceParts = new ArrayList<>();
    cut = new JPAConversionHelper();
  }

  @Test
  void testConvertEmptyInputStream() throws UnsupportedEncodingException {

    final EdmBindingTargetInfo etsInfo = mock(EdmBindingTargetInfo.class);
    final EdmEntitySet ets = mock(EdmEntitySet.class);
    final UriResourceEntitySet uriEs = mock(UriResourceEntitySet.class);

    final InputStream is = new ByteArrayInputStream("".getBytes("UTF-8"));
    uriResourceParts.add(uriEs);

    when(uriEs.getEntitySet()).thenReturn(ets);
    when(uriEs.getKind()).thenReturn(UriResourceKind.entitySet);
    when(request.getBody()).thenReturn(is);
    when(etsInfo.getEdmBindingTarget()).thenReturn(ets);
    when(etsInfo.getTargetEdmBindingTarget()).thenReturn(ets);

    try {
      cut.convertInputStream(OData.newInstance(), request, ContentType.APPLICATION_JSON, uriResourceParts);
    } catch (final ODataJPAProcessorException e) {
      assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testConvertInputStreamComplexColectionProperty() throws UnsupportedEncodingException,
      ODataJPAProcessorException, EdmPrimitiveTypeException {

    final EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
    final EdmEntityType edmEntityType = mock(EdmEntityType.class);
    final EdmProperty edmPropertyInhouse = mock(EdmProperty.class);
    final EdmComplexType edmTypeInhouse = mock(EdmComplexType.class);
    final UriResourceEntitySet uriEs = mock(UriResourceEntitySet.class);
    final UriResourceComplexProperty uriProperty = mock(UriResourceComplexProperty.class);
    final FullQualifiedName fqn = new FullQualifiedName("test", "Person");
    final FullQualifiedName fqnString = new FullQualifiedName("test", "Person");

    final List<String> propertyNames = new ArrayList<>();
    propertyNames.add(INHOUSE_EXT_PROPERTY_NAME);

    uriResourceParts.add(uriEs);
    uriResourceParts.add(uriProperty);

    when(uriEs.getEntitySet()).thenReturn(edmEntitySet);
    when(uriEs.getKind()).thenReturn(UriResourceKind.entitySet);

    when(uriProperty.getProperty()).thenReturn(edmPropertyInhouse);
    when(uriProperty.getKind()).thenReturn(UriResourceKind.complexProperty);

    when(edmTypeInhouse.getFullQualifiedName()).thenReturn(fqnString);
    when(edmTypeInhouse.getKind()).thenReturn(EdmTypeKind.COMPLEX);
    when(edmTypeInhouse.getName()).thenReturn(INHOUSE_EXT_PROPERTY_NAME);
    when(edmTypeInhouse.getPropertyNames()).thenReturn(Arrays.asList("RoomNumber", "Floor", "TaskID", "Building"));
    EdmProperty edmProperty = createPropertyMock("RoomNumber", EdmPrimitiveTypeKind.Int32, Integer.class, 25);
    when(edmTypeInhouse.getProperty("RoomNumber")).thenReturn(edmProperty);
    edmProperty = createPropertyMock("Floor", EdmPrimitiveTypeKind.Int16, Short.class, 2);
    when(edmTypeInhouse.getProperty("Floor")).thenReturn(edmProperty);
    edmProperty = createPropertyMock("TaskID", EdmPrimitiveTypeKind.String, String.class, "DEV");
    when(edmTypeInhouse.getProperty("TaskID")).thenReturn(edmProperty);
    edmProperty = createPropertyMock("Building", EdmPrimitiveTypeKind.String, String.class, "2");
    when(edmTypeInhouse.getProperty("Building")).thenReturn(edmProperty);

    when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
    when(edmEntityType.getFullQualifiedName()).thenReturn(fqn);
    when(edmEntityType.getPropertyNames()).thenReturn(propertyNames);
    when(edmEntityType.getProperty(INHOUSE_EXT_PROPERTY_NAME)).thenReturn(edmPropertyInhouse);
    when(edmPropertyInhouse.getName()).thenReturn(INHOUSE_EXT_PROPERTY_NAME);
    when(edmPropertyInhouse.getType()).thenReturn(edmTypeInhouse);
    when(edmPropertyInhouse.isCollection()).thenReturn(true);
    final InputStream is = new ByteArrayInputStream(
        "{\"value\": [{\"RoomNumber\": 25, \"Floor\": 2,\"TaskID\": \"DEV\", \"Building\": \"2\" }]}".getBytes(
            "UTF-8"));
    when(request.getBody()).thenReturn(is);

    final Entity act = cut.convertInputStream(OData.newInstance(), request, ContentType.APPLICATION_JSON,
        uriResourceParts);
    assertEquals(ValueType.COLLECTION_COMPLEX, act.getProperty(INHOUSE_EXT_PROPERTY_NAME).getValueType());
    final List<ComplexValue> actValue = (List<ComplexValue>) act.getProperty(INHOUSE_EXT_PROPERTY_NAME).getValue();
    assertEquals(1, actValue.size());
    final ComplexValue actInhouseMail = actValue.get(0);
    assertNotNull(actInhouseMail.getValue().get(0).getValue());
  }

  @Test
  void testConvertInputStreamEntitySet() throws UnsupportedEncodingException, ODataJPAProcessorException,
      EdmPrimitiveTypeException {

    prepareEntitySet();
    final InputStream is = new ByteArrayInputStream("{\"ID\" : \"35\"}".getBytes("UTF-8"));
    when(request.getBody()).thenReturn(is);

    final Entity act = cut.convertInputStream(OData.newInstance(), request, ContentType.APPLICATION_JSON,
        uriResourceParts);
    assertEquals("35", act.getProperty("ID").getValue());
  }

  @Test
  void testConvertInputStreamEntitySetWithAnnotationV400() throws UnsupportedEncodingException,
      ODataJPAProcessorException, EdmPrimitiveTypeException {

    headers.add("4.00");
    prepareEntitySet();
    final InputStream is = new ByteArrayInputStream(
        "{\"@odata.context\": \"$metadata#test.Organisation\", \"@odata.type\": \"#test.Organisation\", \"ID\" : \"35\"}"
            .getBytes("UTF-8"));
    when(request.getBody()).thenReturn(is);
    when(request.getHeaders(HttpHeader.ODATA_VERSION)).thenReturn(headers);

    final Entity act = cut.convertInputStream(OData.newInstance(), request, ContentType.APPLICATION_JSON,
        uriResourceParts);
    assertEquals("35", act.getProperty("ID").getValue());
  }

  @Test
  void testConvertInputStreamEntitySetWithAnnotationV401() throws UnsupportedEncodingException,
      ODataJPAProcessorException, EdmPrimitiveTypeException {

    headers.add("4.01");
    prepareEntitySet();
    final InputStream is = new ByteArrayInputStream(
        "{\"@context\": \"$metadata#test.Organisation\", \"@type\": \"#test.Organisation\", \"ID\" : \"35\"}"
            .getBytes("UTF-8"));
    when(request.getBody()).thenReturn(is);
    when(request.getHeaders(HttpHeader.ODATA_VERSION)).thenReturn(headers);

    final Entity act = cut.convertInputStream(OData.newInstance(), request, ContentType.APPLICATION_JSON,
        uriResourceParts);
    assertEquals("35", act.getProperty("ID").getValue());
  }

  @Test
  void testConvertInputStreamEntitySetThrowsExceptioOnAnnotationMissmatch() throws UnsupportedEncodingException,
      ODataJPAProcessorException, EdmPrimitiveTypeException {

    prepareEntitySet();
    final InputStream is = new ByteArrayInputStream(
        "{\"@context\": \"$metadata#com.sap.olingo.jpa.Organization\", \"@type\": \"#com.sap.olingo.jpa.Organization\", \"ID\" : \"35\"}"
            .getBytes("UTF-8"));
    when(request.getBody()).thenReturn(is);

    assertThrows(ODataJPAProcessorException.class, () -> cut.convertInputStream(OData.newInstance(), request,
        ContentType.APPLICATION_JSON, uriResourceParts));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testConvertInputStreamPrimitiveColectionProperty() throws UnsupportedEncodingException,
      ODataJPAProcessorException, EdmPrimitiveTypeException {

    final EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
    final EdmEntityType edmEntityType = mock(EdmEntityType.class);
    final EdmProperty edmPropertyName = mock(EdmProperty.class);
    final EdmPrimitiveType edmTypeName = mock(EdmPrimitiveType.class);
    final UriResourceEntitySet uriEs = mock(UriResourceEntitySet.class);
    final UriResourceProperty uriProperty = mock(UriResourceProperty.class);
    final FullQualifiedName fqn = new FullQualifiedName("test", "Organisation");
    final FullQualifiedName fqnString = new FullQualifiedName("test", "Organisation");

    final List<String> propertyNames = new ArrayList<>();
    propertyNames.add(COMMENT_EXT_PROPERTY_NAME);

    uriResourceParts.add(uriEs);
    uriResourceParts.add(uriProperty);

    when(uriEs.getEntitySet()).thenReturn(edmEntitySet);
    when(uriEs.getKind()).thenReturn(UriResourceKind.entitySet);

    when(uriProperty.getProperty()).thenReturn(edmPropertyName);
    when(uriProperty.getKind()).thenReturn(UriResourceKind.primitiveProperty);

    when(edmTypeName.getFullQualifiedName()).thenReturn(fqnString);
    when(edmTypeName.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);
    when(edmTypeName.getName()).thenReturn("String");
    when(edmTypeName.valueOfString(ArgumentMatchers.eq("YAC"), ArgumentMatchers.anyBoolean(), ArgumentMatchers
        .anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean(),
        (Class<String>) ArgumentMatchers.any())).thenReturn("YAC");
    when(edmTypeName.valueOfString(ArgumentMatchers.eq("WTN"), ArgumentMatchers.anyBoolean(), ArgumentMatchers
        .anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean(),
        (Class<String>) ArgumentMatchers.any())).thenReturn("WTN");

    when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
    when(edmEntityType.getFullQualifiedName()).thenReturn(fqn);
    when(edmEntityType.getPropertyNames()).thenReturn(propertyNames);
    when(edmEntityType.getProperty(COMMENT_EXT_PROPERTY_NAME)).thenReturn(edmPropertyName);
    when(edmPropertyName.getName()).thenReturn(COMMENT_EXT_PROPERTY_NAME);
    when(edmPropertyName.getType()).thenReturn(edmTypeName);
    when(edmPropertyName.isCollection()).thenReturn(true);
    final InputStream is = new ByteArrayInputStream("{ \"value\": [\"YAC\",\"WTN\"] }".getBytes("UTF-8"));
    when(request.getBody()).thenReturn(is);

    final Entity act = cut.convertInputStream(OData.newInstance(), request, ContentType.APPLICATION_JSON,
        uriResourceParts);
    assertEquals(ValueType.COLLECTION_PRIMITIVE, act.getProperty(COMMENT_EXT_PROPERTY_NAME).getValueType());
    final List<String> actValue = (List<String>) act.getProperty(COMMENT_EXT_PROPERTY_NAME).getValue();
    assertEquals(2, actValue.size());
    assertEquals("YAC", actValue.get(0));
    assertEquals("WTN", actValue.get(1));
  }

  static Stream<ByteArrayInputStream> stringIntAndListProvider() throws UnsupportedEncodingException {
    return Arrays.asList(new ByteArrayInputStream("{\"value\" : \"Willi\"}".getBytes("UTF-8")), // Primitive
        new ByteArrayInputStream("{\"value\" : \"Willi\"}".getBytes("UTF-8")), // WithAnnotationV400
        new ByteArrayInputStream( // WithAnnotationV401
            "{ \"@jpa.odata.context\": \"$metadata#Organisations\", \"value\" : \"Willi\"}".getBytes("UTF-8")))
        .stream();

  }

  @ParameterizedTest
  @MethodSource("stringIntAndListProvider")
  void testConvertInputStreamSimpleProperty(final InputStream is) throws UnsupportedEncodingException,
      ODataJPAProcessorException, EdmPrimitiveTypeException {
    final ODataRequest request = preparePrimitiveSimpleProperty();
    when(request.getBody()).thenReturn(is);

    final Entity act = cut.convertInputStream(OData.newInstance(), request, ContentType.APPLICATION_JSON,
        uriResourceParts);

    assertEquals("Willi", act.getProperty("Name2").getValue());
  }

  @Test
  void testConvertPropertiesConvertException() throws ODataJPAModelException {
    final List<Property> odataProperties = new ArrayList<>();
    final JPAStructuredType st = mock(JPAStructuredType.class);
    final Property propertyID = mock(Property.class);

    when(propertyID.getValueType()).thenReturn(ValueType.PRIMITIVE);
    when(propertyID.getName()).thenReturn("iD");
    when(propertyID.getValue()).thenReturn("35");
    odataProperties.add(propertyID);
    when(st.getPath(ArgumentMatchers.anyString())).thenThrow(new ODataJPAModelException(new NullPointerException()));
    try {
      cut.convertProperties(OData.newInstance(), st, odataProperties);
    } catch (final ODataJPAProcessException e) {
      assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testConvertPropertiesEmptyComplexCollcetionProperty() throws ODataJPAProcessException,
      ODataJPAModelException {

    final List<Property> odataProperties = new ArrayList<>();
    final List<ComplexValue> odataComment = new ArrayList<>();
    final JPAStructuredType st = createMetadataForSimpleProperty("Address", "address");
    final JPAStructuredType nb = createMetadataForSimpleProperty("Number", "number");
    final JPAAttribute attributeAddress = mock(JPAAttribute.class);
    when(attributeAddress.getStructuredType()).thenReturn(nb);
    when(st.getAttribute("address")).thenReturn(Optional.of(attributeAddress));

    final Property propertyAddress = mock(Property.class);
    when(propertyAddress.getValueType()).thenReturn(ValueType.COLLECTION_COMPLEX);
    when(propertyAddress.getName()).thenReturn("Address");
    when(propertyAddress.getValue()).thenReturn(odataComment);
    odataProperties.add(propertyAddress);

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);
    assertNotNull(act.get("address"));
    assertEquals(0, ((List<Map<String, Object>>) act.get("address")).size());
  }

  @Test
  void testConvertPropertiesEmptyList() throws ODataJPAProcessException {
    final List<Property> odataProperties = new ArrayList<>();
    final JPAStructuredType st = mock(JPAStructuredType.class);

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(0, act.size());
  }

  @Test
  void testConvertPropertiesEmptySimpleCollcetionProperty() throws ODataJPAProcessException,
      ODataJPAModelException {

    final List<Property> odataProperties = new ArrayList<>();
    final List<String> odataComment = new ArrayList<>();

    final JPAStructuredType st = createMetadataForSimpleProperty(COMMENT_EXT_PROPERTY_NAME, COMMENT_INT_PROPERTY_NAME);

    final Property propertyComment = mock(Property.class);
    when(propertyComment.getValueType()).thenReturn(ValueType.COLLECTION_PRIMITIVE);
    when(propertyComment.getName()).thenReturn(COMMENT_EXT_PROPERTY_NAME);
    when(propertyComment.getValue()).thenReturn(odataComment);
    odataProperties.add(propertyComment);

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);
    assertNotNull(act.get(COMMENT_INT_PROPERTY_NAME));
    assertTrue(((List<?>) act.get(COMMENT_INT_PROPERTY_NAME)).isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testConvertPropertiesOneComplexCollcetionProperty() throws ODataJPAProcessException,
      ODataJPAModelException {

    final List<Property> odataProperties = new ArrayList<>();
    final List<ComplexValue> odataComment = new ArrayList<>();
    final List<Property> addressProperties = new ArrayList<>();
    final JPAStructuredType st = createMetadataForSimpleProperty("Address", "address");
    final JPAStructuredType nb = createMetadataForSimpleProperty("Number", "number");
    final JPAAttribute attributeAddress = mock(JPAAttribute.class);
    when(attributeAddress.getStructuredType()).thenReturn(nb);
    when(st.getAttribute("address")).thenReturn(Optional.of(attributeAddress));
    final ComplexValue cv1 = mock(ComplexValue.class);

    final Property propertyNumber = mock(Property.class);
    when(propertyNumber.getValueType()).thenReturn(ValueType.PRIMITIVE);
    when(propertyNumber.getName()).thenReturn("Number");
    when(propertyNumber.getValue()).thenReturn(32);
    addressProperties.add(propertyNumber);
    when(cv1.getValue()).thenReturn(addressProperties);

    odataComment.add(cv1);
    final Property propertyAddress = mock(Property.class);
    when(propertyAddress.getValueType()).thenReturn(ValueType.COLLECTION_COMPLEX);
    when(propertyAddress.getName()).thenReturn("Address");
    when(propertyAddress.getValue()).thenReturn(odataComment);
    odataProperties.add(propertyAddress);

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);
    assertNotNull(act.get("address"));
    assertEquals(1, ((List<Map<String, Object>>) act.get("address")).size());
    final Map<String, Object> actAddr = (Map<String, Object>) ((List<?>) act.get("address")).get(0);
    assertEquals(32, actAddr.get("number"));
  }

  @Test
  void testConvertPropertiesOneComplexProperty() throws ODataJPAProcessException, ODataJPAModelException {

    final List<Property> odataProperties = new ArrayList<>();
    final JPAStructuredType st = mock(JPAStructuredType.class);
    final Property propertyID = mock(Property.class);
    final JPAAttribute attribute = mock(JPAAttribute.class);
    final JPAPath pathID = mock(JPAPath.class);
    final CsdlProperty edmProperty = mock(CsdlProperty.class);

    when(st.getPath("ID")).thenReturn(pathID);
    when(pathID.getLeaf()).thenReturn(attribute);
    when(attribute.getInternalName()).thenReturn("iD");

    final Answer<?> a = (new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) {
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

    final ComplexValue cv = new ComplexValue();
    final List<JPAElement> addressPathElements = new ArrayList<>();
    final JPAElement addressElement = mock(JPAElement.class);
    addressPathElements.add(addressElement);
    when(addressElement.getInternalName()).thenReturn("address");

    final Property propertyAddress = mock(Property.class);
    when(propertyAddress.getValueType()).thenReturn(ValueType.COMPLEX);
    when(propertyAddress.getName()).thenReturn("Address");
    when(propertyAddress.getValue()).thenReturn(cv);
    odataProperties.add(propertyAddress);
    final JPAPath pathAddress = mock(JPAPath.class);
    when(st.getPath("Address")).thenReturn(pathAddress);
    when(pathAddress.getPath()).thenReturn(addressPathElements);
    final JPAAttribute attributeAddress = mock(JPAAttribute.class);
    when(st.getAttribute("address")).thenReturn(Optional.of(attributeAddress));

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(2, act.size());
    assertTrue(act.get("address") instanceof Map<?, ?>);
  }

  @Test
  void testConvertPropertiesOneEnumPropertyWithConverter() throws ODataJPAProcessException,
      ODataJPAModelException {

    final List<Property> odataProperties = new ArrayList<>();
    final JPAStructuredType st = mock(JPAStructuredType.class);
    final Property propertyID = mock(Property.class);
    final JPAAttribute attribute = mock(JPAAttribute.class);
    final JPAPath path = mock(JPAPath.class);
    final CsdlProperty edmProperty = mock(CsdlProperty.class);

    when(st.getPath("AccessRights")).thenReturn(path);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.getInternalName()).thenReturn("accessRights");

    final Answer<?> a = (new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) {
        return AccessRights.class;
      }
    });
    when(attribute.getType()).thenAnswer(a);
    when(attribute.getProperty()).thenReturn(edmProperty);
    when(attribute.getConverter()).thenAnswer(new Answer<AttributeConverter<?, ?>>() {
      @Override
      public AttributeConverter<?, ?> answer(final InvocationOnMock invocation) throws Throwable {
        return new AccessRightsConverter();
      }
    });
    when(edmProperty.getMaxLength()).thenReturn(100);
    when(propertyID.getValueType()).thenReturn(ValueType.ENUM);
    when(propertyID.getName()).thenReturn("AccessRights");
    when(propertyID.getValue()).thenReturn((short) 8);
    odataProperties.add(propertyID);

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(1, act.size());
    final AccessRights[] actProperty = (AccessRights[]) act.get("accessRights");
    assertArrayEquals(new Object[] { AccessRights.DELETE }, actProperty);
  }

  @Test
  void testConvertPropertiesOneEnumPropertyWithoutConverter() throws ODataJPAProcessException,
      ODataJPAModelException {

    final List<Property> odataProperties = new ArrayList<>();
    final JPAStructuredType st = mock(JPAStructuredType.class);
    final Property propertyID = mock(Property.class);
    final JPAAttribute attribute = mock(JPAAttribute.class);
    final JPAPath path = mock(JPAPath.class);
    final CsdlProperty edmProperty = mock(CsdlProperty.class);

    when(st.getPath("ABCClass")).thenReturn(path);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.getInternalName()).thenReturn("aBCClass");

    final Answer<?> a = (new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) {
        return ABCClassification.class;
      }
    });
    when(attribute.getType()).thenAnswer(a);
    when(attribute.getProperty()).thenReturn(edmProperty);
    when(attribute.isEnum()).thenReturn(true);
    when(propertyID.getValueType()).thenReturn(ValueType.ENUM);
    when(propertyID.getName()).thenReturn("ABCClass");
    when(propertyID.getValue()).thenReturn(1);
    odataProperties.add(propertyID);

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals(ABCClassification.B, act.get("aBCClass"));
  }

  @Test
  void testConvertPropertiesOnePrimitiveProperty() throws ODataJPAProcessException, ODataJPAModelException {
    final List<Property> odataProperties = new ArrayList<>();
    final JPAStructuredType st = mock(JPAStructuredType.class);
    final Property propertyID = mock(Property.class);
    final JPAAttribute attribute = mock(JPAAttribute.class);
    final JPAPath path = mock(JPAPath.class);
    final CsdlProperty edmProperty = mock(CsdlProperty.class);

    when(st.getPath("ID")).thenReturn(path);
    when(path.getLeaf()).thenReturn(attribute);
    when(attribute.getInternalName()).thenReturn("iD");

    final Answer<?> a = (new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) {
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

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("35", act.get("iD"));
  }

  @Test
  void testConvertPropertiesOneSimpleCollcetionProperty() throws ODataJPAProcessException,
      ODataJPAModelException {

    final List<Property> odataProperties = new ArrayList<>();
    final List<String> odataComment = new ArrayList<>();

    final JPAStructuredType st = createMetadataForSimpleProperty(COMMENT_EXT_PROPERTY_NAME, COMMENT_INT_PROPERTY_NAME);

    odataComment.add("First Test");
    final Property propertyComment = mock(Property.class);
    when(propertyComment.getValueType()).thenReturn(ValueType.COLLECTION_PRIMITIVE);
    when(propertyComment.getName()).thenReturn(COMMENT_EXT_PROPERTY_NAME);
    when(propertyComment.getValue()).thenReturn(odataComment);
    odataProperties.add(propertyComment);

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);
    assertNotNull(act.get(COMMENT_INT_PROPERTY_NAME));
    assertEquals(1, ((List<?>) act.get(COMMENT_INT_PROPERTY_NAME)).size());
    assertEquals("First Test", ((List<?>) act.get(COMMENT_INT_PROPERTY_NAME)).get(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testConvertPropertiesTwoComplexCollcetionProperty() throws ODataJPAProcessException,
      ODataJPAModelException {

    final List<Property> odataProperties = new ArrayList<>();
    final List<ComplexValue> odataComment = new ArrayList<>();
    final JPAStructuredType st = createMetadataForSimpleProperty("Address", "address");
    final JPAStructuredType nb = createMetadataForSimpleProperty("Number", "number");
    final JPAAttribute attributeAddress = mock(JPAAttribute.class);
    when(attributeAddress.getStructuredType()).thenReturn(nb);
    when(st.getAttribute("address")).thenReturn(Optional.of(attributeAddress));

    List<Property> addressProperties = new ArrayList<>();
    final ComplexValue cv1 = mock(ComplexValue.class);
    Property propertyNumber = mock(Property.class);
    when(propertyNumber.getValueType()).thenReturn(ValueType.PRIMITIVE);
    when(propertyNumber.getName()).thenReturn("Number");
    when(propertyNumber.getValue()).thenReturn(32);
    addressProperties.add(propertyNumber);
    when(cv1.getValue()).thenReturn(addressProperties);

    addressProperties = new ArrayList<>();
    final ComplexValue cv2 = mock(ComplexValue.class);
    propertyNumber = mock(Property.class);
    when(propertyNumber.getValueType()).thenReturn(ValueType.PRIMITIVE);
    when(propertyNumber.getName()).thenReturn("Number");
    when(propertyNumber.getValue()).thenReturn(16);
    addressProperties.add(propertyNumber);
    when(cv2.getValue()).thenReturn(addressProperties);

    odataComment.add(cv1);
    odataComment.add(cv2);
    final Property propertyAddress = mock(Property.class);
    when(propertyAddress.getValueType()).thenReturn(ValueType.COLLECTION_COMPLEX);
    when(propertyAddress.getName()).thenReturn("Address");
    when(propertyAddress.getValue()).thenReturn(odataComment);
    odataProperties.add(propertyAddress);

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);
    assertNotNull(act.get("address"));
    assertEquals(2, ((List<Map<String, Object>>) act.get("address")).size());
    final Map<String, Object> actAddr1 = (Map<String, Object>) ((List<?>) act.get("address")).get(0);
    assertEquals(32, actAddr1.get("number"));

    final Map<String, Object> actAddr2 = (Map<String, Object>) ((List<?>) act.get("address")).get(1);
    assertEquals(16, actAddr2.get("number"));
  }

  @Test
  void testConvertPropertiesTwoSimpleCollcetionProperty() throws ODataJPAProcessException,
      ODataJPAModelException {

    final List<Property> odataProperties = new ArrayList<>();
    final List<String> odataComment = new ArrayList<>();

    final JPAStructuredType st = createMetadataForSimpleProperty(COMMENT_EXT_PROPERTY_NAME, COMMENT_INT_PROPERTY_NAME);

    odataComment.add("First Test");
    odataComment.add("Second Test");
    final Property propertyComment = mock(Property.class);
    when(propertyComment.getValueType()).thenReturn(ValueType.COLLECTION_PRIMITIVE);
    when(propertyComment.getName()).thenReturn(COMMENT_EXT_PROPERTY_NAME);
    when(propertyComment.getValue()).thenReturn(odataComment);
    odataProperties.add(propertyComment);

    final Map<String, Object> act = cut.convertProperties(OData.newInstance(), st, odataProperties);
    assertNotNull(act.get(COMMENT_INT_PROPERTY_NAME));
    assertEquals(2, ((List<?>) act.get(COMMENT_INT_PROPERTY_NAME)).size());
    assertEquals("First Test", ((List<?>) act.get(COMMENT_INT_PROPERTY_NAME)).get(0));
    assertEquals("Second Test", ((List<?>) act.get(COMMENT_INT_PROPERTY_NAME)).get(1));
  }

  @Test
  void testConvertPropertiesUnknownValueType() {
    final List<Property> odataProperties = new ArrayList<>();
    final JPAStructuredType st = mock(JPAStructuredType.class);
    final Property propertyID = mock(Property.class);

    when(propertyID.getValueType()).thenReturn(ValueType.COLLECTION_ENTITY);
    when(propertyID.getName()).thenReturn("ID");
    when(propertyID.getValue()).thenReturn("35");
    odataProperties.add(propertyID);

    try {
      cut.convertProperties(OData.newInstance(), st, odataProperties);
    } catch (final ODataJPAProcessException e) {
      assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Disabled("Different Instances")
  @Test
  void testDifferentInstanceWhenReadingDifferentInstance() throws ODataJPAProcessorException {

    final Map<String, Object> exp = cut.buildGetterMap(new BusinessPartnerRole("100", "A"));
    final Map<String, Object> act = cut.buildGetterMap(new BusinessPartnerRole("100", "A"));

    assertEquals(exp, act);
  }

  @Test
  void testInstanceNull() {

    try {
      cut.buildGetterMap(null);
    } catch (final ODataJPAProcessorException e) {
      assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), e.getStatusCode());
      return;
    }
    fail();
  }

  @Test
  void testInstanceWithGetter() throws ODataJPAProcessorException {
    final BusinessPartnerRole role = new BusinessPartnerRole();
    role.setBusinessPartnerID("ID");

    final Map<String, Object> act = cut.buildGetterMap(role);
    assertNotNull(act);
    assertEquals(5, act.size());
    assertEquals("ID", act.get("businessPartnerID"));
  }

  @Test
  void testInstanceWithoutGetter() throws ODataJPAProcessorException {

    final Map<String, Object> act = cut.buildGetterMap(new DateConverter());
    assertNotNull(act);
    assertEquals(1, act.size());
    assertNotNull(act.get("class"));
  }

  @Test
  void testSameInstanceWhenReadingTwice() throws ODataJPAProcessorException {
    final BusinessPartnerRole role = new BusinessPartnerRole();

    final Map<String, Object> exp = cut.buildGetterMap(role);
    final Map<String, Object> act = cut.buildGetterMap(role);

    assertEquals(exp, act);
  }

  private JPAStructuredType createMetadataForSimpleProperty(final String externalName, final String internalName)
      throws ODataJPAModelException {
    final JPAStructuredType st = mock(JPAStructuredType.class);
    final JPAAttribute attribute = mock(JPAAttribute.class);
    final JPAPath pathID = mock(JPAPath.class);
    final List<JPAElement> pathElements = new ArrayList<>();
    pathElements.add(attribute);
    when(st.getPath(externalName)).thenReturn(pathID);
    when(pathID.getLeaf()).thenReturn(attribute);
    when(pathID.getPath()).thenReturn(pathElements);
    when(attribute.getInternalName()).thenReturn(internalName);
    return st;
  }

  private EdmProperty createPropertyMock(final String propertyName, final EdmPrimitiveTypeKind propertyType,
      final Class<?> defaultJavaType, final Object value) throws EdmPrimitiveTypeException {

    final EdmProperty edmProperty = mock(EdmProperty.class);
    final EdmPrimitiveType edmType = mock(EdmPrimitiveType.class);
    when(edmType.getFullQualifiedName()).thenReturn(propertyType.getFullQualifiedName());
    when(edmType.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);
    when(edmType.getName()).thenReturn(propertyType.getFullQualifiedName().getName());
    when(edmType.getDefaultType()).thenAnswer(new Answer<Class<?>>() {
      @Override
      public Class<?> answer(final InvocationOnMock invocation) throws Throwable {
        return defaultJavaType;
      }
    });
    when(edmType.valueOfString(value.toString(), true, 0, 0, 0, true, defaultJavaType)).thenAnswer(
        new Answer<Object>() {
          @Override
          public Object answer(final InvocationOnMock invocation) throws Throwable {
            return value;
          }
        });
    when(edmProperty.getName()).thenReturn(propertyName);
    when(edmProperty.getType()).thenReturn(edmType);
    when(edmProperty.isUnicode()).thenReturn(true);
    when(edmProperty.isPrimitive()).thenReturn(true);
    when(edmProperty.isCollection()).thenReturn(false);
    when(edmProperty.isNullable()).thenReturn(true);
    return edmProperty;
  }

  @SuppressWarnings("unchecked")
  private void prepareEntitySet() throws EdmPrimitiveTypeException {
    final EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
    final EdmEntityType edmEntityType = mock(EdmEntityType.class);
    final EdmProperty edmPropertyId = mock(EdmProperty.class);
    final EdmPrimitiveType edmTypeId = mock(EdmPrimitiveType.class);
    final UriResourceEntitySet uriEs = mock(UriResourceEntitySet.class);

    final FullQualifiedName fqn = new FullQualifiedName("test", "Organisation");
    final FullQualifiedName fqnString = new FullQualifiedName("test", "Organisation");

    final List<String> propertyNames = new ArrayList<>();
    propertyNames.add("ID");

    uriResourceParts.add(uriEs);

    when(uriEs.getEntitySet()).thenReturn(edmEntitySet);
    when(uriEs.getKind()).thenReturn(UriResourceKind.entitySet);
    when(edmTypeId.getFullQualifiedName()).thenReturn(fqnString);
    when(edmTypeId.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);
    when(edmTypeId.getName()).thenReturn("String");
    when(edmTypeId.valueOfString(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyInt(),
        ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean(),
        (Class<String>) ArgumentMatchers.any())).thenReturn("35");

    when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
    when(edmEntityType.getFullQualifiedName()).thenReturn(fqn);
    when(edmEntityType.getPropertyNames()).thenReturn(propertyNames);
    when(edmEntityType.getProperty("ID")).thenReturn(edmPropertyId);
    when(edmPropertyId.getName()).thenReturn("ID");
    when(edmPropertyId.getType()).thenReturn(edmTypeId);
  }

  @SuppressWarnings("unchecked")
  private ODataRequest preparePrimitiveSimpleProperty() throws EdmPrimitiveTypeException {
    final ODataRequest request = mock(ODataRequest.class);

    final EdmEntitySet edmEntitySet = mock(EdmEntitySet.class);
    final EdmEntityType edmEntityType = mock(EdmEntityType.class);
    final EdmProperty edmPropertyName = mock(EdmProperty.class);
    final EdmPrimitiveType edmTypeName = mock(EdmPrimitiveType.class);
    final UriResourceEntitySet uriEs = mock(UriResourceEntitySet.class);
    final UriResourceProperty uriProperty = mock(UriResourceProperty.class);
    final FullQualifiedName fqn = new FullQualifiedName("test", "Organisation");
    final FullQualifiedName fqnString = new FullQualifiedName("test", "Organisation");

    final List<String> propertyNames = new ArrayList<>();
    propertyNames.add("Name2");

    uriResourceParts.add(uriEs);
    uriResourceParts.add(uriProperty);

    when(uriEs.getEntitySet()).thenReturn(edmEntitySet);
    when(uriEs.getKind()).thenReturn(UriResourceKind.entitySet);

    when(uriProperty.getProperty()).thenReturn(edmPropertyName);
    when(uriProperty.getKind()).thenReturn(UriResourceKind.primitiveProperty);

    when(edmTypeName.getFullQualifiedName()).thenReturn(fqnString);
    when(edmTypeName.getKind()).thenReturn(EdmTypeKind.PRIMITIVE);
    when(edmTypeName.getName()).thenReturn("String");
    when(edmTypeName.valueOfString(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean(), ArgumentMatchers
        .anyInt(),
        ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean(),
        (Class<String>) ArgumentMatchers.any())).thenReturn("Willi");

    when(edmEntitySet.getEntityType()).thenReturn(edmEntityType);
    when(edmEntityType.getFullQualifiedName()).thenReturn(fqn);
    when(edmEntityType.getPropertyNames()).thenReturn(propertyNames);
    when(edmEntityType.getProperty("Name2")).thenReturn(edmPropertyName);
    when(edmPropertyName.getName()).thenReturn("Name2");
    when(edmPropertyName.getType()).thenReturn(edmTypeName);
    return request;
  }
}
