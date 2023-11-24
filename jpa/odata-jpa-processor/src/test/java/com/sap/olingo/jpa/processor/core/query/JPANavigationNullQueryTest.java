package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterExpression;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRoleProtected;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class JPANavigationNullQueryTest extends TestBase {
  private JPANavigationSubQuery cut;
  private TestHelper helper;
  private EntityManager em;
  private OData odata;
  private UriResourceNavigation uriResourceItem;
  private JPAAbstractQuery parent;
  private JPAAssociationPath association;
  private From<?, ?> from;
  private Root<BusinessPartnerProtected> queryRoot;
  private JPAODataClaimProvider claimsProvider;
  private JPAEntityType jpaEntityType;
  @SuppressWarnings("rawtypes")
  private CriteriaQuery cq;
  private CriteriaBuilder cb;
  private Subquery<Object> subQuery;
  private JPAODataRequestContextAccess requestContext;

  @SuppressWarnings("unchecked")
  @BeforeEach
  public void setup() throws ODataException, ODataJPAIllegalAccessException {
    helper = getHelper();
    em = mock(EntityManager.class);
    parent = mock(JPAAbstractQuery.class);
    association = helper.getJPAAssociationPath(BusinessPartnerRoleProtected.class, "BupaPartnerProtected");
    claimsProvider = mock(JPAODataClaimProvider.class);
    odata = OData.newInstance();
    uriResourceItem = mock(UriResourceNavigation.class);
    jpaEntityType = helper.getJPAEntityType(BusinessPartnerProtected.class);
    cq = mock(CriteriaQuery.class);
    cb = mock(CriteriaBuilder.class); // emf.getCriteriaBuilder();
    subQuery = mock(Subquery.class);
    queryRoot = mock(Root.class);
    from = mock(From.class);
    requestContext = mock(JPAODataRequestContextAccess.class);

    final UriParameter key = mock(UriParameter.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    // when(uriResourceItem.getType()).thenReturn(jpaEntityType);
    when(uriResourceItem.getKeyPredicates()).thenReturn(Collections.singletonList(key));
    when(parent.getQuery()).thenReturn(cq);
    when(cq.subquery(any())).thenReturn(subQuery);
    when(subQuery.from(BusinessPartnerProtected.class)).thenReturn(queryRoot);
    when(claimsProvider.get("UserId")).thenReturn(Collections.singletonList(new JPAClaimsPair<>("Willi")));
    doReturn(BusinessPartnerProtected.class).when(from).getJavaType();
  }

  @Test
  void testCutExists() throws ODataApplicationException {

    cut = new JPANavigationNullQuery(odata, helper.sd, jpaEntityType, em, parent, from, association, Optional.of(
        claimsProvider), Collections.emptyList());
    assertNotNull(cut);
  }

  @Test
  void testGetSubQueryThrowsExceptionWhenChildQueryProvided() throws ODataApplicationException {

    cut = new JPANavigationNullQuery(odata, helper.sd, jpaEntityType, em, parent, from, association, Optional.of(
        claimsProvider), Collections.emptyList());
    assertThrows(ODataJPAQueryException.class, () -> cut.getSubQuery(subQuery, null, Collections.emptyList()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testJoinQueryAggregateWithClaim() throws ODataApplicationException, ODataJPAModelException,
      EdmPrimitiveTypeException {
    final Member member = mock(Member.class);
    final UriInfoResource uriInfoResource = mock(UriInfoResource.class);
    final Literal literal = mock(Literal.class);
    final EdmPrimitiveType edmType = mock(EdmPrimitiveType.class);
    final JPAFilterExpression expression = new JPAFilterExpression(member, literal, BinaryOperatorKind.EQ);
    final JPAODataDatabaseOperations converterExtension = mock(JPAODataDatabaseOperations.class);
    final Path<Object> userNamePath = mock(Path.class);
    final Path<Object> idPath = mock(Path.class);
    final Predicate equalExpression1 = mock(Predicate.class);
    when(parent.getContext()).thenReturn(requestContext);
    when(parent.getJpaEntity()).thenReturn(helper.getJPAEntityType(BusinessPartnerRoleProtected.class));
    when(requestContext.getOperationConverter()).thenReturn(converterExtension);
    when(member.getResourcePath()).thenReturn(uriInfoResource);

    when(literal.getText()).thenReturn("1");
    when(literal.getType()).thenReturn(edmType);
    when(edmType.valueOfString(any(), any(), any(), any(), any(), any(), any())).thenReturn(Integer.valueOf(1));

    when(queryRoot.get("userName")).thenReturn(userNamePath);
    when(queryRoot.get("iD")).thenReturn(idPath);
    when(from.get("businessPartnerID")).thenReturn(idPath);
    when(cb.equal(idPath, idPath)).thenReturn(equalExpression1);

    cut = new JPANavigationNullQuery(odata, helper.sd, jpaEntityType, em, parent, from, association, Optional.of(
        claimsProvider), Collections.emptyList());
    cut.buildExpression(expression, Collections.emptyList());

    cut.getSubQuery(null, null, Collections.emptyList());
    assertNotNull(cut);
    verify(cb).equal(idPath, idPath);
    verify(cb).equal(userNamePath, "Willi");
  }
}
