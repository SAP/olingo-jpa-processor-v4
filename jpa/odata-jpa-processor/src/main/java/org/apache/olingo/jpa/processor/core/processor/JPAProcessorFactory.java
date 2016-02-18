package org.apache.olingo.jpa.processor.core.processor;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.processor.core.api.JPAODataContextAccess;
import org.apache.olingo.jpa.processor.core.serializer.JPASerializerFactory;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;

public class JPAProcessorFactory {
  private final JPAODataContextAccess context;
  private final JPASerializerFactory serializerFactory;
  private final OData odata;

  public JPAProcessorFactory(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataContextAccess context) {
    super();
    this.context = context;
    this.serializerFactory = new JPASerializerFactory(odata, serviceMetadata);
    this.odata = odata;
  }

  public JPARequestProcessor createProcessor(final EntityManager em, final UriInfo uriInfo,
      final ContentType responseFormat)
          throws ODataApplicationException, ODataLibraryException {
    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final UriResource lastItem = resourceParts.get(resourceParts.size() - 1);

    switch (lastItem.getKind()) {
    case count:
      return new JPACountRequestProcessor(odata, context.getEdmProvider().getServiceDocument(), em, uriInfo,
          serializerFactory.createSerializer(responseFormat, uriInfo));
    case function:
      checkFunctionPathSupported(resourceParts);
      return new JPAFunctionRequestProcessor(odata, context, em, uriInfo, serializerFactory.createSerializer(
          responseFormat,
          uriInfo));
    case complexProperty:
    case primitiveProperty:
    case navigationProperty:
    case entitySet:
      checkNavigationPathSupported(resourceParts);
      return new JPANavigationRequestProcessor(odata, context.getEdmProvider().getServiceDocument(), em, uriInfo,
          serializerFactory.createSerializer(
              responseFormat,
              uriInfo));
    default:
      throw new ODataApplicationException("Not implemented",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }
  }

  private void checkFunctionPathSupported(final List<UriResource> resourceParts) throws ODataApplicationException {
    if (resourceParts.size() > 1)
      throw new ODataApplicationException("Functions within a navigation path not supported",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  private void checkNavigationPathSupported(final List<UriResource> resourceParts) throws ODataApplicationException {
    for (final UriResource resourceItem : resourceParts) {
      if (resourceItem.getKind() != UriResourceKind.complexProperty
          && resourceItem.getKind() != UriResourceKind.primitiveProperty
          && resourceItem.getKind() != UriResourceKind.navigationProperty
          && resourceItem.getKind() != UriResourceKind.entitySet)
        throw new ODataApplicationException("Not implemented",
            HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }

  }
}
