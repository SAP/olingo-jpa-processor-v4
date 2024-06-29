package com.sap.olingo.jpa.processor.core.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataEtagHelper;
import com.sap.olingo.jpa.processor.core.testmodel.AdministrativeDivision;
import com.sap.olingo.jpa.processor.core.testmodel.Person;
import com.sap.olingo.jpa.processor.core.util.EdmEntityTypeDouble;
import com.sap.olingo.jpa.processor.core.util.TestBase;
import com.sap.olingo.jpa.processor.core.util.TestHelper;
import com.sap.olingo.jpa.processor.core.util.UriHelperDouble;

class JPAEntityResultConverterTest extends TestBase {
  public static final int NO_POSTAL_ADDRESS_FIELDS = 8;
  public static final int NO_ADMIN_INFO_FIELDS = 2;
  private JPAEntityResultConverter cut;
  private List<Object> jpaQueryResult;
  private UriHelperDouble uriHelper;
  private JPAODataEtagHelper etagHelper;

  @BeforeEach
  void setup() throws ODataException {
    helper = new TestHelper(emf, PUNIT_NAME);
    etagHelper = mock(JPAODataEtagHelper.class);
    jpaQueryResult = new ArrayList<>();
    final HashMap<String, String> keyStrings = new HashMap<>();
    keyStrings.put("BE21", "DivisionCode='BE21',CodeID='NUTS2',CodePublisher='Eurostat'");
    keyStrings.put("BE22", "DivisionCode='BE22',CodeID='NUTS2',CodePublisher='Eurostat'");

    uriHelper = new UriHelperDouble();
    uriHelper.setKeyPredicates(keyStrings, "DivisionCode");
    cut = new JPAEntityResultConverter(uriHelper, helper.sd,
        jpaQueryResult, new EdmEntityTypeDouble(nameBuilder, "AdministrativeDivision"), etagHelper);
  }

  @Test
  void checkConvertsEmptyResult() throws ODataApplicationException, SerializerException, URISyntaxException {
    assertNotNull(cut.getResult());
  }

  @Test
  void checkConvertsOneResult() throws ODataApplicationException, SerializerException, URISyntaxException {
    final AdministrativeDivision division = firstResult();

    jpaQueryResult.add(division);

    final EntityCollection act = cut.getResult();
    assertEquals(1, act.getEntities().size());
  }

  @Test
  void checkConvertsTwoResult() throws ODataApplicationException, SerializerException, URISyntaxException {

    jpaQueryResult.add(firstResult());
    jpaQueryResult.add(secondResult());
    final EntityCollection act = cut.getResult();
    assertEquals(2, act.getEntities().size());
  }

  @Test
  void checkConvertsOneResultOneElement() throws ODataApplicationException, SerializerException,
      URISyntaxException {
    final AdministrativeDivision division = firstResult();

    jpaQueryResult.add(division);

    final EntityCollection act = cut.getResult();
    assertEquals(1, act.getEntities().size());
    assertEquals("BE21", act.getEntities().get(0).getProperty("DivisionCode").getValue().toString());

  }

  @Test
  void checkConvertsOneResultMultiElement() throws ODataApplicationException, SerializerException,
      URISyntaxException {
    final AdministrativeDivision division = firstResult();

    jpaQueryResult.add(division);

    final EntityCollection act = cut.getResult();
    assertEquals(1, act.getEntities().size());
    assertEquals("BE21", act.getEntities().get(0).getProperty("DivisionCode").getValue().toString());
    assertEquals("BE2", act.getEntities().get(0).getProperty("ParentDivisionCode").getValue().toString());
    assertEquals("0", act.getEntities().get(0).getProperty("Population").getValue().toString());
  }

  @Test
  void testEtagAdded() throws ODataJPAModelException, SerializerException, ODataApplicationException,
      URISyntaxException {

    final var personEntityType = helper.getJPAEntityType(Person.class);
    final var personEdmType = mock(EdmEntityType.class);
    when(personEdmType.getNamespace()).thenReturn(personEntityType.getExternalFQN().getNamespace());
    when(personEdmType.getName()).thenReturn(personEntityType.getExternalFQN().getName());
    final var result = new Person();
    result.setID("123");
    result.setETag(12);
    final List<Person> results = Arrays.asList(result);
    when(etagHelper.asEtag(any(), eq(12L))).thenReturn("\"12\"");

    cut = new JPAEntityResultConverter(OData.newInstance().createUriHelper(), helper.sd, results, personEdmType,
        etagHelper);
    final var act = cut.getResult();
    assertEquals(1, act.getEntities().size());
    assertEquals("\"12\"", act.getEntities().get(0).getETag());
  }

  AdministrativeDivision firstResult() {
    final AdministrativeDivision division = new AdministrativeDivision();

    division.setCodePublisher("Eurostat");
    division.setCodeID("NUTS2");
    division.setDivisionCode("BE21");
    division.setCountryCode("BEL");
    division.setParentCodeID("NUTS1");
    division.setParentDivisionCode("BE2");
    division.setAlternativeCode("");
    division.setArea(0);
    division.setPopulation(0);
    return division;
  }

  private Object secondResult() {
    final AdministrativeDivision division = new AdministrativeDivision();

    division.setCodePublisher("Eurostat");
    division.setCodeID("NUTS2");
    division.setDivisionCode("BE22");
    division.setCountryCode("BEL");
    division.setParentCodeID("NUTS1");
    division.setParentDivisionCode("BE2");
    division.setAlternativeCode("");
    division.setArea(0);
    division.setPopulation(0);
    return division;
  }
}
