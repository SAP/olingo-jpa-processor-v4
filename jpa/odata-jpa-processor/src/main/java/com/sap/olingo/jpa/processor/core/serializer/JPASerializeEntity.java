package com.sap.olingo.jpa.processor.core.serializer;

import java.net.URISyntaxException;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.query.Utility;

final class JPASerializeEntity implements JPAOperationSerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfoResource uriInfo;
  private final UriHelper uriHelper;
  private final ODataSerializer serializer;
  private final ContentType responseFormat;
  private final JPAODataSessionContextAccess serviceContext;

  JPASerializeEntity(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriHelper uriHelper, final UriInfoResource uriInfo, final ContentType responseFormat,
      final JPAODataSessionContextAccess context) {
    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = uriHelper;
    this.responseFormat = responseFormat;
    this.serviceContext = context;
  }

  @Override
  public ContentType getContentType() {
    return responseFormat;
  }

  @Override
  public SerializerResult serialize(final Annotatable annotatable, final EdmType entityType, final ODataRequest request)
      throws SerializerException, ODataJPASerializerException {

    final JPAEntityCollectionExtension result = (JPAEntityCollectionExtension) annotatable;
    final String selectList = uriHelper.buildContextURLSelectList((EdmEntityType) entityType, uriInfo.getExpandOption(),
        uriInfo.getSelectOption());
    try {
      final ContextURL contextUrl = ContextURL.with()
          .serviceRoot(buildServiceRoot(request, serviceContext))
          .type(entityType)
          .selectList(selectList)
          .build();

      final EntitySerializerOptions options = EntitySerializerOptions.with()
          .contextURL(contextUrl)
          .select(uriInfo.getSelectOption())
          .expand(uriInfo.getExpandOption())
          .build();

      return serializer.entity(serviceMetadata, (EdmEntityType) entityType,
          result.getFirstResult(), options);
    } catch (final URISyntaxException e) {
      throw new ODataJPASerializerException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final JPAEntityCollectionExtension result)
      throws SerializerException, ODataJPASerializerException {

    final EdmBindingTarget targetEdmBindingTarget = Utility.determineBindingTarget(uriInfo.getUriResourceParts());
    final EdmEntityType entityType = targetEdmBindingTarget.getEntityType();
    return serialize((Annotatable) result, entityType, request);
  }
}
