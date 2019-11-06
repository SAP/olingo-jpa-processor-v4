package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

public class TestJPAExpandJoinQuery extends TestBase {
  private JPAExpandJoinQuery cut;
  private EntityManager em;
  private JPAODataCRUDContextAccess sessionContext;
  private JPAODataRequestContextAccess requestContext;
  private TestHelper helper;
  private JPAKeyPair orgPair;
  private JPAKeyPair adminPair;
  private Optional<JPAKeyPair> orgBoundary;
  private Optional<JPAKeyPair> adminBoundary;
  @SuppressWarnings("rawtypes")
  private Map<JPAAttribute, Comparable> simpleKey;

  @BeforeEach
  public void setup() throws ODataException {
    createHeaders();
    helper = new TestHelper(emf, PUNIT_NAME);
    em = emf.createEntityManager();
    sessionContext = mock(JPAODataCRUDContextAccess.class);
    requestContext = mock(JPAODataRequestContextAccess.class);
    orgPair = new JPAKeyPair(helper.getJPAEntityType("Organizations").getKey());
    orgBoundary = Optional.of(orgPair);
    adminPair = new JPAKeyPair(helper.getJPAEntityType("AdministrativeDivisions").getKey());
    adminBoundary = Optional.of(adminPair);
    JPAServiceDebugger debugger = mock(JPAServiceDebugger.class);

    when(sessionContext.getEdmProvider()).thenReturn(helper.edmProvider);
    when(sessionContext.getOperationConverter()).thenReturn(new JPADefaultDatabaseProcessor());
    when(requestContext.getDebugger()).thenReturn(debugger);
    when(requestContext.getClaimsProvider()).thenReturn(Optional.empty());
    when(requestContext.getEntityManager()).thenReturn(em);
  }

  @Test
  public void testSelectAllWithAllExpand() throws ODataException {
    // .../Organizations?$expand=Roles&$format=json
    JPAInlineItemInfo item = createOrgExpandRoles(null, null);
    cut = new JPAExpandJoinQuery(OData.newInstance(), sessionContext, item, headers, requestContext, Optional.empty());
    JPAExpandQueryResult act = cut.execute();
    assertEquals(4, act.getNoResults());
    assertEquals(7, act.getNoResultsDeep());
  }

  @Test
  public void testSelectOrgByIdWithAllExpand() throws ODataException {

    // .../Organizations('2')?$expand=Roles&$format=json
    UriParameter key = mock(UriParameter.class);
    when(key.getName()).thenReturn("ID");
    when(key.getText()).thenReturn("'2'");
    List<UriParameter> keyPredicates = new ArrayList<>();
    keyPredicates.add(key);
    JPAInlineItemInfo item = createOrgExpandRoles(keyPredicates, null);

    cut = new JPAExpandJoinQuery(OData.newInstance(), sessionContext, item, headers, requestContext, Optional.empty());
    JPAExpandQueryResult act = cut.execute();
    assertEquals(1, act.getNoResults());
    assertEquals(2, act.getNoResultsDeep());
  }

  @Test
  public void testSelectWithMinBoundry() throws ODataException {
    // .../Organizations?$expand=Roles&$skip=2&$format=json
    JPAInlineItemInfo item = createOrgExpandRoles(null, null);
    setSimpleKey(3);
    cut = new JPAExpandJoinQuery(OData.newInstance(), sessionContext, item, headers, requestContext, orgBoundary);
    final JPAExpandQueryResult act = cut.execute();
    assertTrue(cut.getSQLString().contains(".\"ID\" = ?"));
    assertEquals(1, act.getNoResults());
    assertEquals(3, act.getNoResultsDeep());
  }

