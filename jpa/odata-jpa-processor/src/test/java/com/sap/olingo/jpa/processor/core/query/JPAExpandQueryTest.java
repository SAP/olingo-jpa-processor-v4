package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.database.JPADefaultDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.testmodel.BusinessPartnerRole;
import com.sap.olingo.jpa.processor.core.testmodel.CurrentUser;
import com.sap.olingo.jpa.processor.core.testmodel.CurrentUserQueryExtension;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;

abstract class JPAExpandQueryTest extends TestBase {
  protected JPAExpandQuery cut;
  protected EntityManager em;
  protected JPAODataRequestContextAccess requestContext;
  protected TestHelper helper;
  protected JPAKeyPair organizationPair;
  protected JPAKeyPair adminPair;
  protected Optional<JPAKeyBoundary> organizationBoundary;
  protected Optional<JPAKeyBoundary> adminBoundary;
  @SuppressWarnings("rawtypes")
  protected Map<JPAAttribute, Comparable> simpleKey;

  @BeforeEach
  void setup() throws ODataException {
    createHeaders();
    helper = new TestHelper(emf, PUNIT_NAME);
    em = emf.createEntityManager();
    requestContext = mock(JPAODataRequestContextAccess.class);
    organizationPair = new JPAKeyPair(helper.getJPAEntityType("Organizations").getKey());
    organizationBoundary = Optional.of(new JPAKeyBoundary(1, organizationPair));
    adminPair = new JPAKeyPair(helper.getJPAEntityType("AdministrativeDivisions").getKey());
    adminBoundary = Optional.of(new JPAKeyBoundary(1, adminPair));
    final JPAServiceDebugger debugger = mock(JPAServiceDebugger.class);

    when(requestContext.getDebugger()).thenReturn(debugger);
    when(requestContext.getClaimsProvider()).thenReturn(Optional.empty());
    when(requestContext.getEntityManager()).thenReturn(em);
    when(requestContext.getHeader()).thenReturn(mock(JPAHttpHeaderMap.class));
    when(requestContext.getRequestParameter()).thenReturn(mock(JPARequestParameterMap.class));
    when(requestContext.getEdmProvider()).thenReturn(helper.edmProvider);
    when(requestContext.getOperationConverter()).thenReturn(new JPADefaultDatabaseProcessor());
  }

  @Test
  void testSingletonSelectAllWithAllExpand() throws ODataException {
    // .../CurrentUser?$expand=Roles&$format=json
    final JPAInlineItemInfo item = createCurrentUserExpandRoles(Collections.emptyList());
    cut = createCut(item);
    final JPAExpandQueryResult act = cut.execute();
    assertEquals(1, act.getNoResults());
  }

  @Test
  void testSelectOrganizationByIdWithAllExpand() throws ODataException {

    // .../Organizations('2')?$expand=Roles&$format=json
    final UriParameter key = mock(UriParameter.class);
    when(key.getName()).thenReturn("ID");
    when(key.getText()).thenReturn("'2'");
    final List<UriParameter> keyPredicates = new ArrayList<>();
    keyPredicates.add(key);
    final JPAInlineItemInfo item = createOrganizationExpandRoles(keyPredicates);

    cut = createCut(item);
    final JPAExpandQueryResult act = cut.execute();
    assertEquals(1, act.getNoResults());
    assertEquals(2, act.getNoResultsDeep());
  }

  @Test
  void testExpandViaNavigation() throws ODataException {
    // .../Persons('98')/SupportedOrganizations?$expand=Roles
    final UriParameter key = mock(UriParameter.class);
    when(key.getName()).thenReturn("ID");
    when(key.getText()).thenReturn("'98'");
    final List<UriParameter> keyPredicates = Collections.singletonList(key);

    final JPAInlineItemInfo item = createPersonSupportedOrganizationsExpandRoles(keyPredicates);
    cut = createCut(item);
    final JPAExpandQueryResult act = cut.execute();
    assertEquals(1, act.getNoResults());
    assertNotNull(act.getResult("1"));
  }

  protected abstract JPAExpandQuery createCut(JPAInlineItemInfo item) throws ODataException;

  protected JPAInlineItemInfo createOrganizationExpandRoles(final List<UriParameter> keyPredicates)
      throws ODataJPAModelException,
      ODataApplicationException {

    final var et = helper.getJPAEntityType(Organization.class);
    return createWithExpandRoles(keyPredicates, et, "Organizations");
  }

  protected JPAInlineItemInfo createCurrentUserExpandRoles(final List<UriParameter> keyPredicates)
      throws ODataJPAModelException, ODataApplicationException {

    final var et = helper.getJPAEntityType(CurrentUser.class);
    final var provider = new CurrentUserQueryExtension();
    when(requestContext.getQueryEnhancement(et)).thenReturn(Optional.of(provider));
    return createWithExpandRoles(keyPredicates, et, "CurrentUser");
  }

