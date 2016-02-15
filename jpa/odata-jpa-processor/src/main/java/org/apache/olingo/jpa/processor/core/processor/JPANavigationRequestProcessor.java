package org.apache.olingo.jpa.processor.core.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.query.JPAExpandItemInfo;
import org.apache.olingo.jpa.processor.core.query.JPAExpandItemInfoFactory;
import org.apache.olingo.jpa.processor.core.query.JPAExpandQuery;
import org.apache.olingo.jpa.processor.core.query.JPAExpandResult;
import org.apache.olingo.jpa.processor.core.query.JPANavigationProptertyInfo;
import org.apache.olingo.jpa.processor.core.query.JPAQuery;
import org.apache.olingo.jpa.processor.core.query.JPATupleResultConverter;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.jpa.processor.core.serializer.JPASerializer;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.CountOption;

public class JPANavigationRequestProcessor extends JPAAbstractRequestProcessor implements JPARequestProcessor {

  public JPANavigationRequestProcessor(OData odata, ServicDocument sd, EntityManager em, UriInfo uriInfo,
      JPASerializer serializer) {
    super(odata, sd, em, uriInfo, serializer);
  }

  @Override
  public void retrieveData(ODataRequest request, ODataResponse response, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(resourceParts);

    // Create a JPQL Query and execute it
    final JPAQuery query = new JPAQuery(odata, targetEdmEntitySet, sd, uriInfo, em, request.getAllHeaders());
    final JPAExpandResult result = query.execute();

    result.putChildren(readExpandEntities(request.getAllHeaders(), null, uriInfo));

    // Convert tuple result into an OData Result
    EntityCollection entityCollection;
    try {
      entityCollection = new JPATupleResultConverter(targetEdmEntitySet, sd, result).getResult();
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Convertion error", HttpStatusCode.INTERNAL_SERVER_ERROR.ordinal(),
          Locale.ENGLISH, e);
    }

    // Count results if requested
    CountOption countOption = uriInfo.getCountOption();
    if (countOption != null && countOption.getValue())
      // TODO SetCount expects an Integer why not a Long?
      entityCollection.setCount(Integer.valueOf(query.countResults().intValue()));

    if (entityCollection.getEntities() != null && entityCollection.getEntities().size() > 0) {
      SerializerResult serializerResult = serializer.serialize(request, entityCollection);
      createSuccessResonce(response, responseFormat, serializerResult);
    } else
      // TODO more fine gain response handling e.g. 204 vs. 404
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

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
   * @throws ODataApplicationException
   */
  private Map<JPAAssociationPath, JPAExpandResult> readExpandEntities(Map<String, List<String>> headers,
      List<JPANavigationProptertyInfo> parentHops, UriInfoResource uriResourceInfo)
          throws ODataApplicationException {
    Map<JPAAssociationPath, JPAExpandResult> allExpResults =
        new HashMap<JPAAssociationPath, JPAExpandResult>();
    // x/a?$expand=b/c($expand=d,e/f)

    List<JPAExpandItemInfo> itemInfoList = new JPAExpandItemInfoFactory()
        .buildExpandItemInfo(sd, uriResourceInfo.getUriResourceParts(), uriResourceInfo.getExpandOption(), parentHops);

    for (JPAExpandItemInfo item : itemInfoList) {
      JPAExpandQuery expandQuery = new JPAExpandQuery(odata, sd, em, item, headers);
      JPAExpandResult expandResult = expandQuery.execute();
      expandResult.putChildren(
          readExpandEntities(headers, item.getHops(), item.getUriInfo()));

      allExpResults.put(item.getExpandAssociation(), expandResult);
    }
    return allExpResults;
  }
}
