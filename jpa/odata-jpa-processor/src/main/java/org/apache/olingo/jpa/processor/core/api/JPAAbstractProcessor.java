package org.apache.olingo.jpa.processor.core.api;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
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
import org.apache.olingo.jpa.processor.core.query.JPAResultConverter;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.jpa.processor.core.serializer.JPASerializerFactory;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.CountOption;

public abstract class JPAAbstractProcessor {
  public static final String ACCESS_MODIFIER_GET = "get";
  public static final String ACCESS_MODIFIER_SET = "set";
  public static final String ACCESS_MODIFIER_IS = "is";

  // TODO eliminate transaction handling
  protected EntityManager em;
  protected final ServicDocument sd;
  protected CriteriaBuilder cb;
  protected OData odata;
  protected ServiceMetadata serviceMetadata;
  protected JPASerializerFactory factory;

  public JPAAbstractProcessor(ServicDocument sd, EntityManager em) {
    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
  }

  public void init(OData odata, ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
    this.factory = new JPASerializerFactory(odata, serviceMetadata);
  }

  /**
   * @param response
   * @param responseFormat
   * @param serializerResult
   */
  protected final void createSuccessResonce(ODataResponse response, ContentType responseFormat,
      SerializerResult serializerResult) {
    response.setContent(serializerResult.getContent());
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
  }

  protected final void readEntityInternal(final ODataRequest request, final ODataResponse response,
      final UriInfo uriInfo, final ContentType responseFormat, JPASerializer serializer) throws SerializerException,
          ODataApplicationException {

    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(resourceParts);

    // Create a JPQL Query and execute it
    final JPAQuery query = new JPAQuery(targetEdmEntitySet, sd, uriInfo, em, request.getAllHeaders());
    final List<Tuple> result = query.execute();

    Map<JPAAssociationPath, JPAExpandResult> allExpResults = readExpandEntities(request.getAllHeaders(), null,
        uriInfo);

    // Convert tuple result into an OData Result
    EntityCollection entityCollection;
    try {
      entityCollection = new JPAResultConverter(targetEdmEntitySet, sd, result, allExpResults)
          .getResult();
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Convertion error", HttpStatusCode.INTERNAL_SERVER_ERROR.ordinal(),
          Locale.ENGLISH, e);
    }

    // Count results if requested
    CountOption countOption = uriInfo.getCountOption();
    if (countOption != null && countOption.getValue())
      // TODO SetCount expects an Integer why not a Long?
      entityCollection.setCount(Integer.valueOf(query.countResults().intValue()));
    // return entityCollection;

    if (entityCollection.getEntities() != null && entityCollection.getEntities().size() > 0) {
      SerializerResult serializerResult = serializer.serialize(request, entityCollection);
      createSuccessResonce(response, responseFormat, serializerResult);
    } else
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
  }

  protected final EntityCollection countEntities(final ODataRequest request, final ODataResponse response,
      final UriInfo uriInfo) throws SerializerException, ODataApplicationException {

    EntityCollection entityCollection = new EntityCollection();
    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(resourceParts);

    final JPAQuery query = new JPAQuery(targetEdmEntitySet, sd, uriInfo, em, request.getAllHeaders());

    entityCollection.setCount(Integer.valueOf(query.countResults().intValue()));
    return entityCollection;
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
      JPAExpandQuery expandQuery = new JPAExpandQuery(sd, em, item, headers);
      JPAExpandResult expandResult = expandQuery.execute();
      expandResult.putChildren(
          readExpandEntities(headers, item.getHops(), item.getUriInfo()));

      allExpResults.put(item.getExpandAssociation(), expandResult);
    }
    return allExpResults;
  }
}
