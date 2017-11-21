package com.sap.olingo.jpa.processor.core.serializer;

import java.util.List;

import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

public final class JPASerializerFactory {
  private final ServiceMetadata serviceMetadata;
  private final OData odata;
  private UriHelper uriHelper;

  public JPASerializerFactory(final OData odata, final ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = odata.createUriHelper();
  }

  public JPASerializer createCUDSerializer(ContentType responseFormat, UriInfo uriInfo) throws SerializerException {
    return new JPASerializeCreate(serviceMetadata, odata.createSerializer(responseFormat), uriInfo);
  }

  public JPASerializer createSerializer(final ContentType responseFormat, final UriInfo uriInfo)
      throws ODataApplicationException, SerializerException {
    // Assumption: Type of last resource path item rules the type of the response
    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final UriResource lastItem = resourceParts.get(resourceParts.size() - 1);
    final boolean isColletion = determineIsCollection(lastItem);

    return createSerializer(responseFormat, uriInfo, lastItem.getKind(), isColletion);
  }

  private boolean determineIsCollection(final UriResource lastItem) {
    if (lastItem instanceof UriResourcePartTyped)
      return ((UriResourcePartTyped) lastItem).isCollection();
    return false;
  }

  public ServiceMetadata getServiceMetadata() {
    return serviceMetadata;
  }

  JPASerializer createSerializer(ContentType responseFormat, UriInfo uriInfo, EdmTypeKind edmTypeKind,
      boolean isColletion) throws SerializerException, ODataJPASerializerException {
    switch (edmTypeKind) {
    case ENTITY:
      if (isColletion)
        return new JPASerializeEntityCollection(serviceMetadata, odata.createSerializer(responseFormat), uriHelper,
            uriInfo, responseFormat);
      else
        return new JPASerializeEntity(serviceMetadata, odata.createSerializer(responseFormat), uriHelper, uriInfo,
            responseFormat);
    case COMPLEX:
      if (isColletion)
        return new JPASerializeComplexCollection(serviceMetadata, odata.createSerializer(responseFormat),
            responseFormat);
      else
        return new JPASerializeComplex(serviceMetadata, odata.createSerializer(responseFormat), uriHelper, uriInfo,
            responseFormat);
    case PRIMITIVE:
      if (isColletion)
        return new JPASerializePrimitiveCollection(serviceMetadata, odata.createSerializer(responseFormat),
            responseFormat);
      else
        return new JPASerializePrimitive(serviceMetadata, odata.createSerializer(responseFormat), uriInfo,
            responseFormat);
    default:
      throw new ODataJPASerializerException(ODataJPASerializerException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE,
          HttpStatusCode.NOT_IMPLEMENTED, edmTypeKind.toString());
    }
  }

  JPASerializer createSerializer(final ContentType responseFormat, final UriInfo uriInfo,
      final UriResourceKind uriResourceKind, boolean isColletion) throws SerializerException,
      ODataJPASerializerException {

    switch (uriResourceKind) {
    case entitySet:
    case navigationProperty:
      if (isColletion)
        return new JPASerializeEntityCollection(serviceMetadata, odata.createSerializer(responseFormat), uriHelper,
            uriInfo, responseFormat);
      else
        return new JPASerializeEntity(serviceMetadata, odata.createSerializer(responseFormat), uriHelper, uriInfo,
            responseFormat);
    case complexProperty:
      return new JPASerializeComplex(serviceMetadata, odata.createSerializer(responseFormat), uriHelper, uriInfo,
          responseFormat);
    case primitiveProperty:
      return new JPASerializePrimitive(serviceMetadata, odata.createSerializer(responseFormat), uriInfo,
          responseFormat);
    case action:
    case function:
      return new JPASerializeFunction(uriInfo, responseFormat, this);
    case count:
      return new JPASerializeCount(odata.createFixedFormatSerializer());
    case value:
      return new JPASerializeValue(serviceMetadata, odata.createFixedFormatSerializer(), uriInfo);
    default:
      throw new ODataJPASerializerException(ODataJPASerializerException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE,
          HttpStatusCode.NOT_IMPLEMENTED, uriResourceKind.toString());
    }
  }
}
