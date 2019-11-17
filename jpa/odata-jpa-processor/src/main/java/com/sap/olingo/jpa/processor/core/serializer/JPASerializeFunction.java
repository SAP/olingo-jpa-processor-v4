package com.sap.olingo.jpa.processor.core.serializer;

import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

final class JPASerializeFunction implements JPAOperationSerializer {
  private final JPAOperationSerializer serializer;

  public JPASerializeFunction(final UriInfo uriInfo, final ContentType responseFormat,
      final JPASerializerFactory jpaSerializerFactory, Optional<List<String>> responseVersion)
      throws ODataJPASerializerException, SerializerException {

    this.serializer = (JPAOperationSerializer) createSerializer(jpaSerializerFactory, responseFormat, uriInfo,
        responseVersion);
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException, ODataJPASerializerException {
    return serializer.serialize(request, result);
  }

  @Override
  public SerializerResult serialize(final Annotatable annotatable, final EdmType entityType)
      throws SerializerException, ODataJPASerializerException {
    return serializer.serialize(annotatable, entityType);
  }

  JPASerializer getSerializer() {
    return serializer;
  }

  private JPASerializer createSerializer(final JPASerializerFactory jpaSerializerFactory,
      final ContentType responseFormat, final UriInfo uriInfo, final Optional<List<String>> responseVersion)
      throws ODataJPASerializerException, SerializerException {

    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final UriResourcePartTyped operation = (UriResourcePartTyped) resourceParts.get(resourceParts.size() - 1);
    final EdmTypeKind edmTypeKind = determineReturnEdmTypeKind(operation);
    return jpaSerializerFactory.createSerializer(responseFormat, uriInfo, edmTypeKind, operation.isCollection(),
        responseVersion);
  }

  private EdmTypeKind determineReturnEdmTypeKind(final UriResourcePartTyped operation) {
    if (operation instanceof UriResourceFunction)
      return ((UriResourceFunction) operation).getFunction().getReturnType().getType().getKind();
    else
      return ((UriResourceAction) operation).getAction().getReturnType().getType().getKind();
  }

  @Override
  public ContentType getContentType() {
    return this.serializer.getContentType();
  }

}
