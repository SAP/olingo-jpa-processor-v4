package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceCount;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPACountExpression;
import com.sap.olingo.jpa.processor.core.filter.JPANullExpression;
import com.sap.olingo.jpa.processor.core.testmodel.Person;

class JPANavigationFilterQueryBuilderTest {
  private JPAServiceDocument sd;
  private EntityManager em;
  private UriResourcePartTyped uriResourceItem;
  private JPAAbstractQuery parent;
  private JPAAssociationPath association;
  private VisitableExpression expression;
  private From<?, ?> from;
  private JPAODataClaimProvider claimsProvider;
  private List<String> groups;
  private CriteriaBuilder cb;

  private JPANavigationPropertyInfoAccess navigationInfo;
  private JPAODataRequestContextAccess context;
  private JPAODataDatabaseOperations dbOperations;
  private JPAEntityType et;
  private EdmEntityType type;
  private AbstractQuery<?> query;
  private Subquery<Integer> subQuery;
  private Root<Person> queryRoot;

  private JPANavigationFilterQueryBuilder cut;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() throws ODataJPAModelException {
    sd = mock(JPAServiceDocument.class);
    em = mock(EntityManager.class);
    uriResourceItem = mock(UriResourcePartTyped.class);
    parent = mock(JPAAbstractQuery.class);
    association = mock(JPAAssociationPath.class);
    from = mock(From.class);
    claimsProvider = mock(JPAODataClaimProvider.class);

    navigationInfo = mock(JPANavigationPropertyInfoAccess.class);
    uriResourceItem = mock(UriResourceNavigation.class);
    context = mock(JPAODataRequestContextAccess.class);
    dbOperations = mock(JPAODataDatabaseOperations.class);
    et = mock(JPAEntityType.class);
    type = mock(EdmEntityType.class);
    query = mock(AbstractQuery.class);
    subQuery = mock(Subquery.class);
    queryRoot = mock(Root.class);
    cb = mock(CriteriaBuilder.class);

    cut = new JPANavigationFilterQueryBuilder(cb);

    when(((UriResourceNavigation) uriResourceItem).getType()).thenReturn(type);
    when(sd.getEntity(type)).thenReturn(et);
    doReturn(Integer.class).when(et).getKeyType();
    doReturn(Person.class).when(et).getTypeClass();
    when(context.getOperationConverter()).thenReturn(dbOperations);
    when(parent.getContext()).thenReturn(context);
    when(parent.getLocale()).thenReturn(Locale.GERMANY);
    doReturn(query).when(parent).getQuery();
    when(query.subquery(Integer.class)).thenReturn(subQuery);
    when(subQuery.from(Person.class)).thenReturn(queryRoot);
    when(navigationInfo.getAssociationPath()).thenReturn(association);
    when(navigationInfo.getUriResource()).thenReturn(uriResourceItem);
  }

  @Test
  void testCreatesFilterQuery() throws ODataApplicationException, ODataJPAModelException {
    final OData o = OData.newInstance();
    buildAssoziationPath();

    cut.setOdata(o)
        .setServiceDocument(sd)
        .setEntityManager(em)
        .setNavigationInfo(navigationInfo)
        .setParent(parent)
        .setFrom(from)
        .setClaimsProvider(claimsProvider)
        .setExpression(expression)
        .setGroups(groups);

    final JPAAbstractSubQuery act = cut.build();
    assertTrue(act instanceof JPANavigationFilterQuery);
    assertNotNull(act);
    assertEquals(o, act.odata);
    assertEquals(sd, act.sd);
    assertEquals(em, act.em);
    assertEquals(from, act.from);
    assertEquals(parent, act.parentQuery);
    assertEquals(association, act.association);
    assertEquals(et, act.jpaEntity);
    assertEquals(Locale.GERMANY, act.locale);
    assertEquals(subQuery, act.subQuery);
    assertTrue(act.claimsProvider.isPresent());
  }

  private static Stream<Arguments> provideCountExpressions() {
    return Stream.of(
        Arguments.of(buildCountFromCountExpression()));
  }

  @ParameterizedTest
  @MethodSource("provideCountExpressions")
  void testCreatesCountQuery(final VisitableExpression exp) throws ODataApplicationException, ODataJPAModelException {
    final OData o = OData.newInstance();
    final var assoziationPath = buildAssoziationPath();
    when(navigationInfo.getAssociationPath()).thenReturn(assoziationPath);

    cut.setOdata(o)
        .setServiceDocument(sd)
        .setEntityManager(em)
        .setNavigationInfo(navigationInfo)
        .setParent(parent)
        .setFrom(from)
        .setClaimsProvider(claimsProvider)
        .setExpression(exp)
        .setGroups(groups);

    final JPAAbstractSubQuery act = cut.build();
    assertTrue(act instanceof JPANavigationCountQuery);
    assertNotNull(act);
    assertEquals(o, act.odata);
    assertEquals(sd, act.sd);
    assertEquals(em, act.em);
    assertEquals(from, act.from);
    assertEquals(parent, act.parentQuery);
    assertEquals(association, act.association);
    assertEquals(et, act.jpaEntity);
    assertEquals(Locale.GERMANY, act.locale);
    assertEquals(subQuery, act.subQuery);
    assertTrue(act.claimsProvider.isPresent());
  }

  private static Stream<Arguments> provideNullExpressions() {
    return Stream.of(
        Arguments.of(buildNullFromNullExpression()));
  }

