package com.sap.olingo.jpa.processor.core.query;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

public class TestJPAExpandResult extends TestBase {
  private JPAExpandQuery cut;
  private EntityManager em;
  private JPAODataSessionContextAccess sessionContext;
  private TestHelper helper;

  @Before
  public void setup() throws ODataException {
    createHeaders();
    helper = new TestHelper(emf, PUNIT_NAME);
    em = emf.createEntityManager();
    sessionContext = mock(JPAODataSessionContextAccess.class);
    JPAServiceDebugger debugger = mock(JPAServiceDebugger.class);

    when(sessionContext.getEdmProvider()).thenReturn(helper.edmProvider);
    when(sessionContext.getOperationConverter()).thenReturn(new JPADefaultDatabaseProcessor());
    when(sessionContext.getDebugger()).thenReturn(debugger);
  }

  @Test
  public void testSelectAllWithAllExpand() throws ODataException {
    // .../Organizations?$expand=Roles&$format=json
    JPAExpandItemInfo item = createOrgExpandRoles(null, null);

    cut = new JPAExpandQuery(OData.newInstance(), sessionContext, em, item, headers);
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
    JPAExpandItemInfo item = createOrgExpandRoles(keyPredicates, null);

    cut = new JPAExpandQuery(OData.newInstance(), sessionContext, em, item, headers);
    JPAExpandQueryResult act = cut.execute();
    assertEquals(1, act.getNoResults());
    assertEquals(2, act.getNoResultsDeep());
  }

  @Ignore // Moved because of problems creating the WHERE clause temporarily moved to
          // TestJPAQueryWhereClause.testFilterWithAllExpand
  @Test
  public void testSelectOrgByFilterWithAllExpand() throws ODataException {

    // .../Organizations?$filter=Name1 eq 'Third Org.'&$expand=Roles
    JPAExpandItemInfo item = createOrgExpandRoles(null, createOrgFilter(em.getCriteriaBuilder()));

    cut = new JPAExpandQuery(OData.newInstance(), sessionContext, em, item, headers);
    JPAExpandQueryResult act = cut.execute();
    assertEquals(1, act.getNoResults());
    assertEquals(3, act.getNoResultsDeep());
  }

  private JPAExpandItemInfo createOrgExpandRoles(final List<UriParameter> keyPredicates, Expression<Boolean> expression)
      throws ODataJPAModelException {
    JPAEntityType et = helper.getJPAEntityType("BusinessPartnerRoles");
    JPAExpandItemWrapper uriInfo = mock(JPAExpandItemWrapper.class);
    UriResourceEntitySet uriEts = mock(UriResourceEntitySet.class);
    EdmEntityType edmType = mock(EdmEntityType.class);

    JPANavigationProptertyInfo hop = new JPANavigationProptertyInfo(uriEts, helper.getJPAEntityType("Organizations")
        .getAssociationPath("Roles"), keyPredicates, expression);
    List<JPANavigationProptertyInfo> hops = new ArrayList<>();
    hops.add(hop);

    JPAExpandItemInfo item = mock(JPAExpandItemInfo.class);
    UriResourceNavigation target = mock(UriResourceNavigation.class);
    EdmNavigationProperty targetProperty = mock(EdmNavigationProperty.class);
    when(targetProperty.getName()).thenReturn("Roles");
    when(target.getProperty()).thenReturn(targetProperty);
    List<UriResource> resourceParts = new ArrayList<>();
    resourceParts.add(target);

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

  private Expression<Boolean> createOrgFilter(final CriteriaBuilder cb) {
    final CriteriaQuery<Object> cq = cb.createQuery();
    final Root<Organization> org = cq.from(Organization.class);

    return cb.equal(org.get("name1"), cb.literal("Third Org."));
  }

}
