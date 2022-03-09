package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Predicate.BooleanOperator;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.eclipse.persistence.internal.jpa.querydef.CompoundExpressionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAProtectionInfo;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;
import com.sap.olingo.jpa.processor.core.testmodel.DeepProtectedExample;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.JPAEntityTypeDouble;
import com.sap.olingo.jpa.processor.core.util.TestQueryBase;

class TestJPAQueryWithProtection extends TestQueryBase {
  private JPAODataSessionContextAccess contextSpy;
  private JPAServiceDocument sdSpy;
  private EdmType odataType;
  private List<JPAAttribute> attributes;
  private Set<String> claimNames;
  private List<String> pathList;
  private JPAEntityType etSpy;
  private List<JPAProtectionInfo> protections;
  private CriteriaBuilder cbSpy;
  private JPARequestParameterMap parameter;

  @Override
  @BeforeEach
  public void setup() throws ODataException, ODataJPAIllegalAccessException {
    super.setup();
    contextSpy = spy(context);
    final JPAEdmProvider providerSpy = spy(context.getEdmProvider());
    sdSpy = spy(context.getEdmProvider().getServiceDocument());
    when(contextSpy.getEdmProvider()).thenReturn(providerSpy);
    when(providerSpy.getServiceDocument()).thenReturn(sdSpy);

    final EntityManager emSpy = spy(emf.createEntityManager());
    parameter = mock(JPARequestParameterMap.class);
    cbSpy = spy(emSpy.getCriteriaBuilder());
    when(emSpy.getCriteriaBuilder()).thenReturn(cbSpy);
    when(externalContext.getEntityManager()).thenReturn(emSpy);
    when(externalContext.getRequestParameter()).thenReturn(parameter);
  }

  @Test
  void testRestrictOnePropertyOneValue() throws IOException, ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID,Name1,Country", claims);
    helper.assertStatus(200);

