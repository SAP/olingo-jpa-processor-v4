package com.sap.olingo.jpa.processor.core.processor;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;

import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializerFactory;

public final class JPAProcessorFactory {
  private final JPAODataSessionContextAccess sessionContext;
  private final JPASerializerFactory serializerFactory;
  private final OData odata;
  private final ServiceMetadata serviceMetadata;

  public JPAProcessorFactory(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataSessionContextAccess context) {
    super();
    this.sessionContext = context;
    this.serializerFactory = new JPASerializerFactory(odata, serviceMetadata);
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  public JPACUDRequestProcessor createCUDRequestProcessor(final EntityManager em, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataException {

    final JPAODataRequestContextAccess requestContext = new JPARequestContext(em, uriInfo, serializerFactory
        .createCUDSerializer(responseFormat, uriInfo));

    return new JPACUDRequestProcessor(odata, serviceMetadata, sessionContext, requestContext,
        new JPAConversionHelper());
  }

  public JPACUDRequestProcessor createCUDRequestProcessor(EntityManager em, UriInfo uriInfo) throws ODataException {

    final JPAODataRequestContextAccess requestContext = new JPARequestContext(em, uriInfo, null);

    return new JPACUDRequestProcessor(odata, serviceMetadata, sessionContext, requestContext,
        new JPAConversionHelper());
  }

  public JPAActionRequestProcessor createActionProcessor(final EntityManager em, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataException {

    final JPAODataRequestContextAccess requestContext = new JPARequestContext(em, uriInfo,
        responseFormat != null ? serializerFactory.createSerializer(responseFormat, uriInfo) : null);

    return new JPAActionRequestProcessor(odata, sessionContext, requestContext);

  }

  public JPARequestProcessor createProcessor(final EntityManager em, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataException {

    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final UriResource lastItem = resourceParts.get(resourceParts.size() - 1);
    final JPAODataRequestContextAccess requestContext = new JPARequestContext(em, uriInfo, serializerFactory
        .createSerializer(responseFormat, uriInfo));

    switch (lastItem.getKind()) {
    case count:
      return new JPACountRequestProcessor(odata, sessionContext, requestContext);
    case function:
      checkFunctionPathSupported(resourceParts);
      return new JPAFunctionRequestProcessor(odata, sessionContext, requestContext);
    case complexProperty:
    case primitiveProperty:
    case navigationProperty:
    case entitySet:
    case value:
      checkNavigationPathSupported(resourceParts);
      return new JPANavigationRequestProcessor(odata, serviceMetadata, sessionContext, requestContext);
    default:
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE,
          HttpStatusCode.NOT_IMPLEMENTED, lastItem.getKind().toString());
    }
  }

  private void checkFunctionPathSupported(final List<UriResource> resourceParts) throws ODataApplicationException {
    if (resourceParts.size() > 1)
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_FUNC_WITH_NAVI,
          HttpStatusCode.NOT_IMPLEMENTED);
  }

  private void checkNavigationPathSupported(final List<UriResource> resourceParts) throws ODataApplicationException {
    for (final UriResource resourceItem : resourceParts) {
      if (resourceItem.getKind() != UriResourceKind.complexProperty
          && resourceItem.getKind() != UriResourceKind.primitiveProperty
          && resourceItem.getKind() != UriResourceKind.navigationProperty
          && resourceItem.getKind() != UriResourceKind.entitySet
          && resourceItem.getKind() != UriResourceKind.value)
        throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE,
            HttpStatusCode.NOT_IMPLEMENTED, resourceItem.getKind().toString());
    }

  }
}
