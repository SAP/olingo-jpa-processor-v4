package org.apache.olingo.jpa.processor.core.processor;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys;
import org.apache.olingo.jpa.processor.core.modify.JPACUDRequestHandler;
import org.apache.olingo.jpa.processor.core.modify.JPAEntityResult;
import org.apache.olingo.jpa.processor.core.query.ExpressionUtil;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.prefer.Preferences;
import org.apache.olingo.server.api.prefer.Preferences.Return;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.org.jpa.processor.core.converter.JPATupleResultConverter;

public class JPACUDRequestProcessor extends JPAAbstractRequestProcessor {

  private final JPAODataSessionContextAccess sessionContext;
  private final ServiceMetadata serviceMetadata;

  public JPACUDRequestProcessor(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataSessionContextAccess sessionContext, final JPAODataRequestContextAccess requestContext)
      throws ODataException {

    super(odata, sessionContext, requestContext);
    this.sessionContext = sessionContext;
    this.serviceMetadata = serviceMetadata;
    this.successStatusCode = HttpStatusCode.CREATED.getStatusCode();
  }

  public void createEntity(final ODataRequest request, final ODataResponse response, final ContentType requestFormat,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final JPAEntityType et;
    Map<String, Object> jpaAttributes = new HashMap<String, Object>();

    EdmEntitySet edmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
    Entity requestEntity = convertInputStream(request, requestFormat, edmEntitySet.getEntityType());

    try {
      et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySet.getName());
      jpaAttributes = convertProperty(et, requestEntity.getProperties());
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }

    // Create entity
    Object primaryKey = null;
    em.getTransaction().begin();
    try {
      primaryKey = handler.createEntity(et, jpaAttributes, em);
    } catch (ODataJPAProcessException e) {
      throw e;
    } catch (Throwable e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    em.getTransaction().commit();

    // Create response
    // 11.4.2 Create an Entity states:
    // Upon successful completion, the response MUST contain a Location header that contains the edit URL or read URL of
    // the created entity.
    //
    // 8.3.2 Header Location
    // The Location header MUST be returned in the response from a Create Entity or Create Media Entity request to
    // specify the edit URL, or for read-only entities the read URL, of the created entity, and in responses returning
    // 202 Accepted to specify the URL that the client can use to request the status of an asynchronous request.
    //
    // 8.2.8.7 Preference return=representation and return=minimal states:
    // A preference of return=minimal requests that the service invoke the request but does not return content in the
    // response. The service MAY apply this preference by returning 204 No Content in which case it MAY include a
    // Preference-Applied response header containing the return=minimal preference.
    // A preference of return=representation requests that the service invokes the request and returns the modified
    // resource. The service MAY apply this preference by returning the representation of the successfully modified
    // resource in the body of the response, formatted according to the rules specified for the requested format. In
    // this case the service MAY include a Preference-Applied response header containing the return=representation
    // preference.

    Preferences prefer = odata.createPreferences(request.getHeaders((HttpHeader.PREFER)));

    if (prefer.getReturn() == Return.MINIMAL) {
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
      response.setHeader(HttpHeader.PREFERENCE_APPLIED, "return=minimal");
      response.setHeader(HttpHeader.LOCATION, convertKeyToLocal(request, edmEntitySet, et, primaryKey));
    } else {
      // TODO Header Location
      Object newEntity = em.find(et.getTypeClass(), primaryKey);
      em.refresh(newEntity);
      Entity createdEntity = convertEntity(et, newEntity, request.getAllHeaders());
      EntityCollection entities = new EntityCollection();
      entities.getEntities().add(createdEntity);
      createSuccessResonce(response, responseFormat, serializer.serialize(request, entities));
    }
  }

