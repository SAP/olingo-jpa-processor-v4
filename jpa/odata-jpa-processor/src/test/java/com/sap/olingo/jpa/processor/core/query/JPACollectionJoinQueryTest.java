package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class JPACollectionJoinQueryTest extends TestBase {

  private TestHelper helper;
  private JPAODataRequestContextAccess requestContext;
  private JPAODataClaimProvider claimProvider;
  private EntityManager em;

  @BeforeEach
  void setup() throws ODataException {
    helper = new TestHelper(emf, PUNIT_NAME);
    requestContext = mock(JPAODataRequestContextAccess.class);
    claimProvider = mock(JPAODataClaimProvider.class);
    em = emf.createEntityManager();

    final JPAServiceDebugger debugger = mock(JPAServiceDebugger.class);
    when(requestContext.getDebugger()).thenReturn(debugger);
    when(requestContext.getClaimsProvider()).thenReturn(Optional.of(claimProvider));
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getHeader()).thenReturn(mock(JPAHttpHeaderMap.class));
    when(requestContext.getRequestParameter()).thenReturn(mock(JPARequestParameterMap.class));
    when(requestContext.getEdmProvider()).thenReturn(helper.edmProvider);
    when(requestContext.getOperationConverter()).thenReturn(new JPADefaultDatabaseProcessor());

  }

  @Test
  void createQuery() throws ODataException {
    final JPACollectionItemInfo item = createBusinessPartnerToComment(null);
    assertNotNull(new JPACollectionJoinQuery(OData.newInstance(), item, requestContext, Optional.empty()));
  }

  @Test
  void createWhereThrowsExceptionOnMissingClaim() throws ODataException {
    final JPACollectionItemInfo item = createBusinessPartnerToComment(null);
    final JPACollectionJoinQuery cut = new JPACollectionJoinQuery(OData.newInstance(), item, requestContext, Optional
        .empty());

    assertThrows(ODataJPAQueryException.class, cut::createWhere);
  }

  @Test
  void createWhereContainsProtected() throws ODataException {
    final CriteriaQuery<Tuple> bp = em.getCriteriaBuilder().createTupleQuery();
    final Root<BusinessPartnerProtected> from = bp.from(BusinessPartnerProtected.class);

    final JPACollectionItemInfo item = createBusinessPartnerToComment(from);
    final JPACollectionJoinQuery cut = new JPACollectionJoinQuery(OData.newInstance(), item, requestContext, Optional
        .empty());

    final List<JPAClaimsPair<String>> claims = Arrays.asList(new JPAClaimsPair<>("Willi"));
    doReturn(claims).when(claimProvider).get("UserId");

    assertNotNull(cut.createWhere());
  }

  private JPACollectionItemInfo createBusinessPartnerToComment(final From<?, ?> from) throws ODataJPAModelException,
      ODataApplicationException {

    final JPACollectionItemInfo item = mock(JPACollectionItemInfo.class);
    final JPAEntityType et = helper.getJPAEntityType(BusinessPartnerProtected.class);
    final UriResourceEntitySet uriEts = mock(UriResourceEntitySet.class);
    final EdmEntityType edmType = mock(EdmEntityType.class);
    final EdmEntitySet edmSet = mock(EdmEntitySet.class);

    final List<JPANavigationPropertyInfo> hops = new ArrayList<>();
    JPANavigationPropertyInfo hop = new JPANavigationPropertyInfo(helper.sd, uriEts,
        et.getCollectionAttribute("Comment").asAssociation(), null);
    hop.setFromClause(from);
    hops.add(hop);
    hop = new JPANavigationPropertyInfo(helper.sd, null, null, et);
    hops.add(hop);

    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    final JPACollectionExpandWrapper wrappedUriInfo = new JPACollectionExpandWrapper(et, uriInfo);

    when(item.getEntityType()).thenReturn(et);
    when(item.getUriInfo()).thenReturn(wrappedUriInfo);
    when(item.getHops()).thenReturn(hops);
    when(item.getExpandAssociation()).thenReturn(et.getCollectionAttribute("Comment").asAssociation());
    when(uriEts.getType()).thenReturn(edmType);
    when(uriEts.getEntitySet()).thenReturn(edmSet);
    when(edmSet.getName()).thenReturn(helper.sd.getEntitySet(et).getExternalName());
    when(edmType.getNamespace()).thenReturn(PUNIT_NAME);
    when(edmType.getName()).thenReturn(et.getExternalName());

    return item;
  }

}