  @Test
  public void testSelectWithMinBoundryEmbedded() throws ODataException {
    // .../Organizations?$expand=Roles&$skip=2&$format=json
    JPAInlineItemInfo item = createAdminDivExpandchildren(null, null);
    setComplexKey("Eurostat", "NUTS1", "BE2");
    cut = new JPAExpandJoinQuery(OData.newInstance(), sessionContext, item, headers, requestContext, adminBoundary);
    final JPAExpandQueryResult act = cut.execute();
    assertTrue(cut.getSQLString().contains(
        "(((t1.\"DivisionCode\" = ?) AND (t1.\"CodeID\" = ?)) AND (t1.\"CodePublisher\" = ?)) "));
    assertEquals(1, act.getNoResults());
    assertEquals(5, act.getNoResultsDeep());
  }

  @Test
  public void testSelectWithMinMaxBoundry() throws ODataException {
    // .../Organizations?$expand=Roles&$top=3&$format=json
    final JPAInlineItemInfo item = createOrgExpandRoles(null, null);
    setSimpleKey(2);
    setSimpleKey(1);
    cut = new JPAExpandJoinQuery(OData.newInstance(), sessionContext, item, headers, requestContext, orgBoundary);
    final JPAExpandQueryResult act = cut.execute();
    assertTrue(cut.getSQLString().contains(".\"ID\" >= ?"));
    assertTrue(cut.getSQLString().contains(".\"ID\" <= ?"));
    assertEquals(2, act.getNoResults());
    assertEquals(3, act.getNoResultsDeep());
  }

  @Test
  public void testSelectWithMinMaxBoundryEmbeddedOnlyLastDiffers() throws ODataException {

    JPAInlineItemInfo item = createAdminDivExpandchildren(null, null);
    setComplexKey("Eurostat", "NUTS1", "BE1");
    setComplexKey("Eurostat", "NUTS2", "BE25");
    cut = new JPAExpandJoinQuery(OData.newInstance(), sessionContext, item, headers, requestContext, adminBoundary);
    final JPAExpandQueryResult act = cut.execute();
    assertTrue(cut.getSQLString().contains(
        "(((t1.\"DivisionCode\" >= ?) AND (t1.\"CodeID\" = ?)) AND (t1.\"CodePublisher\" = ?))"));
    assertTrue(cut.getSQLString().contains(
        "(((t1.\"DivisionCode\" <= ?) AND (t1.\"CodeID\" = ?)) AND (t1.\"CodePublisher\" = ?))"));
    assertTrue(cut.getSQLString().contains(
        "(t1.\"CodeID\" > ?)) AND (t1.\"CodePublisher\" = ?))"));
    assertTrue(cut.getSQLString().contains(
        "(t1.\"CodeID\" < ?)) AND (t1.\"CodePublisher\" = ?))"));
    assertEquals(9, act.getNoResults());
    assertEquals(34, act.getNoResultsDeep());
  }

  @Test
  public void testSQLStringNotEmptyAfterExecute() throws ODataException {
    // .../Organizations?$expand=Roles&$format=json
    JPAInlineItemInfo item = createOrgExpandRoles(null, null);
    cut = new JPAExpandJoinQuery(OData.newInstance(), sessionContext, item, headers, requestContext, Optional.empty());
    assertTrue(cut.getSQLString().isEmpty());
    cut.execute();
    assertFalse(cut.getSQLString().isEmpty());
  }

