package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
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
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.exception.JPAIllicalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.processor.JPAODataRequestContextImpl;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.JPAEntityTypeDouble;
import com.sap.olingo.jpa.processor.core.util.TestQueryBase;

public class TestJPAQueryWithProtection extends TestQueryBase {
  private JPAODataCRUDContextAccess contextSpy;
  private JPAServiceDocument sdSpy;
  private EdmType odataType;
  private List<JPAAttribute> attributes;
  private Set<String> claimNames;
  private List<String> pathList;
  private JPAEntityType etSpy;
  private List<JPAProtectionInfo> protections;

  @Override
  @BeforeEach
  public void setup() throws ODataException, JPAIllicalAccessException {
    super.setup();
    contextSpy = Mockito.spy(context);
    JPAEdmProvider providerSpy = Mockito.spy(context.getEdmProvider());
    sdSpy = Mockito.spy(context.getEdmProvider().getServiceDocument());
    when(contextSpy.getEdmProvider()).thenReturn(providerSpy);
    when(providerSpy.getServiceDocument()).thenReturn(sdSpy);

  }

  @Test
  public void testRestrictOnePropertyOneValue() throws IOException, ODataException {
    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID,Name1,Country", claims);
    helper.assertStatus(200);

    final ArrayNode bupa = helper.getValues();
    assertEquals(3, bupa.size());
  }

  @ParameterizedTest
  @ValueSource(strings = { "Wil*", "Wi%i", "Wil+i", "Will_" })
  public void testRestrictOnePropertyOneValueWithWildcard(final String minValue) throws IOException, ODataException {
    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>(minValue));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID,Name1,Country", claims);
    helper.assertStatus(200);

