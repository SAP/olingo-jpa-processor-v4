package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;

import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceCount;
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
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.properties.JPAOrderByPropertyFactory;
import com.sap.olingo.jpa.processor.core.properties.JPAProcessorAttribute;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivisionDescription;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class JPAOrderByBuilderTest extends TestBase {
  private JPAOrderByBuilder cut;
  private Map<String, From<?, ?>> joinTables;
  private UriInfoResource uriResource;
  private TopOption top;
  private SkipOption skip;
  private OrderByOption orderBy;
  private From<?, ?> adminTarget;
  private From<?, ?> organizationTarget;
  private CriteriaBuilder cb;
  private JPAEntityType jpaAdminEntity;
  private JPAEntityType jpaOrganizationEntity;
  private List<String> groups;
  private UriInfo uriInfo;
  private List<JPAProcessorAttribute> orderByAttributes;

  @BeforeEach
  void setup() throws ODataException {
    cb = emf.getCriteriaBuilder();
    jpaAdminEntity = getHelper().getJPAEntityType(AdministrativeDivisionDescription.class);
    adminTarget = cb.createQuery().from(getHelper().getEntityType(AdministrativeDivisionDescription.class));
    jpaOrganizationEntity = getHelper().getJPAEntityType(Organization.class);
    organizationTarget = cb.createQuery().from(getHelper().getEntityType(Organization.class));
    groups = new ArrayList<>();

    cut = new JPAOrderByBuilder(jpaAdminEntity, adminTarget, cb, groups);
    top = mock(TopOption.class);
    skip = mock(SkipOption.class);
    orderBy = mock(OrderByOption.class);
    uriResource = mock(UriInfoResource.class);
    uriInfo = mock(UriInfo.class);
    joinTables = new HashMap<>();
    orderByAttributes = new ArrayList<>();

    when(top.getValue()).thenReturn(5);
    when(skip.getValue()).thenReturn(5);

  }

  @Test
  void testNoTopSkipOrderByReturnsEmptyList() throws ODataException {
    final List<Order> act = cut.createOrderByList(joinTables, orderByAttributes, uriInfo);
    assertEquals(0, act.size());
  }

  @Test
  void testTopReturnsByPrimaryKey() throws ODataException {
    when(uriInfo.getTopOption()).thenReturn(top);

    final List<Order> act = cut.createOrderByList(joinTables, orderByAttributes, uriInfo);

    assertEquals(4, act.size());
    assertEquals(4, act.stream()
        .filter(Order::isAscending)
        .toList().size());
    assertOrder(act);
  }

  @Test
  void testSkipReturnsByPrimaryKey() throws ODataException {
    when(uriInfo.getSkipOption()).thenReturn(skip);
    final List<Order> act = cut.createOrderByList(joinTables, orderByAttributes, uriInfo);

    assertEquals(4, act.size());
    assertEquals(4, act.stream()
        .filter(Order::isAscending)
        .toList().size());
    assertOrder(act);
  }

  @Test
  void testTopSkipReturnsByPrimaryKey() throws ODataException {
    when(uriInfo.getTopOption()).thenReturn(top);
    when(uriInfo.getSkipOption()).thenReturn(skip);

    final List<Order> act = cut.createOrderByList(joinTables, orderByAttributes, uriInfo);

    assertEquals(4, act.size());
    assertEquals(4, act.stream()
        .filter(Order::isAscending)
        .toList().size());
    assertOrder(act);
  }

  @Test
  void testOrderByOneProperty() throws ODataApplicationException {
    createOrderByItem("Name");
    final List<Order> act = cut.createOrderByList(joinTables, orderByAttributes, uriInfo);
    assertEquals(1, act.size());
    assertFalse(act.get(0).isAscending());
  }

  @Test
  void testOrderByNavigationCountDefault() throws ODataException {
    createCountOrderByItem(false);
    cut = new JPAOrderByBuilder(jpaOrganizationEntity, organizationTarget, cb, groups);
    final List<Order> act = cut.createOrderByList(joinTables, orderByAttributes, uriInfo);

    assertEquals(1, act.size());
    assertTrue(act.get(0).isAscending());
    assertEquals("COUNT", ((FunctionExpressionImpl<?>) act.get(0).getExpression()).getOperation());
  }

  @Test
  void testOrderByNavigationCountDescending() throws ODataException {
    createCountOrderByItem(true);
    cut = new JPAOrderByBuilder(jpaOrganizationEntity, organizationTarget, cb, groups);
    final List<Order> act = cut.createOrderByList(joinTables, orderByAttributes, uriInfo);

    assertEquals(1, act.size());
    assertFalse(act.get(0).isAscending());
    assertEquals("COUNT", ((FunctionExpressionImpl<?>) act.get(0).getExpression()).getOperation());
  }

  @Test
  void testOrderByPropertyAndTop() throws ODataException {
    createOrderByItem("DivisionCode");
    when(top.getValue()).thenReturn(5);
    when(uriResource.getOrderByOption()).thenReturn(orderBy);
    when(uriResource.getTopOption()).thenReturn(top);

    final List<Order> act = cut.createOrderByList(joinTables, orderByAttributes, uriInfo);

    assertFalse(act.isEmpty());
    assertEquals(String.class, act.get(0).getExpression().getJavaType());
    assertEquals("DivisionCode", act.get(0).getExpression().getAlias());
  }

  @Test
  void testThrowExceptionOrderByTransientPrimitiveSimpleProperty() throws ODataException {
    final JPAEntityType jpaEntity = getHelper().getJPAEntityType(Person.class);
    final From<?, ?> target = cb.createQuery().from(getHelper().getEntityType(Person.class));
    cut = new JPAOrderByBuilder(jpaEntity, target, cb, groups);
    createOrderByItem("FullName", jpaEntity, target);

    final ODataJPAQueryException act = assertThrows(ODataJPAQueryException.class,
        () -> cut.createOrderByList(joinTables, orderByAttributes, uriInfo));
    assertEquals(HttpStatusCode.BAD_REQUEST.getStatusCode(), act.getStatusCode());
  }

  @Test
  void testOrderByPropertyAndPage() throws ODataException {
    createOrderByItem("DivisionCode");
    when(uriInfo.getTopOption()).thenReturn(top);
    when(uriResource.getOrderByOption()).thenReturn(orderBy);

    final List<Order> act = cut.createOrderByList(joinTables, orderByAttributes, uriInfo);

    assertFalse(act.isEmpty());
    assertEquals(String.class, act.get(0).getExpression().getJavaType());
    assertEquals("DivisionCode", act.get(0).getExpression().getAlias());
    assertEquals(5, act.size());
  }

  private List<UriResource> createOrderByClause(final Boolean isDescending) {
    final OrderByItem item = mock(OrderByItem.class);
    final Member expression = mock(Member.class);
    final List<UriResource> pathParts = new ArrayList<>();
    when(item.getExpression()).thenReturn(expression);
    if (isDescending != null) {
      when(item.isDescending()).thenReturn(isDescending);
    }
    when(expression.getResourcePath()).thenReturn(uriInfo);
    when(uriInfo.getUriResourceParts()).thenReturn(pathParts);
    when(uriResource.getOrderByOption()).thenReturn(orderBy);
    when(orderBy.getOrders()).thenReturn(Collections.singletonList(item));
    return pathParts;
  }

  private void createOrderByItem(final String externalName) {
    createOrderByItem(externalName, jpaAdminEntity, adminTarget);
  }

  private void createOrderByItem(final String externalName, final JPAEntityType jpaEntity, final From<?, ?> target) {

    final List<UriResource> pathParts = createOrderByClause(Boolean.TRUE);
    final UriResourceProperty part = mock(UriResourcePrimitiveProperty.class);
    when(part.getKind()).thenReturn(UriResourceKind.primitiveProperty);
    pathParts.add(part);
    createPrimitiveEdmProperty(part, externalName);
    final var attribute = new JPAOrderByPropertyFactory().createProperty(orderBy.getOrders().get(0), jpaEntity,
        Locale.ENGLISH);
    attribute.setTarget(target, joinTables, cb);
    orderByAttributes.add(attribute);
  }

  private void createCountOrderByItem(final boolean descending) {

    final OrderByItem item = mock(OrderByItem.class);
    final var navigation = mock(UriResourceNavigation.class);
    final var count = mock(UriResourceCount.class);
    final var edmNavigation = mock(EdmNavigationProperty.class);
    final Member expression = mock(Member.class);
    when(uriInfo.getUriResourceParts()).thenReturn(Arrays.asList(navigation, count));
    when(navigation.getProperty()).thenReturn(edmNavigation);
    when(edmNavigation.getName()).thenReturn("Roles");
    when(count.getKind()).thenReturn(UriResourceKind.count);
    when(item.isDescending()).thenReturn(descending);
    when(item.getExpression()).thenReturn(expression);
    when(expression.getResourcePath()).thenReturn(uriInfo);

    final var attribute = new JPAOrderByPropertyFactory().createProperty(item, jpaOrganizationEntity, Locale.ENGLISH);
    attribute.setTarget(organizationTarget, joinTables, cb);
    orderByAttributes.add(attribute);

  }

  private void createPrimitiveEdmProperty(final UriResourceProperty primitivePart, final String name) {
    final EdmProperty edmPrimitiveProperty = mock(EdmProperty.class);
    when(primitivePart.getProperty()).thenReturn(edmPrimitiveProperty);
    when(edmPrimitiveProperty.getName()).thenReturn(name);
  }

  private void assertOrder(final List<Order> act) {
    assertEquals("CodePublisher", act.get(0).getExpression().getAlias());
    assertEquals("CodeID", act.get(1).getExpression().getAlias());
    assertEquals("DivisionCode", act.get(2).getExpression().getAlias());
    assertEquals("Language", act.get(3).getExpression().getAlias());
  }

}
