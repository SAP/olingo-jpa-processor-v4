package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.converter.JPAExpandResult.ROOT_RESULT_KEY;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ATTRIBUTE_NOT_FOUND;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.BEFORE_IMAGE_MERGED;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ENTITY_TYPE_UNKNOWN;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.RETURN_MISSING_ENTITY;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.RETURN_NULL;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.WRONG_RETURN_TYPE;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;
import static org.apache.olingo.commons.api.http.HttpStatusCode.NO_CONTENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
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
import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory.JPAODataTransaction;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAInvocationTargetException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPATransactionException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;
import com.sap.olingo.jpa.processor.core.modify.JPACreateResultFactory;
import com.sap.olingo.jpa.processor.core.modify.JPAUpdateResult;
import com.sap.olingo.jpa.processor.core.query.EdmBindingTargetInfo;
import com.sap.olingo.jpa.processor.core.query.ExpressionUtil;
import com.sap.olingo.jpa.processor.core.query.Util;

public final class JPACUDRequestProcessor extends JPAAbstractRequestProcessor {

  private static final String DEBUG_CREATE_ENTITY = "createEntity";
  private static final String DEBUG_UPDATE_ENTITY = "updateEntity";
  private final ServiceMetadata serviceMetadata;
  private final JPAConversionHelper helper;

  public JPACUDRequestProcessor(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataRequestContextAccess requestContext,
      final JPAConversionHelper cudHelper) throws ODataException {

    super(odata, requestContext);
    this.serviceMetadata = serviceMetadata;
    this.helper = cudHelper;
  }

  public void clearFields(final ODataRequest request, final ODataResponse response) throws ODataJPAProcessException {

    final int handle = debugger.startRuntimeMeasurement(this, "clearFields");
    final JPACUDRequestHandler handler = requestContext.getCUDRequestHandler();
    final EdmBindingTargetInfo edmEntitySetInfo = Util.determineBindingTargetAndKeys(uriInfo.getUriResourceParts());

    final JPARequestEntity requestEntity = createRequestEntity(edmEntitySetInfo, uriInfo.getUriResourceParts(), request
        .getAllHeaders());

    JPAODataTransaction ownTransaction = null;
    final boolean foreignTransaction = requestContext.getTransactionFactory().hasActiveTransaction();

    if (!foreignTransaction)
      ownTransaction = requestContext.getTransactionFactory().createTransaction();
    try {
      final int updateHandle = debugger.startRuntimeMeasurement(handler, DEBUG_UPDATE_ENTITY);
      handler.updateEntity(requestEntity, em, determineHttpVerb(request, uriInfo.getUriResourceParts()));
      if (!foreignTransaction)
        handler.validateChanges(em);
      debugger.stopRuntimeMeasurement(updateHandle);
    } catch (final ODataJPAProcessException e) {
      checkForRollback(ownTransaction, foreignTransaction);
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    } catch (final Exception e) {
      checkForRollback(ownTransaction, foreignTransaction);
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(e, INTERNAL_SERVER_ERROR);
    }
    if (!foreignTransaction)
      ownTransaction.commit();
    debugger.stopRuntimeMeasurement(handle);
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
  }