    final ArrayNode bupa = helper.getValues();
    assertEquals(3, bupa.size());
  }

  @ParameterizedTest
  @ValueSource(strings = { "Wil*", "Wi%i", "Wil+i", "Will_", "Wi*i", "_illi" })
  void testRestrictOnePropertyOneValueWithWildcard(final String minValue) throws IOException, ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>(minValue));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID,Name1,Country", claims);
    helper.assertStatus(200);

    final ArrayNode bupa = helper.getValues();
    assertEquals(3, bupa.size());
  }

  @Test
  void testRestrictOnePropertyTwoValues() throws IOException, ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID,Name1,Country", claims);
    helper.assertStatus(200);

    final ArrayNode bupa = helper.getValues();
    assertEquals(13, bupa.size());
  }

  @ParameterizedTest
  @CsvSource({
      "200, 'Willi;Marvin', 13",
      "200, 'Willi', 3", })
  void testRestrictOnePropertyCount(final int statusCodeValue, final String claimEntries,
      final int noResults) throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    final String[] claimEntriesList = claimEntries.split(";");
    for (final String claimEntry : claimEntriesList) {
      claims.add("UserId", new JPAClaimsPair<>(claimEntry));
    }
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds/$count", claims);
    helper.assertStatus(statusCodeValue);

    final ValueNode act = helper.getSingleValue();
    assertEquals(noResults, act.asInt());
  }

  @Test
  void testRestrictNavigationResult() throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));
    claims.add("RoleCategory", new JPAClaimsPair<>("A", "B"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds('3')/RolesProtected", claims);
    helper.assertStatus(200);

    final ArrayNode act = helper.getValues();
    assertEquals(2, act.size());
  }

  @Test
  void testRestrictExpandResult() throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));
    claims.add("RoleCategory", new JPAClaimsPair<>("A", "B"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$filter=ID eq '3'&$expand=RolesProtected", claims);
    helper.assertStatus(200);

    final ArrayNode act = helper.getValues();
    assertEquals(1, act.size());
    final ArrayNode actExpand = (ArrayNode) act.get(0).get("RolesProtected");
    assertEquals(2, actExpand.size());
  }

  @Test
  void testRestrictExpandResultWithTop() throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));
    claims.add("RoleCategory", new JPAClaimsPair<>("A", "B"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$filter=ID eq '3'&$expand=RolesProtected($top=1)", claims);
    helper.assertStatus(200);

    final ArrayNode act = helper.getValues();
    assertEquals(1, act.size());
    final ArrayNode actExpand = (ArrayNode) act.get(0).get("RolesProtected");
    assertEquals(1, actExpand.size());
  }

  @Test
  void testThrowsUnauthorizedOnMissingClaimforRestrictExpandResult() throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$filter=ID eq '3'&$expand=RolesProtected", claims);
    helper.assertStatus(403);
  }

  @ParameterizedTest
  @CsvSource({
      "200, 'Willi;Marvin', 13",
      "200, 'Willi', 3", })
  void testRestrictOnePropertyInlineCount(final int statusCodeValue, final String claimEntries,
      final int noResults) throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    final String[] claimEntriesList = claimEntries.split(";");
    for (final String claimEntry : claimEntriesList) {
      claims.add("UserId", new JPAClaimsPair<>(claimEntry));
    }
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$count=true", claims);
    helper.assertStatus(statusCodeValue);

    final ValueNode act = helper.getSingleValue("@odata.count");
    assertEquals(noResults, act.asInt());
  }

  @Test
  void testRestrictOnePropertyNoProvider() throws IOException, ODataException {
    final JPAODataClaimsProvider claims = null;

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID,Name1,Country", claims);
    helper.assertStatus(403);
  }

  @Test
  void testRestrictOnePropertyNoValue() throws IOException, ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID,Name1,Country", claims);
    helper.assertStatus(403);
  }

  @Test
  void testRestrictOnePropertyBetweenValues() throws IOException, ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin", "Willi"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID,Name1,Country", claims);
    helper.assertStatus(200);

    final ArrayNode bupa = helper.getValues();
    assertEquals(13, bupa.size());
  }

  @ParameterizedTest
  @CsvSource({
      "200, '1', 'Max'",
      "200, '2', 'Urs'",
      "200, '7', ''",
  })
  void testRestrictOnePropertyDeep(final int statusCodeValue, final String building, final String firstName)
      throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("Creator", new JPAClaimsPair<>("*"));
    claims.add("Updator", new JPAClaimsPair<>("*"));
    claims.add("BuildingNumber", new JPAClaimsPair<>(building));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "PersonProtecteds?$select=ID,FirstName", claims);
    helper.assertStatus(statusCodeValue);
    final ArrayNode persons = helper.getValues();

    if (!firstName.isEmpty()) {
      assertEquals(1, persons.size());
      assertEquals(firstName, persons.get(0).get("FirstName").asText());
    } else
      assertEquals(0, persons.size());
  }

  @Test
  void testRestrictOnePropertyOneValueWithNavigationToRoles() throws IOException, ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds('99')/Roles", claims);
    helper.assertStatus(200);

    final ArrayNode bupa = helper.getValues();
    assertEquals(2, bupa.size());
  }

  @Test
  void testRestrictComplexOnePropertyOneValue() throws ODataException, JPANoSelectionException {
    prepareTest();
    prepareComplexAttributeCreateUser("UserId");

    claimNames.add("UserId");
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));

    final JPAAttribute aSpy = spy(etSpy.getAttribute("administrativeInformation").get());
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath("UserId");
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertEqual(act);
  }

  @ParameterizedTest
  @ValueSource(strings = { "Mar*", "Mar%", "Mar+", "Mar_" })
  void testRestrictComplexOnePropertyOneValueWildcardTrue(final String minValue) throws ODataException,
      JPANoSelectionException {
    prepareTest();
    prepareComplexAttributeUser("UserId", "AdministrativeInformation/Created/By", "created", true);

    claimNames.add("UserId");
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>(minValue));

    final JPAAttribute aSpy = spy(etSpy.getAttribute("administrativeInformation").get());
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath("UserId");
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertLike(act);
  }

  @Test
  void testRestrictComplexOnePropertyUpperLowerValues() throws ODataException, JPANoSelectionException {
    final String claimName = "UserId";
    prepareTest();
    prepareComplexAttributeCreateUser(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>("Marvin", "Willi"));

    final JPAAttribute aSpy = spy(etSpy.getAttribute("administrativeInformation").get());
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath("UserId");
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertBetween(act);
  }

  @Test
  void testRestrictComplexOnePropertyUpperLowerValuesWildcardTrue() throws ODataException,
      JPANoSelectionException {
    final String claimName = "UserId";
    prepareTest();
    prepareComplexAttributeUser(claimName, "AdministrativeInformation/Created/By", "created", true);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>("Marv*", "Willi"));

    final JPAAttribute aSpy = spy(etSpy.getAttribute("administrativeInformation").get());
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath("UserId");
    attributes.add(aSpy);

    assertThrows(ODataJPAQueryException.class, () -> ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(
        claims)));

  }

  @Test
  void testRestrictComplexOnePropertyTwoValues() throws ODataException, JPANoSelectionException {
    final String claimName = "UserId";
    prepareTest();
    prepareComplexAttributeCreateUser(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>("Marvin"));
    claims.add(claimName, new JPAClaimsPair<>("Willi"));

    final JPAAttribute aSpy = spy(etSpy.getAttribute("administrativeInformation").get());
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath(claimName);
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertEquals(BooleanOperator.OR, ((CompoundExpressionImpl) act).getOperator());
    for (final Expression<?> part : ((CompoundExpressionImpl) act).getChildExpressions())
      assertEqual(part);
  }

  @Test
  void testRestrictComplexOnePropertyOneValuesDate() throws ODataException, JPANoSelectionException {
    final String claimName = "CreationDate";
    prepareTest();
    prepareComplexAttributeDate(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>(Date.valueOf("2010-01-01")));

    final JPAAttribute aSpy = spy(etSpy.getAttribute("administrativeInformation").get());
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath(claimName);
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertEqual(act);
  }

  @Test
  void testRestrictComplexOnePropertyUpperLowerValuesDate() throws ODataException, JPANoSelectionException {
    final String claimName = "CreationDate";
    prepareTest();
    prepareComplexAttributeDate(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>(Date.valueOf("2010-01-01"), Date.valueOf("9999-12-30")));

    final JPAAttribute aSpy = spy(etSpy.getAttribute("administrativeInformation").get());
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath(claimName);
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertBetween(act);
  }

  @Test
  void testRestrictComplexTwoPropertyOneValuesOperatorAND() throws ODataException, JPANoSelectionException {
    final String claimName = "UserId";
    prepareTest();
    prepareComplexAttributeCreateUser(claimName);
    prepareComplexAttributeUpdateUser(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>("Marvin"));

    final JPAAttribute aSpy = spy(etSpy.getAttribute("administrativeInformation").get());
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath(claimName);
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    verify(cbSpy).and(any(), any());
    for (final Expression<?> part : ((CompoundExpressionImpl) act).getChildExpressions())
      assertEqual(part);
  }

  @Test
  void testRestrictTwoPropertiesOneValuesOperatorAND() throws ODataException, JPANoSelectionException {
    final String claimName = "UserId";
    prepareTest();
    prepareComplexAttributeCreateUser(claimName);
    prepareComplexAttributeUpdateUser(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));

    final JPAAttribute aSpy = spy(etSpy.getAttribute("administrativeInformation").get());
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath(claimName);
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    verify(cbSpy).and(any(), any());
    for (final Expression<?> part : ((Predicate) act).getExpressions())
      assertEqual(part);
  }

  @Test
  void testAllowAllOnNonStringProperties() throws ODataException, JPANoSelectionException {
    prepareTestDeepProtected();
    when(etSpy.getProtections()).thenCallRealMethod();
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("BuildingNumber", new JPAClaimsPair<>("DEV"));
    claims.add("Floor", new JPAClaimsPair<>(Short.valueOf("12")));
    claims.add("RoomNumber", new JPAClaimsPair<>("*"));
    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertNotNull(act);
    assertEquals(2, ((Predicate) act).getExpressions().size());
    verify(cbSpy).and(any(), any());
  }

  @Test
  void testAllowAllOnNonStringPropertiesAlsoDouble() throws ODataException, JPANoSelectionException {
    prepareTestDeepProtected();
    when(etSpy.getProtections()).thenCallRealMethod();
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("BuildingNumber", new JPAClaimsPair<>("DEV"));
    claims.add("Floor", new JPAClaimsPair<>(Short.valueOf("12")));
    claims.add("RoomNumber", new JPAClaimsPair<>("*"));
    claims.add("RoomNumber", new JPAClaimsPair<>(1, 10));
    ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    verify(cbSpy).and(any(), any());
  }

  @Test
  void testAllowAllOnMultipleClaims() throws ODataException, JPANoSelectionException {
    prepareTestDeepProtected();
    when(etSpy.getProtections()).thenCallRealMethod();
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("BuildingNumber", new JPAClaimsPair<>(JPAClaimsPair.ALL));
    claims.add("Floor", new JPAClaimsPair<>(Short.valueOf("12")));
    claims.add("RoomNumber", new JPAClaimsPair<>(JPAClaimsPair.ALL));
    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertNotNull(act);
    verify(cbSpy, times(0)).and(any(), any());
  }

  private void assertBetween(final Expression<Boolean> act) {
    assertExpression(act, "between", 3);
  }

  private void assertEqual(final Expression<?> act) {
    assertExpression(act, "equal", 2);
  }

  private void assertLike(final Expression<?> act) {
    assertExpression(act, "like", 2);
  }

  private void assertExpression(final Expression<?> act, final String operator, final int size) {
    assertNotNull(act);
    final List<Expression<?>> actChildren = ((CompoundExpressionImpl) act).getChildExpressions();
    assertEquals(size, actChildren.size());
    assertEquals(operator, ((CompoundExpressionImpl) act).getOperation());
    assertEquals("Path", actChildren.get(0).getClass().getInterfaces()[0].getSimpleName());
  }

  private void prepareComplexAttributeUser(final String claimName, final String pathName,
      final String intermediateElement) throws ODataJPAModelException {
    prepareComplexAttributeUser(claimName, pathName, intermediateElement, false);
  }

  private void prepareComplexAttributeUser(final String claimName, final String pathName,
      final String intermediateElement, final boolean wildcardSupported) throws ODataJPAModelException {

    final JPAProtectionInfo protection = mock(JPAProtectionInfo.class);
    protections.add(protection);

    final String path = pathName;
    pathList.add(path);
    final JPAPath jpaPath = mock(JPAPath.class);
    final JPAElement adminAttribute = mock(JPAElement.class);
    final JPAElement complexAttribute = mock(JPAElement.class);
    final JPAAttribute simpleAttribute = mock(JPAAttribute.class);
    final List<JPAElement> pathElements = Arrays.asList(adminAttribute, complexAttribute, simpleAttribute);
    doReturn(pathElements).when(jpaPath).getPath();
    doReturn("administrativeInformation").when(adminAttribute).getInternalName();
    doReturn(intermediateElement).when(complexAttribute).getInternalName();
    doReturn("by").when(simpleAttribute).getInternalName();
    doReturn(String.class).when(simpleAttribute).getType();
    doReturn(simpleAttribute).when(jpaPath).getLeaf();
    doReturn(jpaPath).when(etSpy).getPath(path);

    doReturn(simpleAttribute).when(protection).getAttribute();
    doReturn(jpaPath).when(protection).getPath();
    doReturn(claimName).when(protection).getClaimName();
    doReturn(wildcardSupported).when(protection).supportsWildcards();
  }

  private void prepareComplexAttributeCreateUser(final String claimName) throws ODataJPAModelException {
    prepareComplexAttributeUser(claimName, "AdministrativeInformation/Created/By", "created");
  }

  private void prepareComplexAttributeUpdateUser(final String claimName) throws ODataJPAModelException {
    prepareComplexAttributeUser(claimName, "AdministrativeInformation/Updated/By", "updated");
  }

  private void prepareComplexAttributeDate(final String claimName) throws ODataJPAModelException {

    final JPAProtectionInfo protection = mock(JPAProtectionInfo.class);
    protections.add(protection);

    final String path = "AdministrativeInformation/Created/At";
    pathList.add(path);
    final JPAPath jpaPath = mock(JPAPath.class);
    final JPAElement adminAttribute = mock(JPAElement.class);
    final JPAElement complexAttribute = mock(JPAElement.class);
    final JPAAttribute simpleAttribute = mock(JPAAttribute.class);
    final List<JPAElement> pathElements = Arrays.asList(adminAttribute, complexAttribute, simpleAttribute);
    doReturn(pathElements).when(jpaPath).getPath();
    doReturn("administrativeInformation").when(adminAttribute).getInternalName();
    doReturn("created").when(complexAttribute).getInternalName();
    doReturn("at").when(simpleAttribute).getInternalName();
    doReturn(Date.class).when(simpleAttribute).getType();
    doReturn(simpleAttribute).when(jpaPath).getLeaf();
    doReturn(jpaPath).when(etSpy).getPath(path);

    doReturn(simpleAttribute).when(protection).getAttribute();
    doReturn(jpaPath).when(protection).getPath();
    doReturn(claimName).when(protection).getClaimName();

  }

  private void prepareTest() throws ODataException, JPANoSelectionException {
    buildUriInfo("BusinessPartnerProtecteds", "BusinessPartnerProtected");
    odataType = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getType();
    attributes = new ArrayList<>();
    claimNames = new HashSet<>();
    pathList = new ArrayList<>();
    protections = new ArrayList<>();

    etSpy = spy(new JPAEntityTypeDouble(sdSpy.getEntity("BusinessPartnerProtecteds")));
    doReturn(attributes).when(etSpy).getAttributes();
    doReturn(protections).when(etSpy).getProtections();
    doReturn(etSpy).when(sdSpy).getEntity("BusinessPartnerProtecteds");
    doReturn(etSpy).when(sdSpy).getEntity(odataType);
    final JPAODataInternalRequestContext requestContext = new JPAODataInternalRequestContext(externalContext,
        contextSpy);
    try {
      requestContext.setUriInfo(uriInfo);
    } catch (final ODataJPAIllegalAccessException e) {
      fail();
    }
    cut = new JPAJoinQuery(null, requestContext);
    cut.createFromClause(new ArrayList<JPAAssociationPath>(1), new ArrayList<JPAPath>(), cut.cq, null);
  }

  private void prepareTestDeepProtected() throws ODataException, JPANoSelectionException {
    buildUriInfo("ProtectionExamples", "ProtectionExample");
    odataType = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getType();
    attributes = new ArrayList<>();
    claimNames = new HashSet<>();
    pathList = new ArrayList<>();
    protections = new ArrayList<>();

    etSpy = spy(new JPAEntityTypeDouble(sdSpy.getEntity(DeepProtectedExample.class)));
    doReturn(attributes).when(etSpy).getAttributes();
    doReturn(protections).when(etSpy).getProtections();
    doReturn(etSpy).when(sdSpy).getEntity("ProtectionExamples");
    doReturn(etSpy).when(sdSpy).getEntity(odataType);
    doReturn(null).when(etSpy).getAssociation("");
    final JPAODataInternalRequestContext requestContext = new JPAODataInternalRequestContext(externalContext, context);
    try {
      requestContext.setUriInfo(uriInfo);
    } catch (final ODataJPAIllegalAccessException e) {
      fail();
    }
    cut = new JPAJoinQuery(null, requestContext);
    cut.createFromClause(new ArrayList<JPAAssociationPath>(1), new ArrayList<JPAPath>(), cut.cq, null);
  }
}