  private JPAInlineItemInfo createPersonSupportedOrganizationsExpandRoles(final List<UriParameter> keyPredicates)
      throws ODataJPAModelException, ODataApplicationException {
    final var person = helper.getJPAEntityType(Person.class);
    final var organization = helper.getJPAEntityType(Organization.class);
    final var associationPath = organization.getAssociationPath("Roles");
    final var supportedOrganizations = person.getAssociationPath("SupportedOrganizations");

    final var roleNavigation = createUriResourceNavigation("Roles", "BusinessPartnerRole");
    final var supportedOrganizationsNavigation = createUriResourceNavigation("SupportedOrganizations", organization
        .getExternalName());
    final var esResource = createEntitySetResource(keyPredicates, "Persons", person);

    final var uriInfo = mock(UriInfoResource.class);
    final var orderByOption = mock(OrderByOption.class);
    when(uriInfo.getOrderByOption()).thenReturn(orderByOption);
    when(uriInfo.getUriResourceParts()).thenReturn(Collections.singletonList(roleNavigation));

    final var uriInfo2 = mock(UriInfoResource.class);
    when(uriInfo2.getOrderByOption()).thenReturn(orderByOption);
    when(uriInfo2.getUriResourceParts()).thenReturn(Arrays.asList(esResource, supportedOrganizationsNavigation));

    final var hops = Arrays.asList(
        new JPANavigationPropertyInfo(helper.sd, esResource, supportedOrganizations, null),
        new JPANavigationPropertyInfo(helper.sd, associationPath, uriInfo2, organization),
        new JPANavigationPropertyInfo(helper.sd, null, uriInfo, (JPAEntityType) associationPath.getTargetType()));
    final JPAInlineItemInfo item = mock(JPAInlineItemInfo.class);

    when(item.getExpandAssociation()).thenReturn(associationPath);
    when(item.getEntityType()).thenReturn((JPAEntityType) associationPath.getTargetType());
    when(item.getHops()).thenReturn(hops);
    when(item.getUriInfo()).thenReturn(uriInfo);
    return item;
  }

  private UriResourceNavigation createUriResourceNavigation(final String name, final String targetName) {
    final var navigation = mock(UriResourceNavigation.class);
    final var property = mock(EdmNavigationProperty.class);
    final var edmType = mock(EdmEntityType.class);
    when(navigation.getProperty()).thenReturn(property);
    when(navigation.getType()).thenReturn(edmType);
    when(property.getName()).thenReturn(name);
    when(property.getType()).thenReturn(edmType);
    when(edmType.getNamespace()).thenReturn(PUNIT_NAME);
    when(edmType.getName()).thenReturn(targetName);
    return navigation;
  }

  JPAInlineItemInfo createWithExpandRoles(final List<UriParameter> keyPredicates, final JPAEntityType parent,
      final String esName) throws ODataJPAModelException, ODataApplicationException {

    final JPAEntityType et = helper.getJPAEntityType(BusinessPartnerRole.class);
    final JPAExpandItemWrapper uriExpandInfo = mock(JPAExpandItemWrapper.class);

    JPANavigationPropertyInfo hop = createRoleNavigationPropertyInfo(keyPredicates, parent.getAssociationPath("Roles"),
        esName, et);

    final List<JPANavigationPropertyInfo> hops = new ArrayList<>();
    hops.add(hop);

    final JPAInlineItemInfo item = mock(JPAInlineItemInfo.class);
    final UriResourceNavigation target = mock(UriResourceNavigation.class);
    final EdmNavigationProperty targetProperty = mock(EdmNavigationProperty.class);
    when(targetProperty.getName()).thenReturn("Roles");
    when(target.getProperty()).thenReturn(targetProperty);
    final List<UriResource> resourceParts = new ArrayList<>();
    resourceParts.add(target);

    hop = new JPANavigationPropertyInfo(helper.sd, null, uriExpandInfo, et);
    hops.add(hop);

    when(item.getEntityType()).thenReturn(et);
    when(item.getUriInfo()).thenReturn(uriExpandInfo);
    when(item.getHops()).thenReturn(hops);
    when(item.getExpandAssociation()).thenReturn(parent.getAssociationPath("Roles"));
    when(uriExpandInfo.getUriResourceParts()).thenReturn(resourceParts);
    return item;
  }

  private JPANavigationPropertyInfo createRoleNavigationPropertyInfo(final List<UriParameter> keyPredicates,
      final JPAAssociationPath jpaAssociationPath, final String esName, final JPAEntityType et)
      throws ODataApplicationException {

    final UriInfo uriInfo = mock(UriInfo.class);
    final UriResourceEntitySet uriEs = createEntitySetResource(keyPredicates, esName, et);
    when(uriInfo.getUriResourceParts()).thenReturn(Collections.singletonList(uriEs));
    return new JPANavigationPropertyInfo(helper.sd, uriEs, jpaAssociationPath, uriInfo);
  }

  private UriResourceEntitySet createEntitySetResource(final List<UriParameter> keyPredicates, final String esName,
      final JPAEntityType et) {
    final UriResourceEntitySet uriEs = mock(UriResourceEntitySet.class);
    when(uriEs.getKeyPredicates()).thenReturn(keyPredicates);
    final EdmEntityType edmType = mock(EdmEntityType.class);
    final EdmEntitySet edmSet = mock(EdmEntitySet.class);

    when(uriEs.getType()).thenReturn(edmType);
    when(uriEs.getEntitySet()).thenReturn(edmSet);
    when(edmSet.getName()).thenReturn(esName);
    when(edmType.getNamespace()).thenReturn(PUNIT_NAME);
    when(edmType.getName()).thenReturn(et.getExternalName());
    return uriEs;
  }

}