  private String convertKeyToLocal(final ODataRequest request, EdmEntitySet edmEntitySet, JPAEntityType et,
      Object primaryKey) throws SerializerException, ODataJPAProcessorException {

    Entity createdEntity = new Entity();

    try {
      final List<JPAPath> keyPath = et.getKeyPath();
      final List<Property> properties = createdEntity.getProperties();

      if (keyPath.size() > 1) {
        final Map<String, Object> getter = buildGetterMap(primaryKey);

        for (JPAPath key : keyPath) {
          final Property property = new Property(null, key.getLeaf().getExternalName());
          property.setValue(ValueType.PRIMITIVE, getter.get(key.getLeaf().getInternalName()));
          properties.add(property);
        }
      } else {
        JPAPath key = keyPath.get(0);
        final Property property = new Property(null, key.getLeaf().getExternalName());
        property.setValue(ValueType.PRIMITIVE, primaryKey);
        properties.add(property);
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }

    final String location = request.getRawBaseUri() + '/'
        + odata.createUriHelper().buildCanonicalURL(edmEntitySet, createdEntity);
    return location;
  }

  /*
   * 4.4 Addressing References between Entities
   * DELETE http://host/service/Categories(1)/Products/$ref?$id=../../Products(0)
   * DELETE http://host/service/Products(0)/Category/$ref
   */
  public void deleteEntity(final ODataResponse response) throws ODataJPAProcessException {
    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final JPAEntityType et;
    final Map<String, Object> jpaKeyPredicates = new HashMap<String, Object>();

    // 1. Retrieve the entity set which belongs to the requested entity
    List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
    // Note: only in our example we can assume that the first segment is the EntitySet
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

    // 2. Convert Key from URL to JPA
    try {
      et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySet.getName());
      List<UriParameter> uriKeyPredicates = uriResourceEntitySet.getKeyPredicates();
      for (UriParameter uriParam : uriKeyPredicates) {
        JPAAttribute attribute = et.getPath(uriParam.getName()).getLeaf();
        jpaKeyPredicates.put(attribute.getInternalName(), ExpressionUtil.convertValueOnAttribute(odata, attribute,
            uriParam.getText(), true));
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

  Entity convertEntity(JPAEntityType et, Object jpaEntity, Map<String, List<String>> headers)
      throws ODataJPAProcessorException {
    JPATupleResultConverter converter;
    try {
      converter = new JPATupleResultConverter(sd, new JPAEntityResult(et, jpaEntity, headers), odata
          .createUriHelper(), serviceMetadata);
      return converter.getResult().getEntities().get(0);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (ODataApplicationException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

  }

  private Entity convertInputStream(final ODataRequest request, final ContentType requestFormat,
      EdmEntityType edmEntityType) throws DeserializerException {
    InputStream requestInputStream = request.getBody();
    ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
    Entity requestEntity = result.getEntity();
    return requestEntity;
  }

  //
  private Map<String, Object> convertProperty(final JPAStructuredType st, List<Property> odataProperties)
      throws ODataJPAModelException, ODataJPAProcessorException, ODataJPAFilterException {

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
        jpaAttribute = ExpressionUtil.convertValueOnAttribute(odata, attribute, odataProperty.getValue().toString(),
            false);
        break;
      default:
        throw new ODataJPAProcessorException(MessageKeys.NOT_SUPPORTED_PROP_TYPE, HttpStatusCode.NOT_IMPLEMENTED,
            odataProperty.getValueType().name());
      }
      jpaAttributes.put(internalName, jpaAttribute);
    }
    return jpaAttributes;
  }

  private Map<String, Object> buildGetterMap(Object instance) throws ODataJPAProcessorException {

    final Map<String, Object> getterMap = new HashMap<String, Object>();
    // TODO Performance: Don't recreate the complete getter list over and over again
    Method[] methods = instance.getClass().getMethods();
    for (Method meth : methods) {
      if (meth.getName().substring(0, 3).equals("get")) {
        String attributeName = meth.getName().substring(3, 4).toLowerCase() + meth.getName().substring(4);
        try {
          Object value = meth.invoke(instance);
          getterMap.put(attributeName, value);
        } catch (IllegalAccessException e) {
          throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException e) {
          throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        } catch (InvocationTargetException e) {
          throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      }
    }
    return getterMap;
  }
}
