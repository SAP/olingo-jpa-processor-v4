package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.converter.JPAExpandResult.ROOT_RESULT_KEY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
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
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.UriResourceValue;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPACUDRequestHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;
import com.sap.olingo.jpa.processor.core.modify.JPACreateResultFactory;
import com.sap.olingo.jpa.processor.core.modify.JPAUpdateResult;
import com.sap.olingo.jpa.processor.core.query.EdmEntitySetInfo;
import com.sap.olingo.jpa.processor.core.query.ExpressionUtil;
import com.sap.olingo.jpa.processor.core.query.Util;

public final class JPACUDRequestProcessor extends JPAAbstractRequestProcessor {

  private static final String DEBUG_CREATE_ENTITY = "createEntity";
  private static final String DEBUG_UPDATE_ENTITY = "updateEntity";
  private final ServiceMetadata serviceMetadata;
  private final JPAConversionHelper helper;

  public JPACUDRequestProcessor(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataSessionContextAccess sessionContext, final JPAODataRequestContextAccess requestContext,
      JPAConversionHelper cudHelper) throws ODataException {

    super(odata, sessionContext, requestContext);
    this.serviceMetadata = serviceMetadata;
    this.helper = cudHelper;
  }

  public void clearFields(final ODataRequest request, ODataResponse response) throws ODataJPAProcessException {

    final int handle = debugger.startRuntimeMeasurement(this, "clearFields");
    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final EdmEntitySetInfo edmEntitySetInfo = Util.determineTargetEntitySetAndKeys(uriInfo.getUriResourceParts());

    final JPARequestEntity requestEntity = createRequestEntity(edmEntitySetInfo, uriInfo.getUriResourceParts(), request
        .getAllHeaders());
    final boolean foreignTransation = em.getTransaction().isActive();

    if (!foreignTransation)
      em.getTransaction().begin();
    try {
      final int updateHandle = debugger.startRuntimeMeasurement(handler, DEBUG_UPDATE_ENTITY);
      handler.updateEntity(requestEntity, em, request.getMethod());
      if (!foreignTransation)
        handler.validateChanges(em);
      debugger.stopRuntimeMeasurement(updateHandle);
    } catch (ODataJPAProcessException e) {
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    } catch (Exception e) {
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    if (!foreignTransation)
      em.getTransaction().commit();
    debugger.stopRuntimeMeasurement(handle);
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
  }

  public void createEntity(final ODataRequest request, final ODataResponse response, final ContentType requestFormat,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    final int handle = debugger.startRuntimeMeasurement(this, DEBUG_CREATE_ENTITY);
    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();

    final EdmEntitySetInfo edmEntitySetInfo = Util.determineModifyEntitySetAndKeys(uriInfo.getUriResourceParts());
    final Entity odataEntity = helper.convertInputStream(odata, request, requestFormat, edmEntitySetInfo);

    final JPARequestEntity requestEntity = createRequestEntity(edmEntitySetInfo, odataEntity, request.getAllHeaders());

    // Create entity
    Object result = null;
    final boolean foreignTransation = em.getTransaction().isActive();
    if (!foreignTransation)
      em.getTransaction().begin();
    try {
      final int createHandle = debugger.startRuntimeMeasurement(handler, DEBUG_CREATE_ENTITY);
      result = handler.createEntity(requestEntity, em);
      if (!foreignTransation)
        handler.validateChanges(em);
      debugger.stopRuntimeMeasurement(createHandle);
    } catch (ODataJPAProcessException e) {
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    } catch (Exception e) {
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    if (result != null && result.getClass() != requestEntity.getEntityType().getTypeClass()
        && !(result instanceof Map<?, ?>)) {
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(MessageKeys.WRONG_RETURN_TYPE, HttpStatusCode.INTERNAL_SERVER_ERROR, result
          .getClass().toString(), requestEntity.getEntityType().getTypeClass().toString());
    }

    if (!foreignTransation)
      em.getTransaction().commit();

    createCreateResponse(request, response, responseFormat, requestEntity, edmEntitySetInfo, result);
    debugger.stopRuntimeMeasurement(handle);
  }

  /*
   * 4.4 Addressing References between Entities
   * DELETE http://host/service/Categories(1)/Products/$ref?$id=../../Products(0)
   * DELETE http://host/service/Products(0)/Category/$ref
   */
  public void deleteEntity(final ODataRequest request, final ODataResponse response) throws ODataJPAProcessException {
    final int handle = debugger.startRuntimeMeasurement(this, "deleteEntity");
    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final JPAEntityType et;
    final Map<String, Object> jpaKeyPredicates = new HashMap<>();

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
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
    final JPARequestEntity requestEntity = createRequestEntity(et, jpaKeyPredicates, request.getAllHeaders());

    // 3. Perform Delete
    final boolean foreignTransation = em.getTransaction().isActive();
    if (!foreignTransation)
      em.getTransaction().begin();
    try {
      final int deleteHandle = debugger.startRuntimeMeasurement(handler, "deleteEntity");
      handler.deleteEntity(requestEntity, em);
      if (!foreignTransation)
        handler.validateChanges(em);
      debugger.stopRuntimeMeasurement(deleteHandle);
    } catch (ODataJPAProcessException e) {
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    } catch (Throwable e) {
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    if (!foreignTransation)
      em.getTransaction().commit();

    // 4. configure the response object
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    debugger.stopRuntimeMeasurement(handle);
  }

  public void updateEntity(final ODataRequest request, final ODataResponse response, final ContentType requestFormat,
      final ContentType responseFormat) throws ODataJPAProcessException, ODataLibraryException {

    final int handle = debugger.startRuntimeMeasurement(this, DEBUG_UPDATE_ENTITY);
    final JPACUDRequestHandler handler = sessionContext.getCUDRequestHandler();
    final EdmEntitySetInfo edmEntitySetInfo = Util.determineModifyEntitySetAndKeys(uriInfo.getUriResourceParts());
    final Entity odataEntity = helper.convertInputStream(odata, request, requestFormat, edmEntitySetInfo);
    // http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752300
    // 11.4.3 Update an Entity
    // ...
    // The entity MUST NOT contain related entities as inline content. It MAY contain binding information for
    // navigation properties. For single-valued navigation properties this replaces the relationship. For
    // collection-valued navigation properties this adds to the relationship.
    // TODO navigation properties this replaces the relationship
    final JPARequestEntity requestEntity = createRequestEntity(edmEntitySetInfo, odataEntity, request.getAllHeaders());

    // Update entity
    JPAUpdateResult updateResult = null;

    final boolean foreignTransation = em.getTransaction().isActive();
    if (!foreignTransation)
      em.getTransaction().begin();
    try {
      // http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752300
      // 11.4.3 Update an Entity
      // Services SHOULD support PATCH as the preferred means of updating an entity. ... .Services MAY additionally
      // support PUT, but should be aware of the potential for data-loss in round-tripping properties that the client
      // may not know about in advance, such as open or added properties, or properties not specified in metadata.
      // 11.4.4 Upsert an Entity
      // To ensure that an update request is not treated as an insert, the client MAY specify an If-Match header in the
      // update request. The service MUST NOT treat an update request containing an If-Match header as an insert.
      // A PUT or PATCH request MUST NOT be treated as an update if an If-None-Match header is specified with a value of
      // "*".
      final int updateHandle = debugger.startRuntimeMeasurement(handler, DEBUG_UPDATE_ENTITY);
      updateResult = handler.updateEntity(requestEntity, em, request.getMethod());
      if (!foreignTransation)
        handler.validateChanges(em);
      debugger.stopRuntimeMeasurement(updateHandle);
    } catch (ODataJPAProcessException e) {
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    } catch (Throwable e) {
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    if (updateResult == null) {
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(MessageKeys.RETURN_NULL, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    if (updateResult.getModifyedEntity() != null && !requestEntity.getEntityType().getTypeClass().isInstance(
        updateResult.getModifyedEntity())) {
      // This shall tolerate that e.g. EclipseLink return at least in case of InheritanceType.TABLE_PER_CLASS an
      // instance of a sub class even so the super class was requested.
      if (!foreignTransation)
        em.getTransaction().rollback();
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(MessageKeys.WRONG_RETURN_TYPE, HttpStatusCode.INTERNAL_SERVER_ERROR,
          updateResult.getModifyedEntity().getClass().toString(), requestEntity.getEntityType().getTypeClass()
              .toString());
    }
    if (!foreignTransation)
      em.getTransaction().commit();

    if (updateResult.wasCreate()) {
      createCreateResponse(request, response, responseFormat, requestEntity.getEntityType(), edmEntitySetInfo
          .getEdmEntitySet(), updateResult.getModifyedEntity());
      debugger.stopRuntimeMeasurement(handle);
    } else {
      createUpdateResponse(request, response, responseFormat, requestEntity, edmEntitySetInfo, updateResult);
      debugger.stopRuntimeMeasurement(handle);
    }

  }

  final JPARequestEntity createRequestEntity(final EdmEntitySet edmEntitySet, final Entity odataEntity,
      final Map<String, List<String>> headers) throws ODataJPAProcessorException {

    try {
      final JPAEntityType et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySet.getName());
      return createRequestEntity(et, odataEntity, new HashMap<String, Object>(0), headers, null);
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  final JPARequestEntity createRequestEntity(final EdmEntitySetInfo edmEntitySetInfo, final Entity odataEntity,
      final Map<String, List<String>> headers) throws ODataJPAProcessorException {

    try {
      final JPAEntityType et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySetInfo
          .getName());
      final Map<String, Object> keys = helper.convertUriKeys(odata, et, edmEntitySetInfo.getKeyPredicates());
      return createRequestEntity(et, odataEntity, keys, headers, et.getAssociationPath(edmEntitySetInfo
          .getNavigationPath()));
    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  /**
   * Converts the deserialized request into the internal (JPA) format, which shall be provided to the hook method
   * @param edmEntitySet
   * @param odataEntity
   * @param headers
   * @return
   * @throws ODataJPAProcessorException
   */
  final JPARequestEntity createRequestEntity(final JPAEntityType et, final Entity odataEntity,
      final Map<String, Object> keys, final Map<String, List<String>> headers,
      final JPAAssociationPath jpaAssociationPath) throws ODataJPAProcessorException {

    try {
      if (jpaAssociationPath == null) {
        final Map<String, Object> jpaAttributes = helper.convertProperties(odata, et, odataEntity.getProperties());
        final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities = createInlineEntities(et, odataEntity,
            headers);
        final Map<JPAAssociationPath, List<JPARequestLink>> relationLinks = createRelationLinks(et, odataEntity);
        return new JPARequestEntityImpl(et, jpaAttributes, relatedEntities, relationLinks, keys, headers);
      } else {
        // Handle requests like POST
        // .../AdministrativeDivisions(DivisionCode='DE6',CodeID='NUTS1',CodePublisher='Eurostat')/Children
        final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities = createInlineEntities(odataEntity,
            jpaAssociationPath, headers);
        return new JPARequestEntityImpl(et, new HashMap<>(0), relatedEntities, new HashMap<>(1), keys, headers);
      }

    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  /**
   * Create an RequestEntity instance for delete requests
   * @param et
   * @param keys
   * @param headers
   * @return
   */
  final JPARequestEntity createRequestEntity(final JPAEntityType et, final Map<String, Object> keys,
      final Map<String, List<String>> headers) {

    final Map<String, Object> jpaAttributes = new HashMap<>(0);
    final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities = new HashMap<>(0);
    final Map<JPAAssociationPath, List<JPARequestLink>> relationLinks = new HashMap<>(0);

    return new JPARequestEntityImpl(et, jpaAttributes, relatedEntities, relationLinks, keys, headers);
  }

  private Entity convertEntity(JPAEntityType et, Object result, Map<String, List<String>> headers)
      throws ODataJPAProcessorException {

    try {
      final JPATupleChildConverter converter = new JPATupleChildConverter(sd, odata.createUriHelper(), serviceMetadata);
      final JPACreateResultFactory factory = new JPACreateResultFactory(converter);//
      return converter.getResult(factory.getJPACreateResult(et, result, headers)).get(ROOT_RESULT_KEY).getEntities()
          .get(0);
    } catch (ODataJPAModelException | ODataApplicationException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

  }

  private Map<String, Object> convertUriPath(JPAEntityType et, final List<UriResource> resourcePaths)
      throws ODataJPAModelException {

    final Map<String, Object> jpaAttributes = new HashMap<>();
    Map<String, Object> currentMap = jpaAttributes;
    JPAStructuredType st = et;
    int lastIndex;

    if (resourcePaths.get(resourcePaths.size() - 1) instanceof UriResourceValue)
      lastIndex = resourcePaths.size() - 1;
    else
      lastIndex = resourcePaths.size();

    for (int i = 1; i < lastIndex; i++) {
      final UriResourceProperty uriResourceProperty = (UriResourceProperty) resourcePaths.get(i);
      if (uriResourceProperty instanceof UriResourceComplexProperty && i < resourcePaths.size() - 1) {
        final Map<String, Object> jpaEmbedded = new HashMap<>();
        final JPAPath path = st.getPath(uriResourceProperty.getProperty().getName());
        final String internalName = path.getPath().get(0).getInternalName();

        currentMap.put(internalName, jpaEmbedded);

        currentMap = jpaEmbedded;
        st = st.getAttribute(internalName).getStructuredType();
      } else
        currentMap.put(st.getPath(uriResourceProperty.getProperty().getName()).getLeaf().getInternalName(), null);
    }
    return jpaAttributes;
  }

  private void createCreateResponse(final ODataRequest request, final ODataResponse response,
      final ContentType responseFormat, final JPAEntityType et, EdmEntitySet edmEntitySet, final Object result)
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
    successStatusCode = HttpStatusCode.CREATED.getStatusCode();
    Preferences prefer = odata.createPreferences(request.getHeaders(HttpHeader.PREFER));
    // TODO Stream properties

    String location = helper.convertKeyToLocal(odata, request, edmEntitySet, et, result);
    if (prefer.getReturn() == Return.MINIMAL) {
      createMinimalCreateResponce(response, location);
    } else {
      Entity createdEntity = convertEntity(et, result, request.getAllHeaders());
      EntityCollection entities = new EntityCollection();
      entities.getEntities().add(createdEntity);
      createSuccessResponce(response, responseFormat, serializer.serialize(request, entities));
      response.setHeader(HttpHeader.LOCATION, location);
    }
  }

  private void createCreateResponse(final ODataRequest request, final ODataResponse response,
      final ContentType responseFormat, final JPARequestEntity requestEntity, final EdmEntitySetInfo edmEntitySet,
      final Object result) throws SerializerException, ODataJPAProcessorException, ODataJPASerializerException {

    if (!requestEntity.getKeys().isEmpty()) {
      // .../AdministrativeDivisions(DivisionCode='DE5',CodeID='NUTS1',CodePublisher='Eurostat')/Children
      // As of now only one related entity can be created
      try {
        final JPAAssociationPath path = requestEntity.getEntityType().getAssociationPath(edmEntitySet
            .getNavigationPath());

        final JPARequestEntity linkedEntity = requestEntity.getRelatedEntities().get(path).get(0);
        final Object linkedResult = getLinkedResult(result, path);
        createCreateResponse(request, response, responseFormat, linkedEntity.getEntityType(), edmEntitySet
            .getTargetEdmEntitySet(), linkedResult);
      } catch (ODataJPAModelException e) {
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    } else
      createCreateResponse(request, response, responseFormat, requestEntity.getEntityType(), edmEntitySet
          .getEdmEntitySet(), result);
  }

  @SuppressWarnings("unchecked")
  private Object getLinkedResult(Object result, JPAAssociationPath path) throws ODataJPAProcessorException {

    if (result instanceof Map<?, ?>) {
      Map<String, Object> target = (Map<String, Object>) result;
      for (JPAElement pathItem : path.getPath())
        target = (Map<String, Object>) target.get(pathItem.getInternalName());
      return target;
    } else {
      Object value = result;
      for (JPAElement pathItem : path.getPath()) {
        final Map<String, Object> embeddedGetterMap = helper.buildGetterMap(value);
        value = embeddedGetterMap.get(pathItem.getInternalName());
      }
      if (path.getLeaf().isCollection() && value != null) {
        if (((Collection<?>) value).isEmpty())
          value = null;
        else
          value = ((Collection<?>) value).toArray()[0];
      }
      return value;
    }
  }

  private Map<JPAAssociationPath, List<JPARequestEntity>> createInlineEntities(final Entity odataEntity,
      final JPAAssociationPath path, final Map<String, List<String>> headers) throws ODataJPAProcessorException {

    final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities = new HashMap<>(1);
    final List<JPARequestEntity> inlineEntities = new ArrayList<>();

    inlineEntities.add(createRequestEntity((JPAEntityType) path.getTargetType(), odataEntity,
        new HashMap<String, Object>(0), headers, null));

    relatedEntities.put(path, inlineEntities);

    return relatedEntities;
  }

  private Map<JPAAssociationPath, List<JPARequestEntity>> createInlineEntities(JPAEntityType et, Entity odataEntity,
      Map<String, List<String>> headers) throws ODataJPAModelException, ODataJPAProcessorException {

    final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities = new HashMap<>();

    for (JPAAssociationPath path : et.getAssociationPathList()) {
      List<Property> stProperties = odataEntity.getProperties();
      Property p = null;
      for (JPAElement pathItem : path.getPath()) {
        if (pathItem == path.getLeaf()) { // We have reached the target and can process further
          final Link navigationLink = p != null ? p.asComplex().getNavigationLink(pathItem.getExternalName())
              : odataEntity.getNavigationLink(pathItem.getExternalName());
          createInlineEntities((JPAEntityType) path.getTargetType(), headers, relatedEntities, navigationLink, path);
        }
        p = findProperty(pathItem.getExternalName(), stProperties);
        if (p == null) break;
        if (p.isComplex()) {
          stProperties = p.asComplex().getValue();
        }
      }
    }
    return relatedEntities;
  }

  private void createInlineEntities(JPAEntityType st, Map<String, List<String>> headers,
      final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities, Link navigationLink,
      JPAAssociationPath path) throws ODataJPAProcessorException {

    if (navigationLink == null) return;
    final List<JPARequestEntity> inlineEntities = new ArrayList<>();
    if (path.getLeaf().isCollection()) {
      for (Entity e : navigationLink.getInlineEntitySet().getEntities()) {
        inlineEntities.add(createRequestEntity(st, e, new HashMap<String, Object>(0), headers, null));
      }
      relatedEntities.put(path, inlineEntities);
    } else {
      inlineEntities.add(createRequestEntity(st, navigationLink.getInlineEntity(),
          new HashMap<String, Object>(0), headers, null));
      relatedEntities.put(path, inlineEntities);
    }
  }

  private void createMinimalCreateResponce(final ODataResponse response, String location) {
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    response.setHeader(HttpHeader.PREFERENCE_APPLIED, "return=minimal");
    response.setHeader(HttpHeader.LOCATION, location);
    response.setHeader(HttpHeader.ODATA_ENTITY_ID, location);
  }

  private Map<JPAAssociationPath, List<JPARequestLink>> createRelationLinks(JPAEntityType et, Entity odataEntity)
      throws ODataJPAModelException {

    final Map<JPAAssociationPath, List<JPARequestLink>> relationLinks =
        new HashMap<>();
    for (Link binding : odataEntity.getNavigationBindings()) {
      final List<JPARequestLink> bindingLinks = new ArrayList<>();
      JPAAssociationPath path = et.getAssociationPath(binding.getTitle());
      if (path.getLeaf().isCollection()) {
        for (String bindingLink : binding.getBindingLinks()) {
          final JPARequestLink requestLink = new JPARequestLinkImpl(path, bindingLink, helper);
          bindingLinks.add(requestLink);
        }
      } else {
        final JPARequestLink requestLink = new JPARequestLinkImpl(path, binding.getBindingLink(), helper);
        bindingLinks.add(requestLink);
      }
      relationLinks.put(path, bindingLinks);
    }
    return relationLinks;
  }

  private JPARequestEntity createRequestEntity(EdmEntitySetInfo edmEntitySetInfo, List<UriResource> resourceParts,
      Map<String, List<String>> headers) throws ODataJPAProcessorException {

    try {
      final JPAEntityType et = sessionContext.getEdmProvider().getServiceDocument().getEntity(edmEntitySetInfo
          .getEdmEntitySet().getName());
      final Map<String, Object> keys = helper.convertUriKeys(odata, et, edmEntitySetInfo.getKeyPredicates());
      final Map<String, Object> jpaAttributes = convertUriPath(et, resourceParts);

      return new JPARequestEntityImpl(et, jpaAttributes, new HashMap<JPAAssociationPath, List<JPARequestEntity>>(0),
          new HashMap<JPAAssociationPath, List<JPARequestLink>>(0), keys, headers);

    } catch (ODataException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  private void createUpdateResponse(final ODataRequest request, final ODataResponse response,
      final ContentType responseFormat, final JPARequestEntity requestEntity, final EdmEntitySetInfo edmEntitySetInfo,
      final JPAUpdateResult updateResult)
      throws SerializerException, ODataJPAProcessorException, ODataJPASerializerException {

    // http://docs.oasis-open.org/odata/odata/v4.0/odata-v4.0-part1-protocol.html

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
    successStatusCode = HttpStatusCode.OK.getStatusCode();
    Preferences prefer = odata.createPreferences(request.getHeaders(HttpHeader.PREFER));
    // TODO Stream properties
    if (updateResult == null || prefer.getReturn() == Return.MINIMAL) {
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
      response.setHeader(HttpHeader.PREFERENCE_APPLIED, "return=minimal");
    } else {
      if (updateResult.getModifyedEntity() == null)
        throw new ODataJPAProcessorException(MessageKeys.RETURN_MISSING_ENTITY, HttpStatusCode.INTERNAL_SERVER_ERROR);

      Entity updatedEntity = null;
      if (!requestEntity.getKeys().isEmpty()) {
        try {
          // PATCH .../Organizations('1')/AdministrativeInformation/Updated/User
          final JPAAssociationPath path = requestEntity.getEntityType().getAssociationPath(edmEntitySetInfo
              .getNavigationPath());
          final JPARequestEntity linkedEntity = requestEntity.getRelatedEntities().get(path).get(0);
          final Object linkedResult = getLinkedResult(updateResult.getModifyedEntity(), path);
          updatedEntity = convertEntity(linkedEntity.getEntityType(), linkedResult, request.getAllHeaders());
        } catch (ODataJPAModelException e) {
          throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
      } else
        updatedEntity = convertEntity(requestEntity.getEntityType(), updateResult.getModifyedEntity(), request
            .getAllHeaders());
      EntityCollection entities = new EntityCollection();
      entities.getEntities().add(updatedEntity);
      createSuccessResponce(response, responseFormat, serializer.serialize(request, entities));
    }
  }

  private Property findProperty(final String name, final List<Property> properties) {

    for (Property property : properties) {
      if (name.equals(property.getName())) {
        return property;
      }
    }
    return null;
  }

}
