package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.converter.JPAExpandResult.ROOT_RESULT_KEY;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.ODATA_MAXPAGESIZE_NOT_A_NUMBER;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.QUERY_PREPARATION_ERROR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.VALIDATION_NOT_POSSIBLE_TOO_MANY_RESULTS;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.Tuple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.etag.PreconditionException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAnnotatable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPANotImplementedException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.query.JPACollectionItemInfo;
import com.sap.olingo.jpa.processor.core.query.JPACollectionJoinQuery;
import com.sap.olingo.jpa.processor.core.query.JPAConvertibleResult;
import com.sap.olingo.jpa.processor.core.query.JPAExpandItemInfo;
import com.sap.olingo.jpa.processor.core.query.JPAExpandItemInfoFactory;
import com.sap.olingo.jpa.processor.core.query.JPAExpandQueryFactory;
import com.sap.olingo.jpa.processor.core.query.JPAJoinCountQuery;
import com.sap.olingo.jpa.processor.core.query.JPAJoinQuery;
import com.sap.olingo.jpa.processor.core.query.JPAKeyBoundary;
import com.sap.olingo.jpa.processor.core.query.JPANavigationPropertyInfo;
import com.sap.olingo.jpa.processor.core.query.Utility;

public final class JPANavigationRequestProcessor extends JPAAbstractGetRequestProcessor {
  private static final Log LOGGER = LogFactory.getLog(JPANavigationRequestProcessor.class);
  private final ServiceMetadata serviceMetadata;
  private final UriResource lastItem;

  public JPANavigationRequestProcessor(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataRequestContextAccess requestContext) throws ODataException {

    super(odata, requestContext);
    this.serviceMetadata = serviceMetadata;
    final var resourceParts = uriInfo.getUriResourceParts();
    this.lastItem = resourceParts.get(resourceParts.size() - 1);
  }

  @Override
  public <K extends Comparable<K>> void retrieveData(final ODataRequest request, final ODataResponse response,
      final ContentType responseFormat) throws ODataException {

    try (var measurement = debugger.newMeasurement(this, "retrieveData")) {
      checkRequestSupported();
      // Create a JPQL Query and execute it
      JPAJoinQuery query = null;
      try {
        query = new JPAJoinQuery(odata, requestContext);
      } catch (final ODataException e) {
        throw new ODataJPAProcessorException(QUERY_PREPARATION_ERROR, HttpStatusCode.INTERNAL_SERVER_ERROR, e);
      }

      final var result = query.execute();
      // Validate If-Match and If-None-Match headers
      final var conditionValidationResult = validateEntityTag(result, requestContext.getHeader());
      // Read Expand and Collection
      EntityCollection entityCollection;
      if (conditionValidationResult == JPAETagValidationResult.SUCCESS && !isRootResultEmpty(result)) {
        final var keyBoundary = result.getKeyBoundary(requestContext, query.getNavigationInfo());
        final var watchDog = new JPAExpandWatchDog(determineTargetEntitySet(requestContext));
        watchDog.watch(uriInfo.getExpandOption(), uriInfo.getUriResourceParts());
        result.putChildren(readExpandEntities(request.getAllHeaders(), query.getNavigationInfo(), uriInfo, keyBoundary,
            watchDog));
      }
      // Convert tuple result into an OData Result
      try (var converterMeasurement = debugger.newMeasurement(this, "convertResult")) {
        entityCollection = result.asEntityCollection(new JPATupleChildConverter(sd, odata.createUriHelper(),
            serviceMetadata, requestContext)).get(ROOT_RESULT_KEY);
      } catch (final ODataApplicationException e) {
        throw new ODataJPAProcessorException(QUERY_RESULT_CONV_ERROR, HttpStatusCode.INTERNAL_SERVER_ERROR, e);
      }
      // Set Next Link
      entityCollection.setNext(buildNextLink(uriInfo));
      // Count results if requested
      final var countOption = uriInfo.getCountOption();
      if (countOption != null && countOption.getValue())
        entityCollection.setCount(new JPAJoinCountQuery(odata, requestContext)
            .countResults().intValue());

      /*
       * See part 1:
       * - 9.1.1 Response Code 200 OK: A request that does not create a resource returns 200 OK if it is completed
       * successfully and the value of the resource is not null. In this case, the response body MUST contain the value
       * of the resource specified in the request URL.
       * - 9.2.1 Response Code 404 Not Found: 404 Not Found indicates that the resource specified by the request URL
       * does not exist. The response body MAY provide additional information.
       * - 11.2.1 Requesting Individual Entities:
       * -- If no entity exists with the key values specified in the request URL, the service responds with 404 Not
       * Found.
       * - 11.2.3 Requesting Individual Properties:
       * -- If the property is single-valued and has the null value, the service responds with 204 No Content.
       * -- If the property is not available, for example due to permissions, the service responds with 404 Not Found.
       * - 11.2.6 Requesting Related Entities:
       * -- If the navigation property does not exist on the entity indicated by the request URL, the service returns
       * 404
       * Not Found.
       * -- If the relationship terminates on a collection, the response MUST be the format-specific representation of
       * the
       * collection of related entities. If no entities are related, the response is the format-specific representation
       * of
       * an empty collection.
       * -- If the relationship terminates on a single entity, the response MUST be the format-specific representation
       * of
       * the related single entity. If no entity is related, the service returns 204 No Content.
       */
      if (conditionValidationResult == JPAETagValidationResult.NOT_MODIFIED)
        createNotModifiedResponse(response, entityCollection);
      else if (conditionValidationResult == JPAETagValidationResult.PRECONDITION_FAILED)
        createPreconditionFailedResponse(response);
      else if (hasNoContent(entityCollection.getEntities()))
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
      else if (doesNotExists(entityCollection.getEntities()))
        response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
      // 200 OK indicates that either a result was found or that the a Entity Collection query had no result
      else if (entityCollection.getEntities() != null) {
        try (var serializerMeasurement = debugger.newMeasurement(this, "serialize")) {
          final var serializerResult = serializer.serialize(request, entityCollection);
          createSuccessResponse(response, responseFormat, serializerResult, entityCollection);
        }
      } else {
        // A request returns 204 No Content if the requested resource has the null value, or if the service applies a
        // return=minimal preference. In this case, the response body MUST be empty.
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
      }
    }
  }

