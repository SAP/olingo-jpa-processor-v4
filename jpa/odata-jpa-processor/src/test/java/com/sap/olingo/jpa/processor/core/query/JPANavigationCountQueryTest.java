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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
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
import com.sap.olingo.jpa.processor.core.testmodel.JoinPartnerRoleRelation;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class JPANavigationCountQueryTest extends TestBase {
  private JPANavigationSubQuery cut;
  private TestHelper helper;
  private EntityManager em;
  private OData odata;
  private UriResourceNavigation uriResourceItem;
  private JPAAbstractQuery parent;
  private JPAAssociationPath association;
  private From<?, ?> from;
  private Root<JoinPartnerRoleRelation> queryJoinTable;
  private Root<BusinessPartnerRoleProtected> queryRoot;
  private Root<BusinessPartnerProtected> parentRoot;
  private JPAODataClaimProvider claimsProvider;
  private EdmEntityType edmEntityType;
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
    association = helper.getJPAAssociationPath(BusinessPartnerProtected.class, "RolesJoinProtected");
    claimsProvider = mock(JPAODataClaimProvider.class);
    odata = OData.newInstance();
    uriResourceItem = mock(UriResourceNavigation.class);
    edmEntityType = mock(EdmEntityType.class);
    cq = mock(CriteriaQuery.class);
    cb = mock(CriteriaBuilder.class); // emf.getCriteriaBuilder();
    subQuery = mock(Subquery.class);
    queryJoinTable = mock(Root.class);
    queryRoot = mock(Root.class);
    parentRoot = mock(Root.class);
    from = mock(From.class);
    requestContext = mock(JPAODataRequestContextAccess.class);

    final UriParameter key = mock(UriParameter.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(uriResourceItem.getType()).thenReturn(edmEntityType);
    when(uriResourceItem.getKeyPredicates()).thenReturn(Collections.singletonList(key));
    when(edmEntityType.getName()).thenReturn("BusinessPartnerRoleProtected");
    when(edmEntityType.getNamespace()).thenReturn(PUNIT_NAME);
    when(parent.getQuery()).thenReturn(cq);
    when(cq.subquery(any())).thenReturn(subQuery);
    when(subQuery.from(JoinPartnerRoleRelation.class)).thenReturn(queryJoinTable);
    when(subQuery.from(BusinessPartnerRoleProtected.class)).thenReturn(queryRoot);
    when(claimsProvider.get("RoleCategory")).thenReturn(Collections.singletonList(new JPAClaimsPair<>("A")));
    doReturn(BusinessPartnerProtected.class).when(from).getJavaType();
  }

  @Test
  void testCutExists() throws ODataApplicationException {

    cut = new JPANavigationCountQuery(odata, helper.sd, edmEntityType, em, parent, from, association, Optional.of(
        claimsProvider), Collections.emptyList());
    assertNotNull(cut);
  }

  @Test
  void testGetSubQueryThrowsExceptionWhenChildQueryProvided() throws ODataApplicationException {

    cut = new JPANavigationCountQuery(odata, helper.sd, edmEntityType, em, parent, from, association, Optional.of(
        claimsProvider), Collections.emptyList());
    assertThrows(ODataJPAQueryException.class, () -> cut.getSubQuery(subQuery, null));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testJoinQueryAggregateWithClaim() throws ODataApplicationException, ODataJPAModelException,
      EdmPrimitiveTypeException {
    final Member member = mock(Member.class);
    final UriInfoResource uriInfoResource = mock(UriInfoResource.class);
    final UriResource uriResource = mock(UriResource.class);
    final Literal literal = mock(Literal.class);
    final EdmPrimitiveType edmType = mock(EdmPrimitiveType.class);
    final JPAFilterExpression expression = new JPAFilterExpression(member, literal, BinaryOperatorKind.EQ);
    final JPAODataDatabaseOperations converterExtension = mock(JPAODataDatabaseOperations.class);
    final Join<Object, Object> innerJoin = mock(Join.class);
    final Path<Object> roleCategoryPath = mock(Path.class);
    final Path<Object> idPath = mock(Path.class);
    final Path<Object> keyPath = mock(Path.class);
    final Path<Object> sourceIdPath = mock(Path.class);
    final Predicate equalExpression1 = mock(Predicate.class);
    when(parent.getContext()).thenReturn(requestContext);
    when(parent.getJpaEntity()).thenReturn(helper.getJPAEntityType(BusinessPartnerProtected.class));
    when(requestContext.getOperationConverter()).thenReturn(converterExtension);
    when(member.getResourcePath()).thenReturn(uriInfoResource);
    when(uriInfoResource.getUriResourceParts()).thenReturn(Collections.singletonList(uriResource));
    when(uriResource.getKind()).thenReturn(UriResourceKind.count);

    when(subQuery.from(BusinessPartnerProtected.class)).thenReturn(parentRoot);
    when(parentRoot.join("rolesJoinProtected", JoinType.LEFT)).thenReturn(innerJoin);

    when(literal.getText()).thenReturn("1");
    when(literal.getType()).thenReturn(edmType);
    when(edmType.valueOfString(any(), any(), any(), any(), any(), any(), any())).thenReturn(Integer.valueOf(1));

    when(queryRoot.get("roleCategory")).thenReturn(roleCategoryPath);
    when(queryJoinTable.get("key")).thenReturn(keyPath);
    when(parentRoot.get("key")).thenReturn(keyPath);
    when(from.get("iD")).thenReturn(idPath);
    when(keyPath.get("sourceID")).thenReturn(sourceIdPath);
    when(cb.equal(keyPath, keyPath)).thenReturn(equalExpression1);

    when(innerJoin.get("roleCategory")).thenReturn(roleCategoryPath);

    cut = new JPANavigationCountQuery(odata, helper.sd, edmEntityType, em, parent, from, association, Optional.of(
        claimsProvider), Collections.emptyList());
    cut.buildExpression(expression, Collections.emptyList());

    cut.getSubQuery(null, null);
    assertNotNull(cut);
    verify(cb).equal(sourceIdPath, idPath);
    verify(cb).equal(roleCategoryPath, "A");
  }
}
