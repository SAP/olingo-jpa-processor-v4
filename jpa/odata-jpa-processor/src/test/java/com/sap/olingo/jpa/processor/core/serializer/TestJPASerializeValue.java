package com.sap.olingo.jpa.processor.core.serializer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.FixedFormatSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

class TestJPASerializeValue {
  private JPASerializeValue cut;
  private ServiceMetadata serviceMetadata;
  private FixedFormatSerializer serializer;
  private UriInfo uriInfo;

  @BeforeEach
  public void setup() {
    serviceMetadata = mock(ServiceMetadata.class);
    uriInfo = mock(UriInfo.class);
    serializer = mock(FixedFormatSerializer.class);
    cut = new JPASerializeValue(serviceMetadata, serializer, uriInfo);
  }

  @Test
  void testDoesNotProvideNullValue() throws SerializerException, ODataJPASerializerException {

    final EntityCollection results = new EntityCollection();
    final Entity result = new Entity();
    final Property value = new Property();
    final ODataRequest request = new ODataRequest();
    final EdmPrimitiveType valueODataType = mock(EdmPrimitiveType.class);
    final EdmProperty valueODataProperty = mock(EdmProperty.class);
    final EdmProperty addrODataProperty = mock(EdmProperty.class);
    final UriResourceEntitySet orgOData = mock(UriResourceEntitySet.class);
    final UriResourceComplexProperty addrOData = mock(UriResourceComplexProperty.class);
    final UriResourcePrimitiveProperty streetOData = mock(UriResourcePrimitiveProperty.class);
    final UriResourceValue valueOData = mock(UriResourceValue.class);
    final List<UriResource> uriResourceParts = Arrays.asList(orgOData, addrOData, streetOData, valueOData);

    when(streetOData.getType()).thenReturn(valueODataType);
    when(valueODataProperty.getType()).thenReturn(valueODataType);
    when(valueODataProperty.getName()).thenReturn("StreetName");
    when(addrODataProperty.getName()).thenReturn("Address");
    when(streetOData.getProperty()).thenReturn(valueODataProperty);
    when(addrOData.getProperty()).thenReturn(addrODataProperty);
    when(uriInfo.getUriResourceParts()).thenReturn(uriResourceParts);
    value.setValue(ValueType.PRIMITIVE, null);
    value.setName("StreetName");
    result.getProperties().add(value);
    results.getEntities().add(result);
    // [Companies, Address, StreetName, $value]
    // result.getEntities().get(0).getProperties()
    // final EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) uriProperty.getProperty().getType();
    final SerializerResult act = cut.serialize(request, results);
    assertNotNull(act);
    verify(serializer, times(0)).primitiveValue(eq(valueODataType), isNull(), any());
  }
}
