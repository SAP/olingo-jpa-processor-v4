package com.sap.olingo.jpa.processor.core.filter;

import static org.apache.olingo.commons.api.http.HttpStatusCode.FORBIDDEN;
import static org.apache.olingo.commons.api.http.HttpStatusCode.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.metadata.odata.v4.provider.JavaBasedCapabilitiesAnnotationsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.util.Assertions;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryWhereClause extends TestBase {

  static Stream<Arguments> getFilterQuery() {
    return Stream.of(
        // Simple filter
        arguments("OneNotEqual", "Organizations?$filter=ID ne '3'", 9),
        // '10' is smaller than '5' when comparing strings!
        arguments("OneGreaterEquals", "Organizations?$filter=ID ge '5'", 5),
        arguments("OneLowerThanTwo", "AdministrativeDivisions?$filter=DivisionCode lt CountryCode", 244),
        arguments("OneGreaterThan", "Organizations?$filter=ID gt '5'", 4),
        arguments("OneLowerThan", "Organizations?$filter=ID lt '5'", 5),
        arguments("OneLowerEquals", "Organizations?$filter=ID le '5'", 6),
        arguments("OneGreaterEqualsNumber", "AdministrativeDivisions?$filter=Area ge 119330610", 4),
        arguments("OneAndEquals", "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and CodeID eq 'NUTS2'",
            11),
        arguments("OneOrEquals", "Organizations?$filter=ID eq '5' or ID eq '10'", 2),
        arguments("OneNotLower", "AdministrativeDivisions?$filter=not (Area lt 50000000)", 24),
        arguments("AddGreater", "AdministrativeDivisions?$filter=Area add 7000000 ge 50000000", 31),
        arguments("SubGreater", "AdministrativeDivisions?$filter=Area sub 7000000 ge 60000000", 15),
        arguments("DivGreater", "AdministrativeDivisions?$filter=Area gt 0 and Area div Population ge 6000", 9),
        arguments("MulGreater", "AdministrativeDivisions?$filter=Area mul Population gt 0", 64),
        arguments("Mod", "AdministrativeDivisions?$filter=Area gt 0 and Area mod 3578335 eq 0", 1),
        arguments("Length", "AdministrativeDivisionDescriptions?$filter=length(Name) eq 10", 11),
        arguments("Now", "Persons?$filter=AdministrativeInformation/Created/At lt now()", 3),
        arguments("Contains", "AdministrativeDivisions?$filter=contains(CodeID,'166')", 110),
        arguments("Endswith", "AdministrativeDivisions?$filter=endswith(CodeID,'166-1')", 4),
        arguments("Startswith", "AdministrativeDivisions?$filter=startswith(DivisionCode,'DE-')", 16),
        arguments("Not Startswith", "AdministrativeDivisions?$filter=not startswith(DivisionCode,'BE')", 176),
        arguments("IndexOf", "AdministrativeDivisions?$filter=indexof(DivisionCode,'3') eq 4", 7),
        arguments("SubstringStartIndex",
            "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and substring(Name,6) eq 'Dakota'", 2),
        arguments("SubstringStartEndIndex",
            "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and substring(Name,0,5) eq 'North'", 2),
        arguments("SubstringLengthCalculated",
            "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and substring(Name,0,1 add 4) eq 'North'", 2),
        arguments("ToLower",
            "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and tolower(Name) eq 'brandenburg'", 1),
        arguments("ToUpper",
            "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and toupper(Name) eq 'HESSEN'", 1),
        arguments("ToUpperInverse", "AdministrativeDivisions?$filter=toupper('nuts1') eq CodeID", 19),
        arguments("Trim", "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and trim(Name) eq 'Sachsen'", 1),
        arguments("Concat", "Persons?$filter=concat(concat(LastName,','),FirstName) eq 'Mustermann,Max'", 1),
        arguments("OneHas", "Persons?$filter=AccessRights has com.sap.olingo.jpa.AccessRights'READ'", 1),
        arguments("OnNull", "AdministrativeDivisions?$filter=CodePublisher eq 'ISO' and ParentCodeID eq null", 4),
        arguments("OneEqualsTwoProperties", "AdministrativeDivisions?$filter=DivisionCode eq CountryCode", 4),
        arguments("SubstringStartEndIndexToLower",
            "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and tolower(substring(Name,0,5)) eq 'north'",
            2),
        // IN expression
        arguments("Simple IN", "AdministrativeDivisions?$filter=ParentDivisionCode in ('BE1', 'BE2')", 6),
        arguments("Simple NOT IN", "AdministrativeDivisions?$filter=not (ParentDivisionCode in ('BE1', 'BE2'))", 219),
        // Filter to many associations
        arguments("NavigationPropertyToManyValueAnyNoRestriction", "Organizations?$select=ID&$filter=Roles/any()", 4),
        arguments("NavigationPropertyToManyValueAnyMultiParameter",
            "Organizations?$select=ID&$filter=Roles/any(d:d/RoleCategory eq 'A' and d/BusinessPartnerID eq '1')", 1),
        arguments("NavigationPropertyToManyValueNotAny",
            "Organizations?$filter=not (Roles/any(d:d/RoleCategory eq 'A'))", 7),
        arguments("NavigationPropertyToManyValueAny",
            "Organizations?$select=ID&$filter=Roles/any(d:d/RoleCategory eq 'A')", 3),
        arguments("NavigationPropertyToManyValueAll",
            "Organizations?$select=ID&$filter=Roles/all(d:d/RoleCategory eq 'A')", 1),
        arguments("NavigationPropertyToManyNested2Level",
            "AdministrativeDivisions?$filter=Children/any(c:c/Children/any(cc:cc/ParentDivisionCode eq 'BE25'))", 1),
        arguments("NavigationPropertyToManyNested3Level",
            "AdministrativeDivisions?$filter=Children/any(c:c/Children/any(cc:cc/Children/any(ccc:ccc/ParentDivisionCode eq 'BE251')))",
            1),
        arguments("NavigationPropertyToManyNestedWithJoinTable",
            "Organizations?$select=ID&$filter=SupportEngineers/any(s:s/AdministrativeInformation/Created/User/Roles/any(a:a/RoleCategory eq 'Y'))",
            2),

        arguments("NavigationPropertyDescriptionViaComplexTypeWOSubselectSelectAll",
            "Organizations?$filter=Address/RegionName eq 'Kalifornien'", 3),
        arguments("NavigationPropertyDescriptionViaComplexTypeWOSubselectSelectId",
            "Organizations?$filter=Address/RegionName eq 'Kalifornien'&$select=ID", 3),
        arguments("NavigationPropertyDescriptionToOneValueViaComplexTypeWSubselect1",
            "Organizations?$filter=AdministrativeInformation/Created/User/LocationName eq 'Schweiz'", 1),
        arguments("NavigationPropertyDescriptionToOneValueViaComplexTypeWSubselect2",
            "Organizations?$filter=AdministrativeInformation/Created/User/LocationName eq 'Schweiz'&$select=ID", 1),

        // Filter collection property
        arguments("CountCollectionPropertyOne",
            "Persons?$select=ID&$filter=InhouseAddress/any(d:d/Building eq '7')", 1),
        arguments("CollectionPropertyViaCast",
            "Organizations?$select=ID&$filter=SupportEngineers/any(s:s/InhouseAddress/any(a:a/Building eq '2'))", 2),
        // https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398301
        // Example 43: return all Categories with less than 10 products
        // Filter to many associations count
        arguments("NoChildren", "AdministrativeDivisions?$filter=Children/$count eq 0", 220),
        arguments("CountNavigationPropertyTwo", "Organizations?$select=ID&$filter=Roles/$count eq 2", 1),
        arguments("CountNavigationPropertyZero", "Organizations?$select=ID&$filter=Roles/$count eq 0", 6),
        arguments("CountNavigationPropertyMultipleHops",
            "Organizations?$select=ID&$filter=AdministrativeInformation/Created/User/Roles/$count ge 2", 8),
        arguments("CountNavigationPropertyMultipleHopsNavigations not zero",
            "AdministrativeDivisions?$filter=Parent/Children/$count eq 2", 2),
        arguments("CountNavigationPropertyMultipleHopsNavigations zero",
            "AdministrativeDivisions?$filter=Parent/Children/$count eq 0", 0),
        arguments("CountNavigationPropertyJoinTable not zero", "JoinSources?$filter=OneToMany/$count eq 4", 1),
        // Filter collection property count
        arguments("CountCollectionPropertyOne", "Organizations?$select=ID&$filter=Comment/$count ge 1", 2),
        arguments("CountCollectionPropertyTwoJoinOne", "CollectionWithTwoKeys?$filter=Nested/$count eq 1", 1),
        arguments("CountCollectionPropertyTwoJoinZero", "CollectionWithTwoKeys?$filter=Nested/$count eq 0", 3),
        // To one association null
        arguments("NavigationPropertyIsNull",
            "AssociationOneToOneSources?$format=json&$filter=ColumnTarget eq null", 1),
        arguments("NavigationPropertyIsNull",
            "AssociationOneToOneSources?$format=json&$filter=ColumnTarget ne null", 3),
        arguments("NavigationPropertyIsNullOneHop",
            "AdministrativeDivisions?$filter=Parent/Parent eq null and CodePublisher eq 'Eurostat'", 11),
        arguments("NavigationPropertyMixCountAndNull",
            "AdministrativeDivisions?$filter=Parent/Children/$count eq 2 and Parent/Parent/Parent eq null", 2),
        arguments("NavigationPropertyIsNullJoinTable", "JoinTargets?$filter=ManyToOne ne null", 4),
        // Filter to one association
        arguments("NavigationPropertyToOneValue", "AdministrativeDivisions?$filter=Parent/CodeID eq 'NUTS1'", 11),
        arguments("NavigationPropertyToOneValueAndEquals",
            "AdministrativeDivisions?$filter=Parent/CodeID eq 'NUTS1' and DivisionCode eq 'BE34'", 1),
        arguments("NavigationPropertyToOneValueTwoHops",
            "AdministrativeDivisions?$filter=Parent/Parent/CodeID eq 'NUTS1' and DivisionCode eq 'BE212'", 1),
        arguments("NavigationPropertyToOneValueViaComplexType",
            "Organizations?$filter=AdministrativeInformation/Created/User/LastName eq 'Mustermann'", 8));
  }

  static Stream<Arguments> getEnumQuery() {
    return Stream.of(
        arguments("OneEnumNotEqual", "Persons?$filter=AccessRights ne com.sap.olingo.jpa.AccessRights'WRITE'", "97"),
        arguments("OneEnumEqualMultipleValues",
            "Persons?$filter=AccessRights eq com.sap.olingo.jpa.AccessRights'READ,DELETE'", "97")
    // @Disabled("Clarify if GT, LE .. not supported by OData or \"only\" by Olingo")
    // arguments("OneEnumGreaterThan", "Persons?$filter=AccessRights gt com.sap.olingo.jpa.AccessRights'Read'", "99")
    );
  }

  static Stream<Arguments> getFilterCollectionQuery() {
    return Stream.of(
        arguments("SimpleCount", "Persons?$filter=InhouseAddress/$count eq 2", 1),
        arguments("SimpleCountViaJoinTable",
            "JoinSources?$filter=OneToMany/$count gt 1&$select=SourceID", 1),
        arguments("DeepSimpleCount",
            "CollectionDeeps?$filter=FirstLevel/SecondLevel/Comment/$count eq 2&$select=ID", 1),
        arguments("DeepComplexCount",
            "CollectionDeeps?$filter=FirstLevel/SecondLevel/Address/$count eq 2&$select=ID", 1),
        arguments("DeepSimpleCountZero",
            "CollectionDeeps?$filter=FirstLevel/SecondLevel/Comment/$count eq 0&$select=ID", 1),
        arguments("Any", "Organizations?$select=ID&$filter=Comment/any(s:contains(s, 'just'))", 1),
        arguments("AsPartOfComplexAny",
            "CollectionDeeps?$filter=FirstLevel/SecondLevel/Address/any(s:s/TaskID eq 'DEV')", 1));
  }

  static Stream<Arguments> getFilterNavigationPropertyRequiresGroupsQuery() {
    return Stream.of(
        arguments("NavigationRequiresGroupsProvided",
            "BusinessPartnerWithGroupss?$select=ID&$filter=Roles/any(d:d/Details eq 'A')", OK),
        arguments("CollectionRequiresGroupsReturnsForbidden",
            "BusinessPartnerWithGroupss?$select=ID&$filter=Comment/any(s:contains(s, 'just'))", FORBIDDEN),
        arguments("CollectionRequiresGroupsProvided",
            "BusinessPartnerWithGroupss?$select=ID&$filter=Comment/any(s:contains(s, 'just'))", OK),
        arguments("CollectionProtectedPropertyRequiresGroupsReturnsForbidden",
            "BusinessPartnerWithGroupss('99')/InhouseAddress?$filter=RoomNumber eq 1", FORBIDDEN),
        arguments("CollectionProtectedPropertyRequiresGroupsProvided",
            "BusinessPartnerWithGroupss('99')/InhouseAddress?$filter=RoomNumber eq 1", OK));
  }

  @Test
  void testFilterOneEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=ID eq '3'");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(1, organizations.size());
    assertEquals("3", organizations.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneEqualsDateTime() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=CreationDateTime eq 2016-01-20T09:21:23Z");
    helper.assertStatus(200);
    // This test shall ensure that the Date Time value is mapped correct.
    // Unfortunately the query returns an empty result locally, but 10 rows on Jenkins
    final ArrayNode organizations = helper.getValues();
    assertNotNull(organizations);
  }

  @Test
  void testFilterOneDescriptionEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=LocationName eq 'Deutschland'");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(1, organizations.size());
    assertEquals("10", organizations.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneDescriptionEqualsFieldNotSelected() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=LocationName eq 'Deutschland'&$select=ID");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(1, organizations.size());
    assertEquals("10", organizations.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneEnumEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=ABCClass eq com.sap.olingo.jpa.ABCClassification'A'");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(1, organizations.size());
    assertEquals("1", organizations.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneEqualsInvert() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter='3' eq ID");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(1, organizations.size());
    assertEquals("3", organizations.get(0).get("ID").asText());
  }

  @ParameterizedTest
  @MethodSource("getFilterQuery")
  // @Tag(Assertions.CB_ONLY_TEST)
  void testFilterOne(final String text, final String queryString, final int numberOfResults)
      throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, queryString);
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(numberOfResults, organizations.size(), text);
  }

  @ParameterizedTest
  @MethodSource("getEnumQuery")
  void testFilterEnum(final String text, final String queryString, final String result)
      throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, queryString);
    helper.assertStatus(200);

    final ArrayNode persons = helper.getValues();
    assertEquals(1, persons.size());
    assertEquals(result, persons.get(0).get("ID").asText(), text);
  }

  @Test
  void testFilterTwoAndEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and CodeID eq 'NUTS2' and DivisionCode eq 'BE25'");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(1, organizations.size());
    assertEquals("BEL", organizations.get(0).get("CountryCode").asText());
  }

  @Test
  void testFilterAndOrEqualsParenthesis() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and (DivisionCode eq 'BE25' or  DivisionCode eq 'BE24')&$orderby=DivisionCode desc");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(2, organizations.size());
    assertEquals("BE25", organizations.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterAndOrEqualsNoParenthesis() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and DivisionCode eq 'BE25' or  CodeID eq '3166-1'&$orderby=DivisionCode desc");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(5, organizations.size());
    assertEquals("USA", organizations.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterAndWithFunction1() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and contains(tolower(DivisionCode),tolower('BE1'))&$orderby=DivisionCode asc");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(3, organizations.size());
    assertEquals("BE1", organizations.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterAndWithFunction2() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and contains(DivisionCode,'BE1')&$orderby=DivisionCode asc");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(3, organizations.size());
    assertEquals("BE1", organizations.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterAndWithComparisonContainingFunction() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and tolower(DivisionCode) eq tolower('BE1')");
    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(1, organizations.size());
    assertEquals("BE1", organizations.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterComparisonViaNavigationContainingFunction() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$filter=tolower(Organization/Name1) eq 'third org.'");
    helper.assertStatus(200);
    final ArrayNode act = helper.getValues();
    assertEquals(3, act.size());
  }

  @Test
  void testFilterComparisonTwoFunctionsContainingNavigationNotSupported() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$filter=tolower(Organization/Name1) eq tolower(Organization/Name2)");
    helper.assertStatus(501);
  }

  @Test
  void testFilterComparisonViaNavigationContainingNestedFunctionNotSupported() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$filter=contains(tolower(Organization/Name1), 'third org.')");
    helper.assertStatus(501);
  }

  // Usage of mult currently creates parser error: The types 'Edm.Double' and '[Int64, Int32, Int16, Byte, SByte]' are
  // not compatible.
  @Disabled("Usage of mult currently creates parser error")
  @Test
  void testFilterSubstringStartCalculated() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and substring(Name,2 mul 3) eq 'Dakota'");

    helper.assertStatus(200);

    final ArrayNode organizations = helper.getValues();
    assertEquals(2, organizations.size());
  }

  @Test
  void testFilterNavigationPropertyToManyValueAnyProtected() throws IOException, ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$filter=Roles/any(d:d/RoleCategory eq 'X')", claims);

    helper.assertStatus(200);
    final ArrayNode organizations = helper.getValues();
    assertEquals(2, organizations.size());
  }

  @Test
  void testFilterNavigationPropertyToManyValueAnyProtectedThrowsErrorOnMissingClaim() throws IOException,
      ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$filter=RolesProtected/any(d:d/RoleCategory eq 'X')", claims);

    helper.assertStatus(403);
  }

  @Test
  void testFilterNavigationPropertyToManyValueAnyProtectedDeep() throws IOException,
      ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));
    claims.add("RoleCategory", new JPAClaimsPair<>("C"));

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$filter=RolesProtected/any(d:d/RoleCategory eq 'X')", claims);

    helper.assertStatus(200);
    final ArrayNode act = helper.getValues();
    assertEquals(0, act.size());
  }

  @Test
  void testFilterNavigationStartsWithAll() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=InhouseAddress/all(d:startswith(d/TaskID, 'D'))");

    helper.assertStatus(200);
    final ArrayNode person = helper.getValues();
    assertEquals(1, person.size());
    assertEquals("97", person.get(0).get("ID").asText());
  }

  @Test
  void testFilterCountNavigationPropertyProtectedAllResults() throws IOException, ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("*"));
    claims.add("RoleCategory", new JPAClaimsPair<>("A"));
    claims.add("RoleCategory", new JPAClaimsPair<>("C"));
    IntegrationTestHelper helper;
    helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID&$filter=RolesProtected/$count ge 2", claims);

    helper.assertStatus(200);
    final ArrayNode act = helper.getValues();
    assertEquals(2, act.size());
  }

  @Test
  void testFilterCountNavigationPropertyProtected() throws IOException, ODataException {
    // https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398301
    // Example 43: return all Categories with less than 10 products
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));
    claims.add("RoleCategory", new JPAClaimsPair<>("A", "B"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID&$filter=RolesProtected/$count ge 2", claims); // and ID eq '3'

    helper.assertStatus(200);
    final ArrayNode act = helper.getValues();
    assertEquals(1, act.size());
    assertEquals("3", act.get(0).get("ID").asText());
  }

  @Test
  void testFilterCountNavigationPropertyProtectedThrowsErrorOnMissingClaim() throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Marvin"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$select=ID&$filter=RolesProtected/$count ge 2", claims);

    helper.assertStatus(403);
  }

  @Test
  void testFilterNavigationPropertyContainsProtectedDeep() throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("*"));
    claims.add("RoleCategory", new JPAClaimsPair<>("Z"));

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoleProtecteds?$filter=contains(BupaPartnerProtected/Name1, 'o')", claims);

    helper.assertStatus(200);
    final ArrayNode organizations = helper.getValues();
    assertEquals(0, organizations.size());
  }

  @Test
  void testFilterNavigationPropertyEqualsProtectedDeep() throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));
    claims.add("RoleCategory", new JPAClaimsPair<>("*"));

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoleProtecteds?$filter=BupaPartnerProtected/Type eq '1'", claims);

    helper.assertStatus(200);
    final ArrayNode organizations = helper.getValues();
    assertEquals(3, organizations.size());
  }

  @Test
  void testFilterNavigationPropertyAndExpandThatNavigationProperty() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Parent/DivisionCode eq 'BE2'&$expand=Parent");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(5, admin.size());
    assertNotNull(admin.get(3).findValue("Parent"));
    assertFalse(admin.get(3).findValue("Parent") instanceof NullNode);
    assertEquals("BE2", admin.get(3).findValue("Parent").get("DivisionCode").asText());
  }

  @Test
  void testFilterNavigationPropertyViaJoinTableSubtype() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=ID&$filter=SupportedOrganizations/any()");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(2, admin.size());
    assertEquals("98", admin.get(0).findValue("ID").asText());

  }

  @Disabled // EclipseLinkProblem see https://bugs.eclipse.org/bugs/show_bug.cgi?id=529565
  @Test
  void testFilterNavigationPropertyViaJoinTableCountSubType() throws IOException, // NOSONAR
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=ID&$filter=SupportedOrganizations/$count gt 1");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(2, admin.size());
    assertEquals("98", admin.get(0).findValue("ID").asText());

  }

  @Test
  void testFilterMappedNavigationPropertyViaJoinTableSubtype() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=Name1&$filter=SupportEngineers/any(d:d/LastName eq 'Doe')");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(1, admin.size());
    assertEquals("First Org.", admin.get(0).findValue("Name1").asText());

  }

  @Tag(Assertions.CB_ONLY_TEST)
  @Test
  void testFilterNavigationPropertyViaJoinTableCount() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=Teams/$count eq 0&$select=ID");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(1, admin.size());
    assertEquals("98", admin.get(0).findValue("ID").asText());

  }

  @Test
  void testFilterMappedNavigationPropertyViaJoinTableFilter() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Teams?$select=Name&$filter=Member/any(d:d/LastName eq 'Mustermann')");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(2, admin.size());
  }

  @Test
  void testFilterWithAllExpand() throws ODataException, IOException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=Name1 eq 'Third Org.'&$expand=Roles");

    helper.assertStatus(200);
    final ArrayNode organization = helper.getValues();
    assertNotNull(organization);
    assertEquals(1, organization.size());
    assertEquals(3, organization.get(0).get("Roles").size());
  }

  @Test
  void testExpandWithFilterOnCollectionAttribute() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$expand=SupportEngineers($filter=InhouseAddress/any(p:p/Building eq '2'))");
    helper.assertStatus(200);
    final ArrayNode organizations = helper.getValues();
    for (final JsonNode organization : organizations) {
      if (organization.get("ID").asText().equals("1") || organization.get("ID").asText().equals("2")) {
        final ArrayNode supportEngineers = (ArrayNode) organization.get("SupportEngineers");
        assertEquals(1, supportEngineers.size());
        final ArrayNode address = (ArrayNode) supportEngineers.get(0).get("InhouseAddress");
        assertEquals(1, address.size());
        assertEquals("2", address.get(0).get("Building").asText());
      } else {
        final ArrayNode supportEngineers = (ArrayNode) organization.get("SupportEngineers");
        assertEquals(0, supportEngineers.size());
      }
    }
  }

  @Test
  void testAnyFilterOnExpandWithMultipleHops() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$expand=AdministrativeInformation/Created/User($filter=Jobs/any(p:p/Id eq '98'))");
    helper.assertStatus(200);
    final ArrayNode organizations = helper.getValues();
    for (final JsonNode organization : organizations) {
      final JsonNode user = organization.get("AdministrativeInformation").get("Created").get("User");
      if (organization.get("ID").asText().equals("4")) {
        assertNotNull(user);
        assertEquals("98", user.get("ID").asText());
      } else {
        assert (user instanceof NullNode);
      }
    }
  }

  @Test
  void testExpandWithFilterOnNavigation() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=startswith(DivisionCode,'BE2')&$expand=Children($filter=Children/any(c:c/ParentDivisionCode eq 'BE25'))");
    helper.assertStatus(200);
    final ArrayNode divisions = helper.getValues();
    for (final var division : divisions) {
      final ArrayNode children = (ArrayNode) division.get("Children");
      if (division.get("DivisionCode").asText().equals("BE2"))
        assertFalse(children.isEmpty());
      else
        assertTrue(children.isEmpty());
    }
  }

  @Test
  void testFilterNavigationTarget() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')/Children?$filter=DivisionCode eq 'BE21'");
    helper.assertStatus(200);

    final ObjectNode division = helper.getValue();
    final ObjectNode result = (ObjectNode) division.get("value").get(0);
    assertNotNull(result);
    assertEquals("BE21", result.get("DivisionCode").asText());
  }

  @Test
  void testFilterCollectionSimplePropertyThrowsError() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=contains(Comment, 'just')");

    helper.assertStatus(400); // Olingo rejects a bunch of functions.
  }

  @ParameterizedTest
  @MethodSource("getFilterCollectionQuery")
  void testFilterCollectionProperty(final String text, final String queryString, final int result)
      throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, queryString);
    helper.assertStatus(200);

    final ArrayNode deep = helper.getValues();
    assertEquals(result, deep.size());

  }

  @Test
  void testFilterCollectionPropertyAsPartOfComplexWithSelect() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps?$select=FirstLevel/TransientCollection&$filter=FirstLevel/SecondLevel/Address/any(s:s/TaskID eq 'DEV')");
    helper.assertStatus(200);
    final ArrayNode organization = helper.getValues();
    assertNotNull(organization);
    assertEquals(1, organization.size());
  }

  @Test
  void testFilterCollectionOnPropertyWithNavigation() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons('99')/InhouseAddress?$filter=TaskID eq 'DEV'");

    helper.assertStatus(200);
    final ArrayNode address = helper.getValues();
    assertNotNull(address);
    assertEquals(1, address.size());
  }

  @Test
  void testFilterCollectionPropertyWithoutNavigationThrowsError() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=ID&$filter=InhouseAddress/TaskID eq 'DEV'");

    helper.assertStatus(400); // The URI is malformed
  }

  @Test
  void testFilterCollectionPropertyWithoutEntityTypeThrowsError() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionWithTwoKeys?$filter=Nested/any(d:d/Inner/Figure1 eq 1)");
    helper.assertStatus(400);
  }

  @Test
  void testFilterOnGroupedSimplePropertyWithoutGroupsReturnsForbidden() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$filter=Country eq 'DEU'");
    helper.assertStatus(403);
  }

  @Test
  void testFilterOnGroupedSimplePropertyGroupsProvided() throws IOException, ODataException {
    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Person");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$filter=Country eq 'DEU'", groups);
    helper.assertStatus(200);
    final ArrayNode act = helper.getValues();
    for (int i = 0; i < act.size(); i++) {
      final ObjectNode bupa = (ObjectNode) act.get(i);
      if (bupa.get("ID").asText().equals("99")) {
        final ArrayNode inHouse = (ArrayNode) bupa.get("InhouseAddress");
        assertFalse(inHouse.isNull());
        assertEquals(2, inHouse.size());
      }
    }
  }

  @Test
  void testFilterNavigationPropertyRequiresGroupsReturnsForbidden() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$select=ID&$filter=Roles/any(d:d/Details eq 'A')");
    helper.assertStatus(403);
  }

  @ParameterizedTest
  @MethodSource("getFilterNavigationPropertyRequiresGroupsQuery")
  void testFilterNavigationPropertyRequiresGroups(final String text, final String queryString,
      final HttpStatusCode result)
      throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    if (result != FORBIDDEN)
      groups.addGroup("Company");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, queryString, groups);
    helper.assertStatus(result.getStatusCode());
  }

  @Test
  void testFilterOnTransientSimpleProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=contains(FullName, 'willi')");
    helper.assertStatus(501);
  }

  @Test
  void testFilterOnTransientCollectionProperty() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps?$filter=FirstLevel/TransientCollection/any(s:contains(s, 'just'))");
    helper.assertStatus(501);
  }

  @Test
  void testNavigationPropertyToManyValueAnyViaJoinTable() throws IOException, ODataException {
    final JPAODataClaimsProvider provided = new JPAODataClaimsProvider();
    provided.add("UserId", new JPAClaimsPair<>("Marvin"));
    provided.add("RoleCategory", new JPAClaimsPair<>("B"));
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$filter=RolesJoinProtected/all(d:d/RoleCategory eq 'B')", provided);
    helper.assertStatus(200);
  }

  @Test
  void testFilterRestrictionByAnnotation() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AnnotationsParents?$top=1", new JavaBasedCapabilitiesAnnotationsProvider());
    helper.assertStatus(400);
  }

  @Test
  void testStartsWithCompleteness() throws IOException, ODataException {
    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions/$count");
    final var all = helper.getSingleValue().asInt();

    helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions/$count?$filter=not startswith(DivisionCode,'BE')");
    final var notStarts = helper.getSingleValue().asInt();

    helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions/$count?$filter=startswith(DivisionCode,'BE')");
    final var starts = helper.getSingleValue().asInt();

    assertEquals(all, notStarts + starts);
  }

  @Test
  void testStartsWithCompletenessContainingNull() throws IOException, ODataException {
    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions/$count");
    final var all = helper.getSingleValue().asInt();

    helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions/$count?$filter=not startswith(ParentDivisionCode,'BE')");
    final var notStarts = helper.getSingleValue().asInt();

    helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions/$count?$filter=startswith(ParentDivisionCode,'BE')");
    final var starts = helper.getSingleValue().asInt();

    helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions/$count?$filter=ParentDivisionCode eq null");
    final var nullValues = helper.getSingleValue().asInt();
    assertNotEquals(0, nullValues);
    assertEquals(all, notStarts + starts + nullValues);
  }

  @Test
  void testContainsCompleteness() throws IOException, ODataException {
    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions/$count");
    final var all = helper.getSingleValue().asInt();

    helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions/$count?$filter=not contains(DivisionCode,'14')");
    final var notStarts = helper.getSingleValue().asInt();

    helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions/$count?$filter=contains(DivisionCode,'14')");
    final var starts = helper.getSingleValue().asInt();

    assertEquals(all, notStarts + starts);
  }
}
