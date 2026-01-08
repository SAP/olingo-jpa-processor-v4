package com.sap.olingo.jpa.processor.core.api.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    final Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);

    assertEquals(0, act.get().skip());
    assertEquals(2, act.get().top());
    assertNotNull(toODataString(act.get().skipToken().toString()));
    assertEquals(info, act.get().uriInfo());
  }

  @Test
  void testReturnDefaultTopSkipPageSize5() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    final Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);

    assertEquals(0, act.get().skip());
    assertEquals(5, act.get().top());
    assertNotNull(toODataString(act.get().skipToken().toString()));
    assertEquals(info, act.get().uriInfo());
  }

  @Test
  void testReturnDefaultTopSkipPageSizeOther() throws ODataApplicationException {
    final UriInfo info = buildUriInfo("AdministrativeDivisions", "AdministrativeDivision");
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    when(countQuery.countResults()).thenReturn(12L);
    final Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);

    assertEquals(0, act.get().skip());
    assertEquals(10, act.get().top());
    assertNotNull(toODataString(act.get().skipToken().toString()));
    assertEquals(info, act.get().uriInfo());
  }

  @Test
  void testReturnDefaultTopSkipPageSize5NextPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);
    act = cut.getNextPage(toODataString(act.get().skipToken().toString()), null, null, null, null);

    assertEquals(5, act.get().skip());
    assertEquals(5, act.get().top());
    assertEquals(info, act.get().uriInfo());
  }

  @Test
  void testReturnNullIfEntitySetIsUnknown() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createPersonCut(5);
    final Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);

    assertTrue(act.isEmpty());
  }

  @Test
  void testReturnNullIfEntitySetIsUnknownButMaxPageSizeHeader() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createPersonCut(5);
    final Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, 3, countQuery, null);

    assertTrue(act.isEmpty());
  }

  @Test
  void testReturnGetFirstPageRespectMaxPageSizeHeader() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    final Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, 3, countQuery, null);

    assertEquals(0, act.get().skip());
    assertEquals(3, act.get().top());
    assertNotNull(toODataString(act.get().skipToken().toString()));
    assertEquals(info, act.get().uriInfo());
  }

  @Test
  void testReturnGetNextPageRespectMaxPageSizeHeader() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, 3, countQuery, null);
    act = cut.getNextPage(toODataString(act.get().skipToken().toString()), null, null, null, null);

    assertEquals(3, act.get().skip());
    assertEquals(3, act.get().top());
    assertNotNull(toODataString((act.get().skipToken().toString())));
    assertEquals(info, act.get().uriInfo());
  }

  @Test
  void testReturnSkipTokenNullAtLastPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);
    act = cut.getNextPage(toODataString(act.get().skipToken().toString()), null, null, null, null);

    assertNull(act.get().skipToken());
  }

  @Test
  void testReturnSkipTokenNullOnlyOnePage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo("AdministrativeDivisions", "AdministrativeDivision");
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    final Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);

    assertNull(act.get().skipToken());
  }

  @Test
  void testReturnSkipTokenIfNotLastPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(2);
    Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);
    act = cut.getNextPage(toODataString(act.get().skipToken().toString()), null, null, null, null);

    assertNotNull(toODataString(act.get().skipToken().toString()));
  }

  @Test
  void testReturnThirdPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final JPAExamplePagingProvider cut = createOrganizationCut(2);
    Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);
    act = cut.getNextPage(toODataString(act.get().skipToken().toString()), null, null, null, null);
    act = cut.getNextPage(toODataString(act.get().skipToken().toString()), null, null, null, null);

    assertNotNull(toODataString(act.get().skipToken().toString()));
  }

  @Test
  void testRespectTopSkipOfUriFirstPageLowerMaxSize() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    addTopSkipToUri(info);
    final JPAExamplePagingProvider cut = createOrganizationCut(10);
    final Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);

    assertEquals(2, act.get().skip());
    assertEquals(7, act.get().top());
  }

  @Test
  void testRespectTopSkipOfUriFirstPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    addTopSkipToUri(info);
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    final Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);

    assertEquals(2, act.get().skip());
    assertEquals(5, act.get().top());
  }

  @Test
  void testRespectTopSkipOfUriNextPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    addTopSkipToUri(info);
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);
    act = cut.getNextPage(toODataString(act.get().skipToken().toString()), null, null, null, null);

    assertEquals(7, act.get().skip());
    assertEquals(2, act.get().top());
  }

  @Test
  void testNoSkipTokenIfRealNoReturnedLowerPage() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    addTopSkipToUri(info, 8, 10);
    final JPAExamplePagingProvider cut = createOrganizationCut(5);
    final Optional<JPAODataPage> act = cut.getFirstPage(null, null, info, null, countQuery, null);

    assertNull(act.get().skipToken());
    assertEquals(8, act.get().skip());
  }

  @Test
  void testBufferFull() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final Map<String, Integer> sizes = new HashMap<>();
    sizes.put("Organizations", 2);

    final JPAExamplePagingProvider cut = new JPAExamplePagingProvider(sizes, 2);
    final Optional<JPAODataPage> first = cut.getFirstPage(null, null, info, null, countQuery, null);
    assertNotNull(cut.getNextPage((first.get().skipToken()).toString(), null, null, null, null));
    final Optional<JPAODataPage> second = cut.getNextPage(first.get().skipToken().toString(), null, null, null, null);
    assertTrue(cut.getNextPage((second.get().skipToken()).toString(), null, null, null, null).isPresent());
    final Optional<JPAODataPage> third = cut.getNextPage((second.get().skipToken().toString()), null, null, null, null);
    assertTrue(cut.getNextPage(third.get().skipToken().toString(), null, null, null, null).isPresent());
    assertTrue(cut.getNextPage(first.get().skipToken().toString(), null, null, null, null).isEmpty());
  }

  @Test
  void testBufferNotFull() throws ODataApplicationException {
    final UriInfo info = buildUriInfo();
    final Map<String, Integer> sizes = new HashMap<>();
    sizes.put("Organizations", 2);

    final JPAExamplePagingProvider cut = new JPAExamplePagingProvider(sizes, 10);
    final Optional<JPAODataPage> first = cut.getFirstPage(null, null, info, null, countQuery, null);
    assertNotNull(cut.getNextPage(first.get().skipToken().toString(), null, null, null, null));
    final Optional<JPAODataPage> second = cut.getNextPage(first.get().skipToken().toString(), null, null, null, null);
    assertTrue(cut.getNextPage(second.get().skipToken().toString(), null, null, null, null).isPresent());
    final Optional<JPAODataPage> third = cut.getNextPage(second.get().skipToken().toString(), null, null, null, null);
    assertTrue(cut.getNextPage(third.get().skipToken().toString(), null, null, null, null).isPresent());
    assertTrue(cut.getNextPage(first.get().skipToken().toString(), null, null, null, null).isPresent());
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
    addTopSkipToUri(info, 2, 7);
  }

  private void addTopSkipToUri(final UriInfo info, final int skip, final int top) {
    final SkipOption skipOption = mock(SkipOption.class);
    final TopOption topOption = mock(TopOption.class);

    when(skipOption.getValue()).thenReturn(skip);
    when(topOption.getValue()).thenReturn(top);
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
