package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRoleProtected;
import com.sap.olingo.jpa.processor.core.testmodel.JoinPartnerRoleRelation;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

class JPANavigationFilterQueryTest extends TestBase {
  private JPAAbstractSubQuery cut;
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
    cut = new JPANavigationFilterQuery(odata, helper.sd, uriResourceItem,
        parent, em, association, from, Optional.of(claimsProvider));
    assertNotNull(cut);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testJoinQueryWithClaim() throws ODataApplicationException {
    final Path<Object> keyPath = mock(Path.class);
    final Path<Object> sourcePath = mock(Path.class);
    final Path<Object> targetPath = mock(Path.class);
    final Path<Object> roleIdPath = mock(Path.class);
    final Path<Object> roleCategoryPath = mock(Path.class);
    final Path<Object> idPath = mock(Path.class);
    final Predicate equalExpression1 = mock(Predicate.class);
    final Predicate equalExpression2 = mock(Predicate.class);
    final Predicate equalExpression3 = mock(Predicate.class);
    final Predicate andExpression1 = mock(Predicate.class);
    final Predicate andExpression2 = mock(Predicate.class);
    when(queryJoinTable.get("key")).thenReturn(keyPath);
    when(keyPath.get("sourceID")).thenReturn(sourcePath);
    when(keyPath.get("targetID")).thenReturn(targetPath);
    when(from.get("iD")).thenReturn(idPath);
    when(cb.equal(idPath, sourcePath)).thenReturn(equalExpression1);
    when(cb.equal(sourcePath, idPath)).thenReturn(equalExpression1);

    when(queryRoot.get("businessPartnerID")).thenReturn(roleIdPath);
    when(queryRoot.get("roleCategory")).thenReturn(roleCategoryPath);
    when(queryRoot.get("iD")).thenReturn(idPath);
    when(cb.equal(idPath, sourcePath)).thenReturn(equalExpression1);
    when(cb.equal(sourcePath, idPath)).thenReturn(equalExpression1);

    when(cb.equal(roleIdPath, sourcePath)).thenReturn(equalExpression2);
    when(cb.equal(sourcePath, roleIdPath)).thenReturn(equalExpression2);
    when(cb.equal(roleCategoryPath, targetPath)).thenReturn(equalExpression3);
    when(cb.equal(targetPath, roleCategoryPath)).thenReturn(equalExpression3);
    when(cb.and(equalExpression2, equalExpression3)).thenReturn(andExpression1);
    when(cb.and(equalExpression1, andExpression1)).thenReturn(andExpression2);

    cut = new JPANavigationFilterQuery(odata, helper.sd, uriResourceItem,
        parent, em, association, from, Optional.of(claimsProvider));
    @SuppressWarnings("unused")
    final Subquery<Object> act = cut.getSubQuery(subQuery, null);
    verify(subQuery).select(roleIdPath);
    verify(subQuery).where(andExpression2);
    verify(cb).equal(roleCategoryPath, targetPath);
    verify(cb).equal(roleCategoryPath, "A");
  }
}
