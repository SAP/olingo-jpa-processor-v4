package com.sap.olingo.jpa.processor.core.serializer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;

import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

public final class JPASerializerFactory {
  private final ServiceMetadata serviceMetadata;
  private final OData odata;
  private final UriHelper uriHelper;
  private final JPAODataCRUDContextAccess serviceContext;

  public JPASerializerFactory(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataCRUDContextAccess serviceContext) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = odata.createUriHelper();
    this.serviceContext = serviceContext;
  }

  public JPASerializer createCUDSerializer(final ContentType responseFormat, final UriInfo uriInfo,
      final Optional<List<String>> responseVersion) throws SerializerException {
    final ODataSerializer serializer = odata.createSerializer(responseFormat,
        responseVersion.orElse(Collections.emptyList()));
    return new JPASerializeCreate(serviceMetadata, serializer, uriInfo, serviceContext);
  }

  public JPASerializer createSerializer(final ContentType responseFormat, final UriInfo uriInfo,
      final Optional<List<String>> responseVersion) throws ODataApplicationException, SerializerException {

    // Assumption: Type of last resource path item rules the type of the response
    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final UriResource lastItem = resourceParts.get(resourceParts.size() - 1);
    final boolean isColletion = determineIsCollection(lastItem);

    return createSerializer(responseFormat, uriInfo, lastItem.getKind(), isColletion, responseVersion);
  }

  public ServiceMetadata getServiceMetadata() {
    return serviceMetadata;
  }

  JPASerializer createSerializer(final ContentType responseFormat, final UriInfo uriInfo, final EdmTypeKind edmTypeKind,
      final boolean isColletion, final Optional<List<String>> responseVersion) throws SerializerException,
      ODataJPASerializerException {

    final ODataSerializer serializer = odata.createSerializer(responseFormat,
        responseVersion.orElse(Collections.emptyList()));
    switch (edmTypeKind) {
      case ENTITY:
        if (isColletion)
          return new JPASerializeEntityCollection(serviceMetadata, serializer, uriHelper, uriInfo, responseFormat,
              serviceContext);
        else
          return new JPASerializeEntity(serviceMetadata, serializer, uriHelper, uriInfo, responseFormat,
              serviceContext);
      case COMPLEX:
        if (isColletion)
          return new JPASerializeComplexCollection(serviceMetadata, serializer, responseFormat, serviceContext);
        else
          return new JPASerializeComplex(serviceMetadata, serializer, uriHelper, uriInfo, responseFormat,
              serviceContext);
      case PRIMITIVE:
        if (isColletion)
          return new JPASerializePrimitiveCollection(serviceMetadata, serializer, responseFormat, serviceContext);
        else
          return new JPASerializePrimitive(serviceMetadata, serializer, uriInfo, responseFormat, serviceContext);
      default:
        throw new ODataJPASerializerException(ODataJPASerializerException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE,
            HttpStatusCode.NOT_IMPLEMENTED, edmTypeKind.toString());
    }
  }

  JPASerializer createSerializer(final ContentType responseFormat, final UriInfo uriInfo,
      final UriResourceKind uriResourceKind, final boolean isColletion, final Optional<List<String>> responseVersion)
      throws SerializerException, ODataJPASerializerException {

    switch (uriResourceKind) {
      case entitySet:
      case navigationProperty:
        return createSerializerCollectionRequest(responseFormat, uriInfo, isColletion, responseVersion);
      case complexProperty:
        return createSerializerComplexPropertyRequest(responseFormat, uriInfo, responseVersion);
      case primitiveProperty:
        return createSerializerPrimitivePropertyRequest(responseFormat, uriInfo, responseVersion);
      case action:
      case function:
        return new JPASerializeFunction(uriInfo, responseFormat, this, responseVersion);
      case count:
        return new JPASerializeCount(odata.createFixedFormatSerializer());
      case value:
        return new JPASerializeValue(serviceMetadata, odata.createFixedFormatSerializer(), uriInfo);
      default:
        throw new ODataJPASerializerException(ODataJPASerializerException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE,
            HttpStatusCode.NOT_IMPLEMENTED, uriResourceKind.toString());
    }
  }

  private JPASerializer createSerializerCollectionRequest(final ContentType responseFormat, final UriInfo uriInfo,
      final boolean isColletion, final Optional<List<String>> responseVersion) throws SerializerException {

    final ODataSerializer serializer = odata.createSerializer(responseFormat,
        responseVersion.orElse(Collections.emptyList()));
    if (isColletion)
      return new JPASerializeEntityCollection(serviceMetadata, serializer, uriHelper, uriInfo, responseFormat,
          serviceContext);
    else
      return new JPASerializeEntity(serviceMetadata, serializer, uriHelper, uriInfo, responseFormat, serviceContext);
  }

  private JPASerializer createSerializerComplexPropertyRequest(final ContentType responseFormat, final UriInfo uriInfo,
      final Optional<List<String>> responseVersion) throws SerializerException {

    final ODataSerializer serializer = odata.createSerializer(responseFormat,
        responseVersion.orElse(Collections.emptyList()));
    return new JPASerializeComplex(serviceMetadata, serializer, uriHelper, uriInfo, responseFormat, serviceContext);
  }

  private JPASerializer createSerializerPrimitivePropertyRequest(final ContentType responseFormat,
      final UriInfo uriInfo, final Optional<List<String>> responseVersion) throws SerializerException {

    final ODataSerializer serializer = odata.createSerializer(responseFormat,
        responseVersion.orElse(Collections.emptyList()));
    return new JPASerializePrimitive(serviceMetadata, serializer, uriInfo, responseFormat, serviceContext);
  }

  private boolean determineIsCollection(final UriResource lastItem) {
    if (lastItem instanceof UriResourcePartTyped)
      return ((UriResourcePartTyped) lastItem).isCollection();
    return false;
  }
}
