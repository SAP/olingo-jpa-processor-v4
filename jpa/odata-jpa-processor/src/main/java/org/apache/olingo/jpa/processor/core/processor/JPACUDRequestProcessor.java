package org.apache.olingo.jpa.processor.core.processor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPATypeConvertor;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

public class JPACUDRequestProcessor {
  final private OData odata;
  final private JPAODataSessionContextAccess sessionContext;
  final private JPAODataRequestContextAccess requestContext;
  final private ServiceMetadata serviceMetadata;

  public JPACUDRequestProcessor(ServiceMetadata serviceMetadata, OData odata,
      JPAODataSessionContextAccess sessionContext, JPAODataRequestContextAccess requestContext) {
    this.odata = odata;
    this.sessionContext = sessionContext;
    this.requestContext = requestContext;
    this.serviceMetadata = serviceMetadata;
  }

  public void createEntity(final ODataRequest request, ODataResponse response, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final EntityManager em = requestContext.getEntityManager();
    final JPAEntityType et;
    Map<String, Object> jpaAttributes = new HashMap<String, Object>();

    EdmEntitySet edmEntitySet = Util.determineTargetEntitySet(requestContext.getUriInfo().getUriResourceParts());
    EdmEntityType edmEntityType = edmEntitySet.getEntityType();

    InputStream requestInputStream = request.getBody();
    ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
    Entity requestEntity = result.getEntity();

    try {
      et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySet.getName());
      jpaAttributes = convertProperty(et, requestEntity.getProperties());
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }

    Entity createdEntity = null;
    em.getTransaction().begin();
    try {
      createdEntity = handler.createEntity(et, jpaAttributes, em, request.getAllHeaders());
    } catch (ODataJPAProcessException e) {
      throw e;
    } catch (Throwable e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    em.getTransaction().commit();

    // 3. serialize the response (we have to return the created entity)
    ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
    // expand and select currently not supported
    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

    ODataSerializer serializer = this.odata.createSerializer(responseFormat);
    SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);

    // 4. configure the response object
    response.setContent(serializedResponse.getContent());
    response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
  }

  /*
   * 4.4 Addressing References between Entities
   * DELETE http://host/service/Categories(1)/Products/$ref?$id=../../Products(0)
   * DELETE http://host/service/Products(0)/Category/$ref
   */
  public void deleteEntity(final ODataResponse response) throws ODataJPAProcessException {
    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final EntityManager em = requestContext.getEntityManager();
    final JPAEntityType et;
    final Map<String, Object> jpaKeyPredicates = new HashMap<String, Object>();

    // 1. Retrieve the entity set which belongs to the requested entity
    List<UriResource> resourcePaths = requestContext.getUriInfo().getUriResourceParts();
    // Note: only in our example we can assume that the first segment is the EntitySet
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

    // 2. Convert Key from URL to JPA
    try {
      et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySet.getName());
      List<UriParameter> uriKeyPredicates = uriResourceEntitySet.getKeyPredicates();
      for (UriParameter uriParam : uriKeyPredicates) {
        JPAAttribute attribute = et.getPath(uriParam.getName()).getLeaf();
        jpaKeyPredicates.put(attribute.getInternalName(), convertValue(uriParam.getText(), attribute, true));
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }

    // 3. Perform Delete
    em.getTransaction().begin();
    try {
      handler.deleteEntity(et, jpaKeyPredicates, em);
    } catch (ODataJPAProcessException e) {
      throw e;
    } catch (Throwable e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    em.getTransaction().commit();

    // 4. configure the response object
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

  }

  //
  private Map<String, Object> convertProperty(final JPAStructuredType st, List<Property> odataProperties)
      throws ODataJPAModelException, ODataJPAProcessorException {

    final Map<String, Object> jpaAttributes = new HashMap<String, Object>();
    String internalName;
    Object jpaAttribute = null;
    for (Property odataProperty : odataProperties) {
      switch (odataProperty.getValueType()) {
      case COMPLEX:
        JPAPath path = st.getPath(odataProperty.getName());
        internalName = path.getPath().get(0).getInternalName();
        JPAStructuredType a = st.getAttribute(internalName).getStructuredType();

        jpaAttribute = convertProperty(a, ((ComplexValue) odataProperty.getValue()).getValue());
        break;
      case PRIMITIVE:
        final JPAAttribute attribute = st.getPath(odataProperty.getName()).getLeaf();
        internalName = attribute.getInternalName();
        jpaAttribute = convertValue(odataProperty.getValue().toString(), attribute, false);
        break;
      default:
        throw new ODataJPAProcessorException(MessageKeys.NOT_SUPPORTED_PROP_TYPE, HttpStatusCode.NOT_IMPLEMENTED,
            odataProperty.getValueType().name());
      }
      jpaAttributes.put(internalName, jpaAttribute);
    }
    return jpaAttributes;
  }

  private Object convertValue(String value, JPAAttribute attribute, Boolean isKey) throws ODataJPAProcessorException {
    try {

      final CsdlProperty edmProperty = (CsdlProperty) attribute.getProperty();
      final EdmPrimitiveTypeKind edmTypeKind = JPATypeConvertor.convertToEdmSimpleType(attribute.getType());
      final EdmPrimitiveType edmType = odata.createPrimitiveTypeInstance(edmTypeKind);
      if (isKey) {
        value = edmType.fromUriLiteral(value);
      }
      return edmType.valueOfString(value, edmProperty.isNullable(), edmProperty.getMaxLength(),
          edmProperty.getPrecision(), edmProperty.getScale(), true, attribute.getType());

    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    } catch (EdmPrimitiveTypeException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

}