    final ArrayNode bupa = helper.getValues();
    assertEquals(3, bupa.size());
  }

  @Test
  public void testRestrictOnePropertyTwoValues() throws IOException, ODataException {
    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
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
  public void testRestrictOnePropertyCount(final int statusCodeValue, final String claimEntries,
      final int noResults) throws IOException, ODataException {

    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
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
  public void testRestrictNavigationResult() throws IOException, ODataException {

    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));
    claims.add("RoleCategory", new JPAClaimsPair<>("A", "B"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds('3')/RolesProtected", claims);
    helper.assertStatus(200);

    final ArrayNode act = helper.getValues();
    assertEquals(2, act.size());
  }

  @Test
  public void testRestrictExpandResult() throws IOException, ODataException {

    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
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
  public void testThrowsUnauthorizedOnMissingClaimforRestrictExpandResult() throws IOException, ODataException {

    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$filter=ID eq '3'&$expand=RolesProtected", claims);
    helper.assertStatus(403);
  }

  @ParameterizedTest
  @CsvSource({
      "200, 'Willi;Marvin', 13",
      "200, 'Willi', 3", })
  public void testRestrictOnePropertyInlineCount(final int statusCodeValue, final String claimEntries,
      final int noResults) throws IOException, ODataException {

    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
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
  public void testRestrictOnePropertyNoProvider() throws IOException, ODataException {
    JPAODataClaimsProvider claims = null;

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID,Name1,Country", claims);
    helper.assertStatus(403);
  }

  @Test
  public void testRestrictOnePropertyNoValue() throws IOException, ODataException {
    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID,Name1,Country", claims);
    helper.assertStatus(403);
  }

  @Test
  public void testRestrictOnePropertyBetweenValues() throws IOException, ODataException {
    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
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
  public void testRestrictOnePropertyDeep(final int statusCodeValue, final String building, final String firstName)
      throws IOException, ODataException {

    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
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
  public void testRestrictOnePropertyOneValueWithNavigationToRoles() throws IOException, ODataException {
    JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds('99')/Roles", claims);
    helper.assertStatus(200);

    final ArrayNode bupa = helper.getValues();
    assertEquals(2, bupa.size());
  }

  @Test
  public void testRestrictComplexOnePropertyOneValue() throws ODataException, JPANoSelectionException {
    prepareTest();
    prepareComplexAttributeCreateUser("UserId");

    claimNames.add("UserId");
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));

    JPAAttribute aSpy = Mockito.spy(etSpy.getAttribute("administrativeInformation"));
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath("UserId");
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertEqual(act);
  }

  @ParameterizedTest
  @ValueSource(strings = { "Mar*", "Mar%", "Mar+", "Mar_" })
  public void testRestrictComplexOnePropertyOneValueWildcardTrue(final String minValue) throws ODataException,
      JPANoSelectionException {
    prepareTest();
    prepareComplexAttributeUser("UserId", "AdministrativeInformation/Created/By", "created", true);

    claimNames.add("UserId");
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>(minValue));

    JPAAttribute aSpy = Mockito.spy(etSpy.getAttribute("administrativeInformation"));
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath("UserId");
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertLike(act);
  }

  @Test
  public void testRestrictComplexOnePropertyUpperLowerValues() throws ODataException, JPANoSelectionException {
    final String claimName = "UserId";
    prepareTest();
    prepareComplexAttributeCreateUser(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>("Marvin", "Willi"));

    JPAAttribute aSpy = Mockito.spy(etSpy.getAttribute("administrativeInformation"));
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath("UserId");
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertBetween(act);
  }

  @Test
  public void testRestrictComplexOnePropertyUpperLowerValuesWildcardTrue() throws ODataException,
      JPANoSelectionException {
    final String claimName = "UserId";
    prepareTest();
    prepareComplexAttributeUser(claimName, "AdministrativeInformation/Created/By", "created", true);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>("Marv*", "Willi"));

    JPAAttribute aSpy = Mockito.spy(etSpy.getAttribute("administrativeInformation"));
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath("UserId");
    attributes.add(aSpy);

    assertThrows(ODataJPAQueryException.class, () -> ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(
        claims)));

  }

  @Test
  public void testRestrictComplexOnePropertyTwoValues() throws ODataException, JPANoSelectionException {
    final String claimName = "UserId";
    prepareTest();
    prepareComplexAttributeCreateUser(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>("Marvin"));
    claims.add(claimName, new JPAClaimsPair<>("Willi"));

    JPAAttribute aSpy = Mockito.spy(etSpy.getAttribute("administrativeInformation"));
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath(claimName);
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertEquals(BooleanOperator.OR, ((CompoundExpressionImpl) act).getOperator());
    for (Expression<?> part : ((CompoundExpressionImpl) act).getChildExpressions())
      assertEqual(part);
  }

  @Test
  public void testRestrictComplexOnePropertyOneValuesDate() throws ODataException, JPANoSelectionException {
    final String claimName = "CreationDate";
    prepareTest();
    prepareComplexAttributeDate(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>(Date.valueOf("2010-01-01")));

    JPAAttribute aSpy = Mockito.spy(etSpy.getAttribute("administrativeInformation"));
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath(claimName);
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertEqual(act);
  }

  @Test
  public void testRestrictComplexOnePropertyUpperLowerValuesDate() throws ODataException, JPANoSelectionException {
    final String claimName = "CreationDate";
    prepareTest();
    prepareComplexAttributeDate(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>(Date.valueOf("2010-01-01"), Date.valueOf("9999-12-30")));

    JPAAttribute aSpy = Mockito.spy(etSpy.getAttribute("administrativeInformation"));
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath(claimName);
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertBetween(act);
  }

  @Test
  public void testRestrictComplexTwoPropertyOneValuesOperatorAND() throws ODataException, JPANoSelectionException {
    final String claimName = "UserId";
    prepareTest();
    prepareComplexAttributeCreateUser(claimName);
    prepareComplexAttributeUpdateUser(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add(claimName, new JPAClaimsPair<>("Marvin"));

    JPAAttribute aSpy = Mockito.spy(etSpy.getAttribute("administrativeInformation"));
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath(claimName);
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertEquals(BooleanOperator.AND, ((CompoundExpressionImpl) act).getOperator());
    for (Expression<?> part : ((CompoundExpressionImpl) act).getChildExpressions())
      assertEqual(part);
  }

  @Test
  public void testRestrictTwoPropertiesOneValuesOperatorAND() throws ODataException, JPANoSelectionException {
    final String claimName = "UserId";
    prepareTest();
    prepareComplexAttributeCreateUser(claimName);
    prepareComplexAttributeUpdateUser(claimName);

    claimNames.add(claimName);
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));

    JPAAttribute aSpy = Mockito.spy(etSpy.getAttribute("administrativeInformation"));
    doReturn(true).when(aSpy).hasProtection();
    doReturn(claimNames).when(aSpy).getProtectionClaimNames();
    doReturn(pathList).when(aSpy).getProtectionPath(claimName);
    attributes.add(aSpy);

    final Expression<Boolean> act = ((JPAJoinQuery) cut).createProtectionWhere(Optional.of(claims));
    assertEquals(BooleanOperator.AND, ((Predicate) act).getOperator());
    for (Expression<?> part : ((Predicate) act).getExpressions())
      assertEqual(part);
  }

  private void assertBetween(Expression<Boolean> act) {
    assertExpression(act, "between", 3);
  }

  private void assertEqual(Expression<?> act) {
    assertExpression(act, "equal", 2);
  }

  private void assertLike(Expression<?> act) {
    assertExpression(act, "like", 2);
  }

  private void assertExpression(Expression<?> act, String operator, int size) {
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

    final JPAProtectionInfo protection = Mockito.mock(JPAProtectionInfo.class);
    protections.add(protection);

    final String path = pathName;
    pathList.add(path);
    final JPAPath jpaPath = Mockito.mock(JPAPath.class);
    final JPAElement adminAttri = Mockito.mock(JPAElement.class);
    final JPAElement complexAttri = Mockito.mock(JPAElement.class);
    final JPAAttribute simpleAttri = Mockito.mock(JPAAttribute.class);
    final List<JPAElement> pathElements = Arrays.asList(new JPAElement[] { adminAttri, complexAttri, simpleAttri });
    doReturn(pathElements).when(jpaPath).getPath();
    doReturn("administrativeInformation").when(adminAttri).getInternalName();
    doReturn(intermediateElement).when(complexAttri).getInternalName();
    doReturn("by").when(simpleAttri).getInternalName();
    doReturn(String.class).when(simpleAttri).getType();
    doReturn(simpleAttri).when(jpaPath).getLeaf();
    doReturn(jpaPath).when(etSpy).getPath(path);

    doReturn(simpleAttri).when(protection).getAttribute();
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

    final JPAProtectionInfo protection = Mockito.mock(JPAProtectionInfo.class);
    protections.add(protection);

    final String path = "AdministrativeInformation/Created/At";
    pathList.add(path);
    final JPAPath jpaPath = Mockito.mock(JPAPath.class);
    final JPAElement adminAttri = Mockito.mock(JPAElement.class);
    final JPAElement complexAttri = Mockito.mock(JPAElement.class);
    final JPAAttribute simpleAttri = Mockito.mock(JPAAttribute.class);
    final List<JPAElement> pathElements = Arrays.asList(new JPAElement[] { adminAttri, complexAttri, simpleAttri });
    doReturn(pathElements).when(jpaPath).getPath();
    doReturn("administrativeInformation").when(adminAttri).getInternalName();
    doReturn("created").when(complexAttri).getInternalName();
    doReturn("at").when(simpleAttri).getInternalName();
    doReturn(Date.class).when(simpleAttri).getType();
    doReturn(simpleAttri).when(jpaPath).getLeaf();
    doReturn(jpaPath).when(etSpy).getPath(path);

    doReturn(simpleAttri).when(protection).getAttribute();
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

    etSpy = Mockito.spy(new JPAEntityTypeDouble(sdSpy.getEntity("BusinessPartnerProtecteds")));
    doReturn(attributes).when(etSpy).getAttributes();
    doReturn(protections).when(etSpy).getProtections();
    doReturn(etSpy).when(sdSpy).getEntity("BusinessPartnerProtecteds");
    doReturn(etSpy).when(sdSpy).getEntity(odataType);
    final JPAODataRequestContextImpl requestContext = new JPAODataRequestContextImpl();
    requestContext.setEntityManager(emf.createEntityManager());
    try {
      requestContext.setUriInfo(uriInfo);
    } catch (JPAIllicalAccessException e) {
      fail();
    }
    cut = new JPAJoinQuery(null, contextSpy, headers, requestContext);
    cut.createFromClause(new ArrayList<JPAAssociationPath>(1), new ArrayList<JPAPath>(), cut.cq, null);
  }
}
