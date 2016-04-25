package org.apache.olingo.jpa.processor.core.filter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.jpa.processor.core.util.IntegrationTestHelper;
import org.apache.olingo.jpa.processor.core.util.TestBase;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;

public class TestJPAQueryWhereClause extends TestBase {

  @Test
  public void testFilterOneEquals() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID eq '3'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("3", orgs.get(0).get("ID").asText());
  }

  @Test
  public void testFilterOneEqualsTwoProperties() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=DivisionCode eq CountryCode");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
  }

  @Test
  public void testFilterOneEqualsInvert() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter='3' eq ID");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("3", orgs.get(0).get("ID").asText());
  }

  @Test
  public void testFilterOneNotEqual() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID ne '3'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(9, orgs.size());
  }

  @Test
  public void testFilterOneGreaterEqualsString() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID ge '5'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(5, orgs.size()); // '10' is smaller than '5' when comparing strings!
  }

  @Test
  public void testFilterOneLowerThanTwoProperties() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=DivisionCode lt CountryCode");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(202, orgs.size());
  }

  @Test
  public void testFilterOneGreaterThanString() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID gt '5'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size()); // '10' is smaller than '5' when comparing strings!
  }

  @Test
  public void testFilterOneLowerThanString() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID lt '5'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(5, orgs.size());
  }

  @Test
  public void testFilterOneLowerEqualsString() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "Organizations?$filter=ID le '5'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(6, orgs.size());
  }

  @Test
  public void testFilterOneGreaterEqualsNumber() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf, "AdministrativeDivisions?$filter=Area ge 119330610");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size());
  }

  @Test
  public void testFilterOneAndEquals() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and CodeID eq 'NUTS2'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(11, orgs.size());
  }

  @Test
  public void testFilterOneOrEquals() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=ID eq '5' or ID eq '10'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  public void testFilterOneNotLower() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=not (Area lt 50000000)");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(24, orgs.size());
  }

  @Test
  public void testFilterTwoAndEquals() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and CodeID eq 'NUTS2' and DivisionCode eq 'BE25'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
    assertEquals("BEL", orgs.get(0).get("CountryCode").asText());
  }

  @Test
  public void testFilterAndOrEqualsParenthesis() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and (DivisionCode eq 'BE25' or  DivisionCode eq 'BE24')&$orderby=DivisionCode desc");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
    assertEquals("BE25", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  public void testFilterAndOrEqualsNoParenthesis() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=CodePublisher eq 'Eurostat' and DivisionCode eq 'BE25' or  CodeID eq '3166-1'&$orderby=DivisionCode desc");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size());
    assertEquals("USA", orgs.get(0).get("DivisionCode").asText());
  }

  @Test
  public void testFilterAddGreater() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area add 7000000 ge 50000000");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(31, orgs.size());
  }

  @Test
  public void testFilterSubGreater() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area sub 7000000 ge 60000000");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(15, orgs.size());
  }

  @Test
  public void testFilterDivGreater() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area gt 0 and Area div Population ge 6000");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(9, orgs.size());
  }

  @Test
  public void testFilterMulGreater() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area mul Population gt 0");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(64, orgs.size());
  }

  @Test
  public void testFilterMod() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Area gt 0 and Area mod 3578335 eq 0");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  public void testFilterLength() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=length(Name) eq 10");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(9, orgs.size());
//    for (JsonNode n : orgs) {
//      System.out.println(n.get("Name").asText());
//    }
  }

  @Test
  public void testFilterNow() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=AdministrativeInformation/Created/At lt now()");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  public void testFilterContains() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=contains(CodeID,'166')");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(83, orgs.size());
  }

  @Test
  public void testFilterEndswith() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=endswith(CodeID,'166-1')");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
  }

  @Test
  public void testFilterStartswith() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=startswith(DivisionCode,'DE-')");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(16, orgs.size());
  }

  @Test
  public void testFilterIndexOf() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=indexof(DivisionCode,'3') eq 4");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(7, orgs.size());
  }

  @Test
  public void testFilterSubstringStartIndex() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and substring(Name,7) eq 'Dakota'");
    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  public void testFilterSubstringStartEndIndex() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and substring(Name,0,5) eq 'North'");

    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }

  @Test
  public void testFilterSubstringToLower() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and tolower(Name) eq 'brandenburg'");

    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  public void testFilterSubstringToUpper() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and toupper(Name) eq 'HESSEN'");

    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Ignore
  @Test
  public void testFilterSubstringToUpperInvers() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=toupper('nuts1') eq CodeID");

    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  public void testFilterTrim() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and trim(Name) eq 'Sachsen'");

    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  public void testFilterConcat() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Persons?$filter=concat(concat(LastName,','),FirstName) eq 'Mustermann,Max'");

    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  public void testFilterNavigationPropertyToManyValueAny() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=Roles/any(d:d/RoleCategory eq 'A')");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(3, orgs.size());
  }

  @Test
  public void testFilterNavigationPropertyToManyValueAnyMultiParameter() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=Roles/any(d:d/RoleCategory eq 'A' and d/BusinessPartnerID eq '1')");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  public void testFilterNavigationPropertyToManyValueAnyNoRestriction() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=Roles/any()");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(4, orgs.size());
  }

  @Test
  public void testFilterNavigationPropertyToManyValueAll() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=Roles/all(d:d/RoleCategory eq 'A')");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Ignore
  @Test
  public void testFilterNavigationPropertyToManyValueAllNoRestriction() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=Roles/all()");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  public void testFilterCountNavigationProperty() throws IOException, ODataException {
//https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398301
//Example 43: return all Categories with less than 10 products    
    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=Roles/$count eq 2");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  }

  @Test
  public void testFilterCountNavigationPropertyMultipleHops() throws IOException, ODataException {
    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$select=ID&$filter=AdministrativeInformation/Created/User/Roles/$count ge 2");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(8, orgs.size());
  }

  @Test
  public void testFilterNavigationPropertyToOneValue() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Parent/CodeID eq 'NUTS1'");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(11, orgs.size());
  }

  @Test
  public void testFilterNavigationPropertyToOneValueAndEquals() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Parent/CodeID eq 'NUTS1' and DivisionCode eq 'BE34'");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  };

  @Test
  public void testFilterNavigationPropertyToOneValueTwoHops() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisions?$filter=Parent/Parent/CodeID eq 'NUTS1' and DivisionCode eq 'BE212'");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(1, orgs.size());
  };

  @Test
  public void testFilterNavigationPropertyToOneValueViaComplexType() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "Organizations?$filter=AdministrativeInformation/Created/User/LastName eq 'Mustermann'");

    helper.assertStatus(200);
    ArrayNode orgs = helper.getValues();
    assertEquals(8, orgs.size());
  };

  @Test
  public void testFilterSubstringStartEndIndexToLower() throws IOException, ODataException {

    IntegrationTestHelper helper = new IntegrationTestHelper(emf,
        "AdministrativeDivisionDescriptions?$filter=Language eq 'de' and tolower(substring(Name,0,5)) eq 'north'");

    helper.assertStatus(200);

    ArrayNode orgs = helper.getValues();
    assertEquals(2, orgs.size());
  }
}
