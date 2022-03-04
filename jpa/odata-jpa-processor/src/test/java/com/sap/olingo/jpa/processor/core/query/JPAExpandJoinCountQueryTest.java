package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.processor.JPAEmptyDebugger;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class JPAExpandJoinCountQueryTest extends TestBase {
  private JPAExpandJoinCountQuery cut;
  private OData odata;
  private JPAODataRequestContextAccess requestContext;
  private JPAEdmProvider edmProvider;
  private JPAServiceDocument sd;
  private JPAEntityType et;
  private JPAAssociationPath association;
  private List<JPANavigationPropertyInfo> hops;
  private Optional<JPAKeyBoundary> keyBoundary;

  @BeforeEach
  void setup() throws ODataException {
    odata = OData.newInstance();
    edmProvider = mock(JPAEdmProvider.class);
    sd = mock(JPAServiceDocument.class);
    requestContext = mock(JPAODataRequestContextAccess.class);
    et = mock(JPAEntityType.class);
    association = mock(JPAAssociationPath.class);
    hops = new ArrayList<>();
    keyBoundary = Optional.empty();

    when(edmProvider.getServiceDocument()).thenReturn(sd);
    when(requestContext.getEntityManager()).thenReturn(emf.createEntityManager());
    when(requestContext.getDebugger()).thenReturn(new JPAEmptyDebugger());
    when(requestContext.getEdmProvider()).thenReturn(edmProvider);
  }

  @Test
  void testCreateCountQuery() {
    assertDoesNotThrow(() -> new JPAExpandJoinCountQuery(odata, requestContext, et, association, hops,
        keyBoundary));
  }

  @Test
  void testExecuteReturnsNull() throws ODataException {
    cut = new JPAExpandJoinCountQuery(odata, requestContext, et, association, hops, keyBoundary);
    assertNull(cut.execute());
  }

  @Test
  void testAssociationReturnsSecondLast() throws ODataException {
    final JPAAssociationPath exp = mock(JPAAssociationPath.class);
    final List<JPANavigationPropertyInfo> parentHops = new ArrayList<>();
    final JPAExpandItem uriInfo = mock(JPAExpandItem.class);

    parentHops.add(createHop());
    parentHops.add(createHop());

    final JPAInlineItemInfo itemInfo = new JPACollectionItemInfo(sd, uriInfo, exp, parentHops);

    cut = new JPAExpandJoinCountQuery(odata, requestContext, et, association, hops, keyBoundary);
    final JPAAssociationPath act = cut.getAssociation(itemInfo);
    assertEquals(exp, act);
  }

  @Test
  void testConvertCountResultCanHandleInteger() throws ODataException {
    final List<Tuple> intermediateResult = new ArrayList<>();
    final Tuple row = mock(Tuple.class);
    when(row.get(JPAAbstractQuery.COUNT_COLUMN_NAME)).thenReturn(Integer.valueOf(5));
    intermediateResult.add(row);

    cut = new JPAExpandJoinCountQuery(odata, requestContext, et, association, hops, keyBoundary);
    final Map<String, Long> act = cut.convertCountResult(intermediateResult);

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals(5L, act.get(""));
  }

  @Test
  void testConvertCountResultCanHandleLong() throws ODataException {
    final List<Tuple> intermediateResult = new ArrayList<>();
    final Tuple row = mock(Tuple.class);
    when(row.get(JPAAbstractQuery.COUNT_COLUMN_NAME)).thenReturn(Long.valueOf(5));
    intermediateResult.add(row);

    cut = new JPAExpandJoinCountQuery(odata, requestContext, et, association, hops, keyBoundary);
    final Map<String, Long> act = cut.convertCountResult(intermediateResult);

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals(5L, act.get(""));
  }
  
  private JPANavigationPropertyInfo createHop(final JPAAssociationPath exp) {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    return new JPANavigationPropertyInfo(sd, exp, uriInfo, et);
  }

  private JPANavigationPropertyInfo createHop() {
    return createHop(null);
  }
}
