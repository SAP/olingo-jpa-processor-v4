package com.sap.olingo.jpa.processor.core.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.CountOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.converter.JPATupleChildConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.query.JPAExpandItemInfo;
import com.sap.olingo.jpa.processor.core.query.JPAExpandItemInfoFactory;
import com.sap.olingo.jpa.processor.core.query.JPAExpandQuery;
import com.sap.olingo.jpa.processor.core.query.JPAExpandQueryResult;
import com.sap.olingo.jpa.processor.core.query.JPANavigationProptertyInfo;
import com.sap.olingo.jpa.processor.core.query.JPAQuery;
import com.sap.olingo.jpa.processor.core.query.Util;

public final class JPANavigationRequestProcessor extends JPAAbstractGetRequestProcessor {
  private final ServiceMetadata serviceMetadata;
  private final UriResource lastItem;

  public JPANavigationRequestProcessor(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataSessionContextAccess context, final JPAODataRequestContextAccess requestContext)
      throws ODataException {
    super(odata, context, requestContext);
    this.serviceMetadata = serviceMetadata;
    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    this.lastItem = resourceParts.get(resourceParts.size() - 1);
  }

  @Override
  public void retrieveData(final ODataRequest request, final ODataResponse response, final ContentType responseFormat)
      throws ODataException {

    final int handle = debugger.startRuntimeMeasurement(this, "retrieveData");

    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(resourceParts);

    // Create a JPQL Query and execute it
    JPAQuery query = null;
    try {
      query = new JPAQuery(odata, targetEdmEntitySet, sessionContext, uriInfo, em, request.getAllHeaders());
    } catch (ODataException e) {
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_PREPARATION_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }

    final JPAExpandQueryResult result = query.execute();
    result.putChildren(readExpandEntities(request.getAllHeaders(), null, uriInfo));
    // Convert tuple result into an OData Result
    final int converterHandle = debugger.startRuntimeMeasurement(this, "convertResult");
    EntityCollection entityCollection;
    try {

      entityCollection = result.asEntityCollection(new JPATupleChildConverter(sd, odata.createUriHelper(),
          serviceMetadata)).get("root");
      debugger.stopRuntimeMeasurement(converterHandle);
    } catch (ODataApplicationException e) {
      debugger.stopRuntimeMeasurement(converterHandle);
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_RESULT_CONV_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }

    // Count results if requested
    final CountOption countOption = uriInfo.getCountOption();
    if (countOption != null && countOption.getValue())
      // TODO SetCount expects an Integer why not a Long?
      entityCollection.setCount(Integer.valueOf(query.countResults().intValue()));

    // 404 Not Found indicates that the resource specified by the request URL does not exist. The response body MAY
    // provide additional information.
    // This is the case for individual property, complex type, a navigation property or entity is not available.
    // See 11.2.6 Requesting Related Entities and 11.2.3 Requesting Individual Properties
    if (isResultEmpty(entityCollection.getEntities()))
      response.setStatusCode(HttpStatusCode.NOT_FOUND.getStatusCode());
    // 200 OK indicates that either a result was found or that the a Entity Collection query had no result
    else if (entityCollection.getEntities() != null) {
      final int serializerHandle = debugger.startRuntimeMeasurement(serializer, "serialize");
      final SerializerResult serializerResult = serializer.serialize(request, entityCollection);
      debugger.stopRuntimeMeasurement(serializerHandle);
      createSuccessResponce(response, responseFormat, serializerResult);
    } else
      // A request returns 204 No Content if the requested resource has the null value, or if the service applies a
      // return=minimal preference. In this case, the response body MUST be empty.
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

    debugger.stopRuntimeMeasurement(handle);
  }

