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
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;

import com.sap.olingo.jpa.processor.core.util.matcher.PrimitiveSerializerOptionsMatcher;

public class TestJPASerializePrimitive extends TestJPAOperationSerializer {
  private EdmPrimitiveType edmPT;

  @SuppressWarnings("unchecked")
  @Override
  protected PrimitiveSerializerOptionsMatcher createMatcher(final String pattern) {
    return new PrimitiveSerializerOptionsMatcher(pattern);
  }

  @Override
  protected EdmType getType() {
    return edmPT;
  }

  @Override
  protected void initTest(final List<UriResource> resouceParts) {
    annotatable = mock(Property.class);
    edmPT = mock(EdmPrimitiveType.class);

    final UriResourcePrimitiveProperty uriPT = mock(UriResourcePrimitiveProperty.class);
    final EdmProperty edmProperty = mock(EdmProperty.class);
    when(uriPT.getProperty()).thenReturn(edmProperty);
    when(uriPT.getKind()).thenReturn(UriResourceKind.primitiveProperty);
    when(edmProperty.getName()).thenReturn("name1");
    when(edmProperty.isCollection()).thenReturn(false);
    resouceParts.add(uriPT);

    final Entity resultEntity = mock(Entity.class);
    final List<Entity> resultEntities = new ArrayList<>();
    resultEntities.add(resultEntity);
    when(resultEntity.getProperties()).thenReturn(Collections.emptyList());
    when(result.getEntities()).thenReturn(resultEntities);

    cut = new JPASerializePrimitive(serviceMetadata, serializer, uriInfo, ContentType.APPLICATION_JSON, context);
  }

  @Override
  protected void verifySerializerCall(final ODataSerializer serializer, final String pattern)
      throws SerializerException {

    verify(serializer).primitive(any(), any(), any(), argThat(createMatcher(pattern)));
  }

}
