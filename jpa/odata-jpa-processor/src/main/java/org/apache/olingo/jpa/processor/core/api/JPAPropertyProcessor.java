package org.apache.olingo.jpa.processor.core.api;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;

public class JPAPropertyProcessor extends JPAAbstractProcessor implements PrimitiveValueProcessor,
    ComplexProcessor {

  public JPAPropertyProcessor(ServicDocument sd, EntityManager em) {
    super(sd, em);
  }

  @Override
  public void deleteComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deletePrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  @Override
  public void readComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    UriResource uriResource = uriInfo.getUriResourceParts().get(0);
    JPASerializer serializer = factory.createSerializer(responseFormat, uriInfo);

    if (uriResource instanceof UriResourceEntitySet) {
      readEntityInternal(request, response, uriInfo, responseFormat, serializer);
    } else {
      throw new ODataApplicationException("Unsupported resource type", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
          Locale.ENGLISH);
    }
  }

  @Override
  public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    List<UriResource> resources = uriInfo.getUriResourceParts();
    UriResource uriResource = resources.get(0);

    if (uriResource instanceof UriResourceEntitySet) {
      JPASerializer serializer = factory.createSerializer(responseFormat, uriInfo);
      readEntityInternal(request, response, uriInfo, responseFormat, serializer);
    } else {
      throw new ODataApplicationException("Unsupported resource type", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
          Locale.ENGLISH);
    }
  }

  @Override
  public void readPrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  @Override
  public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  @Override
  public void updatePrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  private SerializerResult serializeComplexResult(ODataRequest request, ContentType responseFormat,
      EdmEntitySet targetEdmEntitySet, EntityCollection result, UriInfo uriInfo) throws SerializerException {

    ODataSerializer serializer = odata.createSerializer(responseFormat);
    Property property = result.getEntities().get(0).getProperties().get(0);

    UriResourceProperty uriProperty = Util.determineStartNavigationPath(uriInfo.getUriResourceParts());
    EdmComplexType edmPropertyType = (EdmComplexType) uriProperty.getProperty().getType();

    String selectList = odata.createUriHelper().buildContextURLSelectList(targetEdmEntitySet.getEntityType(),
        null, uriInfo.getSelectOption());

    ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet)
        .navOrPropertyPath(Util.determineProptertyNavigationPath(uriInfo.getUriResourceParts()))
        .selectList(selectList)
        .build();
    ComplexSerializerOptions options = ComplexSerializerOptions.with()
        .contextURL(contextUrl)
        .select(uriInfo.getSelectOption())
        .expand(uriInfo.getExpandOption())
        .build();

    SerializerResult serializerResult = serializer.complex(serviceMetadata, edmPropertyType, property, options);
    return serializerResult;
  }

  private SerializerResult serializePrimitiveResult(ODataRequest request, ContentType responseFormat,
      EdmEntitySet targetEdmEntitySet, EntityCollection result, UriInfo uriInfo) throws SerializerException {

    ODataSerializer serializer = odata.createSerializer(responseFormat);

    Property property = result.getEntities().get(0).getProperties().get(0);

    UriResourceProperty uriProperty = (UriResourceProperty) uriInfo.getUriResourceParts().get(uriInfo
        .getUriResourceParts().size() - 1);
    EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) uriProperty.getProperty().getType();

    ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet)
        .navOrPropertyPath(property.getName())
        .build();
    PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();
    // 3.2. serialize
    SerializerResult serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, property, options);
    return serializerResult;
  }
}
