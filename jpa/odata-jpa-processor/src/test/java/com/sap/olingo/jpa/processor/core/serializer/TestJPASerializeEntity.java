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
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriResource;

import com.sap.olingo.jpa.processor.core.util.matcher.EntitySerializerOptionsMatcher;

public class TestJPASerializeEntity extends TestJPAOperationSerializer {
  @SuppressWarnings("unchecked")
  @Override
  protected EntitySerializerOptionsMatcher createMatcher(final String pattern) {
    return new EntitySerializerOptionsMatcher(pattern);
  }

  @Override
  protected EdmType getType() {
    return edmEt;
  }

  @Override
  protected void initTest(final List<UriResource> resouceParts) {
    annotatable = mock(EntityCollection.class);
    final Entity resultEntity = mock(Entity.class);
    final List<Entity> resultEntities = new ArrayList<>();
    resultEntities.add(resultEntity);
    when(resultEntity.getProperties()).thenReturn(Collections.emptyList());
    when(result.getEntities()).thenReturn(resultEntities);
    when(((EntityCollection) annotatable).getEntities()).thenReturn(resultEntities);
    cut = new JPASerializeEntity(serviceMetadata, serializer, uriHelper, uriInfo, ContentType.APPLICATION_JSON,
        context);
  }

  @Override
  protected void verifySerializerCall(final ODataSerializer serializer, final String pattern)
      throws SerializerException {

    verify(serializer).entity(any(), any(), any(), argThat(createMatcher(pattern)));
  }

}