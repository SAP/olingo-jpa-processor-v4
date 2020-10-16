package com.sap.olingo.jpa.processor.core.serializer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceKind;

import com.sap.olingo.jpa.processor.core.util.matcher.ComplexSerializerOptionsMatcher;

public class TestJPASerializeComplex extends TestJPAOperationSerializer {
  private EdmComplexType edmCT;

  @SuppressWarnings("unchecked")
  @Override
  protected ComplexSerializerOptionsMatcher createMatcher(final String pattern) {
    return new ComplexSerializerOptionsMatcher(pattern);
  }

  @Override
  protected EdmType getType() {
    return edmCT;
  }

  @Override
  protected void initTest(final List<UriResource> resourceParts) {
    annotatable = mock(Property.class);
    edmCT = mock(EdmComplexType.class);
    final UriResourceComplexProperty uriCT = mock(UriResourceComplexProperty.class);
    final EdmProperty edmProperty = mock(EdmProperty.class);
    when(uriCT.getComplexType()).thenReturn(edmCT);
    when(uriCT.getProperty()).thenReturn(edmProperty);
    when(uriCT.getKind()).thenReturn(UriResourceKind.complexProperty);
    when(edmProperty.getName()).thenReturn("InhouseAddress");
    when(edmProperty.isCollection()).thenReturn(false);
    resourceParts.add(uriCT);

    final Entity resultEntity = mock(Entity.class);
    final List<Entity> resultEntities = new ArrayList<>();
    resultEntities.add(resultEntity);
    when(resultEntity.getProperties()).thenReturn(Collections.emptyList());
    when(result.getEntities()).thenReturn(resultEntities);

    cut = new JPASerializeComplex(serviceMetadata, serializer, uriHelper, uriInfo, ContentType.APPLICATION_JSON,
        context);
  }

  @Override
  protected void verifySerializerCall(final ODataSerializer serializer, final String pattern)
      throws SerializerException {

    verify(serializer).complex(any(), any(), any(), argThat(createMatcher(pattern)));
  }

}