  @ParameterizedTest
  @MethodSource("provideNullExpressions")
  void testCreatesNullQuery(final VisitableExpression exp) throws ODataApplicationException, ODataJPAModelException {
    final OData o = OData.newInstance();
    final var assoziationPath = buildAssoziationPath();
    when(navigationInfo.getAssociationPath()).thenReturn(assoziationPath);

    cut.setOdata(o)
        .setServiceDocument(sd)
        .setEntityManager(em)
        .setNavigationInfo(navigationInfo)
        .setParent(parent)
        .setFrom(from)
        .setClaimsProvider(claimsProvider)
        .setExpression(exp)
        .setGroups(groups);

    final JPAAbstractSubQuery act = cut.build();
    assertTrue(act instanceof JPANavigationNullQuery);
    assertNotNull(act);
    assertEquals(o, act.odata);
    assertEquals(sd, act.sd);
    assertEquals(em, act.em);
    assertEquals(from, act.from);
    assertEquals(parent, act.parentQuery);
    assertEquals(association, act.association);
    assertEquals(et, act.jpaEntity);
    assertEquals(Locale.GERMANY, act.locale);
    assertEquals(subQuery, act.subQuery);
    assertTrue(act.claimsProvider.isPresent());
  }

  @Test
  void testAsInQueryForProcessorCriteriaBuilder() throws ODataJPAFilterException {
    cb = mock(ProcessorCriteriaBuilder.class);
    cut = new JPANavigationFilterQueryBuilder(cb);
    cut.setExpression(buildCountFromCountExpression());
    assertTrue(cut.asInQuery());
  }

  @Test
  void testAsExistsQueryForProcessorCriteriaBuilderNotCount() throws ODataJPAFilterException {
    cb = mock(ProcessorCriteriaBuilder.class);
    cut = new JPANavigationFilterQueryBuilder(cb);
    assertFalse(cut.asInQuery());
  }

  @Test
  void testAsInQueryTrueForCriteriaBuilderAssociationOneAttribute() throws ODataJPAModelException,
      ODataJPAFilterException, ODataJPAQueryException {

    final JPAPath leftPath = mock(JPAPath.class);
    final JPAAssociationPath associationPath = mock(JPAAssociationPath.class);
    final JPAOnConditionItem onCondition = mock(JPAOnConditionItem.class);
    when(associationPath.getJoinColumnsList()).thenReturn(Collections.singletonList(onCondition));
    when(associationPath.getLeftColumnsList()).thenReturn(Collections.singletonList(leftPath));
    when(navigationInfo.getAssociationPath()).thenReturn(associationPath);
    cut.setNavigationInfo(navigationInfo);
    cut.setExpression(buildCountFromCountExpression());
    assertTrue(cut.asInQuery());
  }

  @Test
  void testAsInQueryFalseForCriteriaBuilderAssociationTwoAttribute() throws ODataJPAModelException,
      ODataJPAFilterException, ODataJPAQueryException {
    final var assoziationPath = buildAssoziationPath();
    when(navigationInfo.getAssociationPath()).thenReturn(assoziationPath);
    cut.setNavigationInfo(navigationInfo);
    cut.setExpression(buildCountFromCountExpression());
    assertFalse(cut.asInQuery());
  }

  @Test
  void testAsInQueryRethrowsException() throws ODataJPAFilterException, ODataJPAModelException, ODataJPAQueryException {

    final JPAAssociationPath associationPath = mock(JPAAssociationPath.class);
    when(associationPath.getLeftColumnsList()).thenThrow(ODataJPAModelException.class);
    when(associationPath.getJoinColumnsList()).thenThrow(ODataJPAModelException.class);
    when(navigationInfo.getAssociationPath()).thenReturn(associationPath);
    cut.setNavigationInfo(navigationInfo);
    assertThrows(ODataJPAFilterException.class, () -> cut.asInQuery());
  }

  private JPAAssociationPath buildAssoziationPath() throws ODataJPAModelException {
    final JPAOnConditionItem onCondition = mock(JPAOnConditionItem.class);
    final JPAElement pathItem = mock(JPAElement.class);
    when(association.getJoinColumnsList()).thenReturn(Arrays.asList(onCondition, onCondition));
    when(association.getPath()).thenReturn(Arrays.asList(pathItem));
    return association;
  }

  private static VisitableExpression buildCountFromCountExpression() {
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    final JPACountExpression exp = mock(JPACountExpression.class);
    final UriResourceCount countPart = mock(UriResourceCount.class);
    final UriResourceNavigation navigationPart = mock(UriResourceNavigation.class);
    final List<UriResource> resources = new ArrayList<>();

    when(exp.getMember()).thenReturn(uriInfo);
    when(uriInfo.getUriResourceParts()).thenReturn(resources);
    when(navigationPart.getKind()).thenReturn(UriResourceKind.navigationProperty);
    when(countPart.getKind()).thenReturn(UriResourceKind.count);
    resources.add(navigationPart);
    resources.add(countPart);

    return exp;
  }

  private static VisitableExpression buildNullFromNullExpression() {
    final Literal literal = mock(Literal.class);
    final UriInfoResource uriInfo = mock(UriInfoResource.class);
    final UriResourceNavigation navigationPart = mock(UriResourceNavigation.class);
    final List<UriResource> resources = new ArrayList<>();
    final JPANullExpression exp = mock(JPANullExpression.class);

    when(exp.getMember()).thenReturn(uriInfo);
    when(exp.getLiteral()).thenReturn(literal);
    when(uriInfo.getUriResourceParts()).thenReturn(resources);
    when(navigationPart.getKind()).thenReturn(UriResourceKind.navigationProperty);
    when(literal.getText()).thenReturn("null");

    return exp;
  }

}
