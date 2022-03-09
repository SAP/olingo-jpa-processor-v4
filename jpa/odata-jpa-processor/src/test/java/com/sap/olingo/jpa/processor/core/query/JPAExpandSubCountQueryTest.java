package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class JPAExpandSubCountQueryTest extends TestBase {
  private JPAExpandSubCountQuery cut;
  private OData odata;
  private JPAEdmProvider edmProvider;
  private JPAODataRequestContextAccess requestContext;
  private JPAEntityType et;
  private JPAAssociationPath association;
  private List<JPANavigationPropertyInfo> hops;
  private JPAServiceDocument sd;

  @BeforeEach
  void setup() throws ODataException {
    odata = OData.newInstance();
    edmProvider = mock(JPAEdmProvider.class);
    requestContext = mock(JPAODataRequestContextAccess.class);
    et = mock(JPAEntityType.class);
    sd = mock(JPAServiceDocument.class);
    association = mock(JPAAssociationPath.class);
    hops = new ArrayList<>();

    when(requestContext.getEntityManager()).thenReturn(emf.createEntityManager());
    when(edmProvider.getServiceDocument()).thenReturn(sd);
    when(requestContext.getEdmProvider()).thenReturn(edmProvider);
  }

  @Test
  void testConvertCountResultCanHandleInteger() throws ODataException {
    final List<Tuple> intermediateResult = new ArrayList<>();
    final Tuple row = mock(Tuple.class);
    when(row.get(JPAAbstractQuery.COUNT_COLUMN_NAME)).thenReturn(Integer.valueOf(5));
    intermediateResult.add(row);

    cut = new JPAExpandSubCountQuery(odata, requestContext, et, association, hops);

    final Map<String, Long> act = cut.convertCountResult(intermediateResult);

    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals(5L, act.get(""));
  }
}