  private JPAInlineItemInfo createAdminDivExpandchildren(final List<UriParameter> keyPredicates,
      Expression<Boolean> expression)
      throws ODataJPAModelException, ODataApplicationException {

    JPAEntityType et = helper.getJPAEntityType("AdministrativeDivisions");
    JPAExpandItemWrapper uriInfo = mock(JPAExpandItemWrapper.class);
    UriResourceEntitySet uriEts = mock(UriResourceEntitySet.class);
    when(uriEts.getKeyPredicates()).thenReturn(keyPredicates);
    EdmEntityType edmType = mock(EdmEntityType.class);

    List<JPANavigationProptertyInfo> hops = new ArrayList<>();
    JPANavigationProptertyInfo hop = new JPANavigationProptertyInfo(helper.sd, uriEts, et.getAssociationPath(
        "Children"), null);
    hops.add(hop);

    JPAInlineItemInfo item = mock(JPAInlineItemInfo.class);
    UriResourceNavigation target = mock(UriResourceNavigation.class);
    EdmNavigationProperty targetProperty = mock(EdmNavigationProperty.class);
    when(targetProperty.getName()).thenReturn("Children");
    when(target.getProperty()).thenReturn(targetProperty);
    List<UriResource> resourceParts = new ArrayList<>();
    resourceParts.add(target);

    hop = new JPANavigationProptertyInfo(helper.sd, null, null, et);
    hops.add(hop);

    when(item.getEntityType()).thenReturn(et);
    when(item.getUriInfo()).thenReturn(uriInfo);
    when(item.getHops()).thenReturn(hops);
    when(item.getExpandAssociation()).thenReturn(et.getAssociationPath("Children"));
    when(uriInfo.getUriResourceParts()).thenReturn(resourceParts);
    when(uriEts.getType()).thenReturn(edmType);
    when(edmType.getNamespace()).thenReturn(PUNIT_NAME);
    when(edmType.getName()).thenReturn("AdministrativeDivision");
    return item;
  }

  private JPAInlineItemInfo createOrgExpandRoles(final List<UriParameter> keyPredicates, Expression<Boolean> expression)
      throws ODataJPAModelException, ODataApplicationException {
    JPAEntityType et = helper.getJPAEntityType("BusinessPartnerRoles");
    JPAExpandItemWrapper uriInfo = mock(JPAExpandItemWrapper.class);
    UriResourceEntitySet uriEts = mock(UriResourceEntitySet.class);
    when(uriEts.getKeyPredicates()).thenReturn(keyPredicates);
    EdmEntityType edmType = mock(EdmEntityType.class);

    List<JPANavigationProptertyInfo> hops = new ArrayList<>();
    JPANavigationProptertyInfo hop = new JPANavigationProptertyInfo(helper.sd, uriEts, helper.getJPAEntityType(
        "Organizations").getAssociationPath("Roles"), null);
    hops.add(hop);

    JPAInlineItemInfo item = mock(JPAInlineItemInfo.class);
    UriResourceNavigation target = mock(UriResourceNavigation.class);
    EdmNavigationProperty targetProperty = mock(EdmNavigationProperty.class);
    when(targetProperty.getName()).thenReturn("Roles");
    when(target.getProperty()).thenReturn(targetProperty);
    List<UriResource> resourceParts = new ArrayList<>();
    resourceParts.add(target);

    hop = new JPANavigationProptertyInfo(helper.sd, null, null, et);
    hops.add(hop);

    when(item.getEntityType()).thenReturn(et);
    when(item.getUriInfo()).thenReturn(uriInfo);
    when(item.getHops()).thenReturn(hops);
    when(item.getExpandAssociation()).thenReturn(helper.getJPAEntityType("Organizations")
        .getAssociationPath("Roles"));
    when(uriInfo.getUriResourceParts()).thenReturn(resourceParts);
    when(uriEts.getType()).thenReturn(edmType);
    when(edmType.getNamespace()).thenReturn(PUNIT_NAME);
    when(edmType.getName()).thenReturn("Organization");
    return item;
  }

  private void setSimpleKey(final Integer value) throws ODataJPAModelException {
    simpleKey = new HashMap<>(1);
    simpleKey.put(helper.getJPAEntityType("Organizations").getKey().get(0), value);
    orgPair.setValue(simpleKey);
  }

  private void setComplexKey(final String key1, final String key2, final String key3)
      throws ODataJPAModelException {
    simpleKey = new HashMap<>(3);
    final JPAEntityType et = helper.getJPAEntityType("AdministrativeDivisions");
    simpleKey.put(et.getKey().get(0), key3);
    simpleKey.put(et.getKey().get(1), key2);
    simpleKey.put(et.getKey().get(2), key1);
    adminPair.setValue(simpleKey);

  }

}