  void checkRequestSupported() throws ODataJPAProcessException {
    if (uriInfo.getApplyOption() != null)
      throw new ODataJPANotImplementedException("$apply");
  }

  /**
   * Validate
   * <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-14.24">If-Match</a> and
   * <a href="https://datatracker.ietf.org/doc/html/rfc2616#section-14.26">If-None-Match</a>
   * headers
   * @param result Query result
   * @param header List of all request headers
   * @throws ODataJPAProcessorException
   */
  JPAETagValidationResult validateEntityTag(final JPAConvertibleResult result, final JPAHttpHeaderMap header)
      throws ODataJPAProcessorException {

    try {
      if (result instanceof final JPAExpandResult expandResult) {
        if (expandResult.getEntityType().hasEtag()) {

          final var results = expandResult.getResult(ROOT_RESULT_KEY);
          final var ifNoneMatchEntityTags = getMatchHeader(header, HttpHeader.IF_NONE_MATCH);
          final var ifMatchEntityTags = getMatchHeader(header, HttpHeader.IF_MATCH);
          if (results.size() == 1) {
            final var etagAlias = expandResult.getEntityType().getEtagPath().getAlias();
            final var etag = requestContext.getEtagHelper().asEtag(expandResult.getEntityType(),
                results.get(0).get(etagAlias));
            if (requestContext.getEtagHelper().checkReadPreconditions(etag, ifMatchEntityTags, ifNoneMatchEntityTags))
              return JPAETagValidationResult.NOT_MODIFIED;
          } else if (!preconditionCheckSupported(results, ifNoneMatchEntityTags, ifMatchEntityTags)) {
            throw new ODataJPAProcessorException(VALIDATION_NOT_POSSIBLE_TOO_MANY_RESULTS,
                HttpStatusCode.BAD_REQUEST);
          }
        }
      } else {
        LOGGER.warn("Result is not of type JPAExpandResult. ETag validation not possible");
      }
    } catch (final ODataJPAModelException | ODataJPAQueryException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    catch (final PreconditionException e) {
      return JPAETagValidationResult.PRECONDITION_FAILED;
    }
    return JPAETagValidationResult.SUCCESS;
  }

  private boolean isRootResultEmpty(final JPAConvertibleResult result) {
    if (result instanceof final JPAExpandResult expandResult) {
      return expandResult.getResult(ROOT_RESULT_KEY).isEmpty();
    }
    return false;
  }

  private List<String> getMatchHeader(final JPAHttpHeaderMap headers, final String matchHeader) {
    return Optional.ofNullable(headers.get(matchHeader)).orElseGet(() -> headers.get(
        matchHeader.toLowerCase(Locale.ENGLISH)));
  }

  private boolean preconditionCheckSupported(final List<Tuple> results, final List<String> ifNoneMatchEntityTags,
      final List<String> ifMatchEntityTags) {
    return results.size() <= 1
        || (isEmpty(ifMatchEntityTags) && isEmpty(ifNoneMatchEntityTags));
  }

  private boolean isEmpty(final List<String> list) {
    return list == null || list.isEmpty();
  }

  private URI buildNextLink(final UriInfoResource uriInfo) throws ODataJPAProcessorException {
    if (uriInfo != null && uriInfo.getSkipTokenOption() != null) {
      try {
        final var skipToken = uriInfo.getSkipTokenOption().getValue();
        return new URI(Utility.determineBindingTarget(uriInfo.getUriResourceParts()).getName() + "?"
            + SystemQueryOptionKind.SKIPTOKEN.toString() + "=" + skipToken);
      } catch (final URISyntaxException e) {
        throw new ODataJPAProcessorException(ODATA_MAXPAGESIZE_NOT_A_NUMBER, HttpStatusCode.INTERNAL_SERVER_ERROR, e);
      }
    }
    return null;
  }

  private boolean complexHasNoContent(final List<Entity> entities) {
    final String name;
    if (entities.isEmpty())
      return false;
    name = Utility.determineStartNavigationPath(uriInfo.getUriResourceParts()).getProperty().getName();
    final var property = entities.get(0).getProperty(name);
    if (property != null) {
      for (final Property p : ((ComplexValue) property.getValue()).getValue()) {
        if (p.getValue() != null) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean doesNotExists(final List<Entity> entities) throws ODataApplicationException {
    // handle ../Organizations('xx')
    return (entities.isEmpty()
        && ((lastItem.getKind() == UriResourceKind.primitiveProperty
            || lastItem.getKind() == UriResourceKind.complexProperty
            || lastItem.getKind() == UriResourceKind.entitySet
                && !Utility.determineKeyPredicates(lastItem).isEmpty())
            || lastItem.getKind() == UriResourceKind.singleton));
  }

  private boolean hasNoContent(final List<Entity> entities) {

    if (lastItem.getKind() == UriResourceKind.primitiveProperty
        || lastItem.getKind() == UriResourceKind.navigationProperty
        || lastItem.getKind() == UriResourceKind.complexProperty) {

      if (((UriResourcePartTyped) this.lastItem).isCollection()) {
        // Collections always return 200 no matter if type are empty or not
        return false;
      }

      if (lastItem.getKind() == UriResourceKind.primitiveProperty) {
        return primitiveHasNoContent(entities);
      }
      if (lastItem.getKind() == UriResourceKind.complexProperty) {
        return complexHasNoContent(entities);
      }
      if (entities.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  private boolean primitiveHasNoContent(final List<Entity> entities) {
    final String name;
    if (entities.isEmpty())
      return false;
    name = Utility.determineStartNavigationPath(uriInfo.getUriResourceParts()).getProperty().getName();
    final var property = entities.get(0).getProperty(name);
    return (property != null && property.getValue() == null);
  }

  /**
   * $expand is implemented as a recursively processing of all expands with a DB round trip per expand item.
   * Alternatively also a <i>big</i> join could be created. This would lead to a transport of redundant data, but has
   * only one round trip. As of now it has not been measured under which conditions which solution has the better
   * performance, but a big join has also the following draw back:
   * <ul>
   * <li>In case a multiple $expands are requested maybe on multiple levels
   * including filtering and ordering the query becomes very complex which reduces the maintainability and comes with
   * the risk that some databases are not able to handles those.</li>
   * <li>The number of returned columns becomes big, which may become a problem for some databases</li>
   * <li>This hard to create a big join for <code>$level=*</code></li>
   * <li>Server driven paging seems to be more complicated</li>
   * </ul>
   * and the goal is to implement a general solution, multiple round trips have been taken.
   * <p>
   * For a general overview see:
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398298"
   * >OData Version 4.0 Part 1 - 11.2.4.2 System Query Option $expand</a>
   * <p>
   *
   * For a detailed description of the URI syntax see:
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398162"
   * >OData Version 4.0 Part 2 - 5.1.2 System Query Option $expand</a> boundary
   * @param headers
   * @param parentHops
   * @param uriResourceInfo
   * @param keyBoundary
   * @param watchDog
   * @return
   * @throws ODataException
   */
  private Map<JPAAssociationPath, JPAExpandResult> readExpandEntities(final Map<String, List<String>> headers,
      final List<JPANavigationPropertyInfo> parentHops, final UriInfoResource uriResourceInfo,
      final Optional<JPAKeyBoundary> keyBoundary, final JPAExpandWatchDog watchDog) throws ODataException {

    try (var expandMeasurement = debugger.newMeasurement(this, "readExpandEntities")) {

      final var factory = new JPAExpandQueryFactory(odata, requestContext, cb);
      final Map<JPAAssociationPath, JPAExpandResult> allExpResults = new HashMap<>();
      if (watchDog.getRemainingLevels() > 0) {
        // x/a?$expand=b/c($expand=d,e/f)&$filter=...&$top=3&$orderBy=...
        // x?$expand=*(levels=3)
        // For performance reasons the expand query should only return results for the results of the higher-level
        // query.
        // The solution for restrictions like a given key or a given filter condition, as it can be propagated to a
        // sub-query.
        // For $top and $skip things are more difficult as the Subquery does not support LIMIT and OFFSET, this is
        // done on the TypedQuery created out of the CriteriaQuery. In addition not all databases support LIMIT within a
        // sub-query used within EXISTS.
        // Solution: Forward the highest and lowest key from the root and create a "between" those.

        final var itemInfoList = new JPAExpandItemInfoFactory(requestContext)
            .buildExpandItemInfo(sd, uriResourceInfo, parentHops, keyBoundary, factory);
        for (final JPAExpandItemInfo item : watchDog.filter(itemInfoList)) {
          final var expandQuery = factory.createQuery(item, keyBoundary);
          final var expandResult = expandQuery.execute();
          if (expandResult.getNoResults() > 0)
            // Only go to the next hop if the current one has a result
            expandResult.putChildren(readExpandEntities(headers, item.getHops(), item.getUriInfo(), keyBoundary,
                watchDog));
          allExpResults.put(item.getExpandAssociation(), expandResult);
        }
        watchDog.levelProcessed();
      }
      // process collection attributes
      final var collectionInfoList = new JPAExpandItemInfoFactory(requestContext)
          .buildCollectionItemInfo(sd, uriResourceInfo, parentHops, requestContext.getGroupsProvider());
      for (final JPACollectionItemInfo item : collectionInfoList) {
        final var collectionQuery = new JPACollectionJoinQuery(odata, item,
            new JPAODataInternalRequestContext(item.getUriInfo(), requestContext, headers), keyBoundary);
        final JPAExpandResult expandResult = collectionQuery.execute();
        allExpResults.put(item.getExpandAssociation(), expandResult);
      }
      return allExpResults;
    }
  }

  private static Optional<JPAAnnotatable> determineTargetEntitySet(final JPAODataRequestContextAccess requestContext)
      throws ODataException {

    final var bindingTarget = Utility.determineBindingTarget(requestContext.getUriInfo()
        .getUriResourceParts());
    if (bindingTarget instanceof EdmEntitySet)
      return requestContext.getEdmProvider().getServiceDocument().getEntitySet(bindingTarget.getName())
          .map(JPAAnnotatable.class::cast);
    return Optional.empty();
  }
}
