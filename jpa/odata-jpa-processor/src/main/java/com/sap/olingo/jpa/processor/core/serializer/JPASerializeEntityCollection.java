package com.sap.olingo.jpa.processor.core.serializer;

import java.net.URISyntaxException;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.query.Util;

final class JPASerializeEntityCollection implements JPAOperationSerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfo uriInfo;
  private final UriHelper uriHelper;
  private final ODataSerializer serializer;
  private ContentType responseFormat;
  private final JPAODataCRUDContextAccess serviceContext;

  JPASerializeEntityCollection(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriHelper uriHelper, final UriInfo uriInfo, final ContentType responseFormat,
      final JPAODataCRUDContextAccess serviceContext) {
    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = uriHelper;
    this.responseFormat = responseFormat;
    this.serviceContext = serviceContext;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException, ODataJPASerializerException {

    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());

    final String selectList = uriHelper.buildContextURLSelectList(targetEdmEntitySet.getEntityType(),
        uriInfo.getExpandOption(), uriInfo.getSelectOption());

    ContextURL contextUrl;
    try {
      contextUrl = ContextURL.with()
          .serviceRoot(buildServiceRoot(request, serviceContext))
          .entitySet(targetEdmEntitySet)
          .selectList(selectList)
          .build();
    } catch (URISyntaxException e) {
      throw new ODataJPASerializerException(e, HttpStatusCode.BAD_REQUEST);
    }

    final String id = request.getRawBaseUri() + "/" + targetEdmEntitySet.getEntityType().getName();
    final EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
        .contextURL(contextUrl)
        .id(id)
        .count(uriInfo.getCountOption())
        .select(uriInfo.getSelectOption())
        .expand(uriInfo.getExpandOption())
        .build();

    return serializer.entityCollection(this.serviceMetadata, targetEdmEntitySet.getEntityType(), result, opts);

  }

  @Override
  public SerializerResult serialize(final Annotatable annotatable, final EdmType entityType, final ODataRequest request)
      throws SerializerException, ODataJPASerializerException {

    final EntityCollection result = (EntityCollection) annotatable;
    final String selectList = uriHelper.buildContextURLSelectList((EdmEntityType) entityType, uriInfo.getExpandOption(),
        uriInfo.getSelectOption());

    ContextURL contextUrl;
    try {
      contextUrl = ContextURL.with()
          .serviceRoot(buildServiceRoot(request, serviceContext))
          .asCollection()
          .type(entityType)
          .selectList(selectList)
          .build();
    } catch (URISyntaxException e) {
      throw new ODataJPASerializerException(e, HttpStatusCode.BAD_REQUEST);
    }

    final EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
        .contextURL(contextUrl)
        .select(uriInfo.getSelectOption())
        .expand(uriInfo.getExpandOption())
        .build();

    return serializer.entityCollection(serviceMetadata, (EdmEntityType) entityType, result, options);
  }

  @Override
  public ContentType getContentType() {
    return responseFormat;
  }
}
