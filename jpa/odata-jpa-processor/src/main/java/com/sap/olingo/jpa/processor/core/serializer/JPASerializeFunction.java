package com.sap.olingo.jpa.processor.core.serializer;

import java.util.List;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceFunction;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

final class JPASerializeFunction implements JPAFunctionSerializer {

  private final JPASerializer serializer;

  public JPASerializeFunction(final UriInfo uriInfo, ContentType responseFormat,
      final JPASerializerFactory jpaSerializerFactory) throws ODataJPASerializerException, SerializerException {

    this.serializer = createSerializer(jpaSerializerFactory, responseFormat, uriInfo);
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException, ODataJPASerializerException {
    return serializer.serialize(request, result);
  }

  @Override
  public SerializerResult serialize(ODataRequest request, Object result) throws SerializerException,
      ODataJPASerializerException {
    // TODO Auto-generated method stub
    return null;
  }

  JPASerializer getSerializer() {
    return serializer;
  }

  private JPASerializer createSerializer(final JPASerializerFactory jpaSerializerFactory,
      final ContentType responseFormat,
      final UriInfo uriInfo) throws ODataJPASerializerException, SerializerException {

    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    UriResourceFunction function = (UriResourceFunction) resourceParts.get(resourceParts.size() - 1);
    EdmTypeKind edmTypeKind = function.getFunction().getReturnType().getType().getKind();
    boolean isColletion = function.getFunction().getReturnType().isCollection();
    return jpaSerializerFactory.createSerializer(responseFormat, uriInfo, edmTypeKind, isColletion);
  }

}
