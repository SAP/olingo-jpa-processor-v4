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

    Preferences prefer = odata.createPreferences(request.getHeaders(HttpHeader.PREFER));

    if (prefer.getReturn() == Return.MINIMAL) {
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
      response.setHeader(HttpHeader.PREFERENCE_APPLIED, "return=minimal");
      response.setHeader(HttpHeader.LOCATION, helper.convertKeyToLocal(odata, request, edmEntitySet, et, primaryKey));
    } else {
      Object newEntity = em.find(et.getTypeClass(), primaryKey);
      em.refresh(newEntity);
      Entity createdEntity = convertEntity(et, newEntity, request.getAllHeaders());
      EntityCollection entities = new EntityCollection();
      entities.getEntities().add(createdEntity);
      createSuccessResonce(response, responseFormat, serializer.serialize(request, entities));
      response.setHeader(HttpHeader.LOCATION, helper.convertKeyToLocal(odata, request, edmEntitySet, et, primaryKey));
    }
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
}
