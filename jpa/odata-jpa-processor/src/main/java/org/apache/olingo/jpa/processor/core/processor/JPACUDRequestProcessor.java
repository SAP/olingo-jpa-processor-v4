package org.apache.olingo.jpa.processor.core.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys;
import org.apache.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import org.apache.olingo.jpa.processor.core.modify.JPACUDRequestHandler;
import org.apache.olingo.jpa.processor.core.modify.JPAConversionHelper;
import org.apache.olingo.jpa.processor.core.modify.JPAEntityResult;
import org.apache.olingo.jpa.processor.core.query.ExpressionUtil;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
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
  private final JPAConversionHelper helper;

  public JPACUDRequestProcessor(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataSessionContextAccess sessionContext, final JPAODataRequestContextAccess requestContext,
      JPAConversionHelper cudHelper)
      throws ODataException {

    super(odata, sessionContext, requestContext);
    this.sessionContext = sessionContext;
    this.serviceMetadata = serviceMetadata;
    this.successStatusCode = HttpStatusCode.CREATED.getStatusCode();
    this.helper = cudHelper;
  }

  public void createEntity(final ODataRequest request, final ODataResponse response, final ContentType requestFormat,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final JPAEntityType et;
    Map<String, Object> jpaAttributes = new HashMap<String, Object>();

    EdmEntitySet edmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
    Entity requestEntity = helper.convertInputStream(odata, request, requestFormat, edmEntitySet);

    try {
      et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySet.getName());
      jpaAttributes = helper.convertProperties(odata, et, requestEntity.getProperties());
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }

    // Create entity
    Object newPOJO = null;
    final boolean activeTransation = em.getTransaction().isActive();
    if (!activeTransation)
      em.getTransaction().begin();
    try {
      // TODO allow already converter Entity as return
      newPOJO = handler.createEntity(et, jpaAttributes, em);
    } catch (ODataJPAProcessException e) {
      throw e;
    } catch (Throwable e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    if (newPOJO.getClass() != et.getTypeClass()) {
      throw new ODataJPAProcessorException(MessageKeys.WRONG_RETURN_TYPE, HttpStatusCode.INTERNAL_SERVER_ERROR, newPOJO
          .getClass().toString(), et.getTypeClass().toString());
    }

    if (!activeTransation)
      em.getTransaction().commit();

    createModificationResponse(request, response, responseFormat, et, edmEntitySet, newPOJO);
  }

  public void updateEntity(ODataRequest request, ODataResponse response, ContentType requestFormat,
      ContentType responseFormat) throws ODataJPAProcessException {
    // TODO Auto-generated method stub

    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final JPAEntityType et;
    Map<String, Object> jpaAttributes = new HashMap<String, Object>();
    EdmEntitySet edmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
    Entity requestEntity = helper.convertInputStream(odata, request, requestFormat, edmEntitySet);

    try {
      et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySet.getName());
      jpaAttributes = helper.convertProperties(odata, et, requestEntity.getProperties());
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
    // Create entity
    Object primaryKey = null;
    final boolean activeTransation = em.getTransaction().isActive();
    if (!activeTransation)
      em.getTransaction().begin();
    try {
      // http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752300
      // 11.4.3 Update an Entity
      // Services SHOULD support PATCH as the preferred means of updating an entity. ... .Services MAY additionally
      // support PUT, but should be aware of the potential for data-loss in round-tripping properties that the client
      // may not know about in advance, such as open or added properties, or properties not specified in metadata.
      // 11.4.4 Upsert an Entity
      primaryKey = handler.updateEntity(et, jpaAttributes, em, request.getMethod());
    } catch (ODataJPAProcessException e) {
      throw e;
    } catch (Throwable e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    if (!activeTransation)
      em.getTransaction().commit();
    // TODO clarify correct response. Till then make a simple response:
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    response.setHeader(HttpHeader.PREFERENCE_APPLIED, "return=minimal");

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
    final boolean activeTransation = em.getTransaction().isActive();
    if (!activeTransation)
      em.getTransaction().begin();
    try {
      handler.deleteEntity(et, jpaKeyPredicates, em);
    } catch (ODataJPAProcessException e) {
      throw e;
    } catch (Throwable e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    if (!activeTransation)
      em.getTransaction().commit();

    // 4. configure the response object
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

  }

  private Entity convertEntity(JPAEntityType et, Object jpaEntity, Map<String, List<String>> headers)
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

  private void createModificationResponse(final ODataRequest request, final ODataResponse response,
      final ContentType responseFormat, final JPAEntityType et, EdmEntitySet edmEntitySet, Object newPOJO)
      throws SerializerException, ODataJPAProcessorException, ODataJPASerializerException {

    // http://docs.oasis-open.org/odata/odata/v4.0/odata-v4.0-part1-protocol.html
    // Create response:
    // 8.3.2 Header Location
    // The Location header MUST be returned in the response from a Create Entity or Create Media Entity request to
    // specify the edit URL, or for read-only entities the read URL, of the created entity, and in responses returning
    // 202 Accepted to specify the URL that the client can use to request the status of an asynchronous request.
    //
    // 8.3.3 Header OData-EntityId
    // A response to a create or upsert operation that returns 204 No Content MUST include an OData-EntityId response
    // header. The value of the header is the entity-id of the entity that was acted on by the request. The syntax of
    // the OData-EntityId header is specified in [OData-ABNF].
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
    //
    // 11.4.1.5 Returning Results from Data Modification Requests
    // Clients can request whether created or modified resources are returned from create, update, and upsert operations
    // using the return preference header. In the absence of such a header, services SHOULD return the created or
    // modified content unless the resource is a stream property value.
    // When returning content other than for an update to a media entity stream, services MUST return the same content
    // as a subsequent request to retrieve the same resource. For updating media entity streams, the content of a
    // non-empty response body MUST be the updated media entity.
    //
    // 11.4.2 Create an Entity
    // Upon successful completion, the response MUST contain a Location header that contains the edit URL or read URL of
    // the created entity.
    //
    Preferences prefer = odata.createPreferences(request.getHeaders(HttpHeader.PREFER));
    // TODO Stream properties
    String location = helper.convertKeyToLocal(odata, request, edmEntitySet, et, newPOJO);
    if (prefer.getReturn() == Return.MINIMAL) {
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
      response.setHeader(HttpHeader.PREFERENCE_APPLIED, "return=minimal");
      response.setHeader(HttpHeader.LOCATION, location);
      response.setHeader(HttpHeader.ODATA_ENTITY_ID, location);
    } else {
//      Object newEntity = em.find(et.getTypeClass(), primaryKey);
//      em.refresh(newEntity);
      Entity createdEntity = convertEntity(et, newPOJO, request.getAllHeaders());
      EntityCollection entities = new EntityCollection();
      entities.getEntities().add(createdEntity);
      createSuccessResonce(response, responseFormat, serializer.serialize(request, entities));
      response.setHeader(HttpHeader.LOCATION, location);
    }
  }

}
