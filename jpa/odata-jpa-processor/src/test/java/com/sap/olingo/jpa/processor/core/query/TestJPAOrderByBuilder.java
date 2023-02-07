package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;

import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceCount;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.eclipse.persistence.internal.jpa.querydef.FunctionExpressionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.exception.ODataJPANotImplementedException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerWithGroups;
import com.sap.olingo.jpa.processor.core.testmodel.CollectionDeep;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAOrderByBuilder extends TestBase {
  private JPAOrderByBuilder cut;
  private Map<String, From<?, ?>> joinTables;
  private UriInfoResource uriResource;
  private TopOption top;
  private SkipOption skip;
  private OrderByOption orderBy;
  private From<?, ?> adminTarget;
  private From<?, ?> orgTarget;
  private CriteriaBuilder cb;
  private JPAEntityType jpaAdminEntity;
  private JPAEntityType jpaOrgEntity;
  private List<String> groups;
  private JPAODataPage page;

  @BeforeEach
  void setup() throws ODataJPAModelException, ODataException {
    cb = emf.getCriteriaBuilder();
    jpaAdminEntity = getHelper().getJPAEntityType(AdministrativeDivisionDescription.class);
    adminTarget = cb.createQuery().from(getHelper().getEntityType(AdministrativeDivisionDescription.class));
    jpaOrgEntity = getHelper().getJPAEntityType(Organization.class);
    orgTarget = cb.createQuery().from(getHelper().getEntityType(Organization.class));
    groups = new ArrayList<>();

    cut = new JPAOrderByBuilder(jpaAdminEntity, adminTarget, cb, groups);
    top = mock(TopOption.class);
    skip = mock(SkipOption.class);
    orderBy = mock(OrderByOption.class);
    uriResource = mock(UriInfoResource.class);
    page = null;
    joinTables = new HashMap<>();
  }

  @Test
  void testNoTopSkipOrderByReturnsEmptyList() throws IOException, ODataException {
    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);
    assertEquals(0, act.size());
  }

  @Test
  void testTopReturnsByPrimaryKey() throws IOException, ODataException {
    when(uriResource.getTopOption()).thenReturn(top);
    when(top.getValue()).thenReturn(5);

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(4, act.size());
    assertEquals(4, act.stream()
        .filter(Order::isAscending)
        .collect(Collectors.toList()).size());
    assertOrder(act);
  }

  @Test
  void testSkipReturnsByPrimaryKey() throws IOException, ODataException {
    when(uriResource.getSkipOption()).thenReturn(skip);
    when(skip.getValue()).thenReturn(5);

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(4, act.size());
    assertEquals(4, act.stream()
        .filter(Order::isAscending)
        .collect(Collectors.toList()).size());
    assertOrder(act);
  }

  @Test
  void testTopSkipReturnsByPrimaryKey() throws IOException, ODataException {
    when(uriResource.getTopOption()).thenReturn(top);
    when(top.getValue()).thenReturn(5);
    when(uriResource.getSkipOption()).thenReturn(skip);
    when(skip.getValue()).thenReturn(5);

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(4, act.size());
    assertEquals(4, act.stream()
        .filter(Order::isAscending)
        .collect(Collectors.toList()).size());
    assertOrder(act);
  }

  @Test
  void testOrderByEmptyReturnsEmptyList() throws ODataApplicationException {
    when(uriResource.getOrderByOption()).thenReturn(orderBy);
    when(orderBy.getOrders()).thenReturn(Collections.emptyList());
    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);
    assertEquals(0, act.size());
  }

  @Test
  void testOrderByOneProperty() throws ODataApplicationException, ODataJPAModelException {
    createOrderByItem("Name");
    when(uriResource.getOrderByOption()).thenReturn(orderBy);
    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);
    assertEquals(1, act.size());
    assertFalse(act.get(0).isAscending());
  }

  @Test
  void testOrderByOneComplexProperty() throws ODataApplicationException, ODataJPAModelException {
    cut = new JPAOrderByBuilder(jpaOrgEntity, orgTarget, cb, groups);
    createComplexOrderByItem();
    when(uriResource.getOrderByOption()).thenReturn(orderBy);
    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);
    assertEquals(1, act.size());
    assertFalse(act.get(0).isAscending());
  }

  @Test
  void testThrowsNotImplementedOnOrderByFunction() {
    final List<UriResource> pathParts = createOrderByClause(null);
    final UriResourceFunction part = mock(UriResourceFunction.class);
    pathParts.add(part);
    when(part.getKind()).thenReturn(UriResourceKind.function);

    assertThrows(ODataJPANotImplementedException.class,
        () -> cut.createOrderByList(joinTables, uriResource, page));
  }

  @Test
  void testOrderByNavigationCountDefault() throws ODataException {
    cut = new JPAOrderByBuilder(jpaOrgEntity, orgTarget, cb, groups);
    final List<UriResource> pathParts = createOrderByClause(null);
    final UriResourceNavigation navigationPart = mock(UriResourceNavigation.class);
    final UriResourceCount countPart = mock(UriResourceCount.class);
    final EdmNavigationProperty navigationProperty = mock(EdmNavigationProperty.class);
    pathParts.add(navigationPart);
    pathParts.add(countPart);

    when(navigationPart.getProperty()).thenReturn(navigationProperty);
    when(navigationProperty.getName()).thenReturn("Roles");
    joinTables.put("Roles", cb.createQuery().from(getHelper().getEntityType(BusinessPartnerRole.class)));

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(1, act.size());
    assertTrue(act.get(0).isAscending());
    assertEquals("COUNT", ((FunctionExpressionImpl<?>) act.get(0).getExpression()).getOperation());
  }

  @Test
  void testOrderByNavigationCountDescending() throws ODataException {
    cut = new JPAOrderByBuilder(jpaOrgEntity, orgTarget, cb, groups);
    final List<UriResource> pathParts = createOrderByClause(Boolean.TRUE);
    final UriResourceNavigation navigationPart = mock(UriResourceNavigation.class);
    final UriResourceCount countPart = mock(UriResourceCount.class);
    final EdmNavigationProperty navigationProperty = mock(EdmNavigationProperty.class);
    pathParts.add(navigationPart);
    pathParts.add(countPart);

    when(navigationPart.getProperty()).thenReturn(navigationProperty);
    when(navigationProperty.getName()).thenReturn("Roles");
    joinTables.put("Roles", cb.createQuery().from(getHelper().getEntityType(BusinessPartnerRole.class)));

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(1, act.size());
    assertFalse(act.get(0).isAscending());
    assertEquals("COUNT", ((FunctionExpressionImpl<?>) act.get(0).getExpression()).getOperation());
  }

  @Test
  void testOrderByCollectionOrderByCountAsc() throws IOException, ODataException {
    final JPAEntityType jpaEntity = getHelper().getJPAEntityType(CollectionDeep.class);
    final From<?, ?> target = cb.createQuery().from(getHelper().getEntityType(CollectionDeep.class));
    cut = new JPAOrderByBuilder(jpaEntity, target, cb, groups);

    final List<UriResource> pathParts = createOrderByClause(Boolean.FALSE);
    final UriResourceProperty firstLevelPart = mock(UriResourceComplexProperty.class);
    final UriResourceProperty secondLevelPart = mock(UriResourceComplexProperty.class);
    final UriResourceProperty commentPart = mock(UriResourceProperty.class);
    final UriResourceCount countPart = mock(UriResourceCount.class);
    when(commentPart.isCollection()).thenReturn(Boolean.TRUE);
    pathParts.add(firstLevelPart);
    pathParts.add(secondLevelPart);
    pathParts.add(commentPart);
    pathParts.add(countPart);

    createComplexEdmProperty(firstLevelPart, "FirstLevel");
    createComplexEdmProperty(secondLevelPart, "SecondLevel");
    createPrimitiveEdmProperty(commentPart, "Comment");
    joinTables.put("FirstLevel/SecondLevel/Comment",
        cb.createQuery().from(getHelper().getEntityType(BusinessPartnerRole.class)));

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(1, act.size());
    assertTrue(act.get(0).isAscending());
    assertEquals("COUNT", ((FunctionExpressionImpl<?>) act.get(0).getExpression()).getOperation());
  }

  @Test
  void testThrowsBadRequestExceptionOnUnknownProperty() throws ODataApplicationException, ODataJPAModelException {
    createOrderByItem("Name");
    when(uriResource.getOrderByOption()).thenReturn(orderBy);
    cut = new JPAOrderByBuilder(jpaOrgEntity, orgTarget, cb, groups);
    assertThrows(ODataJPAProcessorException.class,
        () -> cut.createOrderByList(joinTables, uriResource, page));
  }

  @Test
  void testThrowsBadRequestExceptionOnUnknownComplex() throws ODataApplicationException, ODataJPAModelException {
    createComplexOrderByItem();
    when(uriResource.getOrderByOption()).thenReturn(orderBy);
    cut = new JPAOrderByBuilder(jpaAdminEntity, adminTarget, cb, groups);
    assertThrows(ODataJPAProcessorException.class,
        () -> cut.createOrderByList(joinTables, uriResource, page));
  }

  @Test
  void testThrowExceptionOrderByGroupedPropertyWithoutGroup() throws IOException, ODataException {
    final JPAEntityType jpaEntity = getHelper().getJPAEntityType(BusinessPartnerWithGroups.class);
    final From<?, ?> target = cb.createQuery().from(getHelper().getEntityType(BusinessPartnerWithGroups.class));
    cut = new JPAOrderByBuilder(jpaEntity, target, cb, groups);
    createOrderByItem("Country");

    cut = new JPAOrderByBuilder(jpaEntity, target, cb, groups);
    final ODataJPAQueryException act = assertThrows(ODataJPAQueryException.class,
        () -> cut.createOrderByList(joinTables, uriResource, page));
    assertEquals(HttpStatusCode.FORBIDDEN.getStatusCode(), act.getStatusCode());
  }

  @Test
  void testOrderByPropertyWithGroupsOneGroup() throws IOException, ODataException {
    final JPAEntityType jpaEntity = getHelper().getJPAEntityType(BusinessPartnerWithGroups.class);
    final From<?, ?> target = cb.createQuery().from(getHelper().getEntityType(BusinessPartnerWithGroups.class));
    groups.add("Person");
    cut = new JPAOrderByBuilder(jpaEntity, target, cb, groups);
    createOrderByItem("Country");

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(1, act.size());
    assertFalse(act.get(0).isAscending());
  }

  @Test
  void testOrderByPropertyAndTop() throws IOException, ODataException {
    createOrderByItem("DivisionCode");
    when(top.getValue()).thenReturn(5);
    when(uriResource.getOrderByOption()).thenReturn(orderBy);
    when(uriResource.getTopOption()).thenReturn(top);

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertFalse(act.isEmpty());
    assertEquals(String.class, act.get(0).getExpression().getJavaType());
    assertEquals("DivisionCode", act.get(0).getExpression().getAlias());
  }

  @Test
  void testThrowExceptionOrderByTransientPrimitiveSimpleProperty() throws IOException, ODataException {
    final JPAEntityType jpaEntity = getHelper().getJPAEntityType(Person.class);
    final From<?, ?> target = cb.createQuery().from(getHelper().getEntityType(Person.class));
    cut = new JPAOrderByBuilder(jpaEntity, target, cb, groups);
    createOrderByItem("FullName");

    final ODataJPAQueryException act = assertThrows(ODataJPAQueryException.class,
        () -> cut.createOrderByList(joinTables, uriResource, page));
    assertEquals(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), act.getStatusCode());
  }
  
  @Test
  void testPagePresentOnlyTopValue() throws IOException, ODataException {
    page = mock(JPAODataPage.class);
    when(page.getTop()).thenReturn(Integer.valueOf(10));

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(4, act.size());
    assertEquals(4, act.stream()
        .filter(Order::isAscending)
        .collect(Collectors.toList()).size());
    assertOrder(act);
  }
  
  @Test
  void testPagePresentMaxTopValueNoOrdering() throws IOException, ODataException {
    page = mock(JPAODataPage.class);
    when(page.getTop()).thenReturn(Integer.MAX_VALUE);

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(0, act.size());
  }

  @Test
  void testPagePresentOnlySkipValue() throws IOException, ODataException {
    page = mock(JPAODataPage.class);
    when(page.getSkip()).thenReturn(Integer.valueOf(10));

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(4, act.size());
    assertEquals(4, act.stream()
        .filter(Order::isAscending)
        .collect(Collectors.toList()).size());
    assertOrder(act);
  }
  
  @Test
  void testPageAndTopPresent() throws IOException, ODataException {
    page = mock(JPAODataPage.class);
    when(page.getSkip()).thenReturn(Integer.valueOf(10));
    when(top.getValue()).thenReturn(5);
    when(uriResource.getTopOption()).thenReturn(top);
    
    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(4, act.size());
    assertEquals(4, act.stream()
        .filter(Order::isAscending)
        .collect(Collectors.toList()).size());
    assertOrder(act);
  }

  
  @Test
  void testPageMaxTopValueAndTopPresent() throws IOException, ODataException {
    page = mock(JPAODataPage.class);
    when(page.getSkip()).thenReturn(Integer.MAX_VALUE);
    when(top.getValue()).thenReturn(5);
    when(uriResource.getTopOption()).thenReturn(top);
    
    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertEquals(4, act.size());
    assertEquals(4, act.stream()
        .filter(Order::isAscending)
        .collect(Collectors.toList()).size());
    assertOrder(act);
  }
  
  @Test
  void testOrderByPropertyAndPage() throws IOException, ODataException {
    createOrderByItem("DivisionCode");
    page = mock(JPAODataPage.class);
    when(page.getSkip()).thenReturn(Integer.valueOf(10));
    when(uriResource.getOrderByOption()).thenReturn(orderBy);

    final List<Order> act = cut.createOrderByList(joinTables, uriResource, page);

    assertFalse(act.isEmpty());
    assertEquals(String.class, act.get(0).getExpression().getJavaType());
    assertEquals("DivisionCode", act.get(0).getExpression().getAlias());
    assertEquals(5, act.size());
  }
  
  private List<UriResource> createOrderByClause(final Boolean isDescending) {
    final OrderByItem item = mock(OrderByItem.class);
    final Member expression = mock(Member.class);
    final UriInfo uriInfo = mock(UriInfo.class);
    final List<UriResource> pathParts = new ArrayList<>();
    when(item.getExpression()).thenReturn(expression);
    if (isDescending != null)
      when(item.isDescending()).thenReturn(isDescending);
    when(expression.getResourcePath()).thenReturn(uriInfo);
    when(uriInfo.getUriResourceParts()).thenReturn(pathParts);
    when(uriResource.getOrderByOption()).thenReturn(orderBy);
    when(orderBy.getOrders()).thenReturn(Collections.singletonList(item));
    return pathParts;
  }

  private void createOrderByItem(final String externalName) throws ODataJPAModelException {

    final List<UriResource> pathParts = createOrderByClause(Boolean.TRUE);
    final UriResourceProperty part = mock(UriResourcePrimitiveProperty.class);

    pathParts.add(part);

    createPrimitiveEdmProperty(part, externalName);
  }

  private void createComplexOrderByItem() throws ODataJPAModelException {

    final List<UriResource> pathParts = createOrderByClause(Boolean.TRUE);
    final UriResourceProperty complexPart = mock(UriResourceComplexProperty.class);
    final UriResourceProperty primitivePart = mock(UriResourcePrimitiveProperty.class);

    pathParts.add(complexPart);
    pathParts.add(primitivePart);

    createComplexEdmProperty(complexPart, "Address");
    createPrimitiveEdmProperty(primitivePart, "Region");
  }

  private void createPrimitiveEdmProperty(final UriResourceProperty primitivePart, final String name) {
    final EdmProperty edmPrimitiveProperty = mock(EdmProperty.class);
    when(primitivePart.getProperty()).thenReturn(edmPrimitiveProperty);
    when(edmPrimitiveProperty.getName()).thenReturn(name);
  }

  private void createComplexEdmProperty(final UriResourceProperty complexPart, final String name) {
    final EdmProperty edmComplexProperty = mock(EdmProperty.class);
    when(complexPart.getProperty()).thenReturn(edmComplexProperty);
    when(edmComplexProperty.getName()).thenReturn(name);
  }

  private void assertOrder(final List<Order> act) {
    assertEquals("CodePublisher", act.get(0).getExpression().getAlias());
    assertEquals("CodeID", act.get(1).getExpression().getAlias());
    assertEquals("DivisionCode", act.get(2).getExpression().getAlias());
    assertEquals("Language", act.get(3).getExpression().getAlias());
  }

}