  public void createEntity(final ODataRequest request, final ODataResponse response, final ContentType requestFormat,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    final int handle = debugger.startRuntimeMeasurement(this, DEBUG_CREATE_ENTITY);
    final JPACUDRequestHandler handler = requestContext.getCUDRequestHandler();

    final EdmBindingTargetInfo edmEntitySetInfo = Util.determineModifyEntitySetAndKeys(uriInfo.getUriResourceParts());
    final Entity odataEntity = helper.convertInputStream(odata, request, requestFormat, uriInfo.getUriResourceParts());

    final JPARequestEntity requestEntity = createRequestEntity(edmEntitySetInfo, odataEntity, request.getAllHeaders());

    // Create entity
    Object result = null;
    JPAODataTransaction ownTransaction = null;
    final boolean foreignTransaction = requestContext.getTransactionFactory().hasActiveTransaction();
    if (!foreignTransaction)
      ownTransaction = requestContext.getTransactionFactory().createTransaction();
    try {
      final int createHandle = debugger.startRuntimeMeasurement(handler, DEBUG_CREATE_ENTITY);
      result = handler.createEntity(requestEntity, em);
      if (!foreignTransaction)
        handler.validateChanges(em);
      debugger.stopRuntimeMeasurement(createHandle);
    } catch (final ODataJPAProcessException e) {
      checkForRollback(ownTransaction, foreignTransaction);
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    } catch (final Exception e) {
      checkForRollback(ownTransaction, foreignTransaction);
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(e, INTERNAL_SERVER_ERROR);
    }

    if (result != null && result.getClass() != requestEntity.getEntityType().getTypeClass()
        && !(result instanceof Map<?, ?>)) {
      checkForRollback(ownTransaction, foreignTransaction);
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(WRONG_RETURN_TYPE, INTERNAL_SERVER_ERROR, result.getClass().toString(),
          requestEntity.getEntityType().getTypeClass().toString());
    }

    if (!foreignTransaction)
      ownTransaction.commit();

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
    final JPACUDRequestHandler handler = requestContext.getCUDRequestHandler();
    final JPAEntityType et;
    final Map<String, Object> jpaKeyPredicates = new HashMap<>();

    // 1. Retrieve the entity set which belongs to the requested entity
    final List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
    // Note: only in our example we can assume that the first segment is the EntitySet
    final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
    final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

    // 2. Convert Key from URL to JPA
    try {
      et = sd.getEntity(edmEntitySet.getName());
      if (et == null)
        throw new ODataJPAProcessorException(ENTITY_TYPE_UNKNOWN, BAD_REQUEST, edmEntitySet.getName());
      final List<UriParameter> uriKeyPredicates = uriResourceEntitySet.getKeyPredicates();
      for (final UriParameter uriParam : uriKeyPredicates) {
        final JPAAttribute attribute = et.getPath(uriParam.getName()).getLeaf();
        jpaKeyPredicates.put(attribute.getInternalName(), ExpressionUtil.convertValueOnAttribute(odata, attribute,
            uriParam.getText(), true));
      }
    } catch (final ODataException e) {
      throw new ODataJPAProcessorException(e, BAD_REQUEST);
    }
    final JPARequestEntity requestEntity = createRequestEntity(et, jpaKeyPredicates, request.getAllHeaders());

    // 3. Perform Delete
    JPAODataTransaction ownTransaction = null;
    final boolean foreignTransaction = requestContext.getTransactionFactory().hasActiveTransaction();
    if (!foreignTransaction)
      ownTransaction = requestContext.getTransactionFactory().createTransaction();
    try {
      final int deleteHandle = debugger.startRuntimeMeasurement(handler, "deleteEntity");
      handler.deleteEntity(requestEntity, em);
      if (!foreignTransaction)
        handler.validateChanges(em);
      debugger.stopRuntimeMeasurement(deleteHandle);
    } catch (final ODataJPAProcessException e) {
      checkForRollback(ownTransaction, foreignTransaction);
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    } catch (final Throwable e) { // NOSONAR
      checkForRollback(ownTransaction, foreignTransaction);
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(e, INTERNAL_SERVER_ERROR);
    }
    if (!foreignTransaction)
      ownTransaction.commit();

    // 4. configure the response object
    response.setStatusCode(NO_CONTENT.getStatusCode());
    debugger.stopRuntimeMeasurement(handle);
  }

  public void updateEntity(final ODataRequest request, final ODataResponse response, final ContentType requestFormat,
      final ContentType responseFormat) throws ODataJPAProcessException, ODataLibraryException {

    final int handle = debugger.startRuntimeMeasurement(this, DEBUG_UPDATE_ENTITY);
    final JPACUDRequestHandler handler = requestContext.getCUDRequestHandler();
    final EdmBindingTargetInfo edmEntitySetInfo = Util.determineModifyEntitySetAndKeys(uriInfo.getUriResourceParts());
    final Entity odataEntity = helper.convertInputStream(odata, request, requestFormat, uriInfo.getUriResourceParts());

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

    JPAODataTransaction ownTransaction = null;
    final boolean foreignTransaction = requestContext.getTransactionFactory().hasActiveTransaction();
    if (!foreignTransaction)
      ownTransaction = requestContext.getTransactionFactory().createTransaction();
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
      updateResult = handler.updateEntity(requestEntity, em, determineHttpVerb(request, uriInfo.getUriResourceParts()));
      if (!foreignTransaction)
        handler.validateChanges(em);
    } catch (final ODataJPAProcessException e) {
      checkForRollback(ownTransaction, foreignTransaction);
      throw e;
    } catch (final Throwable e) {
      checkForRollback(ownTransaction, foreignTransaction);
      throw new ODataJPAProcessorException(e, INTERNAL_SERVER_ERROR);
    } finally {
      debugger.stopRuntimeMeasurement(handle);
    }
    if (updateResult == null) {
      checkForRollback(ownTransaction, foreignTransaction);
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(RETURN_NULL, INTERNAL_SERVER_ERROR);
    }
    if (updateResult.getModifiedEntity() != null && !requestEntity.getEntityType().getTypeClass().isInstance(
        updateResult.getModifiedEntity())) {
      checkForRollback(ownTransaction, foreignTransaction);
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(WRONG_RETURN_TYPE, INTERNAL_SERVER_ERROR,
          updateResult.getModifiedEntity().getClass().toString(), requestEntity.getEntityType().getTypeClass()
              .toString());
    }
    if (!foreignTransaction)
      ownTransaction.commit();

