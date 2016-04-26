package org.apache.olingo.jpa.processor.core.serializer;

import java.io.InputStream;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.FixedFormatSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;

class JPASerializeStreamValue implements JPASerializer {
  private final FixedFormatSerializer serializer;
  private final ServiceMetadata serviceMetadata;

  JPASerializeStreamValue(final ServiceMetadata serviceMetadata, final FixedFormatSerializer serializer,
      final UriHelper uriHelper, final UriInfo uriInfo) {

    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException {

    Property property = null;
    Entity et = result.getEntities().get(0);
    EdmEntityType edmEt = serviceMetadata.getEdm().getEntityType(new FullQualifiedName(et.getType()));
    List<EdmKeyPropertyRef> p = edmEt.getKeyPropertyRefs();
    for (final Property item : result.getEntities().get(0).getProperties()) {
      if (!isKey(p, item)) {
        property = item;
        break;
      }
    }

    final InputStream serializerResult = serializer.binary((byte[]) property.getValue());
    return new JPAValueSerializerResult(serializerResult);
  }

  private boolean isKey(List<EdmKeyPropertyRef> keyist, Property item) {
    for (EdmKeyPropertyRef key : keyist) {
      if (key.getName().equals(item.getName()))
        return true;
    }
    return false;
  }
}
