package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;

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
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterExpression;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerProtected;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRoleProtected;
import com.sap.olingo.jpa.processor.core.testmodel.JoinPartnerRoleRelation;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

abstract class JPANavigationCountQueryTest extends TestBase {

  protected JPANavigationSubQuery cut;
  protected TestHelper helper;
  protected EntityManager em;
  protected OData odata;
  protected UriResourceNavigation uriResourceItem;
  protected JPAAbstractQuery parent;
  protected JPAAssociationPath association;
  protected From<?, ?> from;

  protected JPAODataClaimProvider claimsProvider;
  protected JPAEntityType jpaEntityType;
  @SuppressWarnings("rawtypes")
  protected CriteriaQuery cq;
  protected CriteriaBuilder cb;
  protected Subquery<Comparable<?>> subQuery;
  protected JPAODataRequestContextAccess requestContext;

  public JPANavigationCountQueryTest() {
    super();
  }

  @SuppressWarnings("unchecked")
  @BeforeEach
  public void setup() throws ODataException, ODataJPAIllegalAccessException {
    helper = getHelper();
    em = mock(EntityManager.class);
    parent = mock(JPAAbstractQuery.class);

    claimsProvider = mock(JPAODataClaimProvider.class);
    odata = OData.newInstance();
    uriResourceItem = mock(UriResourceNavigation.class);
    cq = mock(CriteriaQuery.class);
    cb = mock(CriteriaBuilder.class); // emf.getCriteriaBuilder();
    subQuery = createSubQuery();
    from = mock(From.class);
    requestContext = mock(JPAODataRequestContextAccess.class);

    final UriParameter key = mock(UriParameter.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(uriResourceItem.getKeyPredicates()).thenReturn(Collections.singletonList(key));
    when(parent.getQuery()).thenReturn(cq);
    when(cq.<Comparable<?>> subquery(any())).thenReturn(subQuery);
    when(claimsProvider.get("RoleCategory")).thenReturn(Collections.singletonList(new JPAClaimsPair<>("A")));
    doReturn(BusinessPartnerProtected.class).when(from).getJavaType();
  }

  protected void createEdmEntityType(final Class<?> clazz) throws ODataJPAModelException {
    jpaEntityType = helper.getJPAEntityType(clazz);
  }

  protected abstract Subquery<Comparable<?>> createSubQuery();

  @Test
  void testCutExists() throws ODataApplicationException, ODataJPAModelException {
    createEdmEntityType(BusinessPartnerRoleProtected.class);
    cut = createCut();
    assertNotNull(cut);
  }

  protected abstract JPANavigationSubQuery createCut() throws ODataApplicationException;

  @Test
  void testGetSubQueryThrowsExceptionWhenChildQueryProvided() throws ODataApplicationException, ODataJPAModelException {

    createEdmEntityType(BusinessPartnerRoleProtected.class);
    cut = createCut();
    assertThrows(ODataJPAQueryException.class, () -> cut.getSubQuery(subQuery, null, Collections.emptyList()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testQueryWithJoinTableAggregateWithClaim() throws ODataApplicationException, ODataJPAModelException,
      EdmPrimitiveTypeException, ODataJPAIllegalAccessException {

    association = helper.getJPAAssociationPath(BusinessPartnerProtected.class, "RolesJoinProtected");
    createEdmEntityType(BusinessPartnerRoleProtected.class);
    final Root<JoinPartnerRoleRelation> queryJoinTable = mock(Root.class);
    final Root<BusinessPartnerRoleProtected> queryRoot = mock(Root.class);
    final Root<BusinessPartnerProtected> parentRoot = mock(Root.class);
    when(subQuery.from(JoinPartnerRoleRelation.class)).thenReturn(queryJoinTable);
    when(subQuery.from(BusinessPartnerRoleProtected.class)).thenReturn(queryRoot);

    final JPAFilterExpression expression = createCountFilter();
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
    when(idPath.getAlias()).thenReturn("iD");

    when(subQuery.from(BusinessPartnerProtected.class)).thenReturn(parentRoot);
    when(parentRoot.join("rolesJoinProtected", JoinType.LEFT)).thenReturn(innerJoin);

    when(queryRoot.get("roleCategory")).thenReturn(roleCategoryPath);
    when(queryJoinTable.get("key")).thenReturn(keyPath);
    when(parentRoot.get("key")).thenReturn(keyPath);
    when(from.get("iD")).thenReturn(idPath);
    when(keyPath.get("sourceID")).thenReturn(sourceIdPath);
    when(cb.equal(keyPath, keyPath)).thenReturn(equalExpression1);

    when(innerJoin.get("roleCategory")).thenReturn(roleCategoryPath);

    cut = createCut();
    cut.buildExpression(expression, Collections.emptyList());

    cut.getSubQuery(null, null, Collections.emptyList());
    assertAggregateClaims(roleCategoryPath, idPath, sourceIdPath, cut.getLeftPaths());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testQueryMultipleJoinColumns() throws ODataApplicationException, ODataJPAModelException,
      EdmPrimitiveTypeException, ODataJPAIllegalAccessException {

    association = helper.getJPAAssociationPath(AdministrativeDivision.class, "Children");
    createEdmEntityType(AdministrativeDivision.class);

    final Root<AdministrativeDivision> queryRoot = mock(Root.class);
    when(subQuery.from(AdministrativeDivision.class)).thenReturn(queryRoot);

    final JPAODataDatabaseOperations converterExtension = mock(JPAODataDatabaseOperations.class);
    when(parent.getContext()).thenReturn(requestContext);
    when(parent.getJpaEntity()).thenReturn(helper.getJPAEntityType(AdministrativeDivision.class));
    when(requestContext.getOperationConverter()).thenReturn(converterExtension);

    final JPAFilterExpression expression = createCountFilter();
    createAttributePath(queryRoot, "codePublisher", "codeID", "divisionCode", "parentCodeID", "parentDivisionCode");
    createAttributePath(from, "codePublisher", "codeID", "divisionCode", "parentCodeID", "parentDivisionCode");

    cut = createCut();
    cut.buildExpression(expression, Collections.emptyList());
    cut.getSubQuery(null, null, Collections.emptyList());

    assertNotNull(cut);
    assertMultipleJoinColumns(subQuery, cut.getLeftPaths());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testQueryOneJoinColumnsWithClaims() throws ODataApplicationException, ODataJPAModelException,
      EdmPrimitiveTypeException, ODataJPAIllegalAccessException {

    association = helper.getJPAAssociationPath(BusinessPartnerProtected.class, "RolesProtected");
    createEdmEntityType(BusinessPartnerRoleProtected.class);

    final Path<Object> roleCategoryPath = mock(Path.class);
    final Root<BusinessPartnerRoleProtected> queryRoot = mock(Root.class);
    when(subQuery.from(BusinessPartnerRoleProtected.class)).thenReturn(queryRoot);
    when(queryRoot.get("roleCategory")).thenReturn(roleCategoryPath);

    final JPAODataDatabaseOperations converterExtension = mock(JPAODataDatabaseOperations.class);
    when(parent.getContext()).thenReturn(requestContext);
    when(parent.getJpaEntity()).thenReturn(helper.getJPAEntityType(BusinessPartnerProtected.class));
    when(requestContext.getOperationConverter()).thenReturn(converterExtension);

    final JPAFilterExpression expression = createCountFilter();
    createAttributePath(queryRoot, "businessPartnerID");
    final Predicate equalExpression1 = mock(Predicate.class);
    when(cb.equal(roleCategoryPath, "A")).thenReturn(equalExpression1);
    createAttributePath(from, "iD");
    cut = createCut();
    cut.buildExpression(expression, Collections.emptyList());
    cut.getSubQuery(null, null, Collections.emptyList());

    assertNotNull(cut);
    assertOneJoinColumnsWithClaims(subQuery, equalExpression1, cut.getLeftPaths());
  }

  @Test
  void testGetLeftOnEarlyAccess() throws ODataApplicationException, ODataJPAIllegalAccessException,
      ODataJPAModelException {
    createEdmEntityType(BusinessPartnerRoleProtected.class);
    cut = createCut();
    assertLeftEarlyAccess();
  }

  protected abstract void assertLeftEarlyAccess() throws ODataJPAIllegalAccessException;

  protected abstract void assertOneJoinColumnsWithClaims(final Subquery<Comparable<?>> subQuery,
      final Predicate equalExpression, final List<Path<Comparable<?>>> paths);

  protected abstract void assertMultipleJoinColumns(final Subquery<Comparable<?>> subQuery,
      final List<Path<Comparable<?>>> paths);

  protected abstract void assertAggregateClaims(final Path<Object> roleCategoryPath, final Path<Object> idPath,
      final Path<Object> sourceIdPath, final List<Path<Comparable<?>>> paths);

  protected JPAFilterExpression createCountFilter()
      throws EdmPrimitiveTypeException {

    final Literal literal = mock(Literal.class);
    final EdmPrimitiveType edmType = mock(EdmPrimitiveType.class);
    final UriInfoResource uriInfoResource = mock(UriInfoResource.class);
    final UriResource uriResource = mock(UriResource.class);
    final Member member = mock(Member.class);
    final JPAFilterExpression expression = new JPAFilterExpression(member, literal, BinaryOperatorKind.EQ);
    when(uriInfoResource.getUriResourceParts()).thenReturn(Collections.singletonList(uriResource));
    when(uriResource.getKind()).thenReturn(UriResourceKind.count);
    when(member.getResourcePath()).thenReturn(uriInfoResource);
    when(literal.getText()).thenReturn("1");
    when(literal.getType()).thenReturn(edmType);
    when(edmType.valueOfString(any(), any(), any(), any(), any(), any(), any())).thenReturn(Integer.valueOf(1));
    return expression;
  }

  @SuppressWarnings("unchecked")
  protected void createAttributePath(final From<?, ?> from, final String... names) {
    for (final String name : names) {
      final Path<Object> path = mock(jakarta.persistence.criteria.Path.class);
      when(from.get(name)).thenReturn(path);
      when(path.getAlias()).thenReturn(name);
    }
  }

  protected void assertContainsPath(final List<?> pathList, final int expSize, final String... names) {
    final List<String> selections = pathList.stream().map(path -> ((Selection<?>) path).getAlias()).toList();
    assertEquals(expSize, pathList.size());
    for (final String name : names) {
      assertTrue(selections.contains(name));
    }
  }

}