    if (updateResult.wasCreate()) {
      createCreateResponse(request, response, responseFormat, requestEntity.getEntityType(),
          (EdmEntitySet) edmEntitySetInfo.getEdmBindingTarget(), updateResult.getModifiedEntity()); // Singleton
      debugger.stopRuntimeMeasurement(handle);
    } else {
      createUpdateResponse(request, response, responseFormat, requestEntity, edmEntitySetInfo, updateResult);
      debugger.stopRuntimeMeasurement(handle);
    }

  }

  private void checkForRollback(final JPAODataTransaction ownTransaction, final boolean foreignTransaction)
      throws ODataJPATransactionException {
    if (!foreignTransaction)
      ownTransaction.rollback();
  }

  private HttpMethod determineHttpVerb(final ODataRequest request, final List<UriResource> resourceParts) {
    final HttpMethod originalMethod = request.getMethod();
    final HttpMethod targetMethod;
    final int noResourceParts = resourceParts.size();
    if (originalMethod == HttpMethod.PUT && resourceParts.get(noResourceParts - 1) instanceof UriResourceProperty) {
      targetMethod = HttpMethod.PATCH;
    } else {
      targetMethod = originalMethod;
    }
    return targetMethod;
  }

  final JPARequestEntity createRequestEntity(final EdmEntitySet edmEntitySet, final Entity odataEntity,
      final Map<String, List<String>> headers) throws ODataJPAProcessorException {

    try {
      final JPAEntityType et = sd.getEntity(edmEntitySet.getName());
      if (et == null)
        throw new ODataJPAProcessorException(ENTITY_TYPE_UNKNOWN, BAD_REQUEST, edmEntitySet.getName());
      return createRequestEntity(et, odataEntity, new HashMap<>(0), headers, null);
    } catch (final ODataException e) {
      throw new ODataJPAProcessorException(e, BAD_REQUEST);
    }
  }

  final JPARequestEntity createRequestEntity(final EdmBindingTargetInfo edmEntitySetInfo, final Entity odataEntity,
      final Map<String, List<String>> headers) throws ODataJPAProcessorException {

    try {
      final JPAEntityType et = sd.getEntity(edmEntitySetInfo.getName());
      if (et == null)
        throw new ODataJPAProcessorException(ENTITY_TYPE_UNKNOWN, BAD_REQUEST, edmEntitySetInfo.getName());
      final Map<String, Object> keys = helper.convertUriKeys(odata, et, edmEntitySetInfo.getKeyPredicates());
      final JPARequestEntityImpl requestEntity = (JPARequestEntityImpl) createRequestEntity(et, odataEntity, keys,
          headers, et.getAssociationPath(edmEntitySetInfo.getNavigationPath()));
      requestEntity.setBeforeImage(createBeforeImage(requestEntity, em));
      return requestEntity;
    } catch (final ODataException e) {
      throw new ODataJPAProcessorException(e, BAD_REQUEST);
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
        return new JPARequestEntityImpl(et, jpaAttributes, relatedEntities, relationLinks, keys, headers,
            requestContext);
      } else {
        // Handle requests like POST
        // .../AdministrativeDivisions(DivisionCode='DE6',CodeID='NUTS1',CodePublisher='Eurostat')/Children
        final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities = createInlineEntities(odataEntity,
            jpaAssociationPath, headers);
        return new JPARequestEntityImpl(et, Collections.emptyMap(), relatedEntities, Collections.emptyMap(), keys,
            headers, requestContext);
      }

    } catch (final ODataException e) {
      throw new ODataJPAProcessorException(e, BAD_REQUEST);
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

    return new JPARequestEntityImpl(et, jpaAttributes, relatedEntities, relationLinks, keys, headers, requestContext);
  }

  private Entity convertEntity(final JPAEntityType et, final Object result, final Map<String, List<String>> headers)
      throws ODataJPAProcessorException {

    try {
      final JPATupleChildConverter converter = new JPATupleChildConverter(sd, odata.createUriHelper(), serviceMetadata,
          requestContext);
      final JPACreateResultFactory factory = new JPACreateResultFactory(converter);//
      return converter.getResult(factory.getJPACreateResult(et, result, headers), Collections.emptySet())
          .get(ROOT_RESULT_KEY).getEntities().get(0);
    } catch (ODataJPAModelException | ODataApplicationException e) {
      throw new ODataJPAProcessorException(e, INTERNAL_SERVER_ERROR);
    }

  }

  private Map<String, Object> convertUriPath(final JPAEntityType et, final List<UriResource> resourcePaths)
      throws ODataJPAModelException, ODataJPAProcessorException {

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
        st = st.getAttribute(internalName).orElseThrow(() -> new ODataJPAProcessorException(ATTRIBUTE_NOT_FOUND,
            INTERNAL_SERVER_ERROR, internalName)).getStructuredType();
      } else {
        currentMap.put(st.getPath(uriResourceProperty.getProperty().getName()).getLeaf().getInternalName(), null);
      }
    }
    return jpaAttributes;
  }

  private Optional<Object> createBeforeImage(final JPARequestEntity requestEntity, final EntityManager em)
      throws ODataJPAProcessorException, ODataJPAInvocationTargetException {

    if (!requestEntity.getKeys().isEmpty()) {
      final Object key = requestEntity.getModifyUtil().createPrimaryKey(requestEntity.getEntityType(), requestEntity
          .getKeys(), requestEntity.getEntityType());
      final Optional<Object> beforeImage = Optional.ofNullable(em.find(requestEntity.getEntityType().getTypeClass(),
          key));
      if (beforeImage.isPresent())
        em.detach(beforeImage.get());
      return beforeImage;
    }
    return Optional.empty();
  }

  private void createCreateResponse(final ODataRequest request, final ODataResponse response,
      final ContentType responseFormat, final JPAEntityType et, final EdmEntitySet edmEntitySet, final Object result)
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
    final Preferences prefer = odata.createPreferences(request.getHeaders(HttpHeader.PREFER));
    // TODO Stream properties

    final String location = helper.convertKeyToLocal(odata, request, edmEntitySet, et, result);
    if (prefer.getReturn() == Return.MINIMAL) {
      createMinimalCreateResponse(response, location);
    } else {
      final Entity createdEntity = convertEntity(et, result, request.getAllHeaders());
      final EntityCollection entities = new EntityCollection();
      entities.getEntities().add(createdEntity);
      createSuccessResponse(response, responseFormat, serializer.serialize(request, entities));
      response.setHeader(HttpHeader.LOCATION, location);
    }
  }

  private void createCreateResponse(final ODataRequest request, final ODataResponse response,
      final ContentType responseFormat, final JPARequestEntity requestEntity, final EdmBindingTargetInfo edmEntitySet,
      final Object result) throws SerializerException, ODataJPAProcessorException, ODataJPASerializerException {

    if (!requestEntity.getKeys().isEmpty()) {
      // .../AdministrativeDivisions(DivisionCode='DE5',CodeID='NUTS1',CodePublisher='Eurostat')/Children
      // As of now only one related entity can be created
      try {
        final JPAAssociationPath path = requestEntity.getEntityType().getAssociationPath(edmEntitySet
            .getNavigationPath());

        final JPARequestEntity linkedEntity = requestEntity.getRelatedEntities().get(path).get(0);
        final Object linkedResult = getLinkedResult(result, path, requestEntity.getBeforeImage());
        createCreateResponse(request, response, responseFormat, linkedEntity.getEntityType(),
            (EdmEntitySet) edmEntitySet.getTargetEdmBindingTarget(), linkedResult);
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAProcessorException(e, INTERNAL_SERVER_ERROR);
      }
    } else {
      createCreateResponse(request, response, responseFormat, requestEntity.getEntityType(),
          (EdmEntitySet) edmEntitySet.getEdmBindingTarget(), result);
    }
  }

  private Map<JPAAssociationPath, List<JPARequestEntity>> createInlineEntities(final Entity odataEntity,
      final JPAAssociationPath path, final Map<String, List<String>> headers) throws ODataJPAProcessorException {

    final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities = new HashMap<>(1);
    final List<JPARequestEntity> inlineEntities = new ArrayList<>();

    inlineEntities.add(createRequestEntity((JPAEntityType) path.getTargetType(), odataEntity, new HashMap<>(0), headers,
        null));

    relatedEntities.put(path, inlineEntities);

    return relatedEntities;
  }

  private Map<JPAAssociationPath, List<JPARequestEntity>> createInlineEntities(final JPAEntityType et,
      final Entity odataEntity, final Map<String, List<String>> headers) throws ODataJPAModelException,
      ODataJPAProcessorException {

    final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities = new HashMap<>();

    for (final JPAAssociationPath path : et.getAssociationPathList()) {
      List<Property> stProperties = odataEntity.getProperties();
      Property p = null;
      for (final JPAElement pathItem : path.getPath()) {
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

  private void createInlineEntities(final JPAEntityType st, final Map<String, List<String>> headers,
      final Map<JPAAssociationPath, List<JPARequestEntity>> relatedEntities, final Link navigationLink,
      final JPAAssociationPath path) throws ODataJPAProcessorException {

    if (navigationLink == null) return;
    final List<JPARequestEntity> inlineEntities = new ArrayList<>();
    if (path.getLeaf().isCollection()) {
      for (final Entity e : navigationLink.getInlineEntitySet().getEntities()) {
        inlineEntities.add(createRequestEntity(st, e, new HashMap<>(0), headers, null));
      }
      relatedEntities.put(path, inlineEntities);
    } else {
      inlineEntities.add(createRequestEntity(st, navigationLink.getInlineEntity(), new HashMap<>(0), headers, null));
      relatedEntities.put(path, inlineEntities);
    }
  }

  private void createMinimalCreateResponse(final ODataResponse response, final String location) {
    response.setStatusCode(NO_CONTENT.getStatusCode());
    response.setHeader(HttpHeader.PREFERENCE_APPLIED, "return=minimal");
    response.setHeader(HttpHeader.LOCATION, location);
    response.setHeader(HttpHeader.ODATA_ENTITY_ID, location);
  }

  private Map<JPAAssociationPath, List<JPARequestLink>> createRelationLinks(final JPAEntityType et,
      final Entity odataEntity)
      throws ODataJPAModelException {

    final Map<JPAAssociationPath, List<JPARequestLink>> relationLinks =
        new HashMap<>();
    for (final Link binding : odataEntity.getNavigationBindings()) {
      final List<JPARequestLink> bindingLinks = new ArrayList<>();
      final JPAAssociationPath path = et.getAssociationPath(binding.getTitle());
      if (path.getLeaf().isCollection()) {
        for (final String bindingLink : binding.getBindingLinks()) {
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

  private JPARequestEntity createRequestEntity(final EdmBindingTargetInfo edmEntitySetInfo,
      final List<UriResource> resourceParts,
      final Map<String, List<String>> headers) throws ODataJPAProcessorException {

    try {
      final JPAEntityType et = sd.getEntity(edmEntitySetInfo
          .getEdmBindingTarget().getName());
      final Map<String, Object> keys = helper.convertUriKeys(odata, et, edmEntitySetInfo.getKeyPredicates());
      final Map<String, Object> jpaAttributes = convertUriPath(et, resourceParts);

      return new JPARequestEntityImpl(et, jpaAttributes, Collections.emptyMap(), Collections.emptyMap(), keys, headers,
          requestContext);

    } catch (final ODataException e) {
      throw new ODataJPAProcessorException(e, BAD_REQUEST);
    }
  }

  private void createUpdateResponse(final ODataRequest request, final ODataResponse response,
      final ContentType responseFormat, final JPARequestEntity requestEntity,
      final EdmBindingTargetInfo edmEntitySetInfo,
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
    final Preferences prefer = odata.createPreferences(request.getHeaders(HttpHeader.PREFER));
    // TODO Stream properties
    if (updateResult == null || prefer.getReturn() == Return.MINIMAL) {
      response.setStatusCode(NO_CONTENT.getStatusCode());
      response.setHeader(HttpHeader.PREFERENCE_APPLIED, "return=minimal");
    } else {
      if (updateResult.getModifiedEntity() == null)
        throw new ODataJPAProcessorException(RETURN_MISSING_ENTITY, INTERNAL_SERVER_ERROR);

      Entity updatedEntity = null;
      JPAAssociationPath path;
      try {
        path = requestEntity.getEntityType().getAssociationPath(edmEntitySetInfo
            .getNavigationPath());
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAProcessorException(e, INTERNAL_SERVER_ERROR);
      }
      if (path != null) {
        // PATCH .../Organizations('1')/AdministrativeInformation/Updated/User
        final JPARequestEntity linkedEntity = requestEntity.getRelatedEntities().get(path).get(0);
        final Object linkedResult = getLinkedResult(updateResult.getModifiedEntity(), path, Optional.empty());
        updatedEntity = convertEntity(linkedEntity.getEntityType(), linkedResult, request.getAllHeaders());
      } else {
        updatedEntity = convertEntity(requestEntity.getEntityType(), updateResult.getModifiedEntity(), request
            .getAllHeaders());
      }
      final EntityCollection entities = new EntityCollection();
      entities.getEntities().add(updatedEntity);
      createSuccessResponse(response, responseFormat, serializer.serialize(request, entities));
    }
  }

  private Property findProperty(final String name, final List<Property> properties) {

    for (final Property property : properties) {
      if (name.equals(property.getName())) {
        return property;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private Object getLinkedResult(final Object result, final JPAAssociationPath path, final Optional<Object> beforeImage)
      throws ODataJPAProcessorException {

    if (result instanceof Map<?, ?>) {
      return getLinkedMapBasedResult((Map<String, Object>) result, path);
    } else {
      if (beforeImage.isPresent() && beforeImage.get().equals(result)) {
        return getLinkedInstanceBasedResultByDelta(result, path, beforeImage);
      } else {
        return getLinkedInstanceBasedResultByIndex(result, path);
      }
    }
  }

  /*
   * The method compares before image with the current state of a collection. It is expected that exactly one entry has
   * been created. Up to now no contract checks are performed, which may change in the future.
   */
  Object getLinkedInstanceBasedResultByDelta(final Object result, final JPAAssociationPath path,
      final Optional<Object> beforeImage) throws ODataJPAProcessorException {
    if (beforeImage.isPresent()) {
      if (em.contains(beforeImage.get())) {
        throw new ODataJPAProcessorException(BEFORE_IMAGE_MERGED, INTERNAL_SERVER_ERROR);
      }
      if (!path.getLeaf().isCollection())
        return getLinkedInstanceBasedResultByIndex(result, path);

      Object value = result;
      Object before = beforeImage.get();
      for (final JPAElement pathItem : path.getPath()) {
        final Map<String, Object> valueGetterMap = helper.buildGetterMap(value);
        value = valueGetterMap.get(pathItem.getInternalName());
        // We are not able to use the buffered Getter Map for the before image as well, as the buffer uses a HashMap and
        // before image and result have the same key and therefore are equal and have likely the same hash value.
        final Map<String, Object> beforeGetterMap = helper.determineGetter(before);
        before = beforeGetterMap.get(pathItem.getInternalName());
      }
      if (value != null && !((Collection<?>) value).isEmpty()) {
        for (final Object element : ((Collection<?>) value)) {
          if (!((Collection<?>) before).contains(element))
            return element;
        }
      }
      return null;
    }
    return null;
  }

  private Object getLinkedInstanceBasedResultByIndex(final Object result, final JPAAssociationPath path)
      throws ODataJPAProcessorException {

    Object value = result;
    for (final JPAElement pathItem : path.getPath()) {
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

  @SuppressWarnings("unchecked")
  private Object getLinkedMapBasedResult(final Map<String, Object> result, final JPAAssociationPath path) {
    Map<String, Object> target = result;
    for (final JPAElement pathItem : path.getPath())
      target = (Map<String, Object>) target.get(pathItem.getInternalName());
    return target;
  }
}
