package org.apache.olingo.jpa.processor.core.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.query.JPAExpandItemInfo;
import org.apache.olingo.jpa.processor.core.query.JPAExpandItemInfoFactory;
import org.apache.olingo.jpa.processor.core.query.JPAExpandQuery;
import org.apache.olingo.jpa.processor.core.query.JPAExpandQueryResult;
import org.apache.olingo.jpa.processor.core.query.JPANavigationProptertyInfo;
import org.apache.olingo.jpa.processor.core.query.JPAQuery;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.org.jpa.processor.core.converter.JPATupleResultConverter;

public class JPANavigationRequestProcessor extends JPAAbstractRequestProcessor implements JPARequestProcessor {
  private final ServiceMetadata serviceMetadata;

  public JPANavigationRequestProcessor(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataSessionContextAccess context, final JPAODataRequestContextAccess requestContext)
      throws ODataException {
    super(odata, context, requestContext);
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public void retrieveData(final ODataRequest request, final ODataResponse response, final ContentType responseFormat)
      throws ODataException {

    final int handle = debugger.startRuntimeMeasurement("JPANavigationRequestProcessor", "retrieveData");

    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(resourceParts);

    // Create a JPQL Query and execute it
    JPAQuery query = null;
    try {
      query = new JPAQuery(odata, targetEdmEntitySet, context, uriInfo, em, request.getAllHeaders());
    } catch (ODataJPAModelException e) {
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_PREPARATION_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    } catch (ODataException e) {
      debugger.stopRuntimeMeasurement(handle);
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_PREPARATION_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }

    final JPAExpandQueryResult result = query.execute();
    result.putChildren(readExpandEntities(request.getAllHeaders(), null, uriInfo));
    // Convert tuple result into an OData Result
    final int converterHandle = debugger.startRuntimeMeasurement("JPATupleResultConverter", "getResult");
    EntityCollection entityCollection;
    try {
      entityCollection = new JPATupleResultConverter(sd, result, odata.createUriHelper(), serviceMetadata)
          .getResult();
      debugger.stopRuntimeMeasurement(converterHandle);
    } catch (ODataJPAModelException e) {
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

    if (entityCollection.getEntities() != null && entityCollection.getEntities().size() > 0) {
      final int serializerHandle = debugger.startRuntimeMeasurement("JPASerializer", "serialize");
      final SerializerResult serializerResult = serializer.serialize(request, entityCollection);
      debugger.stopRuntimeMeasurement(serializerHandle);
      createSuccessResonce(response, responseFormat, serializerResult);
    } else
      // 404 Not Found indicates that the resource specified by the request URL does not exist. The response body MAY
      // provide additional information.
      // A request returns 204 No Content if the requested resource has the null value, or if the service applies a
      // return=minimal preference. In this case, the response body MUST be empty.
      // Assumption 404 is handled by Olingo during URL parsing
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

    debugger.stopRuntimeMeasurement(handle);
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

    final int handle = debugger.startRuntimeMeasurement("JPANavigationRequestProcessor", "readExpandEntities");

    final Map<JPAAssociationPath, JPAExpandQueryResult> allExpResults =
        new HashMap<JPAAssociationPath, JPAExpandQueryResult>();
    // x/a?$expand=b/c($expand=d,e/f)

    final List<JPAExpandItemInfo> itemInfoList = new JPAExpandItemInfoFactory()
        .buildExpandItemInfo(sd, uriResourceInfo.getUriResourceParts(), uriResourceInfo.getExpandOption(), parentHops);

    for (final JPAExpandItemInfo item : itemInfoList) {
      final JPAExpandQuery expandQuery = new JPAExpandQuery(odata, context, em, item, headers);
      final JPAExpandQueryResult expandResult = expandQuery.execute();

      expandResult.putChildren(readExpandEntities(headers, item.getHops(), item.getUriInfo()));
      allExpResults.put(item.getExpandAssociation(), expandResult);
    }

    debugger.stopRuntimeMeasurement(handle);
    return allExpResults;
  }
}
