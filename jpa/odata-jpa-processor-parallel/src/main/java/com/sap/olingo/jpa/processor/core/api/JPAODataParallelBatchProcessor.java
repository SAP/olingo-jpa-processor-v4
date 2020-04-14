package com.sap.olingo.jpa.processor.core.api;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPABatchException.MessageKeys.UNSUPPORTED_BATCH_PARTS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.batch.BatchFacade;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;

import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchRuntimeException;

/**
 * Process parts of a batch request in parallel. This is possible GET requests until the first request with side effects
 * comes. In case a request with side effect is followed by a set of request without side effect, they are processed in
 * parallel again.
 * @author Oliver Grande
 * Created: 27.02.2020
 */
public class JPAODataParallelBatchProcessor extends JPAODataBatchProcessor {

  public JPAODataParallelBatchProcessor(final JPAODataCRUDContextAccess serviceContext,
      final JPAODataRequestContextAccess requestContext) {
    super(serviceContext, requestContext);
  }

  @Override
  protected List<ODataResponsePart> executeBatchParts(final BatchFacade facade,
      final List<BatchRequestPart> requestParts) throws ODataApplicationException, ODataLibraryException {

    try {
      return buildGroups(requestParts).stream()
          .map(JPAODataBatchRequestGroup::execute)
          .flatMap(List::stream)
          .collect(Collectors.toList());
    } catch (ODataJPABatchRuntimeException e) {
      throw new ODataJPABatchException(e);
    }
  }

  List<JPAODataBatchRequestGroup> buildGroups(@Nonnull final List<BatchRequestPart> requestParts)
      throws ODataJPABatchException {
    if (requestParts.isEmpty())
      return Collections.emptyList();

    final List<JPAODataBatchRequestGroup> groups = new ArrayList<>();
    Boolean isGetGroup = null;
    List<BatchRequestPart> groupElements = new ArrayList<>();
    for (final BatchRequestPart part : requestParts) {
      checkPartConsistancy(part);
      if (isGetGroup == null) {
        isGetGroup = !part.isChangeSet() && isGetRequest(part);
      } else if (Boolean.TRUE.equals(isGetGroup) && (!isGetRequest(part) || part.isChangeSet())) {
        if (groupElements.size() == 1)
          groups.add(new JPAODataBatchSequentialRequestGroup(this, groupElements));
        else
          groups.add(new JPAODataBatchParallelRequestGroup(this, groupElements));
        groupElements = new ArrayList<>();
        isGetGroup = Boolean.FALSE;
      } else if (Boolean.FALSE.equals(isGetGroup) && isGetRequest(part)) {
        groups.add(new JPAODataBatchSequentialRequestGroup(this, groupElements));
        groupElements = new ArrayList<>();
        isGetGroup = Boolean.TRUE;
      }
      groupElements.add(part);
    }
    addLastGroup(groups, isGetGroup, groupElements);
    requestContext.getDebugger().debug(this, "Number of groups build: %d", groups.size());
    return groups;
  }

  private void addLastGroup(final List<JPAODataBatchRequestGroup> groups, final Boolean isGetGroup,
      final List<BatchRequestPart> groupElements) {
    if (Boolean.FALSE.equals(isGetGroup) || groupElements.size() == 1)
      groups.add(new JPAODataBatchSequentialRequestGroup(this, groupElements));
    else
      groups.add(new JPAODataBatchParallelRequestGroup(this, groupElements));
  }

  private boolean isGetRequest(final BatchRequestPart part) {
    return part.getRequests().get(0).getMethod() == HttpMethod.GET;
  }

  private void checkPartConsistancy(final BatchRequestPart part) throws ODataJPABatchException {
    if (part.getRequests().size() > 1 && !part.isChangeSet())
      throw new ODataJPABatchException(UNSUPPORTED_BATCH_PARTS, HttpStatusCode.INTERNAL_SERVER_ERROR);
  }

}