  private boolean isResultEmpty(List<Entity> entities) throws ODataApplicationException {

    if (entities.isEmpty()
        && lastItem.getKind() == UriResourceKind.entitySet
        && !Util.determineKeyPredicates(lastItem).isEmpty())
      // handle ../Organizations('xx')
      return true;
    else if (lastItem.getKind() == UriResourceKind.primitiveProperty
        || lastItem.getKind() == UriResourceKind.navigationProperty
        || lastItem.getKind() == UriResourceKind.complexProperty) {
      if (entities.isEmpty())
        return true;

      Object resultElement = null;
      String name = "";
      if (lastItem.getKind() == UriResourceKind.primitiveProperty) {
        name = Util.determineStartNavigationPath(uriInfo.getUriResourceParts()).getProperty().getName();
        final Property property = entities.get(0).getProperty(name);
        if (property != null) {
          resultElement = property.getValue();
        }
      }
      if (lastItem.getKind() == UriResourceKind.complexProperty) {
        name = Util.determineStartNavigationPath(uriInfo.getUriResourceParts()).getProperty().getName();
        final Property property = entities.get(0).getProperty(name);
        if (property != null) {
          for (Property p : ((ComplexValue) property.getValue()).getValue()) {
            if (p.getValue() != null) {
              resultElement = p;
              break;
            }
          }
        }
      }
      if (lastItem.getKind() == UriResourceKind.navigationProperty
          && !entities.get(0).getProperties().isEmpty()) {
        resultElement = Boolean.FALSE;
      }

      return resultElement == null;
    } else
      return false;
  }

  /**
   * $expand is implemented as a recursively processing of all expands with a DB round trip per expand item.
   * Alternatively also a <i>big</i> join could be created. This would lead to a transport of redundant data, but has
   * only one round trip. It has not been measured under which conditions which solution as the better performance.
   * <p>For a general overview see:
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398298"
   * >OData Version 4.0 Part 1 - 11.2.4.2 System Query Option $expand</a><p>
   * 
   * For a detailed description of the URI syntax see:
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398162"
   * >OData Version 4.0 Part 2 - 5.1.2 System Query Option $expand</a>
   * @param headers
   * @param naviStartEdmEntitySet
   * @param parentHops
   * @param uriResourceInfo
   * @return
   * @throws ODataException
   */
  private Map<JPAAssociationPath, JPAExpandQueryResult> readExpandEntities(final Map<String, List<String>> headers,
      final List<JPANavigationProptertyInfo> parentHops, final UriInfoResource uriResourceInfo)
      throws ODataException {

    final int handle = debugger.startRuntimeMeasurement(this, "readExpandEntities");

    final Map<JPAAssociationPath, JPAExpandQueryResult> allExpResults =
        new HashMap<>();
    // x/a?$expand=b/c($expand=d,e/f)&$filter=...&$top=3&$orderBy=...
    // For performance reasons the expand query should only return results for the results of the higher-level query.
    // The solution for restrictions like a given key or a given filter condition, as it can be propagated to a
    // sub-query.
    // For $top and $skip things are more difficult as the criteria query does not support LIMIT and OFFSET, this is
    // done on the TypedQuery created out of the Criteria Query. In addition not all databases support LIMIT within a
    // sub-query used within EXISTS.

    final List<JPAExpandItemInfo> itemInfoList = new JPAExpandItemInfoFactory()
        .buildExpandItemInfo(sd, uriResourceInfo, parentHops);
    // .buildExpandItemInfo(sd, uriResourceInfo.getUriResourceParts(), uriResourceInfo.getExpandOption(), parentHops);
    for (final JPAExpandItemInfo item : itemInfoList) {
      final JPAExpandQuery expandQuery = new JPAExpandQuery(odata, sessionContext, em, item, headers);
      final JPAExpandQueryResult expandResult = expandQuery.execute();
      if (expandResult.getNoResults() > 0)
        // Only go the next hop if the current one has a result
        expandResult.putChildren(readExpandEntities(headers, item.getHops(), item.getUriInfo()));
      allExpResults.put(item.getExpandAssociation(), expandResult);
    }

    debugger.stopRuntimeMeasurement(handle);
    return allExpResults;
  }
}
