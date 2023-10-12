package com.sap.olingo.jpa.processor.core.api.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.query.JPACountQuery;

class JPAExamplePagingProviderTest {
  private JPACountQuery countQuery;

  @BeforeEach
  void setup() throws ODataApplicationException {
    countQuery = mock(JPACountQuery.class);
    when(countQuery.countResults()).thenReturn(10L);
  }

  @Test
  void testReturnDefaultTopSkipPageSize2() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(2);
    final JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);

    assertEquals(0, act.skip());
    assertEquals(2, act.top());
    assertNotNull(toODataString((String) act.skipToken()));
    assertEquals(info, act.uriInfo());
  }

  @Test
  void testReturnDefaultTopSkipPageSize5() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    final JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);

    assertEquals(0, act.skip());
    assertEquals(5, act.top());
    assertNotNull(toODataString((String) act.skipToken()));
    assertEquals(info, act.uriInfo());
  }

  @Test
  void testReturnDefaultTopSkipPageSizeOther() throws ODataApplicationException {
    final UriInfo info = buildUriInfo("AdministrativeDivisions", "AdministrativeDivision");
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    when(countQuery.countResults()).thenReturn(12L);
    final JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);

    assertEquals(0, act.skip());
    assertEquals(10, act.top());
    assertNotNull(toODataString((String) act.skipToken()));
    assertEquals(info, act.uriInfo());
  }

  @Test
  void testReturnDefaultTopSkipPageSize5NextPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);
    act = cut.getNextPage(toODataString((String) act.skipToken()));

    assertEquals(5, act.skip());
    assertEquals(5, act.top());
    assertEquals(info, act.uriInfo());
  }

  @Test
  void testReturnNullIfEntitySetIsUnknown() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createPersonCut(5);
    final JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);

    assertNull(act);
  }

  @Test
  void testReturnNullIfEntitySetIsUnknownButMaxPageSizeHeader() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createPersonCut(5);
    final JPAODataPage act = cut.getFirstPage(info, 3, countQuery, null);

    assertNull(act);
  }

  @Test
  void testReturnRespectMaxPageSizeHeader() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    final JPAODataPage act = cut.getFirstPage(info, 3, countQuery, null);

    assertEquals(0, act.skip());
    assertEquals(3, act.top());
    assertNotNull(toODataString((String) act.skipToken()));
    assertEquals(info, act.uriInfo());
  }

  @Test
  void testReturnSkipTokenNullAtLastPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);
    act = cut.getNextPage(toODataString((String) act.skipToken()));

    assertNull(act.skipToken());
  }

  @Test
  void testReturnSkipTokenNullOnlyOnePage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo("AdministrativeDivisions", "AdministrativeDivision");
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    final JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);

    assertNull(act.skipToken());
  }

  @Test
  void testReturnSkipTokenIfNotLastPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(2);
    JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);
    act = cut.getNextPage(toODataString((String) act.skipToken()));

    assertNotNull(toODataString((String) act.skipToken()));
  }

  @Test
  void testReturnThirdPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(2);
    JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);
    act = cut.getNextPage(toODataString((String) act.skipToken()));
    act = cut.getNextPage(toODataString((String) act.skipToken()));

    assertNotNull(toODataString((String) act.skipToken()));
  }

  @Test
  void testRespectTopSkipOfUriFirstPageLowerMaxSize() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    addTopSkipToUri(info);
    final JPAExamplePagingProvider cut = createOrganizationCut(10);
    final JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);

    assertEquals(2, act.skip());
    assertEquals(7, act.top());
  }

  @Test
  void testRespectTopSkipOfUriFirstPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    addTopSkipToUri(info);
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    final JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);

    assertEquals(2, act.skip());
    assertEquals(5, act.top());
  }

  @Test
  void testRespectTopSkipOfUriNextPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    addTopSkipToUri(info);
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    JPAODataPage act = cut.getFirstPage(info, null, countQuery, null);
    act = cut.getNextPage(toODataString((String) act.skipToken()));

    assertEquals(7, act.skip());
    assertEquals(2, act.top());
  }

  @Test
  void testBufferFilled() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final Map<String, Integer> sizes = new HashMap<>();
    sizes.put("Organizations", 2);

    final JPAExamplePagingProvider cut = new JPAExamplePagingProvider(sizes, 2);
    final JPAODataPage first = cut.getFirstPage(info, null, countQuery, null);
    assertNotNull(cut.getNextPage((String) first.skipToken()));
    final JPAODataPage second = cut.getNextPage((String) first.skipToken());
    assertNotNull(cut.getNextPage((String) second.skipToken()));
    final JPAODataPage third = cut.getNextPage((String) second.skipToken());
    assertNotNull(cut.getNextPage((String) third.skipToken()));
    assertNull(cut.getNextPage((String) first.skipToken()));
  }

  @Test
  void testBufferNotFilled() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final Map<String, Integer> sizes = new HashMap<>();
    sizes.put("Organizations", 2);

    final JPAExamplePagingProvider cut = new JPAExamplePagingProvider(sizes, 10);
    final JPAODataPage first = cut.getFirstPage(info, null, countQuery, null);
    assertNotNull(cut.getNextPage((String) first.skipToken()));
    final JPAODataPage second = cut.getNextPage((String) first.skipToken());
    assertNotNull(cut.getNextPage((String) second.skipToken()));
    final JPAODataPage third = cut.getNextPage((String) second.skipToken());
    assertNotNull(cut.getNextPage((String) third.skipToken()));
    assertNotNull(cut.getNextPage((String) first.skipToken()));
  }

  private UriInfo buildUriInfo() {
    return buildUriInfo("Organizations", "Organization");
  }

  private UriInfo buildUriInfo(final String esName, final String etName) {
    final UriInfo uriInfo = mock(UriInfo.class);
    final UriResourceEntitySet uriEs = mock(UriResourceEntitySet.class);
    final EdmEntitySet es = mock(EdmEntitySet.class);
    final EdmType type = mock(EdmType.class);
    final OrderByOption order = mock(OrderByOption.class);
    final OrderByItem orderItem = mock(OrderByItem.class);
    final Member orderExpression = mock(Member.class);
    final UriInfoResource orderResourcePath = mock(UriInfoResource.class);
    final UriResourcePrimitiveProperty orderResourcePathItem = mock(UriResourcePrimitiveProperty.class);
    final EdmProperty orderProperty = mock(EdmProperty.class);
    final List<OrderByItem> orderItems = new ArrayList<>();
    final List<UriResource> orderResourcePathItems = new ArrayList<>();

    orderItems.add(orderItem);
    orderResourcePathItems.add(orderResourcePathItem);
    when(uriEs.getKind()).thenReturn(UriResourceKind.entitySet);
    when(uriEs.getEntitySet()).thenReturn(es);
    when(uriEs.getType()).thenReturn(type);
    when(es.getName()).thenReturn(esName);
    when(type.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(type.getName()).thenReturn(etName);
    when(order.getKind()).thenReturn(SystemQueryOptionKind.ORDERBY);
    when(orderItem.isDescending()).thenReturn(true);
    when(orderItem.getExpression()).thenReturn(orderExpression);
    when(orderExpression.getResourcePath()).thenReturn(orderResourcePath);
    when(orderResourcePath.getUriResourceParts()).thenReturn(orderResourcePathItems);
    when(orderResourcePathItem.getProperty()).thenReturn(orderProperty);
    when(orderProperty.getName()).thenReturn("ID");
    when(order.getOrders()).thenReturn(orderItems);
    final List<UriResource> resourceParts = new ArrayList<>();
    resourceParts.add(uriEs);
    when(uriInfo.getUriResourceParts()).thenReturn(resourceParts);
    when(uriInfo.getOrderByOption()).thenReturn(order);
    return uriInfo;
  }

  private void addTopSkipToUri(final UriInfo info) {
    final SkipOption skipOption = mock(SkipOption.class);
    final TopOption topOption = mock(TopOption.class);

    when(skipOption.getValue()).thenReturn(2);
    when(topOption.getValue()).thenReturn(7);
    when(info.getSkipOption()).thenReturn(skipOption);
    when(info.getTopOption()).thenReturn(topOption);

  }

  private JPAExamplePagingProvider createOrganizationCut(final int size) {
    final Map<String, Integer> sizes = new HashMap<>();
    sizes.put("Organizations", size);
    sizes.put("AdministrativeDivisions", 10);
    return new JPAExamplePagingProvider(sizes);
  }

  private JPAExamplePagingProvider createPersonCut(final int size) {
    final Map<String, Integer> sizes = new HashMap<>();
    sizes.put("Persons", size);
    return new JPAExamplePagingProvider(sizes);
  }

  private String toODataString(final String skipToken) {
    return "'" + skipToken + "'";
  }
}
