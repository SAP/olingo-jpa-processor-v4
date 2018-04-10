package com.sap.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataPagingProvider;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

public class TestJPAServerDrivenPaging extends TestBase {
  @Test
  public void testReturnsNotImplementedIfPagingProviderNotAvailable() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$skiptoken=xyz");
    helper.assertStatus(501);
  }

  @Test
  public void testReturnsGoneIfPagingProviderRetunrsNullForSkiptoken() throws IOException, ODataException {
    final JPAODataPagingProvider provider = mock(JPAODataPagingProvider.class);
    when(provider.getNextPage("xyz")).thenReturn(null);
    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$skiptoken=xyz", provider);
    helper.assertStatus(410);

  }

  @Test
  public void testReturnsFullResultIfProviderDoesNotReturnPage() throws IOException, ODataException {
    final JPAODataPagingProvider provider = mock(JPAODataPagingProvider.class);
    when(provider.getFristPage(null)).thenReturn(null);
    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations", provider);
    helper.assertStatus(200);
    assertEquals(10, helper.getValues().size());
  }

  @Test
  public void testReturnsPartResultIfProviderPages() throws IOException, ODataException {
    final UriInfo uriInfo = buildUriInfo();
    final JPAODataPagingProvider provider = mock(JPAODataPagingProvider.class);
    when(provider.getFristPage(any())).thenReturn(new JPAODataPage(uriInfo, 0, 5, "Hugo"));
    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=ID desc", provider);
    helper.assertStatus(200);
    assertEquals(5, helper.getValues().size());
  }

  @Test
  public void testReturnsNextLinkIfProviderPages() throws IOException, ODataException {
    final UriInfo uriInfo = buildUriInfo();
    final JPAODataPagingProvider provider = mock(JPAODataPagingProvider.class);
    when(provider.getFristPage(any())).thenReturn(new JPAODataPage(uriInfo, 0, 5, "Hugo"));
    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$orderby=ID desc", provider);
    helper.assertStatus(200);
    assertEquals(5, helper.getValues().size());
    assertEquals("Organizations?$skiptoken='Hugo'", helper.getValue().get("@odata.nextLink").asText());
  }

  @Test
  public void testReturnsNextPagesRespectingFilter() throws IOException, ODataException {
    final UriInfo uriInfo = buildUriInfo();

    final JPAODataPagingProvider provider = mock(JPAODataPagingProvider.class);
    when(provider.getNextPage("xyz")).thenReturn(new JPAODataPage(uriInfo, 5, 5, null));

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$skiptoken=xyz", provider);
    helper.assertStatus(200);
    assertEquals(5, helper.getValues().size());
    ObjectNode org = (ObjectNode) helper.getValues().get(4);
    assertEquals("1", org.get("ID").asText());
  }

  private UriInfo buildUriInfo() {
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
    when(es.getName()).thenReturn("Organizations");
    when(type.getNamespace()).thenReturn("com.sap.olingo.jpa");
    when(type.getName()).thenReturn("Organization");
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
}
