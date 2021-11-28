package com.sap.olingo.jpa.processor.core.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.olingo.jpa.processor.core.api.JPAClaimsPair;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimsProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupsProvider;
import com.sap.olingo.jpa.processor.core.util.IntegrationTestHelper;
import com.sap.olingo.jpa.processor.core.util.TestBase;

class TestJPAQueryWhereClause extends TestBase {

  @Test
  void testFilterOneEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=ID eq '3'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("3", orgs.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneEqualsDateTime() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=CreationDateTime eq 2016-01-20T09:21:23Z");
    helper.assertStatus(200);
    // This test shall ensure that the Date Time value is mapped correct.
    // Unfortunately the query returns an empty result locally, but 10 rows on Jenkins
    final ArrayNode orgs = helper.getValues();
    assertNotNull(orgs);
  }

  @Test
  void testFilterOneDescriptionEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=LocationName eq 'Deutschland'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("10", orgs.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneDescriptionEqualsFieldNotSelected() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=LocationName eq 'Deutschland'&$select=ID");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("10", orgs.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneEnumEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=ABCClass eq com.sap.olingo.jpa.ABCClassification'A'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("1", orgs.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneEqualsTwoProperties() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=DivisionCode eq CountryCode");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size());
  }

  @Test
  void testFilterOneEqualsInvert() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter='3' eq ID");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("3", orgs.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneNotEqual() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID ne '3'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(9, orgs.size());
  }

  @Test
  void testFilterOneEnumNotEqual() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=AccessRights ne com.sap.olingo.jpa.AccessRights'Write'");
    helper.assertStatus(200);

    final ArrayNode persons = helper.getValues();
    assertEquals(1, persons.size());
    assertEquals("97", persons.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneEnumEqualMultipleValues() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=AccessRights eq com.sap.olingo.jpa.AccessRights'Read,Delete'");
    helper.assertStatus(200);

    final ArrayNode persons = helper.getValues();
    assertEquals(1, persons.size());
    assertEquals("97", persons.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneGreaterEqualsString() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID ge '5'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(5, orgs.size()); // '10' is smaller than '5' when comparing strings!
  }

  @Test
  void testFilterOneLowerThanTwoProperties() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=DivisionCode lt CountryCode");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(244, orgs.size());
  }

  @Test
  void testFilterOneGreaterThanString() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID gt '5'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size()); // '10' is smaller than '5' when comparing strings!
  }

  @Test
  void testFilterOneLowerThanString() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID lt '5'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(5, orgs.size());
  }

  @Test
  void testFilterOneLowerEqualsString() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID le '5'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(6, orgs.size());
  }

  @Test
  void testFilterOneGreaterEqualsNumber() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area ge 119330610");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size());
  }

  @Disabled("Clarify if GT, LE .. not supported by OData or \"only\" by Olingo")
  @Test
  void testFilterOneEnumGreaterThan() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=AccessRights gt com.sap.olingo.jpa.AccessRights'Read'");
    helper.assertStatus(200);

    final ArrayNode persons = helper.getValues();
    assertEquals(1, persons.size());
    assertEquals("99", persons.get(0).get("ID").asText());
  }

  @Test
  void testFilterOneAndEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and CodeID eq 'NUTS2'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(11, orgs.size());
  }

  @Test
  void testFilterOneOrEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=ID eq '5' or ID eq '10'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  void testFilterOneNotLower() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=not (Area lt 50000000)");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(24, orgs.size());
  }

  @Test
  void testFilterTwoAndEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and CodeID eq 'NUTS2' and DivisionCode eq 'BE25'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("BEL", orgs.get(0).get("CountryCode").asText());
  }

  @Test
  void testFilterAndOrEqualsParenthesis() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and (DivisionCode eq 'BE25' or  DivisionCode eq 'BE24')&$orderby=DivisionCode desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
    assertEquals("BE25", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterAndOrEqualsNoParenthesis() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and DivisionCode eq 'BE25' or  CodeID eq '3166-1'&$orderby=DivisionCode desc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(5, orgs.size());
    assertEquals("USA", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterAndWithFunction1() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and contains(tolower(DivisionCode),tolower('BE1'))&$orderby=DivisionCode asc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
    assertEquals("BE1", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterAndWithFunction2() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and contains(DivisionCode,'BE1')&$orderby=DivisionCode asc");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
    assertEquals("BE1", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterAndWithComparisonContainingFunction() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and tolower(DivisionCode) eq tolower('BE1')");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("BE1", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  void testFilterhComparisonViaNavigationContainingFunction() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$filter=tolower(Organization/Name1) eq 'third org.'");
    helper.assertStatus(200);
    final ArrayNode act = helper.getValues();
    assertEquals(3, act.size());
  }

  @Test
  void testFilterhComparisonTwoFunctionsContainingNavigationNotSupported() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$filter=tolower(Organization/Name1) eq tolower(Organization/Name2)");
    helper.assertStatus(501);
  }

  @Test
  void testFilterhComparisonViaNavigationContainingNestedFunctionNotSupported() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoles?$filter=contains(tolower(Organization/Name1), 'third org.')");
    helper.assertStatus(501);
  }

  @Test
  void testFilterAddGreater() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area add 7000000 ge 50000000");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(31, orgs.size());
  }

  @Test
  void testFilterSubGreater() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area sub 7000000 ge 60000000");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(15, orgs.size());
  }

  @Test
  void testFilterDivGreater() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area gt 0 and Area div Population ge 6000");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(9, orgs.size());
  }

  @Test
  void testFilterMulGreater() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area mul Population gt 0");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(64, orgs.size());
  }

  @Test
  void testFilterMod() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area gt 0 and Area mod 3578335 eq 0");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  void testFilterLength() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=length(Name) eq 10");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(11, orgs.size());
  }

  @Test
  void testFilterNow() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=AdministrativeInformation/Created/At lt now()");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
  }

  @Test
  void testFilterContains() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=contains(CodeID,'166')");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(110, orgs.size());
  }

  @Test
  void testFilterEndswith() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=endswith(CodeID,'166-1')");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size());
  }

  @Test
  void testFilterStartswith() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=startswith(DivisionCode,'DE-')");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(16, orgs.size());
  }

  @Test
  void testFilterIndexOf() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=indexof(DivisionCode,'3') eq 4");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(7, orgs.size());
  }

  @Test
  void testFilterSubstringStartIndex() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and substring(Name,6) eq 'Dakota'");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  void testFilterSubstringStartEndIndex() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and substring(Name,0,5) eq 'North'");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  void testFilterSubstringLengthCalculated() throws IOException, ODataException {
    // substring(CompanyName, 1 add 4, 2 mul 3)
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and substring(Name,0,1 add 4) eq 'North'");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  // Usage of mult currently creates parser error: The types 'Edm.Double' and '[Int64, Int32, Int16, Byte, SByte]' are
  // not compatible.
  @Disabled("Usage of mult currently creates parser error")
  @Test
  void testFilterSubstringStartCalculated() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and substring(Name,2 mul 3) eq 'Dakota'");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  void testFilterToLower() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and tolower(Name) eq 'brandenburg'");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  void testFilterToUpper() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and toupper(Name) eq 'HESSEN'");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  void testFilterToUpperInverse() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=toupper('nuts1') eq CodeID");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(19, orgs.size());
  }

  @Test
  void testFilterTrim() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and trim(Name) eq 'Sachsen'");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  void testFilterConcat() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=concat(concat(LastName,','),FirstName) eq 'Mustermann,Max'");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  void testFilterNavigationPropertyToManyValueAny() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=Roles/any(d:d/RoleCategory eq 'A')");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
  }

  @Test
  void testFilterNavigationPropertyToManyValueAnyProtected() throws IOException, ODataException {
    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerProtecteds?$filter=Roles/any(d:d/RoleCategory eq 'X')", claims);

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
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
  void testFilterNavigationPropertyToManyValueNotAny() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=not (Roles/any(d:d/RoleCategory eq 'A'))");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(7, orgs.size());
  }

  @Test
  void testFilterNavigationPropertyToManyValueAnyMultiParameter() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=Roles/any(d:d/RoleCategory eq 'A' and d/BusinessPartnerID eq '1')");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  void testFilterNavigationPropertyToManyValueAnyNoRestriction() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=Roles/any()");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size());
  }

  @Test
  void testFilterNavigationStartsWithAll() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=InhouseAddress/all(d:startswith(d/TaskID, 'D'))");

    helper.assertStatus(200);
    final ArrayNode pers = helper.getValues();
    assertEquals(1, pers.size());
    assertEquals("97", pers.get(0).get("ID").asText());
  }

  @Test
  void testFilterNavigationPropertyToManyValueAll() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=Roles/all(d:d/RoleCategory eq 'A')");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  void testFilterCountNavigationProperty() throws IOException, ODataException {
    // https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398301
    // Example 43: return all Categories with less than 10 products
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=Roles/$count eq 2");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
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
  void testFilterCountNavigationPropertyMultipleHops() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=AdministrativeInformation/Created/User/Roles/$count ge 2");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(8, orgs.size());
  }

  @Test
  void testFilterNavigationPropertyToOneValue() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Parent/CodeID eq 'NUTS1'");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(11, orgs.size());
  }

  @Test
  void testFilterNavigationPropertyToOneValueAndEquals() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Parent/CodeID eq 'NUTS1' and DivisionCode eq 'BE34'");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  };

  @Test
  void testFilterNavigationPropertyToOneValueTwoHops() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Parent/Parent/CodeID eq 'NUTS1' and DivisionCode eq 'BE212'");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  };

  @Test
  void testFilterNavigationPropertyToOneValueViaComplexType() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=AdministrativeInformation/Created/User/LastName eq 'Mustermann'");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(8, orgs.size());
  };

  @Test
  void testFilterNavigationPropertyDescriptionViaComplexTypeWOSubselectSelectAll() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=Address/RegionName eq 'Kalifornien'");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
  };

  @Test
  void testFilterNavigationPropertyDescriptionViaComplexTypeWOSubselectSelectId() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=Address/RegionName eq 'Kalifornien'&$select=ID");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
  };

  @Test
  void testFilterNavigationPropertyDescriptionToOneValueViaComplexTypeWSubselect1() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=AdministrativeInformation/Created/User/LocationName eq 'Schweiz'");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  };

  @Test
  void testFilterNavigationPropertyContainsProtectedDeep() throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("*"));
    claims.add("RoleCategory", new JPAClaimsPair<>("Z"));

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoleProtecteds?$filter=contains(BupaPartnerProtected/Name1, 'o')", claims);

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(0, orgs.size());
  };

  @Test
  void testFilterNavigationPropertyEqualsProtectedDeep() throws IOException, ODataException {

    final JPAODataClaimsProvider claims = new JPAODataClaimsProvider();
    claims.add("UserId", new JPAClaimsPair<>("Willi"));
    claims.add("RoleCategory", new JPAClaimsPair<>("*"));

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerRoleProtecteds?$filter=BupaPartnerProtected/Type eq '1'", claims);

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
  };

  @Test
  void testFilterNavigationPropertyDescriptionToOneValueViaComplexTypeWSubselect2() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=AdministrativeInformation/Created/User/LocationName eq 'Schweiz'&$select=ID");

    helper.assertStatus(200);
    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  };

  @Test
  void testFilterNavigationPropertyAndExandThatNavigationProperty() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Parent/DivisionCode eq 'BE2'&$expand=Parent");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(5, admin.size());
    assertNotNull(admin.get(3).findValue("Parent"));
    assertFalse(admin.get(3).findValue("Parent") instanceof NullNode);
    assertEquals("BE2", admin.get(3).findValue("Parent").get("DivisionCode").asText());
  };

  @Test
  void testFilterNavigationPropertyViaJoinTableSubtype() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=ID&$filter=SupportedOrganizations/any()");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(2, admin.size());
    assertEquals("98", admin.get(0).findValue("ID").asText());

  };

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

  };

  @Test
  void testFilterMappedNavigationPropertyViaJoinTableSubtype() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=Name1&$filter=SupportEngineers/any(d:d/LastName eq 'Doe')");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(1, admin.size());
    assertEquals("First Org.", admin.get(0).findValue("Name1").asText());

  };

  @Test
  void testFilterNavigationPropertyViaJoinTableCount() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=Teams/$count eq 0&$select=ID");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(1, admin.size());
    assertEquals("98", admin.get(0).findValue("ID").asText());

  };

  @Test
  void testFilterMappedNavigationPropertyViaJoinTableFilter() throws IOException,
      ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Teams?$select=Name&$filter=Member/any(d:d/LastName eq 'Mustermann')");

    helper.assertStatus(200);
    final ArrayNode admin = helper.getValues();
    assertEquals(2, admin.size());
  };

  @Test
  void testFilterWithAllExpand() throws ODataException, IOException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=Name1 eq 'Third Org.'&$expand=Roles");

    helper.assertStatus(200);
    final ArrayNode org = helper.getValues();
    assertNotNull(org);
    assertEquals(1, org.size());
    assertEquals(3, org.get(0).get("Roles").size());
  }

  @Test
  void testFilterSubstringStartEndIndexToLower() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and tolower(substring(Name,0,5)) eq 'north'");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  void testFilterOneHas() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=AccessRights has com.sap.olingo.jpa.AccessRights'Read'");

    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  void testFilterNavigationTarget() throws IOException, ODataException {
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions(DivisionCode='BE2',CodeID='NUTS1',CodePublisher='Eurostat')/Children?$filter=DivisionCode eq 'BE21'");
    helper.assertStatus(200);

    final ObjectNode div = helper.getValue();
    final ObjectNode result = (ObjectNode) div.get("value").get(0);
    assertNotNull(result);
    assertEquals("BE21", result.get("DivisionCode").asText());
  }

  @Test
  void testFilterCollectionSinplePropertyThrowsError() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=contains(Comment, 'just')");

    helper.assertStatus(400); // Olingo rejects a bunch of functions.
  }

  @Test
  void testFilterCollectionPropertyAny() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=Comment/any(s:contains(s, 'just'))");

    helper.assertStatus(200);
    final ArrayNode org = helper.getValues();
    assertNotNull(org);
    assertEquals(1, org.size());
  }

  @Test
  void testFilterCollectionPropertySimpleCount() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=InhouseAddress/$count eq 2");

    helper.assertStatus(200);
    final ArrayNode deep = helper.getValues();
    assertNotNull(deep);
    assertEquals(1, deep.size());
  }

  @Test
  void testFilterCollectionPropertyDeepSimpleCount() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps?$filter=FirstLevel/SecondLevel/Comment/$count eq 2&$select=ID");

    helper.assertStatus(200);
    final ArrayNode deep = helper.getValues();
    assertNotNull(deep);
    assertEquals(1, deep.size());
  }

  @Test
  void testFilterCollectionPropertyDeepComplexCount() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps?$filter=FirstLevel/SecondLevel/Address/$count eq 2&$select=ID");

    helper.assertStatus(200);
    final ArrayNode deep = helper.getValues();
    assertNotNull(deep);
    assertEquals(1, deep.size());
  }

  @Test
  void testFilterCollectionPropertyAsPartOfComplexAny() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps?$filter=FirstLevel/SecondLevel/Address/any(s:s/TaskID eq 'DEV')");

    helper.assertStatus(200);
    final ArrayNode org = helper.getValues();
    assertNotNull(org);
    assertEquals(1, org.size());
  }

  @Test
  void testFilterCollectionPropertyAsPartOfComplexWithSelect() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "CollectionDeeps?$select=FirstLevel/TransientCollection&$filter=FirstLevel/SecondLevel/Address/any(s:s/TaskID eq 'DEV')");
    helper.assertStatus(200);
    final ArrayNode org = helper.getValues();
    assertNotNull(org);
    assertEquals(1, org.size());
  }

  @Test
  void testFilterCollectionOnPropertyWithNavigation() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons('99')/InhouseAddress?$filter=TaskID eq 'DEV'");

    helper.assertStatus(200);
    final ArrayNode addr = helper.getValues();
    assertNotNull(addr);
    assertEquals(1, addr.size());
  }

  @Test
  void testFilterCollectionPropertyWithOutNavigationThrowsError() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$select=ID&$filter=InhouseAddress/TaskID eq 'DEV'");

    helper.assertStatus(400); // The URI is malformed
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
        final ArrayNode inhouse = (ArrayNode) bupa.get("InhouseAddress");
        assertFalse(inhouse.isNull());
        assertEquals(2, inhouse.size());
      }
    }
  }

  @Test
  void testFilterNavigationPropertyRequiresGroupsReturnsForbidden() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$select=ID&$filter=Roles/any(d:d/Details eq 'A')");
    helper.assertStatus(403);
  }

  @Test
  void testFilterNavigationPropertyRequiresGroupsProvided() throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Company");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$select=ID&$filter=Roles/any(d:d/Details eq 'A')", groups);
    helper.assertStatus(200);
  }

  @Test
  void testFilterCollectionPropertyRequiresGroupsReturnsForbidden() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$select=ID&$filter=Comment/any(s:contains(s, 'just'))");
    helper.assertStatus(403);
  }

  @Test
  void testFilterCollectionPropertyRequiresGroupsProvided() throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Company");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss?$select=ID&$filter=Comment/any(s:contains(s, 'just'))", groups);
    helper.assertStatus(200);
  }

  @Test
  void testFilterCollectionProtectedPropertyRequiresGroupsReturnsForbidden() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss('99')/InhouseAddress?$filter=RoomNumber eq 1");
    helper.assertStatus(403);
  }

  @Test
  void testFilterCollectionProtectedPropertyRequiresGroupsProvided() throws IOException, ODataException {

    final JPAODataGroupsProvider groups = new JPAODataGroupsProvider();
    groups.addGroup("Company");
    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "BusinessPartnerWithGroupss('99')/InhouseAddress?$filter=RoomNumber eq 1", groups);
    helper.assertStatus(200);
  }

  @Test
  void testFilterOnNull() throws IOException, ODataException {

    final IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'ISO' and ParentCodeID eq null");
    helper.assertStatus(200);

    final ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size());
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
}
